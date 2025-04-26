package com.motorola.ccc.ota.ui;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class NotificationService extends Service {
    private static final String ALARM_ACTION = "com.motorola.ccc.ota.ns.alarmtriggerintent";
    private static final String MY_ACTION = "com.motorola.ccc.ota.ns.alarmintent";
    private String channelId;
    private boolean isLowMemoryNotification;
    private String mAction;
    private AlarmManager mAlarmManager;
    private PendingIntent mAlarmPendingIntent;
    private String mMessage;
    private long mNextPrompt;
    private int mNotificationExpiryMins;
    private boolean mNotificationInStatusBar;
    private NotificationManager mNotificationManager;
    private PendingIntent mPendingIntent;
    private MyIntentReceiver mRecv = new MyIntentReceiver();
    private boolean mRespondBackForSystemUpdates;
    private boolean mResponseAsBroadcast;
    private Intent mResponseIntent;
    private String mTitle;
    private String mVersion;
    private float progressPercentage;
    private BotaSettings settings;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.mAlarmManager = (AlarmManager) getSystemService("alarm");
        this.mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(MY_ACTION), 335544320);
        this.mAlarmPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ALARM_ACTION), 335544320);
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        this.channelId = NotificationChannelCreator.getUrgentNotificationChannelId();
        this.settings = new BotaSettings();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpdaterUtils.ACTION_MANUAL_CHECK_UPDATE);
        intentFilter.addAction(MY_ACTION);
        intentFilter.addAction(ALARM_ACTION);
        registerReceiver(this.mRecv, intentFilter, Permissions.INTERACT_OTA_SERVICE, null, 2);
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        Logger.debug("OtaApp", "NotificationService, onDestroy");
        this.mNotificationInStatusBar = false;
        unregisterReceiver(this.mRecv);
        this.settings.removeConfig(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY);
        this.mNotificationManager.cancel(NotificationUtils.OTA_NOTIFICATION_ID);
        this.mAlarmManager.cancel(this.mPendingIntent);
        this.mAlarmManager.cancel(this.mAlarmPendingIntent);
        clearState();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        MetaData from;
        Logger.debug("OtaApp", "Received start id " + i2 + ": " + intent);
        if (this.settings.getBoolean(Configs.FORCE_UPGRADE_TIME_COMPLETED) && (from = MetaDataBuilder.from(this.settings.getString(Configs.METADATA))) != null && !from.showPreInstallScreen()) {
            Logger.info("OtaApp", "NotificationService.onStartCommand,  finish service as forceUpgradeTimer expired");
            stopSelf();
        }
        if (intent != null) {
            String action = intent.getAction();
            this.mAction = action;
            if (!MY_ACTION.equals(action) && !ALARM_ACTION.equals(this.mAction)) {
                Logger.debug("OtaApp", "handle intent : " + this.mAction);
                this.mVersion = intent.getStringExtra(UpgradeUtilConstants.KEY_VERSION);
                handleIntent(intent);
            }
        }
        return super.onStartCommand(intent, i, i2);
    }

    private void handleIntent(Intent intent) {
        this.mTitle = intent.getStringExtra(NotificationUtils.KEY_NOTIFICATION_TITLE);
        this.mMessage = intent.getStringExtra(NotificationUtils.KEY_NOTIFICATION_MESSAGE);
        this.mResponseIntent = (Intent) intent.getParcelableExtra(NotificationUtils.KEY_NOTIFICATION_TARGET_INTENT);
        this.mResponseAsBroadcast = intent.getBooleanExtra(NotificationUtils.KEY_NOTIFICATION_RESPONSE_AS_BROADCAST, false);
        this.mNotificationExpiryMins = intent.getIntExtra(NotificationUtils.KEY_NOTIFICATION_TIMER_EXPIRY, UpdaterUtils.DEFAULT_DEFER_TIME);
        this.mRespondBackForSystemUpdates = intent.getBooleanExtra(NotificationUtils.KEY_RESPOND_FOR_SYSTEM_UPDATES, false);
        this.progressPercentage = intent.getFloatExtra(NotificationUtils.KEY_PROGRESS_PERCENTAGE, -1.0f);
        this.isLowMemoryNotification = intent.getBooleanExtra(NotificationUtils.KEY_IS_NOTIFY_LOW_MEMORY, false);
        long currentTimeMillis = System.currentTimeMillis();
        long j = this.settings.getLong(Configs.NOTIFICATION_NEXT_PROMPT, 0L);
        this.mNextPrompt = j;
        if (this.mNotificationInStatusBar && j > 0 && currentTimeMillis < j) {
            Logger.debug("OtaApp", "supress duplicated intent, nextPompt: " + TimeUnit.MILLISECONDS.toMinutes(this.mNextPrompt - currentTimeMillis) + " in mins and mNotificationInStatusBar: " + this.mNotificationInStatusBar);
            this.mAlarmManager.setExactAndAllowWhileIdle(0, this.mNextPrompt, this.mAlarmPendingIntent);
        } else if (this.settings.getBoolean(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY) && this.mNotificationInStatusBar) {
            Logger.debug("OtaApp", "NotificationService, waiting for device unlock");
        } else {
            long j2 = this.mNextPrompt;
            if (j2 > 0 && currentTimeMillis > j2) {
                Logger.debug("OtaApp", "Device powered back after timer expiry, reposnd back appropriately");
                this.mResponseIntent.putExtra(UpgradeUtilConstants.KEY_FULL_SCREEN_REMINDER, "afterAlarmExpiry");
                if (UpgradeUtilConstants.ACTION_PAUSE_DOWNLOAD_FOR_CELLULAR.equals(this.mAction)) {
                    UpdaterUtils.setProgressScreenDisplayNextPrompt(this.settings);
                }
                if (this.mResponseAsBroadcast) {
                    sendBroadcast(this.mResponseIntent, Permissions.INTERACT_OTA_SERVICE);
                } else {
                    this.mResponseIntent.setFlags(268435456);
                    getApplicationContext().startActivity(this.mResponseIntent);
                }
                stopSelf();
                return;
            }
            displayNotification(this.mTitle, this.mMessage, intent);
            if (this.mNotificationExpiryMins != -1) {
                this.mNextPrompt = System.currentTimeMillis() + (this.mNotificationExpiryMins * 60000);
                Logger.debug("OtaApp", "NotificationService, Notification will expire after " + this.mNotificationExpiryMins + " mins");
                this.mAlarmManager.setExactAndAllowWhileIdle(0, this.mNextPrompt, this.mAlarmPendingIntent);
            } else {
                this.mNextPrompt = -1L;
            }
            saveState();
        }
    }

    private void displayNotification(String str, String str2, Intent intent) {
        String string;
        Intent intent2;
        this.mNotificationInStatusBar = true;
        RemoteViews remoteViews = new RemoteViews(getPackageName(), (int) R.layout.custom_notification_collapse);
        remoteViews.setTextViewText(R.id.txtNotifyTitle, str);
        remoteViews.setTextViewText(R.id.txtNotifyBody, str2);
        RemoteViews remoteViews2 = new RemoteViews(getPackageName(), (int) R.layout.custom_notification_expand);
        remoteViews2.setTextViewText(R.id.txtNotifyTitle, str);
        remoteViews2.setTextViewText(R.id.txtNotifyBody, str2);
        NotificationCompat.Builder category = new NotificationCompat.Builder(this, this.channelId).setSmallIcon((int) R.drawable.ota_icon).setColor(getResources().getColor(getUpdateTypeInterface().getUpdateSpecificColor(), getApplicationContext().getTheme())).setStyle(new NotificationCompat.DecoratedCustomViewStyle()).setCustomContentView(remoteViews).setContentIntent(this.mPendingIntent).setLocalOnly(true).setCategory("alarm");
        Bundle bundle = new Bundle();
        bundle.putString(UpgradeUtilConstants.KEY_NOTIFICATION_TYPE, NotificationUtils.KEY_NOTIFY_TYPE_SERVICE);
        bundle.putBoolean(NotificationUtils.KEY_IS_NOTIFY_LOW_MEMORY, this.isLowMemoryNotification);
        bundle.putString(NotificationUtils.KEY_UPDATE_NOTIFICATION_SERVICE_TYPE, intent.getStringExtra(NotificationUtils.KEY_UPDATE));
        bundle.putParcelable(NotificationUtils.KEY_NOTIFICATION_INTENT, this.mResponseIntent);
        if (BuildPropReader.isStreamingUpdate()) {
            string = getString(R.string.update);
        } else {
            string = getString(R.string.download_now_button);
        }
        if (NotificationUtils.KEY_DOWNLOAD.equals(intent.getStringExtra(NotificationUtils.KEY_UPDATE))) {
            if (UpdaterUtils.isWifiOnly()) {
                intent2 = new Intent(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_RESPONSE);
                intent2.putExtra(UpgradeUtilConstants.KEY_VERSION, BuildPropReader.getDeviceSha1(UpgradeSourceType.upgrade.toString()));
                intent2.putExtra(UpgradeUtilConstants.KEY_RESPONSE_ACTION, true);
                intent2.putExtra(UpgradeUtilConstants.KEY_RESPONSE_FLAVOUR, UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI.name());
                intent2.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_MODE, "userInitiatedDLFromNotification");
            } else {
                intent2 = new Intent(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_AVAILABLE_RESPONSE);
                intent2.putExtra(UpgradeUtilConstants.KEY_METADATA, this.settings.getString(Configs.METADATA));
                intent2.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_REQ_FROM_NOTIFY, true);
            }
            category.addAction((int) R.drawable.ic_ota_download_24px, NotificationUtils.getActionBtnText(string, R.color.colorPrimaryDark), PendingIntent.getBroadcast(getApplicationContext(), 0, intent2, 335544320));
        }
        if (this.progressPercentage > -1.0f) {
            category.setSubText(NumberFormat.getPercentInstance().format(this.progressPercentage / 100.0f));
        }
        if (UpgradeUtilConstants.UPGRADE_RESTART_NOTIFICATION.equals(this.mAction) || UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE.equals(this.mAction)) {
            Intent upgradeLaunchProceed = UpgradeUtilMethods.getUpgradeLaunchProceed(this, this.mVersion, true, false, "userInitiatedRestartFromNotification");
            String string2 = getString(R.string.restart_now);
            bundle.putString(NotificationUtils.KEY_UPDATE_NOTIFICATION_SERVICE_TYPE, NotificationUtils.KEY_RESTART);
            if (UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE.equals(this.mAction)) {
                string2 = getString(R.string.install);
                upgradeLaunchProceed = UpgradeUtilMethods.getUpgradeLaunchProceed(this, this.mVersion, true, true, "userInitiatedInstallFromNotification");
                bundle.putString(NotificationUtils.KEY_UPDATE_NOTIFICATION_SERVICE_TYPE, NotificationUtils.KEY_INSTALL);
            }
            category.addAction((int) R.drawable.ic_ota_reboot_24dp, NotificationUtils.getActionBtnText(string2, R.color.colorPrimaryDark), PendingIntent.getBroadcast(getApplicationContext(), 0, upgradeLaunchProceed, 335544320));
        } else if (UpgradeUtilConstants.ACTION_PAUSE_DOWNLOAD_FOR_CELLULAR.equals(this.mAction)) {
            Intent intent3 = (Intent) intent.getParcelableExtra(NotificationUtils.KEY_NOTIFICATION_BUILDER_ACTION_INTENT);
            intent3.setComponent(new ComponentName(OtaApplication.getGlobalContext(), UpdateReceiver.class));
            PendingIntent broadcast = PendingIntent.getBroadcast(OtaApplication.getGlobalContext(), 0, intent3, 67108864);
            if (!this.settings.getBoolean(Configs.BATTERY_LOW)) {
                category.addAction(17301560, NotificationUtils.getActionBtnText(getResources().getString(R.string.resume_download_on_cellular), R.color.colorPrimaryDark), broadcast);
            }
        }
        category.setExtras(bundle);
        Notification build = category.build();
        build.flags |= 32;
        this.mNotificationManager.notify(NotificationUtils.OTA_NOTIFICATION_ID, build);
    }

    private UpdateType.UpdateTypeInterface getUpdateTypeInterface() {
        UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(this.mResponseIntent);
        if (upgradeInfoDuringOTAUpdate != null) {
            return UpdateType.getUpdateType(upgradeInfoDuringOTAUpdate.getUpdateTypeData());
        }
        return UpdateType.getUpdateType(UpdateType.DIFFUpdateType.DEFAULT.toString());
    }

    private void saveState() {
        this.settings.setLong(Configs.NOTIFICATION_NEXT_PROMPT, this.mNextPrompt);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearState() {
        this.settings.removeConfig(Configs.NOTIFICATION_NEXT_PROMPT);
        this.settings.removeConfig(Configs.ACTIVITY_NEXT_PROMPT);
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    private class MyIntentReceiver extends BroadcastReceiver {
        private MyIntentReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.debug("OtaApp", "NotificationService.handleIntent: " + intent.getAction());
            if (UpdaterUtils.ACTION_MANUAL_CHECK_UPDATE.equals(intent.getAction())) {
                NotificationService.this.settings.setBoolean(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY, false);
                if (!NotificationService.this.mResponseAsBroadcast || NotificationService.this.mRespondBackForSystemUpdates) {
                    NotificationService.this.stopSelf();
                    return;
                }
            }
            if (NotificationService.MY_ACTION.equals(intent.getAction())) {
                NotificationService.this.settings.setBoolean(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY, false);
                NotificationService.this.mResponseIntent.putExtra(UpgradeUtilConstants.KEY_FULL_SCREEN_REMINDER, "userTappedOnNotification");
                NotificationService.this.clearState();
                if (NotificationService.this.mResponseAsBroadcast) {
                    NotificationService.this.settings.setBoolean(Configs.NOTIFICATION_TAPPED, true);
                    NotificationService.this.mResponseIntent.putExtra(UpdaterUtils.KEY_CHECK_FOR_MAP, false);
                    context.sendBroadcast(NotificationService.this.mResponseIntent, Permissions.INTERACT_OTA_SERVICE);
                } else {
                    NotificationService.this.mResponseIntent.setFlags(268435456);
                    context.startActivity(NotificationService.this.mResponseIntent);
                }
                NotificationService.this.stopSelf();
            } else if (NotificationService.ALARM_ACTION.equals(intent.getAction())) {
                if (UpgradeUtilConstants.ACTION_PAUSE_DOWNLOAD_FOR_CELLULAR.equals(NotificationService.this.mAction)) {
                    UpdaterUtils.setProgressScreenDisplayNextPrompt(NotificationService.this.settings);
                }
                if (shouldIBlockFullScreenRemainder(context)) {
                    NotificationService.this.settings.setBoolean(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY, true);
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("android.intent.action.USER_PRESENT");
                    NotificationService notificationService = NotificationService.this;
                    notificationService.registerReceiver(notificationService.mRecv, intentFilter, 2);
                    Logger.info("OtaApp", "ScreenListener registered");
                    return;
                }
                NotificationService.this.clearState();
                NotificationService.this.mResponseIntent.putExtra(UpgradeUtilConstants.KEY_FULL_SCREEN_REMINDER, "afterAlarmExpiry");
                if (NotificationService.this.mResponseAsBroadcast) {
                    NotificationService.this.mResponseIntent.putExtra(UpdaterUtils.KEY_CHECK_FOR_MAP, true);
                    context.sendBroadcast(NotificationService.this.mResponseIntent, Permissions.INTERACT_OTA_SERVICE);
                } else {
                    NotificationService.this.mResponseIntent.setFlags(268435456);
                    context.startActivity(NotificationService.this.mResponseIntent);
                }
                NotificationService.this.stopSelf();
            } else if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                Logger.debug("OtaApp", "NotificationService:user unlocked the device");
                NotificationService.this.settings.setBoolean(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY, false);
                NotificationService.this.mResponseIntent.putExtra(UpgradeUtilConstants.KEY_FULL_SCREEN_REMINDER, "afterScreenUnlock");
                NotificationService.this.clearState();
                if (NotificationService.this.mResponseAsBroadcast) {
                    context.sendBroadcast(NotificationService.this.mResponseIntent, Permissions.INTERACT_OTA_SERVICE);
                } else {
                    NotificationService.this.mResponseIntent.setFlags(268435456);
                    context.startActivity(NotificationService.this.mResponseIntent);
                }
                NotificationService.this.stopSelf();
            }
        }

        private boolean shouldIBlockFullScreenRemainder(Context context) {
            UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(NotificationService.this.mResponseIntent);
            if (UpdaterUtils.isDeviceLocked(context)) {
                return false;
            }
            if (UpgradeUtilConstants.ACTION_PAUSE_DOWNLOAD_FOR_CELLULAR.equals(NotificationService.this.mAction)) {
                return true;
            }
            if (upgradeInfoDuringOTAUpdate == null || upgradeInfoDuringOTAUpdate.isCriticalUpdate() || upgradeInfoDuringOTAUpdate.isForceDownloadTimeSet() || upgradeInfoDuringOTAUpdate.isForceInstallTimeSet()) {
                return false;
            }
            return !NotificationService.this.settings.getBoolean(Configs.CHECKBOX_SELECTED);
        }
    }
}
