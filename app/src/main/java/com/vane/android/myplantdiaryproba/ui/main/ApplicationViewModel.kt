package com.vane.android.myplantdiaryproba.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vane.android.myplantdiaryproba.service.PlantService
import kotlinx.coroutines.launch

class ApplicationViewModel(application: Application) : AndroidViewModel(application) {

    private var _plantService: PlantService = PlantService(application)

    private val locationLiveData = LocationLiveData(application)
    fun getLocationLiveData() = locationLiveData

    init {
        fetchPlants("e")
    }

    fun fetchPlants(plantName: String) {
        viewModelScope.launch {
            _plantService.fetchPlants(plantName)
        }
    }

    internal var plantService : PlantService
        get() {return _plantService}
        set(value) {_plantService = value}
}