package com.motorola.ccc.ota.env;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class TOSSetupReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Logger.debug("OtaApp", "TOSSetupReceiver.handleIntent: " + intent.getAction());
        try {
            BotaSettings botaSettings = new BotaSettings();
            if (UpgradeUtilConstants.SETUP_TOS_ACCEPTED.equals(intent.getAction())) {
                botaSettings.setBoolean(Configs.SETUP_TOS_ACCEPTED, true);
                if (BuildPropReader.isCtaVersion(botaSettings)) {
                    return;
                }
                CusAndroidUtils.postTOSCompleted(OtaApplication.getGlobalContext());
            }
        } catch (Exception e) {
            Logger.error("OtaApp", "TOSSetupReceiver, Error receiving TOS intent from OOB:" + e.toString());
        }
    }
}
