package com.motorola.otalib.cdsservice.requestdataobjects.builders;

import com.motorola.otalib.cdsservice.requestdataobjects.ResourcesRequest;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.downloadservice.utils.DownloadServiceSettings;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ResourcesRequestBuilder {
    public static ResourcesRequest from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new ResourcesRequest(jSONObject.getString(DownloadServiceSettings.KEY_ID), jSONObject.getLong("contentTimestamp"), jSONObject.getJSONObject("deviceInfo"), jSONObject.getJSONObject("extraInfo"), jSONObject.getJSONObject("identityInfo"), jSONObject.getString("idType"), jSONObject.getString("reportingTags"), jSONObject.optString("reason"));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "ResourcesRequestBuilder.from(object) caught exception :" + e);
            return null;
        }
    }

    public static ResourcesRequest from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "ResourcesRequestBuilder.from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(ResourcesRequest resourcesRequest) {
        if (resourcesRequest == null) {
            return null;
        }
        try {
            return new JSONObject().put(DownloadServiceSettings.KEY_ID, resourcesRequest.getId()).put("contentTimestamp", resourcesRequest.getContentTimestamp()).put("deviceInfo", resourcesRequest.getDeviceInfo()).put("extraInfo", resourcesRequest.getExtraInfo()).put("identityInfo", resourcesRequest.getIdentityInfo()).put("idType", resourcesRequest.getIdType()).put("reportingTags", resourcesRequest.getReportingTags()).put("reason", resourcesRequest.getReason());
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "ResourcesRequestBuilder.toJSONObject(resourcesRequest) caught exception :" + e);
            return null;
        }
    }

    public static String toJSONString(ResourcesRequest resourcesRequest) {
        JSONObject jSONObject;
        if (resourcesRequest == null || (jSONObject = toJSONObject(resourcesRequest)) == null) {
            return null;
        }
        return jSONObject.toString();
    }
}
