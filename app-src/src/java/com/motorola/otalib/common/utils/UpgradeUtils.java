package com.motorola.otalib.common.utils;

import android.content.Context;
import android.text.TextUtils;
import androidx.core.content.ContextCompat;
import com.motorola.otalib.common.CommonLogger;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.errorCodes.UpdaterEngineErrorCodes;
import com.motorola.otalib.main.PublicUtilityMethods;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpgradeUtils {
    public static final String CHINA_PRODUCTION_SERVER = "moto-cds.svcmot.cn";
    public static final String CHINA_STAGING_SERVER = "ota-cn-sdc.blurdev.com";
    public static final int DEFAULT_CRITICAL_DEFER_COUNT = 3;
    public static final int DEFAULT_CRITICAL_UPDATE_ANNOY_VALUE = 240;
    public static final int DEFAULT_EXTRA_REMINDER_COUNT = 6;
    public static final long DEFAULT_EXTRA_REMINDER_PERIOD = 10;
    public static final int DEFAULT_MAX_CHUNK_SPACE_REQUIRED = 0;
    public static final int DEFAULT_MAX_UPDATE_FAIL_COUNT = 3;
    public static final String DEFAULT_MOTO_HELP = "";
    public static final String DEFAULT_OS_VERSION = "";
    public static final String DEVELOPMENT_SERVER = "moto-cds-dev.appspot.com";
    public static final int MEGABYTE = 1048576;
    public static final String PRODUCTION_SERVER = "moto-cds.appspot.com";
    public static final String QA_SERVER = "moto-cds-qa.appspot.com";
    public static final String STAGING_SERVER = "moto-cds-staging.appspot.com";

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum AB_INSTALL_TYPE {
        defaultAb,
        classicOnAb,
        streamingOnAb
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum DownloadStatus {
        STATUS_OK,
        STATUS_TEMP_OK,
        STATUS_RESOURCES,
        STATUS_DISABLED,
        STATUS_NETWORK,
        STATUS_FAIL,
        STATUS_SPACE,
        STATUS_VERIFY,
        STATUS_COPYFAIL,
        STATUS_MISMATCH,
        STATUS_SERVER,
        STATUS_SDCARD_RESOURCES_NOSDCARD,
        STATUS_SDCARD_RESOURCES_NOTMOUNTED,
        STATUS_SDCARD_RESOURCES_SPACE,
        STATUS_SDCARD_RESOURCES_WARNING,
        STATUS_SDCARD_RESOURCES_FAIL_REMOVAL,
        STATUS_SDCARD_RESOURCES_FAIL_SPACE,
        STATUS_CANCEL,
        STATUS_INSTALL_CANCEL,
        STATUS_INSTALL_CANCEL_NOTIFICATION,
        STATUS_DOWNLOAD_CANCEL_NOTIFICATION,
        STATUS_RESOURCES_REBOOT,
        STATUS_INSTALL_FAIL,
        STATUS_SPACE_BACKGROUND_INSTALL,
        STATUS_SPACE_INSTALL,
        STATUS_SPACE_INSTALL_CACHE,
        LOW_BATTERY_INSTALL,
        STATUS_SPACE_PAYLOAD_METADATA_CHECK,
        STATUS_FAIL_PAYLOAD_METADATA_VERIFY,
        STATUS_RESUME_ON_CELLULAR,
        STATUS_ALLOCATE_SPACE,
        STATUS_ALLOCATE_SPACE_SUCESS,
        STATUS_VAB_MAKE_SPACE_REQUEST_USER,
        STATUS_DEFERRED,
        STATUS_RETRIED,
        STATUS_VITAL_UPDATE_FAILED,
        FOTA_SHOW_ALERT_CELLULAR_POPUP,
        STATUS_SYSTEM_UPDATE_POLICY_ENABLED,
        STATUS_RESOURCES_WIFI
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum Error {
        ERR_OK,
        ERR_NET,
        ERR_REQUESTING,
        ERR_ALREADY,
        ERR_DOWNLOADING,
        ERR_INSTALL,
        ERR_NOTFOUND,
        ERR_NOTFOUND_BOOT_UNLOCK,
        ERR_NOTFOUND_ROOTED,
        ERR_NOTFOUND_DEVICE_CORRUPTED,
        ERR_NOTFOUND_ADV_NOTICE,
        ERR_NOTFOUND_EOL,
        ERR_TEMP,
        ERR_FAIL,
        ERR_BADPARAM,
        ERR_WIFI_NEEDED,
        ERR_INTERNAL,
        ERR_INIT_OTA_SERVICE,
        ERR_ROAMING,
        ERR_IN_CALL,
        ERR_BACKGROUND_INSTALL,
        ERR_STORAGE_LOW,
        ERR_CONTACTING_SERVER,
        ERR_POLICY_SET,
        ERR_VERITY_DISABLED,
        ERR_VAB_VALIDATION,
        ERR_VAB_VALIDATION_SUCCESS,
        ERR_VAB_VALIDATION_FAILURE,
        ERR_VAB_MERGE_PENDING,
        ERR_VAB_MERGE_RESTART,
        ERR_CTA_BG_DATA_DISABLED,
        ERR_VU_WIFI_ONLY_WIFI_NOT_AVAILABLE,
        ERR_ASC_ALLOWED,
        ERR_ASC_DENIED,
        ERR_NOT_ASC_DEVICE,
        ERR_ASC_SERVER,
        ERR_ASC_TIMEOUT,
        ERR_ASC_UNKNOWN,
        ERR_ASC_ALREADY,
        ERR_ASC_INVALID_TRANSACTION_ID,
        ERR_ASC_FAIL
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static final class MergeStatus {
        public static final int APPLY_PAYLOAD_SUCCESS = UpdaterEngineErrorCodes.K_SUCCESS;
        public static final int APPLY_PAYLOAD_FAILURE = UpdaterEngineErrorCodes.K_ERROR;
        public static final int APPLY_MERGE_CORRUPTED = UpdaterEngineErrorCodes.K_DEVICE_CORRUPTED;
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum SeverityType {
        OPTIONAL,
        MANDATORY,
        CRITICAL
    }

    public static String getAdminApnUrl(String str) {
        if (!TextUtils.isEmpty(str)) {
            try {
                return new JSONObject(str).optString("adminApnUrl");
            } catch (JSONException e) {
                CommonLogger.e(CommonLogger.TAG, "JSONException in UpgradeUtils, getAdminApnUrl: " + e.toString());
            }
        }
        return null;
    }

    public static JSONObject getJsonObjectFromMap(Map<String, Object> map, String str) {
        JSONObject jSONObject = new JSONObject();
        for (String str2 : map.keySet()) {
            try {
                jSONObject.put(str2, map.get(str2));
            } catch (JSONException e) {
                CommonLogger.e(CommonLogger.TAG, str + " caught exception on " + str2 + " :" + e);
            }
        }
        return jSONObject;
    }

    public static boolean checkIfAlreadyhavePermission(Context context, String str) {
        return ContextCompat.checkSelfPermission(context, str) == 0;
    }

    public static PublicUtilityMethods.OtaState PackageStateToOtaStateConverter(ApplicationEnv.PackageState packageState) {
        HashMap hashMap = new HashMap();
        hashMap.put(ApplicationEnv.PackageState.Notified, PublicUtilityMethods.OtaState.UpdateAvailable);
        hashMap.put(ApplicationEnv.PackageState.RequestPermission, PublicUtilityMethods.OtaState.WaitingForDLPermission);
        hashMap.put(ApplicationEnv.PackageState.QueueForDownload, PublicUtilityMethods.OtaState.QueueForDownload);
        hashMap.put(ApplicationEnv.PackageState.GettingDescriptor, PublicUtilityMethods.OtaState.FetchingDLDetails);
        hashMap.put(ApplicationEnv.PackageState.GettingPackage, PublicUtilityMethods.OtaState.Downloading);
        hashMap.put(ApplicationEnv.PackageState.Querying, PublicUtilityMethods.OtaState.WaitingForInstallPermission);
        hashMap.put(ApplicationEnv.PackageState.ABApplyingPatch, PublicUtilityMethods.OtaState.Installing);
        hashMap.put(ApplicationEnv.PackageState.Upgrading, PublicUtilityMethods.OtaState.Rebooting);
        hashMap.put(ApplicationEnv.PackageState.Result, PublicUtilityMethods.OtaState.Result);
        return (PublicUtilityMethods.OtaState) hashMap.get(packageState);
    }

    public static ApplicationEnv.PackageState OtaLibStateToPackageStateConverter(PublicUtilityMethods.OtaState otaState) {
        HashMap hashMap = new HashMap();
        hashMap.put(PublicUtilityMethods.OtaState.UpdateAvailable, ApplicationEnv.PackageState.Notified);
        hashMap.put(PublicUtilityMethods.OtaState.WaitingForDLPermission, ApplicationEnv.PackageState.RequestPermission);
        hashMap.put(PublicUtilityMethods.OtaState.UserApprovedDL, ApplicationEnv.PackageState.QueueForDownload);
        hashMap.put(PublicUtilityMethods.OtaState.QueueForDownload, ApplicationEnv.PackageState.QueueForDownload);
        hashMap.put(PublicUtilityMethods.OtaState.FetchingDLDetails, ApplicationEnv.PackageState.GettingDescriptor);
        hashMap.put(PublicUtilityMethods.OtaState.Downloading, ApplicationEnv.PackageState.GettingPackage);
        hashMap.put(PublicUtilityMethods.OtaState.WaitingForInstallPermission, ApplicationEnv.PackageState.Querying);
        hashMap.put(PublicUtilityMethods.OtaState.UserApprovedInstall, ApplicationEnv.PackageState.ABApplyingPatch);
        hashMap.put(PublicUtilityMethods.OtaState.Installing, ApplicationEnv.PackageState.ABApplyingPatch);
        hashMap.put(PublicUtilityMethods.OtaState.Rebooting, ApplicationEnv.PackageState.Upgrading);
        hashMap.put(PublicUtilityMethods.OtaState.Result, ApplicationEnv.PackageState.Result);
        return (ApplicationEnv.PackageState) hashMap.get(otaState);
    }
}
