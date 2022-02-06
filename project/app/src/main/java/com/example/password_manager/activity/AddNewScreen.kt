package com.example.password_manager.activity

import android.content.Context
import android.content.Intent
import android.content.RestrictionEntry.TYPE_NULL
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.KeyListener
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.es_job_manager.utilities.ConfigurationConstant
import com.example.password_manager.R
import com.example.password_manager.beans.StoreData
import com.example.password_manager.databinding.ActivityAddNewBinding
import com.example.password_manager.utilities.AESEncryption
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
    lateinit var oldData: StoreData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNewBinding.inflate(layoutInflater)

        sharedPreferences = getSharedPreferences(ConfigurationConstant.LOGIN_PREFERENCE, MODE_PRIVATE)
        editor = sharedPreferences.edit()
        AES_KEY = sharedPreferences.getString(ConfigurationConstant.CRYPTO_KEY, "").toString()
        USER_ID = sharedPreferences.getString(ConfigurationConstant.USER_ID, "").toString()

        encryptionObj = AESEncryption()
        AES_KEY = sharedPreferences.getString(ConfigurationConstant.CRYPTO_KEY, "").toString()

        view = binding.root
        setContentView(view)

        val is_edit_mode = intent.getBooleanExtra(ConfigurationConstant.B_IS_EDIT, false)
        Log.d(TAG, "onCreate: $is_edit_mode")

        if (is_edit_mode) {
            setEditEnable()
        } else {
            setEditDisable()

            var site = intent.getStringExtra(ConfigurationConstant.B_SITE)
            var uname = intent.getStringExtra(ConfigurationConstant.B_UNAME)
            var pass = intent.getStringExtra(ConfigurationConstant.B_PASSWORD)

            try {
                var unameCipher = encryptionObj.encryption(AES_KEY, uname.toString())
                var passwordCipher = encryptionObj.encryption(AES_KEY, pass.toString())
                oldData = StoreData(USER_ID, site, unameCipher, passwordCipher)
            }catch (e: Exception){
                Log.e(TAG, "onCreateException: ${e.toString()}")
            }

            binding.etSiteName.setText(site)
            binding.etUserName.setText(uname)
            binding.etPassword.setText(pass)

            binding.btnAdd.setText("EDIT")
        }

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
            if (binding.btnAdd.text.equals("EDIT")){
                // enable all edit text
                setEditEnable()
                binding.btnAdd.text = "UPDATE"
            } else {
                var site = binding.etSiteName.text
                var user_name = binding.etUserName.text
                var password = binding.etPassword.text

                try {
                    var nameCipher = encryptionObj.encryption(AES_KEY, user_name.toString())
                    var passwordCipher = encryptionObj.encryption(AES_KEY, password.toString())

                    val storageData = StoreData(USER_ID, site.toString(), nameCipher, passwordCipher)

                    var updatedMap = mutableMapOf<String, Any>()

                    updatedMap[ConfigurationConstant.B_SITE] = site.toString()
                    updatedMap[ConfigurationConstant.B_UNAME] = nameCipher
                    updatedMap[ConfigurationConstant.B_PASSWORD] = passwordCipher

                    if( binding.btnAdd.text.equals("ADD")){
                        // add to db
                        saveUserData(storageData)
                    }
                    else {
                        //Update
                        updateData(oldData, updatedMap)
                    }
                }catch (e: Exception){
                    Log.e(TAG, "onCreate: ${e.toString()}")
                }
            }
        }
    }

    // Update data
    private fun updateData(storageData: StoreData, newMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch {
        val storageQuery = storageCollectionRef
            .whereEqualTo(ConfigurationConstant.UID, storageData.user_id)
            .whereEqualTo(ConfigurationConstant.SITE, storageData.site_name)
            .get().await()

        if (storageQuery.documents.isNotEmpty()) {
            for (document in storageQuery){
                try {
                    Log.d(TAG, "updateData: ID= $document.id")

                    storageCollectionRef.document(document.id).set(
                        newMap,
                        SetOptions.merge()
                    ).await()

                    withContext(Dispatchers.Main){
                        Toast.makeText(this@AddNewScreen, "Update Successfully", Toast.LENGTH_SHORT).show()
                        binding.btnAdd.setText("EDIT")
                        setEditDisable()
                        closeKeyBoard()
                    }
                }catch (e: Exception){
                    Log.e(TAG, "updateDataError: ${e.toString()}")
                }
            }
        }
    }

    // if data add successfully then reset all input field
    private fun resetInputField(){
        binding.etSiteName.setText("")
        binding.etUserName.setText("")
        binding.etPassword.setText("")
        closeKeyBoard()
    }

    // close the key board
    private fun closeKeyBoard(){
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
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

    fun EditText.setReadOnly(value: Boolean, inputType: Int = InputType.TYPE_NULL) {
        isFocusable = !value
        isFocusableInTouchMode = !value
        this.inputType = inputType
    }

    private fun setEditEDExtent() {
        binding.etSiteName.isFocusable = true
        binding.etSiteName.isFocusableInTouchMode = true
    }

    private fun setEditEnable() {
        setEditEDExtent()
        binding.etSiteName.inputType = InputType.TYPE_CLASS_TEXT
        binding.etUserName.inputType = InputType.TYPE_CLASS_TEXT
        binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT
    }

    private fun setEditDisable() {
        setEditEDExtent()
        binding.etSiteName.inputType = TYPE_NULL
        binding.etUserName.inputType = TYPE_NULL
        binding.etPassword.inputType = TYPE_NULL
    }

    //go-to landing page
    private fun landingPageInit() {
        val intent = Intent(this, LandingPage::class.java)
        startActivity(intent)
        finish()
    }
}