package com.motorola.otalib.downloadservice;

import android.accounts.Account;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.otalib.aidl.IDownloadService;
import com.motorola.otalib.aidl.IDownloadServiceCallback;
import com.motorola.otalib.common.backoff.IncrementalBackoffValueProvider;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import com.motorola.otalib.downloadservice.dataobjects.DownloadRequest;
import com.motorola.otalib.downloadservice.dataobjects.DownloadRequestBuilder;
import com.motorola.otalib.downloadservice.download.DownloadProcessor;
import com.motorola.otalib.downloadservice.download.HttpFileDownloadException;
import com.motorola.otalib.downloadservice.download.HttpUrlBuilder;
import com.motorola.otalib.downloadservice.download.PackageDownloader;
import com.motorola.otalib.downloadservice.download.policy.DownloadPolicy;
import com.motorola.otalib.downloadservice.download.policy.ZeroRatedManager;
import com.motorola.otalib.downloadservice.download.policy.ZeroRatedServices;
import com.motorola.otalib.downloadservice.utils.DownloadServiceLogger;
import com.motorola.otalib.downloadservice.utils.DownloadServiceSettings;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Random;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.HttpHost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DownloadService extends Service {
    public static final int ACTIVITY_RESULT_GET_AUTH_TOKEN_CODE = 2;
    public static final int MAX_RETRY_COUNT = 2;
    private static final String NO_NETWORK = "no_network";
    private static IDownloadServiceCallback clientCallBack;
    private static Context context;
    private static PackageDownloader.FileDownloader downloader;
    private static ShutDownActionsBroadcastReceiver mShutdownReceiver;
    private static int retryCount;
    private static PowerManager.WakeLock wl;
    private String downloadId;
    private ConnectivityManager mCm;
    private DownloadServiceSettings mDownloadServiceSettings;
    private Account[] mGoogleAccounts;
    private TelephonyManager mTm;
    private DownloadPolicy od;
    private Random randomNumber;
    private SparseArray<Object> requestResponseMapping;
    private ServiceHandler serviceHandler;
    private Looper serviceLooper;
    private ZeroRatedServices zs;
    private ZeroRatedActionsBroadcastReceiver _mZeroRatedActionsListener = null;
    private final IDownloadService.Stub mBinder = new IDownloadService.Stub() { // from class: com.motorola.otalib.downloadservice.DownloadService.3
        @Override // com.motorola.otalib.aidl.IDownloadService
        public boolean registerCallback(String str, IDownloadServiceCallback iDownloadServiceCallback) {
            IDownloadServiceCallback unused = DownloadService.clientCallBack = iDownloadServiceCallback;
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "registerCallback : " + DownloadService.clientCallBack);
            return true;
        }

        @Override // com.motorola.otalib.aidl.IDownloadService
        public void unregisterCallback(String str) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "unregisterCallback : " + DownloadService.clientCallBack);
            IDownloadServiceCallback unused = DownloadService.clientCallBack = null;
        }

        @Override // com.motorola.otalib.aidl.IDownloadService
        public boolean downloadRequest(String str, String str2) {
            DownloadServiceLogger.v(DownloadServiceLogger.TAG, "DownloadService : downloadRequest - " + str2);
            DownloadService.this.downloadId = str;
            DownloadService.this.mDownloadServiceSettings.setConfig(DownloadServiceSettings.KEY_ID, str);
            DownloadService.this.mDownloadServiceSettings.setConfig(DownloadServiceSettings.KEY_DOWNLOAD_REQUEST, str2);
            DownloadService.this.mDownloadServiceSettings.setConfig(DownloadServiceSettings.KEY_DO_NOT_BIND_OTA_PROCESS, String.valueOf(false));
            DownloadService.this.postDownloadRequest(DownloadRequestBuilder.from(str2));
            return true;
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum WHAT {
        DOWNLOAD_SERVICE_REQUEST,
        STOP_DOWNLOAD_SERVICE
    }

    static /* synthetic */ int access$004() {
        int i = retryCount + 1;
        retryCount = i;
        return i;
    }

    @Override // android.app.Service
    public void onCreate() {
        context = getApplicationContext();
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Creating download service");
        this.randomNumber = new Random();
        this.requestResponseMapping = new SparseArray<>();
        HandlerThread handlerThread = new HandlerThread("DownloadService.ServiceHandlerThread");
        handlerThread.start();
        this.serviceLooper = handlerThread.getLooper();
        this.serviceHandler = new ServiceHandler(this.serviceLooper);
        this.mCm = (ConnectivityManager) context.getSystemService("connectivity");
        this.mTm = (TelephonyManager) context.getSystemService("phone");
        DownloadServiceSettings downloadServiceSettings = new DownloadServiceSettings(getSharedPreferences(DownloadServiceSettings.KEY_PREFS_NAME, 0));
        this.mDownloadServiceSettings = downloadServiceSettings;
        this.zs = ZeroRatedServices.getZeroRatedServices(context, downloadServiceSettings);
        this.od = new DownloadPolicy(this.mCm, this.mTm, this.zs);
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Bound to download service " + this.mBinder);
        return this.mBinder;
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Unbound from download service");
        clientCallBack = null;
        return true;
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Starting download service");
        return 2;
    }

    @Override // android.app.Service
    public void onDestroy() {
        DownloadServiceLogger.i(DownloadServiceLogger.TAG, "DownloadService.onDestroy(), Stopping download service");
        retryCount = 0;
        this.mDownloadServiceSettings.clearConfigValue(DownloadServiceSettings.KEY_DO_NOT_BIND_OTA_PROCESS);
        unregisterShutdownActions();
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService.onDestroy(), Stopping admin apn");
        if (!TextUtils.isEmpty(UpgradeUtils.getAdminApnUrl(DownloadRequestBuilder.from(this.mDownloadServiceSettings.getConfigValue(DownloadServiceSettings.KEY_DOWNLOAD_REQUEST)).getContentResource()))) {
            this.od.stopUsingZeroRatedChannel();
            unregisterZeroRatedActionsReceiver();
        }
        stopDownloadService();
        this.serviceLooper.quitSafely();
        try {
            PowerManager.WakeLock wakeLock = wl;
            if (wakeLock == null || !wakeLock.isHeld()) {
                return;
            }
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Releasing wake lock");
            wl.release();
            wl = null;
        } catch (Exception e) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "DownloadService.onDestroy(),Exception Releasing wake lock" + e);
        }
    }

    public static Context getDSContext() {
        return context;
    }

    private void addProcessor(PackageDownloader.FileDownloader fileDownloader, final DownloadRequest downloadRequest) {
        fileDownloader.addProcessor(new DownloadProcessor() { // from class: com.motorola.otalib.downloadservice.DownloadService.1
            @Override // com.motorola.otalib.downloadservice.download.DownloadProcessor
            public void progress(byte[] bArr) {
                try {
                    int unused = DownloadService.retryCount = 0;
                    DownloadService.clientCallBack.progress(DownloadService.this.downloadId);
                } catch (Exception e) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "progress : exception " + e);
                }
            }

            @Override // com.motorola.otalib.downloadservice.download.DownloadProcessor
            public void failed(HttpFileDownloadException httpFileDownloadException) {
                String downloadUrl = DownloadService.this.getDownloadUrl(downloadRequest.getContentResource());
                try {
                } catch (Exception e) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "failed : exception" + e);
                }
                if (downloadUrl == null) {
                    DownloadService.clientCallBack.initFailed(DownloadService.this.downloadId, "Aborting the OTA process as downloarUrl is null", ErrorCodeMapper.KEY_DOWNLOAD_URL_NULL);
                    return;
                }
                if (downloadUrl.equals(DownloadService.NO_NETWORK)) {
                    DownloadService.clientCallBack.suspended(DownloadService.this.downloadId, true);
                    return;
                }
                int unused = DownloadService.retryCount = 0;
                try {
                    if (!TextUtils.isEmpty(UpgradeUtils.getAdminApnUrl(downloadRequest.getContentResource()))) {
                        DownloadService.this.od.stopUsingZeroRatedChannel();
                        DownloadService.this.unregisterZeroRatedActionsReceiver();
                    }
                    DownloadServiceLogger.d(DownloadServiceLogger.TAG, "failed in service : " + httpFileDownloadException.getDownloaderStatus() + SystemUpdateStatusUtils.FIELD_SEPERATOR + httpFileDownloadException.getStatus());
                } catch (Exception e2) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "failed : exception " + e2);
                }
                try {
                    DownloadService.clientCallBack.failed(DownloadService.this.downloadId, httpFileDownloadException.getStatus(), httpFileDownloadException.toString(), httpFileDownloadException.getSpErrorCode());
                } catch (Exception e3) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "failed : exception at callback " + e3);
                }
            }

            @Override // com.motorola.otalib.downloadservice.download.DownloadProcessor
            public void finished() {
                try {
                    if (!TextUtils.isEmpty(UpgradeUtils.getAdminApnUrl(downloadRequest.getContentResource()))) {
                        DownloadService.this.od.stopUsingZeroRatedChannel();
                        DownloadService.this.unregisterZeroRatedActionsReceiver();
                    }
                } catch (Exception e) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "finished : exception : " + e.toString());
                }
                try {
                    DownloadService.clientCallBack.finished(DownloadService.this.downloadId);
                } catch (Exception e2) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "finished : exception at callback " + e2);
                }
            }

            /* JADX WARN: Multi-variable type inference failed */
            /* JADX WARN: Type inference failed for: r2v1 */
            /* JADX WARN: Type inference failed for: r2v10 */
            /* JADX WARN: Type inference failed for: r2v7, types: [java.lang.String] */
            /* JADX WARN: Type inference failed for: r2v9 */
            /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:8:0x0019 -> B:14:0x0021). Please submit an issue!!! */
            @Override // com.motorola.otalib.downloadservice.download.DownloadProcessor
            public void exception(Exception exc) {
                String str;
                ?? r2;
                try {
                    if (exc instanceof IOException) {
                        str = "IOException : " + exc;
                        this = this;
                    } else {
                        str = exc.toString();
                        this = this;
                    }
                } catch (Exception unused) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "exception");
                    str = null;
                    r2 = this;
                }
                try {
                    IDownloadServiceCallback iDownloadServiceCallback = DownloadService.clientCallBack;
                    this = DownloadService.this.downloadId;
                    iDownloadServiceCallback.exception(this, str);
                } catch (Exception e) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "exception : exception at callback " + e);
                }
            }

            @Override // com.motorola.otalib.downloadservice.download.DownloadProcessor
            public void errorCode(int i, boolean z) {
                try {
                    DownloadServiceLogger.i(DownloadServiceLogger.TAG, "clientCallBack.errorCode : " + i);
                    if (!TextUtils.isEmpty(UpgradeUtils.getAdminApnUrl(downloadRequest.getContentResource()))) {
                        DownloadService.this.od.stopUsingZeroRatedChannel();
                        DownloadService.this.unregisterZeroRatedActionsReceiver();
                    }
                    String backOffValues = downloadRequest.getBackOffValues();
                    IncrementalBackoffValueProvider incrementalBackoffValueProvider = !TextUtils.isEmpty(backOffValues) ? new IncrementalBackoffValueProvider(backOffValues) : null;
                    if (z) {
                        if (DownloadService.access$004() > downloadRequest.getMaxRetryCount()) {
                            String str = "DownloadService.errorCode, retries for errorcode " + i + " maxed out retry count (attempted) " + DownloadService.retryCount + " so giving up";
                            DownloadServiceLogger.i(DownloadServiceLogger.TAG, str);
                            DownloadService.clientCallBack.initFailed(DownloadService.this.downloadId, str, ErrorCodeMapper.KEY_DOWNLOAD_FAILED_5XX);
                        } else {
                            long j = 0;
                            for (int i2 = 0; i2 < DownloadService.retryCount; i2++) {
                                j = incrementalBackoffValueProvider.getNextTimeoutValue();
                            }
                            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService.errorCode, retryCount " + DownloadService.retryCount + " backoffValue : " + j);
                            DownloadService.this.serviceHandler.postDelayed(new Runnable() { // from class: com.motorola.otalib.downloadservice.DownloadService.1.1
                                @Override // java.lang.Runnable
                                public void run() {
                                    if (NetworkUtils.hasNetwork(DownloadService.this.mCm)) {
                                        DownloadService.this.postDownloadRequest(downloadRequest);
                                    } else {
                                        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService.errorCode, no need to retry as there is no network");
                                    }
                                }
                            }, j);
                        }
                    }
                } catch (Exception e) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "clientCallBack.errorCode : exception" + e);
                    z = false;
                }
                try {
                    DownloadService.clientCallBack.errorCode(DownloadService.this.downloadId, i, z);
                } catch (Exception e2) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "clientCallBack.errorCode : exception at callback " + e2);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleDownloadServiceRequest(final DownloadRequest downloadRequest) {
        try {
            String adminApnUrl = UpgradeUtils.getAdminApnUrl(downloadRequest.getContentResource());
            if (UpgradeUtils.checkIfAlreadyhavePermission(context, "android.permission.READ_PHONE_STATE") || UpgradeUtils.checkIfAlreadyhavePermission(context, "android.permission.READ_PRIVILEGED_PHONE_STATE")) {
                if (!this.od.canIUseZeroRatedNetwork(context, downloadRequest.getWifiOnly(), adminApnUrl, downloadRequest.getDisallowedNetworks(), downloadRequest.getContentResource())) {
                    if (!TextUtils.isEmpty(adminApnUrl)) {
                        this.od.stopUsingZeroRatedChannel();
                        unregisterZeroRatedActionsReceiver();
                    }
                    DownloadPolicy.DownloadingChoices canIDownload = this.od.canIDownload(context, adminApnUrl, downloadRequest.getWifiOnly(), downloadRequest.getDisallowedNetworks(), downloadRequest.getAllowOnRoaming());
                    DownloadServiceLogger.d(DownloadServiceLogger.TAG, "handleDownloadServiceRequest: downloadingChoice " + canIDownload);
                    if (canIDownload != DownloadPolicy.DownloadingChoices.WIFI_OK && canIDownload != DownloadPolicy.DownloadingChoices.WAN_OK) {
                        clientCallBack.suspended(this.downloadId, true);
                        return;
                    }
                } else if (this.od.canIDownloadUsingZeroRatedChannel()) {
                    DownloadServiceLogger.d(DownloadServiceLogger.TAG, "handleDownloadServiceRequest: can be downloaded now; reason zero rated NW is available");
                } else {
                    DownloadServiceLogger.d(DownloadServiceLogger.TAG, "handleDownloadServiceRequest: cannot be downloaded now; reason zero rated NW is not available");
                    registerForZeroRatedActions();
                    this.od.startZeroRatedProcess();
                    return;
                }
            }
            if (downloadRequest.getHostName() != null && downloadRequest.getHostName().length() > 0 && downloadRequest.getPort() > -1) {
                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "proxy host:" + downloadRequest.getHostName() + ", port:" + downloadRequest.getPort());
                new HttpHost(downloadRequest.getHostName(), downloadRequest.getPort());
            }
            String backOffValues = downloadRequest.getBackOffValues();
            IncrementalBackoffValueProvider incrementalBackoffValueProvider = !TextUtils.isEmpty(backOffValues) ? new IncrementalBackoffValueProvider(backOffValues) : null;
            final String downloadUrl = getDownloadUrl(downloadRequest.getContentResource());
            if (downloadUrl == null) {
                clientCallBack.initFailed(this.downloadId, "Aborting the OTA process as downloarUrl is null", ErrorCodeMapper.KEY_DOWNLOAD_URL_NULL);
            } else if (downloadUrl.equals(NO_NETWORK)) {
                clientCallBack.suspended(this.downloadId, true);
            } else {
                DownloadServiceLogger.v(DownloadServiceLogger.TAG, "handleDownloadServiceRequest:downloadUrl " + downloadUrl + " size " + downloadRequest.getSize());
                try {
                    PackageDownloader.FileDownloader fileDownloader = new PackageDownloader().getFileDownloader(incrementalBackoffValueProvider, new HttpUrlBuilder().openConnection(downloadRequest.getHostName(), downloadRequest.getPort(), new URL(Uri.parse(downloadUrl).toString()), DownloadServiceSettings.DOWNLOAD_SOCKET_TIMEOUT, DownloadServiceSettings.DOWNLOAD_SOCKET_TIMEOUT, getSSLSocketFactory()));
                    downloader = fileDownloader;
                    addProcessor(fileDownloader, downloadRequest);
                    if (!NetworkUtils.hasNetwork(this.mCm)) {
                        clientCallBack.suspended(this.downloadId, true);
                        return;
                    }
                    JSONObject downloadHeaders = getDownloadHeaders(downloadRequest.getContentResource());
                    if (downloadHeaders != null) {
                        try {
                            if (downloadHeaders.length() > 0) {
                                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "headers: " + downloadHeaders + " headers.length: " + downloadHeaders.length());
                                JSONArray names = downloadHeaders.names();
                                for (int i = 0; i < names.length(); i++) {
                                    String string = names.getString(i);
                                    String string2 = downloadHeaders.getString(string);
                                    DownloadServiceLogger.i(DownloadServiceLogger.TAG, "Adding header " + string + SmartUpdateUtils.MASK_SEPARATOR + string2);
                                    downloader.addHeader(string, string2);
                                }
                            }
                        } catch (JSONException e) {
                            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "Caught exception while parsing headers" + e.getMessage());
                        }
                    }
                    wl = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "Ota:HttpFileDownloader");
                    registerShutdownActions();
                    final File file = new File(downloadRequest.getFileName());
                    Thread thread = new Thread(new Runnable() { // from class: com.motorola.otalib.downloadservice.DownloadService.2
                        @Override // java.lang.Runnable
                        public void run() {
                            try {
                                DownloadService.downloader.downloadFile(downloadRequest.getHostName(), downloadRequest.getPort(), downloadUrl, file, downloadRequest.getSize(), DownloadService.wl, downloadRequest.getUpgradeSourceType(), downloadRequest.getStartingOffset());
                            } catch (HttpFileDownloadException unused) {
                                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "HttpFileDownloadException - nothing to be done");
                            } catch (Exception e2) {
                                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "Caught exception while downloading file" + e2.getMessage());
                                try {
                                    if (DownloadService.clientCallBack != null) {
                                        DownloadService.clientCallBack.initFailed(DownloadService.this.downloadId, "Caught exception" + e2.getMessage(), ErrorCodeMapper.KEY_PARSE_ERROR);
                                    }
                                } catch (RemoteException e3) {
                                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "Caught exception while callback" + e3.getMessage());
                                }
                            }
                        }
                    });
                    thread.setName("DownloadFileThread");
                    thread.start();
                } catch (Exception e2) {
                    clientCallBack.initFailed(this.downloadId, "Caught exception while building client object " + e2, ErrorCodeMapper.KEY_PARSE_ERROR);
                }
            }
        } catch (Exception e3) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "Caught exception while handling download request " + e3.getMessage());
            try {
                clientCallBack.initFailed(this.downloadId, "Caught exception while building client object " + e3.getMessage(), ErrorCodeMapper.KEY_PARSE_ERROR);
            } catch (Exception e4) {
                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "Caught exception while callback" + e4.getMessage());
            }
        }
    }

    private boolean addAuthorizationHeader(String str) {
        try {
            URI uri = new URI(str);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme.equals("https")) {
                return host.equals("storage.googleapis.com");
            }
            return false;
        } catch (Exception e) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "addAuthorizationHeader,Caught exception" + e);
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "handleMessage :" + WHAT.values()[message.what]);
            int i = AnonymousClass4.$SwitchMap$com$motorola$otalib$downloadservice$DownloadService$WHAT[WHAT.values()[message.what].ordinal()];
            if (i != 1) {
                if (i == 2 && DownloadService.downloader != null) {
                    DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService.onDestroy(), shutting down HttpFileDownloader");
                    DownloadService.downloader.shutdown();
                    return;
                }
                return;
            }
            DownloadRequest downloadRequest = (DownloadRequest) message.obj;
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService : DOWNLOAD_SERVICE_REQUEST");
            DownloadService.this.handleDownloadServiceRequest(downloadRequest);
            DownloadService.this.requestResponseMapping.put(Integer.valueOf(DownloadService.this.randomNumber.nextInt()).intValue(), downloadRequest);
        }
    }

    /* renamed from: com.motorola.otalib.downloadservice.DownloadService$4  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$downloadservice$DownloadService$WHAT;

        static {
            int[] iArr = new int[WHAT.values().length];
            $SwitchMap$com$motorola$otalib$downloadservice$DownloadService$WHAT = iArr;
            try {
                iArr[WHAT.DOWNLOAD_SERVICE_REQUEST.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$otalib$downloadservice$DownloadService$WHAT[WHAT.STOP_DOWNLOAD_SERVICE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    private void registerShutdownActions() {
        if (mShutdownReceiver == null) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService.registerShutdownActions,registering for shut down actions");
            mShutdownReceiver = new ShutDownActionsBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
            intentFilter.addAction("android.intent.action.REBOOT");
            context.registerReceiver(mShutdownReceiver, intentFilter, 2);
            return;
        }
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService.registerShutdownActions,already registered for shut down actions");
    }

    private void unregisterShutdownActions() {
        if (mShutdownReceiver != null) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService.unregisterShutdownActions,unregistering from listening to shut down actions");
            context.unregisterReceiver(mShutdownReceiver);
            mShutdownReceiver = null;
            return;
        }
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService.unregisterShutdownActions,not registered to shut down actions");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getDownloadUrl(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            String optString = jSONObject.optString("wifiUrl");
            String optString2 = jSONObject.optString("cellUrl");
            String optString3 = jSONObject.optString("adminApnUrl");
            if (TextUtils.isEmpty(optString) && TextUtils.isEmpty(optString2) && TextUtils.isEmpty(optString3)) {
                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "DownloadService:getDownloadUrl, no url to proceed");
                return null;
            } else if (NetworkUtils.isWifi(this.mCm)) {
                return optString;
            } else {
                if (NetworkUtils.isWan(this.mCm)) {
                    return optString2;
                }
                if (NetworkUtils.isAdminApn(this.mCm)) {
                    return optString3;
                }
                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "DownloadService:getDownloadUrl, Not connected");
                return NO_NETWORK;
            }
        } catch (Exception e) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "DownloadService:getDownloadUrl, Exception occured " + e);
            return null;
        }
    }

    private JSONObject getDownloadHeaders(String str) {
        JSONObject optJSONObject;
        try {
            JSONObject jSONObject = new JSONObject(str);
            if (NetworkUtils.isWifi(this.mCm)) {
                optJSONObject = jSONObject.optJSONObject("wifiHeaders");
            } else if (NetworkUtils.isWan(this.mCm)) {
                optJSONObject = jSONObject.optJSONObject("cellHeaders");
            } else if (!NetworkUtils.isAdminApn(this.mCm)) {
                return null;
            } else {
                optJSONObject = jSONObject.optJSONObject("adminApnHeaders");
            }
            return optJSONObject;
        } catch (Exception e) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "DownloadService:getDownloadHeaders, Exception occured " + e);
            return null;
        }
    }

    synchronized void postDownloadRequest(DownloadRequest downloadRequest) {
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "appending download service request to serviceHandler");
        Message obtainMessage = this.serviceHandler.obtainMessage();
        obtainMessage.what = WHAT.DOWNLOAD_SERVICE_REQUEST.ordinal();
        obtainMessage.obj = downloadRequest;
        this.serviceHandler.sendMessage(obtainMessage);
    }

    void stopDownloadService() {
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "stopDownloadService()");
        this.serviceHandler.sendEmptyMessage(WHAT.STOP_DOWNLOAD_SERVICE.ordinal());
    }

    private SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sSLContext = SSLContext.getInstance("TLS");
            sSLContext.init(null, getTrustManagerFactory().getTrustManagers(), null);
            return sSLContext.getSocketFactory();
        } catch (KeyManagementException e) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "Error creating connection:" + e);
            return null;
        } catch (NoSuchAlgorithmException e2) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "Error creating connection:" + e2);
            return null;
        }
    }

    private TrustManagerFactory getTrustManagerFactory() {
        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore());
            return trustManagerFactory;
        } catch (KeyStoreException e) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "Error creating connection:" + e);
            return trustManagerFactory;
        } catch (NoSuchAlgorithmException e2) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "Error creating connection:" + e2);
            return trustManagerFactory;
        }
    }

    private KeyStore keyStore() {
        KeyStore keyStore;
        CertificateException e;
        NoSuchAlgorithmException e2;
        KeyStoreException e3;
        IOException e4;
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "loading default keystore. keystorename=AndroidCAStore");
        try {
            keyStore = KeyStore.getInstance("AndroidCAStore");
            try {
                keyStore.load(null, null);
            } catch (IOException e5) {
                e4 = e5;
                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "IOException creating SSL socket factory. keyStoreName=AndroidCAStore" + e4);
                return keyStore;
            } catch (KeyStoreException e6) {
                e3 = e6;
                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "KeyStoreException creating SSL socket factory. keyStoreName=AndroidCAStore" + e3);
                return keyStore;
            } catch (NoSuchAlgorithmException e7) {
                e2 = e7;
                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "NoSuchAlgorithmException creating SSL socket factory. keyStoreName=AndroidCAStore" + e2);
                return keyStore;
            } catch (CertificateException e8) {
                e = e8;
                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "CertificateException creating SSL socket factory. keyStoreName=AndroidCAStore" + e);
                return keyStore;
            }
        } catch (IOException e9) {
            keyStore = null;
            e4 = e9;
        } catch (KeyStoreException e10) {
            keyStore = null;
            e3 = e10;
        } catch (NoSuchAlgorithmException e11) {
            keyStore = null;
            e2 = e11;
        } catch (CertificateException e12) {
            keyStore = null;
            e = e12;
        }
        return keyStore;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class ZeroRatedActionsBroadcastReceiver extends BroadcastReceiver {
        private ZeroRatedActionsBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ZeroRatedManager.ACTION_ZERORATED_CHANNEL_ACTIVE.equals(action)) {
                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Received zero rated channel active intent, start/resume the download");
                DownloadService downloadService = DownloadService.this;
                downloadService.postDownloadRequest(DownloadRequestBuilder.from(downloadService.mDownloadServiceSettings.getConfigValue(DownloadServiceSettings.KEY_DOWNLOAD_REQUEST)));
            }
            if (ZeroRatedManager.ACTION_ZERORATED_CLEANUP.equals(action)) {
                try {
                    DownloadService.clientCallBack.initFailed(DownloadService.this.downloadId, intent.getStringExtra(ZeroRatedManager.ACTION_ZERORATED_CLEANUP_MESSAGE), ErrorCodeMapper.KEY_OTHER);
                } catch (Exception e) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "Caught exception while callback" + e.getMessage());
                }
            }
            if (ZeroRatedManager.ACTION_ZERORATED_CHANNEL_INACTIVE.equals(action)) {
                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Received zero rated channel inactive intent, bringup again if needed");
                try {
                    if (DownloadService.downloader != null) {
                        DownloadServiceLogger.i(DownloadServiceLogger.TAG, "DownloadService.ZeroRatedActionsBroadcastReceiver(), shutting down HttpFileDownloader");
                        DownloadService.downloader.shutdown();
                    }
                    if ("onUnavailable".equals(intent.getStringExtra(ZeroRatedManager.KEY_INACTIVE_REASON))) {
                        DownloadService.clientCallBack.suspended(DownloadService.this.downloadId, true);
                        DownloadService.this.stopSelf();
                        return;
                    }
                    DownloadRequest from = DownloadRequestBuilder.from(DownloadService.this.mDownloadServiceSettings.getConfigValue(DownloadServiceSettings.KEY_DOWNLOAD_REQUEST));
                    if (!DownloadService.this.od.canIUseZeroRatedNetwork(context, from.getWifiOnly(), UpgradeUtils.getAdminApnUrl(from.getContentResource()), from.getDisallowedNetworks(), from.getContentResource())) {
                        DownloadService.clientCallBack.suspended(DownloadService.this.downloadId, true);
                        DownloadService.this.stopSelf();
                        return;
                    }
                    DownloadServiceLogger.d(DownloadServiceLogger.TAG, "ZeroRatedActionsBroadcastReceiver: zero rated channel not active, start zero rated process");
                    DownloadService.this.od.startZeroRatedProcess();
                    DownloadService.this.registerForZeroRatedActions();
                    DownloadService.clientCallBack.suspended(DownloadService.this.downloadId, false);
                } catch (Exception e2) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "ZeroRatedActionsBroadcastReceiver : exception " + e2);
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static class ShutDownActionsBroadcastReceiver extends BroadcastReceiver {
        private ShutDownActionsBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.ACTION_SHUTDOWN".equals(action) || "android.intent.action.REBOOT".equals(action)) {
                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Received shutdown/reboot intent. Shuting down downloadThread");
                try {
                    if (DownloadService.downloader != null) {
                        DownloadServiceLogger.i(DownloadServiceLogger.TAG, "DownloadService.ShutDownActionsBroadcastReceiver(), shutting down HttpFileDownloader");
                        DownloadService.downloader.shutdown();
                    }
                } catch (Exception e) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "DownloadService.ShutDownActionsBroadcastReceiver(), Exception in shutting down HttpFileDownloader" + e);
                }
            }
        }
    }

    public void registerForZeroRatedActions() {
        if (this._mZeroRatedActionsListener == null) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService, registering for zero rated actions");
            this._mZeroRatedActionsListener = new ZeroRatedActionsBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ZeroRatedManager.ACTION_ZERORATED_CHANNEL_ACTIVE);
            intentFilter.addAction(ZeroRatedManager.ACTION_ZERORATED_CHANNEL_INACTIVE);
            intentFilter.addAction(ZeroRatedManager.ACTION_ZERORATED_CLEANUP);
            BroadcastUtils.registerLocalReceiver(context, this._mZeroRatedActionsListener, intentFilter);
            return;
        }
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService, already registered for zero rated actions");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unregisterZeroRatedActionsReceiver() {
        try {
            if (this._mZeroRatedActionsListener != null) {
                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService, unregistering from listening to zerorated actions");
                BroadcastUtils.unregisterLocalReceiver(context, this._mZeroRatedActionsListener);
                this._mZeroRatedActionsListener = null;
            } else {
                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadService, not registered for any zero rated actions");
            }
        } catch (Exception unused) {
        }
    }
}
