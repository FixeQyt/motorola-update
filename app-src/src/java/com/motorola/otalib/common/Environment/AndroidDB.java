package com.motorola.otalib.common.Environment;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.otalib.common.CommonLogger;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import java.util.Date;
import org.json.JSONException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class AndroidDB extends SQLiteOpenHelper implements ApplicationEnv.Database {
    private static final String STATUS_TABLE_CREATION = "CREATE TABLE IF NOT EXISTS status (_id INTEGER PRIMARY KEY, time BIGINT , dev TEXT NOT NULL, src TEXT,  dest TEXT, state TEXT NOT NULL, status TEXT, info TEXT, Repository TEXT, reportingTag TEXT, trackingId TEXT, srcSha1 TEXT NOT NULL, targetSha1 TEXT NOT NULL, updateVia INT NOT NULL)";
    private static final String STATUS_TABLE_FORMAT = "(_id INTEGER PRIMARY KEY, time BIGINT , dev TEXT NOT NULL, src TEXT,  dest TEXT, state TEXT NOT NULL, status TEXT, info TEXT, Repository TEXT, reportingTag TEXT, trackingId TEXT, srcSha1 TEXT NOT NULL, targetSha1 TEXT NOT NULL, updateVia INT NOT NULL)";
    private static final String STATUS_TABLE_NAME = "status";
    private static final int VERSION = 5;
    private static final String VERSION_TABLE_FORMAT = "(Time INT, Version TEXT NOT NULL PRIMARY KEY, State TEXT NOT NULL DEFAULT 'Notified', Repository TEXT ,Metadata  BLOB, Status TEXT, Info TEXT, Dirty BOOLEAN DEFAULT true, updateVia INT NOT NULL)";
    private static final String VERSION_TABLE_NAME = "vt";
    private static final String VT_TABLE_CREATION = "CREATE TABLE IF NOT EXISTS vt (Time INT, Version TEXT NOT NULL PRIMARY KEY, State TEXT NOT NULL DEFAULT 'Notified', Repository TEXT ,Metadata  BLOB, Status TEXT, Info TEXT, Dirty BOOLEAN DEFAULT true, updateVia INT NOT NULL)";
    private final SQLiteDatabase _db;
    private int mUpdateViaSha1;

    private static long getTime() {
        return new Date().getTime();
    }

    public AndroidDB(String str, Context context) {
        super(context, str, (SQLiteDatabase.CursorFactory) null, 5);
        this.mUpdateViaSha1 = 0;
        CommonLogger.d(CommonLogger.TAG, "CusAndroidDB.CusAndroidDB");
        this._db = getWritableDatabase();
    }

    private boolean createTable(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.execSQL(VT_TABLE_CREATION);
            sQLiteDatabase.execSQL(STATUS_TABLE_CREATION);
            return true;
        } catch (SQLException e) {
            CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.createTable failed: exception" + e);
            return false;
        }
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        CommonLogger.d(CommonLogger.TAG, "CusAndroidDB.onCreate()");
        createTable(sQLiteDatabase);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        CommonLogger.d(CommonLogger.TAG, "CusAndroidDB.onUpgrade(), oldVersion: " + i + " newVersion: " + i2);
        try {
            sQLiteDatabase.execSQL(VT_TABLE_CREATION);
            sQLiteDatabase.execSQL(STATUS_TABLE_CREATION);
            if (i == 3) {
                if (!isColumnExist(sQLiteDatabase, "Repository", STATUS_TABLE_NAME)) {
                    sQLiteDatabase.execSQL("ALTER TABLE status ADD COLUMN Repository TEXT");
                }
                if (!isColumnExist(sQLiteDatabase, "reportingTag", STATUS_TABLE_NAME)) {
                    sQLiteDatabase.execSQL("ALTER TABLE status ADD COLUMN reportingTag TEXT");
                }
                if (isColumnExist(sQLiteDatabase, "trackingId", STATUS_TABLE_NAME)) {
                    return;
                }
                sQLiteDatabase.execSQL("ALTER TABLE status ADD COLUMN trackingId TEXT");
            } else if (i == 4) {
                if (!isColumnExist(sQLiteDatabase, "srcSha1", STATUS_TABLE_NAME)) {
                    sQLiteDatabase.execSQL("ALTER TABLE status ADD COLUMN srcSha1 TEXT");
                }
                if (!isColumnExist(sQLiteDatabase, "targetSha1", STATUS_TABLE_NAME)) {
                    sQLiteDatabase.execSQL("ALTER TABLE status ADD COLUMN targetSha1 TEXT");
                }
                if (!isColumnExist(sQLiteDatabase, "updateVia", STATUS_TABLE_NAME)) {
                    sQLiteDatabase.execSQL("ALTER TABLE status ADD COLUMN updateVia INT");
                }
                if (!isColumnExist(sQLiteDatabase, "updateVia", VERSION_TABLE_NAME)) {
                    sQLiteDatabase.execSQL("ALTER TABLE vt ADD COLUMN updateVia INT");
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put("updateVia", (Integer) 1);
                CommonLogger.v(CommonLogger.TAG, "numbe of rows updated in vt " + sQLiteDatabase.update(VERSION_TABLE_NAME, contentValues, null, null));
                contentValues.put("srcSha1", "");
                contentValues.put("targetSha1", "");
                CommonLogger.v(CommonLogger.TAG, "numbe of rows updated in status " + sQLiteDatabase.update(STATUS_TABLE_NAME, contentValues, null, null));
                reCreateTable(sQLiteDatabase, VT_TABLE_CREATION, VERSION_TABLE_NAME);
                reCreateTable(sQLiteDatabase, STATUS_TABLE_CREATION, STATUS_TABLE_NAME);
            }
        } catch (SQLException e) {
            CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.onUpgrade failed: exception" + e);
        }
    }

    private void reCreateTable(SQLiteDatabase sQLiteDatabase, String str, String str2) {
        CommonLogger.d(CommonLogger.TAG, " reCreateTable " + str + SystemUpdateStatusUtils.SPACE + str2);
        sQLiteDatabase.execSQL("ALTER TABLE " + str2 + " RENAME TO " + str2 + "_old;");
        CommonLogger.v(CommonLogger.TAG, " renamed old table ");
        sQLiteDatabase.execSQL(str);
        CommonLogger.v(CommonLogger.TAG, " created new table");
        sQLiteDatabase.execSQL("INSERT INTO " + str2 + " SELECT * FROM " + str2 + "_old;");
        CommonLogger.v(CommonLogger.TAG, " added values");
        sQLiteDatabase.execSQL("DROP TABLE " + str2 + "_old;");
        CommonLogger.v(CommonLogger.TAG, " drop old table");
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onDowngrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        CommonLogger.d(CommonLogger.TAG, "CusAndroidDB.onDowngrade(), oldVersion: " + i + " newVersion: " + i2);
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS vt");
        CommonLogger.v(CommonLogger.TAG, "dropped vt");
        sQLiteDatabase.execSQL("DROP TABLE IF EXISTS status");
        CommonLogger.v(CommonLogger.TAG, "dropped status");
        onCreate(sQLiteDatabase);
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x004d, code lost:
        if (r0 == null) goto L11;
     */
    /* JADX WARN: Code restructure failed: missing block: B:17:0x0054, code lost:
        return r2.booleanValue();
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean isColumnExist(android.database.sqlite.SQLiteDatabase r3, java.lang.String r4, java.lang.String r5) {
        /*
            r2 = this;
            r2 = 0
            java.lang.Boolean r2 = java.lang.Boolean.valueOf(r2)
            r0 = 0
            java.lang.String r1 = "select * from %s  LIMIT 0,1"
            java.lang.Object[] r5 = new java.lang.Object[]{r5}     // Catch: java.lang.Throwable -> L37 android.database.SQLException -> L39
            java.lang.String r5 = java.lang.String.format(r1, r5)     // Catch: java.lang.Throwable -> L37 android.database.SQLException -> L39
            android.database.Cursor r0 = r3.rawQuery(r5, r0)     // Catch: java.lang.Throwable -> L37 android.database.SQLException -> L39
            if (r0 == 0) goto L31
            int r3 = r0.getColumnIndex(r4)     // Catch: java.lang.Throwable -> L37 android.database.SQLException -> L39
            r5 = -1
            if (r3 == r5) goto L31
            java.lang.String r3 = com.motorola.otalib.common.CommonLogger.TAG     // Catch: java.lang.Throwable -> L37 android.database.SQLException -> L39
            java.lang.String r5 = "CusAndroidDB.isColumnExist: %s column exists."
            java.lang.Object[] r1 = new java.lang.Object[]{r4}     // Catch: java.lang.Throwable -> L37 android.database.SQLException -> L39
            java.lang.String r5 = java.lang.String.format(r5, r1)     // Catch: java.lang.Throwable -> L37 android.database.SQLException -> L39
            com.motorola.otalib.common.CommonLogger.d(r3, r5)     // Catch: java.lang.Throwable -> L37 android.database.SQLException -> L39
            r3 = 1
            java.lang.Boolean r2 = java.lang.Boolean.valueOf(r3)     // Catch: java.lang.Throwable -> L37 android.database.SQLException -> L39
        L31:
            if (r0 == 0) goto L50
        L33:
            r0.close()
            goto L50
        L37:
            r2 = move-exception
            goto L55
        L39:
            r3 = move-exception
            java.lang.String r5 = com.motorola.otalib.common.CommonLogger.TAG     // Catch: java.lang.Throwable -> L37
            java.lang.String r1 = "SQLiteDatabse.rawQuery failed for: %s : %s"
            java.lang.String r3 = r3.toString()     // Catch: java.lang.Throwable -> L37
            java.lang.Object[] r3 = new java.lang.Object[]{r4, r3}     // Catch: java.lang.Throwable -> L37
            java.lang.String r3 = java.lang.String.format(r1, r3)     // Catch: java.lang.Throwable -> L37
            com.motorola.otalib.common.CommonLogger.e(r5, r3)     // Catch: java.lang.Throwable -> L37
            if (r0 == 0) goto L50
            goto L33
        L50:
            boolean r2 = r2.booleanValue()
            return r2
        L55:
            if (r0 == 0) goto L5a
            r0.close()
        L5a:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.otalib.common.Environment.AndroidDB.isColumnExist(android.database.sqlite.SQLiteDatabase, java.lang.String, java.lang.String):boolean");
    }

    private boolean exec(String str, Object[] objArr) {
        try {
            CommonLogger.d(CommonLogger.TAG, String.format("exec %s data", str));
            this._db.execSQL(str, objArr);
            return true;
        } catch (SQLException e) {
            CommonLogger.e(CommonLogger.TAG, String.format("CusAndroidDB.exec failed: %s : %s", str, e.toString()));
            return false;
        }
    }

    private boolean exec(String str) {
        try {
            CommonLogger.d(CommonLogger.TAG, String.format("exec %s", str));
            this._db.execSQL(str);
            return true;
        } catch (SQLException e) {
            CommonLogger.e(CommonLogger.TAG, String.format("CusAndroidDB.exec failed: %s : %s", str, e.toString()));
            return false;
        }
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public boolean insert(String str, String str2, MetaData metaData, String str3, String str4) {
        long time = getTime();
        String packageState = ApplicationEnv.PackageState.Notified.toString();
        if (!insert_version(time, str, packageState, str2, metaData, "RS_TEMP_OK", str3)) {
            CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.insert failed: insert_version");
            return false;
        }
        String str5 = "Repository: " + str2 + "; Location: " + str3;
        if (insert_status(time, str, metaData, packageState, "RS_TEMP_OK", str4 != null ? str5 + "; AddOnInfo: " + str4 : str5, str2, metaData.getReportingTags(), metaData.getTrackingId())) {
            return true;
        }
        CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.insert failed: insert_status");
        return true;
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public boolean setState(String str, ApplicationEnv.PackageState packageState, boolean z, String str2, String str3) {
        String str4;
        CommonLogger.i(CommonLogger.TAG, "vesion:" + str + " state:" + packageState + " info:" + str2);
        if (packageState != ApplicationEnv.PackageState.Result) {
            str4 = "RS_TEMP_OK";
        } else if (z) {
            str4 = "RS_OK";
        } else {
            str4 = "RS_FAIL";
        }
        long time = getTime();
        if (!update_version(time, str, packageState.toString(), str4, str2)) {
            CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.setState failed: update_version");
            return false;
        }
        String str5 = str3 + SystemUpdateStatusUtils.FIELD_SEPERATOR + str2;
        CommonLogger.i(CommonLogger.TAG, "Status Info:" + str5);
        if (insert_status(time, str, packageState.toString(), str4, str5)) {
            return true;
        }
        CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.setState failed: insert_status");
        return true;
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public boolean setState(String str, ApplicationEnv.PackageState packageState, boolean z, String str2, String str3, String str4) {
        String str5;
        CommonLogger.i(CommonLogger.TAG, "vesion:" + str + " state:" + packageState + " info:" + str2);
        if (packageState != ApplicationEnv.PackageState.Result) {
            str5 = "RS_TEMP_OK";
        } else if (z) {
            str5 = "RS_OK";
        } else {
            str5 = "RS_FAIL";
        }
        String str6 = str5;
        long time = getTime();
        if (!update_version(time, str, packageState.toString(), str6, str2)) {
            CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.setState failed: update_version");
            return false;
        }
        String str7 = str3 + SystemUpdateStatusUtils.FIELD_SEPERATOR + str2;
        CommonLogger.i(CommonLogger.TAG, "Status Info:" + str7);
        ApplicationEnv.Database.Descriptor description = getDescription(str);
        if (description == null) {
            CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.insert_status failed: no description for version " + str);
            return false;
        } else if (insert_status(time, str, description.getMeta(), packageState.toString(), str6, str7, str4, description.getMeta().getReportingTags(), description.getMeta().getTrackingId())) {
            return true;
        } else {
            CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.setState failed: insert_status");
            return true;
        }
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public boolean setVersionState(String str, ApplicationEnv.PackageState packageState, String str2) {
        if (update_version(getTime(), str, packageState.toString(), "RS_TEMP_OK", str2)) {
            return true;
        }
        CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.setVersionState failed: update_version");
        return false;
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public boolean setState(String str, ApplicationEnv.PackageState packageState, String str2) {
        return setState(str, packageState, true, str2, "");
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:26:0x0064  */
    /* JADX WARN: Type inference failed for: r6v0, types: [com.motorola.otalib.common.Environment.AndroidDB] */
    /* JADX WARN: Type inference failed for: r6v2 */
    /* JADX WARN: Type inference failed for: r6v4, types: [android.database.Cursor] */
    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public java.util.List<java.lang.String> getVersions() {
        /*
            r6 = this;
            r0 = 0
            android.database.sqlite.SQLiteDatabase r6 = r6._db     // Catch: java.lang.Throwable -> L3c java.lang.Exception -> L41
            java.lang.String r1 = "select Version from %s"
            java.lang.String r2 = "vt"
            java.lang.Object[] r2 = new java.lang.Object[]{r2}     // Catch: java.lang.Throwable -> L3c java.lang.Exception -> L41
            java.lang.String r1 = java.lang.String.format(r1, r2)     // Catch: java.lang.Throwable -> L3c java.lang.Exception -> L41
            android.database.Cursor r6 = r6.rawQuery(r1, r0)     // Catch: java.lang.Throwable -> L3c java.lang.Exception -> L41
            int r1 = r6.getCount()     // Catch: java.lang.Exception -> L3a java.lang.Throwable -> L61
            if (r1 != 0) goto L1d
            r6.close()     // Catch: java.lang.Exception -> L3a java.lang.Throwable -> L61
            return r0
        L1d:
            java.util.LinkedList r1 = new java.util.LinkedList     // Catch: java.lang.Exception -> L3a java.lang.Throwable -> L61
            r1.<init>()     // Catch: java.lang.Exception -> L3a java.lang.Throwable -> L61
        L22:
            boolean r2 = r6.moveToNext()     // Catch: java.lang.Exception -> L3a java.lang.Throwable -> L61
            if (r2 == 0) goto L36
            java.lang.String r2 = "Version"
            int r2 = r6.getColumnIndex(r2)     // Catch: java.lang.Exception -> L3a java.lang.Throwable -> L61
            java.lang.String r2 = r6.getString(r2)     // Catch: java.lang.Exception -> L3a java.lang.Throwable -> L61
            r1.add(r2)     // Catch: java.lang.Exception -> L3a java.lang.Throwable -> L61
            goto L22
        L36:
            r6.close()     // Catch: java.lang.Exception -> L3a java.lang.Throwable -> L61
            return r1
        L3a:
            r1 = move-exception
            goto L43
        L3c:
            r6 = move-exception
            r5 = r0
            r0 = r6
            r6 = r5
            goto L62
        L41:
            r1 = move-exception
            r6 = r0
        L43:
            java.lang.String r2 = com.motorola.otalib.common.CommonLogger.TAG     // Catch: java.lang.Throwable -> L61
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L61
            r3.<init>()     // Catch: java.lang.Throwable -> L61
            java.lang.String r4 = "AndriodDB.getVersions, exception : "
            java.lang.StringBuilder r3 = r3.append(r4)     // Catch: java.lang.Throwable -> L61
            java.lang.StringBuilder r1 = r3.append(r1)     // Catch: java.lang.Throwable -> L61
            java.lang.String r1 = r1.toString()     // Catch: java.lang.Throwable -> L61
            com.motorola.otalib.common.CommonLogger.e(r2, r1)     // Catch: java.lang.Throwable -> L61
            if (r6 == 0) goto L60
            r6.close()
        L60:
            return r0
        L61:
            r0 = move-exception
        L62:
            if (r6 == 0) goto L67
            r6.close()
        L67:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.otalib.common.Environment.AndroidDB.getVersions():java.util.List");
    }

    /* JADX WARN: Not initialized variable reg: 2, insn: 0x00c4: MOVE  (r1 I:??[OBJECT, ARRAY]) = (r2 I:??[OBJECT, ARRAY]), block:B:26:0x00c4 */
    /* JADX WARN: Removed duplicated region for block: B:28:0x00c7  */
    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public com.motorola.otalib.common.Environment.ApplicationEnv.Database.Descriptor getDescription(final java.lang.String r14) {
        /*
            r13 = this;
            java.lang.String r0 = "AndroidDB.getDescription, exception : "
            r1 = 0
            android.database.sqlite.SQLiteDatabase r2 = r13._db     // Catch: java.lang.Throwable -> La7 java.lang.Exception -> La9
            java.lang.String r3 = "select * from %s where Version='%s'"
            java.lang.String r4 = "vt"
            java.lang.Object[] r4 = new java.lang.Object[]{r4, r14}     // Catch: java.lang.Throwable -> La7 java.lang.Exception -> La9
            java.lang.String r3 = java.lang.String.format(r3, r4)     // Catch: java.lang.Throwable -> La7 java.lang.Exception -> La9
            android.database.Cursor r2 = r2.rawQuery(r3, r1)     // Catch: java.lang.Throwable -> La7 java.lang.Exception -> La9
            int r3 = r2.getCount()     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            if (r3 != 0) goto L1f
            r2.close()     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            return r1
        L1f:
            r2.moveToNext()     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r3 = "State"
            int r3 = r2.getColumnIndex(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r3 = r2.getString(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            com.motorola.otalib.common.Environment.ApplicationEnv$PackageState r7 = com.motorola.otalib.common.Environment.ApplicationEnv.PackageState.valueOf(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r3 = "Time"
            int r3 = r2.getColumnIndex(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            int r8 = r2.getInt(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r3 = "Info"
            int r3 = r2.getColumnIndex(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r9 = r2.getString(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r3 = "Metadata"
            int r3 = r2.getColumnIndex(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            int r4 = r2.getType(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            r5 = 4
            if (r4 != r5) goto L5b
            java.lang.String r4 = new java.lang.String     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            byte[] r3 = r2.getBlob(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            r4.<init>(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            goto L5f
        L5b:
            java.lang.String r4 = r2.getString(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
        L5f:
            java.lang.String r3 = "''"
            java.lang.String r5 = "'"
            java.lang.String r3 = r4.replaceAll(r3, r5)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            com.motorola.otalib.common.metaData.MetaData r10 = com.motorola.otalib.common.metaData.builder.MetaDataBuilder.from(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r3 = "Repository"
            int r3 = r2.getColumnIndex(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r11 = r2.getString(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r3 = "Status"
            int r3 = r2.getColumnIndex(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r12 = r2.getString(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r3 = "Dirty"
            int r3 = r2.getColumnIndex(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r3 = r2.getString(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.Boolean r3 = java.lang.Boolean.valueOf(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            r3.booleanValue()     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            java.lang.String r3 = "updateVia"
            int r3 = r2.getColumnIndex(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            r2.getInt(r3)     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            r2.close()     // Catch: java.lang.Exception -> La5 java.lang.Throwable -> Lc3
            com.motorola.otalib.common.Environment.AndroidDB$1 r2 = new com.motorola.otalib.common.Environment.AndroidDB$1     // Catch: java.lang.Throwable -> La7 java.lang.Exception -> La9
            r4 = r2
            r5 = r13
            r6 = r14
            r4.<init>()     // Catch: java.lang.Throwable -> La7 java.lang.Exception -> La9
            return r2
        La5:
            r13 = move-exception
            goto Lab
        La7:
            r13 = move-exception
            goto Lc5
        La9:
            r13 = move-exception
            r2 = r1
        Lab:
            java.lang.String r14 = com.motorola.otalib.common.CommonLogger.TAG     // Catch: java.lang.Throwable -> Lc3
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> Lc3
            r3.<init>(r0)     // Catch: java.lang.Throwable -> Lc3
            java.lang.StringBuilder r13 = r3.append(r13)     // Catch: java.lang.Throwable -> Lc3
            java.lang.String r13 = r13.toString()     // Catch: java.lang.Throwable -> Lc3
            com.motorola.otalib.common.CommonLogger.e(r14, r13)     // Catch: java.lang.Throwable -> Lc3
            if (r2 == 0) goto Lc2
            r2.close()
        Lc2:
            return r1
        Lc3:
            r13 = move-exception
            r1 = r2
        Lc5:
            if (r1 == 0) goto Lca
            r1.close()
        Lca:
            throw r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.otalib.common.Environment.AndroidDB.getDescription(java.lang.String):com.motorola.otalib.common.Environment.ApplicationEnv$Database$Descriptor");
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public void remove(String str) {
        exec(String.format("delete from %s where Version='%s'", VERSION_TABLE_NAME, str));
    }

    private boolean insert_version(long j, String str, String str2, String str3, MetaData metaData, String str4, String str5) {
        try {
            return exec(String.format("insert into %s values (%s, ?,?,?,?,?,?,'false',?)", VERSION_TABLE_NAME, Integer.valueOf((int) (j / 1000))), new Object[]{str, str2, str3, MetaDataBuilder.toJSONString(metaData).replaceAll("'", "''"), str4, str5, Integer.valueOf(this.mUpdateViaSha1)});
        } catch (JSONException e) {
            CommonLogger.e(CommonLogger.TAG, "Caught JSON exception while inserting version" + e);
            return false;
        }
    }

    private boolean update_version(long j, String str, String str2, String str3, String str4) {
        if (str4 == null) {
            return exec(String.format("update %s set Time=%s, State='%s', Status='%s', Dirty='false' where Version='%s'", VERSION_TABLE_NAME, Integer.valueOf((int) (j / 1000)), str2, str3, str));
        }
        return exec(String.format("update %s set Time=%s, State='%s', Status='%s', Info=?, Dirty='false' where Version='%s'", VERSION_TABLE_NAME, Integer.valueOf((int) (j / 1000)), str2, str3, str), new Object[]{str4});
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public boolean update_column_vt(String str, String str2, String str3) {
        if (str2 != null) {
            return exec(String.format("update %s set " + str + "='%s', Dirty='false' where Version='%s'", VERSION_TABLE_NAME, str2.replaceAll("'", "''"), str3));
        }
        return false;
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public void setStatus(String str, MetaData metaData, String str2, String str3, String str4, String str5) {
        String str6 = "Repository: " + str2 + "; Location: " + str3;
        if (!insert_status(getTime(), str, metaData, ApplicationEnv.PackageState.Notified.toString(), "RS_TEMP_OK", str5 != null ? str6 + "; AddOnInfo: " + str5 : str6, str2, metaData.getReportingTags(), metaData.getTrackingId())) {
            CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.setStatus failed to set Notify.");
        } else if (insert_status(getTime(), str, metaData, ApplicationEnv.PackageState.Result.toString(), "RS_FAIL", str4, str2, metaData.getReportingTags(), metaData.getTrackingId())) {
        } else {
            CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.setStatus failed to set Result.");
        }
    }

    private boolean insert_status(long j, String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11) {
        return exec(String.format("insert into %s (_id, time, dev, src, dest, state, status, info, Repository, reportingTag, trackingId, srcSha1, targetSha1, updateVia) values (NULL, %s, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", STATUS_TABLE_NAME, Long.valueOf(j)), new Object[]{str, str2, str3, str4, str5, str6, str7, str8, str9, str10, str11, Integer.valueOf(this.mUpdateViaSha1)});
    }

    private boolean insert_status(long j, String str, MetaData metaData, String str2, String str3, String str4, String str5, String str6, String str7) {
        return insert_status(j, str, metaData.getMinVersion(), metaData.getVersion(), str2, str3, str4, str5, str6, str7, metaData.getSourceSha1(), metaData.getTargetSha1());
    }

    private boolean insert_status(long j, String str, String str2, String str3, String str4) {
        ApplicationEnv.Database.Descriptor description = getDescription(str);
        if (description == null) {
            CommonLogger.e(CommonLogger.TAG, "CusAndroidDB.insert_status failed: no description for version " + str);
            return false;
        }
        return insert_status(j, str, description.getMeta(), str2, str3, str4, description.getRepository(), description.getMeta().getReportingTags(), description.getMeta().getTrackingId());
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public void remove_status(int i) {
        exec(String.format("delete from %s where _id=%s", STATUS_TABLE_NAME, Integer.valueOf(i)));
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public void clear_status() {
        exec(String.format("delete from %s", STATUS_TABLE_NAME));
    }

    /* JADX WARN: Not initialized variable reg: 3, insn: 0x00cf: MOVE  (r2 I:??[OBJECT, ARRAY]) = (r3 I:??[OBJECT, ARRAY]), block:B:22:0x00cf */
    /* JADX WARN: Removed duplicated region for block: B:24:0x00d2  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private com.motorola.otalib.common.Environment.ApplicationEnv.Database.Status get_statusInternal(java.lang.String r19) {
        /*
            r18 = this;
            java.lang.String r1 = "AndroidDB.getStatus, exception : "
            r2 = 0
            r0 = r18
            android.database.sqlite.SQLiteDatabase r3 = r0._db     // Catch: java.lang.Throwable -> Lb2 java.lang.Exception -> Lb4
            r4 = r19
            android.database.Cursor r3 = r3.rawQuery(r4, r2)     // Catch: java.lang.Throwable -> Lb2 java.lang.Exception -> Lb4
            int r4 = r3.getCount()     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            if (r4 != 0) goto L17
            r3.close()     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            return r2
        L17:
            r3.moveToNext()     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "_id"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            int r5 = r3.getInt(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "time"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            long r6 = r3.getLong(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "dev"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r15 = r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "src"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "dest"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "state"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r8 = r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "status"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r9 = r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "info"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r10 = r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "Repository"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r11 = r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "reportingTag"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r12 = r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "trackingId"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r13 = r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "srcSha1"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r14 = r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "targetSha1"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r16 = r3.getString(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            java.lang.String r4 = "updateVia"
            int r4 = r3.getColumnIndex(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            r3.getInt(r4)     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            r3.close()     // Catch: java.lang.Exception -> Lb0 java.lang.Throwable -> Lce
            com.motorola.otalib.common.Environment.AndroidDB$2 r17 = new com.motorola.otalib.common.Environment.AndroidDB$2     // Catch: java.lang.Throwable -> Lb2 java.lang.Exception -> Lb4
            r3 = r17
            r4 = r18
            r3.<init>()     // Catch: java.lang.Throwable -> Lb2 java.lang.Exception -> Lb4
            return r17
        Lb0:
            r0 = move-exception
            goto Lb6
        Lb2:
            r0 = move-exception
            goto Ld0
        Lb4:
            r0 = move-exception
            r3 = r2
        Lb6:
            java.lang.String r4 = com.motorola.otalib.common.CommonLogger.TAG     // Catch: java.lang.Throwable -> Lce
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> Lce
            r5.<init>(r1)     // Catch: java.lang.Throwable -> Lce
            java.lang.StringBuilder r0 = r5.append(r0)     // Catch: java.lang.Throwable -> Lce
            java.lang.String r0 = r0.toString()     // Catch: java.lang.Throwable -> Lce
            com.motorola.otalib.common.CommonLogger.e(r4, r0)     // Catch: java.lang.Throwable -> Lce
            if (r3 == 0) goto Lcd
            r3.close()
        Lcd:
            return r2
        Lce:
            r0 = move-exception
            r2 = r3
        Ld0:
            if (r2 == 0) goto Ld5
            r2.close()
        Ld5:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.otalib.common.Environment.AndroidDB.get_statusInternal(java.lang.String):com.motorola.otalib.common.Environment.ApplicationEnv$Database$Status");
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public ApplicationEnv.Database.Status get_status(int i) {
        return get_statusInternal(String.format("select * from %s WHERE _id=%s", STATUS_TABLE_NAME, Integer.valueOf(i)));
    }

    @Override // com.motorola.otalib.common.Environment.ApplicationEnv.Database
    public ApplicationEnv.Database.Status get_status() {
        return get_statusInternal(String.format("select * from %s ORDER BY _id LIMIT 1", STATUS_TABLE_NAME));
    }
}
