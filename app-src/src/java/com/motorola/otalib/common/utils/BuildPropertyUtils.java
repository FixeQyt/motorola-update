package com.motorola.otalib.common.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import com.motorola.otalib.common.CommonLogger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class BuildPropertyUtils {
    public static final String CHANNEL_ID = "channel_id";
    private static final String IS_PRODUCTION_DEVICE = "ro.product.is_production";
    public static final String MOTO_SETTINGS_UNKNOWN = "unknown";
    private static final String RO_AB_UPDATE = "ro.build.ab_update";
    private static final String SERIAL_NUMBER_NOT_AVAILABLE = "SERIAL_NUMBER_NOT_AVAILABLE";

    public static String getId(Context context) {
        if (UpgradeUtils.checkIfAlreadyhavePermission(context, "android.permission.READ_PRIVILEGED_PHONE_STATE")) {
            String serial = Build.getSerial();
            return TextUtils.isEmpty(serial) ? SERIAL_NUMBER_NOT_AVAILABLE : serial;
        }
        return SERIAL_NUMBER_NOT_AVAILABLE;
    }

    public static boolean isSecure() {
        if (Build.FINGERPRINT == null || !Build.FINGERPRINT.contains("release-keys")) {
            return false;
        }
        CommonLogger.d(CommonLogger.TAG, "Secure Build.");
        return true;
    }

    public static boolean isDogfoodDevice() {
        String systemStringProperty = getSystemStringProperty(IS_PRODUCTION_DEVICE);
        return !TextUtils.isEmpty(systemStringProperty) && systemStringProperty.toLowerCase().equals("false");
    }

    public static boolean isChinaDevice(Context context) {
        String systemStringProperty = getSystemStringProperty("ro.mot.build.customerid");
        String systemStringProperty2 = getSystemStringProperty("ro.lenovo.region");
        String systemStringProperty3 = getSystemStringProperty("ro.product.is_prc");
        if (isDogfoodDevice()) {
            boolean z = "retcn".equalsIgnoreCase(getCarrierName(context)) || "cmcc".equalsIgnoreCase(getCarrierName(context)) || "ctcn".equalsIgnoreCase(getCarrierName(context)) || "cucn".equalsIgnoreCase(getCarrierName(context)) || "cbncn".equalsIgnoreCase(getCarrierName(context));
            if (TextUtils.isEmpty(systemStringProperty) || !"china".equals(systemStringProperty)) {
                return (!TextUtils.isEmpty(systemStringProperty2) && "prc".equals(systemStringProperty2)) || Boolean.parseBoolean(systemStringProperty3) || z;
            }
            return true;
        } else if (TextUtils.isEmpty(systemStringProperty) || !"china".equals(systemStringProperty)) {
            return (!TextUtils.isEmpty(systemStringProperty2) && "prc".equals(systemStringProperty2)) || Boolean.parseBoolean(systemStringProperty3);
        } else {
            return true;
        }
    }

    public static boolean isLenovoDeBrandDevice() {
        return Boolean.parseBoolean(getSystemStringProperty("ro.product.white_label"));
    }

    public static String getSystemStringProperty(String str) {
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            return (String) cls.getMethod("get", String.class).invoke(cls, str);
        } catch (Exception e) {
            CommonLogger.e(CommonLogger.TAG, "get " + str + " from SystemProperties failed - exception : " + e.toString());
            return null;
        }
    }

    public static boolean getSystemBooleanProperty(String str, boolean z) {
        try {
            boolean booleanValue = ((Boolean) Class.forName("android.os.SystemProperties").getDeclaredMethod("getBoolean", String.class, Boolean.TYPE).invoke(null, str, Boolean.valueOf(z))).booleanValue();
            CommonLogger.e(CommonLogger.TAG, "BuildPropertyUtils.getSystemBooleanProperty " + booleanValue);
            return booleanValue;
        } catch (Exception e) {
            CommonLogger.e(CommonLogger.TAG, "get " + str + " from SystemProperties failed - exception : " + e.toString());
            return z;
        }
    }

    public static String getSystemStringProperty(String str, String str2) {
        try {
            return (String) Class.forName("android.os.SystemProperties").getDeclaredMethod("get", String.class, String.class).invoke(null, str, str2);
        } catch (Exception e) {
            CommonLogger.e(CommonLogger.TAG, "get " + str + " from SystemProperties failed - exception : " + e.toString());
            return str2;
        }
    }

    public static int getSystemIntProperty(String str, int i) {
        try {
            return ((Integer) Class.forName("android.os.SystemProperties").getDeclaredMethod("getInt", String.class, Integer.TYPE).invoke(null, str, Integer.valueOf(i))).intValue();
        } catch (Exception e) {
            CommonLogger.e(CommonLogger.TAG, "get " + str + " from SystemProperties failed - exception : " + e.toString());
            return i;
        }
    }

    public static boolean isProductWaveAtleastRefWave(String str) {
        String systemStringProperty = getSystemStringProperty("ro.mot.product_wave");
        boolean z = false;
        if (systemStringProperty != null && str != null) {
            String[] split = systemStringProperty.split("\\.");
            String[] split2 = str.split("\\.");
            if (split != null && split.length == 2 && split2 != null && split2.length == 2) {
                try {
                    int parseInt = Integer.parseInt(split[0]);
                    int parseInt2 = Integer.parseInt(split[1]);
                    int parseInt3 = Integer.parseInt(split2[0]);
                    int parseInt4 = Integer.parseInt(split2[1]);
                    if (parseInt >= parseInt3 && (parseInt > parseInt3 || parseInt2 >= parseInt4)) {
                        z = true;
                    }
                } catch (NumberFormatException e) {
                    CommonLogger.e(CommonLogger.TAG, "Exception in isProductWaveAtleastRefWave:" + e.getMessage());
                }
            }
            CommonLogger.d(CommonLogger.TAG, "productWave = " + systemStringProperty + " refWave = " + str + " isWaveAtleast = " + z);
        }
        return z;
    }

    public static boolean doesDeviceSupportAbUpdate() {
        String systemStringProperty = getSystemStringProperty(RO_AB_UPDATE);
        return !TextUtils.isEmpty(systemStringProperty) && systemStringProperty.toLowerCase().equals("true");
    }

    public static String generateSHA1(String str) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(str.getBytes());
            byte[] digest = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", Integer.valueOf(b & 255)));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException unused) {
            CommonLogger.e(CommonLogger.TAG, "Exception while generating SHA-1 for " + str);
            return "";
        }
    }

    public static int getApkVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            CommonLogger.d(CommonLogger.TAG, "Exception while getting versionCode :" + e);
            return -1;
        }
    }

    public static String getMotoSettingValueAsString(Context context, String str) {
        try {
            String str2 = (String) Class.forName("com.motorola.android.provider.MotorolaSettings$Global").getMethod("getString", ContentResolver.class, String.class).invoke(null, context.getContentResolver(), str);
            return str2 != null ? str2 : "unknown";
        } catch (Exception e) {
            CommonLogger.e(CommonLogger.TAG, "get " + str + " from MotorolaSettings failed - exception : " + e.toString());
            return "unknown";
        }
    }

    public static String getCarrierName(Context context) {
        return getMotoSettingValueAsString(context, "channel_id");
    }
}
