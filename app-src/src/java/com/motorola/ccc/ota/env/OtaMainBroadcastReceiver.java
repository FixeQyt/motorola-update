package com.motorola.ccc.ota.env;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtilConstants;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtils;
import com.motorola.ccc.ota.sources.modem.ModemPollingManager;
import com.motorola.ccc.ota.ui.CloudPickerActivity;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.CusUtilMethods;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.PMUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public final class OtaMainBroadcastReceiver extends BroadcastReceiver {
    private static final String DB_LOC = "cus.db";
    private static ApplicationEnv env;
    private static ForceUpgradeManager mForceUpgradeManager;
    private static OtaWiFiDiscoveryManager mOtaWiFiDiscoveryManager;
    private static BotaSettings settings;
    private static CusSM sm;

    @Override // android.content.BroadcastReceiver
    public void onReceive(final Context context, final Intent intent) {
        OtaApplication.getExecutorService().submit(new Runnable() { // from class: com.motorola.ccc.ota.env.OtaMainBroadcastReceiver.1
            @Override // java.lang.Runnable
            public void run() {
                if (UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE.equals(intent.getAction()) && OtaMainBroadcastReceiver.sm != null) {
                    Logger.debug("OtaApp", String.format("AndroidClientUpgradeService.handleIntent: %s : bootstrap %s interactive %s id (%s)", UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE, Boolean.valueOf(UpgradeUtilMethods.isBootstrap(intent)), Boolean.valueOf(UpgradeUtilMethods.isInteractive(intent)), Integer.valueOf(UpgradeUtilMethods.getRequestId(intent))));
                    OtaMainBroadcastReceiver.sm.onIntentCheckForUpdate(UpgradeUtilMethods.isBootstrap(intent), UpgradeUtilMethods.getRequestId(intent), UpgradeUtilMethods.isInteractive(intent));
                    return;
                }
                OtaMainBroadcastReceiver.this.handleIntent(context, intent);
            }
        });
    }

    public synchronized void handleIntent(Context context, Intent intent) {
        long j;
        Logger.debug("OtaApp", "AndroidClientUpgradeService.handleIntent: " + intent.getAction());
        if (sm == null) {
            Context globalContext = OtaApplication.getGlobalContext();
            settings = new BotaSettings();
            env = new AndroidENV(DB_LOC, globalContext, settings);
            mOtaWiFiDiscoveryManager = new OtaWiFiDiscoveryManager(globalContext);
            mForceUpgradeManager = new ForceUpgradeManager(globalContext);
            CusSM cusSM = new CusSM(env, (ConnectivityManager) globalContext.getSystemService("connectivity"), settings);
            sm = cusSM;
            cusSM.onStart();
        }
        if (intent.getAction().equals(UpgradeUtilConstants.OTA_START_ACTION)) {
            return;
        }
        if (intent.getAction().equals(UpgradeUtilConstants.OTA_STOP_ACTION)) {
            sm.onDestroy();
        }
        if (intent.getAction().equals(UpgradeUtilConstants.ACTION_STOP_OTA_SERVICE)) {
            sm.onIntentOtaServiceStop();
        }
        if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE)) {
            Logger.debug("OtaApp", String.format("AndroidClientUpgradeService.handleIntent: %s : bootstrap %s interactive %s id (%s)", UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE, Boolean.valueOf(UpgradeUtilMethods.isBootstrap(intent)), Boolean.valueOf(UpgradeUtilMethods.isInteractive(intent)), Integer.valueOf(UpgradeUtilMethods.getRequestId(intent))));
            sm.onIntentCheckForUpdate(UpgradeUtilMethods.isBootstrap(intent), UpgradeUtilMethods.getRequestId(intent), UpgradeUtilMethods.isInteractive(intent));
            return;
        }
        if (!ThinkShieldUtilConstants.ACTION_ASC_SESSION_DONE.equals(intent.getAction()) && !ThinkShieldUtilConstants.ACTION_ASC_OTA_INTERNAL_TIMEOUT.equals(intent.getAction())) {
            if (ThinkShieldUtilConstants.ACTION_ASC_SYSTEM_UPDATE_POLICY_CHANGED.equals(intent.getAction())) {
                ThinkShieldUtils.onSystemUpdatePolicyChanged(settings, sm.isBusy());
                return;
            } else if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_RESPONSE)) {
                sm.onIntentUpdateNotificationResponse(UpgradeUtilMethods.versionFromIntent(intent), UpgradeUtilMethods.responseActionFromIntent(intent), UpgradeUtilMethods.responseFlavourFromIntent(intent), intent.getStringExtra(UpgradeUtilConstants.KEY_DOWNLOAD_MODE));
                return;
            } else if (UpgradeUtilConstants.UPGRADE_UPDATE_NOTIFICATION_AVAILABLE_RESPONSE.equals(intent.getAction())) {
                boolean booleanExtra = intent.getBooleanExtra(UpgradeUtilConstants.KEY_DOWNLOAD_REQ_FROM_NOTIFY, false);
                String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_FULL_SCREEN_REMINDER);
                if (booleanExtra) {
                    settings.removeConfig(Configs.NOTIFICATION_NEXT_PROMPT);
                    settings.removeConfig(Configs.ACTIVITY_NEXT_PROMPT);
                    settings.setBoolean(Configs.DOWNLOAD_REQ_FROM_NOTIFY, booleanExtra);
                }
                sm.onIntentSystemUpdateNotificationResponse(stringExtra);
                return;
            } else if (UpgradeUtilConstants.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE_RESPONSE.equals(intent.getAction())) {
                sm.onIntentInstallSystemUpdateNotificationResponse(intent.getStringExtra(NotificationUtils.KEY_UPDATE), intent.getStringExtra(UpgradeUtilConstants.KEY_FULL_SCREEN_REMINDER));
                return;
            } else if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_DOWNLOAD_NOTIFICATION_RESPONSE)) {
                sm.onIntentDownloadNotificationResponse(UpgradeUtils.DownloadStatus.valueOf(intent.getStringExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS)));
                return;
            } else if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_BACKGROUND_INSTALL_CANCEL_RESPONSE)) {
                sm.onIntentBackgroundInstallCancelResponse();
                return;
            } else if (intent.getAction().equals(UpgradeUtilConstants.USER_BACKGROUND_INSTALL_RESPONSE)) {
                sm.onIntentBackgroundInstallResponse(UpgradeUtils.DownloadStatus.valueOf(intent.getStringExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS)));
                return;
            } else if ("android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(intent.getAction())) {
                sm.onIntentDeviceIdleModeChanged();
                return;
            } else if ("android.net.conn.RESTRICT_BACKGROUND_CHANGED".equals(intent.getAction())) {
                sm.onIntentDeviceDatasaverModeChanged();
                return;
            } else {
                if (UpgradeUtilConstants.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    sm.onActionBatteryChanged(intent.getBooleanExtra(UpdaterUtils.KEY_BATTERY_LOW, false));
                }
                if (intent.getAction().equals(UpgradeUtilConstants.RUN_STATE_MACHINE)) {
                    if (UpdaterUtils.ACTION_USER_DEFERERD_WIFI_SETUP.equals(intent.getStringExtra(UpdaterUtils.WHOM))) {
                        settings.setBoolean(Configs.WIFI_SETTINGS_DEFFERED, false);
                        settings.setInt(Configs.WIFI_PROMPT_COUNT_FOR_FOTA, settings.getInt(Configs.WIFI_PROMPT_COUNT_FOR_FOTA, 0) + 1);
                    }
                    sm.pleaseRunStateMachine();
                    return;
                } else if (intent.getAction().equals(UpgradeUtilConstants.ACTION_VERIFY_PAYLOAD_STATUS)) {
                    sm.onIntentVerifyPayloadStatus(intent.getStringExtra(UpgradeUtilConstants.KEY_REASON), intent.getStringExtra(UpgradeUtilConstants.KEY_DOWNLOAD_STATUS), intent.getStringExtra(UpgradeUtilConstants.KEY_UPGRADE_STATUS));
                    return;
                } else if (intent.getAction().equals(UpgradeUtilConstants.ACTION_VAB_VERIFY_STATUS)) {
                    sm.onIntentVABVerifyPayloadStatus(intent.getBooleanExtra(UpgradeUtilConstants.KEY_VAB_VALIDATION_STATUS, false));
                    return;
                } else if (intent.getAction().equals(UpgradeUtilConstants.ACTION_VAB_ALLOCATE_SPACE_RESULT)) {
                    sm.onIntentAllocateSpaceResult(intent.getLongExtra(UpgradeUtilConstants.KEY_ALLOCATE_SPACE_RESULT, 0L));
                    return;
                } else if (intent.getAction().equals(UpgradeUtilConstants.CREATE_RESERVE_SPACE_POST_FIFTEEN_MINUTES)) {
                    sm.createReserveSpace();
                    return;
                } else if (UpgradeUtilConstants.ACTION_VAB_CLEANUP_APLLIED_PAYLOAD.equals(intent.getAction())) {
                    sm.onIntentVABCleanupAppliedPayload(intent.getIntExtra(UpgradeUtilConstants.KEY_VAB_CLEANUP_APPLIED_PAYLOAD, -1));
                    return;
                } else if (intent.getAction().equals(UpgradeUtilConstants.UPGRADE_LAUNCH_UPGRADE)) {
                    sm.onIntentLaunchUpgrade(UpgradeUtilMethods.versionFromIntent(intent), UpgradeUtilMethods.proceedFromIntent(intent), intent.getBooleanExtra(UpgradeUtilConstants.KEY_CHECK_FOR_LOW_BATTERY, false), intent.getStringExtra(UpgradeUtilConstants.KEY_INSTALL_MODE));
                    return;
                } else {
                    if (!intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN") && !intent.getAction().equals("android.intent.action.REBOOT")) {
                        if (UpgradeUtilConstants.ACTION_SIM_STATE_CHANGE.equals(intent.getAction())) {
                            sm.onSimStateChanged(UpdaterUtils.isSimLoaded(intent));
                        }
                        if (intent.getAction().equals(PMUtils.POLLINGMGR_CONNECTIVITY)) {
                            if (CusAndroidUtils.isRadioUp(intent)) {
                                sm.onRadioUp();
                            } else {
                                sm.onRadioDown();
                            }
                        }
                        if (intent.getAction().equals(PMUtils.POLLINGMGR_ROAMING_CHANGE)) {
                            if (CusAndroidUtils.isRoaming(intent)) {
                                sm.onRoaming();
                            } else {
                                sm.onNotRoaming();
                            }
                            return;
                        } else if (intent.getAction().equals(CusAndroidUtils.INTERNAL_NOTIFICATION)) {
                            sm.onInternalNotification(CusAndroidUtils.versionFromIntent(intent), CusAndroidUtils.typeFromIntent(intent), CusAndroidUtils.resultFromIntent(intent));
                            return;
                        } else if (intent.getAction().equals(CusAndroidUtils.START_DOWNLOAD_NOTIFICATION)) {
                            sm.onStartDownloadNotification(CusAndroidUtils.versionFromIntent(intent));
                            return;
                        } else if (intent.getAction().equals(CusAndroidUtils.ACTION_CAPTIVE_PORTAL_LOGGED_IN)) {
                            Logger.debug("OtaApp", "Received ACTION_CAPTIVE_PORTAL_LOGGED_IN intent, result=" + intent.getIntExtra("result", -1));
                            sm.onRadioUp();
                            return;
                        } else if (intent.getAction().equals(CusAndroidUtils.ACTION_GET_OTA_RESERVED_SPACE)) {
                            sm.sendAvailableReserveSpace();
                            return;
                        } else if (intent.getAction().equals(CusAndroidUtils.ACTION_GET_DESCRIPTOR)) {
                            sm.onActionGetDescriptor(CusAndroidUtils.versionFromIntent(intent), CusAndroidUtils.reportStatusFromIntent(intent).booleanValue(), CusAndroidUtils.getReasonFromIntent(intent));
                            return;
                        } else if ("android.intent.action.LOCALE_CHANGED".equals(intent.getAction())) {
                            Logger.debug("OtaApp", "locale changed: Refresh Ota notification if available");
                            NotificationUtils.refreshOtaNotifications(context);
                            return;
                        } else {
                            if (intent.getAction().equals(AndroidPollingManager.INTENT_ACTION_POLLING_MANAGER)) {
                                Logger.debug("OtaApp", "CusPollingService interval expired, doing check for upgrade");
                                sm.onPollingExpiryNotification(false, 2, false);
                            }
                            if (intent.getAction().equals(ModemPollingManager.INTENT_ACTION_POLLING_MANAGER)) {
                                Logger.debug("OtaApp", "ModemPollingManager service interval expired, doing check for modem upgrade");
                                sm.onModemPollingExpiryNotification(false, false);
                            }
                            if (UpgradeUtilConstants.ACTION_MODEM_FSG_POLL.equals(intent.getAction())) {
                                Logger.debug("OtaApp", "Received com.motorola.modemservice.START_FSG_POLLING intent from modem app to trigger polling, so sending poll check request to check modem update");
                                settings.setInt(Configs.MODEM_POLLING_COUNT, 0);
                                settings.setLong(Configs.POLL_MODEM_AFTER, 86400000L);
                                settings.setInt(Configs.MAX_MODEM_POLLING_COUNT, 7);
                                UpdaterUtils.scheduleModemWorkManager(context);
                                sm.onModemPollingExpiryNotification(false, false);
                            }
                            if (UpgradeUtilConstants.ACTION_MODEM_UPDATE_STATUS.equals(intent.getAction())) {
                                String stringExtra2 = intent.getStringExtra(UpgradeUtilConstants.KEY_EXTRA_MODEM_UPDATE_STATUS_MSG);
                                int intExtra = intent.getIntExtra(UpgradeUtilConstants.KEY_EXTRA_MODEM_UPDATE_STATUS_CODE, -1);
                                Logger.debug("OtaApp", "Modem result status errorCode=" + intExtra + " : status msg = " + stringExtra2);
                                sm.onModemUpdateStatusResult(intExtra, stringExtra2);
                            }
                            if (intent.getAction().equals(UpgradeUtilConstants.INTENT_HEALTH_CHECK)) {
                                sm.timeForAHealthCheckUp();
                            }
                            if (UpgradeUtilConstants.ACTION_SMART_UPDATE_CONFIG_CHANGED.equals(intent.getAction())) {
                                sm.onIntentSmartUpdateConfigChanged(intent.getBooleanExtra(UpgradeUtilConstants.KEY_SMART_UPDATE_ENABLED, false));
                                return;
                            } else if (intent.getAction().equals(AndroidFotaInterface.ACTION_FOTA_REQUEST_UPDATE_RESPONSE)) {
                                sm.onIntentFotaRequestUpdateResponse(AndroidFotaInterface.getIdFromIntent(intent), AndroidFotaInterface.getErrorFromIntent(intent));
                                return;
                            } else if (intent.getAction().equals(AndroidFotaInterface.ACTION_FOTA_UPDATE_AVAILABLE)) {
                                long idFromIntent = AndroidFotaInterface.getIdFromIntent(intent);
                                long sizeFromIntent = AndroidFotaInterface.getSizeFromIntent(intent);
                                boolean booleanExtra2 = intent.getBooleanExtra(AndroidFotaInterface.EXTRA_IS_WIFI_ONLY, true);
                                if (sizeFromIntent == -1) {
                                    Logger.error("OtaApp", "AndroidClientUpgradeService.handleIntent " + intent.getAction() + " no size for action setting default");
                                    j = 1024;
                                } else {
                                    j = sizeFromIntent;
                                }
                                String dDDescriptionFromIntent = AndroidFotaInterface.getDDDescriptionFromIntent(intent);
                                boolean isForcedFromIntent = AndroidFotaInterface.getIsForcedFromIntent(intent);
                                String stringExtra3 = intent.getStringExtra(AndroidFotaInterface.EXTRA_UPDATE_TYPE);
                                Logger.debug("OtaApp", "Description: " + dDDescriptionFromIntent + " Forced: " + isForcedFromIntent + " Size: " + j + "Update type: " + stringExtra3);
                                sm.onIntentFotaUpdateAvailable(idFromIntent, j, dDDescriptionFromIntent, isForcedFromIntent, stringExtra3, booleanExtra2);
                                return;
                            } else {
                                if (intent.getAction().equals(AndroidFotaInterface.ACTION_FOTA_DOWNLOAD_MODE_CHANGED)) {
                                    long idFromIntent2 = AndroidFotaInterface.getIdFromIntent(intent);
                                    boolean booleanExtra3 = intent.getBooleanExtra(AndroidFotaInterface.EXTRA_IS_WIFI_ONLY, true);
                                    Logger.debug("OtaApp", "Download Mode changed. Wifi Only = " + booleanExtra3);
                                    if (BuildPropReader.isFotaATT()) {
                                        sm.onIntentFotaDownloadModeChanged(idFromIntent2, booleanExtra3);
                                    } else {
                                        sm.onIntentFotaDownloadModeChanged();
                                    }
                                }
                                if (intent.getAction().equals(UpgradeUtilConstants.MOVE_FOTA_TO_GETTING_DESCRIPTOR)) {
                                    sm.moveFotaToGettingDescriptorState();
                                    return;
                                } else if (intent.getAction().equals(AndroidFotaInterface.ACTION_FOTA_USER_ALERT_CELLULAR_OPT)) {
                                    Context globalContext2 = OtaApplication.getGlobalContext();
                                    globalContext2.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                                    CusUtilMethods.showPopupToOptCellularDataAtt(globalContext2);
                                    return;
                                } else if (intent.getAction().equals(AndroidFotaInterface.ACTION_FOTA_DOWNLOAD_COMPLETE)) {
                                    sm.onIntentFotaDownloadCompleted(AndroidFotaInterface.getIdFromIntent(intent), AndroidFotaInterface.getErrorFromIntent(intent), intent.getStringExtra(AndroidFotaInterface.EXTRA_DL_PACKAGE_PATH));
                                    return;
                                } else if (intent.getAction().equals(AndroidFotaInterface.ACTION_FOTA_SERVER_TRANSPORT_MEDIA)) {
                                    settings.setString(Configs.SERVER_FOTA_TRANSPORTMEDIA_VALUE, intent.getStringExtra(AndroidFotaInterface.EXTRA_SERVER_TRANSPORT_MEDIA));
                                    return;
                                } else if (UpgradeUtilConstants.REGISTER_WIFI_DISCOVER_MANAGER.equals(intent.getAction())) {
                                    mOtaWiFiDiscoveryManager.init(UpgradeUtilMethods.getDiscoverTime(intent), intent.getBooleanExtra(UpgradeUtilConstants.KEY_ONLY_ON_NETWORK, true), intent.getBooleanExtra(UpgradeUtilConstants.KEY_ALLOW_ON_ROAMING, false));
                                    return;
                                } else if (UpgradeUtilConstants.UNREGISTER_WIFI_DISCOVER_MANAGER.equals(intent.getAction())) {
                                    mOtaWiFiDiscoveryManager.onDestroy();
                                    return;
                                } else {
                                    if (!OtaWiFiDiscoveryManager.INTENT_ACTION_POLLING_MANAGER.equals(intent.getAction()) && !UpgradeUtilConstants.WIFI_DISCOVER_TIMER_EXPIRY.equals(intent.getAction())) {
                                        if (UpgradeUtilConstants.REGISTER_FORCE_UPGRADE_MANAGER.equals(intent.getAction())) {
                                            mForceUpgradeManager.init(UpgradeUtilMethods.getForceUpgradeTime(intent));
                                            return;
                                        } else if (UpgradeUtilConstants.UNREGISTER_FORCE_UPGRADE_MANAGER.equals(intent.getAction())) {
                                            mForceUpgradeManager.onDestroy();
                                            return;
                                        } else {
                                            if (!ForceUpgradeManager.INTENT_ACTION_POLLING_MANAGER.equals(intent.getAction()) && !UpgradeUtilConstants.FORCE_UPGRADE_TIMER_EXPIRY.equals(intent.getAction())) {
                                                if (CusAndroidUtils.SECRET_CODE.equals(intent.getAction())) {
                                                    Logger.debug("OtaApp", "Triggering a Polling Initiated Check for Update on keypress");
                                                    if (CusAndroidUtils.MODEM_SECRET_CODE_HOST.equals(intent.getData().getHost())) {
                                                        sm.onModemPollingExpiryNotification(false, false);
                                                    } else {
                                                        sm.onPollingExpiryNotification(false, 2, false);
                                                    }
                                                    return;
                                                } else if (CusAndroidUtils.INTENT_ACTION_CLOUD_PICKER.equals(intent.getAction()) && BuildPropertyUtils.isDogfoodDevice()) {
                                                    Intent intent2 = new Intent();
                                                    intent2.setFlags(268435456);
                                                    intent2.setClass(context, CloudPickerActivity.class);
                                                    context.startActivity(intent2);
                                                    return;
                                                } else if (intent.getAction().equals(UpgradeUtilConstants.AB_UPGRADE_COMPLETED_INTENT)) {
                                                    sm.onIntentABApplyingPatchCompleted(intent.getBooleanExtra(UpgradeUtilConstants.KEY_AB_UPGRADE_STATUS_SUCCESS, false), intent.getStringExtra(UpgradeUtilConstants.KEY_AB_UPGRADE_STATUS_REASON), intent.getStringExtra(UpgradeUtilConstants.UPGRADE_UPDATE_STATUS));
                                                    return;
                                                } else {
                                                    if ("android.app.action.SYSTEM_UPDATE_POLICY_CHANGED".equals(intent.getAction())) {
                                                        if (ThinkShieldUtilConstants.ASC_VERSION_V3.equals(ThinkShieldUtils.getAscVersion(context))) {
                                                            return;
                                                        }
                                                        new SystemUpdaterPolicy().onSystemUpdatePolicyChanged(settings, sm.isBusy());
                                                    }
                                                    if (intent.getAction().equals(UpgradeUtilConstants.ACTION_DM_CANCEL_ONGOING_UPGRADE)) {
                                                        Logger.debug("OtaApp", "AndroidClientUpgradeService.handleIntent: " + intent.getAction());
                                                        if (settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                                                            UpdaterUtils.stopBGInstallActivity(context);
                                                        }
                                                        sm.onIntentDmCancelUpgrade(intent.getStringExtra("UpdateType"));
                                                        return;
                                                    } else if (intent.getAction().equals(CceSyncSettingsHandler.CCE_SETTINGS_UPDATED)) {
                                                        CceSyncSettingsHandler.fetchSettingsList(context);
                                                        return;
                                                    } else if (intent.getAction().equals(CceSyncSettingsHandler.CCE_SEND_SETTINGS_RESPONSE)) {
                                                        CceSyncSettingsHandler.onReceiveSettingsList(intent, settings, env);
                                                        return;
                                                    } else {
                                                        if (intent.getAction().equals(UpgradeUtilConstants.CANCEL_UPDATE)) {
                                                            sm.cancelOTA(intent.getStringExtra(UpgradeUtilConstants.KEY_REASON), intent.getStringExtra(UpgradeUtilConstants.KEY_UPGRADE_STATUS));
                                                        }
                                                        if (UpgradeUtilConstants.ACTION_OVERRIDE_METADATA.equals(intent.getAction())) {
                                                            sm.overWriteMetaData(MetaDataBuilder.from(intent.getStringExtra(UpgradeUtilConstants.KEY_METADATA)));
                                                        }
                                                        return;
                                                    }
                                                }
                                            }
                                            sm.onIntentForceUpgradeTimerExpiry();
                                            return;
                                        }
                                    }
                                    sm.onIntentWiFiDiscoverTimerExpiry();
                                    return;
                                }
                            }
                        }
                    }
                    sm.onDeviceShutdown();
                    return;
                }
            }
        }
        sm.onIntentASCSessionDone(intent.getIntExtra(ThinkShieldUtilConstants.EXTRA_CHECK_UPDATE_ASC_ERROR, -1), intent.getLongExtra(ThinkShieldUtilConstants.EXTRA_TRANSACTION_ID, -1L), settings);
    }
}
