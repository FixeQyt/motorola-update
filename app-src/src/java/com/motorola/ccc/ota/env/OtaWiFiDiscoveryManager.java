package com.motorola.ccc.ota.env;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.motorola.ccc.ota.aidl.IPollingManagerService;
import com.motorola.ccc.ota.utils.Logger;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class OtaWiFiDiscoveryManager {
    private static final int APP_NAME_POLLING_MANAGER = 2;
    private static final String APP_SECRET_POLLING_MANAGER = "regANAmYreVOCSidifIW";
    private static final int DEFAULT_WIFI_DISCOVERY_INTERVAL = 82800;
    public static final String INTENT_ACTION_POLLING_MANAGER = "com.motorola.ccc.ota.WiFiDiscoveryManager.pollingManagerIntent";
    private static final int MAXIMUM_WIFI_DISCOVERY_INTERVAL = 604800;
    private static final int MINIMUM_WIFI_DISCOVERY_INTERVAL = 0;
    private Context ctx;
    private boolean mAllowOnRoaming;
    private int mDiscoveryTime;
    private boolean mOnlyOnNetwork;
    private IPollingManagerService pollingManager;
    private boolean mIsBound = false;
    protected ServiceConnection mConnection = new ServiceConnection() { // from class: com.motorola.ccc.ota.env.OtaWiFiDiscoveryManager.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Logger.debug("OtaApp", "OtaWiFiDiscoveryManager,successfully bound to 3C Polling Manger Service");
            OtaWiFiDiscoveryManager.this.pollingManager = IPollingManagerService.Stub.asInterface(iBinder);
            OtaWiFiDiscoveryManager.this.registerApp();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Logger.debug("OtaApp", "disconnected from PollingManager service");
            OtaWiFiDiscoveryManager.this.mIsBound = false;
            OtaWiFiDiscoveryManager.this.pollingManager = null;
        }
    };

    public OtaWiFiDiscoveryManager(Context context) {
        this.ctx = context;
    }

    public void init(int i, boolean z, boolean z2) {
        Logger.debug("OtaApp", "Starting OtaWiFiDiscoveryManager, mIsbound :" + this.mIsBound + " onlyOnNetwork " + z + " allowOnRoaming " + z2);
        this.mDiscoveryTime = i;
        this.mOnlyOnNetwork = z;
        this.mAllowOnRoaming = z2;
        doBindService();
    }

    public void onDestroy() {
        Logger.debug("OtaApp", "shutting down OtaWiFiDiscoveryManager, mIsbound :" + this.mIsBound);
        doUnbindService();
    }

    private void doBindService() {
        if (this.mIsBound) {
            return;
        }
        Logger.debug("OtaApp", "binding to PollingManager Service");
        this.ctx.bindService(new Intent(this.ctx, OtaService.class), this.mConnection, 1);
        this.mIsBound = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerApp() {
        try {
            int wifiDiscoverytime = getWifiDiscoverytime();
            Logger.info("OtaApp", "registerApp(), WiFi Discover interval: " + wifiDiscoverytime);
            long j = wifiDiscoverytime;
            int registerApp = this.pollingManager.registerApp(2, APP_SECRET_POLLING_MANAGER, INTENT_ACTION_POLLING_MANAGER, null, j, this.mOnlyOnNetwork, this.mAllowOnRoaming);
            if (-1 == registerApp && (registerApp = unRegisterApp()) == 0) {
                registerApp = this.pollingManager.registerApp(2, APP_SECRET_POLLING_MANAGER, INTENT_ACTION_POLLING_MANAGER, null, j, this.mOnlyOnNetwork, this.mAllowOnRoaming);
            }
            if (registerApp != 0) {
                Logger.error("OtaApp", "registerApp(): got an error trying to register with polling manager: " + registerApp);
            }
        } catch (RemoteException e) {
            Logger.error("OtaApp", "registerApp(): got exception " + e);
        }
    }

    private int getWifiDiscoverytime() {
        int i = this.mDiscoveryTime;
        if (i <= 0 || i > 604800) {
            i = DEFAULT_WIFI_DISCOVERY_INTERVAL;
        }
        return i * 1000;
    }

    private int unRegisterApp() throws RemoteException {
        return this.pollingManager.unregisterApp(2, APP_SECRET_POLLING_MANAGER);
    }

    private void doUnbindService() {
        if (this.mIsBound) {
            try {
                unRegisterApp();
            } catch (RemoteException e) {
                Logger.error("OtaApp", "Exception in OtaWiFiDiscoveryManager, doUnbindService: " + e);
            }
            this.ctx.unbindService(this.mConnection);
            this.mIsBound = false;
        }
    }
}
