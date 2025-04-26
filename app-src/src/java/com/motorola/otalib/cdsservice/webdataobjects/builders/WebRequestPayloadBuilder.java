package com.motorola.otalib.cdsservice.webdataobjects.builders;

import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequestPayload;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequestPayloadType;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WebRequestPayloadBuilder {
    public static WebRequestPayload from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            return new WebRequestPayload(WebRequestPayloadType.valueOf(jSONObject.getString("type")), jSONObject.getString(FileUtils.DATA));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "WebRequestPayloadBuilder:from() Caught JSON exception" + e);
            return null;
        }
    }

    public static WebRequestPayload from(String str) {
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "WebRequestPayloadBuilder:from() Caught JSON exception" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(WebRequestPayload webRequestPayload) throws JSONException {
        if (webRequestPayload == null) {
            return null;
        }
        return new JSONObject().put("type", webRequestPayload.getType().name()).put(FileUtils.DATA, webRequestPayload.getData());
    }

    public static String toJSONString(WebRequestPayload webRequestPayload) throws JSONException {
        if (webRequestPayload == null) {
            return null;
        }
        return toJSONObject(webRequestPayload).toString();
    }
}
