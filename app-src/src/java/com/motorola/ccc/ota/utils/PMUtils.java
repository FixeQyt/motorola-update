package com.motorola.ccc.ota.utils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class PMUtils {
    public static final int APP_IS_EASY = Integer.MAX_VALUE;
    public static final int ARBITRARY_TOLERANCE = -1;
    public static final String BIND_POLLINGMANAGER_WSPROXY_SVC = "com.motorola.ccc.ota.pm.BIND_POLLINGMANAGER_WSPROXY_SVC";
    public static final int DEFAULT_POLL_VALUE = 86400;
    public static final String KEY_APPSECRET = "com.motorola.ccc.ota.pm.appsecret";
    public static final String KEY_CONNECTIVITY_EXTRA = "com.motorola.ccc.ota.pm.connectivity";
    public static final String KEY_CONNECTIVITY_TYPE_EXTRA = "com.motorola.ccc.ota.pm.connectivityType";
    public static final String KEY_DATAROAMING_EXTRA = "com.motorola.ccc.ota.pm.dataroaming";
    public static final String KEY_ROAMING_EXTRA = "com.motorola.ccc.ota.pm.roaming";
    public static final int MAX_MODEM_POLLING_COUNT_PRIMARY = 7;
    public static final int MAX_MODEM_POLLING_COUNT_SECONDARY = 3;
    public static final int MODEM_POLL_VALUE_PRIMARY = 86400;
    public static final int MODEM_POLL_VALUE_SECONDARY = 604800;
    public static final String MODEM_UNIQUE_WORK_NAME = "MODEM_POLLING_TRIGGER";
    public static final String MODEM_WORK_TAG = "MODEM_WORK_TAG";
    public static final String OTAAPP_WORK_TAG = "OTAAPP_WORK_TAG";
    public static final String OTA_UNIQUE_WORK_NAME = "OTA_POLLING_TRIGGER";
    public static final String POLLINGMGR_CONNECTIVITY = "com.motorola.ccc.ota.pm.POLLINGMGR_CONNECTIVITY";
    public static final String POLLINGMGR_ROAMING_CHANGE = "com.motorola.ccc.ota.pm.POLLINGMGR_ROAMING_CHANGE";

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static class PMErrorCodes {
        public static final int ERR_ALREADY = -1;
        public static final int ERR_BADPARAM = -2;
        public static final int ERR_INVALIDPARAM = -5;
        public static final int ERR_NONE = 0;
        public static final int ERR_NOTFOUND = -4;
        public static final int ERR_NULLPARAM = -3;
        public static final int ERR_OUTOFMEMORY = -6;
        public static final int ERR_SERVICEDIED = -7;
    }
}
