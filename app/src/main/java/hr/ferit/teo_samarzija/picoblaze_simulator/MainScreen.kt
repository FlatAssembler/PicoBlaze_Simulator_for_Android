package hr.ferit.teo_samarzija.picoblaze_simulator

import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebChromeClient
import android.graphics.Color

@Composable
fun MainScreen(
    activity: MainActivity,
    onOpenBrowser: () -> Unit,
    onShowExamples: () -> Unit,
    onAssemble: () -> Unit,
    onStartSimulating: () -> Unit,
    onHighlightAssembly: () -> Unit,
    onWebViewCreated: (WebView) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {

        Text(
            text = "PicoBlaze Simulator - Assembly Code",
            style = MaterialTheme.typography.headlineMedium
        )

        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { context ->

                WebView(context).apply {
                    onWebViewCreated(this)
                    settings.javaScriptEnabled = true
                    webViewClient = android.webkit.WebViewClient()
                    webChromeClient = object : WebChromeClient() {

                        override fun onConsoleMessage(message: android.webkit.ConsoleMessage): Boolean {

                            android.util.Log.d(
                                "WEBVIEW_JS",
                                message.message()
                            )

                            return true
                        }
                    }

                    addJavascriptInterface(
                        WebAppInterface(activity),
                        "PicoBlaze"
                    )
                    //setBackgroundColor(android.graphics.Color.YELLOW)
                    loadUrl("file:///android_asset/MainActivityWebView.html")
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Button(onClick = onHighlightAssembly) {
                Text("Highlight")
            }

            Button(onClick = onAssemble) {
                Text("Assemble")
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onShowExamples
        ) {
            Text("Choose Example")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onStartSimulating
        ) {
            Text("Assemble and Run")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenBrowser
        ) {
            Text("Open in Browser")
        }
    }
}