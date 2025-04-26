package com.motorola.otalib.cdsservice.webdataobjects.builders;

import com.motorola.otalib.cdsservice.responsedataobjects.builders.HashMapBuilder;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.cdsservice.webdataobjects.WebResponse;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WebResponseBuilder {
    public static WebResponse from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new WebResponse(jSONObject.optInt("statusCode", 1000), jSONObject.optJSONObject("payload"), HashMapBuilder.from(jSONObject.optJSONObject("headers")));
        } catch (Exception e) {
            CDSLogger.e(CDSLogger.TAG, "WebResponse.from(object) caught exception :" + e);
            return null;
        }
    }

    public static WebResponse from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "WebResponse.from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(WebResponse webResponse) throws JSONException {
        if (webResponse == null) {
            return null;
        }
        return new JSONObject().put("statusCode", webResponse.getStatusCode()).putOpt("payload", webResponse.getPayload()).putOpt("headers", HashMapBuilder.toJSONObject(webResponse.getHeaders()));
    }

    public static String toJSONString(WebResponse webResponse) throws JSONException {
        if (webResponse == null) {
            return null;
        }
        return toJSONObject(webResponse).toString();
    }
}
