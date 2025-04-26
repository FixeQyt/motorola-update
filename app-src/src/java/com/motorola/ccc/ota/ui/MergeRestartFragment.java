package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class MergeRestartFragment extends Fragment {
    private Activity activity;
    private Button btnRebootNow;
    private Context context;
    private boolean mDoNotShowNotification;
    private View mRootView;
    private BotaSettings settings;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof Activity) {
            this.activity = (Activity) context;
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.merge_restart, viewGroup, false);
        this.settings = new BotaSettings();
        Button button = (Button) this.mRootView.findViewById(R.id.restart_now);
        this.btnRebootNow = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.MergeRestartFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MergeRestartFragment.this.proceedWithReboot();
            }
        });
        return this.mRootView;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Window window = this.activity.getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.setStatusBarColor(0);
        WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
        if (((UiModeManager) getContext().getSystemService("uimode")).getNightMode() != 2) {
            windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
            windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void proceedWithReboot() {
        if (UpdaterUtils.isInActiveCall(this.context)) {
            UpdaterUtils.checkAndEnableCallStateChangeReceiver();
            this.activity.finish();
            return;
        }
        this.mDoNotShowNotification = true;
        String string = this.settings.getString(Configs.STATS_VAB_MERGE_RESTARTED_BY);
        this.settings.setString(Configs.STATS_VAB_MERGE_RESTARTED_BY, ((string == null || string.equalsIgnoreCase("null")) ? "" : "") + ",User");
        Logger.info("OtaApp", "MergeRestartFragment:Restarting the device");
        UpgradeUtilMethods.sendMergeRestartIntent(OtaApplication.getGlobalContext());
    }

    public void onResume() {
        super.onResume();
        NotificationUtils.cancelOtaNotification();
    }

    public void onStart() {
        super.onStart();
    }

    public void onStop() {
        super.onStop();
        if (UpdaterUtils.isDeviceLocked(this.context) || this.mDoNotShowNotification) {
            return;
        }
        NotificationUtils.displayMergeRestartNotification();
    }

    public void onDestroyView() {
        this.activity.finish();
        super.onDestroyView();
    }
}
