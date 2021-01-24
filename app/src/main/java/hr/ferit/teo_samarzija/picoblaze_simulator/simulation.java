package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class simulation extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simulation);
        WebView webView=findViewById(R.id.WebView);
        webView.loadData("STUB - not yet implemented!",null,null);
    }
    public void showAssembly(View view) {
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    public void showRegisters(View view) {
        Intent intent=new Intent(this,registerDump.class);
        startActivity(intent);
    }
}
