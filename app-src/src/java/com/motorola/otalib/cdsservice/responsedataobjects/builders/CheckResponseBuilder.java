package com.motorola.otalib.cdsservice.responsedataobjects.builders;

import com.motorola.otalib.cdsservice.responsedataobjects.CheckResponse;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CheckResponseBuilder {
    public static CheckResponse from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new CheckResponse(jSONObject.getBoolean("proceed"), jSONObject.getString("context"), jSONObject.getString("contextKey"), jSONObject.getLong("contentTimestamp"), jSONObject.optString("reportingTags"), jSONObject.optString("trackingId", "unknown"), jSONObject.optLong("pollAfterSeconds"), jSONObject.optJSONObject("content"), jSONObject.optInt("smartUpdateBitmap", -1), jSONObject.optJSONObject("settings"), jSONObject.optBoolean("uploadFailureLogs", false));
        } catch (Exception e) {
            CDSLogger.e(CDSLogger.TAG, "CheckResponseBuilder.from(object) caught exception :" + e);
            return null;
        }
    }

    public static CheckResponse from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "CheckResponseBuilder.from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(CheckResponse checkResponse) throws JSONException {
        if (checkResponse == null) {
            return null;
        }
        return new JSONObject().put("proceed", checkResponse.proceed()).put("context", checkResponse.getContext()).put("contextKey", checkResponse.getContextKey()).put("contentTimestamp", checkResponse.getContextTimeStamp()).put("reportingTags", checkResponse.getReportingTags()).put("trackingId", checkResponse.getTrackingId()).put("pollAfterSeconds", checkResponse.getPollAfterSeconds()).put("content", checkResponse.getContent()).put("smartUpdateBitmap", checkResponse.getSmartUpdateBitmap()).put("settings", checkResponse.getSettings()).put("uploadFailureLogs", checkResponse.isUploadFailureLogsEnabled());
    }

    public static String toJSONString(CheckResponse checkResponse) throws JSONException {
        if (checkResponse == null) {
            return null;
        }
        return toJSONObject(checkResponse).toString();
    }
}
