package com.motorola.ccc.ota.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateFormat;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class SystemUpdateStatusUtils {
    public static final String ABAPPLYINGPATCH = "ABApplyingPatch";
    public static final int ABUPDATE_IN_PROGRESS = 253;
    public static final int ALERT_CORRUPTED_FIRMWARE_UPDATE_PACKAGE = 402;
    public static final int ALERT_DOWNLOAD_FAILED_DUE_TO_MEMORY_FULL = 501;
    public static final int ALERT_DOWNLOAD_FAILED_DUE_TO_NETWORK_ISSUE = 503;
    public static final int ALERT_DOWNLOAD_FAILED_DUE_TO_TIMEOUT = 407;
    public static final int ALERT_FAILED_FIRMWARE_UPDATE_PACKAGE_CERT_VALIDATE = 404;
    public static final int ALERT_FIRMWARE_UPDATE_FAILED = 410;
    public static final int ALERT_FIRMWARE_UPDATE_FAILED_DUE_TO_SYSTEM_UPDATE_POLICY_ENABLED = 499;
    public static final int ALERT_INSTALL_FAILED_DUE_TO_MEMORY_FULL = 502;
    public static final int ALERT_MISMATCH_FIRMWARE_UPDATE_PACKAGE = 403;
    public static final int ALERT_SUCCESSFUL = 200;
    public static final int ALERT_UNDEFINED_ERROR_FIRMWARE_UPDATE_PACKAGE = 409;
    public static final int ALERT_UPDATE_CANCELED_BY_SERVER = 504;
    public static final int ALERT_USER_CANCEL = 401;
    public static final String DEFAULT_STRING = "Not available";
    public static final int DOWNLOAD_DEFERRED = 250;
    public static final int DOWNLOAD_FAILED = 450;
    public static final int DOWNLOAD_IN_PROGRESS = 251;
    public static final String FIELD_SEPERATOR = " : ";
    public static final String GETTING_DESCRIPTOR = "GettingDescriptor";
    public static final String GETTING_PACKAGE = "GettingPackage";
    public static final int INSTALL_DEFERRED = 252;
    public static final String KEY_ALREADY_WORKING = "already working on version";
    public static final String KEY_CANCEL_OTA_LOW_BATTERY = "low battery";
    public static final String KEY_DM_CANCEL_OTA = "cancelled by DM";
    public static final String KEY_DOWNLOAD_CANCELLED = "user declined to accept the download";
    public static final String KEY_DOWNLOAD_CANCELLED_VITAL = "User cancelled vital update";
    public static final String KEY_DOWNLOAD_SKIPPED_VITAL = "User skipped vital update";
    public static final String KEY_UPATE_SUCCESS = "woohoo";
    public static final String KEY_UPDATE_CANCELLED = "user declined to accept the upgrade";
    public static final String KEY_UPDATE_FAIL = "Installation aborted";
    private static final long LEARN_MORE_LINK_SHOW_TIME = -1702967296;
    public static final String NEWLINE_SEPERATOR = "\n";
    public static final String NOTIFIED = "Notified";
    public static final String OTA_DATA_PREF = "OtaData";
    public static final String OTA_DOWNLOAD_SUCCESS = "255";
    public static final String OTA_UPDATE_SUCCESS = "200";
    public static final String OTA_VITAL_UPDATE_SUCCESS = "214";
    private static final String PREFS_FILE = "DMUpdatePrefs";
    public static final String QUERYING = "Querying";
    public static final String QUERYINGINSTALL = "QueryingInstall";
    public static final String REQUEST_PERMISSION = "RequestPermission";
    public static final String RESULT = "Result";
    public static final String SMART_UPDATE_DISABLED = "216";
    public static final String SMART_UPDATE_ENABLED = "215";
    public static final String SOFTWARE_UPDATE = "SU";
    public static final String SPACE = " ";
    public static final int STATUS_INDEX = 2;
    public static final String SUCANCEL_AFTER_UPDATE = "552";
    public static final String SUCANCEL_PRIOR_TO_DOWNLOAD = "553";
    public static final String SUCANCEL_PRIOR_TO_UPDATE = "554";
    public static final String SUMMARY_SEPERATOR = " - ";
    public static final int SU_LINK_OR_ERR_INDEX = 4;
    public static final int SU_TIMESTAMP_INDEX = 3;
    private static final String SW_UPDATE_PLANNED_CHECKED_KEY = "sw_update_planned_checked_date";
    private static final String TAG = "SystemUpdateStatusUtils";
    public static final int UPDATE_ALREADY_IN_PROGRESS = 2529;
    public static final int UPDATE_CANCELLED = 401;
    public static final int UPDATE_FAIL = 410;
    public static final int UPDATE_FAIL_SUCANCEL_DM = 552;
    public static final int UPDATE_SUCCESS = 200;
    public static final String UPGRADING = "Upgrading";
    public static final int UP_TO_DATE = 201;
    public static final int VER_INDEX = 1;
    public static final int VITAL_UPDATE_LOW_BATTERY = 403;
    private static final String[][] error_codes = {new String[]{"short read", " Short Read"}, new String[]{"has unexpected contents", " SHA1 Failure"}, new String[]{"script aborted", " Upgrade Failure"}, new String[]{"Package expects build fingerprint of", " Fingerprint mismatch"}, new String[]{"signature verification failed", " Signature verification failure"}, new String[]{"Not enough free space", " No Free Space"}, new String[]{"Can't open", " Package Open Fail"}};

    public static void updateSoftwareStatusInPds(Context context, String str, int i, String str2) {
        Logger.debug("OtaApp", "SystemUpdateStatusUtils, Received Version: " + str + " Code: " + i + " LinkOrError: " + str2);
        BotaSettings botaSettings = new BotaSettings();
        botaSettings.setString(Configs.ONGOING_HISTORY_VERSION_NAME, str);
        botaSettings.setString(Configs.ONGOING_HISTORY_LINK_ERROR_VALUE, str2);
        botaSettings.setInt(Configs.ONGOING_HISTORY_UPDATE_STATUS, i);
        botaSettings.setLong(Configs.ONGOING_HISTORY_TIME_STAMP, System.currentTimeMillis());
        removeSwUpdatePlannedInfo(context);
    }

    private static boolean isSuValid(String[] strArr) {
        if (strArr.length < 4) {
            Logger.debug("OtaApp", "SystemUpdateStatusUtils, Required data is not found hence read next Item");
            return false;
        }
        return true;
    }

    public static List<String> readLastUpdateVersionFromPref(Context context) {
        ArrayList arrayList = new ArrayList();
        BotaSettings botaSettings = new BotaSettings();
        String[] strArr = {SOFTWARE_UPDATE, botaSettings.getString(Configs.ONGOING_HISTORY_VERSION_NAME), String.valueOf(botaSettings.getInt(Configs.ONGOING_HISTORY_UPDATE_STATUS, 0)), String.valueOf(botaSettings.getLong(Configs.ONGOING_HISTORY_TIME_STAMP, 0L)), botaSettings.getString(Configs.ONGOING_HISTORY_LINK_ERROR_VALUE)};
        String parseLastSu = parseLastSu(context, strArr);
        if (parseLastSu != null) {
            arrayList.add(parseLastSu);
            long longValue = Long.valueOf(strArr[3]).longValue();
            boolean hasSuLinkNeeded = hasSuLinkNeeded(strArr);
            long currentTimeMillis = System.currentTimeMillis() - LEARN_MORE_LINK_SHOW_TIME;
            if (longValue != -1 && longValue < currentTimeMillis && hasSuLinkNeeded) {
                arrayList.add(context.getResources().getString(R.string.learn_more_online_link));
            }
            return arrayList;
        }
        return null;
    }

    private static String parseLastSu(Context context, String[] strArr) {
        long longValue;
        String string;
        String str;
        String statusFromCode;
        String str2;
        String str3;
        String str4 = null;
        if (isSuValid(strArr)) {
            String str5 = strArr[1];
            int intValue = Integer.valueOf(strArr[2]).intValue();
            Logger.debug("OtaApp", "SystemUpdateStatusUtils, Version: " + str5 + " Code: " + intValue + " time stamp: " + Long.valueOf(strArr[3]).longValue());
            if (str5 == null) {
                return null;
            }
            Resources resources = context.getResources();
            if (intValue == 200) {
                str = resources.getString(R.string.software_update_to);
                statusFromCode = resources.getString(R.string.applied);
                if (4 < strArr.length) {
                    str2 = strArr[4];
                }
                str2 = null;
            } else if (intValue == 410) {
                str = resources.getString(R.string.software_not_update_to);
                String string2 = resources.getString(R.string.failed);
                if (4 >= strArr.length || TextUtils.isEmpty(strArr[4])) {
                    str2 = null;
                    statusFromCode = string2;
                } else {
                    statusFromCode = string2;
                    str4 = resources.getString(R.string.error_code) + strArr[4];
                    str2 = null;
                }
            } else {
                if (intValue == 251 || intValue == 253) {
                    string = resources.getString(R.string.software_update_in_progress);
                } else {
                    string = resources.getString(R.string.software_not_update_to);
                }
                str = string;
                statusFromCode = getStatusFromCode(intValue, context);
                str2 = null;
            }
            if (str4 != null) {
                str3 = "SU : " + str + SPACE + str5 + FIELD_SEPERATOR + statusFromCode + SPACE + getDate(longValue, context) + NEWLINE_SEPERATOR + str4 + FIELD_SEPERATOR + str2;
            } else if (str4 == null && intValue == 201) {
                str3 = "SU : " + statusFromCode + NEWLINE_SEPERATOR + context.getResources().getString(R.string.update_history_version, String.valueOf(BuildPropReader.getBuildDescription()));
            } else {
                str3 = "SU : " + str + SPACE + str5 + FIELD_SEPERATOR + statusFromCode + SPACE + getDate(longValue, context) + FIELD_SEPERATOR + str2;
            }
            Logger.debug("OtaApp", "SystemUpdateStatusUtils, SU Version: " + str5 + " Status: " + statusFromCode + " Error string: " + str4 + " Link: " + str2);
            return str3;
        }
        return null;
    }

    private static boolean hasSuLinkNeeded(String[] strArr) {
        return Integer.valueOf(strArr[2]).intValue() == 200 && 4 < strArr.length && strArr[4] != null;
    }

    private static String getDate(long j, Context context) {
        return context.getResources().getString(R.string.date_time, (String) DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMM dd, yyyy"), j), (String) DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context) ? "k:mm" : "h:mmaa"), j));
    }

    public static String getSwUpdatePlannedInfo(Context context) {
        long j = context.getSharedPreferences(PREFS_FILE, 0).getLong(SW_UPDATE_PLANNED_CHECKED_KEY, -1L);
        if (j == -1) {
            Logger.debug("OtaApp", "SystemUpdateStatusUtils, getSwUpdatePlannedInfo: There is no SW Update planned.");
            return null;
        }
        String date = getDate(j, context);
        Logger.debug("OtaApp", "SystemUpdateStatusUtils, getSwUpdatePlannedInfo: checkedAt = " + j + ", formattedDateTime = " + date);
        return date;
    }

    public static void setSwUpdatePlannedInfo(Context context) {
        long currentTimeMillis = System.currentTimeMillis();
        Logger.debug("OtaApp", "SystemUpdateStatusUtils, setSwUpdatePlannedInfo: There is a new SW Update planned. Checked at " + currentTimeMillis);
        context.getSharedPreferences(PREFS_FILE, 0).edit().putLong(SW_UPDATE_PLANNED_CHECKED_KEY, currentTimeMillis).apply();
    }

    public static void removeSwUpdatePlannedInfo(Context context) {
        Logger.debug("OtaApp", "SystemUpdateStatusUtils, removeSwUpdatePlannedInfo: There is no SW Update planned. Remove (if any) previous info");
        context.getSharedPreferences(PREFS_FILE, 0).edit().remove(SW_UPDATE_PLANNED_CHECKED_KEY).apply();
    }

    public static void storeOngoingSystemUpdateStatus(Context context, Intent intent) {
        String readSharedPrefKey;
        String readSharedPrefKey2;
        String str;
        String readSharedPrefKey3;
        String action = intent.getAction();
        Logger.debug("OtaApp", "SystemUpdateStatusUtils: storeOngoingSystemUpdateStatus, Action: " + action);
        if (UpgradeUtilConstants.UPGRADE_UPDATE_STATUS.equals(action)) {
            boolean booleanExtra = intent.getBooleanExtra(UpgradeUtilConstants.KEY_UPDATE_STATUS, false);
            int codeFromInfo = getCodeFromInfo(null, booleanExtra ? KEY_UPATE_SUCCESS : KEY_UPDATE_FAIL);
            String readSharedPrefKey4 = readSharedPrefKey(UpgradeUtilConstants.KEY_DISPLAY_VERSION, context);
            if (readSharedPrefKey4.isEmpty()) {
                readSharedPrefKey4 = BuildPropReader.getBuildDescription();
            }
            if (codeFromInfo == 410) {
                readSharedPrefKey3 = getErrorString(intent.getStringExtra(UpgradeUtilConstants.KEY_REASON));
            } else {
                readSharedPrefKey3 = readSharedPrefKey(UpgradeUtilConstants.KEY_RELEASE_NOTES, context);
            }
            Logger.debug("OtaApp", "SystemUpdateStatusUtils: storeOngoingSystemUpdateStatus, Software Version : " + readSharedPrefKey4 + " Update Status: " + booleanExtra + " Error/Link : " + readSharedPrefKey3);
            updateSoftwareStatusInPds(context, readSharedPrefKey4, codeFromInfo, readSharedPrefKey3);
            if (codeFromInfo == 200) {
                Logger.debug("OtaApp", "SystemUpdateStatusUtils: storeOngoingSystemUpdateStatus, Software Upgrade Success. sending DM alert Notification");
                if (new BotaSettings().getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                    DmSendAlertService.sendDmAlertNotification(context, OTA_VITAL_UPDATE_SUCCESS);
                } else {
                    DmSendAlertService.sendDmAlertNotification(context, OTA_UPDATE_SUCCESS);
                }
            }
        } else if (UpgradeUtilConstants.ALREADY_UP_TO_DATE.equals(action)) {
            updateSoftwareStatusInPds(context, BuildPropReader.getBuildDescription(), UP_TO_DATE, null);
        } else if (UpgradeUtilConstants.ACTION_UPGRADE_UPDATE_STATUS.equals(action)) {
            String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_CURRENT_STATE);
            String stringExtra2 = intent.getStringExtra(UpgradeUtilConstants.KEY_UPDATE_INFO);
            if (UpgradeSourceType.modem.name().equals(intent.getStringExtra(UpgradeUtilConstants.KEY_DOWNLOAD_SOURCE))) {
                return;
            }
            if (stringExtra2 == null) {
                stringExtra2 = DEFAULT_STRING;
            }
            if (stringExtra2.contains(KEY_DM_CANCEL_OTA)) {
                String readSharedPrefKey5 = readSharedPrefKey(UpgradeUtilConstants.KEY_CURRENT_STATE, context);
                if (NOTIFIED.equals(readSharedPrefKey5) || GETTING_DESCRIPTOR.equals(readSharedPrefKey5) || GETTING_PACKAGE.equals(readSharedPrefKey5)) {
                    str = SUCANCEL_PRIOR_TO_DOWNLOAD;
                } else if (!QUERYING.equals(readSharedPrefKey5)) {
                    str = SUCANCEL_AFTER_UPDATE;
                } else {
                    str = SUCANCEL_PRIOR_TO_UPDATE;
                }
                Logger.debug("OtaApp", "SystemUpdateStatusUtils: storeOngoingSystemUpdateStatus, SU Cancel Alert sending ... Prev State: " + readSharedPrefKey5 + " Alert Code: " + str);
                DmSendAlertService.sendDmAlertNotification(context, str);
            }
            if (stringExtra2.contains(ErrorCodeMapper.KEY_SRC_VALIDATION_FAILED)) {
                return;
            }
            storeSharedPrefKey(UpgradeUtilConstants.KEY_CURRENT_STATE, stringExtra, context);
            if (QUERYING.equals(stringExtra)) {
                DmSendAlertService.sendDmAlertNotification(context, OTA_DOWNLOAD_SUCCESS);
            }
            int codeFromInfo2 = getCodeFromInfo(stringExtra, stringExtra2);
            if (codeFromInfo2 == 403) {
                DmSendAlertService.sendDmAlertNotification(context, "403");
            }
            if (codeFromInfo2 == 2529) {
                return;
            }
            if (NOTIFIED.equals(stringExtra)) {
                readSharedPrefKey = intent.getStringExtra(UpgradeUtilConstants.KEY_DISPLAY_VERSION);
                storeSharedPrefKey(UpgradeUtilConstants.KEY_DISPLAY_VERSION, readSharedPrefKey, context);
                readSharedPrefKey2 = getUrl(intent.getStringExtra(UpgradeUtilConstants.KEY_RELEASE_NOTES));
                storeSharedPrefKey(UpgradeUtilConstants.KEY_RELEASE_NOTES, readSharedPrefKey2, context);
            } else {
                readSharedPrefKey = readSharedPrefKey(UpgradeUtilConstants.KEY_DISPLAY_VERSION, context);
                readSharedPrefKey2 = readSharedPrefKey(UpgradeUtilConstants.KEY_RELEASE_NOTES, context);
            }
            Logger.debug("OtaApp", "SystemUpdateStatusUtils: storeOngoingSystemUpdateStatus, Software Version : " + readSharedPrefKey + " Update Status: " + codeFromInfo2 + " Release notes: " + readSharedPrefKey2);
            updateSoftwareStatusInPds(context, readSharedPrefKey, codeFromInfo2, readSharedPrefKey2);
        } else if (UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE_RESPONSE.equals(action)) {
            boolean booleanExtra2 = intent.getBooleanExtra(UpgradeUtilConstants.KEY_OTA_UPDATE_PLANNED, false);
            Logger.debug("OtaApp", "SystemUpdateStatusUtils: storeOngoingSystemUpdateStatus, Software Update Planned : " + booleanExtra2);
            if (booleanExtra2) {
                setSwUpdatePlannedInfo(context);
            } else {
                removeSwUpdatePlannedInfo(context);
            }
        }
    }

    private static String readSharedPrefKey(String str, Context context) {
        Logger.debug("OtaApp", "SystemUpdateStatusUtils, readSharedPrefKey, key = " + str);
        return context.getSharedPreferences(OTA_DATA_PREF, 0).getString(str, "");
    }

    private static void storeSharedPrefKey(String str, String str2, Context context) {
        Logger.debug("OtaApp", "SystemUpdateStatusUtils, storeSharedPrefKey, key = " + str + " value = " + str2);
        SharedPreferences.Editor edit = context.getSharedPreferences(OTA_DATA_PREF, 0).edit();
        edit.putString(str, str2);
        edit.apply();
    }

    public static final int getCodeFromInfo(String str, String str2) {
        Logger.debug("OtaApp", " SystemUpdateStatusUtils, state: " + str + "; info: " + str2);
        if (NOTIFIED.equalsIgnoreCase(str) || REQUEST_PERMISSION.equalsIgnoreCase(str)) {
            return DOWNLOAD_DEFERRED;
        }
        if (QUERYING.equalsIgnoreCase(str) || QUERYINGINSTALL.equalsIgnoreCase(str)) {
            return INSTALL_DEFERRED;
        }
        if (GETTING_DESCRIPTOR.equals(str) || GETTING_PACKAGE.equals(str)) {
            return DOWNLOAD_IN_PROGRESS;
        }
        if (ABAPPLYINGPATCH.equals(str) || UPGRADING.equals(str)) {
            return ABUPDATE_IN_PROGRESS;
        }
        if (str2.contains(KEY_UPDATE_CANCELLED) || str2.contains(KEY_DOWNLOAD_CANCELLED) || str2.contains(KEY_DOWNLOAD_CANCELLED_VITAL)) {
            return 401;
        }
        if (str2.contains(KEY_DOWNLOAD_SKIPPED_VITAL)) {
            return str2.contains(KEY_CANCEL_OTA_LOW_BATTERY) ? 403 : 401;
        } else if (str2.contains(KEY_UPATE_SUCCESS)) {
            return 200;
        } else {
            if (str2.contains(KEY_UPDATE_FAIL)) {
                return 410;
            }
            return str2.contains(KEY_DM_CANCEL_OTA) ? UPDATE_FAIL_SUCANCEL_DM : str2.contains(KEY_ALREADY_WORKING) ? UPDATE_ALREADY_IN_PROGRESS : DOWNLOAD_FAILED;
        }
    }

    public static final String getStatusFromCode(int i, Context context) {
        if (i != 200) {
            if (i != 201) {
                if (i != 401) {
                    if (i != 410) {
                        if (i == 552) {
                            return context.getResources().getString(R.string.update_fail_sucancel_dm_status);
                        }
                        switch (i) {
                            case DOWNLOAD_DEFERRED /* 250 */:
                                return context.getResources().getString(R.string.download_deferred_status);
                            case DOWNLOAD_IN_PROGRESS /* 251 */:
                                return context.getResources().getString(R.string.download_in_progress_status);
                            case INSTALL_DEFERRED /* 252 */:
                                return context.getResources().getString(R.string.install_deferred_status);
                            case ABUPDATE_IN_PROGRESS /* 253 */:
                                return context.getResources().getString(R.string.abupdate_in_progress_status);
                            default:
                                return context.getResources().getString(R.string.download_failed_status);
                        }
                    }
                    return context.getResources().getString(R.string.update_fail_status);
                }
                return context.getResources().getString(R.string.update_cancelled_status);
            }
            return context.getResources().getString(R.string.android_latest_sw_text);
        }
        return context.getResources().getString(R.string.update_success_status);
    }

    public static String getErrorString(String str) {
        if (str == null || TextUtils.isEmpty(str)) {
            return "";
        }
        int i = 0;
        while (true) {
            CharSequence[][] charSequenceArr = error_codes;
            if (i >= charSequenceArr.length) {
                return "";
            }
            if (str.contains(charSequenceArr[i][0])) {
                return charSequenceArr[i][1];
            }
            i++;
        }
    }

    public static String getUrl(String str) {
        String[] split;
        if (str == null || TextUtils.isEmpty(str)) {
            return null;
        }
        for (String str2 : str.split(SPACE)) {
            try {
                new URL(str2);
                return str2;
            } catch (MalformedURLException unused) {
            }
        }
        return null;
    }
}
