package org.coursera.android.shakit;

import android.app.Activity;
import android.os.Bundle;

public abstract class ShakeableActivity extends Activity {
    private ShakitManager shakitManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shakitManager = new ShakitManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        shakitManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        shakitManager.onPause();
    }
}