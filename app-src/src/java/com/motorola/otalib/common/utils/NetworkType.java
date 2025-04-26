package com.motorola.otalib.common.utils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public enum NetworkType {
    UNKNOWN(0),
    GPRS(1),
    EDGE(2),
    UMTS(3),
    CDMA(4),
    EVDO_0(5),
    EVDO_A(6),
    EVDO_B(12),
    OnexRTT(7),
    HSDPA(8),
    HSPA(10),
    IDEN(11),
    LTE(13),
    EHRPD(14),
    HSPAPLUS(15),
    NR(20);
    
    private int networkType;

    NetworkType(int i) {
        this.networkType = i;
    }

    public static NetworkType fromCode(int i) {
        NetworkType[] values;
        for (NetworkType networkType : values()) {
            if (networkType.networkType == i) {
                return networkType;
            }
        }
        return UNKNOWN;
    }

    public int getNetworkTypeInt() {
        return this.networkType;
    }
}
