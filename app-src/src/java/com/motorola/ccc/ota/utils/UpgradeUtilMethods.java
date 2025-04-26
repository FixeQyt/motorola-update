package com.motorola.ccc.ota.utils;

import android.content.Context;
import android.content.Intent;
import android.os.UserManager;
import android.text.TextUtils;
import android.webkit.URLUtil;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpgradeUtilMethods {
    public static final void sendCheckForUpdate(Context context, boolean z, int i) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE);
        intent.putExtra(UpgradeUtilConstants.KEY_BOOTSTRAP, z);
        intent.putExtra(UpgradeUtilConstants.KEY_REQUESTID, i);
        intent.putExtra(UpgradeUtilConstants.KEY_INTERACTIVE, false);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final void sendCheckForUpdate(Context context, boolean z, int i, boolean z2) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE);
        intent.putExtra(UpgradeUtilConstants.KEY_BOOTSTRAP, z);
        intent.putExtra(UpgradeUtilConstants.KEY_REQUESTID, i);
        intent.putExtra(UpgradeUtilConstants.KEY_INTERACTIVE, z2);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final boolean isBootstrap(Intent intent) {
        return intent.getBooleanExtra(UpgradeUtilConstants.KEY_BOOTSTRAP, false);
    }

    public static final int getRequestId(Intent intent) {
        return intent.getIntExtra(UpgradeUtilConstants.KEY_REQUESTID, -1);
    }

    public static final boolean isInteractive(Intent intent) {
        if (getRequestId(intent) == 0) {
            return intent.getBooleanExtra(UpgradeUtilConstants.KEY_INTERACTIVE, true);
        }
        return intent.getBooleanExtra(UpgradeUtilConstants.KEY_INTERACTIVE, false);
    }

    public static final void sendCheckForUpdateResponse(Context context, UpgradeUtils.Error error, int i, boolean z, String str) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE_RESPONSE);
        intent.putExtra(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE, error.toString());
        intent.putExtra(UpgradeUtilConstants.KEY_REQUESTID, i);
        intent.putExtra(UpgradeUtilConstants.KEY_BOOTSTRAP, z);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str);
        intent.putExtra(UpgradeUtilConstants.KEY_OTA_UPDATE_PLANNED, URLUtil.isHttpsUrl(new BotaSettings().getString(Configs.ADVANCE_NOTICE_URL)));
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final void sendActionUpdateResponse(Context context, UpgradeUtils.Error error, int i, boolean z, String str) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_ACTION_UPDATE_RESPONSE);
        intent.putExtra(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE, error.toString());
        intent.putExtra(UpgradeUtilConstants.KEY_REQUESTID, i);
        intent.putExtra(UpgradeUtilConstants.KEY_BOOTSTRAP, z);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final void sendUpgradeStatus(Context context, String str, String str2, String str3, String str4, String str5, String str6, long j, boolean z, String str7, boolean z2, String str8) {
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_UPGRADE_UPDATE_STATUS);
        intent.putExtra(UpgradeUtilConstants.KEY_SOURCE_SHA1, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_DESTINAION_SHA1, str3);
        intent.putExtra(UpgradeUtilConstants.KEY_UPDATE_INFO, str4);
        intent.putExtra(UpgradeUtilConstants.KEY_CURRENT_STATE, str);
        intent.putExtra(UpgradeUtilConstants.KEY_RELEASE_NOTES, str5);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_SOURCE, str6);
        intent.putExtra(UpgradeUtilConstants.KEY_TIMESTAMP, j);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_WIFIONLY, z);
        intent.putExtra(UpgradeUtilConstants.KEY_DISPLAY_VERSION, str7);
        intent.putExtra(UpgradeUtilConstants.KEY_SILENT_OTA, z2);
        intent.putExtra(UpgradeUtilConstants.KEY_UPDATE_TYPE, str8);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final UpgradeUtils.Error errorFromIntent(Intent intent) {
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE_RESPONSE)) {
            return UpgradeUtils.Error.valueOf(intent.getStringExtra(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE));
        }
        return null;
    }

    public static final void sendUpdateNotification(Context context, String str, String str2, String str3, String str4) {
        context.sendBroadcast(getUpdateNotificationIntent(str, str2, str3, str4), Permissions.INTERACT_OTA_SERVICE);
    }

    public static Intent getUpdateNotificationIntent(String str, String str2, String str3, String str4) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str);
        intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, str3);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_OPT_CHECK, str4);
        return intent;
    }

    public static void sendStartDownloadProgressFragment(Context context, String str, String str2, String str3) {
        Intent intent = new Intent(UpgradeUtilConstants.START_DOWNLOAD_PROGRESS_FRAGMENT);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, str3);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static void sendStartBackgroundInstallationFragment(Context context, String str, String str2, String str3) {
        Intent intent = new Intent(UpgradeUtilConstants.START_BACKGROUND_INSTALLATION_FRAGMENT);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, str3);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final void sendUpdaterStateReset(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.UPGRADE_UPDATER_STATE_CLEAR));
    }

    public static final void cancelUpdateNotification(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.UPGRADE_UPDATER_UPDATE_NOTIFICATION_CLEAR));
    }

    public static final void cancelDownloadNotification(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.UPGRADE_UPDATER_DOWNLOAD_NOTIFICATION_CLEAR));
    }

    public static final void cancelBGInstallNotification(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.UPGRADE_UPDATER_BG_INSTALL_NOTIFICATION_CLEAR));
    }

    public static void cancelRestartNotification(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.UPGRADE_UPDATER_RESTART_NOTIFICATION_CLEAR));
    }

    public static final void sendUpdateStatus(Context context, String str, String str2, String str3, boolean z, int i) {
        Logger.debug("OtaApp", "sendUpdateStatus: broadcast intent to showupdatestatusactivity");
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_UPDATE_STATUS);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str2);
        if (str3 != null) {
            intent.putExtra(UpgradeUtilConstants.KEY_REASON, str3);
        }
        intent.putExtra(UpgradeUtilConstants.KEY_UPDATE_STATUS, z);
        intent.putExtra(UpgradeUtilConstants.KEY_UPDATE_FAILURE_COUNT, i);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final String versionFromIntent(Intent intent) {
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION) || intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_RESPONSE) || intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS) || intent.getAction().equals(UpgradeUtilConstants.UPGRADE_LAUNCH_UPGRADE) || intent.getAction().equals(UpgradeUtilConstants.UPGRADE_EXECUTE_UPGRADE)) {
            return intent.getStringExtra(UpgradeUtilConstants.KEY_VERSION);
        }
        return null;
    }

    public static final String locationTypeFromIntent(Intent intent) {
        return intent.getStringExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE);
    }

    public static final void sendUpdateNotificationResponse(Context context, String str, boolean z, String str2, String str3) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_RESPONSE);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str);
        intent.putExtra(UpgradeUtilConstants.KEY_RESPONSE_ACTION, z);
        intent.putExtra(UpgradeUtilConstants.KEY_RESPONSE_FLAVOUR, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_MODE, str3);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final void sendDownloadNotificationResponse(Context context, UpgradeUtils.DownloadStatus downloadStatus) {
        BotaSettings botaSettings = new BotaSettings();
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_DOWNLOAD_NOTIFICATION_RESPONSE);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, botaSettings.getString(Configs.METADATA));
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS, downloadStatus.toString());
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final void sendUserResponseDuringBackgroundInstallation(Context context, UpgradeUtils.DownloadStatus downloadStatus) {
        BotaSettings botaSettings = new BotaSettings();
        Intent intent = new Intent(UpgradeUtilConstants.USER_BACKGROUND_INSTALL_RESPONSE);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, botaSettings.getString(Configs.METADATA));
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS, downloadStatus.toString());
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final boolean responseActionFromIntent(Intent intent) {
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_RESPONSE)) {
            return intent.getBooleanExtra(UpgradeUtilConstants.KEY_RESPONSE_ACTION, false);
        }
        return false;
    }

    public static final String responseFlavourFromIntent(Intent intent) {
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_RESPONSE)) {
            return intent.getStringExtra(UpgradeUtilConstants.KEY_RESPONSE_FLAVOUR);
        }
        return null;
    }

    public static final void sendUpdateDownloadStatus(Context context, String str, UpgradeUtils.DownloadStatus downloadStatus, long j, long j2, String str2, String str3, String str4, int i, boolean z) {
        context.sendBroadcast(getUpdateDownloadStatusIntent(str, downloadStatus.toString(), j, j2, str2, str3, str4, i, z), Permissions.INTERACT_OTA_SERVICE);
    }

    public static Intent getUpdateDownloadStatusIntent(String str, String str2, long j, long j2, String str3, String str4, String str5, int i, boolean z) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_BYTES_RECEIVED, j);
        intent.putExtra(UpgradeUtilConstants.KEY_BYTES_TOTAL, j2);
        intent.putExtra(UpgradeUtilConstants.KEY_FILE_LOCATION, str3);
        intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, str5);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str4);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED, i);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_ON_WIFI, z);
        intent.putExtra(UpdaterUtils.KEY_CHECK_FOR_MAP, true);
        return intent;
    }

    public static final void sendUpdateDownloadStatusProgress(Context context, String str, long j, long j2, String str2) {
        sendUpdateDownloadStatus(context, str, UpgradeUtils.DownloadStatus.STATUS_TEMP_OK, j, j2, null, null, str2, 0, false);
    }

    private static Intent getAllocateFreeSpaceIntent(String str, long j) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS, str);
        intent.putExtra(UpgradeUtilConstants.KEY_FREE_SPACE_REQUIRED, j);
        return intent;
    }

    private static void sendAllocateFreeSpaceIntent(Context context, UpgradeUtils.DownloadStatus downloadStatus, long j) {
        context.sendBroadcast(getAllocateFreeSpaceIntent(downloadStatus.toString(), j), Permissions.INTERACT_OTA_SERVICE);
    }

    public static final long getFreeSpaceFromIntent(Intent intent) {
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS)) {
            long longExtra = intent.getLongExtra(UpgradeUtilConstants.KEY_FREE_SPACE_REQUIRED, 0L);
            Logger.debug("OtaApp", "freeSpace = " + longExtra);
            return longExtra;
        }
        return 0L;
    }

    public static final void showAllocateFreeSpaceDialog(Context context, UpgradeUtils.DownloadStatus downloadStatus, long j) {
        sendAllocateFreeSpaceIntent(context, downloadStatus, j);
    }

    public static final void sendUpdateDownloadStatusError(Context context, String str, UpgradeUtils.DownloadStatus downloadStatus) {
        sendUpdateDownloadStatus(context, str, downloadStatus, 0L, 0L, null, null, null, 0, false);
    }

    public static final void sendUpdateDownloadStatusError(Context context, String str, UpgradeUtils.DownloadStatus downloadStatus, String str2) {
        sendUpdateDownloadStatus(context, str, downloadStatus, 0L, 0L, null, str2, null, 0, false);
    }

    public static final void sendUpdateDownloadStatusError(Context context, String str, UpgradeUtils.DownloadStatus downloadStatus, String str2, String str3) {
        sendUpdateDownloadStatus(context, str, downloadStatus, 0L, 0L, null, str2, str3, 0, false);
    }

    public static final void sendUpdateDownloadStatusSuccess(Context context, String str, long j, String str2, String str3, String str4) {
        sendUpdateDownloadStatus(context, str, UpgradeUtils.DownloadStatus.STATUS_OK, j, j, str2, str3, str4, 0, false);
    }

    public static final void sendUpdateDownloadStatusRetried(Context context, String str, long j, long j2) {
        sendUpdateDownloadStatus(context, str, UpgradeUtils.DownloadStatus.STATUS_TEMP_OK, j, j2, null, null, null, 1, false);
    }

    public static final void sendUpdateDownloadStatusSuspended(Context context, String str, long j, long j2, String str2, boolean z) {
        sendUpdateDownloadStatus(context, str, UpgradeUtils.DownloadStatus.STATUS_TEMP_OK, j, j2, null, null, str2, -1, z);
    }

    public static final void sendVerifyPayloadMetadataFileDownloadStatus(Context context, UpgradeUtils.DownloadStatus downloadStatus, String str) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_UPDATE_VERIFY_PAYLOAD_METADATA_DOWNLOAD_STATUS);
        intent.putExtra(UpgradeUtilConstants.KEY_COMPATIBILITY_STATUS, downloadStatus.toString());
        intent.putExtra(UpgradeUtilConstants.KEY_FREE_SPACE_REQUIRED, str);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final int deferredFromIntent(Intent intent) {
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS)) {
            return intent.getIntExtra(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED, 0);
        }
        return 0;
    }

    public static final boolean downloadOnWifiFromIntent(Intent intent) {
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS)) {
            return intent.getBooleanExtra(UpgradeUtilConstants.KEY_DOWNLOAD_ON_WIFI, false);
        }
        return false;
    }

    public static final UpgradeUtils.DownloadStatus downloadStatusFromIntent(Intent intent) {
        if (TextUtils.isEmpty(intent.getStringExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS))) {
            return null;
        }
        return UpgradeUtils.DownloadStatus.valueOf(intent.getStringExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS));
    }

    public static final long receivedBytesFromIntent(Intent intent) {
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS)) {
            return intent.getLongExtra(UpgradeUtilConstants.KEY_BYTES_RECEIVED, -1L);
        }
        return -1L;
    }

    public static final long totalBytesFromIntent(Intent intent) {
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS)) {
            return intent.getLongExtra(UpgradeUtilConstants.KEY_BYTES_TOTAL, -1L);
        }
        return -1L;
    }

    public static final void sendUpgradeLaunchProceed(Context context, String str, boolean z, String str2) {
        context.sendBroadcast(getUpgradeLaunchProceed(context, str, z, false, str2), Permissions.INTERACT_OTA_SERVICE);
    }

    public static Intent getUpgradeLaunchProceed(Context context, String str, boolean z, boolean z2, String str2) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_LAUNCH_UPGRADE);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str);
        intent.putExtra(UpgradeUtilConstants.KEY_UPGRADE_LAUNCH_PROCEED, z);
        intent.putExtra(UpgradeUtilConstants.KEY_INSTALL_MODE, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_CHECK_FOR_LOW_BATTERY, z2);
        return intent;
    }

    public static final boolean proceedFromIntent(Intent intent) {
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_LAUNCH_UPGRADE)) {
            return intent.getBooleanExtra(UpgradeUtilConstants.KEY_UPGRADE_LAUNCH_PROCEED, false);
        }
        return false;
    }

    public static final void sendUpgradeExecute(Context context, String str, String str2, String str3) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_EXECUTE_UPGRADE);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str);
        intent.putExtra(UpgradeUtilConstants.KEY_FILE_LOCATION, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str3);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final void sendMergeRestartIntent(Context context) {
        context.sendBroadcast(new Intent(UpgradeUtilConstants.MERGE_RESTART_UPGRADE), Permissions.INTERACT_OTA_SERVICE);
    }

    public static void sendActionVerifyPayloadStatus(Context context, String str, UpgradeUtils.DownloadStatus downloadStatus, String str2) {
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_VERIFY_PAYLOAD_STATUS);
        intent.putExtra(UpgradeUtilConstants.KEY_REASON, str);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS, downloadStatus.toString());
        intent.putExtra(UpgradeUtilConstants.KEY_UPGRADE_STATUS, str2);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static void sendActionVerifyPayloadStatus(Context context, boolean z) {
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_VAB_VERIFY_STATUS);
        intent.putExtra(UpgradeUtilConstants.KEY_VAB_VALIDATION_STATUS, z);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static void sendActionAllocateSpaceResult(Context context, long j) {
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_VAB_ALLOCATE_SPACE_RESULT);
        intent.putExtra(UpgradeUtilConstants.KEY_ALLOCATE_SPACE_RESULT, j);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final void sendPollIntent(Context context) {
        UpdaterUtils.scheduleWorkManager(context);
    }

    public static void scheduleModemPolling() {
        if (BuildPropReader.isATT() || TextUtils.isEmpty(BuildPropReader.getMCFGConfigVersion())) {
            return;
        }
        BotaSettings botaSettings = new BotaSettings();
        if (botaSettings.getInt(Configs.MODEM_POLLING_COUNT, 0) <= botaSettings.getInt(Configs.MAX_MODEM_POLLING_COUNT, 7)) {
            sendModemPollIntent(OtaApplication.getGlobalContext());
        } else {
            shutdownModemPolling();
        }
    }

    public static void shutdownModemPolling() {
        UpdaterUtils.shutDownPolling(OtaApplication.getGlobalContext(), PMUtils.MODEM_UNIQUE_WORK_NAME);
    }

    public static final void sendModemPollIntent(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.ACTION_MODEM_UPGRADE_POLL));
    }

    public static final void sendOtaServiceStopIntent(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.ACTION_STOP_OTA_SERVICE));
    }

    public static final void registerWithWiFiDiscoveryManager(Context context, long j, boolean z, boolean z2) {
        Intent intent = new Intent(UpgradeUtilConstants.REGISTER_WIFI_DISCOVER_MANAGER);
        intent.putExtra(UpgradeUtilConstants.KEY_DISCOVERY_TIME, j);
        intent.putExtra(UpgradeUtilConstants.KEY_ONLY_ON_NETWORK, z);
        intent.putExtra(UpgradeUtilConstants.KEY_ALLOW_ON_ROAMING, z2);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final int getDiscoverTime(Intent intent) {
        return (int) intent.getLongExtra(UpgradeUtilConstants.KEY_DISCOVERY_TIME, -1L);
    }

    public static final void unRegisterWithWiFiDiscoveryManager(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.UNREGISTER_WIFI_DISCOVER_MANAGER));
    }

    public static final void sendDiscoverTimerExpiryIntent(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.WIFI_DISCOVER_TIMER_EXPIRY));
    }

    public static void sendSystemUpdateAvailableNotification(Context context, String str) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_AVAILABLE);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static void sendInstallSystemUpdateAvailableNotification(Context context, String str, String str2, String str3) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str3);
        intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final void registerWithForceUpgradeManager(Context context, long j) {
        Intent intent = new Intent(UpgradeUtilConstants.REGISTER_FORCE_UPGRADE_MANAGER);
        intent.putExtra(UpgradeUtilConstants.KEY_FORCE_UPGRADE_TIME, j);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final int getForceUpgradeTime(Intent intent) {
        return (int) intent.getLongExtra(UpgradeUtilConstants.KEY_FORCE_UPGRADE_TIME, -1L);
    }

    public static final void unRegisterWithForceUpgradeManager(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.UNREGISTER_FORCE_UPGRADE_MANAGER));
    }

    public static final void sendForceUpgradeTimerExpiryIntent(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.FORCE_UPGRADE_TIMER_EXPIRY));
    }

    public static void sendDownloadCompletedToSettings(Context context) {
        Intent intent = new Intent(UpgradeUtilConstants.DOWNLOAD_COMPLETED_TO_SETTINGS);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_COMPLETED, System.currentTimeMillis());
        Logger.debug("OtaApp", "sendDownloadCompletedToSettings");
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static void sendDownloadNotifiedToSettings(Context context) {
        Intent intent = new Intent(UpgradeUtilConstants.DOWNLOAD_NOTIFIED_TO_SETTINGS);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_NOTIFIED, System.currentTimeMillis());
        Logger.debug("OtaApp", "sendDownloadNotifiedToSettings");
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static void moveFotaToGettingDescriptorState(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.MOVE_FOTA_TO_GETTING_DESCRIPTOR));
    }

    public static final void sendStartRestartActivity(Context context, String str, String str2, String str3) {
        context.sendBroadcast(getStartRestartActivityIntent(str, str2, str3), Permissions.INTERACT_OTA_SERVICE);
    }

    public static Intent getStartRestartActivityIntent(String str, String str2, String str3) {
        Intent intent = new Intent(UpgradeUtilConstants.START_RESTART_ACTIVITY_INTENT);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str);
        intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, str3);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str2);
        intent.putExtra(UpdaterUtils.KEY_CHECK_FOR_MAP, true);
        return intent;
    }

    public static void sendStartMergeRestartActivityIntent(Context context) {
        context.sendBroadcast(new Intent(UpgradeUtilConstants.START_MERGE_RESTART_ACTIVITY_INTENT), Permissions.INTERACT_OTA_SERVICE);
    }

    public static void sendSystemRestartNotificationForABUpdate(Context context, String str, String str2, String str3) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_RESTART_NOTIFICATION);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, str3);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static void sendUpgradeStatusSuspended(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.ACTION_AB_APPLY_PAYLOAD_SUSPEND));
    }

    public static void cancelOta(String str, String str2) {
        Intent intent = new Intent(UpgradeUtilConstants.CANCEL_UPDATE);
        intent.putExtra(UpgradeUtilConstants.KEY_REASON, str);
        intent.putExtra(UpgradeUtilConstants.KEY_UPGRADE_STATUS, str2);
        BroadcastUtils.sendLocalBroadcast(OtaApplication.getGlobalContext(), intent);
    }

    public static void sendActionBatteryLow() {
        BroadcastUtils.sendLocalBroadcast(OtaApplication.getGlobalContext(), new Intent(UpgradeUtilConstants.ACTION_BATTERY_LOW));
    }

    public static void sendActionDataSaverDuringABStreaming() {
        BroadcastUtils.sendLocalBroadcast(OtaApplication.getGlobalContext(), new Intent(UpgradeUtilConstants.ACTION_DATA_SAVER_DURING_AB_STREAMING));
    }

    public static void sendActionMetadataOverride(String str) {
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_OVERRIDE_METADATA);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str);
        BroadcastUtils.sendLocalBroadcast(OtaApplication.getGlobalContext(), intent);
    }

    public static void informPendingRebootInfo(Context context, long j) {
        Intent intent = new Intent(UpgradeUtilConstants.AB_UPGRADE_RESTART_PENDING);
        intent.putExtra(UpgradeUtilConstants.KEY_FORCE_INSTALL_TIME, j);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static boolean isSystemUser(Context context) {
        if (((UserManager) context.getSystemService("user")).isSystemUser()) {
            return true;
        }
        Logger.debug("OtaApp", "UpdgradeUtilMethods:not a system user");
        return false;
    }

    public static String getStatusVerifyResult(String str) {
        String str2 = "no signature in file (no footer)";
        if (!str.contains("no signature in file (no footer)")) {
            str2 = "no signature in file (bad footer)";
            if (!str.contains("no signature in file (bad footer)")) {
                if (str.contains("EOCD marker found after start of EOCD")) {
                    return "EOCD marker found after start of EOCD(Signature info improper)";
                }
                str2 = "signedData is null";
                if (!str.contains("signedData is null")) {
                    if (str.contains("encCerts is empty")) {
                        return "encCerts is empty(encoded certificate in signature is empty)";
                    }
                    str2 = "signature contains no certificates";
                    if (!str.contains("signature contains no certificates")) {
                        if (str.contains("no signer infos!")) {
                            return "no signer infos!(no signer infos in encoded certificates)";
                        }
                        str2 = "signature doesn't match any trusted key";
                        if (!str.contains("signature doesn't match any trusted key")) {
                            str2 = "verification was interrupted";
                            if (!str.contains("verification was interrupted")) {
                                str2 = "signature digest verification failed";
                                if (!str.contains("signature digest verification failed")) {
                                    return str;
                                }
                            }
                        }
                    }
                }
            }
        }
        return str2;
    }

    public static void sendCleanupAppliedPayloadResult(Context context, int i) {
        Logger.debug("OtaApp", "sendCleanupAppliedPayloadResult : cleanupAppliedPayload=" + i);
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_VAB_CLEANUP_APLLIED_PAYLOAD);
        intent.putExtra(UpgradeUtilConstants.KEY_VAB_CLEANUP_APPLIED_PAYLOAD, i);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }
}
