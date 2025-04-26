package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.otalib.common.utils.BuildPropertyUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpdateFailedFragment extends Fragment {
    private boolean isMergeFailure;
    private LottieAnimationView lottieAnimationView;
    private Activity mActivity;
    private Button mDone;
    private UpdaterUtils.UpgradeInfo mInfo;
    private Intent mIntent;
    private TextView mPostInstallFailureNotes;
    private View mRootView;
    private TextView mUpdateFailedDesc;
    private TextView mUpdateFailedTitle;
    private UpdateType.UpdateTypeInterface mUpdateType;

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
            ((NotificationManager) context.getSystemService("notification")).cancel(4);
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.updatefail, viewGroup, false);
        initUI();
        return this.mRootView;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Resources resources = getResources();
        UpdaterUtils.UpgradeInfo upgradeInfoAfterOTAUpdate = UpdaterUtils.getUpgradeInfoAfterOTAUpdate(this.mIntent);
        this.mInfo = upgradeInfoAfterOTAUpdate;
        if (upgradeInfoAfterOTAUpdate == null) {
            Logger.error("OtaApp", "updateFailFragment, No upgradeInfo found.");
            this.mActivity.finishAndRemoveTask();
            return;
        }
        this.mUpdateType = UpdateType.getUpdateType(upgradeInfoAfterOTAUpdate.getUpdateTypeData());
        Window window = this.mActivity.getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.setStatusBarColor(0);
        if (!UpdaterUtils.isBatterySaverEnabled(getContext())) {
            WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
            if (((UiModeManager) getContext().getSystemService("uimode")).getNightMode() != 2) {
                windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
                windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
            }
        }
        this.lottieAnimationView.setRepeatCount(-1);
        this.lottieAnimationView.setAnimation("alert_state.json");
        this.lottieAnimationView.playAnimation();
        if (!UpdaterUtils.isMaxRetryCountReachedForVerizon(this.mIntent)) {
            this.mUpdateFailedDesc.setText(resources.getString(R.string.update_fail_desc));
        }
        if (this.mInfo.hasPostInstallFailureMessage()) {
            setPostInstallNotes(this.mInfo);
        } else {
            if (UpdaterUtils.isMaxRetryCountReachedForVerizon(this.mIntent) && !BuildPropertyUtils.isProductWaveAtleastRefWave("2024.2")) {
                this.mUpdateFailedDesc.setText(resources.getString(R.string.verizon_failure_text));
            }
            this.mPostInstallFailureNotes.setVisibility(8);
        }
        if (this.isMergeFailure) {
            this.mUpdateFailedTitle.setText(resources.getString(R.string.merge_update_fail_notify_title));
            this.mUpdateFailedDesc.setText(resources.getString(R.string.merge_update_fail_text));
        }
    }

    public void onStart() {
        super.onStart();
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
    }

    void initUI() {
        Logger.debug("OtaApp", "Update Fail Status displayed to user");
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.mIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
            this.isMergeFailure = arguments.getBoolean(UpdaterUtils.KEY_MERGE_FAILURE);
        }
        this.mUpdateFailedTitle = (TextView) this.mRootView.findViewById(R.id.update_failed_title);
        this.mUpdateFailedDesc = (TextView) this.mRootView.findViewById(R.id.update_fail_desc);
        TextView textView = (TextView) this.mRootView.findViewById(R.id.postinstallfailnote);
        this.mPostInstallFailureNotes = textView;
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        this.mDone = (Button) this.mRootView.findViewById(R.id.update_status_ok);
        this.lottieAnimationView = (LottieAnimationView) this.mRootView.findViewById(R.id.lottieAlertMark);
        this.mDone.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdateFailedFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Logger.info("OtaApp", "OTA Update Fail Status accepted by user!");
                UpdateFailedFragment.this.handleOnCancelUpdateFragment();
            }
        });
    }

    private void setPostInstallNotes(UpdaterUtils.UpgradeInfo upgradeInfo) {
        String locationType = upgradeInfo.getLocationType();
        String postInstallFailureMessage = upgradeInfo.getPostInstallFailureMessage();
        try {
            if ("sdcard".equals(locationType)) {
                return;
            }
            this.mPostInstallFailureNotes.setVisibility(0);
            this.mPostInstallFailureNotes.setText(Html.fromHtml(postInstallFailureMessage, 0, null, new HtmlUtils(postInstallFailureMessage)));
            this.mPostInstallFailureNotes.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (Exception e) {
            this.mPostInstallFailureNotes.setVisibility(4);
            Logger.error("OtaApp", "UpdateFailedFragment, error setting PostInstallFailureNotes." + e);
        }
    }

    public void onStop() {
        super.onStop();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleOnCancelUpdateFragment() {
        this.mActivity.finish();
    }

    public void onDestroyView() {
        this.mActivity.finish();
        super.onDestroyView();
    }
}
