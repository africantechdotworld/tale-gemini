package com.app.tale;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.tale.databinding.ActivitySetupBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetupActivity extends AppCompatActivity {

    private ActivitySetupBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "SetupActivity";
    private static final String PREFS_NAME = "VideoPrefs";
    private static final String KEY_VIDEO_PATHS = "videoPaths";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        } else {
            List<Map<String, Object>> videoDataList = getStoredVideoData();
            if (videoDataList == null || videoDataList.isEmpty()) {
                loadVideos();
            } else {
                navigateToHome();
            }
        }

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadVideos();
        } else {
            // Handle the case where permission is not granted
            Toast.makeText(SetupActivity.this, "Permission needed to continue", Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private List<Map<String, Object>> getStoredVideoData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(KEY_VIDEO_PATHS, ""); // Retrieve JSON stringgetString(KEY_VIDEO_PATHS, null);
        Gson gson = new Gson();
        Type type = new TypeToken<List<Map<String, Object>>>() {}.getType(); // Define the expected type
        return json == null ? new ArrayList<>() : gson.fromJson(json, type); // Deserialize JSON to List<Map<String, Object>>
    }


    private void storeVideoData(List<Map<String, Object>> videoData) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(videoData); // Convert List<Map<String, Object>> to JSON
        editor.putString(KEY_VIDEO_PATHS, json); // Store JSON string in SharedPreferences
        editor.apply();
    }

    private void loadVideos() {
        executorService.execute(() -> {
            List<Map<String, Object>> videoDataList = new ArrayList<>();
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA};
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

            if (cursor != null) {
                int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

                while (cursor.moveToNext()) {
                    String filePath = cursor.getString(dataIndex);
                    Log.d(TAG, "Processing file: " + filePath);
                    try {
                        retriever.setDataSource(filePath);
                        int width = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
                        int height = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));

                        if (height > width) {
                            Map<String, Object> videoMap = new HashMap<>();
                            videoMap.put("videoPath", filePath);
                            videoMap.put("description", ""); // Default description value
                            videoDataList.add(videoMap);
                        }
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Failed to process file: " + filePath, e);
                    }
                }
                cursor.close();
            }

            try {
                retriever.release();
            } catch (IOException | RuntimeException e) {
                Log.e(TAG, "Failed to release MediaMetadataRetriever", e);
            }

            if (!videoDataList.isEmpty()) {
                storeVideoData(videoDataList);
                runOnUiThread(this::navigateToHome);
            } else {
                Toast.makeText(this, "No portrait video found!", Toast.LENGTH_SHORT).show();
                finish();
            }

        });
    }
}