package com.app.tale.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlaylistUtil {

    private static final String PLAYLIST_PREFS = "Playlists";
    private static final String PLAYLIST_SET_KEY_PREFIX = "playlist_";

    // Add a video to an existing playlist
    public static void addToPlaylist(Context context, String playlistName, String videoPath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAYLIST_PREFS, Context.MODE_PRIVATE);
        Set<String> playlist = sharedPreferences.getStringSet(PLAYLIST_SET_KEY_PREFIX + playlistName, new HashSet<>());

        if (playlist != null) {
            playlist.add(videoPath);
            sharedPreferences.edit().putStringSet(PLAYLIST_SET_KEY_PREFIX + playlistName, playlist).apply();
            Toast.makeText(context, "Added to " + playlistName, Toast.LENGTH_SHORT).show();
        }
    }

    // Create a new playlist and add the video to it
    public static void createPlaylistAndAddVideo(Context context, String playlistName, String videoPath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAYLIST_PREFS, Context.MODE_PRIVATE);
        Set<String> newPlaylist = new HashSet<>();
        newPlaylist.add(videoPath);
        sharedPreferences.edit().putStringSet(PLAYLIST_SET_KEY_PREFIX + playlistName, newPlaylist).apply();
        Toast.makeText(context, "Playlist created and video added", Toast.LENGTH_SHORT).show();
    }

    // Get a list of all playlist names
    public static List<String> getAllPlaylists(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAYLIST_PREFS, Context.MODE_PRIVATE);
        List<String> playlistNames = new ArrayList<>();
        for (String key : sharedPreferences.getAll().keySet()) {
            if (key.startsWith(PLAYLIST_SET_KEY_PREFIX)) {
                playlistNames.add(key.replace(PLAYLIST_SET_KEY_PREFIX, ""));
            }
        }
        return playlistNames;
    }

    // Get videos in a specific playlist
    public static List<String> getPlaylistVideos(Context context, String playlistName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAYLIST_PREFS, Context.MODE_PRIVATE);
        Set<String> playlist = sharedPreferences.getStringSet(PLAYLIST_SET_KEY_PREFIX + playlistName, new HashSet<>());
        return playlist != null ? new ArrayList<>(playlist) : new ArrayList<>();
    }

    // Remove video from playlist
    public static void removeVideoFromPlaylist(Context context, String playlistName, String videoPath) {
        SharedPreferences prefs = context.getSharedPreferences(PLAYLIST_PREFS, Context.MODE_PRIVATE);
        Set<String> playlist = prefs.getStringSet(PLAYLIST_SET_KEY_PREFIX + playlistName, new HashSet<>());
        if (playlist != null && playlist.contains(videoPath)) {
            playlist.remove(videoPath);
            prefs.edit().putStringSet(PLAYLIST_SET_KEY_PREFIX + playlistName, playlist).apply();
        }
    }

    // Delete playlist entirely
    public static void deletePlaylist(Context context, String playlistName) {
        SharedPreferences prefs = context.getSharedPreferences(PLAYLIST_PREFS, Context.MODE_PRIVATE);
        prefs.edit().remove(PLAYLIST_SET_KEY_PREFIX + playlistName).apply();
    }

    // Get all playlists containing a particular video
    public static Set<String> getPlaylistsContainingVideo(Context context, String videoPath) {
        SharedPreferences prefs = context.getSharedPreferences(PLAYLIST_PREFS, Context.MODE_PRIVATE);
        Set<String> playlistsContainingVideo = new HashSet<>();

        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(PLAYLIST_SET_KEY_PREFIX)) {
                Set<String> playlist = prefs.getStringSet(key, new HashSet<>());
                if (playlist != null && playlist.contains(videoPath)) {
                    playlistsContainingVideo.add(key.replace(PLAYLIST_SET_KEY_PREFIX, ""));
                }
            }
        }
        return playlistsContainingVideo;
    }
}
