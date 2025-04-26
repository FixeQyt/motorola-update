package com.motorola.ccc.ota.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Html;
import android.text.format.Formatter;
import androidx.core.app.NotificationCompat;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import java.text.NumberFormat;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class DownloadNotification {
    private static boolean sNotificationSuspended;
    private static boolean sNotificationSwiped;
    private Context mContext;
    private NotificationManager mNotificationMgr;
    public BroadcastReceiver mSwipedReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.DownloadNotification.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (UpgradeUtilConstants.ACTION_NOTIFICATION_SWIPED.equals(intent.getAction())) {
                Logger.debug("OtaApp", "DownloadNotification, ACTION_NOTIFICATION_SWIPED");
                DownloadNotification.sNotificationSwiped = true;
                DownloadNotification.sNotificationSuspended = false;
            }
            if (UpgradeUtilConstants.UPGRADE_UPDATER_STATE_CLEAR.equals(intent.getAction())) {
                Logger.debug("OtaApp", "DownloadNotification, UPGRADE_UPDATER_STATE_CLEAR");
                DownloadNotification.sNotificationSwiped = false;
                DownloadNotification.sNotificationSuspended = false;
            }
            NotificationUtils.unRegisterSwipeableNotificationReceiver(DownloadNotification.this.mContext, DownloadNotification.this.mSwipedReceiver);
        }
    };
    private String mUrgentChannelId = NotificationChannelCreator.getUrgentNotificationChannelId();
    private String mMediumChannelId = NotificationChannelCreator.getMediumNotificationChannelId();

    int getProgressDeferred() {
        return R.string.progress_deffered;
    }

    public DownloadNotification(Context context) {
        this.mContext = context;
        this.mNotificationMgr = (NotificationManager) context.getSystemService("notification");
    }

    public String getErrorNotificationText(int i, boolean z, boolean z2, BotaSettings botaSettings) {
        String downloadProgressText;
        Resources resources = this.mContext.getResources();
        Logger.debug("OtaApp", "DownloadNotification.getNotificationText retriedOrSuspend = " + i);
        if (i != 0) {
            if (UpdaterUtils.isDeviceInDatasaverMode()) {
                downloadProgressText = resources.getString(R.string.progress_suspended_datasaver);
            } else if (i == 1) {
                downloadProgressText = resources.getString(R.string.progress_retried);
            } else if (botaSettings.getBoolean(Configs.BATTERY_LOW)) {
                downloadProgressText = resources.getString(R.string.low_battery_download);
            } else if (z2) {
                downloadProgressText = resources.getString(R.string.progress_suspended_wifi);
            } else if (z && !UpdaterUtils.getAutomaticDownloadForCellular()) {
                downloadProgressText = resources.getString(R.string.progress_suspended_wifi);
            } else if (UpdaterUtils.isDataNetworkRoaming(this.mContext)) {
                downloadProgressText = resources.getString(R.string.progress_suspended_roaming);
            } else if (UpdaterUtils.isAdminAPNEnabled()) {
                downloadProgressText = resources.getString(R.string.progress_suspended_no_adminapn);
            } else {
                downloadProgressText = resources.getString(R.string.progress_suspended);
            }
        } else {
            downloadProgressText = UpdateType.getUpdateType(UpdaterUtils.getUpdateType(botaSettings.getString(Configs.METADATA))).getDownloadProgressText();
        }
        Logger.debug("OtaApp", "DownloadNotification.getNotificationText bigTextMessage = " + downloadProgressText);
        return downloadProgressText;
    }

    public void updateNotification(long j, long j2, String str, int i, boolean z, boolean z2, BotaSettings botaSettings) {
        long j3;
        boolean z3;
        int i2;
        int i3;
        NotificationCompat.Builder builder;
        Resources resources;
        Intent intent;
        String str2;
        String str3;
        String str4;
        Resources resources2;
        Intent intent2;
        Intent intent3;
        String string = botaSettings.getString(Configs.METADATA);
        if (MetaDataBuilder.from(string) == null) {
            Logger.debug("OtaApp", "empty metadata, returning");
            return;
        }
        UpdateType.UpdateTypeInterface updateType = UpdateType.getUpdateType(UpdaterUtils.getUpdateType(string));
        String str5 = "<b>" + updateType.getDownloadNotificationTitle() + "<b>";
        String downloadProgressText = updateType.getDownloadProgressText();
        String systemUpdatePausedNotificationTitle = updateType.getSystemUpdatePausedNotificationTitle();
        if (SmartUpdateUtils.isDownloadForcedForSmartUpdate(botaSettings)) {
            if (-1 != i && !sNotificationSuspended) {
                cancel();
                return;
            }
            sNotificationSuspended = true;
        }
        if (sNotificationSwiped && NotificationUtils.showSwipeAbleNotification(i)) {
            return;
        }
        sNotificationSwiped = false;
        NotificationHandler notificationHandler = new NotificationHandler(this.mContext);
        Resources resources3 = this.mContext.getResources();
        Intent pendingIntentForDlProgressScreen = UpdaterUtils.getPendingIntentForDlProgressScreen(this.mContext, i, j, j2, str, z, string);
        if (!botaSettings.getBoolean(Configs.BATTERY_LOW) && z && !UpdaterUtils.isWifiConnected(this.mContext) && j2 <= 0 && UpdaterUtils.downloadFileSize() <= 0) {
            if (botaSettings.getBoolean(Configs.WIFI_SETTINGS_DEFFERED)) {
                Logger.debug("OtaApp", "DownloadNotification.updateNotification(): defer sending wifi settings notification");
                return;
            } else {
                notificationHandler.sendWiFiNotification();
                return;
            }
        }
        botaSettings.setBoolean(Configs.WIFI_SETTINGS_DEFFERED, false);
        if (i != 0) {
            z3 = z2;
            if (z3 && !UpdaterUtils.getAutomaticDownloadForCellular() && UpdaterUtils.checkForFinalDeferTimeForForceUpdate()) {
                if (botaSettings.getBoolean(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY)) {
                    return;
                }
                NotificationUtils.startNotificationService(this.mContext, NotificationUtils.fillSystemUpdatePausedNotificationDetails(this.mContext, systemUpdatePausedNotificationTitle, getErrorNotificationText(i, true, z, botaSettings) + System.lineSeparator() + resources3.getString(R.string.automatic_download_on_cellular_warning, DateFormatUtils.getCalendarString(this.mContext, UpdaterUtils.getMaxForceDownloadDeferTime())), string, UpdaterUtils.getProgressScreenDisplayNextPromptInMins(botaSettings), pendingIntentForDlProgressScreen, (int) ((100 * j2) / j), new Intent(UpdaterUtils.ACTION_USER_RESUME_DOWNLOAD_ON_CELLULAR)));
                return;
            }
            j3 = j;
        } else {
            j3 = j;
            z3 = z2;
        }
        NotificationUtils.stopNotificationService(this.mContext);
        NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this.mContext, this.mMediumChannelId);
        builder2.setContentTitle(Html.fromHtml(str5, 0));
        int i4 = (int) (j3 / 10);
        int i5 = (int) (j2 / 10);
        builder2.setProgress(i4, i5, false);
        builder2.setColor(resources3.getColor(updateType.getUpdateSpecificColor(), this.mContext.getTheme()));
        builder2.setWhen(0L);
        builder2.setLocalOnly(true);
        builder2.setCategory("alarm");
        if (i != 0) {
            botaSettings.setInt(Configs.KEY_RETRY_SUSPEND_STATUS, i);
            builder2.setSmallIcon((int) R.drawable.ota_icon);
            builder2.setContentTitle(Html.fromHtml("<b>" + systemUpdatePausedNotificationTitle + "<b>", 0));
            if (UpdaterUtils.isDeviceInDatasaverMode()) {
                pendingIntentForDlProgressScreen.setClassName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity");
                pendingIntentForDlProgressScreen.setFlags(536870912);
            } else if (botaSettings.getBoolean(Configs.BATTERY_LOW)) {
                if (BuildPropReader.isFotaATT() && botaSettings.getBoolean(Configs.BATTERY_LOW)) {
                    if (j2 > 0) {
                        return;
                    }
                    intent3 = new Intent();
                    pendingIntentForDlProgressScreen = intent3;
                }
            } else if (z) {
                intent3 = new Intent();
                intent3.setAction("android.settings.WIFI_SETTINGS");
                intent3.setFlags(536870912);
                if (BuildPropReader.isBotaATT() && !UpdaterUtils.isWifiOnlyPkg() && !UpdaterUtils.getAutomaticDownloadForCellular()) {
                    addCellularButton(builder2, resources3);
                }
                pendingIntentForDlProgressScreen = intent3;
            } else if ((z3 || SmartUpdateUtils.isDownloadForcedForSmartUpdate(botaSettings)) && !UpdaterUtils.getAutomaticDownloadForCellular()) {
                addCellularButton(builder2, resources3);
            }
            builder2.setStyle(new NotificationCompat.BigTextStyle().bigText(getErrorNotificationText(i, z3, z, botaSettings)));
            str4 = str;
            builder = builder2;
            resources2 = resources3;
            Intent intent4 = pendingIntentForDlProgressScreen;
            str2 = "sdcard";
            intent2 = intent4;
        } else {
            long[] storeReceivedBytes = storeReceivedBytes(j2, botaSettings);
            if (storeReceivedBytes != null) {
                intent = pendingIntentForDlProgressScreen;
                str2 = "sdcard";
                i2 = i4;
                i3 = i5;
                builder = builder2;
                resources = resources3;
                str3 = UpdaterUtils.getEstimatedTime(j, j2, storeReceivedBytes, resources3);
            } else {
                i2 = i4;
                i3 = i5;
                builder = builder2;
                resources = resources3;
                intent = pendingIntentForDlProgressScreen;
                str2 = "sdcard";
                str3 = null;
            }
            builder.setSmallIcon(17301633);
            if (j3 == j2) {
                resources2 = resources;
                builder.setContentText(resources2.getString(R.string.progress_verify));
                builder.setProgress(i2, i3, true);
                botaSettings.removeConfig(Configs.KEY_RECEIVED_BYTES);
                botaSettings.removeConfig(Configs.KEY_TIME_RECEIVED_BYTES);
                botaSettings.removeConfig(Configs.KEY_RETRY_SUSPEND_STATUS);
                str4 = str;
            } else {
                str4 = str;
                resources2 = resources;
                if (str2.equals(str4)) {
                    builder.setContentText(resources2.getString(R.string.progress_copying_sd));
                } else if (str3 == null) {
                    builder.setContentText(downloadProgressText);
                } else {
                    builder.setStyle(new NotificationCompat.BigTextStyle().bigText(resources2.getString(R.string.download_progress_message, str3, Formatter.formatFileSize(this.mContext, j2), Formatter.formatFileSize(this.mContext, j3))));
                }
            }
            intent2 = intent;
        }
        if (UpdaterUtils.showCancelOption() && !str2.equals(str4)) {
            Intent intent5 = new Intent(UpdaterUtils.ACTION_USER_CANCELLED_DOWNLOAD);
            intent5.setComponent(new ComponentName(OtaApplication.getGlobalContext(), UpdateReceiver.class));
            builder.addAction(17301560, NotificationUtils.getActionBtnText(resources2.getString(R.string.cancel_download_notification), R.color.colorPrimaryDark), PendingIntent.getBroadcast(this.mContext, 0, intent5, 67108864));
        }
        builder.setSubText(getDownloadingText(j, j2));
        builder.setContentIntent(PendingIntent.getActivity(this.mContext, 0, intent2, 201326592));
        if (NotificationUtils.showSwipeAbleNotification(i)) {
            NotificationUtils.registerSwipeableNotificationReceiver(this.mContext, this.mSwipedReceiver);
            builder.setDeleteIntent(PendingIntent.getBroadcast(this.mContext, 0, new Intent(UpgradeUtilConstants.ACTION_NOTIFICATION_SWIPED), 67108864));
        }
        Bundle bundle = new Bundle();
        bundle.putLong(UpgradeUtilConstants.KEY_BYTES_TOTAL, j3);
        bundle.putLong(UpgradeUtilConstants.KEY_DL_PERCENTAGE, j2);
        bundle.putString(UpgradeUtilConstants.KEY_LOCATION_TYPE, str4);
        NotificationCompat.Builder builder3 = builder;
        bundle.putInt(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED, i);
        bundle.putBoolean(UpgradeUtilConstants.KEY_DOWNLOAD_WIFIONLY, z);
        bundle.putBoolean(UpgradeUtilConstants.KEY_DOWNLOAD_ON_WIFI, z2);
        bundle.putString(UpgradeUtilConstants.KEY_NOTIFICATION_TYPE, NotificationUtils.KEY_NOTIFY_TYPE_DL_PROGRESS);
        builder3.setExtras(bundle);
        Notification build = builder3.build();
        if (!NotificationUtils.showSwipeAbleNotification(i)) {
            build.flags |= 2;
        }
        this.mNotificationMgr.notify(NotificationUtils.OTA_NOTIFICATION_ID, build);
    }

    public void cancel() {
        this.mNotificationMgr.cancel(NotificationUtils.OTA_NOTIFICATION_ID);
    }

    private String getDownloadingText(long j, long j2) {
        if (j <= 0) {
            return "";
        }
        return NumberFormat.getPercentInstance().format(((float) j2) / ((float) j));
    }

    private long[] storeReceivedBytes(long j, BotaSettings botaSettings) {
        if (botaSettings.getInt(Configs.KEY_RETRY_SUSPEND_STATUS, 0) != 0) {
            botaSettings.setLong(Configs.KEY_RECEIVED_BYTES, j);
            botaSettings.setLong(Configs.KEY_TIME_RECEIVED_BYTES, System.currentTimeMillis());
            botaSettings.setInt(Configs.KEY_RETRY_SUSPEND_STATUS, 0);
            return null;
        } else if (botaSettings.getLong(Configs.KEY_RECEIVED_BYTES, -1L) <= 0) {
            botaSettings.setLong(Configs.KEY_RECEIVED_BYTES, j);
            botaSettings.setLong(Configs.KEY_TIME_RECEIVED_BYTES, System.currentTimeMillis());
            return null;
        } else {
            long j2 = botaSettings.getLong(Configs.KEY_RECEIVED_BYTES, -1L);
            long j3 = botaSettings.getLong(Configs.KEY_TIME_RECEIVED_BYTES, -1L);
            if (j < j2) {
                botaSettings.removeConfig(Configs.KEY_RECEIVED_BYTES);
                botaSettings.removeConfig(Configs.KEY_TIME_RECEIVED_BYTES);
                return null;
            }
            botaSettings.setLong(Configs.KEY_RECEIVED_BYTES, j);
            botaSettings.setLong(Configs.KEY_TIME_RECEIVED_BYTES, System.currentTimeMillis());
            return new long[]{j2, j3};
        }
    }

    private void addCellularButton(NotificationCompat.Builder builder, Resources resources) {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (UpdaterUtils.isWifiConnected(this.mContext)) {
            return;
        }
        Intent intent = new Intent(UpdaterUtils.ACTION_USER_RESUME_DOWNLOAD_ON_CELLULAR);
        intent.setComponent(new ComponentName(OtaApplication.getGlobalContext(), UpdateReceiver.class));
        builder.addAction(17301560, NotificationUtils.getActionBtnText(resources.getString(R.string.resume_download_on_cellular), R.color.colorPrimaryDark), PendingIntent.getBroadcast(this.mContext, 0, intent, 67108864));
        if (BuildPropReader.isBotaATT()) {
            Intent intent2 = new Intent("android.settings.WIFI_SETTINGS");
            intent2.setFlags(536870912);
            builder.addAction(17301540, NotificationUtils.getActionBtnText(resources.getString(R.string.connect_to_wifi), R.color.colorPrimaryDark), PendingIntent.getActivity(this.mContext, 0, intent2, 335544320));
        }
    }
}
