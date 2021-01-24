package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class registerDump extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_dump);
    }
    public void continueSimulating(View view) {
        Intent intent=new Intent(this,simulation.class);
        startActivity(intent);
    }
    public void showMachineCode(View view) {
        Intent intent=new Intent(this,machineCode.class);
        startActivity(intent);
    }
    public void showAssemblyCode(View view) {
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
