package com.motorola.otalib.downloadservice.download;

import android.os.PowerManager;
import com.motorola.otalib.common.backoff.BackoffValueProvider;
import com.motorola.otalib.downloadservice.utils.DownloadServiceLogger;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class PackageDownloader {

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public interface FileDownloader {
        void addHeader(String str, String str2);

        void addProcessor(DownloadProcessor downloadProcessor);

        void downloadFile(String str, int i, String str2, File file, long j, PowerManager.WakeLock wakeLock, String str3, long j2) throws HttpFileDownloadException, URISyntaxException;

        void shutdown();
    }

    public FileDownloader getFileDownloader(BackoffValueProvider backoffValueProvider, HttpURLConnection httpURLConnection) {
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "FileDownloaderFactory.getFileDownloader downloaderType ADVANCE download");
        return new AdvancedFileDownloader(httpURLConnection, backoffValueProvider);
    }
}
