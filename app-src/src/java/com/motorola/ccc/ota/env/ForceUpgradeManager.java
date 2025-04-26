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
public class ForceUpgradeManager {
    private static final int APP_NAME_POLLING_MANAGER = 3;
    private static final String APP_SECRET_POLLING_MANAGER = "regANAmedARGPuecROF";
    public static final String INTENT_ACTION_POLLING_MANAGER = "com.motorola.ccc.ota.ForceUpgradeManager.pollingManagerIntent";
    private Context ctx;
    private int mForceUpgradeTime;
    private IPollingManagerService pollingManager;
    private boolean mIsBound = false;
    protected ServiceConnection mConnection = new ServiceConnection() { // from class: com.motorola.ccc.ota.env.ForceUpgradeManager.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Logger.debug("OtaApp", "ForceUpgradeManager,successfully bound to 3C Polling Manger Service");
            ForceUpgradeManager.this.pollingManager = IPollingManagerService.Stub.asInterface(iBinder);
            ForceUpgradeManager.this.registerApp();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Logger.debug("OtaApp", "ForceUpgradeManager, disconnected from PollingManager service");
            ForceUpgradeManager.this.mIsBound = false;
            ForceUpgradeManager.this.pollingManager = null;
        }
    };

    public ForceUpgradeManager(Context context) {
        this.ctx = context;
    }

    public void init(int i) {
        Logger.debug("OtaApp", "Starting ForceUpgradeManager, mIsbound :" + this.mIsBound);
        this.mForceUpgradeTime = i;
        doBindService();
    }

    public void onDestroy() {
        Logger.debug("OtaApp", "shutting down ForceUpgradeManager, mIsbound :" + this.mIsBound);
        doUnbindService();
    }

    private void doBindService() {
        if (this.mIsBound) {
            return;
        }
        Logger.debug("OtaApp", "ForceUpgradeManager, binding to PollingManager Service");
        this.ctx.bindService(new Intent(this.ctx, OtaService.class), this.mConnection, 1);
        this.mIsBound = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void registerApp() {
        try {
            long forceUpgradetime = getForceUpgradetime();
            Logger.info("OtaApp", "ForceUpgradeManager.registerApp(), force upgrade interval: " + forceUpgradetime);
            int registerApp = this.pollingManager.registerApp(3, APP_SECRET_POLLING_MANAGER, INTENT_ACTION_POLLING_MANAGER, null, forceUpgradetime, false, false);
            if (-1 == registerApp && (registerApp = unRegisterApp()) == 0) {
                registerApp = this.pollingManager.registerApp(3, APP_SECRET_POLLING_MANAGER, INTENT_ACTION_POLLING_MANAGER, null, forceUpgradetime, false, false);
            }
            if (registerApp != 0) {
                Logger.error("OtaApp", "ForceUpgradeManager.registerApp(): got an error trying to register with polling manager: " + registerApp);
            }
        } catch (RemoteException e) {
            Logger.error("OtaApp", "ForceUpgradeManager.registerApp(): got exception " + e);
        }
    }

    private long getForceUpgradetime() {
        return this.mForceUpgradeTime * 1000;
    }

    private int unRegisterApp() throws RemoteException {
        return this.pollingManager.unregisterApp(3, APP_SECRET_POLLING_MANAGER);
    }

    private void doUnbindService() {
        if (this.mIsBound) {
            try {
                unRegisterApp();
            } catch (RemoteException e) {
                Logger.error("OtaApp", "Exception in ForceUpgradeManager: doUnbindService: " + e);
            }
            this.ctx.unbindService(this.mConnection);
            this.mIsBound = false;
        }
    }
}
