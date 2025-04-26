package com.motorola.ccc.ota.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import androidx.core.app.NotificationCompat;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class InstallNotification {
    private static boolean sNotificationSuspended = false;
    private static boolean sNotificationSwiped = false;
    private static int total = 100;
    private Context mContext;
    private NotificationManager mNotificationMgr;
    private PendingIntent mPendingIntent;
    public BroadcastReceiver mSwipedReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.InstallNotification.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (UpgradeUtilConstants.ACTION_NOTIFICATION_SWIPED.equals(intent.getAction())) {
                Logger.debug("OtaApp", "InstallNotification, ACTION_NOTIFICATION_SWIPED");
                InstallNotification.sNotificationSwiped = true;
                InstallNotification.sNotificationSuspended = false;
            }
            if (UpgradeUtilConstants.UPGRADE_UPDATER_STATE_CLEAR.equals(intent.getAction())) {
                Logger.debug("OtaApp", "InstallNotification, UPGRADE_UPDATER_STATE_CLEAR");
                InstallNotification.sNotificationSwiped = false;
                InstallNotification.sNotificationSuspended = false;
            }
            NotificationUtils.unRegisterSwipeableNotificationReceiver(InstallNotification.this.mContext, InstallNotification.this.mSwipedReceiver);
        }
    };
    private String mMediumChannelId = NotificationChannelCreator.getMediumNotificationChannelId();

    public InstallNotification(Context context) {
        this.mContext = context;
        this.mNotificationMgr = (NotificationManager) this.mContext.getSystemService("notification");
    }

    /* JADX WARN: Removed duplicated region for block: B:90:0x02de  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void updateNotification(float r19, int r20, int r21, java.lang.String r22, boolean r23, com.motorola.ccc.ota.sources.bota.settings.BotaSettings r24) {
        /*
            Method dump skipped, instructions count: 946
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.ccc.ota.ui.InstallNotification.updateNotification(float, int, int, java.lang.String, boolean, com.motorola.ccc.ota.sources.bota.settings.BotaSettings):void");
    }

    public void cancel() {
        this.mNotificationMgr.cancel(NotificationUtils.OTA_NOTIFICATION_ID);
    }

    private void addCellularButton(NotificationCompat.Builder builder, Resources resources) {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (UpdaterUtils.isWifiConnected(this.mContext)) {
            return;
        }
        Intent intent = new Intent(UpdaterUtils.ACTION_USER_RESUME_STREAMING_DOWNLOAD_ON_CELLULAR);
        intent.setComponent(new ComponentName(OtaApplication.getGlobalContext(), UpdateReceiver.class));
        builder.addAction(17301540, NotificationUtils.getActionBtnText(resources.getString(R.string.resume_download_on_cellular), R.color.colorPrimaryDark), PendingIntent.getBroadcast(this.mContext, 0, intent, 67108864));
        if (BuildPropReader.isBotaATT()) {
            Intent intent2 = new Intent("android.settings.WIFI_SETTINGS");
            intent2.setFlags(536870912);
            builder.addAction(17301540, NotificationUtils.getActionBtnText(resources.getString(R.string.connect_to_wifi), R.color.colorPrimaryDark), PendingIntent.getActivity(this.mContext, 0, intent2, 335544320));
        }
    }
}
