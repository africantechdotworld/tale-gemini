package com.app.tale.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;

public class FileUtil {

    /**
     * Method to share a video file via external apps.
     *
     * @param context   The context to use for creating the intent and accessing resources.
     * @param videoPath The path of the video to be shared.
     */
    public static void shareVideo(Context context, String videoPath) {
        File videoFile = new File(videoPath);

        if (videoFile.exists()) {
            Uri videoUri = FileProvider.getUriForFile(
                    context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    videoFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("video/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share Video"));
            } catch (Exception e) {
                Toast.makeText(context, "Unable to share video", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Video file does not exist", Toast.LENGTH_SHORT).show();
        }
    }
}
