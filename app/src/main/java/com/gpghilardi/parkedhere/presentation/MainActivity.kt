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
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.gpghilardi.parkedhere.R
import com.gpghilardi.parkedhere.presentation.theme.ParkedHereTheme

private lateinit var fusedLocationClient: FusedLocationProviderClient
private var lastStoredLocation: Location? = null

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure our smartwatch has a GPS receiver available...
        if (!hasGps()) {
            Log.e("ParkedHere", "This hardware doesn't have GPS.")
            return
        }

        initializeLocationServices()

        // Show the interface
        setContent {
            ParkedHearWearApp()
        }
    }

    /**
     * Initialize the LocationServices object, used for obtaining the current location.
     * Before initialiing it, this mehod checks for GPS-related Android permissions and
     * ask the user (popup) if they are missing
     */
    private fun initializeLocationServices() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("ParkedHere", "Precise location access granted.")
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("ParkedHere", "Only approximate location access granted.")
                }

                else -> {
                    Log.w("ParkedHere", "No location access granted.")
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
}

@Composable
fun ParkedHearWearApp() {
    ParkedHereTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ParkedHereButtons()
        }
    }
}

@SuppressLint("MissingPermission")
fun StoreLocation(context: Context) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                lastStoredLocation = location
                Toast.makeText(
                    context,
                    "Position stored!",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(
                    "ParkedHere", "Stored position: " +
                            lastStoredLocation?.latitude.toString() + ", " +
                            lastStoredLocation?.longitude.toString()
                )
            } else {
                Toast.makeText(
                    context,
                    "No position to store at this time!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}

@SuppressLint("PrivateResource")
@Composable
fun ParkedHereButtons() {
    val context = LocalContext.current

    // Fist button: "Set position"
    Button(
        onClick = {
            StoreLocation(context)
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF03A9F4),
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
            if (lastStoredLocation != null) {
                Toast.makeText(
                    context,
                    "Navigating to last stored position...",
                    Toast.LENGTH_SHORT
                ).show()
                val lat = lastStoredLocation?.latitude
                val lng = lastStoredLocation?.longitude
                val mapIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q=$lat,$lng")
                )
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                }
            } else {
                Toast.makeText(
                    context,
                    "No location stored!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF03A9F4),
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

@Preview(device = Devices.WEAR_OS_LARGE_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ParkedHearWearApp()
}