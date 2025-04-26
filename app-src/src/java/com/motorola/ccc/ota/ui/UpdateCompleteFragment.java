package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpdateCompleteFragment extends Fragment {
    private ExpandableListView expandableListView;
    private boolean isTransactionSafe;
    private FragmentActivity mActivity;
    private TextView mAndroidVersion;
    private Button mDone;
    private TextView mExpTitle;
    private UpdaterUtils.UpgradeInfo mInfo;
    private Intent mIntent;
    private TextView mPostInstallNotes;
    private View mRootView;
    private TextView mSecurityPatch;
    private TextView mUpdateComplete;
    private TextView mUpdateStatusDesc;
    private UpdateType.UpdateTypeInterface mUpdateType;
    private TextView osReleaseNotes;
    private BotaSettings settings;
    private LottieAnimationView updateStatusAnimation;

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.mActivity = (FragmentActivity) context;
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            if (notificationManager != null) {
                notificationManager.cancel(4);
            }
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.settings = new BotaSettings();
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.updatecomplete, viewGroup, false);
        initUI();
        return this.mRootView;
    }

    public void onActivityCreated(Bundle bundle) {
        String string;
        super.onActivityCreated(bundle);
        getResources();
        UpdaterUtils.UpgradeInfo upgradeInfoAfterOTAUpdate = UpdaterUtils.getUpgradeInfoAfterOTAUpdate(this.mIntent);
        this.mInfo = upgradeInfoAfterOTAUpdate;
        if (upgradeInfoAfterOTAUpdate == null) {
            Logger.error("OtaApp", "updateStatusFragment, No upgradeInfo found.");
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
        if (UpdateType.DIFFUpdateType.OS.toString().equalsIgnoreCase(this.mInfo.getUpdateTypeData())) {
            string = getResources().getString(R.string.update_success_desc_os, BuildPropReader.getAndroidVersion());
        } else {
            string = getResources().getString(R.string.update_success_desc, this.mInfo.getDisplayVersion());
        }
        this.mUpdateStatusDesc.setText(string);
        ExpandableListView expandableList = UpdaterUtils.setExpandableList(this.expandableListView, this.mInfo.hasReleaseNotes(), this.mInfo.getReleaseNotes(), this.mExpTitle, getActivity(), this.mUpdateType, this.mRootView.findViewById(R.id.dash_line));
        this.expandableListView = expandableList;
        this.expandableListView = UpdaterUtils.handleExpList(expandableList, "");
        UpdaterUtils.setOsReleaseNotes(getContext(), this.osReleaseNotes, this.mInfo, "");
        this.updateStatusAnimation.setRepeatCount(-1);
        this.updateStatusAnimation.setAnimation("check_mark.json");
        this.updateStatusAnimation.playAnimation();
        if (this.mInfo.hasPostInstallNotes()) {
            setPostInstallNotes(this.mInfo);
        } else {
            this.mPostInstallNotes.setVisibility(8);
        }
    }

    public void onStart() {
        super.onStart();
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() { // from class: com.motorola.ccc.ota.ui.UpdateCompleteFragment$$ExternalSyntheticLambda0
            @Override // android.view.View.OnKeyListener
            public final boolean onKey(View view2, int i, KeyEvent keyEvent) {
                return UpdateCompleteFragment.this.m255lambda$onViewCreated$0$commotorolacccotauiUpdateCompleteFragment(view2, i, keyEvent);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$onViewCreated$0$com-motorola-ccc-ota-ui-UpdateCompleteFragment  reason: not valid java name */
    public /* synthetic */ boolean m255lambda$onViewCreated$0$commotorolacccotauiUpdateCompleteFragment(View view, int i, KeyEvent keyEvent) {
        if (i == 4) {
            handleOnCancelUpdateFragment();
            return true;
        }
        return false;
    }

    public void onResume() {
        super.onResume();
        this.isTransactionSafe = true;
    }

    public void onPause() {
        super.onPause();
        this.isTransactionSafe = false;
    }

    void initUI() {
        Logger.debug("OtaApp", "Update Status displayed to user");
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.mIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
        this.mUpdateStatusDesc = (TextView) this.mRootView.findViewById(R.id.update_status_desc);
        this.mPostInstallNotes = (TextView) this.mRootView.findViewById(R.id.postinstallnote);
        this.mExpTitle = (TextView) this.mRootView.findViewById(R.id.expTitle);
        this.expandableListView = (ExpandableListView) this.mRootView.findViewById(R.id.expandableListView);
        this.osReleaseNotes = (TextView) this.mRootView.findViewById(R.id.os_release_notes);
        this.updateStatusAnimation = (LottieAnimationView) this.mRootView.findViewById(R.id.lAnimation);
        this.mPostInstallNotes.setMovementMethod(ScrollingMovementMethod.getInstance());
        this.mDone = (Button) this.mRootView.findViewById(R.id.update_status_ok);
        this.mUpdateComplete = (TextView) this.mRootView.findViewById(R.id.update_complete);
        this.mDone.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdateCompleteFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Logger.info("OtaApp", "OTA Update Status accepted by user!");
                UpdateCompleteFragment.this.handleOnCancelUpdateFragment();
            }
        });
        setTextViewText(this.mRootView);
    }

    private void setTextViewText(View view) {
        this.mAndroidVersion = (TextView) view.findViewById(R.id.android_version);
        TextView textView = (TextView) view.findViewById(R.id.security_patch);
        this.mSecurityPatch = textView;
        checkAndDisplayText(textView, getContext().getString(R.string.security_patch), DateFormatUtils.getSecurityPatch());
        checkAndDisplayText(this.mAndroidVersion, getContext().getString(R.string.android_version), BuildPropReader.getAndroidVersion());
    }

    private void checkAndDisplayText(TextView textView, String str, String str2) {
        if (str2 != null) {
            textView.setText(str + SystemUpdateStatusUtils.SPACE + str2);
        } else {
            textView.setVisibility(8);
        }
    }

    private void setPostInstallNotes(UpdaterUtils.UpgradeInfo upgradeInfo) {
        String locationType = upgradeInfo.getLocationType();
        String postInstallNotes = upgradeInfo.getPostInstallNotes();
        try {
            if ("sdcard".equals(locationType)) {
                return;
            }
            this.mPostInstallNotes.setText(Html.fromHtml(postInstallNotes, 0, null, new HtmlUtils(postInstallNotes)));
            this.mPostInstallNotes.setVisibility(0);
            this.mPostInstallNotes.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (Exception e) {
            this.mPostInstallNotes.setVisibility(4);
            Logger.error("OtaApp", "UpdateCompleteFragment, error setting PostInstallNotes." + e);
        }
    }

    public void onStop() {
        super.onStop();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleOnCancelUpdateFragment() {
        if (SmartUpdateUtils.shouldShowTrySmartUpdatePopUp(this.settings, this.mInfo.getUpdateTypeData(), false)) {
            SmartUpdateUtils.showTrySmartUpdatePopUp(getContext(), getContext().getResources().getString(R.string.smart_update_recommendation), this.mUpdateType, this.mActivity, this.settings, SmartUpdateUtils.SmartUpdateLaunchMode.TRY_SMART_UPDATES_POPUP_UPDATE_COMPLETE.name());
            return;
        }
        this.mActivity.finish();
    }

    public void onDestroyView() {
        this.mActivity.finish();
        super.onDestroyView();
    }
}
