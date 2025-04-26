package com.motorola.ccc.ota.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class PowerdownReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : "";
        Logger.debug("OtaApp", "PowerdownReceiver, Received intent " + action);
        if ("android.intent.action.ACTION_SHUTDOWN".equals(action) || "android.intent.action.REBOOT".equals(action)) {
            new BotaSettings().setBoolean(Configs.DEVICE_REBOOTED, true);
            UpdaterUtils.disableBatteryStatusReceiver();
            UpdaterUtils.disablePowerDownReceiver();
        }
    }
}
