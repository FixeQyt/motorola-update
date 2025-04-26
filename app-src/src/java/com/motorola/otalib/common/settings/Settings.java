package com.motorola.otalib.common.settings;

import android.content.SharedPreferences;
import com.motorola.otalib.common.CommonLogger;
import java.util.HashMap;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class Settings {
    private final SharedPreferences prefs;

    public void setupDefaults() {
    }

    public Settings(SharedPreferences sharedPreferences) {
        this.prefs = sharedPreferences;
    }

    public String getConfig(String str) {
        return this.prefs.getString(str, null);
    }

    public String getConfig(String str, String str2) {
        return this.prefs.getString(str, str2);
    }

    public String getConfig(ISetting iSetting, String str) {
        return getConfig(iSetting.key(), str);
    }

    public void setConfig(String str, String str2) {
        this.prefs.edit().putString(str, str2).commit();
    }

    public void setConfig(ISetting iSetting, String str) {
        setConfig(iSetting.key(), str);
    }

    public void removeConfig(String str) {
        this.prefs.edit().remove(str).commit();
    }

    public void removeConfig(ISetting iSetting) {
        removeConfig(iSetting.key());
    }

    public SharedPreferences getPrefs() {
        return this.prefs;
    }

    public static void saveSettings(SharedPreferences sharedPreferences, HashMap<String, String> hashMap) {
        Settings settings = new Settings(sharedPreferences);
        for (String str : hashMap.keySet()) {
            settings.setConfig(str, hashMap.get(str));
        }
    }

    public int getInt(ISetting iSetting, int i) {
        try {
            return Integer.valueOf(getConfig(iSetting, String.valueOf(i))).intValue();
        } catch (NumberFormatException unused) {
            return i;
        }
    }

    public String getString(ISetting iSetting) {
        return getConfig(iSetting, (String) null);
    }

    public long getLong(ISetting iSetting, long j) {
        try {
            return Long.valueOf(getConfig(iSetting, String.valueOf(j))).longValue();
        } catch (Exception unused) {
            return j;
        }
    }

    public boolean getBoolean(ISetting iSetting) {
        return Boolean.valueOf(getConfig(iSetting, (String) null)).booleanValue();
    }

    public boolean getBoolean(ISetting iSetting, boolean z) {
        return Boolean.valueOf(getConfig(iSetting, String.valueOf(z))).booleanValue();
    }

    public float getFloat(ISetting iSetting, float f) {
        try {
            return Float.parseFloat(getConfig(iSetting, String.valueOf(f)));
        } catch (Exception unused) {
            return f;
        }
    }

    public double getDouble(ISetting iSetting, Double d) {
        try {
            return Double.parseDouble(getConfig(iSetting, String.valueOf(d)));
        } catch (Exception unused) {
            return d.doubleValue();
        }
    }

    public JSONObject getJsonObject(ISetting iSetting) {
        try {
            return new JSONObject(getConfig(iSetting, (String) null));
        } catch (Exception e) {
            CommonLogger.e(CommonLogger.TAG, "Exception in BotaSettings, getJsonObject: " + e);
            return null;
        }
    }

    public void setInt(ISetting iSetting, int i) {
        setConfig(iSetting, String.valueOf(i));
    }

    public void setString(ISetting iSetting, String str) {
        setConfig(iSetting, str);
    }

    public void setLong(ISetting iSetting, long j) {
        setConfig(iSetting, String.valueOf(j));
    }

    public void setFloat(ISetting iSetting, float f) {
        setConfig(iSetting, String.valueOf(f));
    }

    public void setBoolean(ISetting iSetting, boolean z) {
        setConfig(iSetting, String.valueOf(z));
    }

    public void setJsonObject(ISetting iSetting, JSONObject jSONObject) {
        setConfig(iSetting, jSONObject.toString());
    }

    public void incrementPrefs(ISetting iSetting) {
        int i = 0;
        try {
            i = Integer.valueOf(getConfig(iSetting, String.valueOf(0))).intValue();
        } catch (NumberFormatException unused) {
        }
        setConfig(iSetting, String.valueOf(i + 1));
    }

    public void decrementPrefs(ISetting iSetting) {
        setConfig(iSetting, String.valueOf(Integer.max(getInt(iSetting, 0) - 1, 0)));
    }
}
