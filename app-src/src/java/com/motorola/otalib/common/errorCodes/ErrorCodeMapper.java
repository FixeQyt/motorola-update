package com.motorola.otalib.common.errorCodes;

import com.motorola.otalib.common.CommonLogger;
import com.motorola.otalib.main.PublicUtilityMethods;
import java.util.HashSet;
import java.util.Set;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ErrorCodeMapper {
    public static final String KEY_APPLY_FAILURE = "APPLY_FAILURE.";
    public static final String KEY_BIND_FAILURE = "BIND_FAILURE.";
    public static final String KEY_BOOTLOADER_UNLOCKED = "BOOTLOADER_UNLOCKED.";
    public static final String KEY_CACHE_OUT_OF_SPACE = "CACHE_OUT_OF_SPACE.";
    public static final String KEY_CANCEL_VU_POST_SETUP_COMPLETE = "CANCEL_VU_POST_SETUP_COMPLETE.";
    public static final String KEY_DATA_OUT_OF_SPACE = "DATA_OUT_OF_SPACE.";
    public static final String KEY_DEVICE_BOOTED_FROM_SRC = "DEVICE_BOOTED_FROM_SRC.";
    public static final String KEY_DONT_BOTHER_WINDOW = "USER_IN_DONT_BOTHER_WINDOW.";
    public static final String KEY_DOWNLOAD_FAILED_5XX = "DOWNLOAD_FAILED_5XX.";
    public static final String KEY_DOWNLOAD_FAILED_ALLOCATE_SPACE_EXCEPTION = "DOWNLOAD_FAILED_ALLOCATE_SPACE_EXCEPTION.";
    public static final String KEY_DOWNLOAD_FAILED_PACKAGE_4XX = "DOWNLOAD_FAILED_PACKAGE_4XX.";
    public static final String KEY_DOWNLOAD_FAILED_PACKAGE_EXCEPTION = "DOWNLOAD_FAILED_PACKAGE_EXCEPTION.";
    public static final String KEY_DOWNLOAD_FAILED_PACKAGE_OTHER = "DOWNLOAD_FAILED_PACKAGE_OTHER.";
    public static final String KEY_DOWNLOAD_FAILED_PAYLOAD_METADATA_4XX = "DOWNLOAD_FAILED_PAYLOAD_METADATA_4XX.";
    public static final String KEY_DOWNLOAD_FAILED_PAYLOAD_METADATA_EXCEPTION = "DOWNLOAD_FAILED_PAYLOAD_METADATA_EXCEPTION.";
    public static final String KEY_DOWNLOAD_FAILED_PAYLOAD_METADATA_OTHER = "DOWNLOAD_FAILED_PAYLOAD_METADATA_OTHER.";
    public static final String KEY_DOWNLOAD_FAILED_UE_4XX = "DOWNLOAD_FAILED_UE_4XX.";
    public static final String KEY_DOWNLOAD_FAILED_UE_EXCEPTION = "DOWNLOAD_FAILED_PACKAGE_EXCEPTION.";
    public static final String KEY_DOWNLOAD_FAILED_UE_OTHER = "DOWNLOAD_FAILED_PACKAGE_OTHER.";
    public static final String KEY_DOWNLOAD_URL_NULL = "DOWNLOAD_URL_NULL.";
    public static final String KEY_EMPTY_STATUS_INFO = "EMPTY_STATUS_INFO";
    public static final String KEY_ERROR_DD_RESPONSE = "ERROR_DD_RESPONSE.";
    public static final String KEY_FAILED_FOTA = "FAILED_FOTA.";
    public static final String KEY_FAILED_FOTA_WIFI_DISCOVERY_TIMER = "FAILED_FOTA_WIFI_DISCOVERY_TIMER.";
    public static final String KEY_FAILED_IN_RECOVERY = "FAILED_IN_RECOVERY.";
    public static final String KEY_FINGERPRINT_MISMATCH = "FINGERPRINT_MISMATCH.";
    public static final String KEY_MERGE_STATUS_DEVICE_CORRUPTED = "MERGE_STATUS_DEVICE_CORRUPTED";
    public static final String KEY_MERGE_STATUS_FAILURE = "MERGE_STATUS_FAILURE";
    public static final String KEY_MODEM_CANCELED_BY_SERVER = "MODEM_CANCELED_BY_SERVER.";
    public static final String KEY_MODEM_DOWNLOAD_TIMER_EXPIRED = "MODEM_DOWNLOAD_TIMER_EXPIRED.";
    public static final String KEY_MODEM_UPDATE_FAILED = "MODEM_UPDATE_FAILED.";
    public static final String KEY_OTA_CANCELED_BY_SERVER = "CANCELED_BY_SERVER.";
    public static final String KEY_OTHER = "OTHER.";
    public static final String KEY_PACKAGE_NOT_IN_DEVICE = "PACKAGE_NOT_IN_DEVICE.";
    public static final String KEY_PACKAGE_SIZE_ZERO = "PACKAGE_SIZE_ZERO.";
    public static final String KEY_PACKAGE_VERIFICATION_FAILED = "PACKAGE_VERIFICATION_FAILED.";
    public static final String KEY_PARSE_ERROR = "PARSE_ERROR.";
    public static final String KEY_PAYLOAD_METADATA_VERIFICATION_FAILED = "PAYLOAD_METADATA_VERIFICATION_FAILED.";
    public static final String KEY_ROOTED = "ROOTED.";
    public static final String KEY_SRC_VALIDATION_FAILED = "SRC_VALIDATION_FAILED.";
    public static final String KEY_SUCCESS = "SUCCESS.";
    public static final String KEY_SU_CANCEL_BY_DM = "OMADM_CANCELED_UPDATE.";
    public static final String KEY_SYSTEM_UPDATE_POLICY = "DEVICE_UNDER_SYSTEM_UPDATE_POLICY.";
    public static final String KEY_TARGET_VERSION_MISMATCH = "TARGET_VERSION_MISMATCH.";
    public static final String KEY_UE_NOT_RESPONDING = "UE_NOT_RESPONDING.";
    public static final String KEY_UPDATE_BLOCKED_FREEZE_PERIOD = "KEY_UPDATE_BLOCKED_FREEZE_PERIOD.";
    public static final String KEY_UPDATE_CANCELLED = "USER_CANCELED_UPDATE.";
    public static final String KEY_UPDATE_DISABLED_BY_MOTO_POLICY_MNGR = "KEY_UPDATE_DISABLED_BY_MOTO_POLICY_MNGR.";
    public static final String KEY_UPDATE_DISABLED_BY_POLICY_MNGR = "KEY_UPDATE_DISABLED_BY_POLICY_MNGR.";
    public static final String KEY_UPDATE_FAILED_BY_ASC = "KEY_UPDATE_FAILED_BY_ASC.";
    public static final String KEY_USER_CANCELED_DOWNLOAD = "USER_CANCELED_DOWNLOAD.";
    public static final String KEY_VU_WIFI_ONLY_PACKAGE_WIFI_NOT_AVAILABLE = "KEY_VU_WIFI_ONLY_PACKAGE_WIFI_NOT_AVAILABLE.";
    private static Set<String> keyLibStatusMap;
    private static Set<String> keyStatusMap;

    static {
        HashSet hashSet = new HashSet();
        keyStatusMap = hashSet;
        hashSet.add(KEY_SUCCESS);
        keyStatusMap.add(KEY_UPDATE_CANCELLED);
        keyStatusMap.add(KEY_SU_CANCEL_BY_DM);
        keyStatusMap.add(KEY_FINGERPRINT_MISMATCH);
        keyStatusMap.add(KEY_DONT_BOTHER_WINDOW);
        keyStatusMap.add(KEY_OTA_CANCELED_BY_SERVER);
        keyStatusMap.add(KEY_MODEM_CANCELED_BY_SERVER);
        keyStatusMap.add(KEY_MODEM_UPDATE_FAILED);
        keyStatusMap.add(KEY_MODEM_DOWNLOAD_TIMER_EXPIRED);
        keyStatusMap.add(KEY_DEVICE_BOOTED_FROM_SRC);
        keyStatusMap.add(KEY_PACKAGE_NOT_IN_DEVICE);
        keyStatusMap.add(KEY_DATA_OUT_OF_SPACE);
        keyStatusMap.add(KEY_CACHE_OUT_OF_SPACE);
        keyStatusMap.add(KEY_PARSE_ERROR);
        keyStatusMap.add(KEY_SRC_VALIDATION_FAILED);
        keyStatusMap.add(KEY_TARGET_VERSION_MISMATCH);
        keyStatusMap.add(KEY_FAILED_IN_RECOVERY);
        keyStatusMap.add(KEY_SYSTEM_UPDATE_POLICY);
        keyStatusMap.add(KEY_CANCEL_VU_POST_SETUP_COMPLETE);
        keyStatusMap.add(KEY_PACKAGE_SIZE_ZERO);
        keyStatusMap.add(KEY_OTHER);
        keyStatusMap.add(KEY_USER_CANCELED_DOWNLOAD);
        keyStatusMap.add(KEY_DOWNLOAD_FAILED_PACKAGE_4XX);
        keyStatusMap.add(KEY_DOWNLOAD_FAILED_5XX);
        keyStatusMap.add("DOWNLOAD_FAILED_PACKAGE_OTHER.");
        keyStatusMap.add("DOWNLOAD_FAILED_PACKAGE_EXCEPTION.");
        keyStatusMap.add(KEY_DOWNLOAD_FAILED_PAYLOAD_METADATA_4XX);
        keyStatusMap.add(KEY_DOWNLOAD_FAILED_PAYLOAD_METADATA_EXCEPTION);
        keyStatusMap.add(KEY_DOWNLOAD_FAILED_PAYLOAD_METADATA_OTHER);
        keyStatusMap.add(KEY_DOWNLOAD_FAILED_UE_4XX);
        keyStatusMap.add("DOWNLOAD_FAILED_PACKAGE_OTHER.");
        keyStatusMap.add("DOWNLOAD_FAILED_PACKAGE_EXCEPTION.");
        keyStatusMap.add(KEY_PACKAGE_VERIFICATION_FAILED);
        keyStatusMap.add(KEY_ERROR_DD_RESPONSE);
        keyStatusMap.add(KEY_DOWNLOAD_URL_NULL);
        keyStatusMap.add(KEY_PAYLOAD_METADATA_VERIFICATION_FAILED);
        keyStatusMap.add(KEY_FAILED_FOTA);
        keyStatusMap.add(KEY_FAILED_FOTA_WIFI_DISCOVERY_TIMER);
        keyStatusMap.add(KEY_APPLY_FAILURE);
        keyStatusMap.add(KEY_BIND_FAILURE);
        keyStatusMap.add(KEY_UE_NOT_RESPONDING);
        keyStatusMap.add(KEY_ROOTED);
        keyStatusMap.add(KEY_BOOTLOADER_UNLOCKED);
        keyStatusMap.add(KEY_UPDATE_DISABLED_BY_POLICY_MNGR);
        keyStatusMap.add(KEY_UPDATE_DISABLED_BY_MOTO_POLICY_MNGR);
        keyStatusMap.add(KEY_UPDATE_BLOCKED_FREEZE_PERIOD);
        keyStatusMap.add(KEY_VU_WIFI_ONLY_PACKAGE_WIFI_NOT_AVAILABLE);
        keyStatusMap.add(KEY_UPDATE_FAILED_BY_ASC);
        keyStatusMap.add(KEY_MERGE_STATUS_DEVICE_CORRUPTED);
        keyStatusMap.add(KEY_EMPTY_STATUS_INFO);
        HashSet hashSet2 = new HashSet();
        keyLibStatusMap = hashSet2;
        hashSet2.add(PublicUtilityMethods.STATUS_CODE.ERROR_CORRUPT_FIRMWARE.name());
        keyLibStatusMap.add(PublicUtilityMethods.STATUS_CODE.ERROR_LOW_BATTERY.name());
        keyLibStatusMap.add(PublicUtilityMethods.STATUS_CODE.ERROR_DEVICE_OUT_OF_RANGE.name());
        keyLibStatusMap.add(PublicUtilityMethods.STATUS_CODE.ERROR_DEVICE_DISCONNECTED.name());
        keyLibStatusMap.add(PublicUtilityMethods.STATUS_CODE.ERROR_CASE_CLOSED.name());
        keyLibStatusMap.add(PublicUtilityMethods.STATUS_CODE.ERROR_USER_CANCELLED.name());
        keyLibStatusMap.add(PublicUtilityMethods.STATUS_CODE.ERROR_VERSION_MISMATCH.name());
        keyLibStatusMap.add(PublicUtilityMethods.STATUS_CODE.ERROR_OTHER.name());
        keyLibStatusMap.add(PublicUtilityMethods.STATUS_CODE.SUCCESS.name());
    }

    public static String getStatus(String str) {
        if (str == null) {
            return null;
        }
        for (String str2 : keyStatusMap) {
            if (str.contains(str2)) {
                return str2.substring(0, str2.length() - 1);
            }
        }
        for (String str3 : keyLibStatusMap) {
            if (str.contains(str3)) {
                return str3;
            }
        }
        String failureResultStatus = UpdaterEngineErrorCodes.getFailureResultStatus(str);
        if (failureResultStatus != null) {
            return failureResultStatus;
        }
        CommonLogger.d(CommonLogger.TAG, "Info " + str);
        return KEY_OTHER.substring(0, 5);
    }
}
