package com.rafhaanshah.studyassistant.utils;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.MainActivity;
import com.rafhaanshah.studyassistant.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class HelperUtils {

    private final static String LECTURE_DIRECTORY = "lectures";

    private HelperUtils() {
    }

    public static ArrayList<File> getLectureFiles(Context context) {
        return new ArrayList<>(Arrays.asList(getLectureDirectory(context).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(MainActivity.PDF);
            }
        })));
    }

    public static File getLectureDirectory(Context context) {
        return new File(context.getFilesDir().getAbsolutePath() + File.separator + LECTURE_DIRECTORY);
    }

    public static int getColour(Context context, int position) {
        int colour = 0;
        switch (position % 12) {
            case 0:
                colour = ContextCompat.getColor(context, R.color.materialRed);
                break;
            case 1:
                colour = ContextCompat.getColor(context, R.color.materialDeepOrange);
                break;
            case 2:
                colour = ContextCompat.getColor(context, R.color.materialOrange);
                break;

            case 3:
                colour = ContextCompat.getColor(context, R.color.materialPurple);
                break;
            case 4:
                colour = ContextCompat.getColor(context, R.color.materialDeepPurple);
                break;
            case 5:
                colour = ContextCompat.getColor(context, R.color.materialIndigo);
                break;

            case 6:
                colour = ContextCompat.getColor(context, R.color.materialBlue);
                break;
            case 7:
                colour = ContextCompat.getColor(context, R.color.materialLightBlue);
                break;
            case 8:
                colour = ContextCompat.getColor(context, R.color.materialCyan);
                break;

            case 9:
                colour = ContextCompat.getColor(context, R.color.materialTeal);
                break;
            case 10:
                colour = ContextCompat.getColor(context, R.color.materialGreen);
                break;
            case 11:
                colour = ContextCompat.getColor(context, R.color.materialLightGreen);
                break;
        }
        return colour;
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

    public static void setDrawableColour(Drawable drawable, int colour) {
        if (drawable != null)
            drawable.setColorFilter(new PorterDuffColorFilter(colour, PorterDuff.Mode.SRC_IN));
    }

    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showSoftKeyboard(final Context context, View view) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.toggleSoftInput(0, 0);
            }
        }, 100);
    }

    public static void rotateView(View view, int duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ROTATION, 360);
        animator.setDuration(duration);
        animator.start();
    }

    public static void fadeOutView(final View view, final int duration) {
        final AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(duration);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

        });
        view.startAnimation(fadeOut);
    }

    public static void fadeInView(final View view, final int duration) {
        final AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(duration);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

        });
        view.startAnimation(fadeIn);
    }

    public static void moveViewY(View view, int distance, int duration) {
        ObjectAnimator objectanimator = ObjectAnimator.ofFloat(view, View.Y, distance);
        objectanimator.setDuration(duration);
        objectanimator.start();
    }

    public static void moveViewX(View view, int distance, int duration) {
        ObjectAnimator objectanimator = ObjectAnimator.ofFloat(view, View.X, distance);
        objectanimator.setDuration(duration);
        objectanimator.start();
    }

    public static void fadeImageChange(final ImageView imageView, final Drawable drawable, final int duration) {
        final AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(duration);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.setBackground(drawable);
                final AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(duration);
                imageView.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

        });
        imageView.startAnimation(fadeOut);
    }

    public static void fadeTextChange(final String text, final TextView textView, final int duration) {
        final AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(duration);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setText(text);
                final AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(duration);
                textView.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

        });
        textView.startAnimation(fadeOut);
    }

    public static void fadeColourChange(Context context, final View view, final int colour, final int duration) {
        ColorDrawable viewBackground = (ColorDrawable) view.getBackground();
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), viewBackground.getColor(), colour);
        colorAnimation.setDuration(duration);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    public static void fadeStatusBarColourChange(final Window window, final int colour, final int duration) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), window.getStatusBarColor(), colour);
        colorAnimation.setDuration(duration);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                window.setStatusBarColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    public static void scrollToTop(Context context, RecyclerView recyclerView) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(context) {
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 50f / displayMetrics.densityDpi;
            }
        };
        linearSmoothScroller.setTargetPosition(0);
        recyclerView.getLayoutManager().startSmoothScroll(linearSmoothScroller);
    }
}