package org.coursera.android.shakit;

import android.os.Bundle;

public class ShakeableFragmentActivity extends android.support.v4.app.FragmentActivity {
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