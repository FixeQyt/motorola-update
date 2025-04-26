package com.motorola.ccc.ota.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.otalib.cdsservice.utils.CDSUtils;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class BuildPropReader {
    public static final String ALL_HEX_NUMERIC_PATTERN = "^[0-9a-fA-F]{14,50}";
    private static final String BOOTLOADER_BOOT_STATE = "ro.boot.verifiedbootstate";
    public static final String BOOTLOADER_LOCKED = "locked";
    public static final String BOOTLOADER_NA = "not-applicable";
    private static final String BOOTLOADER_SECURE_HW = "ro.boot.secure_hardware";
    public static final String BOOTLOADER_UNLOCKED = "unlocked";
    public static final String CHANNEL_ID = "channel_id";
    private static final String COUNTRY = "ro.product.locale.region";
    private static final String DEVICE_BUILD_ID = "ro.build.id";
    private static final String DEVICE_CHIPSET = "persist.vendor.radio.mcfg_ver_num";
    private static final String DEVICE_MCFG_CONFIG_VERSION = "persist.vendor.radio.mcfg_ver_num";
    private static final String DEVICE_RAM = "ro.vendor.hw.ram";
    private static final String DEVICE_SHA1 = "ro.mot.build.guid";
    private static final String DEVICE_VERITY_STATE = "ro.boot.veritymode";
    private static final String HW_MODEL_PROP = "ro.product.model";
    public static final String IMEI_NOT_AVAILABLE = "IMEI_NOT_AVAILABLE";
    public static final String INVALID_IMEI = "INVALID_IMEI";
    private static final String IS_DAP_DEVICE = "ro.boot.dynamic_partitions";
    private static final String IS_TREBLE_ENABLED = "ro.treble.enabled";
    private static final String KEY_LENOVO_SETUP_PRIVACY = "persist.sys.lenovo_setup_privacy";
    private static final String LANGUAGE = "language";
    private static final String LOCALE = "ro.product.locale";
    private static final String MCCMNC_NOT_AVAILABLE = "MCCMNC_NOT_AVAILABLE";
    public static final String MOTO_SETTINGS_UNKNOWN = "unknown";
    private static final String MOT_OVERLAY = "ro.mot.overlay.version";
    private static final String NOT_AVAILABLE = "not_available";
    private static final String PRODUCT = "ro.vendor.product.display";
    private static final String PRODUCT_NAME = "ro.vendor.product.display";
    private static final String REGION = "region";
    private static final String REMOUNT_DEVICE_STATE = "dev.mnt.blk.mnt.scratch";
    private static final String ROOT_STATUS = "persist.qe";
    private static final String RO_ANDROID_VERSION = "ro.build.version.release";
    private static final String RO_BUILD_PRODUCT = "ro.build.product";
    private static final String RO_CUSTOMERID = "ro.mot.build.customerid";
    private static final String RO_DATE_UTC = "ro.build.date.utc";
    private static final String RO_HARDWARE_SKU = "ro.boot.hardware.sku";
    private static final String RO_OTA_RESERVED_SPACE = "ro.mot.ota.reserved.space";
    private static final String RO_PRODUCT_NAME = "ro.product.name";
    private static final String RO_SECURITY_PATCH = "ro.build.version.security_patch";
    private static final String RO_VAB_COMPRESSION = "ro.virtual_ab.compression.enabled";
    private static final String RO_VAB_UPDATE = "ro.virtual_ab.enabled";

    public static String getOverlay() {
        if (TextUtils.isEmpty(BuildPropertyUtils.getSystemStringProperty(MOT_OVERLAY))) {
            return null;
        }
        return BuildPropertyUtils.getSystemStringProperty(MOT_OVERLAY);
    }

    public static long getOtaReservedSpaceValue() {
        String systemStringProperty = BuildPropertyUtils.getSystemStringProperty(RO_OTA_RESERVED_SPACE);
        if (TextUtils.isEmpty(systemStringProperty)) {
            return -1L;
        }
        try {
            return Long.parseLong(systemStringProperty);
        } catch (Exception e) {
            Logger.error("OtaApp", "getLongValue failed to parse ro.mot.ota.reserved.space: " + e.toString());
            return -1L;
        }
    }

    public static String getRootStatus() {
        return BuildPropertyUtils.getSystemStringProperty(ROOT_STATUS);
    }

    public static String getCarrier() {
        return getCarrierName(OtaApplication.getGlobalContext());
    }

    public static boolean isATT() {
        String carrier = getCarrier();
        return (TextUtils.isEmpty(carrier) || "unknown".equals(carrier)) ? "att".equalsIgnoreCase(queryCustomeridOrBrand()) || "attpre".equalsIgnoreCase(queryCustomeridOrBrand()) || "cricket".equalsIgnoreCase(queryCustomeridOrBrand()) : "att".equalsIgnoreCase(carrier) || "attpre".equalsIgnoreCase(carrier) || "cricket".equalsIgnoreCase(carrier);
    }

    public static boolean is4GBRam() {
        return "4GB".equals(BuildPropertyUtils.getSystemStringProperty(DEVICE_RAM));
    }

    public static String getDeviceChipset() {
        return !TextUtils.isEmpty(BuildPropertyUtils.getSystemStringProperty("persist.vendor.radio.mcfg_ver_num")) ? "Qualcomm" : "Others";
    }

    public static boolean isBotaATT() {
        return isATT() && BuildPropertyUtils.isProductWaveAtleastRefWave("2023.4");
    }

    public static boolean isFotaATT() {
        return isATT() && !BuildPropertyUtils.isProductWaveAtleastRefWave("2023.4");
    }

    public static boolean isVerizon() {
        String carrier = getCarrier();
        if (TextUtils.isEmpty(carrier) || "unknown".equals(carrier)) {
            return "verizon".equalsIgnoreCase(queryCustomeridOrBrand());
        }
        return "verizon".equalsIgnoreCase(carrier) || "vzw".equalsIgnoreCase(carrier) || "vzwpre".equalsIgnoreCase(carrier);
    }

    public static boolean isSoftbank() {
        String carrier = getCarrier();
        return (TextUtils.isEmpty(carrier) || "unknown".equals(carrier)) ? "softbank".equalsIgnoreCase(queryCustomeridOrBrand()) || "ymobile".equalsIgnoreCase(queryCustomeridOrBrand()) : "softbank".equalsIgnoreCase(carrier) || "ymobile".equalsIgnoreCase(carrier);
    }

    public static boolean isSoftbankSIM() {
        TelephonyManager telephonyManager = (TelephonyManager) OtaApplication.getGlobalContext().getSystemService("phone");
        List<SubscriptionInfo> activeSubscriptionInfoList = ((SubscriptionManager) OtaApplication.getGlobalContext().getSystemService("telephony_subscription_service")).getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null) {
            return false;
        }
        for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
            TelephonyManager createForSubscriptionId = telephonyManager.createForSubscriptionId(subscriptionInfo.getSubscriptionId());
            String simOperator = createForSubscriptionId.getSimOperator();
            String subscriberId = createForSubscriptionId.getSubscriberId();
            if ("44020".equals(simOperator)) {
                return true;
            }
            if (subscriberId != null && subscriberId.startsWith("44020")) {
                return true;
            }
        }
        return false;
    }

    public static String getFingerPrint() {
        return Build.FINGERPRINT;
    }

    public static String getModel() {
        String systemStringProperty = BuildPropertyUtils.getSystemStringProperty("ro.vendor.product.display");
        return TextUtils.isEmpty(systemStringProperty) ? Build.MODEL : systemStringProperty;
    }

    public static String getDeviceModel() {
        return BuildPropertyUtils.getSystemStringProperty(HW_MODEL_PROP) != null ? BuildPropertyUtils.getSystemStringProperty(HW_MODEL_PROP) : "Unknown";
    }

    public static String getProductName() {
        return BuildPropertyUtils.getSystemStringProperty(RO_PRODUCT_NAME) != null ? BuildPropertyUtils.getSystemStringProperty(RO_PRODUCT_NAME) : "Unknown";
    }

    public static String getHardwareSku() {
        return BuildPropertyUtils.getSystemStringProperty(RO_HARDWARE_SKU);
    }

    public static String getDeviceName() {
        return BuildPropertyUtils.getSystemStringProperty(RO_BUILD_PRODUCT);
    }

    private static String queryCustomeridOrBrand() {
        String systemStringProperty = BuildPropertyUtils.getSystemStringProperty(RO_CUSTOMERID);
        return systemStringProperty != null ? systemStringProperty : Build.BRAND;
    }

    public static boolean isUEUpdateEnabled() {
        String name;
        MetaData from = MetaDataBuilder.from(new BotaSettings().getString(Configs.METADATA));
        if (from != null) {
            name = from.getAbInstallType();
        } else {
            name = UpgradeUtils.AB_INSTALL_TYPE.defaultAb.name();
        }
        return BuildPropertyUtils.doesDeviceSupportAbUpdate() && !UpgradeUtils.AB_INSTALL_TYPE.classicOnAb.name().contentEquals(name);
    }

    public static boolean doesDeviceSupportVABUpdate() {
        return BuildPropertyUtils.getSystemBooleanProperty(RO_VAB_UPDATE, false);
    }

    public static boolean doesDeviceSupportVABc() {
        return BuildPropertyUtils.getSystemBooleanProperty(RO_VAB_COMPRESSION, false);
    }

    public static boolean isStreamingUpdate() {
        String name;
        if (BuildPropertyUtils.doesDeviceSupportAbUpdate()) {
            MetaData from = MetaDataBuilder.from(new BotaSettings().getString(Configs.METADATA));
            if (from != null) {
                name = from.getAbInstallType();
            } else {
                name = UpgradeUtils.AB_INSTALL_TYPE.defaultAb.name();
            }
            return UpgradeUtils.AB_INSTALL_TYPE.streamingOnAb.name().contentEquals(name);
        }
        return false;
    }

    public static String getBuildDescription() {
        return Build.DISPLAY;
    }

    public static String getSecurityPatch() {
        return BuildPropertyUtils.getSystemStringProperty(RO_SECURITY_PATCH);
    }

    public static String getAndroidVersion() {
        return BuildPropertyUtils.getSystemStringProperty(RO_ANDROID_VERSION);
    }

    public static long getCurrentUTC() {
        try {
            return Long.valueOf(BuildPropertyUtils.getSystemStringProperty(RO_DATE_UTC)).longValue();
        } catch (NumberFormatException unused) {
            return 0L;
        }
    }

    public static boolean getTrebleEnabled() {
        String systemStringProperty = BuildPropertyUtils.getSystemStringProperty(IS_TREBLE_ENABLED);
        Logger.debug("OtaApp", "Does this device support treble feature " + systemStringProperty);
        return "TRUE".equalsIgnoreCase(systemStringProperty);
    }

    public static String getBuildId() {
        return Build.ID;
    }

    public static String getContextKey(String str) {
        return getDeviceSha1(str);
    }

    public static JSONObject getDeviceInfoAsJsonObject() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("manufacturer", Build.MANUFACTURER);
        linkedHashMap.put("hardware", Build.HARDWARE);
        linkedHashMap.put("brand", Build.BRAND);
        linkedHashMap.put("model", Build.MODEL);
        linkedHashMap.put("product", BuildPropertyUtils.getSystemStringProperty("ro.vendor.product.display"));
        linkedHashMap.put("os", getOperatingSystem());
        linkedHashMap.put("osVersion", Build.VERSION.RELEASE);
        linkedHashMap.put("country", BuildPropertyUtils.getSystemStringProperty(COUNTRY));
        linkedHashMap.put(REGION, getValue(BuildPropertyUtils.getSystemStringProperty(LOCALE), REGION));
        linkedHashMap.put(LANGUAGE, getValue(BuildPropertyUtils.getSystemStringProperty(LOCALE), LANGUAGE));
        linkedHashMap.put("userLanguage", Locale.getDefault().toString());
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "getDeviceInfoAsJsonObject");
    }

    public static String getDeviceSha1(String str) {
        if (UpgradeSourceType.modem.toString().equals(str)) {
            return getDeviceModemConfigVersionSha1();
        }
        return BuildPropertyUtils.getSystemStringProperty(DEVICE_SHA1);
    }

    public static String getMCFGConfigVersion() {
        return BuildPropertyUtils.getSystemStringProperty("persist.vendor.radio.mcfg_ver_num");
    }

    public static Map<String, String> getMCFGConfigVersionMap() {
        String mCFGConfigVersion = getMCFGConfigVersion();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        int i = 0;
        for (String str : mCFGConfigVersion.split(",")) {
            i++;
            linkedHashMap.put("" + i, str);
        }
        Logger.debug("OtaApp", "getMCFGConfigVersionMap:configVersionMap =" + linkedHashMap);
        return linkedHashMap;
    }

    public static String getDeviceModemConfigVersion() {
        Map<String, String> storedModemMap = UpdaterUtils.getStoredModemMap();
        if (storedModemMap != null && !storedModemMap.isEmpty()) {
            String value = storedModemMap.entrySet().iterator().next().getValue();
            if (!TextUtils.isEmpty(value) && !"0".equals(value)) {
                return value;
            }
        }
        return "";
    }

    public static String getDeviceModemConfigVersionSha1() {
        String deviceModemConfigVersion = getDeviceModemConfigVersion();
        if (TextUtils.isEmpty(deviceModemConfigVersion)) {
            return "";
        }
        return BuildPropertyUtils.generateSHA1(BuildPropertyUtils.getSystemStringProperty(DEVICE_BUILD_ID) + deviceModemConfigVersion);
    }

    public static String getDeviceModemSourceSha1() {
        Map<String, String> storedModemMap = UpdaterUtils.getStoredModemMap();
        if (storedModemMap == null || storedModemMap.isEmpty()) {
            return "";
        }
        String str = getMCFGConfigVersionMap().get(storedModemMap.entrySet().iterator().next().getKey());
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return BuildPropertyUtils.generateSHA1(BuildPropertyUtils.getSystemStringProperty(DEVICE_BUILD_ID) + str);
    }

    public static JSONObject getExtraInfoAsJsonObject(Context context, String str, int i, String str2, String str3, String str4, String str5, BotaSettings botaSettings) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("clientIdentity", "motorola-ota-client-app");
        linkedHashMap.put("carrier", getCarrierName(context));
        linkedHashMap.put("bootloaderVersion", Build.BOOTLOADER);
        linkedHashMap.put("brand", Build.BRAND);
        linkedHashMap.put("model", Build.MODEL);
        linkedHashMap.put("fingerprint", Build.FINGERPRINT);
        linkedHashMap.put("radioVersion", Build.getRadioVersion());
        linkedHashMap.put("buildTags", Build.TAGS);
        linkedHashMap.put("buildType", Build.TYPE);
        linkedHashMap.put("buildDevice", Build.DEVICE);
        linkedHashMap.put("buildId", Build.ID);
        linkedHashMap.put("buildDisplayId", Build.DISPLAY);
        linkedHashMap.put("buildIncrementalVersion", Build.VERSION.INCREMENTAL);
        linkedHashMap.put("releaseVersion", Build.VERSION.RELEASE);
        linkedHashMap.put("otaSourceSha1", getDeviceSha1(str4));
        linkedHashMap.put("network", str);
        linkedHashMap.put("apkVersion", Integer.valueOf(i));
        linkedHashMap.put("provisionedTime", Long.valueOf(str2 != null ? getLongValue(str2) : 0L));
        linkedHashMap.put("incrementalVersion", Integer.valueOf(str3 != null ? getIntValue(str3) : 0));
        linkedHashMap.put("additionalInfo", str5);
        linkedHashMap.put("userLocation", LocationUtils.isDeviceInChina(context) ? "CN" : "Non-CN");
        linkedHashMap.put("bootloaderStatus", getBootloaderStatus());
        linkedHashMap.put("deviceRooted", isDeviceRooted());
        linkedHashMap.put("is4GBRam", Boolean.valueOf(is4GBRam()));
        linkedHashMap.put("deviceChipset", getDeviceChipset());
        if (botaSettings.getBoolean(Configs.IS_UPLOAD_UE_FAILURE_LOGS_ENABLED)) {
            linkedHashMap.put("prevLogFileUploadUri", botaSettings.getString(Configs.PREV_LOG_FILE_UPLOAD_LINK));
        }
        String[] mccMncValues = getMccMncValues();
        String[] imeiValues = getImeiValues();
        try {
            int parseInt = Integer.parseInt(UpdaterUtils.getStoredModemMap().entrySet().iterator().next().getKey());
            if (parseInt < 1) {
                parseInt = 1;
            }
            int i2 = parseInt - 1;
            linkedHashMap.put("mccmnc", mccMncValues[i2]);
            linkedHashMap.put("imei", imeiValues[i2]);
        } catch (Exception unused) {
            linkedHashMap.put("mccmnc", mccMncValues[0]);
            linkedHashMap.put("imei", imeiValues[0]);
        }
        if (isMultiSimDevice()) {
            linkedHashMap.put("mccmnc2", mccMncValues[1]);
            linkedHashMap.put("imei2", imeiValues[1]);
        }
        linkedHashMap.put("ro.mot.build.device", BuildPropertyUtils.getSystemStringProperty("ro.mot.build.device", "unknown"));
        linkedHashMap.put("ro.vendor.hw.storage", BuildPropertyUtils.getSystemStringProperty("ro.vendor.hw.storage", "unknown"));
        linkedHashMap.put(DEVICE_RAM, BuildPropertyUtils.getSystemStringProperty(DEVICE_RAM, "unknown"));
        linkedHashMap.put("ro.vendor.hw.esim", BuildPropertyUtils.getSystemStringProperty("ro.vendor.hw.esim", "unknown"));
        linkedHashMap.put("ro.mot.product_wave", BuildPropertyUtils.getSystemStringProperty("ro.mot.product_wave", "unknown"));
        linkedHashMap.put("ro.mot.build.oem.product", BuildPropertyUtils.getSystemStringProperty("ro.mot.build.oem.product", "unknown"));
        linkedHashMap.put("ro.mot.build.system.product", BuildPropertyUtils.getSystemStringProperty("ro.mot.build.system.product", "unknown"));
        linkedHashMap.put("ro.mot.build.product.increment", BuildPropertyUtils.getSystemStringProperty("ro.mot.build.product.increment", "unknown"));
        linkedHashMap.put(DEVICE_VERITY_STATE, BuildPropertyUtils.getSystemStringProperty(DEVICE_VERITY_STATE, "unknown"));
        linkedHashMap.put("partition.system.verified", BuildPropertyUtils.getSystemStringProperty("partition.system.verified", "unknown"));
        linkedHashMap.put("ro.mot.version", Integer.valueOf(BuildPropertyUtils.getSystemIntProperty("ro.mot.version", -1)));
        linkedHashMap.put("securityVersion", BuildPropertyUtils.getSystemStringProperty(RO_SECURITY_PATCH));
        linkedHashMap.put("ro.enterpriseedition", Boolean.valueOf(SmartUpdateUtils.isMotoSettingsGlobalEnterpriseEditionFlagSet()));
        linkedHashMap.put(RO_VAB_UPDATE, Boolean.valueOf(doesDeviceSupportVABUpdate()));
        if (botaSettings.getBoolean(Configs.INITIAL_SETUP_COMPLETED) && botaSettings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            botaSettings.removeConfig(Configs.FLAG_IS_VITAL_UPDATE);
        }
        linkedHashMap.put("vitalUpdate", Boolean.valueOf(botaSettings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)));
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "BuildPropReader:ExtraInfo ");
    }

    private static String[] getMccMncValues() {
        String[] strArr = new String[2];
        int i = 0;
        strArr[0] = MCCMNC_NOT_AVAILABLE;
        strArr[1] = MCCMNC_NOT_AVAILABLE;
        TelephonyManager telephonyManager = (TelephonyManager) OtaApplication.getGlobalContext().getSystemService("phone");
        List<SubscriptionInfo> activeSubscriptionInfoList = ((SubscriptionManager) OtaApplication.getGlobalContext().getSystemService("telephony_subscription_service")).getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null) {
            Logger.error("OtaApp", "getMccMncValues:subscriptionInfoList null send default values");
            return strArr;
        }
        for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
            String simOperator = telephonyManager.createForSubscriptionId(subscriptionInfo.getSubscriptionId()).getSimOperator();
            strArr[i] = simOperator;
            if (TextUtils.isEmpty(simOperator)) {
                strArr[i] = MCCMNC_NOT_AVAILABLE;
            }
            i++;
        }
        return strArr;
    }

    private static String[] getImeiValues() {
        String[] strArr = new String[2];
        int i = 0;
        strArr[0] = IMEI_NOT_AVAILABLE;
        strArr[1] = IMEI_NOT_AVAILABLE;
        TelephonyManager telephonyManager = (TelephonyManager) OtaApplication.getGlobalContext().getSystemService("phone");
        List<SubscriptionInfo> activeSubscriptionInfoList = ((SubscriptionManager) OtaApplication.getGlobalContext().getSystemService("telephony_subscription_service")).getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null) {
            Logger.error("OtaApp", "getImeiValues: subscriptionInfoList null send default values");
            return strArr;
        }
        for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
            String imei = telephonyManager.createForSubscriptionId(subscriptionInfo.getSubscriptionId()).getImei();
            strArr[i] = imei;
            if (TextUtils.isEmpty(imei)) {
                strArr[i] = IMEI_NOT_AVAILABLE;
            } else if (!Pattern.matches(ALL_HEX_NUMERIC_PATTERN, strArr[i])) {
                Logger.error("OtaApp", "getImeiValues: invalid send default values");
                strArr[i] = INVALID_IMEI;
            }
            i++;
        }
        return strArr;
    }

    public static String getBootloaderStatus() {
        String systemStringProperty = BuildPropertyUtils.getSystemStringProperty(BOOTLOADER_SECURE_HW);
        String systemStringProperty2 = BuildPropertyUtils.getSystemStringProperty(BOOTLOADER_BOOT_STATE);
        if ("1".equals(systemStringProperty)) {
            if ("green".equals(systemStringProperty2)) {
                return BOOTLOADER_LOCKED;
            }
            return BOOTLOADER_UNLOCKED;
        }
        return BOOTLOADER_NA;
    }

    public static String isDeviceRooted() {
        String systemStringProperty = BuildPropertyUtils.getSystemStringProperty(REMOUNT_DEVICE_STATE);
        String systemStringProperty2 = BuildPropertyUtils.getSystemStringProperty(DEVICE_VERITY_STATE);
        String systemStringProperty3 = BuildPropertyUtils.getSystemStringProperty(IS_DAP_DEVICE);
        if (!BuildPropertyUtils.isSecure()) {
            if (!TextUtils.isEmpty(systemStringProperty)) {
                if ("true".equals(systemStringProperty3)) {
                    Logger.debug("OtaApp", "Device has been remounted. But can bere enabling verity");
                    return "enableVerity";
                }
                Logger.debug("OtaApp", "Device has been remounted. Reflash required");
                return "deviceRemounted";
            } else if ("disabled".equalsIgnoreCase(systemStringProperty2)) {
                Logger.debug("OtaApp", "Verity disabled. Need to re enable verity");
                return "enableVerity";
            } else {
                return "false";
            }
        }
        return "false";
    }

    private static long getLongValue(String str) {
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            Logger.error("OtaApp", "Failed to parse long value from string " + str + " exception :" + e);
            return 0L;
        }
    }

    private static int getIntValue(String str) {
        try {
            return Integer.valueOf(str).intValue();
        } catch (Exception e) {
            Logger.error("OtaApp", "Failed to parse int value from string " + str + " exception :" + e);
            return 0;
        }
    }

    public static JSONObject getIdentityInfoAsJsonObject(Context context) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(CDSUtils.IDTYPE, BuildPropertyUtils.getId(OtaApplication.getGlobalContext()));
            return jSONObject;
        } catch (JSONException e) {
            Logger.error("OtaApp", "Failed to create JSON object for identityInfo" + e);
            return null;
        }
    }

    public static String getMotoSettingValueAsString(Context context, String str) {
        try {
            String str2 = (String) Class.forName("com.motorola.android.provider.MotorolaSettings$Global").getMethod("getString", ContentResolver.class, String.class).invoke(null, context.getContentResolver(), str);
            return str2 != null ? str2 : "unknown";
        } catch (Exception e) {
            Logger.error("OtaApp", "get " + str + " from MotorolaSettings failed - exception : " + e.toString());
            return "unknown";
        }
    }

    public static String getCarrierName(Context context) {
        return getMotoSettingValueAsString(context, "channel_id");
    }

    private static String getOperatingSystem() {
        String property = System.getProperty("os.name");
        String property2 = System.getProperty("vm.name");
        return property + SmartUpdateUtils.MASK_SEPARATOR + property2 + SmartUpdateUtils.MASK_SEPARATOR + System.getProperty("runtime.name");
    }

    private static boolean isMultiSimDevice() {
        return ((TelephonyManager) OtaApplication.getGlobalContext().getSystemService("phone")).isMultiSimSupported() == 0;
    }

    private static String getValue(String str, String str2) {
        if (!TextUtils.isEmpty(str)) {
            try {
                String[] split = str.split("-");
                if (split != null) {
                    if (REGION.equals(str2) && split.length > 1) {
                        return split[1];
                    }
                    if (LANGUAGE.equals(str2) && split.length > 0) {
                        return split[0];
                    }
                    return NOT_AVAILABLE;
                }
                return NOT_AVAILABLE;
            } catch (Exception e) {
                Logger.error("OtaApp", "CDSUtils.getValue, exception " + e);
                return NOT_AVAILABLE;
            }
        }
        return NOT_AVAILABLE;
    }

    public static boolean isCtaVersion(BotaSettings botaSettings) {
        Logger.verbose("OtaApp", "isCtaVersion:isChinaDevice=" + BuildPropertyUtils.isChinaDevice(OtaApplication.getGlobalContext()));
        if (BuildPropertyUtils.isChinaDevice(OtaApplication.getGlobalContext())) {
            if (botaSettings.getBoolean(Configs.USER_ACCEPTED_CTA_BG_DATA)) {
                Logger.debug("OtaApp", "User enabled the CTA bg data to allow the OTA updates");
                return false;
            }
            String systemStringProperty = BuildPropertyUtils.getSystemStringProperty(KEY_LENOVO_SETUP_PRIVACY, "false");
            Logger.debug("OtaApp", "isCtaVersion=" + systemStringProperty);
            return systemStringProperty != null && systemStringProperty.contains("true");
        }
        return false;
    }
}
