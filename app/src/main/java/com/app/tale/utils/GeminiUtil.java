package com.app.tale.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.vertexai.FirebaseVertexAI;
import com.google.firebase.vertexai.GenerativeModel;
import com.google.firebase.vertexai.java.GenerativeModelFutures;
import com.google.firebase.vertexai.type.Content;
import com.google.firebase.vertexai.type.GenerateContentResponse;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class GeminiUtil {

    private static final String TAG = "GeminiUtil";
    private final String projectId;
    private final String location;
    private final String modelName;
    private static GenerativeModel generativeModel;
    private final Context context;

    // Constructor to initialize with project-specific details
    public GeminiUtil(Context context, String projectId, String location, String modelName) {
        this.projectId = projectId;
        this.location = location;
        this.modelName = modelName;
        this.context = context;
        initializeVertexAI();
    }

    // Initialize the VertexAI client with Firebase
    private void initializeVertexAI() {
        // Initialize Firebase
        FirebaseApp.initializeApp(context);

        // Initialize the Vertex AI service and the generative model
        generativeModel = FirebaseVertexAI.getInstance().generativeModel(modelName);
        Log.d(TAG, "VertexAI initialized successfully with modelName: " + modelName);
    }

    // Public method to analyze a video and return the response as a string
    public void analyzeVideo(Uri videoUri, String prompt, final Callback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Convert Uri to InputStream directly using ContentResolver
                ContentResolver resolver = context.getContentResolver();
                InputStream inputStream = resolver.openInputStream(videoUri);
                if (inputStream == null) {
                    throw new IOException("Failed to open InputStream for URI: " + videoUri);
                }

                // Read the video bytes
                byte[] videoBytes = new byte[inputStream.available()];
                inputStream.read(videoBytes);
                inputStream.close();

                Content content = new Content.Builder()
                        .addBlob("video/mp4", videoBytes)
                        .addText(prompt)
                        .build();

                Publisher<GenerateContentResponse> streamingResponse = GenerativeModelFutures.from(generativeModel).generateContentStream(content);

                final StringBuilder fullResponse = new StringBuilder();

                streamingResponse.subscribe(new Subscriber<GenerateContentResponse>() {
                    @Override
                    public void onNext(GenerateContentResponse generateContentResponse) {
                        String chunk = generateContentResponse.getText();
                        fullResponse.append(chunk);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "Response: " + fullResponse.toString());
                        callback.onSuccess(fullResponse.toString());
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "Error: " + t.getMessage(), t);
                        callback.onError(t);
                    }

                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "Error while analyzing video: " + e.getMessage(), e);
                callback.onError(e);
            }
        });
    }


    public interface Callback {
        void onSuccess(String response);
        void onError(Throwable t);
    }
}
