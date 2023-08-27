/**
 * MIT License
 *
 * Copyright (c) 2023 Gian Paolo Ghilardi and other contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gpghilardi.parkedhere.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.gpghilardi.parkedhere.R
import com.gpghilardi.parkedhere.presentation.di.appModule
import com.gpghilardi.parkedhere.presentation.theme.ParkedHereBlue
import com.gpghilardi.parkedhere.presentation.theme.ParkedHereTheme
import com.gpghilardi.parkedhere.presentation.viewmodel.LocationViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin

/**
 * Global constants
 */
const val TAG: String = "ParkedHere"
const val PREFIX: String = "ParkedHere"

/**
 * MainActivity: this app contains just this single Activity, starting everything.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startKoin {
            androidLogger()
            androidContext(applicationContext)
            modules(appModule)
        }

        // Ensure our device has a GPS receiver available...
        if (!hasGps()) {
            Log.e(TAG, "$PREFIX: this device lacks a GPS receiver.")
            return
        }

        // Initialize the location services (it also checks for the proper permissions)
        initializeLocationServices()

        // Show the interface
        setContent {
            val locationViewModel = koinViewModel<LocationViewModel>()
            val lastLocation by locationViewModel.lastLocation.collectAsState(initial = null)


            ParkedHearWearApp(
                lastLocation = lastLocation,
                onSetPositionClicked = {
                    locationViewModel.saveLocation()
                },
                onNavigateLastClicked = {
                    lastLocation?.let { navigateToStoredLocation(this, it) }
                }
            )
        }
    }

    /**
     * Initialize the LocationServices object, used for obtaining the current location.
     * Before initializing it, this method checks for location-related Android permissions and
     * asks the user (via a popup) if they are missing
     */
    private fun initializeLocationServices() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d(TAG, "$PREFIX: precise location access granted.")
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d(TAG, "$PREFIX: only approximate location access granted.")
                }

                else -> {
                    Log.w(TAG, "$PREFIX: no location access granted.")
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
            // No permissions? Ask the user for them (via popup)...
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun hasGps(): Boolean =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)

}


/**
 * This create the app's UI structure: a single vertically- and horizontally-centered
 * column for nesting other UI components
 */
@Composable
fun ParkedHearWearApp(
    lastLocation: Location?,
    onSetPositionClicked: () -> Unit,
    onNavigateLastClicked: () -> Unit
) {
    ParkedHereTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fist button: "Set position"
            Button(
                onClick = onSetPositionClicked,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ParkedHereBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 4.dp, top = 4.dp)
            ) {
                Row {
                    Icon(
                        painter = painterResource(R.drawable.baseline_add_location_24),
                        contentDescription = "Set position"
                    )
                    Text(text = "Set position", Modifier.padding(start = 10.dp))
                }
            }

            // Second button: "Navigate"
            if (lastLocation != null) {
                Button(
                    onClick = onNavigateLastClicked,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ParkedHereBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
                ) {
                    Row {
                        Icon(
                            painter = painterResource(R.drawable.baseline_navigation_24),
                            contentDescription = "Navigate"
                        )
                        Text(text = "Navigate", Modifier.padding(start = 10.dp))
                    }
                }
            }
        }
    }
}

/**
 * Callback function invoked by the "Navigate" button for opening Google Maps, pointing it
 * to the previously stored location data (if any)
 */
private fun navigateToStoredLocation(context: Context, location: Location) {
    val mapIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("google.navigation:q=${location.latitude},${location.longitude}")
    )
    mapIntent.setPackage("com.google.android.apps.maps")
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent) // Open Google Maps...
        Log.d(
            TAG,
            PREFIX + ": opening Google Maps to coords: ${location.latitude}, ${location.longitude}"
        )
    } else {
        Log.w(
            TAG,
            PREFIX + ": cannot open Google Maps to coords: ${location.latitude}, ${location.longitude}"

        )
    }
}

/**
 * Required for previewing the app UI in Android Studio
 */
@Preview(device = Devices.WEAR_OS_LARGE_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    ParkedHearWearApp(
        lastLocation = Location(""),
        onNavigateLastClicked = {},
        onSetPositionClicked = {}
    )
}
