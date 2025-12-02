package com.example.group21.database


import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SightingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val sightingsRef = db.collection("sightingData")

    //add a Sighting to the database
    suspend fun addSighting(localImageUri: Uri, sighting: Sighting): Result<String> = try {

        //Upload image to Firebase Storage
        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("sightings/${UUID.randomUUID()}.jpg")

        // Upload  file
        storageRef.putFile(localImageUri).await()

        // Get download URL
        val downloadUrl = storageRef.downloadUrl.await().toString()

        // Store Sighting with the download URL
        val sightingWithPhoto = sighting.copy(photoUrl = downloadUrl)

        val documentRef = sightingsRef.add(sightingWithPhoto).await()

        Log.d("SightingRepository", "Sighting saved successfully! ID: ${documentRef.id}")
        Result.success(documentRef.id)
    } catch (e: Exception) {
        Log.e("SightingRepository", "Failed to save sighting", e)
        Result.failure(e)
    }

    //get all sightings from the database
    suspend fun getAllSightings(): List<Sighting> {
        return try {
            val snapshot = sightingsRef.get().await()
            snapshot.documents.mapNotNull { doc ->//loads the doc id
                doc.toObject(Sighting::class.java)?.copy(
                    documentId = doc.id   // Attach Firestore document ID
                )
            }
        } catch (e: Exception) {
            Log.e("SightingRepository", "Error loading sightings", e)
            emptyList()
        }
    }

    // Delete a single sighting by its Firestore document ID
    suspend fun deleteSighting(documentId: String): Result<Unit> = try {
        sightingsRef.document(documentId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Delete all sightings for a given user
    suspend fun deleteAllSightings(userId: String): Result<Unit> = try {
        val querySnapshot = sightingsRef
            .whereEqualTo("userId", userId)
            .get()
            .await()

        db.runBatch { batch ->
            for (doc in querySnapshot.documents) {
                batch.delete(doc.reference)
            }
        }.await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Admin: wipe the entire `sightingData` collection
    suspend fun wipeAllSightings(): Result<Unit> = try {
        val snapshot = sightingsRef.get().await()

        db.runBatch { batch ->
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
        }.await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}