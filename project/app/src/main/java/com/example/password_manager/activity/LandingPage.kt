package com.example.password_manager.activity

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.es_job_manager.utilities.ConfigurationConstant
import com.example.password_manager.R
import com.example.password_manager.databinding.ActivityLandingPageBinding
import com.example.password_manager.utilities.AESEncryption
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/*
* Splash Screen of the app. Execute for three second
* Ahmed Dider Rahat- 4 th Feb 2022
*/

class LandingPage : AppCompatActivity() {
    private val TAG = "LandingScreen"

    lateinit var binding: ActivityLandingPageBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var AES_KEY: String
    private val userCollectionRef = Firebase.firestore.collection("user_data")
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing_page)

        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        sharedPreferences = getSharedPreferences(ConfigurationConstant.LOGIN_PREFERENCE, MODE_PRIVATE)
        editor = sharedPreferences.edit()
        AES_KEY = sharedPreferences.getString(ConfigurationConstant.CRYPTO_KEY, "").toString()

        // load user data
        loadUserName()


        binding.btnLogout.setOnClickListener {
            showDialog()
        }

        binding.btnAddNew.setOnClickListener {
            addNewPageInit()
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
    private fun logoutProcess(){
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