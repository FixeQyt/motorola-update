package com.motorola.ccc.ota.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.utils.Logger;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class NotificationChannelCreator {
    private static String sMediumNotificationChannelId = "ota_notification_channel_medium_01";
    private static String sUrgentNotificationChannelId = "ota_notification_channel_urgent_01";

    static {
        NotificationManager notificationManager = (NotificationManager) OtaApplication.getGlobalContext().getSystemService("notification");
        if (notificationManager.getNotificationChannel(sUrgentNotificationChannelId) == null) {
            Logger.debug("OtaApp", "creating new NotificationChannel with urgent importance");
            String string = OtaApplication.getGlobalContext().getResources().getString(R.string.urgent_channel_name);
            NotificationChannel notificationChannel = new NotificationChannel(sUrgentNotificationChannelId, string, 4);
            notificationChannel.setDescription(string);
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        if (notificationManager.getNotificationChannel(sMediumNotificationChannelId) == null) {
            Logger.debug("OtaApp", "creating new NotificationChannel with medium importance");
            String string2 = OtaApplication.getGlobalContext().getResources().getString(R.string.medium_channel_name);
            NotificationChannel notificationChannel2 = new NotificationChannel(sMediumNotificationChannelId, string2, 2);
            notificationChannel2.setDescription(string2);
            notificationManager.createNotificationChannel(notificationChannel2);
        }
    }

    public static String getUrgentNotificationChannelId() {
        return sUrgentNotificationChannelId;
    }

    public static String getMediumNotificationChannelId() {
        return sMediumNotificationChannelId;
    }
}
