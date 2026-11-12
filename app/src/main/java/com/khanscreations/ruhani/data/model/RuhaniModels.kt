package com.khanscreations.ruhani.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    val userId: String = "",
    val displayName: String = "মুসলিম ব্যবহারকারী",
    val email: String? = null,
    val phone: String? = null,
    val photoUrl: String? = null,
    val district: String = "Dhaka",
    val calculationMethod: String = "ISLAMIC_FOUNDATION_BD",
    val madhhab: String = "HANAFI",
    val language: String = "bn",
    val themeMode: String = "DARK",
    val tasbihAllTimeTotal: Long = 0L,
    val qazaFajr: Long = 0L,
    val qazaDhuhr: Long = 0L,
    val qazaAsr: Long = 0L,
    val qazaMaghrib: Long = 0L,
    val qazaIsha: Long = 0L,
    val qazaWitr: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SalatLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateKey: String = "",
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false,
    val witr: Boolean = false,
    val duha: Boolean = false,
    val awwabin: Boolean = false,
    val tahajjud: Boolean = false,
    val fasting: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class QuranBookmark(
    @DocumentId val id: String = "",
    val userId: String = "",
    val surahNumber: Long = 1L,
    val ayahNumber: Long = 1L,
    val surahNameBn: String = "",
    val surahNameEn: String = "",
    val arabicPreview: String = "",
    val translationPreview: String = "",
    val note: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
