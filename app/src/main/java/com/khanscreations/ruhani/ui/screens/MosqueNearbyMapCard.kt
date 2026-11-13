package com.khanscreations.ruhani.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri

/**
 * Full-screen Google Maps embed showing mosques around a coordinate.
 *
 * Uses the Google Maps web search URL. The page may return an intent:// URL
 * to open the Google Maps app; we intercept that and load the fallback HTTPS
 * URL so the map renders inside the WebView. No API key required.
 * Loads over HTTPS to keep network_security_config intact.
 */
@SuppressLint(
    "SetJavaScriptEnabled"
) // The Google Maps web page requires JavaScript; only a first-party google.com URL over HTTPS is loaded here
@Composable
fun NearbyMosqueMapCard(
    isBn: Boolean,
    districtLabelBn: String,
    lat: Double,
    lon: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coord = "$lat,$lon"
    val embedUrl = remember(coord) {
        "https://www.google.com/maps/search/mosque/@${lat},${lon},15z".toUri().toString()
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    // Intercept intent:// URLs and load the fallback HTTPS URL instead
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            if (url.startsWith("intent://")) {
                                // Extract the fallback URL from the intent
                                val fallbackStart = url.indexOf("S.browser_fallback_url=")
                                if (fallbackStart >= 0) {
                                    var fallback = url.substring(fallbackStart + "S.browser_fallback_url=".length)
                                    // Trim at next ; or end
                                    val semi = fallback.indexOf(';')
                                    if (semi >= 0) fallback = fallback.substring(0, semi)
                                    // URL decode
                                    try {
                                        fallback = Uri.decode(fallback)
                                    } catch (_: Exception) { }
                                    view.loadUrl(fallback)
                                    return true
                                }
                            }
                            return super.shouldOverrideUrlLoading(view, url)
                        }
                    }
                    loadUrl(embedUrl)
                }
            },
            update = { webView ->
                if (webView.url != embedUrl) webView.loadUrl(embedUrl)
            },
            onRelease = { webView ->
                webView.destroy()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}