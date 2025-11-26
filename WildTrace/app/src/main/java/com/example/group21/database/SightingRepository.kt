package com.example.group21.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SightingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val sightingsRef = db.collection("sightingData")

    suspend fun addSighting(sighting: Sighting): Result<String> = try {
        //val currentUser = auth.currentUser
        //?: return Result.failure(Exception("User not logged in"))

        val documentRef = sightingsRef.add(sighting).await()

        Log.d("SightingRepository", "Sighting saved successfully! ID: ${documentRef.id}")
        Result.success(documentRef.id)
    } catch (e: Exception) {
        Log.e("SightingRepository", "Failed to save sighting", e)
        Result.failure(e)
    }

    suspend fun getAllSightings(): List<Sighting> {
        return try {
            val snapshot = sightingsRef.get().await()
            snapshot.toObjects(Sighting::class.java)   // ← converts automatically
        } catch (e: Exception) {
            Log.e("SightingRepository", "Error loading sightings", e)
            emptyList()  // just return empty list if something goes wrong
        }
    }
}