package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    WebView webView = findViewById(R.id.WebView);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.setWebChromeClient(
        new WebChromeClient()); // Because JavaScript alerts do not work in
                                // WebView (no idea why), but they do work in
                                // Chrome.
    webView.addJavascriptInterface(new WebAppInterface(this), "PicoBlaze");
    webView.loadUrl("file:///android_asset/MainActivityWebView.html");
  }
  public void openInBrowser(View view) {
    Intent openURL = new Intent(Intent.ACTION_VIEW);
    openURL.setData(
        Uri.parse("https://flatassembler.github.io/PicoBlaze/PicoBlaze.html"));
    startActivity(openURL);
  }
  public void moveToMachineCode() {
    Button assembleButton=findViewById(R.id.button13);
    assembleButton.setEnabled(true);
    Button simulationButton=findViewById(R.id.button10);
    simulationButton.setEnabled(true);
    Intent intent = new Intent(this, machineCode.class);
    startActivity(intent);
  }
  public void assemble(View view) {
    Button assembleButton=findViewById(R.id.button13);
    assembleButton.setEnabled(false);
    Button simulationButton=findViewById(R.id.button10);
    simulationButton.setEnabled(false);
    WebView webView = findViewById(R.id.WebView);
    webView.evaluateJavascript(
        "sendMachineCodeToJava()", new ValueCallback<String>() {
          @Override
          public void onReceiveValue(String value) {
            Log.d("PicoBlaze", "JavaScript returned: " + value);
            if (value.compareTo("\"Success!\"") == 0)
              moveToMachineCode();
            else
              warnAboutErrorInJavaScript();
          }
        });
  }
  public void warnAboutErrorInJavaScript() {
    Button assembleButton=findViewById(R.id.button13);
    assembleButton.setEnabled(true);
    Button simulationButton=findViewById(R.id.button10);
    simulationButton.setEnabled(true);
    Toast
        .makeText(
            this,
            "The assembler (written in JavaScript) terminated without sending any machine code to Java.",
            Toast.LENGTH_LONG)
        .show();
  }
  public void showExamples(View view) {
    Intent intent = new Intent(this, examples.class);
    startActivity(intent);
  }
  public void moveToSimulation() {
    Button assembleButton=findViewById(R.id.button13);
    assembleButton.setEnabled(true);
    Button simulationButton=findViewById(R.id.button10);
    simulationButton.setEnabled(true);
    Intent intent = new Intent(this, simulation.class);
    startActivity(intent);
  }
  public void startSimulating(View view) {
    Button assembleButton=findViewById(R.id.button13);
    assembleButton.setEnabled(false);
    Button simulationButton=findViewById(R.id.button10);
    simulationButton.setEnabled(false);
    WebView webView = findViewById(R.id.WebView);
    webView.evaluateJavascript(
        "sendMachineCodeToJava()", new ValueCallback<String>() {
          @Override
          public void onReceiveValue(String value) {
            Log.d("PicoBlaze", "JavaScript returned: " + value);
            if (value.compareTo("\"Success!\"") == 0)
              moveToSimulation();
            else
              warnAboutErrorInJavaScript();
          }
        });
  }
  public void highlightAssembly(View view) {
    WebView webView = findViewById(R.id.WebView);
    webView.evaluateJavascript("syntaxHighlighter()", null);
  }
}