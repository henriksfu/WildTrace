package com.example.group21

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel() {
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

    fun login() {
        //
        // need to check database to login
    }

    fun createProfile(){
        //
        // need to check database to create profile
    }
}