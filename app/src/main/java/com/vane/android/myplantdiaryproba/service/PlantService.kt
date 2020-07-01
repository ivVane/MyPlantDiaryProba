package com.vane.android.myplantdiaryproba.service

import com.vane.android.myplantdiaryproba.RetrofitClientInstance
import com.vane.android.myplantdiaryproba.dao.IPlantDAO
import com.vane.android.myplantdiaryproba.dto.Plant
import kotlinx.coroutines.*
import java.util.ArrayList

class PlantService {

    internal suspend fun fetchPlants(plantName: String) {
        withContext(Dispatchers.IO) {
            val service = RetrofitClientInstance.retrofitInstance?.create(IPlantDAO::class.java)
            val plants = async { service?.getAllPlants() }

            updateLocalPlants(plants.await())

            delay(30000)
        }
    }

    /**
     * Store this plants locally, so that we can use the data without network latency.
     */
    private suspend fun updateLocalPlants(plants: ArrayList<Plant>?) {
        var sizeOfPlants = plants?.size
        delay(30000)
    }
}