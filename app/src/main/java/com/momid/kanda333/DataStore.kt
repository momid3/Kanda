package com.momid.kanda333

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.momid.kanda333.database.data_model.User

private const val PREF_USER_ID = "pref_user_id"
private const val PREF_IS_LOGGED_IN = "pref_is_logged_in"

class DataStore(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        sharedPreferences.edit {
            putInt(PREF_USER_ID, user.id)
            putBoolean(PREF_IS_LOGGED_IN, true)
        }
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt(PREF_USER_ID, -1)
    }
    fun isLoggedIn(): Boolean{
        return sharedPreferences.getBoolean(PREF_IS_LOGGED_IN, false)
    }

    fun clearUser() {
        sharedPreferences.edit {
            remove(PREF_USER_ID)
            remove(PREF_IS_LOGGED_IN)
        }
    }
}
