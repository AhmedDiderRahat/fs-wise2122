package com.example.password_manager.activity

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.password_manager.R
import com.example.password_manager.databinding.ActivityLoginPageBinding


class LogIn : AppCompatActivity() {
    val TAG = "LogInScreen"
    private lateinit var binding: ActivityLoginPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginPageBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

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
            var user_email = binding.etEmail.text.toString()
            var user_password = binding.etPassword.text.toString()
            //logInUser(user_email, user_password)

            Toast.makeText(this, "$user_email and $user_password", Toast.LENGTH_SHORT).show()
        }

        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, SignUpScreen::class.java)
            startActivity(intent)
        }

    }
}