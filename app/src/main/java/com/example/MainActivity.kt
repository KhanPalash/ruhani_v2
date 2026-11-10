package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.RuhaniFirestoreRepository
import com.example.ui.MainTab
import com.example.ui.RuhaniViewModel
import com.example.ui.SubScreen
import com.example.ui.screens.BabyNamesScreen
import com.example.ui.screens.DigitalTasbihScreen
import com.example.ui.screens.DuaAndDhikrScreen
import com.example.ui.screens.FatwaScreen
import com.example.ui.screens.HadithCollectionScreen
import com.example.ui.screens.HajjGuideScreen
import com.example.ui.screens.HaramainLiveScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.IslamicBooksScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.NamesOfAllahScreen
import com.example.ui.screens.NearbyMosquesScreen
import com.example.ui.screens.OnboardingAndAuthScreen
import com.example.ui.screens.PrayerScheduleScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QiblaCompassScreen
import com.example.ui.screens.QuranBrowserScreen
import com.example.ui.screens.RamadanHubScreen
import com.example.ui.screens.RuhaniTopBar
import com.example.ui.screens.SalatAndQazaTrackerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SurahReaderScreen
import com.example.ui.screens.TriCalendarScreen
import com.example.ui.screens.ZakatCalculatorScreen
import com.example.ui.theme.NoorEmeraldPrimary
import com.example.ui.theme.NoorMintLuminous
import com.example.ui.theme.RuhaniTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RuhaniAppRoot()
        }
    }
}

@Composable
fun RuhaniAppRoot() {
    val context = LocalContext.current
    val appContext = context.applicationContext

    var currentUserId by remember {
        mutableStateOf(
            try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseAuth.getInstance().currentUser?.uid
                } else null
            } catch (_: Exception) {
                null
            }
        )
    }
    var currentUserEmail by remember {
        mutableStateOf(
            try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseAuth.getInstance().currentUser?.email
                } else null
            } catch (_: Exception) {
                null
            }
        )
    }
    var currentUserName by remember {
        mutableStateOf(
            try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseAuth.getInstance().currentUser?.displayName
                } else null
            } catch (_: Exception) {
                null
            }
        )
    }

    DisposableEffect(Unit) {
        val auth = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseAuth.getInstance() else null
        } catch (_: Exception) {
            null
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUserId = firebaseAuth.currentUser?.uid
            currentUserEmail = firebaseAuth.currentUser?.email
            currentUserName = firebaseAuth.currentUser?.displayName
        }
        auth?.addAuthStateListener(listener)
        onDispose {
            auth?.removeAuthStateListener(listener)
        }
    }

    key(currentUserId) {
        val ruhaniViewModel: RuhaniViewModel = viewModel(
            key = currentUserId ?: "guest_session",
            factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = try {
                        if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                            val databaseId = appContext.getString(R.string.firestore_database_id)
                            val firestore = FirebaseFirestore.getInstance(databaseId)
                            RuhaniFirestoreRepository(firestore)
                        } else null
                    } catch (_: Exception) {
                        null
                    }
                    return RuhaniViewModel(
                        appContext = appContext,
                        firestoreRepository = repo,
                        authenticatedUserId = currentUserId,
                        authenticatedEmail = currentUserEmail,
                        authenticatedName = currentUserName
                    ) as T
                }
            }
        )

        val uiState by ruhaniViewModel.uiState.collectAsStateWithLifecycle()

        RuhaniTheme(themeMode = uiState.themeMode) {
            // Back handler for tab switching when at root subScreen
            BackHandler(
                enabled = uiState.activeSubScreen != SubScreen.NONE || uiState.currentTab != MainTab.HOME
            ) {
                ruhaniViewModel.navigateBack()
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (uiState.activeSubScreen != SubScreen.SURAH_READER) {
                        RuhaniTopBar(
                            uiState = uiState,
                            onSelectDistrict = ruhaniViewModel::updateDistrict,
                            onOpenSettings = { ruhaniViewModel.openSubScreen(SubScreen.SETTINGS) },
                            onOpenProfile = { ruhaniViewModel.openSubScreen(SubScreen.PROFILE) }
                        )
                    }
                },
                bottomBar = {
                    if (uiState.activeSubScreen != SubScreen.SURAH_READER) {
                        RuhaniBottomNavigationBar(
                            currentTab = uiState.currentTab,
                            isBn = uiState.language == "bn",
                            onSelectTab = ruhaniViewModel::selectTab
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (uiState.activeSubScreen) {
                        SubScreen.SURAH_READER -> SurahReaderScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onSelectSurah = ruhaniViewModel::openSurah,
                            onAdjustFontSize = ruhaniViewModel::adjustQuranFontSize,
                            onSelectQari = ruhaniViewModel::selectQari,
                            onToggleWordByWord = ruhaniViewModel::toggleWordByWord,
                            onToggleHifzArabic = ruhaniViewModel::toggleHifzHideArabic,
                            onSetMushafMode = ruhaniViewModel::setMushafMode,
                            onPlayOrPauseAyah = ruhaniViewModel::playOrPauseAyah,
                            onToggleGlobalPlayPause = ruhaniViewModel::toggleGlobalAudioPlayPause,
                            onStepAyahAudio = ruhaniViewModel::stepAyahAudio,
                            onToggleRepeat3x = ruhaniViewModel::toggleAudioRepeat3x,
                            onToggleTafsir = ruhaniViewModel::toggleTafsirForAyah,
                            onToggleBookmark = ruhaniViewModel::toggleBookmark
                        )

                        SubScreen.TASBIH -> DigitalTasbihScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onSelectPreset = ruhaniViewModel::selectTasbihPreset,
                            onIncrement = ruhaniViewModel::incrementTasbih,
                            onRequestReset = ruhaniViewModel::requestTasbihReset,
                            onDismissReset = ruhaniViewModel::dismissTasbihReset,
                            onConfirmReset = ruhaniViewModel::confirmTasbihReset,
                            onNewCycle = ruhaniViewModel::startNewTasbihCycle,
                            onToggleVibration = ruhaniViewModel::toggleTasbihVibration,
                            onToggleSound = ruhaniViewModel::toggleTasbihSound,
                            onSpeakArabic = ruhaniViewModel::speakArabicText
                        )

                        SubScreen.SALAT_TRACKER, SubScreen.NAWAFIL -> SalatAndQazaTrackerScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onToggleSalat = ruhaniViewModel::toggleDailySalat,
                            onAdjustQaza = ruhaniViewModel::adjustQazaCount
                        )

                        SubScreen.DUA_DHIKR -> DuaAndDhikrScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onSpeakArabic = ruhaniViewModel::speakArabicText
                        )

                        SubScreen.QIBLA -> QiblaCompassScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) }
                        )

                        SubScreen.CALENDAR -> TriCalendarScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onUpdateHijriOffset = ruhaniViewModel::updateHijriOffset
                        )

                        SubScreen.ZAKAT -> ZakatCalculatorScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onSetSilverNisab = ruhaniViewModel::setZakatNisabStandard,
                            onUpdateInputs = ruhaniViewModel::updateZakatInputs
                        )

                        SubScreen.RAMADAN -> RamadanHubScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onSpeakArabic = ruhaniViewModel::speakArabicText
                        )

                        SubScreen.NAMES_OF_ALLAH -> NamesOfAllahScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onSpeakArabic = ruhaniViewModel::speakArabicText
                        )

                        SubScreen.HADITH -> HadithCollectionScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) }
                        )

                        SubScreen.MOSQUES -> NearbyMosquesScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) }
                        )

                        SubScreen.BOOKS -> IslamicBooksScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) }
                        )

                        SubScreen.FATWA -> FatwaScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) }
                        )

                        SubScreen.HAJJ_GUIDE -> HajjGuideScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) }
                        )

                        SubScreen.BABY_NAMES -> BabyNamesScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) }
                        )

                        SubScreen.HARAMAIN_LIVE -> HaramainLiveScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onSelectChannel = ruhaniViewModel::selectHaramainChannel,
                            onTogglePlayback = ruhaniViewModel::toggleHaramainPlayback
                        )

                        SubScreen.PROFILE -> ProfileScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onUpdateProfile = ruhaniViewModel::updateProfileDetails,
                            onSyncNow = ruhaniViewModel::syncProfileToCloud
                        )

                        SubScreen.SETTINGS -> SettingsScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onUpdateTheme = ruhaniViewModel::updateThemeMode,
                            onUpdateLanguage = ruhaniViewModel::updateLanguage,
                            onToggleBanglaDigits = ruhaniViewModel::toggleBanglaDigits,
                            onUpdateDistrict = ruhaniViewModel::updateDistrict,
                            onUpdateMethod = ruhaniViewModel::updateCalculationMethod,
                            onUpdateMadhhab = ruhaniViewModel::updateMadhhab
                        )

                        SubScreen.NONE -> {
                            when (uiState.currentTab) {
                                MainTab.HOME -> HomeDashboardScreen(
                                    uiState = uiState,
                                    onSelectTab = ruhaniViewModel::selectTab,
                                    onOpenSubScreen = ruhaniViewModel::openSubScreen
                                )

                                MainTab.QURAN -> QuranBrowserScreen(
                                    uiState = uiState,
                                    onOpenSurah = ruhaniViewModel::openSurah,
                                    onToggleOfflineDownload = ruhaniViewModel::toggleOfflineSurahDownload
                                )

                                MainTab.PRAYER -> PrayerScheduleScreen(
                                    uiState = uiState,
                                    onShiftDay = ruhaniViewModel::shiftPrayerScheduleDay,
                                    onToggleAzanAlert = ruhaniViewModel::toggleAzanAlert,
                                    onSelectDistrict = ruhaniViewModel::updateDistrict,
                                    onSelectMethod = ruhaniViewModel::updateCalculationMethod
                                )

                                MainTab.MORE -> MoreHubScreen(
                                    uiState = uiState,
                                    onOpenSubScreen = ruhaniViewModel::openSubScreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RuhaniBottomNavigationBar(
    currentTab: MainTab,
    isBn: Boolean,
    onSelectTab: (MainTab) -> Unit
) {
    val navItems = listOf(
        Triple(MainTab.HOME, if (isBn) "হোম" else "Home", Icons.Default.Home),
        Triple(MainTab.QURAN, if (isBn) "কুরআন" else "Quran", Icons.AutoMirrored.Filled.MenuBook),
        Triple(MainTab.PRAYER, if (isBn) "নামাজ" else "Prayer", Icons.Default.Schedule),
        Triple(MainTab.MORE, if (isBn) "আরও" else "More", Icons.Default.GridView)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { (tab, label, icon) ->
            val selected = currentTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = NoorMintLuminous,
                    indicatorColor = NoorEmeraldPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
