package com.motorola.ccc.ota.utils;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.SearchIndexablesContract;
import android.provider.SearchIndexablesProvider;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.ui.SettingsActivity;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class SearchResultProvider extends SearchIndexablesProvider {
    private static final String RAW_KEY = "ota_system_update";

    public boolean onCreate() {
        return true;
    }

    public Cursor queryXmlResources(String[] strArr) {
        return new MatrixCursor(SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS);
    }

    public Cursor queryRawData(String[] strArr) {
        MatrixCursor matrixCursor = new MatrixCursor(SearchIndexablesContract.INDEXABLES_RAW_COLUMNS);
        Object[] objArr = new Object[SearchIndexablesContract.INDEXABLES_RAW_COLUMNS.length];
        objArr[0] = 1;
        objArr[1] = getContext().getString(R.string.setting_sw_title);
        objArr[8] = Integer.valueOf((int) R.drawable.ic_system_update);
        objArr[12] = RAW_KEY;
        objArr[10] = getContext().getPackageName();
        objArr[11] = SettingsActivity.class.getName();
        objArr[7] = SettingsActivity.class.getName();
        objArr[9] = "android.intent.action.MAIN";
        String string = getContext().getString(R.string.setting_sw_summary);
        objArr[2] = string;
        objArr[3] = string;
        matrixCursor.addRow(objArr);
        return matrixCursor;
    }

    public Cursor queryNonIndexableKeys(String[] strArr) {
        return new MatrixCursor(SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS);
    }
}
