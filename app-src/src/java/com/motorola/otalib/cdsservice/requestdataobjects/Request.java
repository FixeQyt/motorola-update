package com.motorola.otalib.cdsservice.requestdataobjects;

import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class Request {
    private long mContentTimestamp;
    private JSONObject mDeviceInfo;
    private JSONObject mExtraInfo;
    private String mId;
    private JSONObject mIdentityInfo;

    public Request(String str, long j, JSONObject jSONObject, JSONObject jSONObject2, JSONObject jSONObject3) {
        this.mId = str;
        this.mContentTimestamp = j;
        this.mDeviceInfo = jSONObject;
        this.mExtraInfo = jSONObject2;
        this.mIdentityInfo = jSONObject3;
    }

    public String getId() {
        return this.mId;
    }

    public long getContentTimestamp() {
        return this.mContentTimestamp;
    }

    public JSONObject getDeviceInfo() {
        return this.mDeviceInfo;
    }

    public JSONObject getExtraInfo() {
        return this.mExtraInfo;
    }

    public JSONObject getIdentityInfo() {
        return this.mIdentityInfo;
    }
}
