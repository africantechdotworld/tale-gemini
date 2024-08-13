package com.app.tale.adapters;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.app.tale.HomeActivity.KEY_VIDEO_PATHS;
import static com.app.tale.HomeActivity.PREFS_NAME;
import static com.app.tale.utils.IconColorUtil.applyIconTint;
import static com.app.tale.utils.FileUtil.shareVideo;
import static com.app.tale.utils.SharedPreferencesUtil.saveDescriptionsToPreferences;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.app.tale.R;
import com.app.tale.databinding.ItemVideoBinding;
import com.app.tale.fragments.CreatePlaylistDialog;
import com.app.tale.utils.FavoritesUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.app.tale.utils.FirebaseUtil;
import com.app.tale.utils.GeminiUtil;
import com.app.tale.utils.PlaylistUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import android.widget.ListView;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<Map<String, Object>> videoDataList;
    private final boolean showThumbnails;
    private Context context;
    private final Map<String, Integer> playbackPositions = new HashMap<>(); // Store playback positions
    private static final String TAG = "VideoAdapter";
    private Map<String, String> videoDescriptions = new HashMap<>();
    private int clickedPos;
    private Gson gson = new Gson();

    public VideoAdapter(List<Map<String, Object>> videoDataList, boolean showThumbnails) {
        this.videoDataList = videoDataList;
        this.showThumbnails = showThumbnails;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ItemVideoBinding binding = ItemVideoBinding.inflate(LayoutInflater.from(context), parent, false);
        return new VideoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Map<String, Object> videoData = videoDataList.get(position);
        String video = (String) videoData.get("videoPath");
        String description = (String) videoData.get("description");
        holder.bind(video, description, position, context);
    }

    @Override
    public int getItemCount() {
        return videoDataList.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VideoViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // Resume the video from the last known position when re-attached
        Map<String, Object> videoData = videoDataList.get(holder.getAdapterPosition());
        String videoPath = (String) videoData.get("videoPaths");
        Integer lastPlaybackPosition = playbackPositions.get(videoPath);
        if (lastPlaybackPosition != null) {
            holder.binding.videoView.seekTo(lastPlaybackPosition);
        }
        holder.binding.videoView.start();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        // Save the current position when the view is detached (e.g., scrolled out of view)
        Map<String, Object> videoData = videoDataList.get(holder.getAdapterPosition());
        String videoPath = (String) videoData.get("videoPaths");
        playbackPositions.put(videoPath, holder.binding.videoView.getCurrentPosition());
        holder.binding.videoView.pause();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        ItemVideoBinding binding;

        public VideoViewHolder(@NonNull ItemVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String videoPath, String description, int pos, Context context) {
            // video logic
            binding.videoView.setVideoURI(Uri.parse(videoPath));

            if (showThumbnails) {
                try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                    retriever.setDataSource(videoPath);
                    Bitmap bitmap = retriever.getFrameAtTime();
                    binding.videoView.setBackground(new BitmapDrawable(context.getResources(), bitmap));
                    try {
                        retriever.release();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to release MediaMetadataRetriever", e);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed to release MediaMetadataRetriever", e);
                }
            } else {
                binding.videoView.setBackground(null);
            }

            binding.videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                Map<String, Object> videoData = videoDataList.get(pos);
                ///String videoPathKey = videoPath;
                Integer lastPlaybackPosition = playbackPositions.get(videoPath);
                if (lastPlaybackPosition != null) {
                    binding.videoView.seekTo(lastPlaybackPosition);
                }
                binding.playPause.setVisibility(View.GONE);
                binding.videoView.start();
            });

            binding.videoView.setOnInfoListener((mp, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    //String videoPathKey = videoPaths.get(getAdapterPosition());
                    playbackPositions.put(videoPath, binding.videoView.getCurrentPosition());
                }
                return false;
            });

            // UI CODE

            if (!description.equals(""))  {
                binding.caption.setText(description);
            } else {
                binding.caption.setText("Use the Gemini powered magic button at the right middle to generate a description for your text");
            }

            applyIconTint(binding.playPause, "#FFFFBB", "#FFFFFF");
            applyIconTint(binding.geminiIcon, "#FFFFFF", "#FFFFFF");

            // Check if the video is a favorite and set the icon color
            if (FavoritesUtil.isFavorite(context, videoPath)) {
                binding.likeIcon.setColorFilter(Color.RED);
            } else {
                binding.likeIcon.setColorFilter(Color.WHITE);
            }

            // Check if a particular video is added to playlist
            Set<String> playlists = PlaylistUtil.getPlaylistsContainingVideo(context, videoPath);
            if (!playlists.isEmpty()) {
                binding.saveIcon.setColorFilter(Color.BLACK);
            } else {
                binding.saveIcon.setColorFilter(Color.WHITE);
            }

            // ONCLICK LOGIC

            binding.caption.setOnLongClickListener(view -> {
                // Get the ClipboardManager instance
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

                // Get the text from the TextView
                String textToCopy = binding.caption.getText().toString();

                // Create a ClipData object with the text
                ClipData clip = ClipData.newPlainText("copied_text", textToCopy);

                // Set the ClipData to the ClipboardManager
                clipboard.setPrimaryClip(clip);

                // Optionally, show a toast to inform the user that the text has been copied
                Toast.makeText(context, "Description copied to clipboard", Toast.LENGTH_SHORT).show();

                // Return true to indicate that the long click event has been handled
                return true;
            });

            binding.avatar.setOnClickListener(view -> {
                Toast.makeText(context, "Tale - Ai Video Player", Toast.LENGTH_SHORT).show();
            });


            // pause or play video when clicked
            binding.container.setOnClickListener(view -> {
                if (binding.videoView.isPlaying()) {
                    binding.videoView.pause();
                    binding.playPause.setVisibility(View.VISIBLE);
                } else {
                    binding.videoView.start();
                    binding.playPause.setVisibility(View.GONE);
                }
            });

            // Set click listener for the favorite button
            binding.likeIcon.setOnClickListener(v -> {
                if (FavoritesUtil.isFavorite(context, videoPath)) {
                    FavoritesUtil.removeFavorite(context, videoPath);
                    binding.likeIcon.setColorFilter(Color.WHITE);
                    Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                } else {
                    FavoritesUtil.addFavorite(context, videoPath);
                    binding.likeIcon.setColorFilter(Color.RED);
                    Toast.makeText(context, "Added to Favorites", Toast.LENGTH_SHORT).show();
                }
            });

            // gemini ai button
            binding.geminiIcon.setOnClickListener(view -> {
                //String vidPath = videoPaths.get(getAdapterPosition());
                clickedPos = pos;
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();

                if (user == null) {
                    // User is not signed in, sign in anonymously
                    Toast.makeText(context, "Signing in...", Toast.LENGTH_SHORT).show();
                    mAuth.signInAnonymously()
                            .addOnCompleteListener((Activity) context, task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success, proceed with uploading video
                                    Toast.makeText(context, "Signed in successful...", Toast.LENGTH_SHORT).show();
                                    FirebaseUser signedInUser = mAuth.getCurrentUser();
                                    handleGeminiMagicButtonClick(view.getContext(), videoPath, pos);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("FirebaseAuth", "signInAnonymously:failure", task.getException());
                                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // User is already signed in, proceed with uploading video
                    Toast.makeText(context, "Uploading Video...", Toast.LENGTH_SHORT).show();
                    handleGeminiMagicButtonClick(view.getContext(), videoPath, pos);
                }
            });

            // share button
            binding.shareIcon.setOnClickListener(view -> {
                shareVideo(context, videoPath);
            });

            // save button
            binding.saveIcon.setOnClickListener(view -> {
                showBottomSheetDialog(videoPath);
            });
        }

        private void showBottomSheetDialog(String videoPath) {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
            View bottomSheetView = LayoutInflater.from(context).inflate(
                    R.layout.bottom_sheet_playlist,
                    itemView.findViewById(R.id.bottom_sheet_container)
            );

            ListView playlistListView = bottomSheetView.findViewById(R.id.playlist_list_view);
            Button createNewPlaylistButton = bottomSheetView.findViewById(R.id.create_new_playlist_button);

            // Get existing playlists using PlaylistUtil
            List<String> existingPlaylists = PlaylistUtil.getAllPlaylists(context);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, existingPlaylists);
            playlistListView.setAdapter(adapter);

            // Add video to an existing playlist
            playlistListView.setOnItemClickListener((parent, view, position, id) -> {
                String selectedPlaylist = existingPlaylists.get(position);
                PlaylistUtil.addToPlaylist(context, selectedPlaylist, videoPath);
                bottomSheetDialog.dismiss();
            });

            // Create a new playlist and add the video to it
            createNewPlaylistButton.setOnClickListener(v -> {
                showCreatePlaylistDialog(videoPath);
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        }

        private void showCreatePlaylistDialog(String videoPath) {
            CreatePlaylistDialog dialog = new CreatePlaylistDialog();
            dialog.setOnPlaylistCreatedListener(playlistName -> {
                PlaylistUtil.createPlaylistAndAddVideo(context, playlistName, videoPath);
            });
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "CreatePlaylistDialog");
        }

        private void handleGeminiMagicButtonClick(Context context, String videoPath, int pos) {
            // Check if the description is already stored
            if (videoDescriptions.containsKey(videoPath)) {
                String description = videoDescriptions.get(videoPath);
                // Use the stored description
                showDescription(description, pos);
            } else {
                // Extract audio and upload in the background
                analyzeVideo(context, videoPath, pos);
            }
        }

        public Uri getUriForFile(Context context, String filePath) {

            File file = new File(filePath);
            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        }

        private void analyzeVideo(Context context, String videoPath, int pos) {
            GeminiUtil geminiUtil = new GeminiUtil(context, "aqueous-entry-430618-v5", "us-central1", "gemini-1.5-flash-001");

            Uri videoUri = getUriForFile(context, videoPath);
            String prompt = "Describe the content of this video.";

            geminiUtil.analyzeVideo(videoUri, prompt, new GeminiUtil.Callback() {
                @Override
                public void onSuccess(String response) {
                    // Update UI on the main thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        videoDescriptions.put(videoPath, response);
                        saveDescriptionsToPreferences(context, videoDescriptions);
                        showDescription(response, pos);
                    });
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, "Failed to analyze video.", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Failed to analyze video.");
                    });
                }
            });
        }


        private void updateDescription(int position, String newDescription) {
            if (videoDataList != null && position < videoDataList.size()) {
                Map<String, Object> videoData = videoDataList.get(position);
                videoData.put("description", newDescription); // Update the description
                // Optionally, save the updated data back to SharedPreferences
                storeVideoData(videoDataList);
            }
        }


        private void showDescription(String description, int pos) {
            // Example usage:
            updateDescription(pos, description);
            notifyItemChanged(pos);

        }

        private void storeVideoData(List<Map<String, Object>> videoData) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(videoData); // Convert List<Map<String, Object>> to JSON
            editor.putString(KEY_VIDEO_PATHS, json); // Store JSON string in SharedPreferences
            editor.apply();
        }

    }
}
