package com.example.group21.database
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth

//import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

class SightingViewModel(
    private val repository: SightingRepository = SightingRepository()
) : ViewModel() {

    fun saveSighting(sighting: Sighting) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        //if(currentUser == null){
        //    Log.e("sighting","not logged in!")
        //    return //for authentication
        //}
        Log.e("sighting","Calling repo.saveSighting")
        viewModelScope.launch {
            repository.addSighting(sighting)
                .onSuccess { docId ->
                    println("Sighting saved with ID: $docId")
                }
                .onFailure { e ->
                    println("Error saving sighting: $e")
                }
        }
    }

    // Example: load all sightings
    fun loadSightings(onResult: (List<Sighting>) -> Unit) {
        viewModelScope.launch {
            repository.getAllSightings()
                .onSuccess { list ->
                    onResult(list)
                }
                .onFailure { e ->
                    println("Error loading sightings: $e")
                }
        }
    }
}