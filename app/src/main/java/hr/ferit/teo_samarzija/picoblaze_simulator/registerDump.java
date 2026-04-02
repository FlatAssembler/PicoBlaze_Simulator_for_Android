package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class registerDump extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.register_dump);
    RecyclerView recyclerView = findViewById(R.id.recyclerViewWithRegisters);
    recyclerView.setAdapter(new AdapterForRegisterDumping());
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
  }
  public void continueSimulating(View view) {
    Intent intent = new Intent(this, simulation.class);
    startActivity(intent);
  }
  public void showMachineCode(View view) {
    Intent intent = new Intent(this, machineCode.class);
    startActivity(intent);
  }
  public void showAssemblyCode(View view) {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
  }
}
