package com.example.password_manager.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.es_job_manager.utilities.ConfigurationConstant
import com.example.password_manager.R
import kotlinx.coroutines.*;

/*
* Splash Screen of the app. Execute for three second
* Ahmed Dider Rahat- 4 th Feb 2022
*/

class SplashScreen : AppCompatActivity() {
    private lateinit var mScope: CoroutineScope
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val TAG = "SplashScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        sharedPref = getSharedPreferences(ConfigurationConstant.LOGIN_PREFERENCE, MODE_PRIVATE)
        editor = sharedPref.edit()
        mScope = CoroutineScope(Dispatchers.IO)
        encryptionProcess()
    }

    //Coroutine initializer
    private fun withCoroutine(delay: Long) {
        mScope.launch {
            delay(delay)
            withContext(Dispatchers.Main) {
                checkLoggedInState()
                mScope.cancel(null)
            }
        }
    }

    //Activity launcher
    private fun launchNextScreen(intent: Intent) {
        startActivity(intent)
        finish() // necessary because we do not want user to come back to this activity
    }

    //check the login successfully done or not
    private fun checkLoggedInState() {

        val loginStatus = sharedPref.getBoolean(ConfigurationConstant.LOGIN_STATUS, false)

        Log.d(TAG, "checkLoggedInState: $loginStatus")

        val intent = if (loginStatus) Intent(this, LandingPage::class.java) else Intent(
            this,
            LogIn::class.java
        )

        launchNextScreen(intent)
    }

    private fun getRandomString(length: Int) : String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    private fun getAESKey(): String? {
        return sharedPref.getString(ConfigurationConstant.CRYPTO_KEY, "")
    }

    private fun encryptionProcess(){
        // check key is stored
        if (getAESKey().equals("")){
            // key generate
            var key = getRandomString(16)
            Log.d(TAG, "encryptionProcess: $key")
            editor.apply{
                putString(ConfigurationConstant.CRYPTO_KEY, key)
                apply()
            }
        }

        withCoroutine(ConfigurationConstant.SPLASH_SCREEN_DURATION)
    }
}

