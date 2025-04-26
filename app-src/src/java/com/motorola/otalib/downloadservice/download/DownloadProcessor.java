package com.motorola.otalib.downloadservice.download;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public interface DownloadProcessor {
    void errorCode(int i, boolean z);

    void exception(Exception exc);

    void failed(HttpFileDownloadException httpFileDownloadException);

    void finished();

    void progress(byte[] bArr);
}
