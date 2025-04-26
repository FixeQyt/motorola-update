package com.motorola.otalib.cdsservice.requestdataobjects;

import com.google.gson.Gson;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UrlRequest {
    private String mBaseUrl;
    private String mContext;
    private String mContextKey;
    private String mIsSecure;
    private String mServerUrl;
    private String mState;
    private String mTestUrl;
    private String mTrackingId;

    public UrlRequest(String str, String str2, String str3, String str4, String str5, String str6) {
        this.mServerUrl = str;
        this.mBaseUrl = str2;
        this.mContext = str3;
        this.mContextKey = str4;
        this.mIsSecure = str5;
        this.mTestUrl = str6;
    }

    public UrlRequest(String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        this.mServerUrl = str;
        this.mBaseUrl = str2;
        this.mContext = str3;
        this.mContextKey = str4;
        this.mTrackingId = str5;
        this.mIsSecure = str6;
        this.mTestUrl = str7;
    }

    public UrlRequest(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8) {
        this.mServerUrl = str;
        this.mBaseUrl = str2;
        this.mContext = str3;
        this.mContextKey = str4;
        this.mState = str5;
        this.mTrackingId = str6;
        this.mIsSecure = str7;
        this.mTestUrl = str8;
    }

    public String getServerUrl() {
        return this.mServerUrl;
    }

    public String getBaseUrl() {
        return this.mBaseUrl;
    }

    public String getTestUrl() {
        return this.mTestUrl;
    }

    public String getContext() {
        return this.mContext;
    }

    public String getContextKey() {
        return this.mContextKey;
    }

    public String getState() {
        return this.mState;
    }

    public String getTrackingId() {
        return this.mTrackingId;
    }

    public String getIsSecure() {
        return this.mIsSecure;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public static UrlRequest fromJsonString(String str) {
        return (UrlRequest) new Gson().fromJson(str, UrlRequest.class);
    }
}
