package com.example.group21.database
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SightingViewModel(
    private val repository: SightingRepository = SightingRepository()
) : ViewModel() {

    fun saveSighting(localImageUri: Uri, sighting: Sighting) {
        //val currentUser = FirebaseAuth.getInstance().currentUser ?: return //Authentication is instead done by caller
        //
        // Save to the database
        viewModelScope.launch {
            Log.i("sighting","Calling repo.saveSighting")
            repository.addSighting(localImageUri, sighting)
                .onSuccess { docId ->
                    println("Sighting saved with ID: $docId")
                    _allSightings.value = _allSightings.value ?: (emptyList<Sighting>() + sighting)
                }
                .onFailure { e ->
                    println("Error saving sighting: $e")
                }
        }
    }

    private val _allSightings = MutableLiveData<List<Sighting>>()
    val allSightings: LiveData<List<Sighting>> = _allSightings

    fun getSighting(id: String): Sighting? {
        if(_allSightings.value == null) return null
        for( s in _allSightings.value!! ){
            if( s.documentId == id ){
                return s
            }
        }
        return null
    }

    fun loadAllSightings() {
        viewModelScope.launch {
            Log.i("sighting", "Calling repo.getAllSightings()")
            val list = repository.getAllSightings()
            _allSightings.value = list.sortedByDescending { it.createdAt?.seconds }
            if( _allSightings.value != null) {
                for (item in _allSightings.value!!) {
                    Log.d("Sighting", item.toString())
                }
            }
        }
    }

    //
    // Takes a document ID
    // removes the sighting from the database
    // and from the livedata (to trigger observer changes)
    fun loadFilteredSightings(pattern: String) {
        viewModelScope.launch {
            Log.i("sighting", "Calling repo.getAllSightings()")
            val list = repository.getAllSightings()
            if( pattern == "" ){
                _allSightings.value = list.sortedByDescending { it.createdAt?.seconds }
            }
            else {
                _allSightings.value = list.filter {
                    it.animalName.lowercase().contains(pattern) || it.notes.lowercase().contains(pattern)
                }.sortedByDescending { it.createdAt?.seconds }
            }
            if( _allSightings.value != null) {
                for (item in _allSightings.value!!) {
                    Log.d("Sighting", item.toString())
                }
            }
        }
    }

    fun deleteSighting(documentId: String) {
        //
        // remove from livedata
        _allSightings.value = _allSightings.value?.filter {
            it.documentId != documentId
        }?.sortedByDescending { it.createdAt?.seconds }
        //
        // Delete from the database
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
    //
    // Called from the search view
    // filters the livedata to only entries whose names or comments include this
    // If there is no filter, get all of the entries

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
        //
        // clear the livedata
        _allSightings.value = emptyList<Sighting>()
        //
        // Clear all sightings from the database
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