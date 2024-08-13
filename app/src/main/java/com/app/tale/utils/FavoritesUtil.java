package com.app.tale.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class FavoritesUtil {

    private static final String PREFS_NAME = "favorites_prefs";
    private static final String KEY_FAVORITES = "favorites";

    /**
     * Adds a video path to the favorites list.
     *
     * @param context  The context to use for accessing SharedPreferences.
     * @param videoPath The path of the video to be added to the favorites list.
     */
    @SuppressLint("MutatingSharedPrefs")
    public static void addFavorite(Context context, String videoPath) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet(KEY_FAVORITES, new HashSet<>());

        favorites.add(videoPath);  // Add the video path to the favorites list

        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();  // Save the updated list
    }

    /**
     * Removes a video path from the favorites list.
     *
     * @param context  The context to use for accessing SharedPreferences.
     * @param videoPath The path of the video to be removed from the favorites list.
     */
    @SuppressLint("MutatingSharedPrefs")
    public static void removeFavorite(Context context, String videoPath) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> favorites = prefs.getStringSet(KEY_FAVORITES, new HashSet<>());

        favorites.remove(videoPath);  // Remove the video path from the favorites list

        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();  // Save the updated list
    }

    /**
     * Retrieves the list of favorite video paths.
     *
     * @param context The context to use for accessing SharedPreferences.
     * @return A set containing the paths of favorite videos.
     */
    public static Set<String> getFavorites(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_FAVORITES, new HashSet<>());
    }

    /**
     * Checks if a video path is in the favorites list.
     *
     * @param context  The context to use for accessing SharedPreferences.
     * @param videoPath The path of the video to check.
     * @return True if the video path is in the favorites list, false otherwise.
     */
    public static boolean isFavorite(Context context, String videoPath) {
        Set<String> favorites = getFavorites(context);
        return favorites.contains(videoPath);
    }
}
