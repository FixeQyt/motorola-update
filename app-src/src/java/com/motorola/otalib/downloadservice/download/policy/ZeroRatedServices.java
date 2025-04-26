package com.motorola.otalib.downloadservice.download.policy;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.motorola.otalib.downloadservice.utils.DownloadServiceLogger;
import com.motorola.otalib.downloadservice.utils.DownloadServiceSettings;
import java.net.URL;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ZeroRatedServices {
    public static final String CARRIER_ENABLED = "carrier_enabled";
    public static final Uri CONTENT_URI = Uri.parse("content://telephony/carriers");
    private static ZeroRatedServices instance = null;
    private static DownloadServiceSettings mDownloadServiceSettings;
    private Context context;

    public ZeroRatedServices(Context context, DownloadServiceSettings downloadServiceSettings) {
        this.context = context;
        mDownloadServiceSettings = downloadServiceSettings;
    }

    public static synchronized ZeroRatedServices getZeroRatedServices(Context context, DownloadServiceSettings downloadServiceSettings) {
        ZeroRatedServices zeroRatedServices;
        synchronized (ZeroRatedServices.class) {
            if (instance == null) {
                instance = new ZeroRatedServices(context, downloadServiceSettings);
            }
            zeroRatedServices = instance;
        }
        return zeroRatedServices;
    }

    public boolean getZeroRatedDBStatus() {
        Cursor cursor = null;
        boolean z = false;
        try {
            try {
                cursor = this.context.getContentResolver().query(CONTENT_URI, new String[]{CARRIER_ENABLED}, "apn='VZWADMIN'", null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    if (Integer.parseInt(cursor.getString(0)) != 0) {
                        z = true;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (!z) {
                    DownloadServiceLogger.i(DownloadServiceLogger.TAG, "ZeroRatedServices.getZeroRatedDBStatus::.ZeroRated(VZWADMIN) db status: " + z);
                }
                return z;
            } catch (Exception unused) {
                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "ZeroRatedServices.getZeroRatedDBStatus::ZeroRated(VZWADMIN) apn class doesn't exist");
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public String getZeroRatedHostName(String str) {
        try {
            String optString = new JSONObject(str).optString("adminApnUrl", "");
            if (TextUtils.isEmpty(optString)) {
                DownloadServiceLogger.i(DownloadServiceLogger.TAG, "getZeroRatedHostName:: could not find adminApnUrl in contentResources");
                return null;
            }
            URL url = new URL(optString);
            DownloadServiceLogger.i(DownloadServiceLogger.TAG, "getZeroRatedHostName:: adminApnHost : " + url.getHost());
            return url.getHost();
        } catch (Exception unused) {
            DownloadServiceLogger.i(DownloadServiceLogger.TAG, "getZeroRatedHostName:: error parsing adminApnUrl from contentResources");
            return null;
        }
    }

    public void startZeroRatedChannel() {
        new ZeroRatedManager(this.context, mDownloadServiceSettings).requestZeroRatedNetworkChannel();
    }

    public boolean zeroRatedChannelStatus() {
        if (new ZeroRatedManager(this.context, mDownloadServiceSettings).isZeroRatedNetworkActive()) {
            return true;
        }
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "ZeroRatedServices.zeroRatedChannelStatus:: zero rated nw not active");
        return false;
    }

    public void stopZeroRatedChannel() {
        new ZeroRatedManager(this.context, mDownloadServiceSettings).stopZeroRatedChannel();
    }
}
