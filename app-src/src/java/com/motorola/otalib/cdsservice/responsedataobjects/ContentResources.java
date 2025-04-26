package com.motorola.otalib.cdsservice.responsedataobjects;

import java.util.Map;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ContentResources {
    private Map<String, String> mAdminApnHeaders;
    private String mAdminApnUrl;
    private Map<String, String> mCellularHeaders;
    private String mCellularUrl;
    private boolean mProceed;
    private String mTrackingId;
    private Map<String, String> mWifiHeaders;
    private String mWifiUrl;

    public ContentResources(boolean z, String str, Map<String, String> map, String str2, Map<String, String> map2, String str3, Map<String, String> map3, String str4) {
        this.mProceed = z;
        this.mWifiUrl = str;
        this.mCellularUrl = str2;
        this.mWifiHeaders = map;
        this.mCellularHeaders = map2;
        this.mAdminApnUrl = str3;
        this.mAdminApnHeaders = map3;
        this.mTrackingId = str4;
    }

    public boolean getProceed() {
        return this.mProceed;
    }

    public String getWifiUrl() {
        return this.mWifiUrl;
    }

    public Map<String, String> getWifiHeaders() {
        return this.mWifiHeaders;
    }

    public String getCellularUrl() {
        return this.mCellularUrl;
    }

    public Map<String, String> getCellularHeaders() {
        return this.mCellularHeaders;
    }

    public String getAdminApnUrl() {
        return this.mAdminApnUrl;
    }

    public Map<String, String> getAdminApnHeaders() {
        return this.mAdminApnHeaders;
    }

    public String getTrackingId() {
        return this.mTrackingId;
    }
}
