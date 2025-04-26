package com.motorola.otalib.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.motorola.otalib.common.CommonLogger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class NetworkUtils {
    private static final String SOFTBANK_APN_NAME = "plus.acs.jp.v6";
    private static String[] NETWORK_TAG_LIST = {"WIFI", "CELL", "USEADMINAPN"};
    public static final HashMap<String, List<String>> DEFAULT_VZW_SIM = new HashMap<String, List<String>>() { // from class: com.motorola.otalib.common.utils.NetworkUtils.1
        {
            put("20404", Arrays.asList("BA01270000000000", "BAE0000000000000"));
            put("311480", Arrays.asList("", "BAE2000000000000", "BA01270000000000", "BAE0000000000000"));
            put("310590", Collections.singletonList("BA01270000000000"));
            put("310591", Collections.singletonList("BA01270000000000"));
            put("310592", Collections.singletonList("BA01270000000000"));
            put("310593", Collections.singletonList("BA01270000000000"));
            put("310594", Collections.singletonList("BA01270000000000"));
            put("310595", Collections.singletonList("BA01270000000000"));
            put("310596", Collections.singletonList("BA01270000000000"));
            put("310597", Collections.singletonList("BA01270000000000"));
            put("310598", Collections.singletonList("BA01270000000000"));
            put("310599", Collections.singletonList("BA01270000000000"));
        }
    };

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum networkType {
        WIFI,
        CELLULAR,
        CELL3G,
        CELL4G,
        CELL5G,
        ROAMING,
        UNKNOWN
    }

    public static boolean hasNetwork(ConnectivityManager connectivityManager) {
        NetworkCapabilities networkCapabilities;
        return (connectivityManager == null || (networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork())) == null || !networkCapabilities.hasCapability(12)) ? false : true;
    }

    public static boolean isSoftBankApn(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(connectivityManager.getActiveNetwork());
        if (networkInfo != null && "plus.acs.jp.v6".equalsIgnoreCase(networkInfo.getExtraInfo())) {
            CommonLogger.v(CommonLogger.TAG, "isSoftbankApn: Softbank apn network, return true");
            return true;
        }
        CommonLogger.v(CommonLogger.TAG, "isSoftbankApn: not Softbank apn network, return false");
        return false;
    }

    public static int getWanType(Context context, ConnectivityManager connectivityManager, TelephonyManager telephonyManager) {
        if ((UpgradeUtils.checkIfAlreadyhavePermission(context, "android.permission.READ_PHONE_STATE") || UpgradeUtils.checkIfAlreadyhavePermission(context, "android.permission.READ_PRIVILEGED_PHONE_STATE")) && hasNetwork(connectivityManager)) {
            return NetworkType.fromCode(telephonyManager.getNetworkType()).getNetworkTypeInt();
        }
        return -1;
    }

    public static String getWanTypeAsString(Context context, ConnectivityManager connectivityManager, TelephonyManager telephonyManager) {
        if ((UpgradeUtils.checkIfAlreadyhavePermission(context, "android.permission.READ_PHONE_STATE") || UpgradeUtils.checkIfAlreadyhavePermission(context, "android.permission.READ_PRIVILEGED_PHONE_STATE")) && hasNetwork(connectivityManager)) {
            return NetworkType.fromCode(telephonyManager.getNetworkType()).name();
        }
        return null;
    }

    public static boolean isWifi(ConnectivityManager connectivityManager) {
        return connectivityManager != null && (1 == getNetworkCapabilityType(connectivityManager) || 4 == getNetworkCapabilityType(connectivityManager));
    }

    public static boolean isWan(ConnectivityManager connectivityManager) {
        return connectivityManager != null && getNetworkCapabilityType(connectivityManager) == 0;
    }

    public static boolean isRoaming(ConnectivityManager connectivityManager) {
        Network activeNetwork;
        NetworkCapabilities networkCapabilities;
        if (connectivityManager == null || (activeNetwork = connectivityManager.getActiveNetwork()) == null || (networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)) == null || !networkCapabilities.hasCapability(12)) {
            return false;
        }
        return !networkCapabilities.hasCapability(18);
    }

    public static String getCurrentNetworkType(Context context, ConnectivityManager connectivityManager, TelephonyManager telephonyManager) {
        if (isWifi(connectivityManager)) {
            return "wifi";
        }
        return "carrier_network(" + getWanTypeAsString(context, connectivityManager, telephonyManager) + ")";
    }

    public static Network returnActiveNetwork(ConnectivityManager connectivityManager) {
        if (connectivityManager == null) {
            return null;
        }
        return connectivityManager.getActiveNetwork();
    }

    public static boolean isNetWorkConnected(ConnectivityManager connectivityManager) {
        NetworkCapabilities networkCapabilities;
        if (connectivityManager == null || (networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork())) == null || !networkCapabilities.hasCapability(12)) {
            return false;
        }
        return networkCapabilities.hasTransport(1) || networkCapabilities.hasTransport(0) || networkCapabilities.hasTransport(3) || networkCapabilities.hasTransport(4);
    }

    public static boolean isZeroRatedNetworkActive(ConnectivityManager connectivityManager) {
        return getNetworkCapabilityType(connectivityManager) == 3;
    }

    public static boolean checkWhetherUserDisabledCellularNetwork(ConnectivityManager connectivityManager) {
        Network[] allNetworks;
        if (connectivityManager == null || (allNetworks = connectivityManager.getAllNetworks()) == null) {
            return true;
        }
        for (Network network : allNetworks) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities != null && networkCapabilities.hasCapability(12) && networkCapabilities.hasTransport(0)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAdminApn(ConnectivityManager connectivityManager) {
        Network activeNetwork;
        NetworkCapabilities networkCapabilities;
        if (connectivityManager == null || (activeNetwork = connectivityManager.getActiveNetwork()) == null || (networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)) == null || !networkCapabilities.hasCapability(12)) {
            return false;
        }
        return networkCapabilities.hasCapability(3);
    }

    public static int getNetworkCapabilityType(ConnectivityManager connectivityManager) {
        if (connectivityManager != null) {
            return getNetworkCapabilityType(connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork()));
        }
        return -1;
    }

    public static int getNetworkCapabilityType(NetworkCapabilities networkCapabilities) {
        if (networkCapabilities != null && networkCapabilities.hasCapability(12)) {
            if (networkCapabilities.hasTransport(1)) {
                return 1;
            }
            if (networkCapabilities.hasTransport(4)) {
                return 4;
            }
            if (networkCapabilities.hasTransport(0)) {
                return 0;
            }
            if (networkCapabilities.hasTransport(3)) {
                return 3;
            }
            int i = 2;
            if (!networkCapabilities.hasCapability(2)) {
                i = 10;
                if (!networkCapabilities.hasCapability(10)) {
                    if (networkCapabilities.hasCapability(3)) {
                        return 3;
                    }
                }
            }
            return i;
        }
        return -1;
    }

    public static boolean isNetworkTagValid(String str) {
        for (String str2 : NETWORK_TAG_LIST) {
            if (str2.equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVerizonSIM(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        String simOperator = telephonyManager.getSimOperator();
        String groupIdLevel1 = telephonyManager.getGroupIdLevel1();
        String subscriberId = telephonyManager.getSubscriberId();
        if (!TextUtils.isEmpty(simOperator) && !TextUtils.isEmpty(subscriberId) && !TextUtils.isEmpty(groupIdLevel1)) {
            if (BuildPropertyUtils.isProductWaveAtleastRefWave("2023.4")) {
                List<String> list = DEFAULT_VZW_SIM.get(simOperator);
                if (list != null) {
                    return list.isEmpty() || list.contains(groupIdLevel1.toUpperCase(Locale.ROOT));
                }
                return false;
            }
            String[] strArr = {"311480", "20404"};
            String[] strArr2 = {"", "BAE0000000000000", "BA01270000000000"};
            for (int i = 0; i < 2; i++) {
                if (((!TextUtils.isEmpty(simOperator) && simOperator.equals(strArr[i])) || (!TextUtils.isEmpty(subscriberId) && subscriberId.startsWith(strArr[i]))) && (TextUtils.isEmpty(strArr2[i]) || strArr2[i].equals(groupIdLevel1))) {
                    CommonLogger.d(CommonLogger.TAG, "It is Verizon SIM");
                    return true;
                }
            }
        }
        return false;
    }

    public static String getNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        String wanTypeAsString = getWanTypeAsString(context, connectivityManager, (TelephonyManager) context.getSystemService("phone"));
        if (isWifi(connectivityManager)) {
            return networkType.WIFI.name();
        }
        if (!isWan(connectivityManager) || "UNKNOWN".equals(wanTypeAsString)) {
            return networkType.UNKNOWN.name();
        }
        if (isRoaming(connectivityManager)) {
            return networkType.ROAMING.name();
        }
        if ("LTE".equals(wanTypeAsString)) {
            return networkType.CELL4G.name();
        }
        if ("EHRPD".equals(wanTypeAsString) || "HSPA".equals(wanTypeAsString) || "HSPAPLUS".equals(wanTypeAsString) || "HSDPA".equals(wanTypeAsString) || "UMTS".equals(wanTypeAsString)) {
            return networkType.CELL3G.name();
        }
        if ("NR".equals(wanTypeAsString)) {
            return networkType.CELL5G.name();
        }
        return networkType.CELLULAR.name();
    }
}
