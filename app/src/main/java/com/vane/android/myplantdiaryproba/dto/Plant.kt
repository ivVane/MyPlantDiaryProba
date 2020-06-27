package com.vane.android.myplantdiaryproba.dto

import com.google.gson.annotations.SerializedName

data class Plant(var genus : String, var species : String, var common : String,
                 @SerializedName("id") var planId : Int = 0) {

    override fun toString(): String {
        return common
    }
}