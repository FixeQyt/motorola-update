package com.motorola.otalib.downloadservice.utils;

import android.content.SharedPreferences;
import com.motorola.otalib.common.settings.Settings;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DownloadServiceSettings extends Settings {
    public static final int DOWNLOAD_SOCKET_TIMEOUT = 300000;
    public static final String KEY_ADMIN_APN_STATUS = "adminapnstatus";
    public static final String KEY_DOWNLOAD_BACKOFF_VALUE = "downloadBackoffValues";
    public static final String KEY_DOWNLOAD_REQUEST = "downloadRequest";
    public static final String KEY_DOWNLOAD_SOCKET_TIMEOUT = "downloadSocketTimeout";
    public static final String KEY_DO_NOT_BIND_OTA_PROCESS = "doNotBindOtaProcess";
    public static final String KEY_FEATURE_NAME = "feature";
    public static final String KEY_ID = "id";
    public static final String KEY_NETWORK_TYPE = "networktype";
    public static final String KEY_PREFS_NAME = "dl_prefs";

    public DownloadServiceSettings(SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    public String getConfigValue(String str) {
        return getConfig(str);
    }

    public void clearConfigValue(String str) {
        removeConfig(str);
    }

    public void setConfigValue(String str, String str2) {
        setConfig(str, str2);
    }

    public int getConfigValueInt(String str) {
        try {
            return Integer.valueOf(getConfig(str)).intValue();
        } catch (NumberFormatException unused) {
            return 0;
        }
    }
}
