package com.motorola.otalib.main.Downloader;

import android.content.Context;
import com.motorola.otalib.common.backoff.IncrementalBackoffValueProvider;
import com.motorola.otalib.common.utils.UpgradeUtils;
import com.motorola.otalib.main.LibCussm;
import com.motorola.otalib.main.Logger;
import com.motorola.otalib.main.OtaLibService;
import com.motorola.otalib.main.Settings.LibConfigs;
import com.motorola.otalib.main.Settings.LibSettings;
import java.io.File;
import java.util.concurrent.ScheduledFuture;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ResetSMToGettingDescriptor {
    private static boolean mRetryPending;
    private static ScheduledFuture retryTask;
    private final Context ctx;
    private final boolean deleteFile;
    private final File downloadFile;
    private final String errorTitle;
    private Context mContext;
    private final String primaryKey;
    private final LibSettings settings;
    private final LibCussm sm;

    public ResetSMToGettingDescriptor(Context context, int i, String str, String str2, File file, boolean z, LibSettings libSettings, LibCussm libCussm) {
        this.ctx = context;
        this.errorTitle = str;
        this.primaryKey = str2;
        this.downloadFile = file;
        this.deleteFile = z;
        this.settings = libSettings;
        this.sm = libCussm;
    }

    public boolean shouldRetry(final String str, final boolean z, String str2) {
        if (this.deleteFile) {
            this.downloadFile.delete();
        }
        this.settings.incrementPrefs(LibConfigs.OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS);
        int i = this.settings.getInt(LibConfigs.OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS, 0);
        if (i > this.settings.getInt(LibConfigs.MAX_RETRY_COUNT_DL, 3)) {
            mRetryPending = false;
            this.sm.failProgress(this.ctx, this.primaryKey, UpgradeUtils.DownloadStatus.STATUS_FAIL, "Aborting OTA process as the retry for " + str + "(" + this.errorTitle + ") have maxed out , retryCount : " + i, str2);
            return false;
        }
        mRetryPending = true;
        IncrementalBackoffValueProvider incrementalBackoffValueProvider = new IncrementalBackoffValueProvider(this.settings.getString(LibConfigs.BACKOFF_VALUES));
        long j = 0;
        for (int i2 = 0; i2 < i; i2++) {
            j = incrementalBackoffValueProvider.getNextTimeoutValue();
        }
        retryTask = OtaLibService.getScheduledExecutorService().schedule(new Runnable() { // from class: com.motorola.otalib.main.Downloader.ResetSMToGettingDescriptor.1
            @Override // java.lang.Runnable
            public void run() {
                boolean unused = ResetSMToGettingDescriptor.mRetryPending = false;
                ResetSMToGettingDescriptor.this.sm.onActionGetDescriptor(ResetSMToGettingDescriptor.this.ctx, ResetSMToGettingDescriptor.this.primaryKey, z, "encountered errorCode " + str + " go and fetch new download url");
            }
        }, j, incrementalBackoffValueProvider.getTimeUnit());
        Logger.debug(Logger.OTALib_TAG, "4xx retry. RetryCount = " + i + " RetryInterval= " + j);
        return true;
    }

    public static void clearRetryTask() {
        Logger.debug(Logger.OTALib_TAG, "ResetSMToGettingDescriptor.clearRetryTask() clearing pending retry task ");
        retryTask.cancel(false);
        mRetryPending = false;
    }

    public static boolean isRetryPending() {
        return mRetryPending;
    }
}
