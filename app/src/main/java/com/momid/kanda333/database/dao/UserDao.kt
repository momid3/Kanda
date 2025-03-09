package com.momid.kanda333.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.momid.kanda333.database.data_model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username")
    fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Insert
    suspend fun insert(user: User)
}
