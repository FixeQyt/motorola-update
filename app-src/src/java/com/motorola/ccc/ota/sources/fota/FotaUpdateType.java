package com.motorola.ccc.ota.sources.fota;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public abstract class FotaUpdateType {
    long getCriticalPackageInstallInterval() {
        return 10L;
    }

    long getPriorityPackageInstallInterval() {
        return 14400L;
    }

    int getPriorityPackageMaxInstallPostpnes() {
        return 3;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isDownloadVisible() {
        return false;
    }
}
