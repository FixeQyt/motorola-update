package com.motorola.otalib.downloadservice.dataobjects;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DownloadRequest {
    private boolean allowOnRoaming;
    private String contentResource;
    private String disallowedNetworks;
    private String fileName;
    private String hostName;
    private String mBackOffValues;
    private int mMaxRetryCount;
    private int port;
    private long size;
    private long startingOffset;
    private long time;
    private String upgradeSourceType;
    private boolean wifiOnly;

    public DownloadRequest(String str, boolean z, long j, long j2, String str2, String str3, int i, String str4, String str5, int i2, boolean z2, String str6, boolean z3, long j3) {
        this.contentResource = str;
        this.wifiOnly = z;
        this.time = j;
        this.size = j2;
        this.fileName = str2;
        this.hostName = str3;
        this.port = i;
        this.disallowedNetworks = str4;
        this.mBackOffValues = str5;
        this.mMaxRetryCount = i2;
        this.upgradeSourceType = str6;
        this.allowOnRoaming = z3;
        this.startingOffset = j3;
    }

    public String getContentResource() {
        return this.contentResource;
    }

    public boolean getWifiOnly() {
        return this.wifiOnly;
    }

    public long getTime() {
        return this.time;
    }

    public long getSize() {
        return this.size;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getHostName() {
        String str = this.hostName;
        return str != null ? str : "";
    }

    public int getPort() {
        return this.port;
    }

    public String getDisallowedNetworks() {
        String str = this.disallowedNetworks;
        return str != null ? str : "";
    }

    public String getBackOffValues() {
        return this.mBackOffValues;
    }

    public int getMaxRetryCount() {
        return this.mMaxRetryCount;
    }

    public String getUpgradeSourceType() {
        return this.upgradeSourceType;
    }

    public boolean getAllowOnRoaming() {
        return this.allowOnRoaming;
    }

    public long getStartingOffset() {
        return this.startingOffset;
    }
}
