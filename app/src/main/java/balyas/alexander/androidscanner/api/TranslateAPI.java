package balyas.alexander.androidscanner.api;


import balyas.alexander.androidscanner.BuildConfig;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TranslateAPI {@GET("/api/v1.5/tr.json/translate?lang=en-ru&key=" + BuildConfig.API_YANDEX )
Call<Translate> getTranslateEnRu(@Query("text") String text);

    @GET("/api/v1.5/tr.json/translate?lang=ru-en&key=" + BuildConfig.API_YANDEX )
    Call<Translate> getTranslateRuEn(@Query("text") String text);
}
