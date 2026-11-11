package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.BanglaHadithApiService
import com.example.data.remote.BanglaHadithRecord
import com.example.data.remote.HadithApiSource
import com.example.domain.*
import com.example.ui.RuhaniUiState
import com.example.ui.SubScreen
import com.example.ui.auth.GoogleAuthHelper
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.NoorEmeraldDark
import com.example.ui.theme.NoorEmeraldPrimary
import com.example.ui.theme.NoorGoldAccent
import com.example.ui.theme.NoorMintLuminous
import com.example.ui.theme.NoorRedAlert
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.launch

data class MoreMenuItem(
    val titleBn: String,
    val titleEn: String,
    val subtitleBn: String,
    val icon: ImageVector,
    val screen: SubScreen
)

@Composable
fun MoreHubScreen(
    uiState: RuhaniUiState,
    onOpenSubScreen: (SubScreen) -> Unit
) {
    val isBn = uiState.language == "bn"
    var searchQuery by remember { mutableStateOf("") }

    val allItems = remember {
        listOf(
            MoreMenuItem("ডিজিটাল তাসবীহ", "Digital Tasbih", "হ্যাপটিক যিকির ও টার্গেট কাউন্টার", Icons.Default.TouchApp, SubScreen.TASBIH),
            MoreMenuItem("সালাত ও কাজা ট্র্যাকার", "Prayer & Qaza Tracker", "দৈনিক ৫ ওয়াক্ত ও কাজা নামাজের হিসাব", Icons.Default.CheckCircle, SubScreen.SALAT_TRACKER),
            MoreMenuItem("দোয়া, যিকির ও রুকইয়াহ", "Dua, Adhkar & Ruqyah", "হিসনুল মুসলিম ও রুকইয়াহ শারইয়াহ", Icons.Default.Favorite, SubScreen.DUA_DHIKR),
            MoreMenuItem("রমজান ক্যালেন্ডার ও আমল", "Ramadan Dashboard", "সেহরি, ইফতার ও ৩০ দিনের আমল", Icons.Default.NightlightRound, SubScreen.RAMADAN),
            MoreMenuItem("কিবলা কম্পাস", "Qibla Compass", "মক্কার দিক ও দূরত্ব নির্ণয়", Icons.Default.Explore, SubScreen.QIBLA),
            MoreMenuItem("যাকাত ক্যালকুলেটর (৳)", "Zakat Calculator", "স্বর্ণ ও রৌপ্য নিসাব অনুযায়ী ২.৫% যাকাত", Icons.Default.Calculate, SubScreen.ZAKAT),
            MoreMenuItem("হিজরি ও বাংলা ক্যালেন্ডার", "Tri-Calendar & Holidays", "হিজরি, বঙ্গাব্দ ও ইসলামিক দিবস", Icons.Default.CalendarMonth, SubScreen.CALENDAR),
            MoreMenuItem("আল্লাহর ৯৯টি নাম", "99 Names of Allah", "আসমাউল হুসনা, অর্থ ও ফজিলত", Icons.Default.AutoAwesome, SubScreen.NAMES_OF_ALLAH),
            MoreMenuItem("সহীহ হাদিস সম্ভার", "Hadith Collection", "সহীহ বুখারী, মুসলিম ও রিয়াদুস সালেহীন", Icons.Default.FormatQuote, SubScreen.HADITH),
            MoreMenuItem("নিকটস্থ মসজিদ ও জামাত", "Nearby Mosques", "মসজিদ লোকেটর ও জামাতের সময়", Icons.Default.Mosque, SubScreen.MOSQUES),
            MoreMenuItem("ইসলামিক বই ও তাফসীর", "Islamic Books", "সীরাত, তাফসীর ও ফিকহ গ্রন্থগার", Icons.Default.Book, SubScreen.BOOKS),
            MoreMenuItem("মাসআলা ও ফতোয়া", "Mas'ala & Fatwa", "প্রামাণিক শরয়ী প্রশ্নোত্তর", Icons.Default.QuestionAnswer, SubScreen.FATWA),
            MoreMenuItem("হজ্জ ও উমরাহ গাইড", "Hajj & Umrah Guide", "ধাপে ধাপে হজ্জ ও উমরাহর নিয়ম ও দোয়া", Icons.Default.Public, SubScreen.HAJJ_GUIDE),
            MoreMenuItem("ইসলামিক শিশুর নাম", "Islamic Baby Names", "অর্থসহ ছেলে ও মেয়ের সুন্দর নাম", Icons.Default.ChildCare, SubScreen.BABY_NAMES),
            MoreMenuItem("মক্কা ও মদিনা লাইভ", "Haramain 24/7 Live", "মসজিদুল হারাম ও মসজিদে নববী সরাসরি", Icons.Default.LiveTv, SubScreen.HARAMAIN_LIVE),
            MoreMenuItem("প্রোফাইল ও ক্লাউড ব্যাকআপ", "Profile & Cloud Sync", "গুগল একাউন্ট ও আমল ব্যাকআপ", Icons.Default.Person, SubScreen.PROFILE),
            MoreMenuItem("সেটিংস ও মাযহাব", "Settings & Preferences", "থিম, ভাষা, জেলা ও হিসাব পদ্ধতি", Icons.Default.Settings, SubScreen.SETTINGS)
        )
    }

    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) allItems
        else allItems.filter {
            it.titleBn.contains(searchQuery, ignoreCase = true) ||
                it.titleEn.contains(searchQuery, ignoreCase = true) ||
                it.subtitleBn.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("more_hub_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Account & Cloud Backup Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("more_profile_card")
                    .clickable { onOpenSubScreen(SubScreen.PROFILE) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
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
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NoorEmeraldPrimary.copy(alpha = 0.2f))
                                .border(1.dp, NoorMintLuminous, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.displayName.take(2).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                        }
                        Column {
                            Text(
                                text = uiState.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (uiState.currentUserId != null) {
                                    uiState.userEmail ?: "Google Cloud Sync Active"
                                } else {
                                    if (isBn) "গেস্ট মোড • ক্লাউড সিঙ্ক করতে সাইন ইন করুন" else "Guest Mode • Tap to Sign In with Google"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = NoorMintLuminous
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Profile",
                        tint = NoorMintLuminous
                    )
                }
            }
        }

        // Search Tools Input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(if (isBn) "সকল ইসলামিক সেবা খুঁজুন..." else "Search all 16 Islamic tools...")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NoorMintLuminous)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filtered.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("more_item_${item.screen.name.lowercase()}")
                            .clickable { onOpenSubScreen(item.screen) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NoorEmeraldPrimary.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.titleEn,
                                    tint = NoorMintLuminous,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = if (isBn) item.titleBn else item.titleEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.subtitleBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            val context = LocalContext.current
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NoorEmeraldPrimary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://khanscreations.com"))
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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NamesOfAllahScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onSpeakArabic: (String) -> Unit
) {
    BackHandler { onBack() }
    val isBn = uiState.language == "bn"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("names_of_allah_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "আল্লাহর ৯৯টি গুণবাচক নাম",
                titleEn = "Asma-ul-Husna (99 Names)",
                subtitle = if (isBn) "বাংলা উচ্চারণ, অর্থ, ফজিলত ও অডিও" else "Arabic calligraphy, meanings & audio pronunciation",
                isBn = isBn,
                onBack = onBack
            )
        }

        items(RuhaniContentCatalog.allahNames) { item ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NoorEmeraldPrimary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.number.toString(),
                                style = MaterialTheme.typography.labelLarge.copy(fontFamily = SpaceGroteskFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                        }
                        Column {
                            Text(
                                text = "${item.banglaName} (${item.transliteration})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${item.meaningBn} • ${item.meaningEn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NoorMintLuminous
                            )
                            Text(
                                text = item.virtueBn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.arabic,
                            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = AmiriFontFamily),
                            color = NoorMintLuminous
                        )
                        IconButton(
                            onClick = { onSpeakArabic(item.arabic) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HadithCollectionScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onSpeakArabic: (String) -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = uiState.language == "bn"

    var selectedSource by remember { mutableStateOf(HadithApiSource.ALL) }
    var selectedBookKey by remember { mutableStateOf("all") }
    var selectedChapterNo by remember { mutableIntStateOf(0) } // 0 = All chapters
    var searchQuery by remember { mutableStateOf("") }
    var isSyncing by remember { mutableStateOf(false) }
    var statusLabel by remember {
        mutableStateOf(
            if (isBn) "alQuranBD • RakibRahman • BanglaHadith API সক্রিয়"
            else "alQuranBD • RakibRahman • BanglaHadith APIs Active"
        )
    }

    var hadithRecords by remember {
        mutableStateOf(BanglaHadithApiService.getSeededMultiApiHadiths())
    }
    var inspectedHadithJson by remember { mutableStateOf<BanglaHadithRecord?>(null) }
    var showApiOverviewDialog by remember { mutableStateOf(false) }

    val availableChapters = remember(selectedBookKey) {
        if (selectedBookKey == "all") emptyList()
        else BanglaHadithApiService.getChaptersForBook(selectedBookKey)
    }

    // Fetch & merge live Hadiths when source, book, or chapter changes
    LaunchedEffect(selectedSource, selectedBookKey, selectedChapterNo) {
        isSyncing = true
        val targetBook = if (selectedBookKey == "all") "bukhari" else selectedBookKey
        val targetChapter = if (selectedChapterNo <= 0) 1 else selectedChapterNo
        val syncResult = BanglaHadithApiService.fetchHadiths(
            bookKey = if (selectedBookKey == "all") "all" else targetBook,
            chapterNo = if (selectedChapterNo <= 0) 0 else targetChapter,
            sourceFilter = selectedSource,
            forceRefresh = false
        )
        hadithRecords = syncResult.hadiths
        statusLabel = if (isBn) syncResult.statusSummaryBn else syncResult.statusSummaryEn
        isSyncing = false
    }

    val filteredHadiths = remember(hadithRecords, selectedSource, selectedBookKey, selectedChapterNo, searchQuery) {
        hadithRecords.filter { item ->
            val matchesSource = selectedSource == HadithApiSource.ALL || item.source == selectedSource
            val matchesBook = selectedBookKey == "all" || item.bookKey.equals(selectedBookKey, ignoreCase = true)
            val matchesChapter = selectedChapterNo <= 0 || item.chapterNo == selectedChapterNo
            val matchesQuery = searchQuery.isBlank() ||
                item.translationBn.contains(searchQuery, ignoreCase = true) ||
                item.translationEn.contains(searchQuery, ignoreCase = true) ||
                item.narratorBn.contains(searchQuery, ignoreCase = true) ||
                item.bookBn.contains(searchQuery, ignoreCase = true) ||
                item.chapterBn.contains(searchQuery, ignoreCase = true) ||
                item.hadithNumber.contains(searchQuery.trim(), ignoreCase = true)
            matchesSource && matchesBook && matchesChapter && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hadith_collection_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header
        item {
            SubScreenHeader(
                titleBn = "সহীহ হাদিস সম্ভার (Hadith API Hub)",
                titleEn = "Authentic Bangla Hadith API Hub",
                subtitle = if (isBn) "alQuranBD • RakibRahman • MuntashirAkon BanglaHadith"
                else "alQuranBD • RakibRahman • MuntashirAkon BanglaHadith",
                isBn = isBn,
                onBack = onBack
            )
        }

        // 2. Multi-API Status & Repository Banner Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.45f)),
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
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "৩টি ওপেন-সোর্স হাদিস API সংযুক্ত"
                                else "3 Open-Source Hadith APIs Integrated",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSyncing) {
                                    if (isBn) "API থেকে হাদিস সিঙ্ক হচ্ছে..." else "Syncing Hadiths from APIs..."
                                } else statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = NoorMintLuminous
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .testTag("hadith_api_overview_btn")
                                    .clickable { showApiOverviewDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "API Info",
                                        tint = NoorMintLuminous,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "API JSON",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        isSyncing = true
                                        val targetBook = if (selectedBookKey == "all") "bukhari" else selectedBookKey
                                        val res = BanglaHadithApiService.fetchHadiths(
                                            bookKey = if (selectedBookKey == "all") "all" else targetBook,
                                            chapterNo = selectedChapterNo,
                                            sourceFilter = selectedSource,
                                            forceRefresh = true
                                        )
                                        hadithRecords = res.hadiths
                                        statusLabel = if (isBn) res.statusSummaryBn else res.statusSummaryEn
                                        isSyncing = false
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NoorEmeraldPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Hadith APIs",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Source Filter Pills (All / alQuranBD / RakibRahman / MuntashirAkon)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HadithApiSource.entries.forEach { src ->
                            val active = selectedSource == src
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (active) NoorEmeraldPrimary else Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(
                                    1.dp,
                                    if (active) NoorMintLuminous else Color.White.copy(alpha = 0.18f)
                                ),
                                modifier = Modifier
                                    .testTag("hadith_source_${src.id}")
                                    .clickable { selectedSource = src }
                            ) {
                                Text(
                                    text = if (isBn) src.labelBn else src.labelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.White else Color(0xFFD0DDD7),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (isBn) "হাদিস, রাবী বা নম্বর খুঁজুন (যেমন: নিয়ত, উমর, 1, 5027)..."
                        else "Search Hadith, narrator, or number (e.g. intention, 1, 5027)..."
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Hadith",
                        tint = NoorMintLuminous
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hadith_search_input")
            )
        }

        // 4. Hadith Book Selector Strip (alQuranBD & BanglaHadith Books)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val allBooksSelected = selectedBookKey == "all"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (allBooksSelected) NoorEmeraldPrimary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (allBooksSelected) NoorEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.clickable {
                        selectedBookKey = "all"
                        selectedChapterNo = 0
                    }
                ) {
                    Text(
                        text = if (isBn) "সকল গ্রন্থ (All Books)" else "All Books",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (allBooksSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }

                BanglaHadithApiService.defaultBooks.forEach { book ->
                    val selected = selectedBookKey.equals(book.bookKey, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .testTag("hadith_book_${book.bookKey}")
                            .clickable {
                                selectedBookKey = book.bookKey
                                selectedChapterNo = 0
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = if (isBn) book.nameBn else book.nameEn,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${book.bookKey} • ${book.totalHadiths} হাদিস",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (selected) Color.White.copy(alpha = 0.85f) else NoorMintLuminous
                            )
                        }
                    }
                }
            }
        }

        // 5. Chapter Filter Strip when a specific Book is chosen
        if (availableChapters.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selectedChapterNo == 0) NoorEmeraldPrimary.copy(alpha = 0.22f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (selectedChapterNo == 0) NoorEmeraldPrimary else Color.Transparent
                        ),
                        modifier = Modifier.clickable { selectedChapterNo = 0 }
                    ) {
                        Text(
                            text = if (isBn) "সকল অধ্যায়" else "All Chapters",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedChapterNo == 0) NoorMintLuminous else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    availableChapters.forEach { ch ->
                        val active = selectedChapterNo == ch.chapterNo
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) NoorEmeraldPrimary.copy(alpha = 0.22f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (active) NoorEmeraldPrimary else Color.Transparent
                            ),
                            modifier = Modifier.clickable { selectedChapterNo = ch.chapterNo }
                        ) {
                            Text(
                                text = if (isBn) "অধ্যায় ${ch.chapterNo}: ${ch.nameBn}"
                                else "Ch ${ch.chapterNo}: ${ch.nameEn}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (active) NoorMintLuminous else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Hadith Cards List
        items(filteredHadiths, key = { it.id }) { hadith ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hadith_card_${hadith.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top Row: Book + Hadith # + Source API Tag + Authenticity Grade
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${hadith.bookBn} • হাদিস ${hadith.hadithNumber}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                            Text(
                                text = "${hadith.chapterBn} • ${hadith.source.labelEn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (hadith.arabic.isNotBlank()) {
                                IconButton(
                                    onClick = { onSpeakArabic(hadith.arabic) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Listen Arabic Hadith",
                                        tint = NoorMintLuminous,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NoorEmeraldPrimary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = hadith.gradeBn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NoorMintLuminous,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    if (hadith.arabic.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = hadith.arabic,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = AmiriFontFamily,
                                textDirection = TextDirection.Rtl,
                                lineHeight = 36.sp
                            ),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "বাংলা অনুবাদ: “${hadith.translationBn}”",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (hadith.translationEn.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "“${hadith.translationEn}”",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "বর্ণনাকারী (রাবী): ${hadith.narratorBn}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "সূত্র: ${hadith.references}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Inspect Raw API JSON Button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .testTag("hadith_json_btn_${hadith.id}")
                                    .clickable { inspectedHadithJson = hadith }
                            ) {
                                Text(
                                    text = "{ } JSON",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = NoorMintLuminous,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "${hadith.arabic}\n\n${hadith.translationBn}\n\nবর্ণনাকারী: ${hadith.narratorBn}\nসূত্র: ${hadith.references} (${hadith.gradeBn})"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Hadith"))
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Hadith",
                                    tint = NoorMintLuminous,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Raw JSON Inspector Modal for individual Hadith
    inspectedHadithJson?.let { record ->
        AlertDialog(
            onDismissRequest = { inspectedHadithJson = null },
            title = {
                Text(
                    text = "${record.source.labelEn} • JSON Payload",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Endpoint: ${record.endpointUrl}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                        color = NoorMintLuminous
                    )
                    Text(
                        text = "Repository: ${record.source.repoUrl}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = record.rawJson,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectedHadithJson = null }) {
                    Text(if (isBn) "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    // Overview Modal of all 3 Integrated Hadith Repositories & Endpoints
    if (showApiOverviewDialog) {
        AlertDialog(
            onDismissRequest = { showApiOverviewDialog = false },
            title = {
                Text(
                    text = "Bangla Hadith APIs & JSON Schemas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "1. alQuranBD/Bangla-Hadith-api",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                            Text(
                                text = "Repo: github.com/alQuranBD/Bangla-Hadith-api\n" +
                                    "• GET /api/hadith\n" +
                                    "• GET /api/hadith/{book_key}\n" +
                                    "• GET /api/hadith/{book_key}/{chapter_no}/{page_no}\n" +
                                    "Fields: book_key, chSerial, hadithNo, hadithArabic, hadithBengali, hadithEnglish, rabi_bn",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "2. RakibRahman/hadith-api",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                            Text(
                                text = "Repo: github.com/RakibRahman/hadith-api.git\n" +
                                    "• GET https://bn-hadith-api.herokuapp.com/hadiths\n" +
                                    "Fields: id, name, description, references, grade",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "3. MuntashirAkon/BanglaHadith",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                            Text(
                                text = "Repo: github.com/MuntashirAkon/BanglaHadith\n" +
                                    "• SQLite / JSON: hadith.db (books, chapters, hadith)\n" +
                                    "Fields: book, chapter, hadith_no, narrator, arabic, bangla, grade",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showApiOverviewDialog = false }) {
                    Text(if (isBn) "ঠিক আছে" else "Done")
                }
            }
        )
    }
}

@Composable
fun NearbyMosquesScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isBn = uiState.language == "bn"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("nearby_mosques_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "নিকটস্থ মসজিদ ও জামাতের সময়",
                titleEn = "Nearby Mosques & Jama'at",
                subtitle = "বাংলাদেশের প্রসিদ্ধ মসজিদসমূহ ও জামাতের সময়সূচী",
                isBn = isBn,
                onBack = onBack
            )
        }

        items(RuhaniContentCatalog.mosqueList) { mosque ->
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
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mosque.nameBn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                            Text(
                                text = "${mosque.nameEn} • ${mosque.addressBn}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NoorEmeraldPrimary.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = mosque.distanceKm,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "জামাত: ${mosque.jamaatSummaryBn}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "জুমআ: ${mosque.jummahTimeBn} • সুবিধা: ${mosque.facilitiesBn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val mapUri = Uri.parse("geo:0,0?q=${Uri.encode(mosque.nameEn + " " + mosque.district)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                            try {
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "ম্যাপে দিকনির্দেশনা দেখুন" else "Open in Google Maps")
                    }
                }
            }
        }
    }
}

@Composable
fun IslamicBooksScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onToggleArticleOffline: (OfflineArticleItem) -> Unit = {},
    onUpdateArticleProgress: (OfflineArticleItem, Int) -> Unit = { _, _ -> },
    onPlayCompanionAudio: (Int, Int) -> Unit = { _, _ -> },
    onSyncOfflineNow: () -> Unit = {}
) {
    BackHandler { onBack() }
    val isBn = uiState.language == "bn"
    var selectedBookId by remember { mutableStateOf<String?>(null) }
    var expandedArticleId by remember { mutableStateOf<String?>("art_khushu_salat") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("islamic_books_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "ইসলামিক বই, প্রবন্ধ ও অফলাইন লাইব্রেরি",
                titleEn = "Islamic Library & Offline Articles",
                subtitle = "অফলাইনে পড়ার ও শোনার জন্য প্রবন্ধ, অডিও তিলাওয়াত ও তাফসীর গ্রন্থসমূহ",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Offline Sync & Storage Banner
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NoorEmeraldPrimary.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.isDeviceOnline) {
                                "● অনলাইন • অফলাইন স্টোরেজ: ${uiState.offlineStorageUsedKb} KB"
                            } else {
                                "○ অফলাইন মোড • সংরক্ষিত কন্টেন্ট সচল"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = NoorMintLuminous
                        )
                        Text(
                            text = "ডাউনলোডকৃত প্রবন্ধ: ${uiState.downloadedArticleIds.size}টি • সূরা অডিও: ${uiState.downloadedSurahIds.size}টি • পেন্ডিং সিঙ্ক: ${uiState.pendingSyncQueue.size}টি",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (uiState.pendingSyncQueue.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onSyncOfflineNow,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("library_sync_now_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBn) "সিঙ্ক" else "Sync", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = if (isBn) "অফলাইন প্রবন্ধ ও অডিও (Offline Articles & Audio)" else "Offline Articles & Companion Audio",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NoorMintLuminous
            )
        }

        items(OfflineContentAndSyncManager.curatedArticles, key = { it.id }) { article ->
            val isDownloaded = article.id in uiState.downloadedArticleIds
            val isExpanded = expandedArticleId == article.id
            val progress = uiState.articleReadProgress[article.id] ?: 0

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (isDownloaded) NoorEmeraldPrimary.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_article_card_${article.id}")
                    .clickable { expandedArticleId = if (isExpanded) null else article.id }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NoorEmeraldPrimary.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = article.categoryBn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NoorMintLuminous,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                if (isDownloaded) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NoorGoldAccent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "✓ অফলাইনে সংরক্ষিত (${article.sizeKb} KB)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NoorGoldAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = article.titleBn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${article.authorBn} • ${article.estimatedMinutes} মিনিট পাঠ • অগ্রগতি: $progress%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onToggleArticleOffline(article) },
                            modifier = Modifier.testTag("toggle_offline_article_${article.id}")
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                                contentDescription = "Download Article Offline",
                                tint = if (isDownloaded) NoorMintLuminous else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = article.contentBn,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onPlayCompanionAudio(
                                        article.audioRecitationSurahNo,
                                        article.audioRecitationAyahNo
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = NoorMintLuminous
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBn) "অফলাইন অডিও শুনুন" else "Listen Audio",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(50, 100).forEach { pct ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (progress >= pct) NoorEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable {
                                            onUpdateArticleProgress(article, pct)
                                        }
                                    ) {
                                        Text(
                                            text = "$pct% পঠিত",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (progress >= pct) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isBn) "ইসলামিক গ্রন্থসমূহ (Classical Islamic Books)" else "Classical Islamic Books",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = NoorMintLuminous
            )
        }

        items(RuhaniContentCatalog.islamicBooks) { book ->
            val expanded = selectedBookId == book.id
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedBookId = if (expanded) null else book.id }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "${book.titleBn} (${book.titleEn})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NoorMintLuminous)
                            Text(text = "লেখক: ${book.authorBn} • ${book.categoryBn} • ${book.pages} পৃষ্ঠা", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = book.summaryBn, style = MaterialTheme.typography.bodySmall)
                    if (expanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NoorEmeraldPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = book.sampleExcerptBn,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FatwaScreen(uiState: RuhaniUiState, onBack: () -> Unit) {
    BackHandler { onBack() }
    val isBn = uiState.language == "bn"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("fatwa_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "মাসআলা ও ফতোয়া (Q&A)",
                titleEn = "Mas'ala & Verified Fatwa",
                subtitle = "প্রামাণিক ফিকহ ও শরয়ী প্রশ্নের দলিলভিত্তিক উত্তর",
                isBn = isBn,
                onBack = onBack
            )
        }

        items(RuhaniContentCatalog.fatwaList) { fatwa ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NoorEmeraldPrimary.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = fatwa.categoryBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = NoorMintLuminous,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "প্রশ্ন: ${fatwa.questionBn}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "উত্তর: ${fatwa.answerBn}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "দলিল ও সূত্র: ${fatwa.scholarReferenceBn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NoorMintLuminous
                    )
                }
            }
        }
    }
}

@Composable
fun HajjGuideScreen(uiState: RuhaniUiState, onBack: () -> Unit) {
    BackHandler { onBack() }
    val isBn = uiState.language == "bn"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hajj_guide_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "হজ্জ ও উমরাহ গাইড",
                titleEn = "Hajj & Umrah Step-by-Step",
                subtitle = "ইহরাম, তাওয়াফ, সাঈ, আরাফাহ ও কুরবানির পূর্ণাঙ্গ নিয়ম",
                isBn = isBn,
                onBack = onBack
            )
        }

        items(RuhaniContentCatalog.hajjSteps) { step ->
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
                        Text(
                            text = "ধাপ ${step.stepNumber}: ${step.titleBn}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NoorMintLuminous
                        )
                        Text(
                            text = step.locationBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = step.descriptionBn, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = step.duaArabic,
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = AmiriFontFamily),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = step.duaBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = NoorMintLuminous
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BabyNamesScreen(uiState: RuhaniUiState, onBack: () -> Unit) {
    BackHandler { onBack() }
    val isBn = uiState.language == "bn"
    var filterGender by remember { mutableStateOf("ALL") } // "ALL", " ছেলে (Boy)", "মেয়ে (Girl)"

    val names = remember(filterGender) {
        if (filterGender == "ALL") RuhaniContentCatalog.babyNames
        else RuhaniContentCatalog.babyNames.filter { it.genderBn.contains(filterGender.trim()) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("baby_names_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "ইসলামিক শিশুর নাম ও অর্থ",
                titleEn = "Islamic Baby Names",
                subtitle = "কুরআন ও হাদিসসম্মত সুন্দর ছেলে ও মেয়ের নাম",
                isBn = isBn,
                onBack = onBack
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL" to "সকল নাম", "ছেলে" to "ছেলে (Boy)", "মেয়ে" to "মেয়ে (Girl)").forEach { (key, label) ->
                    val selected = filterGender == key
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { filterGender = key }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        items(names) { item ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.nameBn} (${item.nameEn})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NoorMintLuminous
                        )
                        Text(
                            text = "অর্থ: ${item.meaningBn} (${item.meaningEn})",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${item.genderBn} • উৎস: ${item.origin}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = item.arabic,
                        style = MaterialTheme.typography.headlineSmall.copy(fontFamily = AmiriFontFamily),
                        color = NoorMintLuminous
                    )
                }
            }
        }
    }
}

@Composable
fun HaramainLiveScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onSelectChannel: (String) -> Unit,
    onTogglePlayback: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isBn = uiState.language == "bn"
    val isMakkah = uiState.haramainChannel == "MAKKAH"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("haramain_live_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "মক্কা ও মদিনা ২৪/৭ লাইভ",
                titleEn = "Haramain 24/7 Live Stream",
                subtitle = "মসজিদুল হারাম (কাবা শরীফ) ও মসজিদে নববীর সরাসরি সম্প্রচার",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Channel Selector Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onSelectChannel("MAKKAH") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMakkah) NoorEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🕋 মক্কা লাইভ (Makkah)",
                        color = if (isMakkah) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
                Button(
                    onClick = { onSelectChannel("MADINAH") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isMakkah) NoorEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "🕌 মদিনা লাইভ (Madinah)",
                        color = if (!isMakkah) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Live Broadcast Player Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF153529), Color(0xFF0A1410))))
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(6.dp), color = NoorRedAlert) {
                            Text(
                                text = "● LIVE 24/7 HD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = if (isMakkah) "Al-Quran Al-Kareem TV" else "Al-Sunnah Al-Nabawiyah TV",
                            style = MaterialTheme.typography.labelSmall,
                            color = NoorMintLuminous
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = if (isMakkah) "🕋" else "🕌",
                        fontSize = 54.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isMakkah) "মসজিদুল হারাম, মক্কা আল-মুকাররমাহ" else "মসজিদে নববী, মদিনা আল-মুনাওয়ারাহ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isMakkah) "Live Tawaf & Tilawat from Masjid al-Haram"
                        else "Live Rawdah & Hadith from Masjid an-Nabawi",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA3B8B0)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onTogglePlayback,
                            colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary)
                        ) {
                            Icon(
                                imageVector = if (uiState.isHaramainPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uiState.isHaramainPlaying) "স্ট্রীম চলছে (Playing)" else "চালু করুন (Play)")
                        }

                        OutlinedButton(
                            onClick = {
                                val url = if (isMakkah) "https://www.youtube.com/results?search_query=Makkah+Live+Official"
                                else "https://www.youtube.com/results?search_query=Madinah+Live+Official"
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (_: Exception) {
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("HD Video")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onSyncNow: () -> Unit,
    onRegisterEmailPassword: (String, String, String, Boolean) -> Unit = { _, _, _, _ -> },
    onLoginEmailPassword: (String, String) -> Unit = { _, _ -> },
    onSignOutSession: () -> Unit = {},
    onUpdatePrivacySettings: (Boolean, Boolean, Boolean) -> Unit = { _, _, _ -> },
    onExportPrivacyData: () -> Unit = {},
    onDismissExportedData: () -> Unit = {},
    onDeleteAccountAndData: () -> Unit = {},
    onSynchronizeOfflineQueue: () -> Unit = {},
    onToggleNotificationPref: (String) -> Unit = {},
    onSendPersonalizedAlert: () -> Unit = {},
    onSendNewContentAlert: () -> Unit = {},
    onSendWaqtTestAlert: () -> Unit = {},
    onMarkAllNotificationsRead: () -> Unit = {}
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = uiState.language == "bn"
    var nameInput by remember(uiState.displayName) { mutableStateOf(uiState.displayName) }
    var phoneInput by remember(uiState.phone) { mutableStateOf(uiState.phone) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Email/Password Auth State
    var isRegisterMode by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var registerNameInput by remember { mutableStateOf("") }
    var privacyConsentAccepted by remember { mutableStateOf(true) }

    // Runtime POST_NOTIFICATIONS permission launcher (Android 13+)
    var hasNotifPermission by remember {
        mutableStateOf(RuhaniPushNotificationManager.hasNotificationPermission(context))
    }
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "প্রোফাইল ও ব্যক্তিগত পছন্দসমূহ",
                titleEn = "Profile & Personal Preferences",
                subtitle = "অ্যাকাউন্ট আইডি, ক্লাউড ব্যাকআপ, পুশ নোটিফিকেশন ও ডেটা প্রাইভেসী",
                isBn = isBn,
                onBack = onBack
            )
        }

        // 1. Secure User Authentication Hub (Email/Password + Google Sign-In)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(NoorEmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.currentUserId != null) Icons.Default.CloudDone else Icons.Default.Lock,
                                contentDescription = null,
                                tint = NoorMintLuminous,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    uiState.currentUserId != null && uiState.authProvider == "GOOGLE" ->
                                        "Google Account: ${uiState.userEmail ?: "Connected"}"
                                    uiState.currentUserId != null && uiState.authProvider == "EMAIL_PASSWORD" ->
                                        "Email Account (PBKDF2-SHA256): ${uiState.userEmail}"
                                    else ->
                                        if (isBn) "গেস্ট মোড • সাইন ইন করে ক্লাউড সিঙ্ক চালু করুন" else "Guest Mode • Sign in to enable sync"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = NoorMintLuminous
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (uiState.currentUserId == null) {
                        // Mode Switcher: Login vs Register
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (!isRegisterMode) NoorEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_tab_login")
                                    .clickable { isRegisterMode = false }
                            ) {
                                Text(
                                    text = if (isBn) "ইমেইল লগইন (Login)" else "Email Login",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isRegisterMode) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isRegisterMode) NoorEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_tab_register")
                                    .clickable { isRegisterMode = true }
                            ) {
                                Text(
                                    text = if (isBn) "নতুন নিবন্ধন (Register)" else "Register Account",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRegisterMode) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isRegisterMode) {
                            OutlinedTextField(
                                value = registerNameInput,
                                onValueChange = { registerNameInput = it },
                                label = { Text(if (isBn) "পূর্ণ নাম (Full Name)" else "Full Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_register_name_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text(if (isBn) "ইমেইল ঠিকানা (Email Address)" else "Email Address") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(if (isBn) "পাসওয়ার্ড (কমপক্ষে ৮ অক্ষর ও সংখ্যা)" else "Password (min 8 chars)") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                TextButton(onClick = { showPassword = !showPassword }) {
                                    Text(
                                        text = if (showPassword) "Hide" else "Show",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        if (isRegisterMode) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = privacyConsentAccepted,
                                    onCheckedChange = { privacyConsentAccepted = it },
                                    modifier = Modifier.testTag("auth_privacy_checkbox")
                                )
                                Text(
                                    text = if (isBn) {
                                        "আমি গোপনীয়তা নীতিমালা ও PBKDF2 এনক্রিপ্টেড ডেটা সুরক্ষায় সম্মতি দিচ্ছি"
                                    } else {
                                        "I agree to Privacy Best Practices & encrypted data handling"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                authError = null
                                if (isRegisterMode) {
                                    onRegisterEmailPassword(
                                        registerNameInput.ifBlank { nameInput },
                                        emailInput,
                                        passwordInput,
                                        privacyConsentAccepted
                                    )
                                } else {
                                    onLoginEmailPassword(emailInput, passwordInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_submit_email_btn")
                        ) {
                            Text(
                                text = if (isRegisterMode) {
                                    if (isBn) "ইমেইল দিয়ে নিবন্ধন করুন (Register)" else "Create Account with Email"
                                } else {
                                    if (isBn) "ইমেইল দিয়ে লগইন করুন (Login)" else "Sign In with Email"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                authError = null
                                scope.launch {
                                    GoogleAuthHelper.onGoogleSignInClicked(
                                        context = context,
                                        onError = { err -> authError = err }
                                    )
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, NoorMintLuminous.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_google_sign_in_btn")
                        ) {
                            Text(
                                text = if (isBn) "অথবা Google দিয়ে সাইন ইন করুন (Google Sign-In)" else "Continue with Google Sign-In",
                                color = NoorMintLuminous,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    onSyncNow()
                                    onSynchronizeOfflineQueue()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isBn) "ক্লাউডে সিঙ্ক করুন" else "Sync Now", color = Color.White)
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        GoogleAuthHelper.signOut(context)
                                        onSignOutSession()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_sign_out_btn")
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isBn) "সাইন আউট" else "Sign Out")
                            }
                        }
                    }

                    if (authError != null || uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authError ?: uiState.errorMessage ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = NoorRedAlert
                        )
                    }
                    if (uiState.syncStatusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.syncStatusMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = NoorMintLuminous
                        )
                    }
                }
            }
        }

        // 2. Offline Capabilities & Sync Queue Engine Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_sync_engine_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = NoorMintLuminous)
                            Text(
                                text = if (isBn) "অফলাইন ডাউনলোড ও অটো-সিঙ্ক ইঞ্জিন" else "Offline Content & Auto-Sync",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (uiState.isDeviceOnline) NoorEmeraldPrimary.copy(alpha = 0.2f) else NoorGoldAccent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (uiState.isDeviceOnline) "ONLINE" else "OFFLINE",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isDeviceOnline) NoorMintLuminous else NoorGoldAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "অফলাইন অডিও সূরা: ${uiState.downloadedSurahIds.size}টি • অফলাইন প্রবন্ধ: ${uiState.downloadedArticleIds.size}টি • স্টোরেজ: ${uiState.offlineStorageUsedKb} KB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "সর্বশেষ সিঙ্ক: ${uiState.lastSyncedLabel} • অপেক্ষমাণ (Pending) পরিবর্তন: ${uiState.pendingSyncQueue.size}টি",
                        style = MaterialTheme.typography.labelSmall,
                        color = NoorMintLuminous
                    )

                    if (uiState.pendingSyncQueue.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            uiState.pendingSyncQueue.take(3).forEach { item ->
                                Text(
                                    text = "• ${item.descriptionBn}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onSynchronizeOfflineQueue,
                        colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_offline_changes_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "অফলাইন অগ্রগতি এখনই সিঙ্ক করুন (Sync Progress)" else "Synchronize Offline Progress Now",
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 3. Push & Personalized Notification Service Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("push_notification_service_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = NoorGoldAccent)
                            Text(
                                text = if (isBn) "পুশ নোটিফিকেশন ও রিমাইন্ডার সার্ভিস" else "Push Notification Service",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            OutlinedButton(
                                onClick = {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isBn) "অনুমতি দিন" else "Allow", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "নতুন কন্টেন্ট ও হাদিস আপডেট" else "New Content Alerts",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = uiState.notifyNewContentAlerts,
                            onCheckedChange = { onToggleNotificationPref("new_content") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "গুরুত্বপূর্ণ ইসলামিক দিবস ও ওয়াক্ত আপডেট" else "Important Islamic & Waqt Updates",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = uiState.notifyImportantUpdates,
                            onCheckedChange = { onToggleNotificationPref("important_updates") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "ব্যক্তিগত আমল ও তিলাওয়াত রিমাইন্ডার" else "Personalized Activity Alerts",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = uiState.notifyPersonalizedActivity,
                            onCheckedChange = { onToggleNotificationPref("personalized_activity") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSendPersonalizedAlert,
                            colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_personalized_notif_btn")
                        ) {
                            Text(
                                text = if (isBn) "আমল অ্যালার্ট" else "Personal Alert",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = onSendNewContentAlert,
                            colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_content_notif_btn")
                        ) {
                            Text(
                                text = if (isBn) "নতুন কন্টেন্ট" else "Content Alert",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                        OutlinedButton(
                            onClick = onSendWaqtTestAlert,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_waqt_notif_btn")
                        ) {
                            Text(
                                text = if (isBn) "আযান টেস্ট" else "Waqt Alert",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    if (uiState.notificationFeed.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "সাম্প্রতিক নোটিফিকেশনসমূহ" else "Recent Notifications",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                            TextButton(onClick = onMarkAllNotificationsRead) {
                                Text(if (isBn) "সব পঠিত" else "Mark Read", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        uiState.notificationFeed.take(4).forEach { notif ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = notif.titleBn,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (notif.isRead) MaterialTheme.colorScheme.onSurface else NoorMintLuminous
                                        )
                                        Text(
                                            text = notif.timestampLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = notif.messageBn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Privacy & Data Protection Best Practices Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("privacy_controls_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = NoorMintLuminous)
                        Text(
                            text = if (isBn) "গোপনীয়তা ও ডেটা সুরক্ষা (Privacy & Security)" else "Privacy & Data Protection",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "পাসওয়ার্ড সমূহ PBKDF2-HMAC-SHA256 সল্টেড হ্যাশিং দ্বারা সুরক্ষিত এবং ক্লাউড ডেটা শুধুমাত্র আপনার ইউজার আইডিতে সীমাবদ্ধ।",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "ক্লাউড ব্যাকআপ ও সিঙ্ক সম্মতি" else "Cloud Sync Consent",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = uiState.privacyCloudSyncConsent,
                            onCheckedChange = {
                                onUpdatePrivacySettings(
                                    it,
                                    uiState.privacyPersonalizedAlertsConsent,
                                    uiState.privacyDataMinimization
                                )
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "ডেটা মিনিমাইজেশন (অপ্রয়োজনীয় টেলিমেট্রি বন্ধ)" else "Strict Data Minimization",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Switch(
                            checked = uiState.privacyDataMinimization,
                            onCheckedChange = {
                                onUpdatePrivacySettings(
                                    uiState.privacyCloudSyncConsent,
                                    uiState.privacyPersonalizedAlertsConsent,
                                    it
                                )
                            }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onExportPrivacyData,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_privacy_data_btn")
                        ) {
                            Text(if (isBn) "ডেটা এক্সপোর্ট (JSON)" else "Export My Data", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = onDeleteAccountAndData,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, NoorRedAlert.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("delete_account_data_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = NoorRedAlert, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "ডেটা মুছুন" else "Delete Data",
                                style = MaterialTheme.typography.labelSmall,
                                color = NoorRedAlert
                            )
                        }
                    }
                }
            }
        }

        // 5. Editable Profile Details
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isBn) "ব্যক্তিগত তথ্য সম্পাদনা" else "Edit Profile Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(if (isBn) "আপনার নাম (Display Name)" else "Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text(if (isBn) "মোবাইল নম্বর (ঐচ্ছিক)" else "Mobile Number (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onUpdateProfile(nameInput, phoneInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isBn) "সংরক্ষণ করুন (Save Profile)" else "Save Profile", color = Color.White)
                    }
                }
            }
        }
    }

    if (uiState.exportedUserDataJson != null) {
        AlertDialog(
            onDismissRequest = onDismissExportedData,
            title = {
                Text(
                    text = if (isBn) "আপনার সংরক্ষিত ব্যক্তিগত ডেটা (JSON)" else "Your Exported Personal Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.exportedUserDataJson,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissExportedData) {
                    Text(if (isBn) "বন্ধ করুন" else "Close")
                }
            }
        )
    }
}

@Composable
fun SettingsScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onUpdateTheme: (String) -> Unit,
    onUpdateLanguage: (String) -> Unit,
    onToggleBanglaDigits: () -> Unit,
    onUpdateDistrict: (BdDistrict) -> Unit,
    onUpdateMethod: (CalculationMethod) -> Unit,
    onUpdateMadhhab: (Madhhab) -> Unit,
    onToggleTasbihVibration: () -> Unit = {},
    onToggleTasbihSound: () -> Unit = {}
) {
    BackHandler { onBack() }
    val isBn = uiState.language == "bn"
    var showDistrictDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "অ্যাপ সেটিংস",
                titleEn = "App Settings",
                subtitle = "সিস্টেম থিম, ভাষা, জেলা, হিসাব পদ্ধতি ও মাযহাব",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Theme Mode (Noor Dark / Vibrant Light / System)
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = NoorMintLuminous)
                        Text(
                            text = if (isBn) "অ্যাপ থিম (Appearance)" else "App Theme Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DARK" to "Noor Dark", "LIGHT" to "Vibrant Light", "SYSTEM" to "System").forEach { (mode, label) ->
                            val selected = uiState.themeMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_mode_$mode")
                                    .clickable { onUpdateTheme(mode) }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Language & Numeral Format
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = NoorMintLuminous)
                        Text(
                            text = if (isBn) "ভাষা ও সংখ্যা (Language & Digits)" else "Language & Numerals",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("bn" to "বাংলা (Bangla)", "en" to "English").forEach { (code, label) ->
                            val selected = uiState.language == code
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateLanguage(code) }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "বাংলা সংখ্যা ব্যবহার করুন (০১২৩...)" else "Use Bangla Numerals (০১২৩...)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = uiState.useBanglaDigits,
                            onCheckedChange = { onToggleBanglaDigits() }
                        )
                    }
                }
            }
        }

        // Madhhab & Calculation Method
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBn) "মাযহাব ও আসরের ওয়াক্ত (Asr Juristic Method)" else "Madhhab (Asr Calculation)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Madhhab.entries.forEach { m ->
                        val selected = uiState.madhhab == m
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) NoorEmeraldPrimary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selected) BorderStroke(1.dp, NoorEmeraldPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateMadhhab(m) }
                        ) {
                            Text(
                                text = if (isBn) m.titleBn else m.titleEn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBn) "নামাজের সময় হিসাব পদ্ধতি" else "Prayer Time Calculation Authority",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    CalculationMethod.entries.forEach { method ->
                        val selected = uiState.calculationMethod == method
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) NoorEmeraldPrimary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selected) BorderStroke(1.dp, NoorEmeraldPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onUpdateMethod(method) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBn) method.titleBn else method.titleEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Fajr ${method.fajrAngle}° • Isha ${method.ishaAngle}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { showDistrictDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("জেলা পরিবর্তন করুন: ${uiState.selectedDistrict.nameBn} (${uiState.selectedDistrict.nameEn})")
                    }
                }
            }
        }

        // Tasbih Settings (Vibration & Sound Toggles)
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = NoorMintLuminous)
                        Text(
                            text = if (isBn) "ডিজিটাল তাসবীহ সেটিংস (Tasbih Counter)" else "Digital Tasbih Settings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Vibration Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = if (uiState.tasbihVibrationEnabled) NoorEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (isBn) "হ্যাপটিক ভাইব্রেশন" else "Haptic Vibration",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isBn) "প্রতি গণনায় মৃদু কম্পন" else "Vibration feedback on tap",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.tasbihVibrationEnabled,
                            onCheckedChange = { onToggleTasbihVibration() }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    // Sound Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.tasbihSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = null,
                                tint = if (uiState.tasbihSoundEnabled) NoorEmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = if (isBn) "ক্লিক সাউন্ড" else "Click Sound",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isBn) "গণনার সময় অডিও ক্লিক" else "Play sound effect on tap",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = uiState.tasbihSoundEnabled,
                            onCheckedChange = { onToggleTasbihSound() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Developer / Company Branding Card
            val context = LocalContext.current
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://khanscreations.com"))
                        context.startActivity(intent)
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.khanscreations_logo),
                        contentDescription = "KhansCreations Logo",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ruhani (রুহানি) v1.0",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Powered by KhansCreations",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NoorEmeraldPrimary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "https://khanscreations.com",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDistrictDialog) {
        DistrictPickerDialog(
            selectedDistrict = uiState.selectedDistrict,
            isBn = isBn,
            onDismiss = { showDistrictDialog = false },
            onSelect = {
                onUpdateDistrict(it)
                showDistrictDialog = false
            }
        )
    }
}

@Composable
fun OnboardingAndAuthScreen(
    uiState: RuhaniUiState,
    onSelectDistrict: (BdDistrict) -> Unit,
    onSelectMadhhab: (Madhhab) -> Unit,
    onCompleteOnboarding: (Boolean) -> Unit,
    onRegisterEmailPassword: (String, String, String, Boolean) -> Unit = { _, _, _, _ -> },
    onLoginEmailPassword: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAuthStep by remember { mutableStateOf(false) }
    var showDistrictModal by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isRegisterTab by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var consentChecked by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF10281F), Color(0xFF080D0B))
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!showAuthStep) {
            // STEP 1: GET STARTED
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // App Brand Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(NoorEmeraldPrimary, NoorEmeraldDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NightlightRound,
                        contentDescription = "Ruhani",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "রুহানি • Ruhani",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "বাংলাদেশের মুসলিমদের জন্য পূর্ণাঙ্গ ইসলামিক লাইফস্টাইল অ্যাপ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA3B8B0),
                    textAlign = TextAlign.Center
                )

                // Feature Highlights
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val highlights = listOf(
                        Triple(Icons.Default.Schedule, "সঠিক নামাজের সময়সূচী ও আজান", "৬৪টি জেলা ও ইসলামিক ফাউন্ডেশন মেথড অনুযায়ী নির্ভুল সময়"),
                        Triple(Icons.AutoMirrored.Filled.MenuBook, "আল-কুরআন (১১৪টি পূর্ণাঙ্গ সূরা)", "প্রতিটি সূরার আয়াতভিত্তিক আরবি, বাংলা অনুবাদ ও ক্বারীদের অডিও"),
                        Triple(Icons.Default.Fingerprint, "ডিজিটাল তাসবীহ ও হিসনুল মুসলিম", "প্রাত্যহিক সকাল-সন্ধ্যার মাসনুন দু'আ ও স্মার্ট ভাইব্রেশন কাউন্টার"),
                        Triple(Icons.Default.Explore, "সরাসরি কিবলা কম্পাস ও ক্যালেন্ডার", "রিয়েল-টাইম সেন্সর কম্পাস এবং বাংলা-ইংরেজি-হিজরি ক্যালেন্ডার")
                    )

                    highlights.forEach { (icon, title, desc) ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF141E1A),
                            border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NoorEmeraldPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = NoorMintLuminous,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF8FA89E)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Setup Card (District & Madhhab)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF141E1A),
                    border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "আপনার জেলা ও মাযহাব নির্বাচন করুন",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = NoorMintLuminous
                        )

                        OutlinedButton(
                            onClick = { showDistrictModal = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF283A32)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = NoorMintLuminous
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "জেলা: " + uiState.selectedDistrict.nameBn + " (" + uiState.selectedDistrict.nameEn + ")",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Madhhab.entries.forEach { m ->
                                val selected = uiState.madhhab == m
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) NoorEmeraldPrimary else Color(0xFF1E2C26),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectMadhhab(m) }
                                ) {
                                    Text(
                                        text = if (m == Madhhab.HANAFI) "হানাফী (Hanafi)" else "শাফেয়ী (Shafi'i)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Primary GET STARTED Button
                Button(
                    onClick = { showAuthStep = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_get_started_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "শুরু করুন • Get Started",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = "পরবর্তী ধাপে সাইন ইন করতে পারবেন অথবা সরাসরি অতিথি হিসেবে প্রবেশ করতে পারবেন",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF758D84),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            // STEP 2: SIGNUP / SIGNIN (OPTIONAL) WITH GUEST AT BOTTOM
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header Row with Back Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAuthStep = false }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অ্যাকাউন্ট তৈরি বা লগইন",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "আমল ও বুকমার্ক ক্লাউডে সংরক্ষণ করতে সাইন ইন করুন (ঐচ্ছিক)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA3B8B0)
                        )
                    }
                }

                // Email/Password Registration & Login Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF141E1A),
                    border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isRegisterTab) NoorEmeraldPrimary else Color(0xFF1E2C26),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isRegisterTab = false }
                            ) {
                                Text(
                                    text = "ইমেইল লগইন",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isRegisterTab) NoorEmeraldPrimary else Color(0xFF1E2C26),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isRegisterTab = true }
                            ) {
                                Text(
                                    text = "নতুন নিবন্ধন",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }

                        if (isRegisterTab) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("আপনার নাম") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("ইমেইল (Email)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("পাসওয়ার্ড (কমপক্ষে ৮ অক্ষর)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (isRegisterTab) {
                                    onRegisterEmailPassword(fullName, email, password, consentChecked)
                                } else {
                                    onLoginEmailPassword(email, password)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                text = if (isRegisterTab) "ইমেইল দিয়ে নিবন্ধন করুন" else "ইমেইল দিয়ে লগইন করুন",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Google Sign-In Button
                Button(
                    onClick = {
                        errorMsg = null
                        scope.launch {
                            GoogleAuthHelper.onGoogleSignInClicked(
                                context = context,
                                onSuccess = { onCompleteOnboarding(false) },
                                onError = { err -> errorMsg = err }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2C26)),
                    border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("onboarding_google_sign_in_btn")
                ) {
                    Text(
                        text = "Google দিয়ে সাইন ইন করুন (Cloud Backup)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Divider: অথবা
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF283A32))
                    Text(
                        text = " অথবা • OR ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8FA89E),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF283A32))
                }

                // BOTTOM AS A GUEST OPTION (Prominent Outlined)
                OutlinedButton(
                    onClick = { onCompleteOnboarding(true) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, NoorMintLuminous),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_guest_continue_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = null,
                            tint = NoorMintLuminous
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "সাইন ইন ছাড়াই অতিথি হিসেবে শুরু করুন (Continue as Guest)",
                            color = NoorMintLuminous,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "কোনো অ্যাকাউন্ট বা পাসওয়ার্ড ছাড়াই অ্যাপের সকল ফিচার সম্পূর্ণ বিনামূল্যে ব্যবহার করতে পারবেন",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF758D84),
                    textAlign = TextAlign.Center
                )

                if (errorMsg != null || uiState.errorMessage != null) {
                    Text(
                        text = errorMsg ?: uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = NoorRedAlert,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Powered by KhansCreations branding
                Text(
                    text = "Powered by KhansCreations (https://khanscreations.com)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NoorMintLuminous,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://khanscreations.com"))
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showDistrictModal) {
        DistrictPickerDialog(
            selectedDistrict = uiState.selectedDistrict,
            isBn = true,
            onDismiss = { showDistrictModal = false },
            onSelect = {
                onSelectDistrict(it)
                showDistrictModal = false
            }
        )
    }
}
