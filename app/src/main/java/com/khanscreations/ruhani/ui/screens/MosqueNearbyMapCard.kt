package com.khanscreations.ruhani.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri

/**
 * Full-screen Google Maps embed showing mosques around a coordinate.
 *
 * Google requires the embed URL to be loaded inside an <iframe>, so we wrap
 * the Google Maps embed URL in a local HTML page containing an iframe, then
 * load that HTML into the WebView with maps.google.com as base URL.
 * No Maps API key required.
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

    // Local HTML wrapper that loads the Google Maps embed URL inside an iframe.
    // Google Maps Embed API requires an iframe context; loading the URL directly
    // in WebView triggers "must be used in an iframe" error.
    val html = remember(embedUrl) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: #f1f3f4; }
                iframe { width: 100%; height: 100%; border: none; display: block; }
            </style>
        </head>
        <body>
            <iframe src="$embedUrl" allowfullscreen></iframe>
        </body>
        </html>
        """.trimIndent()
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    tag = embedUrl
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.setGeolocationEnabled(true)

                    webChromeClient = object : WebChromeClient() {
                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String,
                            callback: GeolocationPermissions.Callback
                        ) {
                            callback.invoke(origin, true, false)
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest
                        ): Boolean {
                            val url = request.url.toString()
                            Log.d("NearbyMosqueMap", "shouldOverrideUrlLoading: $url")

                            // Handle intent:// and geo: schemes for turn-by-turn navigation
                            if (url.startsWith("intent://") || url.startsWith("geo:")) {
                                try {
                                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                                    context.startActivity(intent)
                                    return true
                                } catch (_: Exception) {
                                    val fallback = request.url.getQueryParameter("browser_fallback_url")
                                    if (fallback != null) {
                                        view.loadUrl(fallback)
                                        return true
                                    }
                                }
                            } else if (!url.contains("output=embed") &&
                                (url.contains("maps.google.com") || url.contains("google.com/maps"))
                            ) {
                                // User clicked external "View larger map" or "Directions" — open in external app
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, request.url)
                                    context.startActivity(intent)
                                    return true
                                } catch (_: Exception) { }
                            }
                            return false
                        }

                        override fun onPageFinished(view: WebView, url: String) {
                            super.onPageFinished(view, url)
                            Log.d("NearbyMosqueMap", "onPageFinished: $url")
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest?,
                            error: WebResourceError
                        ) {
                            super.onReceivedError(view, request, error)
                            Log.e("NearbyMosqueMap", "onReceivedError: ${error.description}")
                        }
                    }

                    Log.d("NearbyMosqueMap", "Loading iframe HTML with embed URL")
                    loadDataWithBaseURL("https://maps.google.com", html, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                if (webView.tag != embedUrl) {
                    Log.d("NearbyMosqueMap", "Updating embed URL for new location: $coord")
                    webView.tag = embedUrl
                    webView.loadDataWithBaseURL("https://maps.google.com", html, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier.fillMaxSize(),
            onRelease = { webView ->
                webView.destroy()
            }
        )
    }
}
