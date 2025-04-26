package com.motorola.otalib.main;

import android.content.Context;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.main.Settings.LibConfigs;
import com.motorola.otalib.main.Settings.LibSettings;
import java.io.File;
import java.net.URL;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class PublicUtilityMethods {
    private static final String ASSETS_DIRECTORY_PATH = "Assets";
    public static final int DEFAULT_MAX_RETRY_COUNT_DOWNLOAD_PACKAGE = 3;
    public static int ERROR_INVALID_REQUEST = 205;
    public static int ERROR_INVALID_RESPONSE = 206;
    public static int ERROR_RETRY = 202;
    public static int ERROR_VERSION_MISMATCH = 204;
    public static int NO_NETWORK = 201;
    private static final String OTA_DIRECTORY_PATH = "OTA";
    public static int SUCCESS = 200;
    public static int SUSPENDED = 203;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum OtaState {
        UpdateAvailable,
        WaitingForDLPermission,
        UserApprovedDL,
        QueueForDownload,
        FetchingDLDetails,
        Downloading,
        WaitingForInstallPermission,
        UserApprovedInstall,
        Installing,
        Rebooting,
        Result
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum STATUS_CODE {
        ERROR_LOW_BATTERY,
        ERROR_DEVICE_OUT_OF_RANGE,
        ERROR_DEVICE_DISCONNECTED,
        ERROR_CORRUPT_FIRMWARE,
        ERROR_CASE_CLOSED,
        ERROR_USER_CANCELLED,
        ERROR_VERSION_MISMATCH,
        ERROR_OTHER,
        SUCCESS
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum TRIGGER_BY {
        polling,
        user,
        pairing,
        setup
    }

    public static String SHA1Generator(String str) {
        return BuildPropertyUtils.generateSHA1(str);
    }

    public static void createOtaDirectory(Context context) {
        File file = new File(context.getFilesDir(), "OTA");
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(context.getFilesDir(), ASSETS_DIRECTORY_PATH);
        if (file2.exists()) {
            return;
        }
        file2.mkdirs();
    }

    public static String getFileName(Context context, String str, long j) {
        String str2 = context.getFilesDir() + ("/OTA/" + str.replace(SystemUpdateStatusUtils.SPACE, "_") + "_" + j + FileUtils.EXT);
        Logger.debug(Logger.OTALib_TAG, "File name " + str2);
        return str2;
    }

    public static boolean checkForUrlExpiry(LibSettings libSettings) {
        return System.currentTimeMillis() - libSettings.getLong(LibConfigs.DOWNLOAD_DESCRIPTOR_TIME, System.currentTimeMillis()) >= CusAndroidUtils.URL_EXPIRY_TIME;
    }

    public static String getConfigFilePath(Context context, String str) {
        String str2 = context.getFilesDir() + ("/Assets/" + getFileNameFromPath(str));
        Logger.debug(Logger.OTALib_TAG, "File name " + str2);
        return str2;
    }

    public static String getFileNameFromPath(String str) {
        String str2 = "";
        try {
            String file = new URL(str).getFile();
            str2 = file.substring(file.lastIndexOf(47) + 1);
            Logger.debug(Logger.OTALib_TAG, "Filename: " + str2);
            return str2;
        } catch (Exception e) {
            e.printStackTrace();
            return str2;
        }
    }

    public static String getMFPBaseUrl() {
        return "https://d2xbblc68nqw6k.cloudfront.net/";
    }

    public static void cleanUpCurrentPackage(Context context, String str) {
        File[] listFiles;
        Logger.debug(Logger.OTALib_TAG, "FileUtils.cleanUpCurrentPackage, current package filename: " + str);
        File file = new File(context.getFilesDir(), "OTA");
        if (file.exists()) {
            try {
                for (File file2 : file.listFiles()) {
                    Logger.debug(Logger.OTALib_TAG, "FileUtils.cleanUpCurrentPackage, File list " + file2.getName());
                    if (str.contains(file2.getName())) {
                        Logger.debug(Logger.OTALib_TAG, "FileUtils.cleanUpCurrentPackage, deleted " + file2.getName());
                        file2.delete();
                        return;
                    }
                }
            } catch (Exception e) {
                Logger.error(Logger.OTALib_TAG, "FileUtils.cleanUpCurrentPackage, exception : " + e);
            }
        }
    }

    public static void cleanUpOlderPackage(Context context, String str, String str2) {
        File[] listFiles;
        Logger.debug(Logger.OTALib_TAG, "FileUtils.cleanUpOlderPackage, current filename: " + str);
        File file = new File(context.getFilesDir(), "OTA");
        if (file.exists()) {
            try {
                for (File file2 : file.listFiles()) {
                    Logger.debug(Logger.OTALib_TAG, "FileUtils.cleanUpOlderPackage, file list " + file2.getName());
                    if (file2.getName().contains(str2) && !str.contains(file2.getName())) {
                        Logger.debug(Logger.OTALib_TAG, "FileUtils.cleanUpOlderPackage, deleted " + file2.getName());
                        file2.delete();
                        return;
                    }
                }
            } catch (Exception e) {
                Logger.error(Logger.OTALib_TAG, "FileUtils.cleanUpOlderPackage, exception : " + e);
            }
        }
    }
}
