package org.coursera.android.example;

import android.os.Bundle;

import org.coursera.android.shakit.Log;
import org.coursera.android.shakit.ShakitManager;
import org.coursera.android.shakit.ShakeableActivity;

public class MainActivity extends ShakeableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // All settings should be done before super.onCreate when subclassing ShakeableActivity

        // This tells Log if you are in debug mode.
        Log.setup(this);

        // This customizes the strings shown throughout the Shakit process
        ShakitManager.configure(getString(R.string.shakit_generating_log_file),
                getString(R.string.shakit_alert_dialog_title),
                getString(R.string.shakit_alert_dialog_message),
                getString(R.string.shakit_alert_dialog_positive_message),
                getString(R.string.shakit_alert_dialog_neutral_message),
                getString(R.string.shakit_alert_dialog_negative_message),
                getString(R.string.shakit_email_chooser_title),
                getString(R.string.shakit_email_subject),
                getString(R.string.shakit_email_body_prefix),
                null, null, null);

        // When false, this
        ShakitManager.setAllowsPermanentDisabling(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(MainActivity.class.getSimpleName(), "Main activity has been created!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(MainActivity.class.getSimpleName(), "Main activity has been resumed!");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(MainActivity.class.getSimpleName(), "Main activity has been paused!");
    }
}