package com.example.group21.database
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

class SightingViewModel(
    private val repository: SightingRepository = SightingRepository()
) : ViewModel() {

    fun saveSighting(sighting: Sighting) {
        //val currentUser = FirebaseAuth.getInstance().currentUser ?: return //for authentication

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
}