package com.gpghilardi.parkedhere.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.gpghilardi.parkedhere.presentation.PREFIX
import com.gpghilardi.parkedhere.presentation.TAG
import com.gpghilardi.parkedhere.presentation.repository.LocationStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationViewModel(
    private val context: Context,
    private val locationStorage: LocationStorage
) : ViewModel() {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    val lastLocation = locationStorage.getLocation()

    /**
     * Callback function invoked by the "Set position" button for actually storing the location data
     */
    @SuppressLint("MissingPermission")
    fun saveLocation() {
        viewModelScope.launch {
            try {
                val location = fusedLocationClient.lastLocation.await()
                Log.d(TAG, "Last location obtained $location")
                locationStorage.setLocation(location)
            } catch (ex: Exception) {
                Log.e(TAG, "Error get location", ex)
                Toast.makeText(
                    context,
                    "$PREFIX: no position to store at this time!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}