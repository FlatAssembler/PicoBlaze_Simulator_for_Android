package hr.ferit.teo_samarzija.picoblaze_simulator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebView webView=findViewById(R.id.WebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/MainActivityWebView.html");
    }
    public void openInBrowser(View view) {
        Intent openURL=new Intent(Intent.ACTION_VIEW);
        openURL.setData(Uri.parse("https://flatassembler.github.io/PicoBlaze/PicoBlaze.html"));
        startActivity(openURL);
    }
    public void assemble(View view) {
        Intent intent=new Intent(this,machineCode.class);
        startActivity(intent);
    }
    public void showExamples(View view) {
        Intent intent=new Intent(this,examples.class);
        startActivity(intent);
    }
    public void startSimulating(View view) {
        Intent intent=new Intent(this,simulation.class);
        startActivity(intent);
    }
    public void highlightAssembly(View view) {
        WebView webView=findViewById(R.id.WebView);
        webView.evaluateJavascript("syntaxHighlighter()",null);
    }
}