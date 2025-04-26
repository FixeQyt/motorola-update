package com.motorola.ccc.ota.ui.updateType;

import com.motorola.ccc.ota.utils.Logger;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpdateType {

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum DIFFUpdateType {
        DEFAULT,
        OS,
        SMR,
        MR
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public interface UpdateTypeInterface {
        String getABRestartWarning();

        int getCriticalInstallMessagePopup();

        int getDefaultInstructionImage();

        int getDialogTheme();

        int getDownloadInstructionImage();

        int getDownloadNotificationImage();

        String getDownloadNotificationTitle();

        String getDownloadPauseAnimation();

        String getDownloadProgressAnimation();

        String getDownloadProgressText();

        String getInstallAnimation();

        int getInstallNotificationImage();

        int getInstallToastMessage();

        String getInstallUpdateNotificationText();

        String getInstallationTitle();

        int getLowStorageTitle();

        String getPDLNotificationTitle();

        String getPdlAnimation();

        int getPersonalInfoInstructionImage();

        String getRestartAnimation();

        int getRestartInstructionImage();

        int getRestartNotificationImage();

        int getSecureInstructionImage();

        int getStatusBarColor();

        String getSystemUpdateAvailableNotificationText();

        int getSystemUpdateAvailablePendingNotificationText();

        String getSystemUpdatePausedNotificationTitle();

        int getToolbarColor();

        String getToolbarTitle();

        int getUpdateCompleteNotificationImage();

        int getUpdateFailedImage();

        int getUpdateSpecificColor();

        String getUpdateStatusAnimation();
    }

    public static UpdateTypeInterface getUpdateType(String str) {
        DIFFUpdateType dIFFUpdateType = DIFFUpdateType.DEFAULT;
        try {
            dIFFUpdateType = DIFFUpdateType.valueOf(str);
        } catch (IllegalArgumentException | NullPointerException e) {
            Logger.error("OtaApp", "Exception in UpdateType, getUpdateType: " + e);
        }
        int i = AnonymousClass1.$SwitchMap$com$motorola$ccc$ota$ui$updateType$UpdateType$DIFFUpdateType[dIFFUpdateType.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    return new MR();
                }
                return new MR();
            }
            return new SMR();
        }
        return new OS();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.ui.updateType.UpdateType$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$ccc$ota$ui$updateType$UpdateType$DIFFUpdateType;

        static {
            int[] iArr = new int[DIFFUpdateType.values().length];
            $SwitchMap$com$motorola$ccc$ota$ui$updateType$UpdateType$DIFFUpdateType = iArr;
            try {
                iArr[DIFFUpdateType.OS.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$ui$updateType$UpdateType$DIFFUpdateType[DIFFUpdateType.SMR.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$ui$updateType$UpdateType$DIFFUpdateType[DIFFUpdateType.MR.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }
}
