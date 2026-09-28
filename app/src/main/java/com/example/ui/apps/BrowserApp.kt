package com.example.ui.apps

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.SamsungBlueLight

@Composable
fun BrowserApp(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember { mutableStateOf("https://www.google.com") }
    var currentUrl by remember { mutableStateOf("https://www.google.com") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F121A))
    ) {
        // Browser URL and Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161A24))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close Browser", tint = Color.White)
            }

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("browser_url_input"),
                placeholder = { Text("بحث أو إدخال عنوان الويب...", color = Color(0xFF7E8A9E), fontSize = 12.sp) },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E2330),
                    unfocusedContainerColor = Color(0xFF1A1E29),
                    focusedBorderColor = SamsungBlueLight,
                    unfocusedBorderColor = Color(0xFF2C3242),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        val target = if (urlInput.startsWith("http://") || urlInput.startsWith("https://")) {
                            urlInput
                        } else {
                            "https://www.google.com/search?q=${urlInput.trim()}"
                        }
                        currentUrl = target
                        webViewInstance?.loadUrl(target)
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Go", tint = AuraCyan)
                    }
                }
            )

            IconButton(onClick = { webViewInstance?.reload() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.White)
            }
        }

        // Quick Bookmarks Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF12151E))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                "Google" to "https://www.google.com",
                "Wikipedia" to "https://ar.wikipedia.org",
                "Samsung" to "https://www.samsung.com"
            ).forEach { (name, link) ->
                Text(
                    text = name,
                    color = AuraCyan,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1D2230))
                        .clickable {
                            urlInput = link
                            currentUrl = link
                            webViewInstance?.loadUrl(link)
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // WebView Container
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (url != null) urlInput = url
                                isLoading = false
                            }
                        }
                        loadUrl(currentUrl)
                        webViewInstance = this
                    }
                },
                update = { view ->
                    if (view.url != currentUrl) {
                        view.loadUrl(currentUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
