package com.motorola.otalib.cdsservice.responsedataobjects.builders;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class HashMapBuilder {
    public static Map<String, String> from(JSONObject jSONObject) {
        HashMap hashMap = new HashMap();
        if (jSONObject == null) {
            return null;
        }
        Iterator<String> keys = jSONObject.keys();
        while (keys.hasNext()) {
            try {
                String next = keys.next();
                hashMap.put(next, jSONObject.getString(next));
            } catch (JSONException unused) {
            }
        }
        return hashMap;
    }

    public static Map<String, String> from(String str) {
        try {
            return from(new JSONObject(str));
        } catch (JSONException unused) {
            return null;
        }
    }

    public static JSONObject toJSONObject(Map<String, String> map) {
        if (map != null) {
            return new JSONObject(map);
        }
        return null;
    }

    public static String toJSONString(Map<String, String> map) {
        if (map != null) {
            return toJSONObject(map).toString();
        }
        return null;
    }
}
