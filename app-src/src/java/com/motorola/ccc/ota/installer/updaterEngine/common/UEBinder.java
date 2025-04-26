package com.motorola.ccc.ota.installer.updaterEngine.common;

import java.lang.reflect.InvocationTargetException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class UEBinder {
    private static boolean IS_BINDED;

    public static void checkAndBindWithUE(UpdateEngineCallbacker updateEngineCallbacker) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        setBinded(true);
        UpdaterEngineHelper.bindWithUpdaterEngine(updateEngineCallbacker);
    }

    public static boolean isBinded() {
        return IS_BINDED;
    }

    public static void resetBinded() {
        UpdaterEngineHelper.unbindUpdateEngine();
        setBinded(false);
    }

    public static void setBinded(boolean z) {
        IS_BINDED = z;
    }
}
