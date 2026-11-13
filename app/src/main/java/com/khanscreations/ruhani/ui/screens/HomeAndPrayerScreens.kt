package com.khanscreations.ruhani.ui.screens

import android.Manifest
import android.content.Intent

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khanscreations.ruhani.R
import com.khanscreations.ruhani.domain.*
import com.khanscreations.ruhani.ui.MainTab
import com.khanscreations.ruhani.ui.RuhaniUiState
import com.khanscreations.ruhani.ui.SubScreen
import com.khanscreations.ruhani.ui.theme.AmiriFontFamily
import com.khanscreations.ruhani.ui.theme.EmeraldPrimary
import com.khanscreations.ruhani.ui.theme.NoorAmberWarning
import com.khanscreations.ruhani.ui.theme.NoorEmeraldDark
import com.khanscreations.ruhani.ui.theme.NoorEmeraldPrimary
import com.khanscreations.ruhani.ui.theme.NoorGoldAccent
import com.khanscreations.ruhani.ui.theme.NoorMintLuminous
import com.khanscreations.ruhani.ui.theme.NoorRedAlert
import com.khanscreations.ruhani.ui.theme.PureBlack
import com.khanscreations.ruhani.ui.theme.PureWhite
import com.khanscreations.ruhani.ui.theme.RuhaniHalkaCardBrush
import com.khanscreations.ruhani.ui.theme.RuhaniHalkaDarkBrush
import com.khanscreations.ruhani.ui.theme.ruhaniCardBrush
import com.khanscreations.ruhani.ui.theme.SignalRed
import com.khanscreations.ruhani.ui.theme.SpaceGroteskFontFamily
import androidx.core.net.toUri

data class EssentialFeatureCardModel(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val badgeText: String?,
    val badgeIsRed: Boolean = false,
    val badgeIsEmerald: Boolean = false,
    val icon: ImageVector,
    val targetSubScreen: SubScreen? = null,
    val targetTab: MainTab? = null
)

@Composable
fun RuhaniTopBar(
    uiState: RuhaniUiState,
    onSelectDistrict: (BdDistrict) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit
) {
    var showDistrictModal by remember { mutableStateOf(false) }
    val isBn = uiState.language == "bn"

    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ruhaniCardBrush())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Profile Avatar + Brand Identity + District Selector Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Profile Avatar on left of header
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SignalRed)
                            .border(1.5.dp, PureWhite.copy(alpha = 0.7f), CircleShape)
                            .testTag("top_profile_button")
                            .clickable { onOpenProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = uiState.displayName.trim().take(2).uppercase()
                        Text(
                            text = initials.ifEmpty { "R" },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PureWhite
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(EmeraldPrimary, Color(0xFF0A5C3F))
                                )
                            )
                            .border(1.dp, PureWhite.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightlightRound,
                            contentDescription = "Ruhani Logo",
                            tint = PureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isBn) "রুহানি" else "Ruhani",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "#14A673 • NOOR EDITION",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    // District Pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = EmeraldPrimary.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.55f)),
                        modifier = Modifier
                            .testTag("district_selector_pill")
                            .clickable { showDistrictModal = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = SignalRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isBn) uiState.selectedDistrict.nameBn else uiState.selectedDistrict.nameEn,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select District",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Right Action: Settings only
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.35f), CircleShape)
                        .testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            HorizontalDivider(color = EmeraldPrimary.copy(alpha = 0.3f))
        }
    }

    if (showDistrictModal) {
        DistrictPickerDialog(
            selectedDistrict = uiState.selectedDistrict,
            isBn = isBn,
            onDismiss = { showDistrictModal = false },
            onSelect = {
                onSelectDistrict(it)
                showDistrictModal = false
            }
        )
    }
}

@Composable
fun DistrictPickerDialog(
    selectedDistrict: BdDistrict,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onSelect: (BdDistrict) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredDistricts = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            RuhaniContentCatalog.bdDistricts
        } else {
            val q = searchQuery.trim().lowercase()
            RuhaniContentCatalog.bdDistricts.filter {
                it.nameEn.lowercase().contains(q) ||
                it.nameBn.contains(q) ||
                it.divisionBn.contains(q)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isBn) "জেলা নির্বাচন করুন (বাংলাদেশ)" else "Select District (Bangladesh)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (isBn) "জেলার নাম খুঁজুন..." else "Search district...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NoorEmeraldPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("district_search_input")
                )

                if (filteredDistricts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBn) "কোনো জেলা পাওয়া যায়নি" else "No districts found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredDistricts, key = { it.id }) { district ->
                            val isSelected = district.id == selectedDistrict.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) NoorEmeraldPrimary.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) BorderStroke(1.dp, NoorEmeraldPrimary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(district) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${district.nameBn} (${district.nameEn})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "বিভাগ: ${district.divisionBn}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = NoorEmeraldPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বন্ধ করুন" else "Close")
            }
        }
    )
}

@Composable
fun HomeDashboardScreen(
    uiState: RuhaniUiState,
    onSelectTab: (MainTab) -> Unit,
    onOpenSubScreen: (SubScreen) -> Unit,
    onIncrementTasbih: () -> Unit = {},
    onResetTasbih: () -> Unit = {}
) {
    val context = LocalContext.current
    val schedule = uiState.schedule
    val isBn = uiState.language == "bn"
    val useBnDigits = uiState.useBanglaDigits

    fun fmt(value: String): String =
        if (useBnDigits) AstronomicalEngine.toBanglaDigits(value) else value

    val essentialFeatures = remember(isBn, schedule) {
        listOf(
            EssentialFeatureCardModel(
                id = "prayer_times",
                titleEn = "Prayer Times",
                titleBn = "নামাজের সময়সূচী",
                subtitleEn = "5 Waqt & Forbidden Times",
                subtitleBn = "৫ ওয়াক্ত ও নিষিদ্ধ সময়",
                badgeText = if (isBn) "চলমান" else "Active",
                badgeIsEmerald = true,
                icon = Icons.Default.Schedule,
                targetTab = MainTab.PRAYER
            ),
            EssentialFeatureCardModel(
                id = "al_quran",
                titleEn = "Al-Quran",
                titleBn = "আল-কুরআন",
                subtitleEn = "Bangla Tafsir & Audio",
                subtitleBn = "বাংলা অনুবাদ, তাফসীর ও অডিও",
                badgeText = "Surah ${uiState.lastReadSurahNumber}:${uiState.lastReadAyahNumber}",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                targetTab = MainTab.QURAN
            ),
            EssentialFeatureCardModel(
                id = "tasbih",
                titleEn = "Tasbih Counter",
                titleBn = "ডিজিটাল তাসবীহ",
                subtitleEn = "Digital Dhikr & Target",
                subtitleBn = "যিকির ও হ্যাপটিক কাউন্টার",
                badgeText = "33x",
                icon = Icons.Default.TouchApp,
                targetSubScreen = SubScreen.TASBIH
            ),
            EssentialFeatureCardModel(
                id = "dua_dhikr",
                titleEn = "Dua & Dhikr",
                titleBn = "দোয়া ও যিকির",
                subtitleEn = "Morning, Evening & Ruqyah",
                subtitleBn = "সকাল-সন্ধ্যা ও রুকইয়াহ",
                badgeText = "120+ Duas",
                icon = Icons.Default.Favorite,
                targetSubScreen = SubScreen.DUA_DHIKR
            ),
            EssentialFeatureCardModel(
                id = "ramadan",
                titleEn = "Ramadan",
                titleBn = "রমজান ক্যালেন্ডার",
                subtitleEn = "Sehri ${schedule.sehriEndFormatted} • Iftar ${schedule.iftarFormatted}",
                subtitleBn = "সেহরি ${schedule.sehriEndFormatted} • ইফতার ${schedule.iftarFormatted}",
                badgeText = "UPCOMING",
                badgeIsRed = true,
                icon = Icons.Default.NightlightRound,
                targetSubScreen = SubScreen.RAMADAN
            ),
            EssentialFeatureCardModel(
                id = "qibla",
                titleEn = "Qibla Compass",
                titleBn = "কিবলা কম্পাস",
                subtitleEn = "${uiState.selectedDistrict.qiblaBearingDeg.toInt()}° West from ${uiState.selectedDistrict.nameEn}",
                subtitleBn = "${uiState.selectedDistrict.nameBn} থেকে ${uiState.selectedDistrict.qiblaBearingDeg.toInt()}° পশ্চিম",
                badgeText = "AlAdhan API",
                badgeIsEmerald = true,
                icon = Icons.Default.Explore,
                targetSubScreen = SubScreen.QIBLA
            ),
            EssentialFeatureCardModel(
                id = "zakat",
                titleEn = "Zakat Calculator",
                titleBn = "যাকাত ক্যালকুলেটর",
                subtitleEn = "Nisab in BDT (৳) & Gold",
                subtitleBn = "স্বর্ণ ও রৌপ্য নিসাব (৳)",
                badgeText = "2.5%",
                icon = Icons.Default.Calculate,
                targetSubScreen = SubScreen.ZAKAT
            ),
            EssentialFeatureCardModel(
                id = "calendar",
                titleEn = "Islamic Calendar",
                titleBn = "ইসলামিক ও বাংলা ক্যালেন্ডার",
                subtitleEn = "Hijri, Bangla & Gregorian",
                subtitleBn = "হিজরি, বঙ্গাব্দ ও ইংরেজি",
                badgeText = "1446 AH",
                icon = Icons.Default.CalendarMonth,
                targetSubScreen = SubScreen.CALENDAR
            ),
            EssentialFeatureCardModel(
                id = "names_99",
                titleEn = "99 Names of Allah",
                titleBn = "আল্লাহর ৯৯টি নাম",
                subtitleEn = "Asma-ul-Husna & Audio",
                subtitleBn = "আসমাউল হুসনা ও ফজিলত",
                badgeText = "99",
                icon = Icons.Default.AutoAwesome,
                targetSubScreen = SubScreen.NAMES_OF_ALLAH
            ),
            EssentialFeatureCardModel(
                id = "hadith",
                titleEn = "Hadith Collection",
                titleBn = "সহীহ হাদিস সম্ভার",
                subtitleEn = "Bukhari, Muslim & Riyadus",
                subtitleBn = "বুখারী, মুসলিম ও রিয়াদুস সালেহীন",
                badgeText = "Sahih",
                icon = Icons.Default.FormatQuote,
                targetSubScreen = SubScreen.HADITH
            ),
            EssentialFeatureCardModel(
                id = "salat_tracker",
                titleEn = "Prayer Tracker",
                titleBn = "সালাত ও কাজা ট্র্যাকার",
                subtitleEn = "Daily Log & Qaza Counter",
                subtitleBn = "দৈনিক নামাজ ও কাজা হিসাব",
                badgeText = "Track",
                badgeIsEmerald = true,
                icon = Icons.Default.CheckCircle,
                targetSubScreen = SubScreen.SALAT_TRACKER
            ),
            EssentialFeatureCardModel(
                id = "mosque_finder",
                titleEn = "Nearby Mosque",
                titleBn = "নিকটস্থ মসজিদ",
                subtitleEn = "Masjid & Jamaat Locator",
                subtitleBn = "মসজিদ ও জামাতের সময়",
                badgeText = "GPS",
                icon = Icons.Default.Mosque,
                targetSubScreen = SubScreen.MOSQUES
            ),
            EssentialFeatureCardModel(
                id = "islamic_books",
                titleEn = "Islamic Books",
                titleBn = "ইসলামিক বই ও তাফসীর",
                subtitleEn = "Tafsir, Seerah & Fiqh",
                subtitleBn = "সীরাত, ফিকহ ও তাফসীর গ্রন্থ",
                badgeText = null,
                icon = Icons.Default.Book,
                targetSubScreen = SubScreen.BOOKS
            ),
            EssentialFeatureCardModel(
                id = "fatwa",
                titleEn = "Mas'ala & Fatwa",
                titleBn = "মাসআলা ও ফতোয়া",
                subtitleEn = "Verified Islamic Q&A",
                subtitleBn = "প্রামাণিক শরয়ী প্রশ্নোত্তর",
                badgeText = null,
                icon = Icons.Default.QuestionAnswer,
                targetSubScreen = SubScreen.FATWA
            ),
            EssentialFeatureCardModel(
                id = "hajj",
                titleEn = "Hajj & Umrah",
                titleBn = "হজ্জ ও উমরাহ গাইড",
                subtitleEn = "Step-by-Step Ritual Guide",
                subtitleBn = "ধাপে ধাপে হজ্জ ও উমরাহ",
                badgeText = null,
                icon = Icons.Default.Public,
                targetSubScreen = SubScreen.HAJJ_GUIDE
            ),
            EssentialFeatureCardModel(
                id = "baby_names",
                titleEn = "Islamic Names",
                titleBn = "ইসলামিক শিশুর নাম",
                subtitleEn = "Boy & Girl Names Meaning",
                subtitleBn = "অর্থসহ ছেলে ও মেয়ের নাম",
                badgeText = null,
                icon = Icons.Default.ChildCare,
                targetSubScreen = SubScreen.BABY_NAMES
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_dashboard_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Live Clock, Tri-Calendar & Sunrise/Sunset Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = fmt(schedule.currentTimeFormatted),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NoorEmeraldPrimary.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = if (isBn) "${uiState.selectedDistrict.nameBn} সময়" else "${uiState.selectedDistrict.nameEn} Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = NoorMintLuminous,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBn) {
                            "${schedule.hijriDateBn} • ${schedule.banglaDateBn} • ${schedule.gregorianDateBn}"
                        } else {
                            "${schedule.hijriDateEn} • ${schedule.banglaDateEn} • ${schedule.gregorianDateEn}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                // Sunrise & Sunset Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Sunrise",
                                tint = NoorGoldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = fmt(schedule.sunriseFormatted.removeSuffix(" AM")),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(12.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbTwilight,
                                contentDescription = "Sunset",
                                tint = NoorAmberWarning,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = fmt(schedule.sunsetFormatted.removeSuffix(" PM")),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Current Waqt Hero Card + 5-Waqt Horizontal Strip (#14a673 Halka Gradient + #f02828 Accent)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.65f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_current_waqt_card")
                    .clickable { onSelectTab(MainTab.PRAYER) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RuhaniHalkaDarkBrush)
                ) {
                    // Subtle decorative Mosque Hero Image overlay in corner
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_mosque_1794329174379),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.14f)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left: Current Waqt Name & Window
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = NoorEmeraldPrimary.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(NoorMintLuminous)
                                        )
                                        Text(
                                            text = if (isBn) "চলমান ওয়াক্ত" else "CURRENT WAQT",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = NoorMintLuminous
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isBn) schedule.currentWaqt.nameBn else schedule.currentWaqt.nameEn,
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isBn) schedule.currentWaqt.nameEn else schedule.currentWaqt.nameBn,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color(0xFFA3B8B0),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${schedule.currentWaqt.startFormatted} – ${schedule.currentWaqt.endFormatted}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = SpaceGroteskFontFamily),
                                    color = Color(0xFFA3B8B0)
                                )
                            }

                            // Right: Remaining Countdown + Red TIME LEFT Badge
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isBn) "বাকি সময়" else "REMAINING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFA3B8B0)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = fmt(schedule.remainingFormatted),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = NoorMintLuminous
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NoorRedAlert
                                ) {
                                    Text(
                                        text = if (isBn) "সময় বাকি" else "TIME LEFT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress Bar
                        val elapsedPercent = (schedule.waqtProgressFraction * 100).toInt().coerceIn(1, 99)
                        LinearProgressIndicator(
                            progress = { schedule.waqtProgressFraction },
                            color = NoorEmeraldPrimary,
                            trackColor = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${if (isBn) "শুরু" else "Started"} ${schedule.currentWaqt.startFormatted}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF889E96)
                            )
                            Text(
                                text = "$elapsedPercent% ${if (isBn) "অতিবাহিত" else "Elapsed"}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = NoorMintLuminous
                            )
                            Text(
                                text = "${if (isBn) "শেষ" else "Ends"} ${schedule.currentWaqt.endFormatted}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF889E96)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Next Prayer Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Next Prayer",
                                    tint = NoorMintLuminous,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isBn) "পরবর্তী নামাজ:" else "Next Prayer:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFA3B8B0)
                                )
                                Text(
                                    text = if (isBn) "${schedule.nextWaqt.nameBn} (${schedule.nextWaqt.nameEn})"
                                    else "${schedule.nextWaqt.nameEn} (${schedule.nextWaqt.nameBn})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = fmt(schedule.nextWaqt.startFormatted),
                                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = SpaceGroteskFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = schedule.nextWaqtInFormatted,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                        color = Color(0xFFA3B8B0),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5-Waqt Compact Horizontal Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                schedule.fiveWaqts.forEach { waqt ->
                    val isCurrent = waqt.type == schedule.currentWaqt.type
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrent) NoorEmeraldPrimary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isCurrent) NoorEmeraldPrimary.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectTab(MainTab.PRAYER) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isBn) waqt.nameBn else waqt.nameEn,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) NoorMintLuminous else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = fmt(waqt.startFormatted.substringBefore(" ")),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = waqt.startFormatted.substringAfter(" ", ""),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = if (isCurrent) NoorMintLuminous else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. Essential Services Section Header (Top 10 Features)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isBn) "প্রয়োজনীয় সেবা" else "Essential Services",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (isBn) "১০টি সেবা" else "Top 10",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                TextButton(
                    onClick = { onSelectTab(MainTab.MORE) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (isBn) "সবগুলো দেখুন" else "View All",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View All",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // 4. Top 10 Essential Services Grid (5 rows of 2 cards)
        items(essentialFeatures.take(10).chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { feature ->
                    EssentialServiceGridCard(
                        feature = feature,
                        isBn = isBn,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when {
                                feature.targetSubScreen != null -> onOpenSubScreen(feature.targetSubScreen)
                                feature.targetTab != null -> onSelectTab(feature.targetTab)
                            }
                        }
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // 4B. "More" Services Button
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.2.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_more_services_button")
                    .clickable { onSelectTab(MainTab.MORE) }
            ) {
                Row(
                    modifier = Modifier
                        .background(ruhaniCardBrush())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldPrimary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "More Services",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isBn) "আরও সেবা দেখুন" else "Explore More Services",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBn) "মাসআলা, বই, হজ্জ, শিশুর নাম ও অন্যান্য সেবা সমূহ"
                                else "Fatwa, Books, Hajj Guide, Baby Names & More",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isBn) "+৬টি আরও" else "+6 More",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Go to More",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 5. Hadith of the Day Card
        item {
            val dailyHadith = RuhaniContentCatalog.hadithList.first()
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_hadith_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NoorEmeraldPrimary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = "Hadith",
                                    tint = NoorMintLuminous,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = if (isBn) "আজকের হাদিস • Hadith of the Day" else "Hadith of the Day • আজকের হাদিস",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                        }
                        Text(
                            text = "${dailyHadith.bookEn} ${dailyHadith.hadithNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "“${if (isBn) dailyHadith.translationBn else dailyHadith.translationEn}”",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBn) "“${dailyHadith.translationEn}”" else "“${dailyHadith.translationBn}”",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "বর্ণনাকারী: ${dailyHadith.narratorBn}" else "Narrated by ${dailyHadith.narratorEn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "${dailyHadith.translationBn}\n— ${dailyHadith.bookBn} (${dailyHadith.hadithNumber})"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Hadith"))
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isBn) "হাদিস শেয়ার করুন" else "Share Hadith",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = NoorMintLuminous,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Haramain Live Banner Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_haramain_live_card")
                    .clickable { onOpenSubScreen(SubScreen.HARAMAIN_LIVE) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NoorEmeraldPrimary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = "Haramain Live",
                                tint = NoorMintLuminous
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isBn) "মক্কা ও মদিনা লাইভ (Haramain)" else "Makkah & Madinah 24/7 Live",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NoorRedAlert
                                ) {
                                    Text(
                                        text = "LIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isBn) "মসজিদুল হারাম ও মসজিদে নববীর সরাসরি সম্প্রচার" else "Watch live stream from Masjid al-Haram & Masjid an-Nabawi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = NoorMintLuminous
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Powered by KhansCreations Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NoorEmeraldPrimary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, "https://khanscreations.com".toUri())
                        context.startActivity(intent)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.khanscreations_logo),
                        contentDescription = "KhansCreations Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "KhansCreations",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NoorMintLuminous
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Powered by KhansCreations (khanscreations.com)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.Underline
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (isBn) "ভিজিট করুন ↗" else "Visit ↗",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = NoorEmeraldPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun EssentialServiceGridCard(
    feature: EssentialFeatureCardModel,
    isBn: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
        modifier = modifier
            .testTag("feature_card_${feature.id}")
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .background(ruhaniCardBrush())
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.16f))
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = feature.titleEn,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (feature.badgeText != null) {
                    val badgeBg = when {
                        feature.badgeIsRed -> SignalRed
                        feature.badgeIsEmerald -> EmeraldPrimary
                        else -> EmeraldPrimary.copy(alpha = 0.16f)
                    }
                    val badgeFg = when {
                        feature.badgeIsRed -> PureWhite
                        feature.badgeIsEmerald -> PureWhite
                        else -> EmeraldPrimary
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = feature.badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isBn) feature.titleBn else feature.titleEn,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (isBn) feature.titleEn else feature.titleBn,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isBn) feature.subtitleBn else feature.subtitleEn,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PrayerScheduleScreen(
    uiState: RuhaniUiState,
    onShiftDay: (Int) -> Unit,
    onToggleAzanAlert: (WaqtType) -> Unit,
    onSelectDistrict: (BdDistrict) -> Unit,
    onSelectMethod: (CalculationMethod) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val schedule = uiState.schedule
    val isBn = uiState.language == "bn"
    var showChangeConfigModal by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            com.khanscreations.ruhani.domain.PrayerAlarmScheduler.schedulePrayerAlarms(context)
        }
    }

    val exactAlarmPermission = rememberExactAlarmPermission()
    val anyAzanAlertOn = uiState.azanAlerts.values.any { it }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("prayer_schedule_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Location & Calculation Method Bar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NoorEmeraldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = NoorMintLuminous,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "${uiState.selectedDistrict.nameBn} (${uiState.selectedDistrict.nameEn}), Bangladesh",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBn) "${uiState.calculationMethod.titleBn} • ${uiState.madhhab.titleBn}"
                                else "${uiState.calculationMethod.titleEn} • ${uiState.madhhab.titleEn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NoorEmeraldPrimary.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .testTag("prayer_change_method_btn")
                            .clickable { showChangeConfigModal = true }
                    ) {
                        Text(
                            text = if (isBn) "পরিবর্তন" else "Change",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NoorMintLuminous,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 2. Date Switcher (< Date >)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onShiftDay(-1) },
                        modifier = Modifier.testTag("prayer_prev_day_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Day"
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isBn) "${schedule.hijriDateBn} • ${schedule.banglaDateBn}"
                            else "${schedule.hijriDateEn} • ${schedule.banglaDateEn}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = NoorMintLuminous
                        )
                        Text(
                            text = if (isBn) schedule.gregorianDateBn else schedule.gregorianDateEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.prayerDayOffset != 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBn) "আজকের তারিখে ফিরুন" else "Reset to Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = NoorGoldAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onShiftDay(0) }
                            )
                        }
                    }

                    IconButton(
                        onClick = { onShiftDay(1) },
                        modifier = Modifier.testTag("prayer_next_day_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Day"
                        )
                    }
                }
            }
        }

        // 3. Current Waqt Hero Card with Start / Jama'at / End 3-Column Metrics
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF153529), Color(0xFF0E1D17))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = NoorEmeraldPrimary.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = if (isBn) "চলমান ওয়াক্ত • CURRENT WAQT" else "CURRENT WAQT • চলমান ওয়াক্ত",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NoorMintLuminous,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${schedule.currentWaqt.nameEn} (${schedule.currentWaqt.nameBn})",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${schedule.currentWaqt.arabicName} • ${schedule.currentWaqt.rakahSummaryBn}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFA3B8B0)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NoorRedAlert
                            ) {
                                Text(
                                    text = if (isBn) "বাকি সময়" else "REMAINING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schedule.remainingFormatted,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = NoorMintLuminous
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-Column Metrics: Start, Jama'at, End
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WaqtMetricMiniBox(
                            label = if (isBn) "শুরু (Start)" else "Start Time",
                            value = schedule.currentWaqt.startFormatted,
                            modifier = Modifier.weight(1f)
                        )
                        WaqtMetricMiniBox(
                            label = if (isBn) "জামাত (Jama'at)" else "Jama'at",
                            value = schedule.currentWaqt.jamaatFormatted,
                            highlight = true,
                            modifier = Modifier.weight(1f)
                        )
                        WaqtMetricMiniBox(
                            label = if (isBn) "শেষ (End)" else "Waqt Ends",
                            value = schedule.currentWaqt.endFormatted,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Fasting & Iftar Today Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightlightRound,
                                contentDescription = "Fasting",
                                tint = NoorMintLuminous,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = if (isBn) "আজকের সেহরি ও ইফতার" else "Fasting & Iftar Today",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "সুবহে সাদিক ও সূর্যাস্তের সময়" else "Sehri (Suhoor) & Maghrib Iftar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBn) "সেহরির শেষ সময় (Sehri Ends)" else "SEHRI ENDS (LAST TIME)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = schedule.sehriEndFormatted,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = NoorEmeraldPrimary.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBn) "ইফতারের সময় (Iftar Time)" else "IFTAR TIME (MAGHRIB)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NoorMintLuminous
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = schedule.iftarFormatted,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = NoorMintLuminous
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Prohibited Prayer Times (Makruh) Red-Bordered Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = NoorRedAlert.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, NoorRedAlert.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prohibited_makruh_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Prohibited Times",
                                tint = NoorRedAlert,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isBn) "নামাজের নিষিদ্ধ সময় (মাকরূহ)" else "Prohibited Prayer Times (Makruh)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8080)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NoorRedAlert
                        ) {
                            Text(
                                text = if (isBn) "নামাজ নিষিদ্ধ" else "Avoid Salah",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MakruhIntervalBox(
                            title = if (isBn) "সূর্যোদয় (Sunrise)" else "Sunrise",
                            range = schedule.prohibitedSunriseRange,
                            modifier = Modifier.weight(1f)
                        )
                        MakruhIntervalBox(
                            title = if (isBn) "দ্বিপ্রহর (Zawal)" else "Zawal (Noon)",
                            range = schedule.prohibitedZawalRange,
                            modifier = Modifier.weight(1f)
                        )
                        MakruhIntervalBox(
                            title = if (isBn) "সূর্যাস্ত (Sunset)" else "Sunset",
                            range = schedule.prohibitedSunsetRange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 6. Daily Prayer Schedule Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "দৈনিক নামাজের সময়সূচী" else "Daily Prayer Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isBn) "আজান নোটিফিকেশন টগল করুন" else "Tap bell for Azan alerts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 6b. Exact-alarm warning (only when alerts are on but exact alarms are not granted)
        if (!exactAlarmPermission.granted && anyAzanAlertOn) {
            item {
                ExactAlarmWarningBanner(
                    isBn = isBn,
                    onGrant = exactAlarmPermission.requestPermission
                )
            }
        }

        // 7. 5 Waqts + Sunrise Row
        items(schedule.fiveWaqts) { waqt ->
            val isCurrent = waqt.type == schedule.currentWaqt.type
            val alertEnabled = uiState.azanAlerts[waqt.type] ?: true

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isCurrent) NoorEmeraldPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = if (isCurrent) 1.5.dp else 1.dp,
                    color = if (isCurrent) NoorEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prayer_row_${waqt.type.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${waqt.nameEn} • ${waqt.nameBn}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) NoorMintLuminous else MaterialTheme.colorScheme.onSurface
                            )
                            if (isCurrent) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = NoorEmeraldPrimary
                                ) {
                                    Text(
                                        text = if (isBn) "চলমান" else "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isBn) waqt.rakahSummaryBn else waqt.rakahSummaryEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${waqt.startFormatted} – ${waqt.endFormatted}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${if (isBn) "জামাত" else "Jama'at"}: ${waqt.jamaatFormatted}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                color = NoorMintLuminous
                            )
                        }

                        IconButton(
                            onClick = {
                                if (!alertEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (!com.khanscreations.ruhani.domain.RuhaniPushNotificationManager.hasNotificationPermission(context)) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                onToggleAzanAlert(waqt.type)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (alertEnabled) NoorEmeraldPrimary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("azan_toggle_${waqt.type.name.lowercase()}")
                        ) {
                            Icon(
                                imageVector = if (alertEnabled) Icons.Default.NotificationsActive
                                else Icons.Default.NotificationsOff,
                                contentDescription = "Toggle Azan Alert",
                                tint = if (alertEnabled) NoorMintLuminous else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 8. Sunnah & Voluntary Prayers (Tahajjud, Ishraq & Duha, Awwabin)
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "নফল ও সুন্নাহ নামাজের সময় (Nawafil)" else "Sunnah & Voluntary Prayers (Nawafil)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    NawafilRowItem(
                        title = if (isBn) "তাহাজ্জুদ (Tahajjud)" else "Tahajjud (Last Third of Night)",
                        subtitle = if (isBn) "রাতের শেষ তৃতীয়াংশ • ২–৮ রাকাত" else "Best voluntary night prayer • 2–8 Rakah",
                        time = schedule.tahajjudRange
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                    NawafilRowItem(
                        title = if (isBn) "ইশরাক ও চাশত (Ishraq & Duha)" else "Ishraq & Duha (Forenoon)",
                        subtitle = if (isBn) "সূর্যোদয়ের ২০ মিনিট পর • ২–৪ রাকাত" else "20 mins after sunrise • 2–4 Rakah",
                        time = "${schedule.ishraqRange.substringBefore(" – ")} – ${schedule.duhaRange.substringAfter(" – ")}"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                    NawafilRowItem(
                        title = if (isBn) "আওয়াবীন (Awwabin)" else "Awwabin (After Maghrib)",
                        subtitle = if (isBn) "মাগরিবের ফরজ ও সুন্নতের পর • ৬ রাকাত" else "Between Maghrib & Isha • 6 Rakah",
                        time = schedule.awwabinRange
                    )
                }
            }
        }
    }

    if (showChangeConfigModal) {
        AlertDialog(
            onDismissRequest = { showChangeConfigModal = false },
            title = {
                Text(
                    text = if (isBn) "জেলা ও হিসাব পদ্ধতি" else "Location & Calculation Method",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBn) "হিসাব পদ্ধতি (Calculation Method):" else "Select Calculation Method:",
                        style = MaterialTheme.typography.labelMedium,
                        color = NoorMintLuminous
                    )
                    CalculationMethod.entries.forEach { method ->
                        val selected = method == uiState.calculationMethod
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) NoorEmeraldPrimary.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selected) BorderStroke(1.dp, NoorEmeraldPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectMethod(method) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isBn) method.titleBn else method.titleEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Fajr ${method.fajrAngle}° • Isha ${method.ishaAngle}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangeConfigModal = false }) {
                    Text(if (isBn) "সম্পন্ন" else "Done")
                }
            }
        )
    }
}

@Composable
private fun WaqtMetricMiniBox(
    label: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (highlight) NoorEmeraldPrimary.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.07f),
        border = BorderStroke(
            1.dp,
            if (highlight) NoorMintLuminous.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (highlight) NoorMintLuminous else Color(0xFFA3B8B0)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun MakruhIntervalBox(
    title: String,
    range: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, NoorRedAlert.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = range,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = Color(0xFFFF8080),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NawafilRowItem(
    title: String,
    subtitle: String,
    time: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = NoorMintLuminous
        )
    }
}
