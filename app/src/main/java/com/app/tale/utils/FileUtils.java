package com.app.tale.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FileUtils {

    public static String createTempVideoFilePath(Context context) {
        File cacheDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (cacheDir != null && !cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return new File(cacheDir, "temp_compressed_video.mp4").getAbsolutePath();
    }
}

