package com.motorola.ccc.ota.env;

import android.content.Context;
import android.content.Intent;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.modem.ModemPollingManager;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.PMUtils;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CheckUpdateWorker extends Worker {
    public CheckUpdateWorker(Context context, WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    public synchronized ListenableWorker.Result doWork() {
        final Intent intent;
        Logger.debug("OtaApp", "work manager triggered");
        final Context globalContext = OtaApplication.getGlobalContext();
        if (!UpdaterUtils.isTOSCheckPassed(new BotaSettings())) {
            Logger.debug("OtaApp", "TOS check is not passed for china device, don't do any network call, return from here");
            ListenableWorker.Result.success();
        }
        if (!UpdaterUtils.isOtaServiceRunning(globalContext)) {
            Logger.debug("OtaApp", "CheckUpdateWorker - doWork : OTA Service is not running, starting OTA Service...");
            globalContext.startService(new Intent(globalContext, OtaService.class));
        }
        UpdaterUtils.setOtaSystemBindServiceEnabledState(globalContext, true);
        Set tags = getTags();
        if (tags != null && tags.contains(PMUtils.MODEM_WORK_TAG)) {
            intent = new Intent(ModemPollingManager.INTENT_ACTION_POLLING_MANAGER);
        } else {
            intent = new Intent(AndroidPollingManager.INTENT_ACTION_POLLING_MANAGER);
        }
        OtaApplication.getScheduledExecutorService().schedule(new Runnable() { // from class: com.motorola.ccc.ota.env.CheckUpdateWorker.1
            @Override // java.lang.Runnable
            public void run() {
                Logger.debug("OtaApp", "CusSM.CheckUpdateWorker sending broadcast " + intent);
                globalContext.sendBroadcast(intent);
            }
        }, 10L, TimeUnit.SECONDS);
        return ListenableWorker.Result.success();
    }
}
