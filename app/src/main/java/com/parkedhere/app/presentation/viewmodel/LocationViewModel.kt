package com.parkedhere.app.presentation.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.parkedhere.app.R
import com.parkedhere.app.presentation.TAG
import com.parkedhere.app.presentation.repository.LocationStorage
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
                if (location == null) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.no_location_available),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.d(TAG, "Last location obtained $location")
                locationStorage.setLocation(location)
            } catch (ex: Exception) {
                Log.e(TAG, "Error get location", ex)
                Toast.makeText(
                    context,
                    context.getString(R.string.error_save_location),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}