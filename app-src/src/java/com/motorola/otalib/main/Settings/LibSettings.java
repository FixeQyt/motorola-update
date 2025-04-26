package com.motorola.otalib.main.Settings;

import android.content.Context;
import android.content.SharedPreferences;
import com.motorola.otalib.common.settings.Settings;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class LibSettings extends Settings {
    private static final String OTALib_SHARED_PREFS_NAME = "otalib_prefs";

    public LibSettings(Context context) {
        super(getGlobalSharedPreference(context));
    }

    private static SharedPreferences getGlobalSharedPreference(Context context) {
        return context.getSharedPreferences(OTALib_SHARED_PREFS_NAME, 0);
    }
}
