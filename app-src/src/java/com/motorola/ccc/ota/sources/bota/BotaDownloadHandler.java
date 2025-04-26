package com.motorola.ccc.ota.sources.bota;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.DownloadHelper;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.installer.updaterEngine.common.InstallerUtilMethods;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.download.error.ResetSMToGettingDescriptor;
import com.motorola.ccc.ota.sources.bota.download.error.ResetSMToGettingDescriptorExceptionHandler;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.stats.StatsHelper;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.aidl.IDownloadService;
import com.motorola.otalib.aidl.IDownloadServiceCallback;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import com.motorola.otalib.downloadservice.DownloadService;
import com.motorola.otalib.downloadservice.dataobjects.DownloadRequest;
import com.motorola.otalib.downloadservice.dataobjects.DownloadRequestBuilder;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class BotaDownloadHandler implements DownloadHandler {
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    private static final String OTA_DOWNLOAD_ID = "com.motorola.ccc.ota.sources.bota.OtaDownloadRequest";
    private static ConnectivityManager cm;
    private static Map<Integer, ResetSMToGettingDescriptor> errorCodeHandlers;
    private static String fileName;
    private static ResetSMToGettingDescriptorExceptionHandler ioExceptionHandler;
    private static ServiceConnection mConnection;
    private static IDownloadService mService;
    private static Timer mTimer;
    private static String repo;
    private static long size;
    private static long time;
    private static String version;
    private static boolean wifiOnly;
    private DownloadHelper _dlHelper;
    private final Context ctx;
    private final ApplicationEnv env;
    private boolean isModemUpdate;
    private IDownloadServiceCallback mCallback = new IDownloadServiceCallback.Stub() { // from class: com.motorola.ccc.ota.sources.bota.BotaDownloadHandler.1
        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void dlResponse(String str) {
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void progress(String str) {
            ApplicationEnv.Database.Descriptor description = BotaDownloadHandler.this.sm.getDescription(BotaDownloadHandler.version);
            BotaDownloadHandler.this.settings.removeConfig(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
            BotaDownloadHandler.this.settings.removeConfig(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
            if (ResetSMToGettingDescriptor.isRetryPending()) {
                ResetSMToGettingDescriptor.clearRetryTask();
            }
            if (ResetSMToGettingDescriptorExceptionHandler.isRetryPending()) {
                ResetSMToGettingDescriptorExceptionHandler.clearRetryTask();
            }
            ConnectivityManager connectivityManager = (ConnectivityManager) BotaDownloadHandler.this.ctx.getSystemService("connectivity");
            if (BotaDownloadHandler.wifiOnly) {
                if (NetworkUtils.isWifi(connectivityManager)) {
                    BotaDownloadHandler.this.env.getUtilities().cleanBotaWifiDiscoveryTimer();
                } else {
                    Logger.debug("OtaApp", " WiFi-only package but not on WiFi; we cannot start a new download; startWifiDiscoveryTimer");
                    BotaDownloadHandler.this.env.getUtilities().startBotaWifiDiscoveryTimer();
                    BotaDownloadHandler.this.setProgressAndActualDownloadTime(false);
                    BotaDownloadHandler.this.unBindwithDownloadService();
                    return;
                }
            }
            if (BotaDownloadHandler.this._dlHelper.notifyProgress()) {
                BotaDownloadHandler.this._dlHelper.incrementPercentDownloaded();
                Logger.verbose("OtaApp", "Notifying Updater to update progress " + BotaDownloadHandler.this._dlHelper.get_percentDownloaded());
                BotaDownloadHandler.this.env.getUtilities().sendUpdateDownloadStatusProgress(BotaDownloadHandler.this._dlHelper.version(), BotaDownloadHandler.this._dlHelper.size(), BotaDownloadHandler.this._dlHelper.expected(), description.getRepository());
            }
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void failed(String str, int i, String str2, String str3) {
            Logger.info("OtaApp", "BotaDownloadHandler.failed(). Download failed : " + i);
            BotaDownloadHandler.this.setProgressAndActualDownloadTime(false);
            BotaDownloadHandler.this.unBindwithDownloadService();
            if (!NetworkUtils.isNetWorkConnected(BotaDownloadHandler.cm)) {
                Logger.info("OtaApp", "BotaDownloadHandler.failed(). no network, return");
                return;
            }
            ResetSMToGettingDescriptor resetSMToGettingDescriptor = (ResetSMToGettingDescriptor) BotaDownloadHandler.errorCodeHandlers.get(Integer.valueOf(i));
            if (resetSMToGettingDescriptor != null) {
                if (ResetSMToGettingDescriptor.isRetryPending()) {
                    Logger.error("OtaApp", "BotaDownloadHandler.failed(). retry pending, return");
                    BotaDownloadHandler.this.env.getUtilities().sendUpdateDownloadStatusRetried(BotaDownloadHandler.version, new File(BotaDownloadHandler.fileName).length(), BotaDownloadHandler.size);
                    return;
                }
                boolean shouldRetry = resetSMToGettingDescriptor.shouldRetry(str3 + " during BOTA package download ", false, ErrorCodeMapper.KEY_DOWNLOAD_FAILED_PACKAGE_4XX);
                if (shouldRetry) {
                    BotaDownloadHandler.this.env.getUtilities().sendUpdateDownloadStatusRetried(BotaDownloadHandler.version, new File(BotaDownloadHandler.fileName).length(), BotaDownloadHandler.size);
                }
                Logger.error("OtaApp", "BotaDownloadHandler.failed(). retry status " + shouldRetry);
                return;
            }
            String str4 = "Aborting the OTA process as there is no error handler installed " + i + SmartUpdateUtils.MASK_SEPARATOR + str2 + SmartUpdateUtils.MASK_SEPARATOR + str3;
            Logger.info("OtaApp", str4);
            new File(BotaDownloadHandler.fileName).delete();
            BotaDownloadHandler.this.sm.failDownload(BotaDownloadHandler.version, UpgradeUtils.DownloadStatus.STATUS_FAIL, str4, "DOWNLOAD_FAILED_PACKAGE_OTHER.");
            BotaDownloadHandler.this.settings.incrementPrefs(Configs.ADVANCE_DL_RETRY_COUNT);
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void finished(String str) {
            ApplicationEnv.Database.Descriptor description = BotaDownloadHandler.this.sm.getDescription(BotaDownloadHandler.version);
            Logger.info("OtaApp", "BotaDownloadHandler.finished: I appear to have read the entire file " + BotaDownloadHandler.this._dlHelper.expected() + FileUtils.SD_CARD_DIR + BotaDownloadHandler.this._dlHelper.left());
            if (BuildPropReader.isBotaATT()) {
                BotaDownloadHandler.this.env.getUtilities().cleanFotaWifiDiscoveryTimer();
            }
            ConnectivityManager connectivityManager = (ConnectivityManager) BotaDownloadHandler.this.ctx.getSystemService("connectivity");
            TelephonyManager telephonyManager = (TelephonyManager) BotaDownloadHandler.this.ctx.getSystemService("phone");
            BotaDownloadHandler botaDownloadHandler = BotaDownloadHandler.this;
            if (!botaDownloadHandler.verifyFile(botaDownloadHandler._dlHelper.fileName())) {
                Logger.info("OtaApp", "BotaDownloadHandler.finished: verifyFile failed");
                BotaDownloadHandler.this.sm.failDownload(description.getVersion(), UpgradeUtils.DownloadStatus.STATUS_VERIFY, BotaDownloadHandler.this.settings.getString(Configs.UPGRADE_STATUS_VERIFY), ErrorCodeMapper.KEY_PACKAGE_VERIFICATION_FAILED);
                BotaDownloadHandler.this.settings.incrementPrefs(Configs.ADVANCE_DL_RETRY_COUNT);
                return;
            }
            BotaDownloadHandler.this.setProgressAndActualDownloadTime(false);
            if (BuildPropReader.isVerizon()) {
                UpgradeUtilMethods.sendDownloadCompletedToSettings(BotaDownloadHandler.this.ctx);
            }
            StatsHelper.setAndBuildDownloadStats(BotaDownloadHandler.this.settings, BotaDownloadHandler.this.ctx, connectivityManager);
            BotaDownloadHandler.this.env.getUtilities().sendInternalNotification(description.getVersion(), description.getRepository(), null);
            BotaDownloadHandler.this.settings.removeConfig(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
            BotaDownloadHandler.this.settings.removeConfig(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
            BotaDownloadHandler.this.settings.removeConfig(Configs.ADVANCE_DL_RETRY_COUNT);
            BotaDownloadHandler.this.unBindwithDownloadService();
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void exception(String str, String str2) {
            Logger.info("OtaApp", "BotaDownloadHandler.exception(). Download received exception :" + str2);
            BotaDownloadHandler.this.env.getUtilities().sendException(str2);
            BotaDownloadHandler.this.setProgressAndActualDownloadTime(false);
            if (!NetworkUtils.isNetWorkConnected(BotaDownloadHandler.cm)) {
                Logger.info("OtaApp", "BotaDownloadHandler.exception(). no network, return false");
            } else if (str2 != null) {
                BotaDownloadHandler.this.unBindwithDownloadService();
                if (str2.contains("ENOSPC")) {
                    Logger.info("OtaApp", "BotaDownloadHandler.exception:" + str2 + " failing the download for this exception");
                    UpgradeUtils.DownloadStatus downloadStatus = UpgradeUtils.DownloadStatus.STATUS_RESOURCES;
                    if (FileUtils.DATA.equals(BotaDownloadHandler.this.settings.getPackageDownloadLocation())) {
                        downloadStatus = UpgradeUtils.DownloadStatus.STATUS_SPACE;
                    }
                    BotaDownloadHandler.this.sm.failDownload(BotaDownloadHandler.version, downloadStatus, "ENOSPC:Does not have enough free space on /data " + BotaDownloadHandler.this.sm.getDeviceAdditionalInfo(), ErrorCodeMapper.KEY_DATA_OUT_OF_SPACE);
                } else if (str2.contains("IOException") && BotaDownloadHandler.ioExceptionHandler != null) {
                    if (ResetSMToGettingDescriptorExceptionHandler.isRetryPending()) {
                        Logger.error("OtaApp", "BotaDownloadHandler.exception(). retry pending, return");
                        BotaDownloadHandler.this.env.getUtilities().sendUpdateDownloadStatusRetried(BotaDownloadHandler.version, new File(BotaDownloadHandler.fileName).length(), BotaDownloadHandler.size);
                        return;
                    }
                    boolean handleException = BotaDownloadHandler.ioExceptionHandler.handleException(str2, str2 + " during BOTA package download", false, "DOWNLOAD_FAILED_PACKAGE_EXCEPTION.");
                    Logger.error("OtaApp", "BotaDownloadHandler.exception(). retry status " + handleException);
                    if (!handleException) {
                        BotaDownloadHandler.this.settings.incrementPrefs(Configs.ADVANCE_DL_RETRY_COUNT);
                    } else {
                        BotaDownloadHandler.this.env.getUtilities().sendUpdateDownloadStatusRetried(BotaDownloadHandler.version, new File(BotaDownloadHandler.fileName).length(), BotaDownloadHandler.size);
                    }
                } else {
                    String str3 = "Unhandled Exception found " + str2 + " failing the download for this exception";
                    Logger.info("OtaApp", str3);
                    BotaDownloadHandler.this.sm.failDownload(BotaDownloadHandler.version, UpgradeUtils.DownloadStatus.STATUS_FAIL, str3, "DOWNLOAD_FAILED_PACKAGE_EXCEPTION.");
                    BotaDownloadHandler.this.settings.incrementPrefs(Configs.ADVANCE_DL_RETRY_COUNT);
                }
            } else {
                BotaDownloadHandler.this.unBindwithDownloadService();
            }
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void errorCode(String str, int i, boolean z) {
            Logger.info("OtaApp", "BotaDownloadHandler.errorCode(). Download received errorCode :" + i);
            if (!z) {
                BotaDownloadHandler.this.setProgressAndActualDownloadTime(false);
            } else {
                BotaDownloadHandler.this.env.getUtilities().sendUpdateDownloadStatusRetried(BotaDownloadHandler.version, new File(BotaDownloadHandler.fileName).length(), BotaDownloadHandler.size);
            }
            BotaDownloadHandler.this.env.getUtilities().sendErrorCode(i);
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void suspended(String str, boolean z) {
            Logger.info("OtaApp", "BotaDownloadHandler.suspended()");
            BotaDownloadHandler.this.env.getUtilities().sendUpdateDownloadStatusSuspended(BotaDownloadHandler.version, new File(BotaDownloadHandler.fileName).length(), BotaDownloadHandler.size, BotaDownloadHandler.repo, BotaDownloadHandler.wifiOnly);
            BotaDownloadHandler.this.setProgressAndActualDownloadTime(false);
            BotaDownloadHandler.this.unBindwithDownloadService();
        }

        @Override // com.motorola.otalib.aidl.IDownloadServiceCallback
        public void initFailed(String str, String str2, String str3) {
            Logger.info("OtaApp", "BotaDownloadHandler.initFailed()");
            BotaDownloadHandler.this.sm.failDownload(BotaDownloadHandler.version, UpgradeUtils.DownloadStatus.STATUS_FAIL, str2, str3);
            BotaDownloadHandler.this.setProgressAndActualDownloadTime(false);
            BotaDownloadHandler.this.unBindwithDownloadService();
        }
    };
    private AtomicBoolean progress = new AtomicBoolean(false);
    private final BotaSettings settings;
    private final CusSM sm;

    public BotaDownloadHandler(Context context, CusSM cusSM, ApplicationEnv applicationEnv, BotaSettings botaSettings, boolean z) {
        this.ctx = context;
        this.sm = cusSM;
        this.env = applicationEnv;
        this.settings = botaSettings;
        this.isModemUpdate = z;
        errorCodeHandlers = new HashMap();
        cm = (ConnectivityManager) context.getSystemService("connectivity");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void callDownloadRequest() {
        String str;
        String string;
        String str2;
        String proxyHost;
        int proxyPort;
        try {
            Logger.debug("OtaApp", "callDownloadRequest() " + mConnection);
            mService.registerCallback(OTA_DOWNLOAD_ID, this.mCallback);
            if (this.isModemUpdate) {
                string = this.settings.getString(Configs.MODEM_DOWNLOAD_DESCRIPTOR);
            } else {
                string = this.settings.getString(Configs.DOWNLOAD_DESCRIPTOR);
            }
            str2 = string;
            proxyHost = getProxyHost(str2);
            proxyPort = getProxyPort(str2);
            str = "OtaApp";
        } catch (Exception e) {
            e = e;
            str = "OtaApp";
        }
        try {
            mService.downloadRequest(OTA_DOWNLOAD_ID, DownloadRequestBuilder.toJSONString(new DownloadRequest(str2, wifiOnly, time, size, fileName, proxyHost, proxyPort, this.settings.getString(Configs.DISALLOWED_NETS), this.settings.getString(Configs.BACKOFF_VALUES), this.settings.getInt(Configs.MAX_RETRY_COUNT_DL, 9), UpdaterUtils.getAdvancedDownloadFeature(), UpgradeSourceType.upgrade.toString(), this.settings.getBoolean(Configs.ALLOW_ON_ROAMING), 0L)));
        } catch (Exception e2) {
            e = e2;
            Logger.error(str, "callDownloadRequest(): Exception" + e);
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
            Logger.error("OtaApp", "exception while getting host name from descriptor " + e);
            string = this.settings.getString(Configs.DOWNLOAD_HTTP_PROXY_HOST);
        }
        Logger.debug("OtaApp", "HttpProxyFromDownloadDescriptor: Sync settings proxy host:" + string);
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
            Logger.error("OtaApp", "exception while getting port from descriptor" + e);
            i = this.settings.getInt(Configs.DOWNLOAD_HTTP_PROXY_PORT, -1);
        }
        Logger.debug("OtaApp", "HttpProxyFromDownloadDescriptor: Sync settings proxy port:" + i);
        return i;
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public synchronized void transferUpgrade(ApplicationEnv.Database.Descriptor descriptor) {
        stopTimer();
        MetaData meta = descriptor.getMeta();
        repo = descriptor.getRepository();
        time = descriptor.getTime();
        size = descriptor.getMeta().getSize();
        if (this.isModemUpdate) {
            wifiOnly = false;
            fileName = FileUtils.getModemDownloadFilePath();
        } else {
            wifiOnly = UpdaterUtils.isWifiOnly();
            fileName = FileUtils.getLocalPath(this.settings);
        }
        this._dlHelper = new DownloadHelper(descriptor.getVersion(), fileName, descriptor.getMeta().getSize());
        version = descriptor.getVersion();
        ioExceptionHandler = new ResetSMToGettingDescriptorExceptionHandler(this.ctx, version, this.settings, this.sm);
        File file = new File(fileName);
        errorCodeHandlers.put(400, new ResetSMToGettingDescriptor(this.ctx, 400, "400 Bad Request", version, file, false, this.settings, this.sm));
        errorCodeHandlers.put(401, new ResetSMToGettingDescriptor(this.ctx, 401, "401 Unauthorized", version, file, false, this.settings, this.sm));
        errorCodeHandlers.put(Integer.valueOf((int) SystemUpdateStatusUtils.ALERT_FAILED_FIRMWARE_UPDATE_PACKAGE_CERT_VALIDATE), new ResetSMToGettingDescriptor(this.ctx, SystemUpdateStatusUtils.ALERT_FAILED_FIRMWARE_UPDATE_PACKAGE_CERT_VALIDATE, "404 Not Found", version, file, true, this.settings, this.sm));
        errorCodeHandlers.put(403, new ResetSMToGettingDescriptor(this.ctx, 403, "403 Forbidden", version, file, false, this.settings, this.sm));
        errorCodeHandlers.put(410, new ResetSMToGettingDescriptor(this.ctx, 410, "410 Gone", version, file, true, this.settings, this.sm));
        errorCodeHandlers.put(412, new ResetSMToGettingDescriptor(this.ctx, 412, "412 Precondition failed", version, file, true, this.settings, this.sm));
        errorCodeHandlers.put(Integer.valueOf((int) HTTP_TOO_MANY_REQUESTS), new ResetSMToGettingDescriptor(this.ctx, HTTP_TOO_MANY_REQUESTS, "429 Too many requests", version, file, true, this.settings, this.sm));
        Logger.debug("OtaApp", "OTAUpgradeSource.transferUpgrade: Version " + descriptor.getVersion() + " a file of size " + meta.getSize());
        ConnectivityManager connectivityManager = (ConnectivityManager) this.ctx.getSystemService("connectivity");
        if (!this.isModemUpdate && this.settings.getBoolean(Configs.BATTERY_LOW)) {
            Logger.debug("OtaApp", "In BotaDownloadHandler.transferupgrade,Low Battery : sending suspended intent to UI");
            this.env.getUtilities().sendUpdateDownloadStatusSuspended(descriptor.getVersion(), new File(FileUtils.getLocalPath(this.settings)).length(), descriptor.getMeta().getSize(), descriptor.getRepository(), wifiOnly);
            return;
        }
        if (wifiOnly) {
            if (BuildPropReader.isBotaATT()) {
                this.env.getUtilities().startFotaWifiDiscoveryTimer();
            }
            if (NetworkUtils.isWifi(connectivityManager)) {
                this.env.getUtilities().cleanBotaWifiDiscoveryTimer();
            } else {
                if (!BuildPropReader.isATT()) {
                    this.env.getUtilities().startBotaWifiDiscoveryTimer();
                }
                Logger.debug("OtaApp", "In BotaDownloadHandler.transferupgrade,sending suspended intent to UI");
                this.env.getUtilities().sendUpdateDownloadStatusSuspended(descriptor.getVersion(), new File(FileUtils.getLocalPath(this.settings)).length(), descriptor.getMeta().getSize(), descriptor.getRepository(), wifiOnly);
                return;
            }
        }
        if (CusAndroidUtils.isDeviceInDatasaverMode()) {
            Logger.debug("OtaApp", "In BotaDownloadHandler.transferupgrade,Datasaver is ON : sending suspended intent to UI");
            this.env.getUtilities().sendUpdateDownloadStatusSuspended(descriptor.getVersion(), new File(FileUtils.getLocalPath(this.settings)).length(), descriptor.getMeta().getSize(), descriptor.getRepository(), wifiOnly);
            return;
        }
        boolean z = !TextUtils.isEmpty(InstallerUtilMethods.getAdminApnUrl(this.settings));
        if ((descriptor.getMeta().getForceDownloadTime() >= 0.0d || SmartUpdateUtils.isDownloadForcedForSmartUpdate(this.settings)) && !z && !UpdaterUtils.getAutomaticDownloadForCellular() && !NetworkUtils.isWifi(connectivityManager)) {
            Logger.debug("OtaApp", "In BotaDownloadHandler.transferupgrade,forced update, but not on wifi : sending suspended intent to UI");
            this.env.getUtilities().sendUpdateDownloadStatusSuspended(descriptor.getVersion(), new File(FileUtils.getLocalPath(this.settings)).length(), descriptor.getMeta().getSize(), descriptor.getRepository(), wifiOnly);
        } else if (CusAndroidUtils.checkForUrlExpiry(this.settings, this.isModemUpdate)) {
            Logger.debug("OtaApp", "In BotaDownloadHandler.transferupgrade,oops url expired fetching new url");
            if (this.isModemUpdate) {
                this.settings.setString(Configs.MODEM_GET_DESCRIPTOR_REASON, "URL timeout");
            } else {
                this.settings.setString(Configs.OTA_GET_DESCRIPTOR_REASON, "URL timeout");
            }
            CusAndroidUtils.sendGetDescriptor(this.ctx, version, "encountered url timeout go and fetch new download url", false);
            this.env.getUtilities().sendUpdateDownloadStatusRetried(version, new File(fileName).length(), size);
            decreaseRetryCount();
            if (ResetSMToGettingDescriptor.isRetryPending()) {
                ResetSMToGettingDescriptor.clearRetryTask();
            }
            if (ResetSMToGettingDescriptorExceptionHandler.isRetryPending()) {
                ResetSMToGettingDescriptorExceptionHandler.clearRetryTask();
            }
        } else {
            UpdaterUtils.setSoftBankProxyData(OtaApplication.getGlobalContext());
            if (mService == null) {
                mConnection = new ServiceConnection() { // from class: com.motorola.ccc.ota.sources.bota.BotaDownloadHandler.2
                    @Override // android.content.ServiceConnection
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        Logger.debug("OtaApp", "onServiceConnected() " + iBinder);
                        BotaDownloadHandler.mService = IDownloadService.Stub.asInterface(iBinder);
                        if (BotaDownloadHandler.mService != null) {
                            BotaDownloadHandler.this.callDownloadRequest();
                        }
                    }

                    @Override // android.content.ServiceConnection
                    public void onServiceDisconnected(ComponentName componentName) {
                        try {
                            if (BotaDownloadHandler.mService != null) {
                                BotaDownloadHandler.mService.unregisterCallback(BotaDownloadHandler.OTA_DOWNLOAD_ID);
                            }
                            Logger.debug("OtaApp", "onServiceDisconnected(): Unregistered for remote service callbacks");
                        } catch (RemoteException e) {
                            Logger.error("OtaApp", "onServiceDisconnected(): Error un-registering our callback with dl service: " + e);
                        }
                        BotaDownloadHandler.this.setProgressAndActualDownloadTime(false);
                        BotaDownloadHandler.mService = null;
                    }
                };
                Logger.info("OtaApp", "mConnection in transferupgrade: " + mConnection);
                this.ctx.bindService(new Intent(this.ctx, DownloadService.class), mConnection, 1);
            } else {
                callDownloadRequest();
            }
            setProgressAndActualDownloadTime(true);
            this.env.getUtilities().sendUpdateDownloadStatusProgress(this._dlHelper.version(), this._dlHelper.size(), this._dlHelper.expected(), descriptor.getRepository());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean verifyFile(String str) {
        try {
            this.env.getUtilities().verifyPackage(new File(str));
            Logger.info("OtaApp", "BotaDownloadHandler.verifyFile, verification of update package successful");
            return true;
        } catch (Exception e) {
            this.settings.setString(Configs.UPGRADE_STATUS_VERIFY, UpgradeUtilMethods.getStatusVerifyResult(e.toString()));
            Logger.error("OtaApp", "BotaDownloadHandler.verifyFile failed: " + UpgradeUtilMethods.getStatusVerifyResult(e.toString()));
            return false;
        }
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public synchronized void radioGotDown() {
        String deviceSha1;
        unBindwithDownloadService();
        startTimer();
        if (this.isModemUpdate) {
            deviceSha1 = BuildPropReader.getDeviceModemConfigVersionSha1();
        } else {
            deviceSha1 = this.env.getServices().getDeviceSha1();
        }
        ApplicationEnv.Database.Descriptor description = this.sm.getDescription(deviceSha1);
        if (description == null) {
            return;
        }
        if (description.getRepository().equals(UpgradeSourceType.upgrade.toString()) && new File(FileUtils.getLocalPath(this.settings)).length() < description.getMeta().getSize()) {
            Logger.debug("OtaApp", "In BotaDownloadHandler.radioGotDown, sending suspended intent to UI");
            this.env.getUtilities().sendUpdateDownloadStatusSuspended(description.getVersion(), new File(FileUtils.getLocalPath(this.settings)).length(), description.getMeta().getSize(), description.getRepository(), UpdaterUtils.isWifiOnly());
        }
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public boolean isBusy() {
        Logger.info("OtaApp", "Current download status:" + this.progress);
        return this.progress.get();
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void close() {
        Logger.info("OtaApp", "BotaDownloadHandler.close, with current dl status : " + this.progress);
        setProgressAndActualDownloadTime(false);
        unBindwithDownloadService();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unBindwithDownloadService() {
        Logger.info("OtaApp", "BotaDownloadHandler.unBindwithDownloadService, with mService:" + mService + " and mConnection: " + mConnection);
        IDownloadService iDownloadService = mService;
        if (iDownloadService == null || mConnection == null) {
            return;
        }
        try {
            iDownloadService.unregisterCallback(OTA_DOWNLOAD_ID);
            this.ctx.unbindService(mConnection);
        } catch (Exception e) {
            Logger.info("OtaApp", "BotaDownloadHandler.unBindwithDownloadService, exception " + e);
        }
        mService = null;
        mConnection = null;
    }

    private int startTimer() {
        synchronized (this) {
            try {
                try {
                    if (mTimer == null) {
                        defaultAction defaultaction = new defaultAction();
                        Timer timer = new Timer();
                        mTimer = timer;
                        timer.schedule(defaultaction, 3000L);
                    } else {
                        Logger.debug("OtaApp", "BotaDownloadHandler.defaultAction have been scheduled, do nothing");
                    }
                } catch (IllegalStateException e) {
                    Logger.error("OtaApp", "BotaDownloadHandler.startTimer, IllegalStateException, Maybe canceled. Ignore it" + e);
                }
            } catch (IllegalArgumentException e2) {
                Logger.error("OtaApp", "BotaDownloadHandler.startTimer, IllegalArgumentException, ignore it." + e2);
            }
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void stopTimer() {
        synchronized (this) {
            if (mTimer != null) {
                Logger.debug("OtaApp", "BotaDownloadHandler.stopTimer, cancel()");
                mTimer.cancel();
                mTimer = null;
            } else {
                Logger.debug("OtaApp", "BotaDownloadHandler.stopTimer, have stopped, do nothing");
            }
        }
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void onDeviceShutdown() {
        setProgressAndActualDownloadTime(false);
        decreaseRetryCount();
    }

    private void decreaseRetryCount() {
        if (ResetSMToGettingDescriptor.isRetryPending() && this.settings.getInt(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS, 0) > 0) {
            this.settings.decrementPrefs(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
        }
        if (!ResetSMToGettingDescriptorExceptionHandler.isRetryPending() || this.settings.getInt(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS, 0) <= 0) {
            return;
        }
        this.settings.decrementPrefs(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
    }

    public void setProgressAndActualDownloadTime(boolean z) {
        if (z) {
            this.settings.setLong(Configs.STATS_DL_LAST_DOWNLOAD_START_TIME, System.currentTimeMillis());
            this.progress.set(z);
        } else if (this.progress.compareAndSet(true, false)) {
            long j = this.settings.getLong(Configs.STATS_DL_ACTUAL_DOWNLOAD_TIME, 0L);
            this.settings.setLong(Configs.STATS_DL_ACTUAL_DOWNLOAD_TIME, j + (System.currentTimeMillis() - this.settings.getLong(Configs.STATS_DL_LAST_DOWNLOAD_START_TIME, 0L)));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class defaultAction extends TimerTask {
        public defaultAction() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            BotaDownloadHandler.this.stopTimer();
            BotaDownloadHandler.this.setProgressAndActualDownloadTime(false);
            if (NetworkUtils.isNetWorkConnected(BotaDownloadHandler.cm)) {
                OtaApplication.getGlobalContext().sendBroadcast(new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE), Permissions.INTERACT_OTA_SERVICE);
            }
        }
    }
}
