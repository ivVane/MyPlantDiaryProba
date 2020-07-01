package com.vane.android.myplantdiaryproba.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class ApplicationViewModel(application: Application) : AndroidViewModel(application) {

    private val locationLiveData = LocationLiveData(application)
    fun getLocationLiveData() = locationLiveData

}