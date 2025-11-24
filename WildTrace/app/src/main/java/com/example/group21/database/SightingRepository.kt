package com.example.group21.database

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SightingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val sightingsCollection = db.collection("sightings")

    // Write a new sighting
    suspend fun addSighting(sighting: Sighting): Result<String> = try {
        val documentRef = sightingsCollection.add(sighting).await()
        Result.success(documentRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Get all sightings (with document ID included in the object)
    suspend fun getAllSightings(): Result<List<Sighting>> = try {
        val snapshot = sightingsCollection.get().await()
        val sightings = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Sighting::class.java)?.copy(documentId = doc.id)
        }
        Result.success(sightings)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Optional: Get sightings by current user
    suspend fun getSightingsByUser(userId: String): Result<List<Sighting>> = try {
        val snapshot = sightingsCollection
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