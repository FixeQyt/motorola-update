package com.motorola.ccc.ota.env;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.aidl.IPollingManagerService;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.PMUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public final class PollingManagerService {
    private static final String LOG_TAG = "OtaApp";
    private static final String MY_ACTION = "com.motorola.blur.service.blur.pm.alarmintent";
    private static final String REQUEST_ID = "requestId";
    private static PollingManagerService mMe;
    private AlarmManager mAlarmManger;
    private ConnectivityManager mConnMgr;
    private MyContentObserver mDataRoamingObserver;
    private final Service mParent;
    private PendingIntent mPendingIntent;
    private PowerManager mPowerManager;
    private TelephonyManager mTelephonyManager;
    private PowerManager.WakeLock mWakeLock;
    private HashMap<Integer, PMApp> mRegisteredApps = new HashMap<>();
    private Object mLock = new Object();
    private MyPhoneStateListener mPhoneStateListener = new MyPhoneStateListener();
    private MyIntentReceiver myRecv = new MyIntentReceiver();
    private MyPollingReceiver myPollingRecv = new MyPollingReceiver();
    private MyConnectivityReceiver mConnectivityRecv = new MyConnectivityReceiver();
    private boolean mRoaming = false;
    private boolean mDataRoaming = false;
    private final IPollingManagerService.Stub mBinder = new IPollingManagerService.Stub() { // from class: com.motorola.ccc.ota.env.PollingManagerService.1
        @Override // com.motorola.ccc.ota.aidl.IPollingManagerService
        public int registerApp(int i, String str, String str2, String[] strArr, long j, boolean z, boolean z2) {
            synchronized (PollingManagerService.this.mLock) {
                Logger.debug("OtaApp", "PollingManagerService, registerApp(): " + i);
                if (i > 0 && str != null && str2 != null) {
                    if (j > 0 || j == UpgradeUtilConstants.WAITING_FOR_NETWORK) {
                        if (PollingManagerService.this.mRegisteredApps.containsKey(Integer.valueOf(i))) {
                            Logger.debug("OtaApp", "PollingManagerService, registerApp(): " + i + " already registered.");
                            return -1;
                        }
                        PMApp pMApp = new PMApp(str, str2, strArr, j, z, z2);
                        PollingManagerService.this.mRegisteredApps.put(Integer.valueOf(i), pMApp);
                        Logger.verbose("OtaApp", "registered app " + pMApp.mAllowOnlyOnNetwork + SystemUpdateStatusUtils.SPACE + pMApp.mAllowOnRoaming);
                        if (pMApp.getNextPollTime() > SystemClock.elapsedRealtime()) {
                            PollingManagerService.this.scheduleTask(pMApp.getNextPollTime(), i);
                        } else {
                            PollingManagerService.this.decideWhoNeedsPolling(-1);
                        }
                        return 0;
                    }
                    return -2;
                }
                return -3;
            }
        }

        @Override // com.motorola.ccc.ota.aidl.IPollingManagerService
        public int unregisterApp(int i, String str) {
            synchronized (PollingManagerService.this.mLock) {
                Logger.debug("OtaApp", "PollingManagerService, unregisterApp(): " + i);
                if (i > 0 && str != null) {
                    if (!PollingManagerService.this.mRegisteredApps.containsKey(Integer.valueOf(i))) {
                        Logger.debug("OtaApp", "PollingManagerService, unregisterApp(): " + i + " not registered.");
                        return -4;
                    } else if (((PMApp) PollingManagerService.this.mRegisteredApps.get(Integer.valueOf(i))).getAppSecret().equals(str)) {
                        PollingManagerService.this.mRegisteredApps.remove(Integer.valueOf(i));
                        Intent intent = new Intent(PollingManagerService.MY_ACTION);
                        intent.putExtra(PollingManagerService.REQUEST_ID, i);
                        PollingManagerService pollingManagerService = PollingManagerService.this;
                        pollingManagerService.mPendingIntent = PendingIntent.getBroadcast(pollingManagerService.mParent, i, intent, 335544320);
                        PollingManagerService.this.mAlarmManger.cancel(PollingManagerService.this.mPendingIntent);
                        return 0;
                    } else {
                        return -5;
                    }
                }
                return -3;
            }
        }
    };
    private AtomicBoolean mDeviceConnected = new AtomicBoolean();
    private AtomicInteger mConnectionType = new AtomicInteger(-1);
    private NetworkStateChangeListener networkStateListener = new NetworkStateChangeListener();

    private PollingManagerService(Service service) {
        this.mParent = service;
        Logger.debug("OtaApp", "PollingManagerService()");
    }

    public static synchronized PollingManagerService getInstance(Service service) {
        PollingManagerService pollingManagerService;
        synchronized (PollingManagerService.class) {
            if (mMe == null) {
                mMe = new PollingManagerService(service);
            }
            pollingManagerService = mMe;
        }
        return pollingManagerService;
    }

    public void init() {
        Logger.debug("OtaApp", "PollingManagerService init()");
        TelephonyManager telephonyManager = (TelephonyManager) this.mParent.getSystemService("phone");
        this.mTelephonyManager = telephonyManager;
        telephonyManager.listen(this.mPhoneStateListener, 193);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MY_ACTION);
        this.mParent.registerReceiver(this.myRecv, intentFilter, Permissions.INTERACT_OTA_SERVICE, null, 2);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.net.conn.RESTRICT_BACKGROUND_CHANGED");
        this.mConnMgr = (ConnectivityManager) this.mParent.getSystemService("connectivity");
        this.mParent.registerReceiver(this.mConnectivityRecv, intentFilter2, 2);
        this.networkStateListener.register();
        if (-1 != NetworkUtils.getNetworkCapabilityType(this.mConnMgr)) {
            this.mDeviceConnected.set(true);
            this.mConnectionType.set(this.mDeviceConnected.get() ? NetworkUtils.getNetworkCapabilityType(this.mConnMgr) : -1);
        }
        this.mDataRoamingObserver = new MyContentObserver(new Handler());
        this.mParent.getContentResolver().registerContentObserver(Settings.Global.getUriFor("data_roaming"), true, this.mDataRoamingObserver);
        this.mAlarmManger = (AlarmManager) this.mParent.getSystemService("alarm");
        PowerManager powerManager = (PowerManager) this.mParent.getSystemService("power");
        this.mPowerManager = powerManager;
        PowerManager.WakeLock newWakeLock = powerManager.newWakeLock(1, "OtaApp");
        this.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(true);
    }

    public void shutdown() {
        PendingIntent pendingIntent;
        Logger.debug("OtaApp", "PollingManagerService shutdown()");
        this.mTelephonyManager.listen(this.mPhoneStateListener, 0);
        AlarmManager alarmManager = this.mAlarmManger;
        if (alarmManager != null && (pendingIntent = this.mPendingIntent) != null) {
            alarmManager.cancel(pendingIntent);
        }
        this.mParent.unregisterReceiver(this.myRecv);
        this.mParent.unregisterReceiver(this.mConnectivityRecv);
        this.networkStateListener.unRegister();
        this.mParent.getContentResolver().unregisterContentObserver(this.mDataRoamingObserver);
    }

    public IBinder getBinder() {
        return this.mBinder;
    }

    public boolean isConnected() {
        return this.mDeviceConnected.get();
    }

    public int getConnectionType() {
        return this.mConnectionType.get();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendRoamingIntent() {
        Intent intent = new Intent(PMUtils.POLLINGMGR_ROAMING_CHANGE);
        intent.putExtra(PMUtils.KEY_ROAMING_EXTRA, this.mRoaming);
        intent.putExtra(PMUtils.KEY_DATAROAMING_EXTRA, this.mDataRoaming);
        BroadcastUtils.sendLocalBroadcast(this.mParent, intent);
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    private class MyContentObserver extends ContentObserver {
        @Override // android.database.ContentObserver
        public boolean deliverSelfNotifications() {
            return true;
        }

        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            boolean z2 = Settings.Global.getInt(PollingManagerService.this.mParent.getContentResolver(), "data_roaming", 0) == 1;
            if (z2 != PollingManagerService.this.mDataRoaming) {
                PollingManagerService.this.mDataRoaming = z2;
                PollingManagerService.this.sendRoamingIntent();
            }
        }
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    private class MyPhoneStateListener extends PhoneStateListener {
        private MyPhoneStateListener() {
        }

        @Override // android.telephony.PhoneStateListener
        public void onServiceStateChanged(ServiceState serviceState) {
            synchronized (PollingManagerService.this.mLock) {
                boolean roaming = serviceState.getRoaming();
                Logger.debug("OtaApp", "PollingMangerService, phone state: roaming: " + roaming);
                if (roaming != PollingManagerService.this.mRoaming) {
                    PollingManagerService.this.mRoaming = roaming;
                    PollingManagerService pollingManagerService = PollingManagerService.this;
                    pollingManagerService.mDataRoaming = Settings.Secure.getInt(pollingManagerService.mParent.getContentResolver(), "data_roaming", 0) == 1;
                    PollingManagerService.this.sendRoamingIntent();
                    if (!PollingManagerService.this.mRoaming || PollingManagerService.this.mDataRoaming) {
                        PollingManagerService.this.decideWhoNeedsPolling(-1);
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static final class PMApp {
        private boolean mAllowOnRoaming;
        private boolean mAllowOnlyOnNetwork;
        private final String mAppSecret;
        private final String mCbAction;
        private final String[] mCbCategories;
        private long mNextPollTime;

        PMApp(String str, String str2, String[] strArr, long j, boolean z, boolean z2) {
            this.mAppSecret = str;
            this.mCbAction = str2;
            this.mCbCategories = strArr;
            this.mNextPollTime = SystemClock.elapsedRealtime() + j;
            this.mAllowOnRoaming = z2;
            this.mAllowOnlyOnNetwork = z;
        }

        final long getNextPollTime() {
            return this.mNextPollTime;
        }

        final String getAppSecret() {
            return this.mAppSecret;
        }

        final String getAction() {
            return this.mCbAction;
        }

        final String[] getCategories() {
            return this.mCbCategories;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class MyPollingReceiver extends BroadcastReceiver {
        private MyPollingReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (PollingManagerService.this.mWakeLock == null || !PollingManagerService.this.mWakeLock.isHeld()) {
                return;
            }
            Logger.debug("OtaApp", "PollingManagerService, wake lock released");
            PollingManagerService.this.mWakeLock.release();
        }
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    private class MyConnectivityReceiver extends BroadcastReceiver {
        private MyConnectivityReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!"android.net.conn.RESTRICT_BACKGROUND_CHANGED".equals(intent.getAction()) || PollingManagerService.this.isDataSaverEnabled()) {
                return;
            }
            PollingManagerService.this.decideWhoNeedsPolling(-1);
        }
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class NetworkStateChangeListener extends ConnectivityManager.NetworkCallback {
        private boolean hasLoggedCapabilities = false;

        public NetworkStateChangeListener() {
        }

        public void register() {
            Logger.debug("OtaApp", "PollingManagerService:register NetworkCallback");
            PollingManagerService.this.mConnMgr.registerDefaultNetworkCallback(this);
        }

        public void unRegister() {
            Logger.debug("OtaApp", "PollingManagerService:unregister NetworkCallback");
            PollingManagerService.this.mConnMgr.unregisterNetworkCallback(this);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            Logger.debug("OtaApp", "PollingManagerService:NetworkCallback:Network onAvailable");
            PollingManagerService.this.mDeviceConnected.set(true);
            this.hasLoggedCapabilities = false;
            PollingManagerService.this.broadcastPollingManagerConnectivity();
            PollingManagerService.this.decideWhoNeedsPolling(-1);
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            if (PollingManagerService.this.mDeviceConnected.get() && !this.hasLoggedCapabilities) {
                Logger.debug("OtaApp", "PollingManagerService:NetworkCallback:networkCapabilities=" + networkCapabilities.toString());
                this.hasLoggedCapabilities = true;
            }
            PollingManagerService.this.mConnectionType.set(NetworkUtils.getNetworkCapabilityType(networkCapabilities));
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            super.onLost(network);
            Logger.debug("OtaApp", "PollingManagerService:NetworkCallback:Network onLost");
            this.hasLoggedCapabilities = false;
            PollingManagerService.this.mDeviceConnected.set(false);
            PollingManagerService.this.mConnectionType.set(-1);
            PollingManagerService.this.broadcastPollingManagerConnectivity();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void broadcastPollingManagerConnectivity() {
        Intent intent = new Intent(PMUtils.POLLINGMGR_CONNECTIVITY);
        intent.putExtra(PMUtils.KEY_CONNECTIVITY_EXTRA, this.mDeviceConnected.get());
        intent.putExtra(PMUtils.KEY_CONNECTIVITY_TYPE_EXTRA, NetworkUtils.getNetworkCapabilityType(this.mConnMgr));
        BroadcastUtils.sendLocalBroadcast(this.mParent, intent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isDataSaverEnabled() {
        return this.mConnMgr.isActiveNetworkMetered() && this.mConnMgr.getRestrictBackgroundStatus() == 3;
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    private class MyIntentReceiver extends BroadcastReceiver {
        private MyIntentReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            synchronized (PollingManagerService.this.mLock) {
                if (intent.getAction().equals(PollingManagerService.MY_ACTION)) {
                    Logger.debug("OtaApp", "PollingManagerService, alarm fired!");
                    PollingManagerService.this.decideWhoNeedsPolling(intent.getIntExtra(PollingManagerService.REQUEST_ID, -1));
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void decideWhoNeedsPolling(int i) {
        synchronized (this.mLock) {
            Logger.debug("OtaApp", "decideWhoNeedsPolling");
            long elapsedRealtime = SystemClock.elapsedRealtime();
            if (i != -1) {
                PMApp pMApp = this.mRegisteredApps.get(Integer.valueOf(i));
                if (pMApp != null && preCheck(pMApp)) {
                    alarmExpired(pMApp);
                    this.mRegisteredApps.remove(Integer.valueOf(i));
                }
            } else {
                for (Map.Entry<Integer, PMApp> entry : this.mRegisteredApps.entrySet()) {
                    PMApp value = entry.getValue();
                    if (elapsedRealtime >= value.getNextPollTime() && preCheck(value)) {
                        alarmExpired(value);
                        this.mRegisteredApps.remove(Integer.valueOf(i));
                    }
                }
            }
        }
    }

    private boolean preCheck(PMApp pMApp) {
        if (pMApp.mAllowOnlyOnNetwork && (!isConnected() || isDataSaverEnabled())) {
            Logger.debug("OtaApp", "no network return");
            return false;
        } else if (pMApp.mAllowOnRoaming || getConnectionType() != 0 || !this.mRoaming || this.mDataRoaming) {
            return true;
        } else {
            Logger.debug("OtaApp", "on roaming return back");
            return false;
        }
    }

    void alarmExpired(PMApp pMApp) {
        Logger.debug("OtaApp", "PollingManagerService, wake lock acquired");
        this.mWakeLock.acquire(30000L);
        Intent intent = new Intent(pMApp.getAction());
        String[] categories = pMApp.getCategories();
        if (categories != null) {
            for (String str : categories) {
                intent.addCategory(str);
            }
        }
        Bundle bundle = new Bundle();
        bundle.putString(PMUtils.KEY_APPSECRET, pMApp.getAppSecret());
        intent.putExtras(bundle);
        this.mParent.sendOrderedBroadcast(intent, Permissions.INTERACT_OTA_SERVICE, this.myPollingRecv, null, -1, null, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleTask(long j, int i) {
        Logger.debug("OtaApp", "Polling alarm set to expire at: " + j + " Current Time: " + SystemClock.elapsedRealtime());
        Intent intent = new Intent(MY_ACTION);
        intent.putExtra(REQUEST_ID, i);
        PendingIntent broadcast = PendingIntent.getBroadcast(this.mParent, i, intent, 335544320);
        this.mPendingIntent = broadcast;
        this.mAlarmManger.setExactAndAllowWhileIdle(2, j, broadcast);
    }
}
