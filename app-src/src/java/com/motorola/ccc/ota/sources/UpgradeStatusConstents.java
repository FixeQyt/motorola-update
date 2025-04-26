package com.motorola.ccc.ota.sources;

import com.motorola.otalib.common.utils.UpgradeUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public enum UpgradeStatusConstents {
    User_Declined_The_Request_Notification,
    User_Canceled_The_Update,
    Package_Verification_failed,
    Internal_Error_Aborting_The_Query,
    Internal_Error_Aborting_The_Upgrade,
    User_Declined_Launching_The_Upgrade,
    Successfully_Launched_The_Upgrade,
    Unsuccessfully_Launched_The_Upgrade,
    Resources_Error_Aborting_The_Query,
    Download_Failed_Due_To_WiFi_Timeout,
    Resources_Error_Aborting_The_Installation,
    System_Update_Policy_Enabled,
    Error_None;

    public static UpgradeStatusConstents getUpgradeStatusConstants(UpgradeUtils.DownloadStatus downloadStatus) {
        if (downloadStatus == null) {
            return Error_None;
        }
        switch (AnonymousClass1.$SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[downloadStatus.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
                return User_Canceled_The_Update;
            case 5:
            case 6:
            case 7:
                return Resources_Error_Aborting_The_Query;
            case 8:
                return Package_Verification_failed;
            case 9:
            case 10:
                return Internal_Error_Aborting_The_Query;
            case 11:
                return Unsuccessfully_Launched_The_Upgrade;
            case 12:
            case 13:
            case 14:
                return Resources_Error_Aborting_The_Installation;
            case 15:
                return System_Update_Policy_Enabled;
            case 16:
                return Download_Failed_Due_To_WiFi_Timeout;
            default:
                return Error_None;
        }
    }

    /* renamed from: com.motorola.ccc.ota.sources.UpgradeStatusConstents$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus;

        static {
            int[] iArr = new int[UpgradeUtils.DownloadStatus.values().length];
            $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus = iArr;
            try {
                iArr[UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_CANCEL.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL_NOTIFICATION.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_DOWNLOAD_CANCEL_NOTIFICATION.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_RESOURCES.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_RESOURCES_REBOOT.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SPACE.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_VERIFY.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_FAIL_PAYLOAD_METADATA_VERIFY.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_FAIL.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_INSTALL_FAIL.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SPACE_INSTALL_CACHE.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SPACE_INSTALL.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SPACE_BACKGROUND_INSTALL.ordinal()] = 14;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SYSTEM_UPDATE_POLICY_ENABLED.ordinal()] = 15;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_RESOURCES_WIFI.ordinal()] = 16;
            } catch (NoSuchFieldError unused16) {
            }
        }
    }
}
