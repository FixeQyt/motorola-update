package com.motorola.ccc.ota.utils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.motorola.ccc.ota.env.OtaApplication;
import java.util.HashMap;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class OtaAppContentProvider extends ContentProvider {
    private static final String DATABASE_NAME = "otaApp.db";
    private static final int DATABASE_VERSION = 1;
    public static final String KEY_SMART_UPDATE = "SmartUpdateKey";
    static final String PROVIDER_NAME = "com.motorola.ccc.ota.utils.OtaAppContentProvider";
    private static final String SQL_CREATE_DB_TABLE = "CREATE TABLE otaConfigParam (keyval String PRIMARY KEY, value String );";
    private static final String TABLE_NAME = "otaConfigParam";
    public static final String keyval = "keyval";
    static final int uriCode = 1;
    static final UriMatcher uriMatcher;
    public static final String value = "value";
    private static HashMap<String, String> values;
    private SQLiteDatabase sqlDB;
    static final String URL = "content://com.motorola.ccc.ota.utils.OtaAppContentProvider/otaAppConfig";
    public static final Uri CONTENT_URL = Uri.parse(URL);

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    static {
        UriMatcher uriMatcher2 = new UriMatcher(-1);
        uriMatcher = uriMatcher2;
        uriMatcher2.addURI(PROVIDER_NAME, "otaAppConfig", 1);
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        SQLiteDatabase writableDatabase = new DatabaseHelper(getContext()).getWritableDatabase();
        this.sqlDB = writableDatabase;
        return writableDatabase != null;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        SQLiteQueryBuilder sQLiteQueryBuilder = new SQLiteQueryBuilder();
        sQLiteQueryBuilder.setTables(TABLE_NAME);
        if (uriMatcher.match(uri) == 1) {
            sQLiteQueryBuilder.setProjectionMap(values);
        } else {
            Logger.error("OtaApp", "Unknown Uri " + uri);
        }
        try {
            Cursor query = sQLiteQueryBuilder.query(this.sqlDB, strArr, str, strArr2, null, null, str2);
            query.setNotificationUri(getContext().getContentResolver(), uri);
            return query;
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception in OtaAppContentProvider, query: " + e);
            return null;
        }
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        if (uriMatcher.match(uri) == 1) {
            return "vnd.android.cursor.dir/otaAppConfig";
        }
        Logger.error("OtaApp", "Unknown Uri " + uri);
        return null;
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        long insert = this.sqlDB.insert(TABLE_NAME, null, contentValues);
        if (insert > 0) {
            Uri withAppendedId = ContentUris.withAppendedId(CONTENT_URL, insert);
            getContext().getContentResolver().notifyChange(withAppendedId, null);
            return withAppendedId;
        }
        return null;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int update;
        if (uriMatcher.match(uri) == 1) {
            update = this.sqlDB.update(TABLE_NAME, contentValues, str, strArr);
        } else {
            Logger.error("OtaApp", "Unknown Uri " + uri);
            update = 0;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return update;
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, OtaAppContentProvider.DATABASE_NAME, (SQLiteDatabase.CursorFactory) null, 1);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onCreate(SQLiteDatabase sQLiteDatabase) {
            sQLiteDatabase.execSQL(OtaAppContentProvider.SQL_CREATE_DB_TABLE);
        }

        @Override // android.database.sqlite.SQLiteOpenHelper
        public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS otaConfigParam");
            onCreate(sQLiteDatabase);
        }
    }

    public static void updateOtaAppContentProvider(String str, String str2) {
        Boolean bool = false;
        ContentValues contentValues = new ContentValues();
        contentValues.put(keyval, str);
        contentValues.put(value, str2);
        Cursor query = OtaApplication.getGlobalContext().getContentResolver().query(CONTENT_URL, new String[]{keyval, value}, null, null);
        if (query.moveToFirst()) {
            while (true) {
                if (str.equals(query.getString(query.getColumnIndex(keyval)))) {
                    OtaApplication.getGlobalContext().getContentResolver().update(CONTENT_URL, contentValues, null, null);
                    bool = true;
                    break;
                } else if (!query.moveToNext()) {
                    break;
                }
            }
        }
        if (bool.booleanValue()) {
            return;
        }
        OtaApplication.getGlobalContext().getContentResolver().insert(CONTENT_URL, contentValues);
    }
}
