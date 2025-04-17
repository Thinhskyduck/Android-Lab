package com.example.lab6_bai4;

import android.view.View;

public abstract class DoubleClickListener implements View.OnClickListener {
    private long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v);
        }
        lastClickTime = clickTime;
    }

    public abstract void onDoubleClick(View v);

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
}
