package balyas.alexander.androidscanner.api;


import balyas.alexander.androidscanner.BuildConfig;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TranslateAPI {
    @GET("translate")
    Call<Translate> getTranslate(@Query("from") String from, @Query("dest") String to, @Query("phrase") String text, @Query("format") String format, @Query("pretty") String pretty);
}
