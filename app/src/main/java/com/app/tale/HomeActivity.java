package com.app.tale;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.tale.adapters.VideoAdapter;
import com.app.tale.databinding.ActivityHomeBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.io.IOException;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.recyclerview.widget.SnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private static final String TAG = "HomeActivity";
    public static final String PREFS_NAME = "VideoPrefs";
    public static final String KEY_VIDEO_PATHS = "videoPaths";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Gson gson = new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<Map<String, Object>> videoDataList = getStoredVideoData();
        if (videoDataList == null || videoDataList.isEmpty()) {
            loadVideos();
        } else {
            setUpRecyclerView(videoDataList);
        }



        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private List<Map<String, Object>> getStoredVideoData() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(KEY_VIDEO_PATHS, ""); // Retrieve JSON string
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

            storeVideoData(videoDataList);

            runOnUiThread(() -> setUpRecyclerView(videoDataList));
        });
    }


    private void setUpRecyclerView(List<Map<String, Object>> videoDataList) {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        boolean showThumbnails = false;
        binding.recyclerView.setAdapter(new VideoAdapter(videoDataList, showThumbnails));

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.recyclerView);
    }

}