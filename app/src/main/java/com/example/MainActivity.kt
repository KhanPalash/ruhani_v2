package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.text.style.TextOverflow
import com.example.domain.QuranCatalog
import com.example.ui.RuhaniUiState
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PureWhite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            if (!uiState.hasCompletedOnboarding) {
                OnboardingAndAuthScreen(
                    uiState = uiState,
                    onSelectDistrict = ruhaniViewModel::updateDistrict,
                    onSelectMadhhab = ruhaniViewModel::updateMadhhab,
                    onCompleteOnboarding = { isGuest ->
                        ruhaniViewModel.completeOnboarding(isGuest)
                    },
                    onRegisterEmailPassword = { name, email, pass, consent ->
                        ruhaniViewModel.registerWithEmailPassword(name, email, pass, consent)
                    },
                    onLoginEmailPassword = { email, pass ->
                        ruhaniViewModel.loginWithEmailPassword(email, pass)
                    }
                )
            } else {
                // Back handler for tab switching when at root subScreen
                BackHandler(
                enabled = uiState.activeSubScreen != SubScreen.NONE || uiState.currentTab != MainTab.HOME
            ) {
                ruhaniViewModel.navigateBack()
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    if (uiState.activeSubScreen == SubScreen.NONE &&
                        uiState.currentTab == MainTab.HOME
                    ) {
                        RuhaniTopBar(
                            uiState = uiState,
                            onSelectDistrict = ruhaniViewModel::updateDistrict,
                            onOpenSettings = { ruhaniViewModel.openSubScreen(SubScreen.SETTINGS) },
                            onOpenProfile = { ruhaniViewModel.openSubScreen(SubScreen.PROFILE) }
                        )
                    }
                },
                bottomBar = {
                    Column {
                        if ((uiState.isAudioPlaying || uiState.playingSurahNumber != null) &&
                            uiState.activeSubScreen != SubScreen.SURAH_READER
                        ) {
                            QuranGlobalMiniPlayer(
                                uiState = uiState,
                                onTogglePlayPause = ruhaniViewModel::toggleGlobalAudioPlayPause,
                                onClose = ruhaniViewModel::stopAndDismissAudio,
                                onOpenSurah = {
                                    val surahNum = uiState.playingSurahNumber ?: 1
                                    val targetSurah = QuranCatalog.surahs.firstOrNull { it.number == surahNum }
                                    if (targetSurah != null) {
                                        ruhaniViewModel.openSurah(targetSurah)
                                    }
                                }
                            )
                        }
                        if (uiState.activeSubScreen != SubScreen.SURAH_READER &&
                            uiState.activeSubScreen != SubScreen.QIBLA
                        ) {
                            RuhaniBottomNavigationBar(
                                currentTab = uiState.currentTab,
                                isBn = uiState.language == "bn",
                                onSelectTab = ruhaniViewModel::selectTab
                            )
                        }
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
                            onToggleAutoAdvance = ruhaniViewModel::toggleAudioAutoAdvance,
                            onToggleTafsir = ruhaniViewModel::toggleTafsirForAyah,
                            onToggleBookmark = ruhaniViewModel::toggleBookmark,
                            onDownloadAyah = ruhaniViewModel::downloadAyahAudio,
                            onCloseAudio = ruhaniViewModel::stopAndDismissAudio
                        )

                        SubScreen.TASBIH -> DigitalTasbihScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onOpenSettings = { ruhaniViewModel.openSubScreen(SubScreen.SETTINGS) },
                            onSelectPreset = ruhaniViewModel::selectTasbihPreset,
                            onIncrement = ruhaniViewModel::incrementTasbih,
                            onRequestReset = ruhaniViewModel::requestTasbihReset,
                            onDismissReset = ruhaniViewModel::dismissTasbihReset,
                            onConfirmReset = ruhaniViewModel::confirmTasbihReset,
                            onNewCycle = ruhaniViewModel::startNewTasbihCycle,
                            onToggleVibration = ruhaniViewModel::toggleTasbihVibration,
                            onToggleSound = ruhaniViewModel::toggleTasbihSound,
                            onSpeakArabic = ruhaniViewModel::speakArabicText,
                            onUpdateTarget = ruhaniViewModel::updateTasbihTarget
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
                            onSpeakArabic = ruhaniViewModel::speakArabicText,
                            onIncrementDuaCount = ruhaniViewModel::incrementDuaCount,
                            onResetDuaCount = ruhaniViewModel::resetDuaCount,
                            onToggleFavoriteDua = ruhaniViewModel::toggleFavoriteDua
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
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onSpeakArabic = ruhaniViewModel::speakArabicText
                        )

                        SubScreen.MOSQUES -> NearbyMosquesScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) }
                        )

                        SubScreen.BOOKS -> IslamicBooksScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onToggleArticleOffline = ruhaniViewModel::toggleOfflineArticleDownload,
                            onUpdateArticleProgress = ruhaniViewModel::updateArticleReadingProgress,
                            onPlayCompanionAudio = { surahNo, ayahNo ->
                                val surah = com.example.domain.QuranCatalog.surahs.firstOrNull { it.number == surahNo }
                                    ?: com.example.domain.QuranCatalog.surahs.first()
                                val ayah = surah.ayahs.firstOrNull { it.ayahNumber == ayahNo } ?: surah.ayahs.first()
                                ruhaniViewModel.playOrPauseAyah(surah, ayah)
                            },
                            onSyncOfflineNow = ruhaniViewModel::synchronizeOfflineChangesNow
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
                            onSyncNow = ruhaniViewModel::syncProfileToCloud,
                            onRegisterEmailPassword = ruhaniViewModel::registerWithEmailPassword,
                            onLoginEmailPassword = ruhaniViewModel::loginWithEmailPassword,
                            onSignOutSession = ruhaniViewModel::signOutUserSession,
                            onUpdatePrivacySettings = ruhaniViewModel::updatePrivacySettings,
                            onExportPrivacyData = ruhaniViewModel::exportMyPrivacyData,
                            onDismissExportedData = ruhaniViewModel::dismissExportedPrivacyData,
                            onDeleteAccountAndData = ruhaniViewModel::deleteAccountAndLocalData,
                            onSynchronizeOfflineQueue = ruhaniViewModel::synchronizeOfflineChangesNow,
                            onToggleNotificationPref = ruhaniViewModel::toggleNotificationPreference,
                            onSendPersonalizedAlert = ruhaniViewModel::triggerPersonalizedActivityNotification,
                            onSendNewContentAlert = ruhaniViewModel::triggerNewContentNotification,
                            onSendWaqtTestAlert = ruhaniViewModel::triggerWaqtTestNotification,
                            onMarkAllNotificationsRead = ruhaniViewModel::markAllNotificationsRead
                        )

                        SubScreen.SETTINGS -> SettingsScreen(
                            uiState = uiState,
                            onBack = { ruhaniViewModel.openSubScreen(SubScreen.NONE) },
                            onUpdateTheme = ruhaniViewModel::updateThemeMode,
                            onUpdateLanguage = ruhaniViewModel::updateLanguage,
                            onToggleBanglaDigits = ruhaniViewModel::toggleBanglaDigits,
                            onUpdateDistrict = ruhaniViewModel::updateDistrict,
                            onUpdateMethod = ruhaniViewModel::updateCalculationMethod,
                            onUpdateMadhhab = ruhaniViewModel::updateMadhhab,
                            onToggleTasbihVibration = ruhaniViewModel::toggleTasbihVibration,
                            onToggleTasbihSound = ruhaniViewModel::toggleTasbihSound
                        )

                        SubScreen.NONE -> {
                            when (uiState.currentTab) {
                                MainTab.HOME -> HomeDashboardScreen(
                                    uiState = uiState,
                                    onSelectTab = ruhaniViewModel::selectTab,
                                    onOpenSubScreen = ruhaniViewModel::openSubScreen,
                                    onIncrementTasbih = ruhaniViewModel::incrementTasbih,
                                    onResetTasbih = ruhaniViewModel::confirmTasbihReset
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
}

private data class RuhaniNavItem(
    val tab: MainTab,
    val label: String,
    val iconRes: Int
)

@Composable
private fun RuhaniBottomNavigationBar(
    currentTab: MainTab,
    isBn: Boolean,
    onSelectTab: (MainTab) -> Unit
) {
    // Tab order matching user reference design: Home -> Prayer -> Quran -> More
    val navItems = listOf(
        RuhaniNavItem(MainTab.HOME, if (isBn) "হোম" else "Home", R.drawable.ic_nav_home),
        RuhaniNavItem(MainTab.PRAYER, if (isBn) "নামাজ" else "Prayer", R.drawable.ic_nav_prayer),
        RuhaniNavItem(MainTab.QURAN, if (isBn) "কুরআন" else "Quran", R.drawable.ic_nav_quran),
        RuhaniNavItem(MainTab.MORE, if (isBn) "আরও" else "More", R.drawable.ic_nav_more)
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // Colors matching user reference image:
    // Selected capsule: soft sage/mint pill (#DCEBE1 in light mode, deep emerald tint in dark mode)
    // Selected text & icon: deep sage green (#2C4C3B in light mode, luminous mint in dark mode)
    // Unselected text & icon: neutral slate/sage (#728078 in light mode, muted mint in dark mode)
    val selectedPillColor = if (isDark) Color(0xFF133628) else Color(0xFFDCEBE1)
    val selectedContentColor = if (isDark) NoorMintLuminous else Color(0xFF2C4C3B)
    val unselectedContentColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f) else Color(0xFF728078)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val selected = currentTab == item.tab
                val animatedContentColor by animateColorAsState(
                    targetValue = if (selected) selectedContentColor else unselectedContentColor,
                    label = "nav_content_color"
                )
                val animatedPillColor by animateColorAsState(
                    targetValue = if (selected) selectedPillColor else Color.Transparent,
                    label = "nav_pill_color"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(animatedPillColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSelectTab(item.tab) }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("nav_tab_${item.tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.label,
                                tint = animatedContentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = animatedContentColor
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun QuranGlobalMiniPlayer(
    uiState: RuhaniUiState,
    onTogglePlayPause: () -> Unit,
    onClose: () -> Unit,
    onOpenSurah: () -> Unit
) {
    val isBn = uiState.language == "bn"
    val surahNum = uiState.playingSurahNumber ?: 1
    val ayahNum = uiState.playingAyahNumber ?: 1
    val surah = QuranCatalog.surahs.firstOrNull { it.number == surahNum }
        ?: QuranCatalog.surahs.first()
    val surahName = if (isBn) surah.nameBn else surah.nameEn
    val ayahText = if (isBn) "আয়াত $ayahNum" else "Ayah $ayahNum"
    val qariName = if (isBn) uiState.selectedQari.nameBn else uiState.selectedQari.nameEn

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("global_quran_mini_player")
            .clickable { onOpenSurah() }
    ) {
        Column {
            LinearProgressIndicator(
                progress = { uiState.audioProgressFraction },
                color = EmeraldPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "$surahName • $ayahText",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = qariName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                            .testTag("global_mini_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (uiState.isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isAudioPlaying) "Pause" else "Play",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("global_mini_player_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Player",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
