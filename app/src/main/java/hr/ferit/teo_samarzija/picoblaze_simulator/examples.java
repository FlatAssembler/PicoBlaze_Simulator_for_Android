package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class examples extends AppCompatActivity implements Callback<List<ExampleProgram>> {
  List<ExampleProgram> examplePrograms;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.examples);
    Retrofit retrofit=new Retrofit.Builder().baseUrl("https://flatassembler.github.io/PicoBlaze/").addConverterFactory(GsonConverterFactory.create()).build();
    ExampleProgramsService service=retrofit.create(ExampleProgramsService.class);
    service.getExamplePrograms().enqueue(this);
  }

  @Override
  public void onResponse(Call<List<ExampleProgram>> call, Response<List<ExampleProgram>> response) {
    examplePrograms=response.body();
    Log.d("PicoBlaze","Received a list of example programs:");
    for (ExampleProgram exampleProgram: examplePrograms) {
      Log.d("PicoBlaze",exampleProgram.name);
    }
    Log.d("PicoBlaze","End of the list!");
  }

  @Override
  public void onFailure(Call<List<ExampleProgram>> call, Throwable t) {
    Toast.makeText(this,"Cannot connect to the server with example programs! "+t.getMessage(),Toast.LENGTH_LONG).show();
  }
}
