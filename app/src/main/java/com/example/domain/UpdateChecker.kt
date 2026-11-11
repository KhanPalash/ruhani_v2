package com.example.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val releaseUrl: String,
    val releaseNotes: String = ""
)

object UpdateChecker {

    private const val GITHUB_API =
        "https://api.github.com/repos/KhanPalash/ruhani_v2/releases/latest"

    /**
     * Checks GitHub releases for a newer version.
     * Returns [UpdateInfo] if an update is available, null otherwise (or on any failure).
     * Runs on the IO dispatcher — safe to call from a coroutine.
     */
    suspend fun checkForUpdate(currentVersion: String): UpdateInfo? =
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(GITHUB_API).openConnection()
                connection.connectTimeout = 6_000
                connection.readTimeout = 6_000
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                val response = connection.getInputStream().bufferedReader().readText()

                val json = JSONObject(response)
                val latestTag = json.getString("tag_name").trimStart('v')
                val htmlUrl = json.getString("html_url")
                val body = json.optString("body", "")

                if (isNewer(latestTag, currentVersion)) {
                    UpdateInfo(
                        latestVersion = latestTag,
                        releaseUrl = htmlUrl,
                        releaseNotes = body.take(300) // keep it short for the dialog
                    )
                } else null
            } catch (_: Exception) {
                // Silent fail: no internet, rate limit, wrong URL, etc.
                null
            }
        }

    /** Returns true if [latest] is strictly newer than [current] using semver comparison. */
    private fun isNewer(latest: String, current: String): Boolean {
        fun parts(v: String) = v.split(".").map { it.toIntOrNull() ?: 0 }
        val l = parts(latest)
        val c = parts(current)
        for (i in 0 until maxOf(l.size, c.size)) {
            val diff = (l.getOrElse(i) { 0 }) - (c.getOrElse(i) { 0 })
            if (diff > 0) return true
            if (diff < 0) return false
        }
        return false
    }
}
