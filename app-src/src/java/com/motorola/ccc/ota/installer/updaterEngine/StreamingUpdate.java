package com.motorola.ccc.ota.installer.updaterEngine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.installer.InstallTypeResolver;
import com.motorola.ccc.ota.installer.updaterEngine.common.CallBackInterface;
import com.motorola.ccc.ota.installer.updaterEngine.common.InstallerUtilMethods;
import com.motorola.ccc.ota.installer.updaterEngine.common.UEBinder;
import com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineHelper;
import com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineStateHandler;
import com.motorola.ccc.ota.installer.updaterEngine.download.DownloadBuilderException;
import com.motorola.ccc.ota.installer.updaterEngine.download.DownloadRequestToUE;
import com.motorola.ccc.ota.installer.updaterEngine.download.UEDownloadRequestBuilder;
import com.motorola.ccc.ota.installer.updaterEngine.download.UEDownloadRetry;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.stats.StatsHelper;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.errorCodes.UpdaterEngineErrorCodes;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.downloadservice.download.policy.DownloadPolicy;
import com.motorola.otalib.downloadservice.download.policy.ZeroRatedManager;
import com.motorola.otalib.downloadservice.download.policy.ZeroRatedServices;
import com.motorola.otalib.downloadservice.utils.DownloadServiceSettings;
import java.lang.reflect.InvocationTargetException;
import org.json.JSONException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class StreamingUpdate extends UpdaterEngineInstaller {
    private static InstallerUtilMethods.InstallerErrorStatus mInstallerStatus;
    private static PendingIntent mPendingIntent;
    private static UEDownloadRequestBuilder.NetworkDetails networkDetails;
    private static StreamingUpdateBroadcastReceiver streamingUpdateBroadcastReceiver;
    private UECallbackHandler callbackHandler;
    private Context context;
    private ApplicationEnv.Database.Descriptor d;
    private UEDownloadRequestBuilder dlBuilder;
    private UEDownloadRequestBuilder.DownloadingChoices downloadingChoices;
    private AlarmManager mAlarmManager;
    private ConnectivityManager mCm;
    private DownloadServiceSettings mDownloadServiceSettings;
    private TelephonyManager mTm;
    private DownloadPolicy od;
    private DownloadRequestToUE request;
    private UEDownloadRetry ueDownloadRetry;
    private ZeroRatedServices zs;

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public boolean isDataSpaceLowForUpgrade(ApplicationEnv.Database.Descriptor descriptor, ApplicationEnv applicationEnv) {
        return false;
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void onInternalNotification(String str, ApplicationEnv.Database.Descriptor descriptor) {
    }

    public StreamingUpdate(ApplicationEnv applicationEnv, BotaSettings botaSettings, ApplicationEnv.Database database) {
        this._db = database;
        this._settings = botaSettings;
        this._env = applicationEnv;
        this.mVersion = this._env.getServices().getDeviceSha1();
        this.d = this._db.getDescription(this.mVersion);
        updaterEngineStateHandler = UpdaterEngineStateHandler.getUpdaterEngineStateHandlerInstance();
        this.context = OtaApplication.getGlobalContext();
        this.ueDownloadRetry = new UEDownloadRetry(this.context, this._settings);
        this.mCm = (ConnectivityManager) this.context.getSystemService("connectivity");
        this.mTm = (TelephonyManager) this.context.getSystemService("phone");
        DownloadServiceSettings downloadServiceSettings = new DownloadServiceSettings(this.context.getSharedPreferences(DownloadServiceSettings.KEY_PREFS_NAME, 0));
        this.mDownloadServiceSettings = downloadServiceSettings;
        this.zs = ZeroRatedServices.getZeroRatedServices(this.context, downloadServiceSettings);
        this.od = new DownloadPolicy(this.mCm, this.mTm, this.zs);
        this.mAlarmManager = (AlarmManager) OtaApplication.getGlobalContext().getSystemService("alarm");
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void onStartDownloadNotification(String str) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(this.mVersion);
        if (description != null && !description.getMeta().showPreInstallScreen() && !description.getMeta().getRebootRequired()) {
            this._db.setState(this.mVersion, ApplicationEnv.PackageState.GettingPackage, str);
        } else if (!this._settings.getBoolean(Configs.VERIFY_PAYLOAD_STATUS_CHECK) && !this._settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            this._db.setVersionState(this.mVersion, ApplicationEnv.PackageState.VerifyPayloadMetadata, str);
        } else {
            this._db.setState(this.mVersion, ApplicationEnv.PackageState.ABApplyingPatch, str);
        }
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void displayScreenForGettingDescriptor(ApplicationEnv.Database.Descriptor descriptor) {
        try {
            this._env.getUtilities().sendStartBackgroundInstallationFragment(this.mVersion, MetaDataBuilder.toJSONString(descriptor.getMeta()), descriptor.getRepository());
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in StreamingUpdate, displayScreenForGettingDescriptor: " + e);
        }
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public synchronized InstallerUtilMethods.InstallerErrorStatus updaterEngineHandler(ApplicationEnv.Database.Descriptor descriptor, SystemUpdaterPolicy systemUpdaterPolicy) {
        Logger.debug("OtaApp", "Starting streaming update");
        if (systemUpdaterPolicy.isOtaUpdateDisabledByPolicyMngr()) {
            NotificationUtils.cancelOtaNotification();
            return InstallerUtilMethods.InstallerErrorStatus.STATUS_SYSTEM_UPDATE_CANCEL_POLICY_SET;
        } else if (systemUpdaterPolicy.shouldIBlockUpdateForSystemPolicy(descriptor, this._settings)) {
            NotificationUtils.cancelOtaNotification();
            return InstallerUtilMethods.InstallerErrorStatus.STATUS_SYSTEM_UPDATE_POLICY_SET;
        } else if (UpdaterEngineStateHandler.isBusy()) {
            return InstallerUtilMethods.InstallerErrorStatus.STATUS_BUSY;
        } else {
            transferUpgradeToUE();
            return InstallerUtilMethods.InstallerErrorStatus.STATUS_OK;
        }
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public synchronized InstallerUtilMethods.InstallerErrorStatus initializeUpdaterEngineHandlerMergeState() {
        if (UpdaterEngineStateHandler.isBusy()) {
            return InstallerUtilMethods.InstallerErrorStatus.STATUS_BUSY;
        }
        this.callbackHandler = new UECallbackHandler();
        updaterEngineStateHandler.initializeUpdaterEngineStateHandler(this, this.callbackHandler);
        updaterEngineStateHandler.transferUpgradeToUE();
        return InstallerUtilMethods.InstallerErrorStatus.STATUS_OK;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class UECallbackHandler implements CallBackInterface {
        private UECallbackHandler() {
        }

        private synchronized void onApplyPayload() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, JSONException, DownloadBuilderException {
            boolean z = StreamingUpdate.this._settings.getBoolean(Configs.VAB_MERGE_PROCESS_RUNNING, false);
            if (!UpdaterEngineInstaller.getApplyPayloadStarted() && !z) {
                if (StreamingUpdate.this.d != null && StreamingUpdate.this.d.getMeta() != null) {
                    UpdaterEngineInstaller.setApplyPayloadStarted(true);
                    if (BuildPropReader.isBotaATT() && UpdaterUtils.isWifiOnly()) {
                        StreamingUpdate.this._env.getUtilities().startFotaWifiDiscoveryTimer();
                    }
                    StreamingUpdate.this.dlBuilder = new UEDownloadRequestBuilder(StreamingUpdate.this._settings, StreamingUpdate.this.mCm, StreamingUpdate.this.mTm);
                    String adminApnUrl = InstallerUtilMethods.getAdminApnUrl(StreamingUpdate.this._settings);
                    if (!StreamingUpdate.this._settings.getBoolean(Configs.BATTERY_LOW) && !UpdaterUtils.isDeviceInDatasaverMode()) {
                        if (!StreamingUpdate.this.od.canIUseZeroRatedNetwork(StreamingUpdate.this.context, UpdaterUtils.isWifiOnly(), adminApnUrl, StreamingUpdate.this._settings.getString(Configs.DISALLOWED_NETS), StreamingUpdate.this._settings.getString(Configs.DOWNLOAD_DESCRIPTOR))) {
                            StreamingUpdate.networkDetails = StreamingUpdate.this.dlBuilder.fetchNonAdminapnNetworkDetails(StreamingUpdate.this.context, adminApnUrl, StreamingUpdate.this._settings.getString(Configs.DISALLOWED_NETS), false);
                            StreamingUpdate.this.downloadingChoices = StreamingUpdate.networkDetails.getDownloadingChoices();
                            if (UEDownloadRequestBuilder.DownloadingChoices.WIFI_ONLY == StreamingUpdate.this.downloadingChoices) {
                                StreamingUpdate.this.startWifiDiscoveryManager();
                                StreamingUpdate.this.clearUEInstallerBeforeExit();
                                return;
                            } else if (UEDownloadRequestBuilder.DownloadingChoices.WIFI_OK != StreamingUpdate.this.downloadingChoices && UEDownloadRequestBuilder.DownloadingChoices.WAN_OK != StreamingUpdate.this.downloadingChoices) {
                                StreamingUpdate streamingUpdate = StreamingUpdate.this;
                                streamingUpdate.sendUpgradeStatus(streamingUpdate._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f), StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0), -1);
                                StreamingUpdate.this.clearUEInstallerBeforeExit();
                                return;
                            } else if (UEDownloadRequestBuilder.DownloadingChoices.WAN_OK == StreamingUpdate.this.downloadingChoices && ((StreamingUpdate.this.d.getMeta().getForceDownloadTime() >= 0.0d || SmartUpdateUtils.isDownloadForcedForSmartUpdate(StreamingUpdate.this._settings)) && !UpdaterUtils.getAutomaticDownloadForCellular())) {
                                Logger.debug("OtaApp", "StreamingUpdate.onApplyPayload, Forced update,but not on wifi, so return");
                                StreamingUpdate streamingUpdate2 = StreamingUpdate.this;
                                streamingUpdate2.sendUpgradeStatus(streamingUpdate2._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f), StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0), -1);
                                StreamingUpdate.this.clearUEInstallerBeforeExit();
                                return;
                            }
                        } else if (StreamingUpdate.this.od.canIDownloadUsingZeroRatedChannel()) {
                            Logger.debug("OtaApp", "handleDownloadServiceRequest: can be downloaded now; reason zero rated NW is available");
                            StreamingUpdate.networkDetails = StreamingUpdate.this.dlBuilder.fetchAdminapnNetworkDetails();
                        } else {
                            Logger.debug("OtaApp", "handleDownloadServiceRequest: cannot be downloaded now; reason zero rated NW is not available");
                            StreamingUpdate.this.od.startZeroRatedProcess();
                            return;
                        }
                        if (CusAndroidUtils.checkForUrlExpiry(StreamingUpdate.this._settings, false)) {
                            StreamingUpdate.this._settings.setString(Configs.OTA_GET_DESCRIPTOR_REASON, "URL timeout");
                            CusAndroidUtils.sendGetDescriptor(StreamingUpdate.this.context, StreamingUpdate.this.mVersion, "encountered url timeout go and fetch new download url", false);
                            StreamingUpdate streamingUpdate3 = StreamingUpdate.this;
                            streamingUpdate3.sendUpgradeStatus(streamingUpdate3._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f), StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0), 1);
                            StreamingUpdate.this.clearUEInstallerBeforeExit();
                            StreamingUpdate.this.clearRetryTasks();
                            return;
                        }
                        StreamingUpdate.this.checkAndStopWifiDiscoveryManager();
                        StreamingUpdate streamingUpdate4 = StreamingUpdate.this;
                        streamingUpdate4.request = streamingUpdate4.dlBuilder.build(StreamingUpdate.networkDetails, StreamingUpdate.this.d.getMeta().getStreamingData());
                        UpdaterEngineHelper.applyPayload(StreamingUpdate.this.request.getDownloadUrl(), StreamingUpdate.this.request.getOffSet(), StreamingUpdate.this.request.getFileSize(), StreamingUpdate.this.request.getHeaderKeyValuePair());
                        StreamingUpdate.this.setExpiryAlarmForUE(0, 0.0f);
                        InstallerUtilMethods.sendActionABApplyPayloadStarted(StreamingUpdate.this.context);
                        InstallerUtilMethods.acquireWakeLock();
                        return;
                    }
                    StreamingUpdate streamingUpdate5 = StreamingUpdate.this;
                    streamingUpdate5.sendUpgradeStatus(streamingUpdate5._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f), StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0), -1);
                    StreamingUpdate.this.clearUEInstallerBeforeExit();
                    return;
                }
                onCompleted(UpdaterEngineErrorCodes.ERROR_EXCEPTION, "onApplyPayload:error while parsing the metadata", ErrorCodeMapper.KEY_PARSE_ERROR);
            }
        }

        @Override // com.motorola.ccc.ota.installer.updaterEngine.common.CallBackInterface
        public void onProgress(int i, float f) {
            if (i == 0) {
                try {
                    onApplyPayload();
                    return;
                } catch (DownloadBuilderException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | JSONException e) {
                    Logger.error("OtaApp", "Caught an exception during apply payload: " + e);
                    onCompleted(UpdaterEngineErrorCodes.ERROR_EXCEPTION, "Caught an exception duringapply payload " + e, ErrorCodeMapper.KEY_APPLY_FAILURE);
                    return;
                } catch (InvocationTargetException e2) {
                    Logger.error("OtaApp", "Exception in StreamingUpdate: onProgress: " + e2);
                    if (e2.getCause() == null || e2.getCause().getMessage() == null || !e2.getCause().getMessage().contains("waiting for reboot")) {
                        onCompleted(UpdaterEngineErrorCodes.ERROR_EXCEPTION, "Caught an exception duringapply payload " + e2, ErrorCodeMapper.KEY_APPLY_FAILURE);
                        return;
                    }
                    Logger.debug("OtaApp", "Exception cause is " + e2.getCause().getMessage());
                    onCompleted(0, null, UpdaterEngineErrorCodes.getFailureResultStatus(0));
                    return;
                }
            }
            if (i == 3) {
                UEDownloadRetry.clearRetryTask();
                StreamingUpdate.this._settings.removeConfig(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
            } else if (i >= 4 && BuildPropReader.isBotaATT()) {
                StreamingUpdate.this._env.getUtilities().cleanFotaWifiDiscoveryTimer();
            }
            if (i == 6 && !UpdaterEngineInstaller.getApplyPayloadStarted()) {
                onCompleted(UpdaterEngineErrorCodes.K_SUCCESS, null, UpdaterEngineErrorCodes.getFailureResultStatus(UpdaterEngineErrorCodes.K_SUCCESS));
                return;
            }
            UpdaterEngineInstaller.setApplyPayloadStarted(true);
            StreamingUpdate.this.sendUpgradeStatus(f * 100.0f, i, 0);
        }

        @Override // com.motorola.ccc.ota.installer.updaterEngine.common.CallBackInterface
        public void onCompleted(int i, String str, String str2) {
            Logger.debug("OtaApp", "StreamingUpdate:onCompleted: errorcode " + i + " Reason " + str);
            String jsonDataFromFile = UpdaterEngineHelper.getJsonDataFromFile(UpdaterEngineHelper.metricsFilePath);
            if (jsonDataFromFile != null) {
                StatsHelper.setStreamingStats(StreamingUpdate.this._settings, jsonDataFromFile);
            }
            if (i == UpdaterEngineErrorCodes.K_SUCCESS) {
                UpdaterEngineInstaller.updaterEngineStateHandler.transferUpgradeBackToOta(OtaApplication.getGlobalContext(), true, null, str2);
            } else if (i == UpdaterEngineErrorCodes.ERROR_NETWORK) {
                if (NetworkUtils.isNetWorkConnected(StreamingUpdate.this.mCm)) {
                    if (!TextUtils.isEmpty(InstallerUtilMethods.getAdminApnUrl(StreamingUpdate.this._settings))) {
                        StreamingUpdate.this.od.stopUsingZeroRatedChannel();
                    }
                    StreamingUpdate.this.restartOnProgress();
                    return;
                }
                StreamingUpdate streamingUpdate = StreamingUpdate.this;
                streamingUpdate.sendUpgradeStatus(streamingUpdate._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f), StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0), -1);
                StreamingUpdate.this.clearUEInstallerBeforeExit();
            } else if (UpdaterEngineErrorCodes.isItaRetriableError(i)) {
                if (!UEDownloadRetry.isRetryPending() && !StreamingUpdate.this.ueDownloadRetry.handleRetry(i, StreamingUpdate.this.mVersion)) {
                    UpdaterEngineInstaller.updaterEngineStateHandler.transferUpgradeBackToOta(StreamingUpdate.this.context, false, null, str2);
                    return;
                }
                Logger.debug("OtaApp", "a retry is already scheduled, so return");
                StreamingUpdate streamingUpdate2 = StreamingUpdate.this;
                streamingUpdate2.sendUpgradeStatus(streamingUpdate2._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f), StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0), 1);
                StreamingUpdate.this.clearUEInstallerBeforeExit();
            } else if (i == UpdaterEngineErrorCodes.K_USER_CANCELED) {
                if (InstallerUtilMethods.InstallerErrorStatus.STATUS_USER_CANCEL == StreamingUpdate.mInstallerStatus) {
                    InstallerUtilMethods.sendCancelBackgroundInstallationResponse(StreamingUpdate.this.context);
                    StreamingUpdate.this.clearUEInstallerBeforeExit();
                } else if (InstallerUtilMethods.InstallerErrorStatus.STATUS_SILENT_FAILURE == StreamingUpdate.mInstallerStatus) {
                    StreamingUpdate.this.clearUEInstallerBeforeExit();
                } else if (InstallerUtilMethods.InstallerErrorStatus.STATUS_UE_NOT_RESPONDING == StreamingUpdate.mInstallerStatus) {
                    UpdaterEngineInstaller.updaterEngineStateHandler.transferUpgradeBackToOta(OtaApplication.getGlobalContext(), false, "Updater engine is not responding at , status " + StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0) + " percentage " + StreamingUpdate.this._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f) + SystemUpdateStatusUtils.SPACE, ErrorCodeMapper.KEY_UE_NOT_RESPONDING);
                } else {
                    if (!TextUtils.isEmpty(InstallerUtilMethods.getAdminApnUrl(StreamingUpdate.this._settings))) {
                        StreamingUpdate.this.od.stopUsingZeroRatedChannel();
                    }
                    StreamingUpdate.this.restartOnProgress();
                }
            } else {
                UpdaterEngineInstaller.updaterEngineStateHandler.transferUpgradeBackToOta(OtaApplication.getGlobalContext(), false, str, str2);
            }
        }
    }

    public void restartOnProgress() {
        setApplyPayloadStarted(false);
        cancelPendingExpiryAlarmSetForUE();
        this.callbackHandler.onProgress(0, 0.0f);
    }

    @Override // com.motorola.ccc.ota.installer.updaterEngine.UpdaterEngineInstaller
    public void clearUEInstallerBeforeExit() {
        Logger.debug("OtaApp", "clearUEInstallerBeforeExit");
        UEBinder.resetBinded();
        StreamingUpdateBroadcastReceiver streamingUpdateBroadcastReceiver2 = streamingUpdateBroadcastReceiver;
        if (streamingUpdateBroadcastReceiver2 != null) {
            BroadcastUtils.unregisterLocalReceiver(this.context, streamingUpdateBroadcastReceiver2);
            this.context.unregisterReceiver(streamingUpdateBroadcastReceiver);
            streamingUpdateBroadcastReceiver = null;
        }
        this.mDownloadServiceSettings.clearConfigValue(DownloadServiceSettings.KEY_DO_NOT_BIND_OTA_PROCESS);
        if (!TextUtils.isEmpty(InstallerUtilMethods.getAdminApnUrl(this._settings))) {
            this.od.stopUsingZeroRatedChannel();
        }
        mInstallerStatus = InstallerUtilMethods.InstallerErrorStatus.STATUS_OK;
        cancelPendingExpiryAlarmSetForUE();
        setApplyPayloadStarted(false);
        updaterEngineStateHandler.clearUEStateHandler();
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void checkAndResetInstallerFromCusSM(ApplicationEnv.PackageState packageState, InstallerUtilMethods.InstallerErrorStatus installerErrorStatus) {
        if (packageState == ApplicationEnv.PackageState.GettingDescriptor || packageState == ApplicationEnv.PackageState.ABApplyingPatch || packageState == ApplicationEnv.PackageState.VerifyPayloadMetadata || packageState == ApplicationEnv.PackageState.VerifyAllocateSpace) {
            this._env.getUtilities().cancelBGInstallNotification();
            setAllocateSpaceStarted(false);
            if ((packageState == ApplicationEnv.PackageState.GettingDescriptor || packageState == ApplicationEnv.PackageState.VerifyPayloadMetadata || packageState == ApplicationEnv.PackageState.VerifyAllocateSpace) && installerErrorStatus == InstallerUtilMethods.InstallerErrorStatus.STATUS_USER_CANCEL) {
                InstallerUtilMethods.sendCancelBackgroundInstallationResponse(this.context);
            } else if (packageState == ApplicationEnv.PackageState.ABApplyingPatch) {
                mInstallerStatus = installerErrorStatus;
                if (UpdaterEngineStateHandler.isBusy() && UpdaterEngineHelper.cancelUpdateEngine()) {
                    return;
                }
                if (installerErrorStatus == InstallerUtilMethods.InstallerErrorStatus.STATUS_USER_CANCEL) {
                    InstallerUtilMethods.sendCancelBackgroundInstallationResponse(this.context);
                }
                mInstallerStatus = InstallerUtilMethods.InstallerErrorStatus.STATUS_OK;
                if (UpdaterEngineStateHandler.isBusy()) {
                    clearUEInstallerBeforeExit();
                }
            }
        } else if (packageState == ApplicationEnv.PackageState.Querying || packageState == ApplicationEnv.PackageState.QueryingInstall || packageState == ApplicationEnv.PackageState.Upgrading) {
            this._env.getUtilities().cancelRestartNotification();
            UpdaterEngineHelper.resetUpdateEngine();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startWifiDiscoveryManager() {
        sendUpgradeStatus(this._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f), this._settings.getInt(Configs.STORED_AB_STATUS, 0), -1);
        this._env.getUtilities().startBotaWifiDiscoveryTimer();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void checkAndStopWifiDiscoveryManager() {
        if (UpdaterUtils.isWifiOnly() && UEDownloadRequestBuilder.DownloadingChoices.WIFI_OK == this.downloadingChoices) {
            this._env.getUtilities().cleanBotaWifiDiscoveryTimer();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendUpgradeStatus(float f, int i, int i2) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(this.mVersion);
        if (description == null || description.getMeta() == null) {
            return;
        }
        if (i2 != 0) {
            InstallerUtilMethods.sendUpgradeStatus(this.context, f, i, i2, InstallTypeResolver.InstallerType.STREAMING);
            return;
        }
        if (i != 0 && f == 0.0f) {
            this._settings.setFloat(Configs.STORED_AB_PROGRESS_PERCENT, -1.0f);
        }
        if (f > this._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f)) {
            InstallerUtilMethods.sendUpgradeStatus(this.context, f, i, i2, InstallTypeResolver.InstallerType.STREAMING);
        }
        this._settings.setFloat(Configs.STORED_AB_PROGRESS_PERCENT, f);
        this._settings.setInt(Configs.STORED_AB_STATUS, i);
    }

    public void registerForStreamingIntent() {
        if (streamingUpdateBroadcastReceiver == null) {
            streamingUpdateBroadcastReceiver = new StreamingUpdateBroadcastReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZeroRatedManager.ACTION_ZERORATED_CHANNEL_ACTIVE);
        intentFilter.addAction(ZeroRatedManager.ACTION_ZERORATED_CHANNEL_INACTIVE);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_AB_APPLY_PAYLOAD_SUSPEND);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_BATTERY_LOW);
        intentFilter.addAction(UpgradeUtilConstants.ACTION_DATA_SAVER_DURING_AB_STREAMING);
        BroadcastUtils.registerLocalReceiver(this.context, streamingUpdateBroadcastReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(InstallerUtilMethods.INTENT_MAX_ALARM_FOR_UE);
        this.context.registerReceiver(streamingUpdateBroadcastReceiver, intentFilter2, 4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class StreamingUpdateBroadcastReceiver extends BroadcastReceiver {
        private StreamingUpdateBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.debug("OtaApp", "StreamingUpdateBroadcastReceiver received " + intent.getAction());
            if (UpgradeUtilConstants.ACTION_BATTERY_LOW.equals(intent.getAction())) {
                UpdaterEngineHelper.cancelUpdateEngine();
                return;
            }
            if (UpgradeUtilConstants.ACTION_DATA_SAVER_DURING_AB_STREAMING.equals(intent.getAction()) && StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0) < 4) {
                UpdaterEngineHelper.cancelUpdateEngine();
            }
            if (intent.getAction().equals(UpgradeUtilConstants.ACTION_AB_APPLY_PAYLOAD_SUSPEND)) {
                if (StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0) >= 4 || StreamingUpdate.networkDetails == null || "WIFI".equals(StreamingUpdate.networkDetails.getNetworkType())) {
                    return;
                }
                UpdaterEngineHelper.cancelUpdateEngine();
            } else if (ZeroRatedManager.ACTION_ZERORATED_CHANNEL_ACTIVE.equals(intent.getAction())) {
                Logger.debug("OtaApp", "Received zero rated channel active intent, start/resume the download");
                StreamingUpdate.this.restartOnProgress();
            } else if (ZeroRatedManager.ACTION_ZERORATED_CHANNEL_INACTIVE.equals(intent.getAction())) {
                Logger.debug("OtaApp", "Received zero rated channel inactive intent, bring up again if needed");
                if ("onUnavailable".equals(intent.getStringExtra(ZeroRatedManager.KEY_INACTIVE_REASON))) {
                    Logger.debug("OtaApp", "Received zero rated channel inactive intent for onUnavailable");
                    StreamingUpdate streamingUpdate = StreamingUpdate.this;
                    streamingUpdate.sendUpgradeStatus(streamingUpdate._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f), StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0), -1);
                    StreamingUpdate.this.clearUEInstallerBeforeExit();
                }
            } else if (InstallerUtilMethods.INTENT_MAX_ALARM_FOR_UE.equals(intent.getAction())) {
                int i = StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0);
                float f = StreamingUpdate.this._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f);
                int intExtra = intent.getIntExtra(InstallerUtilMethods.KEY_STATUS, 0);
                float floatExtra = intent.getFloatExtra(InstallerUtilMethods.KEY_PERCENTAGE, 0.0f);
                if (i > intExtra || (i == intExtra && f > floatExtra)) {
                    StreamingUpdate.this.setExpiryAlarmForUE(i, f);
                } else if (UpdaterEngineStateHandler.isBusy()) {
                    if (StreamingUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0) == 6) {
                        Logger.debug("OtaApp", "Looks like client missed updateComplete callbackfrom UE, moving to Querying state");
                        StreamingUpdate.this.callbackHandler.onCompleted(0, null, UpdaterEngineErrorCodes.getFailureResultStatus(0));
                    } else if (UpdaterEngineHelper.cancelUpdateEngine()) {
                        StreamingUpdate.mInstallerStatus = InstallerUtilMethods.InstallerErrorStatus.STATUS_UE_NOT_RESPONDING;
                    } else {
                        StreamingUpdate.this.callbackHandler.onCompleted(-1, "Updater engine is not responding at , status " + i + " percentage " + f + SystemUpdateStatusUtils.SPACE, ErrorCodeMapper.KEY_UE_NOT_RESPONDING);
                    }
                }
            }
        }
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void clearRetryTasks() {
        super.clearRetryTasks();
        if (UEDownloadRetry.isRetryPending()) {
            UEDownloadRetry.clearRetryTask();
        }
        cancelPendingExpiryAlarmSetForUE();
    }

    private synchronized void transferUpgradeToUE() {
        registerForStreamingIntent();
        this.mDownloadServiceSettings.setConfig(DownloadServiceSettings.KEY_DO_NOT_BIND_OTA_PROCESS, String.valueOf(true));
        this.callbackHandler = new UECallbackHandler();
        updaterEngineStateHandler.initializeUpdaterEngineStateHandler(this, this.callbackHandler);
        updaterEngineStateHandler.transferUpgradeToUE();
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public InstallTypeResolver.InstallerType getCurrentInstallerType() {
        return InstallTypeResolver.InstallerType.STREAMING;
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public synchronized void allocateSpaceBeforeApplyPatch(ApplicationEnv.Database.Descriptor descriptor) throws JSONException {
        if (!getAllocateSpaceStarted()) {
            Logger.debug("OtaApp", "SteamingUpdate.allocateSpaceBeforeApplyPatch inside allocateSpace thread.");
            setAllocateSpaceStarted(true);
            UpdaterEngineHelper.allocateSpace(OtaApplication.getGlobalContext(), FileUtils.getPayloadMetaDataFileName(), InstallerUtilMethods.getHeaderValues(descriptor.getMeta().getStreamingData()));
        } else {
            Logger.debug("OtaApp", "SteamingUpdate.allocateSpaceBeforeApplyPatch allocateSpace thread locked.");
        }
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void onAllocateSpaceResult() {
        setAllocateSpaceStarted(false);
    }

    public void setExpiryAlarmForUE(int i, float f) {
        Intent intent = new Intent(InstallerUtilMethods.INTENT_MAX_ALARM_FOR_UE);
        intent.putExtra(InstallerUtilMethods.KEY_PERCENTAGE, f);
        intent.putExtra(InstallerUtilMethods.KEY_STATUS, i);
        mPendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, 335544320);
        this.mAlarmManager.setExactAndAllowWhileIdle(0, System.currentTimeMillis() + InstallerUtilMethods.MAX_ALARM_TIME_FOR_UE, mPendingIntent);
    }

    public void cancelPendingExpiryAlarmSetForUE() {
        PendingIntent pendingIntent = mPendingIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
        }
    }
}
