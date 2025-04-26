package com.motorola.ccc.ota.ui;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.CusUtilMethods;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.MetadataOverrider;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.metaData.CheckForUpgradeTriggeredBy;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpdateReceiver extends BroadcastReceiver {
    private AlarmManager mAlarmManager;
    private String mNotificationTitle;
    private String mNotificationTxt;
    private PendingIntent mPendingIntent;
    private BotaSettings botaSettings = new BotaSettings();
    private SystemUpdaterPolicy systemUpdaterPolicy = new SystemUpdaterPolicy();

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum PendingIntentType {
        ACTIVITY,
        SERVICE,
        BROADCAST
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (UpgradeUtilMethods.isSystemUser(context)) {
            String action = intent.getAction();
            Logger.debug("OtaApp", "Receive intent: " + action);
            intent.setFlags(805306368);
            if (UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION.equals(action)) {
                if (doSanityCheckForFullScreen(context, NotificationUtils.KEY_DOWNLOAD, intent)) {
                    intent.setClass(context, BaseActivity.class);
                    intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.DOWNLOAD_FRAGMENT.toString());
                    context.startActivity(intent);
                }
            } else if (UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS.equals(action)) {
                DownloadNotification downloadNotification = new DownloadNotification(context);
                UpgradeUtils.DownloadStatus downloadStatusFromIntent = UpgradeUtilMethods.downloadStatusFromIntent(intent);
                if (downloadStatusFromIntent == UpgradeUtils.DownloadStatus.STATUS_OK) {
                    UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent);
                    if (upgradeInfoDuringOTAUpdate == null) {
                        Logger.debug("OtaApp", "metadata is empty return");
                        return;
                    }
                    this.botaSettings.setBoolean(Configs.DOWNLOAD_COMPLETED, true);
                    if (!shouldIForceTheInstallation(upgradeInfoDuringOTAUpdate, context, intent, NotificationUtils.KEY_INSTALL) && doSanityCheckForFullScreen(context, NotificationUtils.KEY_INSTALL, intent)) {
                        NotificationUtils.cancelOtaNotification();
                        intent.setClass(context, BaseActivity.class);
                        intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.INSTALL_FRAGMENT.toString());
                        context.startActivity(intent);
                    }
                } else if (downloadStatusFromIntent == UpgradeUtils.DownloadStatus.STATUS_TEMP_OK) {
                    MetaData from = MetaDataBuilder.from(this.botaSettings.getString(Configs.METADATA));
                    if (from == null) {
                        Logger.debug("OtaApp", "metadata is empty return");
                        return;
                    }
                    int deferredFromIntent = UpgradeUtilMethods.deferredFromIntent(intent);
                    if (!UpdaterUtils.showDownloadProgress()) {
                        if (from.getForceDownloadTime() >= 0.0d) {
                            if (!UpdaterUtils.checkForFinalDeferTimeForForceUpdate()) {
                                return;
                            }
                        } else if (!BuildPropReader.isBotaATT() || deferredFromIntent != -1) {
                            return;
                        } else {
                            try {
                                UpgradeUtilMethods.sendActionMetadataOverride(MetaDataBuilder.toJSONString(MetadataOverrider.returnOverWrittenMetaData(from, "showDownloadProgress", true)));
                            } catch (JSONException unused) {
                                Logger.error("OtaApp", "UpdateReceiver:Exception on overriding metadata");
                            }
                        }
                    }
                    long receivedBytesFromIntent = UpgradeUtilMethods.receivedBytesFromIntent(intent);
                    long j = UpgradeUtilMethods.totalBytesFromIntent(intent);
                    String locationTypeFromIntent = UpgradeUtilMethods.locationTypeFromIntent(intent);
                    boolean downloadOnWifiFromIntent = UpgradeUtilMethods.downloadOnWifiFromIntent(intent);
                    boolean z = from.getForceDownloadTime() >= 0.0d;
                    if (this.botaSettings.getBoolean(Configs.DOWNLOAD_COMPLETED)) {
                        Logger.info("OtaApp", "UpdateReceiver.onReceive, ignore download progress intent after download complete");
                    } else {
                        downloadNotification.updateNotification(j, receivedBytesFromIntent, locationTypeFromIntent, deferredFromIntent, downloadOnWifiFromIntent, z, this.botaSettings);
                    }
                } else if (this.botaSettings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                    if (this.botaSettings.getBoolean(Configs.LAUNCHED_NEXT_SETUP_SCREEN_ON_SKIP_OR_STOP)) {
                        this.botaSettings.removeConfig(Configs.LAUNCHED_NEXT_SETUP_SCREEN_ON_SKIP_OR_STOP);
                    } else {
                        UpdaterUtils.launchNextSetupActivityVitalUpdate(context);
                    }
                    UpdaterUtils.stopBGInstallActivity(context);
                } else if (!UpdaterUtils.isDisplayErrorAllowed(intent)) {
                    NotificationUtils.cancelOtaNotification();
                } else if ((downloadStatusFromIntent == UpgradeUtils.DownloadStatus.STATUS_SPACE || downloadStatusFromIntent == UpgradeUtils.DownloadStatus.STATUS_SPACE_INSTALL || downloadStatusFromIntent == UpgradeUtils.DownloadStatus.STATUS_SPACE_BACKGROUND_INSTALL || downloadStatusFromIntent == UpgradeUtils.DownloadStatus.STATUS_SPACE_PAYLOAD_METADATA_CHECK || downloadStatusFromIntent == UpgradeUtils.DownloadStatus.STATUS_VAB_MAKE_SPACE_REQUEST_USER) && NotificationUtils.isNotificationServiceRunning(context)) {
                    Logger.debug("OtaApp", "persistent notification present in status bar");
                } else {
                    if (downloadStatusFromIntent != UpgradeUtils.DownloadStatus.STATUS_VAB_MAKE_SPACE_REQUEST_USER) {
                        UpdaterUtils.stopBGInstallActivity(context);
                    }
                    intent.setClass(context, MessageActivity.class);
                    context.startActivity(intent);
                    NotificationUtils.cancelOtaNotification();
                }
            } else if (UpdaterUtils.ACTION_MANUAL_CHECK_UPDATE.equals(action) || UpgradeUtilConstants.UPGRADE_UPDATER_STATE_CLEAR.equals(action)) {
                Logger.debug("OtaApp", "Clearing updater state.");
                if (UpdaterUtils.ACTION_MANUAL_CHECK_UPDATE.equals(action)) {
                    this.botaSettings.removeDontBotherPreferences();
                    this.botaSettings.setBoolean(Configs.DEVICE_REBOOTED, false);
                    UpdaterUtils.disablePowerDownReceiver();
                    this.botaSettings.setBoolean(Configs.WIFI_SETTINGS_DEFFERED, false);
                    this.botaSettings.removeConfig(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY);
                    NotificationUtils.clearNextPromptDetails(this.botaSettings);
                }
                if (UpgradeUtilConstants.UPGRADE_UPDATER_STATE_CLEAR.equals(action)) {
                    UpdaterUtils.stopDownloadProgressActivity(context);
                    if (!this.botaSettings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                        UpdaterUtils.stopBGInstallActivity(context);
                    }
                    NotificationUtils.cancelOtaNotification();
                    NotificationUtils.clearNextPromptDetails(this.botaSettings);
                    this.botaSettings.removeConfig(Configs.AUTO_UPDATE_TIME_SELECTED);
                    this.botaSettings.removeConfig(Configs.CHECKBOX_SELECTED);
                    cancelAnyPendingIntentSetPreviously(context, new Intent(context, NotificationService.class), PendingIntentType.SERVICE);
                    cancelAnyPendingIntentSetPreviously(context, new Intent(UpgradeUtilConstants.ACTIVITY_ANNOY_VALUE_EXPIRY), PendingIntentType.BROADCAST);
                    cancelAnyPendingIntentSetPreviously(context, new Intent(UpgradeUtilConstants.START_RESTART_ACTIVITY_INTENT).setClass(context, BaseActivity.class), PendingIntentType.ACTIVITY);
                }
            } else if (UpgradeUtilConstants.UPGRADE_UPDATE_STATUS.equals(action)) {
                Logger.debug("OtaApp", "Received Update Status");
                SystemUpdateStatusUtils.storeOngoingSystemUpdateStatus(context, intent);
                if (!UpdaterUtils.showPostInstallScreen(intent)) {
                    Logger.info("OtaApp", "showPostInstallScreen configured as false");
                    return;
                }
                Boolean valueOf = Boolean.valueOf(intent.getBooleanExtra(UpgradeUtilConstants.KEY_UPDATE_STATUS, false));
                String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_REASON);
                if (stringExtra != null && stringExtra.contains("cleanupAppliedPayload")) {
                    intent.putExtra(UpdaterUtils.KEY_MERGE_FAILURE, true);
                }
                if (BuildPropReader.isATT() && !valueOf.booleanValue() && !CheckForUpgradeTriggeredBy.user.toString().equals(this.botaSettings.getString(Configs.STATS_UPGRADE_SOURCE))) {
                    Logger.debug("OtaApp", "Don't show notification for DI or NI request for Fota failure case");
                } else if (!NotificationUtils.isUpdateNotificationChannelEnabled()) {
                    Logger.debug("OtaApp", "User has disabled the OTA notification channel. Show full screen activity");
                    if (valueOf.booleanValue()) {
                        intent.setClass(context, BaseActivity.class);
                        intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.UPDATE_COMPLETE_FRAGMENT.toString());
                        context.startActivity(intent);
                        return;
                    }
                    intent.setClass(context, BaseActivity.class);
                    intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.UPDATE_FAILED_FRAGMENT.toString());
                    context.startActivity(intent);
                } else {
                    NotificationUtils.displayUpdateStatusNotification(context, intent);
                }
            } else if (UpgradeUtilConstants.ACTION_UPGRADE_UPDATE_STATUS.equals(action) || UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE_RESPONSE.equals(action)) {
                SystemUpdateStatusUtils.storeOngoingSystemUpdateStatus(context, intent);
            } else if (UpgradeUtilConstants.START_DOWNLOAD_PROGRESS_FRAGMENT.equals(action)) {
                intent.setClass(context, BaseActivity.class);
                intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.DOWNLOAD_PROGRESS_FRAGMENT.toString());
                context.startActivity(intent);
            } else if (UpgradeUtilConstants.START_BACKGROUND_INSTALLATION_FRAGMENT.equals(action)) {
                intent.setClass(context, BaseActivity.class);
                intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.BACKGROUND_INSTALLATION_FRAGMENT.toString());
                context.startActivity(intent);
            } else if (UpgradeUtilConstants.UPGRADE_UPDATER_UPDATE_NOTIFICATION_CLEAR.equals(action)) {
                Logger.debug("OtaApp", "Received PDL screen cancel intent");
                NotificationUtils.stopNotificationService(context);
                UpdaterUtils.stopDownloadActivity(context);
                cancelAnyPendingIntentSetPreviously(context, new Intent(context, NotificationService.class), PendingIntentType.SERVICE);
                cancelAnyPendingIntentSetPreviously(context, new Intent(UpgradeUtilConstants.ACTIVITY_ANNOY_VALUE_EXPIRY), PendingIntentType.BROADCAST);
                NotificationUtils.clearNextPromptDetails(this.botaSettings);
            } else if (UpgradeUtilConstants.UPGRADE_UPDATER_DOWNLOAD_NOTIFICATION_CLEAR.equals(action)) {
                Logger.debug("OtaApp", "Received Download notification cancel intent");
                UpdaterUtils.stopDownloadProgressActivity(context);
                NotificationUtils.cancelOtaNotification();
                UpdaterUtils.stopMessageActivity(context);
                NotificationUtils.clearNextPromptDetails(this.botaSettings);
            } else if (UpgradeUtilConstants.UPGRADE_UPDATER_INSTALL_NOTIFICATION_CLEAR.equals(action)) {
                Logger.debug("OtaApp", "Received Install screen cancel intent");
                NotificationUtils.stopNotificationService(context);
                UpdaterUtils.stopInstallActivity(context);
                cancelAnyPendingIntentSetPreviously(context, new Intent(context, NotificationService.class), PendingIntentType.SERVICE);
                cancelAnyPendingIntentSetPreviously(context, new Intent(UpgradeUtilConstants.ACTIVITY_ANNOY_VALUE_EXPIRY), PendingIntentType.BROADCAST);
                NotificationUtils.clearNextPromptDetails(this.botaSettings);
                UpdaterUtils.stopMessageActivity(context);
            } else if (UpgradeUtilConstants.UPGRADE_UPDATER_BG_INSTALL_NOTIFICATION_CLEAR.equals(action)) {
                Logger.debug("OtaApp", "Received BGInstall screen cancel intent");
                if (!this.botaSettings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                    UpdaterUtils.stopBGInstallActivity(context);
                }
                NotificationUtils.cancelOtaNotification();
                UpdaterUtils.stopMessageActivity(context);
            } else if (UpgradeUtilConstants.UPGRADE_UPDATER_RESTART_NOTIFICATION_CLEAR.equals(action)) {
                Logger.debug("OtaApp", "Received Restart screen cancel intent");
                NotificationUtils.cancelOtaNotification();
                UpdaterUtils.stopMessageActivity(context);
                UpdaterUtils.stopRestartActivity(context);
                cancelAnyPendingIntentSetPreviously(context, new Intent(UpgradeUtilConstants.ACTIVITY_ANNOY_VALUE_EXPIRY), PendingIntentType.BROADCAST);
            } else if (UpdaterUtils.ACTION_USER_CANCELLED_DOWNLOAD.equals(action)) {
                Logger.debug("OtaApp", "Received user cancelled download notification cancel intent");
                showCancelPopupIntent(context, UpgradeUtils.DownloadStatus.STATUS_DOWNLOAD_CANCEL_NOTIFICATION);
                closeSystemDialogs(context);
            } else if (UpdaterUtils.ACTION_USER_CANCELLED_BACKGROUND_INSTALL.equals(action)) {
                Logger.debug("OtaApp", "Received user cancelled streaming update notification intent");
                showCancelPopupIntent(context, UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL_NOTIFICATION);
                closeSystemDialogs(context);
            } else if (UpdaterUtils.ACTION_USER_RESUME_DOWNLOAD_ON_CELLULAR.equals(action)) {
                Logger.debug("OtaApp", "User resumed download on cellular");
                Toast.makeText(context, context.getResources().getString(R.string.additional_charges_message), 1).show();
                UpgradeUtilMethods.sendDownloadNotificationResponse(context, UpgradeUtils.DownloadStatus.STATUS_RESUME_ON_CELLULAR);
                closeSystemDialogs(context);
                CusUtilMethods.showPopupToOptCellularDataAtt(context);
            } else if (UpdaterUtils.ACTION_USER_RESUME_STREAMING_DOWNLOAD_ON_CELLULAR.equals(action)) {
                Logger.debug("OtaApp", "User resumed streaming download on cellular");
                Toast.makeText(context, context.getResources().getString(R.string.additional_charges_message), 1).show();
                UpgradeUtilMethods.sendUserResponseDuringBackgroundInstallation(context, UpgradeUtils.DownloadStatus.STATUS_RESUME_ON_CELLULAR);
                closeSystemDialogs(context);
                CusUtilMethods.showPopupToOptCellularDataAtt(context);
            } else if (UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_AVAILABLE.equals(action)) {
                UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate2 = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent);
                if (upgradeInfoDuringOTAUpdate2 == null) {
                    Logger.debug("OtaApp", "metadata is empty return");
                    return;
                }
                UpdateType.UpdateTypeInterface updateType = UpdateType.getUpdateType(upgradeInfoDuringOTAUpdate2.getUpdateTypeData());
                this.mNotificationTitle = updateType.getPDLNotificationTitle();
                String systemUpdateAvailableNotificationText = updateType.getSystemUpdateAvailableNotificationText();
                this.mNotificationTxt = systemUpdateAvailableNotificationText;
                NotificationUtils.startNotificationService(context, NotificationUtils.fillSystemUpdateNotificationDetails(context, this.mNotificationTitle, systemUpdateAvailableNotificationText, this.botaSettings.getString(Configs.METADATA)));
            } else if (UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE.equals(action)) {
                this.botaSettings.setBoolean(Configs.DOWNLOAD_COMPLETED, true);
                UpdaterUtils.stopDownloadProgressActivity(context);
                UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate3 = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent);
                String stringExtra2 = intent.getStringExtra(UpgradeUtilConstants.KEY_VERSION);
                if (upgradeInfoDuringOTAUpdate3 == null) {
                    Logger.debug("OtaApp", "metadata is empty return");
                } else if (shouldIForceTheInstallation(upgradeInfoDuringOTAUpdate3, context, intent, NotificationUtils.KEY_INSTALL)) {
                } else {
                    if (UpdaterUtils.isCriticalUpdateTimerExpired(upgradeInfoDuringOTAUpdate3) && CusUtilMethods.isItFirstNetOnFota(context)) {
                        this.botaSettings.setBoolean(Configs.IS_UPDATE_PENDING_ON_REBOOT, true);
                        UpdaterUtils.notifyRecoveryAboutPendingUpdate(true);
                        showFirstNetPendingRebootNotification(upgradeInfoDuringOTAUpdate3, context, stringExtra2);
                        return;
                    }
                    String updateTypeData = upgradeInfoDuringOTAUpdate3.getUpdateTypeData();
                    this.mNotificationTitle = UpdateType.getUpdateType(updateTypeData).getInstallationTitle();
                    this.mNotificationTxt = NotificationUtils.getInstallNotificationText(UpdateType.getUpdateType(updateTypeData), upgradeInfoDuringOTAUpdate3);
                    NotificationUtils.startNotificationService(context, NotificationUtils.fillInstallSystemUpdateNotificationDetails(context, this.mNotificationTitle, this.mNotificationTxt, 1 + ((int) TimeUnit.MILLISECONDS.toMinutes(UpdaterUtils.getNextPromptForNotification(NotificationUtils.KEY_INSTALL, upgradeInfoDuringOTAUpdate3) - System.currentTimeMillis())), stringExtra2, this.botaSettings.getString(Configs.METADATA)));
                }
            } else if (UpdaterUtils.ACTION_USER_DEFERERD_WIFI_SETUP.equals(action)) {
                new NotificationHandler(context).cancel();
                int delay = UpdaterUtils.getDelay();
                Logger.info("OtaApp", "Received user deferred wifi setup,next notify is scheduled after " + delay + " minutes");
                this.botaSettings.setBoolean(Configs.WIFI_SETTINGS_DEFFERED, true);
                this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
                Intent intent2 = new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE);
                intent2.putExtra(UpdaterUtils.WHOM, UpdaterUtils.ACTION_USER_DEFERERD_WIFI_SETUP);
                PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, intent2, 67108864);
                this.mPendingIntent = broadcast;
                this.mAlarmManager.set(0, System.currentTimeMillis() + (delay * 60000), broadcast);
            } else if (UpgradeUtilConstants.UPGRADE_RESTART_NOTIFICATION.equals(action)) {
                NotificationHandler notificationHandler = new NotificationHandler(context);
                if (!NotificationUtils.isUpdateNotificationChannelEnabled()) {
                    notificationHandler.cancel();
                }
                UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate4 = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent);
                if (shouldIForceTheInstallation(upgradeInfoDuringOTAUpdate4, context, intent, NotificationUtils.KEY_RESTART)) {
                    UpdaterUtils.stopBGInstallActivity(context);
                } else {
                    NotificationUtils.startNotificationService(context, NotificationUtils.fillRestartSystemNotificationDetails(context, intent.getStringExtra(UpgradeUtilConstants.KEY_VERSION), ((int) TimeUnit.MILLISECONDS.toMinutes(UpdaterUtils.getNextPromptForNotification(NotificationUtils.KEY_RESTART, upgradeInfoDuringOTAUpdate4) - System.currentTimeMillis())) + 1, this.botaSettings.getString(Configs.METADATA), upgradeInfoDuringOTAUpdate4));
                }
            } else if (UpgradeUtilConstants.START_RESTART_ACTIVITY_INTENT.equals(action)) {
                UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate5 = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent);
                if (upgradeInfoDuringOTAUpdate5 == null) {
                    return;
                }
                if (shouldIForceTheInstallation(upgradeInfoDuringOTAUpdate5, context, intent, NotificationUtils.KEY_RESTART)) {
                    UpdaterUtils.stopBGInstallActivity(context);
                } else if (doSanityCheckForFullScreen(context, NotificationUtils.KEY_RESTART, intent)) {
                    NotificationUtils.cancelOtaNotification();
                    intent.setClass(context, BaseActivity.class);
                    intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.RESTART_FRAGMENT.toString());
                    context.startActivity(intent);
                }
            } else if (UpgradeUtilConstants.START_MERGE_RESTART_ACTIVITY_INTENT.equals(action)) {
                if (doSanityCheckForMergeRestartFullScreen(context, intent)) {
                    NotificationUtils.cancelOtaNotification();
                    intent.setClass(context, BaseActivity.class);
                    intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.MERGE_RESTART_FRAGMENT.toString());
                    context.startActivity(intent);
                }
            } else if (UpgradeUtilConstants.ACTION_AB_UPDATE_PROGRESS.equals(action)) {
                MetaData from2 = MetaDataBuilder.from(this.botaSettings.getString(Configs.METADATA));
                if (from2 == null) {
                    Logger.debug("OtaApp", "empty metadata, return");
                    return;
                }
                InstallNotification installNotification = new InstallNotification(context);
                int intExtra = intent.getIntExtra(UpgradeUtilConstants.KEY_UPGRADE_STATUS, 0);
                float floatExtra = intent.getFloatExtra(UpgradeUtilConstants.KEY_PERCENTAGE, 0.0f);
                int intExtra2 = intent.getIntExtra(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED, 0);
                String stringExtra3 = intent.getStringExtra(UpgradeUtilConstants.KEY_INSTALLER);
                if (!UpdaterUtils.showDownloadProgress()) {
                    if (from2.getForceDownloadTime() >= 0.0d) {
                        if (!UpdaterUtils.checkForFinalDeferTimeForForceUpdate()) {
                            return;
                        }
                    } else if (!BuildPropReader.isBotaATT() || intExtra2 != -1) {
                        return;
                    } else {
                        try {
                            UpgradeUtilMethods.sendActionMetadataOverride(MetaDataBuilder.toJSONString(MetadataOverrider.returnOverWrittenMetaData(from2, "showDownloadProgress", true)));
                        } catch (JSONException unused2) {
                            Logger.error("OtaApp", "UpdateReceiver:Exception on overriding metadata");
                        }
                    }
                }
                installNotification.updateNotification(floatExtra, intExtra, intExtra2, stringExtra3, from2.getForceDownloadTime() >= 0.0d, this.botaSettings);
            } else if (UpgradeUtilConstants.ACTIVITY_ANNOY_VALUE_EXPIRY.equals(action)) {
                String stringExtra4 = intent.getStringExtra(UpgradeUtilConstants.KEY_NOTIFICATION_TYPE);
                Intent intent3 = (Intent) intent.getParcelableExtra(UpgradeUtilConstants.KEY_ANNOY_EXPIRY_TARGET_INTENT);
                intent3.setFlags(805306368);
                if (stringExtra4.equals(NotificationUtils.KEY_MERGE_RESTART) && doSanityCheckForMergeRestartFullScreen(context, intent)) {
                    context.startActivity(getAnnoyExpiryTargetIntent(context, intent3, stringExtra4));
                    return;
                }
                UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate6 = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent3);
                if (shouldIForceTheInstallation(upgradeInfoDuringOTAUpdate6, context, intent, stringExtra4)) {
                    return;
                }
                intent.putExtra(UpdaterUtils.KEY_CHECK_FOR_MAP, true);
                intent.putExtra(UpgradeUtilConstants.KEY_METADATA, this.botaSettings.getString(Configs.METADATA));
                intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, upgradeInfoDuringOTAUpdate6.getLocationType());
                if (doSanityCheckForFullScreen(context, stringExtra4, intent)) {
                    context.startActivity(getAnnoyExpiryTargetIntent(context, intent3, stringExtra4));
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelAnyPendingIntentSetPreviously(Context context, Intent intent, PendingIntentType pendingIntentType) {
        Logger.debug("OtaApp", "cancelAnyPendingIntentSetPreviously of type " + pendingIntentType.toString());
        int i = AnonymousClass3.$SwitchMap$com$motorola$ccc$ota$ui$UpdateReceiver$PendingIntentType[pendingIntentType.ordinal()];
        if (i == 1) {
            this.mPendingIntent = PendingIntent.getActivity(context, 0, intent, 335544320);
        } else if (i == 2) {
            this.mPendingIntent = PendingIntent.getService(context, 0, intent, 335544320);
        } else if (i != 3) {
        } else {
            this.mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 335544320);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.ui.UpdateReceiver$3  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$ccc$ota$ui$UpdateReceiver$PendingIntentType;

        static {
            int[] iArr = new int[PendingIntentType.values().length];
            $SwitchMap$com$motorola$ccc$ota$ui$UpdateReceiver$PendingIntentType = iArr;
            try {
                iArr[PendingIntentType.ACTIVITY.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$ui$UpdateReceiver$PendingIntentType[PendingIntentType.SERVICE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$ui$UpdateReceiver$PendingIntentType[PendingIntentType.BROADCAST.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    private boolean doSanityCheckForForceInstall(final Context context, final Intent intent, String str) {
        if (UpdaterUtils.isPromptAllowed(context, str)) {
            int batteryLevel = UpdaterUtils.getBatteryLevel(context);
            Logger.info("OtaApp", "UpdateReceiver: battery level " + batteryLevel);
            if (batteryLevel < UpdaterUtils.allowedBatteryLevel() && !NotificationUtils.KEY_RESTART.equals(str)) {
                this.botaSettings.setBoolean(Configs.BATTERY_LOW, true);
                UpdaterUtils.checkAndEnableBatteryStatusReceiver();
                UpdaterUtils.checkAndEnablePowerDownReceiver();
                return false;
            } else if (UpdaterUtils.isPriorityAppRunning(context)) {
                OtaApplication.getExecutorService().submit(new Runnable() { // from class: com.motorola.ccc.ota.ui.UpdateReceiver.1
                    @Override // java.lang.Runnable
                    public void run() {
                        UpdateReceiver.this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
                        UpdateReceiver.this.mPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 335544320);
                        UpdateReceiver.this.mAlarmManager.cancel(UpdateReceiver.this.mPendingIntent);
                        UpdateReceiver.this.mAlarmManager.setExactAndAllowWhileIdle(0, System.currentTimeMillis() + CusAndroidUtils.URL_EXPIRY_TIME, UpdateReceiver.this.mPendingIntent);
                    }
                });
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void closeSystemDialogs(Context context) {
        context.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    private void showCancelPopupIntent(Context context, UpgradeUtils.DownloadStatus downloadStatus) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.addFlags(268435456);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS, downloadStatus.toString());
        context.startActivity(intent);
    }

    private boolean doSanityCheckForFullScreen(final Context context, final String str, final Intent intent) {
        UpdateReceiver updateReceiver;
        final UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent);
        final String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_VERSION);
        final long currentTimeMillis = System.currentTimeMillis();
        if (UpdaterUtils.shouldBlockFullScreen()) {
            return false;
        }
        if (this.botaSettings.getBoolean(Configs.IS_UPDATE_PENDING_ON_REBOOT)) {
            if (!NotificationUtils.isNotificationServiceRunning(context)) {
                showFirstNetPendingRebootNotification(upgradeInfoDuringOTAUpdate, context, stringExtra);
            }
            return false;
        }
        long j = this.botaSettings.getLong(Configs.NOTIFICATION_NEXT_PROMPT, 0L);
        if (j > System.currentTimeMillis()) {
            if (!NotificationUtils.isNotificationServiceRunning(context)) {
                NotificationUtils.displayNotification(context, str, j, intent, upgradeInfoDuringOTAUpdate);
            }
            return false;
        } else if (UpdaterUtils.isPriorityAppRunning(context) && !this.botaSettings.getBoolean(Configs.NOTIFICATION_TAPPED) && intent.getBooleanExtra(UpdaterUtils.KEY_CHECK_FOR_MAP, false)) {
            if (NotificationUtils.KEY_RESTART.equals(str) && SmartUpdateUtils.isSmartUpdateNearestToInstall(this.botaSettings, upgradeInfoDuringOTAUpdate.getUpdateTypeData())) {
                NotificationUtils.cancelOtaNotification();
            }
            UpdaterUtils.priorityAppRunningPostponeActivity(context, str, intent, false);
            return false;
        } else {
            this.botaSettings.removeConfig(Configs.NOTIFICATION_TAPPED);
            if (UpdaterUtils.isPromptAllowed(context, str)) {
                final long j2 = this.botaSettings.getLong(Configs.ACTIVITY_NEXT_PROMPT, 0L);
                Logger.debug("OtaApp", "dosanity : remaining (mins) " + TimeUnit.MILLISECONDS.toMinutes(j2 - currentTimeMillis));
                if (currentTimeMillis < j2) {
                    OtaApplication.getExecutorService().submit(new Runnable() { // from class: com.motorola.ccc.ota.ui.UpdateReceiver.2
                        @Override // java.lang.Runnable
                        public void run() {
                            UpdateReceiver.this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
                            if (SmartUpdateUtils.isSmartUpdateNearestToInstall(UpdateReceiver.this.botaSettings, upgradeInfoDuringOTAUpdate.getUpdateTypeData()) && (str.equals(NotificationUtils.KEY_RESTART) || str.equals(NotificationUtils.KEY_INSTALL))) {
                                UpdateReceiver.this.cancelAnyPendingIntentSetPreviously(context, new Intent(UpgradeUtilConstants.ACTIVITY_ANNOY_VALUE_EXPIRY), PendingIntentType.BROADCAST);
                                intent.setClass(context, BaseActivity.class);
                                if (str.equals(NotificationUtils.KEY_RESTART)) {
                                    intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.RESTART_FRAGMENT.toString());
                                }
                                if (str.equals(NotificationUtils.KEY_INSTALL)) {
                                    intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.INSTALL_FRAGMENT.toString());
                                }
                                intent.putExtra(UpgradeUtilConstants.KEY_METADATA, UpdateReceiver.this.botaSettings.getString(Configs.METADATA));
                                intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, upgradeInfoDuringOTAUpdate.getLocationType());
                                Intent fillAnnoyValueExpiryDetails = UpdaterUtils.fillAnnoyValueExpiryDetails(str, intent);
                                UpdateReceiver.this.mPendingIntent = PendingIntent.getBroadcast(context, 0, fillAnnoyValueExpiryDetails, 335544320);
                                UpdateReceiver.this.mAlarmManager.cancel(UpdateReceiver.this.mPendingIntent);
                                Logger.debug("OtaApp", "UpdateReceiver:smart update:reset alarm");
                                UpdateReceiver.this.mAlarmManager.setExactAndAllowWhileIdle(0, j2, UpdateReceiver.this.mPendingIntent);
                            } else if (UpdateReceiver.this.botaSettings.getBoolean(Configs.CHECKBOX_SELECTED)) {
                                Logger.debug("OtaApp", "UpdateReceiver.doSanityCheckForFullScreen: Put notification + alarm");
                                Context context2 = context;
                                PendingIntent service = PendingIntent.getService(context, 0, NotificationUtils.fillInstallLaterNotificationDetails(context2, context2.getResources().getString(R.string.auto_install_notification_text, DateFormatUtils.getCalendarString(context, UpdateReceiver.this.botaSettings.getLong(Configs.AUTO_UPDATE_TIME_SELECTED, -1L))), context.getResources().getString(R.string.install_time, Integer.toString(UpdaterUtils.getUpdateTime(upgradeInfoDuringOTAUpdate.getInstallTime()))), intent, Math.min(60, (int) TimeUnit.MILLISECONDS.toMinutes(j2 - currentTimeMillis)), stringExtra), 335544320);
                                UpdateReceiver.this.mAlarmManager.cancel(service);
                                UpdateReceiver.this.mAlarmManager.setExactAndAllowWhileIdle(0, j2 - UpgradeUtilConstants.ONE_HOUR, service);
                            } else {
                                Logger.debug("OtaApp", "UpdateReceiver.doSanityCheckForFullScreen: Put only alarm");
                                intent.putExtra(UpgradeUtilConstants.KEY_METADATA, UpdateReceiver.this.botaSettings.getString(Configs.METADATA));
                                String updateTypeData = upgradeInfoDuringOTAUpdate.getUpdateTypeData();
                                if (NotificationUtils.KEY_DOWNLOAD.equals(str)) {
                                    UpdateReceiver.this.mNotificationTitle = UpdateType.getUpdateType(updateTypeData).getPDLNotificationTitle();
                                    UpdateReceiver.this.mNotificationTxt = UpdateType.getUpdateType(updateTypeData).getSystemUpdateAvailableNotificationText();
                                    Intent fillDownloadLaterNotificationDetails = NotificationUtils.fillDownloadLaterNotificationDetails(context, UpdateReceiver.this.mNotificationTitle, UpdateReceiver.this.mNotificationTxt, intent);
                                    UpdateReceiver.this.mPendingIntent = PendingIntent.getService(context, 0, fillDownloadLaterNotificationDetails, 335544320);
                                    UpdateReceiver.this.mAlarmManager.cancel(UpdateReceiver.this.mPendingIntent);
                                    UpdateReceiver.this.mAlarmManager.setExactAndAllowWhileIdle(0, j2, UpdateReceiver.this.mPendingIntent);
                                } else if (NotificationUtils.KEY_INSTALL.equals(str)) {
                                    UpdateReceiver.this.mNotificationTitle = UpdateType.getUpdateType(updateTypeData).getInstallationTitle();
                                    UpdateReceiver.this.mNotificationTxt = NotificationUtils.getInstallNotificationText(UpdateType.getUpdateType(updateTypeData), upgradeInfoDuringOTAUpdate);
                                    Intent fillInstallLaterNotificationDetails = NotificationUtils.fillInstallLaterNotificationDetails(context, UpdateReceiver.this.mNotificationTitle, UpdateReceiver.this.mNotificationTxt, intent, NotificationUtils.getInstallLaterNotificationExpiryMins(upgradeInfoDuringOTAUpdate, j2 - System.currentTimeMillis()), stringExtra);
                                    UpdateReceiver.this.mPendingIntent = PendingIntent.getService(context, 0, fillInstallLaterNotificationDetails, 335544320);
                                    UpdateReceiver.this.mAlarmManager.cancel(UpdateReceiver.this.mPendingIntent);
                                    UpdateReceiver.this.mAlarmManager.setExactAndAllowWhileIdle(0, j2, UpdateReceiver.this.mPendingIntent);
                                } else if (NotificationUtils.KEY_RESTART.equals(str)) {
                                    Logger.debug("OtaApp", "OTA restarted at restart phase, so rescheduling alarm to display the notification after " + (((int) TimeUnit.MILLISECONDS.toMinutes(j2 - System.currentTimeMillis())) + 1) + " mins");
                                    Intent fillRestartSystemNotificationDetails = NotificationUtils.fillRestartSystemNotificationDetails(context, stringExtra, 1, intent, upgradeInfoDuringOTAUpdate);
                                    UpdateReceiver.this.mPendingIntent = PendingIntent.getService(context, 0, fillRestartSystemNotificationDetails, 335544320);
                                    UpdateReceiver.this.mAlarmManager.cancel(UpdateReceiver.this.mPendingIntent);
                                    UpdateReceiver.this.mAlarmManager.setExactAndAllowWhileIdle(0, j2, UpdateReceiver.this.mPendingIntent);
                                }
                            }
                        }
                    });
                    return false;
                }
                if (UpdaterUtils.shouldDisplayLowBatteryPopup() && !str.equals(NotificationUtils.KEY_RESTART)) {
                    int batteryLevel = UpdaterUtils.getBatteryLevel(context);
                    Logger.info("OtaApp", "UpdateReceiver: battery level " + batteryLevel);
                    if (batteryLevel < UpdaterUtils.allowedBatteryLevel()) {
                        this.botaSettings.setBoolean(Configs.BATTERY_LOW, true);
                        UpdaterUtils.checkAndEnableBatteryStatusReceiver();
                        UpdaterUtils.checkAndEnablePowerDownReceiver();
                        return false;
                    }
                }
                updateReceiver = this;
            } else {
                updateReceiver = this;
            }
            Logger.debug("OtaApp", "UpdateReceiver:Received true from doSanityCheckForFullScreen");
            updateReceiver.cancelAnyPendingIntentSetPreviously(context, new Intent(UpgradeUtilConstants.ACTIVITY_ANNOY_VALUE_EXPIRY), PendingIntentType.BROADCAST);
            return true;
        }
    }

    private boolean doSanityCheckForMergeRestartFullScreen(Context context, Intent intent) {
        Logger.error("OtaApp", "doSanityCheckForMergeRestartFullScreen:notifyservicerunning=" + NotificationUtils.hasNotification(context, NotificationUtils.OTA_NOTIFICATION_ID));
        if (NotificationUtils.hasNotification(context, NotificationUtils.OTA_NOTIFICATION_ID)) {
            return false;
        }
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
            if (activityManager.getAppTasks().size() >= 1) {
                if (BaseActivity.class.getName().equals(activityManager.getAppTasks().get(0).getTaskInfo().topActivity.getClassName())) {
                    return false;
                }
            }
        } catch (NullPointerException unused) {
        }
        if (!UpdaterUtils.isPriorityAppRunning(context)) {
            return UpdaterUtils.isPromptAllowed(context, NotificationUtils.KEY_MERGE_RESTART);
        }
        UpdaterUtils.priorityAppRunningPostponeActivity(context, NotificationUtils.KEY_MERGE_RESTART, intent, false);
        return false;
    }

    private Intent getAnnoyExpiryTargetIntent(Context context, Intent intent, String str) {
        if (str.equals(NotificationUtils.KEY_DOWNLOAD)) {
            return intent.setClass(context, BaseActivity.class).putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.DOWNLOAD_FRAGMENT.toString());
        }
        if (str.equals(NotificationUtils.KEY_RESTART)) {
            return intent.setClass(context, BaseActivity.class).putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.RESTART_FRAGMENT.toString());
        }
        if (str.equals(NotificationUtils.KEY_MERGE_RESTART)) {
            return intent.setClass(context, BaseActivity.class).putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.MERGE_RESTART_FRAGMENT.toString());
        }
        return intent.setClass(context, BaseActivity.class).putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.INSTALL_FRAGMENT.toString());
    }

    private boolean shouldIForceTheInstallation(UpdaterUtils.UpgradeInfo upgradeInfo, Context context, Intent intent, String str) {
        String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_VERSION);
        if (this.systemUpdaterPolicy.isSystemUpdatePolicyEnabled(upgradeInfo.getUpdateTypeData(), this.botaSettings)) {
            NotificationUtils.cancelOtaNotification();
            if (doSanityCheckForForceInstall(context, intent, str)) {
                this.systemUpdaterPolicy.handleSystemUpdatePolicy(context, intent, this.botaSettings);
            }
            return true;
        } else if (!upgradeInfo.showPreInstallScreen() && UpdaterUtils.sDeviceIdleModeRequired) {
            NotificationUtils.cancelOtaNotification();
            if (!UpdaterUtils.checkDozeMode(context)) {
                Logger.debug("OtaApp", "Waiting for doze mode.");
            } else if (doSanityCheckForForceInstall(context, intent, str)) {
                UpgradeUtilMethods.sendUpgradeLaunchProceed(context, stringExtra, true, "dozeModeInstall");
            }
            return true;
        } else {
            if (upgradeInfo.isForceInstallTimerExpired()) {
                if (!UpdaterUtils.checkDozeMode(context)) {
                    if (UpdaterUtils.isWaitForDozeModeOver()) {
                        Logger.debug("OtaApp", "UpdaterUtils.amIDoingSilentOta: Did not enter doze mode in 10 days, force installation now");
                    } else {
                        Logger.debug("OtaApp", "UpdaterUtils.amIDoingSilentOta: Waiting for doze mode.");
                    }
                    return false;
                } else if (doSanityCheckForForceInstall(context, intent, str)) {
                    NotificationUtils.cancelOtaNotification();
                    UpgradeUtilMethods.sendUpgradeLaunchProceed(context, stringExtra, true, "forceInstallInDozeMode");
                    return true;
                }
            }
            return false;
        }
    }

    private void showFirstNetPendingRebootNotification(UpdaterUtils.UpgradeInfo upgradeInfo, Context context, String str) {
        String string = context.getString(R.string.first_net_text, NumberFormat.getPercentInstance().format(UpdaterUtils.getminBatteryRequiredForInstall(upgradeInfo.getminBatteryRequiredForInstall()) / 100.0f));
        this.mNotificationTxt = string;
        NotificationUtils.startNotificationService(context, NotificationUtils.fillInstallSystemUpdateNotificationDetails(context, this.mNotificationTitle, string, -1, str));
    }
}
