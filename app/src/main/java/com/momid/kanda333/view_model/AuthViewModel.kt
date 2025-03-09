package com.momid.kanda333.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.momid.kanda333.DataStore
import com.momid.kanda333.database.dao.UserDao
import com.momid.kanda333.database.data_model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(val userDao: UserDao, val dataStore: DataStore) : ViewModel() {
    var user by mutableStateOf<User?>(null)
        private set

    init {
        loadUserFromPreferences()
    }

    suspend fun register(user: User) {
        userDao.insert(user)
    }

    fun login(username: String, password: String): Boolean {
        val user = userDao.getUserByUsername(username)
        return if (user != null && user.password == password) {
            this.user = user
            dataStore.saveUser(user)
            true
        } else {
            false
        }
    }

    fun logout() {
        user = null
        dataStore.clearUser()
    }

    private fun loadUserFromPreferences() {
        if (dataStore.isLoggedIn()) {
            val userId = dataStore.getUserId()
            if (userId != -1) {
                viewModelScope.launch(Dispatchers.IO) {
                    val loadedUser = userDao.getUserById(userId)
                    withContext(Dispatchers.Main) {
                        user = loadedUser
                    }
                }
            }
        }
    }
}
