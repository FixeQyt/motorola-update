package com.motorola.otalib.main.checkUpdate;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.motorola.otalib.main.PublicUtilityMethods;
import java.util.ArrayList;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CheckRequestObj {
    private String accsSerialNumber;
    private String carrier;
    private String colorId;
    private String contextKey;
    private String internalName;
    private String manufacturer;
    private String modelId;
    private String primaryKey;
    private String product;
    private long sourceVersion;
    private PublicUtilityMethods.TRIGGER_BY triggeredBy;
    private boolean isPRCDevice = false;
    private boolean isProductionDevice = true;
    private boolean forceDownload = false;
    private boolean forceInstall = false;

    public boolean isValidRequest() {
        return (TextUtils.isEmpty(this.accsSerialNumber) || TextUtils.isEmpty(this.contextKey) || this.sourceVersion <= 0 || TextUtils.isEmpty(this.modelId)) ? false : true;
    }

    public String getPrimaryKey() {
        if (!TextUtils.isEmpty(getContextKey()) && !TextUtils.isEmpty(getAccsSerialNumber())) {
            this.primaryKey = PublicUtilityMethods.SHA1Generator(getContextKey().concat(getAccsSerialNumber()));
        }
        return this.primaryKey;
    }

    public PublicUtilityMethods.TRIGGER_BY getTriggeredBy() {
        return this.triggeredBy;
    }

    public void setTriggeredBy(PublicUtilityMethods.TRIGGER_BY trigger_by) {
        if (trigger_by == null) {
            trigger_by = PublicUtilityMethods.TRIGGER_BY.polling;
        }
        this.triggeredBy = trigger_by;
    }

    public String getContextKey() {
        return this.contextKey;
    }

    public void setContextKey(String str) {
        this.contextKey = str;
    }

    public String getAccsSerialNumber() {
        return this.accsSerialNumber;
    }

    public void setAccsSerialNumber(String str) {
        this.accsSerialNumber = str;
    }

    public String getManufacturer() {
        return this.manufacturer;
    }

    public void setManufacturer(String str) {
        this.manufacturer = str;
    }

    public String getColorId() {
        return this.colorId;
    }

    public void setColorId(String str) {
        this.colorId = str;
    }

    public String getModelId() {
        return this.modelId;
    }

    public void setModelId(String str) {
        this.modelId = str;
    }

    public String getProduct() {
        return this.product;
    }

    public void setProduct(String str) {
        this.product = str;
    }

    public long getSourceVersion() {
        return this.sourceVersion;
    }

    public CheckRequestObj setSourceVersion(long j) {
        this.sourceVersion = j;
        return this;
    }

    public String getInternalName() {
        return this.internalName;
    }

    public void setInternalName(String str) {
        this.internalName = str;
    }

    public boolean getIsPRCDevice() {
        return this.isPRCDevice;
    }

    public void setPRCDevice(boolean z) {
        this.isPRCDevice = z;
    }

    public boolean getIsProductionDevice() {
        return this.isProductionDevice;
    }

    public void setProductionDevice(boolean z) {
        this.isProductionDevice = z;
    }

    public boolean isForceDownload() {
        return this.forceDownload;
    }

    public void setForceDownload(boolean z) {
        this.forceDownload = z;
    }

    public boolean isForceInstall() {
        return this.forceInstall;
    }

    public void setForceInstall(boolean z) {
        this.forceInstall = z;
    }

    public String getCarrier() {
        return this.carrier;
    }

    public void setCarrier(String str) {
        this.carrier = str;
    }

    public static String arrayListToJson(List<CheckRequestObj> list) {
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();
        for (CheckRequestObj checkRequestObj : list) {
            jsonArray.add(gson.toJsonTree(checkRequestObj).getAsJsonObject());
        }
        return jsonArray.toString();
    }

    public static List<CheckRequestObj> arrayListFromJson(String str) {
        Gson gson = new Gson();
        JsonArray jsonArray = (JsonArray) gson.fromJson(str, JsonArray.class);
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < jsonArray.size(); i++) {
            arrayList.add((CheckRequestObj) gson.fromJson(jsonArray.get(i).getAsJsonObject(), CheckRequestObj.class));
        }
        return arrayList;
    }

    public String toString() {
        return new Gson().toJson(this);
    }

    public static CheckRequestObj fromJsonString(String str) {
        return (CheckRequestObj) new Gson().fromJson(str, CheckRequestObj.class);
    }
}
