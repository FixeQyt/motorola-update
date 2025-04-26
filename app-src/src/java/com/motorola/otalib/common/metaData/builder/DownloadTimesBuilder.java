package com.motorola.otalib.common.metaData.builder;

import com.motorola.otalib.common.metaData.DownloadTimes;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
class DownloadTimesBuilder {
    DownloadTimesBuilder() {
    }

    private static long[][] getDownloadTimes(JSONArray jSONArray) {
        new Vector();
        return (long[][]) Array.newInstance(Long.TYPE, 0, 0);
    }

    public static DownloadTimes from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new DownloadTimes(jSONObject.getString("useLocalTz"), getDownloadTimes(jSONObject.getJSONArray("timeSlots")));
        } catch (JSONException unused) {
            return null;
        }
    }

    public static DownloadTimes from(String str) {
        try {
            return from(new JSONObject(str));
        } catch (JSONException unused) {
            return null;
        }
    }

    public static JSONObject toJSONObject(DownloadTimes downloadTimes) throws JSONException {
        if (downloadTimes == null) {
            return null;
        }
        return new JSONObject().put("useLocalTz", downloadTimes.isUseLocalTz()).put("timeSlots", new JSONArray((Collection) Collections.emptyList()));
    }

    public static String toJSONString(DownloadTimes downloadTimes) {
        if (downloadTimes == null) {
            return null;
        }
        return toJSONString(downloadTimes);
    }
}
