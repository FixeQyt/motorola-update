package com.motorola.otalib.downloadservice.download.policy;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.downloadservice.utils.DownloadServiceLogger;
import com.motorola.otalib.downloadservice.utils.DownloadServiceSettings;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ZeroRatedManager {
    public static final String ACTION_ZERORATED_CHANNEL_ACTIVE = "com.motorola.downloadservice.ACTION_ZERORATED_CHANNEL_ACTIVE";
    public static final String ACTION_ZERORATED_CHANNEL_INACTIVE = "com.motorola.downloadservice.ACTION_ZERORATED_CHANNEL_INACTIVE";
    public static final String ACTION_ZERORATED_CLEANUP = "com.motorola.downloadservice.ACTION_ZERORATED_CLEANUP";
    public static final String ACTION_ZERORATED_CLEANUP_MESSAGE = "com.motorola.downloadservice.ACTION_ZERORATED_CLEANUP_MESSAGE";
    private static final String CONNECTED = "connected";
    private static final String DISCONNECTED = "disconnected";
    public static final String KEY_INACTIVE_REASON = "key_inactive_reason";
    private static Network mActiveAdminApnNetwork;
    private static ConnectivityManager.NetworkCallback mAdminApnNetworkCallback;
    private boolean doIneedToBroadcast = false;
    private boolean doIneedToBroadcastInActive = false;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    private DownloadServiceSettings mSettings;

    public ZeroRatedManager(Context context, DownloadServiceSettings downloadServiceSettings) {
        this.mContext = context;
        this.mSettings = downloadServiceSettings;
    }

    public void requestZeroRatedNetworkChannel() {
        NetworkRequest build = new NetworkRequest.Builder().addTransportType(0).addCapability(3).build();
        mAdminApnNetworkCallback = new AdminApnNetworkCallback();
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "ZeroRatedManager.requestZeroRatedNetworkChannel, requesting for VZWADMIN network");
        this.mConnectivityManager.requestNetwork(build, mAdminApnNetworkCallback, DownloadServiceSettings.DOWNLOAD_SOCKET_TIMEOUT);
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    private class AdminApnNetworkCallback extends ConnectivityManager.NetworkCallback {
        public AdminApnNetworkCallback() {
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "AdminApnNetworkCallback.onAvailable() for network: " + network);
            ZeroRatedManager zeroRatedManager = ZeroRatedManager.this;
            zeroRatedManager.mConnectivityManager = (ConnectivityManager) zeroRatedManager.mContext.getSystemService("connectivity");
            if (!Boolean.valueOf(ZeroRatedManager.this.mSettings.getConfigValue(DownloadServiceSettings.KEY_DO_NOT_BIND_OTA_PROCESS)).booleanValue()) {
                DownloadServiceLogger.v(DownloadServiceLogger.TAG, "AdminApnNetworkCallback.onAvailable() binding with adminapn network");
                if (!ZeroRatedManager.this.mConnectivityManager.bindProcessToNetwork(network)) {
                    ZeroRatedManager.this.sendCleanUpNotification("Failed adding network " + network + " to bindProcessToNetwork API");
                    return;
                }
            }
            Network unused = ZeroRatedManager.mActiveAdminApnNetwork = network;
            if (ZeroRatedManager.mActiveAdminApnNetwork != null) {
                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Current adminapn nethandler is " + ZeroRatedManager.mActiveAdminApnNetwork.getNetworkHandle());
            }
            ZeroRatedManager.this.doIneedToBroadcast = true;
            ZeroRatedManager.this.doIneedToBroadcastInActive = true;
            ZeroRatedManager.this.mSettings.setConfigValue(DownloadServiceSettings.KEY_ADMIN_APN_STATUS, ZeroRatedManager.CONNECTED);
            ZeroRatedManager.this.informZeroRatedChannelActive();
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "AdminApnNetworkCallback.onLost() for network: " + network);
            if (ZeroRatedManager.CONNECTED.equals(ZeroRatedManager.this.mSettings.getConfigValue(DownloadServiceSettings.KEY_ADMIN_APN_STATUS))) {
                Network unused = ZeroRatedManager.mActiveAdminApnNetwork = null;
                ZeroRatedManager.this.informZeroRatedChannelInActive("onLost");
                ZeroRatedManager.this.stopZeroRatedChannel();
            }
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onUnavailable() {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "AdminApnNetworkCallback.onUnavailable()");
            ZeroRatedManager.this.doIneedToBroadcastInActive = true;
            ZeroRatedManager.this.informZeroRatedChannelInActive("onUnavailable");
        }
    }

    public void stopZeroRatedChannel() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mConnectivityManager = connectivityManager;
        ConnectivityManager.NetworkCallback networkCallback = mAdminApnNetworkCallback;
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "zeroRatedManager, stopZeroRatedChannel(): unregistered admin apn network callback");
            mAdminApnNetworkCallback = null;
        }
        this.mSettings.setConfigValue(DownloadServiceSettings.KEY_ADMIN_APN_STATUS, DISCONNECTED);
        this.doIneedToBroadcast = false;
        this.doIneedToBroadcastInActive = false;
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "zeroRatedManager, stopZeroRatedChannel stopResult: " + this.mConnectivityManager.bindProcessToNetwork(null));
    }

    public boolean isZeroRatedNetworkActive() {
        boolean equals = CONNECTED.equals(this.mSettings.getConfigValue(DownloadServiceSettings.KEY_ADMIN_APN_STATUS));
        boolean z = mActiveAdminApnNetwork != null;
        DownloadServiceLogger.i(DownloadServiceLogger.TAG, "ZeroRatedManager.isZeroRatedNetworkActive, adminApnStatus: " + equals + "; adminApnNetworkActive: " + z);
        return equals && z;
    }

    protected void informZeroRatedChannelActive() {
        if (this.doIneedToBroadcast) {
            this.doIneedToBroadcast = false;
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "zeroRatedManager, Broadcasting ZERORATED_CHANNEL_ACTIVE intent ");
            BroadcastUtils.sendLocalBroadcast(this.mContext, new Intent(ACTION_ZERORATED_CHANNEL_ACTIVE));
            return;
        }
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "zeroRatedManager, ZERORATED_CHANNEL_ACTIVE intent already broadcasted ");
    }

    protected void informZeroRatedChannelInActive(String str) {
        if (this.doIneedToBroadcastInActive) {
            this.doIneedToBroadcastInActive = false;
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "zeroRatedManager, Broadcasting ZERORATED_CHANNEL_INACTIVE intent ");
            Intent intent = new Intent(ACTION_ZERORATED_CHANNEL_INACTIVE);
            intent.putExtra(KEY_INACTIVE_REASON, str);
            BroadcastUtils.sendLocalBroadcast(this.mContext, intent);
            return;
        }
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "zeroRatedManager, ZERORATED_CHANNEL_INACTIVE intent already broadcasted ");
    }

    public static Network returnActiveAdminApnNetwork() {
        return mActiveAdminApnNetwork;
    }

    public void sendCleanUpNotification(String str) {
        Intent intent = new Intent(ACTION_ZERORATED_CLEANUP);
        intent.putExtra(ACTION_ZERORATED_CLEANUP_MESSAGE, str);
        BroadcastUtils.sendLocalBroadcast(this.mContext, intent);
    }
}
