package com.motorola.ccc.ota.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtilConstants;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtils;
import com.motorola.ccc.ota.ui.MessageActivity;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.metaData.CheckForUpgradeTriggeredBy;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CusUtilMethods {
    private static final String DEVICE_POLICY_MANAGER_CLASS_NAME = "android.app.admin.DevicePolicyManager";

    public static void notifySoftwareUpdate(long j, boolean z) {
        try {
            Class.forName(DEVICE_POLICY_MANAGER_CLASS_NAME).getMethod("notifyPendingSystemUpdate", Long.TYPE, Boolean.TYPE).invoke((DevicePolicyManager) OtaApplication.getGlobalContext().getSystemService("device_policy"), Long.valueOf(j), Boolean.valueOf(z));
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception in CusUtilMethods, notifySoftwareUpdate: " + e);
        }
    }

    public static void settingMaxDeferTimeForFOTAUpgrade(ApplicationEnv.Database.Descriptor descriptor, long j, BotaSettings botaSettings) {
        AlarmManager alarmManager = (AlarmManager) OtaApplication.getGlobalContext().getSystemService("alarm");
        if (BuildPropReader.isATT()) {
            if (botaSettings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L) < 0) {
                botaSettings.setLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, System.currentTimeMillis() + j);
            }
            Logger.debug("OtaApp", "CusSm, Maximum defer time for FOTA set to : " + botaSettings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L) + " milliseconds");
            Intent intent = new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE);
            intent.putExtra(UpdaterUtils.WHOM, UpdaterUtils.ACTION_MAX_FOTA_EXPIRY_TIME);
            PendingIntent broadcast = PendingIntent.getBroadcast(OtaApplication.getGlobalContext(), 0, intent, 335544320);
            alarmManager.cancel(broadcast);
            alarmManager.setExactAndAllowWhileIdle(0, botaSettings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L), broadcast);
        }
    }

    public static void settingMaxDeferTimeForCriticalUpdate(ApplicationEnv.Database.Descriptor descriptor, BotaSettings botaSettings) {
        long criticalDeferCount = (descriptor.getMeta().getCriticalDeferCount() * descriptor.getMeta().getCriticalUpdateReminder() * 60 * 1000) + (descriptor.getMeta().getCriticalUpdateExtraWaitCount() * descriptor.getMeta().getCriticalUpdateExtraWaitPeriod() * 60000);
        botaSettings.setLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, System.currentTimeMillis() + criticalDeferCount);
        botaSettings.setLong(Configs.MAX_CRITICAL_UPDATE_EXTENDED_TIME, -1L);
        Logger.debug("OtaApp", "CusSm, Maximum defer time for critical BOTA upgrade set to : " + TimeUnit.MILLISECONDS.toMinutes(criticalDeferCount) + " minutes");
    }

    public static void setReserveSpaceInMB(ApplicationEnv.Database.Descriptor descriptor, BotaSettings botaSettings) {
        long j = 0;
        if (!BuildPropReader.doesDeviceSupportVABc()) {
            long reserveSpaceInMb = descriptor.getMeta().getReserveSpaceInMb();
            if (reserveSpaceInMb >= 0) {
                j = BuildPropReader.isFotaATT() ? 3072L : reserveSpaceInMb;
                Logger.debug("OtaApp", "Found Metadata-spaceValueMeta: " + j);
            } else {
                long reservedSpaceValue = FileUtils.getReservedSpaceValue();
                if (reservedSpaceValue > 0) {
                    Logger.debug("OtaApp", "handleReserveSpace, No Metadata value from server. Setting etc value: " + reservedSpaceValue);
                    j = reservedSpaceValue;
                } else {
                    Logger.debug("OtaApp", "handleReserveSpace, No ReserveSpace value from server. Assuming 0");
                }
            }
        }
        botaSettings.setLong(Configs.RESERVE_SPACE_IN_MB, j);
    }

    public static boolean isPolicyBundleUpdateEnabled(ApplicationEnv.Database.Descriptor descriptor) {
        return !descriptor.getMeta().getRebootRequired() && descriptor.getMeta().getPolicyBundle();
    }

    public static boolean isFirstNetForInstall(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        List<SubscriptionInfo> activeSubscriptionInfoList = ((SubscriptionManager) context.getSystemService("telephony_subscription_service")).getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null) {
            Logger.debug("OtaApp", "CusUtilMethods:subscriptionInfoList:null:return false");
            return false;
        }
        for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
            TelephonyManager createForSubscriptionId = telephonyManager.createForSubscriptionId(subscriptionInfo.getSubscriptionId());
            String simOperator = createForSubscriptionId.getSimOperator();
            String subscriberId = createForSubscriptionId.getSubscriberId();
            Logger.debug("OtaApp", "simOperator: " + simOperator + " IMSI: " + subscriberId);
            if ("312670".equals(simOperator)) {
                return true;
            }
            if ((subscriberId != null && subscriberId.startsWith("312670")) || "313100".equals(simOperator)) {
                return true;
            }
            if (subscriberId != null && subscriberId.startsWith("313100")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isItFirstNetOnFota(Context context) {
        if (BuildPropReader.isATT() && isFirstNetForInstall(context)) {
            Logger.debug("OtaApp", "CusUtilMethods:isFirstNetSimOnFotaSource:returning = true");
            return true;
        }
        return false;
    }

    public static boolean isLibertySIM(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        List<SubscriptionInfo> activeSubscriptionInfoList = ((SubscriptionManager) context.getSystemService("telephony_subscription_service")).getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null) {
            return false;
        }
        for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
            TelephonyManager createForSubscriptionId = telephonyManager.createForSubscriptionId(subscriptionInfo.getSubscriptionId());
            String simOperator = createForSubscriptionId.getSimOperator();
            String subscriberId = createForSubscriptionId.getSubscriberId();
            if ("313790".equals(simOperator)) {
                return true;
            }
            if (subscriberId != null && subscriberId.startsWith("313790")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCheckUpdateDisabled(Context context) {
        return (BuildPropReader.isATT() && isLibertySIM(context)) || new SystemUpdaterPolicy().isOtaUpdateDisabledByPolicyMngr() || ((Boolean) ThinkShieldUtils.getASCCampaignStatusDetails(context).get(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS)).booleanValue();
    }

    public static boolean isBatteryLow(Context context) {
        return UpdaterUtils.getBatteryLevel(context) < UpdaterUtils.allowedBatteryLevel();
    }

    public static void startForceUpgradeTimer(int i, BotaSettings botaSettings, ApplicationEnv applicationEnv) {
        long j = botaSettings.getLong(Configs.FORCE_UPGRADE_TIME, -1L);
        Logger.info("OtaApp", "BotaUpgradeSource.startForceUpgradeTimer, forceUpgradeTime in settings = " + j + ";forceUpgradeTime in check response = " + i);
        if (j < 0) {
            i = (i <= 0 || i > 864000) ? 43200 : 43200;
            botaSettings.setLong(Configs.FORCE_UPGRADE_TIME, System.currentTimeMillis() + (i * 1000));
            applicationEnv.getUtilities().registerWithForceUpgradeManager(i);
        }
    }

    public static void startForceDownloadTimer(double d, BotaSettings botaSettings, ApplicationEnv applicationEnv) {
        Logger.info("OtaApp", "BotaUpgradeSource.startForceDownloadTimer, with value " + d + " days");
        if (botaSettings.getLong(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME, -1L) < 0) {
            long currentTimeMillis = System.currentTimeMillis();
            long j = (long) (currentTimeMillis + (d * 8.64E7d));
            botaSettings.setLong(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME, j);
            botaSettings.setLong(Configs.ACTIVITY_NEXT_PROMPT, j - 259200000);
            int forceDownloadDelay = UpdaterUtils.getForceDownloadDelay(currentTimeMillis);
            if (forceDownloadDelay == 0) {
                applicationEnv.getUtilities().sendForceUpgradeTimerExpiryIntent();
            } else if (forceDownloadDelay > 0) {
                applicationEnv.getUtilities().registerWithForceUpgradeManager(forceDownloadDelay);
            }
        }
    }

    public static void startRestartExpiryTimer(BotaSettings botaSettings) {
        Logger.info("OtaApp", "BotaUpgradeSource.startRestartExpiryTimer");
        if (botaSettings.getLong(Configs.RESTART_EXPIRY_TIMER, -1L) < 0) {
            botaSettings.setLong(Configs.RESTART_EXPIRY_TIMER, System.currentTimeMillis() + 1296000000);
        }
    }

    public static String getReleaseNotesFromPreInstallNotes(String str) {
        if (str != null) {
            try {
                if (str.equals("") || !str.contains("a href")) {
                    return "";
                }
                return str.split("<a href=")[1].split(">")[0];
            } catch (Exception e) {
                Logger.debug("OtaApp", "FileUtils.getReleaseNotesFromPreInstallNotes:" + e);
                return null;
            }
        }
        return "";
    }

    public static String getUpgradeSource(BotaSettings botaSettings, String str) {
        String string = botaSettings.getString(Configs.STATS_UPGRADE_SOURCE);
        if (UpgradeSourceType.upgrade.toString().equals(str)) {
            if (CheckForUpgradeTriggeredBy.user.toString().equals(string)) {
                return "UPGRADED_VIA_PULL";
            }
            if (CheckForUpgradeTriggeredBy.polling.toString().equals(string)) {
                return "UPGRADED_VIA_SCHEDULED_POLL";
            }
            if (CheckForUpgradeTriggeredBy.setup.toString().equals(string)) {
                return "UPGRADED_VIA_INTIAL_SETUP";
            }
            if (CheckForUpgradeTriggeredBy.notification.toString().equals(string)) {
                return "UPGRADED_VIA_PUSH";
            }
            if ("other".equals(string)) {
                return "UPGRADED_VIA_DAISY_CHAIN";
            }
            return "UPGRADED_VIA_UNKNOWN_METHOD";
        } else if (UpgradeSourceType.sdcard.toString().equals(str)) {
            return "UPGRADED_VIA_SDCARD";
        } else {
            return "UPGRADED_VIA_UNKNOWN_METHOD";
        }
    }

    public static void showPopupToOptCellularDataAtt(Context context) {
        if (BuildPropReader.isATT()) {
            Intent intent = new Intent(context, MessageActivity.class);
            intent.addFlags(268435456);
            intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS, UpgradeUtils.DownloadStatus.FOTA_SHOW_ALERT_CELLULAR_POPUP.toString());
            context.startActivity(intent);
        }
    }
}
