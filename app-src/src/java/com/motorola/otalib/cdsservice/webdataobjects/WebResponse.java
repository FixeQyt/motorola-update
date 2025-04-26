package com.motorola.otalib.cdsservice.webdataobjects;

import java.util.Map;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WebResponse {
    private Map<String, String> mHeaders;
    private JSONObject mPayload;
    private int mStatusCode;

    public WebResponse(int i, JSONObject jSONObject, Map<String, String> map) {
        this.mStatusCode = i;
        this.mPayload = jSONObject;
        this.mHeaders = map;
    }

    public int getStatusCode() {
        return this.mStatusCode;
    }

    public JSONObject getPayload() {
        return this.mPayload;
    }

    public Map<String, String> getHeaders() {
        return this.mHeaders;
    }
}
