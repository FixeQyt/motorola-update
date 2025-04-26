package com.motorola.otalib.common.errorCodes;

import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import java.util.HashMap;
import java.util.Map;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpdaterEngineErrorCodes {
    public static int ERROR_EXCEPTION = -1;
    public static int ERROR_NETWORK = 206;
    public static int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
    public static int HTTP_TOO_MANY_REQUESTS = 429;
    public static int K_DEVICE_CORRUPTED = 61;
    public static int K_DOWNLOAD_INVALID_METADATA_MAGIC_STRING = 21;
    public static int K_DOWNLOAD_INVALID_METADATA_SIGNATURE = 33;
    public static int K_DOWNLOAD_INVALID_METADATA_SIZE = 32;
    public static int K_DOWNLOAD_MANIFEST_PARSE_ERROR = 23;
    public static int K_DOWNLOAD_METADATA_SIGNATURE_ERROR = 24;
    public static int K_DOWNLOAD_METADATA_SIGNATURE_MISMATCH = 26;
    public static int K_DOWNLOAD_METADATA_SIGNATURE_MISSING_ERROR = 39;
    public static int K_DOWNLOAD_METADATA_SIGNATURE_VERIFICATION_ERROR = 25;
    public static int K_DOWNLOAD_NEW_PARTITION_INFO_ERROR = 13;
    public static int K_DOWNLOAD_OPERATION_EXECUTION_ERROR = 28;
    public static int K_DOWNLOAD_OPERATION_HASH_MISMATCH = 29;
    public static int K_DOWNLOAD_OPERATION_HASH_MISSING_ERROR = 38;
    public static int K_DOWNLOAD_OPERATION_HASH_VERIFICATION_ERROR = 27;
    public static int K_DOWNLOAD_PAYLOAD_PUB_KEY_VERIFICATION_ERROR = 18;
    public static int K_DOWNLOAD_PAYLOAD_VERIFICATION_ERROR = 12;
    public static int K_DOWNLOAD_SIGNATURE_MISSING_IN_MANIFEST = 22;
    public static int K_DOWNLOAD_STATE_INITIALIZATION_ERROR = 20;
    public static int K_DOWNLOAD_TRANSFER_ERROR = 9;
    public static int K_DOWNLOAD_WRITE_ERROR = 14;
    public static int K_ERROR = 1;
    public static int K_FILE_SYSTEM_COPIER_ERROR = 4;
    public static int K_FILE_SYSTEM_VERIFIER_ERROR = 47;
    public static int K_INSTALL_DEVICE_OPEN_ERROR = 7;
    public static int K_INTERNAL_LIB_CURL_ERROR = 57;
    public static int K_KERNEL_DEVICE_OPEN_ERROR = 8;
    public static int K_NEW_KERNEL_VERIFICATION_ERROR = 16;
    public static int K_NEW_ROOT_FS_VERIFICATION_ERROR = 15;
    public static int K_NOT_ENOUGH_SPACE = 60;
    public static int K_PAYLOAD_HASH_MISMATCH_ERROR = 10;
    public static int K_PAYLOAD_MISMATCHED_TYPE = 6;
    public static int K_PAYLOAD_SIZE_MISMATCH_ERROR = 11;
    public static int K_PAYLOAD_TIME_STAMP_ERROR = 51;
    public static int K_POST_INSTALL_BOOTED_FROM_FIRMWARE_B_ERROR = 19;
    public static int K_POST_INSTALL_FIRMWARE_RO_NOT_UPDATABLE = 43;
    public static int K_POST_INSTALL_POWER_WASH_ERROR = 41;
    public static int K_POST_INSTALL_RUNNER_ERROR = 5;
    public static int K_SIGNED_DELTA_PAYLOAD_EXPECTED_ERROR = 17;
    public static int K_SUCCESS = 0;
    public static int K_UNRESOLVED_HOST_ERROR = 58;
    public static int K_UNRESOLVED_HOST_RECOVERED = 59;
    public static int K_UNSUPPORTED_MAJOR_PAYLOAD_VERSION = 44;
    public static int K_UNSUPPORTED_MINOR_PAYLOAD_VERSION = 45;
    public static int K_UPDATED_BUT_NOT_ACTIVE = 52;
    public static int K_USER_CANCELED = 48;
    public static int K_VERITY_CALCULATION_ERROR = 56;
    public static int SERVER_INSUFFICIENT_STORAGE = 507;
    private static Map<Integer, String> errorCodeHandler_4XX;
    private static Map<Integer, String> errorCodeHandler_UE;

    static {
        HashMap hashMap = new HashMap();
        errorCodeHandler_4XX = hashMap;
        hashMap.put(400, "400 Bad Request");
        errorCodeHandler_4XX.put(401, "401 Unauthorized");
        errorCodeHandler_4XX.put(403, "403 Forbidden");
        errorCodeHandler_4XX.put(Integer.valueOf((int) SystemUpdateStatusUtils.ALERT_FAILED_FIRMWARE_UPDATE_PACKAGE_CERT_VALIDATE), "404 Not Found");
        errorCodeHandler_4XX.put(410, "410 Gone");
        errorCodeHandler_4XX.put(412, "412 Precondition failed");
        errorCodeHandler_4XX.put(Integer.valueOf(HTTP_REQUESTED_RANGE_NOT_SATISFIABLE), "416 Requested range not satisfiable");
        errorCodeHandler_4XX.put(Integer.valueOf(HTTP_TOO_MANY_REQUESTS), "429 Too many requests");
        HashMap hashMap2 = new HashMap();
        errorCodeHandler_UE = hashMap2;
        hashMap2.put(Integer.valueOf(K_ERROR), "METADATA_VERIFICATION_ERROR3_1.");
        errorCodeHandler_UE.put(Integer.valueOf(K_FILE_SYSTEM_COPIER_ERROR), "INSTALL_ERROR_3_4.");
        errorCodeHandler_UE.put(Integer.valueOf(K_POST_INSTALL_RUNNER_ERROR), "POST_INSTALL_ERROR_5_5.");
        errorCodeHandler_UE.put(Integer.valueOf(K_PAYLOAD_MISMATCHED_TYPE), "INSTALL_ERROR_3_6.");
        errorCodeHandler_UE.put(Integer.valueOf(K_INSTALL_DEVICE_OPEN_ERROR), "INSTALL_ERROR_3_7.");
        errorCodeHandler_UE.put(Integer.valueOf(K_KERNEL_DEVICE_OPEN_ERROR), "INSTALL_ERROR_3_8.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_TRANSFER_ERROR), "DOWNLOAD_FAILED_9.");
        errorCodeHandler_UE.put(Integer.valueOf(K_PAYLOAD_HASH_MISMATCH_ERROR), "FILESYSTEM_VERIFICATION_ERROR_4_10.");
        errorCodeHandler_UE.put(Integer.valueOf(K_PAYLOAD_SIZE_MISMATCH_ERROR), "FILESYSTEM_VERIFICATION_ERROR_4_11.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_PAYLOAD_VERIFICATION_ERROR), "METADATA_VERIFICATION_FAILURE_3_12.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_NEW_PARTITION_INFO_ERROR), "INSTALL_ERROR_3_13.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_WRITE_ERROR), "INSTALL_ERROR_3_14.");
        errorCodeHandler_UE.put(Integer.valueOf(K_NEW_ROOT_FS_VERIFICATION_ERROR), "INSTALL_ERROR_3_15.");
        errorCodeHandler_UE.put(Integer.valueOf(K_NEW_KERNEL_VERIFICATION_ERROR), "INSTALL_ERROR_3_16.");
        errorCodeHandler_UE.put(Integer.valueOf(K_SIGNED_DELTA_PAYLOAD_EXPECTED_ERROR), "INSTALL_ERROR_3_17.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_PAYLOAD_PUB_KEY_VERIFICATION_ERROR), "INSTALL_ERROR_3_18.");
        errorCodeHandler_UE.put(Integer.valueOf(K_POST_INSTALL_BOOTED_FROM_FIRMWARE_B_ERROR), "POST_INSTALL_ERROR_5_19.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_STATE_INITIALIZATION_ERROR), "INSTALL_ERROR_3_20.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_INVALID_METADATA_MAGIC_STRING), "METADATA_VERIFICATION_FAILURE_3_21.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_SIGNATURE_MISSING_IN_MANIFEST), "METADATA_VERIFICATION_FAILURE_3_22.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_MANIFEST_PARSE_ERROR), "METADATA_VERIFICATION_FAILURE_3_23.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_METADATA_SIGNATURE_ERROR), "METADATA_VERIFICATION-FAILURE_3_24.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_METADATA_SIGNATURE_VERIFICATION_ERROR), "METADATA_VERIFICATION_FAILURE_3_25.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_METADATA_SIGNATURE_MISMATCH), "METADATA_VERIFICATION_FAILURE_3_26.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_OPERATION_HASH_VERIFICATION_ERROR), "INSTALL_ERROR_3_27.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_OPERATION_EXECUTION_ERROR), "INSTALL_ERROR_3_28.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_OPERATION_HASH_MISMATCH), "INSTALL_ERROR_3_29.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_INVALID_METADATA_SIZE), "METADATA_VERIFICATION_FAILURE_3_32.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_INVALID_METADATA_SIGNATURE), "METADATA_VERIFICATION_FAILURE_3_33.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_OPERATION_HASH_MISSING_ERROR), "INSTALL_ERROR_3_38.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DOWNLOAD_METADATA_SIGNATURE_MISSING_ERROR), "METADATA_VERIFICATION_FAILURE_3_39.");
        errorCodeHandler_UE.put(Integer.valueOf(K_POST_INSTALL_POWER_WASH_ERROR), "POST_INSTALL_ERROR_5_41.");
        errorCodeHandler_UE.put(Integer.valueOf(K_POST_INSTALL_FIRMWARE_RO_NOT_UPDATABLE), "POST_INSTALL_ERROR_5_43.");
        errorCodeHandler_UE.put(Integer.valueOf(K_UNSUPPORTED_MAJOR_PAYLOAD_VERSION), "METADATA_VERIFICATION_FAILURE_3_44.");
        errorCodeHandler_UE.put(Integer.valueOf(K_UNSUPPORTED_MINOR_PAYLOAD_VERSION), "METADATA_VERIFICATION_FAILURE_3_45.");
        errorCodeHandler_UE.put(Integer.valueOf(K_FILE_SYSTEM_VERIFIER_ERROR), "FILESYSTEM_VERIFICATION_ERROR_4_47.");
        errorCodeHandler_UE.put(Integer.valueOf(K_USER_CANCELED), ErrorCodeMapper.KEY_USER_CANCELED_DOWNLOAD);
        errorCodeHandler_UE.put(Integer.valueOf(K_PAYLOAD_TIME_STAMP_ERROR), "METADATA_VERIFICATION_FAILURE_3_51.");
        errorCodeHandler_UE.put(Integer.valueOf(K_UPDATED_BUT_NOT_ACTIVE), "POST_INSTALL_ERROR_5_52.");
        errorCodeHandler_UE.put(Integer.valueOf(K_VERITY_CALCULATION_ERROR), "FILESYSTEM_VERIFICATION_ERROR_4_56.");
        errorCodeHandler_UE.put(Integer.valueOf(K_INTERNAL_LIB_CURL_ERROR), "DOWNLOAD_FAILED_57.");
        errorCodeHandler_UE.put(Integer.valueOf(K_UNRESOLVED_HOST_ERROR), "DOWNLOAD_FAILED_58.");
        errorCodeHandler_UE.put(Integer.valueOf(K_UNRESOLVED_HOST_RECOVERED), "DOWNLOAD_FAILED_59.");
        errorCodeHandler_UE.put(Integer.valueOf(K_NOT_ENOUGH_SPACE), "INSTALL_ERROR_3_60.");
        errorCodeHandler_UE.put(Integer.valueOf(K_DEVICE_CORRUPTED), "MERGE_FAILURE_61.");
    }

    public static boolean isItaRetriableError(int i) {
        if (errorCodeHandler_4XX.containsKey(Integer.valueOf(i)) || i == K_DOWNLOAD_TRANSFER_ERROR) {
            return true;
        }
        return i >= 500 && i <= SERVER_INSUFFICIENT_STORAGE;
    }

    public static boolean shouldWeFetchNewUrl(int i) {
        if (i == K_DOWNLOAD_TRANSFER_ERROR) {
            return true;
        }
        return errorCodeHandler_4XX.containsKey(Integer.valueOf(i));
    }

    public static String getErrorCodeDescription(int i) {
        if (errorCodeHandler_4XX.containsKey(Integer.valueOf(i))) {
            return errorCodeHandler_4XX.get(Integer.valueOf(i));
        }
        if (i == 9) {
            return "9 UE DL transfer error";
        }
        return String.valueOf(i);
    }

    public static String getFailureResultStatus(int i) {
        if (i >= 500 && i <= SERVER_INSUFFICIENT_STORAGE) {
            return ErrorCodeMapper.KEY_DOWNLOAD_FAILED_5XX;
        }
        if (errorCodeHandler_4XX.containsKey(Integer.valueOf(i))) {
            return ErrorCodeMapper.KEY_DOWNLOAD_FAILED_UE_4XX;
        }
        if (errorCodeHandler_UE.containsKey(Integer.valueOf(i))) {
            return errorCodeHandler_UE.get(Integer.valueOf(i));
        }
        return ErrorCodeMapper.KEY_OTHER;
    }

    public static String getFailureResultStatus(String str) {
        for (String str2 : errorCodeHandler_UE.values()) {
            if (str.contains(str2)) {
                return str2.substring(0, str2.length() - 1);
            }
        }
        return null;
    }
}
