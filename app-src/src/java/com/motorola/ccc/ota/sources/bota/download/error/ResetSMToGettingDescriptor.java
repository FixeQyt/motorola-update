package com.motorola.ccc.ota.sources.bota.download.error;

import android.content.Context;
import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.otalib.common.backoff.IncrementalBackoffValueProvider;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.io.File;
import java.util.concurrent.ScheduledFuture;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class ResetSMToGettingDescriptor {
    private static boolean mRetryPending;
    private static ScheduledFuture retryTask;
    private final Context ctx;
    private final boolean deleteFile;
    private final File downloadFile;
    private final String errorTitle;
    private final BotaSettings settings;
    private final CusSM sm;
    private final String version;

    public ResetSMToGettingDescriptor(Context context, int i, String str, String str2, File file, boolean z, BotaSettings botaSettings, CusSM cusSM) {
        this.ctx = context;
        this.errorTitle = str;
        this.version = str2;
        this.downloadFile = file;
        this.deleteFile = z;
        this.settings = botaSettings;
        this.sm = cusSM;
    }

    public boolean shouldRetry(final String str, final boolean z, String str2) {
        if (this.deleteFile) {
            this.downloadFile.delete();
        }
        this.settings.incrementPrefs(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
        int i = this.settings.getInt(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS, 0);
        if (i > this.settings.getInt(Configs.MAX_RETRY_COUNT_DL, 9)) {
            mRetryPending = false;
            this.sm.failDownload(this.version, UpgradeUtils.DownloadStatus.STATUS_FAIL, "Aborting OTA process as the retry for " + str + "(" + this.errorTitle + ") have maxed out , retryCount : " + i, str2);
            return false;
        }
        mRetryPending = true;
        IncrementalBackoffValueProvider incrementalBackoffValueProvider = new IncrementalBackoffValueProvider(this.settings.getString(Configs.BACKOFF_VALUES));
        long j = 0;
        for (int i2 = 0; i2 < i; i2++) {
            j = incrementalBackoffValueProvider.getNextTimeoutValue();
        }
        retryTask = OtaApplication.getScheduledExecutorService().schedule(new Runnable() { // from class: com.motorola.ccc.ota.sources.bota.download.error.ResetSMToGettingDescriptor.1
            @Override // java.lang.Runnable
            public void run() {
                ResetSMToGettingDescriptor.mRetryPending = false;
                ResetSMToGettingDescriptor.this.settings.setString(Configs.OTA_GET_DESCRIPTOR_REASON, ResetSMToGettingDescriptor.this.errorTitle);
                CusAndroidUtils.sendGetDescriptor(ResetSMToGettingDescriptor.this.ctx, ResetSMToGettingDescriptor.this.version, "encountered errorCode " + str + " go and fetch new download url", z);
            }
        }, j, incrementalBackoffValueProvider.getTimeUnit());
        Logger.debug("OtaApp", "4xx retry. RetryCount = " + i + " RetryInterval= " + j);
        return true;
    }

    public static void clearRetryTask() {
        Logger.debug("OtaApp", "ResetSMToGettingDescriptor.clearRetryTask() clearing pending retry task ");
        retryTask.cancel(false);
        mRetryPending = false;
    }

    public static boolean isRetryPending() {
        return mRetryPending;
    }
}
