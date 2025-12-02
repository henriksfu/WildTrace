package com.example.group21.database

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

data class Sighting(
    val animalName: String = "",
    val scientificName: String = "",
    val count: Long = 1,
    val location: GeoPoint? = null,
    val notes: String = "",
    val photoUrl: String = "",
    val userDisplayName: String = "",
    val userId: String = "",

    @ServerTimestamp
    val createdAt: Timestamp? = null,

    val sightingDateTime: Timestamp? = null, // user-picked date/time
    // Don't store documentId in the object that goes to Firestore
    // Keep it only locally if you need it after reading
    @get:Exclude  // This prevents it from being written to Firestore
    val documentId: String? = null
)