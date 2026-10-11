package com.vish.mapbox

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.vish.mapbox.ui.theme.MapboxTheme

class MainActivity : ComponentActivity() {
    private var webViewInstance: WebView? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MapboxTheme {
                var showInfoDialog by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Mapbox DevCon '26",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(Color(0xFF22C55E), shape = CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Booth #8 · CityCube Berlin",
                                            fontSize = 11.sp,
                                            color = Color(0xFF38BDF8),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    webViewInstance?.evaluateJavascript("flyToCityCube()", null)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "CityCube",
                                        tint = Color(0xFF38BDF8)
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    webViewInstance?.evaluateJavascript("toggleOrbit()", null)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Tour",
                                        tint = Color.White
                                    )
                                }
                                IconButton(onClick = { showInfoDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Event Info",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color(0xFF0F172A)
                            )
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (BuildConfig.MAPBOX_ACCESS_TOKEN.isBlank()) {
                            MissingTokenMessage()
                        } else {
                            MapboxWebView(
                                onWebViewCreated = { webView ->
                                    webViewInstance = webView
                                }
                            )
                        }

                        if (showInfoDialog) {
                            RaffleInfoDialog(
                                onDismiss = { showInfoDialog = false },
                                onFlyToBooth = {
                                    showInfoDialog = false
                                    webViewInstance?.evaluateJavascript("flyToCityCube()", null)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val MAP_HTML_ASSET = "mapbox_map.html"
private const val TOKEN_PLACEHOLDER = "__MAPBOX_ACCESS_TOKEN__"

/**
 * Reads the map page from assets and injects the Mapbox access token, which comes from
 * local.properties via BuildConfig, so it never has to be stored in the asset itself.
 */
private fun loadMapHtml(context: Context): String {
    val template = context.assets.open(MAP_HTML_ASSET).bufferedReader().use { it.readText() }
    val token = BuildConfig.MAPBOX_ACCESS_TOKEN
        .replace("\\", "\\\\")
        .replace("'", "\\'")
    return template.replace(TOKEN_PLACEHOLDER, token)
}

/** Shows only the token type and its last four characters, e.g. "pk.…abcd". */
internal fun maskToken(token: String): String = when {
    token.isBlank() -> "not configured"
    token.length <= 8 -> "****"
    else -> token.substringBefore('.', "").let { type ->
        (if (type.isNotEmpty()) "$type." else "") + "…" + token.takeLast(4)
    }
}

@Composable
fun MissingTokenMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No Mapbox access token configured.\n\nAdd MAPBOX_ACCESS_TOKEN to local.properties and rebuild. See README.md.",
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapboxWebView(onWebViewCreated: (WebView) -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    setSupportZoom(false)
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mediaPlaybackRequiresUserGesture = false
                }
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                webChromeClient = object : WebChromeClient() {
                    override fun onGeolocationPermissionsShowPrompt(
                        origin: String?,
                        callback: GeolocationPermissions.Callback?
                    ) {
                        callback?.invoke(origin, true, false)
                    }
                }

                webViewClient = object : WebViewClient() {}

                loadDataWithBaseURL(
                    "file:///android_asset/",
                    loadMapHtml(context),
                    "text/html",
                    "UTF-8",
                    null
                )
                onWebViewCreated(this)
            }
        }
    )
}

@Composable
fun RaffleInfoDialog(
    onDismiss: () -> Unit,
    onFlyToBooth: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🎟️ Next.app DevCon 2026",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Mapbox Booth #8 Raffle",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Prize: 6 LEGO® Sets\n• Rule: Install any Mapbox SDK and show it running!\n• Draw: 3:00 PM Day 3 at Booth #8",
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Featured Mapbox Talks",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Flutter + Mapbox (Erwin Soekianto)\n2. Android Automotive LLM Co-Pilot (Boris Denisenko)",
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF22C55E), shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SDK Status: LIVE & VERIFIED",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF22C55E),
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Token: ${maskToken(BuildConfig.MAPBOX_ACCESS_TOKEN)}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onFlyToBooth,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
            ) {
                Text("Fly to Booth #8")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}