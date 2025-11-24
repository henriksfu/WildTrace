package com.example.group21.database
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

//import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

class SightingViewModel(
    private val repository: SightingRepository = SightingRepository()
) : ViewModel() {

    fun saveSighting(
        animalName: String,
        scientificName: String,
        count: Int,
        location: GeoPoint,
        notes: String,
        photoUrls: List<String>
    ) {
        //val currentUser = FirebaseAuth.getInstance().currentUser ?: return//for authentication
        val currentUser = "test"

        viewModelScope.launch {
            val sighting = Sighting(
                animalName = animalName,
                scientificName = scientificName,
                count = count.toLong(),
                location = location,
                notes = notes,
                photoUrls = photoUrls,
                //once login is set up
                //userDisplayName = currentUser.displayName ?: "Anonymous",
                //userId = currentUser.uid
                userDisplayName = "Anonymous",
                userId = "testid123"
            )

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