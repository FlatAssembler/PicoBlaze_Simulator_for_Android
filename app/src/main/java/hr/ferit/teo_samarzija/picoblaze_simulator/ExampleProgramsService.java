package hr.ferit.teo_samarzija.picoblaze_simulator;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ExampleProgramsService {
    @GET("examples.json")
    Call<List<ExampleProgram>> getExamplePrograms();
}
