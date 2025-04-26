package com.motorola.otalib.downloadservice.download.policy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.downloadservice.utils.DownloadServiceLogger;
import java.util.StringTokenizer;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DownloadPolicy {
    private ConnectivityManager cm;
    private TelephonyManager tm;
    private ZeroRatedServices zs;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum DownloadingChoices {
        WIFI_OK,
        WAN_OK,
        WIFI_ONLY,
        ROAMING,
        NO_WAN,
        WAN_DISALLOWED,
        PEAK_HOUR
    }

    public DownloadPolicy(ConnectivityManager connectivityManager, TelephonyManager telephonyManager, ZeroRatedServices zeroRatedServices) {
        this.cm = connectivityManager;
        this.tm = telephonyManager;
        this.zs = zeroRatedServices;
    }

    public boolean isDownloadAllowed(Context context, String str) {
        if (str != null && str.length() != 0) {
            String wanTypeAsString = NetworkUtils.getWanTypeAsString(context, this.cm, this.tm);
            if (wanTypeAsString == null) {
                wanTypeAsString = "OTHER";
            }
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "isDownloadAllowed: currently on: " + wanTypeAsString + "; disallowed nets are: " + str);
            StringTokenizer stringTokenizer = new StringTokenizer(str, ",");
            while (stringTokenizer.hasMoreTokens()) {
                String nextToken = stringTokenizer.nextToken();
                if (nextToken.compareToIgnoreCase(wanTypeAsString) == 0) {
                    DownloadServiceLogger.i(DownloadServiceLogger.TAG, "isDownloadAllowed: network not allowed " + nextToken);
                    return false;
                }
            }
        }
        return true;
    }

    public DownloadingChoices canIDownload(Context context, String str, boolean z, String str2, boolean z2) {
        String wanTypeAsString = NetworkUtils.getWanTypeAsString(context, this.cm, this.tm);
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIDownload: current wantype " + wanTypeAsString);
        if (NetworkUtils.isWifi(this.cm)) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIDownload:: on WiFi we can continue the on-going download");
            return DownloadingChoices.WIFI_OK;
        } else if (z) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIDownload:: WiFi-only package but not on WiFi; we cannot start a new download");
            return DownloadingChoices.WIFI_ONLY;
        } else if (!z2 && NetworkUtils.isRoaming(this.cm)) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIDownload:: downloading is discontinuted due to roaming");
            return DownloadingChoices.ROAMING;
        } else if (!NetworkUtils.isWan(this.cm)) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "DownloadPolicy.canIDownload: cannot continue downloading over a non wifi and non wan network; current network " + NetworkUtils.getWanTypeAsString(context, this.cm, this.tm) + " seems to be down");
            return DownloadingChoices.NO_WAN;
        } else if (!isDownloadAllowed(context, str2)) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIDownload: cannot downloaded over a disallowed network " + NetworkUtils.getWanTypeAsString(context, this.cm, this.tm));
            return DownloadingChoices.WAN_DISALLOWED;
        } else if (!TextUtils.isEmpty(str)) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIDownload: cannot downloaded over non-AdminAPN N/W " + wanTypeAsString);
            return DownloadingChoices.WAN_DISALLOWED;
        } else {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIDownload: no restriction in continuing the on-going download");
            return DownloadingChoices.WAN_OK;
        }
    }

    public boolean canIUseZeroRatedNetwork(Context context, boolean z, String str, String str2, String str3) {
        String wanTypeAsString = NetworkUtils.getWanTypeAsString(context, this.cm, this.tm);
        if (!NetworkUtils.isVerizonSIM(context)) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIUseZeroRatedNetwork: cannot download in zero rated Network over non-VZW network ");
            return false;
        } else if (NetworkUtils.isWifi(this.cm)) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIUseZeroRatedNetwork:: on WiFi; we can continue the on-going download");
            return false;
        } else if (TextUtils.isEmpty(str)) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIUseZeroRatedNetwork:: ZeroRated feature off");
            return false;
        } else if (z) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIUseZeroRatedNetwork:: WiFi-only package but not on WiFi; we cannot start a new download");
            return false;
        } else if (NetworkUtils.isRoaming(this.cm)) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIUseZeroRatedNetwork:: downloading is discontinuted due to roaming");
            return false;
        } else if (!NetworkUtils.isWan(this.cm)) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "DownloadPolicy.canIUseZeroRatedNetwork: cannot start/continue download over a non wifi and non wan network; current network " + wanTypeAsString + " seems to be down");
            return false;
        } else if (!isDownloadAllowed(context, str2)) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIUseZeroRatedNetwork: cannot downloaded over the disallowed network " + wanTypeAsString);
            return false;
        } else if (this.zs.getZeroRatedHostName(str3) == null) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIUseZeroRatedNetwork: cannot download in zero rated Network over null gratis hostname ");
            return false;
        } else if (wanTypeAsString != null && !"LTE".equalsIgnoreCase(wanTypeAsString) && !"EHRPD".equalsIgnoreCase(wanTypeAsString)) {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIUseZeroRatedNetwork:: Not on LTE/EHRPD network , we can't allow download over zero rated NW");
            return false;
        } else if (this.zs.getZeroRatedDBStatus()) {
            return true;
        } else {
            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "DownloadPolicy.canIUseZeroRatedNetwork: cannot download in zero rated Network over disabled/non-existing zero Rated pdn ");
            return false;
        }
    }

    public boolean canIDownloadUsingZeroRatedChannel() {
        return this.zs.zeroRatedChannelStatus();
    }

    public void startZeroRatedProcess() {
        this.zs.startZeroRatedChannel();
    }

    public void stopUsingZeroRatedChannel() {
        this.zs.stopZeroRatedChannel();
    }

    public String getCurrentNetworkType(Context context) {
        if (NetworkUtils.isWifi(this.cm)) {
            return "wifi";
        }
        return "carrier_network(" + NetworkUtils.getWanTypeAsString(context, this.cm, this.tm) + ")";
    }
}
