package com.motorola.otalib.downloadservice.utils;

import android.content.Context;
import android.util.Log;
import com.motorola.otalib.common.utils.BuildPropertyUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DownloadServiceLogger {
    public static String BASE_TAG = "DownloadService";
    public static String TAG = "DownloadService";

    public static void d(String str, String str2) {
        Log.d(str, str2);
    }

    public static void e(String str, String str2) {
        Log.e(str, str2);
    }

    public static void i(String str, String str2) {
        Log.i(str, str2);
    }

    public static void w(String str, String str2) {
        Log.w(str, str2);
    }

    public static void v(String str, String str2) {
        if (Log.isLoggable(str, 2) || BuildPropertyUtils.isDogfoodDevice() || !BuildPropertyUtils.isSecure()) {
            Log.v(str, str2);
        }
    }

    public static void saveContext(Context context, String str) {
        TAG = str + BASE_TAG;
    }
}
