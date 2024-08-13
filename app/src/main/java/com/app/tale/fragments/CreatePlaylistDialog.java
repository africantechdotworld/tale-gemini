package com.app.tale.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.app.tale.R;

public class CreatePlaylistDialog extends DialogFragment {

    private EditText playlistNameEditText;
    private Button createButton;
    private Button cancelButton;

    private OnPlaylistCreatedListener listener;

    public interface OnPlaylistCreatedListener {
        void onPlaylistCreated(String playlistName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_playlist, container, false);
        playlistNameEditText = view.findViewById(R.id.playlist_name_input);
        createButton = view.findViewById(R.id.create_button);
        cancelButton = view.findViewById(R.id.cancel_button);


        createButton.setOnClickListener(v -> {
            String playlistName = playlistNameEditText.getText().toString().trim();
            if (!playlistName.isEmpty()) {
                if (listener != null) {
                    listener.onPlaylistCreated(playlistName);
                }
                dismiss();
            } else {
                Toast.makeText(getContext(), "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(view1 -> {
            dismiss();
        });

        return view;
    }

    public void setOnPlaylistCreatedListener(OnPlaylistCreatedListener listener) {
        this.listener = listener;
    }
}
