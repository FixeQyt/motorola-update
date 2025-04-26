package com.motorola.otalib.cdsservice.requestdataobjects;

import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class StateRequest extends Request {
    private String mIdType;
    private String mInfo;
    private String mLogs;
    private String mReportingTags;
    private JSONObject mStats;
    private String mStatus;
    private String mUpgradeSource;

    public StateRequest(String str, long j, JSONObject jSONObject, JSONObject jSONObject2, JSONObject jSONObject3, String str2, String str3, String str4, String str5, String str6, String str7, JSONObject jSONObject4) {
        super(str, j, jSONObject, jSONObject2, jSONObject3);
        this.mInfo = str2;
        this.mLogs = str3;
        this.mIdType = str4;
        this.mStatus = str5;
        this.mReportingTags = str6;
        this.mUpgradeSource = str7;
        this.mStats = jSONObject4;
    }

    public String getInfo() {
        return this.mInfo;
    }

    public String getLogs() {
        return this.mLogs;
    }

    public String getStatus() {
        return this.mStatus;
    }

    public String getIdType() {
        return this.mIdType;
    }

    public String getReportingTags() {
        return this.mReportingTags;
    }

    public String getUpgradeSource() {
        return this.mUpgradeSource;
    }

    public JSONObject getStats() {
        return this.mStats;
    }
}
