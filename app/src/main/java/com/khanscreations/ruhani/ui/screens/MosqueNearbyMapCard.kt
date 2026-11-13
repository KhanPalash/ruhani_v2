package com.khanscreations.ruhani.ui.screens

import android.util.Log
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
 * Uses the legacy `maps.google.com/maps?...&output=embed` page which
 * renders an embeddable map with mosque pins. No Maps API key required.
 * Loads over HTTPS to keep network_security_config intact.
 */
@SuppressLint(
    "SetJavaScriptEnabled"
) // The Google Maps embed page requires JavaScript; only a first-party maps.google.com URL over HTTPS is loaded here
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
        "https://maps.google.com/maps".toUri()
            .buildUpon()
            .appendQueryParameter("q", "mosque")
            .appendQueryParameter("ll", coord)
            .appendQueryParameter("sll", coord)
            .appendQueryParameter("z", "15")
            .appendQueryParameter("output", "embed")
            .build()
            .toString()
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
                            Log.d("NearbyMosqueMap", "shouldOverrideUrlLoading: $url")
                            if (url.startsWith("intent://")) {
                                val fallbackStart = url.indexOf("S.browser_fallback_url=")
                                if (fallbackStart >= 0) {
                                    var fallback = url.substring(fallbackStart + "S.browser_fallback_url=".length)
                                    val semi = fallback.indexOf(';')
                                    if (semi >= 0) fallback = fallback.substring(0, semi)
                                    try {
                                        fallback = Uri.decode(fallback)
                                    } catch (_: Exception) { }
                                    Log.d("NearbyMosqueMap", "Loading fallback: $fallback")
                                    view.loadUrl(fallback)
                                    return true
                                }
                            }
                            return super.shouldOverrideUrlLoading(view, url)
                        }
                        override fun onPageFinished(view: WebView, url: String) {
                            super.onPageFinished(view, url)
                            Log.d("NearbyMosqueMap", "onPageFinished: $url")
                        }
                        override fun onReceivedError(view: WebView, request: android.webkit.WebResourceRequest?, error: android.webkit.WebResourceError) {
                            super.onReceivedError(view, request, error)
                            Log.e("NearbyMosqueMap", "onReceivedError: ${error.description}")
                        }
                    }
                    Log.d("NearbyMosqueMap", "Loading URL: $embedUrl")
                    loadUrl(embedUrl)
                }
            },
            update = { webView ->
                if (webView.url != embedUrl) {
                    Log.d("NearbyMosqueMap", "Reloading URL: $embedUrl")
                    webView.loadUrl(embedUrl)
                }
            },
            onRelease = { webView ->
                webView.destroy()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}