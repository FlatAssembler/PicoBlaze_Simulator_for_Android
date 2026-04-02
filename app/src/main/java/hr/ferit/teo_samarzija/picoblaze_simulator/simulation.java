package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

public class simulation extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.simulation);
    WebView webView = findViewById(R.id.WebView);
    Simulator.getInstance().referenceToTheWebViewInSimulation = webView;
    webView.getSettings().setJavaScriptEnabled(true);
    WebAppInterface PicoBlaze = new WebAppInterface(this);
    PicoBlaze.referenceToSimulation = this;
    webView.addJavascriptInterface(PicoBlaze, "PicoBlaze");
    webView.loadUrl("file:///android_asset/SimulationWebView.html");
  }
  public void showAssembly(View view) {
    stopSimulation();
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }
  public void showRegisters(View view) {
    stopSimulation();
    Intent intent = new Intent(this, registerDump.class);
    startActivity(intent);
  }
  public void startSimulation() {
    Log.d("PicoBlaze","Starting the simulation");
    Simulator.getInstance().myTimer.schedule(new MyTimerTask(), 100, 100);
  }

  public void stopSimulation() {
    Log.d("PicoBlaze", "Stopping the simulation");
    Simulator.getInstance().myTimer.cancel();
  }
}
