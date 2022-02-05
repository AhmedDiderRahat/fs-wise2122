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
import com.example.password_manager.databinding.ActivityAddNewBinding
import com.example.password_manager.databinding.ActivitySignUpBinding
import com.example.password_manager.utilities.AESEncryption
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

class AddNewScreen : AppCompatActivity() {

    val TAG = "SignUpScreen"

    lateinit var binding: ActivityAddNewBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var AES_KEY: String
    private val userCollectionRef = Firebase.firestore.collection("user_data")
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNewBinding.inflate(layoutInflater)

        sharedPreferences = getSharedPreferences(ConfigurationConstant.LOGIN_PREFERENCE, MODE_PRIVATE)
        editor = sharedPreferences.edit()
        AES_KEY = sharedPreferences.getString(ConfigurationConstant.CRYPTO_KEY, "").toString()

        val view: View = binding.root
        setContentView(view)


    }
}