package com.app.tale.utils;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import android.media.MediaCodec;


public class VideoUtil {

    public static File extractAudioFromVideo(Context context, String videoPath) throws IOException {
        MediaExtractor mediaExtractor = new MediaExtractor();
        mediaExtractor.setDataSource(videoPath);

        int audioTrackIndex = -1;
        MediaFormat format = null;

        // Find the audio track in the video
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            format = mediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                audioTrackIndex = i;
                break;
            }
        }

        if (audioTrackIndex == -1) {
            throw new IOException("No audio track found in video file.");
        }

        mediaExtractor.selectTrack(audioTrackIndex);

        // Create a new file to store the extracted audio
        File audioFile = new File(context.getCacheDir(), "extracted_audio.aac");
        MediaMuxer mediaMuxer = new MediaMuxer(audioFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        int newTrackIndex = mediaMuxer.addTrack(format);

        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        mediaMuxer.start();

        while (true) {
            int sampleSize = mediaExtractor.readSampleData(buffer, 0);
            if (sampleSize < 0) {
                break;
            }

            info.offset = 0;
            info.size = sampleSize;
            info.presentationTimeUs = mediaExtractor.getSampleTime();

            int sampleFlags = mediaExtractor.getSampleFlags();
            int bufferFlags = 0;

            if ((sampleFlags & MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                bufferFlags |= MediaCodec.BUFFER_FLAG_SYNC_FRAME;
            }
            if ((sampleFlags & MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) != 0) {
                bufferFlags |= MediaCodec.BUFFER_FLAG_PARTIAL_FRAME;
            }

            info.flags = bufferFlags;


            mediaMuxer.writeSampleData(newTrackIndex, buffer, info);
            mediaExtractor.advance();
        }

        mediaMuxer.stop();
        mediaMuxer.release();

        mediaExtractor.release();

        return audioFile;
    }
}
