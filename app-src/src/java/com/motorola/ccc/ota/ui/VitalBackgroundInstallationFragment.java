package com.motorola.ccc.ota.ui;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.text.NumberFormat;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class VitalBackgroundInstallationFragment extends Fragment {
    private static int DEFAULT_RETRY_OR_SUSPEND_VALUE = -2;
    public static final String EMERGENCY_DIALER = "com.android.phone.EmergencyDialer.DIAL";
    private Activity activity;
    private Intent activityIntent;
    private AlertDialog alertDialog;
    private Context context;
    private Button emergencyCall;
    private UpdaterUtils.UpgradeInfo info;
    private long launchTimeInMillis;
    private TextView mBGText;
    private TextView mBGTitle;
    private TextView mNewVersion;
    private TextView mPercentage;
    private int mRetriedOrSuspend;
    private Button mSkip;
    private MetaData meta;
    private ProgressBar progressBar;
    private View rootView;
    private BotaSettings settings;
    private UpdateType.UpdateTypeInterface mUpdateTypeInterface = null;
    private String mSkipReason = "";
    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.VitalBackgroundInstallationFragment.4
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.info("OtaApp", "Vital Update, BackgroundInstallationFragment:mFinishReceiver, finish BackgroundInstallationFragment");
            VitalBackgroundInstallationFragment.this.activity.finishAndRemoveTask();
        }
    };
    private BroadcastReceiver mProgressReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.VitalBackgroundInstallationFragment.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (UpgradeUtilConstants.ACTION_AB_UPDATE_PROGRESS.equals(intent.getAction())) {
                VitalBackgroundInstallationFragment.this.initializeCustomView(intent);
            } else if (UpgradeUtilConstants.START_RESTART_ACTIVITY_INTENT.equals(intent.getAction()) || UpgradeUtilConstants.UPGRADE_RESTART_NOTIFICATION.equals(intent.getAction())) {
                UpdaterUtils.setFullScreenStartPoint(Configs.STATS_RESTART_START_POINT, "resumeAfterBGScreen");
                if (UpdaterUtils.isPriorityAppRunning(context)) {
                    UpdaterUtils.priorityAppRunningPostponeActivity(context, NotificationUtils.KEY_RESTART, intent, false);
                    context.unregisterReceiver(VitalBackgroundInstallationFragment.this.mProgressReceiver);
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
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) { // from class: com.motorola.ccc.ota.ui.VitalBackgroundInstallationFragment.1
            public void handleOnBackPressed() {
                VitalBackgroundInstallationFragment vitalBackgroundInstallationFragment = VitalBackgroundInstallationFragment.this;
                vitalBackgroundInstallationFragment.addCancelConfirmDialog(vitalBackgroundInstallationFragment.context);
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.FINISH_BG_INSTALL_ACTIVITY);
        BroadcastUtils.registerLocalReceiver(this.context, this.mFinishReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(UpgradeUtilConstants.ACTION_AB_UPDATE_PROGRESS);
        intentFilter2.addAction(UpgradeUtilConstants.START_RESTART_ACTIVITY_INTENT);
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_RESTART_NOTIFICATION);
        this.context.registerReceiver(this.mProgressReceiver, intentFilter2, 2);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.activityIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
        this.meta = MetaDataBuilder.from(this.settings.getString(Configs.METADATA));
        this.info = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(this.activityIntent);
        if (this.meta == null) {
            this.activity.finish();
        }
        this.mUpdateTypeInterface = UpdateType.getUpdateType(this.meta.getUpdateTypeData());
        UpdaterUtils.stopWarningAlertDialog(this.context);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.rootView = layoutInflater.inflate(R.layout.vital_check_update, viewGroup, false);
        findViewsById();
        initializeCommonLayout();
        handleButtons();
        initializeCustomView(this.activityIntent);
        return this.rootView;
    }

    private void findViewsById() {
        this.mBGTitle = (TextView) this.rootView.findViewById(R.id.vitalUpdateTitle);
        this.mBGText = (TextView) this.rootView.findViewById(R.id.vitalUpdateBody);
        this.mPercentage = (TextView) this.rootView.findViewById(R.id.percentage);
        this.mNewVersion = (TextView) this.rootView.findViewById(R.id.new_version);
        this.progressBar = (ProgressBar) this.rootView.findViewById(R.id.progress_bar_bg);
        this.mSkip = (Button) this.rootView.findViewById(R.id.skip_btn);
        this.emergencyCall = (Button) this.rootView.findViewById(R.id.emergency_call_btn);
    }

    private void initializeCommonLayout() {
        if (getActivity() == null || !isAdded()) {
            return;
        }
        this.mPercentage.setVisibility(0);
        this.mNewVersion.setVisibility(0);
        this.progressBar.setVisibility(0);
        this.mNewVersion.setText(getString(R.string.download_version, new Object[]{this.meta.getDisplayVersion(), getResources().getString(R.string.total_download_size, Formatter.formatFileSize(getContext(), this.meta.getSize()))}));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initializeCustomView(Intent intent) {
        int intExtra = intent.getIntExtra(UpgradeUtilConstants.KEY_UPGRADE_STATUS, -1);
        float floatExtra = intent.getFloatExtra(UpgradeUtilConstants.KEY_PERCENTAGE, -1.0f);
        int intExtra2 = intent.getIntExtra(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED, DEFAULT_RETRY_OR_SUSPEND_VALUE);
        this.mRetriedOrSuspend = intExtra2;
        if (intExtra2 == DEFAULT_RETRY_OR_SUSPEND_VALUE) {
            Logger.debug("OtaApp", "Vital Update, User did check for update during BG install");
            int i = this.settings.getInt(Configs.STORED_AB_STATUS, -1);
            floatExtra = this.settings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, -1.0f);
            ConnectivityManager connectivityManager = (ConnectivityManager) this.context.getSystemService("connectivity");
            if ((this.settings.getBoolean(Configs.BATTERY_LOW) && i >= 0) || (!NetworkUtils.isNetWorkConnected(connectivityManager) && i < 4)) {
                this.mRetriedOrSuspend = -1;
            } else {
                this.mRetriedOrSuspend = 0;
            }
            Logger.debug("OtaApp", "Vital Update, Updated progress details, progress " + floatExtra + " status " + i + " mRetriedOrSuspend " + this.mRetriedOrSuspend);
            intExtra = i;
        }
        String format = NumberFormat.getPercentInstance().format(floatExtra / 100.0f);
        if (intExtra >= 0) {
            this.mPercentage.setVisibility(0);
            this.mPercentage.setText(getResources().getString(R.string.vital_percentage, format));
            this.progressBar.setProgress(Math.round(floatExtra));
        }
        this.mBGText.setText(this.context.getResources().getString(R.string.vital_bg_text));
        this.mBGTitle.setText(this.context.getResources().getString(R.string.vital_applying_updates));
        if (BuildPropReader.isStreamingUpdate()) {
            if (this.mRetriedOrSuspend != 0) {
                this.mPercentage.setText(getResources().getString(R.string.vital_percentage, format));
                this.progressBar.setProgress(Math.round(floatExtra));
                updatePauseView();
                String errorNotificationText = NotificationUtils.getErrorNotificationText(this.mRetriedOrSuspend, this.meta.getForceDownloadTime() >= 0.0d, this.settings, this.context);
                if (UpdaterUtils.checkForFinalDeferTimeForForceUpdate() && !UpdaterUtils.getAutomaticDownloadForCellular()) {
                    errorNotificationText = errorNotificationText + System.lineSeparator() + getResources().getString(R.string.automatic_bg_install_on_cellular_warning, DateFormatUtils.getCalendarString(this.context, UpdaterUtils.getMaxForceDownloadDeferTime()));
                }
                this.mBGText.setText(errorNotificationText);
                this.mSkipReason = errorNotificationText;
                return;
            }
            updateResumeView();
        }
    }

    private void handleButtons() {
        this.mSkip.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.VitalBackgroundInstallationFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                VitalBackgroundInstallationFragment.this.settings.setString(Configs.VITAL_UPDATE_CANCEL_REASON, "User skipped vital update from paused screen. Error: " + VitalBackgroundInstallationFragment.this.mSkipReason);
                VitalBackgroundInstallationFragment.this.mSkipReason = "";
                VitalBackgroundInstallationFragment.this.settings.setBoolean(Configs.LAUNCHED_NEXT_SETUP_SCREEN_ON_SKIP_OR_STOP, true);
                UpdaterUtils.launchNextSetupActivityVitalUpdate(VitalBackgroundInstallationFragment.this.context);
                UpgradeUtilMethods.sendUserResponseDuringBackgroundInstallation(VitalBackgroundInstallationFragment.this.context, UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL);
            }
        });
        this.emergencyCall.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.VitalBackgroundInstallationFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                VitalBackgroundInstallationFragment.this.settings.setString(Configs.VITAL_UPDATE_CANCEL_REASON, "User clicked on emergency call.");
                VitalBackgroundInstallationFragment.this.settings.setBoolean(Configs.LAUNCHED_NEXT_SETUP_SCREEN_ON_SKIP_OR_STOP, true);
                VitalBackgroundInstallationFragment.this.onclickEmergencyCall();
                UpgradeUtilMethods.sendUserResponseDuringBackgroundInstallation(VitalBackgroundInstallationFragment.this.context, UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL);
            }
        });
    }

    public void onclickEmergencyCall() {
        Intent intent = new Intent(EMERGENCY_DIALER);
        intent.setFlags(1073741824);
        startActivity(intent);
    }

    private void updateResumeView() {
        this.mPercentage.setVisibility(0);
        this.mNewVersion.setVisibility(0);
        this.progressBar.setVisibility(0);
        this.mSkip.setVisibility(8);
        if (UpdaterUtils.isVerizon()) {
            this.emergencyCall.setVisibility(0);
        }
        this.mSkipReason = "";
    }

    private void updatePauseView() {
        this.mPercentage.setVisibility(8);
        this.mNewVersion.setVisibility(8);
        this.progressBar.setVisibility(8);
        if (UpdaterUtils.isVerizon()) {
            this.emergencyCall.setVisibility(0);
        }
        this.mSkip.setVisibility(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addCancelConfirmDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View inflate = getLayoutInflater().inflate(R.layout.custom_dialog, (ViewGroup) null);
        builder.setView(inflate);
        this.alertDialog = builder.create();
        TextView textView = (TextView) inflate.findViewById(R.id.custom_alert_text);
        textView.setVisibility(0);
        textView.setText(context.getResources().getString(R.string.vital_go_back));
        Button button = (Button) inflate.findViewById(R.id.custom_btn_deny);
        button.setVisibility(0);
        button.setText(context.getResources().getString(R.string.vital_stop));
        button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.VitalBackgroundInstallationFragment$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                VitalBackgroundInstallationFragment.this.m290lambda$addCancelConfirmDialog$0$commotorolacccotauiVitalBackgroundInstallationFragment(context, view);
            }
        });
        Button button2 = (Button) inflate.findViewById(R.id.custom_btn_allow);
        button2.setVisibility(0);
        button2.setText(context.getResources().getString(R.string.alert_dialog_continue));
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.VitalBackgroundInstallationFragment$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                VitalBackgroundInstallationFragment.this.m291lambda$addCancelConfirmDialog$1$commotorolacccotauiVitalBackgroundInstallationFragment(view);
            }
        });
        this.alertDialog.create();
        UpdaterUtils.setCornersRounded(context, this.alertDialog);
        this.alertDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$addCancelConfirmDialog$0$com-motorola-ccc-ota-ui-VitalBackgroundInstallationFragment  reason: not valid java name */
    public /* synthetic */ void m290lambda$addCancelConfirmDialog$0$commotorolacccotauiVitalBackgroundInstallationFragment(Context context, View view) {
        this.settings.setString(Configs.VITAL_UPDATE_CANCEL_REASON, "User cancelled vital update from popup onBackClick");
        this.settings.setBoolean(Configs.LAUNCHED_NEXT_SETUP_SCREEN_ON_SKIP_OR_STOP, true);
        UpdaterUtils.launchNextSetupActivityVitalUpdate(context);
        UpgradeUtilMethods.sendUserResponseDuringBackgroundInstallation(context, UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$addCancelConfirmDialog$1$com-motorola-ccc-ota-ui-VitalBackgroundInstallationFragment  reason: not valid java name */
    public /* synthetic */ void m291lambda$addCancelConfirmDialog$1$commotorolacccotauiVitalBackgroundInstallationFragment(View view) {
        this.alertDialog.dismiss();
    }

    public void onResume() {
        super.onResume();
        this.launchTimeInMillis = System.currentTimeMillis();
    }

    public void onStop() {
        super.onStop();
        this.settings.setLong(Configs.TOTAL_TIME_SPEND_ON_BG_SCREEN, this.settings.getLong(Configs.TOTAL_TIME_SPEND_ON_BG_SCREEN, 0L) + (System.currentTimeMillis() - this.launchTimeInMillis));
        AlertDialog alertDialog = this.alertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            this.context.unregisterReceiver(this.mProgressReceiver);
        } catch (IllegalArgumentException e) {
            Logger.error("OtaApp", "Vital Update, BGI Fragment:onDestroy:Receiver already unregistered:msg=" + e);
        }
        BroadcastUtils.unregisterLocalReceiver(this.context, this.mFinishReceiver);
    }
}
