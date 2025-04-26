package com.motorola.ccc.ota.env;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.IBinder;
import android.os.SystemClock;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.modem.ModemPollingManager;
import com.motorola.ccc.ota.ui.UpdateReceiver;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.UpgraderReceiver;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.TestUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.utils.BroadcastUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class OtaService extends Service {
    private Context context;
    private boolean isServiceStartedOnChkUpdate;
    private ConnectivityManager mConnMgr;
    private OtaMainBroadcastReceiver mMainReceiver;
    private OutofBoxUpdateDetectReceiver mOobReceiver;
    private PollingManagerService mPollingManager;
    private OobSetupCompletionObserver mSetupObserver;
    private UpdateReceiver mUpdateReceiver;
    private UpgraderReceiver mUpgraderReceiver;
    private ModemPollingManager modemPollingManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private AndroidPollingManager pollingManager;
    private BotaSettings settings;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        Logger.debug("OtaApp", "onBind() called");
        PollingManagerService pollingManagerService = this.mPollingManager;
        if (pollingManagerService != null) {
            return pollingManagerService.getBinder();
        }
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        Logger.debug("OtaApp", "Launching ota service");
        this.settings = new BotaSettings();
        this.context = getApplicationContext();
        NotificationUtils.stopNotificationService(OtaApplication.getGlobalContext());
        cleanUpDefault();
        this.settings.setupDefaults(this.context);
        PollingManagerService pollingManagerService = PollingManagerService.getInstance(this);
        this.mPollingManager = pollingManagerService;
        pollingManagerService.init();
        this.mUpdateReceiver = new UpdateReceiver();
        this.mOobReceiver = new OutofBoxUpdateDetectReceiver();
        this.mMainReceiver = new OtaMainBroadcastReceiver();
        this.mUpgraderReceiver = new UpgraderReceiver();
        UpdaterUtils.scheduleWorkManager(this);
        UpdaterUtils.scheduleModemWorkManager(this);
        long j = this.settings.getLong(Configs.BOOT_START_TIMESTAMP, -1L);
        if (j > -1) {
            int currentTimeMillis = (int) ((System.currentTimeMillis() - SystemClock.elapsedRealtime()) - j);
            Logger.debug("OtaApp", "Time taken for install=" + currentTimeMillis);
            this.settings.setLong(Configs.STATS_TIME_TAKEN_FOR_INSTALL, currentTimeMillis);
        }
        this.mConnMgr = (ConnectivityManager) getSystemService("connectivity");
        this.networkCallback = new ConnectivityManager.NetworkCallback() { // from class: com.motorola.ccc.ota.env.OtaService.1
            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onAvailable(Network network) {
                if (BuildPropReader.isCtaVersion(OtaService.this.settings)) {
                    return;
                }
                if (OtaService.this.settings.getBoolean(Configs.INITIAL_SETUP_COMPLETED) && !OtaService.this.settings.getBoolean(Configs.INITIAL_SETUP_TRIGGERED) && OtaService.this.settings.getBoolean(Configs.CHANNEL_ID_UPDATED)) {
                    Logger.debug("OtaApp", "OtaService:NetworkCallback:onAvailable:TOS Check passed and initial setup is triggered");
                    OtaService.this.mOobReceiver.disableSelf(OtaService.this.context);
                    OtaService.this.mOobReceiver.broadcastCheckforUpdateIntent(OtaService.this.context);
                    OtaService.this.settings.setBoolean(Configs.INITIAL_SETUP_TRIGGERED, true);
                    OtaService.this.mConnMgr.unregisterNetworkCallback(OtaService.this.networkCallback);
                    return;
                }
                Logger.info("OtaApp", "OtaService.NetworkCallback:onAvailable; data connection available, INITIAL_SETUP_COMPLETED = " + OtaService.this.settings.getBoolean(Configs.INITIAL_SETUP_COMPLETED) + " : INITIAL_SETUP_TRIGGERED" + OtaService.this.settings.getBoolean(Configs.INITIAL_SETUP_TRIGGERED) + " : CHANNEL_ID_UPDATED = " + OtaService.this.settings.getBoolean(Configs.CHANNEL_ID_UPDATED));
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                super.onLost(network);
                Logger.debug("OtaApp", "OtaService.NetworkCallback:onLost no data connection");
            }
        };
        if (!this.settings.getBoolean(Configs.INITIAL_SETUP_TRIGGERED)) {
            this.mConnMgr.registerDefaultNetworkCallback(this.networkCallback);
            registerOutofBoxUpdateDetectReceiver(this.mOobReceiver);
            OobSetupCompletionObserver oobSetupCompletionObserver = OobSetupCompletionObserver.getInstance();
            this.mSetupObserver = oobSetupCompletionObserver;
            oobSetupCompletionObserver.init(this);
        }
        OtaApplication.getExecutorService().submit(new Runnable() { // from class: com.motorola.ccc.ota.env.OtaService.2
            @Override // java.lang.Runnable
            public void run() {
                OtaService otaService = OtaService.this;
                otaService.registerReceiver(otaService.mMainReceiver, new IntentFilter(UpgradeUtilConstants.OTA_START_ACTION), Permissions.INTERACT_OTA_SERVICE, null, 2);
                OtaService.this.registerOtaMainBroadcastReceiver();
                OtaService.this.registerUpdateReceiver();
                OtaService otaService2 = OtaService.this;
                otaService2.registerUpgraderReceiver(otaService2.mUpgraderReceiver);
                OtaService.this.sendBroadcast(new Intent(UpgradeUtilConstants.OTA_START_ACTION), Permissions.INTERACT_OTA_SERVICE);
                if (OtaService.this.isServiceStartedOnChkUpdate) {
                    Logger.debug("OtaApp", "Ota service is started on check for update if it is not running, so sending check for update again once OTA is up");
                    OtaService.this.context.sendBroadcast(new Intent(UpdaterUtils.ACTION_MANUAL_CHECK_UPDATE), Permissions.INTERACT_OTA_SERVICE);
                    UpdaterUtils.sendCheckUpdateIntent(OtaService.this.context);
                    OtaService.this.isServiceStartedOnChkUpdate = false;
                }
            }
        });
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent != null) {
            this.isServiceStartedOnChkUpdate = intent.getBooleanExtra(UpgradeUtilConstants.KEY_SERVICE_STARTED_ON_CHK_UPDATE, false);
        }
        if (!this.settings.getBoolean(Configs.INITIAL_SETUP_TRIGGERED)) {
            this.mSetupObserver.checkSetupCompleted();
        }
        if (intent == null) {
            TestUtils.collectCrashDump();
            return 1;
        }
        return 1;
    }

    @Override // android.app.Service
    public void onDestroy() {
        this.mPollingManager.shutdown();
        OobSetupCompletionObserver oobSetupCompletionObserver = this.mSetupObserver;
        if (oobSetupCompletionObserver != null) {
            oobSetupCompletionObserver.shutdown();
        }
        try {
            unregisterOutofBoxUpdateDetectReceiver();
            unregisterOtaMainBroadcastReceiver();
            unRegisterUpdateReceiver();
            unregisterUpgraderReceiver();
        } catch (Exception e) {
            Logger.error("OtaApp", "OtaService:onDestroy:Exception while unregistering receivers:msg=" + e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerUpgraderReceiver(UpgraderReceiver upgraderReceiver) {
        CusAndroidUtils.registerUpgraderReceiverIntents(this, upgraderReceiver);
    }

    private void unregisterUpgraderReceiver() {
        unregisterReceiver(this.mUpgraderReceiver);
    }

    private void registerOutofBoxUpdateDetectReceiver(OutofBoxUpdateDetectReceiver outofBoxUpdateDetectReceiver) {
        Logger.info("OtaApp", "registering for OutofBoxUpdateDetectReceiver");
        CusAndroidUtils.registerOutofBoxUpdateDetectReceiverIntents(this, outofBoxUpdateDetectReceiver);
    }

    private void unregisterOutofBoxUpdateDetectReceiver() {
        BroadcastUtils.unregisterLocalReceiver(this, this.mOobReceiver);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerOtaMainBroadcastReceiver() {
        CusAndroidUtils.registerFotaIntents(this, this.mMainReceiver);
        CusAndroidUtils.registerMiscIntents(this, this.mMainReceiver);
        CusAndroidUtils.registerStateMachineIntent(this, this.mMainReceiver);
        CusAndroidUtils.registerCheckUpdateActions(this, this.mMainReceiver);
        CusAndroidUtils.registerRadioActions(this, this.mMainReceiver);
        CusAndroidUtils.registerWifiDiscoverActions(this, this.mMainReceiver);
        CusAndroidUtils.registerForceUpgradeActions(this, this.mMainReceiver);
        CusAndroidUtils.registerPollingManagerExpiryIntent(this, this.mMainReceiver);
        CusAndroidUtils.registerInternalIntents(this, this.mMainReceiver);
        CusAndroidUtils.registerCaptivePortalLoginDone(this, this.mMainReceiver);
        CusAndroidUtils.registerReserveSpaceRequest(this, this.mMainReceiver);
        CusAndroidUtils.registerSuCancelRequest(this, this.mMainReceiver);
        CusAndroidUtils.registerShutdownActions(this, this.mMainReceiver);
        CusAndroidUtils.registerConfigChangeIntentActions(this, this.mMainReceiver);
        CusAndroidUtils.registerAscIntentActions(this, this.mMainReceiver);
        CusAndroidUtils.registerModemIntentActions(this, this.mMainReceiver);
        CceSyncSettingsHandler.registerCceIntents(this, this.mMainReceiver);
        if (BuildPropReader.isATT()) {
            CusAndroidUtils.registerSimStateChangeReceiver(this, this.mMainReceiver);
        }
    }

    private void unregisterOtaMainBroadcastReceiver() {
        BroadcastUtils.unregisterLocalReceiver(this, this.mMainReceiver);
        unregisterReceiver(this.mMainReceiver);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerUpdateReceiver() {
        CusAndroidUtils.registerUpdateReceiverIntents(this, this.mUpdateReceiver);
    }

    private void unRegisterUpdateReceiver() {
        BroadcastUtils.unregisterLocalReceiver(this, this.mUpdateReceiver);
        unregisterReceiver(this.mUpdateReceiver);
    }

    private void cleanUpDefault() {
        this.settings.removeConfig(Configs.ADVANCED_DOWNLOAD_FEATURE);
        this.settings.removeConfig(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY);
    }
}
