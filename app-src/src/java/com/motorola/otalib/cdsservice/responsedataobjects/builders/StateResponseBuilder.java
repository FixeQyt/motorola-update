package com.motorola.otalib.cdsservice.responsedataobjects.builders;

import com.motorola.otalib.cdsservice.responsedataobjects.StateResponse;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class StateResponseBuilder {
    public static StateResponse from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new StateResponse(jSONObject.getBoolean("proceed"), jSONObject.getString("context"), jSONObject.getString("contextKey"), jSONObject.getLong("contentTimestamp"), jSONObject.optString("reportingTags"), jSONObject.optString("trackingId", "unknown"), jSONObject.optLong("pollAfterSeconds"), jSONObject.optInt("smartUpdateBitmap", -1), jSONObject.optJSONObject("content"), jSONObject.optBoolean("uploadFailureLogs", false));
        } catch (Exception e) {
            CDSLogger.e(CDSLogger.TAG, "StateResponseBuilder.from(object) caught exception :" + e);
            return null;
        }
    }

    public static StateResponse from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "StateResponseBuilder.from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(StateResponse stateResponse) throws JSONException {
        if (stateResponse == null) {
            return null;
        }
        return new JSONObject().put("proceed", stateResponse.proceed()).put("context", stateResponse.getContext()).put("contextKey", stateResponse.getContextKey()).put("contentTimestamp", stateResponse.getContextTimeStamp()).put("reportingTags", stateResponse.getReportingTags()).put("trackingId", stateResponse.getTrackingId()).put("pollAfterSeconds", stateResponse.getPollAfterSeconds()).put("smartUpdateBitmap", stateResponse.getSmartUpdateBitmap()).put("content", stateResponse.getContent()).put("uploadFailureLogs", stateResponse.isUploadFailureLogsEnabled());
    }

    public static String toJSONString(StateResponse stateResponse) throws JSONException {
        if (stateResponse == null) {
            return null;
        }
        return toJSONObject(stateResponse).toString();
    }
}
