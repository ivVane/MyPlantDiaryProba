package com.vane.android.myplantdiaryproba.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vane.android.myplantdiaryproba.dto.Plant

@Database(entities = arrayOf(Plant::class), version = 1)
abstract class PlantDatabase : RoomDatabase() {

    abstract fun localPlantDAO(): ILocalPlantDAO

}