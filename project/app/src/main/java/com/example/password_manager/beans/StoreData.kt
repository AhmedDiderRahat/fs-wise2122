package com.example.password_manager.beans

data class StoreData(
    val user_id:String ?= null,
    val site_name:String?=null,
    val user_name:String?=null,
    val password: String?=null
)
