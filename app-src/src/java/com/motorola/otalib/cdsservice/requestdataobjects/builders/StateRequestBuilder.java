package com.motorola.otalib.cdsservice.requestdataobjects.builders;

import com.motorola.otalib.cdsservice.requestdataobjects.StateRequest;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.downloadservice.utils.DownloadServiceSettings;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class StateRequestBuilder {
    public static StateRequest from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new StateRequest(jSONObject.getString(DownloadServiceSettings.KEY_ID), jSONObject.optLong("contentTimestamp"), jSONObject.getJSONObject("deviceInfo"), jSONObject.getJSONObject("extraInfo"), jSONObject.getJSONObject("identityInfo"), jSONObject.getString("info"), jSONObject.getString("logs"), jSONObject.getString("idType"), jSONObject.getString("status"), jSONObject.optString("reportingTags"), jSONObject.optString("upgradeSource"), jSONObject.optJSONObject("stats"));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "StateRequestBuilder.from(object) caught exception :" + e);
            return null;
        }
    }

    public static StateRequest from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "StateRequestBuilder.from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(StateRequest stateRequest) {
        if (stateRequest == null) {
            return null;
        }
        try {
            return new JSONObject().put(DownloadServiceSettings.KEY_ID, stateRequest.getId()).put("contentTimestamp", stateRequest.getContentTimestamp()).put("deviceInfo", stateRequest.getDeviceInfo()).put("extraInfo", stateRequest.getExtraInfo()).put("identityInfo", stateRequest.getIdentityInfo()).put("info", stateRequest.getInfo()).put("logs", stateRequest.getLogs()).put("idType", stateRequest.getIdType()).put("status", stateRequest.getStatus()).put("reportingTags", stateRequest.getReportingTags()).put("upgradeSource", stateRequest.getUpgradeSource()).put("stats", stateRequest.getStats());
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "StateRequestBuilder.toJSONObject(StateRequest) caught exception :" + e);
            return null;
        }
    }

    public static String toJSONString(StateRequest stateRequest) {
        JSONObject jSONObject;
        if (stateRequest == null || (jSONObject = toJSONObject(stateRequest)) == null) {
            return null;
        }
        return jSONObject.toString();
    }
}
