package com.example.group21.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SightingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val sightingDataCollection = db.collection("sightingData")
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
    suspend fun getAllSightings(): Result<List<Sighting>> = try {
        val snapshot = sightingDataCollection.get().await()
        val sightings = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Sighting::class.java)?.copy(documentId = doc.id)
        }
        Result.success(sightings)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSightingsByUser(userId: String): Result<List<Sighting>> = try {
        val snapshot = sightingDataCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
        val list = snapshot.documents.mapNotNull {
            it.toObject(Sighting::class.java)?.copy(documentId = it.id)
        }
        Result.success(list)
    } catch (e: Exception) {
        Result.failure(e)
    }
}