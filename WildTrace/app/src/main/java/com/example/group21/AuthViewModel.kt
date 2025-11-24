package com.example.group21

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

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

    private val errorMessageState = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = errorMessageState

    fun onEmailChange(new: String) { emailState.value = new }
    fun onPasswordChange(new: String) { passwordState.value = new }
    fun onfNameChange(new: String) { fNameState.value = new }
    fun onlNameChange(new: String) { lNameState.value = new }

    // -----------------------------
    // LOGIN
    // -----------------------------
    fun login(onSuccess: () -> Unit) {
        val email = emailState.value.trim()
        val password = passwordState.value.trim()

        if (email.isEmpty() || password.isEmpty()) {
            errorMessageState.value = "Email and password required."
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("AuthViewModel", "Login successful")
                    onSuccess()
                } else {
                    Log.e("AuthViewModel", "Login failed", task.exception)
                    errorMessageState.value = task.exception?.message
                }
            }
    }

    // -----------------------------
    // SIGN UP
    // -----------------------------
    fun createProfile(onSuccess: () -> Unit) {
        val email = emailState.value.trim()
        val password = passwordState.value.trim()

        if (email.isEmpty() || password.isEmpty() || fNameState.value.isEmpty() || lNameState.value.isEmpty()) {
            errorMessageState.value = "All fields are required."
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("AuthViewModel", "Account Created: ${auth.currentUser?.uid}")
                    onSuccess()
                } else {
                    Log.e("AuthViewModel", "Sign Up Failed", task.exception)
                    errorMessageState.value = task.exception?.message
                }
            }
    }
}
