package com.motorola.ccc.ota;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.motorola.ccc.ota.NewVersionHandler;
import com.motorola.ccc.ota.env.AndroidFotaInterface;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.installer.InstallTypeResolver;
import com.motorola.ccc.ota.installer.updaterEngine.UpdaterEngineInstaller;
import com.motorola.ccc.ota.installer.updaterEngine.common.InstallerUtilMethods;
import com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineStateHandler;
import com.motorola.ccc.ota.sources.BotaFotaResolver;
import com.motorola.ccc.ota.sources.UpgradeSource;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.UpgradeSources;
import com.motorola.ccc.ota.sources.UpgradeStatusConstents;
import com.motorola.ccc.ota.sources.bota.PayloadMetaDataDownloader;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtilConstants;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtils;
import com.motorola.ccc.ota.sources.fota.FotaConstants;
import com.motorola.ccc.ota.sources.fota.FotaUpgradeSource;
import com.motorola.ccc.ota.stats.StatsHelper;
import com.motorola.ccc.ota.stats.StatsListener;
import com.motorola.ccc.ota.ui.FileUploadService;
import com.motorola.ccc.ota.ui.MessageActivity;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.CusUtilMethods;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.DmSendAlertService;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.MetadataOverrider;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.OtaAppContentProvider;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.TestUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.cdsservice.responsedataobjects.StateResponse;
import com.motorola.otalib.cdsservice.responsedataobjects.builders.StateResponseBuilder;
import com.motorola.otalib.cdsservice.webdataobjects.WebResponse;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.metaData.CheckForUpgradeTriggeredBy;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public final class CusSM {
    private static DownloadHandler _downloader;
    private static InstallTypeResolver.Installer _installType;
    private static int _isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
    private static Timer _mTimer = null;
    private ApplicationEnv.Database _db;
    private final ApplicationEnv _env;
    private boolean _isRoaming;
    private final StatsListener _sl;
    private final UpgradeSources _upgradePlugins;
    private final NewVersionHandler _versionhandler;
    private final ConnectivityManager cm;
    private final BotaSettings settings;
    private final SystemUpdaterPolicy systemUpdaterPolicy;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static class STATE_EVENT_STATUS {
        public static int IDLE = 0;
        public static int SENDING = 1;
        public static int WAITING_FOR_NETWORK = 2;
    }

    public CusSM(ApplicationEnv applicationEnv, ConnectivityManager connectivityManager, BotaSettings botaSettings) {
        this._env = applicationEnv;
        this.settings = botaSettings;
        this.cm = connectivityManager;
        Logger.debug("OtaApp", "CusSM.CusSM: constructing a CusSM object");
        SMNewVersionHandler sMNewVersionHandler = new SMNewVersionHandler();
        this._versionhandler = sMNewVersionHandler;
        this._upgradePlugins = new UpgradeSources(this, sMNewVersionHandler, applicationEnv, botaSettings);
        this._db = applicationEnv.createDatabase();
        this._sl = new StatsListener(connectivityManager, botaSettings);
        this.systemUpdaterPolicy = new SystemUpdaterPolicy();
    }

    public synchronized void onStart() {
        Logger.debug("OtaApp", "CusSM.onStart");
        if (UpdaterUtils.isFeatureOn(this.settings.getString(Configs.STATS_FEATURE))) {
            this._sl.startStatsListener();
        }
        initializeInstaller();
        initializePlugins();
        checkForUpgradeCompleted();
        checkForUpdateInSDCard("sdcard initiated on start");
        checkIfWifiDiscoveryTimerExpired();
        checkIfForceUpgradeTimerExpired();
        checkIfForceDownloadTimerExpired();
        checkAndSendRebootDuringDLNotification();
        checkAndCreateReserveSpace();
        checkAndSendRebootDuringABApplyingUpgradingState();
        runStateMachine();
        SmartUpdateUtils.decideToShowSmartUpdateSuggestion(OtaApplication.getGlobalContext());
    }

    public synchronized void onDestroy() {
        Logger.debug("OtaApp", "CusSM.onDestroy");
        ApplicationEnv.Database database = this._db;
        if (database != null) {
            database.close();
            this._db = null;
        }
        DownloadHandler downloadHandler = _downloader;
        if (downloadHandler != null) {
            downloadHandler.close();
            _downloader = null;
        }
        releasePlugins();
    }

    public synchronized void onPollingExpiryNotification(boolean z, int i, boolean z2) {
        Logger.debug("OtaApp", "CusSM.onPollingExpiryNotification");
        if (BuildPropReader.isCtaVersion(this.settings)) {
            Logger.debug("OtaApp", "Cta Version, should not allow polling request to avoid ota traffic");
        } else {
            this._upgradePlugins.getUpgradeSource(UpgradeSourceType.upgrade).checkForUpdate(z, i, z2);
        }
    }

    public synchronized void onModemPollingExpiryNotification(boolean z, boolean z2) {
        Logger.debug("OtaApp", "CusSM.onModemPollingExpiryNotification");
        UpgradeSource upgradeSource = this._upgradePlugins.getUpgradeSource(UpgradeSourceType.modem);
        if (!z2) {
            String deviceModemConfigVersionSha1 = BuildPropReader.getDeviceModemConfigVersionSha1();
            ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceModemConfigVersionSha1);
            if (description != null && description.getState() == ApplicationEnv.PackageState.WaitingForModemUpdateStatus) {
                if (System.currentTimeMillis() < this.settings.getLong(Configs.MODEM_FILE_DL_NOTIFIED_TIMESTAMP, 0L)) {
                    Logger.debug("OtaApp", "Ota is waiting for modem update status, so no check request; return from here");
                    return;
                } else if (upgradeSource.isUpdateSuccessful(description.getMeta(), UpgradeSourceType.modem.name())) {
                    this._db.setState(deviceModemConfigVersionSha1, ApplicationEnv.PackageState.Result, true, "Device is already upgraded to target version but result status is not received from modem, status waiting time period is expired", ErrorCodeMapper.KEY_SUCCESS);
                    cleanupVersion(deviceModemConfigVersionSha1);
                } else {
                    cancelModemUpdate("Modem Update is cancelled due to expiry of modem update waiting time period , version: " + deviceModemConfigVersionSha1, ErrorCodeMapper.KEY_MODEM_CANCELED_BY_SERVER);
                }
            }
            this.settings.setString(Configs.MODEM_CONFIG_VERSIONS, new JSONObject(BuildPropReader.getMCFGConfigVersionMap()).toString());
            this.settings.incrementPrefs(Configs.MODEM_POLLING_COUNT);
            if (this.settings.getInt(Configs.MODEM_POLLING_COUNT, 0) >= this.settings.getInt(Configs.MAX_MODEM_POLLING_COUNT, 7)) {
                UpgradeUtilMethods.shutdownModemPolling();
            }
        }
        if (TextUtils.isEmpty(BuildPropReader.getDeviceModemConfigVersionSha1())) {
            Logger.debug("OtaApp", "MCFG Config version is empty no check request, check if any further carrier is available");
            if (UpdaterUtils.canISendSuccessiveModemPollRequest()) {
                onModemPollingExpiryNotification(false, true);
            }
            return;
        }
        upgradeSource.checkForUpdate(z);
    }

    private void intimateModem() {
        Logger.debug("OtaApp", "CusSM.intimateModem");
        String modemDownloadFilePath = FileUtils.getModemDownloadFilePath();
        File file = new File(modemDownloadFilePath);
        if (!file.exists()) {
            Logger.debug("OtaApp", "CusSM.intimateModem, no modem file: " + modemDownloadFilePath);
            cancelModemUpdate("Modem Update is cancelled because no downloaded modem file, version: " + BuildPropReader.getDeviceModemConfigVersionSha1(), ErrorCodeMapper.KEY_MODEM_CANCELED_BY_SERVER);
            return;
        }
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_MODEM_UPDATE);
        Uri uriForFile = FileProvider.getUriForFile(OtaApplication.getGlobalContext(), "com.motorola.ccc.ota.fileprovider", file);
        OtaApplication.getGlobalContext().grantUriPermission("com.motorola.bach.modemstats", uriForFile, 1);
        intent.setDataAndType(uriForFile, "application/zip");
        Intent flags = intent.setFlags(1);
        flags.setPackage("com.motorola.bach.modemstats");
        OtaApplication.getGlobalContext().sendBroadcast(flags, Permissions.INTERACT_OTA_SERVICE);
    }

    private void moveOtaToModemWaitingState() {
        String deviceModemConfigVersionSha1 = BuildPropReader.getDeviceModemConfigVersionSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceModemConfigVersionSha1);
        if (description == null) {
            Logger.debug("OtaApp", "CusSM.onModemFileReadStatus: version is not in db");
        } else if (description.getState() != ApplicationEnv.PackageState.IntimateModem) {
            Logger.debug("OtaApp", "CusSM.onModemFileReadStatus failed: version " + deviceModemConfigVersionSha1 + " that is in state " + description.getState().toString() + " (expected state IntimateModem)");
        } else {
            this.settings.setLong(Configs.MODEM_FILE_DL_NOTIFIED_TIMESTAMP, System.currentTimeMillis() + InstallerUtilMethods.MAX_ALARM_TIME_FOR_DL_MODEM);
            this._db.setVersionState(BuildPropReader.getDeviceModemConfigVersionSha1(), ApplicationEnv.PackageState.WaitingForModemUpdateStatus, null);
            runStateMachine();
        }
    }

    public void onModemUpdateStatusResult(int i, String str) {
        String deviceModemConfigVersionSha1 = BuildPropReader.getDeviceModemConfigVersionSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceModemConfigVersionSha1);
        if (description == null) {
            Logger.debug("OtaApp", "CusSM.onModemUpdateStatusResult: version is not in db");
        } else if (description.getState() != ApplicationEnv.PackageState.WaitingForModemUpdateStatus) {
            Logger.debug("OtaApp", "CusSM.onModemUpdateStatusResult failed: version " + deviceModemConfigVersionSha1 + " that is in state " + description.getState().toString() + " (expected state WaitingForModemUpdateStatus)");
        } else {
            if (i == 200) {
                this._db.setState(deviceModemConfigVersionSha1, ApplicationEnv.PackageState.Result, true, SystemUpdateStatusUtils.KEY_UPATE_SUCCESS, ErrorCodeMapper.KEY_SUCCESS);
                UpgradeUtilMethods.shutdownModemPolling();
                runStateMachine();
            } else {
                failProgress(deviceModemConfigVersionSha1, UpgradeUtils.DownloadStatus.STATUS_FAIL, "Modem update is failed : " + str, ErrorCodeMapper.KEY_MODEM_UPDATE_FAILED);
            }
            cleanupVersion(deviceModemConfigVersionSha1);
        }
    }

    public synchronized void onInternalNotification(String str, String str2, String str3) {
        Logger.debug("OtaApp", "CusSM.onInternalNotification: from Repo " + str2);
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        DownloadHandler downloadHandler = _downloader;
        if (downloadHandler != null) {
            downloadHandler.close();
            _downloader = null;
        }
        if (description == null) {
            Logger.debug("OtaApp", "CusSM.onInternalNotification: version is not in db");
        } else if (description.getState() != ApplicationEnv.PackageState.GettingPackage) {
            Logger.error("OtaApp", "CusSM.onInternalNotification failed: notification for version " + str + " that is in state " + description.getState().toString() + " (expected state GettingPackage)");
        } else {
            if (this.settings.getLong(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME, -1L) > 0) {
                this.settings.removeConfig(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME);
                this._env.getUtilities().unRegisterWithForceUpgradeManager();
            }
            _installType = InstallTypeResolver.getInstallTypeHandler(this._env, this.settings, this._db, description, this);
            if (str2.equals(UpgradeSourceType.sdcard.toString()) && str3 != null) {
                Logger.error("OtaApp", "CusSM.onInternalNotification failed: copy from sdcard is hosed; Reason: " + str3);
                failDownload(str, UpgradeUtils.DownloadStatus.STATUS_COPYFAIL, str3, ErrorCodeMapper.KEY_OTHER);
            } else if (description != null && !description.getMeta().showPreInstallScreen() && !description.getMeta().getRebootRequired()) {
                Logger.info("OtaApp", "package configured with showPreInstallScreen and rebootRequired to false proceed with upgrading");
                UpdaterUtils.sendInstallModeStats("policyBundleUpdate");
                this._db.setState(str, ApplicationEnv.PackageState.Upgrading, str3);
            } else {
                _installType.onInternalNotification(str3, description);
            }
            runStateMachine();
        }
    }

    public synchronized void moveFotaToGettingDescriptorState() {
        Logger.debug("OtaApp", "CusSm.moveFotaToGettingDescriptorState ");
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSMmoveFotaToGettingDescriptorState.: version is not in db " + deviceSha1);
            return;
        }
        if (description.getRepository().equals(UpgradeSourceType.fota.toString()) && description.getState() == ApplicationEnv.PackageState.Notified) {
            Logger.debug("OtaApp", "CusSM.moveFotaToGettingDescriptorState");
            this._db.setState(deviceSha1, ApplicationEnv.PackageState.GettingDescriptor, null);
            runStateMachine();
            return;
        }
        Logger.error("OtaApp", String.format("CusSM.moveFotaToGettingDescriptorState failed: Either repository is not fota " + description.getRepository() + "or not in expected state Notified " + description.getState().toString(), new Object[0]));
    }

    public synchronized void onStartDownloadNotification(String str) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onStartDownloadNotification: notification for version is not in db " + str);
        } else if (description.getState() != ApplicationEnv.PackageState.GettingDescriptor) {
            Logger.error("OtaApp", String.format("CusSM.onStartDownloadNotification failed: notification for version %s that is in state %s (expected state GettingDescriptor)", str, description.getState().toString()));
        } else {
            Logger.debug("OtaApp", "CusSM.onStartDownloadNotification : version=" + str);
            String string = this.settings.getString(Configs.FLAVOUR);
            if (string != null && (!string.equals(UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI.toString()) || !description.getRepository().equals(UpgradeSourceType.upgrade.toString()))) {
                string = null;
            }
            _installType = InstallTypeResolver.getInstallTypeHandler(this._env, this.settings, this._db, description, this);
            UpdaterUtils.checkAndEnableBatteryStatusReceiver();
            _installType.onStartDownloadNotification(string);
            runStateMachine();
        }
    }

    public synchronized void onActionGetDescriptor(String str, boolean z, String str2) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onActionGetDescriptor: notification for version is not in db " + str);
        } else if (description.getState() != ApplicationEnv.PackageState.GettingPackage && description.getState() != ApplicationEnv.PackageState.ABApplyingPatch && description.getState() != ApplicationEnv.PackageState.VerifyPayloadMetadata) {
            Logger.error("OtaApp", String.format("CusSM.onActionGetDescriptor failed: notification for version %s that is in state %s (expected state GettingPackage)", str, description.getState().toString()));
        } else {
            if (_downloader != null) {
                Logger.debug("OtaApp", "CusSM.onActionGetDescriptor: reset download status flag");
                _downloader.close();
                _downloader = null;
            }
            Logger.info("OtaApp", str2);
            if (BuildPropReader.isFotaATT()) {
                this._db.setState(str, ApplicationEnv.PackageState.GettingDescriptor, null);
            } else if (z) {
                this._db.setState(str, ApplicationEnv.PackageState.GettingDescriptor, str2);
            } else {
                this._db.setVersionState(str, ApplicationEnv.PackageState.GettingDescriptor, str2);
            }
            runStateMachine();
        }
    }

    public synchronized void onRadioUp() {
        Logger.debug("OtaApp", "CusSM.onRadioUp");
        if (_isSendingUpgradeStatus == STATE_EVENT_STATUS.WAITING_FOR_NETWORK) {
            _isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
        }
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(this._env.getServices().getDeviceSha1());
        if (description != null && description.getState() == ApplicationEnv.PackageState.ABApplyingPatch) {
            if (UpdaterEngineStateHandler.isBusy() && !NetworkUtils.checkWhetherUserDisabledCellularNetwork(this.cm) && NetworkUtils.isWifi(this.cm)) {
                UpgradeUtilMethods.sendUpgradeStatusSuspended(OtaApplication.getGlobalContext());
                return;
            }
        } else if (CusAndroidUtils.isDeviceInDatasaverMode()) {
            Logger.debug("OtaApp", "User enabled data saver, can not access background data");
            DownloadHandler downloadHandler = _downloader;
            if (downloadHandler != null) {
                downloadHandler.radioGotDown();
            }
            return;
        }
        runStateMachine();
    }

    public synchronized void onRadioDown() {
        Logger.debug("OtaApp", "CusSM.onRadioDown");
        DownloadHandler downloadHandler = _downloader;
        if (downloadHandler != null) {
            downloadHandler.radioGotDown();
        }
    }

    public synchronized void onRoaming() {
        this._isRoaming = true;
    }

    public synchronized void onNotRoaming() {
        this._isRoaming = false;
        runStateMachine();
    }

    public synchronized boolean isRoaming() {
        return this._isRoaming;
    }

    public synchronized void onSimStateChanged(boolean z) {
        MetaData overWriteAttFirstNetUpdateReminderValues;
        Context globalContext = OtaApplication.getGlobalContext();
        BroadcastUtils.sendLocalBroadcast(globalContext, new Intent(UpgradeUtilConstants.REFRESH_CHKUPDATE_UI_ON_SIMCHANGE));
        if (z) {
            if (CusUtilMethods.isItFirstNetOnFota(globalContext)) {
                MetaData overWriteAttFirstNetUpdateReminderValues2 = MetadataOverrider.overWriteAttFirstNetUpdateReminderValues(getMetaData(), UpgradeUtils.DEFAULT_CRITICAL_UPDATE_ANNOY_VALUE, 3);
                if (overWriteAttFirstNetUpdateReminderValues2 != null) {
                    Logger.debug("OtaApp", "CusSM:FirstNet sim loaded, over write firstnet critical reminder values");
                    overWriteMetaData(overWriteAttFirstNetUpdateReminderValues2);
                }
            } else {
                if (BuildPropReader.isBotaATT()) {
                    MetaData from = MetaDataBuilder.from(this.settings.getString(Configs.METADATA_ORIGINAL));
                    overWriteAttFirstNetUpdateReminderValues = from != null ? MetadataOverrider.overWriteAttFirstNetUpdateReminderValues(getMetaData(), from.getCriticalUpdateReminder(), from.getCriticalDeferCount()) : null;
                } else if (this.settings.getBoolean(Configs.FOTA_ORIGINAL_FORCED)) {
                    overWriteAttFirstNetUpdateReminderValues = MetadataOverrider.overWriteAttFirstNetUpdateReminderValues(getMetaData(), 0, 0);
                } else {
                    overWriteAttFirstNetUpdateReminderValues = MetadataOverrider.overWriteAttFirstNetUpdateReminderValues(getMetaData(), UpgradeUtils.DEFAULT_CRITICAL_UPDATE_ANNOY_VALUE, 3);
                }
                if (overWriteAttFirstNetUpdateReminderValues != null) {
                    Logger.debug("OtaApp", "CusSM:Att/Cricket (non FirstNet) sim loaded, over write with respective critical reminder values");
                    overWriteMetaData(overWriteAttFirstNetUpdateReminderValues);
                }
                if (this.settings.getBoolean(Configs.IS_UPDATE_PENDING_ON_REBOOT)) {
                    Logger.debug("OtaApp", "first net sim removed");
                    NotificationUtils.stopNotificationService(globalContext);
                    UpdaterUtils.notifyRecoveryAboutPendingUpdate(false);
                    this.settings.setBoolean(Configs.IS_UPDATE_PENDING_ON_REBOOT, false);
                    runStateMachine();
                }
            }
        }
    }

    public synchronized boolean handleStateWebResponse(WebResponse webResponse, int i, String str, String str2) {
        if (this._db.get_status(i) == null) {
            Logger.debug("OtaApp", "duplicate state response with id " + i);
            return false;
        } else if (webResponse.getStatusCode() != 200) {
            Logger.error("OtaApp", "CusSM.handleStateWebResponse: received http error " + webResponse.getStatusCode());
            if (webResponse.getStatusCode() != 0) {
                _isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
            } else {
                _isSendingUpgradeStatus = STATE_EVENT_STATUS.WAITING_FOR_NETWORK;
            }
            if (isServiceControlEnabled(str2)) {
                stopTimer();
                if (BuildPropReader.getDeviceModemConfigVersionSha1().equals(str2)) {
                    this.settings.setString(Configs.MODEM_SERVICE_CONTROL_RESPONSE, "continue");
                } else {
                    this.settings.setString(Configs.SERVICE_CONTROL_RESPONSE, "continue");
                }
                runStateMachine();
            }
            return false;
        } else {
            StateResponse from = StateResponseBuilder.from(webResponse.getPayload());
            if (from == null) {
                _isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
                if (isServiceControlEnabled()) {
                    stopTimer();
                    this.settings.setString(Configs.SERVICE_CONTROL_RESPONSE, "continue");
                    this.settings.setString(Configs.MODEM_SERVICE_CONTROL_RESPONSE, "continue");
                    runStateMachine();
                }
                return false;
            }
            if (BuildPropReader.getDeviceModemConfigVersionSha1().equals(str2)) {
                handleModemStateResponse(from, i, str);
            } else {
                handleStateResponse(from, i, str);
            }
            return true;
        }
    }

    private void handleStateResponse(StateResponse stateResponse, int i, String str) {
        Logger.debug("OtaApp", "Inside handleStateResponse");
        Context globalContext = OtaApplication.getGlobalContext();
        TestUtils.sendStateResponse(globalContext, str);
        _isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
        String string = this.settings.getString(Configs.TRACKINGID);
        String trackingId = stateResponse.getTrackingId();
        String contextKey = stateResponse.getContextKey();
        int smartUpdateBitmap = stateResponse.getSmartUpdateBitmap();
        if (smartUpdateBitmap >= 0) {
            SmartUpdateUtils.storeBits(this.settings, smartUpdateBitmap);
        }
        if (stateResponse.isUploadFailureLogsEnabled()) {
            this.settings.setBoolean(Configs.IS_UPLOAD_UE_FAILURE_LOGS_ENABLED, true);
            if (FileUploadService.canUploadLogFile(new BotaSettings())) {
                Logger.debug("OtaApp", "CusSM: Uploading UE log file to sever");
                new FileUploadService().uploadUEFailureFiles();
            }
        } else {
            this.settings.setBoolean(Configs.IS_UPLOAD_UE_FAILURE_LOGS_ENABLED, false);
        }
        SmartUpdateUtils.decideToShowSmartUpdateSuggestion(globalContext);
        if (SmartUpdateUtils.isSmartUpdateEnabledByServer()) {
            OtaAppContentProvider.updateOtaAppContentProvider(OtaAppContentProvider.KEY_SMART_UPDATE, "true");
        } else {
            OtaAppContentProvider.updateOtaAppContentProvider(OtaAppContentProvider.KEY_SMART_UPDATE, "false");
        }
        this._db.remove_status(i);
        if (string != null && !BuildPropReader.isFotaATT() && string.equals(trackingId)) {
            if (stateResponse.proceed()) {
                if (stateResponse.getContextTimeStamp() != this.settings.getLong(Configs.CONTENT_TIMESTAMP, 0L) && isBusy()) {
                    Logger.debug("OtaApp", "mismatch in ContentTimeStamp: currentContentTimeStamp: " + this.settings.getLong(Configs.CONTENT_TIMESTAMP, 0L) + " responseContentTimeStamp: " + stateResponse.getContextTimeStamp() + "; updating the new MetaData info");
                    MetaData from = MetaDataBuilder.from(stateResponse.getContent());
                    MetaData from2 = MetaDataBuilder.from(this.settings.getString(Configs.METADATA));
                    if (from == null) {
                        Logger.info("OtaApp", "change in contentTimeStamp but there is no new content,so not able to update new content");
                    } else {
                        BotaSettings botaSettings = this.settings;
                        overWriteMetaData(MetadataOverrider.from(from, botaSettings, botaSettings.getString(Configs.TRIGGERED_BY), false, stateResponse.getReportingTags(), stateResponse.getTrackingId()));
                        this.settings.setLong(Configs.CONTENT_TIMESTAMP, stateResponse.getContextTimeStamp());
                        this.settings.setString(Configs.REPORTINGTAGS, stateResponse.getReportingTags());
                        this.settings.setString(Configs.TRACKINGID, stateResponse.getTrackingId());
                        ApplicationEnv.Database.Descriptor description = this._db.getDescription(this._env.getServices().getDeviceSha1());
                        if (description != null && description.getMeta() != null && description.getState() != ApplicationEnv.PackageState.Result) {
                            if (UpdaterUtils.isMaxUpdateFailCountExpired(description.getMeta().getMaxUpdateFailCount())) {
                                Logger.debug("OtaApp", "handleStateResponse, overridePDLValues on FAIL");
                                if (!description.getMeta().showPreDownloadDialog() || description.getMeta().getForceDownloadTime() > 0.0d) {
                                    MetaData overWriteMetaDataValuesOnOtaFailExpiry = MetadataOverrider.overWriteMetaDataValuesOnOtaFailExpiry(description.getMeta(), this.settings.getString(Configs.REPORTINGTAGS), this.settings.getString(Configs.TRACKINGID));
                                    if (overWriteMetaDataValuesOnOtaFailExpiry != null) {
                                        overWriteMetaData(overWriteMetaDataValuesOnOtaFailExpiry);
                                    } else {
                                        Logger.debug("OtaApp", "CusSM.handleStateResponse, null metadata,unable to override showPreDownloadDialog");
                                    }
                                }
                            }
                            if (ThinkShieldUtils.isAscDevice(globalContext)) {
                                MetaData overWriteASCMetaDataValues = MetadataOverrider.overWriteASCMetaDataValues(description.getMeta(), globalContext);
                                if (overWriteASCMetaDataValues != null) {
                                    overWriteMetaData(overWriteASCMetaDataValues);
                                } else {
                                    Logger.debug("OtaApp", "CusSM.handleStateResponse, null metadata,unable to override Metadata for ASC");
                                }
                            }
                            if (from2 != null && !description.getMeta().getAbInstallType().equals(from2.getAbInstallType())) {
                                stopTimer();
                                Logger.debug("OtaApp", "CusSm:Server changed abInstallType,canceling the OTA update");
                                cancelOTA("Server changed abInstallType", ErrorCodeMapper.KEY_OTHER);
                                return;
                            } else if (description.getState() == ApplicationEnv.PackageState.Querying || description.getState() == ApplicationEnv.PackageState.QueryingInstall) {
                                if (description.getMeta().getSeverity() == UpgradeUtils.SeverityType.CRITICAL.ordinal()) {
                                    Logger.debug("OtaApp", "CusSm:Server changed critical update value");
                                    CusUtilMethods.settingMaxDeferTimeForCriticalUpdate(description, this.settings);
                                } else {
                                    Logger.debug("OtaApp", "CusSm:Server changed from critical update value");
                                    this.settings.setLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L);
                                    this.settings.setLong(Configs.MAX_CRITICAL_UPDATE_EXTENDED_TIME, -1L);
                                }
                                if (description.getMeta().isForceInstallTimeSet() && this.settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L) == -1) {
                                    Logger.debug("OtaApp", "CusSm:Server changed force install value");
                                    this.settings.setLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, (long) (System.currentTimeMillis() + (description.getMeta().getForceInstallTime() * 24.0d * 60.0d * 60.0d * 1000.0d)));
                                } else {
                                    this.settings.setLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L);
                                }
                            } else if (description.getState() == ApplicationEnv.PackageState.RequestPermission) {
                                if (description.getMeta().getForceUpgradeTime() > 0 && this.settings.getLong(Configs.FORCE_UPGRADE_TIME, -1L) == -1) {
                                    CusUtilMethods.startForceUpgradeTimer(description.getMeta().getForceUpgradeTime(), this.settings, this._env);
                                    UpdaterUtils.sDeviceIdleModeRequired = true;
                                } else if (description.getMeta().getForceDownloadTime() >= 0.0d && this.settings.getLong(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME, -1L) == -1) {
                                    CusUtilMethods.startForceDownloadTimer(description.getMeta().getForceDownloadTime(), this.settings, this._env);
                                }
                            }
                        }
                    }
                }
                if (isServiceControlEnabled(contextKey)) {
                    stopTimer();
                    this.settings.setString(Configs.SERVICE_CONTROL_RESPONSE, "continue");
                    Logger.debug("OtaApp", "handleStateResponse server told to : " + this.settings.getString(Configs.SERVICE_CONTROL_RESPONSE));
                    runStateMachine();
                }
            } else if (isServiceControlEnabled(contextKey)) {
                stopTimer();
                this.settings.setString(Configs.SERVICE_CONTROL_RESPONSE, "cancel");
                Logger.debug("OtaApp", "handleStateResponse server told to : " + this.settings.getString(Configs.SERVICE_CONTROL_RESPONSE));
                cancelOTA(new String[0]);
            }
        } else {
            Logger.error("OtaApp", "CusSM:state request and response trackingId mismatch, return");
        }
        sendUpgradeStatus(contextKey);
    }

    private void handleModemStateResponse(StateResponse stateResponse, int i, String str) {
        Logger.debug("OtaApp", "Inside handleModemStateResponse");
        TestUtils.sendStateResponse(OtaApplication.getGlobalContext(), str);
        _isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
        String string = this.settings.getString(Configs.MODEM_TRACKINGID);
        String trackingId = stateResponse.getTrackingId();
        String contextKey = stateResponse.getContextKey();
        this._db.remove_status(i);
        if (string != null && string.equals(trackingId)) {
            if (stateResponse.proceed()) {
                if (stateResponse.getContextTimeStamp() != this.settings.getLong(Configs.MODEM_CONTENT_TIMESTAMP, 0L) && isModemBusy()) {
                    Logger.debug("OtaApp", "mismatch in modem's ContentTimeStamp: currentContentTimeStamp: " + this.settings.getLong(Configs.MODEM_CONTENT_TIMESTAMP, 0L) + " responseContentTimeStamp: " + stateResponse.getContextTimeStamp() + "; updating the new MetaData info");
                    MetaData from = MetaDataBuilder.from(stateResponse.getContent());
                    MetaData from2 = MetaDataBuilder.from(this.settings.getString(Configs.MODEM_METADATA));
                    if (from == null) {
                        Logger.info("OtaApp", "change in modem's contentTimeStamp but there is no new content,so not able to update new content");
                    } else {
                        overWriteMetaData(MetadataOverrider.from(from, this.settings, CheckForUpgradeTriggeredBy.polling.name(), false, stateResponse.getReportingTags(), stateResponse.getTrackingId()));
                        this.settings.setLong(Configs.MODEM_CONTENT_TIMESTAMP, stateResponse.getContextTimeStamp());
                        this.settings.setString(Configs.MODEM_REPORTINGTAGS, stateResponse.getReportingTags());
                        this.settings.setString(Configs.MODEM_TRACKINGID, stateResponse.getTrackingId());
                        ApplicationEnv.Database.Descriptor description = this._db.getDescription(BuildPropReader.getDeviceModemConfigVersionSha1());
                        if (from2 != null && !description.getMeta().getAbInstallType().equals(from2.getAbInstallType())) {
                            stopTimer();
                            Logger.debug("OtaApp", "CusSm:Server changed abInstallType,canceling the modem update");
                            cancelModemUpdate("Server changed abInstallType for Modem update", ErrorCodeMapper.KEY_OTHER);
                            return;
                        }
                    }
                }
                if (isServiceControlEnabled(contextKey)) {
                    stopTimer();
                    this.settings.setString(Configs.MODEM_SERVICE_CONTROL_RESPONSE, "continue");
                    Logger.debug("OtaApp", "handleModemStateResponse server told to : " + this.settings.getString(Configs.MODEM_SERVICE_CONTROL_RESPONSE));
                    runStateMachine();
                }
            } else if (isServiceControlEnabled(contextKey)) {
                stopTimer();
                this.settings.setString(Configs.MODEM_SERVICE_CONTROL_RESPONSE, "cancel");
                Logger.debug("OtaApp", "handleModemStateResponse server told to : " + this.settings.getString(Configs.MODEM_SERVICE_CONTROL_RESPONSE));
                cancelModemUpdate(new String[0]);
            }
        } else {
            Logger.error("OtaApp", "CusSM:handleModemStateResponse:state request and response trackingId mismatch, return");
        }
        sendUpgradeStatus(BuildPropReader.getDeviceModemConfigVersionSha1());
    }

    public void onIntentCheckForUpdate(boolean z, int i, boolean z2) {
        if (BuildPropReader.isCtaVersion(this.settings)) {
            Logger.debug("OtaApp", "Cta Version, showing permission dialog to allow or deny the updates");
            this._env.getUtilities().sendActionUpdateResponse(UpgradeUtils.Error.ERR_CTA_BG_DATA_DISABLED, i, false, null);
            return;
        }
        boolean checkIfAlreadyUpdating = checkIfAlreadyUpdating(i, z);
        if (!checkIfAlreadyUpdating) {
            checkForUpdateInSDCard("sdcard intiated by user");
            checkIfAlreadyUpdating = checkIfAlreadyUpdating(i, z);
            if (!TextUtils.isEmpty(this.settings.getString(Configs.UPDATING_VALIDATION_FILE))) {
                this._env.getUtilities().sendActionUpdateResponse(UpgradeUtils.Error.ERR_VAB_VALIDATION, i, z, this.settings.getString(Configs.METADATA));
                return;
            }
        }
        if (checkIfAlreadyUpdating) {
            runStateMachine();
            return;
        }
        this._env.getUtilities().sendActionUpdateResponse(UpgradeUtils.Error.ERR_REQUESTING, i, z, null);
        if (!new CusPolicy(this.cm).canICheckForUpdate(isRoaming())) {
            Logger.error("OtaApp", "[OTA] I can't check it, because of our policy - refer to CusPolicy!!");
            this._env.getUtilities().sendCheckForUpdateResponse(UpgradeUtils.Error.ERR_NET, i, z);
            return;
        }
        Logger.debug("OtaApp", "FOTA-BOTA check order is " + this.settings.getString(Configs.CHECK_ORDER));
        UpgradeSourceType resolveCheckForUpdateRepository = new BotaFotaResolver(this.settings.getString(Configs.CHECK_ORDER)).resolveCheckForUpdateRepository();
        if (z) {
            resolveCheckForUpdateRepository = UpgradeSourceType.bootstrap;
        }
        this._upgradePlugins.getUpgradeSource(resolveCheckForUpdateRepository).checkForUpdate(z, i, z2);
    }

    public synchronized void onIntentASCSessionDone(int i, long j, BotaSettings botaSettings) {
        long j2 = botaSettings.getLong(Configs.ASC_TRANSACTION_ID, -1L);
        if (j != j2) {
            Logger.debug("OtaApp", "Transaction id mismatch, ignore the session");
            return;
        }
        Context globalContext = OtaApplication.getGlobalContext();
        ThinkShieldUtils.cancelASCTimeoutAlarm(globalContext);
        botaSettings.removeConfig(Configs.ASC_SESSION_TIMEOUT_TIMESTAMP);
        botaSettings.removeConfig(Configs.ASC_TRANSACTION_ID);
        if (i == -2) {
            Logger.debug("OtaApp", "Intimating ASC about the OTA request session timout");
            Intent intent = new Intent(ThinkShieldUtilConstants.ACTION_ASC_OTA_TIMEOUT);
            intent.putExtra(ThinkShieldUtilConstants.EXTRA_TRANSACTION_ID, j2);
            globalContext.sendBroadcast(intent, Permissions.INTERACT_OTA_ASC_SERVICE);
        }
        UpgradeUtils.Error ascRequestUpdateResponse = ThinkShieldUtils.ascRequestUpdateResponse(i);
        Logger.debug("OtaApp", "ASC Session was completed; ascErrorCode=" + i + " , otaASCErrorCode=" + ascRequestUpdateResponse);
        this._env.getUtilities().sendCheckForUpdateResponse(ascRequestUpdateResponse, 2, false);
        if (ascRequestUpdateResponse == UpgradeUtils.Error.ERR_ASC_ALLOWED) {
            runStateMachine();
        } else {
            cancelOTA("Update is cancelled by ASC, version: " + this._env.getServices().getDeviceSha1(), ErrorCodeMapper.KEY_UPDATE_FAILED_BY_ASC);
        }
    }

    public void onIntentOtaServiceStop() {
        Context globalContext = OtaApplication.getGlobalContext();
        ApplicationEnv.Database database = this._db;
        if (database != null && database.getVersions() == null && this._db.get_status() == null) {
            if (!this.settings.getBoolean(Configs.INITIAL_SETUP_TRIGGERED, false)) {
                Logger.debug("OtaApp", "CusSM:onIntentOtaServiceStop - setup update is not done don't stop OTA service");
                return;
            }
            Logger.debug("OtaApp", "CusSM:onIntentOtaServiceStop - stopping ota service");
            UpdaterUtils.stopOtaService(globalContext);
            return;
        }
        Logger.debug("OtaApp", "CusSM:onIntentOtaServiceStop - Version or State tables are not empty, so OtaService can't be stopped");
    }

    public void onIntentHandleRebootDuringABUpdate() {
        String metaSourceSha1 = UpdaterUtils.getMetaSourceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(metaSourceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentHandleRebootDuringABUpdate: notification for version is not in db " + metaSourceSha1);
            return;
        }
        long availableDataPartitionSize = this._env.getUtilities().getAvailableDataPartitionSize();
        if (description.getState() == ApplicationEnv.PackageState.Querying || description.getState() == ApplicationEnv.PackageState.QueryingInstall) {
            Logger.error("OtaApp", "CusSM.onIntentHandleRebootDuringABUpdate: device rebooted during querying state");
            if (this.systemUpdaterPolicy.shouldICancelOngoingOtaUpdate(description, this.settings)) {
                cancelOTA("Device is under system update policy, can not update the device : policy type=" + this.systemUpdaterPolicy.getPolicyType() + " : OtaUpdateDisabled policy set = " + this.systemUpdaterPolicy.isOtaUpdateDisabledPolicySet() + " : freeze periods = " + this.systemUpdaterPolicy.getFreezePeriods(), ErrorCodeMapper.KEY_SYSTEM_UPDATE_POLICY);
            } else if (availableDataPartitionSize <= 52428800) {
                cancelOTA("Device on low memory, available space: " + availableDataPartitionSize, ErrorCodeMapper.KEY_DATA_OUT_OF_SPACE);
            } else {
                this.settings.setLong(Configs.BOOT_START_TIMESTAMP, System.currentTimeMillis());
                this._db.setState(metaSourceSha1, ApplicationEnv.PackageState.Upgrading, null);
                this.settings.incrementPrefs(Configs.UPGRADE_ATTEMPT_COUNT);
                UpdaterUtils.sendInstallModeStats("accidentalPowerOff");
            }
        } else if (description.getState() == ApplicationEnv.PackageState.Upgrading) {
            if (availableDataPartitionSize <= 52428800) {
                cancelOTA("Device on low memory, available space: " + availableDataPartitionSize, ErrorCodeMapper.KEY_DATA_OUT_OF_SPACE);
            }
        } else if (description.getState() == ApplicationEnv.PackageState.ABApplyingPatch) {
            Logger.error("OtaApp", "CusSM.onIntentHandleRebootDuringABUpdate: device rebooted during abapplying patch set");
            StatsHelper.setTotalInstallTime(this.settings);
        } else if (description.getState() == ApplicationEnv.PackageState.MergeRestart) {
            this.settings.incrementPrefs(Configs.STATS_VAB_MERGE_REBOOT_FAILURE_COUNT);
        }
    }

    public synchronized void onIntentDeviceIdleModeChanged() {
        boolean isDeviceInIdleMode = this._env.getUtilities().isDeviceInIdleMode();
        Logger.debug("OtaApp", "CusSM.onIntentDeviceIdleModeChanged, deviceIdle : " + isDeviceInIdleMode);
        if (isDeviceInIdleMode) {
            String metaSourceSha1 = UpdaterUtils.getMetaSourceSha1();
            ApplicationEnv.Database.Descriptor description = this._db.getDescription(metaSourceSha1);
            if (description == null) {
                Logger.error("OtaApp", "CusSM.onIntentDeviceIdleModeChanged: version is not in db " + metaSourceSha1);
                return;
            }
            if ((description.getState() == ApplicationEnv.PackageState.Querying || description.getState() == ApplicationEnv.PackageState.QueryingInstall) && ((description.getMeta().isForceInstallTimeSet() && System.currentTimeMillis() > this.settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L)) || !description.getMeta().showPreInstallScreen())) {
                runStateMachine();
            }
            if (description.getState() == ApplicationEnv.PackageState.MergeRestart) {
                Logger.debug("OtaApp", "Device is in MergeRestart state, so rebooting device in doze mode");
                String string = this.settings.getString(Configs.STATS_VAB_MERGE_RESTARTED_BY);
                this.settings.setString(Configs.STATS_VAB_MERGE_RESTARTED_BY, ((string == null || string.equalsIgnoreCase("null")) ? "" : "") + ",Doze Mode");
                UpgradeUtilMethods.sendMergeRestartIntent(OtaApplication.getGlobalContext());
            }
        }
    }

    public synchronized void onIntentDeviceDatasaverModeChanged() {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentDeviceOnDataSaverModeChanged: notification for version is not in db " + deviceSha1);
            return;
        }
        if (CusAndroidUtils.isDeviceInDatasaverMode()) {
            Logger.debug("OtaApp", "User enabled data saver, can not access background data");
            DownloadHandler downloadHandler = _downloader;
            if (downloadHandler != null) {
                downloadHandler.radioGotDown();
            } else if (BuildPropReader.isStreamingUpdate() && description.getState() == ApplicationEnv.PackageState.ABApplyingPatch) {
                UpgradeUtilMethods.sendActionDataSaverDuringABStreaming();
            }
        } else if (description.getState() == ApplicationEnv.PackageState.RequestPermission || description.getState() == ApplicationEnv.PackageState.GettingDescriptor || description.getState() == ApplicationEnv.PackageState.GettingPackage || description.getState() == ApplicationEnv.PackageState.ABApplyingPatch || description.getState() == ApplicationEnv.PackageState.VerifyPayloadMetadata) {
            runStateMachine();
        }
    }

    public synchronized void onActionBatteryChanged(boolean z) {
        Logger.debug("OtaApp", "CusSM:onActionBatteryChanged:batteryLow=" + z);
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onActionBatteryChanged: notification for version is not in db " + deviceSha1);
        } else if (BuildPropReader.isFotaATT() && description.getState() == ApplicationEnv.PackageState.GettingPackage) {
            AndroidFotaInterface.sendBatteryChangedIntentToFota(OtaApplication.getGlobalContext(), z);
            if (z) {
                if (!description.getMeta().showDownloadProgress()) {
                    overWriteMetaData(MetadataOverrider.returnOverWrittenMetaData(description.getMeta(), "showDownloadProgress", true));
                }
            } else {
                runStateMachine();
            }
        } else {
            if (z) {
                if (description.getState() != ApplicationEnv.PackageState.GettingPackage && description.getState() != ApplicationEnv.PackageState.ABApplyingPatch) {
                    Logger.error("OtaApp", String.format("CusSM.onActionBatteryChanged failed: notification for version %s that is in state %s (expected state GettingPackage)", deviceSha1, description.getState().toString()));
                    return;
                }
                int i = AnonymousClass5.$SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[description.getState().ordinal()];
                if (i == 1) {
                    DownloadHandler downloadHandler = _downloader;
                    if (downloadHandler != null) {
                        downloadHandler.radioGotDown();
                    } else {
                        runStateMachine();
                    }
                } else if (i == 2) {
                    if (UpdaterEngineStateHandler.isBusy()) {
                        UpgradeUtilMethods.sendActionBatteryLow();
                        if (BuildPropReader.isATT() && !description.getMeta().showDownloadProgress()) {
                            overWriteMetaData(MetadataOverrider.returnOverWrittenMetaData(description.getMeta(), "showDownloadProgress", true));
                        }
                    } else {
                        runStateMachine();
                    }
                }
            } else {
                runStateMachine();
            }
        }
    }

    public synchronized void onIntentDmCancelUpgrade(String str) {
        String deviceSha1;
        if (UpgradeSourceType.modem.toString().equals(str)) {
            deviceSha1 = BuildPropReader.getDeviceModemConfigVersionSha1();
        } else {
            deviceSha1 = this._env.getServices().getDeviceSha1();
        }
        Logger.debug("OtaApp", "onIntentDmCancelUpgrade:repository=" + str);
        if (this._db.getDescription(deviceSha1) == null) {
            this._env.getUtilities().sendUpgradeStatus(UpgradeUtilConstants.KEY_INVALID_VALUE, UpgradeUtilConstants.KEY_INVALID_VALUE, UpgradeUtilConstants.KEY_INVALID_VALUE, UpgradeUtilConstants.KEY_SU_CANCEL_BY_DM, UpgradeUtilConstants.KEY_INVALID_VALUE, UpgradeUtilConstants.KEY_INVALID_VALUE, System.currentTimeMillis(), false, UpgradeUtilConstants.KEY_INVALID_VALUE, false, UpdateType.DIFFUpdateType.OS.toString());
            Logger.debug("OtaApp", "CusSM.onIntentDmCancelUpgrade, No ongoing OTA, SU cancel is denied.");
        } else if (UpgradeSourceType.modem.toString().equals(str)) {
            cancelModemUpdate("Modem Update is cancelled by DM, version: " + deviceSha1, ErrorCodeMapper.KEY_MODEM_CANCELED_BY_SERVER);
        } else {
            cancelOTA("Update is cancelled by DM, version: " + deviceSha1, ErrorCodeMapper.KEY_SU_CANCEL_BY_DM);
        }
    }

    private boolean checkIfAlreadyUpdating(int i, boolean z) {
        List<String> extractOtaVersionList = extractOtaVersionList(this._db.getVersions());
        if (extractOtaVersionList != null) {
            ApplicationEnv.Database.Descriptor descriptor = null;
            boolean z2 = false;
            boolean z3 = false;
            boolean z4 = false;
            for (String str : extractOtaVersionList) {
                descriptor = this._db.getDescription(str);
                if (descriptor != null && descriptor.getState() != ApplicationEnv.PackageState.Result) {
                    if (_isSendingUpgradeStatus == STATE_EVENT_STATUS.SENDING) {
                        z2 = true;
                        z3 = true;
                    } else if ((descriptor.getState() == ApplicationEnv.PackageState.GettingPackage || descriptor.getState() == ApplicationEnv.PackageState.GettingDescriptor) && !BuildPropReader.isStreamingUpdate()) {
                        z2 = true;
                        z4 = true;
                    } else {
                        z2 = true;
                    }
                }
            }
            if (z2) {
                if (!this.settings.getBoolean(Configs.FORCE_UPGRADE_TIME_COMPLETED) && i == CheckForUpgradeTriggeredBy.user.ordinal() && descriptor != null && ApplicationEnv.PackageState.MergePending != descriptor.getState() && ApplicationEnv.PackageState.MergeRestart != descriptor.getState()) {
                    overWriteMetaData(MetadataOverrider.from(descriptor.getMeta(), this.settings, CheckForUpgradeTriggeredBy.user.name(), true, this.settings.getString(Configs.REPORTINGTAGS), this.settings.getString(Configs.TRACKINGID)));
                    descriptor = this._db.getDescription(this._env.getServices().getDeviceSha1());
                }
                Logger.debug("OtaApp", "CusSM.checkIfAlreadyUpdating: attempt to check for upgrade while an upgrade is in prgress: state=" + descriptor.getState());
                UpgradeUtils.Error error = UpgradeUtils.Error.ERR_ALREADY;
                boolean z5 = (descriptor.getState() == ApplicationEnv.PackageState.Querying || descriptor.getState() == ApplicationEnv.PackageState.QueryingInstall) && !BuildPropReader.isStreamingUpdate();
                boolean z6 = descriptor.getState() == ApplicationEnv.PackageState.RequestPermission;
                boolean isDataNetworkRoaming = UpdaterUtils.isDataNetworkRoaming(OtaApplication.getGlobalContext());
                if (this.systemUpdaterPolicy.shouldIBlockUpdateForSystemPolicy(descriptor, this.settings)) {
                    error = UpgradeUtils.Error.ERR_POLICY_SET;
                } else if (z3) {
                    error = UpgradeUtils.Error.ERR_CONTACTING_SERVER;
                } else if (z4) {
                    error = UpgradeUtils.Error.ERR_DOWNLOADING;
                } else if (descriptor.getState() == ApplicationEnv.PackageState.ABApplyingPatch || descriptor.getState() == ApplicationEnv.PackageState.GettingDescriptor || descriptor.getState() == ApplicationEnv.PackageState.VerifyPayloadMetadata || descriptor.getState() == ApplicationEnv.PackageState.VerifyAllocateSpace) {
                    error = UpgradeUtils.Error.ERR_BACKGROUND_INSTALL;
                } else if (descriptor.getState() == ApplicationEnv.PackageState.MergePending) {
                    error = UpgradeUtils.Error.ERR_VAB_MERGE_PENDING;
                } else if (descriptor.getState() == ApplicationEnv.PackageState.MergeRestart) {
                    error = UpgradeUtils.Error.ERR_VAB_MERGE_RESTART;
                } else if (isDataNetworkRoaming && (z5 || z6)) {
                    error = UpgradeUtils.Error.ERR_ROAMING;
                } else if (UpdaterUtils.isInActiveCall(OtaApplication.getGlobalContext())) {
                    error = UpgradeUtils.Error.ERR_IN_CALL;
                } else {
                    this.settings.setBoolean(Configs.BATTERY_LOW, false);
                }
                try {
                    this._env.getUtilities().sendActionUpdateResponse(error, i, z, MetaDataBuilder.toJSONString(descriptor.getMeta()));
                } catch (JSONException unused) {
                    Logger.error("OtaApp", "Invalid JSON metadata, problem in parsing metadata");
                }
                return true;
            }
        }
        return false;
    }

    private List<String> extractOtaVersionList(List<String> list) {
        if (list == null) {
            return null;
        }
        for (String str : list) {
            if (str.equals(BuildPropReader.getDeviceModemConfigVersionSha1())) {
                list.remove(str);
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        return list;
    }

    /* JADX WARN: Code restructure failed: missing block: B:32:0x00a6, code lost:
        r8.settings.setString(com.motorola.ccc.ota.sources.bota.settings.Configs.FLAVOUR, r11);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public synchronized void onIntentUpdateNotificationResponse(java.lang.String r9, boolean r10, java.lang.String r11, java.lang.String r12) {
        /*
            r8 = this;
            java.lang.String r0 = "CusSM.onIntentUpdateNotificationResponse: notification for version is not in db "
            monitor-enter(r8)
            com.motorola.otalib.common.Environment.ApplicationEnv$Database r1 = r8._db     // Catch: java.lang.Throwable -> Lba
            com.motorola.otalib.common.Environment.ApplicationEnv$Database$Descriptor r1 = r1.getDescription(r9)     // Catch: java.lang.Throwable -> Lba
            if (r1 != 0) goto L1f
            java.lang.String r10 = "OtaApp"
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> Lba
            r11.<init>(r0)     // Catch: java.lang.Throwable -> Lba
            java.lang.StringBuilder r9 = r11.append(r9)     // Catch: java.lang.Throwable -> Lba
            java.lang.String r9 = r9.toString()     // Catch: java.lang.Throwable -> Lba
            com.motorola.ccc.ota.utils.Logger.error(r10, r9)     // Catch: java.lang.Throwable -> Lba
            monitor-exit(r8)
            return
        L1f:
            com.motorola.otalib.common.Environment.ApplicationEnv$PackageState r0 = r1.getState()     // Catch: java.lang.Throwable -> Lba
            com.motorola.otalib.common.Environment.ApplicationEnv$PackageState r2 = com.motorola.otalib.common.Environment.ApplicationEnv.PackageState.Notified     // Catch: java.lang.Throwable -> Lba
            if (r0 == r2) goto L48
            com.motorola.otalib.common.Environment.ApplicationEnv$PackageState r0 = r1.getState()     // Catch: java.lang.Throwable -> Lba
            com.motorola.otalib.common.Environment.ApplicationEnv$PackageState r2 = com.motorola.otalib.common.Environment.ApplicationEnv.PackageState.RequestPermission     // Catch: java.lang.Throwable -> Lba
            if (r0 == r2) goto L48
            java.lang.String r10 = "OtaApp"
            java.lang.String r11 = "CusSM.onIntentUpdateNotificationResponse failed: notification for version %s that is in state %s (expected state Notified or Request_Permission)"
            com.motorola.otalib.common.Environment.ApplicationEnv$PackageState r12 = r1.getState()     // Catch: java.lang.Throwable -> Lba
            java.lang.String r12 = r12.toString()     // Catch: java.lang.Throwable -> Lba
            java.lang.Object[] r9 = new java.lang.Object[]{r9, r12}     // Catch: java.lang.Throwable -> Lba
            java.lang.String r9 = java.lang.String.format(r11, r9)     // Catch: java.lang.Throwable -> Lba
            com.motorola.ccc.ota.utils.Logger.error(r10, r9)     // Catch: java.lang.Throwable -> Lba
            monitor-exit(r8)
            return
        L48:
            if (r10 != 0) goto L6b
            com.motorola.ccc.ota.sources.UpgradeStatusConstents r10 = com.motorola.ccc.ota.sources.UpgradeStatusConstents.User_Declined_The_Request_Notification     // Catch: java.lang.Throwable -> Lba
            r8.sendPluginUpdateStatus(r1, r10)     // Catch: java.lang.Throwable -> Lba
            com.motorola.otalib.common.Environment.ApplicationEnv$Database r2 = r8._db     // Catch: java.lang.Throwable -> Lba
            com.motorola.otalib.common.Environment.ApplicationEnv$PackageState r4 = com.motorola.otalib.common.Environment.ApplicationEnv.PackageState.Result     // Catch: java.lang.Throwable -> Lba
            if (r11 == 0) goto L56
            goto L58
        L56:
            java.lang.String r11 = "user declined to accept the upgrade"
        L58:
            r6 = r11
            java.lang.String r7 = "USER_CANCELED_UPDATE."
            r5 = 0
            r3 = r9
            r2.setState(r3, r4, r5, r6, r7)     // Catch: java.lang.Throwable -> Lba
            com.motorola.ccc.ota.sources.bota.settings.BotaSettings r10 = r8.settings     // Catch: java.lang.Throwable -> Lba
            com.motorola.ccc.ota.sources.bota.settings.Configs r11 = com.motorola.ccc.ota.sources.bota.settings.Configs.UPDATE_FAIL_COUNT     // Catch: java.lang.Throwable -> Lba
            r10.incrementPrefs(r11)     // Catch: java.lang.Throwable -> Lba
            r8.cleanupVersion(r9)     // Catch: java.lang.Throwable -> Lba
            goto Lb5
        L6b:
            android.content.Context r10 = com.motorola.ccc.ota.env.OtaApplication.getGlobalContext()     // Catch: java.lang.Throwable -> Lba
            boolean r10 = com.motorola.ccc.ota.ui.UpdaterUtils.isBatteryLowToStartDownload(r10)     // Catch: java.lang.Throwable -> Lba
            if (r10 == 0) goto L78
            com.motorola.ccc.ota.ui.UpdaterUtils.enableReceiversForBatteryLow()     // Catch: java.lang.Throwable -> Lba
        L78:
            com.motorola.ccc.ota.ui.UpdaterUtils.sendDownloadModeStats(r12)     // Catch: java.lang.Throwable -> Lba
            java.lang.String r10 = r1.getRepository()     // Catch: java.lang.Throwable -> Lba
            com.motorola.ccc.ota.sources.UpgradeSourceType r12 = com.motorola.ccc.ota.sources.UpgradeSourceType.upgrade     // Catch: java.lang.Throwable -> Lba
            java.lang.String r12 = r12.toString()     // Catch: java.lang.Throwable -> Lba
            boolean r10 = r10.equals(r12)     // Catch: java.lang.Throwable -> Lba
            if (r10 != 0) goto La4
            java.lang.String r10 = r1.getRepository()     // Catch: java.lang.Throwable -> Lba
            com.motorola.ccc.ota.sources.UpgradeSourceType r12 = com.motorola.ccc.ota.sources.UpgradeSourceType.fota     // Catch: java.lang.Throwable -> Lba
            java.lang.String r12 = r12.toString()     // Catch: java.lang.Throwable -> Lba
            boolean r10 = r10.equals(r12)     // Catch: java.lang.Throwable -> Lba
            if (r10 == 0) goto L9c
            goto La4
        L9c:
            com.motorola.otalib.common.Environment.ApplicationEnv$Database r10 = r8._db     // Catch: java.lang.Throwable -> Lba
            com.motorola.otalib.common.Environment.ApplicationEnv$PackageState r12 = com.motorola.otalib.common.Environment.ApplicationEnv.PackageState.GettingPackage     // Catch: java.lang.Throwable -> Lba
            r10.setState(r9, r12, r11)     // Catch: java.lang.Throwable -> Lba
            goto Lb5
        La4:
            if (r11 == 0) goto Lad
            com.motorola.ccc.ota.sources.bota.settings.BotaSettings r10 = r8.settings     // Catch: java.lang.Throwable -> Lba
            com.motorola.ccc.ota.sources.bota.settings.Configs r12 = com.motorola.ccc.ota.sources.bota.settings.Configs.FLAVOUR     // Catch: java.lang.Throwable -> Lba
            r10.setString(r12, r11)     // Catch: java.lang.Throwable -> Lba
        Lad:
            com.motorola.otalib.common.Environment.ApplicationEnv$Database r10 = r8._db     // Catch: java.lang.Throwable -> Lba
            com.motorola.otalib.common.Environment.ApplicationEnv$PackageState r11 = com.motorola.otalib.common.Environment.ApplicationEnv.PackageState.GettingDescriptor     // Catch: java.lang.Throwable -> Lba
            r12 = 0
            r10.setState(r9, r11, r12)     // Catch: java.lang.Throwable -> Lba
        Lb5:
            r8.runStateMachine()     // Catch: java.lang.Throwable -> Lba
            monitor-exit(r8)
            return
        Lba:
            r9 = move-exception
            monitor-exit(r8)
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.ccc.ota.CusSM.onIntentUpdateNotificationResponse(java.lang.String, boolean, java.lang.String, java.lang.String):void");
    }

    public synchronized void onIntentABApplyingPatchCompleted(boolean z, String str, String str2) {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentABUpgradeCompleted: notification for version is not in db " + deviceSha1);
        } else if (description.getState() != ApplicationEnv.PackageState.ABApplyingPatch) {
            Logger.error("OtaApp", "CusSM.onIntentABUpgradeCompleted failed: Not in ab applying patch state, version " + deviceSha1);
        } else {
            if (UpdaterUtils.isROREnabled(description)) {
                UpdaterUtils.prepareForUnattendedUpdate();
            }
            if (this.settings.getLong(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME, -1L) > 0) {
                this.settings.removeConfig(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME);
                this._env.getUtilities().unRegisterWithForceUpgradeManager();
            }
            if (z) {
                this._db.setState(deviceSha1, ApplicationEnv.PackageState.Querying, null);
                this.settings.removeConfig(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME.key());
                this.settings.removeConfig(Configs.MAX_CRITICAL_UPDATE_EXTENDED_TIME.key());
                FileUtils.cleanupFiles();
                Logger.debug("OtaApp", "CusSM.getInstallReminder : " + description.getMeta().getInstallReminder());
                UpdaterUtils.disableBatteryStatusReceiver();
                NotificationUtils.clearNextPromptDetails(this.settings);
                if (SmartUpdateUtils.isSmartUpdateEnabledByUser(this.settings)) {
                    SmartUpdateUtils.settingInstallTimeIntervalForSmartUpdate(this.settings);
                }
                if (description.getMeta().getSeverity() == UpgradeUtils.SeverityType.CRITICAL.ordinal()) {
                    CusUtilMethods.settingMaxDeferTimeForCriticalUpdate(description, this.settings);
                } else if (description.getMeta().isForceInstallTimeSet()) {
                    this.settings.setLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, (long) (System.currentTimeMillis() + (description.getMeta().getForceInstallTime() * 8.64E7d)));
                    Logger.debug("OtaApp", "CusSM.onIntentABUpgradeCompleted: Force install set");
                } else if (!TextUtils.isEmpty(description.getMeta().getInstallReminder())) {
                    String installReminder = description.getMeta().getInstallReminder();
                    Logger.debug("OtaApp", "CusSM.onIntentABUpgradeCompleted : annoyString = " + installReminder);
                    UpdaterUtils.setMandatoryInstallDays(installReminder);
                }
                UpgradeUtilMethods.informPendingRebootInfo(OtaApplication.getGlobalContext(), this.settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L));
            } else {
                resetToUpgradeFailure(deviceSha1, description, FileUtils.getFailureDetailsFromFileToUpload(FileUtils.UPDATER_ENGINE_LOG_FILE, str), str2);
            }
            runStateMachine();
        }
    }

    public synchronized void onIntentSystemUpdateNotificationResponse(String str) {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentSystemUpdateAvailableNotificationResponse: notification for version is not in db " + deviceSha1);
        } else if (description.getState() != ApplicationEnv.PackageState.Notified && description.getState() != ApplicationEnv.PackageState.RequestPermission) {
            Logger.error("OtaApp", String.format("CusSM.onIntentSystemUpdateAvailableNotificationResponse failed: notification for version %s that is in state %s  (expected state Notified or RequestPermission)", deviceSha1, description.getState().toString()));
        } else {
            if (description.getState() != ApplicationEnv.PackageState.RequestPermission) {
                this._db.setVersionState(deviceSha1, ApplicationEnv.PackageState.RequestPermission, null);
            }
            UpdaterUtils.setFullScreenStartPoint(Configs.STATS_DL_START_POINT, str);
            runStateMachine();
        }
    }

    public void onIntentVerifyPayloadStatus(String str, String str2, String str3) {
        UpgradeUtils.DownloadStatus downloadStatus;
        Logger.error("OtaApp", "CusSM.onIntentVerifyPayloadStatus: ");
        Context globalContext = OtaApplication.getGlobalContext();
        DownloadHandler downloadHandler = _downloader;
        if (downloadHandler != null) {
            downloadHandler.close();
            _downloader = null;
        }
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentVerifyPayloadStatus: notification for version is not in db " + deviceSha1);
        } else if (description.getState() != ApplicationEnv.PackageState.VerifyPayloadMetadata) {
            Logger.error("OtaApp", "CusSM.onIntentVerifyPayloadStatus failed: notification for version " + deviceSha1 + " that is in state " + description.getState().toString() + " (expected state VerifyPayloadMetadata))");
        } else {
            try {
                downloadStatus = UpgradeUtils.DownloadStatus.valueOf(str2);
            } catch (IllegalArgumentException | NullPointerException e) {
                UpgradeUtils.DownloadStatus downloadStatus2 = UpgradeUtils.DownloadStatus.STATUS_OK;
                Logger.error("OtaApp", "Error in CusSM.onIntentVerifyPayloadStatus: " + e);
                downloadStatus = downloadStatus2;
            }
            if (UpgradeUtils.DownloadStatus.STATUS_OK.toString().equals(str2)) {
                Logger.debug("OtaApp", "CusSM.onIntentVerifyPayload : STATUS_OK");
                PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_OK, this.settings, globalContext, this._env);
                this.settings.setBoolean(Configs.VERIFY_PAYLOAD_STATUS_CHECK, true);
                Logger.debug("OtaApp", "CusSM.onIntentVerifyPayload : Move to cussm to VerifyAllocateSpace");
                PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_ALLOCATE_SPACE, this.settings, globalContext, this._env);
                this._db.setVersionState(deviceSha1, ApplicationEnv.PackageState.VerifyAllocateSpace, null);
                runStateMachine();
                return;
            }
            Logger.debug("OtaApp", "CusSM.onIntentVerifyPayload : STATUS_NOT_OK");
            failDownload(deviceSha1, downloadStatus, str, str3);
        }
    }

    public void onIntentVABVerifyPayloadStatus(boolean z) {
        Logger.debug("OtaApp", "onIntentVABVerifyPayloadStatus:isVABValidationSuccess=" + z);
        if (z) {
            this._env.getUtilities().sendActionUpdateResponse(UpgradeUtils.Error.ERR_VAB_VALIDATION_SUCCESS, 0, false, this.settings.getString(Configs.METADATA));
        } else {
            this._env.getUtilities().sendActionUpdateResponse(UpgradeUtils.Error.ERR_VAB_VALIDATION_FAILURE, 0, false, this.settings.getString(Configs.METADATA));
        }
        try {
            String string = this.settings.getString(Configs.UPDATING_VALIDATION_FILE);
            File file = new File(string.substring(0, string.lastIndexOf(FileUtils.EXT)));
            File file2 = new File(FileUtils.getDownloadDataDirectory() + "/payload_metadata.bin");
            org.apache.commons.io.FileUtils.forceDelete(file);
            file2.delete();
        } catch (IOException | NullPointerException e) {
            Logger.error("OtaApp", "Exception occurred while deleting extracted verified files:" + e);
        }
        this.settings.removeConfig(Configs.UPDATING_VALIDATION_FILE);
    }

    private void requestUserToAllocateSpace(long j) {
        Logger.debug("OtaApp", "Requesting user to allocate space");
        Context globalContext = OtaApplication.getGlobalContext();
        this.settings.setLong(Configs.STATS_VAB_FREESPACEREQ_VALUE, j);
        PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_VAB_MAKE_SPACE_REQUEST_USER, this.settings, globalContext, this._env, String.valueOf(j - MessageActivity.getAvailableDataPartitionSize()));
        Logger.debug("OtaApp", "CusSM.requestUserToAllocateSpace, extra space needed is" + (j / 1048576) + " MB");
        this._env.getUtilities().showAllocateFreeSpaceDialog(UpgradeUtils.DownloadStatus.STATUS_VAB_MAKE_SPACE_REQUEST_USER, j);
    }

    public void onIntentVABCleanupAppliedPayload(int i) {
        String metaSourceSha1 = UpdaterUtils.getMetaSourceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(metaSourceSha1);
        if (description == null) {
            Logger.debug("OtaApp", "CusSM.onIntentVABCleanupAppliedPayload: version is not in db " + metaSourceSha1);
            return;
        }
        try {
            ((UpdaterEngineInstaller) _installType).clearUEInstallerBeforeExit();
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception in onIntentVABCleanupAppliedPayload:msg=" + e);
        }
        int i2 = this.settings.getInt(Configs.STATS_VAB_MERGE_REBOOT_FAILURE_COUNT, 0);
        String string = this.settings.getString(Configs.STATS_VAB_MERGE_STATUS);
        string = (string == null || string.equalsIgnoreCase("null")) ? "" : "";
        if (UpgradeUtils.MergeStatus.APPLY_MERGE_CORRUPTED == i) {
            this.settings.setString(Configs.STATS_VAB_MERGE_UPDATE_FAILED_REASON, "Device Corrupted");
            this.settings.setBoolean(Configs.VAB_MERGE_DEVICE_CORRUPTED, true);
            this.settings.setString(Configs.STATS_VAB_MERGE_STATUS, string + ",Failure");
            resetToUpgradeFailure(metaSourceSha1, description, "cleanupAppliedPayload failed due to device corrupted  : " + i, ErrorCodeMapper.KEY_MERGE_STATUS_DEVICE_CORRUPTED);
        } else if (UpgradeUtils.MergeStatus.APPLY_PAYLOAD_FAILURE == i && i2 >= 1) {
            this.settings.setString(Configs.STATS_VAB_MERGE_UPDATE_FAILED_REASON, "Merge failure count exceeded");
            this.settings.setBoolean(Configs.VAB_MERGE_DEVICE_CORRUPTED, true);
            this.settings.setString(Configs.STATS_VAB_MERGE_STATUS, string + ",Failure");
            resetToUpgradeFailure(metaSourceSha1, description, "cleanupAppliedPayload failed due to merge failure count exceeded, so assuming device corrupted  : " + i, ErrorCodeMapper.KEY_MERGE_STATUS_DEVICE_CORRUPTED);
        } else if (UpgradeUtils.MergeStatus.APPLY_PAYLOAD_SUCCESS == i) {
            this.settings.setString(Configs.STATS_VAB_MERGE_STATUS, string + ",Success");
            resetToUpgradeSuccess(metaSourceSha1, description);
        } else {
            this._db.setVersionState(metaSourceSha1, ApplicationEnv.PackageState.MergeRestart, null);
            this.settings.setString(Configs.STATS_VAB_MERGE_STATUS, string + ",Failure");
        }
        this.settings.setBoolean(Configs.VAB_MERGE_PROCESS_RUNNING, false);
        runStateMachine();
    }

    public void onIntentAllocateSpaceResult(long j) {
        Context globalContext = OtaApplication.getGlobalContext();
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (j >= 0) {
            j += 52428800;
        }
        long max = Math.max(j, 52428800L);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentAllocateSpaceResult: notification for version is not in db " + deviceSha1);
            return;
        }
        Logger.debug("OtaApp", "CusSM.onIntentAllocateSpaceResult, freeSpaceReq = " + max + " State = " + description.getState());
        _installType.onAllocateSpaceResult();
        if (description.getState() == ApplicationEnv.PackageState.VerifyAllocateSpace) {
            if (max <= MessageActivity.getAvailableDataPartitionSize()) {
                PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_ALLOCATE_SPACE_SUCESS, this.settings, globalContext, this._env);
                this._db.setState(deviceSha1, ApplicationEnv.PackageState.ABApplyingPatch, null);
                unregisterFromHealthCheck();
                runStateMachine();
                return;
            } else if (!this.settings.getBoolean(Configs.RESERVE_SPACE_CLEAR_VAB)) {
                Logger.debug("OtaApp", "Clearing reserve space");
                long availableDataPartitionSize = (max - MessageActivity.getAvailableDataPartitionSize()) / 1048576;
                Logger.debug("OtaApp", "onIntentAllocateSpaceResult:freeSpaceReq=" + max + ": avail space=" + MessageActivity.getAvailableDataPartitionSize() + " spaceToBeFreed=" + availableDataPartitionSize + " MB");
                if (freeFromReserveSpace(availableDataPartitionSize)) {
                    Logger.debug("OtaApp", "CusSM-ExtraSpace: deleting of reserve files successful");
                    return;
                }
                Logger.debug("OtaApp", "CusSM-ExtraSpace: deleting of reserve files not successful");
                this.settings.setBoolean(Configs.RESERVE_SPACE_CLEAR_VAB, true);
                if (BuildPropReader.isATT()) {
                    UpgradeUtils.DownloadStatus downloadStatus = UpgradeUtils.DownloadStatus.STATUS_VAB_MAKE_SPACE_REQUEST_USER;
                    if (BuildPropReader.isFotaATT()) {
                        Logger.info("OtaApp", "FotaUpgradeSource.onIntentAllocateSpaceResult failed: " + deviceSha1 + "  5 " + downloadStatus);
                        this._env.getFotaServices().sendUpgradeResult(1138L, 5);
                    } else {
                        Logger.info("OtaApp", "FotaUpgradeSource.onIntentAllocateSpaceResult failed: " + deviceSha1 + "  " + downloadStatus);
                    }
                    String format = String.format("fota installation failed due to allocation of space for update", new Object[0]);
                    if (description.getMeta().showDownloadProgress()) {
                        failDownload(deviceSha1, downloadStatus, format, ErrorCodeMapper.KEY_FAILED_FOTA);
                        return;
                    } else {
                        failDownloadInternalSilent(deviceSha1, format, ErrorCodeMapper.KEY_FAILED_FOTA);
                        return;
                    }
                }
                requestUserToAllocateSpace(max);
                return;
            } else {
                requestUserToAllocateSpace(max);
                return;
            }
        }
        Logger.error("OtaApp", "CusSM.onIntentAllocateSpaceResult, failed: for version " + deviceSha1 + " that is in state " + description.getState().toString() + " (expected state VerifyAllocateSpace))");
    }

    public synchronized void onIntentInstallSystemUpdateNotificationResponse(String str, String str2) {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentInstallSystemUpdateNotificationResponse: notification for version is not in db " + deviceSha1);
        } else if (description.getState() != ApplicationEnv.PackageState.Querying && description.getState() != ApplicationEnv.PackageState.QueryingInstall) {
            Logger.error("OtaApp", String.format("CusSM.onIntentInstallSystemUpdateNotificationResponse failed: notification for version %s that is in state %s (expected state Querying or querying install)", deviceSha1, description.getState().toString()));
        } else {
            if (description.getState() != ApplicationEnv.PackageState.QueryingInstall) {
                this._db.setVersionState(deviceSha1, ApplicationEnv.PackageState.QueryingInstall, null);
            }
            if (NotificationUtils.KEY_INSTALL.equals(str)) {
                UpdaterUtils.setFullScreenStartPoint(Configs.STATS_INSTALL_START_POINT, str2);
            } else if (NotificationUtils.KEY_RESTART.equals(str)) {
                UpdaterUtils.setFullScreenStartPoint(Configs.STATS_RESTART_START_POINT, str2);
            }
            runStateMachine();
        }
    }

    public void onIntentWiFiDiscoverTimerExpiry() {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        if (wifiDiscoverySanityCheck(deviceSha1)) {
            this._env.getUtilities().unRegisterWithWiFiDiscoveryManager();
            this.settings.removeConfig(Configs.WIFI_DISCOVER_TIME);
            if (BuildPropReader.isBotaATT()) {
                Logger.debug("OtaApp", "canceling update: STATUS_RESOURCES_WIFI:KEY_FAILED_FOTA_WIFI_DISCOVERY_TIMER");
                if (getMetaData().showDownloadProgress()) {
                    failDownload(deviceSha1, UpgradeUtils.DownloadStatus.STATUS_RESOURCES_WIFI, "Fota download is failed due to WiFi discovery timer expired", ErrorCodeMapper.KEY_FAILED_FOTA_WIFI_DISCOVERY_TIMER);
                    return;
                } else {
                    failDownloadInternalSilent(deviceSha1, "Fota download is failed due to WiFi discovery timer expired", ErrorCodeMapper.KEY_FAILED_FOTA_WIFI_DISCOVERY_TIMER);
                    return;
                }
            }
            this._env.getUtilities().sendGetDescriptor(deviceSha1, "WiFi discover time expired, go and getch new metadata (is there a change?)", true);
        }
    }

    private void checkIfWifiDiscoveryTimerExpired() {
        if (wifiDiscoverySanityCheck(this._env.getServices().getDeviceSha1())) {
            long currentTimeMillis = System.currentTimeMillis();
            long j = this.settings.getLong(Configs.WIFI_DISCOVER_TIME, -1L);
            int i = (j > 0L ? 1 : (j == 0L ? 0 : -1));
            if (i > 0 && currentTimeMillis >= j) {
                this._env.getUtilities().sendDiscoverTimerExpiryIntent();
            } else if (i <= 0 || currentTimeMillis >= j) {
            } else {
                long j2 = this.settings.getLong(Configs.WIFI_DISCOVER_TIME, -1L) - currentTimeMillis;
                this.settings.setLong(Configs.WIFI_DISCOVER_TIME, currentTimeMillis + j2);
                if (BuildPropReader.isATT()) {
                    this._env.getUtilities().registerWithWiFiDiscoveryManager(j2 / 1000, false, true);
                } else {
                    this._env.getUtilities().registerWithWiFiDiscoveryManager(j2 / 1000, true, false);
                }
            }
        }
    }

    public void onIntentSmartUpdateConfigChanged(boolean z) {
        String str;
        Logger.debug("OtaApp", "CusSM:onIntentSmartUpdateConfigChanged:isSmartUpdateEnabled=" + z);
        if (z) {
            str = SystemUpdateStatusUtils.SMART_UPDATE_ENABLED;
        } else {
            str = SystemUpdateStatusUtils.SMART_UPDATE_DISABLED;
        }
        DmSendAlertService.sendDmAlertDeviceReqNotification(OtaApplication.getGlobalContext(), str);
    }

    private boolean wifiDiscoverySanityCheck(String str) {
        DownloadHandler downloadHandler;
        if (this.settings.getLong(Configs.WIFI_DISCOVER_TIME, -1L) == 0) {
            this.settings.removeConfig(Configs.WIFI_DISCOVER_TIME);
            this._env.getUtilities().unRegisterWithWiFiDiscoveryManager();
            Logger.error("OtaApp", "CusSM.wifiDiscoverySanityCheck failed: something gone wrong with wifi discover time cann't be zero, abort the process");
            return false;
        } else if (this.settings.getLong(Configs.WIFI_DISCOVER_TIME, -1L) < 0) {
            Logger.warn("OtaApp", "CusSM.wifiDiscoverySanityCheck failed: wifi discover time is not set nothing to process");
            return false;
        } else {
            ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
            if (description == null) {
                Logger.error("OtaApp", "CusSM.wifiDiscoverySanityCheck: version is not in db " + str);
                return false;
            } else if (!description.getRepository().equals(UpgradeSourceType.upgrade.toString()) && !description.getRepository().equals(UpgradeSourceType.fota.toString())) {
                Logger.error("OtaApp", String.format("CusSM.wifiDiscoverySanityCheck failed: version %s that is with repository %s (expected upgrade/fota as repository)", str, description.getRepository()));
                return false;
            } else if (!BuildPropReader.isATT() && description.getRepository().equals(UpgradeSourceType.upgrade.toString()) && (downloadHandler = _downloader) != null && downloadHandler.isBusy()) {
                Logger.debug("OtaApp", "CusSM.wifiDiscoverySanityCheck failed: downloading is in progress");
                return false;
            } else if (description.getState() != ApplicationEnv.PackageState.GettingPackage && description.getState() != ApplicationEnv.PackageState.ABApplyingPatch) {
                Logger.error("OtaApp", String.format("CusSM.wifiDiscoverySanityCheck failed: version %s that is in state %s (expected state GettingPackage or ABApplyingPatch)", str, description.getState().toString()));
                return false;
            } else if (!UpdaterUtils.isWifiOnly()) {
                Logger.error("OtaApp", String.format("CusSM.wifiDiscoverySanityCheck failed: version %s that is in state %s not a WiFiOnly package %s (expected true)", str, description.getState().toString(), Boolean.valueOf(description.getMeta().isWifiOnly())));
                return false;
            } else if (this.systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet()) {
                Logger.error("OtaApp", String.format("CusSM.wifiDiscoverySanityCheck failed: version %s that is in state %s allowFotaOverAnyDatanetwork policy is enabled", str, description.getState().toString()));
                return false;
            } else {
                return true;
            }
        }
    }

    public void sendAvailableReserveSpace() {
        this._env.getUtilities().postAvailableReserveSpace();
    }

    private void forceUpgradeTimerExpired() {
        if (forceUpgradeTimerSanityCheck(this._env.getServices().getDeviceSha1())) {
            this._env.getUtilities().unRegisterWithForceUpgradeManager();
            this.settings.removeConfig(Configs.FORCE_UPGRADE_TIME);
            MetaData overWriteForceUpgradeValues = MetadataOverrider.overWriteForceUpgradeValues(getMetaData(), this.settings.getString(Configs.REPORTINGTAGS), this.settings.getString(Configs.TRACKINGID));
            if (overWriteForceUpgradeValues != null) {
                this.settings.setBoolean(Configs.FORCE_UPGRADE_TIME_COMPLETED, true);
                overWriteMetaData(overWriteForceUpgradeValues);
                forceUpgrade();
                return;
            }
            Logger.debug("OtaApp", "CusSM.onIntentForceUpgradeTimerExpiry: Strange, not able to over write metadata values forceUpgradeTime will not work");
        }
    }

    private void forceDownloadTimerExpired() {
        Logger.debug("OtaApp", "CusSm.forceDownloadTimerExpired");
        if (forceDownloadTimerSanityCheck(this._env.getServices().getDeviceSha1())) {
            this._env.getUtilities().unRegisterWithForceUpgradeManager();
            forceDownload();
        }
    }

    public void onIntentForceUpgradeTimerExpiry() {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(this._env.getServices().getDeviceSha1());
        if (description.getMeta().getForceUpgradeTime() > 0) {
            forceUpgradeTimerExpired();
        } else if (description.getMeta().getForceDownloadTime() >= 0.0d) {
            forceDownloadTimerExpired();
        }
    }

    private void checkIfForceUpgradeTimerExpired() {
        if (forceUpgradeTimerSanityCheck(this._env.getServices().getDeviceSha1())) {
            long currentTimeMillis = System.currentTimeMillis();
            long j = this.settings.getLong(Configs.FORCE_UPGRADE_TIME, -1L);
            int i = (j > 0L ? 1 : (j == 0L ? 0 : -1));
            if (i > 0 && currentTimeMillis >= j) {
                this._env.getUtilities().sendForceUpgradeTimerExpiryIntent();
            } else if (i <= 0 || currentTimeMillis >= j) {
            } else {
                long j2 = this.settings.getLong(Configs.FORCE_UPGRADE_TIME, -1L) - currentTimeMillis;
                this.settings.setLong(Configs.FORCE_UPGRADE_TIME, currentTimeMillis + j2);
                this._env.getUtilities().registerWithForceUpgradeManager(j2 / 1000);
            }
        }
    }

    private boolean forceUpgradeTimerSanityCheck(String str) {
        if (this.settings.getLong(Configs.FORCE_UPGRADE_TIME, -1L) == 0) {
            this.settings.removeConfig(Configs.FORCE_UPGRADE_TIME);
            this._env.getUtilities().unRegisterWithForceUpgradeManager();
            Logger.warn("OtaApp", "CusSM.forceUpgradeTimerSanityCheck failed: something gone wrong with force upgrade time cann't be zero, abort the process");
            return false;
        } else if (this.settings.getLong(Configs.FORCE_UPGRADE_TIME, -1L) < 0) {
            Logger.warn("OtaApp", "CusSM.forceUpgradeTimerSanityCheck failed: force upgrade time is not set nothing to process");
            return false;
        } else {
            ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
            if (description == null) {
                Logger.error("OtaApp", "CusSM.forceUpgradeTimerSanityCheck: version is not in db " + str);
                return false;
            } else if (!description.getRepository().equals(UpgradeSourceType.upgrade.toString())) {
                Logger.error("OtaApp", String.format("CusSM.forceUpgradeTimerSanityCheck failed: version %s that is with repository %s (expected upgrade as repository)", str, description.getRepository()));
                return false;
            } else if (description.getState() == ApplicationEnv.PackageState.Result) {
                Logger.error("OtaApp", String.format("CusSM.forceUpgradeTimerSanityCheck failed: version %s that is in state %s (expected state other than Result)", str, description.getState().toString()));
                return false;
            } else {
                return true;
            }
        }
    }

    private void checkIfForceDownloadTimerExpired() {
        if (forceDownloadTimerSanityCheck(this._env.getServices().getDeviceSha1())) {
            int forceDownloadDelay = UpdaterUtils.getForceDownloadDelay(System.currentTimeMillis());
            if (forceDownloadDelay <= 0) {
                this._env.getUtilities().sendForceUpgradeTimerExpiryIntent();
            } else {
                this._env.getUtilities().registerWithForceUpgradeManager(forceDownloadDelay);
            }
        }
    }

    private boolean forceDownloadTimerSanityCheck(String str) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.forceDownloadTimerSanityCheck: version is not in db " + str);
            return false;
        } else if (description.getMeta().getForceDownloadTime() >= 0.0d && this.settings.getLong(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME, -1L) >= 0) {
            if (description.getState() == ApplicationEnv.PackageState.Result) {
                Logger.error("OtaApp", String.format("CusSM.forceDownloadTimerSanityCheck failed: version %s that is in state %s (expected state other than Result)", str, description.getState().toString()));
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public synchronized void onIntentDownloadNotificationResponse(UpgradeUtils.DownloadStatus downloadStatus) {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentDownloadNotificationResponse: notification for version is not in db " + deviceSha1);
        } else if (description.getState() != ApplicationEnv.PackageState.GettingPackage && description.getState() != ApplicationEnv.PackageState.GettingDescriptor) {
            Logger.error("OtaApp", String.format("CusSM.onIntentDownloadNotificationResponse failed: notification for version %s that is in state %s (expected state GettingPackage/GettingDescriptor)", deviceSha1, description.getState().toString()));
        } else {
            int i = AnonymousClass5.$SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[downloadStatus.ordinal()];
            if (i == 1) {
                failDownload(deviceSha1, UpgradeUtils.DownloadStatus.STATUS_CANCEL, SystemUpdateStatusUtils.KEY_DOWNLOAD_CANCELLED, ErrorCodeMapper.KEY_USER_CANCELED_DOWNLOAD);
            } else if (i == 2) {
                if (SmartUpdateUtils.shouldIForceSmartUpdate(this.settings, description.getMeta().getUpdateTypeData())) {
                    UpdaterUtils.sendDownloadModeStats("userResumeCellularForSmartUpdate");
                } else if (description.getMeta().getForceDownloadTime() > 0.0d) {
                    UpdaterUtils.sendDownloadModeStats("userResumeCellular");
                }
                this.settings.setBoolean(Configs.AUTOMATIC_DOWNLOAD_FOR_CELLULAR, true);
                this.settings.removeConfig(Configs.FORCE_UPGRADE_TIME);
                this.settings.removeConfig(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME);
                this._env.getUtilities().unRegisterWithForceUpgradeManager();
                runStateMachine();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.CusSM$5  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState;
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus;

        static {
            int[] iArr = new int[UpgradeUtils.DownloadStatus.values().length];
            $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus = iArr;
            try {
                iArr[UpgradeUtils.DownloadStatus.STATUS_CANCEL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_RESUME_ON_CELLULAR.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            int[] iArr2 = new int[ApplicationEnv.PackageState.values().length];
            $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState = iArr2;
            try {
                iArr2[ApplicationEnv.PackageState.GettingPackage.ordinal()] = 1;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.ABApplyingPatch.ordinal()] = 2;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.Notified.ordinal()] = 3;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.RequestPermission.ordinal()] = 4;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.GettingDescriptor.ordinal()] = 5;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.QueueForDownload.ordinal()] = 6;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.IntimateModem.ordinal()] = 7;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.WaitingForModemUpdateStatus.ordinal()] = 8;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.VerifyPayloadMetadata.ordinal()] = 9;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.VerifyAllocateSpace.ordinal()] = 10;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.Querying.ordinal()] = 11;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.QueryingInstall.ordinal()] = 12;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.Upgrading.ordinal()] = 13;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.MergePending.ordinal()] = 14;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.MergeRestart.ordinal()] = 15;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.Result.ordinal()] = 16;
            } catch (NoSuchFieldError unused19) {
            }
        }
    }

    public synchronized void onIntentBackgroundInstallCancelResponse() {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentBackgroundInstallCancelResponse: notification for version is not in db " + deviceSha1);
        } else if (description.getState() != ApplicationEnv.PackageState.ABApplyingPatch && description.getState() != ApplicationEnv.PackageState.GettingDescriptor && description.getState() != ApplicationEnv.PackageState.VerifyPayloadMetadata && description.getState() != ApplicationEnv.PackageState.VerifyAllocateSpace) {
            Logger.error("OtaApp", String.format("CusSM.onIntentBackgroundInstallCancelResponse failed: notification for version %s that is in state %s (expected state ABApplyingPatch or Getting descriptor)", deviceSha1, description.getState().toString()));
        } else {
            String str = SystemUpdateStatusUtils.KEY_DOWNLOAD_CANCELLED;
            if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                str = this.settings.getString(Configs.VITAL_UPDATE_CANCEL_REASON);
            }
            failProgress(deviceSha1, UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL, UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL.toString() + ";  AddOnInfo: " + str, ErrorCodeMapper.KEY_USER_CANCELED_DOWNLOAD);
        }
    }

    public synchronized void onIntentBackgroundInstallResponse(UpgradeUtils.DownloadStatus downloadStatus) {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentBackgroundInstallResponse: notification for version is not in db " + deviceSha1);
        } else if (description.getState() != ApplicationEnv.PackageState.GettingDescriptor && description.getState() != ApplicationEnv.PackageState.ABApplyingPatch && description.getState() != ApplicationEnv.PackageState.VerifyPayloadMetadata && description.getState() != ApplicationEnv.PackageState.VerifyAllocateSpace) {
            Logger.error("OtaApp", String.format("CusSM.onIntentBackgroundInstallResponse failed: notification for version %s that is in state %s (expected state GettingDescriptor or ABApplyingPatch)", deviceSha1, description.getState().toString()));
        } else {
            int i = AnonymousClass5.$SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[downloadStatus.ordinal()];
            if (i == 2) {
                if (SmartUpdateUtils.shouldIForceSmartUpdate(this.settings, description.getMeta().getUpdateTypeData())) {
                    UpdaterUtils.sendDownloadModeStats("userResumeCellularForSmartUpdate");
                } else if (description.getMeta().getForceDownloadTime() > 0.0d) {
                    UpdaterUtils.sendDownloadModeStats("userResumeCellular");
                } else if (BuildPropReader.isBotaATT()) {
                    UpdaterUtils.sendDownloadModeStats("userResumeCellularATT");
                    FotaUpgradeSource.sendFotaDownloadModeChanged(OtaApplication.getGlobalContext());
                }
                this.settings.setBoolean(Configs.AUTOMATIC_DOWNLOAD_FOR_CELLULAR, true);
                this.settings.removeConfig(Configs.FORCE_UPGRADE_TIME);
                this.settings.removeConfig(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME);
                this._env.getUtilities().unRegisterWithForceUpgradeManager();
                runStateMachine();
            } else if (i == 3) {
                _installType.checkAndResetInstallerFromCusSM(description.getState(), InstallerUtilMethods.InstallerErrorStatus.STATUS_USER_CANCEL);
            }
        }
    }

    public synchronized void onIntentLaunchUpgrade(String str, boolean z, boolean z2, String str2) {
        final String string;
        Logger.debug("OtaApp", String.format("CusSM.onIntentLaunchUpgrade: procceed with upgrade to version %s ? %s checkForLowBattery: %b", str, z ? "yes" : "no", Boolean.valueOf(z2)));
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            Logger.error("OtaApp", String.format("CusSM.onIntentLaunchUpgrade failed: Launch of version %s is not in db, giving up", str));
        } else if (description.getState() != ApplicationEnv.PackageState.Querying && description.getState() != ApplicationEnv.PackageState.QueryingInstall) {
            Logger.error("OtaApp", String.format("CusSM.onIntentLaunchUpgrade failed: Launch of version %s in state %s (expected state Querying)", str, description.getState().toString()));
        } else {
            if (!z) {
                sendPluginUpdateStatus(description, UpgradeStatusConstents.User_Declined_Launching_The_Upgrade);
                this._db.setState(str, ApplicationEnv.PackageState.Result, false, "user declined to launch upgrade", ErrorCodeMapper.KEY_UPDATE_CANCELLED);
                this.settings.incrementPrefs(Configs.UPDATE_FAIL_COUNT);
                cleanupVersion(str);
                _installType.checkAndResetInstallerFromCusSM(description.getState(), InstallerUtilMethods.InstallerErrorStatus.STATUS_SILENT_FAILURE);
            } else {
                final Context globalContext = OtaApplication.getGlobalContext();
                if (z2) {
                    if (CusUtilMethods.isBatteryLow(globalContext)) {
                        try {
                            this._env.getUtilities().sendUpdateDownloadStatusError(str, UpgradeUtils.DownloadStatus.LOW_BATTERY_INSTALL, MetaDataBuilder.toJSONString(description.getMeta()));
                        } catch (JSONException e) {
                            resetStateForJSONException(str, e);
                        }
                        return;
                    }
                } else {
                    if (!UpdaterUtils.isInActiveCall(globalContext)) {
                        if (UpdaterUtils.isMapsNavigationRunning(globalContext)) {
                        }
                    }
                    globalContext.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                    if (UpdaterUtils.isInActiveCall(globalContext)) {
                        Logger.debug("OtaApp", "Device is in call, so it can not be rebooted ");
                        string = globalContext.getString(R.string.restart_notify_sanity_call_msg);
                    } else {
                        Logger.debug("OtaApp", "Maps is running, so device can not be rebooted ");
                        string = globalContext.getString(R.string.restart_notify_sanity_maps_msg);
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.motorola.ccc.ota.CusSM.1
                        @Override // java.lang.Runnable
                        public void run() {
                            Toast.makeText(globalContext, string, 1).show();
                        }
                    });
                    return;
                }
                NotificationUtils.clearNextPromptDetails(this.settings);
                UpdaterUtils.sendInstallModeStats(str2);
                NotificationUtils.cancelOtaNotification();
                this._db.setState(str, ApplicationEnv.PackageState.Upgrading, null);
                if (BuildPropReader.isATT()) {
                    this.settings.removeConfig(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME.key());
                    CusUtilMethods.settingMaxDeferTimeForFOTAUpgrade(description, 86400000L, this.settings);
                }
            }
            runStateMachine();
        }
    }

    public synchronized void onIntentFotaRequestUpdateResponse(long j, int i) {
        UpgradeUtils.Error fotaRequestUpdateResponse = FotaUpgradeSource.fotaRequestUpdateResponse(j, i);
        Logger.info("OtaApp", "CusSM.requestUpdateResponse: fota is authoritative, post response " + j + SystemUpdateStatusUtils.SPACE + fotaRequestUpdateResponse);
        this._env.getUtilities().sendCheckForUpdateResponse(fotaRequestUpdateResponse, (int) j, false);
    }

    public synchronized void onIntentFotaUpdateAvailable(long j, long j2, String str, boolean z, String str2, boolean z2) {
        if (ApplicationEnv.PackageState.IDLE != getCurrentState()) {
            Logger.debug("OtaApp", "CusSm.onIntentFotaUpdateAvailable state machine not in Idle state");
        } else {
            this._upgradePlugins.getUpgradeSource(UpgradeSourceType.fota).updateAvailable(j, j2, str, z, str2, z2);
        }
    }

    public synchronized void onIntentFotaDownloadModeChanged(long j, boolean z) {
        if (ApplicationEnv.PackageState.GettingPackage == getCurrentState()) {
            this._upgradePlugins.getUpgradeSource(UpgradeSourceType.fota).downloadModeChanged(j, z);
        }
    }

    public synchronized void onIntentFotaDownloadModeChanged() {
        if (BuildPropReader.isATT()) {
            int i = this.settings.getInt(Configs.STORED_AB_STATUS, 0);
            ApplicationEnv.PackageState currentState = getCurrentState();
            if (ApplicationEnv.PackageState.GettingPackage == currentState || (BuildPropReader.isStreamingUpdate() && ApplicationEnv.PackageState.ABApplyingPatch == currentState && i == 3)) {
                if (UpdaterUtils.isWifiOnly()) {
                    this._env.getUtilities().startFotaWifiDiscoveryTimer();
                } else {
                    this._env.getUtilities().cleanFotaWifiDiscoveryTimer();
                }
            }
        }
    }

    public synchronized void onIntentFotaDownloadCompleted(long j, int i, String str) {
        if (ApplicationEnv.PackageState.GettingPackage != getCurrentState()) {
            Logger.debug("OtaApp", "CusSM.downloadCompleted()state machine not in getting package state so return");
        } else {
            this._upgradePlugins.getUpgradeSource(UpgradeSourceType.fota).downloadCompleted(j, i, str);
        }
    }

    private void sendPluginUpdateStatus(ApplicationEnv.Database.Descriptor descriptor, UpgradeStatusConstents upgradeStatusConstents) {
        if (BuildPropReader.isBotaATT()) {
            FotaUpgradeSource.handleUpdateStatus(upgradeStatusConstents);
        } else {
            this._upgradePlugins.getUpgradeSource(UpgradeSourceType.valueOf(descriptor.getRepository())).handleUpdateStatus(descriptor, upgradeStatusConstents);
        }
    }

    public void initializeInstaller() {
        List<String> versions = this._db.getVersions();
        String str = null;
        if (versions != null) {
            Iterator<String> it = versions.iterator();
            while (it.hasNext()) {
                str = it.next();
            }
        }
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description != null) {
            _installType = InstallTypeResolver.getInstallTypeHandler(this._env, this.settings, this._db, description, this);
        }
    }

    private void initializePlugins() {
        LinkedList linkedList = new LinkedList();
        List<String> versions = this._db.getVersions();
        if (versions != null) {
            for (String str : versions) {
                ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
                if (description == null) {
                    Logger.error("OtaApp", "CusSM.initializePlugins: version " + str + " in db but could not get description");
                } else {
                    linkedList.add(description);
                }
            }
        }
        this._upgradePlugins.initializePlugins(linkedList);
    }

    private void releasePlugins() {
        this._upgradePlugins.releasePlugins();
    }

    private void checkForUpgradeCompleted() {
        Logger.debug("OtaApp", "CusSM.checkForUpgradeCompleted: no upgrade result files found");
        doSanityCheck();
    }

    private void checkForChain(boolean z, final UpgradeSourceType upgradeSourceType) {
        if (this.settings.getBoolean(Configs.INITIAL_SETUP_COMPLETED) && z) {
            OtaApplication.getScheduledExecutorService().schedule(new Runnable() { // from class: com.motorola.ccc.ota.CusSM.2
                @Override // java.lang.Runnable
                public void run() {
                    Logger.debug("OtaApp", "CusSM.checkForUpgradeCompleted checking for chained upgrade");
                    CusSM.this._upgradePlugins.getUpgradeSource(upgradeSourceType).sendChainUpgradeRequest(upgradeSourceType);
                    if (BuildPropReader.isATT()) {
                        CusSM.this._env.getFotaServices().sendRequestUpdate(1138L);
                    }
                }
            }, 900L, TimeUnit.SECONDS);
        }
    }

    private void checkForUpdateInSDCard(String str) {
        if (this._db.getDescription(this._env.getServices().getDeviceSha1()) != null) {
            Logger.error("OtaApp", "CusSM.checkForUpdateInSDCard : OTA update is going on");
        } else if (FileUtils.isExternalScopedStorageMounted()) {
            this._upgradePlugins.getUpgradeSource(UpgradeSourceType.sdcard).checkForUpdate(FileUtils.getExternalScopedStorage().getAbsolutePath(), str);
        }
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    class SMNewVersionHandler implements NewVersionHandler {
        SMNewVersionHandler() {
        }

        @Override // com.motorola.ccc.ota.NewVersionHandler
        public synchronized NewVersionHandler.ReturnCode handleNewVersion(MetaData metaData, String str, UpgradeSourceType upgradeSourceType, String str2, long j, String str3, String str4) {
            MetaData metaData2 = metaData;
            String str5 = str2;
            synchronized (this) {
                String deviceSha1 = BuildPropReader.getDeviceSha1(upgradeSourceType.toString());
                Logger.debug("OtaApp", String.format("CusSM.handleNewVersion: was notified of a new version : src %s %s : dest %s %s current %s size %d contentTimeStamp %d", metaData.getMinVersion(), metaData.getSourceSha1(), metaData.getVersion(), metaData.getTargetSha1(), deviceSha1, Long.valueOf(metaData.getSize()), Long.valueOf(j)));
                CusSM.this.settings.removeConfig(Configs.ONGOING_HISTORY_POLICY_DISPLAY_NAME);
                if (CusSM.this.systemUpdaterPolicy.isOtaUpdateDisabledByPolicyMngr()) {
                    String str6 = "Device is under disable ota update policy;";
                    String str7 = "Ota update is disabled by policy manager";
                    String str8 = ErrorCodeMapper.KEY_UPDATE_DISABLED_BY_POLICY_MNGR;
                    NewVersionHandler.ReturnCode returnCode = NewVersionHandler.ReturnCode.UPDATE_DISABLED_BY_POLICY_MNGR;
                    if (CusSM.this.systemUpdaterPolicy.isOtaUpdateDisabledPolicySet()) {
                        str6 = "isOtaUpdateDisabled policy is set;";
                        str7 = "Ota update is disabled by motorola policy manager";
                        str8 = ErrorCodeMapper.KEY_UPDATE_DISABLED_BY_MOTO_POLICY_MNGR;
                        returnCode = NewVersionHandler.ReturnCode.UPDATE_DISABLED_BY_MOTO_POLICY_MNGR;
                    } else if (CusSM.this.systemUpdaterPolicy.isDeviceUnderFreezePeriod()) {
                        str6 = "Device is under freeze period; policy type =" + CusSM.this.systemUpdaterPolicy.getPolicyType() + ";";
                        str7 = "Ota update is blocked as device is under freeze peroid";
                        str8 = ErrorCodeMapper.KEY_UPDATE_BLOCKED_FREEZE_PERIOD;
                        returnCode = NewVersionHandler.ReturnCode.UPDATE_BLOCKED_FREEZE_PERIOD;
                    }
                    NewVersionHandler.ReturnCode returnCode2 = returnCode;
                    CusSM.this.settings.setString(Configs.ONGOING_HISTORY_POLICY_DISPLAY_NAME, OtaApplication.getGlobalContext().getResources().getString(R.string.update_blocked_by_policy));
                    CusSM.this.failNotify(deviceSha1, metaData, upgradeSourceType.toString(), str, str7, str6 + str5, str8);
                    return returnCode2;
                } else if (UpdaterUtils.isBitMapSet(metaData.getBitmap(), UpdaterUtils.BitmapFeatures.bootloader.ordinal()) && BuildPropReader.getBootloaderStatus().equals(BuildPropReader.BOOTLOADER_UNLOCKED)) {
                    CusSM.this.failNotify(deviceSha1, metaData, upgradeSourceType.toString(), str, "Bootloader is unlocked", "Bootloader is unlocked; " + str5, ErrorCodeMapper.KEY_BOOTLOADER_UNLOCKED);
                    return NewVersionHandler.ReturnCode.NEW_VERSION_FAIL_BOOTLOADER_UNLOCKED;
                } else if (CusSM.this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE) && metaData.isWifiOnly() && !NetworkUtils.isWifi(CusSM.this.cm) && !CusSM.this.systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet()) {
                    CusSM.this.failNotify(deviceSha1, metaData, upgradeSourceType.toString(), str, "Vital update wifi only package, wifi not available", "Wifi not available, wifi only package, Vital update " + str5, ErrorCodeMapper.KEY_VU_WIFI_ONLY_PACKAGE_WIFI_NOT_AVAILABLE);
                    return NewVersionHandler.ReturnCode.VU_WIFI_ONLY_PACKAGE_WIFI_NOT_AVAILABLE;
                } else {
                    if (UpdaterUtils.isBitMapSet(metaData.getBitmap(), UpdaterUtils.BitmapFeatures.rooted.ordinal())) {
                        String isDeviceRooted = BuildPropReader.isDeviceRooted();
                        if (!isDeviceRooted.equalsIgnoreCase("false")) {
                            CusSM.this.failNotify(deviceSha1, metaData, upgradeSourceType.toString(), str, "Device is rooted", "Device is rooted; " + isDeviceRooted + " ;" + str5, ErrorCodeMapper.KEY_ROOTED);
                            if (isDeviceRooted.equalsIgnoreCase("enableVerity")) {
                                return NewVersionHandler.ReturnCode.NEW_VERSION_VERITY_DISABLED;
                            }
                            return NewVersionHandler.ReturnCode.NEW_VERSION_FAIL_ROOTED;
                        }
                    }
                    if (CusSM.this.settings.getBoolean(Configs.VAB_MERGE_DEVICE_CORRUPTED, false)) {
                        CusSM.this.failNotify(deviceSha1, metaData, upgradeSourceType.toString(), str, "Device is corrupted", "Device is corrupted; " + str5, ErrorCodeMapper.KEY_MERGE_STATUS_DEVICE_CORRUPTED);
                        return NewVersionHandler.ReturnCode.NEW_VERSION_FAIL_DEVICE_CORRUPTED;
                    } else if (metaData.getSize() <= 0) {
                        Logger.debug("OtaApp", "CusSM.handleNewVersion failed: size of package not proper");
                        CusSM.this.failNotify(deviceSha1, metaData, upgradeSourceType.toString(), str, "Package size is improper" + metaData.getSize(), str2, ErrorCodeMapper.KEY_PACKAGE_SIZE_ZERO);
                        return NewVersionHandler.ReturnCode.NEW_VERSION_INVALID;
                    } else {
                        if (metaData.getFingerprint() != null && metaData.getFingerprint().length() > 0) {
                            String fingerPrint = BuildPropReader.getFingerPrint();
                            if (!fingerPrint.equals(metaData.getFingerprint())) {
                                Logger.debug("OtaApp", "CusSM.handleNewVersion: version ( finger print  on device: " + fingerPrint + " and fingerprint in package " + metaData.getFingerprint() + ") does  not match: ");
                                CusSM.this.failNotify(deviceSha1, metaData, upgradeSourceType.toString(), str, "Finger print on device: " + fingerPrint + " does not match with the fingerprint present in package " + metaData.getFingerprint(), str2, ErrorCodeMapper.KEY_FINGERPRINT_MISMATCH);
                                return NewVersionHandler.ReturnCode.NEW_VERSION_INVALID;
                            }
                        }
                        String[] isUpgradeAcceptable = CusSM.this._upgradePlugins.getUpgradeSource(upgradeSourceType).isUpgradeAcceptable(metaData2, upgradeSourceType);
                        if (isUpgradeAcceptable[0] != null) {
                            Logger.info("OtaApp", "CusSm.isAcceptable: " + isUpgradeAcceptable[0]);
                            CusSM.this.failNotify(deviceSha1, metaData, upgradeSourceType.toString(), str, isUpgradeAcceptable[0], str2, isUpgradeAcceptable[1]);
                            return NewVersionHandler.ReturnCode.NEW_VERSION_INVALID;
                        }
                        long j2 = CusSM.this.settings.getLong(Configs.PREVIOUS_CANCELLED_OPT_CONTENT_TIMESTAMP, -1L);
                        if (j2 != -1 && j2 == j && System.currentTimeMillis() < CusSM.this.settings.getLong(Configs.PREVIOUS_CANCELLED_OPT_UPDATE_ANNOY_TIME, 0L)) {
                            Logger.info("OtaApp", "CusSM.handleNewVersion: already working on...User has already declined this package and currently in don't bother me window");
                            CusSM.this.failNotify(deviceSha1, metaData, upgradeSourceType.toString(), str, "already working on...User has already declined this package and currently in don't bother me window", str2, ErrorCodeMapper.KEY_DONT_BOTHER_WINDOW);
                            return NewVersionHandler.ReturnCode.NEW_VERSION_INVALID;
                        } else if (metaData.getEnterpriseOta() && CusSM.this.systemUpdaterPolicy.getPolicyType() == 3 && System.currentTimeMillis() < CusSM.this.settings.getLong(Configs.SYSTEM_UPDATE_POLICY_POSTPONE, -1L)) {
                            CusSM.this.settings.setString(Configs.ONGOING_HISTORY_POLICY_DISPLAY_NAME, OtaApplication.getGlobalContext().getResources().getString(R.string.update_postpone_policy));
                            CusSM.this.failNotify(deviceSha1, metaData, upgradeSourceType.toString(), str, "CusSM.handleNewVersion: device is under system update policy :postpone policy is set and end Time is : " + CusSM.this.settings.getLong(Configs.SYSTEM_UPDATE_POLICY_POSTPONE, -1L), str2, ErrorCodeMapper.KEY_SYSTEM_UPDATE_POLICY);
                            CusUtilMethods.notifySoftwareUpdate(System.currentTimeMillis(), UpdateType.DIFFUpdateType.SMR.toString().equalsIgnoreCase(metaData.getUpdateTypeData()));
                            return NewVersionHandler.ReturnCode.NEW_VERSION_INVALID;
                        } else {
                            if (CusUtilMethods.isItFirstNetOnFota(OtaApplication.getGlobalContext())) {
                                metaData2 = MetadataOverrider.overWriteAttFirstNetUpdateReminderValues(metaData2, UpgradeUtils.DEFAULT_CRITICAL_UPDATE_ANNOY_VALUE, 3);
                            }
                            MetaData from = MetadataOverrider.from(metaData2, CusSM.this.settings, CusSM.this.settings.getString(Configs.TRIGGERED_BY), false, str3, str4);
                            if (!MetadataOverrider.saveMetadata(from, CusSM.this.settings, UpgradeSourceType.modem == upgradeSourceType)) {
                                Logger.error("OtaApp", "CusSM.handleNewVersion failed: could not store overridedmetadata in prefs");
                                return NewVersionHandler.ReturnCode.NEW_VERSION_FAIL;
                            }
                            if (CusSM.this.systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet()) {
                                str5 = "allowFotaOverAnyDataNetwork policy is enabled; " + str5;
                            }
                            if (from.isWifiOnly()) {
                                str5 = "WiFi-only package; " + str5;
                            }
                            if (UpgradeSourceType.sdcard == upgradeSourceType && BuildPropReader.doesDeviceSupportVABUpdate() && FileUtils.VALIDATION.equalsIgnoreCase(from.getPackageType())) {
                                return NewVersionHandler.ReturnCode.VAB_VALIDATION_PKG_FOUND;
                            } else if (!CusSM.this._db.insert(deviceSha1, upgradeSourceType.toString(), from, str, str5)) {
                                Logger.error("OtaApp", "CusSM.handleNewVersion failed: could not store data for " + deviceSha1 + "in db");
                                return NewVersionHandler.ReturnCode.NEW_VERSION_FAIL;
                            } else {
                                CusSM.this._env.getUtilities().sendUpdaterStateReset();
                                if (UpdaterUtils.isFeatureOn(CusSM.this.settings.getString(Configs.STATS_FEATURE))) {
                                    CusSM.this._sl.startStatsListener();
                                    CusSM.this.settings.setString(Configs.STATS_ROOT_STATUS_BEFORE_UPGRADE, BuildPropReader.getRootStatus());
                                    CusSM.this.settings.setString(Configs.STATS_UPGRADE_SOURCE, CusSM.this.settings.getString(Configs.TRIGGERED_BY));
                                }
                                CusUtilMethods.notifySoftwareUpdate(System.currentTimeMillis(), UpdateType.DIFFUpdateType.SMR.toString().equalsIgnoreCase(from.getUpdateTypeData()));
                                return NewVersionHandler.ReturnCode.NEW_VERSION_OK;
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendUpgradeStatus() {
        String metaSourceSha1 = UpdaterUtils.getMetaSourceSha1();
        if (TextUtils.isEmpty(metaSourceSha1)) {
            metaSourceSha1 = this._env.getServices().getDeviceSha1();
        }
        sendUpgradeStatus(metaSourceSha1);
    }

    /* JADX WARN: Removed duplicated region for block: B:100:0x0347  */
    /* JADX WARN: Removed duplicated region for block: B:103:0x0353 A[Catch: all -> 0x0585, TryCatch #1 {, blocks: (B:4:0x001b, B:6:0x0021, B:9:0x0037, B:11:0x003f, B:13:0x0054, B:15:0x005c, B:16:0x0063, B:18:0x0069, B:20:0x0079, B:23:0x008a, B:25:0x00a2, B:27:0x00b7, B:28:0x00c3, B:30:0x010f, B:32:0x011f, B:35:0x013b, B:37:0x017f, B:39:0x018b, B:40:0x0199, B:50:0x01d0, B:54:0x0201, B:56:0x0207, B:58:0x0213, B:61:0x0220, B:63:0x022c, B:66:0x0239, B:69:0x0248, B:74:0x025c, B:76:0x026a, B:78:0x0272, B:97:0x0306, B:99:0x0335, B:101:0x0349, B:103:0x0353, B:105:0x0369, B:107:0x0389, B:109:0x0394, B:111:0x0422, B:112:0x0472, B:79:0x0279, B:81:0x0287, B:83:0x0291, B:92:0x02d3, B:95:0x02f2, B:86:0x029a, B:88:0x02ba, B:90:0x02c1, B:43:0x01b0, B:46:0x01bf, B:34:0x012e), top: B:120:0x001b, inners: #0 }] */
    /* JADX WARN: Removed duplicated region for block: B:104:0x0367  */
    /* JADX WARN: Removed duplicated region for block: B:107:0x0389 A[Catch: all -> 0x0585, TryCatch #1 {, blocks: (B:4:0x001b, B:6:0x0021, B:9:0x0037, B:11:0x003f, B:13:0x0054, B:15:0x005c, B:16:0x0063, B:18:0x0069, B:20:0x0079, B:23:0x008a, B:25:0x00a2, B:27:0x00b7, B:28:0x00c3, B:30:0x010f, B:32:0x011f, B:35:0x013b, B:37:0x017f, B:39:0x018b, B:40:0x0199, B:50:0x01d0, B:54:0x0201, B:56:0x0207, B:58:0x0213, B:61:0x0220, B:63:0x022c, B:66:0x0239, B:69:0x0248, B:74:0x025c, B:76:0x026a, B:78:0x0272, B:97:0x0306, B:99:0x0335, B:101:0x0349, B:103:0x0353, B:105:0x0369, B:107:0x0389, B:109:0x0394, B:111:0x0422, B:112:0x0472, B:79:0x0279, B:81:0x0287, B:83:0x0291, B:92:0x02d3, B:95:0x02f2, B:86:0x029a, B:88:0x02ba, B:90:0x02c1, B:43:0x01b0, B:46:0x01bf, B:34:0x012e), top: B:120:0x001b, inners: #0 }] */
    /* JADX WARN: Removed duplicated region for block: B:108:0x0392  */
    /* JADX WARN: Removed duplicated region for block: B:111:0x0422 A[Catch: all -> 0x0585, TryCatch #1 {, blocks: (B:4:0x001b, B:6:0x0021, B:9:0x0037, B:11:0x003f, B:13:0x0054, B:15:0x005c, B:16:0x0063, B:18:0x0069, B:20:0x0079, B:23:0x008a, B:25:0x00a2, B:27:0x00b7, B:28:0x00c3, B:30:0x010f, B:32:0x011f, B:35:0x013b, B:37:0x017f, B:39:0x018b, B:40:0x0199, B:50:0x01d0, B:54:0x0201, B:56:0x0207, B:58:0x0213, B:61:0x0220, B:63:0x022c, B:66:0x0239, B:69:0x0248, B:74:0x025c, B:76:0x026a, B:78:0x0272, B:97:0x0306, B:99:0x0335, B:101:0x0349, B:103:0x0353, B:105:0x0369, B:107:0x0389, B:109:0x0394, B:111:0x0422, B:112:0x0472, B:79:0x0279, B:81:0x0287, B:83:0x0291, B:92:0x02d3, B:95:0x02f2, B:86:0x029a, B:88:0x02ba, B:90:0x02c1, B:43:0x01b0, B:46:0x01bf, B:34:0x012e), top: B:120:0x001b, inners: #0 }] */
    /* JADX WARN: Removed duplicated region for block: B:99:0x0335 A[Catch: all -> 0x0585, TryCatch #1 {, blocks: (B:4:0x001b, B:6:0x0021, B:9:0x0037, B:11:0x003f, B:13:0x0054, B:15:0x005c, B:16:0x0063, B:18:0x0069, B:20:0x0079, B:23:0x008a, B:25:0x00a2, B:27:0x00b7, B:28:0x00c3, B:30:0x010f, B:32:0x011f, B:35:0x013b, B:37:0x017f, B:39:0x018b, B:40:0x0199, B:50:0x01d0, B:54:0x0201, B:56:0x0207, B:58:0x0213, B:61:0x0220, B:63:0x022c, B:66:0x0239, B:69:0x0248, B:74:0x025c, B:76:0x026a, B:78:0x0272, B:97:0x0306, B:99:0x0335, B:101:0x0349, B:103:0x0353, B:105:0x0369, B:107:0x0389, B:109:0x0394, B:111:0x0422, B:112:0x0472, B:79:0x0279, B:81:0x0287, B:83:0x0291, B:92:0x02d3, B:95:0x02f2, B:86:0x029a, B:88:0x02ba, B:90:0x02c1, B:43:0x01b0, B:46:0x01bf, B:34:0x012e), top: B:120:0x001b, inners: #0 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private synchronized void sendUpgradeStatus(java.lang.String r59) {
        /*
            Method dump skipped, instructions count: 1416
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.ccc.ota.CusSM.sendUpgradeStatus(java.lang.String):void");
    }

    private synchronized void getUpgrade(ApplicationEnv.Database.Descriptor descriptor) {
        String localPath;
        String str;
        UpgradeSource upgradeSource;
        DownloadHandler downloadHandler = _downloader;
        if (downloadHandler != null) {
            if (downloadHandler.isBusy()) {
                Logger.debug("OtaApp", "CusSM.getUpgrade: copying or downloading is in progress");
                return;
            } else {
                _downloader.close();
                _downloader = null;
            }
        }
        UpgradeSource upgradeSource2 = this._upgradePlugins.getUpgradeSource(UpgradeSourceType.valueOf(descriptor.getRepository()));
        if (upgradeSource2.doYouDownloadDirectly(descriptor)) {
            upgradeSource2.downloadDirectly(descriptor);
            return;
        }
        MetaData meta = descriptor.getMeta();
        Logger.debug("OtaApp", "package size: " + meta.getSize());
        if (UpgradeSourceType.modem.toString().equals(descriptor.getRepository())) {
            localPath = FileUtils.getModemDownloadFilePath();
        } else {
            localPath = FileUtils.getLocalPath(this.settings);
        }
        DownloadHelper downloadHelper = new DownloadHelper(descriptor.getVersion(), localPath, meta.getSize());
        if (downloadHelper.isDone()) {
            Logger.info("OtaApp", "CusSM.getUpgrade: no download, but file " + downloadHelper.fileName() + " is already here");
            if (!upgradeSource2.verifyFile(this._env, downloadHelper.fileName(), this.settings)) {
                failDownload(descriptor.getVersion(), UpgradeUtils.DownloadStatus.STATUS_VERIFY, this.settings.getString(Configs.UPGRADE_STATUS_VERIFY), ErrorCodeMapper.KEY_PACKAGE_VERIFICATION_FAILED);
                return;
            } else {
                onInternalNotification(descriptor.getVersion(), descriptor.getRepository(), null);
                return;
            }
        }
        this.settings.removeConfig(Configs.DATA_SPACE_RETRY_COUNT);
        Logger.debug("OtaApp", "CusSM.getUpgrade: finding space");
        long extraSpace = meta.getExtraSpace() <= 0 ? 52428800L : meta.getExtraSpace();
        Logger.debug("OtaApp", "CusSM.getUpgrade: ExtraSpace: " + extraSpace);
        if (FileUtils.isSpaceAvailableData(this._env, extraSpace)) {
            str = "CusSM, Available: ";
            upgradeSource = upgradeSource2;
        } else {
            long availableDataPartitionSize = this._env.getUtilities().getAvailableDataPartitionSize();
            str = "CusSM, Available: ";
            upgradeSource = upgradeSource2;
            Logger.debug("OtaApp", "CusSM.getUpgrade: ExtraSpace: " + meta.getExtraSpace() + "availableSpace: " + availableDataPartitionSize);
            if (extraSpace > availableDataPartitionSize) {
                if (!freeFromReserveSpace((extraSpace - availableDataPartitionSize) / 1048576)) {
                    Logger.debug("OtaApp", "CusSM-ExtraSpace: deleting of some reserve files failed");
                    failDownload(descriptor.getVersion(), UpgradeUtils.DownloadStatus.STATUS_SPACE, "CusSM-ExtraSpace: Device does not have enough extra free space on /data.Update needs" + extraSpace + " Bytes.Available space on /data is" + availableDataPartitionSize + " Bytes.", ErrorCodeMapper.KEY_DATA_OUT_OF_SPACE);
                }
                return;
            }
        }
        FileUtils.ReturnCode packageDownloadLocation = FileUtils.setPackageDownloadLocation(this._env, downloadHelper.left(), extraSpace, this.settings, descriptor.getVersion());
        Logger.debug("OtaApp", "CusSM-getExtraSpace: " + meta.getExtraSpace());
        DownloadHelper downloadHelper2 = new DownloadHelper(descriptor.getVersion(), FileUtils.getLocalPath(this.settings), meta.getSize());
        if (FileUtils.ReturnCode.PACKAGE_PATH_FAIL_DATA == packageDownloadLocation) {
            long availableDataPartitionSize2 = this._env.getUtilities().getAvailableDataPartitionSize();
            if (downloadHelper2.left() + extraSpace > availableDataPartitionSize2) {
                Logger.debug("OtaApp", str + availableDataPartitionSize2 + ", Required Space: " + (downloadHelper2.left() + extraSpace) + " bytes");
                if (!freeFromReserveSpace(((downloadHelper2.left() + extraSpace) - availableDataPartitionSize2) / 1048576)) {
                    Logger.debug("OtaApp", "CusSM, deleting of some reserve files failed");
                    failDownload(descriptor.getVersion(), UpgradeUtils.DownloadStatus.STATUS_SPACE, "CusSM-ExtraSpace: Not enough free space on /data. Package needs " + (extraSpace + downloadHelper2.left()) + " Bytes. Available space on /data is " + availableDataPartitionSize2 + "Bytes.", ErrorCodeMapper.KEY_DATA_OUT_OF_SPACE);
                }
                return;
            }
        } else if (FileUtils.ReturnCode.PACKAGE_PATH_FAIL_SDCARD == packageDownloadLocation) {
            if (this._env.getServices().isSDCardMounted().booleanValue()) {
                try {
                    this._env.getUtilities().sendUpdateDownloadStatusError(descriptor.getVersion(), UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_SPACE, MetaDataBuilder.toJSONString(meta));
                } catch (JSONException e) {
                    resetStateForJSONException(descriptor.getVersion(), e);
                }
            } else if (this._env.getServices().isSDCardPresent().booleanValue()) {
                this._env.getUtilities().sendUpdateDownloadStatusError(descriptor.getVersion(), UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_NOTMOUNTED);
            } else {
                this._env.getUtilities().sendUpdateDownloadStatusError(descriptor.getVersion(), UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_NOSDCARD);
            }
            return;
        }
        DownloadHandler downloadHandler2 = upgradeSource.getDownloadHandler();
        _downloader = downloadHandler2;
        downloadHandler2.transferUpgrade(descriptor);
    }

    private void getPayloadMetaDataDownload(ApplicationEnv.Database.Descriptor descriptor) {
        DownloadHandler downloadHandler = _downloader;
        if (downloadHandler != null) {
            if (downloadHandler.isBusy()) {
                Logger.debug("OtaApp", "CusSM.getPayloadMetaDataDownload: downloading is in progress");
                return;
            } else {
                _downloader.close();
                _downloader = null;
            }
        }
        PayloadMetaDataDownloader payloadMetaDataDownloader = new PayloadMetaDataDownloader(OtaApplication.getGlobalContext(), this, this.settings, this._env);
        _downloader = payloadMetaDataDownloader;
        payloadMetaDataDownloader.transferUpgrade(descriptor);
    }

    private boolean freeFromReserveSpace(long j) {
        Logger.debug("OtaApp", "Total number of reserved space files : " + FileUtils.getNumFilesInDir() + " Number of files to be deleted: " + FileUtils.calculateNumFilesNeeded(j));
        if (FileUtils.getNumFilesInDir() <= 0 || !FileUtils.deleteReserveSpaceFiles(FileUtils.calculateNumFilesNeeded(j))) {
            return false;
        }
        Logger.debug("OtaApp", "CusSM-freeFromReserveSpace: Freed " + j + " MB");
        runStateMachine();
        return true;
    }

    private synchronized UpgradeUtilConstants.CheckSpaceEnum checkSpace(ApplicationEnv.Database.Descriptor descriptor, String str) {
        int i = this.settings.getInt(Configs.DATA_SPACE_RETRY_COUNT, 0);
        long availableDataPartitionSize = this._env.getUtilities().getAvailableDataPartitionSize();
        long extraSpace = descriptor.getMeta().getExtraSpace();
        if (extraSpace <= 0) {
            extraSpace = 52428800;
        }
        Logger.debug("OtaApp", "CusSM.checkSpace, available space in data is " + (availableDataPartitionSize / 1048576) + " MB; extra space needed in data is " + (extraSpace / 1048576) + " MB; dataSpaceRetryCount " + i);
        if (availableDataPartitionSize < extraSpace) {
            long j = (extraSpace - availableDataPartitionSize) / 1048576;
            if (FileUtils.getNumFilesInDir() > 0 && FileUtils.deleteReserveSpaceFiles(FileUtils.calculateNumFilesNeeded((int) j))) {
                Logger.debug("OtaApp", "CusSM.checkSpace: Freed " + j + " MB");
                return UpgradeUtilConstants.CheckSpaceEnum.SPACE_AVAILABLE;
            }
            Logger.info("OtaApp", "CusSM.checkSpace: not enough free space on /data. Package needs \"" + (extraSpace > 0 ? extraSpace / 1048576 : 50L) + " MB\" free, available space on /data is \"" + (availableDataPartitionSize / 1048576) + " MB\".");
            return UpgradeUtilConstants.CheckSpaceEnum.SPACE_NOT_AVAILABLE;
        } else if (i > 0) {
            NotificationUtils.stopNotificationService(OtaApplication.getGlobalContext());
            this.settings.removeConfig(Configs.DATA_SPACE_RETRY_COUNT);
            this._db.setVersionState(str, ApplicationEnv.PackageState.QueryingInstall, null);
            runStateMachine();
            return UpgradeUtilConstants.CheckSpaceEnum.SPACE_AVAILABLE_AFTER_LOW_STORAGE;
        } else {
            this.settings.removeConfig(Configs.DATA_SPACE_RETRY_COUNT);
            return UpgradeUtilConstants.CheckSpaceEnum.SPACE_AVAILABLE;
        }
    }

    public synchronized String getDeviceAdditionalInfo() {
        return this._env.getUtilities().getDeviceAdditionalInfo();
    }

    public synchronized ApplicationEnv.PackageState getCurrentState(String str) {
        if (TextUtils.isEmpty(str)) {
            return ApplicationEnv.PackageState.IDLE;
        }
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            return ApplicationEnv.PackageState.IDLE;
        }
        return description.getState();
    }

    public synchronized ApplicationEnv.PackageState getCurrentState() {
        return getCurrentState(UpdaterUtils.getMetaSourceSha1());
    }

    private synchronized void failProgress(String str, UpgradeUtils.DownloadStatus downloadStatus, String str2, String str3) {
        DownloadHandler downloadHandler = _downloader;
        if (downloadHandler != null) {
            downloadHandler.close();
            _downloader = null;
        }
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            Logger.error("OtaApp", "no version found for this version in db,drop the request to floor");
            return;
        }
        MetaData meta = description.getMeta();
        this._db.setState(str, ApplicationEnv.PackageState.Result, false, str2, str3);
        if (str.equals(BuildPropReader.getDeviceModemConfigVersionSha1())) {
            cleanupVersion(str);
        } else {
            this.settings.incrementPrefs(Configs.UPDATE_FAIL_COUNT);
            scheduleCreateReserveSpace();
            cleanupVersion(str);
            try {
                this._env.getUtilities().sendUpdateDownloadStatusError(str, downloadStatus, MetaDataBuilder.toJSONString(meta), description.getRepository());
            } catch (JSONException unused) {
            }
            sendPluginUpdateStatus(description, UpgradeStatusConstents.getUpgradeStatusConstants(downloadStatus));
        }
        sendUpgradeStatus(str);
    }

    public synchronized void failDownloadInternalSilent(String str, String str2, String str3) {
        DownloadHandler downloadHandler = _downloader;
        if (downloadHandler != null) {
            downloadHandler.close();
            _downloader = null;
        }
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            Logger.error("OtaApp", "no version found for this version in db,drop the request to floor");
            return;
        }
        Logger.info("OtaApp", "Something gone wrong, Server told to cancel the package with version: " + str);
        this._db.setState(str, ApplicationEnv.PackageState.Result, false, str2, str3);
        if (BuildPropReader.getDeviceModemConfigVersionSha1().equals(str)) {
            cleanupVersion(str);
        } else {
            this.settings.incrementPrefs(Configs.UPDATE_FAIL_COUNT);
            scheduleCreateReserveSpace();
            cleanupVersion(str);
            sendPluginUpdateStatus(description, UpgradeStatusConstents.getUpgradeStatusConstants(null));
        }
        sendUpgradeStatus(str);
    }

    public synchronized void failDownload(String str, UpgradeUtils.DownloadStatus downloadStatus, String str2, String str3) {
        failProgress(str, downloadStatus, downloadStatus.toString() + ";  AddOnInfo: " + str2, str3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void failNotify(String str, MetaData metaData, String str2, String str3, String str4, String str5, String str6) {
        String str7 = str6 + SmartUpdateUtils.MASK_SEPARATOR + str4;
        Logger.error("OtaApp", "CusSM.failNotify: notification failed from repository " + str2 + " for reason " + str7 + "; additional information: " + str5);
        if (!isBusy()) {
            this._db.setStatus(str, metaData, str2, str3, str7, str5);
            sendUpgradeStatus(str);
        }
    }

    public synchronized ApplicationEnv.Database.Descriptor getDescription(String str) {
        return this._db.getDescription(str);
    }

    public MetaData getMetaData() {
        return getMetaData(UpdaterUtils.getMetaSourceSha1());
    }

    public MetaData getMetaData(String str) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            Logger.debug("OtaApp", "Description is null");
            return null;
        }
        return description.getMeta();
    }

    private void cleanupVersion(String str) {
        UpdaterUtils.setOtaSystemBindServiceEnabledState(OtaApplication.getGlobalContext(), false);
        if (str.equals(BuildPropReader.getDeviceModemConfigVersionSha1())) {
            cleanupModemVersion(str);
            return;
        }
        if (this.settings.getString(Configs.TRACKINGID) != null) {
            this.settings.setString(Configs.PREVIOUS_TRACKING_ID, this.settings.getString(Configs.TRACKINGID));
        }
        FileUtils.cleanupFiles();
        FileUtils.cleanScopedStorageFiles();
        FileUtils.cleanupPrefs(this._env, this.settings);
        if (UpdaterUtils.isFeatureOn(this.settings.getString(Configs.STATS_FEATURE))) {
            this._sl.stopListener();
        }
        UpdaterUtils.disableBatteryStatusReceiver();
        UpdaterUtils.clearPrepareForUnattendedUpdate();
        this._db.remove(str);
        InstallTypeResolver.Installer installer = _installType;
        if (installer != null) {
            installer.clearRetryTasks();
        }
        this._env.getUtilities().sendUpdaterStateReset();
        unregisterFromHealthCheck();
    }

    private void cleanupVersionSilent(String str) {
        if (this.settings.getString(Configs.TRACKINGID) != null) {
            this.settings.setString(Configs.PREVIOUS_TRACKING_ID, this.settings.getString(Configs.TRACKINGID));
        }
        FileUtils.cleanScopedStorageFiles();
        FileUtils.cleanupPrefs(this._env, this.settings);
        if (UpdaterUtils.isFeatureOn(this.settings.getString(Configs.STATS_FEATURE))) {
            this._sl.stopListener();
        }
        UpdaterUtils.disableBatteryStatusReceiver();
        this._db.remove(str);
        InstallTypeResolver.Installer installer = _installType;
        if (installer != null) {
            installer.clearRetryTasks();
        }
        this._env.getUtilities().sendUpdaterStateReset();
        unregisterFromHealthCheck();
    }

    private void cleanupModemVersion(String str) {
        if (this.settings.getString(Configs.MODEM_TRACKINGID) != null) {
            this.settings.setString(Configs.MODEM_PREVIOUS_TRACKING_ID, this.settings.getString(Configs.MODEM_TRACKINGID));
        }
        FileUtils.cleanupModemPrefs(this.settings);
        FileUtils.cleanupModemFiles();
        this._db.remove(str);
        if (UpdaterUtils.canISendSuccessiveModemPollRequest()) {
            onModemPollingExpiryNotification(false, true);
        }
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            return;
        }
        InstallTypeResolver.Installer installTypeHandler = InstallTypeResolver.getInstallTypeHandler(this._env, this.settings, this._db, description, this);
        _installType = installTypeHandler;
        if (installTypeHandler != null) {
            installTypeHandler.clearRetryTasks();
        }
    }

    private void doSanityCheck() {
        String str;
        List<String> versions = this._db.getVersions();
        if (versions == null) {
            cleanupVersion("");
            return;
        }
        for (String str2 : versions) {
            ApplicationEnv.Database.Descriptor description = this._db.getDescription(str2);
            if (!UpgradeSourceType.modem.toString().equals(description.getRepository())) {
                UpgradeSource upgradeSource = this._upgradePlugins.getUpgradeSource(UpgradeSourceType.valueOf(description.getRepository()));
                if (upgradeSource.isChangeInSrc(description.getMeta(), description.getRepository())) {
                    if (description.getState() == ApplicationEnv.PackageState.Result) {
                        continue;
                    } else if (upgradeSource.isUpdateSuccessful(description.getMeta(), description.getRepository())) {
                        if (BuildPropReader.doesDeviceSupportVABUpdate() && UpdaterUtils.isBitMapSet(description.getMeta().getBitmap(), UpdaterUtils.BitmapFeatures.enableVABMergeFeature.ordinal())) {
                            UpdaterUtils.setOtaSystemBindServiceEnabledState(OtaApplication.getGlobalContext(), true);
                            this._db.setVersionState(str2, ApplicationEnv.PackageState.MergePending, null);
                            this.settings.setBoolean(Configs.VAB_MERGE_PROCESS_RUNNING, true);
                            return;
                        }
                        resetToUpgradeSuccess(str2, description);
                    } else {
                        Logger.error("OtaApp", "CusSM.doSanityCheck: !!forcefully moving version:" + str2 + " to Result state as there is a version mismatch!!");
                        resetToUpgradeFailure(str2, description, "Forcefully moving version: " + str2 + " to Result state current version is " + BuildPropReader.getDeviceSha1(description.getRepository()), ErrorCodeMapper.KEY_TARGET_VERSION_MISMATCH);
                    }
                } else if (this.settings.getInt(Configs.UPGRADE_ATTEMPT_COUNT, 0) >= this.settings.getInt(Configs.MAX_UPGRADE_ATTEMPT_COUNT, 1)) {
                    if (!BuildPropReader.isUEUpdateEnabled()) {
                        str = "Update failed due to an unknown problem during upgrade";
                    } else {
                        str = FileUtils.getFailureDetailsFromFileToUpload(FileUtils.UPDATER_ENGINE_LOG_FILE, null);
                    }
                    resetToUpgradeFailure(str2, description, str, ErrorCodeMapper.KEY_DEVICE_BOOTED_FROM_SRC);
                }
            }
        }
    }

    private void resetToUpgradeSuccess(String str, ApplicationEnv.Database.Descriptor descriptor) {
        MetaData meta = descriptor.getMeta();
        String str2 = BuildPropReader.isUEUpdateEnabled() ? SystemUpdateStatusUtils.KEY_UPATE_SUCCESS : "Device is already upgraded to target version but result file is missing";
        this.settings.removeConfig(Configs.INCREMENTAL_VERSION);
        this.settings.removeConfig(Configs.UPDATE_FAILURE_COUNT);
        String updateTypeData = descriptor.getMeta().getUpdateTypeData();
        if (String.valueOf(UpdateType.DIFFUpdateType.MR).equalsIgnoreCase(updateTypeData) || String.valueOf(UpdateType.DIFFUpdateType.OS).equalsIgnoreCase(updateTypeData)) {
            this.settings.setInt(Configs.MODEM_POLLING_COUNT, 0);
            this.settings.setLong(Configs.POLL_MODEM_AFTER, 604800000L);
            this.settings.setInt(Configs.MAX_MODEM_POLLING_COUNT, 3);
            Logger.debug("OtaApp", "MR/OS update is success re-scheduling modem polling");
            UpdaterUtils.scheduleModemWorkManager(OtaApplication.getGlobalContext());
        }
        sendPluginUpdateStatus(descriptor, UpgradeStatusConstents.Successfully_Launched_The_Upgrade);
        this._db.setState(str, ApplicationEnv.PackageState.Result, true, str2, ErrorCodeMapper.KEY_SUCCESS);
        UpdaterUtils.insertHistory(meta.getDisplayVersion(), meta.getUpdateTypeData(), meta.getReleaseNotes(), meta.getPostInstallNotes(), this.settings);
        this.settings.setInt(Configs.UPDATE_FAIL_COUNT, 0);
        try {
            this._env.getUtilities().sendUpdateStatus(this._env.getServices().getDeviceSha1(), MetaDataBuilder.toJSONString(meta), str2, true, 0);
            UpgradeSourceType valueOf = UpgradeSourceType.valueOf(descriptor.getRepository());
            boolean checkForChainUpgrade = this._upgradePlugins.getUpgradeSource(valueOf).checkForChainUpgrade(valueOf);
            this._env.getUtilities().triggerForceDeviceLogin();
            CusUtilMethods.setReserveSpaceInMB(descriptor, this.settings);
            scheduleCreateReserveSpace();
            cleanupVersion(str);
            checkForChain(checkForChainUpgrade, valueOf);
        } catch (JSONException e) {
            Logger.error("OtaApp", "CusSM:resetToUpgradeSuccess(): caught exception " + e);
        }
    }

    private void resetToUpgradeFailure(String str, ApplicationEnv.Database.Descriptor descriptor, String str2, String str3) {
        this.settings.incrementEvenUpgradefailureRetries();
        sendPluginUpdateStatus(descriptor, UpgradeStatusConstents.Unsuccessfully_Launched_The_Upgrade);
        this._db.setState(str, ApplicationEnv.PackageState.Result, false, str2, str3);
        this.settings.incrementPrefs(Configs.UPDATE_FAIL_COUNT);
        try {
            this._env.getUtilities().sendUpdateStatus(this._env.getServices().getDeviceSha1(), MetaDataBuilder.toJSONString(descriptor.getMeta()), str2, false, this.settings.getInt(Configs.UPDATE_FAILURE_COUNT, 0));
            scheduleCreateReserveSpace();
            cleanupVersion(str);
        } catch (JSONException e) {
            Logger.error("OtaApp", "CusSM:checkForUpgradeCompleted(): caught exception " + e);
        }
    }

    public synchronized void pleaseRunStateMachine() {
        runStateMachine();
    }

    public void registerForHealthCheck() {
        long j = this.settings.getLong(Configs.HEALTH_CHECK_TIME, -1L);
        if (j <= 0) {
            j = System.currentTimeMillis() + 86400000;
            this.settings.setLong(Configs.HEALTH_CHECK_TIME, j);
        }
        Context globalContext = OtaApplication.getGlobalContext();
        PendingIntent broadcast = PendingIntent.getBroadcast(globalContext, 1, new Intent(UpgradeUtilConstants.INTENT_HEALTH_CHECK), 335544320);
        AlarmManager alarmManager = (AlarmManager) globalContext.getSystemService("alarm");
        alarmManager.cancel(broadcast);
        alarmManager.setExactAndAllowWhileIdle(0, j, broadcast);
        Logger.debug("OtaApp", "Health check will run on " + DateFormatUtils.getCalendarString(globalContext, j));
    }

    public void unregisterFromHealthCheck() {
        this.settings.removeConfig(Configs.HEALTH_CHECK_TIME);
        Context globalContext = OtaApplication.getGlobalContext();
        ((AlarmManager) globalContext.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(globalContext, 1, new Intent(UpgradeUtilConstants.INTENT_HEALTH_CHECK), 335544320));
        Logger.debug("OtaApp", "Health check alarm cleared");
    }

    public synchronized void timeForAHealthCheckUp() {
        this.settings.setLong(Configs.HEALTH_CHECK_TIME, 0L);
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description != null && (description.getState() == ApplicationEnv.PackageState.Notified || description.getState() == ApplicationEnv.PackageState.RequestPermission || description.getState() == ApplicationEnv.PackageState.Querying || description.getState() == ApplicationEnv.PackageState.QueryingInstall || description.getState() == ApplicationEnv.PackageState.VerifyAllocateSpace || description.getState() == ApplicationEnv.PackageState.MergeRestart)) {
            this._db.setState(deviceSha1, description.getState(), null);
            Logger.debug("OtaApp", "Time for a health check up");
            sendUpgradeStatus();
        }
    }

    public synchronized boolean isBusy() {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(UpdaterUtils.getMetaSourceSha1());
        if (description == null) {
            return false;
        }
        return description.getState() != ApplicationEnv.PackageState.Result;
    }

    public synchronized boolean isModemBusy() {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(BuildPropReader.getDeviceModemConfigVersionSha1());
        if (description == null) {
            return false;
        }
        Logger.debug("OtaApp", "Modem update is at state = " + description.getState());
        return description.getState() != ApplicationEnv.PackageState.Result;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void runStateMachine() {
        Configs configs;
        sendUpgradeStatus();
        List<String> versions = this._db.getVersions();
        Context globalContext = OtaApplication.getGlobalContext();
        if (versions == null) {
            Logger.debug("OtaApp", "CusSM.runStateMachine: no versions found in database");
            if (UpdaterUtils.isFeatureOn(this.settings.getString(Configs.STATS_FEATURE))) {
                this._sl.stopListener();
            }
            FileUtils.cleanupFiles();
            return;
        }
        for (String str : versions) {
            ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
            if (description == null) {
                Logger.error("OtaApp", "CusSM.runStateMachine: getVersions's version is " + str + ",  but could not get its description");
            } else {
                Logger.debug("OtaApp", "CusSM.runStateMachine: getVersions's version " + str + "; getDescription's version " + description.getVersion());
                Logger.debug("OtaApp", "CusSM.runStateMachine: updateVerion:" + str + " dbState:" + description.getState());
                if (isServiceControlEnabled(str)) {
                    if (BuildPropReader.getDeviceModemConfigVersionSha1().equals(str)) {
                        configs = Configs.MODEM_SERVICE_CONTROL_RESPONSE;
                    } else {
                        configs = Configs.SERVICE_CONTROL_RESPONSE;
                    }
                    Logger.debug("OtaApp", "CusSM.runStateMachine: server told to :" + this.settings.getString(configs));
                    if (description.getState() != ApplicationEnv.PackageState.Result) {
                        if ("wait".equalsIgnoreCase(this.settings.getString(configs))) {
                            return;
                        }
                        if ("cancel".equalsIgnoreCase(this.settings.getString(configs))) {
                            stopTimer();
                            cancelOTA(new String[0]);
                            return;
                        }
                    }
                }
                _installType = InstallTypeResolver.getInstallTypeHandler(this._env, this.settings, this._db, description, this);
                switch (AnonymousClass5.$SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[description.getState().ordinal()]) {
                    case 1:
                        Logger.info("OtaApp", "CusSM.runStateMachine: preparing to get or resume getting version " + str);
                        getUpgrade(description);
                        continue;
                    case 2:
                        InstallerUtilMethods.InstallerErrorStatus updaterEngineHandler = _installType.updaterEngineHandler(description, this.systemUpdaterPolicy);
                        if (updaterEngineHandler == InstallerUtilMethods.InstallerErrorStatus.STATUS_SYSTEM_UPDATE_CANCEL_POLICY_SET) {
                            Logger.info("OtaApp", "CusSM.runStateMachine: Updater engine status " + updaterEngineHandler.name());
                            cancelOTA("Device is under system disable ota update policy, abort the ongoing update", ErrorCodeMapper.KEY_SYSTEM_UPDATE_POLICY);
                            break;
                        } else {
                            if (updaterEngineHandler != InstallerUtilMethods.InstallerErrorStatus.STATUS_BUSY && updaterEngineHandler != InstallerUtilMethods.InstallerErrorStatus.STATUS_OK && updaterEngineHandler != InstallerUtilMethods.InstallerErrorStatus.STATUS_SYSTEM_UPDATE_POLICY_SET) {
                                if (updaterEngineHandler == InstallerUtilMethods.InstallerErrorStatus.STATUS_NO_FILE) {
                                    Logger.error("OtaApp", "CusSM.runStateMachine version " + str + " is in database as applying AB patch but not on disk... cleaning up");
                                    failDownload(str, UpgradeUtils.DownloadStatus.STATUS_FAIL, FileUtils.getLocalPath(this.settings) + " file not found in device", ErrorCodeMapper.KEY_PACKAGE_NOT_IN_DEVICE);
                                    break;
                                } else if (updaterEngineHandler == InstallerUtilMethods.InstallerErrorStatus.STATUS_LOW_SPACE) {
                                    Logger.info("OtaApp", "CusSM.runStateMachine: applying patch, space is low");
                                    try {
                                        this._env.getUtilities().sendUpdateDownloadStatusError(str, UpgradeUtils.DownloadStatus.STATUS_SPACE_BACKGROUND_INSTALL, MetaDataBuilder.toJSONString(description.getMeta()));
                                        this._upgradePlugins.getUpgradeSource(UpgradeSourceType.valueOf(description.getRepository())).setMemoryLowInfo(description);
                                        break;
                                    } catch (JSONException e) {
                                        resetStateForJSONException(str, e);
                                        break;
                                    }
                                } else if (updaterEngineHandler == InstallerUtilMethods.InstallerErrorStatus.STATUS_FOTA_LOW_MEMORY) {
                                    onIntentMaxFotaExpiryTime();
                                    break;
                                } else {
                                    break;
                                }
                            }
                            Logger.info("OtaApp", "CusSM.runStateMachine: Updater engine status " + updaterEngineHandler.name());
                            continue;
                        }
                        break;
                    case 3:
                        UpdaterUtils.setOtaSystemBindServiceEnabledState(globalContext, true);
                        if (UpgradeSourceType.fota.toString().equals(description.getRepository())) {
                            this._env.getUtilities().moveFotaToGettingDescriptorState();
                            break;
                        } else {
                            if (!CheckForUpgradeTriggeredBy.user.name().equals(this.settings.getString(Configs.TRIGGERED_BY)) && !CheckForUpgradeTriggeredBy.user.name().equalsIgnoreCase(description.getMeta().getUpdateReqTriggeredBy())) {
                                if (!this.systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet() && this.systemUpdaterPolicy.getPolicyType() <= 0) {
                                    if (!this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE) && !UpgradeSourceType.modem.toString().equals(description.getRepository()) && description.getMeta().showPreDownloadDialog() && description.getMeta().getForceDownloadTime() < 0.0d && !SmartUpdateUtils.shouldIForceSmartUpdate(this.settings, description.getMeta().getUpdateTypeData())) {
                                        try {
                                            registerForHealthCheck();
                                            this._env.getUtilities().sendSystemUpdateAvailableNotification(MetaDataBuilder.toJSONString(description.getMeta()));
                                            return;
                                        } catch (JSONException e2) {
                                            resetStateForJSONException(str, e2);
                                            break;
                                        }
                                    } else {
                                        if (UpdaterUtils.isBatteryLowToStartDownload(globalContext)) {
                                            this.settings.setBoolean(Configs.BATTERY_LOW, true);
                                        }
                                        if (UpdaterUtils.shouldIBlockUpdateByTOD(getMetaData())) {
                                            Logger.debug("OtaApp", "Update is blocked by TOD");
                                            break;
                                        } else {
                                            if (SmartUpdateUtils.shouldIForceSmartUpdate(this.settings, description.getMeta().getUpdateTypeData())) {
                                                UpdaterUtils.sendDownloadModeStats("autoDownloadForSmartUpdate");
                                            } else if (!description.getMeta().showPreDownloadDialog() || description.getMeta().getForceDownloadTime() > 0.0d) {
                                                UpdaterUtils.sendDownloadModeStats("autoDownloadAfterNotified");
                                            }
                                            this._db.setState(str, ApplicationEnv.PackageState.GettingDescriptor, null);
                                            break;
                                        }
                                    }
                                }
                                if (UpdaterUtils.isBatteryLowToStartDownload(globalContext)) {
                                    this.settings.setBoolean(Configs.BATTERY_LOW, true);
                                }
                                if (this.systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet()) {
                                    UpdaterUtils.setAutomaticDownloadForCellular(true);
                                }
                                UpdaterUtils.sendDownloadModeStats("autoDlBySysUpdatePolicy");
                                Logger.debug("OtaApp", "CusSM:Auto downloading update due to system update policy");
                                this._db.setState(str, ApplicationEnv.PackageState.GettingDescriptor, null);
                                break;
                            }
                            Logger.debug("OtaApp", "user triggered");
                            if (!this.systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet() && this.systemUpdaterPolicy.getPolicyType() <= 0) {
                                if (!this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE) && description.getMeta().showPreDownloadDialog() && description.getMeta().getForceDownloadTime() < 0.0d) {
                                    if (!this._db.setVersionState(str, ApplicationEnv.PackageState.RequestPermission, null)) {
                                        Logger.error("OtaApp", "CusSM.runStateMachine: database error; did not send a permission request to UI");
                                        break;
                                    } else {
                                        UpdaterUtils.setFullScreenStartPoint(Configs.STATS_DL_START_POINT, "userDidCheckUpdate");
                                        runStateMachine();
                                        break;
                                    }
                                } else {
                                    if (UpdaterUtils.isBatteryLowToStartDownload(globalContext)) {
                                        this.settings.setBoolean(Configs.BATTERY_LOW, true);
                                    }
                                    UpdaterUtils.sendDownloadModeStats("autoDownloadAfterNotified");
                                    this._db.setState(str, ApplicationEnv.PackageState.GettingDescriptor, null);
                                    _installType.displayScreenForGettingDescriptor(description);
                                    break;
                                }
                            }
                            if (UpdaterUtils.isBatteryLowToStartDownload(globalContext)) {
                                this.settings.setBoolean(Configs.BATTERY_LOW, true);
                            }
                            if (this.systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet()) {
                                UpdaterUtils.setAutomaticDownloadForCellular(true);
                            }
                            Logger.debug("OtaApp", "CusSM:Auto downloading update due to system update policy");
                            if (UpgradeSourceType.sdcard.toString().equals(description.getRepository())) {
                                UpgradeUtilMethods.sendUpdateNotificationResponse(globalContext, str, true, this.settings.getString(Configs.FLAVOUR), "userInitiated");
                            } else {
                                this._db.setState(str, ApplicationEnv.PackageState.GettingDescriptor, null);
                            }
                            _installType.displayScreenForGettingDescriptor(description);
                            UpdaterUtils.sendDownloadModeStats("autoDlBySysUpdatePolicy");
                            continue;
                        }
                        break;
                    case 4:
                        Logger.debug("OtaApp", "CusSM.runStateMachine: user is being notified for version " + str);
                        String downloadOptStartStopTime = this._upgradePlugins.getUpgradeSource(UpgradeSourceType.valueOf(description.getRepository())).getDownloadOptStartStopTime(description);
                        Logger.debug("OtaApp", "CusSM.runStateMachine: getDownloadOptStartStopTime text " + downloadOptStartStopTime);
                        if (this.systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet()) {
                            if (UpdaterUtils.isBatteryLowToStartDownload(globalContext)) {
                                this.settings.setBoolean(Configs.BATTERY_LOW, true);
                            }
                            UpdaterUtils.setAutomaticDownloadForCellular(true);
                            UpdaterUtils.sendDownloadModeStats("autoDlBySysUpdatePolicyAfterNotified");
                            Logger.error("OtaApp", "CusSM:Auto downloading update due to system update policy");
                            this._db.setState(str, ApplicationEnv.PackageState.GettingDescriptor, null);
                            break;
                        } else if (!this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE) && ((description.getMeta().showPreDownloadDialog() && description.getMeta().getForceDownloadTime() < 0.0d) || UpdaterUtils.isMaxUpdateFailCountExpired(description.getMeta().getMaxUpdateFailCount()))) {
                            try {
                                this._env.getUtilities().sendUpdateNotification(str, MetaDataBuilder.toJSONString(description.getMeta()), description.getRepository(), downloadOptStartStopTime);
                                registerForHealthCheck();
                                continue;
                            } catch (JSONException e3) {
                                resetStateForJSONException(str, e3);
                                break;
                            }
                        } else {
                            this._env.getUtilities().cancelUpdateNotification();
                            if (UpdaterUtils.isBatteryLowToStartDownload(globalContext)) {
                                this.settings.setBoolean(Configs.BATTERY_LOW, true);
                            }
                            UpdaterUtils.sendDownloadModeStats("autoDownloadAfterNotified");
                            this._db.setState(str, ApplicationEnv.PackageState.GettingDescriptor, null);
                            break;
                        }
                        break;
                    case 5:
                        if (BuildPropReader.isBotaATT() && !description.getMeta().isWifiOnly()) {
                            String string = this.settings.getString(Configs.USER_AUTO_DOWNLOAD_OPTION);
                            Logger.debug("OtaApp", "GettingDescriptor:USER_AUTO_DOWNLOAD_OPTION=" + string);
                            if (FotaConstants.AutoDownloadOption.WiFi.toString().equals(string)) {
                                this.settings.setString(Configs.FLAVOUR, UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI.name());
                            } else {
                                this.settings.setString(Configs.FLAVOUR, UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI_AND_MOBILE.name());
                            }
                        }
                        if (_installType.isDataSpaceLowForUpgrade(description, this._env)) {
                            if (description.getRepository().equals(UpgradeSourceType.upgrade.toString())) {
                                try {
                                    this._env.getUtilities().sendUpdateDownloadStatusError(str, UpgradeUtils.DownloadStatus.STATUS_SPACE, MetaDataBuilder.toJSONString(description.getMeta()));
                                    break;
                                } catch (JSONException e4) {
                                    resetStateForJSONException(str, e4);
                                    break;
                                }
                            } else if (BuildPropReader.isATT()) {
                                failDownload(description.getVersion(), UpgradeUtils.DownloadStatus.STATUS_SPACE, "CusSM.runStateMachine: GettingDescriptor failed due tolow memory space", ErrorCodeMapper.KEY_FAILED_FOTA);
                                break;
                            } else {
                                break;
                            }
                        } else {
                            this._upgradePlugins.getUpgradeSource(UpgradeSourceType.valueOf(description.getRepository())).checkForDownloadDescriptor(str);
                            unregisterFromHealthCheck();
                            continue;
                        }
                        break;
                    case 6:
                        long j = this.settings.getLong(Configs.MODEM_FILE_DL_EXPIRED_TIMESTAMP, -1L);
                        long currentTimeMillis = System.currentTimeMillis();
                        if (!BuildPropReader.getDeviceModemConfigVersionSha1().equals(str) && j > 0 && currentTimeMillis >= j) {
                            cancelModemUpdate("Modem Update is cancelled due to modem download timer expired, version: " + str, ErrorCodeMapper.KEY_MODEM_DOWNLOAD_TIMER_EXPIRED);
                            break;
                        } else if (shouldMoveToGettingPackage(versions)) {
                            this._db.setState(str, ApplicationEnv.PackageState.GettingPackage, this.settings.getString(Configs.FLAVOUR));
                            if (BuildPropReader.getDeviceModemConfigVersionSha1().equals(str)) {
                                this.settings.setLong(Configs.MODEM_FILE_DL_EXPIRED_TIMESTAMP, currentTimeMillis + InstallerUtilMethods.MAX_ALARM_TIME_FOR_DL_MODEM);
                            }
                            runStateMachine();
                            break;
                        } else {
                            continue;
                        }
                    case 7:
                        intimateModem();
                        moveOtaToModemWaitingState();
                        continue;
                    case 8:
                        Logger.debug("OtaApp", "Waiting for modem update status from Modem component");
                        if (System.currentTimeMillis() < this.settings.getLong(Configs.MODEM_FILE_DL_NOTIFIED_TIMESTAMP, 0L)) {
                            break;
                        } else if (this._upgradePlugins.getUpgradeSource(UpgradeSourceType.modem).isUpdateSuccessful(description.getMeta(), UpgradeSourceType.modem.name())) {
                            this._db.setState(str, ApplicationEnv.PackageState.Result, true, "Device is already upgraded to target version but result status is not received from modem, status waiting time period is expired", ErrorCodeMapper.KEY_SUCCESS);
                            cleanupModemVersion(str);
                            break;
                        } else {
                            cancelModemUpdate("Modem Update is cancelled due to expiry of modem update waiting time period , version: " + str, ErrorCodeMapper.KEY_MODEM_CANCELED_BY_SERVER);
                            continue;
                        }
                    case 9:
                        Logger.info("OtaApp", "CusSM.runStateMachine: preparing to get payload metadata package getting version " + str);
                        getPayloadMetaDataDownload(description);
                        continue;
                    case 10:
                        try {
                            Logger.info("OtaApp", "CusSM.runStateMachine: allocate space before applying patch for VAB updates.");
                            registerForHealthCheck();
                            _installType.allocateSpaceBeforeApplyPatch(description);
                            continue;
                        } catch (JSONException e5) {
                            resetStateForJSONException(str, e5);
                            break;
                        }
                    case 11:
                        CusUtilMethods.startRestartExpiryTimer(this.settings);
                        Logger.debug("OtaApp", "CusSM.runStateMachine: version " + str + " download complete, now querying the user");
                        if (_installType.doesDownloadedFileClearedFromDisk()) {
                            sendPluginUpdateStatus(description, UpgradeStatusConstents.Internal_Error_Aborting_The_Query);
                            cleanupVersion(str);
                            break;
                        } else {
                            try {
                                registerForHealthCheck();
                                if (_installType.shouldPromptUpgradeNotification(description)) {
                                    _installType.promptUpgradeNotification(description);
                                    break;
                                } else {
                                    this._db.setVersionState(str, ApplicationEnv.PackageState.QueryingInstall, null);
                                    runStateMachine();
                                    continue;
                                }
                            } catch (JSONException e6) {
                                _installType.checkAndResetInstallerFromCusSM(description.getState(), InstallerUtilMethods.InstallerErrorStatus.STATUS_SILENT_FAILURE);
                                resetStateForJSONException(str, e6);
                                break;
                            }
                        }
                    case 12:
                        if (_installType.doesDownloadedFileClearedFromDisk()) {
                            sendPluginUpdateStatus(description, UpgradeStatusConstents.Internal_Error_Aborting_The_Query);
                            cleanupVersion(str);
                            break;
                        } else {
                            try {
                                _installType.promptUpgradeActivity(description);
                                registerForHealthCheck();
                                continue;
                            } catch (JSONException e7) {
                                _installType.checkAndResetInstallerFromCusSM(description.getState(), InstallerUtilMethods.InstallerErrorStatus.STATUS_SILENT_FAILURE);
                                resetStateForJSONException(str, e7);
                                break;
                            }
                        }
                    case 13:
                        UpdaterUtils.setOtaSystemBindServiceEnabledState(globalContext, false);
                        if (this.systemUpdaterPolicy.isOtaUpdateDisabledByPolicyMngr()) {
                            String str2 = "Device is under system disable ota update policy, abort ongoing update";
                            String str3 = ErrorCodeMapper.KEY_UPDATE_DISABLED_BY_POLICY_MNGR;
                            if (this.systemUpdaterPolicy.isOtaUpdateDisabledPolicySet()) {
                                str2 = "isOtaUpdateDisabled policy is set, abort ongoing update";
                                str3 = ErrorCodeMapper.KEY_UPDATE_DISABLED_BY_MOTO_POLICY_MNGR;
                            } else if (this.systemUpdaterPolicy.isDeviceUnderFreezePeriod()) {
                                str2 = "Device is under freeze period; policy type =" + this.systemUpdaterPolicy.getPolicyType() + ", abort ongoing update";
                                str3 = ErrorCodeMapper.KEY_UPDATE_BLOCKED_FREEZE_PERIOD;
                            }
                            cancelOTA(str2, str3);
                            break;
                        } else {
                            int i = this.settings.getInt(Configs.UPGRADE_ATTEMPT_COUNT, 0);
                            int i2 = this.settings.getInt(Configs.MAX_UPGRADE_ATTEMPT_COUNT, 1);
                            if (i >= i2) {
                                Logger.error("OtaApp", "CusSM.runStateMachine version " + str + " upgrade attempt count beyond threshold " + i2);
                                return;
                            }
                            UpgradeUtilConstants.CheckSpaceEnum checkSpace = checkSpace(description, str);
                            if (checkSpace == UpgradeUtilConstants.CheckSpaceEnum.SPACE_AVAILABLE_AFTER_LOW_STORAGE) {
                                return;
                            }
                            if (checkSpace == UpgradeUtilConstants.CheckSpaceEnum.SPACE_NOT_AVAILABLE) {
                                Logger.info("OtaApp", "CusSM.runStateMachine: Upgrading, space is low");
                                long j2 = this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L);
                                if (BuildPropReader.isATT() && j2 > 0 && System.currentTimeMillis() >= j2) {
                                    onIntentMaxFotaExpiryTime();
                                } else {
                                    try {
                                        this._env.getUtilities().sendUpdateDownloadStatusError(str, UpgradeUtils.DownloadStatus.STATUS_SPACE_INSTALL, MetaDataBuilder.toJSONString(description.getMeta()));
                                    } catch (JSONException e8) {
                                        resetStateForJSONException(str, e8);
                                    }
                                }
                                return;
                            }
                            if (description.getMeta().getRebootRequired()) {
                                try {
                                    if (_installType.doesDownloadedFileClearedFromDisk()) {
                                        sendPluginUpdateStatus(description, UpgradeStatusConstents.Internal_Error_Aborting_The_Query);
                                        cleanupVersion(str);
                                        return;
                                    }
                                    Logger.debug("OtaApp", "CusSM.runStateMachine: preparing the device to upgrade to version " + str);
                                    this._env.getUtilities().sendUpgradeExecute(str, FileUtils.getLocalPath(this.settings), MetaDataBuilder.toJSONString(description.getMeta()));
                                } catch (JSONException e9) {
                                    resetStateForJSONException(str, e9);
                                    _installType.checkAndResetInstallerFromCusSM(description.getState(), InstallerUtilMethods.InstallerErrorStatus.STATUS_SILENT_FAILURE);
                                }
                            } else if (CusUtilMethods.isPolicyBundleUpdateEnabled(description)) {
                                doPolicyBundleUpdate(description, str);
                            } else {
                                this._db.setState(str, ApplicationEnv.PackageState.Result, false, "reboot required is set to false in deployment plan", ErrorCodeMapper.KEY_OTHER);
                                this.settings.incrementPrefs(Configs.UPDATE_FAIL_COUNT);
                                cleanupVersion(str);
                                _installType.checkAndResetInstallerFromCusSM(description.getState(), InstallerUtilMethods.InstallerErrorStatus.STATUS_SILENT_FAILURE);
                            }
                            unregisterFromHealthCheck();
                            continue;
                        }
                    case 14:
                        Logger.info("OtaApp", "CusSM.runStateMachine:MergePending: Updater engine status " + _installType.initializeUpdaterEngineHandlerMergeState().name());
                        continue;
                    case 15:
                        registerForHealthCheck();
                        UpgradeUtilMethods.sendStartMergeRestartActivityIntent(OtaApplication.getGlobalContext());
                        continue;
                    case 16:
                        Logger.info("OtaApp", "CusSM.runStateMachine: version " + str + " in Result state, cleaning up");
                        cleanupVersion(str);
                        continue;
                    default:
                        continue;
                }
            }
        }
        sendUpgradeStatus();
    }

    private boolean shouldMoveToGettingPackage(List<String> list) {
        for (String str : list) {
            ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
            if (description != null && description.getState() == ApplicationEnv.PackageState.GettingPackage) {
                return false;
            }
        }
        return true;
    }

    private void resetStateForJSONException(String str, JSONException jSONException) {
        this._db.setState(str, ApplicationEnv.PackageState.Result, false, "CusSM.runStateMachine, error while parsing metadata " + jSONException, ErrorCodeMapper.KEY_PARSE_ERROR);
        this.settings.incrementPrefs(Configs.UPDATE_FAIL_COUNT);
        cleanupVersion(str);
    }

    public synchronized void cancelOTA(String... strArr) {
        String str;
        String str2;
        String metaSourceSha1 = UpdaterUtils.getMetaSourceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(metaSourceSha1);
        if (description != null) {
            if (description.getState() != ApplicationEnv.PackageState.MergePending && description.getState() != ApplicationEnv.PackageState.MergeRestart) {
                Logger.debug("OtaApp", "CusSM.cancelOTA: getVersions's version " + metaSourceSha1 + "; getDescription's version" + description.getVersion());
                Logger.info("OtaApp", "CusSM.cancelOTA: found version " + metaSourceSha1 + " in database with state (" + description.getState() + ")");
                if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                    UpdaterUtils.launchNextSetupActivityVitalUpdate(OtaApplication.getGlobalContext());
                }
                switch (AnonymousClass5.$SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[description.getState().ordinal()]) {
                    case 1:
                    case 5:
                        this._env.getUtilities().cancelDownloadNotification();
                        break;
                    case 2:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                        _installType.checkAndResetInstallerFromCusSM(description.getState(), InstallerUtilMethods.InstallerErrorStatus.STATUS_SILENT_FAILURE);
                        break;
                    case 3:
                        this._env.getUtilities().sendCheckForUpdateResponse(UpgradeUtils.Error.ERR_NET, 0, false);
                        this._env.getUtilities().cancelUpdateNotification();
                        break;
                    case 4:
                        this._env.getUtilities().cancelUpdateNotification();
                        break;
                }
            }
            Logger.debug("OtaApp", "CusSM.cancelOTA: Ota is in state=" + description.getState() + " OTA update can not be canceled at MergePending or MergeRestart state");
            return;
        }
        Logger.debug("OtaApp", "CusSM.cancelOTA: getVersions's version is " + metaSourceSha1 + ",  but could not get its description");
        this.systemUpdaterPolicy.cancelAlarm(OtaApplication.getGlobalContext());
        if (BuildPropReader.getDeviceModemConfigVersionSha1().equals(metaSourceSha1)) {
            str = "Moving update version:" + metaSourceSha1 + " to Result state as Modem update cancelled by server!!";
            str2 = ErrorCodeMapper.KEY_MODEM_CANCELED_BY_SERVER;
        } else {
            str = "Moving update version:" + metaSourceSha1 + " to Result state as OTA cancelled by server!!";
            str2 = ErrorCodeMapper.KEY_OTA_CANCELED_BY_SERVER;
        }
        if (strArr != null && strArr.length >= 2) {
            if (strArr[1].equals(ErrorCodeMapper.KEY_SYSTEM_UPDATE_POLICY) && BuildPropReader.isATT()) {
                AndroidFotaInterface.sendOngoingFotaPolicySetCancelIntent(OtaApplication.getGlobalContext());
            }
            failDownloadInternalSilent(metaSourceSha1, strArr[0], strArr[1]);
        } else {
            failDownloadInternalSilent(metaSourceSha1, str, str2);
        }
    }

    public void cancelModemUpdate(String... strArr) {
        String deviceModemConfigVersionSha1 = BuildPropReader.getDeviceModemConfigVersionSha1();
        if (strArr != null && strArr.length >= 2) {
            failDownloadInternalSilent(deviceModemConfigVersionSha1, strArr[0], strArr[1]);
        } else {
            failDownloadInternalSilent(deviceModemConfigVersionSha1, "Moving update version:" + deviceModemConfigVersionSha1 + " to Result state as Modem update cancelled by server!!", ErrorCodeMapper.KEY_MODEM_CANCELED_BY_SERVER);
        }
    }

    private void doPolicyBundleUpdate(ApplicationEnv.Database.Descriptor descriptor, String str) {
        this._env.getUtilities().cancelDownloadNotification();
        this.settings.setInt(Configs.INCREMENTAL_VERSION, descriptor.getMeta().getIncrementalVersion());
        this._db.setState(str, ApplicationEnv.PackageState.Result, true, SystemUpdateStatusUtils.KEY_UPATE_SUCCESS, ErrorCodeMapper.KEY_SUCCESS);
        this.settings.setInt(Configs.UPDATE_FAIL_COUNT, 0);
        Logger.info("OtaApp", "CusSM.runStateMachine, preparing the device to upgrade policy bundle");
        FileUtils.doPolicyBundleUpdate(FileUtils.getLocalPath(this.settings));
        cleanupVersionSilent(str);
    }

    public synchronized void forceUpgrade() {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description != null) {
            Logger.debug("OtaApp", "CusSM.forceUpgrade: getVersions's version " + deviceSha1 + "; getDescription's version" + description.getVersion());
            Logger.debug("OtaApp", "CusSM.forceUpgrade: found version " + deviceSha1 + " in database with state (" + description.getState() + ")");
            int i = AnonymousClass5.$SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[description.getState().ordinal()];
            if (i != 1 && i != 2) {
                if (i != 3 && i != 4) {
                    if (i != 5) {
                        if (i != 13 && i != 16) {
                            switch (i) {
                                case 11:
                                    Logger.info("OtaApp", "CusSM.forceUpgrade, resend the intent used to show install screen");
                                    try {
                                        _installType.promptUpgradeActivity(description);
                                        break;
                                    } catch (JSONException e) {
                                        resetStateForJSONException(deviceSha1, e);
                                        _installType.checkAndResetInstallerFromCusSM(description.getState(), InstallerUtilMethods.InstallerErrorStatus.STATUS_SILENT_FAILURE);
                                        break;
                                    }
                            }
                        } else {
                            Logger.info("OtaApp", "CusSM.forceUpgrade, no state transistion required");
                        }
                    }
                } else {
                    this._env.getUtilities().cancelUpdateNotification();
                    Logger.info("OtaApp", "CusSM.forceUpgrade, state transition to GettingDescriptor");
                    UpdaterUtils.sendDownloadModeStats("forceUpgrade");
                    if (UpdaterUtils.isBatteryLowToStartDownload(OtaApplication.getGlobalContext())) {
                        this.settings.setBoolean(Configs.BATTERY_LOW, true);
                    }
                    this._db.setState(deviceSha1, ApplicationEnv.PackageState.GettingDescriptor, null);
                    sendUpgradeStatus();
                }
            }
            Logger.info("OtaApp", "CusSM.forceUpgrade, no state transistion required");
            this._env.getUtilities().cancelDownloadNotification();
        } else {
            Logger.debug("OtaApp", "CusSM.forceUpgrade: getVersions's version is " + deviceSha1 + ",  but could not get its description");
        }
    }

    public synchronized void forceDownload() {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description != null) {
            Logger.debug("OtaApp", "CusSM.forceDownload: getVersions's version " + deviceSha1 + "; getDescription's version" + description.getVersion());
            Logger.debug("OtaApp", "CusSM.forceDownload: found version " + deviceSha1 + " in database with state (" + description.getState() + ")");
            if (UpdaterUtils.getMaxForceDownloadDeferTime() < 0) {
                return;
            }
            int forceDownloadDelay = UpdaterUtils.getForceDownloadDelay(System.currentTimeMillis());
            if (forceDownloadDelay <= 0) {
                UpdaterUtils.sendDownloadModeStats("autoCellularDownload");
                this.settings.setBoolean(Configs.AUTOMATIC_DOWNLOAD_FOR_CELLULAR, true);
                this.settings.removeConfig(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME);
            } else {
                this._env.getUtilities().registerWithForceUpgradeManager(forceDownloadDelay);
            }
            runStateMachine();
        } else {
            Logger.debug("OtaApp", "CusSM.forceDownload: getVersions's version is " + deviceSha1 + ",  but could not get its description");
        }
    }

    private boolean isServiceControlEnabled() {
        return isServiceControlEnabled(this._env.getServices().getDeviceSha1());
    }

    private boolean isServiceControlEnabled(String str) {
        MetaData metaData;
        if (!UpdaterUtils.isFeatureOn(this.settings.getString(Configs.OTACANCEL_FEATURE))) {
            Logger.debug("OtaApp", "ota cancel feature not avaialble");
            return false;
        }
        if (TextUtils.isEmpty(str)) {
            metaData = getMetaData();
        } else {
            metaData = getMetaData(str);
        }
        return metaData != null && metaData.isServiceControlEnabled();
    }

    private int startTimer(String str) {
        MetaData metaData = getMetaData(str);
        if (metaData == null) {
            Logger.debug("OtaApp", "startTimer, not setting the timer as description is null");
            return 0;
        }
        synchronized (this) {
            try {
                try {
                    if (_mTimer == null) {
                        defaultAction defaultaction = new defaultAction(metaData);
                        Timer timer = new Timer();
                        _mTimer = timer;
                        timer.schedule(defaultaction, metaData.getServiceTimeoutSeconds() * 1000);
                    } else {
                        Logger.debug("OtaApp", "defaultAction have been scheduled, do nothing");
                    }
                } catch (IllegalStateException e) {
                    Logger.error("OtaApp", "startTimer, IllegalStateException, Maybe canceled. Ignore it" + e);
                }
            } catch (IllegalArgumentException e2) {
                Logger.error("OtaApp", "startTimer, IllegalArgumentException, ignore it." + e2);
            }
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopTimer() {
        synchronized (this) {
            if (_mTimer != null) {
                Logger.debug("OtaApp", "stopTimer, cancel()");
                _mTimer.cancel();
                _mTimer = null;
            } else {
                Logger.debug("OtaApp", "stopTimer, have stoped, do nothing");
            }
        }
    }

    private void scheduleCreateReserveSpace() {
        Context globalContext = OtaApplication.getGlobalContext();
        PendingIntent broadcast = PendingIntent.getBroadcast(globalContext, 1, new Intent(UpgradeUtilConstants.CREATE_RESERVE_SPACE_POST_FIFTEEN_MINUTES), 335544320);
        AlarmManager alarmManager = (AlarmManager) globalContext.getSystemService("alarm");
        alarmManager.cancel(broadcast);
        long currentTimeMillis = System.currentTimeMillis() + InstallerUtilMethods.MAX_ALARM_TIME_FOR_UE;
        alarmManager.setExactAndAllowWhileIdle(0, currentTimeMillis, broadcast);
        Logger.debug("OtaApp", "scheduleCreateReserveSpace, Current time: " + DateFormatUtils.getCalendarString(globalContext, System.currentTimeMillis()));
        Logger.debug("OtaApp", "Creating reserve space files will start at " + DateFormatUtils.getCalendarString(globalContext, currentTimeMillis));
    }

    private void checkAndCreateReserveSpace() {
        if (this._db.getDescription(this._env.getServices().getDeviceSha1()) != null) {
            return;
        }
        long j = this.settings.getLong(Configs.RESERVE_SPACE_IN_MB, -1L);
        if (j < 0) {
            j = FileUtils.getReservedSpaceValue();
            this.settings.setLong(Configs.RESERVE_SPACE_IN_MB, j);
        }
        Logger.debug("OtaApp", "Setting value spaceValue: " + j);
        scheduleCreateReserveSpace();
    }

    public void createReserveSpace() {
        OtaApplication.getExecutorService().submit(new Runnable() { // from class: com.motorola.ccc.ota.CusSM.4
            @Override // java.lang.Runnable
            public void run() {
                FileUtils.checkAndCreateReserveSpace(CusSM.this.settings.getLong(Configs.RESERVE_SPACE_IN_MB, -1L));
            }
        });
    }

    private void checkAndSendRebootDuringDLNotification() {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(this._env.getServices().getDeviceSha1());
        if (description == null) {
            return;
        }
        if (description.getState() == ApplicationEnv.PackageState.GettingPackage || description.getState() == ApplicationEnv.PackageState.GettingDescriptor) {
            Logger.debug("OtaApp", "CusSM.checkAndSendRebootDuringDLNotification: device rebooted during download communicate to stats module");
            this._env.getUtilities().sendRebootDuringDownloadIntent();
            UpdaterUtils.checkAndEnableBatteryStatusReceiver();
            if (UpdaterUtils.getBatteryLevel(OtaApplication.getGlobalContext()) >= this.settings.getInt(Configs.DEFAULT_MIN_BATTERY_LEVEL, 20)) {
                this.settings.setBoolean(Configs.BATTERY_LOW, false);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class defaultAction extends TimerTask {
        MetaData md;

        public defaultAction(MetaData metaData) {
            this.md = metaData;
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            Logger.debug("OtaApp", "No Service response, taking  defaultAction");
            CusSM.this.stopTimer();
            if (this.md.isContinueOnServiceError()) {
                CusSM.this.settings.setString(Configs.SERVICE_CONTROL_RESPONSE, "continue");
                CusSM.this.runStateMachine();
                return;
            }
            CusSM.this.cancelOTA(new String[0]);
        }
    }

    public void overWriteMetaData(MetaData metaData) {
        overWriteMetaData(metaData, false);
    }

    public void overWriteMetaData(MetaData metaData, boolean z) {
        Logger.debug("OtaApp", "CusSm.overWriteMetaData");
        try {
            String deviceSha1 = this._env.getServices().getDeviceSha1();
            if (z) {
                deviceSha1 = BuildPropReader.getDeviceModemConfigVersionSha1();
            }
            this._db.update_column_vt("Metadata", MetaDataBuilder.toJSONString(metaData), deviceSha1);
            MetadataOverrider.saveMetadata(metaData, this.settings, z);
        } catch (JSONException e) {
            Logger.error("OtaApp", "Error while converting metadata to json string " + e);
        }
    }

    public void onDeviceShutdown() {
        DownloadHandler downloadHandler = _downloader;
        if (downloadHandler != null) {
            downloadHandler.onDeviceShutdown();
        }
        if (BuildPropReader.isUEUpdateEnabled()) {
            onIntentHandleRebootDuringABUpdate();
        }
        this.settings.setBoolean(Configs.DEVICE_REBOOTED, true);
        UpdaterUtils.updateMotorolaSettingsProvider(OtaApplication.getGlobalContext(), UpdaterUtils.OTA_UPDATE_COMPLETED, String.valueOf(false));
    }

    private void checkAndSendRebootDuringABApplyingUpgradingState() {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            return;
        }
        if (description.getState() == ApplicationEnv.PackageState.ABApplyingPatch || description.getState() == ApplicationEnv.PackageState.Upgrading) {
            Logger.debug("OtaApp", "CusSM.checkAndSendRebootDuringABApplyingUpgradingState : OTA restarted during " + description.getState().toString());
            long j = this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L);
            if (j > 0 && System.currentTimeMillis() >= j) {
                if (description.getState() == ApplicationEnv.PackageState.ABApplyingPatch) {
                    if (InstallerUtilMethods.isMemoryLowForBackgroundInstallation(this._env, this.settings, description)) {
                        Logger.debug("OtaApp", "CusSM.checkAndSendRebootDuringABAplyingState:OTA is stuck in the ABApplying state due to low memory canceling the update");
                        failDownload(deviceSha1, UpgradeUtils.DownloadStatus.STATUS_SPACE_BACKGROUND_INSTALL, "Exceeded max update defer time", ErrorCodeMapper.KEY_FAILED_FOTA);
                        return;
                    }
                    return;
                } else if (!BuildPropReader.isUEUpdateEnabled() && description.getState() == ApplicationEnv.PackageState.Upgrading) {
                    Logger.debug("OtaApp", "CusSM.checkAndSendRebootDuringUpgradingState: OTA restarted during Upgrading state");
                    if (checkSpace(description, deviceSha1) == UpgradeUtilConstants.CheckSpaceEnum.SPACE_NOT_AVAILABLE) {
                        Logger.debug("OtaApp", "CusSM.checkAndSendRebootDuringUpgradingState:OTA is stuck in the Upgrading state due to low memory canceling the update");
                        sendPluginUpdateStatus(description, UpgradeStatusConstents.getUpgradeStatusConstants(UpgradeUtils.DownloadStatus.STATUS_SPACE_INSTALL));
                        failDownloadInternalSilent(deviceSha1, "Exceeded max update defer time", ErrorCodeMapper.KEY_FAILED_FOTA);
                        return;
                    }
                    return;
                }
            }
            CusUtilMethods.settingMaxDeferTimeForFOTAUpgrade(description, 86400000L, this.settings);
        }
        if (description.getState() == ApplicationEnv.PackageState.VerifyPayloadMetadata || description.getState() == ApplicationEnv.PackageState.VerifyAllocateSpace || description.getState() == ApplicationEnv.PackageState.ABApplyingPatch || description.getState() == ApplicationEnv.PackageState.Upgrading) {
            UpdaterUtils.checkAndEnableBatteryStatusReceiver();
            if (UpdaterUtils.getBatteryLevel(OtaApplication.getGlobalContext()) >= this.settings.getInt(Configs.DEFAULT_MIN_BATTERY_LEVEL, 20)) {
                this.settings.setBoolean(Configs.BATTERY_LOW, false);
            }
        }
    }

    private void onIntentMaxFotaExpiryTime() {
        String deviceSha1 = this._env.getServices().getDeviceSha1();
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(deviceSha1);
        if (description == null) {
            Logger.error("OtaApp", "CusSM.onIntentMaxFotaExpiryTime: notification for version is not in db " + deviceSha1);
            return;
        }
        Logger.debug("OtaApp", "CusSM.onIntentMaxFotaExpiryTime: state = " + description.getState().toString());
        if (description.getState() == ApplicationEnv.PackageState.ABApplyingPatch) {
            Logger.debug("OtaApp", "CusSM.onIntentMaxFotaExpiryTime:OTA is stuck in the ABApplying state due to low memory, canceling the update");
            failDownload(deviceSha1, UpgradeUtils.DownloadStatus.STATUS_SPACE_BACKGROUND_INSTALL, "Exceeded max update defer time", ErrorCodeMapper.KEY_DATA_OUT_OF_SPACE);
        } else if (!BuildPropReader.isUEUpdateEnabled() && description.getState() == ApplicationEnv.PackageState.Upgrading) {
            Logger.debug("OtaApp", "CusSM.onIntentMaxFotaExpiryTime:OTA is stuck in the Upgrading state due to low memory, canceling the update");
            failDownloadInternalSilent(deviceSha1, "Exceeded max update defer time", ErrorCodeMapper.KEY_DATA_OUT_OF_SPACE);
            sendPluginUpdateStatus(description, UpgradeStatusConstents.getUpgradeStatusConstants(UpgradeUtils.DownloadStatus.STATUS_SPACE_INSTALL));
        } else {
            Logger.verbose("OtaApp", "CusSM.onIntentMaxFotaExpiryTime failed: notification for version" + deviceSha1 + " that is in state " + description.getState().toString() + "(expected state ABApplyingState or Upgrading)");
        }
    }
}
