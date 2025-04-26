package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class VitalRestartFragment extends Fragment {
    private static final int INSTALL_COUNTDOWN_IN_SECONDS = 10;
    private static PreInstallCounter mPreInstallCounter;
    private Activity activity;
    private Intent activityIntent;
    private Context context;
    private TextView mDone;
    private ImageView mDoneImg;
    private Button mRestartBtn;
    private TextView mRestartText;
    private TextView mRestartTitle;
    private View rootView;
    private BotaSettings settings;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof Activity) {
            this.activity = (Activity) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must be an instance of Activity");
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.settings = new BotaSettings();
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.activityIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.rootView = layoutInflater.inflate(R.layout.vital_check_update, viewGroup, false);
        findViewsById();
        this.mRestartBtn.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.VitalRestartFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                VitalRestartFragment.this.proceedWithReboot();
            }
        });
        this.settings = new BotaSettings();
        this.mRestartTitle.setText(getResources().getString(R.string.vital_restart_title));
        this.mRestartTitle.setText(getResources().getString(R.string.vital_restart_title));
        this.mDone.setVisibility(0);
        this.mDone.setText(getResources().getString(R.string.vital_done));
        this.mRestartBtn.setVisibility(0);
        this.mDoneImg.setVisibility(0);
        refreshOrStartInstallCounter();
        return this.rootView;
    }

    private void findViewsById() {
        this.mRestartTitle = (TextView) this.rootView.findViewById(R.id.vitalUpdateTitle);
        this.mRestartText = (TextView) this.rootView.findViewById(R.id.vitalUpdateBody);
        this.mRestartBtn = (Button) this.rootView.findViewById(R.id.button_with_bg);
        this.mDone = (TextView) this.rootView.findViewById(R.id.percentage);
        this.mDoneImg = (ImageView) this.rootView.findViewById(R.id.restart_img);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class PreInstallCounter extends CountDownTimer {
        private TextView mWarningTextCounter;
        Resources resources;

        public PreInstallCounter(long j, long j2, TextView textView) {
            super(j, j2);
            this.mWarningTextCounter = textView;
            this.resources = VitalRestartFragment.this.getResources();
        }

        public void refreshUI(TextView textView) {
            this.mWarningTextCounter = textView;
        }

        public void stop() {
            cancel();
        }

        @Override // android.os.CountDownTimer
        public void onFinish() {
            VitalRestartFragment.this.proceedWithReboot();
        }

        @Override // android.os.CountDownTimer
        public void onTick(long j) {
            showPreInstallWarningText(((int) j) / 1000);
        }

        private void showPreInstallWarningText(int i) {
            this.mWarningTextCounter.setText(new SpannableString(this.resources.getQuantityString(R.plurals.vital_restart_desc, i, Integer.valueOf(i))));
        }
    }

    private void refreshOrStartInstallCounter() {
        PreInstallCounter preInstallCounter = mPreInstallCounter;
        if (preInstallCounter != null) {
            preInstallCounter.refreshUI(this.mRestartText);
            return;
        }
        PreInstallCounter preInstallCounter2 = new PreInstallCounter(10000L, 1000L, this.mRestartText);
        mPreInstallCounter = preInstallCounter2;
        preInstallCounter2.start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void proceedWithReboot() {
        Logger.info("OtaApp", "Vital Update, OTA restart accepted by user!");
        UpgradeUtilMethods.sendUpgradeLaunchProceed(this.context, this.activityIntent.getStringExtra(UpgradeUtilConstants.KEY_VERSION), true, UpdaterUtils.getInstallModeStats());
        NotificationUtils.clearNextPromptDetails(this.settings);
        mPreInstallCounter = null;
        this.activity.finish();
    }
}
