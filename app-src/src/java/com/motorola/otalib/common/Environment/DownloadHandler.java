package com.motorola.otalib.common.Environment;

import com.motorola.otalib.common.Environment.ApplicationEnv;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public interface DownloadHandler {
    void close();

    boolean isBusy();

    default void onDeviceShutdown() {
    }

    void radioGotDown();

    void transferUpgrade(ApplicationEnv.Database.Descriptor descriptor);
}
