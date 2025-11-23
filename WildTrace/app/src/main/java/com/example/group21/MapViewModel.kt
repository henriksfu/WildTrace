package com.example.group21

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    private val _imageUri = mutableStateOf<Uri?>(null)
    val imageUri: State<Uri?> = _imageUri

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
        showPhotoDialog()
        //Log.d("uri", uri.toString())
    }

    private val _showPhotoDialog = mutableStateOf(false)
    val showPhotoDialog: State<Boolean> = _showPhotoDialog

    fun showPhotoDialog(){
        _showPhotoDialog.value = true
    }

    fun dismissPhotoDialog() {
        _showPhotoDialog.value = false
    }

}

