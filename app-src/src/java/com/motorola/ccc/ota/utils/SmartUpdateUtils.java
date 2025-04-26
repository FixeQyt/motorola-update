package com.motorola.ccc.ota.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtilConstants;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtils;
import com.motorola.ccc.ota.ui.BaseActivity;
import com.motorola.ccc.ota.ui.UpdatePreferenceFragment;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.metaData.CheckForUpgradeTriggeredBy;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class SmartUpdateUtils {
    public static final String ENTERPRISE_EDITION = "enterprise_edition";
    public static final int INTERVAL_HOURS = 2;
    public static final String MASK_SEPARATOR = ":";
    public static final String MASK_SLOT_TIME_MAPPING = "%02d:%02d:%02d:%02d";
    private static final int MAX_TIME_INTERVAL = 5000;
    private static final int MIN_TIME_INTERVAL = 100;
    public static final int SLOT1_END_HOUR = 3;
    public static final int SLOT1_START_HOUR = 1;
    public static final int SLOT2_END_HOUR = 4;
    public static final int SLOT2_START_HOUR = 2;
    public static final int SLOT3_END_HOUR = 5;
    public static final int SLOT3_START_HOUR = 3;
    public static final int SLOT_CUSTOM_END_HOUR = 19;
    public static final int SLOT_CUSTOM_START_HOUR = 17;
    public static final int TIME_30_MINUTES = 30;
    public static final int TIME_ZERO_MINUTES = 0;
    private static long mDoubleBackToExitPressedOnce;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public interface OnSmartUpdateConfigChangedLister {
        void onSmartUpdateConfigChanged();
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public interface SmartDialog {
        void dismissSmartDialog();
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum SmartUpdateBitmap {
        superSmartUpdateControlFlag,
        forceMRUpdate,
        showAdvancedSetting,
        showDNDForPopUp
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum SmartUpdateLaunchMode {
        SMART_UPDATE_SUGGESTIONS,
        UP_TO_DATE_FRAGMENT,
        TRY_SMART_UPDATES_POPUP_DOWNLOAD,
        TRY_SMART_UPDATES_POPUP_INSTALL,
        TRY_SMART_UPDATES_POPUP_RESTART,
        TRY_SMART_UPDATES_POPUP_UPDATE_COMPLETE,
        UPGRADE_PREFERENCE_FRAGMENT,
        SETTINGS_MENU_FRAGMENT
    }

    public static int getValueFromBoolean(boolean z) {
        return z ? 1 : 0;
    }

    public static void dismissSmartDialogs(FragmentActivity fragmentActivity) {
        List<SmartDialog> fragments;
        if (fragmentActivity == null || (fragments = fragmentActivity.getSupportFragmentManager().getFragments()) == null) {
            return;
        }
        for (SmartDialog smartDialog : fragments) {
            if (smartDialog instanceof SmartDialog) {
                smartDialog.dismissSmartDialog();
            }
        }
    }

    public static void storeBits(BotaSettings botaSettings, int i) {
        if (!isSmartUpdateEnabledByUser(botaSettings)) {
            botaSettings.setBoolean(Configs.SMART_UPDATE_MR_FORCED_BY_SERVER, UpdaterUtils.isBitMapSet(i, SmartUpdateBitmap.forceMRUpdate.ordinal()));
        }
        botaSettings.setBoolean(Configs.SMART_UPDATE_ENABLE_BY_SERVER, UpdaterUtils.isBitMapSet(i, SmartUpdateBitmap.superSmartUpdateControlFlag.ordinal()));
        botaSettings.setBoolean(Configs.SMART_UPDATE_ADV_OPT_BY_SERVER, UpdaterUtils.isBitMapSet(i, SmartUpdateBitmap.showAdvancedSetting.ordinal()));
        botaSettings.setBoolean(Configs.SMART_UPDATE_SHOW_DND_POPUP_BY_SERVER, UpdaterUtils.isBitMapSet(i, SmartUpdateBitmap.showDNDForPopUp.ordinal()));
    }

    public static int getOverriddenSmartUpdateBitMap() {
        int valueFromBoolean = (((((getValueFromBoolean(isShowDNDForPopUpEnabledByServer()) << 1) + getValueFromBoolean(isShowAdvancedSettingValueEnabledByServer())) << 1) + getValueFromBoolean(isForcedMRUpdateEnabledByServer())) << 1) + getValueFromBoolean(isSmartUpdateEnabledByServer());
        Logger.debug("OtaApp", "SmartUpdateUtils.getOverriddenSmartUpdateBitMap = " + valueFromBoolean);
        return valueFromBoolean;
    }

    public static String getMappedTimeSlotForPrefs(int i, int i2, int i3, int i4) {
        return String.format(MASK_SLOT_TIME_MAPPING, Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), Integer.valueOf(i4));
    }

    public static String getMappedTimeSlotForPrefs(int i) {
        if (i == 2131296567) {
            return getMappedTimeSlotForPrefs(1, 0, 3, 0);
        }
        if (i == 2131296568) {
            return getMappedTimeSlotForPrefs(2, 0, 4, 0);
        }
        if (i == 2131296569) {
            return getMappedTimeSlotForPrefs(3, 0, 5, 0);
        }
        return null;
    }

    public static String getDefaultTextForTimeSlot(Context context, int i) {
        if (i == 2131296567) {
            return DateFormatUtils.generateTimeSlotContent(context, 1, 0, 3, 0);
        }
        if (i == 2131296568) {
            return DateFormatUtils.generateTimeSlotContent(context, 2, 0, 4, 0);
        }
        if (i == 2131296569) {
            return DateFormatUtils.generateTimeSlotContent(context, 3, 0, 5, 0);
        }
        return DateFormatUtils.generateTimeSlotContent(context, 17, 30, 19, 30);
    }

    public static List<Calendar> parseTimeslot(String str) {
        ArrayList arrayList = new ArrayList();
        if (str != null && str.length() > 0) {
            String[] split = str.split(MASK_SEPARATOR);
            if (split.length == 4) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(11, Integer.parseInt(split[0]));
                calendar.set(12, Integer.parseInt(split[1]));
                arrayList.add(calendar);
                Calendar calendar2 = Calendar.getInstance();
                calendar2.set(11, Integer.parseInt(split[2]));
                calendar2.set(12, Integer.parseInt(split[3]));
                arrayList.add(calendar2);
            }
        }
        return arrayList;
    }

    public static int getTimerValue(BotaSettings botaSettings, UpdaterUtils.UpgradeInfo upgradeInfo) {
        int i = botaSettings.getInt(Configs.DEFAULT_INSTALL_COUNT_DOWN_SECS, 61);
        if (isSmartUpdateNearestToInstall(botaSettings, upgradeInfo.getUpdateTypeData())) {
            return 181;
        }
        return i;
    }

    public static boolean isSmartUpdateEnabledByUser(BotaSettings botaSettings) {
        return botaSettings.getBoolean(Configs.SMART_UPDATE_ENABLE_BY_USER);
    }

    public static boolean isSmartUpdateTimerExpired(BotaSettings botaSettings, UpdaterUtils.UpgradeInfo upgradeInfo) {
        return isSmartUpdateNearestToInstall(botaSettings, upgradeInfo.getUpdateTypeData()) && botaSettings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L) > 0 && System.currentTimeMillis() >= botaSettings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L);
    }

    public static boolean isSmartUpdateNearestToInstall(BotaSettings botaSettings, String str) {
        if (isSmartUpdateEnabledByServer() && isSmartUpdateEnabledByUser(botaSettings) && isSmartUpdateForcedByUpdateType(botaSettings, str) && !smartUpdateReachedMaxDeferTime(botaSettings)) {
            long j = botaSettings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L);
            long j2 = botaSettings.getLong(Configs.MAX_CRITICAL_UPDATE_EXTENDED_TIME, -1L);
            long j3 = botaSettings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L);
            long j4 = botaSettings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L);
            if (j <= -1 || j >= j3) {
                if (j2 <= -1 || j2 >= j3) {
                    return j4 <= -1 || j4 >= j3;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    public static void setSmartUpdateEnableByUser(BotaSettings botaSettings, boolean z) {
        botaSettings.setBoolean(Configs.SMART_UPDATE_ENABLE_BY_USER, z);
    }

    public static void settingInstallTimeIntervalForSmartUpdate(BotaSettings botaSettings) {
        settingInstallTimeIntervalForSmartUpdate(botaSettings, false);
    }

    public static long generateRandomNumber() {
        return new Random().nextInt(6000) * 1000;
    }

    public static void settingInstallTimeIntervalForSmartUpdate(BotaSettings botaSettings, boolean z) {
        Context globalContext = OtaApplication.getGlobalContext();
        List<Calendar> parseTimeslot = parseTimeslot(botaSettings.getString(Configs.SMART_UPDATE_TIME_SLOT));
        if (parseTimeslot.size() != 2) {
            setDefaultTimeIntervalForSmartUpdate(globalContext, botaSettings, z);
            return;
        }
        for (int i = 0; i < parseTimeslot.size(); i++) {
            Calendar calendar = parseTimeslot.get(i);
            int i2 = calendar.get(11);
            int i3 = calendar.get(12);
            if (i2 == -1) {
                Logger.error("OtaApp", "Invalid time format of Smart Update time slot, setting to default time interval");
                setDefaultTimeIntervalForSmartUpdate(globalContext, botaSettings, z);
                return;
            }
            long timestampOfNextHourMinFromNow = UpdaterUtils.getTimestampOfNextHourMinFromNow(i2, i3);
            if (i == 0) {
                if ((i2 == 1 || i2 == 2 || i2 == 3) && i3 == 0) {
                    timestampOfNextHourMinFromNow += generateRandomNumber();
                }
                botaSettings.setLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, timestampOfNextHourMinFromNow);
            } else {
                botaSettings.setLong(Configs.SMART_UPDATE_MAX_INSTALL_TIME, timestampOfNextHourMinFromNow);
            }
        }
        changeSmartUpdateInstallTimestampsOnDeferBased(botaSettings, z);
        Logger.debug("OtaApp", "Smart Update installation set for : min = " + TimeUnit.MILLISECONDS.toMinutes(botaSettings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L) - System.currentTimeMillis()) + " minutes; max = " + TimeUnit.MILLISECONDS.toMinutes(botaSettings.getLong(Configs.SMART_UPDATE_MAX_INSTALL_TIME, -1L) - System.currentTimeMillis()));
    }

    private static void changeSmartUpdateInstallTimestampsOnDeferBased(BotaSettings botaSettings, boolean z) {
        long j = botaSettings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L);
        long j2 = botaSettings.getLong(Configs.SMART_UPDATE_MAX_INSTALL_TIME, -1L);
        if (j2 - j < 0) {
            Logger.debug("OtaApp", "Setting smart update install time stamps in Time Slot");
            if (z) {
                botaSettings.setLong(Configs.SMART_UPDATE_MAX_INSTALL_TIME, j2 + 86400000);
                return;
            }
            botaSettings.setLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, j - 86400000);
        }
    }

    private static void setDefaultTimeIntervalForSmartUpdate(Context context, BotaSettings botaSettings, boolean z) {
        botaSettings.setLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, UpdaterUtils.getTimestampOfNextHourMinFromNow(1, 0) + generateRandomNumber());
        botaSettings.setLong(Configs.SMART_UPDATE_MAX_INSTALL_TIME, UpdaterUtils.getTimestampOfNextHourMinFromNow(3, 0));
        changeSmartUpdateInstallTimestampsOnDeferBased(botaSettings, z);
        Logger.debug("OtaApp", "Set to default Smart update time interval : " + DateFormatUtils.generateTimeSlotContent(context, 1, 0, 3, 0));
    }

    public static boolean isSmartUpdateForcedByUpdateType(BotaSettings botaSettings, String str) {
        if (UpdateType.DIFFUpdateType.SMR.toString().equals(str)) {
            Logger.debug("OtaApp", "isSmartUpdateForcedByUpdateType:SMR update");
            return true;
        }
        if (UpdateType.DIFFUpdateType.MR.toString().equals(str)) {
            if (isForcedMRUpdateEnabledByServer()) {
                Logger.debug("OtaApp", "isSmartUpdateForcedByUpdateType:Forced MR update");
                return true;
            } else if (isShowAdvancedSettingValueEnabledByServer()) {
                Logger.debug("OtaApp", "isSmartUpdateForcedByUpdateType:User enabled MR update " + botaSettings.getBoolean(Configs.SMART_UPDATE_MR_ENABLE_BY_USER));
                return botaSettings.getBoolean(Configs.SMART_UPDATE_MR_ENABLE_BY_USER);
            }
        }
        if (UpdateType.DIFFUpdateType.OS.toString().equals(str) && isForcedMRUpdateEnabledByServer() && isShowAdvancedSettingValueEnabledByServer()) {
            Logger.debug("OtaApp", "isSmartUpdateForcedByUpdateType: User enabled OS update " + botaSettings.getBoolean(Configs.SMART_UPDATE_OS_ENABLE_BY_USER));
            return botaSettings.getBoolean(Configs.SMART_UPDATE_OS_ENABLE_BY_USER);
        }
        Logger.debug("OtaApp", "isSmartUpdateForcedByUpdateType: return false");
        return false;
    }

    public static boolean shouldIForceSmartUpdate(BotaSettings botaSettings, String str) {
        return isSmartUpdateEnabledByUser(botaSettings) && isSmartUpdateEnabledByServer() && isSmartUpdateForcedByUpdateType(botaSettings, str);
    }

    private static boolean getDefaultSmartUpdateFlag(SmartUpdateBitmap smartUpdateBitmap) {
        return UpdaterUtils.isBitMapSet(new BotaSettings().getInt(Configs.DEFAULT_SMART_UPDATE_BITMAP, 7), smartUpdateBitmap.ordinal());
    }

    public static void resetSmartUpdatePrefs(BotaSettings botaSettings) {
        Logger.debug("OtaApp", "Reset SmartUpdate Prefs");
        botaSettings.removeConfig(Configs.SMART_UPDATE_MAX_INSTALL_TIME);
        botaSettings.removeConfig(Configs.SMART_UPDATE_MIN_INSTALL_TIME);
        botaSettings.setString(Configs.SMART_UPDATE_TIME_SLOT, "");
        botaSettings.setBoolean(Configs.SMART_UPDATE_ENABLE_BY_USER, false);
        botaSettings.setBoolean(Configs.SMART_UPDATE_MR_ENABLE_BY_USER, false);
        botaSettings.setBoolean(Configs.SMART_UPDATE_OS_ENABLE_BY_USER, false);
    }

    public static boolean isDownloadForcedForSmartUpdate(BotaSettings botaSettings) {
        String string = botaSettings.getString(Configs.METADATA);
        String updateType = UpdaterUtils.getUpdateType(string);
        MetaData from = MetaDataBuilder.from(string);
        return shouldIForceSmartUpdate(botaSettings, updateType) && !(CheckForUpgradeTriggeredBy.user.name().equals(botaSettings.getString(Configs.TRIGGERED_BY)) || (from != null && CheckForUpgradeTriggeredBy.user.name().equalsIgnoreCase(from.getUpdateReqTriggeredBy())));
    }

    private static long getDownloadOrInstallNotifiedTime(BotaSettings botaSettings) {
        if (BuildPropReader.isUEUpdateEnabled()) {
            return botaSettings.getLong(Configs.STATS_INSTALL_COMPLETED, System.currentTimeMillis());
        }
        return botaSettings.getLong(Configs.STATS_INSTALL_NOTIFIED, System.currentTimeMillis());
    }

    public static boolean smartUpdateReachedMaxDeferTime(BotaSettings botaSettings) {
        long downloadOrInstallNotifiedTime = getDownloadOrInstallNotifiedTime(botaSettings);
        return downloadOrInstallNotifiedTime > -1 && botaSettings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L) >= downloadOrInstallNotifiedTime + UpdaterUtils.WAIT_TIME_AFTER_FORCE_INSTALL_TIMER;
    }

    public static boolean smartUpdateReachedMaxDeferTimeRORNoNetwork(BotaSettings botaSettings) {
        long downloadOrInstallNotifiedTime = getDownloadOrInstallNotifiedTime(botaSettings);
        return downloadOrInstallNotifiedTime > -1 && botaSettings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L) >= downloadOrInstallNotifiedTime + 777600000;
    }

    public static boolean postponeSmartUpdateROR(Context context, BotaSettings botaSettings) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (!UpdaterUtils.isPreparedForUnattendedUpdate() || NetworkUtils.isNetWorkConnected(connectivityManager) || smartUpdateReachedMaxDeferTimeRORNoNetwork(botaSettings)) {
            return false;
        }
        Logger.debug("OtaApp", "Device is prepared for update but no network available, postponing smart update to next day");
        return true;
    }

    public static boolean shouldShowTrySmartUpdatePopUp(BotaSettings botaSettings, String str, boolean z) {
        boolean z2 = botaSettings.getBoolean(Configs.SMART_UPDATE_POP_UP_DISABLE);
        boolean equals = UpdateType.DIFFUpdateType.SMR.toString().equals(str);
        boolean z3 = (z && BuildPropReader.isVerizon()) ? false : true;
        if (isSmartUpdateEnabledByUser(botaSettings) || z2) {
            return false;
        }
        return (equals || isForcedMRUpdateEnabledByServer()) && isSmartUpdateEnabledByServer() && z3;
    }

    public static boolean shouldShowSmartUpdateBottomSheet(BotaSettings botaSettings) {
        return (isSmartUpdateEnabledByUser(botaSettings) || !isSmartUpdateEnabledByServer() || BuildPropReader.isVerizon() || BuildPropReader.isATT()) ? false : true;
    }

    public static void showTrySmartUpdatePopUp(final Context context, String str, UpdateType.UpdateTypeInterface updateTypeInterface, final Activity activity, final BotaSettings botaSettings, final String str2) {
        final int updateSpecificColor = updateTypeInterface.getUpdateSpecificColor();
        View inflate = activity.getLayoutInflater().inflate(R.layout.pop_up_check_box, (ViewGroup) null);
        final CheckBox checkBox = (CheckBox) inflate.findViewById(R.id.pop_up_checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.motorola.ccc.ota.utils.SmartUpdateUtils$$ExternalSyntheticLambda0
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                SmartUpdateUtils.lambda$showTrySmartUpdatePopUp$0(checkBox, context, updateSpecificColor, botaSettings, compoundButton, z);
            }
        });
        if (!isShowDNDForPopUpEnabledByServer()) {
            checkBox.setVisibility(8);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        ((TextView) inflate.findViewById(R.id.pop_up_title)).setText(str);
        builder.setView(inflate);
        builder.setCancelable(false);
        Button button = (Button) inflate.findViewById(R.id.pop_up_btn_positive);
        button.setText(context.getResources().getString(R.string.turn_it_on));
        Button button2 = (Button) inflate.findViewById(R.id.pop_up_btn_negative);
        button2.setText(context.getResources().getString(R.string.not_now));
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() { // from class: com.motorola.ccc.ota.utils.SmartUpdateUtils$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnKeyListener
            public final boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                return SmartUpdateUtils.lambda$showTrySmartUpdatePopUp$1(activity, context, dialogInterface, i, keyEvent);
            }
        });
        final AlertDialog create = builder.create();
        button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.utils.SmartUpdateUtils$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SmartUpdateUtils.lambda$showTrySmartUpdatePopUp$2(activity, botaSettings, context, str2, create, view);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.utils.SmartUpdateUtils$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SmartUpdateUtils.lambda$showTrySmartUpdatePopUp$3(create, botaSettings, activity, view);
            }
        });
        UpdaterUtils.setCornersRounded(context, create);
        botaSettings.incrementPrefs(Configs.STATS_SMART_UPDATE_POPUP_SHOWN_COUNT);
        create.show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$showTrySmartUpdatePopUp$0(CheckBox checkBox, Context context, int i, BotaSettings botaSettings, CompoundButton compoundButton, boolean z) {
        if (z) {
            checkBox.setButtonTintList(ContextCompat.getColorStateList(context, i));
        } else {
            checkBox.setButtonTintList(ContextCompat.getColorStateList(context, (int) R.color.common_signin_btn_light_text_default));
        }
        botaSettings.setBoolean(Configs.SMART_UPDATE_POP_UP_DISABLE, z);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ boolean lambda$showTrySmartUpdatePopUp$1(Activity activity, Context context, DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
        if (i == 4) {
            if (mDoubleBackToExitPressedOnce + 100 < System.currentTimeMillis() && mDoubleBackToExitPressedOnce + 5000 > System.currentTimeMillis()) {
                dialogInterface.cancel();
                activity.finish();
            } else {
                mDoubleBackToExitPressedOnce = System.currentTimeMillis();
                Toast.makeText(context, context.getResources().getString(R.string.press_back_again_to_exit), 0).show();
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$showTrySmartUpdatePopUp$2(Activity activity, BotaSettings botaSettings, Context context, String str, AlertDialog alertDialog, View view) {
        activity.finish();
        botaSettings.incrementPrefs(Configs.STATS_SMART_UPDATE_POP_UP_GET_STARTED);
        Intent intent = activity.getIntent();
        Intent intent2 = new Intent(intent.getAction());
        intent2.setClass(context, BaseActivity.class);
        intent2.putExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE, intent.getStringExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE));
        intent2.putExtra(UpgradeUtilConstants.KEY_LAUNCH_MODE, str);
        intent2.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.SMART_UPDATE_FRAGMENT.toString());
        context.startActivity(intent2);
        alertDialog.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$showTrySmartUpdatePopUp$3(AlertDialog alertDialog, BotaSettings botaSettings, Activity activity, View view) {
        alertDialog.dismiss();
        botaSettings.incrementPrefs(Configs.STATS_SMART_UPDATE_POP_UP_NOT_NOW);
        activity.finish();
    }

    public static void turnSmartUpdateOn(String str) {
        sendSmartUpdateConfigChangedIntent(OtaApplication.getGlobalContext(), true);
        BotaSettings botaSettings = new BotaSettings();
        setSmartUpdateEnableByUser(botaSettings, true);
        if (TextUtils.isEmpty(botaSettings.getString(Configs.SMART_UPDATE_TIME_SLOT))) {
            botaSettings.setString(Configs.SMART_UPDATE_TIME_SLOT, getMappedTimeSlotForPrefs(R.id.radioSlot1));
        }
        settingInstallTimeIntervalForSmartUpdate(botaSettings);
        botaSettings.setString(Configs.STATS_SMART_UPDATE_ENABLED_VIA, str);
        botaSettings.removeConfig(Configs.STATS_DISABLED_SMART_UPDATE_AFTER_INSTALL);
    }

    public static void turnSmartUpdateOff(String str) {
        sendSmartUpdateConfigChangedIntent(OtaApplication.getGlobalContext(), false);
        BotaSettings botaSettings = new BotaSettings();
        setSmartUpdateEnableByUser(botaSettings, false);
        resetSmartUpdatePrefs(botaSettings);
        if (botaSettings.getBoolean(Configs.STATS_INSTALL_VIA_SMART_UPDATE)) {
            botaSettings.setBoolean(Configs.STATS_DISABLED_SMART_UPDATE_AFTER_INSTALL, true);
        }
        botaSettings.setString(Configs.STATS_SMART_UPDATE_DISABLED_VIA, str);
    }

    public static void decideToShowSmartUpdateSuggestion(Context context) {
        boolean isSmartUpdateEnabledByUser = isSmartUpdateEnabledByUser(new BotaSettings());
        boolean isSmartUpdateEnabledByServer = isSmartUpdateEnabledByServer();
        if (!UpdaterUtils.isVerizon() && isSmartUpdateEnabledByServer && !isSmartUpdateEnabledByUser) {
            enableSmartUpdateSuggestion(context);
        } else {
            disableSmartUpdateSuggestion(context);
        }
    }

    public static void enableSmartUpdateSuggestion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Logger.debug("OtaApp", "SmartUpdateUtils.enableOTASuggestion");
        packageManager.setComponentEnabledSetting(new ComponentName(context, "com.motorola.ccc.ota.OTASuggestion"), 1, 1);
    }

    public static void disableSmartUpdateSuggestion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Logger.debug("OtaApp", "SmartUpdateUtils.disableOTASuggestion");
        packageManager.setComponentEnabledSetting(new ComponentName(context, "com.motorola.ccc.ota.OTASuggestion"), 2, 1);
    }

    public static boolean isForcedMRUpdateEnabledByServer() {
        return new BotaSettings().getBoolean(Configs.SMART_UPDATE_MR_FORCED_BY_SERVER, getDefaultSmartUpdateFlag(SmartUpdateBitmap.forceMRUpdate));
    }

    public static boolean isSmartUpdateEnabledByServer() {
        BotaSettings botaSettings = new BotaSettings();
        if (BuildPropReader.isATT() || canSmartUpdateBlockForSystemUpdatePolicy()) {
            Logger.debug("OtaApp", "isSmartUpdateEnabledByServer: smart update feature is disabled for Fota/Softbank/Sys Policy");
            return false;
        }
        return botaSettings.getBoolean(Configs.SMART_UPDATE_ENABLE_BY_SERVER, getDefaultSmartUpdateFlag(SmartUpdateBitmap.superSmartUpdateControlFlag));
    }

    private static boolean canSmartUpdateBlockForSystemUpdatePolicy() {
        SystemUpdaterPolicy systemUpdaterPolicy = new SystemUpdaterPolicy();
        return isMotoSettingsGlobalEnterpriseEditionFlagSet() || systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet() || systemUpdaterPolicy.isOtaUpdateDisabledByPolicyMngr() || systemUpdaterPolicy.getPolicyType() > 0 || ((Boolean) ThinkShieldUtils.getASCCampaignStatusDetails(OtaApplication.getGlobalContext()).get(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS)).booleanValue();
    }

    public static boolean isShowAdvancedSettingValueEnabledByServer() {
        return new BotaSettings().getBoolean(Configs.SMART_UPDATE_ADV_OPT_BY_SERVER, getDefaultSmartUpdateFlag(SmartUpdateBitmap.showAdvancedSetting));
    }

    public static boolean isShowDNDForPopUpEnabledByServer() {
        return new BotaSettings().getBoolean(Configs.SMART_UPDATE_SHOW_DND_POPUP_BY_SERVER, getDefaultSmartUpdateFlag(SmartUpdateBitmap.showDNDForPopUp));
    }

    public static AlertDialog getAndShowAreYouSurePopUp(Context context, String str, String str2, Activity activity, final DialogInterface.OnClickListener onClickListener, final DialogInterface.OnClickListener onClickListener2) {
        View inflate = activity.getLayoutInflater().inflate(R.layout.pop_up_check_box, (ViewGroup) null);
        ((CheckBox) inflate.findViewById(R.id.pop_up_checkbox)).setVisibility(8);
        ((TextView) inflate.findViewById(R.id.pop_up_message)).setText(str2);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(inflate);
        ((TextView) inflate.findViewById(R.id.pop_up_title)).setText(str);
        builder.setCancelable(true);
        Button button = (Button) inflate.findViewById(R.id.pop_up_btn_positive);
        button.setText(context.getResources().getString(R.string.keep_it_on));
        Button button2 = (Button) inflate.findViewById(R.id.pop_up_btn_negative);
        button2.setText(context.getResources().getString(R.string.turn_it_off));
        final AlertDialog create = builder.create();
        button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.utils.SmartUpdateUtils$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SmartUpdateUtils.lambda$getAndShowAreYouSurePopUp$4(onClickListener, create, view);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.utils.SmartUpdateUtils$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SmartUpdateUtils.lambda$getAndShowAreYouSurePopUp$5(onClickListener2, create, view);
            }
        });
        UpdaterUtils.setCornersRounded(context, create);
        create.show();
        return create;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$getAndShowAreYouSurePopUp$4(DialogInterface.OnClickListener onClickListener, AlertDialog alertDialog, View view) {
        if (onClickListener != null) {
            onClickListener.onClick(alertDialog, -1);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static /* synthetic */ void lambda$getAndShowAreYouSurePopUp$5(DialogInterface.OnClickListener onClickListener, AlertDialog alertDialog, View view) {
        if (onClickListener != null) {
            onClickListener.onClick(alertDialog, -2);
        }
    }

    public static void launchUpdatePreferences(Intent intent, FragmentManager fragmentManager, boolean z) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        UpdatePreferenceFragment updatePreferenceFragment = new UpdatePreferenceFragment();
        updatePreferenceFragment.setArguments(bundle);
        if (z) {
            updatePreferenceFragment.show(fragmentManager, "updatePreferenceFragment");
        }
    }

    public static boolean isMotoSettingsGlobalEnterpriseEditionFlagSet() {
        if ("1".equals(BuildPropReader.getMotoSettingValueAsString(OtaApplication.getGlobalContext(), ENTERPRISE_EDITION))) {
            Logger.debug("OtaApp", "isMotoSettingsGlobalEnterpriseEditionFlagSet:EnterpriseFlag is set:return true");
            return true;
        }
        return false;
    }

    public static void sendSmartUpdateConfigChangedIntent(Context context, boolean z) {
        Intent intent = new Intent(UpgradeUtilConstants.ACTION_SMART_UPDATE_CONFIG_CHANGED);
        intent.putExtra(UpgradeUtilConstants.KEY_SMART_UPDATE_ENABLED, z);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }
}
