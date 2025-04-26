package com.motorola.otalib.cdsservice.requestdataobjects.builders;

import com.motorola.otalib.cdsservice.requestdataobjects.CheckRequest;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.downloadservice.utils.DownloadServiceSettings;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CheckRequestBuilder {
    public static CheckRequest from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new CheckRequest(jSONObject.getString(DownloadServiceSettings.KEY_ID), jSONObject.getLong("contentTimestamp"), jSONObject.getJSONObject("deviceInfo"), jSONObject.getJSONObject("extraInfo"), jSONObject.getJSONObject("identityInfo"), jSONObject.getString("triggeredBy"), jSONObject.getString("idType"));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "CheckRequestBuilder.from(object) caught exception :" + e);
            return null;
        }
    }

    public static CheckRequest from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "CheckRequestBuilder.from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(CheckRequest checkRequest) {
        if (checkRequest == null) {
            return null;
        }
        try {
            return new JSONObject().put(DownloadServiceSettings.KEY_ID, checkRequest.getId()).put("contentTimestamp", checkRequest.getContentTimestamp()).put("deviceInfo", checkRequest.getDeviceInfo()).put("extraInfo", checkRequest.getExtraInfo()).put("identityInfo", checkRequest.getIdentityInfo()).put("triggeredBy", checkRequest.getTriggerdBy()).put("idType", checkRequest.getIdType());
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "CheckRequestBuilder.toJSONObject(checkRequest) caught exception :" + e);
            return null;
        }
    }

    public static String toJSONString(CheckRequest checkRequest) {
        JSONObject jSONObject;
        if (checkRequest == null || (jSONObject = toJSONObject(checkRequest)) == null) {
            return null;
        }
        return jSONObject.toString();
    }
}
