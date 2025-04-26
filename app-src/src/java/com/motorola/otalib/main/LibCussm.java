package com.motorola.otalib.main;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.RemoteException;
import android.text.TextUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.otalib.aidl.IOtaLibServiceCallBack;
import com.motorola.otalib.cdsservice.ResponseHandler;
import com.motorola.otalib.cdsservice.UrlConstructor.StateUrlConstructor;
import com.motorola.otalib.cdsservice.WebService;
import com.motorola.otalib.cdsservice.WebServiceRetryHandler;
import com.motorola.otalib.cdsservice.requestdataobjects.StateRequest;
import com.motorola.otalib.cdsservice.requestdataobjects.UrlRequest;
import com.motorola.otalib.cdsservice.requestdataobjects.builders.StateRequestBuilder;
import com.motorola.otalib.cdsservice.responsedataobjects.StateResponse;
import com.motorola.otalib.cdsservice.responsedataobjects.builders.StateResponseBuilder;
import com.motorola.otalib.cdsservice.utils.CDSUtils;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequest;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequestPayload;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequestPayloadType;
import com.motorola.otalib.cdsservice.webdataobjects.WebResponse;
import com.motorola.otalib.common.Environment.AndroidDB;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import com.motorola.otalib.main.Downloader.ConfigDownloader;
import com.motorola.otalib.main.Downloader.LibDownloadHandler;
import com.motorola.otalib.main.PublicUtilityMethods;
import com.motorola.otalib.main.Settings.LibConfigs;
import com.motorola.otalib.main.Settings.LibSettings;
import com.motorola.otalib.main.checkUpdate.CheckRequestObj;
import com.motorola.otalib.main.checkUpdate.CheckUpdateHandler;
import com.motorola.otalib.main.checkUpdate.UpdateSessionInfo;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class LibCussm {
    private static final String DB_LOC = "ota.db";
    private static int _isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
    private static Timer _mTimer;
    private ApplicationEnv.Database _db;
    private DownloadHandler _downloader;
    IOtaLibServiceCallBack mClientCallBack;
    private ConnectivityManager mConnMgr;
    private LibSettings mSettings;
    private ConnectivityManager.NetworkCallback networkCallback;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static class STATE_EVENT_STATUS {
        public static int IDLE = 0;
        public static int SENDING = 1;
        public static int WAITING_FOR_NETWORK = 2;
    }

    public LibCussm(Context context, LibSettings libSettings) {
        this.mSettings = libSettings;
        this._db = new AndroidDB(DB_LOC, context);
    }

    public synchronized void onStart(Context context, IOtaLibServiceCallBack iOtaLibServiceCallBack) {
        this.mClientCallBack = iOtaLibServiceCallBack;
        cleanUpOldEntry();
        registerConnectivityNetwork(context);
        PublicUtilityMethods.createOtaDirectory(context);
        runStateMachine(context);
    }

    private void cleanUpOldEntry() {
        if (TextUtils.isEmpty(this.mSettings.getConfig("otalib.CHECK_RERUEST_STRING", ""))) {
            Logger.debug(Logger.OTALib_TAG, "Old DB entry clean up is not required its a fresh update");
            return;
        }
        this.mSettings.removeConfig("otalib.CHECK_RERUEST_STRING");
        List<String> versions = this._db.getVersions();
        if (versions == null) {
            Logger.debug(Logger.OTALib_TAG, "CusSM.cleanUpOldEntry: no versions found in database");
            return;
        }
        for (String str : versions) {
            this._db.remove(str);
            Logger.debug(Logger.OTALib_TAG, "CusSM.cleanUpOldEntry: deleting version " + str);
        }
    }

    public synchronized void onDestroy() {
        DownloadHandler downloadHandler = this._downloader;
        if (downloadHandler != null) {
            downloadHandler.close();
            this._downloader = null;
        }
        try {
            this.mConnMgr.unregisterNetworkCallback(this.networkCallback);
        } catch (Exception unused) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelOTA(Context context, String str, String str2) {
        ApplicationEnv.Database database = this._db;
        if (database != null) {
            if (database.getVersions() == null) {
                Logger.debug(Logger.OTALib_TAG, "CusSM.runStateMachine: no update found in database");
                return;
            }
            ApplicationEnv.Database.Descriptor description = this._db.getDescription(str2);
            if (description == null) {
                Logger.error(Logger.OTALib_TAG, "no update found for this sha1 in db,drop the request to floor");
                return;
            }
            if (description.getState() == ApplicationEnv.PackageState.Result) {
                Logger.error(Logger.OTALib_TAG, "Package is already in result state " + str);
            } else {
                this._db.setState(str2, ApplicationEnv.PackageState.Result, false, str, ErrorCodeMapper.KEY_OTA_CANCELED_BY_SERVER, UpdateSessionInfo.getSessionDetails(this.mSettings, str2).toString());
            }
            runStateMachine(context);
        }
    }

    public void pleaseRunStateMachine(Context context) {
        runStateMachine(context);
    }

    private void registerConnectivityNetwork(final Context context) {
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        this.networkCallback = new ConnectivityManager.NetworkCallback() { // from class: com.motorola.otalib.main.LibCussm.1
            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onAvailable(Network network) {
                Logger.debug(Logger.OTALib_TAG, "Network is available");
                if (LibCussm._isSendingUpgradeStatus == STATE_EVENT_STATUS.WAITING_FOR_NETWORK) {
                    int unused = LibCussm._isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
                }
                LibCussm.this.runStateMachine(context);
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                super.onLost(network);
                Logger.debug(Logger.OTALib_TAG, "OtaService.NetworkCallback:onLost no data connection");
                if (LibCussm.this._downloader != null) {
                    LibCussm.this._downloader.radioGotDown();
                }
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void runStateMachine(Context context) {
        IOtaLibServiceCallBack iOtaLibServiceCallBack;
        sendUpgradeStatus(this.mSettings, context, this._db);
        List<String> versions = this._db.getVersions();
        if (versions == null) {
            Logger.debug(Logger.OTALib_TAG, "CusSM.runStateMachine: no versions found in database");
            return;
        }
        for (String str : versions) {
            ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
            UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(this.mSettings, str);
            if (description == null) {
                Logger.error(Logger.OTALib_TAG, "CusSM.runStateMachine: getVersions's primary key is " + str + ",  but could not get its description");
            } else {
                if (isServiceControlEnabled(str)) {
                    Logger.debug(Logger.OTALib_TAG, "CusSM.runStateMachine: server told to :" + sessionDetails.getServiceControlResponse());
                    if (description.getState() != ApplicationEnv.PackageState.Result) {
                        if ("wait".equalsIgnoreCase(sessionDetails.getServiceControlResponse())) {
                            return;
                        }
                        if ("cancel".equalsIgnoreCase(sessionDetails.getServiceControlResponse())) {
                            stopTimer();
                            cancelOTA(context, "Server cancelled", str);
                            return;
                        }
                    }
                }
                MetaData meta = description.getMeta();
                boolean z = !"RS_FAIL".equals(description.getStatus());
                InstallStatusInfo installStatusInfo = new InstallStatusInfo(context, str, this.mSettings, description, sessionDetails.getStatusCode());
                CheckRequestObj checkRequestObj = sessionDetails.getCheckRequestObj();
                try {
                    iOtaLibServiceCallBack = this.mClientCallBack;
                } catch (RemoteException unused) {
                }
                if (iOtaLibServiceCallBack != null && checkRequestObj != null) {
                    iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), z, installStatusInfo.toString());
                    Logger.debug(Logger.OTALib_TAG, "Version " + str + " state " + description.getState() + " session info " + sessionDetails.toString());
                    int i = AnonymousClass3.$SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[description.getState().ordinal()];
                    if (i == 1) {
                        this.mConnMgr.registerDefaultNetworkCallback(this.networkCallback);
                        if (checkRequestObj.isForceDownload()) {
                            this._db.setVersionState(str, ApplicationEnv.PackageState.QueueForDownload, null);
                        } else {
                            this._db.setState(str, ApplicationEnv.PackageState.RequestPermission, null);
                        }
                        runStateMachine(context);
                    } else if (i == 9) {
                        cleanUpFiles(context, str, z, meta.getDisplayVersion());
                    } else if (i == 3) {
                        File file = new File(PublicUtilityMethods.getFileName(context, checkRequestObj.getInternalName(), Long.parseLong(description.getMeta().getDisplayVersion())));
                        Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.transferUpgrade: Download file " + file.getName() + " exists " + file.exists());
                        if (file.exists()) {
                            if (file.length() == description.getMeta().getSize()) {
                                Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.transferUpgrade: no download, but file " + file.getAbsolutePath() + " is already here");
                                if (checkRequestObj != null && checkRequestObj.isForceInstall()) {
                                    this._db.setState(str, ApplicationEnv.PackageState.ABApplyingPatch, true, "", "", sessionDetails.toString());
                                } else {
                                    this._db.setState(str, ApplicationEnv.PackageState.Querying, true, "", "", sessionDetails.toString());
                                }
                                runStateMachine(context);
                                return;
                            }
                            file.delete();
                            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.transferUpgrade: deleting older file");
                        }
                        if (shouldMoveToDownloading(versions)) {
                            this._db.setState(str, ApplicationEnv.PackageState.GettingDescriptor, null);
                            runStateMachine(context);
                        }
                    } else if (i == 4) {
                        try {
                            new CheckUpdateHandler().checkForDownloadDescriptor(this, this._db, context, this.mClientCallBack, this.mSettings, checkRequestObj, description.getInfo());
                        } catch (RemoteException unused2) {
                        }
                    } else if (i == 5) {
                        getUpgrade(context, checkRequestObj, meta, description);
                    }
                } else {
                    this._db.remove(str);
                    return;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.otalib.main.LibCussm$3  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState;

        static {
            int[] iArr = new int[ApplicationEnv.PackageState.values().length];
            $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState = iArr;
            try {
                iArr[ApplicationEnv.PackageState.Notified.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.RequestPermission.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.QueueForDownload.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.GettingDescriptor.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.GettingPackage.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.Querying.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.ABApplyingPatch.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.Upgrading.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$Environment$ApplicationEnv$PackageState[ApplicationEnv.PackageState.Result.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
        }
    }

    private boolean shouldMoveToDownloading(List<String> list) {
        Logger.debug(Logger.OTALib_TAG, list.toString());
        for (String str : list) {
            ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
            if (description != null && (description.getState() == ApplicationEnv.PackageState.GettingDescriptor || description.getState() == ApplicationEnv.PackageState.GettingPackage)) {
                return false;
            }
        }
        return true;
    }

    public synchronized void onActionGetDescriptor(Context context, String str, boolean z, String str2) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            Logger.error(Logger.OTALib_TAG, "CusSM.onActionGetDescriptor: notification for primary key is not in db " + str);
        } else if (description.getState() != ApplicationEnv.PackageState.GettingPackage) {
            Logger.error(Logger.OTALib_TAG, String.format("CusSM.onActionGetDescriptor failed: notification for primary key %s that is in state %s (expected state GettingPackage)", str, description.getState().toString()));
        } else {
            if (this._downloader != null) {
                Logger.debug(Logger.OTALib_TAG, "CusSM.onActionGetDescriptor: reset download status flag");
                this._downloader.close();
                this._downloader = null;
            }
            Logger.info(Logger.OTALib_TAG, str2);
            this._db.setState(str, ApplicationEnv.PackageState.GettingDescriptor, str2);
            runStateMachine(context);
        }
    }

    public synchronized boolean isBusy(String str) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            return false;
        }
        return description.getState() != ApplicationEnv.PackageState.Result;
    }

    public synchronized long getPreviousTargetVersion(String str) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null || description.getMeta() == null) {
            return -1L;
        }
        return Long.parseLong(description.getMeta().getDisplayVersion());
    }

    public synchronized long getPreviousSourceVersion(String str) {
        return UpdateSessionInfo.getSessionDetails(this.mSettings, str).getCheckRequestObj().getSourceVersion();
    }

    private synchronized void getUpgrade(Context context, CheckRequestObj checkRequestObj, MetaData metaData, ApplicationEnv.Database.Descriptor descriptor) {
        DownloadHandler downloadHandler = this._downloader;
        if (downloadHandler != null) {
            if (downloadHandler.isBusy()) {
                Logger.debug(Logger.OTALib_TAG, "CusSM.getUpgrade: copying or downloading is in progress");
                return;
            } else {
                this._downloader.close();
                this._downloader = null;
            }
        }
        LibDownloadHandler libDownloadHandler = new LibDownloadHandler(context, this, this.mSettings, checkRequestObj.getInternalName(), this.mClientCallBack, checkRequestObj.getContextKey(), Long.parseLong(metaData.getDisplayVersion()), checkRequestObj.getAccsSerialNumber(), checkRequestObj.getPrimaryKey(), descriptor);
        this._downloader = libDownloadHandler;
        libDownloadHandler.transferUpgrade(descriptor);
    }

    public void downloadConfigFiles(Context context, IOtaLibServiceCallBack iOtaLibServiceCallBack, String str) {
        DownloadHandler downloadHandler = this._downloader;
        if (downloadHandler != null) {
            if (downloadHandler.isBusy()) {
                Logger.debug(Logger.OTALib_TAG, "CusSM.getUpgrade: copying or downloading is in progress");
                return;
            } else {
                this._downloader.close();
                this._downloader = null;
            }
        }
        ConfigDownloader configDownloader = new ConfigDownloader(context, this.mSettings, iOtaLibServiceCallBack, str);
        this._downloader = configDownloader;
        configDownloader.transferUpgrade(null);
    }

    private boolean isServiceControlEnabled(String str) {
        MetaData metaData = getMetaData(str);
        return metaData != null && metaData.isServiceControlEnabled();
    }

    public synchronized MetaData getMetaData(String str) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            Logger.debug(Logger.OTALib_TAG, "Description is null");
            return null;
        }
        return description.getMeta();
    }

    public synchronized void onInternalNotification(Context context, String str) {
        Logger.debug(Logger.OTALib_TAG, "CusSM.onInternalNotification: from lib");
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        DownloadHandler downloadHandler = this._downloader;
        if (downloadHandler != null) {
            downloadHandler.close();
            this._downloader = null;
        }
        if (description == null) {
            Logger.debug(Logger.OTALib_TAG, "CusSM.onInternalNotification: primaryKey is not in db");
        } else if (description.getState() != ApplicationEnv.PackageState.GettingPackage) {
            Logger.error(Logger.OTALib_TAG, "CusSM.onInternalNotification failed: notification for primaryKey " + str + " that is in state " + description.getState().toString() + " (expected state GettingPackage)");
        } else {
            UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(this.mSettings, description.getVersion());
            CheckRequestObj checkRequestObj = sessionDetails.getCheckRequestObj();
            if (checkRequestObj != null && checkRequestObj.isForceInstall()) {
                this._db.setState(str, ApplicationEnv.PackageState.ABApplyingPatch, true, "", "", sessionDetails.toString());
            } else {
                this._db.setState(str, ApplicationEnv.PackageState.Querying, true, "", "", sessionDetails.toString());
            }
            runStateMachine(context);
        }
    }

    public void OnUpgradeStatus(Context context, String str, boolean z, String str2) {
        Logger.debug(Logger.OTALib_TAG, "CusSM.OnUpgradeStatus: from lib");
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            Logger.debug(Logger.OTALib_TAG, "CusSM.onInternalNotification: primary key is not in db");
            return;
        }
        InstallStatusInfo fromJsonString = InstallStatusInfo.fromJsonString(str2);
        if (fromJsonString == null) {
            this._db.setState(str, ApplicationEnv.PackageState.Result, z, "APK sent empty install status info", ErrorCodeMapper.KEY_EMPTY_STATUS_INFO, UpdateSessionInfo.getSessionDetails(this.mSettings, str).toString());
            runStateMachine(context);
            return;
        }
        Logger.error(Logger.OTALib_TAG, "CusSM.OnUpgradeStatus: Notified version " + str + " that is in state " + description.getState().toString() + " to " + fromJsonString.getServerState());
        String statusMessage = fromJsonString.getStatusMessage();
        PublicUtilityMethods.STATUS_CODE reportingError = fromJsonString.getReportingError() == null ? PublicUtilityMethods.STATUS_CODE.ERROR_OTHER : fromJsonString.getReportingError();
        UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(this.mSettings, description.getVersion());
        if (description.getState() == ApplicationEnv.PackageState.RequestPermission && fromJsonString.getServerState() == ApplicationEnv.PackageState.QueueForDownload) {
            this._db.setVersionState(str, fromJsonString.getServerState(), "");
            runStateMachine(context);
        } else if (description.getState() == ApplicationEnv.PackageState.Querying && fromJsonString.getServerState() == ApplicationEnv.PackageState.ABApplyingPatch) {
            this._db.setState(str, fromJsonString.getServerState(), z, "", "", sessionDetails.toString());
            runStateMachine(context);
        } else if (description.getState() == ApplicationEnv.PackageState.ABApplyingPatch && fromJsonString.getServerState() == ApplicationEnv.PackageState.Upgrading) {
            this._db.setState(str, fromJsonString.getServerState(), z, "", "", sessionDetails.toString());
            runStateMachine(context);
        } else if (fromJsonString.getServerState() == ApplicationEnv.PackageState.Result) {
            CheckRequestObj checkRequestObj = sessionDetails.getCheckRequestObj();
            if (checkRequestObj != null) {
                Logger.debug(Logger.OTALib_TAG, "CusSM.OnUpgradeStatus: " + checkRequestObj.toString());
                checkRequestObj.setSourceVersion(fromJsonString.getSourceVersion());
                sessionDetails.setKeepPackage(fromJsonString.isKeepPackage());
                sessionDetails.setStatusCode(fromJsonString.getStatusCode());
                sessionDetails.setCheckRequestObj(checkRequestObj.toString(), this.mSettings);
                if (z) {
                    reportingError = PublicUtilityMethods.STATUS_CODE.SUCCESS;
                }
                this._db.setState(str, fromJsonString.getServerState(), z, statusMessage, reportingError.name(), sessionDetails.toString());
            }
            runStateMachine(context);
        }
    }

    public synchronized void failProgress(Context context, String str, UpgradeUtils.DownloadStatus downloadStatus, String str2, String str3) {
        DownloadHandler downloadHandler = this._downloader;
        if (downloadHandler != null) {
            downloadHandler.close();
            this._downloader = null;
        }
        if (this._db.getDescription(str) == null) {
            Logger.error(Logger.OTALib_TAG, "no primary Key found for this primary Key in db,drop the request to floor");
            return;
        }
        this.mSettings.removeConfig(LibConfigs.OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS);
        this.mSettings.removeConfig(LibConfigs.OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
        this.mSettings.removeConfig(LibConfigs.DOWNLOAD_DESCRIPTOR);
        this.mSettings.removeConfig(LibConfigs.DOWNLOAD_DESCRIPTOR_TIME);
        this._db.setState(str, ApplicationEnv.PackageState.Result, false, str2, str3, UpdateSessionInfo.getSessionDetails(this.mSettings, str).toString());
        runStateMachine(context);
    }

    public synchronized ApplicationEnv.PackageState getCurrentState(String str) {
        ApplicationEnv.Database.Descriptor description = this._db.getDescription(str);
        if (description == null) {
            return ApplicationEnv.PackageState.IDLE;
        }
        return description.getState();
    }

    private void cleanUpFiles(Context context, String str, boolean z, String str2) {
        this._db.remove(str);
        UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(this.mSettings, str);
        CheckRequestObj checkRequestObj = sessionDetails.getCheckRequestObj();
        if (checkRequestObj != null) {
            String fileName = PublicUtilityMethods.getFileName(context, checkRequestObj.getInternalName(), Long.parseLong(str2));
            if (!z && !sessionDetails.isKeepPackage()) {
                PublicUtilityMethods.cleanUpCurrentPackage(context, fileName);
            } else if (this._db.getVersions() == null) {
                PublicUtilityMethods.cleanUpOlderPackage(context, fileName, checkRequestObj.getInternalName());
            }
        }
        UpdateSessionInfo.deleteSessionDetails(this.mSettings, str);
    }

    public void checkForUpdate(CheckRequestObj checkRequestObj, Context context) {
        try {
            new CheckUpdateHandler().checkForUpdate(checkRequestObj, this._db, this, this.mClientCallBack, context, true, this.mSettings);
        } catch (RemoteException e) {
            UpdateSessionInfo.deleteSessionDetails(this.mSettings, checkRequestObj.getPrimaryKey());
            throw new RuntimeException(e);
        }
    }

    public synchronized void sendUpgradeStatus(final LibSettings libSettings, final Context context, final ApplicationEnv.Database database) {
        String str;
        if (_isSendingUpgradeStatus != STATE_EVENT_STATUS.IDLE) {
            Logger.debug(Logger.OTALib_TAG, "CusSM.sendUpgradeStatus: " + _isSendingUpgradeStatus);
            return;
        }
        final ApplicationEnv.Database.Status status = database.get_status();
        if (status == null) {
            stopTimer();
            return;
        }
        UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(libSettings, status.getDeviceVersion());
        if (ApplicationEnv.PackageState.Querying.name().equals(status.getState()) || ApplicationEnv.PackageState.ABApplyingPatch.name().equals(status.getState()) || ApplicationEnv.PackageState.Upgrading.name().equals(status.getState()) || ApplicationEnv.PackageState.Result.name().equals(status.getState())) {
            sessionDetails = UpdateSessionInfo.fromJsonString(status.getRepository());
        }
        if (sessionDetails != null && sessionDetails.getCheckRequestObj() != null) {
            final CheckRequestObj checkRequestObj = sessionDetails.getCheckRequestObj();
            _isSendingUpgradeStatus = STATE_EVENT_STATUS.SENDING;
            if (isServiceControlEnabled(status.getDeviceVersion()) && !SystemUpdateStatusUtils.RESULT.equalsIgnoreCase(status.getState())) {
                sessionDetails = sessionDetails.setServiceControlResponse(libSettings, status.getDeviceVersion(), "wait");
                startTimer(status.getDeviceVersion(), context);
            }
            if (status.getStatus().equals("RS_OK")) {
                str = "DONE";
            } else if (status.getStatus().equals("RS_FAIL")) {
                str = "FAILED";
            } else if (status.getStatus().equals("RS_TEMP_OK")) {
                str = "PROCESSING";
            } else {
                str = "IGNORED";
            }
            String str2 = str;
            new HashMap().put("appId", libSettings.getString(LibConfigs.APPIID));
            WebRequest webRequest = new WebRequest(StateUrlConstructor.constructUrl(new UrlRequest(CheckUpdateHandler.getMasterCloud(checkRequestObj), libSettings.getString(LibConfigs.UPGRADE_STATE_URL), libSettings.getString(LibConfigs.OTA_CONTEXT), database.get_status().getSourceSha1(), database.get_status().getState(), status.getTrackingID(), libSettings.getString(LibConfigs.UPGRADE_STATE_HTTP_SECURE), libSettings.getString(LibConfigs.UPGRADE_STATE_TEST_URL))), libSettings.getInt(LibConfigs.UPGRADE_STATE_HTTP_RETRIES, 9), libSettings.getString(LibConfigs.UPGRADE_STATE_HTTP_METHOD), null, new WebRequestPayload(WebRequestPayloadType.string, StateRequestBuilder.toJSONString(new StateRequest(checkRequestObj.getAccsSerialNumber(), sessionDetails.getContentTimeSTamp(), CheckUpdateHandler.getDeviceInfoAsJsonObject(checkRequestObj), CheckUpdateHandler.getExtraInfoAsJsonObject(context, "", BuildPropertyUtils.getApkVersion(context), libSettings.getString(LibConfigs.PROVISION_TIME), context.getPackageName(), checkRequestObj), CheckUpdateHandler.getIdentityInfoAsJsonObject(checkRequestObj.getAccsSerialNumber()), database.get_status().getInfo(), null, CDSUtils.IDTYPE, str2 + SmartUpdateUtils.MASK_SEPARATOR + ErrorCodeMapper.getStatus(database.get_status().getInfo()), status.getReportingTag(), CheckUpdateHandler.getUpgradeSource(checkRequestObj.getTriggeredBy()), null))), libSettings.getString(LibConfigs.CDS_HTTP_PROXY_HOST), libSettings.getInt(LibConfigs.CDS_HTTP_PROXY_PORT, -1));
            Logger.debug(Logger.OTALib_TAG, "State request " + webRequest.toString());
            WebService.call(context, webRequest, new ResponseHandler() { // from class: com.motorola.otalib.main.LibCussm.2
                @Override // com.motorola.otalib.cdsservice.ResponseHandler
                public void handleResponse(WebResponse webResponse) {
                    Logger.debug(Logger.OTALib_TAG, "Inside handleStateResponse");
                    LibCussm.this.handleStateWebResponse(webResponse, status.getId(), context, database, libSettings, checkRequestObj);
                }
            }, new WebServiceRetryHandler(), null);
            return;
        }
        Logger.debug(Logger.OTALib_TAG, "CusSM.sendUpgradeStatus: id " + status.getId() + SystemUpdateStatusUtils.SPACE + status.getState() + SystemUpdateStatusUtils.SPACE + status.getRepository());
        database.remove_status(status.getId());
    }

    public synchronized boolean handleStateWebResponse(WebResponse webResponse, int i, Context context, ApplicationEnv.Database database, LibSettings libSettings, CheckRequestObj checkRequestObj) {
        if (database.get_status(i) == null) {
            Logger.debug(Logger.OTALib_TAG, "Duplicate state response with id " + i);
        }
        UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(libSettings, checkRequestObj.getPrimaryKey());
        if (webResponse.getStatusCode() != 200) {
            Logger.error(Logger.OTALib_TAG, "CusSM.handleStateWebResponse: received http error " + webResponse.getStatusCode());
            if (webResponse.getStatusCode() != 0) {
                _isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
            } else {
                _isSendingUpgradeStatus = STATE_EVENT_STATUS.WAITING_FOR_NETWORK;
            }
            if (isServiceControlEnabled(database.get_status().getDeviceVersion())) {
                stopTimer();
                sessionDetails.setServiceControlResponse(libSettings, database.get_status().getDeviceVersion(), "continue");
                runStateMachine(context);
            }
            return false;
        }
        StateResponse from = StateResponseBuilder.from(webResponse.getPayload());
        if (from == null) {
            Logger.error(Logger.OTALib_TAG, "CusSM.handleStateWebResponse: failed to parse upgrade data");
            _isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
            if (isServiceControlEnabled(database.get_status().getDeviceVersion())) {
                stopTimer();
                sessionDetails.setServiceControlResponse(libSettings, database.get_status().getDeviceVersion(), "continue");
                runStateMachine(context);
            }
            return false;
        }
        handleStateResponse(context, from, i, checkRequestObj, sessionDetails);
        return true;
    }

    private void handleStateResponse(Context context, StateResponse stateResponse, int i, CheckRequestObj checkRequestObj, UpdateSessionInfo updateSessionInfo) {
        Logger.debug(Logger.OTALib_TAG, "Inside handleStateResponse");
        _isSendingUpgradeStatus = STATE_EVENT_STATUS.IDLE;
        UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(this.mSettings, checkRequestObj.getPrimaryKey());
        String trackingId = sessionDetails.getTrackingId();
        String trackingId2 = stateResponse.getTrackingId();
        Logger.debug(Logger.OTALib_TAG, "stored id " + trackingId + " response id " + trackingId2);
        ApplicationEnv.Database.Status status = this._db.get_status();
        this._db.remove_status(i);
        if (trackingId != null && trackingId.equals(trackingId2)) {
            if (stateResponse.proceed() && status != null) {
                if (isServiceControlEnabled(status.getDeviceVersion())) {
                    stopTimer();
                    Logger.debug(Logger.OTALib_TAG, "handleStateResponse server told to : " + sessionDetails.setServiceControlResponse(this.mSettings, status.getDeviceVersion(), "continue").getServiceControlResponse());
                    runStateMachine(context);
                }
            } else if (status != null && isServiceControlEnabled(status.getDeviceVersion())) {
                stopTimer();
                Logger.debug(Logger.OTALib_TAG, "handleStateResponse server told to : " + sessionDetails.setServiceControlResponse(this.mSettings, status.getDeviceVersion(), "cancel").getServiceControlResponse());
                runStateMachine(context);
            }
        } else {
            Logger.error(Logger.OTALib_TAG, "CusSM:state request and response trackingId mismatch, return");
        }
        sendUpgradeStatus(this.mSettings, context, this._db);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class defaultAction extends TimerTask {
        Context mContext;
        MetaData md;

        public defaultAction(MetaData metaData, Context context) {
            this.md = metaData;
            this.mContext = context;
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            Logger.debug(Logger.OTALib_TAG, "No Service response, taking  defaultAction");
            LibCussm.this.stopTimer();
            if (this.md.isContinueOnServiceError()) {
                UpdateSessionInfo.getSessionDetails(LibCussm.this.mSettings, this.md.getSourceSha1()).setServiceControlResponse(LibCussm.this.mSettings, this.md.getSourceSha1(), "continue");
                LibCussm.this.runStateMachine(this.mContext);
                return;
            }
            LibCussm.this.cancelOTA(this.mContext, "Server cancelled", this.md.getSourceSha1());
        }
    }

    private int startTimer(String str, Context context) {
        MetaData metaData = getMetaData(str);
        if (metaData == null) {
            Logger.debug(Logger.OTALib_TAG, "startTimer, not setting the timer as description is null");
            return 0;
        }
        synchronized (this) {
            try {
                try {
                    if (_mTimer == null) {
                        defaultAction defaultaction = new defaultAction(metaData, context);
                        Timer timer = new Timer();
                        _mTimer = timer;
                        timer.schedule(defaultaction, metaData.getServiceTimeoutSeconds() * 1000);
                    } else {
                        Logger.debug(Logger.OTALib_TAG, "defaultAction have been scheduled, do nothing");
                    }
                } catch (IllegalStateException e) {
                    Logger.error(Logger.OTALib_TAG, "startTimer, IllegalStateException, Maybe canceled. Ignore it" + e);
                }
            } catch (IllegalArgumentException e2) {
                Logger.error(Logger.OTALib_TAG, "startTimer, IllegalArgumentException, ignore it." + e2);
            }
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopTimer() {
        synchronized (this) {
            if (_mTimer != null) {
                Logger.debug(Logger.OTALib_TAG, "stopTimer, cancel()");
                _mTimer.cancel();
                _mTimer = null;
            } else {
                Logger.debug(Logger.OTALib_TAG, "stopTimer, have stoped, do nothing");
            }
        }
    }
}
