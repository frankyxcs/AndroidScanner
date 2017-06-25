package balyas.alexander.androidscanner.api;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Translate {
    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("tuc")
    @Expose
    private List<Tuc> tuc = null;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<Tuc> getTuc() {
        return tuc;
    }

    public void setTuc(List<Tuc> tuc) {
        this.tuc = tuc;
    }
}
