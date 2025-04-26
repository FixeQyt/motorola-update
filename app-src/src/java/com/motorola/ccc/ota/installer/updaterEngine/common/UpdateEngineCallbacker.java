package com.motorola.ccc.ota.installer.updaterEngine.common;

import android.os.UpdateEngineCallback;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.otalib.common.errorCodes.UpdaterEngineErrorCodes;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpdateEngineCallbacker extends UpdateEngineCallback {
    private static CallBackInterface mCallbackHandler;
    private BotaSettings settings;

    public void onStatusUpdate(int i, float f) {
        Logger.debug("OtaApp", "UpdateEngineCallbackImplementation: StatusUpdate " + i + " percentage " + f);
        if (this.settings.getBoolean(Configs.VAB_MERGE_PROCESS_RUNNING, false) && i == 0) {
            UpdaterEngineHelper.cleanupAppliedPayload();
        }
        CallBackInterface callBackInterface = mCallbackHandler;
        if (callBackInterface == null || i == 11) {
            return;
        }
        callBackInterface.onProgress(i, f);
    }

    public void onPayloadApplicationComplete(int i) {
        Logger.debug("OtaApp", "onPayloadApplicationComplete : errorCode " + i);
        boolean z = this.settings.getBoolean(Configs.VAB_MERGE_PROCESS_RUNNING, false);
        CallBackInterface callBackInterface = mCallbackHandler;
        if (callBackInterface == null || z) {
            return;
        }
        callBackInterface.onCompleted(i, null, UpdaterEngineErrorCodes.getFailureResultStatus(i));
    }

    public void registerForCallBacker(CallBackInterface callBackInterface) {
        Logger.verbose("OtaApp", "Registering callback for updater engine");
        mCallbackHandler = callBackInterface;
        this.settings = new BotaSettings();
    }

    public void unRegisterForCallBacker() {
        Logger.verbose("OtaApp", "UnRegistering callback from updater engine");
        mCallbackHandler = null;
    }
}
