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
import com.example.password_manager.dsl.UserNamePrediction
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
* Landing Page of the app.
* Ahmed Dider Rahat- 4 th Feb 2022
*/

class LandingPage : AppCompatActivity(), StoreDataAdapter.OnItemClickListener {
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
    lateinit var cryptoObj: AESEncryption

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        sharedPreferences =
            getSharedPreferences(ConfigurationConstant.LOGIN_PREFERENCE, MODE_PRIVATE)
        editor = sharedPreferences.edit()
        AES_KEY = sharedPreferences.getString(ConfigurationConstant.CRYPTO_KEY, "").toString()
        USER_ID = sharedPreferences.getString(ConfigurationConstant.USER_ID, "").toString()

        // setup recycler view
        recyclerView = binding.rvAllData
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        dataList = arrayListOf()

        dataAdapter = StoreDataAdapter(dataList, this)
        recyclerView.adapter = dataAdapter

        cryptoObj = AESEncryption()

        // load user data
        loadUserName()

        binding.btnLogout.setOnClickListener {
            showDialog("logout", -1)
        }

        binding.btnAddNew.setOnClickListener {
            addNewPageInit(true, "", "", "")
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

            withContext(Dispatchers.Main) {
                for (document in querySnapshot.documents) {
                    val storeData = document.toObject<StoreData>()
                    if (storeData != null) {
                        dataList.add(storeData)
                    }
                }

                Log.d(TAG, "loadAppData: ${dataList.size}")

                dataAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadAppDataError: ${e.toString()}")
        }
    }

    // show a dialog for logout the user
    private fun showDialog(type: String, index: Int) {
        val dialogClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {

                        if (type.equals("logout")) {
                            logoutProcess()
                        } else {
                            deleteData(index)
                        }

                        dialog.dismiss()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                    }
                }
            }

        val title = if (type.equals("logout")) "Want to Logout?" else "Want to delete?"

        val builder = AlertDialog.Builder(this)
        builder.setMessage(title).setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()
    }

    // process delete item
    private fun deleteData(index: Int) = CoroutineScope(Dispatchers.IO).launch {
        var storeData: StoreData = dataList.get(index)
        val dataQuery = userCollectionRef
            .whereEqualTo(ConfigurationConstant.UID, USER_ID)
            .whereEqualTo(ConfigurationConstant.SITE, storeData.site_name)
            .get().await()

        if (dataQuery.documents.isNotEmpty()) {
            for (document in dataQuery) {
                try {
                    userCollectionRef.document(document.id).delete().await()
                    dataList.remove(storeData)

                    withContext(Dispatchers.Main) {
                        dataAdapter.notifyDataSetChanged()
                        Toast.makeText(
                            this@LandingPage,
                            "Successfully deleted!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "deleteData: ${e.toString()}")
                }
            }
        }
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

            var userNamePrediction = email?.let { UserNamePrediction(it) }
            if (userNamePrediction != null) {
                Log.d(TAG, "loadUserNameRating: ${userNamePrediction.getNameRating()}")
            }


            binding.tvUserName.text = email.toString()
        } catch (e: Exception) {
            Log.e(TAG, "loadUserNameError: ${toString()}")
        }
    }

    //go-to login page
    private fun loginPageInit() {
        val intent = Intent(this, LogIn::class.java)
        startActivity(intent)
        finish()
    }

    //go-to add new page page
    private fun addNewPageInit(
        isEditEnable: Boolean,
        site: String,
        u_name: String,
        password: String
    ) {
        val intent = Intent(this, AddNewScreen::class.java)

        intent.putExtra(ConfigurationConstant.B_IS_EDIT, isEditEnable)
        intent.putExtra(ConfigurationConstant.B_SITE, site)
        intent.putExtra(ConfigurationConstant.B_UNAME, u_name)
        intent.putExtra(ConfigurationConstant.B_PASSWORD, password)
        startActivity(intent)
    }

    // method from interface: pass the data to delete
    override fun onDeleteClick(storeData: StoreData) {
        var index = dataList.indexOf(storeData)
        showDialog("delete", index)
    }

    // method from interface: pass the data to show
    override fun onViewClick(sd: StoreData) {

        var plain_name = sd.user_name?.let { cryptoObj.decryption(AES_KEY, it) }
        var plain_password = sd.password?.let { cryptoObj.decryption(AES_KEY, it) }

        if (plain_password != null) {
            sd.site_name?.let {
                if (plain_name != null) {
                    addNewPageInit(false, it, plain_name, plain_password)
                }
            }
        }
    }
}