package com.app.tale.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.ImageView;

public class IconColorUtil {

    /**
     * Applies a color tint to an ImageView based on its state (focused or pressed).
     *
     * @param imageView  The ImageView to which the tint will be applied.
     * @param normalColorHex  The color hex string for the normal state (e.g., "#FFFFBB").
     * @param pressedColorHex The color hex string for the pressed state (e.g., "#FFFFFF").
     */
    public static void applyIconTint(ImageView imageView, String normalColorHex, String pressedColorHex) {
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_focused}, // Normal state
                new int[] {android.R.attr.state_pressed}   // Pressed state
        };

        int[] colors = new int[] {
                Color.parseColor(normalColorHex),
                Color.parseColor(pressedColorHex)
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);
        imageView.setImageTintList(colorStateList);
    }
}
