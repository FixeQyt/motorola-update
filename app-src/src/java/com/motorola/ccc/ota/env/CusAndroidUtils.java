package com.motorola.ccc.ota.env;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtilConstants;
import com.motorola.ccc.ota.sources.modem.ModemPollingManager;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.PMUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.utils.BroadcastUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public final class CusAndroidUtils {
    public static final String ACTION_CAPTIVE_PORTAL_LOGGED_IN = "android.net.netmon.captive_portal_logged_in";
    public static final String ACTION_GET_DESCRIPTOR = "com.motorola.ccc.ota.CusAndroidUtils.ACTION_GET_DESCRIPTOR";
    public static final String ACTION_GET_OTA_RESERVED_SPACE = "com.motorola.app.Actions.GET_OTA_RESERVED_SPACE";
    public static final String DOWNLOADER_ACTION_CLEANUP = "android.intent.action.DOWNLOAD_CLEANUP";
    public static final String DOWNLOADER_ACTION_CLEANUP_DONE = "android.intent.action.DOWNLOAD_CLEANUP_DONE";
    public static final String DOWNLOADER_EXTRA_CLEANUP_SIZE = "android.intent.action.DOWNLOAD_CLEANUP.size";
    public static final String DOWNLOAD_ERROR_CODE = "com.motorola.ccc.ota.dl_errorcode";
    public static final String DOWNLOAD_EXCEPTION = "com.motorola.ccc.ota.dl_exception";
    public static final String ERROR_CODE = "com.motorola.ccc.ota.errorcode";
    public static final String EXCEPTION = "com.motorola.ccc.ota.exception";
    public static final String INTENT_ACTION_CHANNEL_ID_UPDATED = "com.motorola.ccc.ota.CHANNEL_ID_UPDATED";
    public static final String INTENT_ACTION_CLOUD_PICKER = "com.motorola.ccc.ota.Actions.CloudPicker";
    public static final String INTENT_ACTION_SETUP_COMPLETED = "com.motorola.ccc.ota.SETUP_COMPLETED";
    public static final String INTENT_ACTION_TOS_COMPLETED = "com.motorola.ccc.ota.TOS_COMPLETED";
    public static final String INTERNAL_NOTIFICATION = "com.motorola.ccc.ota.CusAndroidUtils.INTERNAL_NOTIFICATION";
    public static final String INTERNAL_REASON = "com.motorola.ccc.ota.CusAndroidUtils.INTERNAL_REASON";
    public static final String INTERNAL_RESULT = "com.motorola.ccc.ota.CusAndroidUtils.internal_result";
    public static final String INTERNAL_TYPE = "com.motorola.ccc.ota.CusAndroidUtils.internal_type";
    public static final String INTERNAL_VERSION = "com.motorola.ccc.ota.CusAndroidUtils.internal_version";
    public static final String MMAPI_FORCED_LOGIN_REASON = "KEY_REQUEST_REASON";
    public static final String MMAPI_FORCE_LOGIN_ACTION = "com.motorola.blur.service.mmapi.force.login_OTA_complete";
    public static final String MODEM_SECRET_CODE_HOST = "66336";
    public static final String PACKAGE_DATA_OBSERVER_CLASS = "android.content.pm.IPackageDataObserver";
    public static final String PACKAGE_MANAGER_CLASS = "android.content.pm.PackageManager";
    public static final String REBOOT_DURING_DOWNLOAD = "com.motorola.ccc.ota.REBOOT_DURING_DOWNLOAD";
    public static final String REPORT_GET_DESCRIPTOR_STATUS = "com.motorola.ccc.ota.CusAndroidUtils.REPORT_GET_DESCRIPTOR_STATUS";
    public static final String SECRET_CODE = "android.provider.Telephony.SECRET_CODE";
    public static final String SECRET_CODE_HOST = "24325";
    public static final String SECRET_CODE_SCHEME = "android_secret_code";
    public static final String START_DOWNLOAD_NOTIFICATION = "com.motorola.ccc.ota.CusAndroidUtils.START_DOWNLOAD_NOTIFICATION";
    public static final long URL_EXPIRY_TIME = 600000;
    private static boolean alreadySendSetupCompletedIntent;

    static final String getPathFromIntent(Intent intent) {
        return intent.getDataString();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static final void registerRadioActions(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PMUtils.POLLINGMGR_CONNECTIVITY);
        intentFilter.addAction(PMUtils.POLLINGMGR_ROAMING_CHANGE);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_BATTERY_CHANGED);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        context.registerReceiver(broadcastReceiver, intentFilter2, 2);
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
        context.registerReceiver(broadcastReceiver, intentFilter3, 2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static final boolean isRadioUp(Intent intent) {
        if (intent.getAction().equals(PMUtils.POLLINGMGR_CONNECTIVITY)) {
            return intent.getBooleanExtra(PMUtils.KEY_CONNECTIVITY_EXTRA, false);
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static final boolean isRoaming(Intent intent) {
        if (intent.getAction().equals(PMUtils.POLLINGMGR_ROAMING_CHANGE)) {
            return intent.getBooleanExtra(PMUtils.KEY_ROAMING_EXTRA, false);
        }
        return false;
    }

    static final boolean isDataRoaming(Intent intent) {
        if (intent.getAction().equals(PMUtils.POLLINGMGR_ROAMING_CHANGE)) {
            return intent.getBooleanExtra(PMUtils.KEY_DATAROAMING_EXTRA, false);
        }
        return false;
    }

    public static final void registerDownloaderNotification(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DOWNLOADER_ACTION_CLEANUP_DONE);
        context.registerReceiver(broadcastReceiver, intentFilter, 2);
    }

    public static final void registerCaptivePortalLoginDone(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CAPTIVE_PORTAL_LOGGED_IN);
        context.registerReceiver(broadcastReceiver, intentFilter, 2);
    }

    public static final void registerReserveSpaceRequest(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GET_OTA_RESERVED_SPACE);
        context.registerReceiver(broadcastReceiver, intentFilter, Permissions.INTERACT_OTA_SERVICE, null, 2);
    }

    public static final void registerSuCancelRequest(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.ACTION_DM_CANCEL_ONGOING_UPGRADE);
        context.registerReceiver(broadcastReceiver, intentFilter, Permissions.INTERACT_OTA_SERVICE, null, 2);
    }

    public static final void postAvailableReserveSpace(Context context) {
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_OTA_RESERVE_SPACE_RESPONSE);
        long availableReserveSpace = FileUtils.getAvailableReserveSpace() / 1048576;
        intent.putExtra(UpgradeUtilConstants.KEY_RESERVE_SPACE_IN_MB, availableReserveSpace);
        Logger.debug("OtaApp", "Posting response with reserve space value: " + availableReserveSpace);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final void postSetupCompleted(Context context) {
        if (alreadySendSetupCompletedIntent) {
            return;
        }
        BroadcastUtils.sendLocalBroadcast(context, new Intent(INTENT_ACTION_SETUP_COMPLETED));
        alreadySendSetupCompletedIntent = true;
    }

    public static final void postChannelIdUpdatedIntent(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(INTENT_ACTION_CHANNEL_ID_UPDATED));
    }

    public static final void postTOSCompleted(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(INTENT_ACTION_TOS_COMPLETED));
    }

    public static final void sendDownloadCleanup(Context context, long j) {
        Intent intent = new Intent(DOWNLOADER_ACTION_CLEANUP);
        intent.putExtra(DOWNLOADER_EXTRA_CLEANUP_SIZE, j);
        context.sendBroadcast(intent, "android.permission.ACCESS_DOWNLOAD_MANAGER");
    }

    public static final void registerShutdownActions(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction("android.intent.action.REBOOT");
        context.registerReceiver(broadcastReceiver, intentFilter, 2);
    }

    public static final void unregisterShutdownActions(Context context, BroadcastReceiver broadcastReceiver) {
        context.unregisterReceiver(broadcastReceiver);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static final void registerInternalIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTERNAL_NOTIFICATION);
        intentFilter.addAction(ACTION_GET_DESCRIPTOR);
        intentFilter.addAction(UpgradeUtilConstants.OTA_STOP_ACTION);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_STOP_OTA_SERVICE);
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_DOWNLOAD_NOTIFICATION_RESPONSE);
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_BACKGROUND_INSTALL_CANCEL_RESPONSE);
        intentFilter.addAction(UpgradeUtilConstants.USER_BACKGROUND_INSTALL_RESPONSE);
        intentFilter.addAction(UpgradeUtilConstants.AB_UPGRADE_COMPLETED_INTENT);
        intentFilter.addAction(UpgradeUtilConstants.CANCEL_UPDATE);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_VERIFY_PAYLOAD_STATUS);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_VAB_VERIFY_STATUS);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_VAB_ALLOCATE_SPACE_RESULT);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_SMART_UPDATE_CONFIG_CHANGED);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_OVERRIDE_METADATA);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_VAB_CLEANUP_APLLIED_PAYLOAD);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(START_DOWNLOAD_NOTIFICATION);
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_AVAILABLE_RESPONSE);
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE_RESPONSE);
        intentFilter2.addAction(UpgradeUtilConstants.CREATE_RESERVE_SPACE_POST_FIFTEEN_MINUTES);
        context.registerReceiver(broadcastReceiver, intentFilter2, Permissions.INTERACT_OTA_SERVICE, null, 2);
    }

    public static final void sendInternalNotification(Context context, String str, String str2, String str3) {
        Intent intent = new Intent(INTERNAL_NOTIFICATION);
        intent.putExtra(INTERNAL_VERSION, str);
        intent.putExtra(INTERNAL_TYPE, str2);
        intent.putExtra(INTERNAL_RESULT, str3);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final void sendStartDownloadNotification(Context context, String str) {
        Intent intent = new Intent(START_DOWNLOAD_NOTIFICATION);
        intent.putExtra(INTERNAL_VERSION, str);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static final void sendGetDescriptor(Context context, String str, String str2, boolean z) {
        Intent intent = new Intent(ACTION_GET_DESCRIPTOR);
        intent.putExtra(INTERNAL_VERSION, str);
        intent.putExtra(REPORT_GET_DESCRIPTOR_STATUS, z);
        intent.putExtra(INTERNAL_REASON, str2);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final String versionFromIntent(Intent intent) {
        return intent.getStringExtra(INTERNAL_VERSION);
    }

    public static final String typeFromIntent(Intent intent) {
        return intent.getStringExtra(INTERNAL_TYPE);
    }

    public static final String resultFromIntent(Intent intent) {
        return intent.getStringExtra(INTERNAL_RESULT);
    }

    public static final Boolean reportStatusFromIntent(Intent intent) {
        return Boolean.valueOf(intent.getBooleanExtra(REPORT_GET_DESCRIPTOR_STATUS, true));
    }

    public static final String getReasonFromIntent(Intent intent) {
        return intent.getStringExtra(INTERNAL_REASON);
    }

    public static final void sendErrorcode(Context context, int i) {
        Intent intent = new Intent(DOWNLOAD_ERROR_CODE);
        intent.putExtra(ERROR_CODE, i);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final void sendException(Context context, Exception exc) {
        Intent intent = new Intent(DOWNLOAD_EXCEPTION);
        intent.putExtra(EXCEPTION, exc.toString());
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final void sendException(Context context, String str) {
        Intent intent = new Intent(DOWNLOAD_EXCEPTION);
        intent.putExtra(EXCEPTION, str);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static final int errorcodeFromIntent(Intent intent) {
        return intent.getIntExtra(ERROR_CODE, -1);
    }

    public static final String exceptionFromIntent(Intent intent) {
        return intent.getStringExtra(EXCEPTION);
    }

    public static final void registerFotaIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AndroidFotaInterface.ACTION_FOTA_REQUEST_UPDATE_RESPONSE);
        intentFilter.addAction(AndroidFotaInterface.ACTION_FOTA_UPDATE_AVAILABLE);
        intentFilter.addAction(AndroidFotaInterface.ACTION_FOTA_DOWNLOAD_COMPLETE);
        intentFilter.addAction(AndroidFotaInterface.ACTION_FOTA_DOWNLOAD_MODE_CHANGED);
        intentFilter.addAction(AndroidFotaInterface.ACTION_FOTA_SERVER_TRANSPORT_MEDIA);
        intentFilter.addAction(AndroidFotaInterface.ACTION_FOTA_USER_ALERT_CELLULAR_OPT);
        context.registerReceiver(broadcastReceiver, intentFilter, 2);
    }

    public static final void registerMiscIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_CLOUD_PICKER);
        intentFilter.addAction("android.app.action.SYSTEM_UPDATE_POLICY_CHANGED");
        context.registerReceiver(broadcastReceiver, intentFilter, 2);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(SECRET_CODE);
        intentFilter2.addDataScheme(SECRET_CODE_SCHEME);
        intentFilter2.addDataAuthority(SECRET_CODE_HOST, null);
        intentFilter2.addDataAuthority(MODEM_SECRET_CODE_HOST, null);
        context.registerReceiver(broadcastReceiver, intentFilter2, 2);
    }

    public static final void registerStateMachineIntent(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.RUN_STATE_MACHINE);
        context.registerReceiver(broadcastReceiver, intentFilter, Permissions.INTERACT_OTA_SERVICE, null, 2);
    }

    public static final void registerCheckUpdateActions(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.MOVE_FOTA_TO_GETTING_DESCRIPTOR);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE);
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_RESPONSE);
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_LAUNCH_UPGRADE);
        intentFilter2.addAction(ThinkShieldUtilConstants.ACTION_ASC_OTA_INTERNAL_TIMEOUT);
        context.registerReceiver(broadcastReceiver, intentFilter2, Permissions.INTERACT_OTA_SERVICE, null, 2);
        Logger.debug("OtaApp", "registered CheckUpdateActions");
    }

    public static final void registerWifiDiscoverActions(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.REGISTER_WIFI_DISCOVER_MANAGER);
        intentFilter.addAction(UpgradeUtilConstants.UNREGISTER_WIFI_DISCOVER_MANAGER);
        intentFilter.addAction(UpgradeUtilConstants.WIFI_DISCOVER_TIMER_EXPIRY);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter);
    }

    public static final void registerForceUpgradeActions(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.REGISTER_FORCE_UPGRADE_MANAGER);
        intentFilter.addAction(UpgradeUtilConstants.UNREGISTER_FORCE_UPGRADE_MANAGER);
        intentFilter.addAction(UpgradeUtilConstants.FORCE_UPGRADE_TIMER_EXPIRY);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter);
    }

    public static final void registerPollingManagerExpiryIntent(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AndroidPollingManager.INTENT_ACTION_POLLING_MANAGER);
        intentFilter.addAction(ModemPollingManager.INTENT_ACTION_POLLING_MANAGER);
        intentFilter.addAction(OtaWiFiDiscoveryManager.INTENT_ACTION_POLLING_MANAGER);
        intentFilter.addAction(ForceUpgradeManager.INTENT_ACTION_POLLING_MANAGER);
        intentFilter.addAction(UpgradeUtilConstants.INTENT_HEALTH_CHECK);
        context.registerReceiver(broadcastReceiver, intentFilter, Permissions.INTERACT_OTA_SERVICE, null, 2);
    }

    public static final void registerAscIntentActions(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ThinkShieldUtilConstants.ACTION_ASC_SESSION_DONE);
        intentFilter.addAction(ThinkShieldUtilConstants.ACTION_ASC_SYSTEM_UPDATE_POLICY_CHANGED);
        context.registerReceiver(broadcastReceiver, intentFilter, Permissions.INTERACT_ASC_SERVICE, null, 2);
    }

    public static final void sendRebootDuringDownloadIntent(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(REBOOT_DURING_DOWNLOAD));
    }

    public static int getApkVersion() {
        Context globalContext = OtaApplication.getGlobalContext();
        try {
            return globalContext.getPackageManager().getPackageInfo(globalContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.debug("OtaApp", "Exception while getting versionCode :" + e);
            return -1;
        }
    }

    public static void triggerForceDeviceLogin() {
        Logger.debug("OtaApp", "Broadcasting device login");
        Context globalContext = OtaApplication.getGlobalContext();
        Intent intent = new Intent(MMAPI_FORCE_LOGIN_ACTION);
        intent.putExtra(MMAPI_FORCED_LOGIN_REASON, "OtaService");
        intent.setFlags(16777216);
        globalContext.sendBroadcast(intent, Permissions.INTERACT_BLUR_SERVICE);
    }

    public static final void registerUpdateReceiverIntents(Context context, BroadcastReceiver broadcastReceiver) {
        checkPhaseUiIntents(context, broadcastReceiver);
        downloadPhaseUiIntents(context, broadcastReceiver);
        installPhaseUiIntents(context, broadcastReceiver);
        restartPhaseUiIntents(context, broadcastReceiver);
        statusIntents(context, broadcastReceiver);
        notificationIntents(context, broadcastReceiver);
    }

    private static final void checkPhaseUiIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_UPDATER_UPDATE_NOTIFICATION_CLEAR);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_AVAILABLE);
        intentFilter2.addAction(UpdaterUtils.ACTION_MANUAL_CHECK_UPDATE);
        context.registerReceiver(broadcastReceiver, intentFilter2, Permissions.INTERACT_OTA_SERVICE, null, 2);
    }

    private static final void downloadPhaseUiIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_UPDATER_DOWNLOAD_NOTIFICATION_CLEAR);
        intentFilter.addAction(UpgradeUtilConstants.START_DOWNLOAD_PROGRESS_FRAGMENT);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter);
    }

    private static final void installPhaseUiIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_UPDATER_INSTALL_NOTIFICATION_CLEAR);
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_UPDATER_RESTART_NOTIFICATION_CLEAR);
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_UPDATER_BG_INSTALL_NOTIFICATION_CLEAR);
        intentFilter.addAction(UpgradeUtilConstants.START_BACKGROUND_INSTALLATION_FRAGMENT);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE);
        intentFilter2.addAction(UpgradeUtilConstants.ACTION_AB_UPDATE_PROGRESS);
        context.registerReceiver(broadcastReceiver, intentFilter2, Permissions.INTERACT_OTA_SERVICE, null, 2);
    }

    private static final void restartPhaseUiIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.START_RESTART_ACTIVITY_INTENT);
        intentFilter.addAction(UpgradeUtilConstants.START_MERGE_RESTART_ACTIVITY_INTENT);
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_RESTART_NOTIFICATION);
        context.registerReceiver(broadcastReceiver, intentFilter, Permissions.INTERACT_OTA_SERVICE, null, 2);
    }

    private static final void notificationIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.ACTIVITY_ANNOY_VALUE_EXPIRY);
        context.registerReceiver(broadcastReceiver, intentFilter, 2);
    }

    public static void registerConfigChangeIntentActions(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        context.registerReceiver(broadcastReceiver, intentFilter, 2);
    }

    private static final void statusIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_UPDATER_STATE_CLEAR);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_STATUS);
        intentFilter2.addAction(UpgradeUtilConstants.ACTION_UPGRADE_UPDATE_STATUS);
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE_RESPONSE);
        context.registerReceiver(broadcastReceiver, intentFilter2, Permissions.INTERACT_OTA_SERVICE, null, 2);
        Logger.debug("OtaApp", "registered status intents for updatereceiver");
    }

    public static final void registerUpgraderReceiverIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_EXECUTE_UPGRADE);
        intentFilter.addAction(UpgradeUtilConstants.MERGE_RESTART_UPGRADE);
        context.registerReceiver(broadcastReceiver, intentFilter, Permissions.INTERACT_OTA_SERVICE, null, 2);
    }

    public static final void registerOutofBoxUpdateDetectReceiverIntents(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTION_SETUP_COMPLETED);
        intentFilter.addAction(INTENT_ACTION_TOS_COMPLETED);
        intentFilter.addAction(INTENT_ACTION_CHANNEL_ID_UPDATED);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter);
    }

    public static final void registerSimStateChangeReceiver(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.ACTION_SIM_STATE_CHANGE);
        context.registerReceiver(broadcastReceiver, intentFilter, 2);
    }

    public static boolean isDeviceInDatasaverMode() {
        ConnectivityManager connectivityManager = (ConnectivityManager) OtaApplication.getGlobalContext().getSystemService("connectivity");
        return connectivityManager != null && connectivityManager.isActiveNetworkMetered() && connectivityManager.getRestrictBackgroundStatus() == 3;
    }

    public static boolean checkForUrlExpiry(BotaSettings botaSettings, boolean z) {
        long j;
        long currentTimeMillis = System.currentTimeMillis();
        if (z) {
            j = botaSettings.getLong(Configs.MODEM_DOWNLOAD_DESCRIPTOR_TIME, System.currentTimeMillis());
        } else {
            j = botaSettings.getLong(Configs.DOWNLOAD_DESCRIPTOR_TIME, System.currentTimeMillis());
        }
        return currentTimeMillis - j >= URL_EXPIRY_TIME;
    }

    public static void registerModemIntentActions(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.ACTION_MODEM_UPDATE_STATUS);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_MODEM_FSG_POLL);
        context.registerReceiver(broadcastReceiver, intentFilter, Permissions.INTERACT_MODEM_UPDATE_SERVICE, null, 2);
    }
}
