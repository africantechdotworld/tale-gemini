package com.app.tale.utils;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class FirebaseUtil {

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception exception);

        void onError(Exception e);
    }

    public static void uploadVideoToFirebase(String videoPath, UploadCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to the video file you want to upload
        Uri file = Uri.fromFile(new File(videoPath));
        StorageReference videoRef = storageRef.child("videos/" + file.getLastPathSegment());

        // Upload the file
        UploadTask uploadTask = videoRef.putFile(file);

        // Listen for when the upload is done or if it fails
        uploadTask.addOnFailureListener(exception -> {
            // Handle unsuccessful uploads
            Log.e("Firebase", "Upload failed", exception);
            if (callback != null) {
                callback.onFailure(exception);
            }
        }).addOnSuccessListener(taskSnapshot -> {
            // Get the download URL
            videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                Log.d("Firebase", "Upload successful. Download URL: " + downloadUrl);
                if (callback != null) {
                    callback.onSuccess(downloadUrl);
                }
            }).addOnFailureListener(exception -> {
                Log.e("Firebase", "Failed to get download URL", exception);
                if (callback != null) {
                    callback.onFailure(exception);
                }
            });
        });
    }
}
