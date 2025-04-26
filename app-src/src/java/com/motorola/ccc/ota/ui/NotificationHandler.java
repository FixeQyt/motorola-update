package com.motorola.ccc.ota.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import androidx.core.app.NotificationCompat;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class NotificationHandler {
    private BotaSettings botaSettings;
    private String channelId = NotificationChannelCreator.getUrgentNotificationChannelId();
    private Context mContext;
    private NotificationManager mNotificationMgr;
    private DownloadNotification mNotifier;
    Resources mResource;
    private UpdateType.UpdateTypeInterface mUpdateTypeInterface;
    private String text;
    private String title;

    public NotificationHandler(Context context) {
        this.mContext = context;
        this.mResource = context.getResources();
        this.mNotificationMgr = (NotificationManager) this.mContext.getSystemService("notification");
        BotaSettings botaSettings = new BotaSettings();
        this.botaSettings = botaSettings;
        this.mUpdateTypeInterface = UpdateType.getUpdateType(UpdaterUtils.getUpdateType(botaSettings.getString(Configs.METADATA)));
    }

    public void sendWiFiNotification() {
        PendingIntent broadcast;
        int i;
        int i2;
        Intent intent;
        Intent intent2;
        this.title = "<b>" + this.mUpdateTypeInterface.getDownloadNotificationTitle() + "<b>";
        this.text = this.mResource.getString(R.string.enable_wifi_error_txt);
        Intent intent3 = new Intent();
        intent3.setAction("android.settings.WIFI_SETTINGS");
        intent3.setFlags(536870912);
        PendingIntent activity = PendingIntent.getActivity(this.mContext, 0, intent3, 335544320);
        if (BuildPropReader.isBotaATT() && !UpdaterUtils.isWifiOnlyPkg()) {
            if (BuildPropReader.isStreamingUpdate()) {
                intent2 = new Intent(UpdaterUtils.ACTION_USER_RESUME_STREAMING_DOWNLOAD_ON_CELLULAR);
            } else {
                intent2 = new Intent(UpdaterUtils.ACTION_USER_RESUME_DOWNLOAD_ON_CELLULAR);
            }
            intent2.setComponent(new ComponentName(OtaApplication.getGlobalContext(), UpdateReceiver.class));
            broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent2, 67108864);
            i2 = R.string.resume_download_on_cellular;
            i = 17301540;
        } else if (UpdaterUtils.showCancelOption()) {
            if (BuildPropReader.isStreamingUpdate()) {
                intent = new Intent(UpdaterUtils.ACTION_USER_CANCELLED_BACKGROUND_INSTALL);
            } else {
                intent = new Intent(UpdaterUtils.ACTION_USER_CANCELLED_DOWNLOAD);
            }
            intent.setComponent(new ComponentName(OtaApplication.getGlobalContext(), UpdateReceiver.class));
            broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 67108864);
            i = R.drawable.ic_ota_cancel_24px;
            i2 = R.string.cancel;
        } else {
            Intent intent4 = new Intent(UpdaterUtils.ACTION_USER_DEFERERD_WIFI_SETUP);
            intent4.setComponent(new ComponentName(OtaApplication.getGlobalContext(), UpdateReceiver.class));
            broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent4, 67108864);
            i = R.drawable.ic_ota_later_24px;
            i2 = R.string.later_button;
        }
        String string = this.mResource.getString(R.string.wifi_setting);
        if (BuildPropReader.isBotaATT()) {
            string = this.mResource.getString(R.string.connect_to_wifi);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.mContext, this.channelId);
        builder.setWhen(System.currentTimeMillis()).setSmallIcon((int) R.drawable.ota_icon).setColor(this.mResource.getColor(this.mUpdateTypeInterface.getUpdateSpecificColor(), this.mContext.getTheme())).setContentTitle(Html.fromHtml(this.title, 0)).setContentText(this.text).setStyle(new NotificationCompat.BigTextStyle().bigText(this.text)).addAction(i, NotificationUtils.getActionBtnText(this.mResource.getString(i2), R.color.colorPrimaryDark), broadcast).addAction((int) R.drawable.ic_ota_wifi_24px, NotificationUtils.getActionBtnText(string, R.color.colorPrimaryDark), activity).setContentIntent(activity).setLocalOnly(true).setCategory("alarm");
        Bundle bundle = new Bundle();
        bundle.putString(UpgradeUtilConstants.KEY_NOTIFICATION_TYPE, NotificationUtils.KEY_NOTIFY_WIFI_NOTIFICATION);
        builder.setExtras(bundle);
        Notification build = builder.build();
        build.flags |= 2;
        this.mNotificationMgr.notify(NotificationUtils.OTA_NOTIFICATION_ID, build);
    }

    public void cancel() {
        this.mNotificationMgr.cancel(NotificationUtils.OTA_NOTIFICATION_ID);
    }

    public NotificationCompat.Builder buildNotification(String str, String str2, PendingIntent pendingIntent, Intent intent) {
        UpdaterUtils.UpgradeInfo upgradeInfoAfterOTAUpdate = UpdaterUtils.getUpgradeInfoAfterOTAUpdate(intent);
        if (upgradeInfoAfterOTAUpdate != null) {
            this.mUpdateTypeInterface = UpdateType.getUpdateType(upgradeInfoAfterOTAUpdate.getUpdateTypeData());
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.mContext, this.channelId);
        builder.setWhen(System.currentTimeMillis()).setSmallIcon((int) R.drawable.ota_icon).setColor(this.mResource.getColor(this.mUpdateTypeInterface.getUpdateSpecificColor(), this.mContext.getTheme())).setContentTitle(Html.fromHtml("<b>" + str + "<b>", 0)).setStyle(new NotificationCompat.BigTextStyle().bigText(str2)).setContentIntent(pendingIntent).setLocalOnly(true).setCategory("alarm");
        return builder;
    }
}
