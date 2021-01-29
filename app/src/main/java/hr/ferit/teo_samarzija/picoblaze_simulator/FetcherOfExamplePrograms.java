package hr.ferit.teo_samarzija.picoblaze_simulator;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FetcherOfExamplePrograms {
  @GET("{file_name}")
  Call<ResponseBody>
  getExampleWithFileName(@Path("file_name") String file_name);
}
