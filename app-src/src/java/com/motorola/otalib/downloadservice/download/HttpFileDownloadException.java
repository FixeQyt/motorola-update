package com.motorola.otalib.downloadservice.download;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class HttpFileDownloadException extends Exception {
    private static final long serialVersionUID = 1;
    private HttpDownloaderStatus downloaderStatus;
    private String spErrorCode;
    private int status;

    public HttpFileDownloadException(String str, HttpDownloaderStatus httpDownloaderStatus) {
        super(str);
        this.downloaderStatus = httpDownloaderStatus;
        this.status = -1;
    }

    public HttpFileDownloadException(String str, int i) {
        super(str);
        this.downloaderStatus = HttpDownloaderStatus.DOWNLOAD_RECEIVED_HTTP_ERROR;
        this.status = i;
    }

    public HttpFileDownloadException(String str, int i, String str2) {
        super(str);
        this.downloaderStatus = HttpDownloaderStatus.DOWNLOAD_RECEIVED_HTTP_ERROR;
        this.status = i;
        this.spErrorCode = str2;
    }

    public int getStatus() {
        return this.status;
    }

    public HttpDownloaderStatus getDownloaderStatus() {
        return this.downloaderStatus;
    }

    public String getSpErrorCode() {
        return this.spErrorCode;
    }
}
