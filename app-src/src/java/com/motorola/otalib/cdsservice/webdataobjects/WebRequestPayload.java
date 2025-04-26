package com.motorola.otalib.cdsservice.webdataobjects;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WebRequestPayload {
    private String data;
    private WebRequestPayloadType type;

    public WebRequestPayload(WebRequestPayloadType webRequestPayloadType, String str) {
        this.type = webRequestPayloadType;
        this.data = str;
    }

    public WebRequestPayloadType getType() {
        return this.type;
    }

    public String getData() {
        return this.data;
    }
}
