package com.momid.kanda333.view_model

import androidx.lifecycle.ViewModel
import com.momid.kanda333.database.dao.LocationDao
import com.momid.kanda333.database.data_model.LocationData
import kotlinx.coroutines.flow.Flow

class LocationViewModel(private val locationDao: LocationDao) : ViewModel() {
    val locations: Flow<List<LocationData>> = locationDao.getAllLocations()

    suspend fun insertLocation(location: LocationData) {
        locationDao.insert(location)
    }

    suspend fun clearLocations() {
        locationDao.deleteAllLocations()
    }
}
