package com.motorola.ccc.ota.utils;

import android.content.Context;
import android.content.Intent;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class TestUtils {
    private static String CLIENT_STATE = "client_state_name";
    private static String SEND_STATE_RESPONSE = "com.motorola.ccc.ota.CusAndroidUtils.SEND_STATE_RESPONSE";

    public static void collectCrashDump() {
        if (BuildPropertyUtils.isDogfoodDevice()) {
            OtaApplication.getScheduledExecutorService().schedule(new Runnable() { // from class: com.motorola.ccc.ota.utils.TestUtils.1
                @Override // java.lang.Runnable
                public void run() {
                    Logger.debug("OtaApp", "Looks like OTA restarted after reboot inform test app");
                    OtaApplication.getGlobalContext().sendBroadcast(new Intent("com.motorola.blur.service.blur.Actions.OTA_SERVICE_RESTART"), Permissions.INTERACT_OTA_SERVICE);
                }
            }, 20L, TimeUnit.SECONDS);
        }
    }

    public static final void sendStateResponse(Context context, String str) {
        if (BuildPropertyUtils.isDogfoodDevice()) {
            Intent intent = new Intent(SEND_STATE_RESPONSE);
            intent.putExtra(CLIENT_STATE, str);
            context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
        }
    }
}
