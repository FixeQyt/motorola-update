package com.motorola.ccc.ota.installer.updaterEngine.download;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class DownloadRequestToUE {
    private String mDownloadUrl;
    private long mFileSize;
    private String[] mHeaderKeyValuePair;
    private long mOffSet;

    public DownloadRequestToUE(String str, long j, long j2, String[] strArr) {
        this.mDownloadUrl = str;
        this.mOffSet = j;
        this.mFileSize = j2;
        this.mHeaderKeyValuePair = strArr;
    }

    public String getDownloadUrl() {
        return this.mDownloadUrl;
    }

    public long getOffSet() {
        return this.mOffSet;
    }

    public long getFileSize() {
        return this.mFileSize;
    }

    public String[] getHeaderKeyValuePair() {
        return this.mHeaderKeyValuePair;
    }
}
