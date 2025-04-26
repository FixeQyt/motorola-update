package com.motorola.ccc.ota.sources;

import android.text.TextUtils;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.metaData.MetaData;
import java.io.File;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public abstract class UpgradeSource {
    private UpgradeSourceType upgradeType;

    public void checkCallECBLowBattery(String str) {
    }

    public boolean checkForChainUpgrade(UpgradeSourceType upgradeSourceType) {
        return false;
    }

    public void checkForDownloadDescriptor(String str) {
    }

    public void checkForUpdate(String str, String str2) {
    }

    public void checkForUpdate(boolean z) {
    }

    public void checkForUpdate(boolean z, int i, boolean z2) {
    }

    public boolean doYouDownloadDirectly(ApplicationEnv.Database.Descriptor descriptor) {
        return false;
    }

    public void downloadCompleted(long j, int i) {
    }

    public void downloadCompleted(long j, int i, String str) {
    }

    public void downloadDirectly(ApplicationEnv.Database.Descriptor descriptor) {
    }

    public void downloadModeChanged(long j, boolean z) {
    }

    public abstract DownloadHandler getDownloadHandler();

    public String getDownloadOptStartStopTime(ApplicationEnv.Database.Descriptor descriptor) {
        return null;
    }

    public void handleUpdateStatus(ApplicationEnv.Database.Descriptor descriptor, UpgradeStatusConstents upgradeStatusConstents) {
    }

    public void plugin_exit() {
    }

    public void plugin_init(List<ApplicationEnv.Database.Descriptor> list) {
    }

    public void requestUpdateResponse(long j, int i) {
    }

    public void sendChainUpgradeRequest(UpgradeSourceType upgradeSourceType) {
    }

    public void setMemoryLowInfo(ApplicationEnv.Database.Descriptor descriptor) {
    }

    public void updateAvailable(long j, long j2, String str, boolean z, String str2, boolean z2) {
    }

    public boolean updateAvailable(byte[] bArr, Long l) {
        return false;
    }

    public UpgradeSource(UpgradeSourceType upgradeSourceType) {
        this.upgradeType = upgradeSourceType;
    }

    public UpgradeSourceType getUpgradeType() {
        return this.upgradeType;
    }

    public String isUpgradeAcceptable(long j, long j2) {
        Logger.debug("OtaApp", "SourceBuildTimestamp - UpgradeSource" + j2 + SystemUpdateStatusUtils.SPACE + j);
        if (j2 <= 0 || j == j2) {
            return null;
        }
        return "Current and source UTCs does not match" + j + " sourceUtc" + j2;
    }

    public String[] isUpgradeAcceptable(String str, String str2) {
        String[] strArr = new String[2];
        if (!str.equals(str2)) {
            strArr[0] = String.format("Invalid version from server : DeviceSha1 %s andCurrentSha1 %s do not match", str, str2);
            strArr[1] = ErrorCodeMapper.KEY_SRC_VALIDATION_FAILED;
        }
        return strArr;
    }

    public String[] isUpgradeAcceptable(MetaData metaData, UpgradeSourceType upgradeSourceType) {
        String[] strArr = new String[2];
        if ("full".equalsIgnoreCase(metaData.getPackageType())) {
            return strArr;
        }
        String deviceSha1 = BuildPropReader.getDeviceSha1(upgradeSourceType.toString());
        String sourceSha1 = metaData.getSourceSha1();
        String targetSha1 = metaData.getTargetSha1();
        if (TextUtils.isEmpty(sourceSha1) || TextUtils.isEmpty(targetSha1) || TextUtils.isEmpty(deviceSha1)) {
            strArr[0] = String.format("Invalid version from server : sourceSha1 %s targetSha1 %s", sourceSha1, targetSha1);
            strArr[1] = ErrorCodeMapper.KEY_SRC_VALIDATION_FAILED;
            return strArr;
        }
        return isUpgradeAcceptable(deviceSha1, sourceSha1);
    }

    public boolean verifyFile(ApplicationEnv applicationEnv, String str, BotaSettings botaSettings) {
        try {
            applicationEnv.getUtilities().verifyPackage(new File(str));
            return true;
        } catch (Exception e) {
            botaSettings.setString(Configs.UPGRADE_STATUS_VERIFY, UpgradeUtilMethods.getStatusVerifyResult(e.toString()));
            Logger.error("OtaApp", "UpgradeSource.verifyFile failed: " + str);
            return false;
        }
    }

    public boolean isChangeInSrc(MetaData metaData, String str) {
        String sourceSha1 = metaData.getSourceSha1();
        String deviceSha1 = BuildPropReader.getDeviceSha1(str);
        if (TextUtils.isEmpty(sourceSha1)) {
            return false;
        }
        return !sourceSha1.equals(deviceSha1);
    }

    public boolean isUpdateSuccessful(MetaData metaData, String str) {
        String deviceSha1;
        String targetSha1 = metaData.getTargetSha1();
        if (UpgradeSourceType.modem.toString().equals(str)) {
            deviceSha1 = BuildPropReader.getDeviceModemSourceSha1();
        } else {
            deviceSha1 = BuildPropReader.getDeviceSha1(str);
        }
        String str2 = metaData.getmActualTargetVersion();
        Logger.debug("OtaApp", "isUpdateSuccessful: targetSha1=" + targetSha1 + " : devSha1=" + deviceSha1 + "actualDestVersion=" + str2);
        if (TextUtils.isEmpty(deviceSha1)) {
            return false;
        }
        return deviceSha1.equals(targetSha1) || deviceSha1.equals(str2);
    }
}
