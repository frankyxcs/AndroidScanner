package balyas.alexander.androidscanner.api;


import balyas.alexander.androidscanner.BuildConfig;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TranslateAPI {@GET("api/translate")
Call<Translate> getTranslate(@Query("text") String text, @Query("from") String from, @Query("to") String to);


}
