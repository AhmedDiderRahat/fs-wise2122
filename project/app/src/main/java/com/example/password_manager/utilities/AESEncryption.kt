package com.example.password_manager.utilities

import android.util.Log
import com.scottyab.aescrypt.AESCrypt
import java.lang.Exception

class AESEncryption {
    private val TAG = "AESEncryption"

    // Encryption process
    public fun encryption(key: String, message:String): String{
        try {
            return AESCrypt.encrypt(key, message)
        } catch (e: Exception) {
            Log.e(TAG, "encryption: ${e.toString()}")
        }

        return ""
    }

    public fun decryption(key: String, message:String):String{
        try {
            return AESCrypt.decrypt(key, message)
        }catch (e: Exception){
            Log.e(TAG, "decryption: ${e.toString()}")
        }

        return ""
    }
}