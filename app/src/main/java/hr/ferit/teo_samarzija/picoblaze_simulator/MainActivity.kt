package hr.ferit.teo_samarzija.picoblaze_simulator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class MainActivity : AppCompatActivity() {
    var composeWebView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainScreen(
                    activity = this,
                    onOpenBrowser = {
                        openInBrowser(null)
                    },
                    onShowExamples = {
                        showExamples(null)
                    },
                    onAssemble = {
                        assemble(null)
                    },
                    onStartSimulating = {
                        startSimulating(null)
                    },
                    onHighlightAssembly = {
                        highlightAssembly(null)
                    },
                    onWebViewCreated = {
                        composeWebView = it
                    }
                )
            }
        }
    }

    fun openInBrowser(view: View?) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.setData(
            Uri.parse("https://flatassembler.github.io/PicoBlaze/PicoBlaze.html")
        )
        startActivity(openURL)
    }

    fun moveToMachineCode() {
//        val assembleButton = findViewById<Button>(R.id.button13)
//        assembleButton.setEnabled(true)
//        val simulationButton = findViewById<Button>(R.id.button10)
//        simulationButton.setEnabled(true)
        val intent = Intent(this, machineCode::class.java)
        startActivity(intent)
    }

    fun assemble(view: View?) {
/*        val assembleButton = findViewById<Button>(R.id.button13)
        assembleButton.setEnabled(false)
        val simulationButton = findViewById<Button>(R.id.button10)
        simulationButton.setEnabled(false)*/
        val webView = composeWebView ?: return
        webView.evaluateJavascript(
            "sendMachineCodeToJava()", object : ValueCallback<String> {
                override fun onReceiveValue(value: String) {
                    Log.d("PicoBlaze", "JavaScript returned: $value")
                    if (value == "\"Success!\"") moveToMachineCode()
                    else warnAboutErrorInJavaScript()
                }
            })
    }

    fun warnAboutErrorInJavaScript() {
/*        val assembleButton = findViewById<Button>(R.id.button13)
        assembleButton.setEnabled(true)
        val simulationButton = findViewById<Button>(R.id.button10)
        simulationButton.setEnabled(true)*/
        Toast
            .makeText(
                this,
                "The assembler (written in JavaScript) terminated without sending any machine code to the main program (written in Java).",
                Toast.LENGTH_LONG
            )
            .show()
    }

    fun showExamples(view: View?) {
        val intent = Intent(this, examples::class.java)
        startActivity(intent)
    }

    fun moveToSimulation() {
        /*val assembleButton = findViewById<Button>(R.id.button13)
        assembleButton.setEnabled(true)
        val simulationButton = findViewById<Button>(R.id.button10)
        simulationButton.setEnabled(true)*/
        val intent = Intent(this, simulation::class.java)
        startActivity(intent)
    }

    fun startSimulating(view: View?) {
/*        val assembleButton = findViewById<Button>(R.id.button13)
        assembleButton.setEnabled(false)
        val simulationButton = findViewById<Button>(R.id.button10)
        simulationButton.setEnabled(false)*/
        val webView = composeWebView ?: return
        webView.evaluateJavascript(
            "sendMachineCodeToJava()", object : ValueCallback<String> {
                override fun onReceiveValue(value: String) {
                    Log.d("PicoBlaze", "JavaScript returned: $value")
                    if (value == "\"Success!\"") moveToSimulation()
                    else warnAboutErrorInJavaScript()
                }
            })
    }

    fun highlightAssembly(view: View?) {
        val webView = composeWebView ?: return
        webView.evaluateJavascript("syntaxHighlighter()", null)
    }
}
