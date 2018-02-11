package com.rafhaanshah.studyassistant;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class HelperUtils {

    private HelperUtils() {
    }

    public static int darkenColor(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = darken(red, fraction);
        green = darken(green, fraction);
        blue = darken(blue, fraction);
        int alpha = Color.alpha(color);

        return Color.argb(alpha, red, green, blue);
    }

    private static int darken(int color, double fraction) {
        return (int) Math.max(color - (color * fraction), 0);
    }

    public static void hideSoftKeyboard(Context context, View view) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (NullPointerException E) {
            // Not a huge problem if keyboard is not hidden
        }
    }

    public static void showSoftKeyboard(Context context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } catch (NullPointerException E) {
            // Not a huge problem if keyboard is not automatically shown
        }
    }

    public static ArrayList<File> getLectureFiles(Context context) {
        return new ArrayList<>(Arrays.asList(getLectureDirectory(context).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pdf");
            }
        })));
    }

    public static File getLectureDirectory(Context context) {
        return new File(context.getFilesDir().getAbsolutePath() + File.separator + "lectures");
    }

    public static void rotateView(View item) {
        ObjectAnimator animator = ObjectAnimator
                .ofFloat(item, "rotation", 360);
        animator.start();
    }

    public static void fadeOutView(View view) {
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(250);
        view.startAnimation(fadeOut);
    }

    public static void fadeInView(View view) {
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(250);
        view.startAnimation(fadeIn);
    }

    public static void animateTitleChange(final Context context, final String newTitle, final Toolbar toolbar) {
        View v = null;
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View child = toolbar.getChildAt(i);
            if (child instanceof TextView) {
                v = child;
            }
        }
        final View view = v;
        if (view != null) {
            final AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
            fadeOut.setDuration(context.getResources().getInteger(R.integer.animation_fade_time));
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    toolbar.setTitle(newTitle);

                    AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                    fadeIn.setDuration(context.getResources().getInteger(R.integer.animation_fade_time));
                    view.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

            });
            view.startAnimation(fadeOut);
        }
    }
}
