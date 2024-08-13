package com.app.tale.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SharedPreferencesUtil {

    private static final String PREFS_NAME = "gemini_prefs";
    private static final String KEY_VIDEO_DESCRIPTIONS = "video_descriptions";

    public static void saveDescriptionsToPreferences(Context context, Map<String, String> descriptions) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(descriptions);
        editor.putString(KEY_VIDEO_DESCRIPTIONS, json);
        editor.apply();
    }

    public static Map<String, String> getDescriptionsFromPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(KEY_VIDEO_DESCRIPTIONS, null);
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
