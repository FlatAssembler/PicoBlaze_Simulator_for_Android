package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class examples extends AppCompatActivity
    implements Callback<List<ExampleProgram>>, View.OnClickListener {
  List<ExampleProgram> examplePrograms;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.examples);
    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl("https://flatassembler.github.io/PicoBlaze/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    ExampleProgramsService service =
        retrofit.create(ExampleProgramsService.class);
    service.getExamplePrograms().enqueue(this);
  }

  @Override
  public void onResponse(Call<List<ExampleProgram>> call,
                         Response<List<ExampleProgram>> response) {
    if (response.body() == null) {
      Toast.makeText(this, R.string.empty_response_warning, Toast.LENGTH_LONG)
          .show();
      return;
    }
    examplePrograms = response.body();
    Log.d("PicoBlaze", "Received a list of example programs:");
    for (ExampleProgram exampleProgram : examplePrograms) {
      Log.d("PicoBlaze", exampleProgram.name);
    }
    Log.d("PicoBlaze", "End of the list!");
    RecyclerView recyclerView = findViewById(R.id.recyclerViewWithExamples);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(
        new AdapterForExamplePrograms(examplePrograms, this));
  }

  @Override
  public void onFailure(Call<List<ExampleProgram>> call, Throwable t) {
    Toast
        .makeText(this,
                  getString(R.string.network_error_warning) + t.getMessage(),
                  Toast.LENGTH_LONG)
        .show();
  }

  @Override
  public void onClick(View v) {
    RecyclerView recyclerView = findViewById(R.id.recyclerViewWithExamples);
    int position = recyclerView.getChildAdapterPosition(v);
    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl(
                "https://raw.githubusercontent.com/FlatAssembler/PicoBlaze_Simulator_in_JS/master/")
            .build();
    FetcherOfExamplePrograms fetcher =
        retrofit.create(FetcherOfExamplePrograms.class);
    Call<ResponseBody> call =
        fetcher.getExampleWithFileName(examplePrograms.get(position).file_name);
    call.enqueue(new ProgramReceiver(this));
  }
}
