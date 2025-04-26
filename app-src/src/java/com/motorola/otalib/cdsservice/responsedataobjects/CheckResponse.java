package com.motorola.otalib.cdsservice.responsedataobjects;

import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CheckResponse extends Response {
    private JSONObject mSettings;

    public CheckResponse(boolean z, String str, String str2, long j, String str3, String str4, long j2, JSONObject jSONObject, int i, JSONObject jSONObject2, boolean z2) {
        super(z, str, str2, j, str3, str4, j2, i, jSONObject, z2);
        this.mSettings = jSONObject2;
    }

    public JSONObject getSettings() {
        return this.mSettings;
    }
}
