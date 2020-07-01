package com.vane.android.myplantdiaryproba.service

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import androidx.room.Room
import com.vane.android.myplantdiaryproba.RetrofitClientInstance
import com.vane.android.myplantdiaryproba.dao.ILocalPlantDAO
import com.vane.android.myplantdiaryproba.dao.IPlantDAO
import com.vane.android.myplantdiaryproba.dao.PlantDatabase
import com.vane.android.myplantdiaryproba.dto.Plant
import kotlinx.coroutines.*
import java.util.ArrayList

class PlantService(application: Application) {

    private val application = application

    internal suspend fun fetchPlants(plantName: String) {
        withContext(Dispatchers.IO) {
            val service = RetrofitClientInstance.retrofitInstance?.create(IPlantDAO::class.java)
            val plants = async { service?.getAllPlants() }

            updateLocalPlants(plants.await())
        }
    }

    /**
     * Store this plants locally, so that we can use the data without network latency.
     */
    private suspend fun updateLocalPlants(plants: ArrayList<Plant>?) {
        var sizeOfPlants = plants?.size
        try {
            var localPlantDAO = getLocalPlantDAO()
            localPlantDAO.insertAll(plants!!)
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }

    }

    internal fun getLocalPlantDAO(): ILocalPlantDAO {
        val db = Room.databaseBuilder(application, PlantDatabase::class.java, "diary").build()
        val localPlantDao = db.localPlantDAO()
        return localPlantDao
    }
}