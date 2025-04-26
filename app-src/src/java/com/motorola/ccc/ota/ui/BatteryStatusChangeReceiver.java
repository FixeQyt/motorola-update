package com.motorola.ccc.ota.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.utils.BroadcastUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class BatteryStatusChangeReceiver extends BroadcastReceiver {
    private BotaSettings botaSettings;
    private int mBatteryLevel;
    private int previousBatteryLevel;

    public BatteryStatusChangeReceiver(BotaSettings botaSettings) {
        this.botaSettings = botaSettings;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BATTERY_CHANGED".equals(intent != null ? intent.getAction() : "")) {
            int max = Math.max(intent.getIntExtra("level", 0), 0);
            this.mBatteryLevel = max;
            if (max != this.previousBatteryLevel) {
                this.previousBatteryLevel = max;
                Logger.info("OtaApp", "Battery strength : " + this.mBatteryLevel);
            }
            processBatteryValues(context);
        }
    }

    private void processBatteryValues(Context context) {
        boolean isBatteryLowToStartDownload = UpdaterUtils.isBatteryLowToStartDownload(context);
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_BATTERY_CHANGED);
        if (!this.botaSettings.getBoolean(Configs.BATTERY_LOW) && isBatteryLowToStartDownload) {
            this.botaSettings.setBoolean(Configs.BATTERY_LOW, true);
            Logger.debug("OtaApp", "BatteryStatusChangeReceiver:sendBatteryLow = true");
            intent.putExtra(UpdaterUtils.KEY_BATTERY_LOW, true);
            BroadcastUtils.sendLocalBroadcast(context, intent);
        } else if (!this.botaSettings.getBoolean(Configs.BATTERY_LOW) || isBatteryLowToStartDownload) {
        } else {
            this.botaSettings.setBoolean(Configs.BATTERY_LOW, false);
            Logger.debug("OtaApp", "BatteryStatusChangeReceiver:sendBatteryLow = false");
            intent.putExtra(UpdaterUtils.KEY_BATTERY_LOW, false);
            BroadcastUtils.sendLocalBroadcast(context, intent);
        }
    }
}
