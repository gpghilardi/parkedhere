package com.gpghilardi.parkedhere.presentation.viewmodel

import android.location.Location
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.location.LocationServices
import com.gpghilardi.parkedhere.presentation.repository.LocationStorage
import com.gpghilardi.parkedhere.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()


    lateinit var viewModel: LocationViewModel


    @Before
    fun setup() {
//        locationStorage = mockk()
//        mockLocationServices = mockk()

    }

    @Test
    fun locationNull() = runTest {
        var locationStorage: LocationStorage = mockk(relaxed = true)

        every {
            locationStorage.getLocation()
        } returns flow {
            emit(null as Location?)
        }

        val locationService =
            LocationServices.getFusedLocationProviderClient(InstrumentationRegistry.getInstrumentation().targetContext)
                .apply {
                    setMockMode(true)
                }

        viewModel = LocationViewModel(
            InstrumentationRegistry.getInstrumentation().targetContext,
            locationStorage,
            locationService
        )

        val location = viewModel.lastLocation.first()

        Assert.assertTrue(location == null)
    }

    @Test
    fun setLocationGetData() = runTest {
        var locationStorage: LocationStorage = mockk(relaxed = true)
        val location = Location("").apply {
            latitude = 1.0
            longitude = 2.0
        }

        val locationService =
            LocationServices.getFusedLocationProviderClient(InstrumentationRegistry.getInstrumentation().targetContext)
                .apply {
                    setMockMode(true)
                    setMockLocation(location)
                }.run {
                    spyk(this)
                }

        every {
            locationStorage.getLocation()
        } returns flow {
            emit(null as Location?)
        }

        viewModel = LocationViewModel(
            InstrumentationRegistry.getInstrumentation().targetContext,
            locationStorage,
            locationService
        )

        viewModel.saveLocation()

        coVerify {
            locationStorage.getLocation()
        }

        coVerifySequence {
            locationService.lastLocation
            locationStorage.setLocation(any())
        }


        confirmVerified(locationService, locationStorage)


    }
}