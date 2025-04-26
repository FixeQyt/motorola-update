package com.motorola.otalib.common.metaData.builder;

import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.CommonLogger;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.utils.UpgradeUtils;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class MetaDataBuilder {
    public static final String METADATA_FILE = "metadata.json";

    public static MetaData from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new MetaData(jSONObject.optString("metaVersion"), jSONObject.optString("version", ""), jSONObject.optString("minVersion", ""), jSONObject.optBoolean("forced"), jSONObject.optString("downloadOptionsNotes"), jSONObject.optString(UpgradeUtilConstants.KEY_HISTORY_RELEASE_NOTES), jSONObject.getLong("size"), jSONObject.optString("md5_checksum"), jSONObject.optLong("extraSpace"), jSONObject.optString("annoy"), jSONObject.optString("installReminder"), jSONObject.optBoolean("wifionly"), jSONObject.optString("fingerprint"), jSONObject.optInt("installTime"), jSONObject.optBoolean("serviceControlEnabled", true), jSONObject.optInt("serviceTimeoutSeconds", 60), jSONObject.optBoolean("continueOnServiceError", true), jSONObject.optString("upgradeNotification"), jSONObject.optString("preInstallNotes"), jSONObject.optString("postInstallNotes"), jSONObject.optString("postInstallFailureMessage"), jSONObject.optString("reportingTag"), jSONObject.optString("trackingId"), jSONObject.optString("updateReqTriggeredBy"), DownloadTimesBuilder.from(jSONObject.optJSONObject("downloadTimes")), OffPeakDownloadBuilder.from(jSONObject.optJSONObject("offPeakDownload")), jSONObject.optBoolean("showPreDownloadDialog", true), jSONObject.optBoolean("showDownloadOptions", false), jSONObject.optInt("preDownloadNotificationExpiryMins", 2880), jSONObject.optInt("preInstallNotificationExpiryMins", 2880), jSONObject.optJSONObject("uiWorkflowControl"), jSONObject.optInt("optionalUpdateDeferCount", -1), jSONObject.optInt("criticalUpdateDeferCount", 3), jSONObject.optInt("maxUpdateFailCount", 3), jSONObject.optInt("minBatteryRequiredForInstall", 25), jSONObject.optBoolean("bypassPreDownloadDialog", false), jSONObject.optBoolean("showDownloadProgress", true), jSONObject.optBoolean("showPreInstallScreen", true), jSONObject.optBoolean("showPostInstallScreen", true), jSONObject.optBoolean("rebootRequired", true), jSONObject.optInt("incrementalVersion"), jSONObject.optDouble("forceDownloadTime", -1.0d), jSONObject.optDouble("forceInstallTime", -1.0d), jSONObject.optInt("forceUpgradeTime", -1), jSONObject.optBoolean("forceOnCellular", false), jSONObject.optInt("downloadStartTime", -1), jSONObject.optInt("downloadEndTime", -1), jSONObject.optBoolean("policyBundle", false), jSONObject.optLong("extraSpaceCache", 52428800L), jSONObject.optInt("optionalUpdateCancelReminderDays", 45), jSONObject.optLong("reserveSpaceInMb", -1L), jSONObject.optBoolean("enterpriseOta", true), jSONObject.optString("displayVersion"), jSONObject.optBoolean("oemConfigUpdate", false), jSONObject.optString(UpgradeUtilConstants.KEY_HISTORY_UPDATE_TYPE, "DEFAULT"), jSONObject.optLong("abMaxChunkSize", 0L), jSONObject.optString("actualTargetBlurVersion"), jSONObject.optInt("severityType", UpgradeUtils.SeverityType.OPTIONAL.ordinal()), jSONObject.optInt("criticalUpdateReminder", UpgradeUtils.DEFAULT_CRITICAL_UPDATE_ANNOY_VALUE), jSONObject.optString("abInstallType", UpgradeUtils.AB_INSTALL_TYPE.defaultAb.name()), jSONObject.optJSONObject("streamingData"), jSONObject.optString("OSreleaseLink", ""), jSONObject.optString("targetOSVersion", ""), jSONObject.optLong("sourceBuildTimestamp", 0L), jSONObject.optString("preDownloadInstructions", ""), jSONObject.optString("preInstallInstructions", ""), jSONObject.optString("otaSourceSha1", ""), jSONObject.optString("otaTargetSha1", ""), jSONObject.optString("packageType", "delta"), jSONObject.optLong("criticalUpdateExtraWaitPeriod", 10L), jSONObject.optInt("criticalUpdateExtraWaitCount", 6), jSONObject.optInt("featureEnableBitmap", 0), jSONObject.optLong("USERDATA_REQUIRED_FOR_UPDATE"));
        } catch (JSONException e) {
            CommonLogger.e(CommonLogger.TAG, "MetaDataBuilder.from(object) caught exception :" + e);
            return null;
        }
    }

    public static MetaData from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CommonLogger.e(CommonLogger.TAG, "MetaDataBuilder.from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(MetaData metaData) throws JSONException {
        if (metaData == null) {
            return null;
        }
        return new JSONObject().put("metaVersion", metaData.getVersion()).put("version", metaData.getVersion()).put("minVersion", metaData.getMinVersion()).put("forced", metaData.isForced()).put(UpgradeUtilConstants.KEY_HISTORY_RELEASE_NOTES, metaData.getReleaseNotes()).put("downloadOptionsNotes", metaData.getDownloadOptionsNotes()).put("size", metaData.getSize()).putOpt("md5_checksum", metaData.getmd5CheckSum()).put("extraSpace", metaData.getExtraSpace()).put("extraSpace", metaData.getExtraSpace()).put("annoy", metaData.getAnnoy()).put("installReminder", metaData.getInstallReminder()).put("wifionly", metaData.isWifiOnly()).put("fingerprint", metaData.getFingerprint()).put("downloadUrl", metaData.getDownloadUrl()).put("installTime", metaData.getInstallTime()).put("serviceControlEnabled", metaData.isServiceControlEnabled()).put("serviceTimeoutSeconds", metaData.getServiceTimeoutSeconds()).put("continueOnServiceError", metaData.isContinueOnServiceError()).put("upgradeNotification", metaData.getUpgradeNotification()).put("preInstallNotes", metaData.getPreInstallNotes()).put("postInstallNotes", metaData.getPostInstallNotes()).put("postInstallFailureMessage", metaData.getPostInstallFailureMessage()).put("reportingTag", metaData.getReportingTags()).put("trackingId", metaData.getTrackingId()).put("updateReqTriggeredBy", metaData.getUpdateReqTriggeredBy()).put("downloadTimes", DownloadTimesBuilder.toJSONObject(metaData.getDownloadTimes())).put("offPeakDownload", OffPeakDownloadBuilder.toJSONObject(metaData.getOffPeakDownload())).putOpt("showPreDownloadDialog", Boolean.valueOf(metaData.showPreDownloadDialog())).putOpt("showDownloadOptions", Boolean.valueOf(metaData.showDownloadOptions())).putOpt("preDownloadNotificationExpiryMins", Integer.valueOf(metaData.getPreDownloadNotificationExpiryMins())).putOpt("preInstallNotificationExpiryMins", Integer.valueOf(metaData.getPreInstallNotificationExpiryMins())).put("uiWorkflowControl", metaData.getUiWorkflowControl()).put("optionalUpdateDeferCount", metaData.getOptionalDeferCount()).put("criticalUpdateDeferCount", metaData.getCriticalDeferCount()).put("maxUpdateFailCount", metaData.getMaxUpdateFailCount()).put("minBatteryRequiredForInstall", metaData.getminBatteryRequiredForInstall()).put("bypassPreDownloadDialog", metaData.getByPassPreDownloadDialog()).putOpt("showDownloadProgress", Boolean.valueOf(metaData.showDownloadProgress())).putOpt("showPreInstallScreen", Boolean.valueOf(metaData.showPreInstallScreen())).putOpt("showPostInstallScreen", Boolean.valueOf(metaData.showPostInstallScreen())).putOpt("rebootRequired", Boolean.valueOf(metaData.getRebootRequired())).putOpt("incrementalVersion", Integer.valueOf(metaData.getIncrementalVersion())).putOpt("forceDownloadTime", Double.valueOf(metaData.getForceDownloadTime())).putOpt("forceInstallTime", Double.valueOf(metaData.getForceInstallTime())).putOpt("forceUpgradeTime", Integer.valueOf(metaData.getForceUpgradeTime())).putOpt("forceOnCellular", Boolean.valueOf(metaData.isForceOnCellular())).putOpt("downloadStartTime", Integer.valueOf(metaData.getDownloadStartTime())).putOpt("downloadEndTime", Integer.valueOf(metaData.getDownloadEndTime())).putOpt("policyBundle", Boolean.valueOf(metaData.getPolicyBundle())).putOpt("optionalUpdateCancelReminderDays", Integer.valueOf(metaData.getOptionalUpdateCancelReminderDays())).putOpt("reserveSpaceInMb", Long.valueOf(metaData.getReserveSpaceInMb())).putOpt("enterpriseOta", Boolean.valueOf(metaData.getEnterpriseOta())).putOpt("displayVersion", metaData.getDisplayVersion()).putOpt("oemConfigUpdate", Boolean.valueOf(metaData.getOemConfigUpdateData())).putOpt(UpgradeUtilConstants.KEY_HISTORY_UPDATE_TYPE, metaData.getUpdateTypeData()).putOpt("abMaxChunkSize", Long.valueOf(metaData.getChunkSize())).putOpt("actualTargetBlurVersion", metaData.getmActualTargetVersion()).putOpt("severityType", Integer.valueOf(metaData.getSeverity())).putOpt("criticalUpdateReminder", Integer.valueOf(metaData.getCriticalUpdateReminder())).putOpt("abInstallType", metaData.getAbInstallType()).putOpt("streamingData", metaData.getStreamingData()).putOpt("OSreleaseLink", metaData.getOSreleaseLink()).putOpt("targetOSVersion", metaData.getTargetOSVersion()).putOpt("sourceBuildTimestamp", Long.valueOf(metaData.getSourceBuildTimeStamp())).put("otaSourceSha1", metaData.getSourceSha1()).putOpt("preDownloadInstructions", metaData.getPreDownloadInstructions()).putOpt("preInstallInstructions", metaData.getPreInstallInstructions()).put("otaTargetSha1", metaData.getTargetSha1()).putOpt("packageType", metaData.getPackageType()).putOpt("criticalUpdateExtraWaitPeriod", Long.valueOf(metaData.getCriticalUpdateExtraWaitPeriod())).putOpt("criticalUpdateExtraWaitCount", Integer.valueOf(metaData.getCriticalUpdateExtraWaitCount())).putOpt("featureEnableBitmap", Integer.valueOf(metaData.getBitmap())).putOpt("USERDATA_REQUIRED_FOR_UPDATE", Long.valueOf(metaData.getUserDataRequiredForUpdate()));
    }

    public static String toJSONString(MetaData metaData) throws JSONException {
        if (metaData == null) {
            return null;
        }
        return toJSONObject(metaData).toString();
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:66:0x0121 A[Catch: IOException -> 0x011d, TRY_LEAVE, TryCatch #9 {IOException -> 0x011d, blocks: (B:62:0x0119, B:66:0x0121), top: B:78:0x0119 }] */
    /* JADX WARN: Removed duplicated region for block: B:78:0x0119 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r3v1 */
    /* JADX WARN: Type inference failed for: r3v3, types: [java.io.InputStream] */
    /* JADX WARN: Type inference failed for: r3v9 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static com.motorola.otalib.common.metaData.MetaData readMetaDataFromFile(java.lang.String r8, com.motorola.otalib.common.settings.Settings r9, com.motorola.otalib.common.settings.ISetting r10) {
        /*
            Method dump skipped, instructions count: 312
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.otalib.common.metaData.builder.MetaDataBuilder.readMetaDataFromFile(java.lang.String, com.motorola.otalib.common.settings.Settings, com.motorola.otalib.common.settings.ISetting):com.motorola.otalib.common.metaData.MetaData");
    }

    public static String getStringMetaJsonFile(String str, String str2) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject = new JSONObject(str2);
        } catch (JSONException e) {
            CommonLogger.e(CommonLogger.TAG, "Exception occurred while converting metadata json file to Json Object in getStringMetaJsonFile: exception msg=" + e);
        }
        return jSONObject.optString(str);
    }

    public static long getLongMetaJsonFile(String str, String str2) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject = new JSONObject(str2);
        } catch (JSONException e) {
            CommonLogger.e(CommonLogger.TAG, "Exception occurred while converting metadata json file to Json Object in getLongMetaJsonFile: exception msg=" + e);
        }
        return jSONObject.optLong(str);
    }

    public static int getIntMetaJsonFile(String str, String str2) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject = new JSONObject(str2);
        } catch (JSONException e) {
            CommonLogger.e(CommonLogger.TAG, "Exception occurred while converting metadata json file to Json Object in getIntMetaJsonFile: exception msg=" + e);
        }
        return jSONObject.optInt(str);
    }

    public static boolean getBooleanMetaJsonFile(String str, String str2) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject = new JSONObject(str2);
        } catch (JSONException e) {
            CommonLogger.e(CommonLogger.TAG, "Exception occurred while converting metadata json file to Json Object in getBooleanMetaJsonFile: exception msg=" + e);
        }
        return jSONObject.optBoolean(str);
    }

    public static JSONObject getJsonObjectMetaJsonFile(String str, String str2) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject = new JSONObject(str2);
        } catch (JSONException e) {
            CommonLogger.e(CommonLogger.TAG, "Exception occurred while converting metadata json file to Json Object in getJsonObjectMetaJsonFile: exception msg=" + e);
        }
        return jSONObject.optJSONObject(str);
    }
}
