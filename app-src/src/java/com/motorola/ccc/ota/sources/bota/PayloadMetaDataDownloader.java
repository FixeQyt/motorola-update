package com.motorola.ccc.ota.sources.bota;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineHelper;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.download.error.ResetSMToGettingDescriptor;
import com.motorola.ccc.ota.sources.bota.download.error.ResetSMToGettingDescriptorExceptionHandler;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.aidl.IDownloadService;
import com.motorola.otalib.aidl.IDownloadServiceCallback;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import com.motorola.otalib.downloadservice.DownloadService;
import com.motorola.otalib.downloadservice.dataobjects.DownloadRequest;
import com.motorola.otalib.downloadservice.dataobjects.DownloadRequestBuilder;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class PayloadMetaDataDownloader implements DownloadHandler {
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String OTA_DOWNLOAD_ID = "com.motorola.ccc.ota.sources.bota.OtaDownloadRequest";
    private static ConnectivityManager cm;
    private static Map<Integer, ResetSMToGettingDescriptor> errorCodeHandlers;
    private static ResetSMToGettingDescriptorExceptionHandler ioExceptionHandler;
    private static ServiceConnection mConnection;
    private static IDownloadService mService;
    private static boolean progress;
    private static long size;
    private static long startingOffset;
    private static String version;
    private final Context ctx;
    private final ApplicationEnv env;
    private IDownloadServiceCallback mCallback = new IDownloadServiceCallback.Stub() { // from class: com.motorola.ccc.ota.sources.bota.PayloadMetaDataDownloader.1
        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void dlResponse(String str) {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void progress(String str) {
            PayloadMetaDataDownloader.this.settings.removeConfig(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
            PayloadMetaDataDownloader.this.settings.removeConfig(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
            if (ResetSMToGettingDescriptor.isRetryPending()) {
                ResetSMToGettingDescriptor.clearRetryTask();
            }
            if (ResetSMToGettingDescriptorExceptionHandler.isRetryPending()) {
                ResetSMToGettingDescriptorExceptionHandler.clearRetryTask();
            }
            PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_TEMP_OK, PayloadMetaDataDownloader.this.settings, PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.env);
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void failed(String str, int i, String str2, String str3) {
            Logger.info("OtaApp", "PayloadMetaDataDownloader.failed(). Download failed : " + i);
            PayloadMetaDataDownloader.progress = false;
            PayloadMetaDataDownloader.this.unBindwithDownloadService();
            if (!NetworkUtils.isNetWorkConnected(PayloadMetaDataDownloader.cm)) {
                Logger.info("OtaApp", "PayloadMetaDataDownloader.failed(). no network, return");
                PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_DEFERRED, PayloadMetaDataDownloader.this.settings, PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.env);
                return;
            }
            ResetSMToGettingDescriptor resetSMToGettingDescriptor = (ResetSMToGettingDescriptor) PayloadMetaDataDownloader.errorCodeHandlers.get(Integer.valueOf(i));
            if (resetSMToGettingDescriptor != null) {
                if (ResetSMToGettingDescriptor.isRetryPending()) {
                    Logger.error("OtaApp", "PayloadMetaDataDownloader.failed(). retry pending, return");
                    PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_RETRIED, PayloadMetaDataDownloader.this.settings, PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.env);
                    return;
                }
                boolean shouldRetry = resetSMToGettingDescriptor.shouldRetry(str3 + " during payload metadata file download ", true, ErrorCodeMapper.KEY_DOWNLOAD_FAILED_PAYLOAD_METADATA_4XX);
                Logger.error("OtaApp", "PayloadMetaDataDownloader.failed(). retry status " + shouldRetry);
                if (shouldRetry) {
                    PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_RETRIED, PayloadMetaDataDownloader.this.settings, PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.env);
                    return;
                }
                return;
            }
            String str4 = "Aborting the OTA process as there is no error handler installed " + i + SmartUpdateUtils.MASK_SEPARATOR + str2 + SmartUpdateUtils.MASK_SEPARATOR + str3;
            Logger.info("OtaApp", str4);
            UpgradeUtilMethods.sendActionVerifyPayloadStatus(PayloadMetaDataDownloader.this.ctx, str4, UpgradeUtils.DownloadStatus.STATUS_FAIL, ErrorCodeMapper.KEY_DOWNLOAD_FAILED_PAYLOAD_METADATA_OTHER);
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void finished(String str) {
            Logger.info("OtaApp", "PayloadMetaDataDownloader.finished: I appear to have read the entire file ");
            PayloadMetaDataDownloader.this.settings.removeConfig(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
            PayloadMetaDataDownloader.this.settings.removeConfig(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
            PayloadMetaDataDownloader.this.unBindwithDownloadService();
            FileUtils.setPermission(FileUtils.getPayloadMetaDataFileName(), FileUtils.FILE_PERMISSION);
            UpdaterEngineHelper.verifyPayloadMetadata(FileUtils.getPayloadMetaDataFileName(), false);
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void exception(String str, String str2) {
            Logger.info("OtaApp", "PayloadMetaDataDownloader.exception(). Download received exception :" + str2);
            PayloadMetaDataDownloader.progress = false;
            PayloadMetaDataDownloader.this.unBindwithDownloadService();
            if (!NetworkUtils.isNetWorkConnected(PayloadMetaDataDownloader.cm)) {
                Logger.info("OtaApp", "PayloadMetaDataDownloader.exception(). no network, return false");
                PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_DEFERRED, PayloadMetaDataDownloader.this.settings, PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.env);
            } else if (str2 != null) {
                if (str2.contains("ENOSPC")) {
                    String str3 = str2 + "not enough free space on /data failing the payload meta data download for this exception";
                    Logger.info("OtaApp", "PayloadMetaDataDownloader.exception:" + str3);
                    UpgradeUtilMethods.sendActionVerifyPayloadStatus(PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.sm.getDeviceAdditionalInfo() + SystemUpdateStatusUtils.SPACE + str3, UpgradeUtils.DownloadStatus.STATUS_SPACE_PAYLOAD_METADATA_CHECK, ErrorCodeMapper.KEY_DATA_OUT_OF_SPACE);
                } else if (str2.contains("IOException") && PayloadMetaDataDownloader.ioExceptionHandler != null) {
                    if (ResetSMToGettingDescriptorExceptionHandler.isRetryPending()) {
                        Logger.error("OtaApp", "PayloadMetaDataDownloader.exception(). retry pending, return");
                        PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_RETRIED, PayloadMetaDataDownloader.this.settings, PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.env);
                        return;
                    }
                    boolean handleException = PayloadMetaDataDownloader.ioExceptionHandler.handleException(str2, str2 + " during payload meta data file download", true, ErrorCodeMapper.KEY_DOWNLOAD_FAILED_PAYLOAD_METADATA_EXCEPTION);
                    Logger.error("OtaApp", "PayloadMetaDataDownloader.exception(). retry status " + handleException);
                    if (handleException) {
                        PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_RETRIED, PayloadMetaDataDownloader.this.settings, PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.env);
                    }
                } else {
                    String str4 = "PayloadMetaDataDownloader.exception:Unhandled Exception found " + str2 + " failing the download for this exception";
                    Logger.info("OtaApp", str4);
                    UpgradeUtilMethods.sendActionVerifyPayloadStatus(PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.sm.getDeviceAdditionalInfo() + SystemUpdateStatusUtils.SPACE + str4, UpgradeUtils.DownloadStatus.STATUS_FAIL, ErrorCodeMapper.KEY_DOWNLOAD_FAILED_PAYLOAD_METADATA_EXCEPTION);
                }
            }
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void errorCode(String str, int i, boolean z) {
            Logger.info("OtaApp", "PayloadMetaDataDownloader.errorCode(). Download received errorCode :" + i);
            if (!z) {
                PayloadMetaDataDownloader.progress = false;
            } else {
                PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_RETRIED, PayloadMetaDataDownloader.this.settings, PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.env);
            }
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void suspended(String str, boolean z) {
            Logger.info("OtaApp", "PayloadMetaDataDownloader.suspended()");
            PayloadMetaDataDownloader.progress = false;
            PayloadMetaDataDownloader.this.unBindwithDownloadService();
            PayloadMetaDataDownloader.displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_DEFERRED, PayloadMetaDataDownloader.this.settings, PayloadMetaDataDownloader.this.ctx, PayloadMetaDataDownloader.this.env);
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void initFailed(String str, String str2, String str3) {
            String str4 = str2 + " payload meta data file download";
            Logger.info("OtaApp", "PayloadMetaDataDownloader.initFailed() " + str4);
            UpgradeUtilMethods.sendActionVerifyPayloadStatus(PayloadMetaDataDownloader.this.ctx, str4, UpgradeUtils.DownloadStatus.STATUS_FAIL, str3);
        }
    };
    private final BotaSettings settings;
    private final CusSM sm;

    public PayloadMetaDataDownloader(Context context, CusSM cusSM, BotaSettings botaSettings, ApplicationEnv applicationEnv) {
        this.ctx = context;
        this.sm = cusSM;
        this.settings = botaSettings;
        this.env = applicationEnv;
        errorCodeHandlers = new HashMap();
        cm = (ConnectivityManager) context.getSystemService("connectivity");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callDownloadRequest() {
        try {
            Logger.debug("OtaApp", "PayloadMetaDataDownloader : callDownloadRequest() " + mConnection);
            mService.registerCallback(OTA_DOWNLOAD_ID, this.mCallback);
            String string = this.settings.getString(Configs.DOWNLOAD_DESCRIPTOR);
            mService.downloadRequest(OTA_DOWNLOAD_ID, DownloadRequestBuilder.toJSONString(new DownloadRequest(string, false, -1, size, FileUtils.getPayloadMetaDataFileName(), getProxyHost(string), getProxyPort(string), this.settings.getString(Configs.DISALLOWED_NETS), this.settings.getString(Configs.BACKOFF_VALUES), this.settings.getInt(Configs.MAX_RETRY_COUNT_DL, 9), true, UpgradeSourceType.upgrade.toString(), false, startingOffset)));
        } catch (Exception e) {
            Logger.error("OtaApp", "PayloadMetaDataDownloader : callDownloadRequest(): Exception" + e);
        }
    }

    private String getProxyHost(String str) {
        String string;
        try {
            string = new JSONObject(str).optString(Configs.DOWNLOAD_HTTP_PROXY_HOST.name(), null);
            if (TextUtils.isEmpty(string)) {
                string = this.settings.getString(Configs.DOWNLOAD_HTTP_PROXY_HOST);
            }
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in PayloadMetaDataDownloader, getProxyHost: " + e);
            string = this.settings.getString(Configs.DOWNLOAD_HTTP_PROXY_HOST);
        }
        Logger.debug("OtaApp", "PayloadMetaDataDownloader: Sync settings proxy host:" + string);
        return string;
    }

    private int getProxyPort(String str) {
        int i;
        try {
            i = new JSONObject(str).optInt(Configs.DOWNLOAD_HTTP_PROXY_PORT.name(), -1);
            if (i == -1) {
                i = this.settings.getInt(Configs.DOWNLOAD_HTTP_PROXY_PORT, -1);
            }
        } catch (JSONException e) {
            Logger.error("OtaApp", "Exception in PayloadMetaDataDownloader, getProxyPort: " + e);
            i = this.settings.getInt(Configs.DOWNLOAD_HTTP_PROXY_PORT, -1);
        }
        Logger.debug("OtaApp", "PayloadMetaDataDownloader: Sync settings proxy port:" + i);
        return i;
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public synchronized void transferUpgrade(ApplicationEnv.Database.Descriptor descriptor) {
        try {
            long[] payloadMetaDataOffsetAndSize = getPayloadMetaDataOffsetAndSize(descriptor.getMeta().getStreamingData());
            startingOffset = payloadMetaDataOffsetAndSize[0];
            size = payloadMetaDataOffsetAndSize[1];
            version = descriptor.getVersion();
            ioExceptionHandler = new ResetSMToGettingDescriptorExceptionHandler(this.ctx, version, this.settings, this.sm);
            File file = new File(FileUtils.getPayloadMetaDataFileName());
            errorCodeHandlers.put(400, new ResetSMToGettingDescriptor(this.ctx, 400, "400 Bad Request", version, file, false, this.settings, this.sm));
            errorCodeHandlers.put(401, new ResetSMToGettingDescriptor(this.ctx, 401, "401 Unauthorized", version, file, false, this.settings, this.sm));
            errorCodeHandlers.put(Integer.valueOf((int) SystemUpdateStatusUtils.ALERT_FAILED_FIRMWARE_UPDATE_PACKAGE_CERT_VALIDATE), new ResetSMToGettingDescriptor(this.ctx, SystemUpdateStatusUtils.ALERT_FAILED_FIRMWARE_UPDATE_PACKAGE_CERT_VALIDATE, "404 Not Found", version, file, true, this.settings, this.sm));
            errorCodeHandlers.put(403, new ResetSMToGettingDescriptor(this.ctx, 403, "403 Forbidden", version, file, false, this.settings, this.sm));
            errorCodeHandlers.put(410, new ResetSMToGettingDescriptor(this.ctx, 410, "410 Gone", version, file, true, this.settings, this.sm));
            errorCodeHandlers.put(412, new ResetSMToGettingDescriptor(this.ctx, 412, "412 Precondition failed", version, file, true, this.settings, this.sm));
            errorCodeHandlers.put(Integer.valueOf((int) HTTP_TOO_MANY_REQUESTS), new ResetSMToGettingDescriptor(this.ctx, HTTP_TOO_MANY_REQUESTS, "429 Too many requests", version, file, true, this.settings, this.sm));
            Logger.debug("OtaApp", "PayloadMetaDataDownloader.transferUpgrade: Version " + descriptor.getVersion() + " a file of size " + size);
            if (CusAndroidUtils.checkForUrlExpiry(this.settings, false)) {
                Logger.debug("OtaApp", "In BotaDownloadHandler.transferupgrade,oops url expired fetching new url");
                this.settings.setString(Configs.OTA_GET_DESCRIPTOR_REASON, "URL timeout");
                CusAndroidUtils.sendGetDescriptor(this.ctx, version, "encountered url timeout go and fetch new download url", false);
                displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_RETRIED, this.settings, this.ctx, this.env);
                decreaseAndClearRetryCount();
                return;
            }
            UpdaterUtils.setSoftBankProxyData(OtaApplication.getGlobalContext());
            if (mService == null) {
                mConnection = new ServiceConnection() { // from class: com.motorola.ccc.ota.sources.bota.PayloadMetaDataDownloader.2
                    @Override // android.content.ServiceConnection
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        Logger.debug("OtaApp", "PayloadMetaDataDownloader : onServiceConnected() " + iBinder);
                        PayloadMetaDataDownloader.mService = IDownloadService.Stub.asInterface(iBinder);
                        if (PayloadMetaDataDownloader.mService != null) {
                            PayloadMetaDataDownloader.this.callDownloadRequest();
                        }
                    }

                    @Override // android.content.ServiceConnection
                    public void onServiceDisconnected(ComponentName componentName) {
                        try {
                            if (PayloadMetaDataDownloader.mService != null) {
                                PayloadMetaDataDownloader.mService.unregisterCallback(PayloadMetaDataDownloader.OTA_DOWNLOAD_ID);
                            }
                            Logger.debug("OtaApp", "PayloadMetaDataDownloader  : onServiceDisconnected(): Unregistered for remote service callbacks");
                        } catch (RemoteException e) {
                            Logger.error("OtaApp", "PayloadMetaDataDownloader : onServiceDisconnected(): Error un-registering our callback with dl service: " + e);
                        }
                        PayloadMetaDataDownloader.progress = false;
                        PayloadMetaDataDownloader.mService = null;
                    }
                };
                Logger.info("OtaApp", "mConnection in transferupgrade: " + mConnection);
                this.ctx.bindService(new Intent(this.ctx, DownloadService.class), mConnection, 1);
            } else {
                callDownloadRequest();
            }
            progress = true;
            displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_TEMP_OK, this.settings, this.ctx, this.env);
        } catch (JSONException e) {
            String str = "Failed to receive value from the server " + e.toString();
            Logger.info("OtaApp", "PayloadMetaDataDownloader.transferUpgrade : " + str);
            UpgradeUtilMethods.sendActionVerifyPayloadStatus(this.ctx, str, UpgradeUtils.DownloadStatus.STATUS_OK, ErrorCodeMapper.KEY_PAYLOAD_METADATA_VERIFICATION_FAILED);
        }
    }

    private void decreaseAndClearRetryCount() {
        if (ResetSMToGettingDescriptor.isRetryPending()) {
            if (this.settings.getInt(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS, 0) > 0) {
                this.settings.decrementPrefs(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
            }
            ResetSMToGettingDescriptor.clearRetryTask();
        }
        if (ResetSMToGettingDescriptorExceptionHandler.isRetryPending()) {
            if (this.settings.getInt(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS, 0) > 0) {
                this.settings.decrementPrefs(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
            }
            ResetSMToGettingDescriptorExceptionHandler.clearRetryTask();
        }
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public boolean isBusy() {
        Logger.info("OtaApp", "PayloadMetaDataDownloader : Current download status:" + progress);
        return progress;
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void close() {
        Logger.info("OtaApp", "PayloadMetaDataDownloader.close, with current dl status : " + progress);
        progress = false;
        unBindwithDownloadService();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unBindwithDownloadService() {
        Logger.info("OtaApp", "PayloadMetaDataDownloader.unBindwithDownloadService, with mService:" + mService + " and mConnection: " + mConnection);
        IDownloadService iDownloadService = mService;
        if (iDownloadService == null || mConnection == null) {
            return;
        }
        try {
            iDownloadService.unregisterCallback(OTA_DOWNLOAD_ID);
            this.ctx.unbindService(mConnection);
        } catch (Exception e) {
            Logger.info("OtaApp", "PayloadMetaDataDownloader.unBindwithDownloadService, exception " + e);
        }
        mService = null;
        mConnection = null;
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public synchronized void radioGotDown() {
        unBindwithDownloadService();
        progress = false;
        displayVerifyNotification(UpgradeUtils.DownloadStatus.STATUS_DEFERRED, this.settings, this.ctx, this.env);
    }

    public static void displayVerifyNotification(UpgradeUtils.DownloadStatus downloadStatus, BotaSettings botaSettings, Context context, ApplicationEnv applicationEnv) {
        displayVerifyNotification(downloadStatus, botaSettings, context, applicationEnv, "");
    }

    public static void displayVerifyNotification(UpgradeUtils.DownloadStatus downloadStatus, BotaSettings botaSettings, Context context, ApplicationEnv applicationEnv, String str) {
        MetaData from = MetaDataBuilder.from(botaSettings.getString(Configs.METADATA));
        if (from == null || from.showDownloadProgress()) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            if (SmartUpdateUtils.isDownloadForcedForSmartUpdate(botaSettings) && downloadStatus != UpgradeUtils.DownloadStatus.STATUS_DEFERRED) {
                notificationManager.cancel(NotificationUtils.OTA_NOTIFICATION_ID);
                return;
            }
            NotificationUtils.showVerifyNotification(context, downloadStatus);
            applicationEnv.getUtilities().sendVerifyPayloadMetadataFileDownloadStatus(downloadStatus, str);
        }
    }

    public long[] getPayloadMetaDataOffsetAndSize(JSONObject jSONObject) throws JSONException {
        JSONObject optJSONObject;
        JSONObject optJSONObject2;
        if (jSONObject != null && (optJSONObject = jSONObject.optJSONObject("additionalInfo")) != null && (optJSONObject2 = optJSONObject.optJSONObject("payload_metadata")) != null) {
            return new long[]{optJSONObject2.getLong("offset"), optJSONObject2.getLong("size")};
        }
        throw new JSONException("getPayloadMetaDataOffsetAndSize: server did not sendpayloadMetaData properties");
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void onDeviceShutdown() {
        progress = false;
        if (ResetSMToGettingDescriptor.isRetryPending() && this.settings.getInt(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS, 0) > 0) {
            this.settings.decrementPrefs(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
        }
        if (!ResetSMToGettingDescriptorExceptionHandler.isRetryPending() || this.settings.getInt(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS, 0) <= 0) {
            return;
        }
        this.settings.decrementPrefs(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
    }
}
