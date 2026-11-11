package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appName_isRuhani() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ruhani", appName)
    }

    @Test
    fun homeDashboard_displaysHeroWaqtAndNavigatesBetweenTabs() {
        composeTestRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hero_current_waqt_card").assertIsDisplayed()

        // Navigate to Prayer Schedule tab and scroll to Prohibited (Makruh) card
        composeTestRule.onNodeWithTag("nav_tab_prayer").performClick()
        composeTestRule.onNodeWithTag("prayer_schedule_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("prayer_schedule_list")
            .performScrollToNode(hasTestTag("prohibited_makruh_card"))
        composeTestRule.onNodeWithTag("prohibited_makruh_card").assertIsDisplayed()

        // Navigate to Quran Browser tab and open Surah Al-Fatihah
        composeTestRule.onNodeWithTag("nav_tab_quran").performClick()
        composeTestRule.onNodeWithTag("quran_browser_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("quran_last_read_banner").performClick()
        composeTestRule.onNodeWithTag("surah_reader_screen").assertIsDisplayed()

        // Navigate back to Quran Browser and then to More Hub
        composeTestRule.onNodeWithTag("surah_reader_back_btn").performClick()
        composeTestRule.onNodeWithTag("nav_tab_more").performClick()
        composeTestRule.onNodeWithTag("more_hub_list").assertIsDisplayed()
    }

    @Test
    fun digitalTasbih_opensIncrementsAndResetsWithLocalPersistence() {
        composeTestRule.onNodeWithTag("home_dashboard_list")
            .performScrollToNode(hasTestTag("feature_card_tasbih"))
        composeTestRule.onNodeWithTag("feature_card_tasbih").performClick()
        composeTestRule.onNodeWithTag("digital_tasbih_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("digital_tasbih_screen")
            .performScrollToNode(hasTestTag("tasbih_increment_button"))
        composeTestRule.onNodeWithTag("tasbih_increment_button").performClick()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE)
        val savedAfterIncrement = prefs.getInt("tasbih_current", -1)
        assertEquals(24, savedAfterIncrement)

        composeTestRule.onNodeWithTag("tasbih_reset_button").performClick()
        composeTestRule.onNodeWithTag("tasbih_confirm_reset_button").performClick()
        val savedAfterReset = prefs.getInt("tasbih_current", -1)
        assertEquals(0, savedAfterReset)

        composeTestRule.onNodeWithTag("digital_tasbih_screen")
            .performScrollToNode(hasTestTag("tasbih_back_button"))
        composeTestRule.onNodeWithTag("tasbih_back_button").performClick()
        composeTestRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()
    }

    @Test
    fun categorizedDuas_opensAndDisplaysSearchBarAndCategories() {
        composeTestRule.onNodeWithTag("home_dashboard_list")
            .performScrollToNode(hasTestTag("home_categorized_duas_spotlight"))
        composeTestRule.onNodeWithTag("home_categorized_duas_spotlight").performClick()
        composeTestRule.onNodeWithTag("dua_dhikr_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dua_search_bar").assertIsDisplayed()
    }
}
