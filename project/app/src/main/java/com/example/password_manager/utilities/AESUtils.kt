package com.example.password_manager.utilities

import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESUtils {

    fun cipherEncrypt(encryptionKey: String, plainText: String): String? {
        try {
            val secretKeySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
            val iv = encryptionKey.toByteArray()
            val ivParameterSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)

            val encryptedValue = cipher.doFinal(plainText.toByteArray())
            return Base64.encodeToString(encryptedValue, Base64.DEFAULT)
        } catch (e: Exception) {

            e.message?.let{ Log.e("encryptor: ", it) }
        }
        return null
    }

    fun cipherDecrypt(encryptionKey: String, chipperText: String): String? {
        try {
            val secretKeySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
            val iv = encryptionKey.toByteArray()
            val ivParameterSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

            val decodedValue = Base64.decode(chipperText, Base64.DEFAULT)
            val decryptedValue = cipher.doFinal(decodedValue)
            return String(decryptedValue)
        } catch (e: Exception) {
            e.message?.let{ Log.e("decrypt: ", it) }
        }
        return null
    }

}