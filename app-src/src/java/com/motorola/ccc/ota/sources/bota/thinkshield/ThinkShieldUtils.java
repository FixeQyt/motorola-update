package com.motorola.ccc.ota.sources.bota.thinkshield;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.HashMap;
import java.util.Map;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class ThinkShieldUtils {
    public static boolean isAscDevice(Context context) {
        try {
            Bundle call = context.getContentResolver().call(Uri.parse(ThinkShieldUtilConstants.ASC_BASE_URI_PATH), "IS_ASC_DEVICE", (String) null, (Bundle) null);
            if (call == null) {
                call = new Bundle();
            }
            return call.getBoolean("IS_ASC_DEVICE", false);
        } catch (Exception e) {
            Log.e(Logger.THINK_SHIELD_TAG, "Exception happened while calling isAscDevice provider msg=" + e);
            return false;
        }
    }

    public static String getAscVersion(Context context) {
        try {
            Bundle call = context.getContentResolver().call(Uri.parse(ThinkShieldUtilConstants.ASC_BASE_URI_PATH), ThinkShieldUtilConstants.GET_ASC_VERSION, (String) null, (Bundle) null);
            if (call == null) {
                call = new Bundle();
            }
            return call.getString(ThinkShieldUtilConstants.EXTRA_ASC_VERSION, "");
        } catch (Exception unused) {
            return "";
        }
    }

    public static String getTargetVersion(Context context) {
        try {
            Bundle call = context.getContentResolver().call(Uri.parse(ThinkShieldUtilConstants.ASC_BASE_URI_PATH), ThinkShieldUtilConstants.GET_TARGET_VERSION, (String) null, (Bundle) null);
            if (call == null) {
                call = new Bundle();
            }
            return call.getString(ThinkShieldUtilConstants.EXTRA_TARGET_VERSION, "");
        } catch (Exception unused) {
            return "";
        }
    }

    public static String getCampaignType(Context context) {
        try {
            Bundle call = context.getContentResolver().call(Uri.parse(ThinkShieldUtilConstants.ASC_BASE_URI_PATH), ThinkShieldUtilConstants.GET_CAMPAIGN_TYPE, (String) null, (Bundle) null);
            if (call == null) {
                call = new Bundle();
            }
            return call.getString(ThinkShieldUtilConstants.EXTRA_CAMPAIGN_TYPE, "");
        } catch (Exception unused) {
            return "";
        }
    }

    public static void startASCUpdateRequestSession(Context context, MetaData metaData) {
        BotaSettings botaSettings = new BotaSettings();
        long currentTimeMillis = System.currentTimeMillis();
        long j = botaSettings.getLong(Configs.ASC_SESSION_TIMEOUT_TIMESTAMP, -1L);
        if (j != -1 && currentTimeMillis < j) {
            Logger.debug(Logger.THINK_SHIELD_TAG, "Previous ASC session is in progress, so check update request is not allowed at this time");
            return;
        }
        botaSettings.setLong(Configs.ASC_TRANSACTION_ID, currentTimeMillis);
        String targetSha1 = metaData != null ? metaData.getTargetSha1() : "";
        Intent intent = new Intent(ThinkShieldUtilConstants.UPGRADE_ASC_UPDATE_REQUEST);
        intent.putExtra(ThinkShieldUtilConstants.EXTRA_TRANSACTION_ID, currentTimeMillis);
        intent.putExtra(ThinkShieldUtilConstants.EXTRA_TARGET_VERSION, targetSha1);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_ASC_SERVICE);
        Logger.debug("OtaApp", "startASCUpdateRequestSession: target version=" + targetSha1 + " : transaction id =" + currentTimeMillis);
        startASCTimeoutAlarm(context);
    }

    public static void startASCTimeoutAlarm(Context context) {
        long currentTimeMillis = System.currentTimeMillis() + 60000;
        new BotaSettings().setLong(Configs.ASC_SESSION_TIMEOUT_TIMESTAMP, currentTimeMillis);
        Logger.debug(Logger.THINK_SHIELD_TAG, "started the alarm of asc session; timer = 60 secs");
        Intent intent = new Intent(ThinkShieldUtilConstants.ACTION_ASC_OTA_INTERNAL_TIMEOUT);
        intent.putExtra(ThinkShieldUtilConstants.EXTRA_CHECK_UPDATE_ASC_ERROR, -2);
        intent.putExtra(ThinkShieldUtilConstants.EXTRA_TRANSACTION_ID, new BotaSettings().getLong(Configs.ASC_TRANSACTION_ID, -1L));
        PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent, 335544320);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        alarmManager.cancel(broadcast);
        alarmManager.setExactAndAllowWhileIdle(0, currentTimeMillis, broadcast);
    }

    public static void cancelASCTimeoutAlarm(Context context) {
        Intent intent = new Intent(ThinkShieldUtilConstants.ACTION_ASC_OTA_INTERNAL_TIMEOUT);
        intent.putExtra(ThinkShieldUtilConstants.EXTRA_CHECK_UPDATE_ASC_ERROR, -2);
        intent.putExtra(ThinkShieldUtilConstants.EXTRA_TRANSACTION_ID, new BotaSettings().getLong(Configs.ASC_TRANSACTION_ID, -1L));
        ((AlarmManager) context.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(context, 0, intent, 335544320));
    }

    public static UpgradeUtils.Error ascRequestUpdateResponse(int i) {
        if (i != 0) {
            if (i != 1) {
                if (i != 2) {
                    if (i != 3) {
                        if (i != 4) {
                            if (i == 5) {
                                return UpgradeUtils.Error.ERR_ASC_INVALID_TRANSACTION_ID;
                            }
                            return UpgradeUtils.Error.ERR_ASC_FAIL;
                        }
                        return UpgradeUtils.Error.ERR_ASC_UNKNOWN;
                    }
                    return UpgradeUtils.Error.ERR_ASC_SERVER;
                }
                return UpgradeUtils.Error.ERR_NOT_ASC_DEVICE;
            }
            return UpgradeUtils.Error.ERR_ASC_DENIED;
        }
        return UpgradeUtils.Error.ERR_ASC_ALLOWED;
    }

    public static Bundle getPolicyBundle(Context context) {
        Bundle bundle = new Bundle();
        try {
            bundle = context.getContentResolver().call(Uri.parse(ThinkShieldUtilConstants.ASC_BASE_URI_PATH), ThinkShieldUtilConstants.GET_SYSTEM_UPDATE_POLICY, (String) null, (Bundle) null);
        } catch (Exception unused) {
        }
        return bundle == null ? new Bundle() : bundle;
    }

    public static int getPolicyType(Context context) {
        return getPolicyBundle(context).getInt(ThinkShieldUtilConstants.EXTRA_DEPLOYMENT_TYPE, -1);
    }

    public static int getInstallWindowStartTime(Context context) {
        Bundle policyBundle = getPolicyBundle(context);
        if (policyBundle.getInt(ThinkShieldUtilConstants.EXTRA_DEPLOYMENT_TYPE, -1) == 2) {
            return policyBundle.getInt(ThinkShieldUtilConstants.EXTRA_START_WINDOW, -1);
        }
        return -1;
    }

    public static int getInstallWindowEndTime(Context context) {
        Bundle policyBundle = getPolicyBundle(context);
        if (policyBundle.getInt(ThinkShieldUtilConstants.EXTRA_DEPLOYMENT_TYPE, -1) == 2) {
            return policyBundle.getInt(ThinkShieldUtilConstants.EXTRA_END_WINDOW, -1);
        }
        return -1;
    }

    public static boolean isAutoDownloadOverAnyDataNetworkPolicySet(Context context) {
        try {
            Bundle call = context.getContentResolver().call(Uri.parse(ThinkShieldUtilConstants.ASC_BASE_URI_PATH), "IS_FOTA_AUTO_UPDATE_OVER_ANY_DATA_NETWORK", (String) null, (Bundle) null);
            if (call == null) {
                call = new Bundle();
            }
            return call.getBoolean("IS_FOTA_AUTO_UPDATE_OVER_ANY_DATA_NETWORK", false);
        } catch (Exception unused) {
            return false;
        }
    }

    public static boolean isOtaUpdateDisabledPolicySet(Context context) {
        try {
            Bundle call = context.getContentResolver().call(Uri.parse(ThinkShieldUtilConstants.ASC_BASE_URI_PATH), "IS_OTA_UPDATE_DISABLED", (String) null, (Bundle) null);
            if (call == null) {
                call = new Bundle();
            }
            return call.getBoolean("IS_OTA_UPDATE_DISABLED", false);
        } catch (Exception unused) {
            return false;
        }
    }

    public static Bundle getAscInfoBundle(Context context) {
        Bundle bundle = new Bundle();
        try {
            bundle = context.getContentResolver().call(Uri.parse(ThinkShieldUtilConstants.ASC_BASE_URI_PATH), ThinkShieldUtilConstants.GET_ASC_INFO, (String) null, (Bundle) null);
            return bundle == null ? new Bundle() : bundle;
        } catch (Exception unused) {
            return bundle;
        }
    }

    public static Map<String, Integer> getAscOverridenMetaData(Context context) {
        HashMap hashMap = new HashMap();
        Bundle ascInfoBundle = getAscInfoBundle(context);
        int i = ascInfoBundle.getInt(ThinkShieldUtilConstants.EXTRA_CRITICAL_UPDATE_REMINDER, -1);
        int i2 = ascInfoBundle.getInt(ThinkShieldUtilConstants.EXTRA_CRITICAL_UPDATE_DEFER_COUNT, -1);
        int i3 = ascInfoBundle.getInt(ThinkShieldUtilConstants.EXTRA_SHOW_PRE_DOWNLOAD_DIALOG, -1);
        hashMap.put(ThinkShieldUtilConstants.EXTRA_CRITICAL_UPDATE_REMINDER, Integer.valueOf(i));
        hashMap.put(ThinkShieldUtilConstants.EXTRA_CRITICAL_UPDATE_DEFER_COUNT, Integer.valueOf(i2));
        hashMap.put(ThinkShieldUtilConstants.EXTRA_SHOW_PRE_DOWNLOAD_DIALOG, Integer.valueOf(i3));
        return hashMap;
    }

    public static void onSystemUpdatePolicyChanged(BotaSettings botaSettings, boolean z) {
        Context globalContext = OtaApplication.getGlobalContext();
        if (isOtaUpdateDisabledPolicySet(globalContext)) {
            Logger.debug(Logger.THINK_SHIELD_TAG, "ThinkShieldUtils.onSystemUpdatePolicyChanged, isOtaUpdateDisabledPolicySet");
            if (z) {
                UpgradeUtilMethods.cancelOta("isOtaUpdateDisabled policy is set, abort ongoing update", ErrorCodeMapper.KEY_UPDATE_DISABLED_BY_MOTO_POLICY_MNGR);
                return;
            }
            return;
        }
        if (isAutoDownloadOverAnyDataNetworkPolicySet(globalContext)) {
            Logger.debug(Logger.THINK_SHIELD_TAG, "ThinkShieldUtils.onSystemUpdatePolicyChanged, isAutoDownloadOverAnyDataNetworkPolicySet");
        }
        Logger.debug(Logger.THINK_SHIELD_TAG, "ThinkShieldUtils.onSystemUpdatePolicyChanged");
        new SystemUpdaterPolicy().onSystemUpdatePolicyChanged(botaSettings, z);
    }

    public static Map<String, Object> getASCCampaignStatusDetails(Context context) {
        String str;
        HashMap hashMap = new HashMap();
        String ascVersion = getAscVersion(context);
        Resources resources = context.getResources();
        String string = resources.getString(R.string.check_update_disabled_admin);
        String str2 = "";
        if (!isAscDevice(context)) {
            if (ThinkShieldUtilConstants.ASC_VERSION_V2.equalsIgnoreCase(ascVersion) || ThinkShieldUtilConstants.ASC_VERSION_V3.equalsIgnoreCase(ascVersion)) {
                hashMap.put(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS, false);
                hashMap.put(ThinkShieldUtilConstants.KEY_ASC_STATUS_STRING, string + resources.getString(R.string.asc_campaign_status_prefix) + resources.getString(R.string.asc_campaign_not_assigned));
                return hashMap;
            }
            hashMap.put(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS, false);
            hashMap.put(ThinkShieldUtilConstants.KEY_ASC_STATUS_STRING, "");
            return hashMap;
        } else if (!ThinkShieldUtilConstants.ASC_VERSION_V2.equalsIgnoreCase(ascVersion) && !ThinkShieldUtilConstants.ASC_VERSION_V3.equalsIgnoreCase(ascVersion)) {
            hashMap.put(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS, false);
            hashMap.put(ThinkShieldUtilConstants.KEY_ASC_STATUS_STRING, "");
            return hashMap;
        } else if (!ThinkShieldUtilConstants.ALLOW_LIST.equals(getCampaignType(context)) && !ThinkShieldUtilConstants.BLOCK_LIST.equals(getCampaignType(context))) {
            hashMap.put(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS, true);
            hashMap.put(ThinkShieldUtilConstants.KEY_ASC_STATUS_STRING, string + resources.getString(R.string.asc_campaign_status_prefix) + resources.getString(R.string.asc_campaign_not_assigned));
            return hashMap;
        } else {
            String str3 = string + resources.getString(R.string.asc_campaign_status_prefix) + resources.getString(R.string.asc_campaign_assigned);
            if (ThinkShieldUtilConstants.BLOCK_LIST.equals(getCampaignType(context))) {
                hashMap.put(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS, true);
                hashMap.put(ThinkShieldUtilConstants.KEY_ASC_STATUS_STRING, str3 + SystemUpdateStatusUtils.SUMMARY_SEPERATOR + resources.getString(R.string.asc_campaign_is_blocked));
                return hashMap;
            }
            String str4 = str3 + SystemUpdateStatusUtils.SUMMARY_SEPERATOR + resources.getString(R.string.asc_campaign_is_allowed);
            int policyType = getPolicyType(context);
            String targetVersion = getTargetVersion(context);
            if (TextUtils.isEmpty(targetVersion)) {
                str = resources.getString(R.string.asc_sw_up_to_date);
            } else {
                str = resources.getString(R.string.asc_target_version) + targetVersion;
            }
            String str5 = str4 + str;
            if (policyType == 1 || policyType == 2 || policyType == 3) {
                if (policyType == 2) {
                    str2 = resources.getString(R.string.asc_campaign_window_period_status, extractStandardTimeFromWindowTime(getInstallWindowStartTime(context)), extractStandardTimeFromWindowTime(getInstallWindowEndTime(context)));
                }
                if (policyType == 3) {
                    hashMap.put(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS, Boolean.valueOf(isChkUpdateDisableForASCPostponePolicy()));
                } else {
                    hashMap.put(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS, true);
                }
                hashMap.put(ThinkShieldUtilConstants.KEY_ASC_STATUS_STRING, str5 + str2);
                return hashMap;
            }
            hashMap.put(ThinkShieldUtilConstants.KEY_ASC_STATUS_STRING, str5);
            if (getAscInfoBundle(context).getInt(ThinkShieldUtilConstants.EXTRA_CRITICAL_UPDATE_REMINDER, -1) >= 0) {
                hashMap.put(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS, true);
            } else {
                hashMap.put(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS, false);
            }
            return hashMap;
        }
    }

    private static boolean isChkUpdateDisableForASCPostponePolicy() {
        return System.currentTimeMillis() < new BotaSettings().getLong(Configs.SYSTEM_UPDATE_POLICY_POSTPONE, -1L);
    }

    private static String extractStandardTimeFromWindowTime(int i) {
        String str;
        int i2 = i / 60;
        int i3 = i % 60;
        if (i2 <= 9) {
            str = "0" + i2;
        } else {
            str = "" + i2;
        }
        if (i3 <= 9) {
            return str + ":0" + i3;
        }
        return str + SmartUpdateUtils.MASK_SEPARATOR + i3;
    }
}
