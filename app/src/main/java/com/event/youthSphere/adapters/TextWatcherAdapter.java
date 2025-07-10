package com.event.youthSphere.adapters;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

/**
 * A debounced TextWatcher implementation that delays text change events
 * to prevent rapid consecutive calls during user input.
 * <p>
 * Useful for search fields or other inputs where you want to wait until
 * the user has finished typing before processing the input.
 */
public class TextWatcherAdapter implements TextWatcher {

    /**
     * The Runnable to execute when text changes are detected
     */
    private final Runnable onTextChanged;

    /**
     * Handler for managing delayed execution of text change events
     */
    private final Handler handler = new Handler();

    /**
     * The delay (in milliseconds) before processing text changes
     */
    private final int debounceDelay = 400; // ms

    /**
     * Constructs a new TextWatcherAdapter
     *
     * @param onTextChanged The Runnable to execute after text changes
     *                      and the debounce delay has elapsed
     */
    public TextWatcherAdapter(Runnable onTextChanged) {
        this.onTextChanged = onTextChanged;
    }

    /**
     * Called before the text is changed
     *
     * @param s     The text before change
     * @param start The start position of the changed text
     * @param count The number of characters to be replaced
     * @param after The length of the new text replacing the old text
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Intentionally left blank
    }

    /**
     * Called when the text is being changed
     * <p>
     * Implements debounce logic by delaying the execution of onTextChanged
     * and canceling previous pending executions.
     *
     * @param s      The text after change
     * @param start  The start position of the changed text
     * @param before The length of text before change
     * @param count  The number of characters in the changed text
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        handler.removeCallbacksAndMessages(null); // Clear previous pending executions
        handler.postDelayed(onTextChanged, debounceDelay);
    }

    /**
     * Called after the text has been changed
     *
     * @param s The editable text after change
     */
    @Override
    public void afterTextChanged(Editable s) {
        // Intentionally left blank
    }
}