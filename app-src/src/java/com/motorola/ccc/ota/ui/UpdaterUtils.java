package com.motorola.ccc.ota.ui;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.icu.text.MeasureFormat;
import android.icu.util.Calendar;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.google.android.setupcompat.util.WizardManagerHelper;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.AndroidPollingManager;
import com.motorola.ccc.ota.env.CheckUpdateWorker;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.OtaService;
import com.motorola.ccc.ota.env.OtaSystemServerBindService;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.HistoryDbHandler;
import com.motorola.ccc.ota.utils.IntelligentNotificationTime;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.PMUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpdaterUtils {
    public static final String ACTION_MANUAL_CHECK_UPDATE = "com.motorola.blur.service.blur.Actions.MANUAL_CHECK_UPDATE";
    public static final String ACTION_MAX_FOTA_EXPIRY_TIME = "com.motorola.ccc.ota.Action.ACTION_MAX_FOTA_EXPIRY_TIME";
    public static final String ACTION_USER_CANCELLED_BACKGROUND_INSTALL = "com.motorola.ccc.ota.Actions.USER_CANCEL_BACKGROUND_INSTALL";
    public static final String ACTION_USER_CANCELLED_DOWNLOAD = "com.motorola.ccc.ota.Actions.USER_CANCEL_DOWNLOAD";
    public static final String ACTION_USER_DEFERERD_WIFI_SETUP = "com.motorola.ccc.ota.Action.USER_DEFERERD_WIFI_SETUP";
    public static final String ACTION_USER_RESUME_DOWNLOAD_ON_CELLULAR = "com.motorola.ccc.ota.Actions.USER_RESUME_DOWNLOAD_ON_CELLULAR";
    public static final String ACTION_USER_RESUME_STREAMING_DOWNLOAD_ON_CELLULAR = "com.motorola.ccc.ota.Actions.USER_RESUME_STREAMING_DOWNLOAD_ON_CELLULAR";
    public static final String ALLOW_OTA_ON_ROAMING = "ro.ota_on_roaming";
    public static final String BEST_TIME_1_HOUR = "BestTime1Hour";
    public static final String BEST_TIME_1_MINUTE = "BestTime1Minute";
    public static final String BEST_TIME_2_HOUR = "BestTime2Hour";
    public static final String BEST_TIME_2_MINUTE = "BestTime2Minute";
    public static final String BOOTSTRAP = "bootstrap";
    public static final int CRITICAL_UPDATE_EXTEND_RESTART_IN_MINS = 60;
    public static final String Checkbox_selected = "checkbox_selected";
    public static final int DEFAULT_BATTERY_LEVEL = 20;
    public static final int DEFAULT_CRITICAL_ALARM_ANNOY_VALUE = 10;
    public static final int DEFAULT_DEFER_TIME = 1440;
    public static final int DEFAULT_INSTALL_MAX_HOUR_FOR_SMART_UPDATE = 3;
    public static final int DEFAULT_INSTALL_MINUTES_FOR_SMART_UPDATE = 0;
    public static final int DEFAULT_INSTALL_MIN_HOUR_FOR_SMART_UPDATE = 1;
    private static final int DEFAULT_OPTIONAL_DEFER_COUNT_VZW = 2;
    public static final int DEFAULT_UPDATE_TIME = 10;
    public static final String DEFER_TIME_IN_MIN = "deferTimeInMin";
    public static final String DOWNLOAD_MODE_STATS = "downloadModeStats";
    public static final String Delay = "delay";
    public static final int FINAL_DEFER_TIME_FOR_SMR = 259200;
    public static final String FOTA = "fota";
    public static final String HOUR = "hour";
    public static final String INSTALL_AUTOMATICALLY = "installAutomatically";
    public static final String INSTALL_MODE_STATS = "installModeStats";
    public static final String KEY_ACTIVITY_INTENT = "activityIntent";
    public static final String KEY_BATTERY_LOW = "BatteryLow";
    public static final String KEY_CHECK_FOR_MAP = "CheckForMap";
    public static final String KEY_CHK_BODY = "CheckBody";
    public static final String KEY_CHK_TITLE = "CheckTitle";
    public static final String KEY_ERR_MSG = "ErrorMessage";
    public static final String KEY_ERR_TITLE = "ErrorTitle";
    public static final String KEY_INTELLIGENT_NOTIFICATION = "IntelligentNotificationTime";
    public static final String KEY_MERGE_FAILURE = "MergeStatusFailure";
    private static final int MAX_BATTERY_LEVEL = 100;
    private static final int MAX_UPDATE_CANCEL_REMIDER_DAYS = 45;
    public static final int MAX_UPDATE_TIME = 60;
    public static final String MINUTE = "minute";
    private static final int MIN_UPDATE_CANCEL_REMIDER_DAYS = 1;
    private static final String MOTO_SETTINGS_PROVIDER_CLASS_NAME = "com.motorola.android.provider.MotorolaSettings$Global";
    public static final String OEM_CONFIG_UPDATE_FLAG = "oem_config_update";
    public static final String OTA_UPDATE_COMPLETED = "ota_update_completed";
    public static final String PROPERTY_INECM_MODE = "ril.cdma.inecmmode";
    public static final String REBOOT_MODE_STATS = "rebootModeStats";
    public static final String SDCARD = "sdcard";
    private static final String SYSTEM_PROPERTIES_CLASS_NAME = "android.os.SystemProperties";
    private static final String SYSTEM_PROPERTIES_GET_METHOD_NAME = "get";
    public static final String TIME_STRING = "TimeString";
    public static final String UPGRADE = "upgrade";
    public static final long WAIT_TIME_AFTER_FORCE_INSTALL_TIMER = 864000000;
    public static final String WHOM = "whom";
    private static final BotaSettings settings = new BotaSettings();
    private static BatteryStatusChangeReceiver batteryChangeReceiver = null;
    public static boolean sDeviceIdleModeRequired = true;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum BitmapFeatures {
        bootloader,
        rooted,
        intelligentNotification,
        swipableNotification,
        softBankProxy,
        enableVABMergeFeature,
        enableResumeOnReboot
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum InternalUpdateType {
        CRITICAL_UPDATE,
        FORCE_UPDATE,
        SMART_UPDATE,
        DEFAULT_UPDATE
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public interface OnDialogInteractionListener {
        void onDismiss(int i, boolean z);

        default void onNegativeClick(int i) {
        }

        void onPositiveClick(int i, JSONObject jSONObject);
    }

    public static boolean isBitMapSet(int i, int i2) {
        return i >= 0 && ((byte) ((i >> i2) & 1)) == 1;
    }

    public static boolean optionalUpdateDeferCountExpired(int i, int i2) {
        return i != -1 && i2 >= i;
    }

    public static int getUpdateTime(int i) {
        Logger.debug("OtaApp", "Updatetime from metadata: " + i);
        return (i <= 0 || i > 60) ? i > 60 ? 60 : 10 : i;
    }

    public static boolean isMaxUpdateFailCountExpired(int i) {
        if (BuildPropReader.isATT()) {
            return false;
        }
        int i2 = settings.getInt(Configs.UPDATE_FAIL_COUNT, 0);
        Logger.debug("OtaApp", "UpdaterUtils.isMaxUpdateFailCountExpired, otaFailedAttempts = " + i2 + " metaDataValue = " + i);
        return i > 0 && i2 >= i;
    }

    public static int getminBatteryRequiredForInstall(int i) {
        int i2 = settings.getInt(Configs.DEFAULT_MIN_BATTERY_LEVEL, 20);
        if (i2 > i) {
            i = i2;
        }
        if (i > 100) {
            return 20;
        }
        return i;
    }

    public static boolean bypassPreDownloadDialog(boolean z) {
        return !z ? settings.getBoolean(Configs.BYPASS_PREDOWNLOAD) : z;
    }

    public static void notifyRecoveryAboutPendingUpdate(boolean z) {
        Logger.debug("OtaApp", "UpdaterUtils.notifyRecoveryAboutPendingUpdate " + z);
        try {
            Method declaredMethod = Class.forName("android.os.RecoverySystem").getDeclaredMethod("notifyPendingInstallation", Context.class, File.class);
            File file = new File(FileUtils.getLocalPath(settings));
            if (z) {
                declaredMethod.invoke(null, OtaApplication.getGlobalContext(), file);
            } else {
                declaredMethod.invoke(null, OtaApplication.getGlobalContext(), null);
            }
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Logger.error("OtaApp", "Exception in UpdaterUtils.notifyRecoveryAboutPendingUpdate: " + e);
        }
    }

    public static boolean isROREnabled(ApplicationEnv.Database.Descriptor descriptor) {
        if (BuildPropertyUtils.isChinaDevice(OtaApplication.getGlobalContext()) || BuildPropReader.isATT()) {
            Logger.debug("OtaApp", "FOTA/China device, ROR feature is off by default");
            return false;
        } else if (UpgradeSourceType.sdcard.toString().equals(descriptor.getRepository())) {
            Logger.debug("OtaApp", "Sdcard, ROR feature is on by default");
            return true;
        } else if (isBitMapSet(descriptor.getMeta().getBitmap(), BitmapFeatures.enableResumeOnReboot.ordinal())) {
            Logger.debug("OtaApp", "Resume on reboot feature is on");
            return true;
        } else {
            return false;
        }
    }

    public static void prepareForUnattendedUpdate() {
        Logger.debug("OtaApp", "UpdaterUtils.prepareForUnattendedUpdate ");
        try {
            Class.forName("android.os.RecoverySystem").getDeclaredMethod("prepareForUnattendedUpdate", Context.class, String.class, IntentSender.class).invoke(null, OtaApplication.getGlobalContext(), "", null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Logger.error("OtaApp", "Exception in UpdaterUtils.prepareForUnattendedUpdate: " + e);
        }
    }

    public static boolean isPreparedForUnattendedUpdate() {
        boolean z = false;
        try {
            z = ((Boolean) Class.forName("android.os.RecoverySystem").getDeclaredMethod("isPreparedForUnattendedUpdate", Context.class).invoke(null, OtaApplication.getGlobalContext())).booleanValue();
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | NullPointerException | InvocationTargetException e) {
            Logger.debug("OtaApp", "Exception in UpdaterUtils.isPreparedForUnattendedUpdate: " + e);
        }
        Logger.error("OtaApp", "UpdaterUtils.isPreparedForUnattendedUpdate: " + z);
        return z;
    }

    public static void clearPrepareForUnattendedUpdate() {
        Logger.debug("OtaApp", "UpdaterUtils.clearPrepareForUnattendedUpdate");
        try {
            Class.forName("android.os.RecoverySystem").getDeclaredMethod("clearPrepareForUnattendedUpdate", Context.class).invoke(null, OtaApplication.getGlobalContext());
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Logger.error("OtaApp", "Exception in UpdaterUtils.clearPrepareForUnattendedUpdate: " + e);
        }
    }

    public static void rebootAndApply() {
        Logger.debug("OtaApp", "UpdaterUtils.rebootAndApply");
        try {
            Class.forName("android.os.RecoverySystem").getDeclaredMethod("rebootAndApply", Context.class, String.class, String.class).invoke(null, OtaApplication.getGlobalContext(), "", "");
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Logger.error("OtaApp", "Exception in UpdaterUtils.rebootAndApply: " + e);
            ((PowerManager) OtaApplication.getGlobalContext().getSystemService("power")).reboot(null);
        }
    }

    public static float getMandatoryInstallDaysFromMetaData(String str) {
        if (str != null) {
            try {
                return Float.parseFloat(str.replaceAll("\\s+", "").split(",")[0].replace("d", ""));
            } catch (NumberFormatException e) {
                Logger.error("OtaApp", "UpdaterUtils.getMandatoryInstallDaysFromMetaData : " + e);
                return -1.0f;
            }
        }
        return -1.0f;
    }

    public static int getInstallReminderMins(String str) {
        if (str != null) {
            try {
                return Integer.parseInt(str.replaceAll("\\s+", "").split(",")[1].replace("m", ""));
            } catch (NumberFormatException e) {
                Logger.error("OtaApp", "UpdaterUtils.getInstallReminderMins : " + e);
                return -1;
            }
        }
        return -1;
    }

    public static void setListViewHeight(ExpandableListView expandableListView) {
        ExpandableListAdapter expandableListAdapter = expandableListView.getExpandableListAdapter();
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(expandableListView.getWidth(), 1073741824);
        int i = 0;
        for (int i2 = 0; i2 < expandableListAdapter.getGroupCount(); i2++) {
            View groupView = expandableListAdapter.getGroupView(i2, true, null, expandableListView);
            groupView.measure(makeMeasureSpec, 0);
            i += groupView.getMeasuredHeight();
            if (expandableListView.isGroupExpanded(i2)) {
                int i3 = i;
                for (int i4 = 0; i4 < expandableListAdapter.getChildrenCount(i2); i4++) {
                    View childView = expandableListAdapter.getChildView(i2, i4, true, null, expandableListView);
                    childView.measure(makeMeasureSpec, 0);
                    i3 += childView.getMeasuredHeight();
                }
                i = i3;
            }
        }
        ViewGroup.LayoutParams layoutParams = expandableListView.getLayoutParams();
        layoutParams.height = i;
        expandableListView.setLayoutParams(layoutParams);
        expandableListView.requestLayout();
    }

    public static ExpandableListView handleExpList(final ExpandableListView expandableListView, final String str) {
        if (expandableListView != null) {
            expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() { // from class: com.motorola.ccc.ota.ui.UpdaterUtils.1
                private int lastExpandedPosition = -1;
                private boolean firstgroup = true;

                @Override // android.widget.ExpandableListView.OnGroupExpandListener
                public void onGroupExpand(int i) {
                    if (this.firstgroup) {
                        expandableListView.collapseGroup(0);
                        this.firstgroup = false;
                    } else {
                        int i2 = this.lastExpandedPosition;
                        if (i2 != -1 && i != i2) {
                            expandableListView.collapseGroup(i2);
                        }
                    }
                    this.lastExpandedPosition = i;
                    if (TextUtils.isEmpty(UpdaterUtils.settings.getString(Configs.STATS_EXPANDABLE_LIST_CLICK_SCREEN)) && !"history".equals(str)) {
                        UpdaterUtils.settings.setString(Configs.STATS_EXPANDABLE_LIST_CLICK_SCREEN, str);
                    }
                    UpdaterUtils.setListViewHeight(expandableListView);
                }
            });
            expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() { // from class: com.motorola.ccc.ota.ui.UpdaterUtils.2
                @Override // android.widget.ExpandableListView.OnGroupCollapseListener
                public void onGroupCollapse(int i) {
                    UpdaterUtils.setListViewHeight(expandableListView);
                }
            });
        }
        return expandableListView;
    }

    public static ExpandableListView setExpandableList(final ExpandableListView expandableListView, boolean z, String str, TextView textView, final Context context, UpdateType.UpdateTypeInterface updateTypeInterface, View view) {
        JSONObject jSONObject;
        String substring;
        LinkedHashMap<String, List<String>> expandableListData;
        if (z) {
            try {
                jSONObject = new JSONObject(str);
            } catch (JSONException unused) {
                Logger.error("OtaApp", "Release notes is not in json format proceed with html format");
                jSONObject = null;
            }
            try {
                if (jSONObject != null) {
                    Resources resources = context.getResources();
                    substring = resources.getString(R.string.release_note_title);
                    expandableListData = getExpandableListData(resources, jSONObject);
                } else {
                    substring = str.substring(str.indexOf("<title>") + 7, str.indexOf("</title>"));
                    expandableListData = getExpandableListData(str.substring(str.indexOf("<body>"), str.indexOf("</body>") + 7));
                }
                textView.setText(substring);
                textView.setVisibility(0);
                view.setVisibility(0);
                ArrayList arrayList = new ArrayList(expandableListData.keySet());
                if (arrayList.isEmpty()) {
                    textView.setVisibility(8);
                    view.setVisibility(8);
                }
                expandableListView.setAdapter(new ReleaseNotesAdapter(context, arrayList, expandableListData) { // from class: com.motorola.ccc.ota.ui.UpdaterUtils.3
                    @Override // com.motorola.ccc.ota.ui.ReleaseNotesAdapter, android.widget.ExpandableListAdapter
                    public View getGroupView(int i, boolean z2, View view2, ViewGroup viewGroup) {
                        View groupView = super.getGroupView(i, z2, view2, viewGroup);
                        ((TextView) groupView.findViewById(R.id.listTitle)).setTextColor(context.getColor(R.color.black));
                        return groupView;
                    }
                });
                expandableListView.setVerticalScrollBarEnabled(false);
                expandableListView.expandGroup(0);
                expandableListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.motorola.ccc.ota.ui.UpdaterUtils.4
                    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                    public void onGlobalLayout() {
                        expandableListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        UpdaterUtils.setListViewHeight(expandableListView);
                    }
                });
                return expandableListView;
            } catch (Exception e) {
                Logger.error("OtaApp", "UpdaterUtils, error setting ReleaseNotes." + e);
            }
        }
        return expandableListView;
    }

    public static boolean isInstallMinutesValid() {
        try {
            int installReminderMins = getInstallReminderMins(new JSONObject(settings.getString(Configs.METADATA)).optString("installReminder", ""));
            if (installReminderMins > 0) {
                Logger.debug("OtaApp", "UpdaterUtils.installReminderMinutes = " + installReminderMins);
                return true;
            }
            return false;
        } catch (Exception e) {
            Logger.error("OtaApp", "UpdaterUtils.isInstallMinutesValid Exception = " + e);
            return false;
        }
    }

    public static boolean isMandatoryInstallTimeExpired() {
        long currentTimeMillis = System.currentTimeMillis();
        long mandatoryInstallTime = getMandatoryInstallTime();
        return mandatoryInstallTime > 0 && currentTimeMillis >= mandatoryInstallTime && isInstallMinutesValid();
    }

    public static void setMandatoryInstallDays(String str) {
        float mandatoryInstallDaysFromMetaData = getMandatoryInstallDaysFromMetaData(str);
        Logger.debug("OtaApp", "UpdaterUtils.setMandatoryInstallDays = " + mandatoryInstallDaysFromMetaData);
        if (0.0f <= mandatoryInstallDaysFromMetaData) {
            settings.setString(Configs.MANDATORY_INSTALL_TIME, String.valueOf(System.currentTimeMillis() + (mandatoryInstallDaysFromMetaData * 24.0f * 60.0f * 60.0f * 1000.0f)));
        }
    }

    public static long getMandatoryInstallTime() {
        try {
            StringBuilder sb = new StringBuilder("UpdaterUtils.getMandatoryInstallTime = ");
            BotaSettings botaSettings = settings;
            Logger.debug("OtaApp", sb.append(Long.parseLong(botaSettings.getString(Configs.MANDATORY_INSTALL_TIME))).toString());
            return Long.parseLong(botaSettings.getString(Configs.MANDATORY_INSTALL_TIME));
        } catch (NumberFormatException unused) {
            return -1L;
        }
    }

    public static boolean isWaitForDozeModeOver() {
        BotaSettings botaSettings = settings;
        return botaSettings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L) > -1 && System.currentTimeMillis() - botaSettings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L) >= WAIT_TIME_AFTER_FORCE_INSTALL_TIMER;
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    static class UpdaterWakeLock {
        private static PowerManager.WakeLock sWakeLock;

        UpdaterWakeLock() {
        }

        static void acquire(Context context) {
            PowerManager.WakeLock wakeLock = sWakeLock;
            if (wakeLock != null) {
                wakeLock.release();
            }
            PowerManager.WakeLock newWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(805306394, "OtaApp");
            sWakeLock = newWakeLock;
            newWakeLock.acquire();
        }

        static void acquire(Context context, long j) {
            PowerManager.WakeLock wakeLock = sWakeLock;
            if (wakeLock != null && wakeLock.isHeld()) {
                sWakeLock.release();
            }
            PowerManager.WakeLock newWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "OtaApp");
            sWakeLock = newWakeLock;
            newWakeLock.acquire(j);
        }

        static void release() {
            PowerManager.WakeLock wakeLock = sWakeLock;
            if (wakeLock == null || !wakeLock.isHeld()) {
                return;
            }
            sWakeLock.release();
            sWakeLock = null;
        }
    }

    public static UpgradeInfo getUpgradeInfoDuringOTAUpdate(Intent intent) {
        String string = new BotaSettings().getString(Configs.METADATA);
        try {
            return new UpgradeInfo(new JSONObject(string), UpgradeUtilMethods.locationTypeFromIntent(intent));
        } catch (Exception e) {
            Logger.error("OtaApp", "Invalid json metadata, checking for gpb" + e);
            return null;
        }
    }

    public static UpgradeInfo getUpgradeInfoAfterOTAUpdate(Intent intent) {
        try {
            return new UpgradeInfo(new JSONObject(intent.getStringExtra(UpgradeUtilConstants.KEY_METADATA)), UpgradeUtilMethods.locationTypeFromIntent(intent));
        } catch (Exception e) {
            Logger.error("OtaApp", "Invalid json metadata, checking for gpb" + e);
            return null;
        }
    }

    public static String getUpdateType(String str) {
        if (str != null) {
            try {
                return new JSONObject(str).getString(UpgradeUtilConstants.KEY_HISTORY_UPDATE_TYPE);
            } catch (JSONException unused) {
                Logger.error("OtaApp", "Invalid JSON object, problem in parsing metadata or problem in getting updateType");
                return "DEFAULT";
            }
        }
        return "DEFAULT";
    }

    public static boolean isPromptAllowed(Context context, String str) {
        if (isInActiveCall(context)) {
            Logger.info("OtaApp", "Phone in Calling state.");
            if (str.equals(NotificationUtils.KEY_INSTALL) || str.equals(NotificationUtils.KEY_RESTART) || str.equals(NotificationUtils.KEY_MERGE_RESTART)) {
                checkAndEnableCallStateChangeReceiver();
            }
            return false;
        } else if (getSystemProperty(PROPERTY_INECM_MODE)) {
            Logger.info("OtaApp", "Phone in ECB mode.");
            return false;
        } else if (getSystemProperty(ALLOW_OTA_ON_ROAMING) || settings.getBoolean(Configs.ALLOW_ON_ROAMING) || !isDataNetworkRoaming(context) || isWifiConnected(context) || str.equals(NotificationUtils.KEY_RESTART) || str.equals(NotificationUtils.KEY_MERGE_RESTART)) {
            return true;
        } else {
            Logger.info("OtaApp", "Phone in Roaming mode.");
            return false;
        }
    }

    public static void priorityAppRunningPostponeActivity(final Context context, final String str, final Intent intent, final boolean z) {
        OtaApplication.getExecutorService().submit(new Runnable() { // from class: com.motorola.ccc.ota.ui.UpdaterUtils.5
            @Override // java.lang.Runnable
            public void run() {
                UpdaterUtils.settings.removeConfig(Configs.NOTIFICATION_TAPPED);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
                intent.putExtra(UpgradeUtilConstants.KEY_METADATA, UpdaterUtils.settings.getString(Configs.METADATA));
                UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent);
                intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, upgradeInfoDuringOTAUpdate.getLocationType());
                intent.putExtra(UpgradeUtilConstants.KEY_VERSION, BuildPropReader.getDeviceSha1(UpgradeSourceType.upgrade.toString()));
                PendingIntent broadcast = PendingIntent.getBroadcast(context, 0, UpdaterUtils.fillAnnoyValueExpiryDetails(str, intent), 335544320);
                alarmManager.cancel(broadcast);
                long currentTimeMillis = CusAndroidUtils.URL_EXPIRY_TIME + System.currentTimeMillis();
                UpdaterUtils.settings.setLong(Configs.ACTIVITY_NEXT_PROMPT, currentTimeMillis);
                Logger.debug("OtaApp", "Priority app(Maps/Camera) is running, showing full screen is postponed to next prompt : " + currentTimeMillis);
                if (UpgradeUtilConstants.ACTIVITY_ANNOY_VALUE_EXPIRY.equals(intent.getAction()) || z || SmartUpdateUtils.isSmartUpdateNearestToInstall(UpdaterUtils.settings, upgradeInfoDuringOTAUpdate.getUpdateTypeData()) || (NotificationUtils.KEY_MERGE_RESTART.equals(str) && !NotificationUtils.KEY_DOWNLOAD.equals(str))) {
                    alarmManager.setExactAndAllowWhileIdle(0, currentTimeMillis, broadcast);
                } else if (NotificationUtils.isNotificationServiceRunning(context)) {
                } else {
                    NotificationUtils.displayNotification(context, str, currentTimeMillis, intent, upgradeInfoDuringOTAUpdate);
                }
            }
        });
    }

    public static boolean isInActiveCall(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        return (telephonyManager == null || telephonyManager.getCallState() == 0) ? false : true;
    }

    public static boolean isDeviceAtInstallPhase(Intent intent) {
        UpgradeInfo upgradeInfoDuringOTAUpdate = getUpgradeInfoDuringOTAUpdate(intent);
        return upgradeInfoDuringOTAUpdate != null && upgradeInfoDuringOTAUpdate.getSize() == downloadFileSize();
    }

    public static boolean getSystemProperty(String str) {
        try {
            String obj = getMethodObjectForGetSystemProperty().invoke(null, str).toString();
            if ("true".equalsIgnoreCase(obj) || "false".equalsIgnoreCase(obj)) {
                return Boolean.valueOf(obj).booleanValue();
            }
            return false;
        } catch (Exception e) {
            Logger.error("OtaApp", "failed to read property value from system properties " + e);
            return false;
        }
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static final class UpgradeInfo {
        private String mAnnoy;
        private String mLocationType;
        private JSONObject mMetadataMMApi;
        private int[] mMinutes;

        public UpgradeInfo() {
        }

        public UpgradeInfo(JSONObject jSONObject, String str) {
            this.mMetadataMMApi = jSONObject;
            this.mLocationType = str;
            try {
                setAnnoy(jSONObject.getString("annoy"));
            } catch (JSONException unused) {
                setAnnoy("1440");
            }
        }

        public String getVersion() {
            try {
                return this.mMetadataMMApi.getString("version");
            } catch (JSONException unused) {
                return null;
            }
        }

        public long getSize() {
            try {
                return this.mMetadataMMApi.getLong("size");
            } catch (JSONException unused) {
                return -1L;
            }
        }

        public int getInstallTime() {
            try {
                return this.mMetadataMMApi.getInt("installTime");
            } catch (JSONException unused) {
                return -1;
            }
        }

        public long getExtraSize() {
            try {
                return this.mMetadataMMApi.getLong("extraSpace");
            } catch (JSONException unused) {
                return -1L;
            }
        }

        public boolean isForced() {
            SystemUpdaterPolicy systemUpdaterPolicy = new SystemUpdaterPolicy();
            if (systemUpdaterPolicy.getPolicyType() > 0 || systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet()) {
                return true;
            }
            try {
                return this.mMetadataMMApi.getBoolean("forced");
            } catch (JSONException unused) {
                return false;
            }
        }

        public boolean hasUpgradeNotification() {
            return getUpgradeNotification() != null && getUpgradeNotification().length() > 0;
        }

        public String getUpgradeNotification() {
            try {
                return this.mMetadataMMApi.getString("upgradeNotification");
            } catch (JSONException unused) {
                return null;
            }
        }

        public boolean hasReleaseNotes() {
            return getReleaseNotes() != null && getReleaseNotes().length() > 0;
        }

        public String getReleaseNotes() {
            try {
                return this.mMetadataMMApi.getString(UpgradeUtilConstants.KEY_HISTORY_RELEASE_NOTES);
            } catch (JSONException unused) {
                return null;
            }
        }

        public boolean hasPreInstallNotes() {
            return getPreInstallNotes() != null && getPreInstallNotes().length() > 0;
        }

        public String getPreInstallNotes() {
            try {
                return this.mMetadataMMApi.getString("preInstallNotes");
            } catch (JSONException unused) {
                return null;
            }
        }

        public String getOSreleaseLink() {
            try {
                return this.mMetadataMMApi.getString("OSreleaseLink");
            } catch (JSONException unused) {
                return "";
            }
        }

        public String getTargetOSVersion() {
            try {
                return this.mMetadataMMApi.getString("targetOSVersion");
            } catch (JSONException unused) {
                return "";
            }
        }

        public boolean hasPostInstallNotes() {
            return getPostInstallNotes() != null && getPostInstallNotes().length() > 0;
        }

        public String getPostInstallNotes() {
            try {
                return this.mMetadataMMApi.getString("postInstallNotes");
            } catch (JSONException unused) {
                return null;
            }
        }

        public boolean hasPreDownloadInstructions() {
            return getPreDownloadInstructions() != null && getPreDownloadInstructions().length() > 0;
        }

        public String getPreDownloadInstructions() {
            try {
                return this.mMetadataMMApi.getString("preDownloadInstructions");
            } catch (JSONException unused) {
                return null;
            }
        }

        public boolean hasPreInstallInstructions() {
            return getPreInstallInstructions() != null && getPreInstallInstructions().length() > 0;
        }

        public String getPreInstallInstructions() {
            try {
                return this.mMetadataMMApi.getString("preInstallInstructions");
            } catch (JSONException unused) {
                return null;
            }
        }

        public boolean hasPostInstallFailureMessage() {
            return getPostInstallFailureMessage() != null && getPostInstallFailureMessage().length() > 0;
        }

        public String getPostInstallFailureMessage() {
            try {
                return this.mMetadataMMApi.getString("postInstallFailureMessage");
            } catch (JSONException unused) {
                return null;
            }
        }

        public boolean isWifiOnly() {
            try {
                if (new SystemUpdaterPolicy().isAutoDownloadOverAnyDataNetworkPolicySet()) {
                    Logger.debug("OtaApp", "UpdaterUtils:allowFotaOverAnyDataNetwork policy set, so return isWifiOnly false irrespective of wifi only pkg");
                    return false;
                } else if (UpdaterUtils.isForceOnCellular()) {
                    return false;
                } else {
                    return this.mMetadataMMApi.getBoolean("wifionly");
                }
            } catch (JSONException unused) {
                return false;
            }
        }

        public int getOptionalDeferCount() {
            try {
                return this.mMetadataMMApi.getInt("optionalUpdateDeferCount");
            } catch (JSONException unused) {
                return -1;
            }
        }

        public int getCriticalDeferCount() {
            return this.mMetadataMMApi.optInt("criticalUpdateDeferCount", 3);
        }

        public int getminBatteryRequiredForInstall() {
            try {
                return this.mMetadataMMApi.getInt("minBatteryRequiredForInstall");
            } catch (JSONException unused) {
                return -1;
            }
        }

        public boolean getByPassPreDownloadDialog() {
            try {
                return this.mMetadataMMApi.getBoolean("bypassPreDownloadDialog");
            } catch (JSONException unused) {
                return false;
            }
        }

        public boolean showDownloadOptions() {
            if (new SystemUpdaterPolicy().isAutoDownloadOverAnyDataNetworkPolicySet()) {
                return false;
            }
            return this.mMetadataMMApi.optBoolean("showDownloadOptions", false);
        }

        public boolean showPreInstallScreen() {
            return this.mMetadataMMApi.optBoolean("showPreInstallScreen", true);
        }

        public boolean showPostInstallScreen() {
            return this.mMetadataMMApi.optBoolean("showPostInstallScreen", true);
        }

        public double getForceDownloadTime() {
            return this.mMetadataMMApi.optDouble("forceDownloadTime", -1.0d);
        }

        public boolean isForceDownloadTimeSet() {
            return Double.compare(getForceDownloadTime(), -1.0d) > 0;
        }

        public double getForceInstallTime() {
            return this.mMetadataMMApi.optDouble("forceInstallTime", -1.0d);
        }

        public boolean isForceInstallTimeSet() {
            return Double.compare(getForceInstallTime(), -1.0d) > 0;
        }

        public boolean isForceInstallTimerExpired() {
            return isForceInstallTimeSet() && System.currentTimeMillis() >= UpdaterUtils.settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L);
        }

        public int getOptionalUpdateCancelReminderDays() {
            int optInt = this.mMetadataMMApi.optInt("optionalUpdateCancelReminderDays", 45);
            if (optInt < 1) {
                return 1;
            }
            if (optInt > 45) {
                return 45;
            }
            return optInt;
        }

        public boolean getEnterpriseOta() {
            try {
                return this.mMetadataMMApi.getBoolean("enterpriseOta");
            } catch (JSONException unused) {
                return false;
            }
        }

        public int getDelay(int i) {
            if (UpdaterUtils.BOOTSTRAP.equals(this.mLocationType)) {
                return 1;
            }
            int[] iArr = this.mMinutes;
            if (i >= iArr.length) {
                return iArr[iArr.length - 1];
            }
            return iArr[i];
        }

        public boolean isTimeout(int i) {
            return i >= this.mMinutes.length;
        }

        public String getLocationType() {
            return this.mLocationType;
        }

        public boolean isCriticalUpdate() {
            return getSeverity() == UpgradeUtils.SeverityType.CRITICAL.ordinal();
        }

        public boolean isInstallReminderSet() {
            return !TextUtils.isEmpty(this.mMetadataMMApi.optString("installReminder", ""));
        }

        public void setAnnoy(String str) {
            this.mAnnoy = str;
            this.mMinutes = new int[]{UpdaterUtils.DEFAULT_DEFER_TIME};
            if (str != null) {
                if (str.endsWith(",...")) {
                    str = str.substring(0, str.length() - 4);
                }
                try {
                    String[] split = str.split(",");
                    int[] iArr = new int[split.length];
                    for (int i = 0; i < split.length; i++) {
                        int parseInt = Integer.parseInt(split[i]);
                        iArr[i] = parseInt;
                        if (parseInt < 1) {
                            iArr[i] = 1;
                        }
                    }
                    this.mMinutes = iArr;
                } catch (NumberFormatException unused) {
                }
            }
        }

        public String getDisplayVersion() {
            try {
                return this.mMetadataMMApi.getString("displayVersion");
            } catch (JSONException unused) {
                return null;
            }
        }

        public boolean getOemConfigUpdateData() {
            try {
                return this.mMetadataMMApi.getBoolean("oemConfigUpdate");
            } catch (JSONException unused) {
                return false;
            }
        }

        public String getUpdateTypeData() {
            try {
                return this.mMetadataMMApi.getString(UpgradeUtilConstants.KEY_HISTORY_UPDATE_TYPE);
            } catch (JSONException unused) {
                return UpdateType.DIFFUpdateType.DEFAULT.toString();
            }
        }

        public long getChunkSize() {
            return this.mMetadataMMApi.optLong("abMaxChunkSize", 0L);
        }

        public int getSeverity() {
            return this.mMetadataMMApi.optInt("severityType", UpgradeUtils.SeverityType.OPTIONAL.ordinal());
        }

        public int getCriticalUpdateReminder() {
            return this.mMetadataMMApi.optInt("criticalUpdateReminder", UpgradeUtils.DEFAULT_CRITICAL_UPDATE_ANNOY_VALUE);
        }

        public long getCriticalUpdateExtraWaitPeriodInMillis() {
            return this.mMetadataMMApi.optLong("criticalUpdateExtraWaitPeriod", 10L) * 60000;
        }

        public long getCriticalUpdateExtraWaitCount() {
            return this.mMetadataMMApi.optLong("criticalUpdateExtraWaitCount", 6L);
        }

        public long getCompatibilityFileSize() {
            JSONObject optJSONObject;
            JSONObject optJSONObject2;
            try {
                JSONObject jSONObject = this.mMetadataMMApi.getJSONObject("streamingData");
                if (jSONObject == null || (optJSONObject = jSONObject.optJSONObject("additionalInfo")) == null || (optJSONObject2 = optJSONObject.optJSONObject("compatibility")) == null) {
                    return 1048576L;
                }
                return optJSONObject2.getLong("size");
            } catch (JSONException e) {
                Logger.error("OtaApp", "Exception in UpdaterUtils, getCompatibilityFileSize: " + e);
                return 1048576L;
            }
        }

        public int getBitmap() {
            return this.mMetadataMMApi.optInt("featureEnableBitmap", 0);
        }
    }

    private static Method getMethodObjectForGetSystemProperty() throws Exception {
        return Class.forName(SYSTEM_PROPERTIES_CLASS_NAME).getMethod(SYSTEM_PROPERTIES_GET_METHOD_NAME, String.class);
    }

    public static boolean isVerizon() {
        return BuildPropReader.isVerizon();
    }

    public static boolean isWifiConnected(Context context) {
        NetworkCapabilities networkCapabilities;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager == null || (networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork())) == null || !NetworkUtils.hasNetwork(connectivityManager)) {
            return false;
        }
        return networkCapabilities.hasTransport(1) || networkCapabilities.hasTransport(4);
    }

    public static void setDeferStats(Context context, String str, boolean z, long j) {
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_OTA_DEFERRED);
        intent.putExtra(UpgradeUtilConstants.STATS_TYPE, str);
        intent.putExtra(INSTALL_AUTOMATICALLY, z);
        intent.putExtra(DEFER_TIME_IN_MIN, j);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static void sendDownloadModeStats(String str) {
        settings.setString(Configs.STATS_DOWNLOAD_MODE, str);
    }

    public static void sendInstallModeStats(String str) {
        if ("installedViaSmartUpdate".equals(str)) {
            settings.setBoolean(Configs.STATS_INSTALL_VIA_SMART_UPDATE, true);
        } else {
            settings.setBoolean(Configs.STATS_INSTALL_VIA_SMART_UPDATE, false);
        }
        settings.setString(Configs.STATS_INSTALL_MODE, str);
    }

    public static String getStatsType(Intent intent) {
        return intent.getStringExtra(UpgradeUtilConstants.STATS_TYPE);
    }

    public static boolean isInstallAutomatically(Intent intent) {
        return intent.getBooleanExtra(INSTALL_AUTOMATICALLY, false);
    }

    public static long getDeferTime(Intent intent) {
        return intent.getLongExtra(DEFER_TIME_IN_MIN, 0L);
    }

    public static boolean showCancelOption() {
        boolean z;
        boolean z2;
        String string = settings.getString(Configs.METADATA);
        if (string == null) {
            return false;
        }
        SystemUpdaterPolicy systemUpdaterPolicy = new SystemUpdaterPolicy();
        if (systemUpdaterPolicy.getPolicyType() > 0 || systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet()) {
            return false;
        }
        try {
            JSONObject jSONObject = new JSONObject(string);
            z = jSONObject.getBoolean("forced");
            try {
                z2 = jSONObject.getBoolean("bypassPreDownloadDialog");
            } catch (JSONException unused) {
                z2 = false;
                if (z) {
                }
            }
        } catch (JSONException unused2) {
            z = false;
        }
        return z || !(z || bypassPreDownloadDialog(z2));
    }

    public static int getMinimumDelayValue(UpgradeInfo upgradeInfo, long j, long j2) {
        if (upgradeInfo.isForceInstallTimeSet()) {
            long j3 = settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L);
            if (upgradeInfo.isForceInstallTimerExpired()) {
                j3 += WAIT_TIME_AFTER_FORCE_INSTALL_TIMER;
            }
            long minutes = TimeUnit.MILLISECONDS.toMinutes(j3 - (System.currentTimeMillis() + j2)) + 1;
            Logger.debug("OtaApp", "getMinimumDelayValue: actual delay value " + j + " remaining time " + minutes);
            if (minutes < j) {
                return (int) minutes;
            }
        }
        return (int) j;
    }

    public static Intent fillAnnoyValueExpiryDetails(String str, Intent intent) {
        Intent intent2 = new Intent(UpgradeUtilConstants.ACTIVITY_ANNOY_VALUE_EXPIRY);
        intent2.putExtra(UpgradeUtilConstants.KEY_NOTIFICATION_TYPE, str);
        intent2.putExtra(UpgradeUtilConstants.KEY_ANNOY_EXPIRY_TARGET_INTENT, intent);
        return intent2;
    }

    public static Intent getPendingIntentForDlProgressScreen(Context context, int i, long j, long j2, String str, boolean z, String str2) {
        Intent intent = new Intent(context, BaseActivity.class);
        intent.setAction(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str2);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED, i);
        intent.putExtra(UpgradeUtilConstants.KEY_BYTES_TOTAL, j);
        intent.putExtra(UpgradeUtilConstants.KEY_BYTES_RECEIVED, j2);
        intent.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, str);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_ON_WIFI, z);
        intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.DOWNLOAD_PROGRESS_FRAGMENT.toString());
        return intent;
    }

    public static Intent getPendingIntentForBGProgressScreen(Context context, String str, float f, int i, int i2) {
        Intent intent = new Intent(context, BaseActivity.class);
        intent.putExtra(UpgradeUtilConstants.KEY_METADATA, str);
        intent.putExtra(UpgradeUtilConstants.KEY_PERCENTAGE, f);
        intent.putExtra(UpgradeUtilConstants.KEY_UPGRADE_STATUS, i);
        intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED, i2);
        intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.BACKGROUND_INSTALLATION_FRAGMENT.toString());
        return intent;
    }

    public static boolean isWifiOnly() {
        BotaSettings botaSettings;
        String string;
        if (new SystemUpdaterPolicy().isAutoDownloadOverAnyDataNetworkPolicySet() || (string = (botaSettings = settings).getString(Configs.METADATA)) == null) {
            return false;
        }
        try {
            MetaData from = MetaDataBuilder.from(new JSONObject(string));
            if (!isForceOnCellular() && (!BuildPropReader.isBotaATT() || !getAutomaticDownloadForCellular())) {
                String string2 = botaSettings.getString(Configs.FLAVOUR);
                if (string2 != null) {
                    return UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI.name().equals(string2);
                }
                return from.isWifiOnly();
            }
        } catch (JSONException unused) {
        }
        return false;
    }

    public static boolean isWifiOnlyPkg() {
        String string = settings.getString(Configs.METADATA_ORIGINAL);
        if (string == null) {
            return false;
        }
        try {
            return MetaDataBuilder.from(new JSONObject(string)).isWifiOnly();
        } catch (JSONException unused) {
            return false;
        }
    }

    public static boolean isForceOnCellular() {
        String string = settings.getString(Configs.METADATA);
        if (string == null) {
            return false;
        }
        try {
            MetaData from = MetaDataBuilder.from(new JSONObject(string));
            if (BuildPropReader.isFotaATT()) {
                return false;
            }
            return from.isForceOnCellular();
        } catch (JSONException unused) {
            return false;
        }
    }

    public static boolean isDataNetworkRoaming(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager != null) {
            return NetworkUtils.isRoaming(connectivityManager);
        }
        return false;
    }

    public static int getDelay() {
        String string = settings.getString(Configs.METADATA);
        int[] iArr = {DEFAULT_DEFER_TIME};
        if (string != null) {
            try {
                String string2 = new JSONObject(string).getString("annoy");
                if (string2 != null) {
                    if (string2.endsWith(",...")) {
                        string2 = string2.substring(0, string2.length() - 4);
                    }
                    String[] split = string2.split(",");
                    int[] iArr2 = new int[split.length];
                    for (int i = 0; i < split.length; i++) {
                        int parseInt = Integer.parseInt(split[i]);
                        iArr2[i] = parseInt;
                        if (parseInt < 1) {
                            iArr2[i] = 1;
                        }
                    }
                    iArr = iArr2;
                }
            } catch (Exception e) {
                Logger.info("OtaApp", "exception while reading annoy value from metadata" + e);
            }
        }
        return iArr[0];
    }

    public static boolean shouldDisplayLowBatteryPopup() {
        return settings.getBoolean(Configs.BATTERY_LOW);
    }

    public static boolean isBatteryLowToStartDownload(Context context) {
        return getBatteryLevel(context) < allowedBatteryLevel();
    }

    public static String getBatteryLowMessage(Context context) {
        Resources resources = context.getResources();
        if (BuildPropReader.isStreamingUpdate() || BuildPropReader.isUEUpdateEnabled()) {
            return resources.getString(R.string.low_battery_bg_install);
        }
        return resources.getString(R.string.low_battery_download);
    }

    public static boolean checkAndEnableBatteryStatusReceiver() {
        Context globalContext = OtaApplication.getGlobalContext();
        if (globalContext.getPackageManager().getComponentEnabledSetting(new ComponentName(globalContext, BatteryStatusChangeReceiver.class)) == 1) {
            Logger.debug("OtaApp", "BatteryStatusReceiver already enabled");
            registerBatteryStatusChangeReceiver(globalContext);
            return true;
        }
        PackageManager packageManager = globalContext.getPackageManager();
        try {
            Logger.info("OtaApp", "Enabling BatteryStatusReceiver");
            packageManager.setComponentEnabledSetting(new ComponentName(globalContext, BatteryStatusChangeReceiver.class), 1, 1);
            registerBatteryStatusChangeReceiver(globalContext);
            return true;
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception while enabling battery status receiver " + e);
            return false;
        }
    }

    public static void registerBatteryStatusChangeReceiver(Context context) {
        Logger.debug("OtaApp", "registerbattery status change receievr");
        BatteryStatusChangeReceiver batteryStatusChangeReceiver = new BatteryStatusChangeReceiver(settings);
        batteryChangeReceiver = batteryStatusChangeReceiver;
        context.registerReceiver(batteryStatusChangeReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"), 2);
    }

    public static boolean disableBatteryStatusReceiver() {
        Context globalContext = OtaApplication.getGlobalContext();
        if (globalContext.getPackageManager().getComponentEnabledSetting(new ComponentName(globalContext, BatteryStatusChangeReceiver.class)) == 2) {
            Logger.debug("OtaApp", "BatteryStatusReceiver already disabled");
            return true;
        }
        PackageManager packageManager = globalContext.getPackageManager();
        try {
            if (batteryChangeReceiver != null) {
                Logger.info("OtaApp", "unRegistering for battery changed intent");
                globalContext.unregisterReceiver(batteryChangeReceiver);
                batteryChangeReceiver = null;
            }
            packageManager.setComponentEnabledSetting(new ComponentName(globalContext, BatteryStatusChangeReceiver.class), 2, 1);
            return true;
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception while disabling battery status receiver " + e);
            return false;
        }
    }

    public static void enableReceiversForBatteryLow() {
        settings.setBoolean(Configs.BATTERY_LOW, true);
        checkAndEnableBatteryStatusReceiver();
        checkAndEnablePowerDownReceiver();
    }

    public static boolean checkAndEnablePowerDownReceiver() {
        Context globalContext = OtaApplication.getGlobalContext();
        if (globalContext.getPackageManager().getComponentEnabledSetting(new ComponentName(globalContext, PowerdownReceiver.class)) == 1) {
            Logger.debug("OtaApp", "PowerDownReceiver already enabled");
            return true;
        }
        PackageManager packageManager = globalContext.getPackageManager();
        try {
            Logger.info("OtaApp", "Enabling PowerDownReceiver");
            packageManager.setComponentEnabledSetting(new ComponentName(globalContext, PowerdownReceiver.class), 1, 1);
            return true;
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception while enabling powerdown receiver " + e);
            return false;
        }
    }

    public static boolean disablePowerDownReceiver() {
        Context globalContext = OtaApplication.getGlobalContext();
        if (globalContext.getPackageManager().getComponentEnabledSetting(new ComponentName(globalContext, PowerdownReceiver.class)) == 2) {
            Logger.debug("OtaApp", "PowerdownReceiver already disabled");
            return true;
        }
        try {
            globalContext.getPackageManager().setComponentEnabledSetting(new ComponentName(globalContext, PowerdownReceiver.class), 2, 1);
            return true;
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception while disabling Powerdown receiver " + e);
            return false;
        }
    }

    public static boolean checkAndEnableCallStateChangeReceiver() {
        Context globalContext = OtaApplication.getGlobalContext();
        if (globalContext.getPackageManager().getComponentEnabledSetting(new ComponentName(globalContext, CallStateChangeReceiver.class)) == 1) {
            Logger.debug("OtaApp", "CallStateChangeReceiver already enabled");
            return true;
        }
        PackageManager packageManager = globalContext.getPackageManager();
        try {
            Logger.info("OtaApp", "Enabling CallStateChangeReceiver");
            packageManager.setComponentEnabledSetting(new ComponentName(globalContext, CallStateChangeReceiver.class), 1, 1);
            return true;
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception while enabling CallStateChangeReceiver " + e);
            return false;
        }
    }

    public static int getBatteryLevel(Context context) {
        Intent registerReceiver = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"), 2);
        return (registerReceiver.getIntExtra("level", 0) * 100) / registerReceiver.getIntExtra("scale", 100);
    }

    public static int allowedBatteryLevel() {
        int i;
        BotaSettings botaSettings = settings;
        int i2 = botaSettings.getInt(Configs.DEFAULT_MIN_BATTERY_LEVEL, 20);
        if (BuildPropReader.isFotaATT()) {
            return i2;
        }
        String string = botaSettings.getString(Configs.METADATA);
        if (string == null) {
            return getminBatteryRequiredForInstall(i2);
        }
        try {
            i = new JSONObject(string).optInt("minBatteryRequiredForInstall");
        } catch (JSONException unused) {
            i = 0;
        }
        if (i >= 0) {
            i2 = i;
        }
        return getminBatteryRequiredForInstall(i2);
    }

    public static boolean isDisplayErrorAllowed(Intent intent) {
        String string = settings.getString(Configs.METADATA);
        boolean z = true;
        if (string == null) {
            Logger.info("OtaApp", "isDisplayErrorAllowed, no metadata in prefs, return true");
            return true;
        }
        try {
            if (!new JSONObject(string).optBoolean("showDownloadProgress", true)) {
                UpgradeUtils.DownloadStatus downloadStatusFromIntent = UpgradeUtilMethods.downloadStatusFromIntent(intent);
                if (downloadStatusFromIntent != UpgradeUtils.DownloadStatus.STATUS_SPACE && downloadStatusFromIntent != UpgradeUtils.DownloadStatus.STATUS_RESOURCES && downloadStatusFromIntent != UpgradeUtils.DownloadStatus.STATUS_SPACE_INSTALL_CACHE && downloadStatusFromIntent != UpgradeUtils.DownloadStatus.STATUS_RESOURCES_REBOOT && downloadStatusFromIntent != UpgradeUtils.DownloadStatus.STATUS_SPACE_BACKGROUND_INSTALL && downloadStatusFromIntent != UpgradeUtils.DownloadStatus.LOW_BATTERY_INSTALL && downloadStatusFromIntent != UpgradeUtils.DownloadStatus.STATUS_SPACE_PAYLOAD_METADATA_CHECK) {
                    Logger.info("OtaApp", "isDisplayErrorAllowed: showDownloadProgress set to false supress showing error to the user");
                    z = false;
                }
                Logger.info("OtaApp", "Display the error to the user though showDownloadProgress set to false, otherwise user may not get chance to take action for these errors");
            }
        } catch (Exception e) {
            Logger.error("OtaApp", "isDisplayErrorAllowed, exception while reading showDownloadProgress: " + e);
        }
        return z;
    }

    public static boolean showDownloadProgress() {
        String string = settings.getString(Configs.METADATA);
        if (string == null) {
            return true;
        }
        try {
            return new JSONObject(string).optBoolean("showDownloadProgress", true);
        } catch (JSONException unused) {
            return true;
        }
    }

    public static boolean shouldIBlockUpdateByTOD(MetaData metaData) {
        if (BuildPropReader.isBotaATT()) {
            int downloadStartTime = metaData.getDownloadStartTime();
            int downloadEndTime = metaData.getDownloadEndTime();
            long currentTimeMillis = System.currentTimeMillis();
            if (downloadStartTime < 0 || downloadStartTime > 23 || downloadEndTime < 0 || downloadEndTime > 23 || downloadStartTime == downloadEndTime) {
                return false;
            }
            long timestampOfNextHourMinFromNow = getTimestampOfNextHourMinFromNow(downloadStartTime, 0);
            long timestampOfNextHourMinFromNow2 = getTimestampOfNextHourMinFromNow(downloadEndTime, 0);
            if (timestampOfNextHourMinFromNow > timestampOfNextHourMinFromNow2) {
                timestampOfNextHourMinFromNow -= 86400000;
            }
            if (currentTimeMillis < timestampOfNextHourMinFromNow || currentTimeMillis > timestampOfNextHourMinFromNow2) {
                setAlarm(timestampOfNextHourMinFromNow);
                return true;
            }
            return false;
        }
        return false;
    }

    public static String getEstimatedTime(long j, long j2, long[] jArr, Resources resources) {
        if (j2 <= 0 || jArr == null || jArr[0] <= 0) {
            return null;
        }
        try {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(((j - j2) * (System.currentTimeMillis() - jArr[1])) / (j2 - jArr[0]));
            MeasureFormat measureFormat = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT);
            long j3 = seconds / 60;
            return j3 >= 1 ? measureFormat.formatMeasures(new Measure(Long.valueOf(j3), MeasureUnit.MINUTE)) : measureFormat.formatMeasures(new Measure(Long.valueOf(seconds), MeasureUnit.SECOND));
        } catch (Exception e) {
            BotaSettings botaSettings = settings;
            botaSettings.setLong(Configs.KEY_RECEIVED_BYTES, j2);
            botaSettings.setLong(Configs.KEY_TIME_RECEIVED_BYTES, System.currentTimeMillis());
            Logger.error("OtaApp", "exception while calculating estimated time " + e.toString());
            return null;
        }
    }

    public static boolean showPostInstallScreen(Intent intent) {
        UpgradeInfo upgradeInfoAfterOTAUpdate = getUpgradeInfoAfterOTAUpdate(intent);
        if (upgradeInfoAfterOTAUpdate == null) {
            Logger.error("OtaApp", "UpdaterUtils.showPostInstallScreen, No upgradeInfo found.");
            return true;
        }
        return upgradeInfoAfterOTAUpdate.showPostInstallScreen();
    }

    public static void stopDownloadActivity(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.FINISH_DOWNLOAD_ACTIVITY));
    }

    public static void stopDownloadProgressActivity(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.FINISH_DOWNLOAD_PROGRESS_ACTIVITY));
    }

    public static void stopInstallActivity(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.FINISH_INSTALL_ACTIVITY));
    }

    public static void stopBGInstallActivity(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.FINISH_BG_INSTALL_ACTIVITY));
    }

    public static void stopRestartActivity(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.FINISH_RESTART_ACTIVITY));
    }

    public static void stopMessageActivity(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.FINISH_MESSAGE_ACTIVITY));
    }

    public static void stopWarningAlertDialog(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.FINISH_WARNING_ALERT_DIALOG));
    }

    public static void stopDownloadOptionsFragment(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.FINISH_DOWNLOAD_OPTIONS_FRAGMENT));
    }

    public static long downloadFileSize() {
        return new File(FileUtils.getLocalPath(settings)).length();
    }

    public static void sendCheckUpdateIntent(Context context) {
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE);
        intent.putExtra(UpgradeUtilConstants.KEY_BOOTSTRAP, false);
        intent.putExtra(UpgradeUtilConstants.KEY_REQUESTID, 0);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public static void launchNextSetupActivityVitalUpdate(Context context) {
        Logger.debug("OtaApp", "launchNextSetupActivityVitalUpdate");
        Intent nextIntent = WizardManagerHelper.getNextIntent(BaseActivity.vitalUpdateLaunchIntent, -1);
        nextIntent.setFlags(268435456);
        context.startActivity(nextIntent);
    }

    public static boolean checkDozeMode(Context context) {
        return ((PowerManager) context.getSystemService("power")).isDeviceIdleMode();
    }

    public static final boolean isDeviceInDatasaverMode() {
        ConnectivityManager connectivityManager = (ConnectivityManager) OtaApplication.getGlobalContext().getSystemService("connectivity");
        return connectivityManager != null && connectivityManager.isActiveNetworkMetered() && connectivityManager.getRestrictBackgroundStatus() == 3;
    }

    public static boolean updateMotorolaSettingsProvider(Context context, String str, String str2) {
        try {
            return ((Boolean) Class.forName(MOTO_SETTINGS_PROVIDER_CLASS_NAME).getMethod("putString", ContentResolver.class, String.class, String.class).invoke(null, context.getContentResolver(), str, str2)).booleanValue();
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Logger.error("OtaApp", "Exception in UpdaterUtils, updateMotorolaSettingsProvider: " + e);
            return false;
        }
    }

    public static String getStaticFieldName(String str, String str2) {
        try {
            return (String) Class.forName(str).getDeclaredField(str2).get(null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            Logger.error("OtaApp", "Exception in UpdaterUtils, getStaticFieldName: " + e);
            return "";
        }
    }

    public static boolean isSimLoaded(Intent intent) {
        return getStaticFieldName("com.android.internal.telephony.IccCardConstants", "INTENT_VALUE_ICC_LOADED").equals(intent.getStringExtra(getStaticFieldName("com.android.internal.telephony.IccCardConstants", "INTENT_KEY_ICC_STATE")));
    }

    public static boolean isAdminAPNEnabled() {
        return !TextUtils.isEmpty(UpgradeUtils.getAdminApnUrl(settings.getString(Configs.DOWNLOAD_DESCRIPTOR)));
    }

    public static long getNextPromptForNotification(String str, UpgradeInfo upgradeInfo) {
        long currentTimeMillis = System.currentTimeMillis();
        if (NotificationUtils.KEY_DOWNLOAD.equals(str)) {
            return currentTimeMillis + (NotificationUtils.getPreDownloadNotificationExpiryMins() * 60000);
        }
        long preInstallNotificationExpiryMins = (NotificationUtils.getPreInstallNotificationExpiryMins(upgradeInfo) * 60000) + currentTimeMillis;
        if (upgradeInfo != null && upgradeInfo.isCriticalUpdate()) {
            CriticalUpdate criticalUpdate = new CriticalUpdate(upgradeInfo);
            preInstallNotificationExpiryMins = criticalUpdate.isCriticalUpdateTimerExpired() ? currentTimeMillis + CusAndroidUtils.URL_EXPIRY_TIME : criticalUpdate.getAndSetNextPromptValue(System.currentTimeMillis());
        } else if (upgradeInfo != null && upgradeInfo.isForceInstallTimeSet()) {
            long j = settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L);
            if (upgradeInfo.isForceInstallTimerExpired()) {
                j += WAIT_TIME_AFTER_FORCE_INSTALL_TIMER;
            }
            long j2 = j - currentTimeMillis;
            if (j2 < -1) {
                preInstallNotificationExpiryMins = currentTimeMillis + CusAndroidUtils.URL_EXPIRY_TIME;
            } else if (j2 < NotificationUtils.getPreInstallNotificationExpiryMins(upgradeInfo) * 60 * 1000) {
                preInstallNotificationExpiryMins = currentTimeMillis + j2;
            }
        }
        BotaSettings botaSettings = settings;
        return SmartUpdateUtils.isSmartUpdateNearestToInstall(botaSettings, upgradeInfo.getUpdateTypeData()) ? botaSettings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L) : preInstallNotificationExpiryMins;
    }

    public static long getNextPromptForActivity(UpgradeInfo upgradeInfo, int i) {
        CriticalUpdate criticalUpdate = new CriticalUpdate(upgradeInfo);
        if (upgradeInfo.isCriticalUpdate()) {
            criticalUpdate.getAndSetNextPromptValue(System.currentTimeMillis());
        }
        InternalUpdateType internalUpdateType = getInternalUpdateType(upgradeInfo);
        long currentTimeMillis = System.currentTimeMillis() + (upgradeInfo.getDelay(i) * 60 * 1000);
        BotaSettings botaSettings = settings;
        boolean z = botaSettings.getBoolean(Configs.CHECKBOX_SELECTED);
        long j = botaSettings.getLong(Configs.AUTO_UPDATE_TIME_SELECTED, -1L);
        if (!z || System.currentTimeMillis() >= j) {
            int i2 = AnonymousClass7.$SwitchMap$com$motorola$ccc$ota$ui$UpdaterUtils$InternalUpdateType[internalUpdateType.ordinal()];
            if (i2 != 1) {
                if (i2 == 2) {
                    long andSetNextPromptValue = criticalUpdate.getAndSetNextPromptValue(System.currentTimeMillis());
                    return (!criticalUpdate.isCriticalUpdateTimerExpired() || criticalUpdate.isOutsideCriticalUpdateExtendedTime()) ? andSetNextPromptValue : criticalUpdate.getExtendRestartTime();
                } else if (i2 != 3) {
                    return currentTimeMillis;
                } else {
                    long j2 = botaSettings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L);
                    if (upgradeInfo.isForceInstallTimerExpired()) {
                        j2 += WAIT_TIME_AFTER_FORCE_INSTALL_TIMER;
                    }
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(j2 - System.currentTimeMillis()) + 1;
                    if (minutes < upgradeInfo.getDelay(i)) {
                        long currentTimeMillis2 = System.currentTimeMillis() + (minutes * 60000);
                        Logger.debug("OtaApp", "RestartFragment.onNewIntent: Force install next prompt: " + currentTimeMillis2);
                        return currentTimeMillis2;
                    }
                    return currentTimeMillis;
                }
            }
            return botaSettings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L);
        }
        return j;
    }

    /* renamed from: com.motorola.ccc.ota.ui.UpdaterUtils$7  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    static /* synthetic */ class AnonymousClass7 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$ccc$ota$ui$UpdaterUtils$InternalUpdateType;

        static {
            int[] iArr = new int[InternalUpdateType.values().length];
            $SwitchMap$com$motorola$ccc$ota$ui$UpdaterUtils$InternalUpdateType = iArr;
            try {
                iArr[InternalUpdateType.SMART_UPDATE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$ui$UpdaterUtils$InternalUpdateType[InternalUpdateType.CRITICAL_UPDATE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$ui$UpdaterUtils$InternalUpdateType[InternalUpdateType.FORCE_UPDATE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public static boolean isMaxRetryCountReachedForVerizon(Intent intent) {
        return intent.getIntExtra(UpgradeUtilConstants.KEY_UPDATE_FAILURE_COUNT, 0) == 2 && BuildPropReader.isVerizon();
    }

    public static boolean isScreenOff(Context context) {
        return !((PowerManager) context.getSystemService("power")).isInteractive();
    }

    public static boolean isDeviceLocked(Context context) {
        return isScreenOff(context) || ((KeyguardManager) context.getSystemService("keyguard")).isKeyguardLocked();
    }

    public static boolean getAutomaticDownloadForCellular() {
        return settings.getBoolean(Configs.AUTOMATIC_DOWNLOAD_FOR_CELLULAR);
    }

    public static void setAutomaticDownloadForCellular(boolean z) {
        settings.setBoolean(Configs.AUTOMATIC_DOWNLOAD_FOR_CELLULAR, z);
    }

    public static long getMaxForceDownloadDeferTime() {
        return settings.getLong(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME, -1L);
    }

    public static int getForceDownloadDelay(long j) {
        int maxForceDownloadDeferTime = (int) ((getMaxForceDownloadDeferTime() - j) / 1000);
        if (maxForceDownloadDeferTime > 259200) {
            return maxForceDownloadDeferTime - FINAL_DEFER_TIME_FOR_SMR;
        }
        if (maxForceDownloadDeferTime > 259200 || maxForceDownloadDeferTime < 0) {
            return -1;
        }
        sendDownloadModeStats("forceDownloadReminded");
        return maxForceDownloadDeferTime;
    }

    public static boolean shouldBlockFullScreen() {
        return settings.getBoolean(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY);
    }

    public static int getProgressScreenDisplayNextPromptInMins(BotaSettings botaSettings) {
        long j = botaSettings.getLong(Configs.ACTIVITY_NEXT_PROMPT, -1L);
        if (j == -1) {
            return -1;
        }
        return Integer.max((int) TimeUnit.MILLISECONDS.toMinutes(j - System.currentTimeMillis()), 0);
    }

    public static void setProgressScreenDisplayNextPrompt(BotaSettings botaSettings) {
        long j = botaSettings.getLong(Configs.ACTIVITY_NEXT_PROMPT, -1L);
        long maxForceDownloadDeferTime = getMaxForceDownloadDeferTime();
        long currentTimeMillis = j + (((((int) (System.currentTimeMillis() - j)) / 86400000) + 1) * 86400000);
        botaSettings.setLong(Configs.ACTIVITY_NEXT_PROMPT, currentTimeMillis < maxForceDownloadDeferTime ? currentTimeMillis : -1L);
    }

    public static void makeTextViewLinkify(TextView textView, String str) {
        SpannableString spannableString = new SpannableString(str);
        Linkify.addLinks(spannableString, 1);
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static boolean isThreeDaysBeforeForceInstall() {
        long j = settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L);
        return j > -1 && j - System.currentTimeMillis() <= 259200000;
    }

    public static boolean isForceInstallDeferTimeExpired() {
        long j = settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L);
        return j > -1 && System.currentTimeMillis() > j;
    }

    public static int countOccurrences(String str, char c) {
        int i = 0;
        for (char c2 : str.toCharArray()) {
            if (c2 == c) {
                i++;
            }
        }
        return i;
    }

    public static void addNewInstallationTime(long j) {
        Context globalContext;
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor edit = OtaApplication.getGlobalContext().getSharedPreferences(KEY_INTELLIGENT_NOTIFICATION, 0).edit();
        String str = sharedPreferences.getString(TIME_STRING, "") + j + ";";
        if (countOccurrences(str, ';') > 7) {
            str = str.substring(str.indexOf(";") + 1);
        }
        edit.putString(TIME_STRING, str);
        edit.apply();
        Logger.debug("OtaApp", "Added " + DateFormatUtils.getCalendarString(globalContext, j) + " to " + str);
    }

    public static long getStartOfTodayInMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTimeInMillis();
    }

    public static int getTimeRemainingForBestTime(long j) {
        SharedPreferences sharedPreferences = OtaApplication.getGlobalContext().getSharedPreferences(KEY_INTELLIGENT_NOTIFICATION, 0);
        if (sharedPreferences.getInt(BEST_TIME_1_HOUR, -1) == -1) {
            new IntelligentNotificationTime().compute();
        }
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(Math.min(getTimestampOfNextHourMinFromSpecificTime(sharedPreferences.getInt(BEST_TIME_1_HOUR, 0), sharedPreferences.getInt(BEST_TIME_1_MINUTE, 0), j), getTimestampOfNextHourMinFromSpecificTime(sharedPreferences.getInt(BEST_TIME_2_HOUR, 0), sharedPreferences.getInt(BEST_TIME_2_MINUTE, 0), j)) - j);
        Logger.debug("OtaApp", "UpdaterUtils.getTimeRemainingForBestTime: " + minutes + " mins");
        return minutes;
    }

    public static long getTimestampOfNextHourMinFromNow(int i, int i2) {
        return getTimestampOfNextHourMinFromSpecificTime(i, i2, System.currentTimeMillis());
    }

    public static long getTimestampOfNextHourMinFromSpecificTime(int i, int i2, long j) {
        long startOfTodayInMillis = getStartOfTodayInMillis() + (i * AndroidPollingManager.MINIMUM_POLLING_INTERVAL * 1000) + (i2 * 60 * 1000);
        return startOfTodayInMillis < j ? startOfTodayInMillis + 86400000 : startOfTodayInMillis;
    }

    public static String getInstallModeStats() {
        return settings.getString(Configs.STATS_INSTALL_MODE);
    }

    public static boolean isFeatureOn(String str) {
        return str != null && str.equals("on");
    }

    public static boolean getAdvancedDownloadFeature() {
        BotaSettings botaSettings = settings;
        if (botaSettings.getInt(Configs.ADVANCE_DL_RETRY_COUNT, 0) >= 2) {
            return false;
        }
        return isFeatureOn(botaSettings.getString(Configs.ADVANCED_DOWNLOAD_FEATURE));
    }

    public static boolean isCriticalUpdateTimerExpired(UpgradeInfo upgradeInfo) {
        BotaSettings botaSettings = settings;
        return botaSettings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L) > 0 && System.currentTimeMillis() >= botaSettings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L);
    }

    public static void setDontBotherPreferences(int i) {
        BotaSettings botaSettings = settings;
        botaSettings.setLong(Configs.PREVIOUS_CANCELLED_OPT_CONTENT_TIMESTAMP, botaSettings.getLong(Configs.CONTENT_TIMESTAMP, 0L));
        botaSettings.setLong(Configs.PREVIOUS_CANCELLED_OPT_UPDATE_ANNOY_TIME, System.currentTimeMillis() + (i * 86400000));
    }

    public static boolean checkForFinalDeferTimeForForceUpdate() {
        return getMaxForceDownloadDeferTime() > -1 && ((int) ((getMaxForceDownloadDeferTime() - System.currentTimeMillis()) / 1000)) <= 259200;
    }

    public static boolean isPriorityAppRunning(Context context) {
        String str;
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (runningAppProcesses != null) {
            Iterator<ActivityManager.RunningAppProcessInfo> it = runningAppProcesses.iterator();
            while (true) {
                if (!it.hasNext()) {
                    str = null;
                    break;
                }
                ActivityManager.RunningAppProcessInfo next = it.next();
                if (next.importance == 100) {
                    str = next.processName;
                    break;
                }
            }
            return "com.google.android.apps.maps".equals(str) || "com.motorola.camera3".equals(str);
        }
        return false;
    }

    public static boolean isMapsNavigationRunning(Context context) {
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (runningAppProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                if (runningAppProcessInfo.importance == 100 && "com.google.android.apps.maps".equals(runningAppProcessInfo.processName)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public static InternalUpdateType getInternalUpdateType(UpgradeInfo upgradeInfo) {
        CriticalUpdate criticalUpdate = new CriticalUpdate(upgradeInfo);
        if (SmartUpdateUtils.isSmartUpdateNearestToInstall(settings, upgradeInfo.getUpdateTypeData())) {
            return InternalUpdateType.SMART_UPDATE;
        }
        if (criticalUpdate.isCriticalUpdate()) {
            return InternalUpdateType.CRITICAL_UPDATE;
        }
        if (isForceUpdateShouldDisplay(upgradeInfo)) {
            return InternalUpdateType.FORCE_UPDATE;
        }
        return InternalUpdateType.DEFAULT_UPDATE;
    }

    private static boolean isForceUpdateShouldDisplay(UpgradeInfo upgradeInfo) {
        if (upgradeInfo.isForceInstallTimeSet()) {
            long j = settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L);
            if (upgradeInfo.isForceInstallTimerExpired()) {
                j += WAIT_TIME_AFTER_FORCE_INSTALL_TIMER;
            }
            return (TimeUnit.MILLISECONDS.toMinutes(j - System.currentTimeMillis()) + 1) * 60 <= 259200 || upgradeInfo.isForceInstallTimerExpired();
        }
        return false;
    }

    public static void saveNextPrompt(long j) {
        settings.setLong(Configs.ACTIVITY_NEXT_PROMPT, j);
    }

    public static String getURLWithDeviceParam(String str, String str2) {
        HashMap hashMap = new HashMap();
        hashMap.put("ro.product.model", BuildPropReader.getDeviceModel().replaceAll(SystemUpdateStatusUtils.SPACE, "%20"));
        hashMap.put("ro.product.name", BuildPropReader.getProductName().replaceAll(SystemUpdateStatusUtils.SPACE, "%20"));
        hashMap.put("ro.carrier", BuildPropReader.getCarrier());
        hashMap.put("ro.boot.hardware.sku", BuildPropReader.getHardwareSku());
        hashMap.put("ro.build.version.release", str2);
        hashMap.put("apk_package_name", OtaApplication.getGlobalContext().getPackageName());
        for (Map.Entry entry : hashMap.entrySet()) {
            str = str.replace((CharSequence) entry.getKey(), (CharSequence) entry.getValue());
        }
        return str;
    }

    public static void addNoInternetDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View inflate = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.custom_dialog, (ViewGroup) null);
        builder.setView(inflate);
        final AlertDialog create = builder.create();
        TextView textView = (TextView) inflate.findViewById(R.id.custom_alert_title);
        textView.setVisibility(0);
        textView.setText(context.getResources().getString(R.string.dialog_alert_net_title));
        TextView textView2 = (TextView) inflate.findViewById(R.id.custom_alert_text);
        textView2.setVisibility(0);
        textView2.setText(context.getResources().getString(R.string.dialog_alert_net_message));
        Button button = (Button) inflate.findViewById(R.id.custom_btn_allow);
        button.setVisibility(0);
        button.setText(context.getResources().getString(R.string.alert_dialog_ok));
        button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdaterUtils$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                create.dismiss();
            }
        });
        setCornersRounded(context, create);
        create.show();
        UpdateType.getUpdateType(getUpdateType(settings.getString(Configs.METADATA)));
    }

    public static void displayWebViewFragment(Context context, String str, String str2) {
        if (!NetworkUtils.isNetWorkConnected((ConnectivityManager) context.getSystemService("connectivity"))) {
            addNoInternetDialog(context);
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putString(UpgradeUtilConstants.KEY_WEBVIEW_URL, str);
        bundle.putString(UpgradeUtilConstants.KEY_WEBVIEW_BASE_FRAGMENT_STATS, str2);
        WebViewFragment webViewFragment = new WebViewFragment();
        webViewFragment.setArguments(bundle);
        webViewFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "WebViewDialog");
    }

    public static void setOsReleaseNotes(final Context context, TextView textView, UpgradeInfo upgradeInfo, final String str) {
        String oSreleaseLink = upgradeInfo.getOSreleaseLink();
        String targetOSVersion = upgradeInfo.getTargetOSVersion();
        if (TextUtils.isEmpty(oSreleaseLink) || TextUtils.isEmpty(targetOSVersion)) {
            return;
        }
        final String uRLWithDeviceParam = getURLWithDeviceParam(oSreleaseLink, targetOSVersion);
        String str2 = "<a href=" + uRLWithDeviceParam + ">" + context.getResources().getString(R.string.release_notes_string, targetOSVersion) + "</a>";
        textView.setText(Html.fromHtml(str2, 0, null, new HtmlUtils(str2)));
        textView.setVisibility(0);
        textView.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdaterUtils.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                UpdaterUtils.displayWebViewFragment(context, uRLWithDeviceParam, str);
            }
        });
    }

    public static LinkedHashMap<String, List<String>> getExpandableListData(String str) {
        String substring;
        LinkedHashMap<String, List<String>> linkedHashMap = new LinkedHashMap<>();
        int countOfSubString = countOfSubString(str, "<h3>");
        for (int i = 0; i < countOfSubString; i++) {
            ArrayList arrayList = new ArrayList();
            String substring2 = str.substring(str.indexOf("<h3>") + 4, str.indexOf("</h3>"));
            str = str.substring(str.indexOf("</h3>") + 5);
            if (str.contains("<h3>")) {
                substring = str.substring(0, str.indexOf("<h3>"));
            } else {
                substring = str.substring(0, str.indexOf("</body>"));
            }
            int countOfSubString2 = countOfSubString(substring, "<p>");
            String str2 = "";
            for (int i2 = 0; i2 < countOfSubString2; i2++) {
                str2 = str2 + substring.substring(substring.indexOf("<p>") + 3, substring.indexOf("</p>"));
                if (i2 != countOfSubString2 - 1) {
                    str2 = str2 + "<br/><br/>";
                }
                substring = substring.substring(substring.indexOf("</p>") + 4);
            }
            if (str.indexOf("<h3>") != -1) {
                str = str.substring(str.indexOf("<h3>"));
            }
            arrayList.add(str2);
            linkedHashMap.put(substring2, arrayList);
        }
        return linkedHashMap;
    }

    public static LinkedHashMap<String, List<String>> getExpandableListData(Resources resources, JSONObject jSONObject) {
        int i;
        Iterator<String> keys = jSONObject.keys();
        String str = "";
        String str2 = "";
        String str3 = str2;
        while (true) {
            i = 0;
            if (keys.hasNext()) {
                try {
                    String next = keys.next();
                    int hashCode = next.hashCode();
                    if (hashCode == -863554611) {
                        if (next.equals("OSUpdate")) {
                        }
                        i = -1;
                    } else if (hashCode != 1490108406) {
                        if (hashCode == 1842756012 && next.equals("FeatureSet")) {
                            i = 2;
                        }
                        i = -1;
                    } else {
                        if (next.equals("PatchDate")) {
                            i = 1;
                        }
                        i = -1;
                    }
                    if (i == 0) {
                        str2 = jSONObject.getString("OSUpdate");
                    } else if (i == 1) {
                        str = jSONObject.getString("PatchDate");
                    } else if (i == 2) {
                        str3 = jSONObject.getString("FeatureSet");
                    }
                } catch (JSONException e) {
                    Logger.error("OtaApp", "Exception in getExpandableListData: " + e);
                }
            } else {
                try {
                    break;
                } catch (Exception e2) {
                    Logger.error("OtaApp", "Exception in getExpandableListData while parsing patch date: " + e2);
                }
            }
        }
        String[] split = str.split(FileUtils.SD_CARD_DIR);
        str = LocalDate.of(Integer.parseInt(split[2]), Integer.parseInt(split[0]), Integer.parseInt(split[1])).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
        LinkedHashMap<String, List<String>> linkedHashMap = new LinkedHashMap<>();
        ArrayList arrayList = new ArrayList();
        String string = resources.getString(R.string.release_note_os_update_title, str2);
        if (!TextUtils.isEmpty(str2)) {
            arrayList.add(resources.getString(R.string.release_note_os_update_desc));
            linkedHashMap.put(string, arrayList);
        }
        String string2 = resources.getString(R.string.release_note_security_title);
        if (!TextUtils.isEmpty(str)) {
            ArrayList arrayList2 = new ArrayList();
            arrayList2.add(resources.getString(R.string.release_note_security_desc, str));
            linkedHashMap.put(string2, arrayList2);
        }
        if (!TextUtils.isEmpty(str3)) {
            String[] split2 = str3.split(",");
            int length = split2.length;
            while (i < length) {
                Map<String, String> featureDetails = getFeatureDetails(resources, split2[i]);
                if (featureDetails.keySet().iterator().hasNext()) {
                    String next2 = featureDetails.keySet().iterator().next();
                    ArrayList arrayList3 = new ArrayList();
                    arrayList3.add(featureDetails.get(next2));
                    linkedHashMap.put(next2, arrayList3);
                }
                i++;
            }
        }
        return linkedHashMap;
    }

    private static Map<String, String> getFeatureDetails(Resources resources, String str) {
        HashMap hashMap = new HashMap();
        String lowerCase = str.trim().toLowerCase(Locale.ROOT);
        lowerCase.hashCode();
        char c = 65535;
        switch (lowerCase.hashCode()) {
            case -1480388560:
                if (lowerCase.equals("performance")) {
                    c = 0;
                    break;
                }
                break;
            case -1367751899:
                if (lowerCase.equals("camera")) {
                    c = 1;
                    break;
                }
                break;
            case -916596374:
                if (lowerCase.equals("cellular")) {
                    c = 2;
                    break;
                }
                break;
            case -331239923:
                if (lowerCase.equals("battery")) {
                    c = 3;
                    break;
                }
                break;
            case 3649301:
                if (lowerCase.equals("wifi")) {
                    c = 4;
                    break;
                }
                break;
            case 1436115569:
                if (lowerCase.equals("charging")) {
                    c = 5;
                    break;
                }
                break;
            case 1698162601:
                if (lowerCase.equals("stability")) {
                    c = 6;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                hashMap.put(resources.getString(R.string.release_note_performance_title), resources.getString(R.string.release_note_performance_desc));
                break;
            case 1:
                hashMap.put(resources.getString(R.string.release_note_camera_title), resources.getString(R.string.release_note_camera_desc));
                break;
            case 2:
                hashMap.put(resources.getString(R.string.release_note_cellular_title), resources.getString(R.string.release_note_cellular_desc));
                break;
            case 3:
                hashMap.put(resources.getString(R.string.release_note_battery_title), resources.getString(R.string.release_note_battery_desc));
                break;
            case 4:
                hashMap.put(resources.getString(R.string.release_note_wifi_title), resources.getString(R.string.release_note_wifi_desc));
                break;
            case 5:
                hashMap.put(resources.getString(R.string.release_note_charging_title), resources.getString(R.string.release_note_charging_desc));
                break;
            case 6:
                hashMap.put(resources.getString(R.string.release_note_stability_title), resources.getString(R.string.release_note_stability_desc));
                break;
            default:
                hashMap.put(str, resources.getString(R.string.release_note_generic_desc));
                break;
        }
        return hashMap;
    }

    public static int countOfSubString(String str, String str2) {
        int i = 0;
        int i2 = 0;
        while (i != -1) {
            i = str.indexOf(str2, i);
            if (i != -1) {
                i2++;
                i += str2.length();
            }
        }
        return i2;
    }

    public static void getInstructionLayout(Context context, LinearLayout linearLayout, String str, int i) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(0, 20, 0, 0);
        layoutParams.setMarginStart(0);
        layoutParams.setMarginEnd(0);
        TextView textView = (TextView) ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.instruction_style, (ViewGroup) null);
        textView.setText(str);
        textView.setLayoutParams(layoutParams);
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(i, 0, 0, 0);
        linearLayout.addView(textView);
    }

    public static void setInstructions(String str, boolean z, LinearLayout linearLayout, Context context, UpdateType.UpdateTypeInterface updateTypeInterface) {
        Logger.debug("OtaApp", "UpdaterUtils:Instructions: " + str);
        if (z) {
            try {
                int countOfSubString = countOfSubString(str, "<p>") - 1;
                String substring = str.substring(str.indexOf("<p>") + 3, str.indexOf("</p>"));
                String substring2 = str.substring(str.indexOf("</p>") + 4);
                int i = 0;
                if (substring.equalsIgnoreCase("APPEND")) {
                    while (i < countOfSubString) {
                        getInstructionLayout(context, linearLayout, substring2.substring(substring2.indexOf("<p>") + 3, substring2.indexOf("</p>")), updateTypeInterface.getDefaultInstructionImage());
                        substring2 = substring2.substring(substring2.indexOf("</p>") + 4);
                        i++;
                    }
                } else if (substring.equalsIgnoreCase("REPLACE")) {
                    if (linearLayout.getChildCount() > 0) {
                        linearLayout.removeAllViews();
                    }
                    while (i < countOfSubString) {
                        getInstructionLayout(context, linearLayout, substring2.substring(substring2.indexOf("<p>") + 3, substring2.indexOf("</p>")), updateTypeInterface.getDefaultInstructionImage());
                        substring2 = substring2.substring(substring2.indexOf("</p>") + 4);
                        i++;
                    }
                }
            } catch (Exception e) {
                Logger.error("OtaApp", "UpdaterUtils, Error setting Instructions: " + e);
            }
        }
    }

    public static Bitmap getBitmap(Context context, int i) {
        Drawable drawable = ContextCompat.getDrawable(context, i);
        Canvas canvas = new Canvas();
        Bitmap createBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(createBitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return createBitmap;
    }

    public static void insertHistory(String str, String str2, String str3, String str4, BotaSettings botaSettings) {
        try {
            new HistoryDbHandler(OtaApplication.getGlobalContext()).insertHistoryDetails(botaSettings.getString(Configs.HISTORY_SOURCE_VERSION), str, str2, System.currentTimeMillis(), str3, str4);
            botaSettings.removeConfig(Configs.HISTORY_SOURCE_VERSION);
        } catch (Exception e) {
            Logger.debug("OtaApp", "insertHistory Exception = " + e);
        }
    }

    public static void setFullScreenStartPoint(Configs configs, String str) {
        BotaSettings botaSettings = settings;
        if (TextUtils.isEmpty(botaSettings.getString(configs))) {
            botaSettings.setString(configs, str);
        }
    }

    public static boolean isTOSCheckPassed(BotaSettings botaSettings) {
        Logger.verbose("OtaApp", "isTOSCheckPassed:isChinaDevice=" + BuildPropertyUtils.isChinaDevice(OtaApplication.getGlobalContext()));
        if (!BuildPropertyUtils.isChinaDevice(OtaApplication.getGlobalContext()) || botaSettings.getBoolean(Configs.SETUP_TOS_ACCEPTED)) {
            return true;
        }
        Logger.debug("OtaApp", "isTOSCheckPassed : User did not accept TOS blocking setup trigger");
        return false;
    }

    public static void setSoftBankProxyData(Context context) {
        if (isSoftBankApn(context)) {
            BotaSettings botaSettings = settings;
            botaSettings.setString(Configs.CDS_HTTP_PROXY_HOST, UpgradeUtilConstants.SOFTBANK_PROXY_HOST);
            botaSettings.setInt(Configs.CDS_HTTP_PROXY_PORT, UpgradeUtilConstants.SOFTBANK_PROXY_PORT);
            botaSettings.setString(Configs.DOWNLOAD_HTTP_PROXY_HOST, UpgradeUtilConstants.SOFTBANK_PROXY_HOST);
            botaSettings.setInt(Configs.DOWNLOAD_HTTP_PROXY_PORT, UpgradeUtilConstants.SOFTBANK_PROXY_PORT);
            return;
        }
        BotaSettings botaSettings2 = settings;
        botaSettings2.removeConfig(Configs.CDS_HTTP_PROXY_HOST);
        botaSettings2.removeConfig(Configs.CDS_HTTP_PROXY_PORT);
        botaSettings2.removeConfig(Configs.DOWNLOAD_HTTP_PROXY_HOST);
        botaSettings2.removeConfig(Configs.DOWNLOAD_HTTP_PROXY_PORT);
    }

    public static boolean isSoftBankApn(Context context) {
        return new BotaSettings().getBoolean(Configs.IS_SOFTBANK_APN_ALLOWED) && BuildPropReader.isSoftbank() && NetworkUtils.isSoftBankApn(context);
    }

    public static long getUserdataSpaceRequired(MetaData metaData) {
        long optLong;
        if (metaData == null) {
            return 0L;
        }
        if (BuildPropReader.isFotaATT()) {
            optLong = metaData.getUserDataRequiredForUpdate();
        } else {
            JSONObject streamingData = metaData.getStreamingData();
            JSONObject optJSONObject = streamingData != null ? streamingData.optJSONObject("header") : null;
            optLong = optJSONObject != null ? optJSONObject.optLong("USERDATA_REQUIRED_FOR_UPDATE") : 0L;
        }
        if (optLong < 0) {
            return 0L;
        }
        return optLong;
    }

    public static void setNavBarColorFromDialog(Dialog dialog) {
        Window window = dialog.getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.setNavigationBarColor(0);
    }

    public static void setAlarm(long j) {
        Context globalContext = OtaApplication.getGlobalContext();
        AlarmManager alarmManager = (AlarmManager) globalContext.getSystemService("alarm");
        PendingIntent broadcast = PendingIntent.getBroadcast(globalContext, 1, new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE), 335544320);
        alarmManager.cancel(broadcast);
        alarmManager.setExactAndAllowWhileIdle(0, j, broadcast);
    }

    public static void setOtaSystemBindServiceEnabledState(Context context, boolean z) {
        int i = z ? 1 : 2;
        if (z && isOtaSystemBindServiceEnabled(context)) {
            return;
        }
        try {
            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, OtaSystemServerBindService.class), i, 1);
        } catch (Exception e) {
            Logger.error("OtaApp", "setOtaSystemBindServiceEnabled, exception in  setting enable state = " + i + " to  OtaSystemServerBindService:" + e);
        }
    }

    public static boolean isOtaSystemBindServiceEnabled(Context context) {
        try {
            return context.getPackageManager().getComponentEnabledSetting(new ComponentName(context, OtaSystemServerBindService.class)) == 1;
        } catch (Exception e) {
            Logger.error("OtaApp", "isOtaSystemBindServiceEnabled, exception in  getting enable state : " + e);
            return false;
        }
    }

    public static boolean isBatterySaverEnabled(Context context) {
        return ((PowerManager) context.getSystemService(PowerManager.class)).isPowerSaveMode();
    }

    public static Map<String, String> getStoredModemMap() {
        String string = settings.getString(Configs.MODEM_CONFIG_VERSIONS);
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        try {
            JSONObject jSONObject = new JSONObject(string);
            Iterator<String> keys = jSONObject.keys();
            while (keys.hasNext()) {
                String next = keys.next();
                linkedHashMap.put(next, jSONObject.getString(next));
            }
        } catch (Exception e) {
            if (!TextUtils.isEmpty(string)) {
                Logger.error("OtaApp", "getModemMapFromJson:Exception:msg=" + e);
            }
        }
        return linkedHashMap;
    }

    public static boolean canISendSuccessiveModemPollRequest() {
        Map<String, String> storedModemMap = getStoredModemMap();
        if (storedModemMap != null && storedModemMap.isEmpty()) {
            Logger.debug("OtaApp", "no versions in stored configVersionMap pref");
            settings.removeConfig(Configs.MODEM_CONFIG_VERSIONS);
            return false;
        }
        Map.Entry<String, String> next = storedModemMap.entrySet().iterator().next();
        if (next != null) {
            storedModemMap.remove(next.getKey());
        }
        if (storedModemMap.isEmpty()) {
            Logger.debug("OtaApp", "no versions in stored configVersionMap pref");
            settings.removeConfig(Configs.MODEM_CONFIG_VERSIONS);
            return false;
        }
        settings.setString(Configs.MODEM_CONFIG_VERSIONS, new JSONObject(storedModemMap).toString());
        Logger.debug("OtaApp", "Again polling should be triggered for another carrier");
        return true;
    }

    public static void setCornersRounded(Context context, AlertDialog alertDialog) {
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.corner_rounded, null));
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = -2;
        layoutParams.height = -2;
        alertDialog.getWindow().setAttributes(layoutParams);
    }

    public static boolean isOtaServiceRunning(Context context) {
        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) context.getSystemService("activity")).getRunningServices(PMUtils.APP_IS_EASY)) {
            if (OtaService.class.getName().equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void stopOtaService(Context context) {
        setOtaSystemBindServiceEnabledState(context, false);
        if (isOtaServiceRunning(context)) {
            Logger.debug("OtaApp", "UpdaterUtils - stopOtaService : ota service is running, stopping OTA Service");
            context.stopService(new Intent(context, OtaService.class));
        }
    }

    public static void scheduleWorkManager(Context context) {
        if (BuildPropReader.isFotaATT()) {
            Logger.debug("OtaApp", "scheduleWorkManager:Fota ATT Device, don't schedule polling, return from here");
            return;
        }
        BotaSettings botaSettings = settings;
        long j = botaSettings.getLong(Configs.POLL_AFTER, 86400000L);
        long j2 = botaSettings.getLong(Configs.PREV_POLL_AFTER, -1L);
        long j3 = j >= 0 ? j : 86400000L;
        if (j2 == -1) {
            botaSettings.setLong(Configs.PREV_POLL_AFTER, j3);
        }
        Logger.debug("OtaApp", "scheduleWorkManager:repeatInterval=" + j3 + " : prevRepeatInterval=" + j2);
        if (j3 == 0) {
            shutDownPolling(context, PMUtils.OTA_UNIQUE_WORK_NAME);
        } else if (isUniqueWorkScheduled(context, PMUtils.OTA_UNIQUE_WORK_NAME) && j3 == j2) {
            Logger.debug("OtaApp", "scheduleWorkManager:Already WorkManager is scheduled with same interval, so don't schedule again");
        } else {
            botaSettings.setLong(Configs.PREV_POLL_AFTER, j3);
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(PMUtils.OTA_UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, new PeriodicWorkRequest.Builder(CheckUpdateWorker.class, j3, TimeUnit.MILLISECONDS).setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).setInitialDelay(j3, TimeUnit.MILLISECONDS).addTag(PMUtils.OTAAPP_WORK_TAG).build());
            Logger.debug("OtaApp", "scheduleWorkManager : scheduled work manager for " + ((j3 / 1000) / 60) + " minutes");
        }
    }

    public static void scheduleModemWorkManager(Context context) {
        if (BuildPropReader.isATT()) {
            Logger.debug("OtaApp", "scheduleModemWorkManager:ATT Device and modem, don't schedule polling, return from here");
        } else if (TextUtils.isEmpty(BuildPropReader.getMCFGConfigVersion())) {
            Logger.debug("OtaApp", "scheduleModemWorkManager:Non Qualcomm Device, don't schedule polling, return from here");
        } else {
            BotaSettings botaSettings = settings;
            if (botaSettings.getInt(Configs.MODEM_POLLING_COUNT, 0) >= botaSettings.getInt(Configs.MAX_MODEM_POLLING_COUNT, 7)) {
                Logger.debug("OtaApp", "polling count = " + botaSettings.getInt(Configs.MODEM_POLLING_COUNT, 0) + " : Modem polling count is exceeded, we should wait for MR or OS update to be success to resume the modem polling");
                return;
            }
            long j = botaSettings.getLong(Configs.POLL_MODEM_AFTER, 86400000L);
            long j2 = botaSettings.getLong(Configs.PREV_POLL_MODEM_AFTER, -1L);
            if (j2 == -1) {
                botaSettings.setLong(Configs.PREV_POLL_MODEM_AFTER, j);
            }
            Logger.debug("OtaApp", "scheduleModemWorkManager:repeatInterval=" + j + " : prevRepeatInterval=" + j2);
            if (isUniqueWorkScheduled(context, PMUtils.MODEM_UNIQUE_WORK_NAME) && j == j2) {
                Logger.debug("OtaApp", "scheduleModemWorkManager:Already WorkManager is scheduled with same interval, so don't schedule again");
                return;
            }
            botaSettings.setLong(Configs.PREV_POLL_MODEM_AFTER, j);
            WorkManager workManager = WorkManager.getInstance(context);
            Constraints build = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            workManager.enqueueUniquePeriodicWork(PMUtils.MODEM_UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, new PeriodicWorkRequest.Builder(CheckUpdateWorker.class, j, TimeUnit.MILLISECONDS).setConstraints(build).setInitialDelay(j, TimeUnit.MILLISECONDS).addTag(PMUtils.MODEM_WORK_TAG).build());
            Logger.debug("OtaApp", "scheduleModemWorkManager : scheduled work manager for " + ((j / 1000) / 60) + " minutes");
        }
    }

    public static boolean isUniqueWorkScheduled(Context context, String str) {
        Logger.debug("OtaApp", "isUniqueWorkScheduled:uniqueWorkName=" + str);
        try {
            for (WorkInfo workInfo : (List) WorkManager.getInstance(context).getWorkInfosForUniqueWork(str).get()) {
                if (workInfo.getState() == WorkInfo.State.ENQUEUED) {
                    Logger.debug("OtaApp", "isUniqueWorkScheduled:work scheduled to run next at " + workInfo.getNextScheduleTimeMillis());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception in isUniqueWorkScheduled:msg:" + e);
            return false;
        }
    }

    public static void shutDownPolling(Context context, String str) {
        WorkManager workManager = WorkManager.getInstance(context);
        if (isUniqueWorkScheduled(context, str)) {
            workManager.cancelUniqueWork(str);
            Logger.debug("OtaApp", "shutDownPolling:uniqueWorkName=" + str);
        }
    }

    public static String getMetaSourceSha1() {
        MetaData from = MetaDataBuilder.from(settings.getString(Configs.METADATA));
        if (from != null) {
            return from.getSourceSha1();
        }
        return null;
    }
}
