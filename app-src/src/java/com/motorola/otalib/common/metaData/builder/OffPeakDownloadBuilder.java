package com.motorola.otalib.common.metaData.builder;

import com.motorola.otalib.common.metaData.OffPeakDownload;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
class OffPeakDownloadBuilder {
    OffPeakDownloadBuilder() {
    }

    public static OffPeakDownload from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new OffPeakDownload(jSONObject.getString("useLocalTz"), jSONObject.getInt("promotionTime"), jSONObject.getInt("startTime"), jSONObject.getInt("duration"));
        } catch (JSONException unused) {
            return null;
        }
    }

    public static OffPeakDownload from(String str) {
        try {
            return from(new JSONObject(str));
        } catch (JSONException unused) {
            return null;
        }
    }

    public static JSONObject toJSONObject(OffPeakDownload offPeakDownload) throws JSONException {
        if (offPeakDownload == null) {
            return null;
        }
        return new JSONObject().put("useLocalTz", offPeakDownload.isUseLocalTz()).put("promotionTime", offPeakDownload.getPromotionTime()).put("startTime", offPeakDownload.getStartTime()).put("duration", offPeakDownload.getDuration());
    }

    public static String toJSONString(OffPeakDownload offPeakDownload) {
        if (offPeakDownload == null) {
            return null;
        }
        return toJSONString(offPeakDownload);
    }
}
