package com.example.password_manager.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.es_job_manager.utilities.ConfigurationConstant
import com.example.password_manager.R
import com.example.password_manager.databinding.ActivityLoginPageBinding
import com.example.password_manager.utilities.AESEncryption
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception


class LogIn : AppCompatActivity() {
    val TAG = "LogInScreen"
    private lateinit var binding: ActivityLoginPageBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val userCollectionRef = Firebase.firestore.collection("user_data")
    lateinit var AES_KEY: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        sharedPreferences = getSharedPreferences(ConfigurationConstant.LOGIN_PREFERENCE, MODE_PRIVATE)
        editor = sharedPreferences.edit()

        AES_KEY = sharedPreferences.getString(ConfigurationConstant.CRYPTO_KEY, "").toString()

        binding.ivPasswordHideShow.setOnClickListener {

            Log.d(TAG, "password length: ${binding.etPassword.length()}")

            if (binding.ivPasswordHideShow.tag.toString().equals("hide")) {
                binding.ivPasswordHideShow.tag = "show"
                binding.etPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()

                binding.ivPasswordHideShow.setImageResource(R.drawable.show_password)
            } else {
                binding.ivPasswordHideShow.tag = "hide"
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivPasswordHideShow.setImageResource(R.drawable.hide_password)
            }

            binding.etPassword.setSelection(binding.etPassword.length())
        }

        binding.btnLogin.setOnClickListener {
            var encryptionObject = AESEncryption()
            var user_email = binding.etEmail.text.toString()
            var user_password = binding.etPassword.text.toString()

            var emailCipher = encryptionObject.encryption(AES_KEY, user_email)
            var passwordCipher = encryptionObject.encryption(AES_KEY, user_password)

            emailCipher?.let { it1 ->
                if (passwordCipher != null) {
                    logInProcess(it1, passwordCipher)
                }
            }
        }

        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, SignUpScreen::class.java)
            startActivity(intent)
        }
    }

    // check the used already exist or not
    private fun logInProcess(email: String, password:String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Log.d(TAG, "checkAlreadySignIn: $email+$password")
            val querySnapshot = userCollectionRef
                .whereEqualTo(ConfigurationConstant.USER_ID, email)
                .whereEqualTo(ConfigurationConstant.USER_PASS, password)
                .get().await()

            if (querySnapshot.size() > 0){
                Log.d(TAG, "checkAlreadySignIn: Total data= $querySnapshot.size()")
                withContext(Dispatchers.Main){
                    Toast.makeText(this@LogIn, "Log In Successful", Toast.LENGTH_SHORT).show()
                    saveLocalSessions(email)
                }
            }else{
                Log.d(TAG, "checkAlreadySignIn: user name password not matched")
                withContext(Dispatchers.Main){
                    Toast.makeText(this@LogIn, "User Name and Password not matched", Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@LogIn, "Internal Server Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // save local sessions
    private fun saveLocalSessions(userName: String){
        editor.apply{
            putString(ConfigurationConstant.USER_ID, userName)
            putBoolean(ConfigurationConstant.LOGIN_STATUS, true)
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