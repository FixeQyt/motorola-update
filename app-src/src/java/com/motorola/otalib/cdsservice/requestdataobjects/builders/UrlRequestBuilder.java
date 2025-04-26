package com.motorola.otalib.cdsservice.requestdataobjects.builders;

import com.motorola.otalib.cdsservice.requestdataobjects.UrlRequest;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UrlRequestBuilder {
    public static UrlRequest from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new UrlRequest(jSONObject.getString("serverUrl"), jSONObject.getString("baseUrl"), jSONObject.getString("context"), jSONObject.getString("contextKey"), jSONObject.optString("state", null), jSONObject.optString("trackingId", "unknown"), jSONObject.optString("secure", "true"), jSONObject.optString("testurl", null));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "UrlRequestBuilder.from(object) caught exception :" + e);
            return null;
        }
    }

    public static UrlRequest from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "UrlRequestBuilder.from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(UrlRequest urlRequest) {
        if (urlRequest == null) {
            return null;
        }
        try {
            return new JSONObject().put("serverUrl", urlRequest.getServerUrl()).put("baseUrl", urlRequest.getBaseUrl()).put("context", urlRequest.getContext()).put("contextKey", urlRequest.getContextKey()).putOpt("state", urlRequest.getState()).putOpt("trackingId", urlRequest.getTrackingId()).putOpt("secure", urlRequest.getIsSecure()).putOpt("testurl", urlRequest.getTestUrl());
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "UrlRequestBuilder.toJSONObject(urlRequest) caught exception :" + e);
            return null;
        }
    }

    public static String toJSONString(UrlRequest urlRequest) {
        JSONObject jSONObject;
        if (urlRequest == null || (jSONObject = toJSONObject(urlRequest)) == null) {
            return null;
        }
        return jSONObject.toString();
    }
}
