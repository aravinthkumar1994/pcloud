package com.example.cas63.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cas63.firebase.FireBaseUpload;
import com.example.cas63.sessionmanger.SessionManager;
import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.AuthorizationActivity;
import com.pcloud.sdk.AuthorizationResult;
import com.pcloud.sdk.Call;
import com.pcloud.sdk.DataSink;
import com.pcloud.sdk.DataSource;
import com.pcloud.sdk.PCloudSdk;
import com.pcloud.sdk.ProgressListener;
import com.pcloud.sdk.RemoteEntry;
import com.pcloud.sdk.RemoteFile;
import com.pcloud.sdk.RemoteFolder;
import com.pcloud.sdk.UserInfo;
import com.pcloud.sdk.internal.RealRemoteFolder;
import com.timqi.sectorprogressview.ColorfulRingProgressView;

import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MyProgressListener {
    ApiClient apiClient;
    private static final int PCLOUD_AUTHORIZATION_REQUEST_CODE = 123;
    private static final int FOLDERPICKER_CODE = 12;
    String AccessTocken, SessionAccessTocken;
    ImageView thumnail_image;
    public int counter;
    TextView loading_txt, download_txt;
    static ColorfulRingProgressView spv;
    SessionManager sessionManager;
    Context context;
    ProgressBar progressbar;
    private String download_url = "loading", Quota_ifo = "";
    int x = 1;
    String a = "";
    Call<RemoteFolder> newFolder;
    RemoteFile bigFile;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {

            String folderLocations = data.getExtras().getString("data");
            List<String> mPaths = (List<String>) data.getSerializableExtra(VideoPicker.EXTRA_VIDEO_PATH);
            Log.e("folderLocation", String.valueOf(mPaths));
            final String mPath = String.valueOf(mPaths).replace("[", "").replace("]", "");
            Log.e("folderLocation", mPath);
            if (folderLocations != null)
                Log.e("folderLocations", folderLocations);
            try {
                File localFile = new File(mPath);
                Glide.with(context).load(localFile)
                        .into(thumnail_image);
            } catch (Exception e) {
                Log.e("LoadImage: ", String.valueOf(e));
            }
            runaftertwo(mPath);


        } else if (requestCode == PCLOUD_AUTHORIZATION_REQUEST_CODE) {
            AuthorizationResult result = (AuthorizationResult) data.getSerializableExtra(AuthorizationActivity.KEY_AUTHORIZATION_RESULT);
            if (result == AuthorizationResult.ACCESS_GRANTED) {
                String accessToken = data.getStringExtra(AuthorizationActivity.KEY_ACCESS_TOKEN);
                long userId = data.getLongExtra(AuthorizationActivity.KEY_USER_ID, 0);
                AccessTocken = accessToken;
                Log.e("Access token", accessToken);
                sessionManager.setAccessTocken(AccessTocken);
                SessionAccessTocken = sessionManager.getAccessTocken();
                Toast.makeText(getApplicationContext(), SessionAccessTocken, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "Access Token Generated", Toast.LENGTH_SHORT).show();
                System.out.println("SessionAccessTocken   " + SessionAccessTocken);
                //TODO: Do what's needed :)
            } else {
                //TODO: Add proper handling for denied grants or errors.
            }
        }
    }

    private void runaftertwo(final String mPath) {
        progressbar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "started", Toast.LENGTH_SHORT).show();
                new LongOperation().execute(mPath);
            }
        }, 1000);
    }

    public void download(View view) {


        try {
            spv.setPercent(0);
            downloadFile(bigFile, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ApiError apiError) {
            apiError.printStackTrace();
        }

    }

    public void firebase(View view) {
        Intent intent = new Intent(MainActivity.this, FireBaseUpload.class);
        startActivity(intent);
    }


    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String path = params[0];
            apiClient = PCloudSdk.newClientBuilder()
                    .authenticator(Authenticators.newOAuthAuthenticator(SessionAccessTocken))
                    .create();
            try {
                UserInfo userInfo = apiClient.getUserInfo().execute();
                newFolder = apiClient.createFolder(RemoteFolder.ROOT_FOLDER_ID, "newfolder");
                // newFolder.execute();
                bigFile = uploadFile(apiClient, new File(path));
                System.out.println(bigFile.createFileLink());
                download_url = bigFile.createFileLink().toString();
                //    loading_txt.setText(bigFile.createFileLink().toString());
                Quota_ifo = userInfo.email() + " " + userInfo.totalQuota() + " " + userInfo.usedQuota();
                System.out.format(" User email: %s | Total quota %s | Used quota %s ", userInfo.email(), userInfo.totalQuota(), userInfo.usedQuota());

            } catch (IOException | ApiError e) {
                e.printStackTrace();
                apiClient.shutdown();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            loading_txt.setText(download_url);
            download_txt.setText(Quota_ifo);
            download_txt.setVisibility(View.VISIBLE);
//            if(!loading_txt.getText().toString().equalsIgnoreCase("loading"))
//                download_txt.setVisibility(View.VISIBLE);
//                download_txt.setText("Download Now");
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initilize();


    }

    private void initilize() {
        loading_txt = (TextView) findViewById(R.id.loading_txt);
        download_txt = (TextView) findViewById(R.id.download_txt);

        thumnail_image = (ImageView) findViewById(R.id.thumnail_image);
        context = MainActivity.this;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        spv = (ColorfulRingProgressView) findViewById(R.id.spv);
        StrictMode.setThreadPolicy(policy);
        sessionManager = new SessionManager(MainActivity.this);

        SessionAccessTocken = sessionManager.getAccessTocken();
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        System.out.format("SessionAccessTocken on create" + SessionAccessTocken);
        //android O fix bug orientation
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void upload(View view) {
        new VideoPicker.Builder(MainActivity.this)
                .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
                .enableDebuggingMode(true)
                .build();
    }

    public void login(View view) {

        //  Lj3hWIDpP58
        Intent authIntent = AuthorizationActivity.createIntent(MainActivity.this, "atJYLO3iGwH");
        startActivityForResult(authIntent, PCLOUD_AUTHORIZATION_REQUEST_CODE);
    }

    private RemoteFile uploadFile(ApiClient apiClient, File file) throws IOException, ApiError {

        return apiClient.createFile(RemoteFolder.ROOT_FOLDER_ID, file.getName(), DataSource.create(file), new Date(file.lastModified()), new ProgressListener() {
            public void onProgress(final long done, final long total) {
                System.out.format("\rUplddoading... %.2f\n", ((double) done / (double) total) * 100d);
                double a = ((double) done / (double) total) * 100d;

                final int decimal_removed = (int) a;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spv.setPercent(decimal_removed);
                    }
                });


                if (((double) done / (double) total) * 100d == 100) {

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            // Stuff that updates the UI
                            progressbar.setVisibility(View.GONE);

                        }
                    });

                }
            }
        }).execute();


    }


    @Override
    public void progress(final String progress) {
        Log.d("progress", "progress: " + progress);
        double d = Double.parseDouble(progress);
        x = (int) d;
        System.out.println("integer problem" + x);
//         loading_txt.setText(x);
        //new MyTask().execute(x);


    }

    private File downloadFile(RemoteFile remoteFile, File folder) throws IOException, ApiError {
        File destination = new File(folder, remoteFile.name());
        remoteFile.download(DataSink.create(destination), new ProgressListener() {
            public void onProgress(long done, long total) {

                System.out.format("\rDownloading... %.1f\n", ((double) done / (double) total) * 100d);

                double a = ((double) done / (double) total) * 100d;

                final int decimal_removed = (int) a;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spv.setPercent(decimal_removed);
                    }
                });

            }
        });
        return destination;
    }
}
