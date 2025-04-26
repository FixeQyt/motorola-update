package com.motorola.ccc.ota.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtilConstants;
import com.motorola.ccc.ota.sources.bota.thinkshield.ThinkShieldUtils;
import com.motorola.ccc.ota.sources.fota.FotaAutoDownloadSettings;
import com.motorola.ccc.ota.ui.MenuFragment;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.CusUtilMethods;
import com.motorola.ccc.ota.utils.DmSendAlertService;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.utils.BroadcastUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class MenuFragment extends Fragment implements SmartUpdateUtils.SmartDialog, SmartUpdateUtils.OnSmartUpdateConfigChangedLister {
    private Intent activityIntent;
    private LinearLayout checkForUpdates;
    private ImageView imgBack;
    private boolean isTransactionSafe;
    private LinearLayout lnrSmartUpdateParent;
    private FragmentActivity mActivity;
    private Context mContext;
    private View mRootView;
    private LinearLayout mSuUpdate;
    private TextView mSuUpdateSummary;
    private TextView mSuUpdateTitle;
    private BroadcastReceiver mUIRefreshSimChangeReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.MenuFragment.7
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (UpgradeUtilConstants.REFRESH_CHKUPDATE_UI_ON_SIMCHANGE.equals(intent.getAction())) {
                Logger.debug("OtaApp", "MenuFragment:mUIRefreshSimChangeReceiver:update menu fragment ui on liberty sim change");
                if (CusUtilMethods.isCheckUpdateDisabled(MenuFragment.this.mContext)) {
                    MenuFragment.this.checkForUpdates.setVisibility(8);
                } else {
                    MenuFragment.this.checkForUpdates.setVisibility(0);
                }
            }
        }
    };
    private BotaSettings settings;
    private Switch switchSmartUpdate;
    private AlertDialog turnOffSureSmartUpdateDialog;
    private TextView txtAutoDownloadSettings;
    private TextView txtChkDisableITAdmin;
    private TextView txtUpdatePrefText;
    private TextView updateHistory;
    private LinearLayout updatePrefsToolbar;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof FragmentActivity) {
            this.mActivity = (FragmentActivity) context;
        }
    }

    public void onCreate(Bundle bundle) {
        Logger.debug("OtaApp", "MenuFragment, onCreate");
        super.onCreate(bundle);
        this.settings = new BotaSettings();
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        Logger.debug("OtaApp", "MenuFragment, onCreateView");
        this.mRootView = layoutInflater.inflate(R.layout.update_preference, viewGroup, false);
        findViewsById();
        handleButtons();
        initUI();
        return this.mRootView;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Logger.debug("OtaApp", "MenuFragment, onActivityCreated");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.REFRESH_CHKUPDATE_UI_ON_SIMCHANGE);
        BroadcastUtils.registerLocalReceiver(this.mContext, this.mUIRefreshSimChangeReceiver, intentFilter);
        Bundle arguments = getArguments();
        this.mActivity = getActivity();
        this.mContext = getContext();
        this.activityIntent = this.mActivity.getIntent();
        Window window = this.mActivity.getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.setStatusBarColor(0);
        window.setNavigationBarColor(0);
        if (arguments != null) {
            this.activityIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
    }

    public void onResume() {
        super.onResume();
        this.isTransactionSafe = true;
    }

    public void onPause() {
        super.onPause();
        this.isTransactionSafe = false;
    }

    private void findViewsById() {
        this.lnrSmartUpdateParent = (LinearLayout) this.mRootView.findViewById(R.id.lnrSmartUpdateParent);
        this.switchSmartUpdate = (Switch) this.mRootView.findViewById(R.id.switchSmartUpdate);
        this.txtUpdatePrefText = (TextView) this.mRootView.findViewById(R.id.txtUpdatePrefText);
        this.imgBack = (ImageView) this.mRootView.findViewById(R.id.back);
        this.txtAutoDownloadSettings = (TextView) this.mRootView.findViewById(R.id.txtAutoDownloadSettings);
        this.updatePrefsToolbar = (LinearLayout) this.mRootView.findViewById(R.id.update_prefs_toolbar);
        this.updateHistory = (TextView) this.mRootView.findViewById(R.id.history_prefs);
        this.checkForUpdates = (LinearLayout) this.mRootView.findViewById(R.id.check_for_updates_layout);
        this.txtChkDisableITAdmin = (TextView) this.mRootView.findViewById(R.id.txtChkDisabledITAdmin);
        this.mSuUpdate = (LinearLayout) this.mRootView.findViewById(R.id.su_update);
        this.mSuUpdateTitle = (TextView) this.mRootView.findViewById(R.id.su_update_title);
        this.mSuUpdateSummary = (TextView) this.mRootView.findViewById(R.id.su_update_summary);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initUI() {
        if (!BuildPropReader.isATT()) {
            this.mSuUpdate.setVisibility(0);
            setPrefsData();
        } else {
            this.txtAutoDownloadSettings.setVisibility(0);
        }
        this.updatePrefsToolbar.setVisibility(8);
        this.checkForUpdates.setVisibility(0);
        if (UpdaterUtils.isVerizon()) {
            this.txtUpdatePrefText.setText(R.string.smart_settings_string_vzw);
        } else if (BuildPropReader.isSoftbank() && BuildPropReader.isSoftbankSIM()) {
            this.txtUpdatePrefText.setText(R.string.smart_settings_string_Softbank);
        } else {
            this.txtUpdatePrefText.setText(R.string.smart_settings_string);
        }
        if (!SmartUpdateUtils.isSmartUpdateEnabledByServer()) {
            this.lnrSmartUpdateParent.setVisibility(8);
        } else {
            this.lnrSmartUpdateParent.setVisibility(0);
        }
        if (SmartUpdateUtils.isSmartUpdateEnabledByUser(this.settings)) {
            this.switchSmartUpdate.setChecked(true);
        } else {
            this.switchSmartUpdate.setChecked(false);
        }
        Map<String, Object> aSCCampaignStatusDetails = ThinkShieldUtils.getASCCampaignStatusDetails(this.mContext);
        boolean booleanValue = ((Boolean) aSCCampaignStatusDetails.get(ThinkShieldUtilConstants.KEY_ASC_CHK_DISABLE_STATUS)).booleanValue();
        String str = (String) aSCCampaignStatusDetails.get(ThinkShieldUtilConstants.KEY_ASC_STATUS_STRING);
        if (CusUtilMethods.isCheckUpdateDisabled(this.mContext)) {
            this.checkForUpdates.setEnabled(false);
            this.checkForUpdates.setAlpha(0.5f);
            if (new SystemUpdaterPolicy().isOtaUpdateDisabledByPolicyMngr() || booleanValue) {
                this.txtChkDisableITAdmin.setVisibility(0);
                this.txtChkDisableITAdmin.setText(str);
                return;
            }
            this.txtChkDisableITAdmin.setVisibility(8);
            return;
        }
        this.checkForUpdates.setEnabled(true);
        this.checkForUpdates.setAlpha(1.0f);
        if (!TextUtils.isEmpty(str)) {
            this.txtChkDisableITAdmin.setVisibility(0);
            this.txtChkDisableITAdmin.setText(str);
            return;
        }
        this.txtChkDisableITAdmin.setVisibility(8);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.ui.MenuFragment$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class AnonymousClass1 implements CompoundButton.OnCheckedChangeListener {
        AnonymousClass1() {
        }

        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            if (z) {
                if (SmartUpdateUtils.isSmartUpdateEnabledByUser(MenuFragment.this.settings)) {
                    return;
                }
                SmartUpdateUtils.turnSmartUpdateOn(SmartUpdateUtils.SmartUpdateLaunchMode.SETTINGS_MENU_FRAGMENT.name());
                MenuFragment.this.launchSmartUpdate();
            } else if (SmartUpdateUtils.isSmartUpdateEnabledByUser(MenuFragment.this.settings)) {
                String string = MenuFragment.this.mContext.getResources().getString(R.string.are_you_sure);
                String string2 = MenuFragment.this.mContext.getResources().getString(R.string.are_you_sure_pop_up_text);
                MenuFragment menuFragment = MenuFragment.this;
                menuFragment.turnOffSureSmartUpdateDialog = SmartUpdateUtils.getAndShowAreYouSurePopUp(menuFragment.mContext, string, string2, MenuFragment.this.mActivity, new DialogInterface.OnClickListener() { // from class: com.motorola.ccc.ota.ui.MenuFragment$1$$ExternalSyntheticLambda0
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }, new DialogInterface.OnClickListener() { // from class: com.motorola.ccc.ota.ui.MenuFragment$1$$ExternalSyntheticLambda1
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        MenuFragment.AnonymousClass1.this.m204lambda$onCheckedChanged$1$commotorolacccotauiMenuFragment$1(dialogInterface, i);
                    }
                });
                MenuFragment.this.initUI();
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: lambda$onCheckedChanged$1$com-motorola-ccc-ota-ui-MenuFragment$1  reason: not valid java name */
        public /* synthetic */ void m204lambda$onCheckedChanged$1$commotorolacccotauiMenuFragment$1(DialogInterface dialogInterface, int i) {
            SmartUpdateUtils.turnSmartUpdateOff(SmartUpdateUtils.SmartUpdateLaunchMode.SETTINGS_MENU_FRAGMENT.name());
            MenuFragment.this.initUI();
            dialogInterface.cancel();
        }
    }

    private void handleButtons() {
        this.switchSmartUpdate.setOnCheckedChangeListener(new AnonymousClass1());
        this.imgBack.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.MenuFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MenuFragment.this.mActivity.finish();
            }
        });
        this.lnrSmartUpdateParent.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.MenuFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MenuFragment.this.launchSmartUpdate();
            }
        });
        this.txtAutoDownloadSettings.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.MenuFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MenuFragment.this.launchAutoDownloadSettings();
            }
        });
        this.updateHistory.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.MenuFragment.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MenuFragment.this.launchUpdateHistory();
            }
        });
        this.checkForUpdates.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.MenuFragment.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MenuFragment.this.launchCheckForUpdates();
            }
        });
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.SmartDialog
    public void dismissSmartDialog() {
        AlertDialog alertDialog = this.turnOffSureSmartUpdateDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchAutoDownloadSettings() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, this.activityIntent);
        FotaAutoDownloadSettings fotaAutoDownloadSettings = new FotaAutoDownloadSettings();
        fotaAutoDownloadSettings.setArguments(bundle);
        if (this.isTransactionSafe) {
            fotaAutoDownloadSettings.show(this.mActivity.getSupportFragmentManager(), "AutoDownloadSettingsDialog");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchSmartUpdate() {
        Bundle bundle = new Bundle();
        this.activityIntent.putExtra(UpgradeUtilConstants.KEY_LAUNCH_MODE, SmartUpdateUtils.SmartUpdateLaunchMode.SETTINGS_MENU_FRAGMENT.name());
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, this.activityIntent);
        SmartUpdateFragment smartUpdateFragment = new SmartUpdateFragment();
        smartUpdateFragment.setArguments(bundle);
        if (this.isTransactionSafe) {
            smartUpdateFragment.show(this.mActivity.getSupportFragmentManager(), "SmartUpdateDialog");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchUpdateHistory() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, this.activityIntent);
        UpdateHistoryFragment updateHistoryFragment = new UpdateHistoryFragment();
        updateHistoryFragment.setArguments(bundle);
        if (this.isTransactionSafe) {
            updateHistoryFragment.show(this.mActivity.getSupportFragmentManager(), "UpdateHistoryDialog");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchCheckForUpdates() {
        DmSendAlertService.startManualDMSync(this.mContext);
        Intent intent = new Intent();
        intent.setClass(this.mContext, BaseActivity.class);
        intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.CHECK_UPDATE_FRAGMENT.toString());
        intent.setFlags(268435456);
        startActivity(intent);
    }

    public void onDestroyView() {
        Logger.debug("OtaApp", "MenuFragment, onDestroyView");
        Iterator it = getFragmentManager().getFragments().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            SmartUpdateUtils.OnSmartUpdateConfigChangedLister onSmartUpdateConfigChangedLister = (Fragment) it.next();
            if (!this.mActivity.isFinishing() && (onSmartUpdateConfigChangedLister instanceof SmartUpdateUtils.OnSmartUpdateConfigChangedLister)) {
                onSmartUpdateConfigChangedLister.onSmartUpdateConfigChanged();
                break;
            }
        }
        SmartUpdateUtils.decideToShowSmartUpdateSuggestion(OtaApplication.getGlobalContext());
        BroadcastUtils.unregisterLocalReceiver(this.mContext, this.mUIRefreshSimChangeReceiver);
        super.onDestroyView();
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.OnSmartUpdateConfigChangedLister
    public void onSmartUpdateConfigChanged() {
        initUI();
    }

    private void setPrefsData() {
        List<String> readLastUpdateVersionFromPref = SystemUpdateStatusUtils.readLastUpdateVersionFromPref(this.mContext);
        String swUpdatePlannedInfo = SystemUpdateStatusUtils.getSwUpdatePlannedInfo(this.mContext);
        if (!TextUtils.isEmpty(swUpdatePlannedInfo)) {
            String string = getString(R.string.software_update_planned_summary, new Object[]{swUpdatePlannedInfo});
            this.mSuUpdateTitle.setText(getResources().getString(R.string.software_update_planned_title));
            this.mSuUpdateSummary.setText(string);
        } else if (readLastUpdateVersionFromPref == null) {
            this.mSuUpdateSummary.setVisibility(8);
        } else {
            for (int i = 0; i < readLastUpdateVersionFromPref.size(); i++) {
                String[] split = readLastUpdateVersionFromPref.get(i).split(SystemUpdateStatusUtils.FIELD_SEPERATOR);
                try {
                    if (SystemUpdateStatusUtils.SOFTWARE_UPDATE.equals(split[0])) {
                        if (!TextUtils.isEmpty(this.settings.getString(Configs.ONGOING_HISTORY_POLICY_DISPLAY_NAME))) {
                            this.mSuUpdateTitle.setText(this.settings.getString(Configs.ONGOING_HISTORY_POLICY_DISPLAY_NAME));
                            this.mSuUpdateSummary.setVisibility(8);
                        } else {
                            this.mSuUpdateTitle.setText(split[1]);
                            this.mSuUpdateSummary.setVisibility(0);
                        }
                        this.mSuUpdateSummary.setText(split[2]);
                    }
                } catch (Exception e) {
                    Logger.error("OtaApp", "This exception is caused while setting the Update Status in the MenuFragment " + e);
                }
            }
        }
    }
}
