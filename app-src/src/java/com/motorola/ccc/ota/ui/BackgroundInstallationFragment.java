package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.CusAndroidUtils;
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
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.text.NumberFormat;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class BackgroundInstallationFragment extends Fragment {
    private static int DEFAULT_RETRY_OR_SUSPEND_VALUE = -2;
    private Activity activity;
    private Intent activityIntent;
    private AlertDialog alertDialog;
    private Context context;
    private ImageView imgBack;
    private UpdaterUtils.UpgradeInfo info;
    private long launchTimeInMillis;
    private LinearLayout mButtonLayout;
    private Button mCancel;
    private Button mCellularResume;
    private Button mDone;
    private TextView mInstallationMessage;
    private TextView mInstallationPercentage;
    private TextView mInstallationStep;
    private ProgressBar mProgressBar;
    private int mRetriedOrSuspend;
    private Button mWifiSettings;
    private MetaData meta;
    private TextView osReleaseNotes;
    private View rootView;
    private BotaSettings settings;
    private UpdateType.UpdateTypeInterface mUpdateTypeInterface = null;
    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.BackgroundInstallationFragment.6
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.info("OtaApp", "BackgroundInstallationFragment:mFinishReceiver, finish BackgroundInstallationFragment");
            BackgroundInstallationFragment.this.activity.finishAndRemoveTask();
        }
    };
    private BroadcastReceiver mProgressReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.BackgroundInstallationFragment.7
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (UpgradeUtilConstants.ACTION_AB_UPDATE_PROGRESS.equals(intent.getAction())) {
                BackgroundInstallationFragment.this.initializeCustomView(intent);
            } else if (UpgradeUtilConstants.UPGRADE_UPDATE_VERIFY_PAYLOAD_METADATA_DOWNLOAD_STATUS.equals(intent.getAction())) {
                BackgroundInstallationFragment.this.updateUIDuringCompatibilityCheck(intent);
            } else if (UpgradeUtilConstants.START_RESTART_ACTIVITY_INTENT.equals(intent.getAction()) || UpgradeUtilConstants.UPGRADE_RESTART_NOTIFICATION.equals(intent.getAction())) {
                UpdaterUtils.setFullScreenStartPoint(Configs.STATS_RESTART_START_POINT, "resumeAfterBGScreen");
                if (UpdaterUtils.isPriorityAppRunning(context)) {
                    UpdaterUtils.priorityAppRunningPostponeActivity(context, NotificationUtils.KEY_RESTART, intent, false);
                    context.unregisterReceiver(BackgroundInstallationFragment.this.mProgressReceiver);
                    return;
                }
                intent.setClass(context, BaseActivity.class);
                intent.setFlags(805306368);
                intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.RESTART_FRAGMENT.toString());
                context.startActivity(intent);
            }
        }
    };

    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof Activity) {
            this.activity = (Activity) context;
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.settings = new BotaSettings();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.FINISH_BG_INSTALL_ACTIVITY);
        BroadcastUtils.registerLocalReceiver(this.context, this.mFinishReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(UpgradeUtilConstants.ACTION_AB_UPDATE_PROGRESS);
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_VERIFY_PAYLOAD_METADATA_DOWNLOAD_STATUS);
        intentFilter2.addAction(UpgradeUtilConstants.START_RESTART_ACTIVITY_INTENT);
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_RESTART_NOTIFICATION);
        this.context.registerReceiver(this.mProgressReceiver, intentFilter2, 2);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.activityIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
        this.meta = MetaDataBuilder.from(this.settings.getString(Configs.METADATA));
        this.info = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(this.activityIntent);
        MetaData metaData = this.meta;
        if (metaData == null) {
            this.activity.finish();
        } else {
            this.mUpdateTypeInterface = UpdateType.getUpdateType(metaData.getUpdateTypeData());
        }
        UpdaterUtils.stopWarningAlertDialog(this.context);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.rootView = layoutInflater.inflate(R.layout.background_system_install, viewGroup, false);
        findViewsById();
        initializeCommonLayout();
        handleButtons();
        initializeCustomView(this.activityIntent);
        return this.rootView;
    }

    private void findViewsById() {
        this.mButtonLayout = (LinearLayout) this.rootView.findViewById(R.id.button_layout);
        this.mDone = (Button) this.rootView.findViewById(R.id.done);
        this.mCancel = (Button) this.rootView.findViewById(R.id.cancel);
        this.mCellularResume = (Button) this.rootView.findViewById(R.id.cellular_resume);
        this.mWifiSettings = (Button) this.rootView.findViewById(R.id.wifi_settings);
        this.mInstallationPercentage = (TextView) this.rootView.findViewById(R.id.install_percentage);
        this.mInstallationMessage = (TextView) this.rootView.findViewById(R.id.bg_install_message);
        this.mInstallationStep = (TextView) this.rootView.findViewById(R.id.install_step);
        this.mProgressBar = (ProgressBar) this.rootView.findViewById(R.id.progressBar);
        this.imgBack = (ImageView) this.rootView.findViewById(R.id.imgBack);
    }

    private void initializeCommonLayout() {
        FragmentActivity activity = getActivity();
        if (activity == null || !isAdded()) {
            return;
        }
        Window window = activity.getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.setStatusBarColor(0);
        if (!UpdaterUtils.isBatterySaverEnabled(this.context)) {
            WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
            if (((UiModeManager) this.context.getSystemService("uimode")).getNightMode() != 2) {
                windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
                windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
            }
        }
        this.settings.setString(Configs.SCREEN_ANIMATION_VIEW, "downloading");
        TextView textView = (TextView) this.rootView.findViewById(R.id.expTitle);
        UpdaterUtils.handleExpList(UpdaterUtils.setExpandableList((ExpandableListView) this.rootView.findViewById(R.id.expandableListView), this.info.hasReleaseNotes(), this.info.getReleaseNotes(), textView, getActivity(), this.mUpdateTypeInterface, this.rootView.findViewById(R.id.dash_line)), "BGProgress");
        TextView textView2 = (TextView) this.rootView.findViewById(R.id.os_release_notes);
        this.osReleaseNotes = textView2;
        UpdaterUtils.setOsReleaseNotes(this.context, textView2, this.info, "BGProgress");
        ((TextView) this.rootView.findViewById(R.id.install_version)).setText(getString(R.string.download_version, new Object[]{this.meta.getDisplayVersion(), getResources().getString(R.string.total_download_size, Formatter.formatFileSize(getContext(), this.meta.getSize()))}));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initializeCustomView(Intent intent) {
        int intExtra = intent.getIntExtra(UpgradeUtilConstants.KEY_UPGRADE_STATUS, -1);
        float floatExtra = intent.getFloatExtra(UpgradeUtilConstants.KEY_PERCENTAGE, -1.0f);
        int intExtra2 = intent.getIntExtra(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED, DEFAULT_RETRY_OR_SUSPEND_VALUE);
        this.mRetriedOrSuspend = intExtra2;
        if (intExtra2 == DEFAULT_RETRY_OR_SUSPEND_VALUE) {
            Logger.debug("OtaApp", "User did check for update during BG install");
            int i = this.settings.getInt(Configs.STORED_AB_STATUS, -1);
            floatExtra = this.settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, -1.0f);
            ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService("connectivity");
            if ((this.settings.getBoolean(Configs.BATTERY_LOW) && i >= 0) || (!NetworkUtils.isNetWorkConnected(connectivityManager) && i < 4)) {
                this.mRetriedOrSuspend = -1;
            } else {
                this.mRetriedOrSuspend = 0;
            }
            Logger.debug("OtaApp", "Updated progress details, progress " + floatExtra + " status " + i + " mRetriedOrSuspend " + this.mRetriedOrSuspend);
            intExtra = i;
        }
        String format = NumberFormat.getPercentInstance().format(floatExtra / 100.0f);
        if (UpdaterUtils.showCancelOption() && intExtra < 6) {
            this.mCancel.setVisibility(0);
        } else {
            this.mCancel.setVisibility(8);
        }
        if (intExtra >= 0) {
            this.mInstallationPercentage.setVisibility(0);
            this.mProgressBar.setVisibility(0);
            this.mInstallationPercentage.setText(getResources().getString(R.string.completed_install, format));
            this.mProgressBar.setProgress(Math.round(floatExtra));
            this.mInstallationStep.setVisibility(0);
            String string = getResources().getString(R.string.applying_patch, Integer.valueOf(intExtra), 6);
            if (intExtra == 4) {
                string = getResources().getString(R.string.verifying_patch);
            }
            if (intExtra == 5) {
                string = getResources().getString(R.string.finalizing_patch);
            }
            if (intExtra == 6) {
                this.mInstallationPercentage.setText(getResources().getString(R.string.completed_install, NumberFormat.getPercentInstance().format(1.0d)));
                string = getResources().getString(R.string.completed, Integer.valueOf(intExtra), 6);
            }
            this.mInstallationStep.setText(string);
        }
        this.mInstallationMessage.setText(this.context.getResources().getString(R.string.background_install_msg));
        if (BuildPropReader.isStreamingUpdate()) {
            if (this.mRetriedOrSuspend != 0) {
                this.mInstallationPercentage.setText(getResources().getString(R.string.install_progress_paused_message, format));
                this.mProgressBar.setProgress(Math.round(floatExtra));
                updatePauseView();
                String errorNotificationText = NotificationUtils.getErrorNotificationText(this.mRetriedOrSuspend, this.meta.getForceDownloadTime() >= 0.0d, this.settings, this.context);
                if (UpdaterUtils.checkForFinalDeferTimeForForceUpdate() && !UpdaterUtils.getAutomaticDownloadForCellular()) {
                    errorNotificationText = errorNotificationText + System.lineSeparator() + getResources().getString(R.string.automatic_bg_install_on_cellular_warning, DateFormatUtils.getCalendarString(this.context, UpdaterUtils.getMaxForceDownloadDeferTime()));
                }
                this.mInstallationMessage.setText(errorNotificationText);
                return;
            }
            updateResumeView();
        } else if (this.mRetriedOrSuspend != 0 && BuildPropReader.isATT() && this.settings.getBoolean(Configs.BATTERY_LOW)) {
            this.mInstallationPercentage.setText(getResources().getString(R.string.install_progress_paused_message, format));
            this.mProgressBar.setProgress(Math.round(floatExtra));
            this.mInstallationMessage.setText(UpdaterUtils.getBatteryLowMessage(this.context));
        }
    }

    private void updateResumeView() {
        this.mButtonLayout.setOrientation(0);
        this.mButtonLayout.setGravity(8388613);
        this.mCellularResume.setVisibility(8);
        this.mWifiSettings.setVisibility(8);
        this.mDone.setVisibility(0);
        String string = this.settings.getString(Configs.SCREEN_ANIMATION_VIEW);
        if (string == null || string.equals("downloading")) {
            return;
        }
        this.settings.setString(Configs.SCREEN_ANIMATION_VIEW, "downloading");
    }

    private void updatePauseView() {
        boolean z = this.meta.getForceDownloadTime() >= 0.0d;
        BotaSettings botaSettings = new BotaSettings();
        this.mButtonLayout.setOrientation(1);
        this.mButtonLayout.setGravity(1);
        if (!this.settings.getBoolean(Configs.BATTERY_LOW)) {
            if (!UpdaterUtils.isWifiOnly() && ((z || SmartUpdateUtils.isDownloadForcedForSmartUpdate(botaSettings)) && !UpdaterUtils.getAutomaticDownloadForCellular())) {
                this.mCellularResume.setVisibility(0);
            }
            if (BuildPropReader.isBotaATT() && UpdaterUtils.isWifiOnly() && !UpdaterUtils.isWifiOnlyPkg()) {
                this.mCellularResume.setVisibility(0);
            }
            this.mWifiSettings.setVisibility(0);
            this.mDone.setVisibility(8);
        } else {
            this.mButtonLayout.setOrientation(0);
            this.mButtonLayout.setGravity(8388613);
            this.mCellularResume.setVisibility(8);
            this.mWifiSettings.setVisibility(8);
            this.mDone.setVisibility(0);
        }
        this.settings.setString(Configs.SCREEN_ANIMATION_VIEW, "download_stop");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateUIDuringCompatibilityCheck(Intent intent) {
        String string;
        switch (AnonymousClass8.$SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.valueOf(intent.getStringExtra(UpgradeUtilConstants.KEY_COMPATIBILITY_STATUS)).ordinal()]) {
            case 1:
                updatePauseView();
                if (CusAndroidUtils.isDeviceInDatasaverMode()) {
                    string = this.context.getResources().getString(R.string.progress_suspended_datasaver);
                    break;
                } else if (UpdaterUtils.isDataNetworkRoaming(this.context)) {
                    string = this.context.getResources().getString(R.string.progress_suspended_roaming);
                    break;
                } else if (UpdaterUtils.isAdminAPNEnabled()) {
                    string = this.context.getResources().getString(R.string.progress_suspended_no_adminapn);
                    break;
                } else {
                    string = this.context.getResources().getString(R.string.progress_suspended);
                    break;
                }
            case 2:
                updatePauseView();
                string = this.context.getResources().getString(R.string.bg_retried);
                break;
            case 3:
                updateResumeView();
                string = this.context.getResources().getString(R.string.progress_verify);
                break;
            case 4:
                string = this.context.getResources().getString(R.string.compatibility_verification_success);
                break;
            case 5:
                string = this.context.getResources().getString(R.string.progress_verify_allocate_space);
                break;
            case 6:
                string = this.context.getResources().getString(R.string.allocate_space_success);
                break;
            case 7:
                string = this.context.getResources().getString(R.string.error_space_vab, Formatter.formatFileSize(this.context, Long.valueOf(intent.getStringExtra(UpgradeUtilConstants.KEY_FREE_SPACE_REQUIRED)).longValue()));
                break;
            default:
                string = this.context.getResources().getString(R.string.progress_verify);
                break;
        }
        this.mInstallationMessage.setText(string);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.ui.BackgroundInstallationFragment$8  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static /* synthetic */ class AnonymousClass8 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus;

        static {
            int[] iArr = new int[UpgradeUtils.DownloadStatus.values().length];
            $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus = iArr;
            try {
                iArr[UpgradeUtils.DownloadStatus.STATUS_DEFERRED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_RETRIED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_TEMP_OK.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_OK.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_ALLOCATE_SPACE.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_ALLOCATE_SPACE_SUCESS.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_VAB_MAKE_SPACE_REQUEST_USER.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
        }
    }

    private void handleButtons() {
        this.mDone.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.BackgroundInstallationFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                BackgroundInstallationFragment.this.activity.finish();
            }
        });
        this.mCancel.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.BackgroundInstallationFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                BackgroundInstallationFragment backgroundInstallationFragment = BackgroundInstallationFragment.this;
                backgroundInstallationFragment.addCancelConfirmDialog(backgroundInstallationFragment.context);
            }
        });
        this.mCellularResume.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.BackgroundInstallationFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                BackgroundInstallationFragment.this.handleResumeOnCellular();
            }
        });
        this.mWifiSettings.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.BackgroundInstallationFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.settings.WIFI_SETTINGS");
                BackgroundInstallationFragment.this.context.startActivity(intent);
            }
        });
        this.imgBack.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.BackgroundInstallationFragment.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                BackgroundInstallationFragment.this.activity.finish();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleResumeOnCellular() {
        Toast.makeText(this.context, this.context.getResources().getString(R.string.additional_charges_message), 1).show();
        this.mCellularResume.setVisibility(8);
        UpgradeUtilMethods.sendUserResponseDuringBackgroundInstallation(this.context, UpgradeUtils.DownloadStatus.STATUS_RESUME_ON_CELLULAR);
        CusUtilMethods.showPopupToOptCellularDataAtt(this.context);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addCancelConfirmDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View inflate = getLayoutInflater().inflate(R.layout.custom_dialog, (ViewGroup) null);
        builder.setView(inflate);
        this.alertDialog = builder.create();
        TextView textView = (TextView) inflate.findViewById(R.id.custom_alert_text);
        textView.setVisibility(0);
        textView.setText(context.getResources().getString(R.string.bg_install_cancel_confirm));
        Button button = (Button) inflate.findViewById(R.id.custom_btn_allow);
        button.setVisibility(0);
        button.setText(context.getResources().getString(R.string.alert_dialog_continue));
        Button button2 = (Button) inflate.findViewById(R.id.custom_btn_deny);
        button2.setVisibility(0);
        button2.setText(context.getResources().getString(R.string.alert_dialog_cancel));
        button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.BackgroundInstallationFragment$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                BackgroundInstallationFragment.this.m163lambda$addCancelConfirmDialog$0$commotorolacccotauiBackgroundInstallationFragment(view);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.BackgroundInstallationFragment$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                UpgradeUtilMethods.sendUserResponseDuringBackgroundInstallation(context, UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL);
            }
        });
        this.alertDialog.create();
        UpdaterUtils.setCornersRounded(context, this.alertDialog);
        this.alertDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$addCancelConfirmDialog$0$com-motorola-ccc-ota-ui-BackgroundInstallationFragment  reason: not valid java name */
    public /* synthetic */ void m163lambda$addCancelConfirmDialog$0$commotorolacccotauiBackgroundInstallationFragment(View view) {
        this.alertDialog.dismiss();
    }

    public void onResume() {
        super.onResume();
        this.launchTimeInMillis = System.currentTimeMillis();
    }

    public void onStop() {
        super.onStop();
        if (this.mRetriedOrSuspend != 0 && this.meta.getForceDownloadTime() >= 0.0d && !UpdaterUtils.getAutomaticDownloadForCellular() && UpdaterUtils.checkForFinalDeferTimeForForceUpdate()) {
            this.context.sendBroadcast(new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE), Permissions.INTERACT_OTA_SERVICE);
        }
        AlertDialog alertDialog = this.alertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        this.settings.setLong(Configs.TOTAL_TIME_SPEND_ON_BG_SCREEN, this.settings.getLong(Configs.TOTAL_TIME_SPEND_ON_BG_SCREEN, 0L) + (System.currentTimeMillis() - this.launchTimeInMillis));
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            this.context.unregisterReceiver(this.mProgressReceiver);
        } catch (IllegalArgumentException e) {
            Logger.error("OtaApp", "BGI Fragment:onDestroy:Receiver already unregistered:msg=" + e);
        }
        BroadcastUtils.unregisterLocalReceiver(this.context, this.mFinishReceiver);
    }
}
