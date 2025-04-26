package com.motorola.ccc.ota.env;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import com.motorola.ccc.ota.sources.fota.FotaConstants;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import java.util.Enumeration;
import java.util.Properties;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public final class AndroidFotaInterface {
    public static final String ACTION_CHECK_TIMER_EXPIRE = "com.motorola.android.fota.ACITON_CHECK_TIMER_EXPIRE";
    public static final String ACTION_FOTA_BATTERY_CHANGED = "com.motorola.android.fota.BATTERY_CHANGED";
    public static final String ACTION_FOTA_DEVICE_SETUP_COMPLETED = "com.motorola.android.fota.SETUP_COMPLETED";
    public static final String ACTION_FOTA_DOWNLOAD_COMPLETE = "com.motorola.android.fota.FOTA_DOWNLOAD_COMPLETE";
    public static final String ACTION_FOTA_DOWNLOAD_MODE_CHANGED = "com.motorola.android.fota.FOTA_DOWNLOAD_MODE_CHANGED";
    public static final String ACTION_FOTA_INITIALIZATION = "com.motorola.android.fota.FOTA_INITIALIZATION";
    public static final String ACTION_FOTA_POLICY_SET_CANCEL_ONGOING_UPDATE = "com.motorola.android.fota.POLICY_SET_CANCEL_ONGOING_UPDATE";
    public static final String ACTION_FOTA_REQUEST_UPDATE = "com.motorola.android.fota.FOTA_REQUEST_UPDATE";
    public static final String ACTION_FOTA_REQUEST_UPDATE_RESPONSE = "com.motorola.android.fota.FOTA_REQUEST_UPDATE_RESPONSE";
    public static final String ACTION_FOTA_SERVER_TRANSPORT_MEDIA = "com.motorola.android.fota.FOTA_SERVER_TRANSPORT_MEDIA";
    public static final String ACTION_FOTA_UPDATE_AVAILABLE = "com.motorola.android.fota.FOTA_UPDATE_AVAILABLE";
    public static final String ACTION_FOTA_UPDATE_AVAILABLE_RESPONSE = "com.motorola.android.fota.FOTA_UPDATE_AVAILABLE_RESPONSE";
    public static final String ACTION_FOTA_UPGRADE_RESULT = "com.motorola.android.fota.FOTA_UPGRADE_RESULT";
    public static final String ACTION_FOTA_USER_ALERT_CELLULAR_OPT = "com.motorola.android.fota.FOTA_USER_ALERT_CELLULAR_OPT";
    public static final String ACTION_FOTA_USER_AUTO_DOWNLOAD_SETTINGS = "com.motorola.android.fota.USER_AUTO_DOWNLOAD_SETTINGS";
    public static final String ATTR_DESCRIPTION = "description";
    public static final String EXTRA_DL_PACKAGE_PATH = "com.motorola.android.fotaInterface.DL_PACKAGE_PATH";
    public static final String EXTRA_DOWNLOAD_COMPLETE_ERROR = "com.motorola.android.fota.fotainterface.FOTA_EXTRA_DOWNLOAD_COMPLETE_ERROR";
    public static final String EXTRA_ERROR = "com.motorola.android.fota.fotainterface.FOTA_EXTRA_ERROR";
    public static final String EXTRA_IS_FORCED = "com.motorola.android.fota.fotainterface.FOTA_EXTRA_IS_FORCED";
    public static final String EXTRA_IS_WIFI_ONLY = "com.motorola.android.fota.fotainterface.WIFI_ONLY_PACKAGE";
    public static final String EXTRA_REQUEST_ID = "com.motorola.android.fota.fotainterface.FOTA_EXTRA_REQUEST_ID";
    public static final String EXTRA_SERVER_TRANSPORT_MEDIA = "com.motorola.android.fota.fotainterface.EXTRA_SERVER_TRANSPORT_MEDIA";
    public static final String EXTRA_SIZE = "com.motoroal.android.fota.fotainterface.EXTRA_SIZE";
    public static final String EXTRA_UPDATE_AVAILABLE_RESPONSE_ERROR = "com.motorola.android.fota.fotainterface.FOTA_EXTRA_AVAILABLE_RESPONSE_ERROR";
    public static final String EXTRA_UPDATE_TYPE = "com.motorola.android.fota.fotainterface.FOTA_EXTRA_UPDATE_TYPE";
    public static final String EXTRA_UPGRADE_RESULT_ERROR = "com.motorola.android.fota.fotainterface.FOTA_UPGRADE_RESULT_ERROR";
    public static final String EXTRA_USER_SETTING_TRANSPORT_MEDIA = "com.motorola.android.fota.fotainterface.EXTRA_USER_SETTING_TRANSPORT_MEDIA";
    public static final String EXTRA_WAS_MEMORY_LOW = "com.motoroal.android.fota.fotainterface.EXTRA_WAS_MEMORY_LOW";

    private static Intent getIntent(String str, long j, int i) {
        Intent intent = new Intent(str);
        intent.putExtra(EXTRA_REQUEST_ID, j);
        intent.putExtra(EXTRA_ERROR, i);
        return intent;
    }

    public static void sendRequestUpdate(Context context, long j) {
        Intent intent = getIntent(ACTION_FOTA_REQUEST_UPDATE, j, -1);
        intent.setPackage("com.motorola.android.fota");
        context.startService(intent);
    }

    public static void sendRequestUpdateResponse(Context context, long j, int i) {
        context.sendBroadcast(getIntent(ACTION_FOTA_REQUEST_UPDATE_RESPONSE, j, i));
    }

    public static void sendUpdateAvailable(Context context, long j, long j2, Properties[] propertiesArr) {
        Intent intent = getIntent(ACTION_FOTA_UPDATE_AVAILABLE, j, -1);
        intent.putExtra(EXTRA_SIZE, j2);
        if (propertiesArr != null) {
            for (Properties properties : propertiesArr) {
                Enumeration<?> propertyNames = properties.propertyNames();
                while (propertyNames.hasMoreElements()) {
                    String str = (String) propertyNames.nextElement();
                    intent.putExtra(str, properties.getProperty(str));
                }
            }
        }
        context.sendBroadcast(intent);
    }

    public static void sendUpdateAvailableResponse(Context context, long j, int i) {
        Intent intent = getIntent(ACTION_FOTA_UPDATE_AVAILABLE_RESPONSE, j, i);
        intent.setPackage("com.motorola.android.fota");
        context.startService(intent);
    }

    public static void sendUpdateAvailableResponse(Context context, long j, int i, boolean z) {
        Intent intent = getIntent(ACTION_FOTA_UPDATE_AVAILABLE_RESPONSE, j, i);
        intent.setPackage("com.motorola.android.fota");
        intent.putExtra(EXTRA_WAS_MEMORY_LOW, z);
        context.startService(intent);
    }

    public static void sendDownloadComplete(Context context, long j, int i) {
        context.sendBroadcast(getIntent(ACTION_FOTA_DOWNLOAD_COMPLETE, j, i));
    }

    public static void sendUpgradeResult(Context context, long j, int i) {
        Intent intent = getIntent(ACTION_FOTA_UPGRADE_RESULT, j, i);
        intent.setPackage("com.motorola.android.fota");
        context.startService(intent);
    }

    public static void sendFotaInitializationIntent(Context context) {
        Intent intent = new Intent(ACTION_FOTA_INITIALIZATION);
        intent.setPackage("com.motorola.android.fota");
        context.startService(intent);
    }

    public static void registerFotaInterfaceClient(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FOTA_REQUEST_UPDATE_RESPONSE);
        intentFilter.addAction(ACTION_FOTA_UPDATE_AVAILABLE);
        intentFilter.addAction(ACTION_FOTA_DOWNLOAD_COMPLETE);
        context.registerReceiver(broadcastReceiver, intentFilter, 2);
    }

    public static void registerFotaInterfaceServer(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FOTA_REQUEST_UPDATE);
        intentFilter.addAction(ACTION_FOTA_UPDATE_AVAILABLE_RESPONSE);
        intentFilter.addAction(ACTION_FOTA_UPGRADE_RESULT);
        context.registerReceiver(broadcastReceiver, intentFilter, 2);
    }

    public static long getIdFromIntent(Intent intent) {
        return intent.getLongExtra(EXTRA_REQUEST_ID, -1L);
    }

    public static int getErrorFromIntent(Intent intent) {
        return intent.getIntExtra(EXTRA_ERROR, -1);
    }

    public static long getSizeFromIntent(Intent intent) {
        return intent.getLongExtra(EXTRA_SIZE, -1L);
    }

    public static Properties getPropertiesFrom(Intent intent) {
        Properties properties = new Properties();
        Bundle extras = intent.getExtras();
        for (String str : extras.keySet()) {
            if (!str.equals(EXTRA_ERROR) && !str.equals(EXTRA_REQUEST_ID)) {
                properties.setProperty(str, extras.getString(str));
            }
        }
        return properties;
    }

    public static boolean getIsForcedFromIntent(Intent intent) {
        return intent.getBooleanExtra(EXTRA_IS_FORCED, false);
    }

    public static String getDDDescriptionFromIntent(Intent intent) {
        return intent.getStringExtra(ATTR_DESCRIPTION);
    }

    public static void sendWifiDiscoverTimerExpiry(Context context, long j, int i) {
        Intent intent = getIntent(ACTION_CHECK_TIMER_EXPIRE, j, i);
        intent.setPackage("com.motorola.android.fota");
        context.startService(intent);
    }

    public static void sendSetupIntentToFota(Context context) {
        Intent intent = new Intent(ACTION_FOTA_DEVICE_SETUP_COMPLETED);
        intent.setPackage("com.motorola.android.fota");
        context.startService(intent);
    }

    public static void sendOngoingFotaPolicySetCancelIntent(Context context) {
        Intent intent = new Intent(ACTION_FOTA_POLICY_SET_CANCEL_ONGOING_UPDATE);
        intent.setPackage("com.motorola.android.fota");
        context.startService(intent);
    }

    public static void sendBatteryChangedIntentToFota(Context context, boolean z) {
        Intent intent = new Intent(ACTION_FOTA_BATTERY_CHANGED);
        intent.putExtra(UpdaterUtils.KEY_BATTERY_LOW, z);
        intent.setPackage("com.motorola.android.fota");
        context.startService(intent);
    }

    public static void sendAutoDownloadSettingsToFota(Context context, FotaConstants.AutoDownloadOption autoDownloadOption) {
        Intent intent = new Intent(ACTION_FOTA_USER_AUTO_DOWNLOAD_SETTINGS);
        intent.putExtra(EXTRA_USER_SETTING_TRANSPORT_MEDIA, autoDownloadOption.toString());
        intent.setPackage("com.motorola.android.fota");
        context.startService(intent);
    }
}
