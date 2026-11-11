package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalatLog
import com.example.data.remote.AlAdhanCalendarDay
import com.example.data.remote.AlAdhanCalendarService
import com.example.data.remote.AlAdhanMonthlyCalendar
import com.example.data.remote.AlAdhanQiblaResult
import com.example.data.remote.AlAdhanQiblaService
import com.example.data.remote.HijriCalendarMethod
import com.example.domain.*
import com.example.ui.RuhaniUiState
import com.example.ui.theme.AmiriFontFamily
import kotlinx.coroutines.launch
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NoorEmeraldPrimary
import com.example.ui.theme.NoorGoldAccent
import com.example.ui.theme.NoorMintLuminous
import com.example.ui.theme.NoorRedAlert
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RuhaniHalkaCardBrush
import com.example.ui.theme.RuhaniHalkaDarkBrush
import com.example.ui.theme.SignalRed
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlin.math.abs

@Composable
fun SubScreenHeader(
    titleBn: String,
    titleEn: String,
    subtitle: String,
    isBn: Boolean,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(EmeraldPrimary.copy(alpha = 0.16f))
                .border(1.dp, EmeraldPrimary.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .testTag("subscreen_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = EmeraldPrimary
            )
        }
        Column {
            Text(
                text = if (isBn) "$titleBn • $titleEn" else "$titleEn • $titleBn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SalatAndQazaTrackerScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onToggleSalat: (String, String) -> Unit,
    onAdjustQaza: (String, Long) -> Unit
) {
    BackHandler { onBack() }

    val isBn = uiState.language == "bn"
    val dateKey = uiState.schedule.dateKey
    val todayLog = uiState.salatLogsByDate[dateKey] ?: SalatLog(id = dateKey, dateKey = dateKey)

    val completedFarzCount = listOf(
        todayLog.fajr,
        todayLog.dhuhr,
        todayLog.asr,
        todayLog.maghrib,
        todayLog.isha
    ).count { it }

    val totalQaza =
        uiState.qazaFajr + uiState.qazaDhuhr + uiState.qazaAsr + uiState.qazaMaghrib + uiState.qazaIsha + uiState.qazaWitr

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("salat_tracker_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "সালাত ও কাজা ট্র্যাকার",
                titleEn = "Prayer & Qaza Tracker",
                subtitle = if (isBn) "দৈনিক নামাজের হিসাব ও কাজা নামাজ ব্যবস্থাপনা" else "Daily 5-Waqt log & Qaza backlog manager",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Daily Progress Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF153529), Color(0xFF0E1D17))))
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBn) "আজকের ফরজ নামাজ" else "Today's Farz Prayers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${uiState.schedule.gregorianDateBn} • ${uiState.schedule.hijriDateBn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA3B8B0)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NoorEmeraldPrimary.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, NoorMintLuminous.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "$completedFarzCount / 5",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NoorMintLuminous,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 5 Farz + Witr Checkboxes
        item {
            val itemsList = listOf(
                Triple("fajr", "ফজর (Fajr)", todayLog.fajr),
                Triple("dhuhr", "যোহর (Dhuhr)", todayLog.dhuhr),
                Triple("asr", "আসর (Asr)", todayLog.asr),
                Triple("maghrib", "মাগরিব (Maghrib)", todayLog.maghrib),
                Triple("isha", "এশা (Isha)", todayLog.isha),
                Triple("witr", "বিতর (Witr Wajib)", todayLog.witr)
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBn) "ফরজ ও ওয়াজিব সালাত" else "Daily Farz & Wajib Prayers",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    itemsList.forEach { (field, label, checked) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (checked) NoorEmeraldPrimary.copy(alpha = 0.14f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .testTag("salat_check_$field")
                                .clickable { onToggleSalat(dateKey, field) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = label,
                                tint = if (checked) NoorMintLuminous else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Nawafil & Fasting Checkboxes
        item {
            val nawafilItems = listOf(
                Triple("tahajjud", "তাহাজ্জুদ (Tahajjud)", todayLog.tahajjud),
                Triple("duha", "ইশরাক / চাশত (Duha)", todayLog.duha),
                Triple("awwabin", "আওয়াবীন (Awwabin)", todayLog.awwabin),
                Triple("fasting", "আজকের রোজা (Fasting Today)", todayLog.fasting)
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isBn) "নফল নামাজ ও রোজা (Sunnah & Fasting)" else "Voluntary Prayers & Fasting",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    nawafilItems.forEach { (field, label, checked) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (checked) NoorEmeraldPrimary.copy(alpha = 0.14f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { onToggleSalat(dateKey, field) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                            Icon(
                                imageVector = if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = label,
                                tint = if (checked) NoorMintLuminous else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Qaza Prayer Backlog Manager
        item {
            val qazaList = listOf(
                Triple("fajr", "ফজর কাজা (Fajr Qaza)", uiState.qazaFajr),
                Triple("dhuhr", "যোহর কাজা (Dhuhr Qaza)", uiState.qazaDhuhr),
                Triple("asr", "আসর কাজা (Asr Qaza)", uiState.qazaAsr),
                Triple("maghrib", "মাগরিব কাজা (Maghrib Qaza)", uiState.qazaMaghrib),
                Triple("isha", "এশা কাজা (Isha Qaza)", uiState.qazaIsha),
                Triple("witr", "বিতর কাজা (Witr Qaza)", uiState.qazaWitr)
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "বকেয়া কাজা নামাজের হিসাব" else "Missed (Qaza) Prayer Backlog",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (totalQaza > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NoorRedAlert
                            ) {
                                Text(
                                    text = "$totalQaza QAZA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    qazaList.forEach { (key, title, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = title, style = MaterialTheme.typography.bodyMedium)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { onAdjustQaza(key, -1L) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    onClick = { onAdjustQaza(key, 1L) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(NoorEmeraldPrimary.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase",
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
    }
}

@Composable
fun DuaAndDhikrScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onSpeakArabic: (String) -> Unit,
    onIncrementDuaCount: (String, Int) -> Unit = { _, _ -> },
    onResetDuaCount: (String) -> Unit = {},
    onToggleFavoriteDua: (String) -> Unit = {}
) {
    BackHandler { onBack() }

    val isBn = uiState.language == "bn"
    val allDuas = remember { RuhaniContentCatalog.duas }
    val categories = remember {
        listOf(
            "সকল দোয়া",
            "পছন্দের দোয়া ♥",
            "সকাল-সন্ধ্যা",
            "নামাজের পর",
            "দৈনন্দিন জীবন",
            "ঘুম ও জাগরণ",
            "সফর ও নিরাপত্তা",
            "রুকইয়াহ ও শিফা",
            "রমজান ও রোজা",
            "ক্ষমা ও পরিবার"
        )
    }
    val occasionQuickFilters = remember {
        listOf(
            "সকল উপলক্ষ" to "",
            "ফজর ও মাগরিব" to "ফজর",
            "সালাম ফেরানোর পর" to "সালাম",
            "ঘুমানোর পূর্বে" to "ঘুম",
            "ঘর থেকে বের হওয়া" to "ঘর",
            "সফর ও যানবাহন" to "যানবাহন",
            "অসুস্থতা ও বিপদ" to "অসুস্থতা",
            "ইফতার ও রমজান" to "ইফতার",
            "পিতা-মাতা" to "পিতা-মাতা"
        )
    }

    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedOccasionKeyword by remember { mutableStateOf("") }
    val localFallbackCounter = remember { mutableStateMapOf<String, Int>() }

    val filteredDuas = remember(selectedCategory, searchQuery, selectedOccasionKeyword, uiState.favoriteDuaIds) {
        allDuas.filter { dua ->
            val matchesCategory = when (selectedCategory) {
                "সকল দোয়া" -> true
                "পছন্দের দোয়া ♥" -> dua.id in uiState.favoriteDuaIds
                else -> dua.categoryBn == selectedCategory
            }
            val q = searchQuery.trim()
            val matchesSearch = if (q.isEmpty()) {
                true
            } else {
                dua.titleBn.contains(q, ignoreCase = true) ||
                    dua.titleEn.contains(q, ignoreCase = true) ||
                    dua.occasionBn.contains(q, ignoreCase = true) ||
                    dua.occasionEn.contains(q, ignoreCase = true) ||
                    dua.categoryBn.contains(q, ignoreCase = true) ||
                    dua.categoryEn.contains(q, ignoreCase = true) ||
                    dua.meaningBn.contains(q, ignoreCase = true) ||
                    dua.meaningEn.contains(q, ignoreCase = true) ||
                    dua.transliterationBn.contains(q, ignoreCase = true)
            }
            val matchesOccasionChip = if (selectedOccasionKeyword.isEmpty()) {
                true
            } else {
                dua.occasionBn.contains(selectedOccasionKeyword, ignoreCase = true) ||
                    dua.titleBn.contains(selectedOccasionKeyword, ignoreCase = true) ||
                    dua.categoryBn.contains(selectedOccasionKeyword, ignoreCase = true)
            }
            matchesCategory && matchesSearch && matchesOccasionChip
        }
    }

    val groupedDuas = remember(filteredDuas) {
        filteredDuas.groupBy { it.categoryBn to it.categoryEn }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dua_dhikr_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "মাসনূন দোয়া ও যিকির তালিকা",
                titleEn = "Categorized Islamic Duas",
                subtitle = if (isBn) "নাম অথবা উপলক্ষ (Occasion) অনুযায়ী দোয়া খুঁজুন ও আমল করুন" else "Browse & filter authentic Duas by name, category, or occasion",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Halka Gradient (#14a673) Hero Banner with #f02828 Accent Badge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(RuhaniHalkaDarkBrush)
                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldPrimary.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, EmeraldPrimary)
                        ) {
                            Text(
                                text = if (isBn) "হিসনুল মুসলিম • ${allDuas.size}টি মাসনূন দোয়া" else "Hisnul Muslim • ${allDuas.size} Authentic Duas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SignalRed
                        ) {
                            Text(
                                text = if (isBn) "♥ প্রিয়: ${uiState.favoriteDuaIds.size}" else "♥ Saved: ${uiState.favoriteDuaIds.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isBn) "দৈনন্দিন জীবন ও বিশেষ উপলক্ষের প্রামাণিক দোয়া সংকলন" else "Categorized Duas for Every Daily Occasion & Need",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PureWhite
                    )
                    Text(
                        text = if (isBn)
                            "সকাল-সন্ধ্যা, সালাত পরবর্তী, ঘুম, সফর, অসুস্থতা ও ইফতারসহ ৮টি ক্যাটাগরিতে সাজানো। নাম বা উপলক্ষ লিখে দ্রুত ফিল্টার করুন।"
                        else
                            "Organized into 8 categories including Morning/Evening, After Salat, Sleep, Travel, Ruqyah & Ramadan. Search by name or occasion below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Search Bar to quickly filter by Name or Occasion
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dua_search_bar"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Dua",
                        tint = EmeraldPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank() || selectedOccasionKeyword.isNotBlank()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                selectedOccasionKeyword = ""
                            },
                            modifier = Modifier.testTag("dua_search_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = SignalRed
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = if (isBn) "দোয়ার নাম বা উপলক্ষ লিখুন (যেমন: ঘুম, সফর, ইফতার, সকাল)..."
                        else "Search Dua by name or occasion (e.g. Sleep, Travel, Iftar, Morning)...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }

        // Occasion Quick Filter Pills
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isBn) "উপলক্ষ অনুযায়ী দ্রুত ফিল্টার (Filter by Occasion):" else "Quick Filter by Occasion:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    occasionQuickFilters.forEach { (labelBn, keyword) ->
                        val active = selectedOccasionKeyword == keyword
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (active) SignalRed else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (active) SignalRed else EmeraldPrimary.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedOccasionKeyword = if (active && keyword.isNotEmpty()) "" else keyword
                                }
                                .testTag("dua_occasion_chip_${keyword.ifEmpty { "all" }}")
                        ) {
                            Text(
                                text = labelBn,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (active) PureWhite else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Chips with Counts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "দোয়ার ক্যাটাগরি (Categories):" else "Dua Categories:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isBn) "প্রাপ্ত দোয়া: ${filteredDuas.size}টি" else "Showing: ${filteredDuas.size} Duas",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = cat == selectedCategory
                        val countForCat = when (cat) {
                            "সকল দোয়া" -> allDuas.size
                            "পছন্দের দোয়া ♥" -> uiState.favoriteDuaIds.size
                            else -> allDuas.count { it.categoryBn == cat }
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (selected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .testTag("dua_category_chip_${cat.hashCode()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) PureWhite else MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = if (selected) PureBlack.copy(alpha = 0.28f) else EmeraldPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = countForCat.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) PureWhite else EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filteredDuas.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isBn) "কোনো দোয়া খুঁজে পাওয়া যায়নি" else "No matching Duas found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SignalRed
                        )
                        Text(
                            text = if (isBn) "অনুগ্রহ করে অন্য কোনো নাম বা উপলক্ষ দিয়ে খুঁজুন অথবা ফিল্টার রিসেট করুন।"
                            else "Try searching with another name or occasion, or reset the active filters.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedOccasionKeyword = ""
                                selectedCategory = categories.first()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SignalRed,
                                contentColor = PureWhite
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (isBn) "সকল ফিল্টার রিসেট করুন" else "Reset All Filters",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            groupedDuas.forEach { (categoryPair, duasInCategory) ->
                val (catBn, catEn) = categoryPair
                item(key = "header_$catBn") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                            )
                            Text(
                                text = if (isBn) "$catBn • $catEn" else "$catEn • $catBn",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldPrimary.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${duasInCategory.size}টি দোয়া",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                items(duasInCategory, key = { it.id }) { dua ->
                    val currentCount = uiState.duaCountsById[dua.id] ?: (localFallbackCounter[dua.id] ?: 0)
                    val isFav = dua.id in uiState.favoriteDuaIds
                    val isCompleted = currentCount >= dua.repeatTarget

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isCompleted) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dua_card_${dua.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .background(RuhaniHalkaCardBrush)
                                .padding(16.dp)
                        ) {
                            // Top Row: Category + Occasion Badge + Favorite & Audio Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = EmeraldPrimary.copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = if (isBn) dua.categoryBn else dua.categoryEn,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = SignalRed.copy(alpha = 0.14f),
                                            border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.45f))
                                        ) {
                                            Text(
                                                text = if (isBn) "উপলক্ষ: ${dua.occasionBn}" else dua.occasionEn,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SignalRed,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (isBn) dua.titleBn else dua.titleEn,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = { onToggleFavoriteDua(dua.id) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isFav) SignalRed.copy(alpha = 0.18f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                            )
                                            .testTag("dua_fav_${dua.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite Dua",
                                            tint = if (isFav) SignalRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onSpeakArabic(dua.arabic) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(EmeraldPrimary.copy(alpha = 0.18f))
                                            .testTag("dua_speak_${dua.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Listen Dua",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Arabic Supplication Box with Halka Emerald Border
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(EmeraldPrimary.copy(alpha = 0.08f))
                                    .border(1.dp, EmeraldPrimary.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = dua.arabic,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = AmiriFontFamily,
                                        fontSize = 26.sp,
                                        lineHeight = 44.sp,
                                        textDirection = TextDirection.Rtl
                                    ),
                                    textAlign = TextAlign.Right,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "উচ্চারণ: ${dua.transliterationBn}",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Medium,
                                color = EmeraldPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "অনুবাদ: ${dua.translationBn}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dua.translationEn,
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
                                    text = "দলিল: ${dua.referenceBn}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (currentCount > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = SignalRed.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, SignalRed.copy(alpha = 0.5f)),
                                            modifier = Modifier
                                                .clickable {
                                                    localFallbackCounter[dua.id] = 0
                                                    onResetDuaCount(dua.id)
                                                }
                                                .testTag("dua_reset_${dua.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Reset Dua Count",
                                                    tint = SignalRed,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = if (isBn) "রিসেট" else "Reset",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SignalRed
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (isCompleted) EmeraldPrimary else EmeraldPrimary.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, EmeraldPrimary),
                                        modifier = Modifier
                                            .clickable {
                                                val next = if (currentCount >= dua.repeatTarget) 1 else currentCount + 1
                                                localFallbackCounter[dua.id] = next
                                                onIncrementDuaCount(dua.id, dua.repeatTarget)
                                            }
                                            .testTag("dua_count_btn_${dua.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.TouchApp,
                                                contentDescription = null,
                                                tint = if (isCompleted) PureWhite else EmeraldPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "$currentCount / ${dua.repeatTarget} বার",
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCompleted) PureWhite else EmeraldPrimary
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
    }
}

@Composable
fun QiblaCompassScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = uiState.language == "bn"

    var activeLat by remember(uiState.selectedDistrict.id) {
        mutableDoubleStateOf(uiState.selectedDistrict.latitude)
    }
    var activeLng by remember(uiState.selectedDistrict.id) {
        mutableDoubleStateOf(uiState.selectedDistrict.longitude)
    }
    var latInput by remember(uiState.selectedDistrict.id) {
        mutableStateOf(uiState.selectedDistrict.latitude.toString())
    }
    var lngInput by remember(uiState.selectedDistrict.id) {
        mutableStateOf(uiState.selectedDistrict.longitude.toString())
    }
    var selectedSizePx by remember { mutableIntStateOf(600) }

    var apiResult by remember { mutableStateOf<AlAdhanQiblaResult?>(null) }
    var isLoadingApi by remember { mutableStateOf(false) }
    var apiError by remember { mutableStateOf<String?>(null) }

    var showDetailsDialog by remember { mutableStateOf(false) }
    var showDirectionSheet by remember { mutableStateOf(false) }

    fun loadFromAlAdhan(force: Boolean = false) {
        scope.launch {
            isLoadingApi = true
            apiError = null
            val res = AlAdhanQiblaService.fetchAlAdhanQibla(
                latitude = activeLat,
                longitude = activeLng,
                size = selectedSizePx,
                forceRefresh = force
            )
            isLoadingApi = false
            res.onSuccess { data ->
                apiResult = data
            }.onFailure { err ->
                apiError = err.localizedMessage ?: "AlAdhan API connection unavailable"
            }
        }
    }

    LaunchedEffect(activeLat, activeLng, selectedSizePx) {
        loadFromAlAdhan(force = false)
    }

    // Qibla bearing from AlAdhan API (e.g., ~277.51° W from Dhaka)
    val targetBearing = (apiResult?.directionDegrees ?: uiState.selectedDistrict.qiblaBearingDeg).toFloat()

    // Default heading starts near West (268°) so the dial shows W at top and N on the right just like the reference UI
    var sensorAzimuth by remember { mutableFloatStateOf(268f) }
    var hasHardwareCompass by remember { mutableStateOf(false) }

    // Register real Android rotation/orientation sensor listener
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuthDeg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                    sensorAzimuth = azimuthDeg
                    hasHardwareCompass = true
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val animatedAzimuth by animateFloatAsState(targetValue = sensorAzimuth, label = "qibla_azimuth")
    // Relative angle from device heading to Qibla in [-180, +180]
    val relativeQiblaAngle = ((targetBearing - animatedAzimuth + 540f) % 360f) - 180f
    val animatedArrowAngle by animateFloatAsState(targetValue = relativeQiblaAngle, label = "qibla_arrow_angle")
    val isAligned = abs(relativeQiblaAngle) <= 5f

    // Calculate distance in km from activeLat, activeLng to Kaaba (matching 5,166 km for Dhaka)
    val distanceKm = remember(activeLat, activeLng) {
        val kaabaLat = Math.toRadians(21.422487)
        val kaabaLng = Math.toRadians(39.826206)
        val latRad = Math.toRadians(activeLat)
        val lngRad = Math.toRadians(activeLng)
        val dLng = kaabaLng - lngRad
        val cosAngle = (kotlin.math.sin(latRad) * kotlin.math.sin(kaabaLat) +
            kotlin.math.cos(latRad) * kotlin.math.cos(kaabaLat) * kotlin.math.cos(dLng)).coerceIn(-1.0, 1.0)
        (kotlin.math.acos(cosAngle) * 6365.0).toInt()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isAligned) {
                    Brush.verticalGradient(
                        listOf(Color(0xFF061F15), Color(0xFF020806), Color(0xFF000000))
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color(0xFF050606), Color(0xFF000000))
                    )
                }
            )
            .testTag("qibla_compass_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Header: FINDING QIBLA / Kaaba · Makkah + Subtle Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "FINDING QIBLA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = SpaceGroteskFontFamily,
                            letterSpacing = 2.0.sp,
                            fontSize = 12.sp
                        ),
                        color = Color(0xFF9AA0A6)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kaaba · Makkah",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1D1F))
                        .testTag("subscreen_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Back",
                        tint = Color(0xFFD0D5D4),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 2. Center Section: Dark Radar Compass Dial + Giant Dynamic Precision Arrow
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Dark Radar Compass Dial (matches uploaded screenshot)
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clickable { showDirectionSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2f
                        // Dark circular dial face
                        drawCircle(
                            color = Color(0xFF121415),
                            radius = radius
                        )
                        // Outer subtle bezel border
                        drawCircle(
                            color = if (isAligned) NoorEmeraldPrimary.copy(alpha = 0.8f) else Color(0xFF2C3032),
                            radius = radius,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        // Subtle inner concentric radar circle
                        drawCircle(
                            color = Color(0xFF1D2022),
                            radius = radius * 0.54f,
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Top Searchlight / Field-of-View Cone (-116° to -64°, centered at -90° top)
                        val coneBrush = Brush.radialGradient(
                            colors = if (isAligned) {
                                listOf(
                                    NoorMintLuminous.copy(alpha = 0.55f),
                                    NoorEmeraldPrimary.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.36f),
                                    Color.White.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            },
                            center = center,
                            radius = radius * 0.96f
                        )
                        drawArc(
                            brush = coneBrush,
                            startAngle = -115f,
                            sweepAngle = 50f,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2f, radius * 2f)
                        )

                        // Rotating Compass Ring (rotates by -animatedAzimuth so current heading is at top)
                        rotate(-animatedAzimuth) {
                            for (deg in 0 until 360 step 6) {
                                rotate(deg.toFloat()) {
                                    val isCardinal = deg % 90 == 0
                                    val isThirty = deg % 30 == 0
                                    val isNorth = deg == 0

                                    val tickLength = when {
                                        isCardinal -> 14.dp.toPx()
                                        isThirty -> 10.dp.toPx()
                                        else -> 5.dp.toPx()
                                    }
                                    val strokeW = when {
                                        isCardinal -> 2.2.dp.toPx()
                                        isThirty -> 1.5.dp.toPx()
                                        else -> 1.dp.toPx()
                                    }
                                    val tickColor = when {
                                        isNorth -> Color(0xFFF02828)
                                        isCardinal -> Color.White
                                        isThirty -> Color(0xFFB0B5B8)
                                        else -> Color(0xFF505558)
                                    }

                                    drawLine(
                                        color = tickColor,
                                        start = Offset(center.x, center.y - radius + 4.dp.toPx()),
                                        end = Offset(center.x, center.y - radius + 4.dp.toPx() + tickLength),
                                        strokeWidth = strokeW,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            // Draw Cardinal Letters (N in Red, E, S, W in White) upright along the dial
                            val textPaint = android.graphics.Paint().apply {
                                isAntiAlias = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = 13.sp.toPx()
                                typeface = android.graphics.Typeface.create(
                                    android.graphics.Typeface.DEFAULT,
                                    android.graphics.Typeface.BOLD
                                )
                            }
                            val cardinals = listOf(
                                Triple("N", 0.0, android.graphics.Color.parseColor("#F02828")),
                                Triple("E", 90.0, android.graphics.Color.parseColor("#D8DCDD")),
                                Triple("S", 180.0, android.graphics.Color.parseColor("#D8DCDD")),
                                Triple("W", 270.0, android.graphics.Color.parseColor("#FFFFFF"))
                            )
                            val labelRadius = radius - 26.dp.toPx()
                            cardinals.forEach { (letter, angleDeg, colorInt) ->
                                val rad = Math.toRadians(angleDeg - 90.0)
                                val lx = center.x + (labelRadius * kotlin.math.cos(rad)).toFloat()
                                val ly = center.y + (labelRadius * kotlin.math.sin(rad)).toFloat()
                                drawContext.canvas.nativeCanvas.save()
                                drawContext.canvas.nativeCanvas.translate(lx, ly)
                                // Counter-rotate text so N, E, S, W always remain upright and readable
                                drawContext.canvas.nativeCanvas.rotate(animatedAzimuth)
                                textPaint.color = colorInt
                                val fontMetrics = textPaint.fontMetrics
                                val baselineOffset = -(fontMetrics.ascent + fontMetrics.descent) / 2f
                                drawContext.canvas.nativeCanvas.drawText(letter, 0f, baselineOffset, textPaint)
                                drawContext.canvas.nativeCanvas.restore()
                            }

                            // Subtle Emerald Qibla target dot on the outer bezel at targetBearing
                            rotate(targetBearing) {
                                drawCircle(
                                    color = NoorMintLuminous,
                                    radius = 4.dp.toPx(),
                                    center = Offset(center.x, center.y - radius + 4.dp.toPx())
                                )
                            }
                        }

                        // Bright white center pivot dot
                        drawCircle(
                            color = Color.White,
                            radius = 5.5.dp.toPx(),
                            center = center
                        )
                    }
                }

                // Giant Precision Arrow working as Live Qibla Compass
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .testTag("qibla_direction_arrow")
                        .clickable {
                            // Tapping the arrow smoothly aligns or opens the direction calibrator
                            sensorAzimuth = targetBearing
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationZ = animatedArrowAngle
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val arrowColor = if (isAligned) NoorMintLuminous else Color.White
                        val strokeWidthPx = 16.dp.toPx()

                        // Vertical shaft of the arrow (with slight natural tilt matching screenshot)
                        drawLine(
                            color = arrowColor,
                            start = Offset(w * 0.48f, h * 0.86f),
                            end = Offset(w * 0.51f, h * 0.16f),
                            strokeWidth = strokeWidthPx,
                            cap = StrokeCap.Round
                        )

                        // Rounded Chevron Head
                        val headPath = Path().apply {
                            moveTo(w * 0.20f, h * 0.44f)
                            lineTo(w * 0.51f, h * 0.14f)
                            lineTo(w * 0.80f, h * 0.46f)
                        }
                        drawPath(
                            path = headPath,
                            color = arrowColor,
                            style = Stroke(
                                width = strokeWidthPx,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // 3. Bottom Section: Distance Readout + "toward Makkah · turn to align" + Details & Direction Pill Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "%,d km".format(distanceKm),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isAligned) {
                        if (isBn) "toward Makkah · কিবলামুখী (Aligned)" else "toward Makkah · aligned with Qibla"
                    } else {
                        "toward Makkah · turn to align"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = if (isAligned) NoorMintLuminous else Color(0xFF9AA0A6)
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Bottom Dual Pill Buttons: [ ⓘ Details ]  [ ▲ Direction ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF202224),
                        border = BorderStroke(1.dp, Color(0xFF323639)),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("qibla_details_button")
                            .clickable { showDetailsDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Details",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isAligned) NoorEmeraldPrimary.copy(alpha = 0.28f) else Color(0xFF202224),
                        border = BorderStroke(
                            1.dp,
                            if (isAligned) NoorEmeraldPrimary else Color(0xFF323639)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("qibla_direction_button")
                            .clickable { showDirectionSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Direction",
                                tint = if (isAligned) NoorMintLuminous else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Direction",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAligned) NoorMintLuminous else Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // "Details" Modal: AlAdhan Qibla API Telemetry, Official Compass PNG & Custom Coordinates
    if (showDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = {
                Text(
                    text = "Qibla Details • AlAdhan API",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val bitmap = apiResult?.compassBitmap
                    if (bitmap != null) {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, NoorEmeraldPrimary, CircleShape)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "AlAdhan Qibla Compass",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else if (isLoadingApi) {
                        CircularProgressIndicator(color = NoorMintLuminous, modifier = Modifier.size(32.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Location: ${uiState.selectedDistrict.nameBn} (${uiState.selectedDistrict.nameEn})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Coordinates: ${"%.4f".format(activeLat)}° N, ${"%.4f".format(activeLng)}° E",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily)
                            )
                            Text(
                                text = "Qibla Bearing: ${"%.2f".format(targetBearing)}° Clockwise from North",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                color = NoorMintLuminous
                            )
                            Text(
                                text = "Distance to Kaaba: %,d km".format(distanceKm),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily)
                            )
                            Text(
                                text = "API: GET /v1/qibla/${"%.4f".format(activeLat)}/${"%.4f".format(activeLng)}/compass/$selectedSizePx",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latInput,
                            onValueChange = { latInput = it },
                            label = { Text("Lat") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lngInput,
                            onValueChange = { lngInput = it },
                            label = { Text("Lng") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val parsedLat = latInput.toDoubleOrNull()
                            val parsedLng = lngInput.toDoubleOrNull()
                            if (parsedLat != null && parsedLng != null) {
                                activeLat = parsedLat.coerceIn(-90.0, 90.0)
                                activeLng = parsedLng.coerceIn(-180.0, 180.0)
                                loadFromAlAdhan(force = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Update via AlAdhan API", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // "Direction" Modal: Interactive Heading Calibrator & Auto-Align to Qibla
    if (showDirectionSheet) {
        AlertDialog(
            onDismissRequest = { showDirectionSheet = false },
            title = {
                Text(
                    text = "Compass Direction & Alignment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Current Heading: ${sensorAzimuth.toInt()}° • Qibla Bearing: ${"%.1f".format(targetBearing)}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = NoorMintLuminous
                    )
                    Text(
                        text = if (hasHardwareCompass) {
                            "Hardware orientation sensor is active. You can also test rotating the heading below:"
                        } else {
                            "Rotate the heading slider below to watch the radar compass and arrow align with Makkah:"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = sensorAzimuth,
                        onValueChange = { sensorAzimuth = it },
                        valueRange = 0f..360f
                    )
                    Button(
                        onClick = {
                            sensorAzimuth = targetBearing
                            showDirectionSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Align Arrow Straight to Kaaba (${targetBearing.toInt()}°)", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDirectionSheet = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun TriCalendarScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onUpdateHijriOffset: (Int) -> Unit
) {
    BackHandler { onBack() }

    val isBn = uiState.language == "bn"
    val schedule = uiState.schedule
    val coroutineScope = rememberCoroutineScope()

    val today = remember { java.time.LocalDate.now() }
    var activeMonth by remember { mutableIntStateOf(today.monthValue) }
    var activeYear by remember { mutableIntStateOf(today.year) }
    var selectedMethod by remember { mutableStateOf(HijriCalendarMethod.HJCOSA) }
    var monthlyCalendar by remember { mutableStateOf<AlAdhanMonthlyCalendar?>(null) }
    var selectedDay by remember { mutableStateOf<AlAdhanCalendarDay?>(null) }
    var isLoadingCalendar by remember { mutableStateOf(false) }

    // Date Converter state (GET https://api.aladhan.com/v1/gToH/{DD-MM-YYYY})
    var converterInputDate by remember {
        mutableStateOf("%02d-%02d-%04d".format(today.dayOfMonth, today.monthValue, today.year))
    }
    var convertedDayResult by remember { mutableStateOf<AlAdhanCalendarDay?>(null) }
    var isConvertingDate by remember { mutableStateOf(false) }

    fun loadMonthCalendar() {
        coroutineScope.launch {
            isLoadingCalendar = true
            val result = AlAdhanCalendarService.fetchMonthlyHijriCalendar(
                month = activeMonth,
                year = activeYear,
                method = selectedMethod,
                adjustment = uiState.hijriOffsetDays
            )
            result.onSuccess { cal ->
                monthlyCalendar = cal
                val defaultMatch = cal.days.firstOrNull {
                    it.gregorianDay == today.dayOfMonth &&
                        it.gregorianMonthNumber == today.monthValue &&
                        it.gregorianYear == today.year
                } ?: cal.days.firstOrNull()
                selectedDay = defaultMatch
            }
            isLoadingCalendar = false
        }
    }

    LaunchedEffect(activeMonth, activeYear, selectedMethod, uiState.hijriOffsetDays) {
        loadMonthCalendar()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tri_calendar_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "ইসলামিক হিজরি, বাংলা ও ইংরেজি ক্যালেন্ডার",
                titleEn = "AlAdhan Islamic Hijri & Tri-Calendar",
                subtitle = if (isBn) {
                    "AlAdhan Islamic Calendar API • মাসিক হিজরি গ্রিড, তারিখ রূপান্তর ও ইসলামিক দিবস"
                } else {
                    "Powered by AlAdhan Hijri Calendar API (gToHCalendar & gToH)"
                },
                isBn = isBn,
                onBack = onBack
            )
        }

        // Selected Day Tri-Date Hero Card
        item {
            val activeDay = selectedDay
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF153529), Color(0xFF0E1D17))))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NoorEmeraldPrimary.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (monthlyCalendar?.isFromLiveApi == true) {
                                    "LIVE • AlAdhan Hijri API (${selectedMethod.apiCode})"
                                } else {
                                    "AlAdhan Hijri Calendar (${selectedMethod.apiCode})"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                color = NoorMintLuminous,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (activeDay?.isWhiteDayFasting == true) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NoorGoldAccent.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = if (isBn) "আইয়ামে বীজ রোজা" else "White Day Fast",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NoorGoldAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (activeDay != null) {
                            "${activeDay.hijriDay} ${activeDay.hijriMonthBn}, ${activeDay.hijriYear} হিজরি (${activeDay.hijriMonthAr})"
                        } else {
                            schedule.hijriDateBn
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = NoorMintLuminous
                    )
                    Text(
                        text = if (activeDay != null) {
                            "Hijri: ${activeDay.hijriDay} ${activeDay.hijriMonthEn} ${activeDay.hijriYear} AH • ${activeDay.hijriWeekdayEn} (${activeDay.hijriWeekdayAr})"
                        } else {
                            schedule.hijriDateText
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFD6E6DF)
                    )
                    Text(
                        text = if (activeDay != null) {
                            "বাংলা বঙ্গাব্দ: ${activeDay.banglaDateFormatted}"
                        } else {
                            "বাংলা বঙ্গাব্দ: ${schedule.banglaDateBn} (${schedule.banglaDateText})"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = if (activeDay != null) {
                            "ইংরেজি: ${activeDay.gregorianDate} (${activeDay.gregorianWeekdayBn} • ${activeDay.gregorianDay} ${activeDay.gregorianMonthEn} ${activeDay.gregorianYear})"
                        } else {
                            "ইংরেজি: ${schedule.gregorianDateText}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA3B8B0)
                    )

                    if (!activeDay?.holidays.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NoorGoldAccent.copy(alpha = 0.16f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ইসলামিক দিবস: ${activeDay?.holidays?.joinToString(" • ")}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = NoorGoldAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // AlAdhan Calendar Calculation Method Selector (HJCoSA, UAQ, DIYANET, MATHEMATICAL)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "হিজরি গণনা পদ্ধতি (AlAdhan Method)" else "AlAdhan Hijri Calculation Method",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { loadMonthCalendar() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("aladhan_calendar_refresh_btn")
                        ) {
                            if (isLoadingCalendar) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = NoorMintLuminous
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Hijri Calendar",
                                    tint = NoorMintLuminous,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HijriCalendarMethod.entries.forEach { method ->
                            val selected = selectedMethod == method
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .testTag("hijri_method_${method.apiCode}")
                                    .clickable { selectedMethod = method }
                            ) {
                                Text(
                                    text = if (isBn) method.titleBn else method.titleEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "API: ${monthlyCalendar?.endpointUrl ?: AlAdhanCalendarService.buildMonthlyEndpointUrl(activeMonth, activeYear, selectedMethod, uiState.hijriOffsetDays)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Interactive Monthly Hijri + Gregorian Calendar Grid
        item {
            val cal = monthlyCalendar
            val hijriMonthsSpan = cal?.days
                ?.map { "${it.hijriMonthBn} ${it.hijriYear}" }
                ?.distinct()
                ?.joinToString(" – ")
                ?: ""

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("aladhan_monthly_calendar_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Month / Year Navigation Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (activeMonth == 1) {
                                    activeMonth = 12
                                    activeYear -= 1
                                } else {
                                    activeMonth -= 1
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("calendar_prev_month_btn")
                        ) {
                            Text(if (isBn) "◀ পূর্ববর্তী" else "◀ Prev", style = MaterialTheme.typography.labelSmall)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val monthNameEn = java.time.Month.of(activeMonth).name.lowercase()
                                .replaceFirstChar { it.uppercase() }
                            Text(
                                text = "$monthNameEn $activeYear",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = SpaceGroteskFontFamily),
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                            if (hijriMonthsSpan.isNotBlank()) {
                                Text(
                                    text = hijriMonthsSpan,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (activeMonth == 12) {
                                    activeMonth = 1
                                    activeYear += 1
                                } else {
                                    activeMonth += 1
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("calendar_next_month_btn")
                        ) {
                            Text(if (isBn) "পরবর্তী ▶" else "Next ▶", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Weekday Headers (Sun .. Sat)
                    val weekdays = if (isBn) {
                        listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
                    } else {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekdays.forEachIndexed { idx, label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (idx == 5) NoorGoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val days = cal?.days ?: emptyList()
                    if (days.isNotEmpty()) {
                        val firstDayDate = try {
                            java.time.LocalDate.of(activeYear, activeMonth, 1)
                        } catch (_: Exception) {
                            today
                        }
                        // Sunday = 0 .. Saturday = 6
                        val leadingEmptyCells = firstDayDate.dayOfWeek.value % 7
                        val totalCells = leadingEmptyCells + days.size
                        val rows = (totalCells + 6) / 7

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (col in 0 until 7) {
                                        val cellIndex = row * 7 + col
                                        val dayIndex = cellIndex - leadingEmptyCells
                                        val dayItem = days.getOrNull(dayIndex)
                                        if (dayItem == null) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(50.dp)
                                            )
                                        } else {
                                            val isSelected = selectedDay?.gregorianDate == dayItem.gregorianDate
                                            val isTodayCell = dayItem.gregorianDay == today.dayOfMonth &&
                                                dayItem.gregorianMonthNumber == today.monthValue &&
                                                dayItem.gregorianYear == today.year
                                            val hasHoliday = dayItem.holidays.isNotEmpty()

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = when {
                                                    isSelected -> NoorEmeraldPrimary
                                                    isTodayCell -> NoorEmeraldPrimary.copy(alpha = 0.25f)
                                                    hasHoliday -> NoorGoldAccent.copy(alpha = 0.14f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                                },
                                                border = if (isTodayCell && !isSelected) {
                                                    BorderStroke(1.dp, NoorMintLuminous)
                                                } else null,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(50.dp)
                                                    .clickable { selectedDay = dayItem }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = dayItem.gregorianDay.toString(),
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontFamily = SpaceGroteskFontFamily,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Text(
                                                            text = "${dayItem.hijriDay}",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            ),
                                                            color = when {
                                                                isSelected -> NoorGoldAccent
                                                                hasHoliday -> NoorGoldAccent
                                                                else -> NoorMintLuminous
                                                            }
                                                        )
                                                        if (hasHoliday || dayItem.isWhiteDayFasting) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(5.dp)
                                                                    .clip(CircleShape)
                                                                    .background(
                                                                        if (hasHoliday) NoorGoldAccent else NoorMintLuminous
                                                                    )
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
                    }
                }
            }
        }

        // Moon Sighting Adjustment (-2 to +2 days)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "হিজরি তারিখ সমন্বয় (চাঁদ দেখা অনুযায়ী)" else "Hijri Moon-Sighting Offset",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (-2..2).forEach { offset ->
                            val selected = uiState.hijriOffsetDays == offset
                            val label = if (offset > 0) "+$offset দিন" else "$offset দিন"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateHijriOffset(offset) }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // AlAdhan Gregorian-to-Hijri Date Converter (GET /v1/gToH/{date})
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "ইংরেজি থেকে হিজরি ও বাংলা তারিখ রূপান্তর (gToH API)" else "Gregorian to Hijri Date Converter (gToH API)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = converterInputDate,
                            onValueChange = { converterInputDate = it },
                            label = { Text(if (isBn) "তারিখ (DD-MM-YYYY)" else "Date (DD-MM-YYYY)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gtoh_date_input")
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isConvertingDate = true
                                    val res = AlAdhanCalendarService.convertGregorianToHijri(
                                        dateDdMmYyyy = converterInputDate.trim(),
                                        method = selectedMethod,
                                        adjustment = uiState.hijriOffsetDays
                                    )
                                    convertedDayResult = res.getOrNull()
                                    isConvertingDate = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NoorEmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("gtoh_convert_btn")
                        ) {
                            Text(if (isConvertingDate) "..." else if (isBn) "রূপান্তর" else "Convert")
                        }
                    }

                    convertedDayResult?.let { conv ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NoorEmeraldPrimary.copy(alpha = 0.14f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "হিজরি: ${conv.hijriDay} ${conv.hijriMonthBn} ${conv.hijriYear} হিজরি (${conv.hijriDate})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NoorMintLuminous
                                )
                                Text(
                                    text = "বাংলা: ${conv.banglaDateFormatted} • বার: ${conv.gregorianWeekdayBn}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Important Islamic Events List
        item {
            Text(
                text = if (isBn) "গুরুত্বপূর্ণ ইসলামিক দিবস ও রজনী" else "Important Islamic Nights & Holidays",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(RuhaniContentCatalog.islamicEvents) { event ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) event.titleBn else event.titleEn,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NoorMintLuminous
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NoorEmeraldPrimary.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = event.hijriDateBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = NoorMintLuminous,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.significanceBn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ZakatCalculatorScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onSetSilverNisab: (Boolean) -> Unit,
    onUpdateInputs: (String, String, String, String) -> Unit
) {
    BackHandler { onBack() }

    val isBn = uiState.language == "bn"
    val cash = uiState.zakatCashBdt.toDoubleOrNull() ?: 0.0
    val gold = uiState.zakatGoldValueBdt.toDoubleOrNull() ?: 0.0
    val business = uiState.zakatBusinessBdt.toDoubleOrNull() ?: 0.0
    val debts = uiState.zakatDebtsBdt.toDoubleOrNull() ?: 0.0

    // 7.5 Vori Gold vs 52.5 Vori Silver Nisab in BDT
    val nisabThresholdBdt = if (uiState.zakatUseSilverNisab) {
        52.5 * uiState.zakatSilverRatePerVori // ~110,250 BDT
    } else {
        7.5 * uiState.zakatGoldRatePerVori // ~1,035,000 BDT
    }

    val netZakatableAssets = (cash + gold + business - debts).coerceAtLeast(0.0)
    val isEligible = netZakatableAssets >= nisabThresholdBdt
    val payableZakatBdt = if (isEligible) netZakatableAssets * 0.025 else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("zakat_calculator_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "যাকাত ক্যালকুলেটর (৳ BDT)",
                titleEn = "Zakat Calculator",
                subtitle = if (isBn) "বাজুস (BAJUS) স্বর্ণ ও রৌপ্য নিসাব অনুযায়ী ২.৫% যাকাত হিসাব" else "Calculate 2.5% Zakat using Gold (7.5 Vori) or Silver (52.5 Vori) Nisab",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Payable Zakat Summary Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF153529), Color(0xFF0E1D17))))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "প্রদেয় যাকাত (২.৫%)" else "Total Payable Zakat (2.5%)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFA3B8B0)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isEligible) NoorEmeraldPrimary else NoorRedAlert
                        ) {
                            Text(
                                text = if (isEligible) {
                                    if (isBn) "যাকাত ফরজ" else "NISAB MET"
                                } else {
                                    if (isBn) "নিসাবের নিচে" else "BELOW NISAB"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "৳ %,.0f".format(payableZakatBdt),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = NoorMintLuminous
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "নিট যাকাতযোগ্য সম্পদ: ৳ %,.0f • নিসাব সীমা: ৳ %,.0f".format(netZakatableAssets, nisabThresholdBdt),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA3B8B0)
                    )
                }
            }
        }

        // Nisab Standard Selector (Silver 52.5 Vori vs Gold 7.5 Vori)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (uiState.zakatUseSilverNisab) NoorEmeraldPrimary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (uiState.zakatUseSilverNisab) NoorEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSetSilverNisab(true) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isBn) "রৌপ্য নিসাব (৫২.৫ ভরি)" else "Silver Nisab (52.5 Vori)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.zakatUseSilverNisab) NoorMintLuminous else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "৳ %,.0f (Recommended)".format(52.5 * uiState.zakatSilverRatePerVori),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (!uiState.zakatUseSilverNisab) NoorEmeraldPrimary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (!uiState.zakatUseSilverNisab) NoorEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSetSilverNisab(false) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isBn) "স্বর্ণ নিসাব (৭.৫ ভরি)" else "Gold Nisab (7.5 Vori)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (!uiState.zakatUseSilverNisab) NoorMintLuminous else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "৳ %,.0f".format(7.5 * uiState.zakatGoldRatePerVori),
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Asset Input Fields
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.zakatCashBdt,
                        onValueChange = {
                            onUpdateInputs(it, uiState.zakatGoldValueBdt, uiState.zakatBusinessBdt, uiState.zakatDebtsBdt)
                        },
                        label = { Text(if (isBn) "নগদ অর্থ, ব্যাংক ও সঞ্চয় (৳ BDT)" else "Cash, Bank & Savings (৳ BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.zakatGoldValueBdt,
                        onValueChange = {
                            onUpdateInputs(uiState.zakatCashBdt, it, uiState.zakatBusinessBdt, uiState.zakatDebtsBdt)
                        },
                        label = { Text(if (isBn) "স্বর্ণ ও রৌপ্যের বাজারমূল্য (৳ BDT)" else "Gold & Silver Market Value (৳ BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.zakatBusinessBdt,
                        onValueChange = {
                            onUpdateInputs(uiState.zakatCashBdt, uiState.zakatGoldValueBdt, it, uiState.zakatDebtsBdt)
                        },
                        label = { Text(if (isBn) "ব্যবসায়িক পণ্য ও বিনিয়োগ (৳ BDT)" else "Business Inventory & Shares (৳ BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = uiState.zakatDebtsBdt,
                        onValueChange = {
                            onUpdateInputs(uiState.zakatCashBdt, uiState.zakatGoldValueBdt, uiState.zakatBusinessBdt, it)
                        },
                        label = { Text(if (isBn) "বাদ: তাৎক্ষণিক ঋণ ও দেনা (৳ BDT)" else "Less: Immediate Debts & Liabilities (৳ BDT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun RamadanHubScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit,
    onSpeakArabic: (String) -> Unit
) {
    BackHandler { onBack() }

    val isBn = uiState.language == "bn"
    val schedule = uiState.schedule

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ramadan_hub_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "রমজান ক্যালেন্ডার ও আমল",
                titleEn = "Ramadan Companion",
                subtitle = "${uiState.selectedDistrict.nameBn} জেলার সেহরি, ইফতার ও তারাবিহ গাইড",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Today's Sehri & Iftar Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF153529), Color(0xFF0E1D17))))
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isBn) "আজকের সেহরির শেষ সময়" else "Sehri Ends Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA3B8B0)
                        )
                        Text(
                            text = schedule.sehriEndFormatted,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isBn) "আজকের ইফতারের সময়" else "Iftar Time Today",
                            style = MaterialTheme.typography.labelSmall,
                            color = NoorMintLuminous
                        )
                        Text(
                            text = schedule.iftarFormatted,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NoorMintLuminous
                        )
                    }
                }
            }
        }

        // Roza Niyat & Iftar Dua Card
        item {
            val iftarDua = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ"
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
                            text = if (isBn) "ইফতারের সুন্নাহ দোয়া" else "Sunnah Dua for Breaking Fast (Iftar)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NoorMintLuminous
                        )
                        IconButton(onClick = { onSpeakArabic(iftarDua) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = NoorMintLuminous)
                        }
                    }
                    Text(
                        text = iftarDua,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = AmiriFontFamily,
                            textDirection = TextDirection.Rtl
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "উচ্চারণ: যাহাবায যামাউ ওয়াবতাল্লাতিল উরূকু ওয়া সাবাতাল আজরু ইনশাআল্লাহ।",
                        style = MaterialTheme.typography.bodySmall,
                        color = NoorMintLuminous
                    )
                    Text(
                        text = "অর্থ: পিপাসা দূর হলো, শিরা-উপশিরা সিক্ত হলো এবং আল্লাহ চান তো সওয়াব নির্ধারিত হলো। (আবু দাউদ ২৩৫৭)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3 Ashras of Ramadan Overview
        item {
            val ashraList = listOf(
                Triple("১ম দশক: রহমত (1–10 Ramadan)", "রাব্বিগফির ওয়ারহাম ওয়া আনতা খাইরুর রাহিমীন", "হে আমার রব! আমাকে ক্ষমা করুন ও দয়া করুন।"),
                Triple("২য় দশক: মাগফিরাত (11–20 Ramadan)", "আস্তাগফিরুল্লাহা রাব্বি মিন কুল্লি যাম্বিঁউ ওয়া আতুবু ইলাইহি", "আমি আল্লাহর কাছে সকল গুনাহ থেকে ক্ষমা প্রার্থনা করছি।"),
                Triple("৩য় দশক: নাজাত ও লাইলাতুল কদর (21–30 Ramadan)", "আল্লাহুম্মা ইন্নাকা আফুউন তুহিব্বুল আফওয়া ফা'ফু আন্নী", "হে আল্লাহ! আপনি ক্ষমাশীল, ক্ষমা করতে ভালোবাসেন, তাই আমাকে ক্ষমা করুন।")
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ashraList.forEach { (title, duaBn, meaning) ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NoorMintLuminous)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = duaBn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = meaning, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
