package com.motorola.ccc.ota;

import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.otalib.common.metaData.MetaData;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public interface NewVersionHandler {

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public enum ReturnCode {
        NEW_VERSION_OK,
        NEW_VERSION_FAIL,
        NEW_VERSION_ALREADY,
        NEW_VERSION_INVALID,
        NEW_VERSION_FAIL_ROOTED,
        NEW_VERSION_FAIL_BOOTLOADER_UNLOCKED,
        NEW_VERSION_VERITY_DISABLED,
        NEW_VERSION_FAIL_DEVICE_CORRUPTED,
        VAB_VALIDATION_PKG_FOUND,
        UPDATE_DISABLED_BY_POLICY_MNGR,
        UPDATE_DISABLED_BY_MOTO_POLICY_MNGR,
        UPDATE_BLOCKED_FREEZE_PERIOD,
        VU_WIFI_ONLY_PACKAGE_WIFI_NOT_AVAILABLE
    }

    ReturnCode handleNewVersion(MetaData metaData, String str, UpgradeSourceType upgradeSourceType, String str2, long j, String str3, String str4);
}
