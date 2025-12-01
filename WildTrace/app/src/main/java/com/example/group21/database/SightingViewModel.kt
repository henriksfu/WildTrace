package com.example.group21.database
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SightingViewModel(
    private val repository: SightingRepository = SightingRepository()
) : ViewModel() {

    fun saveSighting(sighting: Sighting) {
        //val currentUser = FirebaseAuth.getInstance().currentUser ?: return //Authentication is instead done by caller
        viewModelScope.launch {
            Log.i("sighting","Calling repo.saveSighting")
            repository.addSighting(sighting)
                .onSuccess { docId ->
                    println("Sighting saved with ID: $docId")
                }
                .onFailure { e ->
                    println("Error saving sighting: $e")
                }
        }
    }

    private val _allSightings = MutableLiveData<List<Sighting>>() //private real data
    val allSightings: LiveData<List<Sighting>> = _allSightings //public fake data

    fun loadAllSightings() {
        viewModelScope.launch {
            Log.i("sighting", "Calling repo.getAllSightings()")
            val list = repository.getAllSightings()
            _allSightings.value = list
            if( _allSightings.value != null) {
                for (item in _allSightings.value!!) {
                    Log.d("Sighting", item.toString())
                }
            }
        }
    }

    fun deleteSighting(documentId: String) {
        viewModelScope.launch {
            repository.deleteSighting(documentId)
                .onSuccess {
                    Log.d("SightingViewModel", "Sighting deleted: $documentId")
                }
                .onFailure {
                    Log.e("SightingViewModel", "Delete failed", it)
                }
        }
    }

    fun deleteAllSightings(userId: String) {
        viewModelScope.launch {
            repository.deleteAllSightings(userId)
                .onSuccess {
                    Log.d("SightingViewModel", "All sightings deleted for user $userId")
                }
                .onFailure {
                    Log.e("SightingViewModel", "Failed to delete user sightings", it)
                }
        }
    }

    fun wipeAllSightings() {
        viewModelScope.launch {
            repository.wipeAllSightings()
                .onSuccess {
                    Log.w("SightingViewModel", "ALL sightings wiped by admin")
                }
                .onFailure {
                    Log.e("SightingViewModel", "Admin wipe failed", it)
                }
        }
    }
}