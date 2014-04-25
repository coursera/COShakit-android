package org.coursera.android.shakit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;

import com.squareup.seismic.ShakeDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ShakitManager implements ShakeDetector.Listener {
    private static final String RAGE_SHAKE_ENABLED = "rage_shake_enabled";
    private static final String SHARED_PREFERENCES_NAME = "rage_shake_shared_preferences";

    private AlertDialog mShakeAlertDialog;
    private boolean mShakeAlertDialogShowing = false;
    private SensorManager mSensorManager;
    private ShakeDetector mShakeDetector;
    private Activity mActivity;

    private static boolean ALLOWS_PERMANENT_DISABLING = true;

    private static String GENERATING_LOG_MESSAGE = "Generating log files...";
    private static String SHAKE_DETECTED_DIALOG_TITLE = "Is something wrong?";
    private static String SHAKE_DETECTED_DIALOG_MESSAGE = "It seems you are shaking you device, would you like to send feedback?";
    private static String STOP_SHOWING_DIALOG = "Don't show this";
    private static String NO_STRING = "No";
    private static String YES_STRING = "Yes";
    private static String SEND_EMAIL_CHOOSER_TITLE = "Send Email";
    private static String EMAIL_SUBJECT = "Application Feedback";
    private static String EMAIL_BODY_PREFIX = null;
    private static String[] EMAIL_TO_ADDRESSES = null;
    private static String[] EMAIL_CC_ADDRESSES = null;
    private static String[] EMAIL_BCC_ADDRESSES = null;

    /** SETTINGS **/

    /**
     * Configures the text to display throughout the entire rage shake dialog
     * process.
     *
     * @param generatingLogMessage      The message to showing in a progress dialog while
     *                                  generating and saving the log file to disk.
     * @param alertDialogMessage        The title of the AlertDialog asking the user if they
     *                                  want to send feedback after shaking their device.
     * @param alertDialogMessage        The message of the AlertDialog.
     * @param alertDialogYes            The text to show on the button in the AlertDialog
     *                                  that sends feedback.
     * @param alertDialogNo             The text to show on the button in the AlertDialog
     *                                  that closes the dialog without sending feedback.
     * @param alertDialogStopShowing    The text to show on the button in the AlertDialog
     *                                  that closes the dialog without sending feedback and
     *                                  prevents the dialog from ever showing again.
     * @param sendEmailChooserTitle     The text to show as the title of the Chooser to send
     *                                  the email.
     * @param emailSubject              The subject of the email.
     * @param emailBodyPrefix           The prefix of the body of the email.
     * @param emailToRecipients         The email addresses that will go in the to field.
     * @param emailCcRecipients         The email addresses that will go in the cc field.
     * @param emailBccRecipients         The email addresses that will go in the bcc field.
     */
    public static void configure(String generatingLogMessage, String alertDialogTitle, String alertDialogMessage,
                                 String alertDialogYes, String alertDialogNo, String alertDialogStopShowing,
                                 String sendEmailChooserTitle, String emailSubject, String emailBodyPrefix,
                                 String[] emailToRecipients, String[] emailCcRecipients, String[] emailBccRecipients) {
        GENERATING_LOG_MESSAGE = generatingLogMessage;
        SHAKE_DETECTED_DIALOG_TITLE = alertDialogTitle;
        SHAKE_DETECTED_DIALOG_MESSAGE = alertDialogMessage;
        YES_STRING = alertDialogYes;
        NO_STRING = alertDialogNo;
        STOP_SHOWING_DIALOG = alertDialogStopShowing;
        SEND_EMAIL_CHOOSER_TITLE = sendEmailChooserTitle;
        EMAIL_SUBJECT = emailSubject;
        EMAIL_TO_ADDRESSES = emailToRecipients;
        EMAIL_CC_ADDRESSES = emailCcRecipients;
        EMAIL_BCC_ADDRESSES = emailBccRecipients;
        EMAIL_BODY_PREFIX = emailBodyPrefix;
    }

    /**
     * Configures the text to display throughout the entire rage shake dialog
     * process.
     *
     * @param allowsPermanentDisabling  True means the AlertDialog will show an option
     *                                  to permanently disable shake alerts for this device.
     *                                  False will not show this option.
     */
    public static void setAllowsPermanentDisabling(boolean allowsPermanentDisabling) {
        ALLOWS_PERMANENT_DISABLING = allowsPermanentDisabling;
    }
    /** END SETTINGS **/

    public ShakitManager(Activity activity) {
        mActivity = activity;

        if (isRageShakeEnabled()) {
            mSensorManager = (SensorManager) mActivity.getSystemService(mActivity.SENSOR_SERVICE);
            mShakeDetector = new ShakeDetector(this);

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                    .setCancelable(false)
                    .setTitle(SHAKE_DETECTED_DIALOG_TITLE)
                    .setMessage(SHAKE_DETECTED_DIALOG_MESSAGE)
                    .setNeutralButton(NO_STRING, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mShakeAlertDialogShowing = false;
                        }
                    })
                    .setPositiveButton(YES_STRING, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            sendRageShake();
                            mShakeAlertDialogShowing = false;
                        }
                    });

            if (ALLOWS_PERMANENT_DISABLING) {
                builder.setNegativeButton(STOP_SHOWING_DIALOG, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(ShakeableActivity.class.getName(), "Shake disabled.");
                        getSharedPreferences()
                                .edit()
                                .putBoolean(RAGE_SHAKE_ENABLED, false)
                                .commit();
                        mShakeDetector.stop();
                    }
                });
            }

            mShakeAlertDialog = builder.create();
        }
    }

    private SharedPreferences getSharedPreferences() {
        return mActivity.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private boolean isRageShakeEnabled() {
        return getSharedPreferences().getBoolean(RAGE_SHAKE_ENABLED, true);
    }

    public void onResume() {
        if (isRageShakeEnabled()) {
            mShakeDetector.start(mSensorManager);
        }
    }

    public void onPause() {
        if (isRageShakeEnabled()) {
            mShakeDetector.stop();
        }
    }

    @Override
    public void hearShake() {
        if (!mShakeAlertDialogShowing) {
            Log.i(ShakitManager.class.getSimpleName(), "Shake detected.");
            mShakeAlertDialogShowing = true;
            mShakeAlertDialog.show();
        }
    }

    private Uri takeScreenshot() {
        View rootView = mActivity.findViewById(android.R.id.content).getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap bm = rootView.getDrawingCache();

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/rage_shake");
        myDir.mkdirs();
        String fname = "screenshot.jpg";
        File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            return Uri.fromFile(file);
        } catch (Exception e) {
            return null;
        }
    }

    private void sendRageShake() {
        if (hasWriteExternalPermission()) {
            final ProgressDialog progressDialog = new ProgressDialog(mActivity);
            progressDialog.setMessage(GENERATING_LOG_MESSAGE);
            progressDialog.setCancelable(false);
            progressDialog.show();

            AsyncTask asyncTask = new AsyncTask() {
                @Override
                protected Uri doInBackground(Object[] objects) {
                    return Log.getLogUri();
                }

                @Override
                protected void onPostExecute(Object o) {
                    progressDialog.hide();

                    Uri logUri = (Uri) o;
                    Uri imageUri = takeScreenshot();

                    ArrayList<Uri> attachmentUris = new ArrayList<Uri>();

                    if (logUri != null) {
                        attachmentUris.add(logUri);
                    }

                    if (imageUri != null) {
                        attachmentUris.add(imageUri);
                    }

                    sendEmailWithAttachments(attachmentUris);
                }
            };

            asyncTask.execute();
        } else {
            sendEmailWithAttachments(null);
        }
    }

    private void sendEmailWithAttachments(ArrayList<Uri> attachmentUris) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, EMAIL_TO_ADDRESSES);
        emailIntent.putExtra(Intent.EXTRA_CC, EMAIL_CC_ADDRESSES);
        emailIntent.putExtra(Intent.EXTRA_BCC, EMAIL_BCC_ADDRESSES);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
        emailIntent.putExtra(Intent.EXTRA_TEXT, EMAIL_BODY_PREFIX);

        if (attachmentUris != null) {
            emailIntent.putExtra(Intent.EXTRA_STREAM, attachmentUris);
        }

        Intent chooser = Intent.createChooser(emailIntent, SEND_EMAIL_CHOOSER_TITLE);
        mActivity.startActivity(chooser);
    }

    private boolean hasWriteExternalPermission()
    {
        return mActivity.checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE")
                == PackageManager.PERMISSION_GRANTED;
    }
}