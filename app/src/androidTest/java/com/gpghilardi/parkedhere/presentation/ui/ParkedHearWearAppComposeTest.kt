package com.gpghilardi.parkedhere.presentation.ui

import android.location.Location
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import com.gpghilardi.parkedhere.presentation.ParkedHearWearApp
import org.junit.Rule
import org.junit.Test

class ParkedHearWearAppComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun checkSetPositionExistAndNavigateNot() {
        composeTestRule.setContent {
            ParkedHearWearApp(
                lastLocation = null,
                onSetPositionClicked = {

                },
                onNavigateLastClicked = {

                }
            )
        }

        composeTestRule.onRoot(useUnmergedTree = true).printToLog("TAG")

        composeTestRule.onNodeWithTag("setLocationButton", useUnmergedTree = true)
            .assertExists("Set position not found")
            .assertIsDisplayed()
            .onChildren()
            .assertAny(hasText("Set position"))

        composeTestRule.onNodeWithTag("navigateButton")
            .assertDoesNotExist()
    }

    @Test
    fun checkSetPositionAndNavigateExist() {
        composeTestRule.setContent {
            ParkedHearWearApp(
                lastLocation = Location(""),
                onSetPositionClicked = {

                },
                onNavigateLastClicked = {

                }
            )
        }

        composeTestRule.onNodeWithTag("setLocationButton", useUnmergedTree = true)
            .assertExists("Set position not found")
            .assertIsDisplayed()
            .onChildren()
            .assertAny(hasText("Set position"))

        composeTestRule.onNodeWithTag("navigateButton", useUnmergedTree = true)
            .assertExists("navigate not found")
            .assertIsDisplayed()
            .onChildren()
            .assertAny(hasText("Navigate"))
    }
}