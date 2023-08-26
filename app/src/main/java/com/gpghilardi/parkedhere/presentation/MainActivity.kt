package com.gpghilardi.parkedhere.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.gpghilardi.parkedhere.R
import com.gpghilardi.parkedhere.presentation.theme.ParkedHereBlue
import com.gpghilardi.parkedhere.presentation.theme.ParkedHereTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * Global constants
 */
private val TAG: String = "ParkedHere"
private val PREFIX: String = "ParkedHere"
private val Context.locationDataStore by preferencesDataStore("LOCATION_STORE")

/**
 * Global variables
 */
private lateinit var fusedLocationClient: FusedLocationProviderClient

/**
 * MainActivity: this app contains just this single Activity, starting everything.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If we've previously stored a location data in the app's own data store, use it!
        locationStorage = LocationStorage(this)

        // Ensure our device has a GPS receiver available...
        if (!hasGps()) {
            Log.e(TAG, PREFIX + ": this hardware doesn't have a GPS receiver.")
            return
        }

        // Initialize the location services (it also checks for the proper permissions)
        initializeLocationServices()

        // Show the interface
        setContent {
            ParkedHearWearApp(locationStorage)
        }
    }

    /**
     * Initialize the LocationServices object, used for obtaining the current location.
     * Before initializing it, this method checks for GPS-related Android permissions and
     * asks the user (via a popup) if they are missing
     */
    private fun initializeLocationServices() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d(TAG, PREFIX + ": precise location access granted.")
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d(TAG, PREFIX + ": only approximate location access granted.")
                }

                else -> {
                    Log.w(TAG, PREFIX + ": no location access granted.")
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // No permissions? Ask the user for thme (via popup)...
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun hasGps(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

    private lateinit var locationStorage: LocationStorage
}

/**
 * This simple class is used for make location data persistent across app restarts.
 * The purpose is to protect the location data set by user from app crashes or os-forced app kills.
 */
class LocationStorage(val context: Context) {
    private val locationLatitudeKey = doublePreferencesKey("LOCATION_LATITUDE")
    private val locationLongitudeKey = doublePreferencesKey("LOCATION_LONGITUDE")

    /**
     * Store the location data in app's own data store
     */
    fun setLocation(location: Location) {
        // We block the async call and wait for the values being properly stored
        // Note: we just set a couple of scalar values when the user presses a button!
        runBlocking {
            context.locationDataStore.edit {
                it[locationLatitudeKey] = location.latitude
                it[locationLongitudeKey] = location.longitude
                Log.d(
                    TAG, PREFIX + ": LocationStorage: location stored: " +
                            it[locationLatitudeKey].toString() + ", " +
                            it[locationLongitudeKey].toString()
                )
            }
        }
    }

    /**
     * Read the location data from app's own data store (it may be null,
     * if nothing was previously stored)
     */
    fun getLocation(): Location? {
        // We block the async call until our data is read
        // Note: we just try to get a couple of scalar values once in a while,
        //       when the user presses a button!
        val ret = runBlocking {
            context.locationDataStore.data.map {
                val latitude = it[locationLatitudeKey]
                val longitude = it[locationLongitudeKey]
                if (latitude == null || longitude == null) {
                    return@map null
                }

                val ret = Location("") //provider name is unnecessary
                ret.latitude = latitude
                ret.longitude = longitude
                return@map ret
            }.first()
        }

        if (ret != null) {
            Log.d(
                TAG, PREFIX + ": LocationStorage: found location stored: " +
                        ret.latitude.toString() + ", " +
                        ret.longitude.toString()
            )
        } else {
            Log.d(
                TAG, PREFIX + ": LocationStorage: no location previously stored!"
            )
        }

        return ret
    }
}

/**
 * This create the app UI structure: a single vertically- and horizontally-centered
 * column for other UI components
 */
@Composable
fun ParkedHearWearApp(locationStorage: LocationStorage) {
    ParkedHereTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ParkedHereButtons(locationStorage)
        }
    }
}

/**
 * Callback function invoked by the "Set position" button for actually storing the location data
 */
@SuppressLint("MissingPermission")
fun StoreLocation(context: Context, locationStorage: LocationStorage) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                Toast.makeText(
                    context,
                    "Position stored!",
                    Toast.LENGTH_SHORT
                ).show()
                locationStorage.setLocation(location)
                val lastStoredLocation = locationStorage.getLocation()
                Log.d(
                    TAG, PREFIX + ": new position stored: " +
                            lastStoredLocation?.latitude.toString() + ", " +
                            lastStoredLocation?.longitude.toString()
                )
            } else {
                Toast.makeText(
                    context,
                    PREFIX + ": no position to store at this time!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}

/**
 * Callback function invoked by the "Navigate" button for opening Google Maps
 * to the previously stored location data (if any)
 */
private fun NavigateToStoredLocation(context: Context, locationStorage: LocationStorage) {
    val lastStoredLocation = locationStorage.getLocation()
    if (lastStoredLocation != null) {
        Toast.makeText(
            context,
            "Navigating to last stored position...",
            Toast.LENGTH_SHORT
        ).show()
        val lat = lastStoredLocation.latitude
        val lng = lastStoredLocation.longitude
        val mapIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("google.navigation:q=$lat,$lng")
        )
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
            Log.d(
                TAG, PREFIX + ": opening Google Maps to coords: " +
                        lastStoredLocation.latitude.toString() + ", " +
                        lastStoredLocation.longitude.toString()
            )
        } else {
            Log.w(
                TAG, PREFIX + ": cannot open Google Maps to coords: " +
                        lastStoredLocation.latitude.toString() + ", " +
                        lastStoredLocation.longitude.toString()
            )
        }
    } else {
        Toast.makeText(
            context,
            "No location stored!",
            Toast.LENGTH_SHORT
        ).show()
    }
}

/**
 * This create the app two buttons
 */
@SuppressLint("PrivateResource")
@Composable
fun ParkedHereButtons(locationStorage: LocationStorage) {
    val context = LocalContext.current

    // Fist button: "Set position"
    Button(
        onClick = {
            StoreLocation(context, locationStorage)
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ParkedHereBlue,
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 4.dp, top = 4.dp)
    )
    {
        Row {
            Icon(
                painter = painterResource(R.drawable.baseline_add_location_24),
                contentDescription = "Set position"
            )
            Text(text = "Set position", Modifier.padding(start = 10.dp))
        }
    }

    // Second button: "Navigate"
    Button(
        onClick = {
            NavigateToStoredLocation(context, locationStorage)
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ParkedHereBlue,
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
    )
    {
        Row {
            Icon(
                painter = painterResource(R.drawable.baseline_navigation_24),
                contentDescription = "Navigate"
            )
            Text(text = "Navigate", Modifier.padding(start = 10.dp))
        }
    }
}

/**
 * Required for preview the app UI in Android Studio
 */
@Preview(device = Devices.WEAR_OS_LARGE_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val locationStorage = LocationStorage(LocalContext.current)
    ParkedHearWearApp(locationStorage)
}