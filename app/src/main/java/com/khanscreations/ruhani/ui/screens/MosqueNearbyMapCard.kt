package com.khanscreations.ruhani.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Intent
import androidx.core.net.toUri

/**
 * Google Maps embed that searches for mosques around a coordinate and lists the
 * results inside the app.
 *
 * Uses the legacy `maps.google.com/maps?...&output=embed` page, so no Maps API
 * key is required. The embed loads over HTTPS only, which keeps the
 * `network_security_config` intact. The coordinate is the centre of the
 * selected district, not a stored user location.
 */
@SuppressLint(
    "SetJavaScriptEnabled"
) // the Google Maps embed page requires JavaScript; only a first-party maps.google.com URL over HTTPS is loaded here
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
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isBn) {
                    "কাছাকাছি মসজিদ — Google Maps সার্চ ($districtLabelBn কেন্দ্রিক)"
                } else {
                    "Nearby mosques — Google Maps search (centred on $districtLabelBn)"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(true)
                        webViewClient = WebViewClient()
                        loadUrl(embedUrl)
                    }
                },
                update = { webView ->
                    if (webView.url != embedUrl) webView.loadUrl(embedUrl)
                },
                onRelease = { webView ->
                    webView.destroy()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Text(
                text = if (isBn) {
                    "ম্যাপে স্ক্রল ও জুম করে আপনার এলাকার মসজিদ দেখুন। ম্যাপটি Google Maps থেকে লোড হয়, অ্যাপ কোনো লোকেশন সংরক্ষণ করে না।"
                } else {
                    "Pan and zoom the map to find mosques near you. The map is loaded from Google Maps; the app stores no location."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = {
                    // Full-screen fallback for turn-by-turn navigation.
                    val mapUri = "geo:$coord?q=${Uri.encode("mosque")}&z=15".toUri()
                    val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                    runCatching { context.startActivity(mapIntent) }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBn) "Google Maps অ্যাপে নেভিগেশন খুলুন" else "Open navigation in Google Maps",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
