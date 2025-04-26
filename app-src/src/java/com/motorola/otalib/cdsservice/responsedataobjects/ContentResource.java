package com.motorola.otalib.cdsservice.responsedataobjects;

import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ContentResource {
    private JSONObject headers;
    private String[] tags;
    private String url;
    private String urlTtlSeconds;

    public String getDownloadURL() {
        return this.url;
    }

    public JSONObject getHeaders() {
        return this.headers;
    }

    public String[] getTags() {
        return this.tags;
    }

    public String getURLTtlSeconds() {
        return this.urlTtlSeconds;
    }
}
