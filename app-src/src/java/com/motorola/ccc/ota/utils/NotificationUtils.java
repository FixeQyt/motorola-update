package com.motorola.ccc.ota.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.BaseActivity;
import com.motorola.ccc.ota.ui.CriticalUpdate;
import com.motorola.ccc.ota.ui.DownloadNotification;
import com.motorola.ccc.ota.ui.InstallNotification;
import com.motorola.ccc.ota.ui.NotificationChannelCreator;
import com.motorola.ccc.ota.ui.NotificationHandler;
import com.motorola.ccc.ota.ui.NotificationService;
import com.motorola.ccc.ota.ui.UpdateReceiver;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class NotificationUtils {
    public static final String KEY_DOWNLOAD = "Download";
    public static final String KEY_INSTALL = "Install";
    public static final String KEY_IS_NOTIFY_LOW_MEMORY = "NotificationLowMemory";
    public static final String KEY_MERGE_RESTART = "MergeRestart";
    public static final String KEY_NOTIFICATION_BUILDER_ACTION_INTENT = "BuilderActionIntent";
    public static final String KEY_NOTIFICATION_INTENT = "NotificationIntent";
    public static final String KEY_NOTIFICATION_MESSAGE = "Message";
    public static final String KEY_NOTIFICATION_RESPONSE_AS_BROADCAST = "IntentCallBack";
    public static final String KEY_NOTIFICATION_TARGET_INTENT = "PendingIntent";
    public static final String KEY_NOTIFICATION_TIMER_EXPIRY = "NotificationTimerExpiry";
    public static final String KEY_NOTIFICATION_TITLE = "Title";
    public static final String KEY_NOTIFY_DOWNLOAD_STATUS = "NotificationDownloadStatus";
    public static final String KEY_NOTIFY_MERGE_RESTART = "MergeRestartNotification";
    public static final String KEY_NOTIFY_TYPE_BG_INSTALL = "BGInstall";
    public static final String KEY_NOTIFY_TYPE_DL_PROGRESS = "DownloadProgress";
    public static final String KEY_NOTIFY_TYPE_SERVICE = "NotificationService";
    public static final String KEY_NOTIFY_TYPE_STATUS_VERIFY = "StatusVerify";
    public static final String KEY_NOTIFY_WIFI_NOTIFICATION = "WifiNotification";
    public static final String KEY_PROGRESS_PERCENTAGE = "KeyProgressPercentage";
    public static final String KEY_RESPOND_FOR_SYSTEM_UPDATES = "RespondForSystemUpdates";
    public static final String KEY_RESTART = "Restart";
    public static final String KEY_UPDATE = "UpdateKey";
    public static final String KEY_UPDATE_NOTIFICATION_SERVICE_TYPE = "UpdateNotificationServiceType";
    public static final int OTA_NOTIFICATION_ID = 1729;
    public static final int SMART_UPDATE_NOTIFICATION = 5;
    public static final int UPDATE_STATUS_NOTIFICATION = 4;
    private static final BotaSettings settings = new BotaSettings();

    public static boolean isProgressNotification(int i, int i2) {
        return (i == 6 || -1 == i2) ? false : true;
    }

    public static boolean isNotificationServiceRunning(Context context) {
        List<ActivityManager.RunningServiceInfo> runningServices = ((ActivityManager) context.getSystemService("activity")).getRunningServices(PMUtils.APP_IS_EASY);
        if (runningServices == null || runningServices.size() <= 0) {
            return false;
        }
        for (int i = 0; i < runningServices.size(); i++) {
            if (runningServices.get(i).service.getClassName().equals(NotificationService.class.getName())) {
                return true;
            }
        }
        return false;
    }

    public static void startNotificationService(Context context, Intent intent) {
        context.startService(intent);
    }

    public static void stopNotificationService(Context context) {
        context.stopService(new Intent(context, NotificationService.class));
    }

    public static void cancelOtaNotification() {
        Logger.debug("OtaApp", "cancelOtaNotification");
        Context globalContext = OtaApplication.getGlobalContext();
        ((NotificationManager) globalContext.getSystemService("notification")).cancel(OTA_NOTIFICATION_ID);
        stopNotificationService(globalContext);
    }

    public static void cancelSmartUpdateNotification() {
        Logger.debug("OtaApp", "cancelSmartUpdateNotification");
        ((NotificationManager) OtaApplication.getGlobalContext().getSystemService("notification")).cancel(5);
    }

    public static void clearNextPromptDetails(BotaSettings botaSettings) {
        botaSettings.removeConfig(Configs.ACTIVITY_NEXT_PROMPT);
        botaSettings.removeConfig(Configs.NOTIFICATION_NEXT_PROMPT);
    }

    public static void displayNotification(Context context, String str, long j, Intent intent, UpdaterUtils.UpgradeInfo upgradeInfo) {
        Intent fillInstallSystemUpdateNotificationDetails;
        String string = settings.getString(Configs.METADATA);
        String updateType = UpdaterUtils.getUpdateType(string);
        String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_VERSION);
        UpdateType.UpdateTypeInterface updateType2 = UpdateType.getUpdateType(updateType);
        int minutes = ((int) TimeUnit.MILLISECONDS.toMinutes(j - System.currentTimeMillis())) + 1;
        if (KEY_DOWNLOAD.equals(str)) {
            fillInstallSystemUpdateNotificationDetails = fillSystemUpdateNotificationDetails(context, updateType2.getPDLNotificationTitle(), updateType2.getSystemUpdateAvailableNotificationText(), string, minutes, intent);
        } else if (KEY_RESTART.equals(str)) {
            fillInstallSystemUpdateNotificationDetails = fillRestartSystemNotificationDetails(context, stringExtra, minutes, intent, upgradeInfo);
        } else {
            fillInstallSystemUpdateNotificationDetails = fillInstallSystemUpdateNotificationDetails(context, updateType2.getInstallationTitle(), getInstallNotificationText(updateType2, upgradeInfo), minutes, intent, stringExtra);
        }
        startNotificationService(context, fillInstallSystemUpdateNotificationDetails);
    }

    public static int getPreDownloadNotificationExpiryMins() {
        String string = settings.getString(Configs.METADATA);
        if (string == null) {
            return UpdaterUtils.DEFAULT_DEFER_TIME;
        }
        try {
            return new JSONObject(string).optInt("preDownloadNotificationExpiryMins", UpdaterUtils.DEFAULT_DEFER_TIME);
        } catch (Exception unused) {
            return UpdaterUtils.DEFAULT_DEFER_TIME;
        }
    }

    public static int getPreInstallNotificationExpiryMins(UpdaterUtils.UpgradeInfo upgradeInfo) {
        return getPreInstallNotificationExpiryMins(upgradeInfo, System.currentTimeMillis());
    }

    public static int getPreInstallNotificationExpiryMins(UpdaterUtils.UpgradeInfo upgradeInfo, long j) {
        String string = settings.getString(Configs.METADATA);
        if (string == null) {
            return UpdaterUtils.DEFAULT_DEFER_TIME;
        }
        try {
            if (!UpdaterUtils.isMandatoryInstallTimeExpired()) {
                if (UpdaterUtils.isBitMapSet(upgradeInfo.getBitmap(), UpdaterUtils.BitmapFeatures.intelligentNotification.ordinal())) {
                    return UpdaterUtils.getTimeRemainingForBestTime(j);
                }
                return new JSONObject(string).optInt("preInstallNotificationExpiryMins", UpdaterUtils.DEFAULT_DEFER_TIME);
            }
            JSONObject jSONObject = new JSONObject(string);
            Logger.debug("OtaApp", "getPreInstallNotificationExpiryMins : Force Annoy");
            return UpdaterUtils.getInstallReminderMins(jSONObject.optString("installReminder", ""));
        } catch (Exception e) {
            Logger.error("OtaApp", "UpdaterUtils.getPreInstallNotificationExpiryMins Exception " + e);
            return UpdaterUtils.DEFAULT_DEFER_TIME;
        }
    }

    public static String getInstallNotificationText(UpdateType.UpdateTypeInterface updateTypeInterface, UpdaterUtils.UpgradeInfo upgradeInfo) {
        Context globalContext = OtaApplication.getGlobalContext();
        int updateTime = UpdaterUtils.getUpdateTime(upgradeInfo.getInstallTime());
        CriticalUpdate criticalUpdate = new CriticalUpdate(upgradeInfo);
        BotaSettings botaSettings = settings;
        boolean z = botaSettings.getBoolean(Configs.CHECKBOX_SELECTED);
        long j = botaSettings.getLong(Configs.AUTO_UPDATE_TIME_SELECTED, -1L);
        if (z && j - System.currentTimeMillis() <= UpgradeUtilConstants.ONE_HOUR) {
            return globalContext.getString(R.string.auto_install_notification_text, DateFormatUtils.getCalendarString(globalContext, j));
        }
        if (upgradeInfo.isCriticalUpdate()) {
            String string = globalContext.getString(R.string.critical_install_notification, DateFormatUtils.getCalendarString(globalContext, botaSettings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L)), Integer.valueOf(UpdaterUtils.getUpdateTime(upgradeInfo.getInstallTime())));
            if (UpdaterUtils.isCriticalUpdateTimerExpired(upgradeInfo)) {
                if (!criticalUpdate.isOutsideCriticalUpdateExtendedTime()) {
                    return globalContext.getString(R.string.critical_install_notification, DateFormatUtils.getCalendarString(globalContext, criticalUpdate.getExtendRestartTime()), Integer.valueOf(UpdaterUtils.getUpdateTime(upgradeInfo.getInstallTime())));
                }
                return globalContext.getString(R.string.important_update_install_overdue_message);
            }
            return string;
        } else if ((upgradeInfo.isForceInstallTimeSet() && botaSettings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L) - System.currentTimeMillis() <= 259200000) || upgradeInfo.isForceInstallTimerExpired()) {
            return globalContext.getString(R.string.force_install_notification_message, DateFormatUtils.getCalendarDate(botaSettings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L)), Integer.valueOf(updateTime));
        } else {
            if (UpdaterUtils.isMandatoryInstallTimeExpired()) {
                return globalContext.getString(R.string.mandatory_install_notification);
            }
            return updateTypeInterface.getInstallUpdateNotificationText();
        }
    }

    public static String getRestartNotificationText(Context context, UpdaterUtils.UpgradeInfo upgradeInfo) {
        CriticalUpdate criticalUpdate = new CriticalUpdate(upgradeInfo);
        if (upgradeInfo.isCriticalUpdate() && !CusUtilMethods.isItFirstNetOnFota(context)) {
            String string = context.getString(R.string.critical_restart_notification, DateFormatUtils.getCalendarString(context, settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L)));
            if (UpdaterUtils.isCriticalUpdateTimerExpired(upgradeInfo)) {
                if (!criticalUpdate.isOutsideCriticalUpdateExtendedTime()) {
                    return context.getString(R.string.critical_restart_notification, DateFormatUtils.getCalendarString(context, criticalUpdate.getExtendRestartTime()));
                }
                return context.getString(R.string.important_update_restart_overdue_message);
            }
            return string;
        } else if (UpdaterUtils.isThreeDaysBeforeForceInstall() || UpdaterUtils.isForceInstallDeferTimeExpired()) {
            return context.getString(R.string.force_restart_notification_message, DateFormatUtils.getCalendarDate(settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L)));
        } else {
            if (UpdaterUtils.isMandatoryInstallTimeExpired()) {
                return context.getString(R.string.mandatory_restart_notification);
            }
            long j = settings.getLong(Configs.RESTART_EXPIRY_TIMER, -1L);
            long currentTimeMillis = System.currentTimeMillis();
            int i = (j <= 0 || j >= currentTimeMillis) ? 0 : (int) (((currentTimeMillis - j) / 86400000) + 15);
            if (i > 15) {
                return context.getString(R.string.restart_notification_text, Integer.valueOf(i));
            }
            return context.getString(R.string.restart_message);
        }
    }

    public static void displayUpdateStatusNotification(Context context, Intent intent) {
        String string;
        String string2;
        Logger.debug("OtaApp", "displayUpdateStatusNotification");
        Resources resources = context.getResources();
        UpdaterUtils.UpgradeInfo upgradeInfoAfterOTAUpdate = UpdaterUtils.getUpgradeInfoAfterOTAUpdate(intent);
        if (upgradeInfoAfterOTAUpdate == null) {
            return;
        }
        intent.setClass(context, BaseActivity.class);
        Boolean valueOf = Boolean.valueOf(intent.getBooleanExtra(UpgradeUtilConstants.KEY_UPDATE_STATUS, false));
        if (valueOf.booleanValue()) {
            string = resources.getString(R.string.update_title);
            if (UpdateType.DIFFUpdateType.OS.toString().equalsIgnoreCase(upgradeInfoAfterOTAUpdate.getUpdateTypeData())) {
                string2 = resources.getString(R.string.update_success_desc_os, BuildPropReader.getAndroidVersion());
            } else {
                string2 = resources.getString(R.string.update_success_desc, upgradeInfoAfterOTAUpdate.getDisplayVersion());
            }
            UpdaterUtils.updateMotorolaSettingsProvider(context, UpdaterUtils.OTA_UPDATE_COMPLETED, String.valueOf(valueOf));
            intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.UPDATE_COMPLETE_FRAGMENT.toString());
        } else if (settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            intent.setClass(context, BaseActivity.class);
            intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.UPDATE_FAILED_FRAGMENT.toString());
            context.startActivity(intent);
            return;
        } else {
            string = resources.getString(R.string.update_fail_title);
            if (UpdaterUtils.isMaxRetryCountReachedForVerizon(intent) && !BuildPropertyUtils.isProductWaveAtleastRefWave("2024.2")) {
                string2 = resources.getString(R.string.verizon_failure_notification_text);
            } else if (intent.getBooleanExtra(UpdaterUtils.KEY_MERGE_FAILURE, false)) {
                string = resources.getString(R.string.merge_update_fail_notify_title);
                string2 = resources.getString(R.string.merge_update_fail_notify_text);
            } else {
                string2 = resources.getString(R.string.update_fail_desc);
            }
            intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.UPDATE_FAILED_FRAGMENT.toString());
        }
        PendingIntent activity = PendingIntent.getActivity(context, 0, intent, 335544320);
        NotificationCompat.Builder buildNotification = new NotificationHandler(context).buildNotification(string, string2, activity, intent);
        buildNotification.setCategory("alarm");
        if (valueOf.booleanValue()) {
            String string3 = context.getString(R.string.update_title_text);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), (int) R.layout.custom_notification_collapse);
            remoteViews.setTextViewText(R.id.txtNotifyTitle, string);
            remoteViews.setTextViewText(R.id.txtNotifyBody, string3);
            RemoteViews remoteViews2 = new RemoteViews(context.getPackageName(), (int) R.layout.custom_notification_expand);
            remoteViews2.setTextViewText(R.id.txtNotifyTitle, string);
            remoteViews2.setTextViewText(R.id.txtNotifyBody, string3);
            buildNotification.setStyle(new NotificationCompat.DecoratedCustomViewStyle()).setCustomContentView(remoteViews).setCustomBigContentView(remoteViews2);
        }
        buildNotification.addAction((int) R.drawable.ic_ota_done_24px, getActionBtnText(context.getString(R.string.details), R.color.colorPrimaryDark), activity);
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_NOTIFICATION_INTENT, intent);
        buildNotification.setExtras(bundle);
        BotaSettings botaSettings = settings;
        boolean z = botaSettings.getBoolean(Configs.SMART_UPDATE_POP_UP_DISABLE);
        boolean equals = UpdateType.DIFFUpdateType.SMR.toString().equals(upgradeInfoAfterOTAUpdate.getUpdateTypeData());
        Notification build = buildNotification.build();
        if (valueOf.booleanValue() && !SmartUpdateUtils.isSmartUpdateEnabledByUser(botaSettings) && !z && ((equals || SmartUpdateUtils.isForcedMRUpdateEnabledByServer()) && SmartUpdateUtils.isSmartUpdateEnabledByServer())) {
            build.flags |= 32;
        } else {
            build.flags |= 16;
        }
        try {
            ((NotificationManager) context.getSystemService("notification")).notify(4, build);
        } catch (SecurityException e) {
            Logger.error("OtaApp", "Security Exception while displaying Update status screen: " + e);
        }
    }

    public static void displaySmartUpdateNotification(Context context) {
        String string;
        String string2;
        Logger.debug("OtaApp", "displaySmartUpdateNotification");
        Intent intent = new Intent();
        Resources resources = context.getResources();
        intent.setClass(context, BaseActivity.class);
        if (SmartUpdateUtils.isSmartUpdateEnabledByUser(settings)) {
            string = resources.getString(R.string.su_title_on);
            string2 = resources.getString(R.string.su_text_on);
        } else {
            string = resources.getString(R.string.su_title_off);
            string2 = resources.getString(R.string.su_text_off);
        }
        intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.SMART_UPDATE_FRAGMENT.toString());
        intent.putExtra(UpgradeUtilConstants.NOTIFICATION_ID, 5);
        PendingIntent activity = PendingIntent.getActivity(context, 5, intent, 335544320);
        NotificationCompat.Builder buildNotification = new NotificationHandler(context).buildNotification(string, string2, activity, intent);
        buildNotification.setCategory("alarm");
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), (int) R.layout.custom_notification_collapse);
        remoteViews.setTextViewText(R.id.txtNotifyTitle, string);
        remoteViews.setTextViewText(R.id.txtNotifyBody, string2);
        buildNotification.setStyle(new NotificationCompat.DecoratedCustomViewStyle()).setCustomContentView(remoteViews);
        buildNotification.addAction((int) R.drawable.ic_ota_done_24px, getActionBtnText(context.getString(R.string.su_btn), R.color.colorPrimaryDark), activity);
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_NOTIFICATION_INTENT, intent);
        buildNotification.setExtras(bundle);
        Notification build = buildNotification.build();
        build.flags |= 16;
        ((NotificationManager) context.getSystemService("notification")).notify(5, build);
    }

    public static int getInstallLaterNotificationExpiryMins(UpdaterUtils.UpgradeInfo upgradeInfo, long j) {
        long currentTimeMillis = System.currentTimeMillis() + j;
        if (upgradeInfo.isCriticalUpdate()) {
            int minutes = ((int) TimeUnit.MILLISECONDS.toMinutes(new CriticalUpdate(upgradeInfo).getAndSetNextPromptValue(currentTimeMillis) - currentTimeMillis)) + 1;
            if (minutes <= 0) {
                return 1;
            }
            return minutes;
        }
        return UpdaterUtils.getMinimumDelayValue(upgradeInfo, getPreInstallNotificationExpiryMins(upgradeInfo, currentTimeMillis), j);
    }

    public static Intent fillLowDataStorageNotificationDetails(Context context, boolean z, String str) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra(KEY_NOTIFICATION_TITLE, context.getText(z ? R.string.system_install_title : R.string.system_update_low_storage_title));
        intent.putExtra(KEY_NOTIFICATION_MESSAGE, context.getText(R.string.system_update_low_storage_text));
        intent.putExtra(KEY_NOTIFICATION_RESPONSE_AS_BROADCAST, true);
        intent.putExtra(KEY_RESPOND_FOR_SYSTEM_UPDATES, true);
        intent.putExtra(KEY_NOTIFICATION_TIMER_EXPIRY, -1);
        intent.putExtra(KEY_IS_NOTIFY_LOW_MEMORY, true);
        Intent intent2 = new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE);
        intent2.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, str);
        intent.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent2);
        return intent;
    }

    public static Intent fillSystemUpdatePausedNotificationDetails(Context context, String str, String str2, String str3, int i, Intent intent, float f, Intent intent2) {
        Intent intent3 = new Intent(context, NotificationService.class);
        intent3.setAction(UpgradeUtilConstants.ACTION_PAUSE_DOWNLOAD_FOR_CELLULAR);
        intent3.putExtra(KEY_NOTIFICATION_TITLE, str);
        intent3.putExtra(KEY_NOTIFICATION_MESSAGE, str2);
        intent3.putExtra(KEY_NOTIFICATION_RESPONSE_AS_BROADCAST, false);
        intent3.putExtra(KEY_NOTIFICATION_TIMER_EXPIRY, i);
        intent3.putExtra(KEY_PROGRESS_PERCENTAGE, f);
        intent3.putExtra(KEY_NOTIFICATION_BUILDER_ACTION_INTENT, intent2);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str3);
        intent3.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent);
        return intent3;
    }

    public static Intent fillSystemUpdateNotificationDetails(Context context, String str, String str2, String str3) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra(KEY_NOTIFICATION_TITLE, str);
        intent.putExtra(KEY_UPDATE, KEY_DOWNLOAD);
        intent.putExtra(KEY_NOTIFICATION_MESSAGE, str2);
        intent.putExtra(KEY_NOTIFICATION_RESPONSE_AS_BROADCAST, true);
        intent.putExtra(KEY_NOTIFICATION_TIMER_EXPIRY, getPreDownloadNotificationExpiryMins());
        Intent intent2 = new Intent(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_AVAILABLE_RESPONSE);
        intent2.putExtra(UpgradeUtilConstants.KEY_METADATA, str3);
        intent.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent2);
        return intent;
    }

    public static Intent fillSystemUpdateNotificationDetails(Context context, String str, String str2, String str3, int i, Intent intent) {
        Intent fillSystemUpdateNotificationDetails = fillSystemUpdateNotificationDetails(context, str, str2, str3);
        fillSystemUpdateNotificationDetails.putExtra(KEY_NOTIFICATION_TIMER_EXPIRY, i);
        fillSystemUpdateNotificationDetails.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent);
        return fillSystemUpdateNotificationDetails;
    }

    public static Intent fillRestartSystemNotificationDetails(Context context, String str, int i, UpdaterUtils.UpgradeInfo upgradeInfo) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(UpgradeUtilConstants.UPGRADE_RESTART_NOTIFICATION);
        intent.putExtra(KEY_NOTIFICATION_TITLE, context.getString(R.string.restart_title));
        intent.putExtra(KEY_NOTIFICATION_MESSAGE, getRestartNotificationText(context, upgradeInfo));
        intent.putExtra(KEY_NOTIFICATION_RESPONSE_AS_BROADCAST, true);
        intent.putExtra(KEY_NOTIFICATION_TIMER_EXPIRY, i);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str);
        Intent intent2 = new Intent(UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE_RESPONSE);
        intent2.putExtra(KEY_UPDATE, KEY_RESTART);
        intent.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent2);
        return intent;
    }

    public static Intent fillRestartSystemNotificationDetails(Context context, String str, int i, String str2, UpdaterUtils.UpgradeInfo upgradeInfo) {
        Intent fillRestartSystemNotificationDetails = fillRestartSystemNotificationDetails(context, str, i, upgradeInfo);
        Intent intent = (Intent) fillRestartSystemNotificationDetails.getParcelableExtra(KEY_NOTIFICATION_TARGET_INTENT);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str2);
        fillRestartSystemNotificationDetails.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent);
        return fillRestartSystemNotificationDetails;
    }

    public static Intent fillRestartSystemNotificationDetails(Context context, String str, int i, Intent intent, UpdaterUtils.UpgradeInfo upgradeInfo) {
        Intent fillRestartSystemNotificationDetails = fillRestartSystemNotificationDetails(context, str, i, upgradeInfo);
        fillRestartSystemNotificationDetails.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent);
        return fillRestartSystemNotificationDetails;
    }

    public static void displayMergeRestartNotification() {
        Logger.debug("OtaApp", "displayMergeRestartNotification");
        Context globalContext = OtaApplication.getGlobalContext();
        Resources resources = globalContext.getResources();
        String string = resources.getString(R.string.merge_process_fail_notify_title);
        String string2 = resources.getString(R.string.merge_process_fail_notify_text);
        Intent intent = new Intent(globalContext, BaseActivity.class);
        intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.MERGE_RESTART_FRAGMENT.toString());
        intent.addFlags(268435456);
        NotificationCompat.Builder buildNotification = new NotificationHandler(globalContext).buildNotification(string, string2, PendingIntent.getActivity(globalContext, 0, intent, 335544320), intent);
        buildNotification.setCategory("alarm");
        PendingIntent broadcast = PendingIntent.getBroadcast(globalContext, 0, new Intent(UpgradeUtilConstants.MERGE_RESTART_UPGRADE), 201326592);
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_NOTIFICATION_INTENT, intent);
        bundle.putString(UpgradeUtilConstants.KEY_NOTIFICATION_TYPE, KEY_NOTIFY_MERGE_RESTART);
        buildNotification.setExtras(bundle);
        buildNotification.addAction((int) R.drawable.ic_ota_reboot_24dp, globalContext.getString(R.string.restart_notification), broadcast);
        Notification build = buildNotification.build();
        build.flags |= 32;
        ((NotificationManager) globalContext.getSystemService("notification")).notify(OTA_NOTIFICATION_ID, build);
    }

    public static Intent fillInstallSystemUpdateNotificationDetails(Context context, String str, String str2, int i, String str3) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE);
        intent.putExtra(UpgradeUtilConstants.KEY_VERSION, str3);
        intent.putExtra(KEY_NOTIFICATION_TITLE, str);
        intent.putExtra(KEY_NOTIFICATION_MESSAGE, str2);
        intent.putExtra(KEY_NOTIFICATION_RESPONSE_AS_BROADCAST, true);
        intent.putExtra(KEY_NOTIFICATION_TIMER_EXPIRY, i);
        Intent intent2 = new Intent(UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE_RESPONSE);
        intent2.putExtra(KEY_UPDATE, KEY_INSTALL);
        intent.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent2);
        return intent;
    }

    public static Intent fillInstallSystemUpdateNotificationDetails(Context context, String str, String str2, int i, String str3, String str4) {
        Intent fillInstallSystemUpdateNotificationDetails = fillInstallSystemUpdateNotificationDetails(context, str, str2, i, str3);
        Intent intent = (Intent) fillInstallSystemUpdateNotificationDetails.getParcelableExtra(KEY_NOTIFICATION_TARGET_INTENT);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str4);
        fillInstallSystemUpdateNotificationDetails.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent);
        return fillInstallSystemUpdateNotificationDetails;
    }

    public static Intent fillInstallSystemUpdateNotificationDetails(Context context, String str, String str2, int i, Intent intent, String str3) {
        Intent fillInstallSystemUpdateNotificationDetails = fillInstallSystemUpdateNotificationDetails(context, str, str2, i, str3);
        fillInstallSystemUpdateNotificationDetails.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent);
        return fillInstallSystemUpdateNotificationDetails;
    }

    public static Intent fillDownloadLaterNotificationDetails(Context context, String str, String str2, Intent intent) {
        Intent intent2 = new Intent(context, NotificationService.class);
        intent2.putExtra(KEY_NOTIFICATION_TITLE, str);
        intent2.putExtra(KEY_NOTIFICATION_MESSAGE, str2);
        intent2.putExtra(KEY_UPDATE, KEY_DOWNLOAD);
        intent2.putExtra(KEY_NOTIFICATION_RESPONSE_AS_BROADCAST, true);
        intent2.putExtra(KEY_NOTIFICATION_TIMER_EXPIRY, getPreDownloadNotificationExpiryMins());
        intent2.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent);
        return intent2;
    }

    public static Intent fillInstallLaterNotificationDetails(Context context, String str, String str2, Intent intent, int i, String str3) {
        Intent intent2 = new Intent(context, NotificationService.class);
        intent2.setAction(UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE);
        intent2.putExtra(KEY_NOTIFICATION_TITLE, str);
        intent2.putExtra(KEY_NOTIFICATION_MESSAGE, str2);
        intent2.putExtra(UpgradeUtilConstants.KEY_VERSION, str3);
        intent2.putExtra(KEY_NOTIFICATION_RESPONSE_AS_BROADCAST, true);
        intent2.putExtra(KEY_NOTIFICATION_TIMER_EXPIRY, i);
        intent2.putExtra(KEY_NOTIFICATION_TARGET_INTENT, intent);
        return intent2;
    }

    public static String getErrorNotificationText(int i, boolean z, BotaSettings botaSettings, Context context) {
        String string;
        Resources resources = context.getResources();
        Logger.debug("OtaApp", "InstallNotification.getNotificationText retriedOrSuspend = " + i);
        Boolean valueOf = Boolean.valueOf(UpdaterUtils.isWifiOnly());
        if (i == 1) {
            string = resources.getString(R.string.bg_retried);
        } else if (botaSettings.getBoolean(Configs.BATTERY_LOW)) {
            string = UpdaterUtils.getBatteryLowMessage(context);
        } else if (valueOf.booleanValue()) {
            string = resources.getString(R.string.bg_suspended_wifi);
        } else if (z && !UpdaterUtils.getAutomaticDownloadForCellular()) {
            string = resources.getString(R.string.bg_suspended_wifi);
        } else if (SmartUpdateUtils.isDownloadForcedForSmartUpdate(botaSettings) && !UpdaterUtils.getAutomaticDownloadForCellular()) {
            string = resources.getString(R.string.bg_suspended_wifi);
        } else if (UpdaterUtils.isDataNetworkRoaming(context)) {
            string = resources.getString(R.string.bg_suspended_roaming);
        } else if (UpdaterUtils.isDeviceInDatasaverMode()) {
            string = resources.getString(R.string.progress_suspended_datasaver);
        } else if (UpdaterUtils.isAdminAPNEnabled()) {
            string = resources.getString(R.string.bg_suspended_non_adminapn);
        } else if (settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            string = resources.getString(R.string.bg_suspended_wifi);
        } else {
            string = resources.getString(R.string.bg_suspended);
        }
        Logger.debug("OtaApp", "InstallNotification.getNotificationText = " + string);
        return string;
    }

    public static void showLowMemoryNotification(String str) {
        Intent intent = new Intent("android.settings.INTERNAL_STORAGE_SETTINGS");
        Context globalContext = OtaApplication.getGlobalContext();
        PendingIntent activity = PendingIntent.getActivity(globalContext, 0, intent, 67108864);
        UpdateType.UpdateTypeInterface updateType = UpdateType.getUpdateType(UpdaterUtils.getUpdateType(new BotaSettings().getString(Configs.METADATA)));
        Resources resources = globalContext.getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(globalContext, NotificationChannelCreator.getUrgentNotificationChannelId());
        builder.setContentTitle(Html.fromHtml("<b>" + resources.getString(R.string.system_update_low_storage_title) + "<b>", 0)).setColor(resources.getColor(updateType.getUpdateSpecificColor(), globalContext.getTheme())).setSmallIcon((int) R.drawable.ota_icon).setStyle(new NotificationCompat.BigTextStyle().bigText(str)).setContentIntent(activity).setCategory("alarm").setLocalOnly(true);
        Notification build = builder.build();
        build.flags |= 32;
        ((NotificationManager) globalContext.getSystemService("notification")).notify(OTA_NOTIFICATION_ID, build);
    }

    public static boolean isUpdateNotificationChannelEnabled() {
        return ((NotificationManager) OtaApplication.getGlobalContext().getSystemService("notification")).getNotificationChannel(NotificationChannelCreator.getUrgentNotificationChannelId()).getImportance() != 0;
    }

    public static boolean isProgressNotificationChannelEnabled() {
        return ((NotificationManager) OtaApplication.getGlobalContext().getSystemService("notification")).getNotificationChannel(NotificationChannelCreator.getMediumNotificationChannelId()).getImportance() != 0;
    }

    public static void registerSwipeableNotificationReceiver(Context context, BroadcastReceiver broadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.ACTION_NOTIFICATION_SWIPED);
        context.getApplicationContext().registerReceiver(broadcastReceiver, intentFilter, 2);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATER_STATE_CLEAR);
        BroadcastUtils.registerLocalReceiver(context, broadcastReceiver, intentFilter2);
    }

    public static void unRegisterSwipeableNotificationReceiver(Context context, BroadcastReceiver broadcastReceiver) {
        try {
            context.unregisterReceiver(broadcastReceiver);
            BroadcastUtils.unregisterLocalReceiver(context, broadcastReceiver);
        } catch (Exception e) {
            Logger.debug("OtaApp", "Caught exception " + e);
        }
    }

    public static boolean showSwipeAbleNotification(int i) {
        if (i != -1) {
            return isSwipeAbleBitmapSet() || SmartUpdateUtils.isDownloadForcedForSmartUpdate(settings);
        }
        return false;
    }

    public static boolean showSwipeAbleNotification(int i, int i2) {
        if (isProgressNotification(i, i2)) {
            return isSwipeAbleBitmapSet() || SmartUpdateUtils.isDownloadForcedForSmartUpdate(settings);
        }
        return false;
    }

    public static boolean isSwipeAbleBitmapSet() {
        return UpdaterUtils.isBitMapSet(MetaDataBuilder.from(settings.getString(Configs.METADATA)).getBitmap(), UpdaterUtils.BitmapFeatures.swipableNotification.ordinal());
    }

    public static void showVerifyNotification(Context context, UpgradeUtils.DownloadStatus downloadStatus) {
        String string;
        UpdateType.UpdateTypeInterface updateType = UpdateType.getUpdateType(UpdaterUtils.getUpdateType(settings.getString(Configs.METADATA)));
        String str = "<b>" + updateType.getDownloadNotificationTitle() + "<b>";
        switch (AnonymousClass1.$SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[downloadStatus.ordinal()]) {
            case 1:
                if (CusAndroidUtils.isDeviceInDatasaverMode()) {
                    string = context.getResources().getString(R.string.progress_suspended_datasaver);
                    break;
                } else if (UpdaterUtils.isDataNetworkRoaming(context)) {
                    string = context.getResources().getString(R.string.progress_suspended_roaming);
                    break;
                } else if (UpdaterUtils.isAdminAPNEnabled()) {
                    string = context.getResources().getString(R.string.progress_suspended_no_adminapn);
                    break;
                } else {
                    string = context.getResources().getString(R.string.progress_suspended);
                    break;
                }
            case 2:
                string = context.getResources().getString(R.string.bg_retried);
                break;
            case 3:
                string = context.getResources().getString(R.string.progress_verify);
                break;
            case 4:
                string = context.getResources().getString(R.string.compatibility_verification_success);
                break;
            case 5:
                string = context.getResources().getString(R.string.progress_verify_allocate_space);
                break;
            case 6:
                string = context.getResources().getString(R.string.allocate_space_success);
                break;
            case 7:
                return;
            default:
                string = context.getResources().getString(R.string.progress_verify);
                break;
        }
        Resources resources = context.getResources();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationChannelCreator.getMediumNotificationChannelId());
        builder.setContentTitle(Html.fromHtml(str, 0));
        builder.setColor(resources.getColor(updateType.getUpdateSpecificColor(), context.getTheme()));
        builder.setWhen(0L);
        builder.setSmallIcon(17301633);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(string));
        builder.setLocalOnly(true);
        builder.setCategory("alarm");
        if (UpdaterUtils.showCancelOption()) {
            Intent intent = new Intent(UpdaterUtils.ACTION_USER_CANCELLED_BACKGROUND_INSTALL);
            intent.setComponent(new ComponentName(OtaApplication.getGlobalContext(), UpdateReceiver.class));
            builder.addAction(17301560, getActionBtnText(resources.getString(R.string.cancel_update_notification), R.color.colorPrimaryDark), PendingIntent.getBroadcast(context, 0, intent, 67108864));
        }
        Intent intent2 = new Intent(context, BaseActivity.class);
        intent2.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.BACKGROUND_INSTALLATION_FRAGMENT.toString());
        intent2.setFlags(805306368);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent2, 201326592));
        Bundle bundle = new Bundle();
        bundle.putString(UpgradeUtilConstants.KEY_NOTIFICATION_TYPE, KEY_NOTIFY_TYPE_STATUS_VERIFY);
        bundle.putString(KEY_NOTIFY_DOWNLOAD_STATUS, downloadStatus.toString());
        builder.setExtras(bundle);
        Notification build = builder.build();
        build.flags |= 2;
        notificationManager.notify(OTA_NOTIFICATION_ID, build);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.utils.NotificationUtils$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus;

        static {
            int[] iArr = new int[UpgradeUtils.DownloadStatus.values().length];
            $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus = iArr;
            try {
                iArr[UpgradeUtils.DownloadStatus.STATUS_DEFERRED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_RETRIED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_TEMP_OK.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_OK.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_ALLOCATE_SPACE.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_ALLOCATE_SPACE_SUCESS.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_VAB_MAKE_SPACE_REQUEST_USER.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
        }
    }

    public static void showOtaLowMemoryNotification(Context context, Intent intent) {
        startNotificationService(context, fillLowDataStorageNotificationDetails(context, UpdaterUtils.isDeviceAtInstallPhase(intent), intent.getStringExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE)));
    }

    public static boolean hasNotification(Context context, int i) {
        for (StatusBarNotification statusBarNotification : ((NotificationManager) context.getSystemService("notification")).getActiveNotifications()) {
            if (statusBarNotification.getId() == i) {
                return true;
            }
        }
        return false;
    }

    public static Spannable getActionBtnText(String str, int i) {
        SpannableString spannableString = new SpannableString(str);
        spannableString.setSpan(new ForegroundColorSpan(i), 0, spannableString.length(), 0);
        return spannableString;
    }

    public static void refreshOtaNotifications(Context context) {
        StatusBarNotification[] activeNotifications;
        for (StatusBarNotification statusBarNotification : ((NotificationManager) context.getSystemService("notification")).getActiveNotifications()) {
            Bundle bundle = statusBarNotification.getNotification().extras;
            if (bundle == null) {
                return;
            }
            if (statusBarNotification.getId() == 4) {
                Logger.debug("OtaApp", "refresh Ota UpdateStatus Notification");
                displayUpdateStatusNotification(context, (Intent) bundle.getParcelable(KEY_NOTIFICATION_INTENT));
            } else if (statusBarNotification.getId() == 1729) {
                String string = bundle.getString(UpgradeUtilConstants.KEY_NOTIFICATION_TYPE);
                Logger.debug("OtaApp", "refreshOtaNotifications: notifyType=" + string);
                if (KEY_NOTIFY_TYPE_SERVICE.equals(string)) {
                    Intent intent = (Intent) bundle.getParcelable(KEY_NOTIFICATION_INTENT);
                    if (bundle.getBoolean(KEY_IS_NOTIFY_LOW_MEMORY)) {
                        showOtaLowMemoryNotification(context, intent);
                        return;
                    }
                    String string2 = bundle.getString(KEY_UPDATE_NOTIFICATION_SERVICE_TYPE);
                    long j = settings.getLong(Configs.NOTIFICATION_NEXT_PROMPT, 0L);
                    UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent);
                    stopNotificationService(context);
                    displayNotification(context, string2, j, intent, upgradeInfoDuringOTAUpdate);
                    return;
                } else if (KEY_NOTIFY_TYPE_BG_INSTALL.equals(string)) {
                    new InstallNotification(context).updateNotification(bundle.getFloat(UpgradeUtilConstants.KEY_PERCENTAGE), bundle.getInt(UpgradeUtilConstants.KEY_UPGRADE_STATUS), bundle.getInt(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED), bundle.getString(UpgradeUtilConstants.KEY_INSTALLER), bundle.getBoolean(UpgradeUtilConstants.KEY_DOWNLOAD_ON_WIFI), settings);
                    return;
                } else if (KEY_NOTIFY_TYPE_STATUS_VERIFY.equals(string)) {
                    showVerifyNotification(context, UpgradeUtils.DownloadStatus.valueOf(bundle.getString(KEY_NOTIFY_DOWNLOAD_STATUS)));
                    return;
                } else if (KEY_NOTIFY_TYPE_DL_PROGRESS.equals(string)) {
                    new DownloadNotification(context).updateNotification(bundle.getLong(UpgradeUtilConstants.KEY_BYTES_TOTAL), bundle.getLong(UpgradeUtilConstants.KEY_DL_PERCENTAGE), bundle.getString(UpgradeUtilConstants.KEY_LOCATION_TYPE), bundle.getInt(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED), bundle.getBoolean(UpgradeUtilConstants.KEY_DOWNLOAD_WIFIONLY), bundle.getBoolean(UpgradeUtilConstants.KEY_DOWNLOAD_ON_WIFI), settings);
                    return;
                } else if (KEY_NOTIFY_WIFI_NOTIFICATION.equals(string)) {
                    new NotificationHandler(context).sendWiFiNotification();
                    return;
                } else if (KEY_NOTIFY_MERGE_RESTART.equals(string)) {
                    displayMergeRestartNotification();
                    return;
                }
            } else {
                continue;
            }
        }
    }
}
