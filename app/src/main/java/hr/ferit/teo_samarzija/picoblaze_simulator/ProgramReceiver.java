package hr.ferit.teo_samarzija.picoblaze_simulator;

import android.content.Intent;
import android.widget.Toast;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgramReceiver implements Callback<ResponseBody> {
  examples examples;
  public ProgramReceiver(examples examples) { this.examples = examples; }

  @Override
  public void onResponse(Call<ResponseBody> call,
                         Response<ResponseBody> response) {
    if (response.body() == null) {
      Toast
          .makeText(examples, R.string.empty_response_warning,
                    Toast.LENGTH_LONG)
          .show();
      return;
    }
    try {
      AssembledProgram.getInstance().assemblyCode = response.body().string();
    } catch (IOException e) {
      Toast.makeText(examples, e.getMessage(), Toast.LENGTH_LONG).show();
    }
    Intent intent = new Intent(examples, MainActivity.class);
    examples.startActivity(intent);
  }

  @Override
  public void onFailure(Call<ResponseBody> call, Throwable t) {
    Toast.makeText(examples, t.getMessage(), Toast.LENGTH_LONG).show();
  }
}
