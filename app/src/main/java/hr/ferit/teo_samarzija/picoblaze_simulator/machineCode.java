package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class machineCode extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.machine_code);
    }
    public void showAssemblyCode(View view) {
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    public void startSimulating(View view) {
        Intent intent=new Intent(this,simulation.class);
        startActivity(intent);
    }
    public void dumpRegisters(View view) {
        Intent intent=new Intent(this,registerDump.class);
        startActivity(intent);
    }
}
