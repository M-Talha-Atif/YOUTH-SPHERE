package com.event.youthSphere;

import android.app.Application;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieListener;

public class MyApplication extends Application {
    private static LottieComposition cachedLottieComposition;

    @Override
    public void onCreate() {
        super.onCreate();
        preloadLottieAnimation();
    }

    private void preloadLottieAnimation() {
        LottieCompositionFactory.fromRawRes(this, R.raw.loading)
                .addListener(new LottieListener<LottieComposition>() {
                    @Override
                    public void onResult(LottieComposition composition) {
                        cachedLottieComposition = composition;
                    }
                });
    }

    public static LottieComposition getCachedLottieComposition() {
        return cachedLottieComposition;
    }
}