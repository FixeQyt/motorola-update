package com.motorola.otalib.cdsservice.responsedataobjects.builders;

import com.motorola.otalib.cdsservice.responsedataobjects.Response;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ResponseBuilder {
    public static Response from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new Response(jSONObject.getBoolean("proceed"), jSONObject.getString("context"), jSONObject.getString("contextKey"), jSONObject.getLong("contentTimestamp"), jSONObject.optString("reportingTags"), jSONObject.optString("trackingId", "unknown"), jSONObject.optLong("pollAfterSeconds"), jSONObject.optInt("smartUpdateBitmap", -1), jSONObject.optJSONObject("content"), jSONObject.optBoolean("uploadFailureLogs", false));
        } catch (IllegalArgumentException e) {
            CDSLogger.e(CDSLogger.TAG, "ResponseBuilder.from(object) caught exception :" + e);
            return null;
        } catch (JSONException e2) {
            CDSLogger.e(CDSLogger.TAG, "ResponseBuilder.from(object) caught exception :" + e2);
            return null;
        }
    }

    public static Response from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "ResponseBuilder.from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(Response response) throws JSONException {
        if (response == null) {
            return null;
        }
        return new JSONObject().put("proceed", response.proceed()).put("context", response.getContext()).put("contextKey", response.getContextKey()).put("contentTimestamp", response.getContextTimeStamp()).put("reportingTags", response.getReportingTags()).put("trackingId", response.getTrackingId()).put("pollAfterSeconds", response.getPollAfterSeconds()).put("smartUpdateBitmap", response.getSmartUpdateBitmap()).put("content", response.getContent()).put("uploadFailureLogs", response.isUploadFailureLogsEnabled());
    }

    public static String toJSONString(Response response) throws JSONException {
        if (response == null) {
            return null;
        }
        return toJSONObject(response).toString();
    }
}
