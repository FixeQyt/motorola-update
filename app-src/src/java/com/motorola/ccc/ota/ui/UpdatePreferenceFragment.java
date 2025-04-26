package com.motorola.ccc.ota.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.ui.UpdatePreferenceFragment;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import java.util.Iterator;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpdatePreferenceFragment extends DialogFragment implements SmartUpdateUtils.SmartDialog, SmartUpdateUtils.OnSmartUpdateConfigChangedLister {
    private Intent activityIntent;
    private ImageView imgBack;
    private LinearLayout lnrSmartUpdateParent;
    private FragmentActivity mActivity;
    private Context mContext;
    private View mRootView;
    private BotaSettings settings;
    private Switch switchSmartUpdate;
    private AlertDialog turnOffSureSmartUpdateDialog;
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
        super.onCreate(bundle);
        this.settings = new BotaSettings();
        setStyle(0, R.style.CustomiseActivityTheme);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.update_preference, viewGroup, false);
        UpdaterUtils.setNavBarColorFromDialog(getDialog());
        findViewsById();
        handleButtons();
        initUI();
        return this.mRootView;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Bundle arguments = getArguments();
        this.mActivity = getActivity();
        this.mContext = getContext();
        if (arguments != null) {
            this.activityIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
    }

    private void findViewsById() {
        this.lnrSmartUpdateParent = (LinearLayout) this.mRootView.findViewById(R.id.lnrSmartUpdateParent);
        this.switchSmartUpdate = (Switch) this.mRootView.findViewById(R.id.switchSmartUpdate);
        this.txtUpdatePrefText = (TextView) this.mRootView.findViewById(R.id.txtUpdatePrefText);
        this.imgBack = (ImageView) this.mRootView.findViewById(R.id.imgBack);
        this.updatePrefsToolbar = (LinearLayout) this.mRootView.findViewById(R.id.update_prefs_toolbar);
        this.updateHistory = (TextView) this.mRootView.findViewById(R.id.history_prefs);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initUI() {
        this.updatePrefsToolbar.setBackgroundColor(this.mActivity.getResources().getColor(R.color.mr_toolbar_bar));
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
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.ui.UpdatePreferenceFragment$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class AnonymousClass1 implements CompoundButton.OnCheckedChangeListener {
        AnonymousClass1() {
        }

        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            if (z) {
                if (SmartUpdateUtils.isSmartUpdateEnabledByUser(UpdatePreferenceFragment.this.settings)) {
                    return;
                }
                SmartUpdateUtils.turnSmartUpdateOn(SmartUpdateUtils.SmartUpdateLaunchMode.UPGRADE_PREFERENCE_FRAGMENT.name());
                UpdatePreferenceFragment.this.launchSmartUpdate();
            } else if (SmartUpdateUtils.isSmartUpdateEnabledByUser(UpdatePreferenceFragment.this.settings)) {
                String string = UpdatePreferenceFragment.this.mContext.getResources().getString(R.string.are_you_sure);
                String string2 = UpdatePreferenceFragment.this.mContext.getResources().getString(R.string.are_you_sure_pop_up_text);
                UpdatePreferenceFragment updatePreferenceFragment = UpdatePreferenceFragment.this;
                updatePreferenceFragment.turnOffSureSmartUpdateDialog = SmartUpdateUtils.getAndShowAreYouSurePopUp(updatePreferenceFragment.mContext, string, string2, UpdatePreferenceFragment.this.mActivity, new DialogInterface.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdatePreferenceFragment$1$$ExternalSyntheticLambda0
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }, new DialogInterface.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdatePreferenceFragment$1$$ExternalSyntheticLambda1
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        UpdatePreferenceFragment.AnonymousClass1.this.m265lambda$onCheckedChanged$1$commotorolacccotauiUpdatePreferenceFragment$1(dialogInterface, i);
                    }
                });
                UpdatePreferenceFragment.this.initUI();
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: lambda$onCheckedChanged$1$com-motorola-ccc-ota-ui-UpdatePreferenceFragment$1  reason: not valid java name */
        public /* synthetic */ void m265lambda$onCheckedChanged$1$commotorolacccotauiUpdatePreferenceFragment$1(DialogInterface dialogInterface, int i) {
            SmartUpdateUtils.turnSmartUpdateOff(SmartUpdateUtils.SmartUpdateLaunchMode.UPGRADE_PREFERENCE_FRAGMENT.name());
            UpdatePreferenceFragment.this.initUI();
            dialogInterface.cancel();
        }
    }

    private void handleButtons() {
        this.switchSmartUpdate.setOnCheckedChangeListener(new AnonymousClass1());
        this.imgBack.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdatePreferenceFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                UpdatePreferenceFragment.this.dismiss();
            }
        });
        this.lnrSmartUpdateParent.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdatePreferenceFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                UpdatePreferenceFragment.this.launchSmartUpdate();
            }
        });
        this.updateHistory.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdatePreferenceFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                UpdatePreferenceFragment.this.launchUpdateHistory();
            }
        });
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.SmartDialog
    public void dismissSmartDialog() {
        AlertDialog alertDialog = this.turnOffSureSmartUpdateDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchSmartUpdate() {
        Bundle bundle = new Bundle();
        Intent intent = this.mActivity.getIntent();
        this.activityIntent = intent;
        intent.putExtra(UpgradeUtilConstants.KEY_LAUNCH_MODE, SmartUpdateUtils.SmartUpdateLaunchMode.UPGRADE_PREFERENCE_FRAGMENT.name());
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, this.activityIntent);
        SmartUpdateFragment smartUpdateFragment = new SmartUpdateFragment();
        smartUpdateFragment.setArguments(bundle);
        smartUpdateFragment.show(this.mActivity.getSupportFragmentManager(), "SmartUpdateDialog");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchUpdateHistory() {
        Bundle bundle = new Bundle();
        Intent intent = this.mActivity.getIntent();
        this.activityIntent = intent;
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        UpdateHistoryFragment updateHistoryFragment = new UpdateHistoryFragment();
        updateHistoryFragment.setArguments(bundle);
        updateHistoryFragment.show(this.mActivity.getSupportFragmentManager(), "UpdateHistoryDialog");
    }

    public void onDestroyView() {
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
        super.onDestroyView();
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.OnSmartUpdateConfigChangedLister
    public void onSmartUpdateConfigChanged() {
        initUI();
    }
}
