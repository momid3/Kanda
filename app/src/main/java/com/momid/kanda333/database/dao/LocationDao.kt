package com.momid.kanda333.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.momid.kanda333.database.data_model.LocationData
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: LocationData)

    @Query("SELECT * FROM locations ORDER BY timestamp ASC")
    fun getAllLocations(): Flow<List<LocationData>>

    @Query("DELETE FROM locations")
    suspend fun deleteAllLocations()
}
