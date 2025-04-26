package com.motorola.otalib.main;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.motorola.otalib.aidl.IOtaLibService;
import com.motorola.otalib.aidl.IOtaLibServiceCallBack;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.common.CommonLogger;
import com.motorola.otalib.downloadservice.utils.DownloadServiceLogger;
import com.motorola.otalib.main.PublicUtilityMethods;
import com.motorola.otalib.main.Settings.LibConfigs;
import com.motorola.otalib.main.Settings.LibSettings;
import com.motorola.otalib.main.checkUpdate.CheckRequestObj;
import com.motorola.otalib.main.checkUpdate.UpdateSessionInfo;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class OtaLibService extends Service {
    private static IOtaLibServiceCallBack mClientCallBack;
    private static ScheduledExecutorService scheduledExecutorService;
    private static LibSettings settings;
    private static LibCussm sm;
    private OtaLibBinder mBinder;
    private Context mContext;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    private static final class OtaLibBinder extends IOtaLibService.Stub {
        private OtaLibService mService;

        OtaLibBinder(OtaLibService otaLibService) {
            this.mService = otaLibService;
        }

        void destroy() {
            this.mService = null;
        }

        @Override // com.motorola.otalib.aidl.IOtaLibService
        public boolean registerCallback(IOtaLibServiceCallBack iOtaLibServiceCallBack) {
            IOtaLibServiceCallBack unused = OtaLibService.mClientCallBack = iOtaLibServiceCallBack;
            Logger.debug(Logger.OTALib_TAG, "Client registered with Ota lib service");
            if (OtaLibService.sm == null) {
                LibCussm unused2 = OtaLibService.sm = new LibCussm(this.mService, OtaLibService.settings);
                OtaLibService.sm.onStart(this.mService, OtaLibService.mClientCallBack);
                return true;
            }
            return true;
        }

        @Override // com.motorola.otalib.aidl.IOtaLibService
        public synchronized void checkForUpdate(String str) {
            if (OtaLibService.sm != null) {
                CheckRequestObj fromJsonString = CheckRequestObj.fromJsonString(str);
                String str2 = "";
                String str3 = "";
                if (fromJsonString != null) {
                    try {
                        str3 = fromJsonString.getContextKey();
                        str2 = fromJsonString.getPrimaryKey();
                    } catch (RemoteException unused) {
                    }
                }
                String str4 = str2;
                if (fromJsonString == null || !fromJsonString.isValidRequest()) {
                    InstallStatusInfo installStatusInfo = new InstallStatusInfo(this.mService, str4, OtaLibService.settings, null, PublicUtilityMethods.ERROR_INVALID_REQUEST);
                    installStatusInfo.setStatusMessage("Invalid check request " + str);
                    if (OtaLibService.mClientCallBack == null) {
                        return;
                    }
                    OtaLibService.mClientCallBack.onStatusUpdate(str3, false, installStatusInfo.toString());
                    return;
                }
                UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(OtaLibService.settings, fromJsonString.getPrimaryKey());
                OtaLibService.sm.getCurrentState(fromJsonString.getPrimaryKey());
                if (!UpdateSessionInfo.isUpdateGoingOn(OtaLibService.settings, fromJsonString.getPrimaryKey())) {
                    sessionDetails.setCheckRequestObj(str, OtaLibService.settings);
                    OtaLibService.sm.checkForUpdate(fromJsonString, this.mService);
                } else if (OtaLibService.sm.isBusy(fromJsonString.getPrimaryKey())) {
                    long previousTargetVersion = OtaLibService.sm.getPreviousTargetVersion(fromJsonString.getPrimaryKey());
                    long previousSourceVersion = OtaLibService.sm.getPreviousSourceVersion(fromJsonString.getPrimaryKey());
                    InstallStatusInfo installStatusInfo2 = new InstallStatusInfo();
                    installStatusInfo2.setSourceVersion(fromJsonString.getSourceVersion());
                    installStatusInfo2.setAccSerialNumber(fromJsonString.getAccsSerialNumber());
                    installStatusInfo2.setState(PublicUtilityMethods.OtaState.Result);
                    installStatusInfo2.setTargetVersion(previousTargetVersion);
                    if (previousTargetVersion == fromJsonString.getSourceVersion()) {
                        Log.d(Logger.OTALib_TAG, "Previous update was stuck in " + OtaLibService.sm.getCurrentState(fromJsonString.getPrimaryKey()) + " but update was successful. Reporting Result & sending fresh request");
                        installStatusInfo2.setStatusCode(PublicUtilityMethods.SUCCESS);
                        OtaLibService.sm.OnUpgradeStatus(this.mService, fromJsonString.getPrimaryKey(), true, installStatusInfo2.toString());
                        sessionDetails.setCheckRequestObj(str, OtaLibService.settings);
                        OtaLibService.sm.checkForUpdate(fromJsonString, this.mService);
                    } else if (previousSourceVersion == fromJsonString.getSourceVersion()) {
                        Logger.debug(Logger.OTALib_TAG, "Update is in progress " + OtaLibService.sm.getCurrentState(fromJsonString.getPrimaryKey()));
                        OtaLibService.sm.pleaseRunStateMachine(this.mService);
                    } else {
                        Log.d(Logger.OTALib_TAG, "Previous update was stuck in " + OtaLibService.sm.getCurrentState(fromJsonString.getPrimaryKey()) + " but update was failed. Reporting Result & sending fresh request");
                        installStatusInfo2.setKeepPackage(false);
                        installStatusInfo2.setStatusCode(PublicUtilityMethods.ERROR_VERSION_MISMATCH);
                        installStatusInfo2.setStatusMessage("Target version " + previousTargetVersion + " is not matching with source version " + fromJsonString.getSourceVersion());
                        installStatusInfo2.setReportingError(PublicUtilityMethods.STATUS_CODE.ERROR_VERSION_MISMATCH);
                        OtaLibService.sm.OnUpgradeStatus(this.mService, fromJsonString.getPrimaryKey(), false, installStatusInfo2.toString());
                        sessionDetails.setCheckRequestObj(str, OtaLibService.settings);
                        OtaLibService.sm.checkForUpdate(fromJsonString, this.mService);
                    }
                } else {
                    Logger.debug(Logger.OTALib_TAG, "Duplicate check request");
                    OtaLibService.sm.pleaseRunStateMachine(this.mService);
                }
            } else {
                Logger.error(Logger.OTALib_TAG, "State machine is destroyed, you are calling checkForUpdate() without calling registerCallback()");
            }
        }

        @Override // com.motorola.otalib.aidl.IOtaLibService
        public boolean unregisterCallback() {
            if (OtaLibService.sm != null) {
                OtaLibService.sm.onDestroy();
                LibCussm unused = OtaLibService.sm = null;
            }
            IOtaLibServiceCallBack unused2 = OtaLibService.mClientCallBack = null;
            Logger.debug(Logger.OTALib_TAG, "Client unregistered with Ota lib service");
            return true;
        }

        @Override // com.motorola.otalib.aidl.IOtaLibService
        public void onStatusUpdateBackToLib(String str, boolean z, String str2) {
            Logger.debug(Logger.OTALib_TAG, str + "update status is " + z + " Upgrade info " + str2);
            if (OtaLibService.sm != null) {
                OtaLibService.sm.OnUpgradeStatus(this.mService, PublicUtilityMethods.SHA1Generator(str.concat(InstallStatusInfo.fromJsonString(str2).getAccSerialNumber())), z, str2);
                return;
            }
            Logger.error(Logger.OTALib_TAG, "State machine is destroyed, you are calling onStatusUpdateBackToLib() without calling registerCallback()");
        }

        @Override // com.motorola.otalib.aidl.IOtaLibService
        public void downloadConfigFiles(String str) {
            if (OtaLibService.sm != null) {
                OtaLibService.sm.downloadConfigFiles(this.mService, OtaLibService.mClientCallBack, str);
            } else {
                Logger.error(Logger.OTALib_TAG, "State machine is destroyed, you are calling downloadConfigFiles() without calling registerCallback()");
            }
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Logger.debug(Logger.OTALib_TAG, "Ota lib service Intent");
        return (IBinder) Objects.requireNonNull(this.mBinder);
    }

    @Override // android.app.Service
    public void onCreate() {
        LibConfigs[] values;
        super.onCreate();
        this.mBinder = new OtaLibBinder(this);
        this.mContext = getApplicationContext();
        Logger.OTALib_TAG = getLogPreFix();
        Logger.debug(Logger.OTALib_TAG, "Ota lib service onCreate");
        LibSettings libSettings = new LibSettings(this.mContext);
        settings = libSettings;
        SharedPreferences.Editor edit = libSettings.getPrefs().edit();
        for (LibConfigs libConfigs : LibConfigs.values()) {
            if (!libConfigs.value().isEmpty() && settings.getPrefs().getString(libConfigs.key(), null) == null) {
                edit.putString(libConfigs.key(), libConfigs.value());
            }
        }
        edit.apply();
        saveDownloadLoggerContext(getApplicationContext());
        saveCDSLoggerContext(getApplicationContext());
        saveCommonLoggerContext(getApplicationContext());
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        Logger.debug(Logger.OTALib_TAG, "Ota lib service onStartCommand");
        return 1;
    }

    @Override // android.app.Service
    public void onDestroy() {
        cleanUp();
        OtaLibBinder otaLibBinder = this.mBinder;
        if (otaLibBinder != null) {
            otaLibBinder.destroy();
            this.mBinder = null;
        }
        Logger.debug(Logger.OTALib_TAG, "Ota lib service onDestroy");
        super.onDestroy();
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        cleanUp();
        OtaLibBinder otaLibBinder = this.mBinder;
        if (otaLibBinder != null) {
            otaLibBinder.destroy();
            this.mBinder = null;
        }
        Logger.debug(Logger.OTALib_TAG, "Ota lib service onUnbind");
        super.onUnbind(intent);
        return true;
    }

    @Override // android.app.Service
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Logger.debug(Logger.OTALib_TAG, "Ota lib service onRebind");
    }

    private void saveDownloadLoggerContext(Context context) {
        DownloadServiceLogger.saveContext(context, getLogPreFix());
    }

    private void saveCDSLoggerContext(Context context) {
        CDSLogger.saveContext(context, getLogPreFix());
    }

    private void saveCommonLoggerContext(Context context) {
        CommonLogger.saveContext(context, getLogPreFix());
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
        }
        return scheduledExecutorService;
    }

    private void cleanUp() {
        LibCussm libCussm = sm;
        if (libCussm != null) {
            libCussm.onDestroy();
            sm = null;
        }
        mClientCallBack = null;
    }

    private String getLogPreFix() {
        String[] split = this.mContext.getPackageName().split("\\.");
        return "OtaLib[" + split[split.length - 1] + "]";
    }
}
