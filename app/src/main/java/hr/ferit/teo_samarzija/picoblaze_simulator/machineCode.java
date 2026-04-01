package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class machineCode extends AppCompatActivity {
  AdapterForMachineCode adapter;
  LinearLayoutManager manager;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.machine_code);
    RecyclerView recyclerView = findViewById(R.id.recyclerView2);
    adapter = new AdapterForMachineCode();
    recyclerView.setAdapter(adapter);
    manager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(manager);
      ScrollView scrollView2 = findViewById(R.id.scrollView2);
      scrollView2.setBackgroundColor(Color.BLACK);
      TextView myTextView = new TextView(scrollView2.getContext());
      myTextView.setText(AssembledProgram.getInstance().terminalOutputDuringAssembly);
      myTextView.setTextColor(Color.CYAN);
      myTextView.setPadding(10,10,10,0);
      scrollView2.addView(myTextView);
  }
  public void showAssemblyCode(View view) {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }
  public void startSimulating(View view) {
    Intent intent = new Intent(this, simulation.class);
    startActivity(intent);
  }
  public void dumpRegisters(View view) {
    Intent intent = new Intent(this, registerDump.class);
    startActivity(intent);
  }
}
