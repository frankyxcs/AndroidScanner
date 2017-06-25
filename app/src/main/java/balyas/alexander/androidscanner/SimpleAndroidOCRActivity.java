package balyas.alexander.androidscanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class SimpleAndroidOCRActivity extends AppCompatActivity implements View.OnClickListener, Callback<Translate> {
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";

    public static final int REQUEST_PERMISSION_CAMERA = 101;
    public static final int REQUEST_PERMISSION_EXTERNAL_STORAGE = 102;


    public static String lang = "eng";
    public static String[] langs = {"eng", "rus", "ukr", "fra", "spa"};
    public static String[] langs_2 = {"en", "ru", "uk", "fr", "es"};
    public static String translate = "ru";

    private static final String TAG = "SimpleAndroidOCR.java";

    protected Button _button;
    private Button bTrans;
    private String fromTr;
    // protected ImageView _image;
    protected EditText _field;
    protected EditText _field2;
    protected String _path;
    protected boolean _taken;

    private ConstraintLayout constraintLayout;

    private TextView mainText;
    private ImageButton btEng;
    private ImageButton btRus;
    private ImageButton btUkr;
    private ImageButton btFra;
    private ImageButton btEsp;

    private TextView transText;
    private ImageButton btEngT;
    private ImageButton btRusT;
    private ImageButton btUkrT;
    private ImageButton btFraT;
    private ImageButton btEspT;

    private String recognitionText;

    protected static final String PHOTO_TAKEN = "photo_taken";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        constraintLayout = (ConstraintLayout) findViewById(R.id.cl_main);

        mainText = (TextView) findViewById(R.id.main_text);
        btEng = (ImageButton) findViewById(R.id.bt_eng);
        btRus = (ImageButton) findViewById(R.id.bt_rus);
        btUkr = (ImageButton) findViewById(R.id.bt_ukr);
        btFra = (ImageButton) findViewById(R.id.bt_fra);
        btEsp = (ImageButton) findViewById(R.id.bt_esp);

        transText = (TextView) findViewById(R.id.translate_text);
        btEngT = (ImageButton) findViewById(R.id.bt_eng_tran);
        btRusT = (ImageButton) findViewById(R.id.bt_rus_tran);
        btUkrT = (ImageButton) findViewById(R.id.bt_ukr_tran);
        btFraT = (ImageButton) findViewById(R.id.bt_fra_tran);
        btEspT = (ImageButton) findViewById(R.id.bt_esp_tran);

        String[] paths = new String[]{DATA_PATH, DATA_PATH + "tessdata/"};

        if (hasPermissions()) {

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

        } else {
            requestPermissionWithRationale();
        }

        // _image = (ImageView) findViewById(R.id.image);
        _field = (EditText) findViewById(R.id.field);
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
                mainText.setText(R.string.recognition_eng);
                _button.setEnabled(true);
                lang = "eng";
                fromTr = "en";
                break;
            case R.id.bt_rus:
                mainText.setText(R.string.recognition_rus);
                _button.setEnabled(true);
                lang = "rus";
                fromTr = "ru";
                break;
            case R.id.bt_ukr:
                mainText.setText(R.string.recognition_ukr);
                _button.setEnabled(true);
                lang = "ukr";
                fromTr = "uk";
                break;
            case R.id.bt_fra:
                mainText.setText(R.string.recognition_france);
                _button.setEnabled(true);
                lang = "fra";
                fromTr = "fr";
                break;
            case R.id.bt_esp:
                mainText.setText(R.string.recognition_spa);
                _button.setEnabled(true);
                lang = "esp";
                fromTr = "es";
                break;
            case R.id.bt_eng_tran:
                transText.setText(R.string.translation_eng);
                bTrans.setEnabled(true);
                translate = "en";
                break;
            case R.id.bt_rus_tran:
                transText.setText(R.string.translation_rus);
                bTrans.setEnabled(true);
                translate = "ru";
                break;
            case R.id.bt_ukr_tran:
                transText.setText(R.string.translation_ukr);
                bTrans.setEnabled(true);
                translate = "uk";
                break;
            case R.id.bt_fra_tran:
                transText.setText(R.string.translation_fra);
                bTrans.setEnabled(true);
                translate = "fr";
                break;
            case R.id.bt_esp_tran:
                transText.setText(R.string.translation_spa);
                bTrans.setEnabled(true);
                translate = "es";
                break;
        }
    }


    public class ButtonClickHandler implements View.OnClickListener {
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button:
                    Log.v(TAG, "Starting Camera app");
                    if (hasPermissions()) {
                        startCameraActivity();
                    } else {
                        requestPermissionWithRationale();
                    }
                    break;
                case R.id.bt_get_trans:
                    getTranslate();
            }

        }
    }

    private boolean hasPermissions() {
        int res = 0;
        //string array of permissions,
        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, REQUEST_PERMISSION_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode) {
            case REQUEST_PERMISSION_EXTERNAL_STORAGE:

                for (int res : grantResults) {
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }

                break;

            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }

        if (allowed) {
            //user granted all permissions we can perform our task.
            //create();
        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();

                } else {
                    showNoStoragePermissionSnackbar();
                }
            }
        }

    }


    /**
     * use this method if user choose 'never show again' in dialog
     */
    public void showNoStoragePermissionSnackbar() {
        Snackbar.make(SimpleAndroidOCRActivity.this.findViewById(R.id.cl_main), R.string.permission_isnt_granted, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openApplicationSettings();

                        Toast.makeText(getApplicationContext(),
                                R.string.open_permissions,
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                })
                .show();
    }

    /**
     * Intent to show needed permission in app's settings
     */
    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, REQUEST_PERMISSION_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PERMISSION_CAMERA || requestCode == REQUEST_PERMISSION_EXTERNAL_STORAGE) {
            return;
        }
        if (resultCode == -1) {

            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    recognitionText = onPhotoTaken();
                    _field.setText(recognitionText);
                }
            };

            handler.sendEmptyMessage(0);

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    onPhotoTaken();
//                }
//            });

//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                   recognitionText = onPhotoTaken();
//                }
//            }).start();
//                _field.setText(recognitionText);
        } else {
            Log.v(TAG, "User cancelled");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * use this method if user doesn't grant permissions what he need
     */
    public void requestPermissionWithRationale() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CALENDAR)) {
            final String message = getString(R.string.camera_permission_text);
            Snackbar.make(SimpleAndroidOCRActivity.this.findViewById(R.id.cl_main), message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.grant, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPerms();
                        }
                    })
                    .show();
        } else {
            requestPerms();
        }
    }

    private void getTranslate() {
//        String BASE_URL = "https://glosbe.com/gapi/";
//        String firstPart = _field.getText().toString();
//        if (firstPart.length() > 1) {
//            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            OkHttpClient client2 = new OkHttpClient.Builder().addInterceptor(interceptor).build();
//
//            Gson gson = new GsonBuilder()
//                    .setLenient()
//                    .create();
//
//            Retrofit client = new Retrofit.Builder()
//                    .baseUrl(BASE_URL)
//                    .client(client2)
//                    .addConverterFactory(GsonConverterFactory.create(gson))
//                    .build();
//            TranslateAPI transAPI = client.create(TranslateAPI.class);
//
//
//                Call<Translate> callTrans = transAPI.getTranslate(fromTr, translate, firstPart, "json", "true");
//                callTrans.enqueue(this);
//
//        }

        String text = _field.getText().toString();
        String url = "https://translate.google.com/#" + fromTr + "/" + translate + "/" + text;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public void onResponse(final Call<Translate> call, final Response<Translate> response) {
        if (response.isSuccessful()) {
            String text = response.body().getTuc().get(0).getPhrase().getText();

            _field2.setText(text);

        } else {
            Toast.makeText(SimpleAndroidOCRActivity.this, "Error code " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(final Call<Translate> call, final Throwable t) {

    }


    protected void startCameraActivity() {
        File file = new File(_path);
        Uri outputFileUri = Uri.fromFile(file);

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(intent, 0);
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

    protected String onPhotoTaken() {
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

//        if (recognizedText.length() != 0) {
//            _field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
//            _field.setSelection(_field.getText().toString().length());
//        }

        // Cycle done.
        return recognizedText;
    }
}
