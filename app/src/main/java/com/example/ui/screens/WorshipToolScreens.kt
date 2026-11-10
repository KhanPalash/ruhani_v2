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
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.example.domain.*
import com.example.ui.RuhaniUiState
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.NoorEmeraldPrimary
import com.example.ui.theme.NoorGoldAccent
import com.example.ui.theme.NoorMintLuminous
import com.example.ui.theme.NoorRedAlert
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
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag("subscreen_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Column {
            Text(
                text = if (isBn) "$titleBn • $titleEn" else "$titleEn • $titleBn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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
    onSpeakArabic: (String) -> Unit
) {
    BackHandler { onBack() }

    val isBn = uiState.language == "bn"
    val categories = remember {
        listOf("সকল দোয়া", "সকালের আমল", "সন্ধ্যার আমল", "সালাত পরবর্তী", "দৈনন্দিন", "রুকইয়াহ (Ruqyah)")
    }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    val counterMap = remember { mutableStateMapOf<String, Int>() }

    val filteredDuas = remember(selectedCategory) {
        if (selectedCategory == "সকল দোয়া") RuhaniContentCatalog.duaList
        else RuhaniContentCatalog.duaList.filter { it.categoryBn == selectedCategory }
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
                titleBn = "দোয়া, যিকির ও রুকইয়াহ",
                titleEn = "Dua, Adhkar & Ruqyah",
                subtitle = if (isBn) "হিসনুল মুসলিম ও কুরআন-সুন্নাহর প্রামাণিক দোয়া" else "Authentic supplications from Hisnul Muslim & Sunnah",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val selected = cat == selectedCategory
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) NoorEmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        items(filteredDuas) { dua ->
            val currentCount = counterMap[dua.id] ?: 0
            Surface(
                shape = RoundedCornerShape(20.dp),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) dua.titleBn else dua.titleEn,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = NoorMintLuminous
                            )
                            Text(
                                text = dua.categoryBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onSpeakArabic(dua.arabic) },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NoorEmeraldPrimary.copy(alpha = 0.16f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen Dua",
                                tint = NoorMintLuminous,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = dua.arabic,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = AmiriFontFamily,
                            fontSize = 26.sp,
                            lineHeight = 44.sp,
                            textDirection = TextDirection.Rtl
                        ),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "উচ্চারণ: ${dua.transliterationBn}",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = NoorMintLuminous
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

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সূত্র: ${dua.referenceBn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (currentCount >= dua.repeatTarget) NoorEmeraldPrimary
                            else NoorEmeraldPrimary.copy(alpha = 0.18f),
                            modifier = Modifier.clickable {
                                val next = if (currentCount >= dua.repeatTarget) 0 else currentCount + 1
                                counterMap[dua.id] = next
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = if (currentCount >= dua.repeatTarget) Color.White else NoorMintLuminous,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "$currentCount / ${dua.repeatTarget}x",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFontFamily),
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentCount >= dua.repeatTarget) Color.White else NoorMintLuminous
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
fun QiblaCompassScreen(
    uiState: RuhaniUiState,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val isBn = uiState.language == "bn"
    val targetBearing = uiState.selectedDistrict.qiblaBearingDeg.toFloat()
    var sensorAzimuth by remember { mutableFloatStateOf(277f) }
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
    val angleDiff = abs(((animatedAzimuth - targetBearing + 540f) % 360f) - 180f)
    val isAligned = angleDiff <= 5f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("qibla_compass_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            SubScreenHeader(
                titleBn = "কিবলা কম্পাস",
                titleEn = "Qibla Compass",
                subtitle = "${uiState.selectedDistrict.nameBn} থেকে পবিত্র কাবার দিক ও দূরত্ব",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Bearing & Distance Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBn) "কিবলার কোণ (Qibla Bearing)" else "Qibla Bearing",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${"%.1f".format(targetBearing)}° W",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NoorMintLuminous
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBn) "মক্কার দূরত্ব (Distance)" else "Distance to Kaaba",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "%,d km".format(uiState.selectedDistrict.distanceToMakkahKm),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Interactive Compass Dial Canvas
        item {
            Box(
                modifier = Modifier
                    .size(270.dp)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    // Outer Ring
                    drawCircle(
                        color = if (isAligned) NoorEmeraldPrimary else Color(0xFF283A33),
                        radius = radius,
                        style = Stroke(width = 8.dp.toPx())
                    )
                    // Rotate Dial by -animatedAzimuth
                    rotate(-animatedAzimuth) {
                        for (deg in 0 until 360 step 15) {
                            rotate(deg.toFloat()) {
                                val tickLen = if (deg % 90 == 0) 16.dp.toPx() else 8.dp.toPx()
                                drawLine(
                                    color = if (deg == 0) NoorRedAlert else Color(0xFF7B948B),
                                    start = Offset(center.x, center.y - radius + 14.dp.toPx()),
                                    end = Offset(center.x, center.y - radius + 14.dp.toPx() + tickLen),
                                    strokeWidth = if (deg % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                                )
                            }
                        }
                        // Qibla Needle pointing at targetBearing
                        rotate(targetBearing) {
                            drawLine(
                                color = NoorMintLuminous,
                                start = center,
                                end = Offset(center.x, center.y - radius + 28.dp.toPx()),
                                strokeWidth = 5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = NoorGoldAccent,
                                radius = 9.dp.toPx(),
                                center = Offset(center.x, center.y - radius + 28.dp.toPx())
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🕋",
                        fontSize = 28.sp
                    )
                    Text(
                        text = "${animatedAzimuth.toInt()}°",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isAligned) NoorMintLuminous else MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isAligned) NoorEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isAligned) {
                                if (isBn) "কিবলামুখী (Aligned)" else "Aligned with Kaaba"
                            } else {
                                if (isBn) "${targetBearing.toInt()}° তে ঘোরান" else "Rotate to ${targetBearing.toInt()}°"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAligned) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Manual Orientation Calibration Slider (for emulator / indoor magnetic interference)
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "ম্যানুয়াল কম্পাস ক্যালিব্রেশন" else "Compass Orientation Calibration",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasHardwareCompass) "Hardware Sensor Active" else "Interactive Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = NoorMintLuminous
                        )
                    }
                    Slider(
                        value = sensorAzimuth,
                        onValueChange = { sensorAzimuth = it },
                        valueRange = 0f..360f
                    )
                }
            }
        }
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tri_calendar_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SubScreenHeader(
                titleBn = "হিজরি, বাংলা ও ইংরেজি ক্যালেন্ডার",
                titleEn = "Tri-Calendar & Holidays",
                subtitle = if (isBn) "চাঁদ দেখার সমন্বয় ও ইসলামিক দিবসসমূহ" else "Hijri, Bangla Bongabdo & Gregorian calendar",
                isBn = isBn,
                onBack = onBack
            )
        }

        // Tri-Date Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, NoorEmeraldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF153529), Color(0xFF0E1D17))))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = schedule.hijriDateBn,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = NoorMintLuminous
                    )
                    Text(
                        text = "বাংলা বঙ্গাব্দ: ${schedule.banglaDateBn} (${schedule.banglaDateEn})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Text(
                        text = "ইংরেজি: ${schedule.gregorianDateBn} (${schedule.gregorianDateEn})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA3B8B0)
                    )
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
