package com.motorola.ccc.ota.utils;

import android.content.Context;
import android.text.TextUtils;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtilConstants;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtils;
import com.motorola.otalib.common.metaData.CheckForUpgradeTriggeredBy;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class MetadataOverrider {
    public static MetaData from(MetaData metaData, BotaSettings botaSettings, String str, boolean z, String str2, String str3) {
        if (z) {
            String string = botaSettings.getString(Configs.METADATA_ORIGINAL);
            MetaData from = MetaDataBuilder.from(string);
            if (string != null && from != null) {
                metaData = from;
            }
        }
        if (metaData.getUiWorkflowControl() != null && !TextUtils.isEmpty(str)) {
            try {
                JSONObject jSONObject = new JSONObject(metaData.getUiWorkflowControl().toString());
                int i = AnonymousClass1.$SwitchMap$com$motorola$otalib$common$metaData$CheckForUpgradeTriggeredBy[CheckForUpgradeTriggeredBy.valueOf(str).ordinal()];
                if (i != 1) {
                    if (i != 2) {
                        if (i != 3) {
                            return i != 4 ? metaData : getOverridedMetadata(jSONObject.getJSONObject("polling"), metaData, str2, str3, CheckForUpgradeTriggeredBy.polling.name());
                        }
                        return getOverridedMetadata(jSONObject.getJSONObject("notification"), metaData, str2, str3, CheckForUpgradeTriggeredBy.notification.name());
                    }
                    return getOverridedMetadata(jSONObject.getJSONObject("setup"), metaData, str2, str3, CheckForUpgradeTriggeredBy.setup.name());
                }
                return getOverridedMetadata(jSONObject.getJSONObject("user"), metaData, str2, str3, CheckForUpgradeTriggeredBy.user.name());
            } catch (JSONException e) {
                Logger.debug("OtaApp", "Exception in MetadataOverrider.from " + e);
            }
        }
        return metaData;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.utils.MetadataOverrider$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$common$metaData$CheckForUpgradeTriggeredBy;

        static {
            int[] iArr = new int[CheckForUpgradeTriggeredBy.values().length];
            $SwitchMap$com$motorola$otalib$common$metaData$CheckForUpgradeTriggeredBy = iArr;
            try {
                iArr[CheckForUpgradeTriggeredBy.user.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$metaData$CheckForUpgradeTriggeredBy[CheckForUpgradeTriggeredBy.setup.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$metaData$CheckForUpgradeTriggeredBy[CheckForUpgradeTriggeredBy.notification.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$metaData$CheckForUpgradeTriggeredBy[CheckForUpgradeTriggeredBy.polling.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$metaData$CheckForUpgradeTriggeredBy[CheckForUpgradeTriggeredBy.other.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    public static boolean saveMetadata(MetaData metaData, BotaSettings botaSettings, boolean z) {
        try {
            if (z) {
                botaSettings.setString(Configs.MODEM_METADATA, MetaDataBuilder.toJSONString(metaData));
                return true;
            }
            botaSettings.setString(Configs.METADATA, MetaDataBuilder.toJSONString(metaData));
            return true;
        } catch (JSONException e) {
            Logger.error("OtaApp", "saveMetadata, Exception in setting Metadata " + e);
            return false;
        }
    }

    private static MetaData getOverridedMetadata(JSONObject jSONObject, MetaData metaData, String str, String str2, String str3) throws JSONException {
        JSONObject jSONObject2 = new JSONObject();
        jSONObject2.put("forced", jSONObject.optBoolean("forced", metaData.isForced())).put("wifionly", jSONObject.optBoolean("wifionly", metaData.isWifiOnly())).put("showPreDownloadDialog", jSONObject.optBoolean("showPreDownloadDialog", metaData.showPreDownloadDialog())).put("showDownloadOptions", jSONObject.optBoolean("showDownloadOptions", metaData.showDownloadOptions())).put("preDownloadNotificationExpiryMins", jSONObject.optInt("preDownloadNotificationExpiryMins", metaData.getPreDownloadNotificationExpiryMins())).put("preInstallNotificationExpiryMins", jSONObject.optInt("preInstallNotificationExpiryMins", metaData.getPreInstallNotificationExpiryMins())).put("showDownloadProgress", jSONObject.optBoolean("showDownloadProgress", metaData.showDownloadProgress())).put("showPreInstallScreen", jSONObject.optBoolean("showPreInstallScreen", metaData.showPreInstallScreen())).put("showPostInstallScreen", jSONObject.optBoolean("showPostInstallScreen", metaData.showPostInstallScreen())).put("rebootRequired", jSONObject.optBoolean("rebootRequired", metaData.getRebootRequired())).put("reportingTag", str).put("trackingId", str2).put("updateReqTriggeredBy", str3);
        return returnOverWrittenMetaData(metaData, jSONObject2);
    }

    public static MetaData overWriteForceUpgradeValues(MetaData metaData, String str, String str2) {
        if (metaData == null) {
            Logger.debug("OtaApp", "MetadataOverrider.overWriteForceUpgradeValues, strange no metadata .. return");
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("reportingTag", str).put("trackingId", str2).put("showPreDownloadDialog", false).put("showDownloadProgress", false).put("showPreInstallScreen", false);
            return returnOverWrittenMetaData(metaData, jSONObject);
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in MetadataOverrider.from " + e);
            return metaData;
        }
    }

    public static MetaData overWriteMetaDataValuesOnOtaFailExpiry(MetaData metaData, String str, String str2) {
        if (metaData == null) {
            Logger.debug("OtaApp", "MetadataOverrider.overWritePreDownloadAndInstallDialogValues, strange no metadata .. return");
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("reportingTag", str).put("trackingId", str2).put("showPreDownloadDialog", true).put("forceDownloadTime", -1);
            return returnOverWrittenMetaData(metaData, jSONObject);
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in MetadataOverrider.from " + e);
            return metaData;
        }
    }

    public static MetaData overWriteModemMetaDataValues(MetaData metaData) {
        Logger.debug("OtaApp", "overWriteModemMetaDataValues");
        if (metaData == null) {
            Logger.debug("OtaApp", "MetadataOverrider.overWriteModemMetaDataValues, strange no metadata .. return");
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("showPreDownloadDialog", false).put("showDownloadOptions", false).put("showDownloadProgress", false).put("forced", true);
            return returnOverWrittenMetaData(metaData, jSONObject);
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in MetadataOverrider:overWriteModemMetaDataValues.from " + e);
            return metaData;
        }
    }

    public static MetaData overWriteASCMetaDataValues(MetaData metaData, Context context) {
        if (metaData == null) {
            Logger.debug("OtaApp", "MetadataOverrider.overWriteAttFirstNetUpdateReminderValues, strange no metadata .. return");
            return null;
        }
        Map<String, Integer> ascOverridenMetaData = ThinkShieldUtils.getAscOverridenMetaData(context);
        int intValue = ascOverridenMetaData.get(ThinkShieldUtilConstants.EXTRA_CRITICAL_UPDATE_REMINDER).intValue();
        int intValue2 = ascOverridenMetaData.get(ThinkShieldUtilConstants.EXTRA_CRITICAL_UPDATE_DEFER_COUNT).intValue();
        int intValue3 = ascOverridenMetaData.get(ThinkShieldUtilConstants.EXTRA_SHOW_PRE_DOWNLOAD_DIALOG).intValue();
        try {
            JSONObject jSONObject = new JSONObject();
            if (intValue >= 0) {
                jSONObject.put("criticalUpdateReminder", intValue).put("criticalUpdateDeferCount", intValue2).put("criticalUpdateExtraWaitCount", 0).put("criticalUpdateExtraWaitPeriod", 0).put("severityType", UpgradeUtils.SeverityType.CRITICAL.ordinal());
            }
            if (intValue3 == 0) {
                jSONObject.put("showPreDownloadDialog", false);
            } else if (intValue3 == 1) {
                jSONObject.put("showPreDownloadDialog", true);
            }
            return returnOverWrittenMetaData(metaData, jSONObject);
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in overWriteASCCriticalUpdateReminderValues.from " + e);
            return metaData;
        }
    }

    public static MetaData overWriteAttFirstNetUpdateReminderValues(MetaData metaData, int i, int i2) {
        if (metaData == null) {
            Logger.debug("OtaApp", "MetadataOverrider.overWriteAttFirstNetUpdateReminderValues, strange no metadata .. return");
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("criticalUpdateReminder", i).put("criticalUpdateDeferCount", i2);
            return returnOverWrittenMetaData(metaData, jSONObject);
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in MetadataOverrider.from " + e);
            return metaData;
        }
    }

    public static MetaData returnOverWrittenMetaData(MetaData metaData, String str, String str2) {
        try {
            JSONObject jSONObject = MetaDataBuilder.toJSONObject(metaData);
            jSONObject.put(str, str2);
            return MetaDataBuilder.from(jSONObject);
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in MetadataOverrider, returnOverWrittenMetaData: " + e);
            return metaData;
        }
    }

    public static MetaData returnOverWrittenMetaData(MetaData metaData, String str, boolean z) {
        try {
            JSONObject jSONObject = MetaDataBuilder.toJSONObject(metaData);
            jSONObject.put(str, z);
            return MetaDataBuilder.from(jSONObject);
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in MetadataOverrider, returnOverWrittenMetaData: " + e);
            return metaData;
        }
    }

    public static MetaData returnOverWrittenMetaData(MetaData metaData, String str, long j) {
        try {
            JSONObject jSONObject = MetaDataBuilder.toJSONObject(metaData);
            jSONObject.put(str, j);
            return MetaDataBuilder.from(jSONObject);
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in MetadataOverrider, returnOverWrittenMetaData: " + e);
            return metaData;
        }
    }

    public static MetaData returnOverWrittenMetaData(MetaData metaData, JSONObject jSONObject) throws JSONException {
        JSONObject jSONObject2 = MetaDataBuilder.toJSONObject(metaData);
        if (jSONObject2 == null) {
            return metaData;
        }
        Iterator<String> keys = jSONObject.keys();
        while (keys.hasNext()) {
            String next = keys.next();
            Object obj = jSONObject.get(next);
            if (obj instanceof Integer) {
                jSONObject2.put(next, jSONObject.getInt(next));
            } else if (obj instanceof Long) {
                jSONObject2.put(next, jSONObject.getLong(next));
            } else if (obj instanceof Boolean) {
                jSONObject2.put(next, jSONObject.getBoolean(next));
            } else if (obj instanceof String) {
                jSONObject2.put(next, jSONObject.getString(next));
            } else if (obj instanceof Double) {
                jSONObject2.put(next, jSONObject.getDouble(next));
            } else if (obj instanceof JSONArray) {
                jSONObject2.put(next, jSONObject.getJSONArray(next));
            } else if (obj instanceof JSONObject) {
                jSONObject2.put(next, jSONObject.getJSONObject(next));
            } else {
                jSONObject2.put(next, obj);
            }
        }
        return MetaDataBuilder.from(jSONObject2);
    }
}
