package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.SpannableString;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.CusUtilMethods;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.utils.BroadcastUtils;
import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class RestartFragment extends Fragment implements SmartUpdateUtils.OnSmartUpdateConfigChangedLister {
    private static int INSTALL_COUNTDOWN_IN_SECONDS;
    private static boolean isSmartUpdateTimerRunning;
    private static boolean isTimerRunning;
    private static PreInstallCounter mPreInstallCounter;
    private Activity activity;
    private Intent activityIntent;
    private Context context;
    private ExpandableListView expandableListView;
    private ImageView imgBack;
    private long launchTimeInMillis;
    private TextView mAbRestartCriticalPrompt;
    private AlarmManager mAlarmManager;
    private AlertDialog mAlert;
    private CriticalUpdate mCriticalUpdate;
    private boolean mDoNotShowNotification;
    private TextView mEmergencyCallText;
    private TextView mExpTitle;
    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.RestartFragment.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (UpgradeUtilConstants.FINISH_RESTART_ACTIVITY.equals(intent.getAction())) {
                Logger.debug("OtaApp", "RestartFragment:mFinishReceiver, finish restart fragment");
                RestartFragment.this.mDoNotShowNotification = true;
                RestartFragment.this.activity.finish();
            }
        }
    };
    private LinearLayout mInstructionLayout;
    private TextView mInstructionTitle;
    private String mLocationType;
    private String mMinVersion;
    private long mNextPrompt;
    private PendingIntent mPendingIntent;
    private TextView mPreInstallNotes;
    private int mPromptCount;
    private Button mRebootLater;
    private Button mRebootNow;
    private Button mRestartCancel;
    private Intent mTargetIntent;
    private UpdateType.UpdateTypeInterface mUpdateType;
    private UpdaterUtils.UpgradeInfo mUpgradeInfo;
    private TextView mVersion;
    private TextView mWarningText;
    private TextView osReleaseNotes;
    private View rootView;
    private BotaSettings settings;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof Activity) {
            this.activity = (Activity) context;
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        BotaSettings botaSettings = new BotaSettings();
        this.settings = botaSettings;
        this.mPromptCount = botaSettings.getInt(Configs.STATS_RESTART_ACTIVITY_PROMPT_COUNT, 0);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.FINISH_RESTART_ACTIVITY);
        BroadcastUtils.registerLocalReceiver(this.context, this.mFinishReceiver, intentFilter);
        this.mAlarmManager = (AlarmManager) this.context.getSystemService("alarm");
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.activityIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.rootView = layoutInflater.inflate(R.layout.restart, viewGroup, false);
        findViewsById();
        this.mWarningText.setText(getRestartWarningText());
        handleButtons();
        return this.rootView;
    }

    private String getRestartWarningText() {
        long j = this.settings.getLong(Configs.RESTART_EXPIRY_TIMER, -1L);
        long currentTimeMillis = System.currentTimeMillis();
        int i = (j <= 0 || j >= currentTimeMillis) ? 0 : (int) (((currentTimeMillis - j) / 86400000) + 15);
        return ((long) i) > 15 ? this.context.getString(R.string.ab_restart_fullscreen_text, Integer.valueOf(i)) : this.context.getString(R.string.ab_restart_text);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Intent targetIntent = getTargetIntent(this.activityIntent);
        this.mTargetIntent = targetIntent;
        this.mPendingIntent = PendingIntent.getBroadcast(this.context, 0, UpdaterUtils.fillAnnoyValueExpiryDetails(NotificationUtils.KEY_RESTART, targetIntent), 335544320);
        UpdaterUtils.disableBatteryStatusReceiver();
        this.mUpgradeInfo = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(this.activityIntent);
        this.mMinVersion = this.activityIntent.getStringExtra(UpgradeUtilConstants.KEY_VERSION);
        this.mLocationType = this.activityIntent.getStringExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE);
        if (this.mUpgradeInfo == null) {
            Logger.error("OtaApp", "RestartFragment, No upgradeInfo found.");
            this.activity.finishAndRemoveTask();
            return;
        }
        this.mCriticalUpdate = new CriticalUpdate(this.mUpgradeInfo);
        this.mUpdateType = UpdateType.getUpdateType(this.mUpgradeInfo.getUpdateTypeData());
        if (!UpdaterUtils.isPromptAllowed(this.context, NotificationUtils.KEY_RESTART)) {
            Logger.info("OtaApp", "Restart postponed by phone call/ECB");
            this.activity.finish();
            return;
        }
        setVersionOTA(this.mUpgradeInfo);
        Window window = this.activity.getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.setStatusBarColor(0);
        if (!UpdaterUtils.isBatterySaverEnabled(this.context)) {
            WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
            if (((UiModeManager) this.context.getSystemService("uimode")).getNightMode() != 2) {
                windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
                windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
            }
        }
        setTextViewTitle(this.mUpdateType.getToolbarTitle());
        ExpandableListView expandableList = UpdaterUtils.setExpandableList(this.expandableListView, this.mUpgradeInfo.hasReleaseNotes(), this.mUpgradeInfo.getReleaseNotes(), this.mExpTitle, getActivity(), this.mUpdateType, this.rootView.findViewById(R.id.dash_line));
        this.expandableListView = expandableList;
        this.expandableListView = UpdaterUtils.handleExpList(expandableList, "restart");
        UpdaterUtils.setOsReleaseNotes(this.context, this.osReleaseNotes, this.mUpgradeInfo, "restart");
        if (this.mUpgradeInfo.hasPreInstallInstructions()) {
            UpdaterUtils.setInstructions(this.mUpgradeInfo.getPreInstallInstructions(), this.mUpgradeInfo.hasPreInstallInstructions(), this.mInstructionLayout, getActivity(), this.mUpdateType);
            this.mInstructionLayout.setVisibility(0);
            this.mInstructionTitle.setVisibility(0);
        }
        if (this.mUpgradeInfo.hasPreInstallNotes()) {
            try {
                this.mPreInstallNotes.setText(Html.fromHtml(this.mUpgradeInfo.getPreInstallNotes(), 0, null, new HtmlUtils(this.mUpgradeInfo.getPreInstallNotes())));
                this.mPreInstallNotes.setVisibility(0);
                this.mPreInstallNotes.setMovementMethod(LinkMovementMethod.getInstance());
            } catch (Exception e) {
                this.mPreInstallNotes.setVisibility(8);
                Logger.error("OtaApp", "RestartFragment, error setting PreInstallNotes." + e);
            }
        } else {
            this.mPreInstallNotes.setVisibility(8);
        }
        initializeViewAndSaveNextPrompt();
    }

    private void initializeViewAndSaveNextPrompt() {
        this.mNextPrompt = UpdaterUtils.getNextPromptForActivity(this.mUpgradeInfo, this.mPromptCount);
        Logger.debug("OtaApp", "initializeViewAndSaveNextPrompt:isTimerRunning=" + isTimerRunning);
        if (!isTimerRunning) {
            INSTALL_COUNTDOWN_IN_SECONDS = SmartUpdateUtils.getTimerValue(this.settings, this.mUpgradeInfo);
        }
        initializeView();
        if (isAutoUpdateTimerExpired(this.mUpgradeInfo)) {
            showNormalUIForRestartTimer();
            return;
        }
        Logger.debug("OtaApp", "Next restart reminder at status bar scheduled in: " + TimeUnit.MILLISECONDS.toMinutes(this.mNextPrompt - System.currentTimeMillis()) + " mins");
        this.mAlarmManager.cancel(this.mPendingIntent);
        this.mAlarmManager.setExactAndAllowWhileIdle(0, this.mNextPrompt, this.mPendingIntent);
        UpdaterUtils.saveNextPrompt(this.mNextPrompt);
    }

    private void initializeView() {
        this.mAbRestartCriticalPrompt.setVisibility(8);
        if (this.mCriticalUpdate.isCriticalUpdate() && BuildPropReader.isATT()) {
            initViewForFOTAUpgrade();
        } else if (isTimerRunning) {
        } else {
            int i = AnonymousClass6.$SwitchMap$com$motorola$ccc$ota$ui$UpdaterUtils$InternalUpdateType[UpdaterUtils.getInternalUpdateType(this.mUpgradeInfo).ordinal()];
            if (i == 1) {
                initViewForCriticalUpdate();
            } else if (i == 2) {
                initViewForForceUpdate();
            } else if (i != 3) {
            } else {
                initViewForSmartUpdate();
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.ui.RestartFragment$6  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static /* synthetic */ class AnonymousClass6 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$ccc$ota$ui$UpdaterUtils$InternalUpdateType;

        static {
            int[] iArr = new int[UpdaterUtils.InternalUpdateType.values().length];
            $SwitchMap$com$motorola$ccc$ota$ui$UpdaterUtils$InternalUpdateType = iArr;
            try {
                iArr[UpdaterUtils.InternalUpdateType.CRITICAL_UPDATE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$ui$UpdaterUtils$InternalUpdateType[UpdaterUtils.InternalUpdateType.FORCE_UPDATE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$ui$UpdaterUtils$InternalUpdateType[UpdaterUtils.InternalUpdateType.SMART_UPDATE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public void onResume() {
        super.onResume();
        this.launchTimeInMillis = System.currentTimeMillis();
        UpdaterUtils.stopMessageActivity(this.context);
        NotificationUtils.stopNotificationService(this.context);
    }

    public void onStop() {
        super.onStop();
        this.settings.setLong(Configs.TOTAL_TIME_SPEND_ON_RESTART_SCREEN, this.settings.getLong(Configs.TOTAL_TIME_SPEND_ON_RESTART_SCREEN, 0L) + (System.currentTimeMillis() - this.launchTimeInMillis));
        UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(this.activityIntent);
        this.mUpgradeInfo = upgradeInfoDuringOTAUpdate;
        if (upgradeInfoDuringOTAUpdate == null) {
            return;
        }
        if (SmartUpdateUtils.isSmartUpdateNearestToInstall(this.settings, upgradeInfoDuringOTAUpdate.getUpdateTypeData())) {
            this.mDoNotShowNotification = true;
            Logger.debug("OtaApp", "User deferred - no notification for smart update");
            UpdaterUtils.setDeferStats(this.context, UpgradeUtilConstants.RESTART, false, this.mNextPrompt);
        } else if (UpdaterUtils.isDeviceLocked(this.context) || this.mDoNotShowNotification) {
        } else {
            showRestartSystemNotification();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.settings.incrementPrefs(Configs.STATS_RESTART_ACTIVITY_PROMPT_COUNT);
        Logger.debug("OtaApp", "Restart fragment prompt count " + this.settings.getInt(Configs.STATS_RESTART_ACTIVITY_PROMPT_COUNT, 0));
        BroadcastUtils.unregisterLocalReceiver(this.context, this.mFinishReceiver);
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() { // from class: com.motorola.ccc.ota.ui.RestartFragment$$ExternalSyntheticLambda0
            @Override // android.view.View.OnKeyListener
            public final boolean onKey(View view2, int i, KeyEvent keyEvent) {
                return RestartFragment.this.m229lambda$onViewCreated$0$commotorolacccotauiRestartFragment(view2, i, keyEvent);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$onViewCreated$0$com-motorola-ccc-ota-ui-RestartFragment  reason: not valid java name */
    public /* synthetic */ boolean m229lambda$onViewCreated$0$commotorolacccotauiRestartFragment(View view, int i, KeyEvent keyEvent) {
        if (i == 4) {
            if (this.mPromptCount >= 5) {
                showTrySmartUpdatePopUp();
                return true;
            }
            this.activity.finish();
            return true;
        }
        return false;
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.OnSmartUpdateConfigChangedLister
    public void onSmartUpdateConfigChanged() {
        initializeViewAndSaveNextPrompt();
    }

    private void findViewsById() {
        this.mVersion = (TextView) this.rootView.findViewById(R.id.ab_install_version);
        this.mRebootNow = (Button) this.rootView.findViewById(R.id.restart_now);
        this.mPreInstallNotes = (TextView) this.rootView.findViewById(R.id.restartoptionsnotes);
        this.mRebootLater = (Button) this.rootView.findViewById(R.id.restart_later);
        this.mRestartCancel = (Button) this.rootView.findViewById(R.id.restart_cancel);
        this.mWarningText = (TextView) this.rootView.findViewById(R.id.ab_restart_warn_text);
        this.mEmergencyCallText = (TextView) this.rootView.findViewById(R.id.emergency_call_text);
        this.mAbRestartCriticalPrompt = (TextView) this.rootView.findViewById(R.id.ab_restart_critical_prompt);
        this.mExpTitle = (TextView) this.rootView.findViewById(R.id.expTitle);
        this.expandableListView = (ExpandableListView) this.rootView.findViewById(R.id.expandableListView);
        this.osReleaseNotes = (TextView) this.rootView.findViewById(R.id.os_release_notes);
        this.mInstructionTitle = (TextView) this.rootView.findViewById(R.id.instruction_title);
        this.mInstructionLayout = (LinearLayout) this.rootView.findViewById(R.id.instruction);
        this.imgBack = (ImageView) this.rootView.findViewById(R.id.imgBack);
    }

    private void handleButtons() {
        this.mRebootNow.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.RestartFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                RestartFragment.this.mCriticalUpdate.setInstallStats();
                RestartFragment.this.proceedWithReboot();
            }
        });
        this.mRebootLater.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.RestartFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                UpdaterUtils.setDeferStats(OtaApplication.getGlobalContext(), UpgradeUtilConstants.RESTART, false, TimeUnit.MILLISECONDS.toMinutes(RestartFragment.this.mNextPrompt - System.currentTimeMillis()) + 1);
                RestartFragment.this.mDoNotShowNotification = true;
                if (RestartFragment.this.getResources().getString(R.string.restart_after_one_hour).equals(RestartFragment.this.mRebootLater.getText())) {
                    RestartFragment.this.handleCriticalUpdateExtendRestart();
                    return;
                }
                if (!BuildPropReader.isATT()) {
                    RestartFragment.this.mDoNotShowNotification = false;
                }
                if (RestartFragment.this.mPromptCount >= 5) {
                    RestartFragment.this.showTrySmartUpdatePopUp();
                } else {
                    RestartFragment.this.activity.finish();
                }
            }
        });
        this.mRestartCancel.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.RestartFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                RestartFragment.this.handleRestartCancelOrPostponeToNextDay();
            }
        });
        this.imgBack.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.RestartFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                RestartFragment.this.activity.finish();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showTrySmartUpdatePopUp() {
        if (SmartUpdateUtils.shouldShowTrySmartUpdatePopUp(this.settings, this.mUpgradeInfo.getUpdateTypeData(), true)) {
            SmartUpdateUtils.showTrySmartUpdatePopUp(getContext(), getContext().getResources().getString(R.string.smart_update_recommendation), this.mUpdateType, this.activity, this.settings, SmartUpdateUtils.SmartUpdateLaunchMode.TRY_SMART_UPDATES_POPUP_RESTART.name());
            return;
        }
        this.activity.finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleRestartCancelOrPostponeToNextDay() {
        if (SmartUpdateUtils.isSmartUpdateEnabledByUser(this.settings)) {
            this.settings.incrementPrefs(Configs.STATS_SMART_UPDATE_DEFERRED_ON_RESTART);
            SmartUpdateUtils.settingInstallTimeIntervalForSmartUpdate(this.settings, true);
        }
        stopRestartTimer();
        initializeViewAndSaveNextPrompt();
        if (UpdaterUtils.InternalUpdateType.DEFAULT_UPDATE == UpdaterUtils.getInternalUpdateType(this.mUpgradeInfo) || UpdaterUtils.InternalUpdateType.FORCE_UPDATE == UpdaterUtils.getInternalUpdateType(this.mUpgradeInfo)) {
            return;
        }
        Context context = this.context;
        Toast.makeText(context, getString(R.string.smart_cancel_update_toast_msg, new Object[]{DateFormatUtils.getCalendarString(context, this.mNextPrompt)}), 1).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleCriticalUpdateExtendRestart() {
        stopRestartTimer();
        this.mCriticalUpdate.setExtendRestartTime(UpgradeUtilConstants.ONE_HOUR);
        this.mDoNotShowNotification = false;
        initializeViewAndSaveNextPrompt();
        Context context = this.context;
        showRestartPostponedDialog(context.getString(R.string.critical_restart_message_popup, DateFormatUtils.getCalendarString(context, this.mCriticalUpdate.getExtendRestartTime())));
    }

    private void showRestartPostponedDialog(String str) {
        final Dialog dialog = new Dialog(this.context);
        dialog.setContentView(R.layout.restart_later);
        ((TextView) dialog.findViewById(R.id.dialog_message)).setText(str);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.corner_rounded));
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = -2;
        layoutParams.height = -2;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.show();
        ((Button) dialog.findViewById(R.id.dialog_ok)).setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.RestartFragment$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.motorola.ccc.ota.ui.RestartFragment$$ExternalSyntheticLambda2
            @Override // android.content.DialogInterface.OnCancelListener
            public final void onCancel(DialogInterface dialogInterface) {
                RestartFragment.this.m230lambda$showRestartPostponedDialog$2$commotorolacccotauiRestartFragment(dialogInterface);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$showRestartPostponedDialog$2$com-motorola-ccc-ota-ui-RestartFragment  reason: not valid java name */
    public /* synthetic */ void m230lambda$showRestartPostponedDialog$2$commotorolacccotauiRestartFragment(DialogInterface dialogInterface) {
        this.activity.finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void proceedWithReboot() {
        if (UpdaterUtils.isPriorityAppRunning(this.context)) {
            UpdaterUtils.priorityAppRunningPostponeActivity(this.context, NotificationUtils.KEY_RESTART, this.activityIntent, true);
        } else if (UpdaterUtils.isPromptAllowed(this.context, NotificationUtils.KEY_RESTART)) {
            Logger.info("OtaApp", "OTA restart accepted by user!");
            this.mDoNotShowNotification = true;
            this.mAlarmManager.cancel(this.mPendingIntent);
            UpgradeUtilMethods.sendUpgradeLaunchProceed(this.context, this.activityIntent.getStringExtra(UpgradeUtilConstants.KEY_VERSION), true, UpdaterUtils.getInstallModeStats());
        }
        NotificationUtils.clearNextPromptDetails(this.settings);
        mPreInstallCounter = null;
        this.activity.finish();
    }

    private void stopRestartTimer() {
        if (mPreInstallCounter == null) {
            return;
        }
        this.mRestartCancel.setVisibility(8);
        this.mRebootNow.setText(R.string.restart_now);
        this.mRebootLater.setVisibility(0);
        this.mWarningText.setText(getRestartWarningText());
        mPreInstallCounter.stop();
        mPreInstallCounter = null;
    }

    private boolean isAutoUpdateTimerExpired(UpdaterUtils.UpgradeInfo upgradeInfo) {
        if (isTimerRunning) {
            return true;
        }
        if (!SmartUpdateUtils.isSmartUpdateTimerExpired(this.settings, this.mUpgradeInfo)) {
            return new CriticalUpdate(upgradeInfo).isCriticalUpdateTimerExpired() && this.mCriticalUpdate.isOutsideCriticalUpdateExtendedTime();
        } else if (this.settings.getLong(Configs.SMART_UPDATE_MAX_INSTALL_TIME, -1L) > 0 && System.currentTimeMillis() >= this.settings.getLong(Configs.SMART_UPDATE_MAX_INSTALL_TIME, -1L)) {
            SmartUpdateUtils.settingInstallTimeIntervalForSmartUpdate(this.settings);
            initializeView();
            Logger.debug("OtaApp", "Oh..now it's beyond of smart update max time, so scheduling to next day smart update selected time slot");
            return false;
        } else {
            UpdaterUtils.sendInstallModeStats("installedViaSmartUpdate");
            return true;
        }
    }

    private void showFirstNetUIForCritical() {
        this.mRebootLater.setVisibility(8);
        this.mRebootNow.setText(getResources().getString(R.string.restart_now));
    }

    private void showNormalUIForRestartTimer() {
        this.mRebootLater.setVisibility(8);
        this.mRebootNow.setText(getResources().getString(R.string.restart_now));
        this.mAbRestartCriticalPrompt.setVisibility(8);
        if (isSmartUpdateTimerRunning || SmartUpdateUtils.isSmartUpdateNearestToInstall(this.settings, this.mUpgradeInfo.getUpdateTypeData())) {
            if (SmartUpdateUtils.postponeSmartUpdateROR(this.context, this.settings)) {
                handleRestartCancelOrPostponeToNextDay();
                return;
            }
            isSmartUpdateTimerRunning = true;
            this.mRestartCancel.setVisibility(0);
            setTextViewTitle(this.mUpdateType.getToolbarTitle());
        } else if (this.mUpgradeInfo.isCriticalUpdate()) {
            if (CusUtilMethods.isItFirstNetOnFota(this.context)) {
                showFirstNetUIForCritical();
                return;
            }
            if (this.mCriticalUpdate.shouldAllowExtendedCriticalUpdate()) {
                this.mRebootLater.setText(getResources().getString(R.string.restart_after_one_hour));
                this.mRebootLater.setVisibility(0);
            }
            if (!BuildPropReader.isATT()) {
                setTextViewTitle(getResources().getString(R.string.critical_update_notification_title));
            }
        }
        dismissAlert();
        refreshOrStartInstallCounter();
    }

    private void initViewForFOTAUpgrade() {
        this.mRebootLater.setVisibility(0);
        this.mEmergencyCallText.setVisibility(0);
        long j = this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L);
        if (j == -1 || CusUtilMethods.isItFirstNetOnFota(this.context)) {
            return;
        }
        this.mAbRestartCriticalPrompt.setVisibility(0);
        this.mAbRestartCriticalPrompt.setText(getResources().getString(R.string.critical_restart_message_screen, DateFormatUtils.getCalendarString(this.context, j)));
    }

    private void setTextViewTitle(String str) {
        ((TextView) this.rootView.findViewById(R.id.head_text)).setText(str);
    }

    private void initViewForCriticalUpdate() {
        setTextViewTitle(getResources().getString(R.string.critical_update_notification_title));
        String maxUpdateCalendarString = this.mCriticalUpdate.getMaxUpdateCalendarString(this.context);
        this.mAbRestartCriticalPrompt.setVisibility(0);
        if (this.mCriticalUpdate.isCriticalUpdateTimerExpired()) {
            if (!this.mCriticalUpdate.isOutsideCriticalUpdateExtendedTime()) {
                this.mAbRestartCriticalPrompt.setText(getResources().getString(R.string.critical_restart_message_screen, DateFormatUtils.getCalendarString(this.context, this.mCriticalUpdate.getExtendRestartTime())));
            } else {
                this.mAbRestartCriticalPrompt.setText(getResources().getString(R.string.important_update_restart_overdue_message));
            }
        } else {
            this.mAbRestartCriticalPrompt.setText(getResources().getString(R.string.critical_restart_message_screen, maxUpdateCalendarString));
        }
        if (this.mCriticalUpdate.shouldDisplayPopUp(this.context)) {
            AlertDialog alertDialog = this.mAlert;
            if (alertDialog != null && alertDialog.isShowing()) {
                this.mAlert.dismiss();
            }
            AlertDialog buildPopUp = this.mCriticalUpdate.buildPopUp(this.context, getResources().getString(R.string.restart_title), getResources().getString(R.string.critical_restart_message_popup, this.mCriticalUpdate.getMaxUpdateCalendarString(this.context)));
            this.mAlert = buildPopUp;
            buildPopUp.show();
        }
    }

    private void initViewForForceUpdate() {
        this.mAbRestartCriticalPrompt.setText(getResources().getString(R.string.force_restart_screen_message, DateFormatUtils.getCalendarDate(this.settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L))));
        this.mAbRestartCriticalPrompt.setVisibility(0);
    }

    private void initViewForSmartUpdate() {
        this.mAbRestartCriticalPrompt.setVisibility(0);
        this.mAbRestartCriticalPrompt.setText(getResources().getString(R.string.smart_restart_message_popup, DateFormatUtils.getCalendarString(getContext(), this.settings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L))));
    }

    private void dismissAlert() {
        AlertDialog alertDialog = this.mAlert;
        if (alertDialog == null || !alertDialog.isShowing()) {
            return;
        }
        this.mAlert.dismiss();
        this.mAlert = null;
    }

    private void setVersionOTA(UpdaterUtils.UpgradeInfo upgradeInfo) {
        this.mVersion.setText(getString(R.string.download_version, new Object[]{upgradeInfo.getDisplayVersion(), getResources().getString(R.string.total_download_size, Formatter.formatFileSize(getContext(), upgradeInfo.getSize()))}));
    }

    private void showRestartSystemNotification() {
        int preInstallNotificationExpiryMins;
        this.mAlarmManager.cancel(this.mPendingIntent);
        if (this.mCriticalUpdate.isCriticalUpdate()) {
            if (mPreInstallCounter != null) {
                return;
            }
            preInstallNotificationExpiryMins = ((int) TimeUnit.MILLISECONDS.toMinutes(this.mNextPrompt - System.currentTimeMillis())) + 1;
            long j = this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L);
            if (j > 0 && System.currentTimeMillis() >= j && CusUtilMethods.isItFirstNetOnFota(this.context)) {
                preInstallNotificationExpiryMins = -1;
            }
        } else {
            preInstallNotificationExpiryMins = NotificationUtils.getPreInstallNotificationExpiryMins(this.mUpgradeInfo);
            this.mNextPrompt = System.currentTimeMillis() + (preInstallNotificationExpiryMins * 60 * 1000);
            if (this.mUpgradeInfo.isForceInstallTimeSet()) {
                long j2 = this.settings.getLong(Configs.MAX_FORCE_INSTALL_DEFER_TIME, -1L);
                if (this.mUpgradeInfo.isForceInstallTimerExpired()) {
                    j2 += UpdaterUtils.WAIT_TIME_AFTER_FORCE_INSTALL_TIMER;
                }
                long minutes = TimeUnit.MILLISECONDS.toMinutes(j2 - System.currentTimeMillis()) + 1;
                if (minutes < preInstallNotificationExpiryMins) {
                    this.mNextPrompt = System.currentTimeMillis() + (60000 * minutes);
                    preInstallNotificationExpiryMins = (int) minutes;
                    Logger.debug("OtaApp", "RestartFragment.showRestartSystemNotification: Force install delay changed: " + preInstallNotificationExpiryMins);
                }
            }
            UpdaterUtils.saveNextPrompt(this.mNextPrompt);
        }
        NotificationUtils.startNotificationService(OtaApplication.getGlobalContext(), NotificationUtils.fillRestartSystemNotificationDetails(OtaApplication.getGlobalContext(), this.mMinVersion, preInstallNotificationExpiryMins, this.mTargetIntent, this.mUpgradeInfo));
        UpdaterUtils.setDeferStats(this.context, UpgradeUtilConstants.RESTART, false, preInstallNotificationExpiryMins);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class PreInstallCounter extends CountDownTimer {
        private TextView mWarningTextCounter;
        Resources resources;

        public PreInstallCounter(long j, long j2, TextView textView) {
            super(j, j2);
            this.mWarningTextCounter = textView;
            this.resources = RestartFragment.this.getResources();
        }

        public void refreshUI(TextView textView) {
            this.mWarningTextCounter = textView;
        }

        public void stop() {
            cancel();
            RestartFragment.isTimerRunning = false;
            RestartFragment.isSmartUpdateTimerRunning = false;
            RestartFragment.INSTALL_COUNTDOWN_IN_SECONDS = SmartUpdateUtils.getTimerValue(RestartFragment.this.settings, RestartFragment.this.mUpgradeInfo);
        }

        @Override // android.os.CountDownTimer
        public void onFinish() {
            RestartFragment.isTimerRunning = false;
            RestartFragment.isSmartUpdateTimerRunning = false;
            RestartFragment.INSTALL_COUNTDOWN_IN_SECONDS = SmartUpdateUtils.getTimerValue(RestartFragment.this.settings, RestartFragment.this.mUpgradeInfo);
            RestartFragment.this.proceedWithReboot();
        }

        @Override // android.os.CountDownTimer
        public void onTick(long j) {
            RestartFragment.isTimerRunning = true;
            showPreInstallWarningText(((int) j) / 1000);
        }

        private void showPreInstallWarningText(int i) {
            this.mWarningTextCounter.setText(RestartFragment.this.mUpdateType.getABRestartWarning());
            SpannableString spannableString = new SpannableString(this.resources.getQuantityString(R.plurals.warning_before_restart, i, Integer.valueOf(i)));
            RestartFragment.INSTALL_COUNTDOWN_IN_SECONDS = i;
            spannableString.setSpan(new ForegroundColorSpan(-65536), 0, spannableString.length(), 33);
            this.mWarningTextCounter.append(spannableString);
            this.mWarningTextCounter.append(this.resources.getString(R.string.ab_warning_continues));
        }
    }

    private Intent getTargetIntent(Intent intent) {
        return UpgradeUtilMethods.getStartRestartActivityIntent(this.settings.getString(Configs.METADATA), intent.getStringExtra(UpgradeUtilConstants.KEY_VERSION), intent.getStringExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE));
    }

    private void refreshOrStartInstallCounter() {
        PreInstallCounter preInstallCounter = mPreInstallCounter;
        if (preInstallCounter != null) {
            preInstallCounter.refreshUI(this.mWarningText);
            return;
        }
        PreInstallCounter preInstallCounter2 = new PreInstallCounter(INSTALL_COUNTDOWN_IN_SECONDS * 1000, 1000L, this.mWarningText);
        mPreInstallCounter = preInstallCounter2;
        preInstallCounter2.start();
    }
}
