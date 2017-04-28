package balyas.alexander.androidscanner;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import balyas.alexander.androidscanner.api.Translate;
import balyas.alexander.androidscanner.api.TranslateAPI;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SimpleAndroidOCRActivity extends Activity implements View.OnClickListener, Callback<Translate> {
    public static final String PACKAGE_NAME = "com.datumdroid.android.ocr.simple";
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";

    // You should have the trained data file in assets folder
    // You can get them at:
    // https://github.com/tesseract-ocr/tessdata
    public static String lang = "eng";
    public static String[] langs = {"eng", "rus", "ukr", "fra", "spa"};
    public static String[] langs_2 = {"en", "ru", "uk", "fr", "es"};
    public static String translate = "ru";

    private static final String TAG = "SimpleAndroidOCR.java";

    protected Button _button;
    private Button bTrans;
    // protected ImageView _image;
    protected EditText _field;
    protected EditText _field2;
    protected String _path;
    protected boolean _taken;

    private TextView mainText;
    private Button btEng;
    private Button btRus;
    private Button btUkr;
    private Button btFra;
    private Button btEsp;

    private TextView transText;
    private Button btEngT;
    private Button btRusT;
    private Button btUkrT;
    private Button btFraT;
    private Button btEspT;

    protected static final String PHOTO_TAKEN = "photo_taken";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mainText = (TextView) findViewById(R.id.main_text);
        btEng = (Button) findViewById(R.id.bt_eng);
        btRus = (Button) findViewById(R.id.bt_rus);
        btUkr = (Button) findViewById(R.id.bt_ukr);
        btFra = (Button) findViewById(R.id.bt_fra);
        btEsp = (Button) findViewById(R.id.bt_esp);

        transText = (TextView) findViewById(R.id.translate_text);
        btEngT = (Button) findViewById(R.id.bt_eng_tran);
        btRusT = (Button) findViewById(R.id.bt_rus_tran);
        btUkrT = (Button) findViewById(R.id.bt_ukr_tran);
        btFraT = (Button) findViewById(R.id.bt_fra_tran);
        btEspT = (Button) findViewById(R.id.bt_esp_tran);

        String[] paths = new String[]{DATA_PATH, DATA_PATH + "tessdata/"};

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            } else {
                Log.v(TAG, "Directory has already created: " + path);
            }

        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        for (int i = 0; i < langs.length; i++) {
            if (!(new File(DATA_PATH + "tessdata/" + langs[i] + ".traineddata")).exists()) {
                try {

                    AssetManager assetManager = getAssets();
                    InputStream in = assetManager.open(langs[i] + ".traineddata");
                    //GZIPInputStream gin = new GZIPInputStream(in);
                    OutputStream out = new FileOutputStream(DATA_PATH
                            + "tessdata/" + langs[i] + ".traineddata");

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    //while ((lenf = gin.read(buff)) > 0) {
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    //gin.close();
                    out.close();

                    Log.v(TAG, "Copied " + langs[i] + " traineddata");
                } catch (IOException e) {
                    Log.e(TAG, "Was unable to copy " + langs[i] + " traineddata " + e.toString());
                }
            } else {
                Log.v(TAG, "File is already exist");
            }

        }

        // _image = (ImageView) findViewById(R.id.image);
        _field = (EditText) findViewById(R.id.field);
        _field2 = (EditText) findViewById(R.id.field2);
        _button = (Button) findViewById(R.id.button);
        _button.setOnClickListener(new ButtonClickHandler());

        bTrans = (Button) findViewById(R.id.bt_get_trans);
        bTrans.setOnClickListener(new ButtonClickHandler());

        _path = DATA_PATH + "/ocr.jpg";
    }

    @Override
    public void onClick(final View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_eng:
                mainText.setText("Обрано мову розпізнавання: англійська");
                lang = "eng";
                break;
            case R.id.bt_rus:
                mainText.setText("Обрано мову розпізнавання: російська");
                lang = "rus";
                break;
            case R.id.bt_ukr:
                mainText.setText("Обрано мову розпізнавання: українська");
                lang = "ukr";
                break;
            case R.id.bt_fra:
                mainText.setText("Обрано мову розпізнавання: французька");
                lang = "fra";
                break;
            case R.id.bt_esp:
                mainText.setText("Обрано мову розпізнавання: іспанська");
                lang = "spa";
                break;
            case R.id.bt_eng_tran:
                transText.setText("Обрано мову перекладу: англійська");
                translate = "en";
                break;
            case R.id.bt_rus_tran:
                transText.setText("Обрано мову перекладу: російська");
                translate = "ru";
                break;
            case R.id.bt_ukr_tran:
                transText.setText("Обрано мову перекладу: українська");
                translate = "uk";
                break;
            case R.id.bt_fra_tran:
                transText.setText("Обрано мову перекладу: французька");
                translate = "fr";
                break;
            case R.id.bt_esp_tran:
                transText.setText("Обрано мову перекладу: іспанська");
                translate = "es";
                break;
        }
    }


    public class ButtonClickHandler implements View.OnClickListener {
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button:
                    Log.v(TAG, "Starting Camera app");
                    startCameraActivity();
                    break;
                case R.id.bt_get_trans:
                    getTranslate();
            }

        }
    }

    private void getTranslate() {
        String BASE_URL = "https://translate.yandex.net";
        String firstPart = _field.getText().toString();
        if (firstPart.length() > 1) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client2 = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit client = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client2)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            TranslateAPI transAPI = client.create(TranslateAPI.class);

            if ("ru".compareTo(translate) == 0) {
                Call<Translate> callTrans = transAPI.getTranslateEnRu(firstPart);
                callTrans.enqueue(this);
            } else {

            }
        }
    }

    @Override
    public void onResponse(final Call<Translate> call, final Response<Translate> response) {
        if (response.isSuccessful()){
            String[] array = response.body().getTranslate();

            _field2.setText(array[0]);

        } else {
            Toast.makeText(SimpleAndroidOCRActivity.this, "Error code " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(final Call<Translate> call, final Throwable t) {

    }

    // Simple android photo capture:
    // http://labs.makemachine.net/2010/03/simple-android-photo-capture/

    protected void startCameraActivity() {
        File file = new File(_path);
        Uri outputFileUri = Uri.fromFile(file);

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.i(TAG, "resultCode: " + resultCode);

        if (resultCode == -1) {
            onPhotoTaken();
        } else {
            Log.v(TAG, "User cancelled");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SimpleAndroidOCRActivity.PHOTO_TAKEN, _taken);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");
        if (savedInstanceState.getBoolean(SimpleAndroidOCRActivity.PHOTO_TAKEN)) {
            onPhotoTaken();
        }
    }

    protected void onPhotoTaken() {
        _taken = true;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

        try {
            ExifInterface exif = new ExifInterface(_path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            Log.v(TAG, "Orient: " + exifOrientation);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            Log.v(TAG, "Rotation: " + rotate);

            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }

            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }

        // _image.setImageBitmap( bitmap );

        Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

        // You now have the text in recognizedText var, you can do anything with it.
        // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
        // so that garbage doesn't make it to the display.

        Log.v(TAG, "OCRED TEXT: " + recognizedText);

        if (lang.equalsIgnoreCase("eng")) {
            recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
        }

        recognizedText = recognizedText.trim();

        if (recognizedText.length() != 0) {
            _field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
            _field.setSelection(_field.getText().toString().length());
        }

        // Cycle done.
    }
}
