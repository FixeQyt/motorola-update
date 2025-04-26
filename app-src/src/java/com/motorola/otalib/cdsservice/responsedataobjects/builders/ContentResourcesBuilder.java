package com.motorola.otalib.cdsservice.responsedataobjects.builders;

import com.motorola.otalib.cdsservice.responsedataobjects.ContentResources;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ContentResourcesBuilder {
    public static ContentResources from(JSONObject jSONObject) throws JSONException {
        if (jSONObject == null) {
            return null;
        }
        return new ContentResources(jSONObject.getBoolean("proceed"), jSONObject.optString("wifiUrl"), HashMapBuilder.from(jSONObject.optJSONObject("wifiHeaders")), jSONObject.optString("cellUrl"), HashMapBuilder.from(jSONObject.optJSONObject("cellHeaders")), jSONObject.optString("adminApnUrl"), HashMapBuilder.from(jSONObject.optJSONObject("adminApnHeaders")), jSONObject.optString("trackingId", "unknown"));
    }

    public static ContentResources from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "ContentResourcesBuilder.from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(ContentResources contentResources) {
        if (contentResources == null) {
            return null;
        }
        try {
            return new JSONObject().put("proceed", contentResources.getProceed()).putOpt("wifiUrl", contentResources.getWifiUrl()).putOpt("wifiHeaders", HashMapBuilder.toJSONObject(contentResources.getWifiHeaders())).putOpt("cellUrl", contentResources.getCellularUrl()).putOpt("cellHeaders", HashMapBuilder.toJSONObject(contentResources.getCellularHeaders())).putOpt("adminApnUrl", contentResources.getAdminApnUrl()).putOpt("adminApnHeaders", HashMapBuilder.toJSONObject(contentResources.getAdminApnHeaders())).putOpt("trackingId", contentResources.getTrackingId());
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "ContentResourcesBuilder.toJSONObject(request) caught exception :" + e);
            return null;
        }
    }

    public static String toJSONString(ContentResources contentResources) {
        JSONObject jSONObject;
        if (contentResources == null || (jSONObject = toJSONObject(contentResources)) == null) {
            return null;
        }
        return jSONObject.toString();
    }
}
