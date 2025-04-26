package com.motorola.ccc.ota.env;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.admin.FreezePeriod;
import android.app.admin.SystemUpdatePolicy;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtilConstants;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtils;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class SystemUpdaterPolicy {
    public void onSystemUpdatePolicyChanged(BotaSettings botaSettings, boolean z) {
        int policyType = getPolicyType();
        Logger.debug("OtaApp", "CusSM.onSystemUpdatePolicyChanged, policyType : " + policyType);
        if (policyType == 3) {
            botaSettings.setLong(Configs.SYSTEM_UPDATE_POLICY_POSTPONE, System.currentTimeMillis() + UpgradeUtilConstants.SYSTEM_UPDATE_POLICY_POSTPONE_INTERVAL);
            if (z) {
                UpgradeUtilMethods.cancelOta("Device is under system update policy : Postpone policy is set and end Time is" + botaSettings.getLong(Configs.SYSTEM_UPDATE_POLICY_POSTPONE, -1L), ErrorCodeMapper.KEY_SYSTEM_UPDATE_POLICY);
                return;
            }
        } else {
            botaSettings.removeConfig(Configs.SYSTEM_UPDATE_POLICY_POSTPONE);
        }
        if (policyType == 2) {
            botaSettings.setLong(Configs.SYSTEM_UPDATE_POLICY_WINDOWED, System.currentTimeMillis() + UpgradeUtilConstants.SYSTEM_UPDATE_POLICY_POSTPONE_INTERVAL);
        } else {
            botaSettings.removeConfig(Configs.SYSTEM_UPDATE_POLICY_WINDOWED);
        }
        if (z) {
            OtaApplication.getGlobalContext().sendBroadcast(new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE), Permissions.INTERACT_OTA_SERVICE);
            NotificationUtils.cancelOtaNotification();
        }
    }

    public String getSystemUpdateInfo() {
        return getPolicyType() + SmartUpdateUtils.MASK_SEPARATOR + getInstallWindowStartTime() + SmartUpdateUtils.MASK_SEPARATOR + getInstallWindowEndTime() + SmartUpdateUtils.MASK_SEPARATOR + System.currentTimeMillis() + "";
    }

    public int getPolicyType() {
        Context globalContext = OtaApplication.getGlobalContext();
        if (ThinkShieldUtilConstants.ASC_VERSION_V3.equals(ThinkShieldUtils.getAscVersion(globalContext))) {
            return ThinkShieldUtils.getPolicyType(globalContext);
        }
        try {
            SystemUpdatePolicy systemUpdatePolicy = ((DevicePolicyManager) globalContext.getSystemService("device_policy")).getSystemUpdatePolicy();
            if (systemUpdatePolicy != null) {
                return systemUpdatePolicy.getPolicyType();
            }
            return -1;
        } catch (Exception e) {
            Logger.debug("OtaApp", "UpdaterUtils.handleSystemUpdatePolicy, exception: " + e);
            return -1;
        }
    }

    private int getCurrentTimeInMins() {
        Calendar calendar = Calendar.getInstance();
        return (int) (calendar.get(12) + TimeUnit.HOURS.toMinutes(calendar.get(11)));
    }

    public boolean handleSystemUpdatePolicy(Context context, Intent intent, BotaSettings botaSettings) {
        long currentTimeMillis = System.currentTimeMillis();
        String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_VERSION);
        int policyType = getPolicyType();
        UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent);
        if (policyType == -1 || !upgradeInfoDuringOTAUpdate.getEnterpriseOta()) {
            return false;
        }
        if (policyType == 1) {
            Logger.info("OtaApp", "UpdaterUtils.handleSystemUpdatePolicy, automatic install policy is set, proceeding with install");
            UpgradeUtilMethods.sendUpgradeLaunchProceed(context, stringExtra, true, "automaticInstall");
        } else if (policyType == 3) {
            long j = botaSettings.getLong(Configs.SYSTEM_UPDATE_POLICY_POSTPONE, -1L);
            Logger.debug("OtaApp", "UpdaterUtils.handleSystemUpdatePolicy, postPoneTime = " + j);
            if (currentTimeMillis >= j) {
                return false;
            }
            UpdaterUtils.setAlarm(j - currentTimeMillis);
        } else if (policyType == 2) {
            int installWindowStartTime = getInstallWindowStartTime();
            int installWindowEndTime = getInstallWindowEndTime();
            int currentTimeInMins = getCurrentTimeInMins();
            long j2 = botaSettings.getLong(Configs.WINDOW_POLICY_START_TIMESTAMP, -1L);
            long j3 = botaSettings.getLong(Configs.WINDOW_POLICY_END_TIMESTAMP, -1L);
            if (currentTimeMillis >= botaSettings.getLong(Configs.SYSTEM_UPDATE_POLICY_WINDOWED, -1L)) {
                return false;
            }
            if ((currentTimeInMins >= installWindowStartTime && currentTimeInMins <= installWindowEndTime) || (j2 != -1 && currentTimeMillis >= j2 && currentTimeMillis <= j3)) {
                Logger.info("OtaApp", "UpdaterUtils.handleSystemUpdatePolicy, currentTime falls in windowed maintainence timings, proceeding with install");
                UpgradeUtilMethods.sendUpgradeLaunchProceed(context, stringExtra, true, "windowedInstall");
                return true;
            }
            scheduleWindowUpdate();
            return true;
        }
        return true;
    }

    private void scheduleWindowUpdate() {
        int currentTimeInMins = getCurrentTimeInMins();
        int installWindowStartTime = getInstallWindowStartTime();
        int installWindowEndTime = getInstallWindowEndTime();
        long currentTimeMillis = System.currentTimeMillis();
        if (installWindowStartTime == -1 || installWindowEndTime == -1) {
            return;
        }
        BotaSettings botaSettings = new BotaSettings();
        if (currentTimeInMins < installWindowStartTime) {
            int i = installWindowStartTime - currentTimeInMins;
            Logger.debug("OtaApp", "UpdaterUtils.handleSystemUpdatePolicy, next maintenance window will be " + i + " mins from now");
            long j = (i * 60000) + currentTimeMillis;
            botaSettings.setLong(Configs.WINDOW_POLICY_START_TIMESTAMP, j);
            botaSettings.setLong(Configs.WINDOW_POLICY_END_TIMESTAMP, currentTimeMillis + ((installWindowEndTime - currentTimeInMins) * 60000));
            UpdaterUtils.setAlarm(j);
        } else if (currentTimeInMins > installWindowStartTime) {
            int i2 = 1440 - currentTimeInMins;
            int i3 = installWindowStartTime + i2;
            Logger.debug("OtaApp", "UpdaterUtils.handleSystemUpdatePolicy, next maintenance window will be " + i3 + " mins from now");
            long currentTimeMillis2 = System.currentTimeMillis() + (i3 * 60000);
            botaSettings.setLong(Configs.WINDOW_POLICY_START_TIMESTAMP, currentTimeMillis2);
            botaSettings.setLong(Configs.WINDOW_POLICY_END_TIMESTAMP, currentTimeMillis + ((i2 + installWindowEndTime) * 60000));
            UpdaterUtils.setAlarm(currentTimeMillis2);
        }
    }

    public boolean isSystemUpdatePolicyEnabled(String str, BotaSettings botaSettings) {
        int policyType = getPolicyType();
        return (!(policyType == 2 || policyType == 3) || System.currentTimeMillis() < botaSettings.getLong(Configs.SYSTEM_UPDATE_POLICY_WINDOWED, -1L)) && policyType > 0;
    }

    public boolean shouldIBlockUpdateForSystemPolicy(ApplicationEnv.Database.Descriptor descriptor, BotaSettings botaSettings) {
        int policyType = getPolicyType();
        int currentTimeInMins = getCurrentTimeInMins();
        if (policyType == 2) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis >= botaSettings.getLong(Configs.SYSTEM_UPDATE_POLICY_WINDOWED, -1L)) {
                return false;
            }
            long j = botaSettings.getLong(Configs.WINDOW_POLICY_START_TIMESTAMP, -1L);
            long j2 = botaSettings.getLong(Configs.WINDOW_POLICY_END_TIMESTAMP, -1L);
            if (j2 != -1 && currentTimeMillis >= j && currentTimeMillis <= j2) {
                return false;
            }
            if (currentTimeInMins < getInstallWindowStartTime() || currentTimeInMins > getInstallWindowEndTime()) {
                scheduleWindowUpdate();
                return true;
            }
        }
        if (policyType != 3 || System.currentTimeMillis() >= botaSettings.getLong(Configs.SYSTEM_UPDATE_POLICY_POSTPONE, -1L)) {
            return false;
        }
        UpdaterUtils.setAlarm(botaSettings.getLong(Configs.SYSTEM_UPDATE_POLICY_POSTPONE, -1L) - System.currentTimeMillis());
        return true;
    }

    public boolean shouldICancelOngoingOtaUpdate(ApplicationEnv.Database.Descriptor descriptor, BotaSettings botaSettings) {
        return shouldIBlockUpdateForSystemPolicy(descriptor, botaSettings) || isOtaUpdateDisabledByPolicyMngr();
    }

    public boolean isAutoDownloadOverAnyDataNetworkPolicySet() {
        Context globalContext = OtaApplication.getGlobalContext();
        if (ThinkShieldUtilConstants.ASC_VERSION_V3.equals(ThinkShieldUtils.getAscVersion(globalContext))) {
            return ThinkShieldUtils.isAutoDownloadOverAnyDataNetworkPolicySet(globalContext);
        }
        try {
            Class<?> cls = Class.forName("com.motorola.android.enterprise.MotoExtEnterpriseManager");
            return ((Boolean) cls.getDeclaredMethod("isFotaAutoUpdateOverAnyDataNetworkEnabled", new Class[0]).invoke(cls.getConstructor(Context.class).newInstance(globalContext), new Object[0])).booleanValue();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Log.e(Logger.THINK_SHIELD_TAG, "Exception while fetching auto download policy: exce msg=" + e);
            return false;
        }
    }

    public boolean isOtaUpdateDisabledByPolicyMngr() {
        return isOtaUpdateDisabledPolicySet() || isDeviceUnderFreezePeriod();
    }

    public boolean isOtaUpdateDisabledPolicySet() {
        Context globalContext = OtaApplication.getGlobalContext();
        if (ThinkShieldUtils.isAscDevice(globalContext)) {
            return ThinkShieldUtilConstants.BLOCK_LIST.equals(ThinkShieldUtils.getCampaignType(globalContext));
        }
        try {
            Class<?> cls = Class.forName("com.motorola.android.enterprise.MotoExtEnterpriseManager");
            boolean booleanValue = ((Boolean) cls.getDeclaredMethod("isOtaUpdateDisabled", new Class[0]).invoke(cls.getConstructor(Context.class).newInstance(globalContext), new Object[0])).booleanValue();
            Logger.debug(Logger.THINK_SHIELD_TAG, "isOtaUpdateDisabledPolicySet = " + booleanValue);
            return booleanValue;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Log.e(Logger.THINK_SHIELD_TAG, "Exception while fetching ota update disabled by policy manager: exce msg=" + e);
            return false;
        }
    }

    public boolean isDeviceUnderFreezePeriod() {
        List<FreezePeriod> freezePeriods = getFreezePeriods();
        if (freezePeriods != null && freezePeriods.size() != 0) {
            for (FreezePeriod freezePeriod : freezePeriods) {
                LocalDate now = LocalDate.now();
                MonthDay start = freezePeriod.getStart();
                MonthDay end = freezePeriod.getEnd();
                int monthValue = start.getMonthValue();
                int monthValue2 = end.getMonthValue();
                int dayOfMonth = start.getDayOfMonth();
                int dayOfMonth2 = end.getDayOfMonth();
                if (monthValue == 2 && dayOfMonth == 29) {
                    dayOfMonth--;
                }
                if (monthValue2 == 2 && dayOfMonth2 == 29) {
                    dayOfMonth2--;
                }
                LocalDate of = LocalDate.of(now.getYear(), monthValue, dayOfMonth);
                LocalDate of2 = LocalDate.of(now.getYear(), monthValue2, dayOfMonth2);
                if (of.getDayOfYear() <= of2.getDayOfYear() && now.getDayOfYear() >= of.getDayOfYear() && now.getDayOfYear() <= of2.getDayOfYear()) {
                    Logger.debug("OtaApp", "Device is under freeze period, so update is blocked");
                    return true;
                }
            }
        }
        return false;
    }

    public List<FreezePeriod> getFreezePeriods() {
        Context globalContext = OtaApplication.getGlobalContext();
        if (ThinkShieldUtils.isAscDevice(globalContext)) {
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList();
        SystemUpdatePolicy systemUpdatePolicy = ((DevicePolicyManager) globalContext.getSystemService("device_policy")).getSystemUpdatePolicy();
        return systemUpdatePolicy != null ? systemUpdatePolicy.getFreezePeriods() : arrayList;
    }

    public void cancelAlarm(Context context) {
        ((AlarmManager) context.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(context, 1, new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE), 335544320));
    }

    private int getInstallWindowStartTime() {
        Context globalContext = OtaApplication.getGlobalContext();
        if (ThinkShieldUtilConstants.ASC_VERSION_V3.equals(ThinkShieldUtils.getAscVersion(globalContext))) {
            return ThinkShieldUtils.getInstallWindowStartTime(globalContext);
        }
        try {
            SystemUpdatePolicy systemUpdatePolicy = ((DevicePolicyManager) globalContext.getSystemService("device_policy")).getSystemUpdatePolicy();
            if (systemUpdatePolicy != null) {
                return systemUpdatePolicy.getInstallWindowStart();
            }
            return -1;
        } catch (Exception unused) {
            return -1;
        }
    }

    private int getInstallWindowEndTime() {
        int i;
        int i2;
        Context globalContext = OtaApplication.getGlobalContext();
        if (ThinkShieldUtilConstants.ASC_VERSION_V3.equals(ThinkShieldUtils.getAscVersion(globalContext))) {
            i = ThinkShieldUtils.getInstallWindowStartTime(globalContext);
            i2 = ThinkShieldUtils.getInstallWindowEndTime(globalContext);
        } else {
            try {
                SystemUpdatePolicy systemUpdatePolicy = ((DevicePolicyManager) globalContext.getSystemService("device_policy")).getSystemUpdatePolicy();
                if (systemUpdatePolicy != null) {
                    i = systemUpdatePolicy.getInstallWindowStart();
                    try {
                        i2 = systemUpdatePolicy.getInstallWindowEnd();
                    } catch (Exception unused) {
                        i2 = -1;
                        return (i != -1 || i2 == -1 || i < i2) ? i2 : i2 + UpdaterUtils.DEFAULT_DEFER_TIME;
                    }
                } else {
                    i2 = -1;
                    i = -1;
                }
            } catch (Exception unused2) {
                i = -1;
            }
        }
        return (i != -1 || i2 == -1 || i < i2) ? i2 : i2 + UpdaterUtils.DEFAULT_DEFER_TIME;
    }
}
