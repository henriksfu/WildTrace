package com.example.group21

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AuthViewModel(application: Application) : ViewModel() {

    private val profilePreferences: SharedPreferences =
        application.getSharedPreferences("profile_preferences", Context.MODE_PRIVATE)

    // Example keys
    private val KEY_DB_ID = "db_id"
    private val KEY_LOGGED_IN = "is_logged_in"
    private val emailState = mutableStateOf("")
    val email: State<String> = emailState

    private val passwordState = mutableStateOf("")
    val password: State<String> = passwordState

    private val errorMessageState = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = errorMessageState

    private val lNameState = mutableStateOf("")
    val lName: State<String> = lNameState

    private val fNameState = mutableStateOf("")
    val fName: State<String> = fNameState

    fun onEmailChange(new: String) {
        emailState.value = new
    }

    fun onPasswordChange(new: String) {
        passwordState.value = new
    }

    fun onlNameChange(new: String){
        lNameState.value = new
    }

    fun onfNameChange(new: String){
        fNameState.value = new
    }

    fun isLoggedIn(): Boolean {
        return profilePreferences.getBoolean(KEY_LOGGED_IN, false)
    }

    fun loggedInDBID(): Long {
        return profilePreferences.getLong(KEY_DB_ID, -1)
    }

    fun login() {
        //
        // need to check database to login
        Log.i("AuthViewModel", "Email: ${emailState.value}; Password: ${passwordState.value}")
    }

    fun createProfile(){
        //
        // need to check database to create profile
        Log.i("AuthViewModel", "Email: ${emailState.value}; Password: ${passwordState.value}; fName: ${fNameState.value}; lName: ${lNameState.value}")
    }
}