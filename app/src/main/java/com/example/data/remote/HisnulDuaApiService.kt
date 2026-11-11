package com.example.data.remote

import com.example.domain.DuaItem
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class HisnulCategory(val id: Int, val name: String, val duaCount: Int)

data class HisnulDuaPage(val items: List<DuaItem>, val hasMore: Boolean, val total: Int)

/**
 * Client for the Hisnul Muslim Dua API (Cloudflare Worker + D1, source in /dua-api).
 * Base endpoint: https://dua-api.hisnul.workers.dev
 *  - GET /api/categories
 *  - GET /api/categories/:id/duas?page=&limit=
 *  - GET /api/search?q=&page=&limit=
 *  - GET /api/duas/:id   (full Arabic, transliteration, translation, reference)
 * List endpoints return metadata only, so each page is expanded with the detail endpoint.
 * Mapped DuaItem ids are prefixed with "api_" so favorites/counters never clash with the offline catalog.
 */
object HisnulDuaApiService {

    const val BASE_URL = "https://dua-api.hisnul.workers.dev"
    const val PAGE_SIZE = 15
    const val ID_PREFIX = "api_"

    private val detailCache = ConcurrentHashMap<Int, DuaItem>()

    suspend fun fetchCategories(): List<HisnulCategory> {
        val json = getJson("$BASE_URL/api/categories")
        val arr = json.getJSONArray("data")
        return List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            HisnulCategory(o.getInt("id"), o.getString("name"), o.optInt("dua_count", 0))
        }
    }

    suspend fun fetchCategoryDuas(categoryId: Int, categoryName: String, page: Int): HisnulDuaPage =
        fetchPage("$BASE_URL/api/categories/$categoryId/duas?page=$page&limit=$PAGE_SIZE", categoryName)

    suspend fun search(query: String, page: Int): HisnulDuaPage =
        fetchPage(
            "$BASE_URL/api/search?q=${URLEncoder.encode(query, "UTF-8")}&page=$page&limit=$PAGE_SIZE",
            null
        )

    private suspend fun fetchPage(url: String, categoryName: String?): HisnulDuaPage {
        val json = getJson(url)
        val data = json.getJSONArray("data")
        val pagination = json.optJSONObject("pagination")
        val ids = List(data.length()) { data.getJSONObject(it).getInt("dua_global_id") }
        val items = coroutineScope {
            ids.map { id -> async { fetchDetail(id, categoryName) } }.awaitAll()
        }
        val page = pagination?.optInt("page", 1) ?: 1
        val pages = pagination?.optInt("pages", 1) ?: 1
        return HisnulDuaPage(items, page < pages, pagination?.optInt("total", items.size) ?: items.size)
    }

    private suspend fun fetchDetail(id: Int, categoryName: String?): DuaItem {
        detailCache[id]?.let { return it }
        val d = getJson("$BASE_URL/api/duas/$id").getJSONObject("data")
        val segments = d.optJSONArray("segments")
        val arabic = StringBuilder()
        val translit = StringBuilder()
        val meaning = StringBuilder()
        var reference = ""
        if (segments != null) {
            for (i in 0 until segments.length()) {
                val s = segments.getJSONObject(i)
                appendLine(arabic, s.optStringOrEmpty("arabic"))
                appendLine(translit, s.optStringOrEmpty("transliteration"))
                appendLine(meaning, s.optStringOrEmpty("translations"))
                if (reference.isEmpty()) reference = s.optStringOrEmpty("reference")
            }
        }
        val cats = d.optJSONArray("categories")
        val category = categoryName
            ?: cats?.takeIf { it.length() > 0 }?.getJSONObject(0)?.optString("name")
            ?: "হিসনুল মুসলিম"
        val title = d.optStringOrEmpty("duaname")
        val item = DuaItem(
            id = "$ID_PREFIX$id",
            categoryBn = category,
            categoryEn = category,
            titleBn = title,
            titleEn = title,
            arabic = arabic.toString(),
            transliterationBn = translit.toString(),
            meaningBn = meaning.toString(),
            meaningEn = "",
            reference = reference,
            occasionBn = d.optStringOrEmpty("chapname").ifEmpty { category },
            occasionEn = d.optStringOrEmpty("chapname").ifEmpty { category }
        )
        detailCache[id] = item
        return item
    }

    private fun appendLine(sb: StringBuilder, value: String) {
        if (value.isBlank()) return
        if (sb.isNotEmpty()) sb.append('\n')
        sb.append(value.trim())
    }

    private fun JSONObject.optStringOrEmpty(key: String): String =
        if (isNull(key)) "" else optString(key, "")

    private suspend fun getJson(urlString: String): JSONObject = withContext(Dispatchers.IO) {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (conn.responseCode !in 200..299) error("Dua API HTTP ${conn.responseCode}")
            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            if (!json.optBoolean("success", false)) error(json.optString("error", "Dua API error"))
            json
        } finally {
            conn.disconnect()
        }
    }
}

