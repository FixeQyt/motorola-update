package com.motorola.ccc.ota.installer.updaterEngine.common;

import android.content.Context;
import com.motorola.ccc.ota.installer.updaterEngine.UpdaterEngineInstaller;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.errorCodes.UpdaterEngineErrorCodes;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class UpdaterEngineStateHandler {
    private static UpdaterEngineInstaller UE;
    private static CallBackInterface callBackInterface;
    private static UpdaterEngineStateHandler mUEStateHandler;
    private static UpdateEngineCallbacker mUpdaterEngineCalllbacker;
    private static AtomicBoolean progress;

    private UpdaterEngineStateHandler() {
        progress = new AtomicBoolean(false);
    }

    public static UpdaterEngineStateHandler getUpdaterEngineStateHandlerInstance() {
        if (mUEStateHandler == null) {
            mUEStateHandler = new UpdaterEngineStateHandler();
        }
        return mUEStateHandler;
    }

    public void initializeUpdaterEngineStateHandler(UpdaterEngineInstaller updaterEngineInstaller, CallBackInterface callBackInterface2) {
        setProgress(true);
        UE = updaterEngineInstaller;
        callBackInterface = callBackInterface2;
        UpdateEngineCallbacker updateEngineCallbacker = new UpdateEngineCallbacker();
        mUpdaterEngineCalllbacker = updateEngineCallbacker;
        updateEngineCallbacker.registerForCallBacker(callBackInterface2);
    }

    private static void setProgress(boolean z) {
        progress.set(z);
    }

    public static boolean isBusy() {
        return progress.get();
    }

    public void transferUpgradeToUE() {
        try {
            if (!UEBinder.isBinded()) {
                UEBinder.checkAndBindWithUE(mUpdaterEngineCalllbacker);
            } else {
                callBackInterface.onProgress(0, 0.0f);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            callBackInterface.onCompleted(UpdaterEngineErrorCodes.ERROR_EXCEPTION, "Caught an exception during bind " + e, ErrorCodeMapper.KEY_BIND_FAILURE);
        }
    }

    public void transferUpgradeBackToOta(Context context, boolean z, String str, String str2) {
        InstallerUtilMethods.sendUpdaterEngineStatusCompleted(context, z, str, str2);
        UE.clearUEInstallerBeforeExit();
    }

    public void clearUEStateHandler() {
        Logger.verbose("OtaApp", "Clearing updater engine status");
        UpdateEngineCallbacker updateEngineCallbacker = mUpdaterEngineCalllbacker;
        if (updateEngineCallbacker != null) {
            updateEngineCallbacker.unRegisterForCallBacker();
        }
        InstallerUtilMethods.releaseWakelock();
        setProgress(false);
    }
}
