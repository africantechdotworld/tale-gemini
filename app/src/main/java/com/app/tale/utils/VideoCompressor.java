package com.app.tale.utils;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

public class VideoCompressor {

    public static void compressVideo(String inputPath, String outputPath, VideoCompressionListener listener) {
        String[] command = {
                "-y", // Overwrite output files
                "-i", inputPath, // Input file path
                "-s", "1280x720", // Output resolution
                "-r", "30", // Frame rate
                "-b:v", "1M", // Video bitrate
                "-b:a", "128k", // Audio bitrate
                "-vcodec", "libx264", // Video codec
                "-acodec", "aac", // Audio codec
                outputPath // Output file path
        };

        FFmpeg.executeAsync(command, (executionId, returnCode) -> {
            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                listener.onSuccess(outputPath);
            } else {
                listener.onFailure("Compression failed with return code: " + returnCode);
            }
        });
    }

    public interface VideoCompressionListener {
        void onSuccess(String outputPath);
        void onFailure(String errorMessage);
    }
}

