package com.motorola.ccc.ota.stats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class StatsListener {
    private final ConnectivityManager cm;
    private final BotaSettings settings;
    private StatsBuilderBroadcastReceiver sbbr = null;
    private final Context context = OtaApplication.getGlobalContext();

    public StatsListener(ConnectivityManager connectivityManager, BotaSettings botaSettings) {
        this.cm = connectivityManager;
        this.settings = botaSettings;
    }

    public void startStatsListener() {
        if (this.sbbr == null) {
            this.sbbr = new StatsBuilderBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CusAndroidUtils.ACTION_GET_DESCRIPTOR);
            intentFilter.addAction(CusAndroidUtils.INTERNAL_NOTIFICATION);
            intentFilter.addAction(CusAndroidUtils.DOWNLOAD_ERROR_CODE);
            intentFilter.addAction(CusAndroidUtils.DOWNLOAD_EXCEPTION);
            intentFilter.addAction(UpgradeUtilConstants.UPGRADE_EXECUTE_UPGRADE);
            intentFilter.addAction(UpgradeUtilConstants.ACTION_AB_APPLY_PAYLOAD_STARTED_INTENT);
            intentFilter.addAction(UpgradeUtilConstants.AB_UPGRADE_COMPLETED_INTENT);
            intentFilter.addAction(CusAndroidUtils.REBOOT_DURING_DOWNLOAD);
            BroadcastUtils.registerLocalReceiver(this.context, this.sbbr, intentFilter);
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter2.addAction(CusAndroidUtils.START_DOWNLOAD_NOTIFICATION);
            intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_LAUNCH_UPGRADE);
            intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_RESPONSE);
            intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_STATUS);
            intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION);
            intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS);
            intentFilter2.addAction(UpgradeUtilConstants.ACTION_OTA_DEFERRED);
            this.context.registerReceiver(this.sbbr, intentFilter2, Permissions.INTERACT_OTA_SERVICE, null, 2);
            IntentFilter intentFilter3 = new IntentFilter();
            intentFilter3.addAction(UpgradeUtilConstants.ACTION_UPGRADE_UPDATE_STATUS);
            this.context.registerReceiver(this.sbbr, intentFilter3, 4);
        }
    }

    public void stopListener() {
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        StatsBuilderBroadcastReceiver statsBuilderBroadcastReceiver = this.sbbr;
        if (statsBuilderBroadcastReceiver != null) {
            BroadcastUtils.unregisterLocalReceiver(this.context, statsBuilderBroadcastReceiver);
            this.context.unregisterReceiver(this.sbbr);
            this.sbbr = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class StatsBuilderBroadcastReceiver extends BroadcastReceiver {
        private StatsBuilderBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(final Context context, final Intent intent) {
            OtaApplication.getExecutorService().submit(new Runnable() { // from class: com.motorola.ccc.ota.stats.StatsListener.StatsBuilderBroadcastReceiver.1
                @Override // java.lang.Runnable
                public void run() {
                    StatsBuilderBroadcastReceiver.this.handleIntent(context, intent);
                }
            });
        }

        public void handleIntent(Context context, Intent intent) {
            Logger.debug("OtaApp", "Receive intent (stats): " + intent.getAction());
            if (intent.getAction().equals(UpgradeUtilConstants.ACTION_UPGRADE_UPDATE_STATUS)) {
                String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_CURRENT_STATE);
                String stringExtra2 = intent.getStringExtra(UpgradeUtilConstants.KEY_SOURCE_SHA1);
                String stringExtra3 = intent.getStringExtra(UpgradeUtilConstants.KEY_DESTINAION_SHA1);
                if (SystemUpdateStatusUtils.NOTIFIED.equals(stringExtra)) {
                    StatsHelper.setPackageNotifiedTime(StatsListener.this.settings, stringExtra2, stringExtra3);
                }
            }
            if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION)) {
                StatsHelper.setDownloadNotifiedTime(StatsListener.this.settings);
            }
            if (intent.getAction().equals(UpgradeUtilConstants.ACTION_OTA_DEFERRED)) {
                String statsType = UpdaterUtils.getStatsType(intent);
                long deferTime = UpdaterUtils.getDeferTime(intent);
                boolean isInstallAutomatically = UpdaterUtils.isInstallAutomatically(intent);
                if (statsType.equals(UpgradeUtilConstants.DOWNLOAD)) {
                    StatsHelper.setDownloadDeferStats(StatsListener.this.settings, deferTime);
                } else if (statsType.equals(UpgradeUtilConstants.INSTALL) || statsType.equals(UpgradeUtilConstants.RESTART)) {
                    StatsHelper.setInstallDeferStats(StatsListener.this.settings, isInstallAutomatically, deferTime);
                }
            }
            if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_RESPONSE) && UpgradeUtilMethods.responseActionFromIntent(intent)) {
                StatsHelper.setDownloadAcceptedTime(StatsListener.this.settings);
            }
            if (intent.getAction().equals(CusAndroidUtils.START_DOWNLOAD_NOTIFICATION)) {
                StatsHelper.setTimeAndIfaceStatsAtDownloadStart(StatsListener.this.settings);
            }
            if (intent.getAction().equals(CusAndroidUtils.ACTION_GET_DESCRIPTOR)) {
                StatsHelper.setDDObtainedCount(StatsListener.this.settings);
            }
            if (CusAndroidUtils.REBOOT_DURING_DOWNLOAD.equals(intent.getAction())) {
                StatsHelper.setRebootDurindDownloadCount(StatsListener.this.settings);
            }
            if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS)) {
                UpgradeUtils.DownloadStatus downloadStatusFromIntent = UpgradeUtilMethods.downloadStatusFromIntent(intent);
                if (downloadStatusFromIntent == UpgradeUtils.DownloadStatus.STATUS_OK) {
                    return;
                }
                if (downloadStatusFromIntent == UpgradeUtils.DownloadStatus.STATUS_TEMP_OK) {
                    StatsDownload.downloadingOrStopped(intent, StatsListener.this.settings, StatsListener.this.cm, context);
                    return;
                } else if (downloadStatusFromIntent == UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL) {
                    StatsHelper.setInstallEndTime(StatsListener.this.settings, System.currentTimeMillis());
                    StatsHelper.setTotalInstallTime(StatsListener.this.settings);
                    return;
                } else if (downloadStatusFromIntent != UpgradeUtils.DownloadStatus.STATUS_TEMP_OK) {
                    StatsHelper.setAndBuildDownloadStats(StatsListener.this.settings, context, StatsListener.this.cm);
                    return;
                }
            }
            if (intent.getAction().equals(CusAndroidUtils.DOWNLOAD_ERROR_CODE)) {
                StatsDownload.downloadErrorcode(StatsListener.this.settings, CusAndroidUtils.errorcodeFromIntent(intent));
            }
            if (intent.getAction().equals(CusAndroidUtils.DOWNLOAD_EXCEPTION)) {
                StatsDownload.downloadException(StatsListener.this.settings, CusAndroidUtils.exceptionFromIntent(intent));
            }
            if (intent.getAction().equals(CusAndroidUtils.INTERNAL_NOTIFICATION)) {
                StatsHelper.setInstallNotifiedTime(StatsListener.this.settings);
                if (BuildPropReader.isUEUpdateEnabled()) {
                    StatsHelper.setInstallAcceptedTime(StatsListener.this.settings);
                    StatsHelper.setInstallStartTime(StatsListener.this.settings);
                }
            }
            if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_LAUNCH_UPGRADE) && UpgradeUtilMethods.proceedFromIntent(intent)) {
                if (BuildPropReader.isUEUpdateEnabled()) {
                    return;
                }
                StatsHelper.setInstallAcceptedTime(StatsListener.this.settings);
                return;
            }
            intent.getAction().equals(UpgradeUtilConstants.UPGRADE_EXECUTE_UPGRADE);
            if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_STATUS) && !BuildPropReader.isUEUpdateEnabled()) {
                StatsHelper.setInstallEndTime(StatsListener.this.settings, System.currentTimeMillis() - SystemClock.elapsedRealtime());
                StatsHelper.setTotalInstallTimeForClassic(StatsListener.this.settings);
            }
            if (intent.getAction().equals(UpgradeUtilConstants.ACTION_AB_APPLY_PAYLOAD_STARTED_INTENT)) {
                StatsListener.this.settings.setLong(Configs.STATS_AB_LAST_INSTALLATION_START_TIME, System.currentTimeMillis());
            }
            if (intent.getAction().equals(UpgradeUtilConstants.AB_UPGRADE_COMPLETED_INTENT)) {
                StatsHelper.setTotalInstallTime(StatsListener.this.settings);
                StatsHelper.setInstallEndTime(StatsListener.this.settings, System.currentTimeMillis());
            }
        }
    }
}
