package com.motorola.ccc.ota.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class CallStateChangeReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : "";
        Logger.debug("OtaApp", "CallStateChangeReceiver, Received intent " + action);
        if ("android.intent.action.PHONE_STATE".equals(action)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
            if (telephonyManager == null) {
                Logger.info("OtaApp", "Strange, TelephonyManager is null");
                return;
            }
            int callState = telephonyManager.getCallState();
            if (callState == 0) {
                Logger.info("OtaApp", "CallStateChangeReceiver.onReceive,done with call .. run state machine to take further action");
                context.sendBroadcast(new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE), Permissions.INTERACT_OTA_SERVICE);
                disableSelf(context);
                return;
            }
            Logger.info("OtaApp", "CallStateChangeReceiver.onReceive,call state : " + callState);
        }
    }

    private void disableSelf(Context context) {
        try {
            OtaApplication.getGlobalContext().getPackageManager().setComponentEnabledSetting(new ComponentName(OtaApplication.getGlobalContext(), CallStateChangeReceiver.class), 2, 1);
        } catch (Exception unused) {
        }
    }
}
