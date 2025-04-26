package com.motorola.ccc.ota;

import android.net.ConnectivityManager;
import com.motorola.otalib.common.utils.NetworkUtils;
import java.util.EnumSet;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
class CusPolicy {
    private ConnectivityManager cm;
    private final String CHECK_WIFI_FOR_BOTA = "com.motorola.ccc.ota.R.bool.check_WIFI_for_BOTA";
    EnumSet<ConnectionChoice> preferredCheckForUpdateConnections = null;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    enum ConnectionChoice {
        WIFI
    }

    boolean checkForUpdateOnWifiOnly() {
        return false;
    }

    public CusPolicy(ConnectivityManager connectivityManager) {
        this.cm = connectivityManager;
    }

    public boolean canICheckForUpdate(boolean z) {
        if (!checkForUpdateOnWifiOnly() || this.preferredCheckForUpdateConnections == null) {
            return true;
        }
        return this.preferredCheckForUpdateConnections.contains(NetworkUtils.isWifi(this.cm) ? ConnectionChoice.WIFI : null);
    }
}
