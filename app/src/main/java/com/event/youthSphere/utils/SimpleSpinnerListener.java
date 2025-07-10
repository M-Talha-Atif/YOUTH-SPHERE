package com.event.youthSphere.utils;


import android.view.View;
import android.widget.AdapterView;

public class SimpleSpinnerListener implements AdapterView.OnItemSelectedListener {

    private final Runnable onItemSelected;

    public SimpleSpinnerListener(Runnable onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        onItemSelected.run();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }
}
