package com.motorola.ccc.ota.env;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class OOBSetupReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        try {
            BotaSettings botaSettings = new BotaSettings();
            if (intent.getAction().equals(UpgradeUtilConstants.SMART_UPDATE_USER_OPTIN)) {
                String stringExtra = intent.getStringExtra(UpgradeUtilConstants.SMART_UPDATE_OPTIN);
                Logger.debug("OtaApp", "OOBSetupReceiver, smartupdateOptin: " + stringExtra);
                if ("true".equals(stringExtra)) {
                    SmartUpdateUtils.turnSmartUpdateOn("OOB");
                    botaSettings.setString(Configs.STATS_SMART_UPDATE_LAUNCH_MODE, "OOB");
                    botaSettings.setBoolean(Configs.STATS_SMART_UPDATE_ENABLED_VIA_OOB, true);
                } else {
                    SmartUpdateUtils.turnSmartUpdateOff("OOB");
                    botaSettings.setBoolean(Configs.STATS_SMART_UPDATE_ENABLED_VIA_OOB, false);
                }
            }
        } catch (Exception e) {
            Logger.debug("OtaApp", "OOBSetupReceiver, Error receiving smart update intent from OOB:" + e.toString());
        }
    }
}
