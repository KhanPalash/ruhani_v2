package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

        items(filtered) { item ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("more_item_${item.screen.name.lowercase()}")
                    .clickable { onOpenSubScreen(item.screen) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
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
                        Column {
                            Text(
                                text = "${item.titleBn} • ${item.titleEn}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.subtitleBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isBn = uiState.language == "bn"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hadith_collection_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "সহীহ হাদিস সম্ভার",
                titleEn = "Authentic Hadith Collection",
                subtitle = if (isBn) "সহীহ বুখারী, সহীহ মুসলিম ও রিয়াদুস সালেহীন" else "Sahih Bukhari, Sahih Muslim & Riyadus Salihin",
                isBn = isBn,
                onBack = onBack
            )
        }

        items(RuhaniContentCatalog.hadithList) { hadith ->
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
                            text = "${hadith.bookBn} • হাদিস ${hadith.hadithNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NoorMintLuminous
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NoorEmeraldPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = hadith.grade,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = hadith.arabic,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = AmiriFontFamily,
                            textDirection = TextDirection.Rtl
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "বাংলা অনুবাদ: “${hadith.translationBn}”",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“${hadith.translationEn}”",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বর্ণনাকারী: ${hadith.narratorBn} • ${hadith.topicBn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${hadith.translationBn}\n— ${hadith.bookBn} (${hadith.hadithNumber})")
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Hadith"))
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = NoorMintLuminous, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
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
fun IslamicBooksScreen(uiState: RuhaniUiState, onBack: () -> Unit) {
    BackHandler { onBack() }
    val isBn = uiState.language == "bn"
    var selectedBookId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("islamic_books_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "ইসলামিক বই ও তাফসীর গ্রন্থগার",
                titleEn = "Islamic Library",
                subtitle = "সীরাত, তাফসীর, হাদিস ও ফিকহ গ্রন্থসমূহ",
                isBn = isBn,
                onBack = onBack
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
    onSyncNow: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = uiState.language == "bn"
    var nameInput by remember(uiState.displayName) { mutableStateOf(uiState.displayName) }
    var phoneInput by remember(uiState.phone) { mutableStateOf(uiState.phone) }
    var authError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "প্রোফাইল ও ক্লাউড ব্যাকআপ",
                titleEn = "Profile & Cloud Backup",
                subtitle = "আপনার নামাজ, তাসবীহ ও বুকমার্ক ক্লাউডে সংরক্ষণ করুন",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Google Account Status Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f)),
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
                                imageVector = if (uiState.currentUserId != null) Icons.Default.CloudDone else Icons.Default.Person,
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
                                text = if (uiState.currentUserId != null) {
                                    "Google Account: ${uiState.userEmail ?: "Connected"}"
                                } else {
                                    if (isBn) "গেস্ট মোড • ক্লাউড সিঙ্ক নিষ্ক্রিয়" else "Guest Mode • Local storage only"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = NoorMintLuminous
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (uiState.currentUserId == null) {
                        Button(
                            onClick = {
                                authError = null
                                scope.launch {
                                    GoogleAuthHelper.onGoogleSignInClicked(
                                        context = context,
                                        onError = { err -> authError = err }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_google_sign_in_btn")
                        ) {
                            Text(
                                text = if (isBn) "Google দিয়ে সাইন ইন ও ব্যাকআপ চালু করুন" else "Sign in with Google to Sync Data",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onSyncNow,
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

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authError ?: "",
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

        // Editable Profile Details
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
    onUpdateMadhhab: (Madhhab) -> Unit
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
                titleBn = "সেটিংস ও পছন্দসমূহ",
                titleEn = "Settings & Preferences",
                subtitle = "থিম, ভাষা, জেলা, হিসাব পদ্ধতি ও মাযহাব",
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
    onCompleteOnboarding: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDistrictModal by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF122C22), Color(0xFF0D1311))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(NoorEmeraldPrimary, NoorEmeraldDark))),
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
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "বাংলাদেশের মুসলিমদের জন্য পূর্ণাঙ্গ নামাজের সময়সূচী, আল-কুরআন, তাফসীর, দোয়া ও তাসবীহ অ্যাপ",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFA3B8B0),
                textAlign = TextAlign.Center
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF18221E),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "আপনার জেলা ও মাযহাব নির্বাচন করুন",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = NoorMintLuminous
                    )

                    OutlinedButton(
                        onClick = { showDistrictModal = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = NoorMintLuminous)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "জেলা: ${uiState.selectedDistrict.nameBn} (${uiState.selectedDistrict.nameEn})",
                            color = Color.White
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Madhhab.entries.forEach { m ->
                            val selected = uiState.madhhab == m
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) NoorEmeraldPrimary else Color(0xFF1F2B26),
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

            // Primary Google Sign-In Button
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
                colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_google_sign_in_btn")
            ) {
                Text(
                    text = "Google দিয়ে সাইন ইন করুন (Cloud Backup)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Secondary Guest Mode Button
            OutlinedButton(
                onClick = { onCompleteOnboarding(true) },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NoorMintLuminous.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("onboarding_guest_continue_btn")
            ) {
                Text(
                    text = "সাইন ইন ছাড়াই শুরু করুন (Continue as Guest)",
                    color = NoorMintLuminous,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = NoorRedAlert,
                    textAlign = TextAlign.Center
                )
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
