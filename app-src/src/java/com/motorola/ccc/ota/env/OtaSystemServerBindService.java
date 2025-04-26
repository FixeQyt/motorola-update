package com.motorola.ccc.ota.env;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.PMUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class OtaSystemServerBindService extends Service {
    private static final String EXPERIENCE = "com.motorola.intent.action.EXPERIENCE";
    private Binder mBinder = new Binder();

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        if (intent.getAction().equals(EXPERIENCE)) {
            try {
                Context applicationContext = getApplicationContext();
                if (!isServiceRunning(applicationContext, OtaService.class)) {
                    applicationContext.startService(new Intent(applicationContext, OtaService.class));
                }
            } catch (Exception e) {
                Logger.error("OtaApp", "Cannot start OtaService onBind moto system server" + e);
            }
            return this.mBinder;
        }
        return null;
    }

    private boolean isServiceRunning(Context context, Class<?> cls) {
        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) getSystemService("activity")).getRunningServices(PMUtils.APP_IS_EASY)) {
            if (cls.getName().equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
