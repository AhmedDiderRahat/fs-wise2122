package com.example.password_manager.activity

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.es_job_manager.utilities.ConfigurationConstant
import com.example.password_manager.R
import com.example.password_manager.adapter.StoreDataAdapter
import com.example.password_manager.beans.StoreData

import com.example.password_manager.databinding.ActivityLandingPageBinding
import com.example.password_manager.utilities.AESEncryption

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/*
* Splash Screen of the app. Execute for three second
* Ahmed Dider Rahat- 4 th Feb 2022
*/

class LandingPage : AppCompatActivity() {
    private val TAG = "LandingScreen"

    lateinit var binding: ActivityLandingPageBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var AES_KEY: String
    private val userCollectionRef = Firebase.firestore.collection("all_data_storage")
    lateinit var editor: SharedPreferences.Editor
    lateinit var USER_ID: String

    lateinit var recyclerView: RecyclerView
    lateinit var dataList: ArrayList<StoreData>
    lateinit var dataAdapter: StoreDataAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        sharedPreferences = getSharedPreferences(ConfigurationConstant.LOGIN_PREFERENCE, MODE_PRIVATE)
        editor = sharedPreferences.edit()
        AES_KEY = sharedPreferences.getString(ConfigurationConstant.CRYPTO_KEY, "").toString()
        USER_ID = sharedPreferences.getString(ConfigurationConstant.USER_ID, "").toString()

        // setup recycler view
        recyclerView = binding.rvAllData
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        dataList = arrayListOf()

        dataAdapter = StoreDataAdapter(dataList)
        recyclerView.adapter = dataAdapter

        // load user data
        loadUserName()

        binding.btnLogout.setOnClickListener {
            showDialog()
        }

        binding.btnAddNew.setOnClickListener {
            addNewPageInit()
        }

        loadAppData()
    }

    // retrieve from fire-store
    private fun loadAppData() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = userCollectionRef
                .whereEqualTo(ConfigurationConstant.UID, USER_ID)
                .get().await()

            Log.d(TAG, "loadAppData: ${querySnapshot.size()}")

            withContext(Dispatchers.Main){
                for (document in querySnapshot.documents){
                    val storeData = document.toObject<StoreData>()
                    //if (storeData != null) {
                    if (storeData != null) {
                        dataList.add(storeData)
                    }
                    //}
                    //Log.d(TAG, "loadAppData: ${document.data}")
                    //Log.d(TAG, "loadAppData: ${storeData.toString()}")
                    //document.toObject(StoreData::class.java)?.let { dataList.add(it) }
                }

                Log.d(TAG, "loadAppData: ${dataList.size}")

                dataAdapter.notifyDataSetChanged()
            }
        }catch (e: Exception){
            Log.e(TAG, "loadAppDataError: ${e.toString()}")
        }
    }

    // show a dialog for logout the user
    private fun showDialog() {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        logoutProcess()
                        dialog.dismiss()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                    }
                }
            }

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Want to Logout?").setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()
    }

    // Process all logout procedure
    private fun logoutProcess() {
        // unset all session data
        editor.apply {
            putString(ConfigurationConstant.USER_ID, "")
            putBoolean(ConfigurationConstant.LOGIN_STATUS, false)
            apply()
        }
        // redirect to login page
        loginPageInit()
    }

    // load user name on the top left corner of the page
    private fun loadUserName() {
        try {
            var emailCipher = sharedPreferences.getString(ConfigurationConstant.USER_ID, "")
            
            var key = sharedPreferences.getString(ConfigurationConstant.CRYPTO_KEY, "")
            var decryptionObject = AESEncryption()

            Log.d(TAG, "loadUserName: $key, $emailCipher")

            var email = emailCipher?.let { decryptionObject.decryption(AES_KEY, it) }
            Log.d(TAG, "loadUserName: $email")
            
            binding.tvUserName.text = email.toString()
        }catch (e: Exception){
            Log.e(TAG, "loadUserNameError: ${toString()}")
        }
    }

    //go-to login page
    private fun loginPageInit(){
        val intent = Intent(this, LogIn::class.java)
        startActivity(intent)
        finish()
    }

    //go-to add new page page
    private fun addNewPageInit(){
        val intent = Intent(this, AddNewScreen::class.java)
        startActivity(intent)
    }
}