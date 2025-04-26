package com.motorola.ccc.ota.env;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import java.util.ArrayList;
import java.util.HashMap;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class CceSyncSettingsHandler {
    public static final String CCE_SEND_SETTINGS = "com.motorola.cce.Actions.CCE_SEND_SETTINGS";
    public static final String CCE_SEND_SETTINGS_RESPONSE = "com.motorola.cce.Actions.CCE_SEND_SETTINGS_RESPONSE";
    public static final String CCE_SETTINGS_UPDATED = "com.motorola.cce.Actions.CCE_SETTINGS_UPDATED";
    public static final String DEVICE_MANAGEMENT_PACAKAGE_NAME = "com.motorola.ccc.devicemanagement";
    public static final String KEY_SETTINGS_APP_NAME = "com.motorola.cce.sharedsettings.appName";
    public static final String KEY_SETTINGS_DEFAULTS_OK = "com.motorola.cce.sharedsettings.defaultsOk";
    public static final String KEY_SETTINGS_ERROR = "com.motorola.cce.sharedsettings.error";
    public static final String KEY_SETTINGS_LIST = "com.motorola.cce.sharedsettings.settingsList";
    public static final String KEY_SETTINGS_PATTERN = "com.motorola.cce.sharedsettings.settingsPattern";
    public static final String KEY_SETTINGS_VALUES = "com.motorola.cce.sharedsettings.settingsValues";
    public static final String OTA_CATEGORY = "OTA";
    public static final String POLLING_FEATURE = "ota.service.update.settings.pollingFeature";
    public static final String SETTINGS_ERROR_DEVICE_NOT_PROVISIONED = "com.motorola.cce.sharedsettings.errorDeviceNotProvisioned";
    public static final String SETTINGS_ERROR_INVALID_PARAM = "com.motorola.cce.sharedsettings.errorInvalidParam";
    public static final String SETTINGS_ERROR_OK = "com.motorola.cce.sharedsettings.errorOk";

    public static final void registerCceIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CCE_SETTINGS_UPDATED);
        context.registerReceiver(broadcastReceiver, intentFilter, Permissions.INTERACT_BLUR_SERVICE, null, 2);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(CCE_SEND_SETTINGS_RESPONSE);
        intentFilter2.addCategory("OTA");
        context.registerReceiver(broadcastReceiver, intentFilter2, Permissions.INTERACT_BLUR_SERVICE, null, 2);
    }

    public static void fetchSettingsList(Context context) {
        Logger.info("OtaApp", "CceSyncSettingsHandler.fetchSettingsList:");
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(POLLING_FEATURE);
        Intent intent = new Intent(CCE_SEND_SETTINGS);
        intent.putExtra(KEY_SETTINGS_APP_NAME, "OTA");
        intent.putStringArrayListExtra(KEY_SETTINGS_LIST, arrayList);
        intent.putStringArrayListExtra(KEY_SETTINGS_PATTERN, null);
        intent.putExtra(KEY_SETTINGS_DEFAULTS_OK, false);
        intent.setPackage(DEVICE_MANAGEMENT_PACAKAGE_NAME);
        context.sendBroadcast(intent, Permissions.INTERACT_BLUR_SERVICE);
    }

    public static synchronized void onReceiveSettingsList(Intent intent, BotaSettings botaSettings, ApplicationEnv applicationEnv) {
        synchronized (CceSyncSettingsHandler.class) {
            if (intent.getStringExtra(KEY_SETTINGS_ERROR).equals(SETTINGS_ERROR_OK)) {
                HashMap hashMap = (HashMap) intent.getSerializableExtra(KEY_SETTINGS_VALUES);
                if (hashMap == null) {
                    Logger.info("OtaApp", "CceSyncSettingsHandler.onReceiveSettings: received null settings");
                    return;
                }
                for (String str : hashMap.keySet()) {
                    if (POLLING_FEATURE.contentEquals(str)) {
                        String str2 = (String) hashMap.get(str);
                        Logger.info("OtaApp", "CCeSyncSettingsHandler.onReceiveSettings: Current value : " + botaSettings.getString(Configs.POLLING_FEATURE) + " Received value: " + str2);
                        if (!TextUtils.isEmpty(str2) && str2.equals(botaSettings.getString(Configs.POLLING_FEATURE))) {
                            Logger.info("OtaApp", "CceSyncSettingsHandler.onReceiveSettings: No change in settings");
                        } else if ("on".equalsIgnoreCase(str2)) {
                            botaSettings.setLong(Configs.NEXT_POLLING_VALUE, System.currentTimeMillis());
                            botaSettings.setLong(Configs.POLL_AFTER, botaSettings.getLong(Configs.NO_POLLING_VALUE_FROM_SERVER, 86400L) * 1000);
                            botaSettings.setString(Configs.POLLING_FEATURE, str2);
                            applicationEnv.getUtilities().sendPollIntent();
                        } else if ("off".equalsIgnoreCase(str2)) {
                            botaSettings.removeConfig(Configs.POLLING_FEATURE);
                        }
                    }
                }
            } else {
                Logger.info("OtaApp", "CceSyncSettingsHandler.onReceiveSettings: received: " + intent.getStringExtra(KEY_SETTINGS_ERROR));
            }
        }
    }
}
