package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.text.style.BulletSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.WarningAlertDialog;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DownloadFragment extends Fragment {
    private static final int DIALOG_CANCEL = 2;
    private static final int DIALOG_DATASAVER_ON = 3;
    private static final int DIALOG_NO_NETWORK = 4;
    private static final int DIALOG_USER_CHOICE = 1;
    private static final int REQ_CODE = 0;
    private Activity activity;
    private Intent activityIntent;
    private ConnectivityManager cm;
    private Context context;
    private ExpandableListView expandableListView;
    private ImageView imgBack;
    private boolean isTransactionSafe;
    private long launchTimeInMillis;
    private AlarmManager mAlarmManager;
    private boolean mByPassPreDownloadDialog;
    private Button mCancelBtn;
    private Button mDownloadBtn;
    private TextView mExpTitle;
    private boolean mForced;
    private Button mLaterBtn;
    private LinearLayout mLinearLayout;
    private String mLocationType;
    private long mNextPrompt;
    private PendingIntent mPendingIntent;
    private TextView mPreDownloadNotes;
    private int mPromptCount;
    private boolean mShowDownloadOptions;
    private Intent mTargetIntent;
    private UpdateType.UpdateTypeInterface mUpdateType;
    private TextView mVersion;
    private boolean mWifiOnly;
    private TextView osReleaseNotes;
    private View rootView;
    private BotaSettings settings;
    private String updateTypeString;
    private UpdaterUtils.UpgradeInfo upgradeInfo;
    private String notificationTitle = null;
    private String notificationTxt = null;
    private boolean mDoNotShowNotification = false;
    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.DownloadFragment.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (UpgradeUtilConstants.FINISH_DOWNLOAD_ACTIVITY.equals(intent.getAction())) {
                Logger.info("OtaApp", "DownloadActivity:mFinishReceiver, finish download activity as force upgrade timer expired");
                DownloadFragment.this.mDoNotShowNotification = true;
                DownloadFragment.this.activity.finish();
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
        this.mAlarmManager = (AlarmManager) this.context.getSystemService("alarm");
        this.cm = (ConnectivityManager) this.context.getSystemService("connectivity");
        this.settings = new BotaSettings();
        UpdaterUtils.stopWarningAlertDialog(this.context);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.rootView = layoutInflater.inflate(R.layout.download, viewGroup, false);
        this.isTransactionSafe = true;
        initUI();
        return this.rootView;
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() { // from class: com.motorola.ccc.ota.ui.DownloadFragment$$ExternalSyntheticLambda0
            @Override // android.view.View.OnKeyListener
            public final boolean onKey(View view2, int i, KeyEvent keyEvent) {
                return DownloadFragment.this.m172lambda$onViewCreated$0$commotorolacccotauiDownloadFragment(view2, i, keyEvent);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$onViewCreated$0$com-motorola-ccc-ota-ui-DownloadFragment  reason: not valid java name */
    public /* synthetic */ boolean m172lambda$onViewCreated$0$commotorolacccotauiDownloadFragment(View view, int i, KeyEvent keyEvent) {
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

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.FINISH_DOWNLOAD_ACTIVITY);
        BroadcastUtils.registerLocalReceiver(this.context, this.mFinishReceiver, intentFilter);
        UpdaterUtils.disableBatteryStatusReceiver();
        Intent targetIntent = getTargetIntent(this.activityIntent);
        this.mTargetIntent = targetIntent;
        this.mPendingIntent = PendingIntent.getBroadcast(this.context, 0, UpdaterUtils.fillAnnoyValueExpiryDetails(NotificationUtils.KEY_DOWNLOAD, targetIntent), 335544320);
        UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(this.activityIntent);
        this.upgradeInfo = upgradeInfoDuringOTAUpdate;
        if (upgradeInfoDuringOTAUpdate == null) {
            Logger.error("OtaApp", "DownloadActivity, No upgradeInfo found.");
            this.mDoNotShowNotification = true;
            this.activity.finishAndRemoveTask();
            return;
        }
        this.mUpdateType = UpdateType.getUpdateType(upgradeInfoDuringOTAUpdate.getUpdateTypeData());
        this.updateTypeString = this.upgradeInfo.getUpdateTypeData();
        long j = this.settings.getLong(Configs.STATS_PRE_DL_PACKAGE_NOTIFIED, 0L);
        long j2 = 1296000000 + j;
        long currentTimeMillis = System.currentTimeMillis();
        int i = (j <= 0 || j2 >= currentTimeMillis) ? 0 : (int) (((currentTimeMillis - j2) / 86400000) + 15);
        this.notificationTitle = this.mUpdateType.getPDLNotificationTitle();
        if (i > 15) {
            this.notificationTxt = getResources().getString(this.mUpdateType.getSystemUpdateAvailablePendingNotificationText(), Integer.valueOf(i));
        } else {
            this.notificationTxt = this.mUpdateType.getSystemUpdateAvailableNotificationText();
        }
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
        this.mPromptCount = this.settings.getInt(Configs.STATS_DOWNLOAD_ACTIVITY_PROMPT_COUNT, 0);
        this.mWifiOnly = this.upgradeInfo.isWifiOnly();
        this.mShowDownloadOptions = this.upgradeInfo.showDownloadOptions();
        this.mByPassPreDownloadDialog = this.upgradeInfo.getByPassPreDownloadDialog();
        this.mForced = this.upgradeInfo.isForced();
        if (!UpdaterUtils.isPromptAllowed(this.context, NotificationUtils.KEY_DOWNLOAD)) {
            Logger.info("OtaApp", "OTA download postponed by phone call/ECB/roaming");
            setDeferTimeAutomatically(this.upgradeInfo.getDelay(this.mPromptCount));
            this.activity.finish();
            return;
        }
        this.mLocationType = this.upgradeInfo.getLocationType();
        setUpgradeNotification(this.mPreDownloadNotes, this.upgradeInfo.hasUpgradeNotification(), this.upgradeInfo.getUpgradeNotification(), this.upgradeInfo.getOSreleaseLink(), this.upgradeInfo.getTargetOSVersion());
        ExpandableListView expandableList = UpdaterUtils.setExpandableList(this.expandableListView, this.upgradeInfo.hasReleaseNotes(), this.upgradeInfo.getReleaseNotes(), this.mExpTitle, getActivity(), this.mUpdateType, this.rootView.findViewById(R.id.dash_line));
        this.expandableListView = expandableList;
        this.expandableListView = UpdaterUtils.handleExpList(expandableList, "download");
        UpdaterUtils.setOsReleaseNotes(this.context, this.osReleaseNotes, this.upgradeInfo, "download");
        setPreDownloadInstructions();
        String version = this.upgradeInfo.getVersion();
        if (version == null) {
            Logger.error("OtaApp", "DownloadActivity, invalid version parameter.");
            this.mDoNotShowNotification = true;
            this.activity.finishAndRemoveTask();
            return;
        }
        if (!UpdaterUtils.FOTA.equals(this.mLocationType)) {
            initView(this.upgradeInfo);
        }
        Logger.debug("OtaApp", "DownloadActivity, handling (" + version + ") from ( " + this.mLocationType + ")");
        setCommonDisplay(this.upgradeInfo.isForced(), this.mLocationType, this.mWifiOnly);
        UpdaterUtils.setInstructions(this.upgradeInfo.getPreDownloadInstructions(), this.upgradeInfo.hasPreDownloadInstructions(), this.mLinearLayout, getActivity(), this.mUpdateType);
        if (this.upgradeInfo.isForced() && this.upgradeInfo.isTimeout(this.mPromptCount)) {
            Logger.info("OtaApp", "annoy value reminder's expired, show yes I got it option only");
            this.mLaterBtn.setVisibility(8);
            this.mCancelBtn.setVisibility(8);
            this.mDownloadBtn.setText(getResources().getString(R.string.defer_count_expired));
        }
        if (!this.upgradeInfo.isForced() && UpdaterUtils.optionalUpdateDeferCountExpired(this.upgradeInfo.getOptionalDeferCount(), this.mPromptCount)) {
            Logger.info("OtaApp", "Exceed max defer attempts for optional update, suppresing later btn");
            this.mLaterBtn.setVisibility(8);
            this.mCancelBtn.setVisibility(0);
        }
        if (this.settings.getBoolean(Configs.DOWNLOAD_REQ_FROM_NOTIFY)) {
            handleDownloadOption("userInitiatedDLFromNotification");
            this.settings.removeConfig(Configs.DOWNLOAD_REQ_FROM_NOTIFY);
        }
        setDeferTimeAutomatically(this.upgradeInfo.getDelay(this.mPromptCount));
    }

    public void onResume() {
        super.onResume();
        this.launchTimeInMillis = System.currentTimeMillis();
        NotificationUtils.stopNotificationService(this.context);
        this.isTransactionSafe = true;
    }

    public void onPause() {
        super.onPause();
        this.isTransactionSafe = false;
    }

    public void onStop() {
        super.onStop();
        if (!this.mDoNotShowNotification) {
            showDownloadNotification();
        }
        this.settings.setLong(Configs.TOTAL_TIME_SPEND_ON_DOWNLOAD_SCREEN, this.settings.getLong(Configs.TOTAL_TIME_SPEND_ON_DOWNLOAD_SCREEN, 0L) + (System.currentTimeMillis() - this.launchTimeInMillis));
    }

    public void onDestroy() {
        super.onDestroy();
        this.settings.incrementPrefs(Configs.STATS_DOWNLOAD_ACTIVITY_PROMPT_COUNT);
        Logger.debug("OtaApp", "Download fragment prompt count " + this.settings.getInt(Configs.STATS_DOWNLOAD_ACTIVITY_PROMPT_COUNT, 0));
        BroadcastUtils.unregisterLocalReceiver(this.context, this.mFinishReceiver);
        UpdaterUtils.stopWarningAlertDialog(this.context);
        UpdaterUtils.stopDownloadOptionsFragment(this.context);
    }

    void initView(UpdaterUtils.UpgradeInfo upgradeInfo) {
        setVersionOTA(this.mVersion, getString(R.string.download_version, new Object[]{upgradeInfo.getDisplayVersion(), getResources().getString(R.string.total_download_size, Formatter.formatFileSize(getContext(), upgradeInfo.getSize()))}));
    }

    private void initUI() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.activityIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
        this.mVersion = (TextView) this.rootView.findViewById(R.id.download_version);
        this.mPreDownloadNotes = (TextView) this.rootView.findViewById(R.id.predownloadnotes);
        handleButtons();
        this.mExpTitle = (TextView) this.rootView.findViewById(R.id.expTitle);
        this.expandableListView = (ExpandableListView) this.rootView.findViewById(R.id.expandableListView);
        this.osReleaseNotes = (TextView) this.rootView.findViewById(R.id.os_release_notes);
        this.mLinearLayout = (LinearLayout) this.rootView.findViewById(R.id.linearLayout);
    }

    private void handleButtons() {
        Button button = (Button) this.rootView.findViewById(R.id.no_thanks);
        this.mCancelBtn = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DownloadFragment.this.handleCancelOption();
            }
        });
        Button button2 = (Button) this.rootView.findViewById(R.id.download_now);
        this.mDownloadBtn = button2;
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DownloadFragment.this.handleDownloadOption("userInitiated");
            }
        });
        Button button3 = (Button) this.rootView.findViewById(R.id.no_maybe_later);
        this.mLaterBtn = button3;
        button3.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DownloadFragment.this.handleDownloadLaterOption();
                if (DownloadFragment.this.mPromptCount >= 5) {
                    DownloadFragment.this.showTrySmartUpdatePopUp();
                } else {
                    DownloadFragment.this.getActivity().finish();
                }
            }
        });
        ImageView imageView = (ImageView) this.rootView.findViewById(R.id.imgBack);
        this.imgBack = imageView;
        imageView.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                DownloadFragment.this.activity.finish();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showTrySmartUpdatePopUp() {
        if (SmartUpdateUtils.shouldShowTrySmartUpdatePopUp(this.settings, this.updateTypeString, true)) {
            SmartUpdateUtils.showTrySmartUpdatePopUp(getContext(), getContext().getResources().getString(R.string.smart_update_recommendation), this.mUpdateType, this.activity, this.settings, SmartUpdateUtils.SmartUpdateLaunchMode.TRY_SMART_UPDATES_POPUP_DOWNLOAD.name());
            return;
        }
        this.activity.finish();
    }

    void handleCancelOption() {
        Logger.info("OtaApp", "User declined to accept the upgrade, setting don't annoy user preferences");
        UpdaterUtils.setDontBotherPreferences(this.upgradeInfo.getOptionalUpdateCancelReminderDays());
        cancel();
        displayDialog(2, null);
    }

    void handleDownloadLaterOption() {
        Logger.info("OtaApp", "OTA download postponed by user");
        handleLaterOption();
    }

    private void handleLaterOption() {
        int delay = this.upgradeInfo.getDelay(this.mPromptCount);
        Logger.info("OtaApp", "Next download reminder at status bar scheduled in: " + delay + " mins.");
        long j = delay;
        this.mNextPrompt = System.currentTimeMillis() + (60000 * j);
        PendingIntent service = PendingIntent.getService(this.context, 0, NotificationUtils.fillDownloadLaterNotificationDetails(this.context, this.notificationTitle, this.notificationTxt, this.mTargetIntent), 335544320);
        this.mDoNotShowNotification = true;
        this.mAlarmManager.cancel(this.mPendingIntent);
        this.mAlarmManager.setExactAndAllowWhileIdle(0, this.mNextPrompt, service);
        UpdaterUtils.setDeferStats(this.context, UpgradeUtilConstants.DOWNLOAD, false, j);
        UpdaterUtils.saveNextPrompt(this.mNextPrompt);
    }

    void handleDownloadOption(String str) {
        Logger.info("OtaApp", "OTA download accepted by user!");
        if ("sdcard".equals(this.mLocationType)) {
            Logger.debug("OtaApp", "Sdcard update,proceed with download");
            downloadNow(str);
        } else if (!NetworkUtils.isNetWorkConnected(this.cm)) {
            displayDialog(4, str);
        } else if (UpdaterUtils.isDeviceInDatasaverMode()) {
            displayDialog(3, str);
        } else if (UpdaterUtils.isBatteryLowToStartDownload(this.context) && !this.mShowDownloadOptions) {
            UpdaterUtils.enableReceiversForBatteryLow();
            downloadNow(str);
        } else if (BuildPropReader.isBotaATT() || (this.mWifiOnly && !this.mShowDownloadOptions)) {
            Logger.debug("OtaApp", "Wi-Fi only package or ATT, proceed with download");
            downloadNow(str);
        } else if (this.mShowDownloadOptions) {
            displayDialog(1, str);
        } else {
            downloadNow(str);
        }
    }

    private void displayDialog(int i, String str) {
        if (this.isTransactionSafe) {
            if (i == 1) {
                displayUserChoiceDialogInternal("DownloadOptions", str);
            } else if (i == 2) {
                displayDialogInternal(2, "Dialog_Cancel", getResources().getString(R.string.download_cancelled_title), getResources().getString(R.string.no_thanks_message));
            } else if (i == 3) {
                displayDialogInternal(3, "data_saver", this.mUpdateType.getSystemUpdatePausedNotificationTitle(), getResources().getString(R.string.request_suspended_datasaver));
            } else if (i != 4) {
            } else {
                displayDialogInternal(4, "no_network", this.mUpdateType.getSystemUpdatePausedNotificationTitle(), getResources().getString(R.string.request_suspended_no_network));
            }
        }
    }

    private Intent getTargetIntent(Intent intent) {
        return UpgradeUtilMethods.getUpdateNotificationIntent(intent.getStringExtra(UpgradeUtilConstants.KEY_VERSION), this.settings.getString(Configs.METADATA), intent.getStringExtra(UpgradeUtilConstants.KEY_LOCATION_TYPE), intent.getStringExtra(UpgradeUtilConstants.KEY_DOWNLOAD_OPT_CHECK));
    }

    public void showDownloadNotification() {
        this.mAlarmManager.cancel(this.mPendingIntent);
        long currentTimeMillis = System.currentTimeMillis() + (NotificationUtils.getPreDownloadNotificationExpiryMins() * 60000);
        this.mNextPrompt = currentTimeMillis;
        UpdaterUtils.saveNextPrompt(currentTimeMillis);
        NotificationUtils.startNotificationService(OtaApplication.getGlobalContext(), NotificationUtils.fillDownloadLaterNotificationDetails(OtaApplication.getGlobalContext(), this.notificationTitle, this.notificationTxt, this.mTargetIntent));
        UpdaterUtils.setDeferStats(this.context, UpgradeUtilConstants.DOWNLOAD, false, NotificationUtils.getPreDownloadNotificationExpiryMins());
    }

    void setUpgradeNotification(TextView textView, boolean z, String str, String str2, String str3) {
        if (z) {
            Logger.debug("OtaApp", "DownloadActivity, FormattedText");
            try {
                textView.setText(Html.fromHtml(str, 0, null, new HtmlUtils(str)));
                textView.setVisibility(0);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            } catch (Exception e) {
                Logger.error("OtaApp", "DownloadActivity, error setting PreDownloadNotes." + e);
            }
        }
    }

    private void setPreDownloadInstructions() {
        Context context = this.context;
        createBulletTextView(context, this.mLinearLayout, context.getResources().getString(R.string.secure_instruction));
        Context context2 = this.context;
        createBulletTextView(context2, this.mLinearLayout, context2.getResources().getString(R.string.personalInfo_instruction));
        if (BuildPropReader.isUEUpdateEnabled()) {
            Context context3 = this.context;
            createBulletTextView(context3, this.mLinearLayout, context3.getResources().getString(R.string.download_note));
        }
    }

    public static void createBulletTextView(Context context, LinearLayout linearLayout, String str) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(0, 20, 0, 0);
        layoutParams.setMarginStart(0);
        layoutParams.setMarginEnd(0);
        TextView textView = (TextView) ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.instruction_style, (ViewGroup) null);
        textView.setLayoutParams(layoutParams);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append((CharSequence) str);
        spannableStringBuilder.setSpan(new BulletSpan(24, context.getResources().getColor(R.color.black), 3), 0, spannableStringBuilder.length(), 33);
        spannableStringBuilder.setSpan(new StyleSpan(0), 0, spannableStringBuilder.length(), 33);
        textView.setText(spannableStringBuilder);
        linearLayout.addView(textView);
    }

    void setCommonDisplay(boolean z, String str, boolean z2) {
        if (BuildPropReader.isStreamingUpdate()) {
            this.mDownloadBtn.setText(getResources().getString(R.string.ab_download_now));
            this.mLaterBtn.setText(getResources().getString(R.string.ab_download_later));
        }
        if ("sdcard".equals(str)) {
            this.mLaterBtn.setVisibility(8);
            this.mCancelBtn.setVisibility(0);
            String string = getResources().getString(R.string.sdcard_predownload_text);
            this.mPreDownloadNotes.setVisibility(0);
            this.mPreDownloadNotes.setText(string);
            return;
        }
        this.mCancelBtn.setVisibility(8);
        this.mLaterBtn.setVisibility(0);
    }

    void downloadNow(String str) {
        String str2;
        String error;
        this.mDoNotShowNotification = true;
        this.mAlarmManager.cancel(this.mPendingIntent);
        String stringExtra = this.activityIntent.getStringExtra(UpgradeUtilConstants.KEY_VERSION);
        if (!this.mShowDownloadOptions || (this.mForced && UpdaterUtils.bypassPreDownloadDialog(this.mByPassPreDownloadDialog))) {
            str2 = null;
        } else if (this.settings.getInt(Configs.USERCHOICE, 0) == UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI.ordinal()) {
            str2 = UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI.name();
        } else {
            str2 = UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI_AND_MOBILE.name();
            UpdaterUtils.setAutomaticDownloadForCellular(true);
        }
        if (UpdaterUtils.isBatteryLowToStartDownload(this.context)) {
            UpdaterUtils.enableReceiversForBatteryLow();
        }
        UpgradeUtilMethods.sendUpdateNotificationResponse(this.context, stringExtra, true, str2, str);
        this.settings.removeConfig(Configs.USERCHOICE);
        NotificationUtils.clearNextPromptDetails(this.settings);
        if (BuildPropReader.isFotaATT()) {
            this.activity.finish();
            return;
        }
        Bundle bundle = new Bundle();
        if (BuildPropReader.isStreamingUpdate()) {
            error = UpgradeUtils.Error.ERR_BACKGROUND_INSTALL.toString();
        } else {
            error = UpgradeUtils.Error.ERR_DOWNLOADING.toString();
        }
        bundle.putString(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE, error);
        ((FragmentActionListener) this.activity).onUpdateActionResponse(bundle);
    }

    void cancel() {
        this.mDoNotShowNotification = true;
        this.mAlarmManager.cancel(this.mPendingIntent);
        UpgradeUtilMethods.sendUpdateNotificationResponse(this.context, this.activityIntent.getStringExtra(UpgradeUtilConstants.KEY_VERSION), false, null, null);
        this.settings.removeConfig(Configs.USERCHOICE);
        NotificationUtils.clearNextPromptDetails(this.settings);
    }

    void setVersionOTA(TextView textView, String str) {
        textView.setText(str);
    }

    private void setDeferTimeAutomatically(int i) {
        PendingIntent service = PendingIntent.getService(this.context, 0, NotificationUtils.fillDownloadLaterNotificationDetails(this.context, this.notificationTitle, this.notificationTxt, this.mTargetIntent), 335544320);
        if (service != null) {
            Logger.debug("OtaApp", "cancelling the previous pending intent set for download later");
            this.mAlarmManager.cancel(service);
        }
        Logger.debug("OtaApp", "next automatic download prompt (serverAnnoy)  is scheduled in : " + i + " mins.");
        this.mAlarmManager.set(0, System.currentTimeMillis() + (i * 60000), this.mPendingIntent);
    }

    private void displayUserChoiceDialogInternal(String str, String str2) {
        UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(this.activityIntent);
        if (upgradeInfoDuringOTAUpdate == null) {
            return;
        }
        DownloadOptionsFragment newInstance = DownloadOptionsFragment.newInstance(upgradeInfoDuringOTAUpdate.getUpdateTypeData(), upgradeInfoDuringOTAUpdate.getSize(), str2, 1);
        newInstance.setTargetFragment(this, 0);
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            newInstance.show(fragmentManager, str);
        }
    }

    private void displayDialogInternal(int i, String str, String str2, String str3) {
        WarningAlertDialog.WarningAlertDialogBuilder warningAlertDialogBuilder = new WarningAlertDialog.WarningAlertDialogBuilder(i);
        warningAlertDialogBuilder.setTitle(str2).setMessage(str3).setPositiveText(getResources().getString(R.string.alert_dialog_ok)).setButtonColor(R.color.black);
        WarningAlertDialog buildDialog = warningAlertDialogBuilder.buildDialog();
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            buildDialog.show(fragmentManager, str);
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 0) {
            if (i2 == 0) {
                this.activity.finishAndRemoveTask();
            } else if (i2 == 2) {
                int intExtra = intent.getIntExtra(DownloadOptionsFragment.ARG_PARAM_ID, UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI.ordinal());
                String stringExtra = intent.getStringExtra(DownloadOptionsFragment.ARG_PARAM_MODE);
                this.settings.setInt(Configs.USERCHOICE, intExtra);
                if (!NetworkUtils.isNetWorkConnected(this.cm)) {
                    displayDialog(4, stringExtra);
                } else if (UpdaterUtils.isDeviceInDatasaverMode()) {
                    displayDialog(3, stringExtra);
                } else {
                    if (UpdaterUtils.isBatteryLowToStartDownload(this.context)) {
                        UpdaterUtils.enableReceiversForBatteryLow();
                    }
                    downloadNow(stringExtra);
                }
            }
        }
    }
}
