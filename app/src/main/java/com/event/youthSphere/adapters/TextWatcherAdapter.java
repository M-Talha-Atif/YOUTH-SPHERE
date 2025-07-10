package com.event.youthSphere.adapters;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

public class TextWatcherAdapter implements TextWatcher {

    private final Runnable onTextChanged;
    private final Handler handler = new Handler();
    private final int debounceDelay = 400; // ms

    public TextWatcherAdapter(Runnable onTextChanged) {
        this.onTextChanged = onTextChanged;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        handler.removeCallbacksAndMessages(null); // clear previous
        handler.postDelayed(onTextChanged, debounceDelay);
    }

    @Override
    public void afterTextChanged(Editable s) { }
}
