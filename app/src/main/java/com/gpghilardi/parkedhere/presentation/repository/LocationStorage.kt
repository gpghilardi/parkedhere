package com.gpghilardi.parkedhere.presentation.repository

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.gpghilardi.parkedhere.presentation.PREFIX
import com.gpghilardi.parkedhere.presentation.TAG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * This simple class is used for make location data persistent across app restarts.
 * The purpose is to protect the location data set by user from app crashes or os-forced app kills.
 *
 * Note: this storage belongs to the app. Data is thus stored locally, on the device.
 *       In other words, the stored location data is never collected nor shared to anyone.
 */

private val Context.locationDataStore by preferencesDataStore("LOCATION_STORE")

class LocationStorage(private val context: Context) {
    private val locationLatitudeKey = doublePreferencesKey("LOCATION_LATITUDE")
    private val locationLongitudeKey = doublePreferencesKey("LOCATION_LONGITUDE")

    /**
     * Store the location data in app's own data store
     */
    suspend fun setLocation(location: Location?) {
        if (location != null) {
            context.locationDataStore.edit {
                it[locationLatitudeKey] = location.latitude
                it[locationLongitudeKey] = location.longitude
                Log.d(
                    TAG,
                    PREFIX + ": LocationStorage: location stored: ${it[locationLatitudeKey]}, ${it[locationLongitudeKey]}"
                )
            }
        }
    }

    /**
     * Read the location data from app's own data store (it may be null,
     * if nothing was previously stored)
     */
    fun getLocation(): Flow<Location?> {
        return context.locationDataStore.data.map {
            val latitude = it[locationLatitudeKey]
            val longitude = it[locationLongitudeKey]
            if (latitude != null && longitude != null) {
                val ret = Location("") // Provider name is unnecessary
                ret.latitude = latitude
                ret.longitude = longitude

                Log.d(
                    TAG,
                    "$PREFIX: LocationStorage: found location stored: ${ret.latitude}, ${ret.longitude}"
                )

                return@map ret

            }

            Log.d(TAG, "$PREFIX: LocationStorage: no location previously stored!")

            return@map null
        }
    }
}