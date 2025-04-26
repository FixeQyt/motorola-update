package com.motorola.otalib.cdsservice.webdataobjects.builders;

import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequest;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WebRequestBuilder {
    public static WebRequest from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            JSONObject jSONObject2 = jSONObject.getJSONObject("request");
            return new WebRequest(jSONObject2.getString("url"), jSONObject2.getInt("retries"), jSONObject2.getString("httpMethod"), WebRequestQueryParamsBuilder.from(jSONObject2.optJSONObject("queryParams")), WebRequestPayloadBuilder.from(jSONObject2.optJSONObject("payload")), jSONObject2.optString("proxyHost"), jSONObject2.optInt("proxyPort"));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "WebRequest:from(jsonObject) caught exception :" + e);
            return null;
        }
    }

    public static WebRequest from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "WebRequest:from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(WebRequest webRequest) throws JSONException {
        if (webRequest == null) {
            return null;
        }
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("request", new JSONObject().put("url", webRequest.getUrl()).put("retries", webRequest.getRetries()).put("httpMethod", webRequest.getHttpMethod()).put("queryParams", WebRequestQueryParamsBuilder.toJSONObject(webRequest.getQueryParams())).put("payload", WebRequestPayloadBuilder.toJSONObject(webRequest.getPayload())).put("proxyHost", webRequest.getProxyHost()).put("proxyPort", webRequest.getProxyPort()));
        return jSONObject;
    }

    public static String toJSONString(WebRequest webRequest) throws JSONException {
        if (webRequest == null) {
            return null;
        }
        return toJSONObject(webRequest).toString();
    }
}
