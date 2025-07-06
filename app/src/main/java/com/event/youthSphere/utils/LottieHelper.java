package com.event.youthSphere.utils;

import android.view.View;
import android.widget.FrameLayout;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.event.youthSphere.R;

public class LottieHelper {
    private static final int LOADING_ANIMATION_RES = R.raw.loading;

    public static void setupLoadingAnimation(LottieAnimationView animationView) {
        // Use simple raw resource loading (removed caching complexity)
        animationView.setAnimation(LOADING_ANIMATION_RES);
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        animationView.setVisibility(View.GONE);

        // Performance optimizations
        animationView.setCacheComposition(true); // Enable composition caching
        animationView.enableMergePathsForKitKatAndAbove(true); // Enable merge paths if needed
        animationView.setSpeed(1.5f); // Slightly increase speed if appropriate
    }

    public static void showLoading(LottieAnimationView animationView, FrameLayout overlay) {
        overlay.setVisibility(View.VISIBLE); // Show overlay first
        animationView.setVisibility(View.VISIBLE);
        if (!animationView.isAnimating()) {
            animationView.playAnimation();
        }
    }

    public static void hideLoading(LottieAnimationView animationView, FrameLayout overlay) {
        animationView.pauseAnimation();
        animationView.setVisibility(View.GONE);
        overlay.setVisibility(View.GONE);
    }
}