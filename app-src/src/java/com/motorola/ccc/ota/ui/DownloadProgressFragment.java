package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
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
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.text.NumberFormat;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DownloadProgressFragment extends Fragment {
    private static int DEFAULT_RETRY_OR_SUSPEND_VALUE = -2;
    private Activity activity;
    private Intent activityIntent;
    private AlertDialog alertDialog;
    private Context context;
    private ImageView imgBack;
    private long launchTimeInMillis;
    private Button mCancel;
    private Button mDlCellularResume;
    private LinearLayout mDlPauseButtons;
    private Button mDlPauseCancel;
    private Button mDlPauseOkay;
    private LinearLayout mDlProgressButtons;
    private TextView mDlProgressMessage;
    private Button mDlWifiSettings;
    private TextView mDownloadProgressStatus;
    private TextView mDownloadTimeleft;
    private TextView mDownloadpausedMsg;
    private TextView mExpTitle;
    private ExpandableListView mExpandableListView;
    private MetaData mMeta;
    private Button mOkay;
    private TextView mPercentage;
    private ProgressBar mProgressBar;
    private int mRetriedOrSuspend;
    private TextView mVersion;
    private TextView osReleaseNotes;
    private View rootView;
    private BotaSettings settings;
    private UpdateType.UpdateTypeInterface mUpdateTypeInterface = null;
    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment.8
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.debug("OtaApp", "DownloadProgressFragment:mFinishReceiver, finish DownloadProgressFragment");
            DownloadProgressFragment.this.activity.finishAndRemoveTask();
        }
    };
    private BroadcastReceiver mProgressReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment.9
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            DownloadProgressFragment.this.updateUI(intent);
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
        intentFilter.addAction(UpgradeUtilConstants.FINISH_DOWNLOAD_PROGRESS_ACTIVITY);
        BroadcastUtils.registerLocalReceiver(this.context, this.mFinishReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS);
        this.context.registerReceiver(this.mProgressReceiver, intentFilter2, 2);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.activityIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
        UpdaterUtils.stopWarningAlertDialog(this.context);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.rootView = layoutInflater.inflate(R.layout.download_progress_fragment, viewGroup, false);
        findViewsById();
        handleButtons();
        UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(this.activityIntent);
        if (upgradeInfoDuringOTAUpdate == null) {
            Logger.error("OtaApp", "DownloadProgressFragment, No upgradeInfo found.");
            this.activity.finish();
            return null;
        }
        MetaData from = MetaDataBuilder.from(this.settings.getString(Configs.METADATA));
        this.mMeta = from;
        if (from != null) {
            this.mUpdateTypeInterface = UpdateType.getUpdateType(from.getUpdateTypeData());
            this.mVersion.setText(getString(R.string.download_version, new Object[]{upgradeInfoDuringOTAUpdate.getDisplayVersion(), getResources().getString(R.string.total_download_size, Formatter.formatFileSize(getContext(), upgradeInfoDuringOTAUpdate.getSize()))}));
            this.settings.setString(Configs.SCREEN_ANIMATION_VIEW, "downloading");
            ExpandableListView expandableList = UpdaterUtils.setExpandableList(this.mExpandableListView, upgradeInfoDuringOTAUpdate.hasReleaseNotes(), upgradeInfoDuringOTAUpdate.getReleaseNotes(), this.mExpTitle, getActivity(), this.mUpdateTypeInterface, this.rootView.findViewById(R.id.dash_line));
            this.mExpandableListView = expandableList;
            this.mExpandableListView = UpdaterUtils.handleExpList(expandableList, "DLProgress");
            UpdaterUtils.setOsReleaseNotes(this.context, this.osReleaseNotes, upgradeInfoDuringOTAUpdate, "DLProgress");
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
        }
        updateUI(this.activityIntent);
        return this.rootView;
    }

    private void findViewsById() {
        this.mVersion = (TextView) this.rootView.findViewById(R.id.download_version);
        this.mDlProgressMessage = (TextView) this.rootView.findViewById(R.id.download_progress_message);
        this.mPercentage = (TextView) this.rootView.findViewById(R.id.txt_percentage);
        this.mDownloadProgressStatus = (TextView) this.rootView.findViewById(R.id.download_progress_textView);
        this.mDownloadTimeleft = (TextView) this.rootView.findViewById(R.id.download_timeleft_text);
        this.mProgressBar = (ProgressBar) this.rootView.findViewById(R.id.progressBar);
        this.mExpTitle = (TextView) this.rootView.findViewById(R.id.expTitle);
        this.mExpandableListView = (ExpandableListView) this.rootView.findViewById(R.id.expandableListView);
        this.osReleaseNotes = (TextView) this.rootView.findViewById(R.id.os_release_notes);
        this.mDownloadpausedMsg = (TextView) this.rootView.findViewById(R.id.download_paused_message);
        this.mDlPauseButtons = (LinearLayout) this.rootView.findViewById(R.id.download_pause_buttons);
        this.mDlWifiSettings = (Button) this.rootView.findViewById(R.id.dl_wifi_settings);
        this.mDlPauseCancel = (Button) this.rootView.findViewById(R.id.dl_pause_cancel);
        this.mDlCellularResume = (Button) this.rootView.findViewById(R.id.dl_cellular_resume);
        this.mDlPauseOkay = (Button) this.rootView.findViewById(R.id.dl_pause_okay);
        this.mDlProgressButtons = (LinearLayout) this.rootView.findViewById(R.id.download_progress_buttons);
        this.mCancel = (Button) this.rootView.findViewById(R.id.dl_cancel);
        this.mOkay = (Button) this.rootView.findViewById(R.id.dl_okay);
        this.imgBack = (ImageView) this.rootView.findViewById(R.id.imgBack);
    }

    private void handleButtons() {
        this.mOkay.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DownloadProgressFragment.this.activity.finish();
            }
        });
        this.mDlPauseOkay.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DownloadProgressFragment.this.activity.finish();
            }
        });
        this.mCancel.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DownloadProgressFragment downloadProgressFragment = DownloadProgressFragment.this;
                downloadProgressFragment.addCancelConfirmDialog(downloadProgressFragment.context);
            }
        });
        this.mDlCellularResume.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Toast.makeText(DownloadProgressFragment.this.context, DownloadProgressFragment.this.context.getResources().getString(R.string.additional_charges_message), 1).show();
                DownloadProgressFragment.this.mDlCellularResume.setVisibility(8);
                UpgradeUtilMethods.sendDownloadNotificationResponse(OtaApplication.getGlobalContext(), UpgradeUtils.DownloadStatus.STATUS_RESUME_ON_CELLULAR);
            }
        });
        this.mDlWifiSettings.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.settings.WIFI_SETTINGS");
                DownloadProgressFragment.this.context.startActivity(intent);
            }
        });
        this.mDlPauseCancel.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DownloadProgressFragment downloadProgressFragment = DownloadProgressFragment.this;
                downloadProgressFragment.addCancelConfirmDialog(downloadProgressFragment.context);
            }
        });
        this.imgBack.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment.7
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DownloadProgressFragment.this.activity.finish();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateUI(Intent intent) {
        String string;
        int i;
        String error;
        String string2;
        this.mRetriedOrSuspend = intent.getIntExtra(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED, DEFAULT_RETRY_OR_SUSPEND_VALUE);
        long longExtra = intent.getLongExtra(UpgradeUtilConstants.KEY_BYTES_TOTAL, 0L);
        long longExtra2 = intent.getLongExtra(UpgradeUtilConstants.KEY_BYTES_RECEIVED, -1L);
        String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE);
        boolean isWifiOnly = UpdaterUtils.isWifiOnly();
        if (this.mRetriedOrSuspend == DEFAULT_RETRY_OR_SUSPEND_VALUE) {
            if (!NetworkUtils.isNetWorkConnected((ConnectivityManager) this.context.getSystemService("connectivity")) || this.settings.getBoolean(Configs.BATTERY_LOW)) {
                this.mRetriedOrSuspend = -1;
            } else {
                this.mRetriedOrSuspend = 0;
            }
        }
        this.mDlProgressMessage.setText(this.context.getResources().getString(R.string.download_progress_text));
        MetaData from = MetaDataBuilder.from(this.settings.getString(Configs.METADATA));
        this.mMeta = from;
        if (from == null) {
            Logger.debug("OtaApp", "empty metadata, returning");
            return;
        }
        FragmentActionListener activity = getActivity();
        if (activity == null || !isAdded()) {
            return;
        }
        int downloadPercentage = getDownloadPercentage(longExtra, longExtra2);
        Resources resources = getResources();
        String format = NumberFormat.getPercentInstance().format(downloadPercentage / 100.0d);
        int i2 = 8;
        if (this.mRetriedOrSuspend != 0) {
            this.mDlProgressMessage.setVisibility(8);
            this.mDlProgressButtons.setVisibility(8);
            this.mDlPauseButtons.setVisibility(0);
            this.settings.setString(Configs.SCREEN_ANIMATION_VIEW, "download_stop");
            this.mPercentage.setText(resources.getString(R.string.download_progress_paused_message, String.valueOf(downloadPercentage)));
            this.mProgressBar.setProgress(downloadPercentage);
            if (UpdaterUtils.showCancelOption() && !"sdcard".equals(stringExtra)) {
                this.mDlPauseCancel.setVisibility(0);
            }
            if (UpdaterUtils.isDeviceInDatasaverMode()) {
                string2 = resources.getString(R.string.progress_suspended_datasaver);
            } else if (this.mRetriedOrSuspend == 1) {
                string2 = resources.getString(R.string.progress_retried);
            } else if (this.settings.getBoolean(Configs.BATTERY_LOW)) {
                string2 = resources.getString(R.string.low_battery_download);
                this.mDlWifiSettings.setVisibility(8);
            } else if (isWifiOnly) {
                string2 = resources.getString(R.string.progress_suspended_wifi);
                this.mDlWifiSettings.setVisibility(0);
            } else if (this.mMeta.getForceDownloadTime() >= 0.0d && !UpdaterUtils.getAutomaticDownloadForCellular()) {
                if (UpdaterUtils.checkForFinalDeferTimeForForceUpdate()) {
                    string2 = resources.getString(R.string.progress_suspended_wifi) + System.lineSeparator() + resources.getString(R.string.automatic_download_on_cellular_warning, DateFormatUtils.getCalendarString(this.context, UpdaterUtils.getMaxForceDownloadDeferTime()));
                } else {
                    string2 = resources.getString(R.string.progress_suspended_wifi);
                }
                this.mDlCellularResume.setVisibility(0);
                this.mDlWifiSettings.setVisibility(0);
            } else if (UpdaterUtils.isDataNetworkRoaming(this.context)) {
                string2 = resources.getString(R.string.progress_suspended_roaming);
            } else if (UpdaterUtils.isAdminAPNEnabled()) {
                string2 = resources.getString(R.string.progress_suspended_no_adminapn);
            } else {
                string2 = resources.getString(R.string.progress_suspended);
                if (!UpdaterUtils.getAutomaticDownloadForCellular()) {
                    this.mDlCellularResume.setVisibility(0);
                }
            }
            this.mDownloadpausedMsg.setText(string2);
            this.mDownloadpausedMsg.setVisibility(0);
        } else {
            this.mDlPauseButtons.setVisibility(8);
            this.mDlProgressButtons.setVisibility(0);
            this.mDownloadpausedMsg.setVisibility(8);
            this.mDlProgressMessage.setVisibility(0);
            String string3 = this.settings.getString(Configs.SCREEN_ANIMATION_VIEW);
            if (string3 != null && !string3.equals("downloading")) {
                this.settings.setString(Configs.SCREEN_ANIMATION_VIEW, "downloading");
            }
            if (UpdaterUtils.showCancelOption() && !"sdcard".equals(stringExtra)) {
                this.mCancel.setVisibility(0);
            }
            if (!UpgradeUtilConstants.UPGRADE_UPDATE_DOWNLOAD_STATUS.equals(intent.getAction())) {
                i2 = 8;
                this.mPercentage.setVisibility(8);
                this.mProgressBar.setVisibility(8);
            } else {
                i2 = 8;
                this.mPercentage.setVisibility(0);
                this.mProgressBar.setVisibility(0);
                this.mPercentage.setText(resources.getString(R.string.downloaded_percentage, format));
                this.mProgressBar.setProgress(downloadPercentage);
            }
        }
        int i3 = i2;
        String estimatedTime = UpdaterUtils.getEstimatedTime(longExtra, longExtra2, new long[]{this.settings.getLong(Configs.KEY_RECEIVED_BYTES, -1L), this.settings.getLong(Configs.KEY_TIME_RECEIVED_BYTES, -1L)}, resources);
        if (longExtra == longExtra2) {
            string = resources.getString(R.string.progress_verify);
            Bundle bundle = new Bundle();
            if (BuildPropReader.isUEUpdateEnabled()) {
                error = UpgradeUtils.Error.ERR_BACKGROUND_INSTALL.toString();
            } else {
                error = UpgradeUtils.Error.ERR_INSTALL.toString();
            }
            bundle.putString(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE, error);
            activity.onUpdateActionResponse(bundle);
        } else if ("sdcard".equals(stringExtra)) {
            string = resources.getString(R.string.progress_copying_sd);
        } else if (estimatedTime == null) {
            this.mDownloadTimeleft.setVisibility(i3);
            string = null;
        } else {
            string = resources.getString(R.string.download_progress_fragment_message, Formatter.formatFileSize(getContext(), longExtra2), Formatter.formatFileSize(getContext(), longExtra));
            this.mDownloadTimeleft.setText(resources.getString(R.string.download_time_left, estimatedTime));
            i = 0;
            this.mDownloadTimeleft.setVisibility(0);
            this.mDownloadProgressStatus.setText(string);
            this.mDownloadProgressStatus.setVisibility(i);
        }
        i = 0;
        this.mDownloadProgressStatus.setText(string);
        this.mDownloadProgressStatus.setVisibility(i);
    }

    private int getDownloadPercentage(long j, long j2) {
        try {
            return (int) ((j2 * 100) / j);
        } catch (ArithmeticException unused) {
            Logger.error("OtaApp", "Exception occured while calculating downloading percentage.");
            return 0;
        }
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
        button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                DownloadProgressFragment.this.m184lambda$addCancelConfirmDialog$0$commotorolacccotauiDownloadProgressFragment(view);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadProgressFragment$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                UpgradeUtilMethods.sendDownloadNotificationResponse(context, UpgradeUtils.DownloadStatus.STATUS_CANCEL);
            }
        });
        this.alertDialog.create();
        UpdaterUtils.setCornersRounded(context, this.alertDialog);
        this.alertDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$addCancelConfirmDialog$0$com-motorola-ccc-ota-ui-DownloadProgressFragment  reason: not valid java name */
    public /* synthetic */ void m184lambda$addCancelConfirmDialog$0$commotorolacccotauiDownloadProgressFragment(View view) {
        this.alertDialog.dismiss();
    }

    public void onResume() {
        super.onResume();
        this.launchTimeInMillis = System.currentTimeMillis();
    }

    public void onStop() {
        super.onStop();
        if (this.mRetriedOrSuspend != 0 && this.mMeta.getForceDownloadTime() >= 0.0d && !UpdaterUtils.getAutomaticDownloadForCellular() && UpdaterUtils.checkForFinalDeferTimeForForceUpdate()) {
            this.context.sendBroadcast(new Intent(UpgradeUtilConstants.RUN_STATE_MACHINE), Permissions.INTERACT_OTA_SERVICE);
        }
        AlertDialog alertDialog = this.alertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        this.settings.setLong(Configs.TOTAL_TIME_SPEND_ON_DLP_SCREEN, this.settings.getLong(Configs.TOTAL_TIME_SPEND_ON_DLP_SCREEN, 0L) + (System.currentTimeMillis() - this.launchTimeInMillis));
    }

    public void onDestroy() {
        super.onDestroy();
        this.context.unregisterReceiver(this.mProgressReceiver);
        BroadcastUtils.unregisterLocalReceiver(this.context, this.mFinishReceiver);
    }
}
