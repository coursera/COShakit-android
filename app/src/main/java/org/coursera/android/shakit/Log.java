package org.coursera.android.shakit;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Queue;

public class Log {
    public final static int MAXIMUM_LOGS = 1500;
    private static Queue<String> mLogs = new LinkedList<String>();
    private final static String NewLine = System.getProperty("line.separator");
    private static boolean isDebugMode = false;
    private static boolean setupCalled = false;
    private static UserInfo userInfo;

    public static void setup(Context context) {
        isDebugMode = (0 != (context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
        setupCalled = true;
    }

    public static void setUserInfo(UserInfo userInfo) {
        Log.userInfo = userInfo;
    }

    private static void log(String tag, String message) {
        if (message == null) {
            return;
        }

        synchronized (mLogs) {
            if (mLogs.size() == MAXIMUM_LOGS) {
                mLogs.remove();
            }

            mLogs.add(tag + ": " + message);
        }
    }

    public static void e(String tag, String message) {
        tag = "(ERROR) " + tag;

        log(tag, message);

        android.util.Log.e(tag, message);
    }

    public static String getStaceTraceAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        if (t != null) {
            t.printStackTrace(pw);
        }
        return sw.toString();
    }

    public static void e(String tag, String message, Throwable t) {
        tag = "(ERROR) " + tag;
        StringBuilder sb = new StringBuilder();
        sb.append(message + NewLine);
        sb.append(getStaceTraceAsString(t) + NewLine);
        log(tag, sb.toString());
        android.util.Log.e(tag, message, t);
    }


    public static void i(String tag, String message) {
        tag = "(INFO) " + tag;

        log(tag, message);

        android.util.Log.i(tag, message);
    }


    public static void w(String tag, String message) {
        tag = "(WARNING) " + tag;

        log(tag, message);

        android.util.Log.w(tag, message);
    }

    public static void d(String tag, String message) {
        if (!setupCalled) {
            w(Log.class.getSimpleName(), "Calling Log.d(tag, message) before calling "
                    + "Log.setup(context) will never generate a log message.");
        }

        if (isDebugMode) {
            tag = "(DEBUG) " + tag;

            log(tag, message);

            android.util.Log.d(tag, message);
        }
    }

    private static String getLog() {
        String logString = "";

        synchronized (mLogs) {
            int i = mLogs.size();
            for (String log: mLogs) {
                logString += log + NewLine;
            }
            logString += getDeviceInfo();

            if (userInfo != null) {
                String userInfoString = userInfo.getUserInfo();
                if (userInfoString != null) {
                    logString += userInfoString;
                }
            }
        }

        return logString;
    }

    private static String getDeviceInfo() {
        String deviceInfo = NewLine;
        deviceInfo += "(DEVICE INFO) " + getDeviceName() + NewLine;
        deviceInfo += "(SDK API) " + Build.VERSION.SDK_INT + " - " + Build.VERSION.RELEASE;

        return deviceInfo;
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER.toUpperCase();
        String model = Build.MODEL.toUpperCase();
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static Uri getLogUri() {
        String data = getLog();

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/cour_logs_tmp");
        myDir.mkdirs();
        String fname = "courlog.txt";
        File file = new File (myDir, fname);

        if (file.exists ()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(data.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Uri.fromFile(file);
    }

    public interface UserInfo {
        String getUserInfo();
    }
}