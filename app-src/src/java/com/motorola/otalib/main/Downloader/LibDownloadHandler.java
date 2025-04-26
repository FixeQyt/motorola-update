package com.motorola.otalib.main.Downloader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.otalib.aidl.IDownloadService;
import com.motorola.otalib.aidl.IDownloadServiceCallback;
import com.motorola.otalib.aidl.IOtaLibServiceCallBack;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import com.motorola.otalib.downloadservice.DownloadService;
import com.motorola.otalib.downloadservice.dataobjects.DownloadRequest;
import com.motorola.otalib.downloadservice.dataobjects.DownloadRequestBuilder;
import com.motorola.otalib.main.InstallStatusInfo;
import com.motorola.otalib.main.LibCussm;
import com.motorola.otalib.main.Logger;
import com.motorola.otalib.main.PublicUtilityMethods;
import com.motorola.otalib.main.Settings.LibConfigs;
import com.motorola.otalib.main.Settings.LibSettings;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class LibDownloadHandler implements DownloadHandler {
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String OTA_DOWNLOAD_ID = "com.motorola.ccc.otalib.OtaDownloadRequest";
    private static String accSerialNumber;
    private static ConnectivityManager cm;
    private static String contextKey;
    private static Map<Integer, ResetSMToGettingDescriptor> errorCodeHandlers;
    private static String internalName;
    private static ResetSMToGettingDescriptorExceptionHandler ioExceptionHandler;
    private static ServiceConnection mConnection;
    private static IDownloadService mService;
    private static String primaryKey;
    private static boolean progress;
    private static long size;
    private static long startingOffset;
    private static long targetVersion;
    private final Context ctx;
    private ApplicationEnv.Database.Descriptor descriptor;
    private IOtaLibServiceCallBack mCallBack;
    private IDownloadServiceCallback mCallback = new IDownloadServiceCallback.Stub() { // from class: com.motorola.otalib.main.Downloader.LibDownloadHandler.1
        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void dlResponse(String str) {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void progress(String str) {
            LibDownloadHandler.this.settings.removeConfig(LibConfigs.OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS);
            LibDownloadHandler.this.settings.removeConfig(LibConfigs.OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
            if (ResetSMToGettingDescriptor.isRetryPending()) {
                ResetSMToGettingDescriptor.clearRetryTask();
            }
            if (ResetSMToGettingDescriptorExceptionHandler.isRetryPending()) {
                ResetSMToGettingDescriptorExceptionHandler.clearRetryTask();
            }
            LibDownloadHandler.this.sendStatusToClient(PublicUtilityMethods.SUCCESS, "");
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void failed(String str, int i, String str2, String str3) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.failed(). Download failed : " + i);
            boolean unused = LibDownloadHandler.progress = false;
            LibDownloadHandler.this.unBindwithDownloadService();
            if (NetworkUtils.isNetWorkConnected(LibDownloadHandler.cm)) {
                ResetSMToGettingDescriptor resetSMToGettingDescriptor = (ResetSMToGettingDescriptor) LibDownloadHandler.errorCodeHandlers.get(Integer.valueOf(i));
                if (resetSMToGettingDescriptor != null) {
                    if (ResetSMToGettingDescriptor.isRetryPending()) {
                        Logger.error(Logger.OTALib_TAG, "LibDownloadHandler.failed(). retry pending, return");
                        LibDownloadHandler.this.sendStatusToClient(PublicUtilityMethods.ERROR_RETRY, "Error retrying");
                        return;
                    }
                    boolean shouldRetry = resetSMToGettingDescriptor.shouldRetry(str3 + " during package file download ", true, ErrorCodeMapper.KEY_DOWNLOAD_FAILED_PACKAGE_4XX);
                    Logger.error(Logger.OTALib_TAG, "LibDownloadHandler.failed(). retry status " + shouldRetry);
                    if (shouldRetry) {
                        LibDownloadHandler.this.sendStatusToClient(PublicUtilityMethods.ERROR_RETRY, "Error retrying");
                        return;
                    }
                    return;
                }
                String str4 = "Aborting the OTA process as there is no error handler installed " + i + SmartUpdateUtils.MASK_SEPARATOR + str2 + SmartUpdateUtils.MASK_SEPARATOR + str3;
                Logger.info(Logger.OTALib_TAG, str4);
                LibDownloadHandler.this.sm.failProgress(LibDownloadHandler.this.ctx, LibDownloadHandler.primaryKey, UpgradeUtils.DownloadStatus.STATUS_FAIL, str4, "DOWNLOAD_FAILED_PACKAGE_OTHER.");
                return;
            }
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.failed(). no network, return");
            LibDownloadHandler.this.sendStatusToClient(PublicUtilityMethods.NO_NETWORK, "No network");
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void finished(String str) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.finished: I appear to have read the entire file ");
            LibDownloadHandler.this.finishDownload();
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void exception(String str, String str2) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.exception(). Download received exception :" + str2);
            boolean unused = LibDownloadHandler.progress = false;
            LibDownloadHandler.this.unBindwithDownloadService();
            if (!NetworkUtils.isNetWorkConnected(LibDownloadHandler.cm)) {
                Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.exception(). no network, return false");
                LibDownloadHandler.this.sendStatusToClient(PublicUtilityMethods.NO_NETWORK, "No network");
            } else if (str2 != null) {
                if (str2.contains("ENOSPC")) {
                    String str3 = str2 + "not enough free space on /data failing the meta data download for this exception";
                    Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.exception:" + str3);
                    LibDownloadHandler.this.sm.failProgress(LibDownloadHandler.this.ctx, LibDownloadHandler.primaryKey, UpgradeUtils.DownloadStatus.STATUS_SPACE, str3, ErrorCodeMapper.KEY_DATA_OUT_OF_SPACE);
                } else if (str2.contains("IOException") && LibDownloadHandler.ioExceptionHandler != null) {
                    if (ResetSMToGettingDescriptorExceptionHandler.isRetryPending()) {
                        Logger.error(Logger.OTALib_TAG, "LibDownloadHandler.exception(). retry pending, return");
                        LibDownloadHandler.this.sendStatusToClient(PublicUtilityMethods.ERROR_RETRY, "Retry pending");
                        return;
                    }
                    boolean handleException = LibDownloadHandler.ioExceptionHandler.handleException(str2, str2 + " during meta data file download", true, "DOWNLOAD_FAILED_PACKAGE_EXCEPTION.");
                    Logger.error(Logger.OTALib_TAG, "LibDownloadHandler.exception(). retry status " + handleException);
                    if (handleException) {
                        LibDownloadHandler.this.sendStatusToClient(PublicUtilityMethods.ERROR_RETRY, "Retry pending");
                    }
                } else {
                    String str4 = "LibDownloadHandler.exception:Unhandled Exception found " + str2 + " failing the download for this exception";
                    Logger.info(Logger.OTALib_TAG, str4);
                    LibDownloadHandler.this.sm.failProgress(LibDownloadHandler.this.ctx, LibDownloadHandler.primaryKey, UpgradeUtils.DownloadStatus.STATUS_FAIL, str4, "DOWNLOAD_FAILED_PACKAGE_EXCEPTION.");
                }
            }
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void errorCode(String str, int i, boolean z) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.errorCode(). Download received errorCode :" + i);
            if (!z) {
                boolean unused = LibDownloadHandler.progress = false;
            } else {
                LibDownloadHandler.this.sendStatusToClient(PublicUtilityMethods.ERROR_RETRY, "Retry pending");
            }
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void suspended(String str, boolean z) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.suspended()");
            boolean unused = LibDownloadHandler.progress = false;
            LibDownloadHandler.this.unBindwithDownloadService();
            LibDownloadHandler.this.sendStatusToClient(PublicUtilityMethods.SUSPENDED, "Suspended");
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void initFailed(String str, String str2, String str3) {
            String str4 = str2 + " meta data file download";
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.initFailed() " + str4);
            LibDownloadHandler.this.unBindwithDownloadService();
            LibDownloadHandler.this.sm.failProgress(LibDownloadHandler.this.ctx, LibDownloadHandler.primaryKey, UpgradeUtils.DownloadStatus.STATUS_FAIL, str4, ErrorCodeMapper.KEY_OTHER);
        }
    };
    private final LibSettings settings;
    private final LibCussm sm;

    public LibDownloadHandler(Context context, LibCussm libCussm, LibSettings libSettings, String str, IOtaLibServiceCallBack iOtaLibServiceCallBack, String str2, long j, String str3, String str4, ApplicationEnv.Database.Descriptor descriptor) {
        this.ctx = context;
        this.sm = libCussm;
        this.settings = libSettings;
        internalName = str;
        this.mCallBack = iOtaLibServiceCallBack;
        targetVersion = j;
        primaryKey = str4;
        accSerialNumber = str3;
        this.descriptor = descriptor;
        contextKey = str2;
        errorCodeHandlers = new HashMap();
        cm = (ConnectivityManager) context.getSystemService("connectivity");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callDownloadRequest() {
        try {
            Logger.debug(Logger.OTALib_TAG, "LibDownloadHandler : callDownloadRequest() " + mConnection);
            mService.registerCallback(OTA_DOWNLOAD_ID, this.mCallback);
            String string = this.settings.getString(LibConfigs.DOWNLOAD_DESCRIPTOR);
            Logger.debug(Logger.OTALib_TAG, "Descriptor " + string);
            mService.downloadRequest(OTA_DOWNLOAD_ID, DownloadRequestBuilder.toJSONString(new DownloadRequest(string, false, -1, size, PublicUtilityMethods.getFileName(this.ctx, internalName, targetVersion), getProxyHost(string), getProxyPort(string), this.settings.getString(LibConfigs.DISALLOWED_NETS), this.settings.getString(LibConfigs.BACKOFF_VALUES), this.settings.getInt(LibConfigs.MAX_RETRY_COUNT_DL, 3), true, "OtaLib", false, startingOffset)));
        } catch (Exception e) {
            Logger.error(Logger.OTALib_TAG, "LibDownloadHandler : callDownloadRequest(): Exception" + e);
        }
    }

    private String getProxyHost(String str) {
        String string;
        try {
            string = new JSONObject(str).optString(LibConfigs.DOWNLOAD_HTTP_PROXY_HOST.name(), null);
            if (TextUtils.isEmpty(string)) {
                string = this.settings.getString(LibConfigs.DOWNLOAD_HTTP_PROXY_HOST);
            }
        } catch (JSONException e) {
            Logger.error(Logger.OTALib_TAG, "Exception in LibDownloadHandler, getProxyHost: " + e);
            string = this.settings.getString(LibConfigs.DOWNLOAD_HTTP_PROXY_HOST);
        }
        Logger.debug(Logger.OTALib_TAG, "LibDownloadHandler: Sync settings proxy host:" + string);
        return string;
    }

    private int getProxyPort(String str) {
        int i;
        try {
            i = new JSONObject(str).optInt(LibConfigs.DOWNLOAD_HTTP_PROXY_PORT.name(), -1);
            if (i == -1) {
                i = this.settings.getInt(LibConfigs.DOWNLOAD_HTTP_PROXY_PORT, -1);
            }
        } catch (JSONException e) {
            Logger.error(Logger.OTALib_TAG, "Exception in LibDownloadHandler, getProxyPort: " + e);
            i = this.settings.getInt(LibConfigs.DOWNLOAD_HTTP_PROXY_PORT, -1);
        }
        Logger.debug(Logger.OTALib_TAG, "LibDownloadHandler: Sync settings proxy port:" + i);
        return i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishDownload() {
        this.settings.removeConfig(LibConfigs.OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS);
        this.settings.removeConfig(LibConfigs.OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
        this.settings.removeConfig(LibConfigs.DOWNLOAD_DESCRIPTOR);
        this.settings.removeConfig(LibConfigs.DOWNLOAD_DESCRIPTOR_TIME);
        unBindwithDownloadService();
        this.sm.onInternalNotification(this.ctx, primaryKey);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendStatusToClient(int i, String str) {
        if (i == -1) {
            this.sm.failProgress(this.ctx, primaryKey, UpgradeUtils.DownloadStatus.STATUS_FAIL, str, "DOWNLOAD_FAILED_PACKAGE_OTHER.");
            return;
        }
        InstallStatusInfo installStatusInfo = new InstallStatusInfo(this.ctx, primaryKey, this.settings, this.descriptor, i);
        installStatusInfo.setStatusMessage(str);
        try {
            this.mCallBack.onStatusUpdate(contextKey, true, installStatusInfo.toString());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public synchronized void transferUpgrade(ApplicationEnv.Database.Descriptor descriptor) {
        size = this.descriptor.getMeta().getSize();
        ioExceptionHandler = new ResetSMToGettingDescriptorExceptionHandler(this.ctx, primaryKey, this.settings, this.sm);
        File file = new File(PublicUtilityMethods.getFileName(this.ctx, internalName, targetVersion));
        errorCodeHandlers.put(400, new ResetSMToGettingDescriptor(this.ctx, 400, "400 Bad Request", primaryKey, file, false, this.settings, this.sm));
        errorCodeHandlers.put(401, new ResetSMToGettingDescriptor(this.ctx, 401, "401 Unauthorized", primaryKey, file, false, this.settings, this.sm));
        errorCodeHandlers.put(Integer.valueOf((int) SystemUpdateStatusUtils.ALERT_FAILED_FIRMWARE_UPDATE_PACKAGE_CERT_VALIDATE), new ResetSMToGettingDescriptor(this.ctx, SystemUpdateStatusUtils.ALERT_FAILED_FIRMWARE_UPDATE_PACKAGE_CERT_VALIDATE, "404 Not Found", primaryKey, file, true, this.settings, this.sm));
        errorCodeHandlers.put(403, new ResetSMToGettingDescriptor(this.ctx, 403, "403 Forbidden", primaryKey, file, false, this.settings, this.sm));
        errorCodeHandlers.put(410, new ResetSMToGettingDescriptor(this.ctx, 410, "410 Gone", primaryKey, file, true, this.settings, this.sm));
        errorCodeHandlers.put(412, new ResetSMToGettingDescriptor(this.ctx, 412, "412 Precondition failed", primaryKey, file, true, this.settings, this.sm));
        errorCodeHandlers.put(Integer.valueOf((int) HTTP_TOO_MANY_REQUESTS), new ResetSMToGettingDescriptor(this.ctx, HTTP_TOO_MANY_REQUESTS, "429 Too many requests", primaryKey, file, true, this.settings, this.sm));
        Logger.debug(Logger.OTALib_TAG, "LibDownloadHandler.transferUpgrade: primaryKey " + primaryKey + " a file of size " + size);
        if (!NetworkUtils.isNetWorkConnected(cm)) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.failed(). no network, return");
            sendStatusToClient(PublicUtilityMethods.NO_NETWORK, "No network");
        } else if (PublicUtilityMethods.checkForUrlExpiry(this.settings)) {
            Logger.debug(Logger.OTALib_TAG, "In LibDownloadHandler.transferupgrade,oops url expired fetching new url");
            ResetSMToGettingDescriptor resetSMToGettingDescriptor = errorCodeHandlers.get(403);
            if (resetSMToGettingDescriptor != null) {
                if (ResetSMToGettingDescriptor.isRetryPending()) {
                    Logger.error(Logger.OTALib_TAG, "LibDownloadHandler.failed(). retry pending, return");
                    sendStatusToClient(PublicUtilityMethods.ERROR_RETRY, "Error retrying");
                    return;
                }
                boolean shouldRetry = resetSMToGettingDescriptor.shouldRetry("URL timeout during package file download ", true, ErrorCodeMapper.KEY_DOWNLOAD_FAILED_PACKAGE_4XX);
                Logger.error(Logger.OTALib_TAG, "LibDownloadHandler.failed(). retry status " + shouldRetry);
                if (shouldRetry) {
                    sendStatusToClient(PublicUtilityMethods.ERROR_RETRY, "Error retrying");
                }
            } else {
                Logger.info(Logger.OTALib_TAG, "Aborting the OTA process as there is no error handler installed 403");
                this.sm.failProgress(this.ctx, primaryKey, UpgradeUtils.DownloadStatus.STATUS_FAIL, "Aborting the OTA process as there is no error handler installed 403", "DOWNLOAD_FAILED_PACKAGE_OTHER.");
            }
        } else {
            if (mService == null) {
                mConnection = new ServiceConnection() { // from class: com.motorola.otalib.main.Downloader.LibDownloadHandler.2
                    @Override // android.content.ServiceConnection
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        Logger.debug(Logger.OTALib_TAG, "LibDownloadHandler : onServiceConnected() " + iBinder);
                        IDownloadService unused = LibDownloadHandler.mService = IDownloadService.Stub.asInterface(iBinder);
                        if (LibDownloadHandler.mService != null) {
                            LibDownloadHandler.this.callDownloadRequest();
                        }
                    }

                    @Override // android.content.ServiceConnection
                    public void onServiceDisconnected(ComponentName componentName) {
                        try {
                            if (LibDownloadHandler.mService != null) {
                                LibDownloadHandler.mService.unregisterCallback(LibDownloadHandler.OTA_DOWNLOAD_ID);
                            }
                            Logger.debug(Logger.OTALib_TAG, "LibDownloadHandler  : onServiceDisconnected(): Unregistered for remote service callbacks");
                        } catch (RemoteException e) {
                            Logger.error(Logger.OTALib_TAG, "LibDownloadHandler : onServiceDisconnected(): Error un-registering our callback with dl service: " + e);
                        }
                        boolean unused = LibDownloadHandler.progress = false;
                        IDownloadService unused2 = LibDownloadHandler.mService = null;
                    }
                };
                Logger.info(Logger.OTALib_TAG, "mConnection in transferupgrade: " + mConnection);
                this.ctx.bindService(new Intent(this.ctx, DownloadService.class), mConnection, 1);
            } else {
                callDownloadRequest();
            }
            progress = true;
        }
    }

    private void decreaseAndClearRetryCount() {
        if (ResetSMToGettingDescriptor.isRetryPending()) {
            if (this.settings.getInt(LibConfigs.OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS, 0) > 0) {
                this.settings.decrementPrefs(LibConfigs.OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS);
            }
            ResetSMToGettingDescriptor.clearRetryTask();
        }
        if (ResetSMToGettingDescriptorExceptionHandler.isRetryPending()) {
            if (this.settings.getInt(LibConfigs.OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS, 0) > 0) {
                this.settings.decrementPrefs(LibConfigs.OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
            }
            ResetSMToGettingDescriptorExceptionHandler.clearRetryTask();
        }
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public boolean isBusy() {
        Logger.info(Logger.OTALib_TAG, "LibDownloadHandler : Current download status:" + progress);
        return progress;
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void close() {
        Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.close, with current dl status : " + progress);
        progress = false;
        unBindwithDownloadService();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unBindwithDownloadService() {
        Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.unBindwithDownloadService, with mService:" + mService + " and mConnection: " + mConnection);
        IDownloadService iDownloadService = mService;
        if (iDownloadService == null || mConnection == null) {
            return;
        }
        try {
            iDownloadService.unregisterCallback(OTA_DOWNLOAD_ID);
            this.ctx.unbindService(mConnection);
        } catch (Exception e) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.unBindwithDownloadService, exception " + e);
        }
        mService = null;
        mConnection = null;
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public synchronized void radioGotDown() {
        if (!NetworkUtils.isNetWorkConnected(cm)) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.radioGotDown(). no network, return false");
            sendStatusToClient(PublicUtilityMethods.NO_NETWORK, "No network");
            return;
        }
        unBindwithDownloadService();
        progress = false;
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void onDeviceShutdown() {
        progress = false;
        if (ResetSMToGettingDescriptor.isRetryPending() && this.settings.getInt(LibConfigs.OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS, 0) > 0) {
            this.settings.decrementPrefs(LibConfigs.OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS);
        }
        if (!ResetSMToGettingDescriptorExceptionHandler.isRetryPending() || this.settings.getInt(LibConfigs.OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS, 0) <= 0) {
            return;
        }
        this.settings.decrementPrefs(LibConfigs.OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
    }
}
