package com.motorola.ccc.ota.sources.modem;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.motorola.ccc.ota.aidl.IPollingManagerService;
import com.motorola.ccc.ota.env.OtaService;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.utils.BroadcastUtils;
import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class ModemPollingManager {
    private static final int APP_NAME_POLLING_MANAGER = 4;
    private static final String APP_SECRET_POLLING_MANAGER = "cmRModEmPav@cprimr";
    public static final String INTENT_ACTION_POLLING_MANAGER = "com.motorola.ccc.ota.ModemPollingManager.pollingManagerIntent";
    private static final int POLLING_OFF = 0;
    private static final int POLL_IMMEDIATELY = 60000;
    private Context ctx;
    private IPollingManagerService pollingManager;
    private BotaSettings settings;
    private MyIntentReceiver myReceiver = new MyIntentReceiver();
    protected ServiceConnection mConnection = new ServiceConnection() { // from class: com.motorola.ccc.ota.sources.modem.ModemPollingManager.1
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Logger.debug("OtaApp", "ModemPollingManager, successfully bound to Polling Manger Service...");
            ModemPollingManager.this.pollingManager = IPollingManagerService.Stub.asInterface(iBinder);
            if (ModemPollingManager.this.settings.getBoolean(Configs.INITIAL_SETUP_TRIGGERED)) {
                UpgradeUtilMethods.scheduleModemPolling();
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
            Logger.debug("OtaApp", "ModemPollingManager:onServiceDisconnected");
            ModemPollingManager.this.pollingManager = null;
        }
    };

    public ModemPollingManager(Context context, BotaSettings botaSettings) {
        this.ctx = context;
        this.settings = botaSettings;
        init();
    }

    private void init() {
        Logger.debug("OtaApp", "ModemPollingManager.init, binding to Polling Manager Service");
        this.ctx.bindService(new Intent(this.ctx, OtaService.class), this.mConnection, 1);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.ACTION_MODEM_UPGRADE_POLL);
        BroadcastUtils.registerLocalReceiver(this.ctx, this.myReceiver, intentFilter);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class MyIntentReceiver extends BroadcastReceiver {
        private MyIntentReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.debug("OtaApp", "ModemPollingManager.handleIntent: " + intent.getAction());
            if (intent.getAction().equals(UpgradeUtilConstants.ACTION_MODEM_UPGRADE_POLL)) {
                ModemPollingManager.this.configurePolling();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void configurePolling() {
        if (this.pollingManager == null) {
            Logger.debug("OtaApp", "ModemPollingManager. configurePolling : pollingManager is null");
            return;
        }
        try {
            long delayForNextPolling = getDelayForNextPolling();
            Logger.debug("OtaApp", "ModemPollingManager:getDelayForNextPolling is " + delayForNextPolling);
            if (delayForNextPolling == 0) {
                Logger.info("OtaApp", "ModemPollingManager:Server always wins, Disabling the polling");
                unRegisterApp();
                return;
            }
            Logger.debug("OtaApp", "ModemPollingManager:Next Polling is scheduled at " + TimeUnit.MILLISECONDS.toMinutes(delayForNextPolling) + " mins from now");
            int registerApp = this.pollingManager.registerApp(4, APP_SECRET_POLLING_MANAGER, INTENT_ACTION_POLLING_MANAGER, null, delayForNextPolling, true, true);
            if (-1 == registerApp) {
                Logger.debug("OtaApp", "ModemPollingManager:app is already registered so unregister and register");
                registerApp = unRegisterApp();
                if (registerApp == 0) {
                    registerApp = this.pollingManager.registerApp(4, APP_SECRET_POLLING_MANAGER, INTENT_ACTION_POLLING_MANAGER, null, delayForNextPolling, true, true);
                }
            }
            if (registerApp != 0) {
                Logger.error("OtaApp", "ModemPollingManager:configurePolling(): got an error trying to register with polling manager: " + registerApp);
            }
        } catch (RemoteException e) {
            Logger.error("OtaApp", "ModemPollingManager:Exception in AndroidPollingManager: configurePolling: " + e);
        }
    }

    private long getDelayForNextPolling() {
        long j = this.settings.getLong(Configs.NEXT_MODEM_POLLING_VALUE, -1L);
        if (j == -1) {
            j = System.currentTimeMillis() + 86400000;
            this.settings.setLong(Configs.NEXT_MODEM_POLLING_VALUE, j);
        }
        if (j == 0) {
            return 0L;
        }
        if (j == UpgradeUtilConstants.WAITING_FOR_NETWORK) {
            return UpgradeUtilConstants.WAITING_FOR_NETWORK;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > j) {
            return 60000L;
        }
        long abs = StrictMath.abs(j - currentTimeMillis);
        if (abs < 1000) {
            return 60000L;
        }
        if (abs > this.settings.getLong(Configs.POLL_MODEM_AFTER, 86400000L)) {
            Logger.debug("OtaApp", "ModemPollingManager:Difference is more than pollAfter value that means device time is less than actual time. So resetting it to pollAfter value");
            this.settings.setLong(Configs.NEXT_MODEM_POLLING_VALUE, System.currentTimeMillis() + this.settings.getLong(Configs.POLL_MODEM_AFTER, 86400000L));
            return this.settings.getLong(Configs.POLL_MODEM_AFTER, 86400000L);
        }
        return abs;
    }

    public void onDestroy() {
        Logger.debug("OtaApp", "shutting down ModemPollingManager");
        BroadcastUtils.unregisterLocalReceiver(this.ctx, this.myReceiver);
        if (this.pollingManager != null) {
            try {
                unRegisterApp();
            } catch (RemoteException e) {
                Logger.error("OtaApp", "ModemPollingManager:Exception in AndroidPollingManager: onDestroy: " + e);
            }
        }
    }

    public int unRegisterApp() throws RemoteException {
        return this.pollingManager.unregisterApp(4, APP_SECRET_POLLING_MANAGER);
    }
}
