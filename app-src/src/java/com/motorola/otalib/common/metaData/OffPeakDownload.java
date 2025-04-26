package com.motorola.otalib.common.metaData;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class OffPeakDownload {
    private int duration;
    private int promotionTime;
    private int startTime;
    private String useLocalTz;

    public OffPeakDownload(String str, int i, int i2, int i3) {
        this.useLocalTz = str;
        this.promotionTime = i;
        this.startTime = i2;
        this.duration = i3;
    }

    public String isUseLocalTz() {
        return this.useLocalTz;
    }

    public int getPromotionTime() {
        return this.promotionTime;
    }

    public int getStartTime() {
        return this.startTime;
    }

    public int getDuration() {
        return this.duration;
    }
}
