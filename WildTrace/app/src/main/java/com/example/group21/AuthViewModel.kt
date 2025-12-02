package com.example.group21

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val emailState = mutableStateOf("")
    val email: State<String> = emailState

    private val passwordState = mutableStateOf("")
    val password: State<String> = passwordState

    private val fNameState = mutableStateOf("")
    val fName: State<String> = fNameState

    private val lNameState = mutableStateOf("")
    val lName: State<String> = lNameState

    private val errorMessageState = mutableStateOf("")
    val errorMessage: State<String> = errorMessageState

    fun onEmailChange(new: String) { emailState.value = new }
    fun onPasswordChange(new: String) { passwordState.value = new }
    fun onfNameChange(new: String) { fNameState.value = new }
    fun onlNameChange(new: String) { lNameState.value = new }

    fun clearErrorMessage(){
        errorMessageState.value = ""
    }

    // -----------------------------
    // LOGIN
    // -----------------------------
    fun login(onSuccess: () -> Unit) {
        //
        // trim any leading and trailing whitespace
        val email = emailState.value.trim()
        val password = passwordState.value.trim()
        //
        // Check error states. Both a username and password should be entered.
        if (email.isEmpty() || password.isEmpty()) {
            errorMessageState.value = "Email and password required."
            Log.w("login","Email and password required")
            return
        }
        //
        // perform the firebase auth sign in
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //
                    // On success we reset all state (error messages, email, password)
                    // and call the onsuccess to log in with the UI
                    Log.i("AuthViewModel", "Login successful")
                    resetStates()
                    onSuccess()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        //
                        // Print the error message
                        when (exception.errorCode) {
                            "ERROR_USER_NOT_FOUND" -> { errorMessageState.value = "No account with that email exists." }
                            "ERROR_WRONG_PASSWORD" -> { errorMessageState.value = "Incorrect password." }
                            "ERROR_INVALID_EMAIL" -> { errorMessageState.value = "Invalid email address." }
                            "ERROR_USER_DISABLED" -> { errorMessageState.value = "This account has been disabled." }
                            else -> { errorMessageState.value = "Authentication failed: ${exception.message}" }
                        }

                        Log.e("AuthViewModel", "Login failed (${exception.errorCode})", exception)

                    } else {
                        errorMessageState.value = "An unknown error occurred."
                        Log.e("AuthViewModel", "Login failed", exception)
                    }
                }
            }
    }

    // -----------------------------
    // SIGN UP
    // -----------------------------
    fun createProfile(onSuccess: () -> Unit) {
        //
        // remove leading and trailing whitespace
        val email = emailState.value.trim()
        val password = passwordState.value.trim()
        //
        // Make sure that all fields are filled in for the sign in view
        if (email.isEmpty() || password.isEmpty() || fNameState.value.isEmpty() || lNameState.value.isEmpty()) {
            errorMessageState.value = "All fields are required."
            return
        }
        //
        // perform the firebase auth profile creation
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("AuthViewModel", "Account Created: ${auth.currentUser?.uid}")
                    resetStates()
                    onSuccess()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthException) {
                        when (exception.errorCode) {
                            "ERROR_EMAIL_ALREADY_IN_USE" -> { errorMessageState.value = "That email is already registered." }
                            "ERROR_INVALID_EMAIL" -> { errorMessageState.value = "Please enter a valid email address." }
                            "ERROR_WEAK_PASSWORD" -> { errorMessageState.value = "Password must be at least 6 characters." }
                            else -> { errorMessageState.value = "Sign-up failed: ${exception.message ?: "Unknown error"}" }
                        }
                        Log.e("AuthViewModel","Sign Up Failed (${exception.errorCode})",exception)
                    } else {
                        errorMessageState.value = "An unexpected error occurred."
                        Log.e("AuthViewModel", "Sign Up Failed", exception)
                    }
                }
            }
    }

    fun resetStates(){
        emailState.value = ""
        passwordState.value = ""
        lNameState.value = ""
        fNameState.value = ""
        errorMessageState.value = ""
    }
}

