package com.motorola.ccc.ota.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class HistoryDbHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "historyDB";
    private static final int DB_VERSION = 2;
    private static final String KEY_ID = "id";
    private static final String TABLE_HISTORY = "historyDetails";

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class History {
        private String releaseNotes;
        private String sourceVersion;
        private String targetVersion;
        private long updateTime;
        private String updateType;
        private String upgradeNotes;

        public History() {
        }

        public void setSourceVersion(String str) {
            this.sourceVersion = str;
        }

        public void setTargetVersion(String str) {
            this.targetVersion = str;
        }

        public void setUpdateType(String str) {
            this.updateType = str;
        }

        public void setUpdateTime(long j) {
            this.updateTime = j;
        }

        public void setReleaseNotes(String str) {
            this.releaseNotes = str;
        }

        public void setUpgradeNotes(String str) {
            this.upgradeNotes = str;
        }

        public String getSourceVersion() {
            return this.sourceVersion;
        }

        public String getTargetVersion() {
            return this.targetVersion;
        }

        public String getUpdateType() {
            return this.updateType;
        }

        public long getUpdateTime() {
            return this.updateTime;
        }

        public String getReleaseNotes() {
            return this.releaseNotes;
        }

        public String getUpgradeNotes() {
            return this.upgradeNotes;
        }
    }

    public HistoryDbHandler(Context context) {
        super(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, 2);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE historyDetails(id INTEGER PRIMARY KEY AUTOINCREMENT,sourceVersion TEXT,targetVersion TEXT,updateType TEXT,updateTime INTEGER,releaseNotes TEXT,upgradeNotes TEXT)");
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS historyDetails");
        onCreate(sQLiteDatabase);
    }

    public void insertHistoryDetails(String str, String str2, String str3, long j, String str4, String str5) {
        Logger.debug("OtaApp", "insertHistoryDetails = " + str + SystemUpdateStatusUtils.SPACE + str2 + SystemUpdateStatusUtils.SPACE + str3 + SystemUpdateStatusUtils.SPACE + j + SystemUpdateStatusUtils.SPACE + str4 + SystemUpdateStatusUtils.SPACE + str5);
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UpgradeUtilConstants.KEY_HISTORY_SOURCE_VERSION, str);
        contentValues.put(UpgradeUtilConstants.KEY_HISTORY_TARGET_VERSION, str2);
        contentValues.put(UpgradeUtilConstants.KEY_HISTORY_UPDATE_TYPE, str3);
        contentValues.put(UpgradeUtilConstants.KEY_HISTORY_UPDATE_TIME, Long.valueOf(j));
        contentValues.put(UpgradeUtilConstants.KEY_HISTORY_RELEASE_NOTES, str4);
        contentValues.put(UpgradeUtilConstants.KEY_HISTORY_UPGRADE_NOTES, str5);
        writableDatabase.insert(TABLE_HISTORY, null, contentValues);
        writableDatabase.close();
    }

    public ArrayList<History> getHistory() {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ArrayList<History> arrayList = new ArrayList<>();
        try {
            Cursor rawQuery = writableDatabase.rawQuery("SELECT sourceVersion, targetVersion, updateType, updateTime, releaseNotes, upgradeNotes FROM 'historyDetails'", null);
            while (rawQuery.moveToNext()) {
                History history = new History();
                history.setSourceVersion(rawQuery.getString(rawQuery.getColumnIndex(UpgradeUtilConstants.KEY_HISTORY_SOURCE_VERSION)));
                history.setTargetVersion(rawQuery.getString(rawQuery.getColumnIndex(UpgradeUtilConstants.KEY_HISTORY_TARGET_VERSION)));
                history.setUpdateType(rawQuery.getString(rawQuery.getColumnIndex(UpgradeUtilConstants.KEY_HISTORY_UPDATE_TYPE)));
                history.setUpdateTime(rawQuery.getLong(rawQuery.getColumnIndex(UpgradeUtilConstants.KEY_HISTORY_UPDATE_TIME)));
                history.setReleaseNotes(rawQuery.getString(rawQuery.getColumnIndex(UpgradeUtilConstants.KEY_HISTORY_RELEASE_NOTES)));
                history.setUpgradeNotes(rawQuery.getString(rawQuery.getColumnIndex(UpgradeUtilConstants.KEY_HISTORY_UPGRADE_NOTES)));
                arrayList.add(history);
            }
        } catch (Exception e) {
            Logger.debug("OtaApp", "HistoryDbHandler.getHistory Exception " + e);
        }
        return arrayList;
    }
}
