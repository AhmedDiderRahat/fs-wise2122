package com.example.password_manager.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.es_job_manager.utilities.ConfigurationConstant
import com.example.password_manager.R
import com.example.password_manager.beans.StoreData
import com.example.password_manager.databinding.ActivityAddNewBinding
import com.example.password_manager.utilities.AESEncryption
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

/*
* Sign-up process of the app.
* Ahmed Dider Rahat- 4th Feb 2022
*/

class AddNewScreen : AppCompatActivity() {

    val TAG = "AddNewScreen"

    lateinit var binding: ActivityAddNewBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var AES_KEY: String
    private val storageCollectionRef = Firebase.firestore.collection("all_data_storage")
    lateinit var editor: SharedPreferences.Editor
    lateinit var USER_ID: String
    lateinit var encryptionObj: AESEncryption
    lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNewBinding.inflate(layoutInflater)

        sharedPreferences = getSharedPreferences(ConfigurationConstant.LOGIN_PREFERENCE, MODE_PRIVATE)
        editor = sharedPreferences.edit()
        AES_KEY = sharedPreferences.getString(ConfigurationConstant.CRYPTO_KEY, "").toString()
        USER_ID = sharedPreferences.getString(ConfigurationConstant.USER_ID, "").toString()

        encryptionObj = AESEncryption()

        view = binding.root
        setContentView(view)

        binding.ivPasswordHideShow.setOnClickListener {
            if (binding.ivPasswordHideShow.getTag().toString().equals("hide")) {
                binding.ivPasswordHideShow.setTag("show")
                binding.etPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()

                binding.ivPasswordHideShow.setImageResource(R.drawable.show_password)
            } else {
                binding.ivPasswordHideShow.setTag("hide")
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()

                binding.ivPasswordHideShow.setImageResource(R.drawable.hide_password)
            }

            binding.etPassword.setSelection(binding.etPassword.length())
        }

        // back button implementation
        binding.btnBack.setOnClickListener {
            landingPageInit()
        }

        // add button implementation
        binding.btnAdd.setOnClickListener {
            var site = binding.etSiteName.text
            var user_name = binding.etUserName.text
            var password = binding.etPassword.text

            try {
                var nameCipher = encryptionObj.encryption(AES_KEY, user_name.toString())
                var passwordCipher = encryptionObj.encryption(AES_KEY, password.toString())

                val storageData = StoreData(USER_ID, site.toString(), nameCipher, passwordCipher)
                saveUserData(storageData)
            }catch (e: Exception){
                Log.e(TAG, "onCreate: ${e.toString()}")
            }
        }
    }

    // if data add successfully then reset all input field
    private fun resetInputField(){
        binding.etSiteName.setText("")
        binding.etUserName.setText("")
        binding.etPassword.setText("")

        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.getWindowToken(), 0)
    }

    // store user sign-in data to firestore
    private fun saveUserData(storageData: StoreData) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d(TAG, "saveUserData: SignIn successful")
            storageCollectionRef.add(storageData).await()
            withContext(Dispatchers.Main){
                resetInputField()
                Toast.makeText(this@AddNewScreen, "Successfully sign in", Toast.LENGTH_SHORT).show()
            }
        }catch (e:Exception){
            Log.d(TAG, "saveUserData: SignIn ERROR: ${e.toString()}")
            withContext(Dispatchers.Main){
                Toast.makeText(this@AddNewScreen, "Internal Server Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //go-to landing page
    private fun landingPageInit(){
        val intent = Intent(this, LandingPage::class.java)
        startActivity(intent)
        finish()
    }
}