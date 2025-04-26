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
import java.util.concurrent.ScheduledFuture;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class ResetSMToGettingDescriptorExceptionHandler {
    private static boolean mRetryPending;
    private static ScheduledFuture retryTask;
    private final Context ctx;
    private final BotaSettings mSettings;
    private final CusSM mSm;
    private final int maxRetryCount;
    private int retryCount;
    private final String version;

    public ResetSMToGettingDescriptorExceptionHandler(Context context, String str, BotaSettings botaSettings, CusSM cusSM) {
        this.ctx = context;
        this.version = str;
        this.maxRetryCount = botaSettings.getInt(Configs.MAX_RETRY_COUNT_DL, 9);
        this.mSettings = botaSettings;
        this.mSm = cusSM;
    }

    public boolean handleException(final String str, final String str2, final boolean z, String str3) {
        if (str.contains("IOException")) {
            this.mSettings.incrementPrefs(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
            final int i = this.mSettings.getInt(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS, 0);
            if (i > this.maxRetryCount) {
                String str4 = "ResetSMToGettingDescriptorExceptionHandler.handleException,  Aborting the OTA process as exception (" + str2 + ") retries have maxed out,for retryCount : " + i;
                Logger.info("OtaApp", str4);
                mRetryPending = false;
                this.mSm.failDownload(this.version, UpgradeUtils.DownloadStatus.STATUS_FAIL, str4, str3);
                return false;
            }
            mRetryPending = true;
            IncrementalBackoffValueProvider incrementalBackoffValueProvider = new IncrementalBackoffValueProvider(this.mSettings.getString(Configs.BACKOFF_VALUES));
            long j = 0;
            for (int i2 = 0; i2 < i; i2++) {
                j = incrementalBackoffValueProvider.getNextTimeoutValue();
            }
            retryTask = OtaApplication.getScheduledExecutorService().schedule(new Runnable() { // from class: com.motorola.ccc.ota.sources.bota.download.error.ResetSMToGettingDescriptorExceptionHandler.1
                @Override // java.lang.Runnable
                public void run() {
                    if (i > 0) {
                        String str5 = "ResetSMToGettingDescriptorExceptionHandler.handleException, encountered exception " + str2 + " go and fetch new download url";
                        Logger.info("OtaApp", str5);
                        ResetSMToGettingDescriptorExceptionHandler.mRetryPending = false;
                        ResetSMToGettingDescriptorExceptionHandler.this.mSettings.setString(Configs.OTA_GET_DESCRIPTOR_REASON, str);
                        CusAndroidUtils.sendGetDescriptor(ResetSMToGettingDescriptorExceptionHandler.this.ctx, ResetSMToGettingDescriptorExceptionHandler.this.version, str5, z);
                        return;
                    }
                    Logger.info("OtaApp", "ResetSMToGettingDescriptorExceptionHandler.handleException, encountered exception " + str2 + " but retryCount " + i + "so drop this to floor");
                }
            }, j, incrementalBackoffValueProvider.getTimeUnit());
            Logger.debug("OtaApp", "exception retry. RetryCount = " + i + " RetryInterval= " + j);
            return true;
        }
        return false;
    }

    public static void clearRetryTask() {
        Logger.debug("OtaApp", "ResetSMToGettingDescriptorExceptionHandler.clearRetryTask() clearing pending retry task ");
        retryTask.cancel(false);
        mRetryPending = false;
    }

    public static boolean isRetryPending() {
        return mRetryPending;
    }
}
