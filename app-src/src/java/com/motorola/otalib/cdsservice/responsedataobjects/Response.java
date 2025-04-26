package com.motorola.otalib.cdsservice.responsedataobjects;

import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class Response {
    private JSONObject mContent;
    private long mContentTimestamp;
    private String mContext;
    private String mContextKey;
    private boolean mIsUploadFailureLogsEnabled;
    private long mPollAfterSeconds;
    private boolean mProceed;
    private String mReportingTags;
    private int mSmartUpdateBitmap;
    private String mTrackingId;

    public Response(boolean z, String str, String str2, long j, String str3, String str4, long j2, int i, JSONObject jSONObject, boolean z2) {
        this.mProceed = z;
        this.mContext = str;
        this.mContextKey = str2;
        this.mContentTimestamp = j;
        this.mReportingTags = str3;
        this.mTrackingId = str4;
        this.mPollAfterSeconds = j2;
        this.mSmartUpdateBitmap = i;
        this.mContent = jSONObject;
        this.mIsUploadFailureLogsEnabled = z2;
    }

    public boolean proceed() {
        return this.mProceed;
    }

    public String getContext() {
        return this.mContext;
    }

    public String getContextKey() {
        return this.mContextKey;
    }

    public long getContextTimeStamp() {
        return this.mContentTimestamp;
    }

    public String getReportingTags() {
        return this.mReportingTags;
    }

    public String getTrackingId() {
        return this.mTrackingId;
    }

    public long getPollAfterSeconds() {
        return this.mPollAfterSeconds;
    }

    public int getSmartUpdateBitmap() {
        return this.mSmartUpdateBitmap;
    }

    public JSONObject getContent() {
        return this.mContent;
    }

    public boolean isUploadFailureLogsEnabled() {
        return this.mIsUploadFailureLogsEnabled;
    }
}
