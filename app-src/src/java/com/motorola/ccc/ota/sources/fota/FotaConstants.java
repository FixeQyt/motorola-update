package com.motorola.ccc.ota.sources.fota;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public interface FotaConstants {
    public static final int REQUEST_UPDATE_ERROR_ALREADY = 3;
    public static final int REQUEST_UPDATE_ERROR_BUSY = 4;
    public static final int REQUEST_UPDATE_ERROR_FAIL = 1;
    public static final int REQUEST_UPDATE_ERROR_INTERNAL = 7;
    public static final int REQUEST_UPDATE_ERROR_NET = 2;
    public static final int REQUEST_UPDATE_ERROR_NONE = 5;
    public static final int REQUEST_UPDATE_ERROR_OK = 0;
    public static final int REQUEST_UPDATE_ERROR_ROAMING = 6;
    public static final int REQUEST_UPDATE_ERROR_STORAGE_LOW = 8;
    public static final int RESULT_ERROR_ACK = 2;
    public static final int RESULT_ERROR_APPLY = 6;
    public static final int RESULT_ERROR_DECLINED = 7;
    public static final int RESULT_ERROR_FAIL = 1;
    public static final int RESULT_ERROR_INVALID = 3;
    public static final int RESULT_ERROR_OK = 0;
    public static final int RESULT_ERROR_RESOURCES = 5;
    public static final int RESULT_ERROR_VERIFY = 4;
    public static final int UPDATE_AVAILABLE_ERROR_ALREADY = 4;
    public static final int UPDATE_AVAILABLE_ERROR_DECLINED = 2;
    public static final int UPDATE_AVAILABLE_ERROR_FAIL = 1;
    public static final int UPDATE_AVAILABLE_ERROR_INVALID = 5;
    public static final int UPDATE_AVAILABLE_ERROR_NETWORK = 6;
    public static final int UPDATE_AVAILABLE_ERROR_OK = 0;
    public static final int UPDATE_AVAILABLE_ERROR_RESOURCES = 3;
    public static final long WIFI_DISCOVER_TIMER_EXPIRY = 172800;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public enum AutoDownloadOption {
        WiFi,
        OTAorWiFi,
        RAN
    }
}
