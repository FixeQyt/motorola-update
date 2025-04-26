package com.motorola.otalib.cdsservice.requestdataobjects;

import com.google.gson.Gson;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CheckRequest extends Request {
    private String mIdType;
    private String mTriggeredBy;

    public CheckRequest(String str, long j, JSONObject jSONObject, JSONObject jSONObject2, JSONObject jSONObject3, String str2, String str3) {
        super(str, j, jSONObject, jSONObject2, jSONObject3);
        this.mTriggeredBy = str2;
        this.mIdType = str3;
    }

    public String getTriggerdBy() {
        return this.mTriggeredBy;
    }

    public String getIdType() {
        return this.mIdType;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
