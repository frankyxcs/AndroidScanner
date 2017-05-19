package balyas.alexander.androidscanner.api;


import com.google.gson.annotations.SerializedName;

public class Translate {
    @SerializedName("from")
    private String fromLang;

    @SerializedName("to")
    private String toLang;

    @SerializedName("text")
    private String text;

    @SerializedName("translationText")
    private String  translate;

    public String getFromLang() {
        return fromLang;
    }

    public String getToLang() {
        return toLang;
    }

    public String getText() {
        return text;
    }

    public String getTranslate() {
        return translate;
    }
}
