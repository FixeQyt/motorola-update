package com.motorola.otalib.common.metaData;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DownloadTimes {
    private long[][] downloadSlots;
    private String useLocalTz;

    public DownloadTimes(String str, long[][] jArr) {
        this.useLocalTz = str;
        this.downloadSlots = jArr;
    }

    public String isUseLocalTz() {
        return this.useLocalTz;
    }

    public long[][] getDownloadSlots() {
        return this.downloadSlots;
    }
}
