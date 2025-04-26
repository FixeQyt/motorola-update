package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UptoDateFragment extends Fragment implements SmartUpdateUtils.OnSmartUpdateConfigChangedLister {
    private Intent activityIntent;
    private Button btnDone;
    private Button btnNotNow;
    private Button btnTurnOn;
    private boolean isTransactionSafe;
    private LottieAnimationView lottieAnimationView;
    private FragmentActivity mActivity;
    private TextView mAndroidVersion;
    private TextView mCurrentVersion;
    private TextView mSecurityPatch;
    private View rootView;
    private BotaSettings settings;
    private RelativeLayout smartUpdateBottomSheet;

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.mActivity = (FragmentActivity) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must be an instance of Activity");
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.software_upto_date, viewGroup, false);
        this.rootView = inflate;
        this.btnDone = (Button) inflate.findViewById(R.id.btnDone);
        this.btnTurnOn = (Button) this.rootView.findViewById(R.id.btnTurnOn);
        this.btnNotNow = (Button) this.rootView.findViewById(R.id.btnNotNow);
        this.mAndroidVersion = (TextView) this.rootView.findViewById(R.id.android_version);
        this.mSecurityPatch = (TextView) this.rootView.findViewById(R.id.security_patch);
        this.mCurrentVersion = (TextView) this.rootView.findViewById(R.id.build_number);
        this.smartUpdateBottomSheet = (RelativeLayout) this.rootView.findViewById(R.id.bottomSheet);
        this.lottieAnimationView = (LottieAnimationView) this.rootView.findViewById(R.id.lottieCheckMark);
        return this.rootView;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.activityIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
        getResources();
        this.settings = new BotaSettings();
        Window window = this.mActivity.getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.setStatusBarColor(0);
        window.setNavigationBarColor(0);
        this.lottieAnimationView.setRepeatCount(-1);
        this.lottieAnimationView.setAnimation("check_mark.json");
        this.lottieAnimationView.playAnimation();
        setVersionData();
        handleBottomSheet();
        this.btnDone.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UptoDateFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                try {
                    UptoDateFragment.this.mActivity.onActionPerformed();
                } catch (ClassCastException e) {
                    Logger.error("OtaApp", "Exception in UpToDateFragment, btnDone.onClick: " + e);
                }
            }
        });
        this.btnTurnOn.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UptoDateFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SmartUpdateUtils.turnSmartUpdateOn(SmartUpdateUtils.SmartUpdateLaunchMode.UP_TO_DATE_FRAGMENT.name());
                UptoDateFragment.this.launchSmartUpdate();
            }
        });
        this.btnNotNow.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UptoDateFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                UptoDateFragment.this.btnDone.setVisibility(0);
                UptoDateFragment.this.smartUpdateBottomSheet.setVisibility(8);
                UptoDateFragment.this.settings.incrementPrefs(Configs.STATS_SMART_UPDATE_BOTTOM_SHEET_NOT_NOW);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchSmartUpdate() {
        Bundle bundle = new Bundle();
        Intent intent = this.mActivity.getIntent();
        this.activityIntent = intent;
        intent.putExtra(UpgradeUtilConstants.KEY_LAUNCH_MODE, SmartUpdateUtils.SmartUpdateLaunchMode.UP_TO_DATE_FRAGMENT.name());
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, this.activityIntent);
        SmartUpdateFragment smartUpdateFragment = new SmartUpdateFragment();
        smartUpdateFragment.setArguments(bundle);
        if (this.isTransactionSafe) {
            smartUpdateFragment.show(this.mActivity.getSupportFragmentManager(), "SmartUpdateDialog");
        }
    }

    private void handleBottomSheet() {
        if (SmartUpdateUtils.shouldShowSmartUpdateBottomSheet(this.settings)) {
            this.smartUpdateBottomSheet.setVisibility(0);
            this.btnDone.setVisibility(8);
            return;
        }
        this.btnDone.setVisibility(0);
        this.smartUpdateBottomSheet.setVisibility(8);
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.OnSmartUpdateConfigChangedLister
    public void onSmartUpdateConfigChanged() {
        Logger.debug("OtaApp", "UptoDateFragment,onSmartUpdateConfigChanged");
        handleBottomSheet();
    }

    public void onResume() {
        super.onResume();
        this.isTransactionSafe = true;
    }

    public void onPause() {
        super.onPause();
        this.isTransactionSafe = false;
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void setVersionData() {
        checkAndDisplayText(this.mAndroidVersion, getContext().getString(R.string.android_version), BuildPropReader.getAndroidVersion());
        checkAndDisplayText(this.mSecurityPatch, getContext().getString(R.string.security_patch), DateFormatUtils.getSecurityPatch());
        checkAndDisplayText(this.mCurrentVersion, getContext().getString(R.string.current_version), BuildPropReader.getBuildDescription());
    }

    private void checkAndDisplayText(TextView textView, String str, String str2) {
        if (str2 != null) {
            textView.setText(str + SystemUpdateStatusUtils.SPACE + str2);
        } else {
            textView.setVisibility(8);
        }
    }
}
