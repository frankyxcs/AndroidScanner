package balyas.alexander.androidscanner.api;


import com.google.gson.annotations.SerializedName;

public class Translate {
    @SerializedName("code")
    private int translateCode;

    @SerializedName("text")
    private String[] translate;

    public int getTranslateCode() {
        return translateCode;
    }

    public String[] getTranslate() {
        return translate;
    }
}
