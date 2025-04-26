package com.motorola.ccc.ota.stats;

import android.text.TextUtils;
import com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineHelper;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class StatsBuilder {
    public static JSONObject buildPreDownloadStats(BotaSettings botaSettings) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("preDownloadStats", getPreDownloadStats(botaSettings));
            return jSONObject;
        } catch (JSONException e) {
            Logger.error("OtaApp", "StatsBuilder.buildPreDownloadStats caught exception on preDownloadStats:" + e);
            return null;
        }
    }

    public static JSONObject buildDownloadStats(BotaSettings botaSettings) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("preDownloadStats", getPreDownloadStats(botaSettings));
        linkedHashMap.put("downloadStats", getDownloadStats(botaSettings));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsBuilder.buildDownloadStats");
    }

    public static JSONObject buildInstallStats(BotaSettings botaSettings) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("preDownloadStats", getPreDownloadStats(botaSettings));
        linkedHashMap.put("downloadStats", getDownloadStats(botaSettings));
        linkedHashMap.put("installStats", getInstallStats(botaSettings));
        linkedHashMap.put("totalOtaTime", Integer.valueOf(StatsHelper.getTotalOtaTime(botaSettings)));
        linkedHashMap.put("userDisabledUpdateNotification", Boolean.valueOf(!NotificationUtils.isUpdateNotificationChannelEnabled()));
        linkedHashMap.put("userDisabledProgressNotification", Boolean.valueOf(!NotificationUtils.isProgressNotificationChannelEnabled()));
        linkedHashMap.put("userExpandableClickScreen", botaSettings.getString(Configs.STATS_EXPANDABLE_LIST_CLICK_SCREEN));
        linkedHashMap.put("osLinkLaunchScreen", botaSettings.getString(Configs.STATS_OS_LINK_LAUNCH_SCREEN));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsBuilder.buildInstallStats");
    }

    public static JSONObject buildMergeStats(BotaSettings botaSettings) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("preDownloadStats", getPreDownloadStats(botaSettings));
        linkedHashMap.put("downloadStats", getDownloadStats(botaSettings));
        linkedHashMap.put("installStats", getInstallStats(botaSettings));
        linkedHashMap.put("totalOtaTime", Integer.valueOf(StatsHelper.getTotalOtaTime(botaSettings)));
        linkedHashMap.put("userDisabledUpdateNotification", Boolean.valueOf(!NotificationUtils.isUpdateNotificationChannelEnabled()));
        linkedHashMap.put("userDisabledProgressNotification", Boolean.valueOf(!NotificationUtils.isProgressNotificationChannelEnabled()));
        linkedHashMap.put("userExpandableClickScreen", botaSettings.getString(Configs.STATS_EXPANDABLE_LIST_CLICK_SCREEN));
        linkedHashMap.put("osLinkLaunchScreen", botaSettings.getString(Configs.STATS_OS_LINK_LAUNCH_SCREEN));
        linkedHashMap.put("mergeStats", getMergeStats(botaSettings));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsBuilder.buildInstallStats");
    }

    public static JSONObject getMergeStats(BotaSettings botaSettings) {
        String jsonDataFromFile = UpdaterEngineHelper.getJsonDataFromFile(UpdaterEngineHelper.mergeStatsFilePath);
        JSONObject jSONObject = new JSONObject();
        if (jsonDataFromFile != null) {
            try {
                jSONObject = new JSONObject(jsonDataFromFile).optJSONObject("mergeStats");
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("ueMergeStats", jSONObject);
        linkedHashMap.put("mergeProcessStats", getMergeProcessStats(botaSettings));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsHelper.getMergeStats");
    }

    private static JSONObject getMergeProcessStats(BotaSettings botaSettings) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        String string = botaSettings.getString(Configs.STATS_VAB_MERGE_RESTARTED_BY);
        if (!TextUtils.isEmpty(string)) {
            linkedHashMap.put("mergeRestartedBy", "[" + string.substring(1) + "]");
        }
        String string2 = botaSettings.getString(Configs.STATS_VAB_MERGE_STATUS);
        if (!TextUtils.isEmpty(string2)) {
            linkedHashMap.put("mergeStatus", "[" + string2.substring(1) + "]");
        }
        linkedHashMap.put("mergeUpdateFailedReason", botaSettings.getString(Configs.STATS_VAB_MERGE_UPDATE_FAILED_REASON));
        linkedHashMap.put("mergeRebootFailureCount", Integer.valueOf(botaSettings.getInt(Configs.STATS_VAB_MERGE_REBOOT_FAILURE_COUNT, 0)));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsHelper.getMergeProcessStats");
    }

    static final JSONObject getInstallStats(BotaSettings botaSettings) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("InstallNotifiedTime", Long.valueOf(botaSettings.getLong(Configs.STATS_INSTALL_NOTIFIED, 0L)));
        linkedHashMap.put("InstallAcceptedTime", Long.valueOf(botaSettings.getLong(Configs.STATS_INSTALL_ACCEPTED, 0L)));
        linkedHashMap.put("InstallStartTime", Long.valueOf(botaSettings.getLong(Configs.STATS_INSTALL_STARTED, 0L)));
        linkedHashMap.put("InstallEndTime", Long.valueOf(botaSettings.getLong(Configs.STATS_INSTALL_COMPLETED, 0L)));
        linkedHashMap.put("InstallTotalTimeInSecs", Integer.valueOf(StatsHelper.getTotalInstallTime(botaSettings)));
        if (botaSettings.getString(Configs.STATS_INSTALL_DEFER) != null && botaSettings.getString(Configs.STATS_INSTALL_DEFER).length() > 0) {
            linkedHashMap.put("InstallAutomatically", Boolean.valueOf(botaSettings.getBoolean(Configs.STATS_INSTALL_AUTOMATICALLY)));
            try {
                linkedHashMap.put("InstallDeferStats", new JSONObject(botaSettings.getString(Configs.STATS_INSTALL_DEFER)));
            } catch (JSONException e) {
                Logger.error("OtaApp", "StatsBuilder.getInstallStats caught exception on InstallDeferStats:" + e);
            }
        }
        if (SmartUpdateUtils.isSmartUpdateEnabledByUser(botaSettings)) {
            linkedHashMap.put("smartUpdateDeferredOnRestart", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_DEFERRED_ON_RESTART, "0"));
        }
        if (botaSettings.getString(Configs.STATS_INSTALL_MODE) != null && botaSettings.getString(Configs.STATS_INSTALL_MODE).length() > 0) {
            linkedHashMap.put(UpdaterUtils.INSTALL_MODE_STATS, botaSettings.getString(Configs.STATS_INSTALL_MODE));
        }
        if (botaSettings.getString(Configs.STATS_REBOOT_MODE) != null && botaSettings.getString(Configs.STATS_REBOOT_MODE).length() > 0) {
            linkedHashMap.put(UpdaterUtils.REBOOT_MODE_STATS, botaSettings.getString(Configs.STATS_REBOOT_MODE));
        }
        if (botaSettings.getJsonObject(Configs.STATS_UE_SPECIFIC) != null) {
            linkedHashMap.put("UESpecificStats", botaSettings.getJsonObject(Configs.STATS_UE_SPECIFIC));
        }
        if (BuildPropReader.isUEUpdateEnabled()) {
            linkedHashMap.put("totalRestartReminders", Integer.valueOf(botaSettings.getInt(Configs.STATS_RESTART_ACTIVITY_PROMPT_COUNT, 0)));
            linkedHashMap.put("totalTimeSpendOnRestartScreenInSecs", Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(botaSettings.getLong(Configs.TOTAL_TIME_SPEND_ON_RESTART_SCREEN, 0L))));
            linkedHashMap.put("restartReminderEntryPoint", botaSettings.getString(Configs.STATS_RESTART_START_POINT));
        } else {
            linkedHashMap.put("totalInstallReminders", Integer.valueOf(botaSettings.getInt(Configs.STATS_INSTALL_ACTIVITY_PROMPT_COUNT, 0)));
            linkedHashMap.put("totalTimeSpendOnInstallScreenInSecs", Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(botaSettings.getLong(Configs.TOTAL_TIME_SPEND_ON_INSTALL_SCREEN, 0L))));
            linkedHashMap.put("installReminderEntryPoint", botaSettings.getString(Configs.STATS_INSTALL_START_POINT));
        }
        if (botaSettings.getLong(Configs.BOOT_START_TIMESTAMP, -1L) != -1) {
            linkedHashMap.put("timeTakenToUpdateDeviceInMillSecs", Long.valueOf(botaSettings.getLong(Configs.STATS_TIME_TAKEN_FOR_INSTALL, -1L)));
        }
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsBuilder.getInstallStats");
    }

    static final JSONObject getDownloadStats(BotaSettings botaSettings) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        new JSONObject();
        linkedHashMap.put("downloadStartTime", Long.valueOf(botaSettings.getLong(Configs.STATS_DL_STARTED, 0L)));
        linkedHashMap.put("downloadEndTime", Long.valueOf(botaSettings.getLong(Configs.STATS_DL_COMPLETED, 0L)));
        if (StatsHelper.isCountPresent(botaSettings.getInt(Configs.STATS_PRE_DL_USER_DEFER_COUNT, 0))) {
            linkedHashMap.put("downloadDeferredCount", Integer.valueOf(botaSettings.getInt(Configs.STATS_PRE_DL_USER_DEFER_COUNT, 0)));
        }
        if (StatsHelper.isCountPresent(botaSettings.getInt(Configs.STATS_DL_SUSPENDED, 0))) {
            linkedHashMap.put("downloadSuspendedCount", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_SUSPENDED, 0)));
        }
        if (StatsHelper.isCountPresent(botaSettings.getInt(Configs.STATS_DL_DD_OBTAINED, 0))) {
            linkedHashMap.put("downloadDescriptorCount", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_DD_OBTAINED, 0)));
        }
        if (StatsHelper.isCountPresent(botaSettings.getInt(Configs.STATS_DL_IO_ERROR, 0))) {
            linkedHashMap.put("downloadIoException", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_IO_ERROR, 0)));
        }
        if (botaSettings.getString(Configs.STATS_DL_ERROR_CODES) != null && botaSettings.getString(Configs.STATS_DL_ERROR_CODES).length() > 0) {
            try {
                linkedHashMap.put("downloadErrorCode", new JSONObject(botaSettings.getString(Configs.STATS_DL_ERROR_CODES)));
            } catch (JSONException e) {
                Logger.error("OtaApp", "StatsBuilder.getDownloadStats caught exception on downloadErrorCode:" + e);
            }
        }
        if (botaSettings.getString(Configs.STATS_DL_VIA_WIFI) != null) {
            linkedHashMap.put("downloadPercentageWifi", botaSettings.getString(Configs.STATS_DL_VIA_WIFI));
        }
        if (botaSettings.getString(Configs.STATS_DL_VIA_CELLULAR) != null) {
            linkedHashMap.put("downloadPercentageCellular", botaSettings.getString(Configs.STATS_DL_VIA_CELLULAR));
        }
        if (botaSettings.getString(Configs.STATS_DL_VIA_ADMINAPN) != null) {
            linkedHashMap.put("downloadPercentageAdminApn", botaSettings.getString(Configs.STATS_DL_VIA_ADMINAPN));
        }
        if (botaSettings.getString(Configs.STATS_DL_VIA_PROXY) != null) {
            linkedHashMap.put("downloadPercentageProxy", botaSettings.getString(Configs.STATS_DL_VIA_PROXY));
        }
        if (botaSettings.getString(Configs.STATS_DL_VIA_WIFI_BEFORE_RESTART) != null) {
            linkedHashMap.put("downloadPercentageWifiBeforeRestart", botaSettings.getString(Configs.STATS_DL_VIA_WIFI_BEFORE_RESTART));
        }
        if (botaSettings.getString(Configs.STATS_DL_VIA_CELLULAR_BEFORE_RESTART) != null) {
            linkedHashMap.put("downloadPercentageCellularBeforeRestart", botaSettings.getString(Configs.STATS_DL_VIA_CELLULAR_BEFORE_RESTART));
        }
        if (botaSettings.getString(Configs.STATS_DL_VIA_ADMINAPN_BEFORE_RESTART) != null) {
            linkedHashMap.put("downloadPercentageAdminApnBeforeRestart", botaSettings.getString(Configs.STATS_DL_VIA_ADMINAPN_BEFORE_RESTART));
        }
        if (botaSettings.getLong(Configs.STATS_DL_ADMINAPN_TX_BYTES, 0L) > 0) {
            linkedHashMap.put("downloadAdminApnTxBytes", Long.valueOf(botaSettings.getLong(Configs.STATS_DL_ADMINAPN_TX_BYTES, 0L)));
        }
        if (botaSettings.getLong(Configs.STATS_DL_ADMINAPN_RX_BYTES, 0L) > 0) {
            linkedHashMap.put("downloadAdminApnRxBytes", Long.valueOf(botaSettings.getLong(Configs.STATS_DL_ADMINAPN_RX_BYTES, 0L)));
        }
        if (botaSettings.getLong(Configs.STATS_DL_CELLULAR_TX_BYTES, 0L) > 0) {
            linkedHashMap.put("downloadCellularTxBytes", Long.valueOf(botaSettings.getLong(Configs.STATS_DL_CELLULAR_TX_BYTES, 0L)));
        }
        if (botaSettings.getLong(Configs.STATS_DL_CELLULAR_RX_BYTES, 0L) > 0) {
            linkedHashMap.put("downloadCellularRxBytes", Long.valueOf(botaSettings.getLong(Configs.STATS_DL_CELLULAR_RX_BYTES, 0L)));
        }
        if (botaSettings.getLong(Configs.STATS_DL_WIFI_TX_BYTES, 0L) > 0) {
            linkedHashMap.put("downloadWifiTxBytes", Long.valueOf(botaSettings.getLong(Configs.STATS_DL_WIFI_TX_BYTES, 0L)));
        }
        if (botaSettings.getLong(Configs.STATS_DL_WIFI_RX_BYTES, 0L) > 0) {
            linkedHashMap.put("downloadWifiRxBytes", Long.valueOf(botaSettings.getLong(Configs.STATS_DL_WIFI_RX_BYTES, 0L)));
        }
        if (botaSettings.getString(Configs.STATS_DL_REVENUE_LEAK_DETECTED) != null) {
            linkedHashMap.put("revenueLeakDetected", botaSettings.getString(Configs.STATS_DL_REVENUE_LEAK_DETECTED));
        }
        if (botaSettings.getString(Configs.STATS_DOWNLOAD_MODE) != null && botaSettings.getString(Configs.STATS_DOWNLOAD_MODE).length() > 0) {
            linkedHashMap.put(UpdaterUtils.DOWNLOAD_MODE_STATS, botaSettings.getString(Configs.STATS_DOWNLOAD_MODE));
        }
        linkedHashMap.put("advancedDownloadFeature", Boolean.valueOf(botaSettings.getBoolean(Configs.STATS_DL_MECHANISM)));
        linkedHashMap.put("advancedDownloadRetryCount", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_ADVANCE_RETRY_COUNT, 0)));
        linkedHashMap.put("downloadPercentageStatsUsingIface", Boolean.valueOf(botaSettings.getInt(Configs.STATS_REBOOT_DURING_DL, 0) <= 0));
        linkedHashMap.put("downloadTotalSize", Long.valueOf(botaSettings.getLong(Configs.STATS_DL_TOTAL_SIZE, 0L)));
        linkedHashMap.put("downloadTotalTime", Integer.valueOf(StatsHelper.getTotalDownoadTime(botaSettings)));
        linkedHashMap.put("downloadActualTimeInSecs", Long.valueOf(StatsHelper.getActualDownloadTime(botaSettings)));
        linkedHashMap.put("networkUsedByDownload", botaSettings.getString(Configs.STATS_DL_NETWORK_USED_BY_DOWNLOAD));
        if (BuildPropReader.isUEUpdateEnabled()) {
            linkedHashMap.put("totalTimeSpendOnProgressScreenInSecs", Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(botaSettings.getLong(Configs.TOTAL_TIME_SPEND_ON_BG_SCREEN, 0L))));
        } else {
            linkedHashMap.put("totalTimeSpendOnProgressScreenInSecs", Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(botaSettings.getLong(Configs.TOTAL_TIME_SPEND_ON_DLP_SCREEN, 0L))));
        }
        if (BuildPropReader.doesDeviceSupportVABUpdate() && botaSettings.getLong(Configs.STATS_VAB_FREESPACEREQ_VALUE, 0L) > 0) {
            linkedHashMap.put("vabFreeSpaceReqInBytes", Long.valueOf(botaSettings.getLong(Configs.STATS_VAB_FREESPACEREQ_VALUE, 0L)));
        }
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsBuilder.getDownloadStats");
    }

    static final JSONObject getPreDownloadStats(BotaSettings botaSettings) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("packageNotifiedTime", Long.valueOf(botaSettings.getLong(Configs.STATS_PRE_DL_PACKAGE_NOTIFIED, 0L)));
        linkedHashMap.put("downloadNotifiedTime", Long.valueOf(botaSettings.getLong(Configs.STATS_PRE_DL_NOTIFIED, 0L)));
        linkedHashMap.put("downloadAcceptedTime", Long.valueOf(botaSettings.getLong(Configs.STATS_PRE_DL_ACCEPTED, 0L)));
        if (botaSettings.getString(Configs.STATS_PRE_DL_USER_DEFER) != null && botaSettings.getString(Configs.STATS_PRE_DL_USER_DEFER).length() > 0) {
            try {
                linkedHashMap.put("downloadDeferStats", new JSONObject(botaSettings.getString(Configs.STATS_PRE_DL_USER_DEFER)).optJSONObject("downloadDeferStats"));
            } catch (JSONException e) {
                Logger.error("OtaApp", "StatsBuilder.getPreDownloadStats caught exception on downloadDeferStats:" + e);
            }
        }
        linkedHashMap.put("totalPDLReminders", Integer.valueOf(botaSettings.getInt(Configs.STATS_DOWNLOAD_ACTIVITY_PROMPT_COUNT, 0)));
        linkedHashMap.put("totalTimeSpendOnPDLScreenInSecs", Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(botaSettings.getLong(Configs.TOTAL_TIME_SPEND_ON_DOWNLOAD_SCREEN, 0L))));
        linkedHashMap.put("PDLReminderEntryPoint", botaSettings.getString(Configs.STATS_DL_START_POINT));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsBuilder.getPreDownloadStats");
    }
}
