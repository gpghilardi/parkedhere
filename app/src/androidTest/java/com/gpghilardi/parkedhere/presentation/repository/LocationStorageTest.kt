package com.gpghilardi.parkedhere.presentation.repository

import android.location.Location
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.test.platform.app.InstrumentationRegistry
import com.gpghilardi.parkedhere.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocationStorageTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var locationDataStore: DataStore<Preferences>
    private lateinit var locationStore: LocationStorage

    private val locationLatitudeKey = doublePreferencesKey(LOCATION_LATITUDE)
    private val locationLongitudeKey = doublePreferencesKey(LOCATION_LONGITUDE)

    @Before
    fun setup() {
        locationDataStore =
            InstrumentationRegistry.getInstrumentation().targetContext.getLocationDataStoreTest()
        runBlocking {
            locationDataStore.edit {
                it.clear()
            }
        }
        locationStore = LocationStorage(InstrumentationRegistry.getInstrumentation().context)
    }

    @Test
    fun getLocationEmpty() = runTest {
        val location = locationStore.getLocation().first()
        Assert.assertTrue(location == null)
    }

    @Test
    fun setLocationAndCheckValorized() = runTest {
        val fakeLocation = getFakeLocation()
        locationStore.setLocation(fakeLocation)

        val location = locationDataStore.data.map {
            it[locationLatitudeKey] to it[locationLongitudeKey]
        }.first()

        Assert.assertEquals(fakeLocation.latitude to fakeLocation.longitude, location)
    }

    @Test
    fun setLocationAndGetLocationCheckValorized() = runTest {
        val fakeLocation = getFakeLocation()
        locationStore.setLocation(fakeLocation)

        val location = locationStore.getLocation().first()

        Assert.assertEquals(
            fakeLocation.latitude to fakeLocation.longitude,
            location!!.latitude to location.longitude
        )
    }

    @Test
    fun setLocationNullAndGetLocationCheckNull() = runTest {
        locationStore.setLocation(null)

        val location = locationStore.getLocation().first()

        Assert.assertTrue(location == null)
    }

    private fun getFakeLocation() = Location("").apply {
        latitude = 1.0
        longitude = 2.0
    }
}