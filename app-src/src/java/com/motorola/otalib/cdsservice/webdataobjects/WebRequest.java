package com.motorola.otalib.cdsservice.webdataobjects;

import com.google.gson.Gson;
import java.util.Map;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WebRequest {
    private String mHttpMethod;
    private WebRequestPayload mPayload;
    private String mProxyHost;
    private int mProxyPort;
    private Map<String, String> mQueryParams;
    private int mRetries;
    private String mUrl;

    public WebRequest(String str, int i, String str2, Map<String, String> map, WebRequestPayload webRequestPayload, String str3, int i2) {
        this.mUrl = str;
        this.mRetries = i;
        this.mHttpMethod = str2;
        this.mQueryParams = map;
        this.mPayload = webRequestPayload;
        this.mProxyHost = str3;
        this.mProxyPort = i2;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public int getRetries() {
        return this.mRetries;
    }

    public String getHttpMethod() {
        return this.mHttpMethod;
    }

    public Map<String, String> getQueryParams() {
        return this.mQueryParams;
    }

    public WebRequestPayload getPayload() {
        return this.mPayload;
    }

    public String getProxyHost() {
        return this.mProxyHost;
    }

    public int getProxyPort() {
        return this.mProxyPort;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
