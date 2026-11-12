package com.khanscreations.ruhani

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.khanscreations.ruhani.domain.CalculationMethod
import com.khanscreations.ruhani.domain.Madhhab
import com.khanscreations.ruhani.domain.MushafMode
import com.khanscreations.ruhani.domain.QuranCatalog
import com.khanscreations.ruhani.domain.RuhaniContentCatalog
import com.khanscreations.ruhani.domain.WaqtType
import com.khanscreations.ruhani.ui.MainTab
import com.khanscreations.ruhani.ui.PrayerViewModel
import com.khanscreations.ruhani.ui.QuranAudioViewModel
import com.khanscreations.ruhani.ui.RuhaniViewModel
import com.khanscreations.ruhani.ui.SubScreen
import com.khanscreations.ruhani.ui.SyncAndAccountViewModel
import com.khanscreations.ruhani.ui.TasbihViewModel
import com.khanscreations.ruhani.ui.WorshipToolsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ModularViewModelsTest {

    private lateinit var context: Context
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("ruhani_local_prefs", Context.MODE_PRIVATE).edit().clear().apply()
    }

    @Test
    fun prayerViewModel_updatesSettingsAndSchedule() {
        val vm = PrayerViewModel(appContext = context, externalScope = testScope)
        assertNotNull(vm.prayerState.value.schedule)

        val chittagong = RuhaniContentCatalog.bdDistricts.first { it.id == "Chattogram" }
        vm.updateDistrict(chittagong)
        assertEquals("Chattogram", vm.prayerState.value.selectedDistrict.id)

        vm.updateCalculationMethod(CalculationMethod.KARACHI)
        assertEquals(CalculationMethod.KARACHI, vm.prayerState.value.calculationMethod)

        vm.updateMadhhab(Madhhab.SHAFI)
        assertEquals(Madhhab.SHAFI, vm.prayerState.value.madhhab)

        vm.updateHijriOffset(1)
        assertEquals(1, vm.prayerState.value.hijriOffsetDays)

        vm.shiftPrayerScheduleDay(1)
        assertEquals(1, vm.prayerState.value.prayerDayOffset)

        val currentFajrAlert = vm.prayerState.value.azanAlerts[WaqtType.FAJR] ?: true
        vm.toggleAzanAlert(WaqtType.FAJR)
        assertEquals(!currentFajrAlert, vm.prayerState.value.azanAlerts[WaqtType.FAJR])

        vm.cleanup()
    }

    @Test
    fun tasbihViewModel_incrementsCountsAndHandlesCycles() {
        val vm = TasbihViewModel(appContext = context, externalScope = testScope)
        assertEquals(0, vm.tasbihState.value.tasbihCurrentCount)
        assertEquals(1, vm.tasbihState.value.tasbihCycle)

        vm.updateTasbihTarget(3)
        assertEquals(3, vm.tasbihState.value.tasbihTarget)

        vm.incrementTasbih()
        assertEquals(1, vm.tasbihState.value.tasbihCurrentCount)
        assertEquals(1, vm.tasbihState.value.tasbihTodayTotal)

        vm.incrementTasbih()
        vm.incrementTasbih()
        assertEquals(3, vm.tasbihState.value.tasbihCurrentCount)

        // Rolling over to next cycle
        vm.incrementTasbih()
        assertEquals(1, vm.tasbihState.value.tasbihCurrentCount)
        assertEquals(2, vm.tasbihState.value.tasbihCycle)

        // Reset flow
        vm.requestTasbihReset()
        assertTrue(vm.tasbihState.value.showTasbihResetDialog)
        vm.confirmTasbihReset()
        assertFalse(vm.tasbihState.value.showTasbihResetDialog)
        assertEquals(0, vm.tasbihState.value.tasbihCurrentCount)
        assertEquals(1, vm.tasbihState.value.tasbihCycle)

        // Dua counting
        val duaId = RuhaniContentCatalog.duas.first().id
        vm.incrementDuaCount(duaId, 33)
        assertEquals(1, vm.tasbihState.value.duaCountsById[duaId])
        vm.resetDuaCount(duaId)
        assertEquals(0, vm.tasbihState.value.duaCountsById[duaId])

        vm.cleanup()
    }

    @Test
    fun quranAudioViewModel_managesReaderStateAndPlaybackControls() {
        val vm = QuranAudioViewModel(appContext = context, externalScope = testScope)
        val initialSize = vm.quranAudioState.value.quranArabicFontSizeSp

        vm.adjustQuranFontSize(2)
        assertEquals(initialSize + 2, vm.quranAudioState.value.quranArabicFontSizeSp)

        vm.setMushafMode(MushafMode.WORD_BY_WORD)
        assertEquals(MushafMode.WORD_BY_WORD, vm.quranAudioState.value.mushafMode)
        assertTrue(vm.quranAudioState.value.showWordByWord)

        vm.toggleWordByWord()
        assertFalse(vm.quranAudioState.value.showWordByWord)

        vm.toggleHifzHideArabic()
        assertTrue(vm.quranAudioState.value.hifzHideArabic)

        vm.toggleHifzHideTranslation()
        assertTrue(vm.quranAudioState.value.hifzHideTranslation)

        vm.toggleAudioRepeat3x()
        assertTrue(vm.quranAudioState.value.audioRepeat3x)

        val surah2 = QuranCatalog.surahs[1]
        vm.openSurah(surah2)
        assertEquals(2, vm.quranAudioState.value.selectedSurah.number)
        assertEquals(2, vm.quranAudioState.value.playingSurahNumber)

        vm.stopAndDismissAudio()
        assertFalse(vm.quranAudioState.value.isAudioPlaying)

        vm.cleanup()
    }

    @Test
    fun syncAndAccountViewModel_managesOnboardingSettingsAndNotifications() {
        val vm = SyncAndAccountViewModel(appContext = context, externalScope = testScope)
        assertFalse(vm.syncAndAccountState.value.hasCompletedOnboarding)

        vm.completeOnboarding(asGuest = true)
        assertTrue(vm.syncAndAccountState.value.hasCompletedOnboarding)
        assertTrue(vm.syncAndAccountState.value.isGuestMode)

        vm.updateThemeMode("LIGHT")
        assertEquals("LIGHT", vm.syncAndAccountState.value.themeMode)

        vm.updateLanguage("en")
        assertEquals("en", vm.syncAndAccountState.value.language)

        vm.toggleBanglaDigits()
        assertTrue(vm.syncAndAccountState.value.useBanglaDigits)

        vm.updateProfileDetails("আব্দুল্লাহ", "01700000000")
        assertEquals("আব্দুল্লাহ", vm.syncAndAccountState.value.displayName)
        assertEquals("01700000000", vm.syncAndAccountState.value.phone)

        vm.cleanup()
    }

    @Test
    fun worshipToolsViewModel_handlesNavigationAndSalatTracking() {
        val vm = WorshipToolsViewModel(appContext = context, externalScope = testScope)
        assertEquals(MainTab.HOME, vm.worshipToolsState.value.currentTab)
        assertEquals(SubScreen.NONE, vm.worshipToolsState.value.activeSubScreen)

        vm.selectTab(MainTab.PRAYER)
        assertEquals(MainTab.PRAYER, vm.worshipToolsState.value.currentTab)

        vm.openSubScreen(SubScreen.TASBIH)
        assertEquals(SubScreen.TASBIH, vm.worshipToolsState.value.activeSubScreen)

        val handled = vm.navigateBack()
        assertTrue(handled)
        assertEquals(SubScreen.NONE, vm.worshipToolsState.value.activeSubScreen)

        vm.adjustQazaCount("fajr", 2)
        assertEquals(2L, vm.worshipToolsState.value.qazaFajr)
        vm.adjustQazaCount("fajr", -1)
        assertEquals(1L, vm.worshipToolsState.value.qazaFajr)

        vm.setZakatNisabStandard(false)
        assertFalse(vm.worshipToolsState.value.zakatUseSilverNisab)

        vm.selectHaramainChannel("MADINAH")
        assertEquals("MADINAH", vm.worshipToolsState.value.haramainChannel)

        vm.cleanup()
    }

    @Test
    fun ruhaniViewModel_facadeCoordinatesAllFeaturesSeamlessly() {
        val ruhaniVm = RuhaniViewModel(
            appContext = context,
            firestoreRepository = null,
            authenticatedUserId = null,
            authenticatedEmail = null,
            authenticatedName = null
        )

        // Verify initial composition
        assertNotNull(ruhaniVm.uiState.value.schedule)
        assertEquals(0, ruhaniVm.uiState.value.tasbihCurrentCount)
        assertEquals(MainTab.HOME, ruhaniVm.uiState.value.currentTab)

        // Test delegation
        ruhaniVm.incrementTasbih()
        assertEquals(1, ruhaniVm.tasbihViewModel.tasbihState.value.tasbihCurrentCount)

        ruhaniVm.selectTab(MainTab.MORE)
        assertEquals(MainTab.MORE, ruhaniVm.worshipToolsViewModel.worshipToolsState.value.currentTab)

        ruhaniVm.openSubScreen(SubScreen.SETTINGS)
        assertEquals(SubScreen.SETTINGS, ruhaniVm.worshipToolsViewModel.worshipToolsState.value.activeSubScreen)

        val popped = ruhaniVm.navigateBack()
        assertTrue(popped)
        assertEquals(SubScreen.NONE, ruhaniVm.worshipToolsViewModel.worshipToolsState.value.activeSubScreen)

        ruhaniVm.adjustQazaCount("dhuhr", 3L)
        assertEquals(3L, ruhaniVm.worshipToolsViewModel.worshipToolsState.value.qazaDhuhr)
    }
}
