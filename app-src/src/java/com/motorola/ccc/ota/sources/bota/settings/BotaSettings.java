package com.motorola.ccc.ota.sources.bota.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.otalib.common.settings.Settings;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.List;
import java.util.Map;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class BotaSettings extends Settings {
    private static final String OTA_SHARED_PREFS_NAME = "ota_prefs";

    public BotaSettings() {
        super(getGlobalSharedPreference());
    }

    private static SharedPreferences getGlobalSharedPreference() {
        return OtaApplication.getGlobalContext().getSharedPreferences(OTA_SHARED_PREFS_NAME, 0);
    }

    public void setVerizonValues() {
        Logger.debug("OtaApp", "Setting Verizon Specific values");
        setString(Configs.BACKOFF_VALUES, "30000,180000");
        setString(Configs.MAX_RETRY_COUNT_DL, "2");
        setString(Configs.DEFAULT_SMART_UPDATE_BITMAP, "9");
    }

    public void setVitalUpdateValues() {
        Logger.debug("OtaApp", "Setting vital update specific values");
        setString(Configs.BACKOFF_VALUES, "10000,30000");
        setString(Configs.MAX_RETRY_COUNT_DL, "2");
    }

    public void resetBackoffRetryValuesAfterSetupCompleted(BotaSettings botaSettings) {
        Logger.debug("OtaApp", "Reset vital update specific values after setup completed");
        if (UpdaterUtils.isVerizon()) {
            botaSettings.setVerizonValues();
            return;
        }
        setString(Configs.BACKOFF_VALUES, "5000,15000,30000,60000,300000,600000,600000,600000,600000");
        setString(Configs.MAX_RETRY_COUNT_DL, "9");
    }

    private void setATTValues() {
        Logger.debug("OtaApp", "Setting AT&T Specific values");
        if (BuildPropReader.isBotaATT()) {
            setString(Configs.CHECK_ORDER, "bota");
        } else {
            setString(Configs.CHECK_ORDER, "fota,bota");
        }
        setString(Configs.DEFAULT_INSTALL_COUNT_DOWN_SECS, "11");
        setString(Configs.DEFAULT_SMART_UPDATE_BITMAP, "0");
        setInt(Configs.DEFAULT_MIN_BATTERY_LEVEL, 20);
    }

    public void saveOverlaySettings(Context context) {
        String[] strArr;
        Resources resources = context.getResources();
        String[] strArr2 = null;
        try {
            strArr = resources.getStringArray(R.array.overlayed_configs);
            try {
                strArr2 = resources.getStringArray(R.array.overlayed_configs_values);
            } catch (Resources.NotFoundException e) {
                e = e;
                Logger.error("OtaApp", "OtaService.saveOverlaySettings(): Failed reading resources. Exception = " + e);
                if (strArr == null) {
                    return;
                }
                return;
            }
        } catch (Resources.NotFoundException e2) {
            e = e2;
            strArr = null;
        }
        if (strArr == null && strArr2 != null && strArr.length == strArr2.length) {
            for (int i = 0; i < strArr.length; i++) {
                String str = strArr[i];
                String str2 = strArr2[i];
                if (str2 != null && str2.length() > 0) {
                    setConfig(str, str2);
                    Logger.info("OtaApp", "OtaService.saveOverlaySettings(): saving : " + str + " value : " + str2);
                }
            }
        }
    }

    public void setupDefaults(Context context) {
        Configs[] values;
        SharedPreferences.Editor edit = getPrefs().edit();
        for (Configs configs : Configs.values()) {
            if (configs.value().length() > 0 && getPrefs().getString(configs.key(), null) == null) {
                edit.putString(configs.key(), configs.value());
            }
        }
        edit.commit();
        clearUnusedPrefs();
        if (!BuildPropertyUtils.isDogfoodDevice()) {
            if (!BuildPropertyUtils.isChinaDevice(context)) {
                setConfig(Configs.MASTER_CLOUD, "moto-cds.appspot.com");
            } else {
                setConfig(Configs.MASTER_CLOUD, UpgradeUtils.CHINA_PRODUCTION_SERVER);
            }
        }
        saveOverlaySettings(context);
        if (UpdaterUtils.isVerizon()) {
            setVerizonValues();
        }
        if (BuildPropReader.isATT()) {
            setATTValues();
        }
        if (getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            setVitalUpdateValues();
        }
    }

    private void clearUnusedPrefs() {
        SharedPreferences.Editor edit = getPrefs().edit();
        Map<String, ?> all = getPrefs().getAll();
        List<String> returnAll = Configs.returnAll();
        for (String str : all.keySet()) {
            if (!returnAll.contains(str)) {
                Logger.verbose("OtaApp", "removing " + str);
                edit.remove(str);
            }
        }
        edit.commit();
    }

    public void incrementEvenUpgradefailureRetries() {
        try {
            int i = 1;
            int i2 = getInt(Configs.UPDATE_FAILURE_COUNT, 0) + 1;
            if (i2 != 3) {
                i = i2;
            }
            setConfig(Configs.UPDATE_FAILURE_COUNT, String.valueOf(i));
        } catch (Exception unused) {
        }
    }

    public String getPackageDownloadLocation() {
        return getConfig(Configs.PACKAGE_DOWNLOAD_PATH, (String) null);
    }

    public void clearStats() {
        removeConfig(Configs.STATS_PRE_DL_PACKAGE_NOTIFIED);
        removeConfig(Configs.STATS_PRE_DL_NOTIFIED);
        removeConfig(Configs.STATS_PRE_DL_ACCEPTED);
        removeConfig(Configs.STATS_PRE_DL_USER_DEFER);
        removeConfig(Configs.STATS_PRE_DL_USER_DEFER_COUNT);
        removeConfig(Configs.STATS_DL_STARTED);
        removeConfig(Configs.STATS_DL_SUSPENDED);
        removeConfig(Configs.STATS_DL_COMPLETED);
        removeConfig(Configs.STATS_DL_VIA_WIFI);
        removeConfig(Configs.STATS_DL_VIA_CELLULAR);
        removeConfig(Configs.STATS_DL_VIA_ADMINAPN);
        removeConfig(Configs.STATS_DL_VIA_PROXY);
        removeConfig(Configs.STATS_DL_VIA_WIFI_BEFORE_RESTART);
        removeConfig(Configs.STATS_DL_VIA_CELLULAR_BEFORE_RESTART);
        removeConfig(Configs.STATS_DL_VIA_ADMINAPN_BEFORE_RESTART);
        removeConfig(Configs.STATS_DL_DD_OBTAINED);
        removeConfig(Configs.STATS_DL_MECHANISM);
        removeConfig(Configs.STATS_DL_ADVANCE_RETRY_COUNT);
        removeConfig(Configs.STATS_INSTALL_NOTIFIED);
        removeConfig(Configs.STATS_INSTALL_DEFER);
        removeConfig(Configs.STATS_INSTALL_USER_DEFER_COUNT);
        removeConfig(Configs.STATS_INSTALL_AUTOMATICALLY);
        removeConfig(Configs.STATS_INSTALL_ACCEPTED);
        removeConfig(Configs.STATS_INSTALL_STARTED);
        removeConfig(Configs.STATS_INSTALL_COMPLETED);
        removeConfig(Configs.STATS_INSTALL_TOTAL_TIME);
        removeConfig(Configs.STATS_AB_LAST_INSTALLATION_START_TIME);
        removeConfig(Configs.STATS_SOURCE_SHA1);
        removeConfig(Configs.STATS_DESTINATION_SHA1);
        removeConfig(Configs.STATS_INSTALL_MODE);
        removeConfig(Configs.STATS_REBOOT_MODE);
        removeConfig(Configs.STATS_SMART_UPDATE_DEFERRED_ON_RESTART);
        removeConfig(Configs.STATS_OS_LINK_LAUNCH_SCREEN);
        removeConfig(Configs.STATS_EXPANDABLE_LIST_CLICK_SCREEN);
        removeConfig(Configs.STATS_DOWNLOAD_ACTIVITY_PROMPT_COUNT);
        removeConfig(Configs.STATS_INSTALL_ACTIVITY_PROMPT_COUNT);
        removeConfig(Configs.STATS_RESTART_ACTIVITY_PROMPT_COUNT);
        removeConfig(Configs.TOTAL_TIME_SPEND_ON_DOWNLOAD_SCREEN);
        removeConfig(Configs.TOTAL_TIME_SPEND_ON_INSTALL_SCREEN);
        removeConfig(Configs.TOTAL_TIME_SPEND_ON_DLP_SCREEN);
        removeConfig(Configs.TOTAL_TIME_SPEND_ON_BG_SCREEN);
        removeConfig(Configs.TOTAL_TIME_SPEND_ON_RESTART_SCREEN);
        removeConfig(Configs.STATS_DL_START_POINT);
        removeConfig(Configs.STATS_INSTALL_START_POINT);
        removeConfig(Configs.STATS_RESTART_START_POINT);
        removeConfig(Configs.STATS_VAB_FREESPACEREQ_VALUE);
        removeConfig(Configs.STATS_TIME_TAKEN_FOR_INSTALL);
        removeConfig(Configs.BOOT_START_TIMESTAMP);
        removeConfig(Configs.STATS_VAB_MERGE_RESTARTED_BY);
        removeConfig(Configs.STATS_VAB_MERGE_UPDATE_FAILED_REASON);
        removeConfig(Configs.STATS_VAB_MERGE_REBOOT_FAILURE_COUNT);
        removeConfig(Configs.STATS_VAB_MERGE_STATUS);
        clearStatsForDownloading();
        clearStatsDownloadInterfaces();
    }

    public void clearStatsForDownloading() {
        removeConfig(Configs.STATS_DL_TOTAL_SIZE);
        removeConfig(Configs.STATS_DL_RECEIVED_SIZE);
        removeConfig(Configs.STATS_DL_WIFI);
        removeConfig(Configs.STATS_DL_CELLULAR);
        removeConfig(Configs.STATS_DL_ADMINAPN);
        removeConfig(Configs.STATS_DL_IO_ERROR);
        removeConfig(Configs.STATS_DL_FORBIDDEN_ERROR);
        removeConfig(Configs.STATS_DL_GONE_ERROR);
        removeConfig(Configs.STATS_DL_NOTFOUND_ERROR);
        removeConfig(Configs.STATS_DL_PRECONDITION_ERROR);
        removeConfig(Configs.STATS_DL_RANGE_ERROR);
        removeConfig(Configs.STATS_DL_TOOMANYREQUEST_ERROR);
        removeConfig(Configs.STATS_DL_SU_ERROR);
        removeConfig(Configs.STATS_DL_ERROR_CODES);
        removeConfig(Configs.STATS_DL_START_ADMINAPN_TX_BYTES);
        removeConfig(Configs.STATS_DL_START_ADMINAPN_RX_BYTES);
        removeConfig(Configs.STATS_DL_START_CELLULAR_TX_BYTES);
        removeConfig(Configs.STATS_DL_START_CELLULAR_RX_BYTES);
        removeConfig(Configs.STATS_DL_START_WIFI_TX_BYTES);
        removeConfig(Configs.STATS_DL_START_WIFI_RX_BYTES);
        removeConfig(Configs.STATS_DL_END_ADMINAPN_TX_BYTES);
        removeConfig(Configs.STATS_DL_END_ADMINAPN_RX_BYTES);
        removeConfig(Configs.STATS_DL_END_CELLULAR_TX_BYTES);
        removeConfig(Configs.STATS_DL_END_CELLULAR_RX_BYTES);
        removeConfig(Configs.STATS_DL_END_WIFI_TX_BYTES);
        removeConfig(Configs.STATS_DL_END_WIFI_RX_BYTES);
        removeConfig(Configs.STATS_DL_ADMINAPN_TX_BYTES);
        removeConfig(Configs.STATS_DL_ADMINAPN_RX_BYTES);
        removeConfig(Configs.STATS_DL_CELLULAR_TX_BYTES);
        removeConfig(Configs.STATS_DL_CELLULAR_RX_BYTES);
        removeConfig(Configs.STATS_DL_WIFI_TX_BYTES);
        removeConfig(Configs.STATS_DL_WIFI_RX_BYTES);
        removeConfig(Configs.STATS_REBOOT_DURING_DL);
        removeConfig(Configs.STATS_DL_REVENUE_LEAK_DETECTED);
        removeConfig(Configs.STATS_DL_ACTUAL_DOWNLOAD_TIME);
        removeConfig(Configs.STATS_DL_LAST_DOWNLOAD_START_TIME);
        removeConfig(Configs.STATS_DL_NETWORK_USED_BY_DOWNLOAD);
        removeConfig(Configs.STATS_DOWNLOAD_MODE);
    }

    public void clearStatsDownloadInterfaces() {
        removeConfig(Configs.STATS_DL_INTERNET_IFACE);
        removeConfig(Configs.STATS_DL_ADMIN_IFACE);
        removeConfig(Configs.STATS_DL_WIFI_IFACE);
    }

    public void removePreviousCancelledOptionalUpdateSettings() {
        removeConfig(Configs.PREVIOUS_CANCELLED_OPT_UPDATE_ANNOY_TIME);
        removeConfig(Configs.PREVIOUS_CANCELLED_OPT_CONTENT_TIMESTAMP);
    }

    public void removeEndOfLifeInfo() {
        removeConfig(Configs.END_OF_LIFE_MAIN_IMAGE_URL);
        removeConfig(Configs.END_OF_LIFE_PROMO_IMAGE_URL);
        removeConfig(Configs.END_OF_LIFE_PROMO_LINK_URL);
        removeConfig(Configs.END_OF_LIFE_ADDITIONAL_INFO);
    }

    public void removeDontBotherPreferences() {
        removeConfig(Configs.PREVIOUS_CANCELLED_OPT_CONTENT_TIMESTAMP.key());
        removeConfig(Configs.PREVIOUS_CANCELLED_OPT_UPDATE_ANNOY_TIME.key());
    }

    public void clearBestTimesForIntelligentNotification() {
        SharedPreferences.Editor edit = OtaApplication.getGlobalContext().getSharedPreferences(UpdaterUtils.KEY_INTELLIGENT_NOTIFICATION, 0).edit();
        edit.remove(UpdaterUtils.BEST_TIME_1_HOUR);
        edit.remove(UpdaterUtils.BEST_TIME_1_MINUTE);
        edit.remove(UpdaterUtils.BEST_TIME_2_HOUR);
        edit.remove(UpdaterUtils.BEST_TIME_2_MINUTE);
        edit.apply();
    }
}
