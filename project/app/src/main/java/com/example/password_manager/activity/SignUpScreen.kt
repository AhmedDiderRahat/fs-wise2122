package com.example.password_manager.activity

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.password_manager.R
import com.example.password_manager.databinding.ActivitySignUpBinding

import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*

/*
* Sign-up process of the app.
* Ahmed Dider Rahat- 4th Feb 2022
*/

class SignUpScreen : AppCompatActivity() {

    val TAG = "SignUpScreen"


    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)

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
                //val user_data = UserData(first_name, last_name, birth_date, total_days.toInt(), 0)
                //registerUser(user_data, email, password)
                //Log.d(TAG, "data: $first_name $last_name $birth_date $email $password")
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