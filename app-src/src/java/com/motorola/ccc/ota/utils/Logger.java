package com.motorola.ccc.ota.utils;

import android.util.Log;
import com.motorola.otalib.common.utils.BuildPropertyUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class Logger {
    public static final String OTAAPP_TAG = "OtaApp";
    public static final String THINK_SHIELD_TAG = "ThinkShieldOta";

    public static void debug(String str, String str2) {
        Log.d(str, str2);
    }

    public static void error(String str, String str2) {
        Log.e(str, str2);
    }

    public static void info(String str, String str2) {
        Log.i(str, str2);
    }

    public static void warn(String str, String str2) {
        Log.w(str, str2);
    }

    public static void verbose(String str, String str2) {
        if (Log.isLoggable("OtaApp", 2) || BuildPropertyUtils.isDogfoodDevice()) {
            Log.v(str, str2);
        }
    }
}
