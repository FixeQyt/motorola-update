package com.motorola.otalib.main.Downloader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import com.motorola.otalib.aidl.IDownloadService;
import com.motorola.otalib.aidl.IDownloadServiceCallback;
import com.motorola.otalib.aidl.IOtaLibServiceCallBack;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.downloadservice.DownloadService;
import com.motorola.otalib.downloadservice.dataobjects.DownloadRequest;
import com.motorola.otalib.downloadservice.dataobjects.DownloadRequestBuilder;
import com.motorola.otalib.main.Logger;
import com.motorola.otalib.main.PublicUtilityMethods;
import com.motorola.otalib.main.Settings.LibConfigs;
import com.motorola.otalib.main.Settings.LibSettings;
import java.io.File;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ConfigDownloader implements DownloadHandler {
    private static final String OTA_DOWNLOAD_ID = "com.motorola.ccc.otalib.OtaDownloadRequest";
    private static ServiceConnection mConnection;
    private static String mFilePath;
    private static String mFileUrl;
    private static IDownloadService mService;
    private static boolean progress;
    private static long size;
    private static long startingOffset;
    private final Context ctx;
    private IOtaLibServiceCallBack mCallBack;
    private IDownloadServiceCallback mCallback = new IDownloadServiceCallback.Stub() { // from class: com.motorola.otalib.main.Downloader.ConfigDownloader.1
        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void dlResponse(String str) {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void progress(String str) {
            String str2 = Logger.OTALib_TAG;
            StringBuilder append = new StringBuilder("Download progress for ").append(ConfigDownloader.mFilePath).append(" is ");
            Logger.debug(str2, append.append((int) ((new File(ConfigDownloader.mFilePath).length() * 100) / ConfigDownloader.size)).toString());
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void failed(String str, int i, String str2, String str3) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.failed(). Download failed : " + i);
            boolean unused = ConfigDownloader.progress = false;
            ConfigDownloader.this.unBindwithDownloadService();
            ConfigDownloader.this.sendStatusToClient(false);
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void finished(String str) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.finished: I appear to have read the entire file ");
            ConfigDownloader.this.unBindwithDownloadService();
            ConfigDownloader.this.sendStatusToClient(true);
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void exception(String str, String str2) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.exception(). Download received exception :" + str2);
            boolean unused = ConfigDownloader.progress = false;
            ConfigDownloader.this.unBindwithDownloadService();
            ConfigDownloader.this.sendStatusToClient(false);
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void errorCode(String str, int i, boolean z) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.errorCode(). Download received errorCode :" + i);
            if (!z) {
                boolean unused = ConfigDownloader.progress = false;
            } else {
                ConfigDownloader.this.sendStatusToClient(false);
            }
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void suspended(String str, boolean z) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.suspended()");
            boolean unused = ConfigDownloader.progress = false;
            ConfigDownloader.this.unBindwithDownloadService();
            ConfigDownloader.this.sendStatusToClient(false);
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void initFailed(String str, String str2, String str3) {
            Logger.info(Logger.OTALib_TAG, "LibDownloadHandler.initFailed() " + (str2 + " meta data file download"));
            ConfigDownloader.this.unBindwithDownloadService();
            ConfigDownloader.this.sendStatusToClient(false);
        }
    };
    private final LibSettings settings;

    public ConfigDownloader(Context context, LibSettings libSettings, IOtaLibServiceCallBack iOtaLibServiceCallBack, String str) {
        this.ctx = context;
        this.settings = libSettings;
        this.mCallBack = iOtaLibServiceCallBack;
        mFileUrl = str;
        mFilePath = PublicUtilityMethods.getConfigFilePath(context, str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callDownloadRequest() {
        try {
            Logger.debug(Logger.OTALib_TAG, "LibDownloadHandler : callDownloadRequest() " + mConnection);
            mService.registerCallback(OTA_DOWNLOAD_ID, this.mCallback);
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("proceed", true);
            jSONObject.put("wifiUrl", mFileUrl);
            jSONObject.put("cellUrl", mFileUrl);
            jSONObject.put("adminApnUrl", "");
            jSONObject.put("trackingId", "");
            String jSONObject2 = jSONObject.toString();
            this.settings.setString(LibConfigs.DOWNLOAD_DESCRIPTOR, jSONObject2);
            Logger.debug(Logger.OTALib_TAG, "Descriptor " + jSONObject2);
            mService.downloadRequest(OTA_DOWNLOAD_ID, DownloadRequestBuilder.toJSONString(new DownloadRequest(jSONObject2, false, -1, size, mFilePath, getProxyHost(jSONObject2), getProxyPort(jSONObject2), this.settings.getString(LibConfigs.DISALLOWED_NETS), this.settings.getString(LibConfigs.BACKOFF_VALUES), this.settings.getInt(LibConfigs.MAX_RETRY_COUNT_DL, 3), true, "OtaLib", false, startingOffset)));
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
    public void sendStatusToClient(boolean z) {
        try {
            this.mCallBack.onConfigUpdateStatus(z, mFilePath);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public synchronized void transferUpgrade(ApplicationEnv.Database.Descriptor descriptor) {
        size = -1L;
        Logger.debug(Logger.OTALib_TAG, "LibDownloadHandler.transferUpgrade: filefi " + mFilePath + " a file of size " + size);
        if (mService == null) {
            mConnection = new ServiceConnection() { // from class: com.motorola.otalib.main.Downloader.ConfigDownloader.2
                @Override // android.content.ServiceConnection
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    Logger.debug(Logger.OTALib_TAG, "LibDownloadHandler : onServiceConnected() " + iBinder);
                    IDownloadService unused = ConfigDownloader.mService = IDownloadService.Stub.asInterface(iBinder);
                    if (ConfigDownloader.mService != null) {
                        ConfigDownloader.this.callDownloadRequest();
                    }
                }

                @Override // android.content.ServiceConnection
                public void onServiceDisconnected(ComponentName componentName) {
                    try {
                        if (ConfigDownloader.mService != null) {
                            ConfigDownloader.mService.unregisterCallback(ConfigDownloader.OTA_DOWNLOAD_ID);
                        }
                        Logger.debug(Logger.OTALib_TAG, "LibDownloadHandler  : onServiceDisconnected(): Unregistered for remote service callbacks");
                    } catch (RemoteException e) {
                        Logger.error(Logger.OTALib_TAG, "LibDownloadHandler : onServiceDisconnected(): Error un-registering our callback with dl service: " + e);
                    }
                    boolean unused = ConfigDownloader.progress = false;
                    IDownloadService unused2 = ConfigDownloader.mService = null;
                }
            };
            Logger.info(Logger.OTALib_TAG, "mConnection in transferupgrade: " + mConnection);
            this.ctx.bindService(new Intent(this.ctx, DownloadService.class), mConnection, 1);
        } else {
            callDownloadRequest();
        }
        progress = true;
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
        unBindwithDownloadService();
        progress = false;
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void onDeviceShutdown() {
        progress = false;
    }
}
