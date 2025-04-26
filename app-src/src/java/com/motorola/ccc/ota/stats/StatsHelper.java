package com.motorola.ccc.ota.stats;

import android.content.Context;
import android.net.ConnectivityManager;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.errorCodes.UpdaterEngineErrorCodes;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class StatsHelper {
    public static final boolean isCountPresent(int i) {
        return i > 0;
    }

    public static final void setPackageNotifiedTime(BotaSettings botaSettings, String str, String str2) {
        if (botaSettings.getLong(Configs.STATS_PRE_DL_PACKAGE_NOTIFIED, 0L) > 0) {
            return;
        }
        botaSettings.setLong(Configs.STATS_PRE_DL_PACKAGE_NOTIFIED, System.currentTimeMillis());
        botaSettings.setString(Configs.STATS_SOURCE_SHA1, str);
        botaSettings.setString(Configs.STATS_DESTINATION_SHA1, str2);
    }

    public static final void setDownloadNotifiedTime(BotaSettings botaSettings) {
        if (botaSettings.getLong(Configs.STATS_PRE_DL_NOTIFIED, 0L) > 0) {
            return;
        }
        botaSettings.setLong(Configs.STATS_PRE_DL_NOTIFIED, System.currentTimeMillis());
    }

    public static final void setDownloadAcceptedTime(BotaSettings botaSettings) {
        botaSettings.setLong(Configs.STATS_PRE_DL_ACCEPTED, System.currentTimeMillis());
    }

    public static final void setTimeAndIfaceStatsAtDownloadStart(BotaSettings botaSettings) {
        if (botaSettings.getLong(Configs.STATS_DL_STARTED, 0L) > 0) {
            return;
        }
        botaSettings.setLong(Configs.STATS_DL_STARTED, System.currentTimeMillis());
        botaSettings.setBoolean(Configs.STATS_DL_MECHANISM, UpdaterUtils.getAdvancedDownloadFeature());
        botaSettings.setInt(Configs.STATS_DL_ADVANCE_RETRY_COUNT, botaSettings.getInt(Configs.ADVANCE_DL_RETRY_COUNT, 0));
    }

    public static final void setDownloadEndTime(BotaSettings botaSettings) {
        botaSettings.setLong(Configs.STATS_DL_COMPLETED, System.currentTimeMillis());
    }

    public static final void setDDObtainedCount(BotaSettings botaSettings) {
        botaSettings.incrementPrefs(Configs.STATS_DL_DD_OBTAINED);
    }

    public static final void setRebootDurindDownloadCount(BotaSettings botaSettings) {
        botaSettings.incrementPrefs(Configs.STATS_REBOOT_DURING_DL);
    }

    public static final void setInstallAcceptedTime(BotaSettings botaSettings) {
        botaSettings.setLong(Configs.STATS_INSTALL_ACCEPTED, System.currentTimeMillis());
    }

    public static final void setInstallNotifiedTime(BotaSettings botaSettings) {
        botaSettings.setLong(Configs.STATS_INSTALL_NOTIFIED, System.currentTimeMillis());
    }

    public static final void setInstallStartTime(BotaSettings botaSettings) {
        botaSettings.setLong(Configs.STATS_INSTALL_STARTED, System.currentTimeMillis());
    }

    public static final void setInstallEndTime(BotaSettings botaSettings, long j) {
        botaSettings.setLong(Configs.STATS_INSTALL_COMPLETED, j);
    }

    public static final void setDownloadSuspendedCount(BotaSettings botaSettings) {
        botaSettings.incrementPrefs(Configs.STATS_DL_SUSPENDED);
    }

    public static final void setDownloadTotalSize(BotaSettings botaSettings, long j) {
        if (botaSettings.getLong(Configs.STATS_DL_TOTAL_SIZE, 0L) > 0) {
            return;
        }
        botaSettings.setLong(Configs.STATS_DL_TOTAL_SIZE, j);
    }

    public static final void setReceivedSize(BotaSettings botaSettings, long j) {
        botaSettings.setLong(Configs.STATS_DL_RECEIVED_SIZE, j);
    }

    public static final void setDownloadedSizeViaWifi(BotaSettings botaSettings, long j) {
        botaSettings.setLong(Configs.STATS_DL_WIFI, botaSettings.getLong(Configs.STATS_DL_WIFI, 0L) + (j - botaSettings.getLong(Configs.STATS_DL_RECEIVED_SIZE, 0L)));
    }

    public static final void setDownloadedSizeViaCellular(BotaSettings botaSettings, long j) {
        botaSettings.setLong(Configs.STATS_DL_CELLULAR, botaSettings.getLong(Configs.STATS_DL_CELLULAR, 0L) + (j - botaSettings.getLong(Configs.STATS_DL_RECEIVED_SIZE, 0L)));
    }

    public static final void setDownloadedSizeViaAdminApn(BotaSettings botaSettings, long j) {
        botaSettings.setLong(Configs.STATS_DL_ADMINAPN, botaSettings.getLong(Configs.STATS_DL_ADMINAPN, 0L) + (j - botaSettings.getLong(Configs.STATS_DL_RECEIVED_SIZE, 0L)));
    }

    public static final void setDownloadPercentageViaWifi(BotaSettings botaSettings, long j) {
        setDownloadPercentageViaNetwork(Configs.STATS_DL_WIFI_RX_BYTES, Configs.STATS_DL_VIA_WIFI, Configs.STATS_DL_WIFI, botaSettings, j);
    }

    public static final void setDownloadPercentageViaCellular(BotaSettings botaSettings, long j) {
        setDownloadPercentageViaNetwork(Configs.STATS_DL_CELLULAR_RX_BYTES, Configs.STATS_DL_VIA_CELLULAR, Configs.STATS_DL_CELLULAR, botaSettings, j);
    }

    public static void setDownloadPercentageViaAdminApn(BotaSettings botaSettings, long j) {
        setDownloadPercentageViaNetwork(Configs.STATS_DL_ADMINAPN_RX_BYTES, Configs.STATS_DL_VIA_ADMINAPN, Configs.STATS_DL_ADMINAPN, botaSettings, j);
    }

    public static final void setDownloadPercentageViaWifiBeforeRestart(BotaSettings botaSettings, long j) {
        setDownloadPercentageViaNetwork(Configs.STATS_DL_WIFI_RX_BYTES, Configs.STATS_DL_VIA_WIFI_BEFORE_RESTART, Configs.STATS_DL_WIFI, botaSettings, j);
    }

    public static final void setDownloadPercentageViaCellularBeforeRestart(BotaSettings botaSettings, long j) {
        setDownloadPercentageViaNetwork(Configs.STATS_DL_CELLULAR_RX_BYTES, Configs.STATS_DL_VIA_CELLULAR_BEFORE_RESTART, Configs.STATS_DL_CELLULAR, botaSettings, j);
    }

    public static void setDownloadPercentageViaAdminApnBeforeRestart(BotaSettings botaSettings, long j) {
        setDownloadPercentageViaNetwork(Configs.STATS_DL_ADMINAPN_RX_BYTES, Configs.STATS_DL_VIA_ADMINAPN_BEFORE_RESTART, Configs.STATS_DL_ADMINAPN, botaSettings, j);
    }

    private static void setDownloadPercentageViaNetwork(Configs configs, Configs configs2, Configs configs3, BotaSettings botaSettings, long j) {
        long j2 = botaSettings.getLong(configs, 0L);
        if (botaSettings.getInt(Configs.STATS_REBOOT_DURING_DL, 0) == 0) {
            botaSettings.setString(configs2, getDownloadPercentage(j2, j));
            return;
        }
        long j3 = botaSettings.getLong(configs3, 0L);
        if (j3 <= 0) {
            botaSettings.setString(configs2, null);
        } else {
            botaSettings.setString(configs2, String.format(Locale.getDefault(), "%.1f", Double.valueOf((j3 * 100) / j)));
        }
    }

    public static final void setDownloadDeferStats(BotaSettings botaSettings, long j) {
        botaSettings.incrementPrefs(Configs.STATS_PRE_DL_USER_DEFER_COUNT);
        int i = botaSettings.getInt(Configs.STATS_PRE_DL_USER_DEFER_COUNT, 0);
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("downloadDeferCount", Integer.valueOf(i));
        linkedHashMap.put("deferredTime", Long.valueOf(j));
        JSONObject jsonObjectFromMap = UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "DownloadDeferStats");
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("downloadDeferStats", jsonObjectFromMap);
            botaSettings.setString(Configs.STATS_PRE_DL_USER_DEFER, jSONObject.toString());
        } catch (JSONException unused) {
            botaSettings.setString(Configs.STATS_PRE_DL_USER_DEFER, null);
        }
    }

    public static final void setNetworkUsedByDownload(BotaSettings botaSettings) {
        String str;
        if (getDownloadPercentageViaThisNetwork(botaSettings.getString(Configs.STATS_DL_VIA_WIFI)).doubleValue() <= 0.0d) {
            str = "";
        } else {
            str = "WIFI/";
        }
        if (getDownloadPercentageViaThisNetwork(botaSettings.getString(Configs.STATS_DL_VIA_CELLULAR)).doubleValue() > 0.0d) {
            str = str + "Cellular/";
        }
        if (getDownloadPercentageViaThisNetwork(botaSettings.getString(Configs.STATS_DL_VIA_ADMINAPN)).doubleValue() > 0.0d) {
            str = str + "AdminApn/";
        }
        if (getDownloadPercentageViaThisNetwork(botaSettings.getString(Configs.STATS_DL_VIA_PROXY)).doubleValue() > 0.0d) {
            str = str + "Proxy/";
        }
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
        }
        botaSettings.setString(Configs.STATS_DL_NETWORK_USED_BY_DOWNLOAD, str);
    }

    private static Double getDownloadPercentageViaThisNetwork(String str) {
        Double valueOf = Double.valueOf(0.0d);
        if (str != null) {
            try {
                return Double.valueOf(Double.parseDouble(str));
            } catch (NumberFormatException unused) {
            }
        }
        return valueOf;
    }

    private static Double incrementDownloadPercentageViaThisNetwork(String str, String str2) {
        return Double.valueOf(getDownloadPercentageViaThisNetwork(str).doubleValue() + getDownloadPercentageViaThisNetwork(str2).doubleValue());
    }

    public static final void setInstallDeferStats(BotaSettings botaSettings, boolean z, long j) {
        botaSettings.setBoolean(Configs.STATS_INSTALL_AUTOMATICALLY, z);
        botaSettings.incrementPrefs(Configs.STATS_INSTALL_USER_DEFER_COUNT);
        int i = botaSettings.getInt(Configs.STATS_INSTALL_USER_DEFER_COUNT, 0);
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("InstallDeferCount", Integer.valueOf(i));
        linkedHashMap.put(UpdaterUtils.INSTALL_AUTOMATICALLY, Boolean.valueOf(z));
        linkedHashMap.put("deferredTime", Long.valueOf(j));
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("InstallDeferStats", UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "InstallDeferStats"));
            botaSettings.setString(Configs.STATS_INSTALL_DEFER, jSONObject.toString());
        } catch (JSONException unused) {
            botaSettings.setString(Configs.STATS_INSTALL_DEFER, null);
        }
    }

    public static final int getTotalOtaTime(BotaSettings botaSettings) {
        return (int) Math.max(TimeUnit.MILLISECONDS.toMinutes(botaSettings.getLong(Configs.STATS_INSTALL_COMPLETED, 0L) - botaSettings.getLong(Configs.STATS_PRE_DL_PACKAGE_NOTIFIED, 0L)), 0L);
    }

    public static final int getTotalDownoadTime(BotaSettings botaSettings) {
        return (int) TimeUnit.MILLISECONDS.toSeconds(botaSettings.getLong(Configs.STATS_DL_COMPLETED, 0L) - botaSettings.getLong(Configs.STATS_DL_STARTED, 0L));
    }

    public static final long getActualDownloadTime(BotaSettings botaSettings) {
        return TimeUnit.MILLISECONDS.toSeconds(botaSettings.getLong(Configs.STATS_DL_ACTUAL_DOWNLOAD_TIME, 0L));
    }

    public static final void setDownloadErrorCodes(BotaSettings botaSettings) {
        boolean z;
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        boolean z2 = true;
        if (isCountPresent(botaSettings.getInt(Configs.STATS_DL_FORBIDDEN_ERROR, 0))) {
            linkedHashMap.put("403", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_FORBIDDEN_ERROR, 0)));
            z = true;
        } else {
            z = false;
        }
        if (isCountPresent(botaSettings.getInt(Configs.STATS_DL_NOTFOUND_ERROR, 0))) {
            linkedHashMap.put("404", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_NOTFOUND_ERROR, 0)));
            z = true;
        }
        if (isCountPresent(botaSettings.getInt(Configs.STATS_DL_GONE_ERROR, 0))) {
            linkedHashMap.put("410", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_GONE_ERROR, 0)));
            z = true;
        }
        if (isCountPresent(botaSettings.getInt(Configs.STATS_DL_PRECONDITION_ERROR, 0))) {
            linkedHashMap.put("412", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_PRECONDITION_ERROR, 0)));
            z = true;
        }
        if (isCountPresent(botaSettings.getInt(Configs.STATS_DL_RANGE_ERROR, 0))) {
            linkedHashMap.put("416", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_RANGE_ERROR, 0)));
            z = true;
        }
        if (isCountPresent(botaSettings.getInt(Configs.STATS_DL_TOOMANYREQUEST_ERROR, 0))) {
            linkedHashMap.put("429", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_TOOMANYREQUEST_ERROR, 0)));
            z = true;
        }
        if (isCountPresent(botaSettings.getInt(Configs.STATS_DL_SU_ERROR, 0))) {
            linkedHashMap.put("503", Integer.valueOf(botaSettings.getInt(Configs.STATS_DL_SU_ERROR, 0)));
        } else {
            z2 = z;
        }
        JSONObject jsonObjectFromMap = UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "DownloadErrorCodes");
        if (z2) {
            botaSettings.setString(Configs.STATS_DL_ERROR_CODES, jsonObjectFromMap.toString());
        } else {
            botaSettings.setString(Configs.STATS_DL_ERROR_CODES, null);
        }
    }

    public static final int getTotalInstallTime(BotaSettings botaSettings) {
        return (int) TimeUnit.MILLISECONDS.toSeconds(botaSettings.getLong(Configs.STATS_INSTALL_TOTAL_TIME, 0L));
    }

    public static final void setTotalInstallTimeForClassic(BotaSettings botaSettings) {
        botaSettings.setLong(Configs.STATS_INSTALL_TOTAL_TIME, botaSettings.getLong(Configs.STATS_INSTALL_COMPLETED, 0L) - botaSettings.getLong(Configs.STATS_INSTALL_STARTED, 0L));
    }

    public static final void setTotalInstallTime(BotaSettings botaSettings) {
        botaSettings.setLong(Configs.STATS_INSTALL_TOTAL_TIME, botaSettings.getLong(Configs.STATS_INSTALL_TOTAL_TIME, 0L) + (System.currentTimeMillis() - botaSettings.getLong(Configs.STATS_AB_LAST_INSTALLATION_START_TIME, 0L)));
    }

    public static String getDownloadPercentage(long j, long j2) {
        if (j <= 0) {
            return null;
        }
        return String.format("%.1f", Double.valueOf(Math.min((j * 100) / j2, 100.0d)));
    }

    public static void setAndBuildDownloadStats(BotaSettings botaSettings, Context context, ConnectivityManager connectivityManager) {
        setDownloadEndTime(botaSettings);
        long j = botaSettings.getLong(Configs.STATS_DL_TOTAL_SIZE, 0L);
        if (j > 0) {
            StatsDownload.buildDownloadCompletedOrFailedStats(botaSettings, context, connectivityManager, j);
        }
    }

    public static final String buildStats(BotaSettings botaSettings, String str, String str2, String str3, String str4, String str5, String str6) {
        JSONObject buildPreDownloadStats;
        new JSONObject();
        if (str3.equals(ApplicationEnv.PackageState.Notified.name()) || str3.equals(ApplicationEnv.PackageState.RequestPermission.name())) {
            buildPreDownloadStats = StatsBuilder.buildPreDownloadStats(botaSettings);
        } else if (str3.equals(ApplicationEnv.PackageState.GettingDescriptor.name()) || str3.equals(ApplicationEnv.PackageState.GettingPackage.name()) || str3.equals(ApplicationEnv.PackageState.VerifyAllocateSpace.name())) {
            buildPreDownloadStats = StatsBuilder.buildDownloadStats(botaSettings);
        } else if (str3.equals(ApplicationEnv.PackageState.ABApplyingPatch.name()) || str3.equals(ApplicationEnv.PackageState.Querying.name()) || str3.equals(ApplicationEnv.PackageState.QueryingInstall.name()) || str3.equals(ApplicationEnv.PackageState.Upgrading.name())) {
            buildPreDownloadStats = StatsBuilder.buildInstallStats(botaSettings);
        } else if (str3.equals(ApplicationEnv.PackageState.MergePending.name()) || str3.equals(ApplicationEnv.PackageState.MergeRestart.name())) {
            buildPreDownloadStats = StatsBuilder.buildMergeStats(botaSettings);
        } else if (!str3.equals(ApplicationEnv.PackageState.Result.name())) {
            return null;
        } else {
            buildPreDownloadStats = StatsBuilder.buildInstallStats(botaSettings);
            if (buildPreDownloadStats.optJSONObject("downloadStats").optLong("downloadStartTime") == 0) {
                buildPreDownloadStats.remove("downloadStats");
            }
            if (buildPreDownloadStats.optJSONObject("installStats").optLong("InstallNotifiedTime") == 0) {
                buildPreDownloadStats.remove("installStats");
            }
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            linkedHashMap.put("forceUpgradeTimeExpired", Boolean.valueOf(botaSettings.getBoolean(Configs.FORCE_UPGRADE_TIME_COMPLETED)));
            linkedHashMap.put("upgradeSource", str4);
            if (str2 == null) {
                str2 = "NOT_AVAILABLE";
            }
            linkedHashMap.put("reportingTags", str2);
            linkedHashMap.put("network", str);
            linkedHashMap.put("currentState", str3);
            linkedHashMap.put("trackingId", str6);
            linkedHashMap.put("stateStats", getStateStats(str5, buildPreDownloadStats.optJSONObject("installStats"), buildPreDownloadStats.optJSONObject("downloadStats"), buildPreDownloadStats.optJSONObject("preDownloadStats")));
            linkedHashMap.put("mergeStats", StatsBuilder.getMergeStats(botaSettings));
            JSONObject jsonObjectFromMap = UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsHelper.buildStats");
            Iterator<String> keys = jsonObjectFromMap.keys();
            while (keys.hasNext()) {
                String next = keys.next();
                try {
                    buildPreDownloadStats.put(next, jsonObjectFromMap.get(next));
                } catch (JSONException e) {
                    Logger.error("OtaApp", "StatsHelper.buildStats caught exception while getting key: " + next + " :" + e);
                }
            }
            botaSettings.clearStats();
        }
        botaSettings.setString(Configs.STATS_OTA, buildPreDownloadStats.toString());
        return buildPreDownloadStats.toString();
    }

    private static JSONObject getStateStats(String str, JSONObject jSONObject, JSONObject jSONObject2, JSONObject jSONObject3) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put(ApplicationEnv.PackageState.Notified.name(), getStateStatsObject(jSONObject3 != null ? jSONObject3.optLong("packageNotifiedTime") : 0L, "PROCESSING"));
        linkedHashMap.put(ApplicationEnv.PackageState.RequestPermission.name(), getStateStatsObject(jSONObject3 != null ? jSONObject3.optLong("downloadNotifiedTime") : 0L, "PROCESSING"));
        linkedHashMap.put(ApplicationEnv.PackageState.GettingDescriptor.name(), getStateStatsObject(jSONObject3 != null ? jSONObject3.optLong("downloadAcceptedTime") : 0L, "PROCESSING"));
        linkedHashMap.put(ApplicationEnv.PackageState.GettingPackage.name(), getStateStatsObject(jSONObject2 != null ? jSONObject2.optLong("downloadStartTime") : 0L, "PROCESSING"));
        linkedHashMap.put(ApplicationEnv.PackageState.Querying.name(), getStateStatsObject(jSONObject != null ? jSONObject.optLong("InstallNotifiedTime") : 0L, "PROCESSING"));
        linkedHashMap.put(ApplicationEnv.PackageState.Upgrading.name(), getStateStatsObject(jSONObject != null ? jSONObject.optLong("InstallAcceptedTime") : 0L, "PROCESSING"));
        linkedHashMap.put(ApplicationEnv.PackageState.Result.name(), getStateStatsObject(jSONObject != null ? jSONObject.optLong("InstallEndTime") : 0L, str));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsHelper.getStateStats");
    }

    private static JSONObject getStateStatsObject(long j, String str) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("time", Long.valueOf(j));
        linkedHashMap.put("status", str);
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "StatsHelper.getStateStatsObject");
    }

    public static JSONObject getSmartUpdateJSON() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        BotaSettings botaSettings = new BotaSettings();
        linkedHashMap.put("smartUpdateEnabled", Boolean.valueOf(SmartUpdateUtils.isSmartUpdateEnabledByUser(botaSettings)));
        if (SmartUpdateUtils.isSmartUpdateEnabledByUser(botaSettings)) {
            linkedHashMap.put("smartUpdateTimeSlot", botaSettings.getConfig(Configs.SMART_UPDATE_TIME_SLOT, ""));
            linkedHashMap.put("smartUpdateOSEnabledByUser", botaSettings.getConfig(Configs.SMART_UPDATE_OS_ENABLE_BY_USER, "false"));
            linkedHashMap.put("smartUpdateMREnabledByUser", botaSettings.getConfig(Configs.SMART_UPDATE_MR_ENABLE_BY_USER, "false"));
        }
        linkedHashMap.put("smartUpdateOverriddenBitmap", Integer.toString(SmartUpdateUtils.getOverriddenSmartUpdateBitMap(), 10));
        linkedHashMap.put("smartUpdateLaunchMode", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_LAUNCH_MODE, ""));
        linkedHashMap.put("smartUpdateEnabledViaOOB", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_ENABLED_VIA_OOB, "false"));
        linkedHashMap.put("smartUpdateEnabledVia", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_ENABLED_VIA, ""));
        linkedHashMap.put("smartUpdateDisabledVia", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_DISABLED_VIA, ""));
        linkedHashMap.put("smartUpdateDNDSelected", botaSettings.getConfig(Configs.SMART_UPDATE_POP_UP_DISABLE, "false"));
        linkedHashMap.put("smartUpdateVisitedButDisabled", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_VISITED_BUT_DISABLED, "0"));
        linkedHashMap.put("smartUpdatePopupShowCount", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_POPUP_SHOWN_COUNT, "0"));
        linkedHashMap.put("smartUpdatePopupGetStartedCount", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_POP_UP_GET_STARTED, "0"));
        linkedHashMap.put("smartUpdatePopupNotNowCount", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_POP_UP_NOT_NOW, "0"));
        linkedHashMap.put("smartUpdateBottomSheetNotNowCount", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_BOTTOM_SHEET_NOT_NOW, "0"));
        linkedHashMap.put("smartUpdateUserVisitedSuggestions", botaSettings.getConfig(Configs.STATS_SMART_UPDATE_USER_VISITED_SUGGESTIONS, "0"));
        linkedHashMap.put("smartUpdateDisabledAfterInstall", botaSettings.getConfig(Configs.STATS_DISABLED_SMART_UPDATE_AFTER_INSTALL, "false"));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "getSmartUpdateJSON");
    }

    public static JSONObject getEOLStatsJSON() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        BotaSettings botaSettings = new BotaSettings();
        linkedHashMap.put("eolVisitCount", Integer.valueOf(botaSettings.getInt(Configs.EOL_VISIT_COUNT, 0)));
        linkedHashMap.put("promoLinkClickCount", Integer.valueOf(botaSettings.getInt(Configs.PROMOTIONAL_LINK_CLICK_COUNT, 0)));
        linkedHashMap.put("nonPromoLinkClickCount", Integer.valueOf(botaSettings.getInt(Configs.NON_PROMOTIONAL_LINK_CLICK_COUNT, 0)));
        linkedHashMap.put("totalTimeSpendOnEOLScreenInSecs", Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(botaSettings.getLong(Configs.TOTAL_TIME_SPEND_ON_EOL_SCREEN, 0L))));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "getEOLStatsJSON");
    }

    public static JSONObject getHistoryStatsJSON() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        BotaSettings botaSettings = new BotaSettings();
        linkedHashMap.put("historyVisitCount", Integer.valueOf(botaSettings.getInt(Configs.HISTORY_VISIT_COUNT, 0)));
        linkedHashMap.put("historyTabClickCount", Integer.valueOf(botaSettings.getInt(Configs.HISTORY_TAB_CLICK_COUNT, 0)));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "getHistoryStatsJSON");
    }

    public static JSONObject getWhyUpdateMattersStatsJSON() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("whyUpdateMattersOSLinkClickCount", Integer.valueOf(new BotaSettings().getInt(Configs.WHY_UPDATE_MATTERS_OS_LINK_COUNT, 0)));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "getWhyUpdateMattersStatsJSON");
    }

    public static String getUpdatePreferenceStats(JSONObject jSONObject, Configs configs) {
        JSONObject jsonObject;
        try {
            jsonObject = new BotaSettings().getJsonObject(configs);
        } catch (Exception e) {
            Logger.error("OtaApp", "saveAndSendStats Exception = " + e);
        }
        if (jSONObject == null) {
            return null;
        }
        if (jsonObject == null) {
            return jSONObject.toString();
        }
        if (!jsonObject.toString().equals(jSONObject.toString())) {
            return jSONObject.toString();
        }
        return null;
    }

    public static void storeExtraInfoStats() {
        BotaSettings botaSettings = new BotaSettings();
        botaSettings.setJsonObject(Configs.STATS_SMART_UPDATE_STORED, getSmartUpdateJSON());
        botaSettings.setJsonObject(Configs.STATS_EOL_STORED, getEOLStatsJSON());
        botaSettings.setJsonObject(Configs.STATS_HISTORY_STORED, getHistoryStatsJSON());
        botaSettings.setJsonObject(Configs.STATS_WHY_UPDATE_MATTERS_STORED, getWhyUpdateMattersStatsJSON());
    }

    public static void setStreamingStats(BotaSettings botaSettings, String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            JSONObject optJSONObject = jSONObject.optJSONObject("processingRequired");
            JSONObject optJSONObject2 = jSONObject.optJSONObject("processingNotRequired");
            if (optJSONObject != null) {
                botaSettings.setString(Configs.STATS_DL_VIA_WIFI, String.valueOf(incrementDownloadPercentageViaThisNetwork(optJSONObject.optString("downloadPercentageWifi"), botaSettings.getString(Configs.STATS_DL_VIA_WIFI)).doubleValue()));
                botaSettings.setString(Configs.STATS_DL_VIA_CELLULAR, String.valueOf(incrementDownloadPercentageViaThisNetwork(optJSONObject.optString("downloadPercentageCellular"), botaSettings.getString(Configs.STATS_DL_VIA_CELLULAR)).doubleValue()));
                botaSettings.setString(Configs.STATS_DL_VIA_ADMINAPN, String.valueOf(incrementDownloadPercentageViaThisNetwork(optJSONObject.optString("downloadPercentageAdminApn"), botaSettings.getString(Configs.STATS_DL_VIA_ADMINAPN)).doubleValue()));
                botaSettings.setString(Configs.STATS_DL_VIA_PROXY, String.valueOf(incrementDownloadPercentageViaThisNetwork(optJSONObject.optString("downloadPercentageProxy"), botaSettings.getString(Configs.STATS_DL_VIA_PROXY)).doubleValue()));
                botaSettings.setLong(Configs.STATS_DL_TOTAL_SIZE, optJSONObject.optLong("downloadTotalSize") + botaSettings.getLong(Configs.STATS_DL_TOTAL_SIZE, 0L));
                botaSettings.setLong(Configs.STATS_DL_ACTUAL_DOWNLOAD_TIME, optJSONObject.optLong("downloadTotalTime") + botaSettings.getLong(Configs.STATS_DL_ACTUAL_DOWNLOAD_TIME, 0L));
                int optInt = optJSONObject.optInt(UpgradeUtilConstants.KEY_EXTRA_MODEM_UPDATE_STATUS_CODE);
                if (optInt != UpdaterEngineErrorCodes.ERROR_NETWORK && optInt != UpdaterEngineErrorCodes.K_DOWNLOAD_TRANSFER_ERROR) {
                    StatsDownload.downloadErrorcode(botaSettings, optInt);
                    setDownloadErrorCodes(botaSettings);
                    setNetworkUsedByDownload(botaSettings);
                    setDownloadEndTime(botaSettings);
                    botaSettings.setLong(Configs.STATS_INSTALL_NOTIFIED, botaSettings.getLong(Configs.STATS_DL_STARTED, 0L));
                    botaSettings.setLong(Configs.STATS_INSTALL_ACCEPTED, botaSettings.getLong(Configs.STATS_DL_STARTED, 0L));
                    botaSettings.setLong(Configs.STATS_INSTALL_STARTED, botaSettings.getLong(Configs.STATS_DL_STARTED, 0L));
                }
                setDownloadSuspendedCount(botaSettings);
                setNetworkUsedByDownload(botaSettings);
                setDownloadEndTime(botaSettings);
                botaSettings.setLong(Configs.STATS_INSTALL_NOTIFIED, botaSettings.getLong(Configs.STATS_DL_STARTED, 0L));
                botaSettings.setLong(Configs.STATS_INSTALL_ACCEPTED, botaSettings.getLong(Configs.STATS_DL_STARTED, 0L));
                botaSettings.setLong(Configs.STATS_INSTALL_STARTED, botaSettings.getLong(Configs.STATS_DL_STARTED, 0L));
            }
            if (optJSONObject2 != null) {
                botaSettings.setJsonObject(Configs.STATS_UE_SPECIFIC, optJSONObject2);
            }
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in StatsHelper, setStreamingStats: " + e);
        }
    }
}
