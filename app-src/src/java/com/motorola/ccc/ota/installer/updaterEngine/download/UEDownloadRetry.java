package com.motorola.ccc.ota.installer.updaterEngine.download;

import android.content.Context;
import android.content.Intent;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.backoff.BackoffValueProvider;
import com.motorola.otalib.common.backoff.IncrementalBackoffValueProvider;
import com.motorola.otalib.common.errorCodes.UpdaterEngineErrorCodes;
import java.util.concurrent.ScheduledFuture;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class UEDownloadRetry {
    private static boolean mRetryPending;
    private static ScheduledFuture retryTask;
    private static BotaSettings settings;
    private BackoffValueProvider backOffProvider;
    private Context context;

    public UEDownloadRetry(Context context, BotaSettings botaSettings) {
        this.context = context;
        settings = botaSettings;
        this.backOffProvider = new IncrementalBackoffValueProvider(botaSettings.getString(Configs.BACKOFF_VALUES));
    }

    public boolean handleRetry(final int i, final String str) {
        final Boolean valueOf = Boolean.valueOf(UpdaterEngineErrorCodes.shouldWeFetchNewUrl(i));
        final String errorCodeDescription = UpdaterEngineErrorCodes.getErrorCodeDescription(i);
        int i2 = settings.getInt(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS, 0);
        Logger.debug("OtaApp", "handleRetry : " + i + " fetchNewUrl " + valueOf + " errorCount " + i2);
        if (i2 < settings.getInt(Configs.MAX_RETRY_COUNT_DL, 9)) {
            mRetryPending = true;
            long timeOutValue = this.backOffProvider.getTimeOutValue(i2);
            if (timeOutValue != -1) {
                retryTask = OtaApplication.getScheduledExecutorService().schedule(new Runnable() { // from class: com.motorola.ccc.ota.installer.updaterEngine.download.UEDownloadRetry.1
                    @Override // java.lang.Runnable
                    public void run() {
                        UEDownloadRetry.settings.incrementPrefs(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
                        UEDownloadRetry.settings.setString(Configs.OTA_GET_DESCRIPTOR_REASON, errorCodeDescription);
                        if (valueOf.booleanValue()) {
                            CusAndroidUtils.sendGetDescriptor(UEDownloadRetry.this.context, str, "encountered errorCode " + i + "during ABApplying go and fetch new download url", false);
                        } else {
                            OtaApplication.getGlobalContext().sendBroadcast(new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE), Permissions.INTERACT_OTA_SERVICE);
                        }
                        UEDownloadRetry.mRetryPending = false;
                    }
                }, timeOutValue, this.backOffProvider.getTimeUnit());
                Logger.debug("OtaApp", "a retry is scheduled after " + timeOutValue + SystemUpdateStatusUtils.SPACE + this.backOffProvider.getTimeUnit() + " retryCount is : " + i2);
                return true;
            }
            Logger.error("OtaApp", "invalid index access " + i2);
            return false;
        }
        Logger.debug("OtaApp", "retry count expired " + i2);
        return false;
    }

    public static void clearRetryTask() {
        if (isRetryPending()) {
            Logger.debug("OtaApp", "UEDownloadRetry.clearRetryTask() clearing pending retry task ");
            retryTask.cancel(false);
            mRetryPending = false;
        }
    }

    public static boolean isRetryPending() {
        return mRetryPending;
    }
}
