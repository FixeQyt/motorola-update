package com.motorola.ccc.ota.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.motorola.otalib.common.CommonLogger;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class LocationUtils {
    public static int checkLocationPermission(Context context) {
        return (context.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0 && context.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == 0) ? 0 : -1;
    }

    public static Location getLocation(Context context) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService("location");
            boolean isProviderEnabled = locationManager.isProviderEnabled("gps");
            boolean isProviderEnabled2 = locationManager.isProviderEnabled("network");
            if (checkLocationPermission(context) == 0) {
                Location lastKnownLocation = isProviderEnabled ? locationManager.getLastKnownLocation("gps") : null;
                Location lastKnownLocation2 = isProviderEnabled2 ? locationManager.getLastKnownLocation("network") : null;
                if (lastKnownLocation != null && lastKnownLocation2 != null) {
                    if (lastKnownLocation.getAccuracy() >= lastKnownLocation2.getAccuracy()) {
                        return lastKnownLocation;
                    }
                    return lastKnownLocation2;
                }
                if (lastKnownLocation == null) {
                    if (lastKnownLocation2 == null) {
                        return null;
                    }
                    return lastKnownLocation2;
                }
                return lastKnownLocation;
            }
            CommonLogger.d(CommonLogger.TAG, "Location permission not set.");
            return null;
        } catch (Exception e) {
            CommonLogger.e(CommonLogger.TAG, "Exception in LocationUtils, getLocation: " + e);
            return null;
        }
    }

    public static String getCountryCode(Context context) {
        String str;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager == null) {
            str = "US";
        } else {
            String networkCountryIso = telephonyManager.getNetworkCountryIso();
            if (!TextUtils.isEmpty(networkCountryIso)) {
                CommonLogger.d(CommonLogger.TAG, "Country code using Network Info is: " + networkCountryIso);
                return networkCountryIso;
            }
            str = telephonyManager.getSimCountryIso();
            if (!TextUtils.isEmpty(str)) {
                CommonLogger.v(CommonLogger.TAG, "Country code using SIM card info is: " + str);
                return str;
            }
        }
        Location location = getLocation(context);
        if (location != null) {
            try {
                List<Address> fromLocation = new Geocoder(context, Locale.getDefault()).getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (fromLocation != null && !fromLocation.isEmpty()) {
                    str = fromLocation.get(0).getCountryName();
                    CommonLogger.v(CommonLogger.TAG, "Country code using Location info is: " + str);
                    return str;
                }
            } catch (IOException unused) {
                CommonLogger.e(CommonLogger.TAG, "Unable to get current Location");
            }
        } else {
            CommonLogger.i(CommonLogger.TAG, "No last known Location");
        }
        return str;
    }

    public static boolean isDeviceInChina(Context context) {
        String countryCode = getCountryCode(context);
        if (TextUtils.isEmpty(countryCode)) {
            return false;
        }
        return countryCode.equalsIgnoreCase("CN") || countryCode.equalsIgnoreCase("China");
    }
}
