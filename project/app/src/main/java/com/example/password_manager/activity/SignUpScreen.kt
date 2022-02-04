package com.example.password_manager.activity

import android.content.Intent
import android.content.SharedPreferences

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.es_job_manager.utilities.ConfigurationConstant
import com.example.password_manager.R
import com.example.password_manager.beans.UserData
import com.example.password_manager.databinding.ActivitySignUpBinding
import com.example.password_manager.utilities.AESUtils
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

class SignUpScreen : AppCompatActivity() {

    val TAG = "SignUpScreen"

    lateinit var binding: ActivitySignUpBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var AES_KEY: String
    private val userCollectionRef = Firebase.firestore.collection("user_data")
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)

        sharedPreferences = getSharedPreferences(ConfigurationConstant.LOGIN_PREFERENCE, MODE_PRIVATE)
        editor = sharedPreferences.edit()
        AES_KEY = sharedPreferences.getString(ConfigurationConstant.CRYPTO_KEY, "").toString()

        val view: View = binding.root
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

        binding.btnSignup.setOnClickListener {
            var first_name = binding.etFirstName.text.toString()
            var last_name = binding.etLastName.text.toString()
            val email = binding.etEmail.text.toString();
            val password = binding.etPassword.text.toString()

            binding.etFirstName.setError(null)
            binding.etEmail.setError(null)
            binding.etPassword.setError(null)

            if (first_name.equals("")) {
                binding.etFirstName.setError("Enter First Name")
            } else if (email.equals("")) {
                binding.etEmail.setError("Enter Email")
            } else if (password.equals("")) {
                binding.etPassword.setError("Enter Passwrod")
            } else {
                var encryptionObject = AESUtils()

                var emailCipher = encryptionObject.cipherEncrypt(AES_KEY, email)
                var passwordCipher = encryptionObject.cipherEncrypt(AES_KEY, password)

                val userData =
                    passwordCipher?.let { it1 -> emailCipher?.let { it2 ->
                        UserData(first_name, last_name,
                            it2, it1)
                    } }
                userData?.let { it1 -> checkAlreadySignIn(it1) }
            }
        }
    }

    // check the used already exist or not
    private fun checkAlreadySignIn(userData: UserData) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d(TAG, "checkAlreadySignIn: ${userData.email}")
            val querySnapshot = userCollectionRef.whereEqualTo(ConfigurationConstant.USER_ID, userData.email)
                .get().await()

            if (querySnapshot.size() > 0){
                Log.d(TAG, "checkAlreadySignIn: Total data= $querySnapshot.size()")
                withContext(Dispatchers.Main){
                    Toast.makeText(this@SignUpScreen, "User Already Exist", Toast.LENGTH_SHORT).show()
                }
            }else{
                Log.d(TAG, "checkAlreadySignIn: saving start... ")
                withContext(Dispatchers.Main){
                    saveUserData(userData)
                }
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@SignUpScreen, "Internal Server Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // store user sign-in data to firestore
    private fun saveUserData(userData: UserData) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d(TAG, "saveUserData: SignIn successful")
            userCollectionRef.add(userData).await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@SignUpScreen, "Successfully sign in", Toast.LENGTH_SHORT).show()
                // strore lcoal session
                saveLocalSessions(userData.email)
            }
        }catch (e:Exception){
            Log.d(TAG, "saveUserData: SignIn ERROR: ${e.toString()}")
            withContext(Dispatchers.Main){
                Toast.makeText(this@SignUpScreen, "Internal Server Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // save local sessions
    private fun saveLocalSessions(userName: String){
        editor.apply{
            putString(ConfigurationConstant.USER_ID, userName)
            Log.d(TAG, "saveLocalSessions: Store to local storage")
            apply()
        }
        landingPageInit()
    }

    //go-to landing page
    private fun landingPageInit(){
        val intent = Intent(this, LandingPage::class.java)
        startActivity(intent)
        finish()
    }
}