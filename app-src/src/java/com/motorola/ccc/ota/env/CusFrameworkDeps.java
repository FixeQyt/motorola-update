package com.motorola.ccc.ota.env;

import android.provider.Settings;
import com.motorola.ccc.ota.utils.Logger;
import java.io.File;
import java.lang.reflect.InvocationTargetException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public final class CusFrameworkDeps {
    private static final String FILE_UTILS_CLASS = "android.os.FileUtils";
    private static final String READ_TEXT_FILE_METHOD_NAME = "readTextFile";
    public static final int STATUS_FILE_ERROR = 492;
    public static final int STATUS_UNKNOWN_ERROR = 491;
    private static String USER_SETUP_COMPLETE = "USER_SETUP_COMPLETE";

    public static boolean isStatusClientError(int i) {
        return i >= 400 && i < 500;
    }

    public static boolean isStatusError(int i) {
        return i >= 400 && i < 600;
    }

    public static boolean isStatusServerError(int i) {
        return i >= 500 && i < 600;
    }

    public static String getUserSetupCompleteAsString() {
        try {
            return (String) Settings.Secure.class.getField(USER_SETUP_COMPLETE).get(null);
        } catch (IllegalAccessException e) {
            Logger.error("OtaApp", "Unable to access " + USER_SETUP_COMPLETE + " exception " + e.getMessage());
            return null;
        } catch (NoSuchFieldException e2) {
            Logger.error("OtaApp", "No field by name " + USER_SETUP_COMPLETE + " exception " + e2.getMessage());
            return null;
        }
    }

    public static String readTextFile(File file, int i, String str) {
        try {
            return (String) Class.forName(FILE_UTILS_CLASS).getMethod(READ_TEXT_FILE_METHOD_NAME, File.class, Integer.TYPE, String.class).invoke(null, file, Integer.valueOf(i), str);
        } catch (InvocationTargetException e) {
            Logger.error("OtaApp", "failed to read text file " + e.getTargetException());
            return null;
        } catch (Exception e2) {
            Logger.error("OtaApp", "failed to read text file " + e2);
            return null;
        }
    }
}
