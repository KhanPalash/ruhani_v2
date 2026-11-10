package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.*
import com.example.ui.RuhaniUiState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.NoorEmeraldDark
import com.example.ui.theme.NoorEmeraldPrimary
import com.example.ui.theme.NoorGoldAccent
import com.example.ui.theme.NoorMintLuminous
import com.example.ui.theme.NoorRedAlert
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun QuranBrowserScreen(
    uiState: RuhaniUiState,
    onOpenSurah: (QuranSurah) -> Unit,
    onToggleOfflineDownload: (Int) -> Unit
) {
    val isBn = uiState.language == "bn"
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0=Surahs, 1=Juz, 2=Bookmarks, 3=Offline

    val filteredSurahs = remember(searchQuery, selectedFilterTab, uiState.downloadedSurahIds) {
        val base = if (selectedFilterTab == 3) {
            QuranCatalog.surahs.filter { it.number in uiState.downloadedSurahIds }
        } else {
            QuranCatalog.surahs
        }
        if (searchQuery.isBlank()) base
        else base.filter {
            it.nameEn.contains(searchQuery, ignoreCase = true) ||
                it.nameBn.contains(searchQuery, ignoreCase = true) ||
                it.meaningEn.contains(searchQuery, ignoreCase = true) ||
                it.number.toString() == searchQuery.trim()
        }
    }

    val lastReadSurah = remember(uiState.lastReadSurahNumber) {
        QuranCatalog.findSurah(uiState.lastReadSurahNumber)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_browser_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Resume Last Read Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_last_read_banner")
                    .clickable { onOpenSurah(lastReadSurah) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF153529), Color(0xFF0E1F19))
                            )
                        )
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = NoorEmeraldPrimary.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = if (isBn) "সর্বশেষ পঠিত • LAST READ" else "LAST READ • সর্বশেষ পঠিত",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${lastReadSurah.nameBn} (${lastReadSurah.nameEn})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isBn) "আয়াত নং ${uiState.lastReadAyahNumber} • পারা ${lastReadSurah.juzNumber}"
                            else "Ayah ${uiState.lastReadAyahNumber} • Juz ${lastReadSurah.juzNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFA3B8B0)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = lastReadSurah.arabicTitle,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = AmiriFontFamily,
                                color = NoorMintLuminous
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NoorEmeraldPrimary
                        ) {
                            Text(
                                text = if (isBn) "পড়ুন →" else "Continue →",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Search Input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        if (isBn) "সূরার নাম বা নম্বর দিয়ে খুঁজুন (যেমন: ইয়াসিন, 36)..."
                        else "Search Surah by name or number (e.g. Ya-Sin, 36)..."
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Surah",
                        tint = NoorMintLuminous
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_search_input")
            )
        }

        // 3. Filter Tabs (Surahs, Juz, Bookmarks, Offline)
        item {
            val tabs = if (isBn) {
                listOf("সূরা সমূহ", "পারা (Juz)", "বুকমার্ক (${uiState.bookmarks.size})", "অফলাইন (${uiState.downloadedSurahIds.size})")
            } else {
                listOf("Surahs", "Juz (Para)", "Bookmarks (${uiState.bookmarks.size})", "Offline (${uiState.downloadedSurahIds.size})")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { idx, label ->
                    val selected = selectedFilterTab == idx
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .testTag("quran_filter_tab_$idx")
                            .clickable { selectedFilterTab = idx }
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

        // 4. Content based on selected tab
        when (selectedFilterTab) {
            1 -> {
                // Juz / Para List
                items(QuranCatalog.juzSummaryList) { (juzNum, titleBn, rangeEn) ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val matchingSurah = QuranCatalog.surahs.firstOrNull { it.juzNumber == juzNum }
                                    ?: QuranCatalog.surahs.first()
                                onOpenSurah(matchingSurah)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NoorEmeraldPrimary.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = juzNum.toString(),
                                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = SpaceGroteskFontFamily),
                                        fontWeight = FontWeight.Bold,
                                        color = NoorMintLuminous
                                    )
                                }
                                Column {
                                    Text(
                                        text = "$titleBn (Juz $juzNum)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = rangeEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open Juz",
                                tint = NoorMintLuminous
                            )
                        }
                    }
                }
            }

            2 -> {
                // Bookmarks List
                if (uiState.bookmarks.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = NoorMintLuminous,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isBn) "কোনো বুকমার্ক পাওয়া যায়নি" else "No Bookmarked Ayahs Yet",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBn) "সূরা পড়ার সময় বুকমার্ক আইকনে ট্যাপ করে আয়াত সংরক্ষণ করুন।"
                                    else "Tap the bookmark icon on any Ayah while reading to save it to cloud & device.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.bookmarks) { bm ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onOpenSurah(QuranCatalog.findSurah(bm.surahNumber.toInt()))
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${bm.surahNameBn} (${bm.surahNameEn}) • ${bm.surahNumber}:${bm.ayahNumber}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NoorMintLuminous
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Bookmarked",
                                        tint = NoorGoldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = bm.arabicPreview,
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = AmiriFontFamily),
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = bm.translationPreview,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                // Surah Cards
                items(filteredSurahs) { surah ->
                    val isDownloaded = surah.number in uiState.downloadedSurahIds
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("surah_item_${surah.number}")
                            .clickable { onOpenSurah(surah) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
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
                                        .background(NoorEmeraldPrimary.copy(alpha = 0.15f))
                                        .border(1.dp, NoorEmeraldPrimary.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = surah.number.toString().padStart(2, '0'),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = NoorMintLuminous
                                    )
                                }

                                Column {
                                    Text(
                                        text = "${surah.nameBn} • ${surah.nameEn}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isBn) "${surah.meaningBn} • ${surah.revelationTypeBn} • ${surah.totalAyahs} আয়াত"
                                        else "${surah.meaningEn} • ${surah.revelationType} • ${surah.totalAyahs} Ayahs",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = surah.arabicTitle,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = AmiriFontFamily,
                                        color = NoorMintLuminous
                                    )
                                )
                                IconButton(
                                    onClick = { onToggleOfflineDownload(surah.number) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                        contentDescription = "Offline Audio & Tafsir",
                                        tint = if (isDownloaded) NoorMintLuminous else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SurahReaderScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onSelectSurah: (QuranSurah) -> Unit,
    onAdjustFontSize: (Int) -> Unit,
    onSelectQari: (QariInfo) -> Unit,
    onToggleWordByWord: () -> Unit,
    onToggleHifzArabic: () -> Unit,
    onSetMushafMode: (MushafMode) -> Unit,
    onPlayOrPauseAyah: (QuranSurah, QuranAyah) -> Unit,
    onToggleGlobalPlayPause: () -> Unit,
    onStepAyahAudio: (Int) -> Unit,
    onToggleRepeat3x: () -> Unit,
    onToggleTafsir: (Int, Int) -> Unit,
    onToggleBookmark: (QuranSurah, QuranAyah) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val surah = uiState.selectedSurah
    val isBn = uiState.language == "bn"
    var showQariModal by remember { mutableStateOf(false) }
    var showFontModal by remember { mutableStateOf(false) }

    val allSurahs = QuranCatalog.surahs
    val currentSurahIndex = allSurahs.indexOfFirst { it.number == surah.number }.coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("surah_reader_screen")
    ) {
        // 1. Reader Top Control Strip (Matching surah_reader/screen.png)
        Surface(
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("surah_reader_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Surah ${surah.nameEn}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${surah.nameBn} • Juz ${surah.juzNumber} • ${surah.totalAyahs} Ayahs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Reader Control Pills: Font, Qari, Word-by-Word, Hifz, Mushaf
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReaderActionPill(
                            label = "Font",
                            active = showFontModal,
                            onClick = { showFontModal = !showFontModal }
                        )
                        ReaderActionPill(
                            label = "Qari",
                            active = false,
                            onClick = { showQariModal = true }
                        )
                        ReaderActionPill(
                            label = if (isBn) "শব্দার্থ" else "W-by-W",
                            active = uiState.showWordByWord,
                            onClick = onToggleWordByWord
                        )
                        ReaderActionPill(
                            label = "Hifz",
                            active = uiState.hifzHideArabic,
                            onClick = onToggleHifzArabic
                        )
                    }
                }

                // Font Size Quick Slider Bar
                AnimatedVisibility(visible = showFontModal) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "আরবি ফন্ট সাইজ: ${uiState.quranArabicFontSizeSp} sp"
                            else "Arabic Font Size: ${uiState.quranArabicFontSizeSp} sp",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onAdjustFontSize(-2) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("A-")
                            }
                            OutlinedButton(
                                onClick = { onAdjustFontSize(2) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("A+")
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            }
        }

        // 2. Scrollable Surah Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Surah Header Banner Card
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
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = NoorEmeraldPrimary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "SURAH ${surah.number.toString().padStart(2, '0')} • ${surah.revelationType.uppercase()} • ${surah.totalAyahs} AYAHS • ${surah.rukuCount} RUKU",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = surah.arabicTitle,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = AmiriFontFamily,
                                fontSize = 34.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "${surah.meaningEn} • ${surah.nameBn} (${surah.meaningBn})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFA3B8B0)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "Bangla: Muhiuddin Khan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFA3B8B0),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "Tafsir Ibn Kathir",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFA3B8B0),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NoorEmeraldPrimary.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "● Audio Ready",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NoorMintLuminous,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bismillah Banner
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = AmiriFontFamily,
                                fontSize = 26.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isBn) "পরম করুণাময় ও অসীম দয়ালু আল্লাহর নামে শুরু করছি"
                            else "In the name of Allah, the Entirely Merciful, the Especially Merciful",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Ayah Cards
            items(surah.ayahs) { ayah ->
                val isPlayingThisAyah =
                    uiState.playingSurahNumber == surah.number && uiState.playingAyahNumber == ayah.ayahNumber
                val tafsirKey = "${surah.number}_${ayah.ayahNumber}"
                val isTafsirExpanded = uiState.expandedTafsirAyahKey == tafsirKey
                val isBookmarked = uiState.bookmarks.any {
                    it.surahNumber == surah.number.toLong() && it.ayahNumber == ayah.ayahNumber.toLong()
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isPlayingThisAyah) NoorEmeraldPrimary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isPlayingThisAyah) 1.5.dp else 1.dp,
                        color = if (isPlayingThisAyah) NoorEmeraldPrimary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ayah_card_${surah.number}_${ayah.ayahNumber}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Top Row: Ayah Badge + Playing Pill + Action Buttons (Listen, Tafsir, Bookmark, Share)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isPlayingThisAyah) NoorEmeraldPrimary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${surah.number}:${ayah.ayahNumber}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (isPlayingThisAyah) Color.White else NoorMintLuminous,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                if (isPlayingThisAyah) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NoorEmeraldPrimary.copy(alpha = 0.22f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.GraphicEq,
                                                contentDescription = null,
                                                tint = NoorMintLuminous,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = if (uiState.isAudioPlaying) "PLAYING" else "SELECTED",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = NoorMintLuminous
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Play/Listen Button
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isPlayingThisAyah && uiState.isAudioPlaying) NoorEmeraldPrimary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .testTag("ayah_listen_btn_${ayah.ayahNumber}")
                                        .clickable { onPlayOrPauseAyah(surah, ayah) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingThisAyah && uiState.isAudioPlaying) Icons.Default.Pause
                                            else Icons.Default.PlayArrow,
                                            contentDescription = "Listen Ayah",
                                            tint = if (isPlayingThisAyah && uiState.isAudioPlaying) Color.White
                                            else NoorMintLuminous,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isPlayingThisAyah && uiState.isAudioPlaying) "Playing" else "Listen",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPlayingThisAyah && uiState.isAudioPlaying) Color.White
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Tafsir Button
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isTafsirExpanded) NoorEmeraldPrimary.copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isTafsirExpanded) BorderStroke(1.dp, NoorEmeraldPrimary) else null,
                                    modifier = Modifier
                                        .testTag("ayah_tafsir_btn_${ayah.ayahNumber}")
                                        .clickable { onToggleTafsir(surah.number, ayah.ayahNumber) }
                                ) {
                                    Text(
                                        text = if (isBn) "তাফসীর" else "Tafsir",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isTafsirExpanded) NoorMintLuminous
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                // Bookmark Toggle
                                IconButton(
                                    onClick = { onToggleBookmark(surah, ayah) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("ayah_bookmark_btn_${ayah.ayahNumber}")
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark Ayah",
                                        tint = if (isBookmarked) NoorGoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Share Ayah
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "${ayah.arabicText}\n\n${ayah.translationBn}\n${ayah.translationEn}\n— Surah ${surah.nameEn} (${surah.number}:${ayah.ayahNumber})"
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Ayah"))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Ayah",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Right-aligned Quranic Arabic Text (with Hifz Mode support)
                        if (uiState.hifzHideArabic) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleHifzArabic() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Reveal Arabic",
                                        tint = NoorMintLuminous,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBn) "হিফজ মোড সক্রিয় • আরবি দেখতে ট্যাপ করুন"
                                        else "Hifz Memorization Active • Tap to reveal Arabic",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = NoorMintLuminous
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "${ayah.arabicText} ﴿${ayah.ayahNumber}﴾",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = AmiriFontFamily,
                                    fontSize = uiState.quranArabicFontSizeSp.sp,
                                    lineHeight = (uiState.quranArabicFontSizeSp * 1.75f).sp,
                                    textDirection = TextDirection.Rtl
                                ),
                                textAlign = TextAlign.Right,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Optional Word-by-Word Breakdown Chips
                        AnimatedVisibility(visible = uiState.showWordByWord && ayah.words.isNotEmpty()) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ayah.words.forEach { word ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = word.arabic,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = AmiriFontFamily),
                                                    color = NoorMintLuminous
                                                )
                                                Text(
                                                    text = word.bangla,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = word.english,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Transliteration
                        Text(
                            text = ayah.transliteration,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // Bangla Translation
                        Text(
                            text = ayah.translationBn,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // English Translation
                        Text(
                            text = ayah.translationEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Expandable Tafsir Ibn Kathir Box
                        AnimatedVisibility(visible = isTafsirExpanded) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = NoorEmeraldPrimary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isBn) "তাফসীর ইবনে কাসীর (Tafsir Ibn Kathir)" else "Tafsir Ibn Kathir • তাফসীর ইবনে কাসীর",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NoorMintLuminous
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = ayah.tafsirBn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Previous / Next Surah Footer Navigation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val prevSurah = allSurahs.getOrNull(currentSurahIndex - 1)
                    val nextSurah = allSurahs.getOrNull(currentSurahIndex + 1)

                    OutlinedButton(
                        onClick = { if (prevSurah != null) onSelectSurah(prevSurah) },
                        enabled = prevSurah != null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "পূর্ববর্তী সূরা" else "Previous Surah")
                    }

                    OutlinedButton(
                        onClick = { if (nextSurah != null) onSelectSurah(nextSurah) },
                        enabled = nextSurah != null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isBn) "পরবর্তী সূরা" else "Next Surah")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }

        // 3. Persistent Bottom Qari Audio Player Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                LinearProgressIndicator(
                    progress = { uiState.audioProgressFraction },
                    color = NoorEmeraldPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showQariModal = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NoorEmeraldPrimary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Audio Recitation",
                                tint = NoorMintLuminous
                            )
                        }
                        Column {
                            Text(
                                text = "Surah ${uiState.playingSurahNumber ?: surah.number} • Ayah ${uiState.playingAyahNumber ?: 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isBn) uiState.selectedQari.nameBn else uiState.selectedQari.nameEn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Repeat 3x Hifz Toggle
                        IconButton(
                            onClick = onToggleRepeat3x,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Repeat 3x",
                                tint = if (uiState.audioRepeat3x) NoorMintLuminous
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onStepAyahAudio(-1) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Ayah"
                            )
                        }

                        IconButton(
                            onClick = onToggleGlobalPlayPause,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(NoorEmeraldPrimary)
                                .testTag("quran_audio_play_pause_btn")
                        ) {
                            Icon(
                                imageVector = if (uiState.isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause Recitation",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { onStepAyahAudio(1) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Ayah"
                            )
                        }
                    }
                }
            }
        }
    }

    if (showQariModal) {
        AlertDialog(
            onDismissRequest = { showQariModal = false },
            title = {
                Text(
                    text = if (isBn) "ক্বারী নির্বাচন করুন (Select Qari)" else "Select Quran Reciter (Qari)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuranCatalog.qariList.forEach { qari ->
                        val selected = qari.id == uiState.selectedQari.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) NoorEmeraldPrimary.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selected) BorderStroke(1.dp, NoorEmeraldPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectQari(qari)
                                    showQariModal = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isBn) qari.nameBn else qari.nameEn,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = qari.style,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = NoorEmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQariModal = false }) {
                    Text(if (isBn) "বন্ধ করুন" else "Close")
                }
            }
        )
    }
}

@Composable
private fun ReaderActionPill(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (active) NoorEmeraldPrimary.copy(alpha = 0.22f)
        else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (active) NoorEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (active) NoorMintLuminous else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun DigitalTasbihScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onSelectPreset: (TasbihPreset) -> Unit,
    onIncrement: () -> Unit,
    onRequestReset: () -> Unit,
    onDismissReset: () -> Unit,
    onConfirmReset: () -> Unit,
    onNewCycle: () -> Unit,
    onToggleVibration: () -> Unit,
    onToggleSound: () -> Unit,
    onSpeakArabic: (String) -> Unit
) {
    BackHandler { onBack() }

    val isBn = uiState.language == "bn"
    val preset = uiState.selectedTasbihPreset
    val progress by animateFloatAsState(
        targetValue = (uiState.tasbihCurrentCount.toFloat() / uiState.tasbihTarget.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f),
        label = "tasbih_ring_progress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("digital_tasbih_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Header with Back + Vibration & Sound Toggles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("tasbih_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Column {
                        Text(
                            text = if (isBn) "ডিজিটাল তাসবীহ" else "Digital Tasbih",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "যিকির ও গণনা • Dhikr Counter" else "Dhikr Counter • যিকির ও গণনা",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onToggleVibration,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (uiState.tasbihVibrationEnabled) NoorEmeraldPrimary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("tasbih_vibration_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Vibration Toggle",
                            tint = if (uiState.tasbihVibrationEnabled) NoorMintLuminous
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (uiState.tasbihSoundEnabled) NoorEmeraldPrimary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("tasbih_sound_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.tasbihSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp
                            else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Sound Toggle",
                            tint = if (uiState.tasbihSoundEnabled) NoorMintLuminous
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Selected Dhikr Display Card (Matching digital_tasbih_dark/screen.png)
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = NoorEmeraldPrimary.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "Target: ${uiState.tasbihTarget} • Cycle ${uiState.tasbihCycle}/${uiState.tasbihTotalCycles}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { onSpeakArabic(preset.arabic) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen Pronunciation",
                                tint = NoorMintLuminous,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = preset.arabic,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = AmiriFontFamily,
                            fontSize = 36.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${preset.transliteration} • ${preset.bangla}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NoorMintLuminous
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBn) "অর্থ: “${preset.meaningBn}” (${preset.meaningEn})"
                        else "“${preset.meaningEn}” (${preset.meaningBn})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 3. Preset Dhikr Selector Strip
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RuhaniContentCatalog.tasbihPresets.forEach { itemPreset ->
                    val selected = itemPreset.id == preset.id
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .testTag("tasbih_preset_${itemPreset.id}")
                            .clickable { onSelectPreset(itemPreset) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isBn) itemPreset.bangla else itemPreset.transliteration,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = itemPreset.defaultTarget.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                color = if (selected) Color.White.copy(alpha = 0.85f) else NoorMintLuminous
                            )
                        }
                    }
                }
            }
        }

        // 4. Giant Circular Progress Ring & Tactile Tap Target
        item {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = NoorEmeraldPrimary,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Tactile Circle Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, NoorEmeraldPrimary.copy(alpha = 0.4f)),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .size(204.dp)
                        .clip(CircleShape)
                        .testTag("tasbih_tap_button")
                        .clickable { onIncrement() }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = uiState.tasbihCurrentCount.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 54.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " /${uiState.tasbihTarget}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = NoorEmeraldPrimary.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = NoorMintLuminous,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isBn) "ট্যাপ করুন" else "TAP TO COUNT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NoorMintLuminous
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Today's Total & All-Time Total Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isBn) "আজকের মোট (Today's Total)" else "Today's Total • আজকের মোট",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = uiState.tasbihTodayTotal.toString(),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "dhikr",
                                style = MaterialTheme.typography.labelSmall,
                                color = NoorMintLuminous,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isBn) "সর্বমোট (All-Time Total)" else "All-Time Total • সর্বমোট",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "%,d".format(uiState.tasbihAllTimeTotal),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Reset & New Cycle Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRequestReset,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tasbih_reset_button")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "রিসেট করুন" else "Reset")
                }

                Button(
                    onClick = onNewCycle,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("tasbih_new_cycle_button")
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBn) "নতুন চক্র (New Cycle)" else "New Cycle",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (uiState.showTasbihResetDialog) {
        AlertDialog(
            onDismissRequest = onDismissReset,
            title = {
                Text(
                    text = if (isBn) "তাসবীহ রিসেট করবেন?" else "Reset Current Tasbih?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBn) "আপনার বর্তমান গণনা (${uiState.tasbihCurrentCount}) শূন্য করা হবে। সর্বমোট গণনা সুরক্ষিত থাকবে।"
                    else "Your current cycle count (${uiState.tasbihCurrentCount}) will be reset to 0. Your all-time total will be preserved."
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmReset,
                    colors = ButtonDefaults.buttonColors(containerColor = NoorRedAlert)
                ) {
                    Text(if (isBn) "রিসেট নিশ্চিত করুন" else "Reset Now", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissReset) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
