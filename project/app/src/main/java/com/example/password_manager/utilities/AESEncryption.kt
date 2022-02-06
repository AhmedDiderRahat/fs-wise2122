package com.example.password_manager.utilities

import android.util.Log
import com.scottyab.aescrypt.AESCrypt
import java.lang.Exception

/*
* All the encryption/decryption process of the app.
* Ahmed Dider Rahat- 5th Feb 2022
*/

class AESEncryption {
    private val TAG = "AESEncryption"

    // Encryption process
    fun encryption(key: String, message:String): String{
        try {
            return AESCrypt.encrypt(key, message)
        } catch (e: Exception) {
            Log.e(TAG, "encryption: ${e.toString()}")
        }

        return ""
    }

    fun decryption(key: String, message:String):String{
        try {
            return AESCrypt.decrypt(key, message)
        }catch (e: Exception){
            Log.e(TAG, "decryption: ${e.toString()}")
        }

        return ""
    }
}