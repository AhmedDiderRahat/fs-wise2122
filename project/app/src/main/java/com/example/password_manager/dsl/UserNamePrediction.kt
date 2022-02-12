package com.example.password_manager.dsl

class UserNamePrediction (userEmail: String) {
    lateinit var fName:String
    lateinit var lName:String

    private fun getName():String{
        fName = getFirstName()
        lName = getLastName()

        return fName+ " " + lName
    }

    private fun getFirstName():String{
        //TODO("fetch data from db")
        return "ahmed dider"
    }

    private fun getLastName():String{
        //TODO("fetch data from db")
        return "rahat"
    }

    fun getNameRating(): String{
        //TODO("get the name rating from external system")
        val fulName = getName()
        return "4.5 out of 10.0"
    }
}