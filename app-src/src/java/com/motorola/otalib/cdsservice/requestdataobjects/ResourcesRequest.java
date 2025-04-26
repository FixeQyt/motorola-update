package com.motorola.otalib.cdsservice.requestdataobjects;

import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ResourcesRequest extends Request {
    private String mIdType;
    private String mReason;
    private String mReportingTags;

    public ResourcesRequest(String str, long j, JSONObject jSONObject, JSONObject jSONObject2, JSONObject jSONObject3, String str2, String str3, String str4) {
        super(str, j, jSONObject, jSONObject2, jSONObject3);
        this.mIdType = str2;
        this.mReportingTags = str3;
        this.mReason = str4;
    }

    public String getIdType() {
        return this.mIdType;
    }

    public String getReportingTags() {
        return this.mReportingTags;
    }

    public String getReason() {
        return this.mReason;
    }
}
