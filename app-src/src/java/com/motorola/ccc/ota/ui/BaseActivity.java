package com.motorola.ccc.ota.ui;

import android.app.UiModeManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.OtaMainBroadcastReceiver;
import com.motorola.ccc.ota.env.OtaService;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class BaseActivity extends AppCompatActivity implements FragmentActionListener, UpdaterUtils.OnDialogInteractionListener {
    private static final String VITAL_CHECK_FOR_UPDATE_INTENT = "com.motorola.ccc.ota.CHECK_FOR_VITAL_UPDATE";
    public static Intent vitalUpdateLaunchIntent;
    private CheckUpdateFragment checkUpdateFragment;
    private TextView checkUpdateText;
    private String errorCodeToRefresh;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    private boolean isRunning;
    private Bundle responseBundle;
    private BotaSettings settings;
    private DialogFragment smartUpdateDialogFragment;
    private DialogFragment updatePreferenceDialogFragment;
    private BroadcastReceiver updateReceiver;
    private VitalCheckUpdateFragment vitalCheckUpdateFragment;
    private static final String ERR_OK = UpgradeUtils.Error.ERR_OK.toString();
    private static final String ERR_ALREADY = UpgradeUtils.Error.ERR_ALREADY.toString();
    private static final String ERR_REQUESTING = UpgradeUtils.Error.ERR_REQUESTING.toString();
    private static final String ERR_DOWNLOADING = UpgradeUtils.Error.ERR_DOWNLOADING.toString();
    private static final String ERR_CONTACTSERVER = UpgradeUtils.Error.ERR_CONTACTING_SERVER.toString();
    private static final String ERR_NET = UpgradeUtils.Error.ERR_NET.toString();
    private static final String ERR_POLICY_SET = UpgradeUtils.Error.ERR_POLICY_SET.toString();
    private static final String ERR_NOTFOUND = UpgradeUtils.Error.ERR_NOTFOUND.toString();
    private static final String ERR_NOTFOUND_ROOTED = UpgradeUtils.Error.ERR_NOTFOUND_ROOTED.toString();
    private static final String ERR_VERITY_DISABLED = UpgradeUtils.Error.ERR_VERITY_DISABLED.toString();
    private static final String ERR_NOTFOUND_BOOT_UNLOCK = UpgradeUtils.Error.ERR_NOTFOUND_BOOT_UNLOCK.toString();
    private static final String ERR_VU_WIFI_ONLY_WIFI_NOT_AVAILABLE = UpgradeUtils.Error.ERR_VU_WIFI_ONLY_WIFI_NOT_AVAILABLE.toString();
    private static final String ERR_NOTFOUND_DEVICE_CORRUPTED = UpgradeUtils.Error.ERR_NOTFOUND_DEVICE_CORRUPTED.toString();
    private static final String ERR_NOTFOUND_ADV_NOTICE = UpgradeUtils.Error.ERR_NOTFOUND_ADV_NOTICE.toString();
    private static final String ERR_NOTFOUND_EOL = UpgradeUtils.Error.ERR_NOTFOUND_EOL.toString();
    private static final String ERR_TEMP = UpgradeUtils.Error.ERR_TEMP.toString();
    private static final String ERR_FAIL = UpgradeUtils.Error.ERR_FAIL.toString();
    private static final String ERR_BADPARAM = UpgradeUtils.Error.ERR_BADPARAM.toString();
    private static final String ERR_WIFI_NEEDED = UpgradeUtils.Error.ERR_WIFI_NEEDED.toString();
    private static final String ERR_INTERNAL = UpgradeUtils.Error.ERR_INTERNAL.toString();
    private static final String ERR_INIT_OTA_SERVICE = UpgradeUtils.Error.ERR_INIT_OTA_SERVICE.toString();
    private static final String ERR_VAB_VALIDATION = UpgradeUtils.Error.ERR_VAB_VALIDATION.toString();
    private static final String ERR_VAB_VALIDATION_SUCCESS = UpgradeUtils.Error.ERR_VAB_VALIDATION_SUCCESS.toString();
    private static final String ERR_VAB_VALIDATION_FAILURE = UpgradeUtils.Error.ERR_VAB_VALIDATION_FAILURE.toString();
    private static final String ERR_ROAMING = UpgradeUtils.Error.ERR_ROAMING.toString();
    private static final String ERR_IN_CALL = UpgradeUtils.Error.ERR_IN_CALL.toString();
    private static final String ERR_STORAGE_LOW = UpgradeUtils.Error.ERR_STORAGE_LOW.toString();
    private static final String ERR_BACKGROUND_INSTALL = UpgradeUtils.Error.ERR_BACKGROUND_INSTALL.toString();
    private static final String ERR_VAB_MERGE_PENDING = UpgradeUtils.Error.ERR_VAB_MERGE_PENDING.toString();
    private static final String ERR_VAB_MERGE_RESTART = UpgradeUtils.Error.ERR_VAB_MERGE_RESTART.toString();
    private static final String ERR_CTA_BG_DATA_DISABLED = UpgradeUtils.Error.ERR_CTA_BG_DATA_DISABLED.toString();

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        UpgradeUtilConstants.FragmentTypeEnum fragmentTypeEnum;
        super.onCreate(bundle);
        Logger.debug("OtaApp", "BaseActivity, onCreate: Intent Action" + getIntent().getAction());
        this.isRunning = true;
        this.settings = new BotaSettings();
        Context globalContext = OtaApplication.getGlobalContext();
        if (!UpgradeUtilMethods.isSystemUser(this)) {
            Toast.makeText((Context) this, (CharSequence) getString(R.string.check_update_not_sys_user), 1).show();
            finish();
            return;
        }
        if (VITAL_CHECK_FOR_UPDATE_INTENT.equals(getIntent().getAction())) {
            CusAndroidUtils.registerCheckUpdateActions(this, new OtaMainBroadcastReceiver());
            if (!UpdaterUtils.isOtaServiceRunning(globalContext)) {
                Logger.debug("OtaApp", "OTA service is started for vital check update");
                Intent intent = new Intent((Context) this, (Class<?>) OtaService.class);
                intent.putExtra(UpgradeUtilConstants.KEY_SERVICE_STARTED_ON_CHK_UPDATE, true);
                startService(intent);
            }
            if (!this.settings.getBoolean(Configs.INITIAL_SETUP_COMPLETED)) {
                this.settings.setBoolean(Configs.FLAG_IS_VITAL_UPDATE, true);
                this.settings.setVitalUpdateValues();
                vitalUpdateLaunchIntent = getIntent();
            } else {
                vitalUpdateLaunchIntent = getIntent();
                UpdaterUtils.launchNextSetupActivityVitalUpdate(this);
                finish();
                return;
            }
        } else if (this.settings.getBoolean(Configs.INITIAL_SETUP_COMPLETED) && this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            Logger.debug("OtaApp", "resetting vital update flag");
            this.settings.removeConfig(Configs.FLAG_IS_VITAL_UPDATE);
        }
        if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            setTheme(R.style.VitalUpdateBaseActivityTheme);
        } else {
            setRequestedOrientation(1);
            setTheme(R.style.BaseActivityTheme);
        }
        setContentView(R.layout.ota_activity_main);
        this.fragmentManager = getSupportFragmentManager();
        Window window = getWindow();
        if (!this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            window.addFlags(Integer.MIN_VALUE);
            window.setStatusBarColor(0);
            window.setNavigationBarColor(0);
        }
        if (!UpdaterUtils.isBatterySaverEnabled(this)) {
            WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
            if (((UiModeManager) getSystemService("uimode")).getNightMode() != 2) {
                windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
                windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
            }
        }
        registerActionUpdateReceiver();
        boolean isOtaServiceRunning = UpdaterUtils.isOtaServiceRunning(globalContext);
        if (!isOtaServiceRunning) {
            Logger.debug("OtaApp", "ota service running status " + isOtaServiceRunning);
            Bundle bundle2 = new Bundle();
            bundle2.putString(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE, UpgradeUtils.Error.ERR_INIT_OTA_SERVICE.toString());
            onUpdateActionResponse(bundle2);
            Intent intent2 = new Intent((Context) this, (Class<?>) OtaService.class);
            intent2.putExtra(UpgradeUtilConstants.KEY_SERVICE_STARTED_ON_CHK_UPDATE, true);
            startService(intent2);
            return;
        }
        String stringExtra = getIntent().getStringExtra(UpgradeUtilConstants.FRAGMENT_TYPE);
        if (5 == getIntent().getIntExtra(UpgradeUtilConstants.NOTIFICATION_ID, 0)) {
            NotificationUtils.cancelSmartUpdateNotification();
        }
        UpgradeUtilConstants.FragmentTypeEnum fragmentTypeEnum2 = null;
        try {
            fragmentTypeEnum = UpgradeUtilConstants.FragmentTypeEnum.valueOf(stringExtra);
        } catch (IllegalArgumentException | NullPointerException e) {
            Logger.debug("OtaApp", "Exception in getting fragment type, BaseActivity, onCreate: " + e);
            fragmentTypeEnum = null;
        }
        if ((getIntent().getFlags() & 1048576) != 0) {
            Logger.debug("OtaApp", "activity launched from recent apps");
        } else {
            fragmentTypeEnum2 = fragmentTypeEnum;
        }
        if (getIntent().getComponent().toString().contains(".OTASuggestion")) {
            this.settings.incrementPrefs(Configs.STATS_SMART_UPDATE_USER_VISITED_SUGGESTIONS);
            getIntent().putExtra(UpgradeUtilConstants.KEY_LAUNCH_MODE, SmartUpdateUtils.SmartUpdateLaunchMode.SMART_UPDATE_SUGGESTIONS.name());
            fragmentTypeEnum2 = UpgradeUtilConstants.FragmentTypeEnum.SMART_UPDATE_FRAGMENT;
        }
        initFragment(fragmentTypeEnum2);
    }

    protected void onNewIntent(Intent intent) {
        UpgradeUtilConstants.FragmentTypeEnum fragmentTypeEnum;
        super.onNewIntent(intent);
        Logger.debug("OtaApp", "New Intent : " + intent);
        setIntent(intent);
        try {
            fragmentTypeEnum = UpgradeUtilConstants.FragmentTypeEnum.valueOf(getIntent().getStringExtra(UpgradeUtilConstants.FRAGMENT_TYPE));
        } catch (IllegalArgumentException | NullPointerException e) {
            Logger.debug("OtaApp", "Exception in getting fragment type, BaseActivity, onNewIntent: " + e);
            fragmentTypeEnum = null;
        }
        initFragment(fragmentTypeEnum);
    }

    protected void onResume() {
        super.onResume();
        this.isRunning = true;
    }

    protected void onPostResume() {
        super.onPostResume();
        Bundle bundle = this.responseBundle;
        if (bundle != null) {
            performCheckUpdateAction(bundle);
            this.responseBundle = null;
        }
    }

    protected void onPause() {
        super.onPause();
        this.isRunning = false;
    }

    protected void onDestroy() {
        super.onDestroy();
        BroadcastReceiver broadcastReceiver = this.updateReceiver;
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            this.updateReceiver = null;
        }
    }

    private void registerActionUpdateReceiver() {
        if (this.updateReceiver == null) {
            IntentFilter intentFilter = new IntentFilter(UpgradeUtilConstants.UPGRADE_ACTION_UPDATE_RESPONSE);
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.BaseActivity.1
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE);
                    Logger.debug("OtaApp", "Received update action response, code: " + stringExtra);
                    Bundle bundle = new Bundle();
                    bundle.putString(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE, stringExtra);
                    bundle.putParcelable(UpgradeUtilConstants.KEY_CHECK_RESPONSE_INTENT, intent);
                    BaseActivity.this.onUpdateActionResponse(bundle);
                }
            };
            this.updateReceiver = broadcastReceiver;
            registerReceiver(broadcastReceiver, intentFilter, 2);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void initFragment(UpgradeUtilConstants.FragmentTypeEnum fragmentTypeEnum) {
        SmartUpdateUtils.dismissSmartDialogs(this);
        Logger.debug("OtaApp", "fragment type is " + fragmentTypeEnum);
        if (fragmentTypeEnum == null) {
            sendBroadcast(new Intent(UpdaterUtils.ACTION_MANUAL_CHECK_UPDATE), Permissions.INTERACT_OTA_SERVICE);
            UpdaterUtils.sendCheckUpdateIntent(this);
            return;
        }
        Intent intent = getIntent();
        intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, fragmentTypeEnum);
        setIntent(intent);
        switch (AnonymousClass2.$SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum[fragmentTypeEnum.ordinal()]) {
            case 1:
                if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                    UpdaterUtils.launchNextSetupActivityVitalUpdate(this);
                    return;
                } else {
                    addUpdateCompleteFragment(getIntent());
                    return;
                }
            case 2:
                addUpdateFailedFragment(getIntent());
                return;
            case 3:
                addDownloadFragment(getIntent());
                return;
            case 4:
                addDownloadProgressFragment(getIntent());
                return;
            case 5:
                addBackgroundInstallationFragment(getIntent());
                return;
            case 6:
                addRestartFragment(getIntent());
                return;
            case 7:
                addMergeRestartFragment(getIntent());
                return;
            case 8:
                addSmartUpdateFragment(getIntent());
                return;
            case 9:
                addUpdatePreferenceFragment(getIntent());
                return;
            default:
                sendBroadcast(new Intent(UpdaterUtils.ACTION_MANUAL_CHECK_UPDATE), Permissions.INTERACT_OTA_SERVICE);
                UpdaterUtils.sendCheckUpdateIntent(this);
                return;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.ui.BaseActivity$2  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum;

        static {
            int[] iArr = new int[UpgradeUtilConstants.FragmentTypeEnum.values().length];
            $SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum = iArr;
            try {
                iArr[UpgradeUtilConstants.FragmentTypeEnum.UPDATE_COMPLETE_FRAGMENT.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum[UpgradeUtilConstants.FragmentTypeEnum.UPDATE_FAILED_FRAGMENT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum[UpgradeUtilConstants.FragmentTypeEnum.DOWNLOAD_FRAGMENT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum[UpgradeUtilConstants.FragmentTypeEnum.DOWNLOAD_PROGRESS_FRAGMENT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum[UpgradeUtilConstants.FragmentTypeEnum.BACKGROUND_INSTALLATION_FRAGMENT.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum[UpgradeUtilConstants.FragmentTypeEnum.RESTART_FRAGMENT.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum[UpgradeUtilConstants.FragmentTypeEnum.MERGE_RESTART_FRAGMENT.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum[UpgradeUtilConstants.FragmentTypeEnum.SMART_UPDATE_FRAGMENT.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$utils$UpgradeUtilConstants$FragmentTypeEnum[UpgradeUtilConstants.FragmentTypeEnum.UPDATE_PREF_FRAGMENT.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void addCheckUpdateFragment(String str, String str2) {
        Bundle bundle = new Bundle();
        bundle.putString(UpdaterUtils.KEY_CHK_TITLE, str);
        bundle.putString(UpdaterUtils.KEY_CHK_BODY, str2);
        this.fragmentTransaction = this.fragmentManager.beginTransaction();
        if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            if (this.settings.getBoolean(Configs.VAB_MERGE_PROCESS_RUNNING)) {
                Logger.debug("OtaApp", "Vital update request and merge process is running so don't send check update request; finish the activity");
                UpdaterUtils.launchNextSetupActivityVitalUpdate(this);
                finish();
                return;
            }
            VitalCheckUpdateFragment vitalCheckUpdateFragment = new VitalCheckUpdateFragment();
            this.vitalCheckUpdateFragment = vitalCheckUpdateFragment;
            vitalCheckUpdateFragment.setArguments(bundle);
            this.fragmentTransaction.replace((int) R.id.fragment_id, this.vitalCheckUpdateFragment, "VitalCheckUpdateFragment");
        } else {
            CheckUpdateFragment checkUpdateFragment = new CheckUpdateFragment();
            this.checkUpdateFragment = checkUpdateFragment;
            checkUpdateFragment.setArguments(bundle);
            this.fragmentTransaction.replace((int) R.id.fragment_id, this.checkUpdateFragment, "CheckUpdateFragment");
        }
        commit();
    }

    private void addDownloadFragment(Intent intent) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        this.fragmentTransaction = this.fragmentManager.beginTransaction();
        DownloadFragment downloadFragment = new DownloadFragment();
        downloadFragment.setArguments(bundle);
        this.fragmentTransaction.replace((int) R.id.fragment_id, downloadFragment);
        commit();
    }

    private void addDownloadProgressFragment(Intent intent) {
        Intent intent2 = getIntent();
        intent2.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.DOWNLOAD_PROGRESS_FRAGMENT.toString());
        setIntent(intent2);
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        this.fragmentTransaction = this.fragmentManager.beginTransaction();
        DownloadProgressFragment downloadProgressFragment = new DownloadProgressFragment();
        downloadProgressFragment.setArguments(bundle);
        this.fragmentTransaction.replace((int) R.id.fragment_id, downloadProgressFragment);
        commit();
    }

    private void addBackgroundInstallationFragment(Intent intent) {
        Intent intent2 = getIntent();
        intent2.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.BACKGROUND_INSTALLATION_FRAGMENT.toString());
        setIntent(intent2);
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        this.fragmentTransaction = this.fragmentManager.beginTransaction();
        if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            VitalBackgroundInstallationFragment vitalBackgroundInstallationFragment = new VitalBackgroundInstallationFragment();
            vitalBackgroundInstallationFragment.setArguments(bundle);
            this.fragmentTransaction.replace((int) R.id.fragment_id, vitalBackgroundInstallationFragment);
        } else {
            BackgroundInstallationFragment backgroundInstallationFragment = new BackgroundInstallationFragment();
            backgroundInstallationFragment.setArguments(bundle);
            this.fragmentTransaction.replace((int) R.id.fragment_id, backgroundInstallationFragment);
        }
        commit();
    }

    private void addRestartFragment(Intent intent) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        this.fragmentTransaction = this.fragmentManager.beginTransaction();
        if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            VitalRestartFragment vitalRestartFragment = new VitalRestartFragment();
            vitalRestartFragment.setArguments(bundle);
            this.fragmentTransaction.replace((int) R.id.fragment_id, vitalRestartFragment);
        } else {
            RestartFragment restartFragment = new RestartFragment();
            restartFragment.setArguments(bundle);
            this.fragmentTransaction.replace((int) R.id.fragment_id, restartFragment);
        }
        commit();
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void addMergeRestartFragment(Intent intent) {
        if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            Logger.debug("OtaApp", "Vital update request and merge process is running so don't send check update request; finish the activity");
            UpdaterUtils.launchNextSetupActivityVitalUpdate(this);
            finish();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        this.fragmentTransaction = this.fragmentManager.beginTransaction();
        MergeRestartFragment mergeRestartFragment = new MergeRestartFragment();
        mergeRestartFragment.setArguments(bundle);
        this.fragmentTransaction.replace((int) R.id.fragment_id, mergeRestartFragment);
        commit();
    }

    private void addUpdateCompleteFragment(Intent intent) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        this.fragmentTransaction = this.fragmentManager.beginTransaction();
        UpdateCompleteFragment updateCompleteFragment = new UpdateCompleteFragment();
        updateCompleteFragment.setArguments(bundle);
        this.fragmentTransaction.replace((int) R.id.fragment_id, updateCompleteFragment);
        commit();
    }

    private void addUpdateFailedFragment(Intent intent) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        bundle.putBoolean(UpdaterUtils.KEY_MERGE_FAILURE, intent.getBooleanExtra(UpdaterUtils.KEY_MERGE_FAILURE, false));
        this.fragmentTransaction = this.fragmentManager.beginTransaction();
        if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            VitalUpdateFailedFragment vitalUpdateFailedFragment = new VitalUpdateFailedFragment();
            vitalUpdateFailedFragment.setArguments(bundle);
            this.fragmentTransaction.replace((int) R.id.fragment_id, vitalUpdateFailedFragment);
        } else {
            UpdateFailedFragment updateFailedFragment = new UpdateFailedFragment();
            updateFailedFragment.setArguments(bundle);
            this.fragmentTransaction.replace((int) R.id.fragment_id, updateFailedFragment);
        }
        commit();
    }

    private void addSmartUpdateFragment(Intent intent) {
        if (this.isRunning) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
            SmartUpdateFragment smartUpdateFragment = new SmartUpdateFragment();
            this.smartUpdateDialogFragment = smartUpdateFragment;
            smartUpdateFragment.setArguments(bundle);
            this.smartUpdateDialogFragment.show(this.fragmentManager, "SmartUpdateDialog");
        }
    }

    private void addUpdatePreferenceFragment(Intent intent) {
        SmartUpdateUtils.launchUpdatePreferences(intent, this.fragmentManager, this.isRunning);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void addErrorFragment(Intent intent, String str, String str2) {
        if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
            UpdaterUtils.launchNextSetupActivityVitalUpdate(this);
            finish();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        bundle.putString(UpdaterUtils.KEY_ERR_MSG, str2);
        bundle.putString(UpdaterUtils.KEY_ERR_TITLE, str);
        this.fragmentTransaction = this.fragmentManager.beginTransaction();
        ErrorFragment errorFragment = new ErrorFragment();
        errorFragment.setArguments(bundle);
        this.fragmentTransaction.replace((int) R.id.fragment_id, errorFragment);
        commit();
    }

    void commit() {
        this.fragmentTransaction.commit();
    }

    @Override // com.motorola.ccc.ota.ui.FragmentActionListener
    public void onUpdateActionResponse(Bundle bundle) {
        if (!this.isRunning) {
            this.responseBundle = bundle;
        } else {
            performCheckUpdateAction(bundle);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void performCheckUpdateAction(Bundle bundle) {
        int i;
        String string = bundle.getString(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE);
        this.errorCodeToRefresh = string;
        String string2 = getString(R.string.failure_process);
        if (ERR_VAB_MERGE_PENDING.equals(string)) {
            addCheckUpdateFragment(getString(R.string.merge_progress_title), getString(R.string.merge_progress_text));
        } else if (ERR_VAB_MERGE_RESTART.equals(string)) {
            addMergeRestartFragment(getIntent());
        } else {
            String str = ERR_ALREADY;
            if (str.equals(string)) {
                UpdaterUtils.sDeviceIdleModeRequired = false;
            }
            if (ERR_OK.equals(string) || str.equals(string)) {
                if (BuildPropReader.isFotaATT()) {
                    finish();
                    return;
                }
                return;
            }
            String str2 = null;
            if (ERR_NET.equals(string)) {
                if (getResources().getBoolean(R.bool.check_WIFI_for_BOTA)) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService("connectivity");
                    for (Network network : connectivityManager.getAllNetworks()) {
                        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                        if (networkCapabilities != null) {
                            if (networkCapabilities.hasCapability(12) && networkCapabilities.hasTransport(1)) {
                                str2 = getString(R.string.check_update_internal);
                            } else {
                                string2 = getString(R.string.bad_connection);
                                str2 = getString(R.string.check_update_wifi_only);
                            }
                        } else {
                            string2 = getString(R.string.bad_connection);
                            if (UpdaterUtils.isVerizon() && !BuildPropertyUtils.isProductWaveAtleastRefWave("2024.2")) {
                                str2 = getString(R.string.check_update_net_vzw);
                            } else {
                                str2 = getString(R.string.check_update_net);
                            }
                        }
                    }
                } else {
                    string2 = getString(R.string.bad_connection);
                    if (UpdaterUtils.isVerizon() && !BuildPropertyUtils.isProductWaveAtleastRefWave("2024.2")) {
                        str2 = getString(R.string.check_update_net_vzw);
                    } else {
                        str2 = getString(R.string.check_update_net);
                    }
                }
            } else {
                String str3 = ERR_REQUESTING;
                if (str3.equals(string) || ERR_VAB_VALIDATION.equals(string)) {
                    boolean equals = str3.equals(string);
                    int i2 = R.string.check_update_checking;
                    if (!equals) {
                        i = R.string.check_update_vab_validation;
                    } else if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                        i2 = R.string.vital_check_for_updates;
                        i = R.string.vital_check_for_updates_desc;
                    } else {
                        i = R.string.check_update_text;
                    }
                    addCheckUpdateFragment(getString(i2), getString(i));
                } else if (ERR_DOWNLOADING.equals(string)) {
                    if (BuildPropReader.isFotaATT()) {
                        string2 = getString(R.string.check_update_title_downloading);
                        str2 = getString(R.string.check_update_downloading);
                    } else if (bundle.getParcelable(UpgradeUtilConstants.KEY_CHECK_RESPONSE_INTENT) != null) {
                        addDownloadProgressFragment((Intent) bundle.getParcelable(UpgradeUtilConstants.KEY_CHECK_RESPONSE_INTENT));
                    } else {
                        addDownloadProgressFragment(getIntent());
                    }
                } else if (ERR_CONTACTSERVER.equals(string)) {
                    string2 = getString(R.string.err_title_contact_server);
                    str2 = getString(R.string.check_update_processing);
                } else if (ERR_POLICY_SET.equals(string)) {
                    str2 = getString(R.string.policy_set);
                } else if (ERR_NOTFOUND.equals(string)) {
                    SystemUpdateStatusUtils.storeOngoingSystemUpdateStatus(OtaApplication.getGlobalContext(), new Intent(UpgradeUtilConstants.ALREADY_UP_TO_DATE));
                    if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                        UpdaterUtils.launchNextSetupActivityVitalUpdate(this);
                        finish();
                        return;
                    }
                    this.fragmentTransaction = this.fragmentManager.beginTransaction();
                    this.fragmentTransaction.replace((int) R.id.fragment_id, new UptoDateFragment());
                    commit();
                } else if (ERR_NOTFOUND_BOOT_UNLOCK.equals(string) || ERR_NOTFOUND_ROOTED.equals(string)) {
                    addErrorFragment(getIntent(), getString(R.string.bootloader_unlock_title), getString(R.string.bootloader_unlock_msg));
                } else if (ERR_VU_WIFI_ONLY_WIFI_NOT_AVAILABLE.equals(string)) {
                    addErrorFragment(getIntent(), "", "");
                } else if (ERR_NOTFOUND_DEVICE_CORRUPTED.equalsIgnoreCase(string)) {
                    addErrorFragment(getIntent(), getString(R.string.device_corrupted_title), getString(R.string.device_corrupted_msg));
                } else if (ERR_VERITY_DISABLED.equalsIgnoreCase(string)) {
                    addErrorFragment(getIntent(), getString(R.string.verity_disabled_title), getString(R.string.verity_disabled_msg));
                } else if (ERR_NOTFOUND_ADV_NOTICE.equals(string)) {
                    if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                        UpdaterUtils.launchNextSetupActivityVitalUpdate(this);
                        finish();
                        return;
                    }
                    this.fragmentTransaction = this.fragmentManager.beginTransaction();
                    this.fragmentTransaction.replace((int) R.id.fragment_id, new AdvNoticeFragment());
                    commit();
                } else if (ERR_NOTFOUND_EOL.equals(string)) {
                    if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                        UpdaterUtils.launchNextSetupActivityVitalUpdate(this);
                        finish();
                        return;
                    }
                    this.fragmentTransaction = this.fragmentManager.beginTransaction();
                    this.fragmentTransaction.replace((int) R.id.fragment_id, new EOLFragment());
                    commit();
                } else if (ERR_TEMP.equals(string)) {
                    string2 = getString(R.string.err_title_temp_error);
                    str2 = getString(R.string.check_update_temperror);
                } else if (ERR_FAIL.equals(string)) {
                    string2 = getString(R.string.err_title_update);
                    str2 = getString(R.string.check_update_fail);
                } else if (ERR_BADPARAM.equals(string)) {
                    string2 = getString(R.string.err_title_update);
                    str2 = getString(R.string.check_update_badparam);
                } else if (ERR_WIFI_NEEDED.equals(string)) {
                    string2 = getString(R.string.bad_connection);
                    str2 = getString(R.string.download_wifi_only_error);
                } else if (ERR_INTERNAL.equals(string)) {
                    str2 = getString(R.string.check_update_internal);
                } else if (ERR_INIT_OTA_SERVICE.equals(string)) {
                    addCheckUpdateFragment(getString(R.string.initiating_ota_service_title), getString(R.string.initiating_ota_service_msg));
                } else if (ERR_ROAMING.equals(string)) {
                    string2 = getString(R.string.failed_in_roaming);
                    str2 = getString(R.string.check_update_roaming);
                } else if (ERR_IN_CALL.equals(string)) {
                    string2 = getString(R.string.error_in_call_title);
                    str2 = getString(R.string.error_in_call_msg);
                } else if (ERR_STORAGE_LOW.equals(string)) {
                    string2 = getString(R.string.err_title_memory_low);
                    str2 = getString(R.string.check_update_storage_low);
                } else if (ERR_BACKGROUND_INSTALL.equals(string)) {
                    if (bundle.getParcelable(UpgradeUtilConstants.KEY_CHECK_RESPONSE_INTENT) != null) {
                        addBackgroundInstallationFragment((Intent) bundle.getParcelable(UpgradeUtilConstants.KEY_CHECK_RESPONSE_INTENT));
                    } else {
                        addBackgroundInstallationFragment(getIntent());
                    }
                } else if (ERR_VAB_VALIDATION_SUCCESS.equals(string)) {
                    string2 = getString(R.string.vab_validation_update_success_title);
                    str2 = getString(R.string.vab_validation_update_success_desc);
                } else if (ERR_VAB_VALIDATION_FAILURE.equals(string)) {
                    string2 = getString(R.string.vab_validation_update_failure_title);
                    str2 = getString(R.string.vab_validation_update_failure_desc);
                } else if (ERR_CTA_BG_DATA_DISABLED.equals(string)) {
                    if (this.settings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                        UpdaterUtils.launchNextSetupActivityVitalUpdate(this);
                        finish();
                        return;
                    }
                    showPermissionDialog();
                }
            }
            if (str2 != null) {
                addErrorFragment(getIntent(), string2, str2);
            }
        }
    }

    @Override // com.motorola.ccc.ota.ui.FragmentActionListener
    public void onActionPerformed() {
        finish();
    }

    protected void onStop() {
        super.onStop();
        if (getSupportFragmentManager().findFragmentByTag("CheckUpdateFragment") == null || !ERR_NET.equals(this.errorCodeToRefresh)) {
            return;
        }
        finish();
    }

    @Override // com.motorola.ccc.ota.ui.UpdaterUtils.OnDialogInteractionListener
    public void onPositiveClick(int i, JSONObject jSONObject) {
        finish();
    }

    @Override // com.motorola.ccc.ota.ui.UpdaterUtils.OnDialogInteractionListener
    public void onDismiss(int i, boolean z) {
        if (z) {
            finish();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        try {
            super.dispatchTouchEvent(motionEvent);
            return true;
        } catch (ActivityNotFoundException e) {
            Logger.error("OtaApp", "Exception caught " + e.getMessage());
            return true;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View inflate = getLayoutInflater().inflate(R.layout.custom_permisstion_dialog, (ViewGroup) null);
        builder.setView(inflate);
        final AlertDialog create = builder.create();
        ((Button) inflate.findViewById(R.id.btnAllow)).setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.BaseActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                BaseActivity.this.m164lambda$showPermissionDialog$0$commotorolacccotauiBaseActivity(create, view);
            }
        });
        ((Button) inflate.findViewById(R.id.btnDeny)).setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.BaseActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                BaseActivity.this.m165lambda$showPermissionDialog$1$commotorolacccotauiBaseActivity(view);
            }
        });
        UpdaterUtils.setCornersRounded(this, create);
        create.show();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$showPermissionDialog$0$com-motorola-ccc-ota-ui-BaseActivity  reason: not valid java name */
    public /* synthetic */ void m164lambda$showPermissionDialog$0$commotorolacccotauiBaseActivity(AlertDialog alertDialog, View view) {
        new BotaSettings().setBoolean(Configs.SETUP_TOS_ACCEPTED, true);
        new BotaSettings().setBoolean(Configs.USER_ACCEPTED_CTA_BG_DATA, true);
        initFragment(null);
        alertDialog.dismiss();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$showPermissionDialog$1$com-motorola-ccc-ota-ui-BaseActivity  reason: not valid java name */
    public /* synthetic */ void m165lambda$showPermissionDialog$1$commotorolacccotauiBaseActivity(View view) {
        finish();
    }
}
