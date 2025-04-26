package com.motorola.otalib.common.Environment;

import android.content.Context;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public interface ApplicationEnv {
    public static final int PollingInitiated = 2;
    public static final int clientInitiated = 1;
    public static final int serverInitiated = 3;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public interface Database {

        /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
        public interface Descriptor {
            String getInfo();

            MetaData getMeta();

            String getRepository();

            PackageState getState();

            String getStatus();

            int getTime();

            String getVersion();
        }

        /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
        public interface Status {
            String getDeviceVersion();

            int getId();

            String getInfo();

            String getReportingTag();

            String getRepository();

            String getSourceSha1();

            String getState();

            String getStatus();

            String getTargetSha1();

            long getTime();

            String getTrackingID();
        }

        void clear_status();

        void close();

        Descriptor getDescription(String str);

        List<String> getVersions();

        Status get_status();

        Status get_status(int i);

        boolean insert(String str, String str2, MetaData metaData, String str3, String str4);

        void remove(String str);

        void remove_status(int i);

        boolean setState(String str, PackageState packageState, String str2);

        boolean setState(String str, PackageState packageState, boolean z, String str2, String str3);

        boolean setState(String str, PackageState packageState, boolean z, String str2, String str3, String str4);

        void setStatus(String str, MetaData metaData, String str2, String str3, String str4, String str5);

        boolean setVersionState(String str, PackageState packageState, String str2);

        boolean update_column_vt(String str, String str2, String str3);
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public interface FotaServices {
        UpgradeUtils.DownloadStatus getFotaDownloadCompleted(long j, int i);

        void sendFotaInitializationIntent();

        void sendRequestUpdate(long j);

        void sendUpdateAvailableResponse(long j, int i);

        void sendUpdateAvailableResponse(long j, int i, boolean z);

        void sendUpgradeResult(long j, int i);

        void sendWifiDiscoverTimerExpiry(long j, int i);
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum PackageState {
        IDLE,
        Notified,
        RequestPermission,
        QueueForDownload,
        GettingDescriptor,
        GettingPackage,
        IntimateModem,
        WaitingForModemUpdateStatus,
        VerifyPayloadMetadata,
        VerifyAllocateSpace,
        ABApplyingPatch,
        Querying,
        QueryingInstall,
        Upgrading,
        MergePending,
        MergeRestart,
        Result
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public interface Services {
        String getDeviceSha1();

        Boolean isSDCardMounted();

        Boolean isSDCardPresent();
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public interface Utilities {
        void cancelBGInstallNotification();

        void cancelDownloadNotification();

        void cancelRestartNotification();

        void cancelUpdateNotification();

        void cleanBotaWifiDiscoveryTimer();

        void cleanFotaWifiDiscoveryTimer();

        long getAvailableDataPartitionSize();

        String getDeviceAdditionalInfo();

        String getNetwork();

        long getSpaceAvailable(String str);

        boolean isDeviceInIdleMode();

        boolean isDownloadAllowed(Context context);

        boolean isSpaceAvailable(String str, long j);

        void moveFotaToGettingDescriptorState();

        void postAvailableReserveSpace();

        String readTextFile(File file, int i, String str) throws IOException;

        void registerWithForceUpgradeManager(long j);

        void registerWithWiFiDiscoveryManager(long j, boolean z, boolean z2);

        void sendActionUpdateResponse(UpgradeUtils.Error error, int i, boolean z, String str);

        void sendCheckForUpdate(boolean z, int i);

        void sendCheckForUpdate(boolean z, int i, boolean z2);

        void sendCheckForUpdateResponse(UpgradeUtils.Error error, int i, boolean z);

        void sendDiscoverTimerExpiryIntent();

        void sendErrorCode(int i);

        void sendException(Exception exc);

        void sendException(String str);

        void sendForceUpgradeTimerExpiryIntent();

        void sendGetDescriptor(String str, String str2, boolean z);

        void sendInstallSystemUpdateAvailableNotification(String str, String str2, String str3);

        void sendInternalNotification(String str, String str2, String str3);

        void sendPollIntent();

        void sendRebootDuringDownloadIntent();

        void sendStartBackgroundInstallationFragment(String str, String str2, String str3);

        void sendStartDownloadNotification(String str);

        void sendStartDownloadProgressFragment(String str, String str2, String str3);

        void sendStartRestartActivity(String str, String str2, String str3);

        void sendSystemRestartNotificationForABUpdate(String str, String str2, String str3);

        void sendSystemUpdateAvailableNotification(String str);

        void sendUpdateDownloadStatusError(String str, UpgradeUtils.DownloadStatus downloadStatus);

        void sendUpdateDownloadStatusError(String str, UpgradeUtils.DownloadStatus downloadStatus, String str2);

        void sendUpdateDownloadStatusError(String str, UpgradeUtils.DownloadStatus downloadStatus, String str2, String str3);

        void sendUpdateDownloadStatusProgress(String str, long j, long j2, String str2);

        void sendUpdateDownloadStatusRetried(String str, long j, long j2);

        void sendUpdateDownloadStatusSuccess(String str, long j, String str2, String str3, String str4);

        void sendUpdateDownloadStatusSuspended(String str, long j, long j2, String str2, boolean z);

        void sendUpdateNotification(String str, String str2, String str3, String str4);

        void sendUpdateStatus(String str, String str2, String str3, boolean z, int i);

        void sendUpdaterStateReset();

        void sendUpgradeExecute(String str, String str2, String str3);

        void sendUpgradeStatus(String str, String str2, String str3, String str4, String str5, String str6, long j, boolean z, String str7, boolean z2, String str8);

        void sendVerifyPayloadMetadataFileDownloadStatus(UpgradeUtils.DownloadStatus downloadStatus, String str);

        void showAllocateFreeSpaceDialog(UpgradeUtils.DownloadStatus downloadStatus, long j);

        void startBotaWifiDiscoveryTimer();

        void startFotaWifiDiscoveryTimer();

        void triggerForceDeviceLogin();

        void unRegisterWithForceUpgradeManager();

        void unRegisterWithWiFiDiscoveryManager();

        void verifyPackage(File file) throws Exception;
    }

    Database createDatabase();

    FotaServices getFotaServices();

    Services getServices();

    Utilities getUtilities();
}
