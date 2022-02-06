package com.example.password_manager.beans

/*
* Data class of all-data-storage
* Ahmed Dider Rahat- 5th Feb 2022
*/

data class StoreData(
    val user_id:String ?= null,
    val site_name:String?=null,
    val user_name:String?=null,
    val password: String?=null
)
