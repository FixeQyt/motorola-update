package com.motorola.ccc.ota.stats;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class StatsDownloadIface {
    public IfaceName iface;
    public long rxBytes;
    public long txBytes;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public enum IfaceName {
        admin,
        internet,
        wifi,
        all
    }

    public StatsDownloadIface(IfaceName ifaceName, long j, long j2) {
        this.iface = ifaceName;
        this.rxBytes = j;
        this.txBytes = j2;
    }
}
