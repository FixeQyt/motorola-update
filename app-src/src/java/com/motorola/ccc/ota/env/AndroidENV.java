package com.motorola.ccc.ota.env;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import com.motorola.ccc.ota.installer.updaterEngine.common.InstallerUtilMethods;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.fota.FotaConstants;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.Environment.AndroidDB;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public final class AndroidENV implements ApplicationEnv {
    private final Context _context;
    private final String _dbpath;
    private final BotaSettings settings;
    private final ApplicationEnv.Utilities _utilities = new Utilities();
    private final ApplicationEnv.Services _services = new Services();
    private final ApplicationEnv.FotaServices _fotaServices = new FotaServices();

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    class Utilities implements ApplicationEnv.Utilities {
        Utilities() {
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendCheckForUpdateResponse(UpgradeUtils.Error error, int i, boolean z) {
            UpgradeUtilMethods.sendCheckForUpdateResponse(AndroidENV.this._context, error, i, z, null);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendActionUpdateResponse(UpgradeUtils.Error error, int i, boolean z, String str) {
            Logger.debug("OtaApp", "sendActionUpdateResponse:request id is " + i);
            if (i == 0) {
                UpgradeUtilMethods.sendActionUpdateResponse(AndroidENV.this._context, error, i, z, str);
            }
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpgradeStatus(String str, String str2, String str3, String str4, String str5, String str6, long j, boolean z, String str7, boolean z2, String str8) {
            Logger.debug("OtaApp", "sendUpgradeStatus: sending intent  ACTION_UPGRADE_UPDATE_STATUS: " + str + str2 + str3 + str4 + j + z + str7 + " silentOta: " + z2 + " updateType: " + str8);
            UpgradeUtilMethods.sendUpgradeStatus(AndroidENV.this._context, str, str2, str3, str4, str5, str6, j, z, str7, z2, str8);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendCheckForUpdate(boolean z, int i, boolean z2) {
            UpgradeUtilMethods.sendCheckForUpdate(AndroidENV.this._context, z, i, z2);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendCheckForUpdate(boolean z, int i) {
            UpgradeUtilMethods.sendCheckForUpdate(AndroidENV.this._context, z, i);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpdateDownloadStatusError(String str, UpgradeUtils.DownloadStatus downloadStatus) {
            UpgradeUtilMethods.sendUpdateDownloadStatusError(AndroidENV.this._context, str, downloadStatus);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpdateDownloadStatusError(String str, UpgradeUtils.DownloadStatus downloadStatus, String str2) {
            UpgradeUtilMethods.sendUpdateDownloadStatusError(AndroidENV.this._context, str, downloadStatus, str2);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpdateDownloadStatusError(String str, UpgradeUtils.DownloadStatus downloadStatus, String str2, String str3) {
            UpgradeUtilMethods.sendUpdateDownloadStatusError(AndroidENV.this._context, str, downloadStatus, str2, str3);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void showAllocateFreeSpaceDialog(UpgradeUtils.DownloadStatus downloadStatus, long j) {
            UpgradeUtilMethods.showAllocateFreeSpaceDialog(AndroidENV.this._context, downloadStatus, j);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpdateNotification(String str, String str2, String str3, String str4) {
            UpgradeUtilMethods.sendUpdateNotification(AndroidENV.this._context, str, str2, str3, str4);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendStartDownloadProgressFragment(String str, String str2, String str3) {
            UpgradeUtilMethods.sendStartDownloadProgressFragment(AndroidENV.this._context, str, str2, str3);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendStartBackgroundInstallationFragment(String str, String str2, String str3) {
            UpgradeUtilMethods.sendStartBackgroundInstallationFragment(AndroidENV.this._context, str, str2, str3);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpdateDownloadStatusSuccess(String str, long j, String str2, String str3, String str4) {
            UpgradeUtilMethods.sendUpdateDownloadStatusSuccess(AndroidENV.this._context, str, j, str2, str3, str4);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendStartRestartActivity(String str, String str2, String str3) {
            UpgradeUtilMethods.sendStartRestartActivity(AndroidENV.this._context, str, str2, str3);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendSystemRestartNotificationForABUpdate(String str, String str2, String str3) {
            UpgradeUtilMethods.sendSystemRestartNotificationForABUpdate(AndroidENV.this._context, str, str2, str3);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpgradeExecute(String str, String str2, String str3) {
            UpgradeUtilMethods.sendUpgradeExecute(AndroidENV.this._context, str, str2, str3);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpdateDownloadStatusRetried(String str, long j, long j2) {
            UpgradeUtilMethods.sendUpdateDownloadStatusRetried(AndroidENV.this._context, str, j, j2);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpdateDownloadStatusSuspended(String str, long j, long j2, String str2, boolean z) {
            UpgradeUtilMethods.sendUpdateDownloadStatusSuspended(AndroidENV.this._context, str, j, j2, str2, z);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpdateDownloadStatusProgress(String str, long j, long j2, String str2) {
            UpgradeUtilMethods.sendUpdateDownloadStatusProgress(AndroidENV.this._context, str, j, j2, str2);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendVerifyPayloadMetadataFileDownloadStatus(UpgradeUtils.DownloadStatus downloadStatus, String str) {
            UpgradeUtilMethods.sendVerifyPayloadMetadataFileDownloadStatus(AndroidENV.this._context, downloadStatus, str);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpdaterStateReset() {
            UpgradeUtilMethods.sendUpdaterStateReset(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendUpdateStatus(String str, String str2, String str3, boolean z, int i) {
            UpgradeUtilMethods.sendUpdateStatus(AndroidENV.this._context, str, str2, str3, z, i);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendInternalNotification(String str, String str2, String str3) {
            CusAndroidUtils.sendInternalNotification(AndroidENV.this._context, str, str2, str3);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendStartDownloadNotification(String str) {
            CusAndroidUtils.sendStartDownloadNotification(AndroidENV.this._context, str);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public boolean isSpaceAvailable(String str, long j) {
            StatFs statFs = new StatFs(str);
            return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong() >= j;
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public long getSpaceAvailable(String str) {
            StatFs statFs = new StatFs(str);
            return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void postAvailableReserveSpace() {
            CusAndroidUtils.postAvailableReserveSpace(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public boolean isDownloadAllowed(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) AndroidENV.this._context.getSystemService("connectivity");
            TelephonyManager telephonyManager = (TelephonyManager) AndroidENV.this._context.getSystemService("phone");
            String string = AndroidENV.this.settings.getString(Configs.DISALLOWED_NETS);
            if (string != null && string.length() != 0) {
                String wanTypeAsString = NetworkUtils.getWanTypeAsString(context, connectivityManager, telephonyManager);
                if (wanTypeAsString == null) {
                    wanTypeAsString = "OTHER";
                }
                Logger.debug("OtaApp", "CusNet.isDownloadAllowed: currently on: " + wanTypeAsString + "; disallowed nets are: " + string);
                StringTokenizer stringTokenizer = new StringTokenizer(string, ",");
                while (stringTokenizer.hasMoreTokens()) {
                    String nextToken = stringTokenizer.nextToken();
                    if (nextToken.compareToIgnoreCase(wanTypeAsString) == 0) {
                        Logger.info("OtaApp", "CusNet.isDownloadAllowed: network not allowed " + nextToken);
                        return false;
                    }
                }
            }
            return true;
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void verifyPackage(File file) throws Exception {
            RecoverySystem.verifyPackage(file, null, null);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public String readTextFile(File file, int i, String str) {
            return CusFrameworkDeps.readTextFile(file, i, str);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void cancelUpdateNotification() {
            UpgradeUtilMethods.cancelUpdateNotification(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void cancelDownloadNotification() {
            UpgradeUtilMethods.cancelDownloadNotification(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void cancelBGInstallNotification() {
            UpgradeUtilMethods.cancelBGInstallNotification(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void cancelRestartNotification() {
            UpgradeUtilMethods.cancelRestartNotification(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendPollIntent() {
            UpgradeUtilMethods.sendPollIntent(AndroidENV.this._context);
        }

        private JSONObject getExtraPhoneInfoJson() {
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            linkedHashMap.put("totalDataSizeInBytes", String.valueOf(getTotalSize(Environment.getDataDirectory().toString())));
            linkedHashMap.put("availableDataSizeInBytes", String.valueOf(getAvailableSize(Environment.getDataDirectory().toString())));
            linkedHashMap.put("totalSystemSizeInBytes", String.valueOf(getTotalSize(Environment.getRootDirectory().toString())));
            linkedHashMap.put("availableSystemSizeInBytes", String.valueOf(getAvailableSize(Environment.getRootDirectory().toString())));
            linkedHashMap.put("reservedSpaceInMb", String.valueOf(FileUtils.getAvailableReserveSpace() / 1048576));
            return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "AndroidENV.getMemoryJson");
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public String getDeviceAdditionalInfo() {
            SystemUpdaterPolicy systemUpdaterPolicy = new SystemUpdaterPolicy();
            JSONObject extraPhoneInfoJson = getExtraPhoneInfoJson();
            new JSONObject();
            String currentNetworkType = NetworkUtils.getCurrentNetworkType(AndroidENV.this._context, (ConnectivityManager) AndroidENV.this._context.getSystemService("connectivity"), (TelephonyManager) AndroidENV.this._context.getSystemService("phone"));
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            linkedHashMap.put("networkInfo", currentNetworkType);
            linkedHashMap.put("batteryLevel", UpdaterUtils.getBatteryLevel(OtaApplication.getGlobalContext()) + "%");
            linkedHashMap.put("memoryInfo", extraPhoneInfoJson);
            linkedHashMap.put("systemUpdatePolicy", systemUpdaterPolicy.getSystemUpdateInfo());
            linkedHashMap.put("downloadRetryCount", Integer.valueOf(AndroidENV.this.settings.getInt(Configs.STATS_DL_DD_OBTAINED, 0)));
            return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "AndroidENV.getDeviceAdditionalInfo").toString();
        }

        public long getTotalSize(String str) {
            StatFs statFs = new StatFs(str);
            return statFs.getBlockCountLong() * statFs.getBlockSizeLong();
        }

        public long getAvailableSize(String str) {
            StatFs statFs = new StatFs(str);
            return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public long getAvailableDataPartitionSize() {
            StatFs statFs = new StatFs(Environment.getDataDirectory().toString());
            return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendErrorCode(int i) {
            CusAndroidUtils.sendErrorcode(AndroidENV.this._context, i);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendException(Exception exc) {
            CusAndroidUtils.sendException(AndroidENV.this._context, exc);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendException(String str) {
            CusAndroidUtils.sendException(AndroidENV.this._context, str);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public String getNetwork() {
            return NetworkUtils.getNetwork(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void registerWithWiFiDiscoveryManager(long j, boolean z, boolean z2) {
            UpgradeUtilMethods.registerWithWiFiDiscoveryManager(AndroidENV.this._context, j, z, z2);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void unRegisterWithWiFiDiscoveryManager() {
            UpgradeUtilMethods.unRegisterWithWiFiDiscoveryManager(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendDiscoverTimerExpiryIntent() {
            UpgradeUtilMethods.sendDiscoverTimerExpiryIntent(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendGetDescriptor(String str, String str2, boolean z) {
            CusAndroidUtils.sendGetDescriptor(AndroidENV.this._context, str, str2, z);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendSystemUpdateAvailableNotification(String str) {
            UpgradeUtilMethods.sendSystemUpdateAvailableNotification(AndroidENV.this._context, str);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendInstallSystemUpdateAvailableNotification(String str, String str2, String str3) {
            UpgradeUtilMethods.sendInstallSystemUpdateAvailableNotification(AndroidENV.this._context, str, str2, str3);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendRebootDuringDownloadIntent() {
            CusAndroidUtils.sendRebootDuringDownloadIntent(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void registerWithForceUpgradeManager(long j) {
            UpgradeUtilMethods.registerWithForceUpgradeManager(AndroidENV.this._context, j);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void unRegisterWithForceUpgradeManager() {
            UpgradeUtilMethods.unRegisterWithForceUpgradeManager(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void sendForceUpgradeTimerExpiryIntent() {
            UpgradeUtilMethods.sendForceUpgradeTimerExpiryIntent(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void triggerForceDeviceLogin() {
            CusAndroidUtils.triggerForceDeviceLogin();
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public boolean isDeviceInIdleMode() {
            return ((PowerManager) AndroidENV.this._context.getSystemService("power")).isDeviceIdleMode();
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void moveFotaToGettingDescriptorState() {
            UpgradeUtilMethods.moveFotaToGettingDescriptorState(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void startBotaWifiDiscoveryTimer() {
            if (!BuildPropReader.isATT() && AndroidENV.this.settings.getLong(Configs.WIFI_DISCOVER_TIME, -1L) < 0) {
                Logger.debug("OtaApp", "startBotaWifiDiscoveryTimer");
                AndroidENV.this.settings.setLong(Configs.WIFI_DISCOVER_TIME, System.currentTimeMillis() + (AndroidENV.this.settings.getInt(Configs.DEFAULT_WIFI_DISCOVER_TIME, 82800) * 1000));
                registerWithWiFiDiscoveryManager(AndroidENV.this.settings.getInt(Configs.DEFAULT_WIFI_DISCOVER_TIME, 82800), true, false);
            }
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void startFotaWifiDiscoveryTimer() {
            if (BuildPropReader.isATT() && AndroidENV.this.settings.getLong(Configs.WIFI_DISCOVER_TIME, -1L) < 0) {
                Logger.debug("OtaApp", "startFotaWifiDiscoveryTimer");
                AndroidENV.this.settings.setLong(Configs.WIFI_DISCOVER_TIME, System.currentTimeMillis() + InstallerUtilMethods.MAX_ALARM_TIME_FOR_DL_MODEM);
                registerWithWiFiDiscoveryManager(FotaConstants.WIFI_DISCOVER_TIMER_EXPIRY, false, true);
            }
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void cleanFotaWifiDiscoveryTimer() {
            if (BuildPropReader.isATT()) {
                Logger.debug("OtaApp", "cleanFotaWifiDiscoveryTimer");
                cleanWifiDiscoveryTimer();
            }
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Utilities
        public void cleanBotaWifiDiscoveryTimer() {
            if (BuildPropReader.isATT()) {
                return;
            }
            Logger.debug("OtaApp", "cleanBotaWifiDiscoveryTimer");
            cleanWifiDiscoveryTimer();
        }

        private void cleanWifiDiscoveryTimer() {
            if (AndroidENV.this.settings.getLong(Configs.WIFI_DISCOVER_TIME, -1L) > 0) {
                AndroidENV.this.settings.removeConfig(Configs.WIFI_DISCOVER_TIME);
                unRegisterWithWiFiDiscoveryManager();
            }
        }
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv
    public ApplicationEnv.Utilities getUtilities() {
        return this._utilities;
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    static class Services implements ApplicationEnv.Services {
        Services() {
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Services
        public String getDeviceSha1() {
            return BuildPropReader.getDeviceSha1(UpgradeSourceType.upgrade.toString());
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Services
        public Boolean isSDCardMounted() {
            return Boolean.valueOf("mounted".equals(Environment.getExternalStorageState()));
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Services
        public Boolean isSDCardPresent() {
            return Boolean.valueOf(!"removed".equals(Environment.getExternalStorageState()));
        }
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv
    public ApplicationEnv.Services getServices() {
        return this._services;
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    class FotaServices implements ApplicationEnv.FotaServices {
        FotaServices() {
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.FotaServices
        public void sendWifiDiscoverTimerExpiry(long j, int i) {
            AndroidFotaInterface.sendWifiDiscoverTimerExpiry(AndroidENV.this._context, j, i);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.FotaServices
        public void sendUpdateAvailableResponse(long j, int i) {
            AndroidFotaInterface.sendUpdateAvailableResponse(AndroidENV.this._context, j, i);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.FotaServices
        public void sendUpdateAvailableResponse(long j, int i, boolean z) {
            AndroidFotaInterface.sendUpdateAvailableResponse(AndroidENV.this._context, j, i, z);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.FotaServices
        public void sendFotaInitializationIntent() {
            AndroidFotaInterface.sendFotaInitializationIntent(AndroidENV.this._context);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.FotaServices
        public void sendRequestUpdate(long j) {
            AndroidFotaInterface.sendRequestUpdate(AndroidENV.this._context, j);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.FotaServices
        public void sendUpgradeResult(long j, int i) {
            AndroidFotaInterface.sendUpgradeResult(AndroidENV.this._context, j, i);
        }

        @Override // com.motorola.otalib.common.Environment.ApplicationEnv.FotaServices
        public UpgradeUtils.DownloadStatus getFotaDownloadCompleted(long j, int i) {
            UpgradeUtils.DownloadStatus downloadStatus = UpgradeUtils.DownloadStatus.STATUS_VERIFY;
            if (CusFrameworkDeps.isStatusError(i)) {
                Logger.error("OtaApp", "CusAndroidENV.FotaServices.getFotaDownloadCompleted (failed) " + j + SystemUpdateStatusUtils.SPACE + i);
                if (CusFrameworkDeps.isStatusServerError(i)) {
                    return UpgradeUtils.DownloadStatus.STATUS_SERVER;
                }
                if (CusFrameworkDeps.isStatusClientError(i)) {
                    return UpgradeUtils.DownloadStatus.STATUS_RESOURCES;
                }
                return UpgradeUtils.DownloadStatus.STATUS_FAIL;
            }
            return downloadStatus;
        }
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv
    public ApplicationEnv.FotaServices getFotaServices() {
        return this._fotaServices;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public AndroidENV(String str, Context context, BotaSettings botaSettings) {
        this._dbpath = str;
        this._context = context;
        this.settings = botaSettings;
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv
    public ApplicationEnv.Database createDatabase() {
        return new AndroidDB(this._dbpath, this._context);
    }
}
