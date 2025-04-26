package com.motorola.ccc.ota.installer.updaterEngine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.installer.InstallTypeResolver;
import com.motorola.ccc.ota.installer.updaterEngine.common.CallBackInterface;
import com.motorola.ccc.ota.installer.updaterEngine.common.InstallerUtilMethods;
import com.motorola.ccc.ota.installer.updaterEngine.common.UEBinder;
import com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineHelper;
import com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineStateHandler;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.PayloadMetaDataDownloader;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.CusUtilMethods;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.errorCodes.UpdaterEngineErrorCodes;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.json.JSONException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ABUpdate extends UpdaterEngineInstaller {
    private static InstallerUtilMethods.InstallerErrorStatus mInstallerStatus;
    private ABUpdateBroadcastReceiver abUpdateBroadcastReceiver;
    private UECallbackHandler callbackHandler;
    private Context context;
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    public ABUpdate(ApplicationEnv applicationEnv, BotaSettings botaSettings, ApplicationEnv.Database database, ApplicationEnv.Database.Descriptor descriptor) {
        this._db = database;
        this._settings = botaSettings;
        this._env = applicationEnv;
        this.context = OtaApplication.getGlobalContext();
        if (UpgradeSourceType.modem.toString().equals(descriptor.getRepository())) {
            this.mVersion = BuildPropReader.getDeviceModemConfigVersionSha1();
        } else {
            this.mVersion = this._env.getServices().getDeviceSha1();
        }
        updaterEngineStateHandler = UpdaterEngineStateHandler.getUpdaterEngineStateHandlerInstance();
        this.mAlarmManager = (AlarmManager) OtaApplication.getGlobalContext().getSystemService("alarm");
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void onStartDownloadNotification(String str) {
        this._db.setVersionState(this.mVersion, ApplicationEnv.PackageState.QueueForDownload, str);
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void onInternalNotification(String str, ApplicationEnv.Database.Descriptor descriptor) {
        FileUtils.setPermission(FileUtils.getPackageFilePathForA2B(), FileUtils.FILE_PERMISSION);
        if (BuildPropReader.getDeviceModemConfigVersionSha1().equals(descriptor.getVersion())) {
            this._settings.setBoolean(Configs.MODEM_DOWNLOAD_COMPLETED, true);
            this._settings.removeConfig(Configs.MODEM_FILE_DL_EXPIRED_TIMESTAMP);
            this._db.setVersionState(descriptor.getVersion(), ApplicationEnv.PackageState.IntimateModem, null);
            return;
        }
        CusUtilMethods.settingMaxDeferTimeForFOTAUpgrade(descriptor, 86400000L, this._settings);
        NotificationUtils.clearNextPromptDetails(this._settings);
        PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_ALLOCATE_SPACE, this._settings, OtaApplication.getGlobalContext(), this._env);
        this._settings.setBoolean(Configs.DOWNLOAD_COMPLETED, true);
        this._db.setVersionState(descriptor.getVersion(), ApplicationEnv.PackageState.VerifyAllocateSpace, null);
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void displayScreenForGettingDescriptor(ApplicationEnv.Database.Descriptor descriptor) {
        try {
            this._env.getUtilities().sendStartDownloadProgressFragment(this.mVersion, MetaDataBuilder.toJSONString(descriptor.getMeta()), descriptor.getRepository());
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in ABUpdate: displayScreenForGettingDescriptor: " + e);
        }
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public synchronized InstallerUtilMethods.InstallerErrorStatus updaterEngineHandler(ApplicationEnv.Database.Descriptor descriptor, SystemUpdaterPolicy systemUpdaterPolicy) {
        InstallerUtilMethods.InstallerErrorStatus doSanityForABUpdate;
        doSanityForABUpdate = doSanityForABUpdate(descriptor, systemUpdaterPolicy);
        if (doSanityForABUpdate == InstallerUtilMethods.InstallerErrorStatus.STATUS_OK) {
            registerABUpdateBroadcastReceiver();
            this.callbackHandler = new UECallbackHandler();
            updaterEngineStateHandler.initializeUpdaterEngineStateHandler(this, this.callbackHandler);
            updaterEngineStateHandler.transferUpgradeToUE();
        }
        return doSanityForABUpdate;
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

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public InstallTypeResolver.InstallerType getCurrentInstallerType() {
        return InstallTypeResolver.InstallerType.AB;
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void onAllocateSpaceResult() {
        setAllocateSpaceStarted(false);
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public synchronized void allocateSpaceBeforeApplyPatch(ApplicationEnv.Database.Descriptor descriptor) throws JSONException {
        if (!getAllocateSpaceStarted()) {
            Logger.debug("OtaApp", "ABUpdate.allocateSpaceBeforeApplyPatch inside allocateSpace thread.");
            setAllocateSpaceStarted(true);
            try {
                InstallerUtilMethods.getPayloadMetaDataBin();
                FileUtils.setPermission(FileUtils.getPayloadMetaDataFileName(), FileUtils.FILE_PERMISSION);
                UpdaterEngineHelper.allocateSpace(OtaApplication.getGlobalContext(), FileUtils.getPayloadMetaDataFileName(), InstallerUtilMethods.getHeaderKeyValuePair());
            } catch (IOException e) {
                Logger.debug("OtaApp", "Exception in getHeaderKeyValuePair: " + e);
                UpgradeUtilMethods.sendActionAllocateSpaceResult(this.context, 0L);
            }
        } else {
            Logger.debug("OtaApp", "ABUpdate.allocateSpaceBeforeApplyPatch allocateSpace thread locked.");
        }
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void clearRetryTasks() {
        cancelPendingExpiryAlarmSetForUE();
    }

    @Override // com.motorola.ccc.ota.installer.updaterEngine.UpdaterEngineInstaller
    public void clearUEInstallerBeforeExit() {
        Logger.verbose("OtaApp", "Clearing AB  updater installer");
        UEBinder.resetBinded();
        cancelPendingExpiryAlarmSetForUE();
        setApplyPayloadStarted(false);
        ABUpdateBroadcastReceiver aBUpdateBroadcastReceiver = this.abUpdateBroadcastReceiver;
        if (aBUpdateBroadcastReceiver != null) {
            this.context.unregisterReceiver(aBUpdateBroadcastReceiver);
            this.abUpdateBroadcastReceiver = null;
        }
        updaterEngineStateHandler.clearUEStateHandler();
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void checkAndResetInstallerFromCusSM(ApplicationEnv.PackageState packageState, InstallerUtilMethods.InstallerErrorStatus installerErrorStatus) {
        mInstallerStatus = installerErrorStatus;
        if (packageState == ApplicationEnv.PackageState.ABApplyingPatch) {
            this._env.getUtilities().cancelBGInstallNotification();
            if (!UpdaterEngineStateHandler.isBusy() || UpdaterEngineHelper.cancelUpdateEngine()) {
                return;
            }
            clearUEInstallerBeforeExit();
        } else if (packageState == ApplicationEnv.PackageState.Querying || packageState == ApplicationEnv.PackageState.QueryingInstall || packageState == ApplicationEnv.PackageState.Upgrading) {
            this._env.getUtilities().cancelRestartNotification();
            UpdaterEngineHelper.resetUpdateEngine();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class UECallbackHandler implements CallBackInterface {
        private UECallbackHandler() {
        }

        private synchronized void onApplyPayload() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
            if (ABUpdate.this._settings.getBoolean(Configs.VAB_MERGE_PROCESS_RUNNING, false)) {
                return;
            }
            if (!UpdaterEngineInstaller.getApplyPayloadStarted()) {
                UpdaterEngineInstaller.setApplyPayloadStarted(true);
                if (BuildPropReader.isATT() && ABUpdate.this._settings.getBoolean(Configs.BATTERY_LOW)) {
                    InstallerUtilMethods.sendUpgradeStatus(ABUpdate.this.context, ABUpdate.this._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f), ABUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0), -1, InstallTypeResolver.InstallerType.AB);
                    ABUpdate.this.clearUEInstallerBeforeExit();
                    return;
                }
                UpdaterEngineHelper.applyPayload(InstallerUtilMethods.getDownloadUrl(), InstallerUtilMethods.getOffSetValue(), 0L, InstallerUtilMethods.getHeaderKeyValuePair());
                InstallerUtilMethods.sendActionABApplyPayloadStarted(OtaApplication.getGlobalContext());
                InstallerUtilMethods.acquireWakeLock();
                ABUpdate.this.setExpiryAlarmForUE(0, 0.0f);
            }
        }

        @Override // com.motorola.ccc.ota.installer.updaterEngine.common.CallBackInterface
        public void onProgress(int i, float f) {
            if (i == 0) {
                try {
                    onApplyPayload();
                } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
                    Logger.error("OtaApp", "Caught an exception during apply payload: " + e);
                    onCompleted(UpdaterEngineErrorCodes.ERROR_EXCEPTION, "Caught an exception during apply payload " + e, ErrorCodeMapper.KEY_APPLY_FAILURE);
                } catch (InvocationTargetException e2) {
                    Logger.error("OtaApp", "Exception in ABUpdate: onProgress: " + e2);
                    if (e2.getCause() == null || e2.getCause().getMessage() == null || !e2.getCause().getMessage().contains("waiting for reboot")) {
                        onCompleted(UpdaterEngineErrorCodes.ERROR_EXCEPTION, "Caught an exception duringapply payload " + e2, ErrorCodeMapper.KEY_APPLY_FAILURE);
                        return;
                    }
                    Logger.debug("OtaApp", "Exception cause is " + e2.getCause().getMessage());
                    onCompleted(0, null, UpdaterEngineErrorCodes.getFailureResultStatus(0));
                }
            } else if (i == 6 && !UpdaterEngineInstaller.getApplyPayloadStarted()) {
                onCompleted(UpdaterEngineErrorCodes.K_SUCCESS, null, UpdaterEngineErrorCodes.getFailureResultStatus(UpdaterEngineErrorCodes.K_SUCCESS));
            } else {
                UpdaterEngineInstaller.setApplyPayloadStarted(true);
                float f2 = f * 100.0f;
                ApplicationEnv.Database.Descriptor description = ABUpdate.this._db.getDescription(ABUpdate.this.mVersion);
                if (description == null || description.getMeta() == null) {
                    return;
                }
                if (description.getMeta().showDownloadProgress() && (f2 > ABUpdate.this._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f) || f2 == 0.0f)) {
                    InstallerUtilMethods.sendUpgradeStatus(OtaApplication.getGlobalContext(), f2, i, 0, InstallTypeResolver.InstallerType.AB);
                }
                ABUpdate.this._settings.setFloat(Configs.STORED_AB_PROGRESS_PERCENT, f2);
                ABUpdate.this._settings.setInt(Configs.STORED_AB_STATUS, i);
            }
        }

        @Override // com.motorola.ccc.ota.installer.updaterEngine.common.CallBackInterface
        public void onCompleted(int i, String str, String str2) {
            Logger.debug("OtaApp", "ABUpdate:onCompleted: errorcode " + i + " Reason " + str);
            if (i == UpdaterEngineErrorCodes.K_SUCCESS) {
                UpdaterEngineInstaller.updaterEngineStateHandler.transferUpgradeBackToOta(OtaApplication.getGlobalContext(), true, str, str2);
            } else if (i == UpdaterEngineErrorCodes.K_DOWNLOAD_OPERATION_EXECUTION_ERROR) {
                ApplicationEnv.Database.Descriptor description = ABUpdate.this._db.getDescription(ABUpdate.this.mVersion);
                if (description != null && InstallerUtilMethods.isMemoryLowForBackgroundInstallation(ABUpdate.this._env, ABUpdate.this._settings, description)) {
                    OtaApplication.getGlobalContext().sendBroadcast(new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE), Permissions.INTERACT_OTA_SERVICE);
                    ABUpdate.this.clearUEInstallerBeforeExit();
                    return;
                }
                UpdaterEngineInstaller.updaterEngineStateHandler.transferUpgradeBackToOta(OtaApplication.getGlobalContext(), false, str, str2);
                UpdaterEngineHelper.resetUpdateEngine();
            } else if (i == UpdaterEngineErrorCodes.K_USER_CANCELED) {
                if (InstallerUtilMethods.InstallerErrorStatus.STATUS_USER_CANCEL == ABUpdate.mInstallerStatus) {
                    InstallerUtilMethods.sendCancelBackgroundInstallationResponse(ABUpdate.this.context);
                    ABUpdate.this.clearUEInstallerBeforeExit();
                } else if (InstallerUtilMethods.InstallerErrorStatus.STATUS_UE_NOT_RESPONDING == ABUpdate.mInstallerStatus) {
                    UpdaterEngineInstaller.updaterEngineStateHandler.transferUpgradeBackToOta(OtaApplication.getGlobalContext(), false, "Updater engine is not responding at , status " + ABUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0) + " percentage " + ABUpdate.this._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f) + SystemUpdateStatusUtils.SPACE, ErrorCodeMapper.KEY_UE_NOT_RESPONDING);
                } else {
                    ABUpdate.this.clearUEInstallerBeforeExit();
                    if (BuildPropReader.isATT() && ABUpdate.this._settings.getBoolean(Configs.BATTERY_LOW)) {
                        InstallerUtilMethods.sendUpgradeStatus(ABUpdate.this.context, ABUpdate.this._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f), ABUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0), -1, InstallTypeResolver.InstallerType.AB);
                        return;
                    }
                }
            } else {
                UpdaterEngineInstaller.updaterEngineStateHandler.transferUpgradeBackToOta(OtaApplication.getGlobalContext(), false, str, str2);
                UpdaterEngineHelper.resetUpdateEngine();
            }
            ABUpdate.this._settings.setFloat(Configs.STORED_AB_PROGRESS_PERCENT, -1.0f);
        }
    }

    private InstallerUtilMethods.InstallerErrorStatus doSanityForABUpdate(ApplicationEnv.Database.Descriptor descriptor, SystemUpdaterPolicy systemUpdaterPolicy) {
        if (systemUpdaterPolicy.isOtaUpdateDisabledByPolicyMngr()) {
            NotificationUtils.cancelOtaNotification();
            return InstallerUtilMethods.InstallerErrorStatus.STATUS_SYSTEM_UPDATE_CANCEL_POLICY_SET;
        } else if (systemUpdaterPolicy.shouldIBlockUpdateForSystemPolicy(descriptor, this._settings)) {
            NotificationUtils.cancelOtaNotification();
            return InstallerUtilMethods.InstallerErrorStatus.STATUS_SYSTEM_UPDATE_POLICY_SET;
        } else if (UpdaterEngineStateHandler.isBusy()) {
            return InstallerUtilMethods.InstallerErrorStatus.STATUS_BUSY;
        } else {
            if (!new File(FileUtils.getLocalPath(this._settings)).exists()) {
                return InstallerUtilMethods.InstallerErrorStatus.STATUS_NO_FILE;
            }
            if (InstallerUtilMethods.isMemoryLowForBackgroundInstallation(this._env, this._settings, descriptor)) {
                long j = this._settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L);
                if (j > 0 && System.currentTimeMillis() >= j) {
                    return InstallerUtilMethods.InstallerErrorStatus.STATUS_FOTA_LOW_MEMORY;
                }
                return InstallerUtilMethods.InstallerErrorStatus.STATUS_LOW_SPACE;
            }
            return InstallerUtilMethods.InstallerErrorStatus.STATUS_OK;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class ABUpdateBroadcastReceiver extends BroadcastReceiver {
        private ABUpdateBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.debug("OtaApp", "ABUpdate:UEMonitorBroadcastReceiver:onReceive:action=" + intent.getAction());
            String action = intent.getAction();
            action.hashCode();
            if (action.equals(InstallerUtilMethods.INTENT_MAX_ALARM_FOR_UE)) {
                handleUEMaxTimerExpiry(intent);
            } else if (action.equals(UpgradeUtilConstants.ACTION_BATTERY_LOW)) {
                UpdaterEngineHelper.cancelUpdateEngine();
            }
        }

        private void handleUEMaxTimerExpiry(Intent intent) {
            int i = ABUpdate.this._settings.getInt(Configs.STORED_AB_STATUS, 0);
            float f = ABUpdate.this._settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f);
            int intExtra = intent.getIntExtra(InstallerUtilMethods.KEY_STATUS, 0);
            float floatExtra = intent.getFloatExtra(InstallerUtilMethods.KEY_PERCENTAGE, 0.0f);
            if (i > intExtra || (i == intExtra && f > floatExtra)) {
                ABUpdate.this.setExpiryAlarmForUE(i, f);
            } else if (UpdaterEngineStateHandler.isBusy()) {
                ABUpdate.mInstallerStatus = InstallerUtilMethods.InstallerErrorStatus.STATUS_UE_NOT_RESPONDING;
                if (UpdaterEngineHelper.cancelUpdateEngine()) {
                    return;
                }
                ABUpdate.this.callbackHandler.onCompleted(-1, "Updater engine is not responding at , status " + i + " percentage " + f + SystemUpdateStatusUtils.SPACE, ErrorCodeMapper.KEY_UE_NOT_RESPONDING);
            }
        }
    }

    private void registerABUpdateBroadcastReceiver() {
        if (this.abUpdateBroadcastReceiver == null) {
            this.abUpdateBroadcastReceiver = new ABUpdateBroadcastReceiver();
        }
        if (BuildPropReader.isATT()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(UpgradeUtilConstants.ACTION_BATTERY_LOW);
            BroadcastUtils.registerLocalReceiver(this.context, this.abUpdateBroadcastReceiver, intentFilter);
        }
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(InstallerUtilMethods.INTENT_MAX_ALARM_FOR_UE);
        this.context.registerReceiver(this.abUpdateBroadcastReceiver, intentFilter2, 4);
    }

    public void setExpiryAlarmForUE(int i, float f) {
        Intent intent = new Intent(InstallerUtilMethods.INTENT_MAX_ALARM_FOR_UE);
        intent.putExtra(InstallerUtilMethods.KEY_PERCENTAGE, f);
        intent.putExtra(InstallerUtilMethods.KEY_STATUS, i);
        this.mPendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, 335544320);
        this.mAlarmManager.setExactAndAllowWhileIdle(0, System.currentTimeMillis() + InstallerUtilMethods.MAX_ALARM_TIME_FOR_UE, this.mPendingIntent);
    }

    public void cancelPendingExpiryAlarmSetForUE() {
        PendingIntent pendingIntent = this.mPendingIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
        }
    }
}
