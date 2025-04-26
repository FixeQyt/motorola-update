package com.motorola.otalib.main;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.settings.Settings;
import com.motorola.otalib.common.utils.UpgradeUtils;
import com.motorola.otalib.main.PublicUtilityMethods;
import com.motorola.otalib.main.checkUpdate.CheckRequestObj;
import com.motorola.otalib.main.checkUpdate.UpdateSessionInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class InstallStatusInfo {
    private String accSerialNumber;
    private boolean critical;
    private boolean keepPackage;
    private String md5;
    private String preDownloadNote;
    private int progress;
    private PublicUtilityMethods.STATUS_CODE reportingError;
    private long size;
    private long sourceVersion;
    private PublicUtilityMethods.OtaState state;
    private int statusCode;
    private String statusMessage;
    private long targetVersion;

    public InstallStatusInfo() {
    }

    public InstallStatusInfo(Context context, String str, Settings settings, ApplicationEnv.Database.Descriptor descriptor, int i) {
        UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(settings, str);
        if (sessionDetails != null) {
            CheckRequestObj checkRequestObj = sessionDetails.getCheckRequestObj();
            if (checkRequestObj != null) {
                setAccSerialNumber(checkRequestObj.getAccsSerialNumber());
                try {
                    setProgress((int) ((new File(PublicUtilityMethods.getFileName(context, checkRequestObj.getInternalName(), Long.parseLong(descriptor.getMeta().getDisplayVersion()))).length() * 100) / descriptor.getMeta().getSize()));
                } catch (Exception unused) {
                    setProgress(0);
                }
            }
            if (descriptor != null && descriptor.getMeta() != null) {
                setState(UpgradeUtils.PackageStateToOtaStateConverter(descriptor.getState()));
                setStatusMessage(descriptor.getInfo());
                setTargetVersion(Long.parseLong(descriptor.getMeta().getDisplayVersion()));
                setSize(descriptor.getMeta().getSize());
                setMd5(descriptor.getMeta().getmd5CheckSum());
                setPreDownloadNote(descriptor.getMeta().getUpgradeNotification());
                setCritical(descriptor.getMeta().isForced());
                setStatusCode(i);
                return;
            }
            setState(PublicUtilityMethods.OtaState.Result);
            return;
        }
        setState(PublicUtilityMethods.OtaState.Result);
    }

    public long getSourceVersion() {
        return this.sourceVersion;
    }

    public void setSourceVersion(long j) {
        this.sourceVersion = j;
    }

    public long getTargetVersion() {
        return this.targetVersion;
    }

    public void setTargetVersion(long j) {
        this.targetVersion = j;
    }

    public String getAccSerialNumber() {
        return this.accSerialNumber;
    }

    public void setAccSerialNumber(String str) {
        this.accSerialNumber = str;
    }

    public PublicUtilityMethods.OtaState getState() {
        return this.state;
    }

    public ApplicationEnv.PackageState getServerState() {
        return UpgradeUtils.OtaLibStateToPackageStateConverter(getState());
    }

    public void setState(PublicUtilityMethods.OtaState otaState) {
        this.state = otaState;
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int i) {
        this.progress = i;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int i) {
        this.statusCode = i;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public void setStatusMessage(String str) {
        this.statusMessage = str;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long j) {
        this.size = j;
    }

    public String getMd5() {
        return this.md5;
    }

    public void setMd5(String str) {
        this.md5 = str;
    }

    public boolean isCritical() {
        return this.critical;
    }

    public void setCritical(boolean z) {
        this.critical = z;
    }

    public boolean isKeepPackage() {
        return this.keepPackage;
    }

    public void setKeepPackage(boolean z) {
        this.keepPackage = z;
    }

    public String getPreDownloadNote() {
        return this.preDownloadNote;
    }

    public void setPreDownloadNote(String str) {
        this.preDownloadNote = str;
    }

    public static String arrayListToJson(List<InstallStatusInfo> list) {
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();
        for (InstallStatusInfo installStatusInfo : list) {
            jsonArray.add(gson.toJsonTree(installStatusInfo).getAsJsonObject());
        }
        return jsonArray.toString();
    }

    public static List<InstallStatusInfo> arrayListFromJson(String str) {
        Gson gson = new Gson();
        JsonArray jsonArray = (JsonArray) gson.fromJson(str, JsonArray.class);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jsonArray.size(); i++) {
            arrayList.add((InstallStatusInfo) gson.fromJson(jsonArray.get(i).getAsJsonObject(), InstallStatusInfo.class));
        }
        return arrayList;
    }

    public PublicUtilityMethods.STATUS_CODE getReportingError() {
        return this.reportingError;
    }

    public void setReportingError(PublicUtilityMethods.STATUS_CODE status_code) {
        this.reportingError = status_code;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public static InstallStatusInfo fromJsonString(String str) {
        return (InstallStatusInfo) new Gson().fromJson(str, InstallStatusInfo.class);
    }
}
