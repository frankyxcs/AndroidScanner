package balyas.alexander.androidscanner.api;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tuc {
    @SerializedName("phrase")
    @Expose
    private Phrase phrase;

    public Phrase getPhrase() {
        return phrase;
    }

    public void setPhrase(Phrase phrase) {
        this.phrase = phrase;
    }
}
