package com.motorola.otalib.downloadservice.dataobjects;

import com.motorola.otalib.downloadservice.utils.DownloadServiceLogger;
import com.motorola.otalib.downloadservice.utils.DownloadServiceSettings;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DownloadRequestBuilder {
    public static DownloadRequest from(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            JSONObject jSONObject2 = jSONObject.getJSONObject(DownloadServiceSettings.KEY_DOWNLOAD_REQUEST);
            return new DownloadRequest(jSONObject2.getString("contentResource"), jSONObject2.getBoolean("wifiOnly"), jSONObject2.getInt("time"), jSONObject2.getLong("size"), jSONObject2.getString("fileName"), jSONObject2.getString("hostName"), jSONObject2.getInt("port"), jSONObject2.getString("disallowedNetworks"), jSONObject2.getString("backOffValues"), jSONObject2.getInt("maxRetryCount"), jSONObject2.optBoolean("advancedDownloadFeature"), jSONObject2.getString("upgradeSourceType"), jSONObject2.getBoolean("allowOnRoaming"), jSONObject2.getLong("startingOffset"));
        } catch (JSONException e) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "DownloadRequest:from(jsonObject) caught exception :" + e);
            return null;
        }
    }

    public static DownloadRequest from(String str) {
        if (str == null) {
            return null;
        }
        try {
            return from(new JSONObject(str));
        } catch (JSONException e) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "DownloadRequest:from(jsonString) caught exception :" + e);
            return null;
        }
    }

    public static JSONObject toJSONObject(DownloadRequest downloadRequest) throws JSONException {
        if (downloadRequest == null) {
            return null;
        }
        JSONObject jSONObject = new JSONObject();
        jSONObject.put(DownloadServiceSettings.KEY_DOWNLOAD_REQUEST, new JSONObject().put("contentResource", downloadRequest.getContentResource()).put("wifiOnly", downloadRequest.getWifiOnly()).put("time", downloadRequest.getTime()).put("size", downloadRequest.getSize()).put("fileName", downloadRequest.getFileName()).put("hostName", downloadRequest.getHostName()).put("port", downloadRequest.getPort()).put("disallowedNetworks", downloadRequest.getDisallowedNetworks()).put("backOffValues", downloadRequest.getBackOffValues()).put("maxRetryCount", downloadRequest.getMaxRetryCount()).put("upgradeSourceType", downloadRequest.getUpgradeSourceType()).put("allowOnRoaming", downloadRequest.getAllowOnRoaming()).put("startingOffset", downloadRequest.getStartingOffset()));
        return jSONObject;
    }

    public static String toJSONString(DownloadRequest downloadRequest) throws JSONException {
        if (downloadRequest == null) {
            return null;
        }
        return toJSONObject(downloadRequest).toString();
    }
}
