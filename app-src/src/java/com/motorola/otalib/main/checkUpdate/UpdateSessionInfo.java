package com.motorola.otalib.main.checkUpdate;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.motorola.otalib.common.settings.Settings;
import com.motorola.otalib.main.Logger;
import com.motorola.otalib.main.Settings.LibConfigs;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpdateSessionInfo {
    CheckRequestObj checkRequestObj;
    long contentTimeSTamp;
    boolean keepPackage;
    String reportingTag;
    String serviceControlResponse;
    int statusCode;
    String trackingId;

    public CheckRequestObj getCheckRequestObj() {
        return this.checkRequestObj;
    }

    public CheckRequestObj setCheckRequestObj(String str, Settings settings) {
        CheckRequestObj fromJsonString = CheckRequestObj.fromJsonString(str);
        this.checkRequestObj = fromJsonString;
        setSessionDetails(settings, fromJsonString.getPrimaryKey(), this);
        return fromJsonString;
    }

    public String getTrackingId() {
        return this.trackingId;
    }

    public void setTrackingId(String str) {
        this.trackingId = str;
    }

    public long getContentTimeSTamp() {
        return this.contentTimeSTamp;
    }

    public void setContentTimeSTamp(long j) {
        this.contentTimeSTamp = j;
    }

    public String getReportingTag() {
        return this.reportingTag;
    }

    public void setReportingTag(String str) {
        this.reportingTag = str;
    }

    public String getServiceControlResponse() {
        return this.serviceControlResponse;
    }

    public UpdateSessionInfo setServiceControlResponse(Settings settings, String str, String str2) {
        this.serviceControlResponse = str2;
        setSessionDetails(settings, str, this);
        return this;
    }

    public boolean isKeepPackage() {
        return this.keepPackage;
    }

    public void setKeepPackage(boolean z) {
        this.keepPackage = z;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int i) {
        this.statusCode = i;
    }

    public static String arrayListToJson(List<UpdateSessionInfo> list) {
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();
        for (UpdateSessionInfo updateSessionInfo : list) {
            jsonArray.add(gson.toJsonTree(updateSessionInfo).getAsJsonObject());
        }
        return jsonArray.toString();
    }

    public static List<UpdateSessionInfo> arrayListFromJson(String str) {
        Gson gson = new Gson();
        JsonArray jsonArray = (JsonArray) gson.fromJson(str, JsonArray.class);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jsonArray.size(); i++) {
            arrayList.add((UpdateSessionInfo) gson.fromJson(jsonArray.get(i).getAsJsonObject(), UpdateSessionInfo.class));
        }
        return arrayList;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public static UpdateSessionInfo fromJsonString(String str) {
        return (UpdateSessionInfo) new Gson().fromJson(str, UpdateSessionInfo.class);
    }

    public static void deleteSessionDetails(Settings settings, String str) {
        String string = settings.getString(LibConfigs.UPDATE_SESSION_MAPPER);
        if (TextUtils.isEmpty(string)) {
            return;
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        try {
            JSONObject jSONObject = new JSONObject(string);
            Iterator<String> keys = jSONObject.keys();
            while (keys.hasNext()) {
                String next = keys.next();
                linkedHashMap.put(next, jSONObject.getString(next));
            }
        } catch (Exception e) {
            Logger.error(Logger.OTALib_TAG, "deleteSessionDetails:Exception:msg=" + e);
        }
        linkedHashMap.remove(str);
        settings.setString(LibConfigs.UPDATE_SESSION_MAPPER, new JSONObject(linkedHashMap).toString());
    }

    public static UpdateSessionInfo getSessionDetails(Settings settings, String str) {
        String string = settings.getString(LibConfigs.UPDATE_SESSION_MAPPER);
        if (TextUtils.isEmpty(string)) {
            return new UpdateSessionInfo();
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        try {
            JSONObject jSONObject = new JSONObject(string);
            Iterator<String> keys = jSONObject.keys();
            while (keys.hasNext()) {
                String next = keys.next();
                linkedHashMap.put(next, jSONObject.getString(next));
            }
        } catch (Exception e) {
            Logger.error(Logger.OTALib_TAG, "getSessionDetails:Exception:msg=" + e);
        }
        UpdateSessionInfo fromJsonString = fromJsonString((String) linkedHashMap.get(str));
        return fromJsonString == null ? new UpdateSessionInfo() : fromJsonString;
    }

    public static boolean isUpdateGoingOn(Settings settings, String str) {
        String string = settings.getString(LibConfigs.UPDATE_SESSION_MAPPER);
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        try {
            JSONObject jSONObject = new JSONObject(string);
            Iterator<String> keys = jSONObject.keys();
            while (keys.hasNext()) {
                String next = keys.next();
                linkedHashMap.put(next, jSONObject.getString(next));
            }
        } catch (Exception e) {
            Logger.error(Logger.OTALib_TAG, "isUpdateGoingOn:Exception:msg=" + e);
        }
        return fromJsonString((String) linkedHashMap.get(str)) != null;
    }

    public static Map<String, String> setSessionDetails(Settings settings, String str, UpdateSessionInfo updateSessionInfo) {
        String string = settings.getString(LibConfigs.UPDATE_SESSION_MAPPER);
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        if (!TextUtils.isEmpty(string)) {
            try {
                JSONObject jSONObject = new JSONObject(string);
                Iterator<String> keys = jSONObject.keys();
                while (keys.hasNext()) {
                    String next = keys.next();
                    linkedHashMap.put(next, jSONObject.getString(next));
                }
            } catch (Exception e) {
                Logger.error(Logger.OTALib_TAG, "setSessionDetails:Exception:msg=" + e);
            }
        }
        linkedHashMap.put(str, updateSessionInfo.toString());
        settings.setString(LibConfigs.UPDATE_SESSION_MAPPER, new JSONObject(linkedHashMap).toString());
        return linkedHashMap;
    }
}
