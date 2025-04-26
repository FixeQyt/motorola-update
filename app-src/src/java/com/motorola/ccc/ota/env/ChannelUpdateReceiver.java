package com.motorola.ccc.ota.env;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class ChannelUpdateReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Logger.debug("OtaApp", "ChannelUpdateReceiver.onReceive: " + intent.getAction());
        try {
            BotaSettings botaSettings = new BotaSettings();
            if (UpgradeUtilConstants.PROVISION_DEVICE_RESPONSE_INTENT.equals(intent.getAction())) {
                botaSettings.setBoolean(Configs.CHANNEL_ID_UPDATED, true);
                CusAndroidUtils.postChannelIdUpdatedIntent(context);
            }
        } catch (Exception e) {
            Logger.error("OtaApp", "ChannelUpdateReceiver, Error receiving CCE_CHANNEL_ID_UPDATED intent" + e.toString());
        }
    }
}
