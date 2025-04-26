package com.motorola.otalib.main.Downloader;

import android.content.Context;
import com.motorola.otalib.common.backoff.IncrementalBackoffValueProvider;
import com.motorola.otalib.common.utils.UpgradeUtils;
import com.motorola.otalib.main.LibCussm;
import com.motorola.otalib.main.Logger;
import com.motorola.otalib.main.OtaLibService;
import com.motorola.otalib.main.Settings.LibConfigs;
import com.motorola.otalib.main.Settings.LibSettings;
import java.util.concurrent.ScheduledFuture;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ResetSMToGettingDescriptorExceptionHandler {
    private static boolean mRetryPending;
    private static ScheduledFuture retryTask;
    private final Context ctx;
    private final LibSettings mSettings;
    private final LibCussm mSm;
    private final int maxRetryCount;
    private final String primaryKey;
    private int retryCount;

    public ResetSMToGettingDescriptorExceptionHandler(Context context, String str, LibSettings libSettings, LibCussm libCussm) {
        this.ctx = context;
        this.primaryKey = str;
        this.maxRetryCount = libSettings.getInt(LibConfigs.MAX_RETRY_COUNT_DL, 3);
        this.mSettings = libSettings;
        this.mSm = libCussm;
    }

    public boolean handleException(String str, final String str2, final boolean z, String str3) {
        if (str.contains("IOException")) {
            this.mSettings.incrementPrefs(LibConfigs.OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
            final int i = this.mSettings.getInt(LibConfigs.OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS, 0);
            if (i > this.maxRetryCount) {
                String str4 = "ResetSMToGettingDescriptorExceptionHandler.handleException,  Aborting the OTA process as exception (" + str2 + ") retries have maxed out,for retryCount : " + i;
                Logger.info(Logger.OTALib_TAG, str4);
                mRetryPending = false;
                this.mSm.failProgress(this.ctx, this.primaryKey, UpgradeUtils.DownloadStatus.STATUS_FAIL, str4, str3);
                return false;
            }
            mRetryPending = true;
            IncrementalBackoffValueProvider incrementalBackoffValueProvider = new IncrementalBackoffValueProvider(this.mSettings.getString(LibConfigs.BACKOFF_VALUES));
            long j = 0;
            for (int i2 = 0; i2 < i; i2++) {
                j = incrementalBackoffValueProvider.getNextTimeoutValue();
            }
            retryTask = OtaLibService.getScheduledExecutorService().schedule(new Runnable() { // from class: com.motorola.otalib.main.Downloader.ResetSMToGettingDescriptorExceptionHandler.1
                @Override // java.lang.Runnable
                public void run() {
                    if (i > 0) {
                        String str5 = "ResetSMToGettingDescriptorExceptionHandler.handleException, encountered exception " + str2 + " go and fetch new download url";
                        Logger.info(Logger.OTALib_TAG, str5);
                        boolean unused = ResetSMToGettingDescriptorExceptionHandler.mRetryPending = false;
                        ResetSMToGettingDescriptorExceptionHandler.this.mSm.onActionGetDescriptor(ResetSMToGettingDescriptorExceptionHandler.this.ctx, ResetSMToGettingDescriptorExceptionHandler.this.primaryKey, z, str5);
                        return;
                    }
                    Logger.info(Logger.OTALib_TAG, "ResetSMToGettingDescriptorExceptionHandler.handleException, encountered exception " + str2 + " but retryCount " + i + "so drop this to floor");
                }
            }, j, incrementalBackoffValueProvider.getTimeUnit());
            Logger.debug(Logger.OTALib_TAG, "exception retry. RetryCount = " + i + " RetryInterval= " + j);
            return true;
        }
        return false;
    }

    public static void clearRetryTask() {
        Logger.debug(Logger.OTALib_TAG, "ResetSMToGettingDescriptorExceptionHandler.clearRetryTask() clearing pending retry task ");
        retryTask.cancel(false);
        mRetryPending = false;
    }

    public static boolean isRetryPending() {
        return mRetryPending;
    }
}
