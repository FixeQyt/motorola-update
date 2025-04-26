package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CheckUpdateFragment extends Fragment {
    private Activity activity;
    private Button btnDone;
    private Context context;
    private LottieAnimationView lottieAnimationView;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.CheckUpdateFragment.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE_RESPONSE.equals(intent.getAction())) {
                int intExtra = intent.getIntExtra(UpgradeUtilConstants.KEY_REQUESTID, -1);
                if (intExtra != 0) {
                    Logger.debug("OtaApp", "Ignore check update response, requestId: " + intExtra);
                    return;
                }
                String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE);
                Logger.debug("OtaApp", "Received check update response, code: " + stringExtra);
                try {
                    Bundle bundle = new Bundle();
                    bundle.putString(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE, stringExtra);
                    ((FragmentActionListener) CheckUpdateFragment.this.activity).onUpdateActionResponse(bundle);
                } catch (ClassCastException e) {
                    Logger.debug("OtaApp", "Exception in CheckUpdateFragment, registerCheckUpdateReceiver: " + e);
                }
            } else if (UpgradeUtilConstants.START_MERGE_RESTART_ACTIVITY_INTENT.equals(intent.getAction())) {
                intent.setClass(context, BaseActivity.class);
                intent.setFlags(805306368);
                intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.MERGE_RESTART_FRAGMENT.toString());
                context.startActivity(intent);
            } else if (UpgradeUtilConstants.UPGRADE_UPDATE_STATUS.equals(intent.getAction())) {
                Boolean valueOf = Boolean.valueOf(intent.getBooleanExtra(UpgradeUtilConstants.KEY_UPDATE_STATUS, false));
                if (intent.getStringExtra(UpgradeUtilConstants.KEY_REASON).contains("cleanupAppliedPayload")) {
                    intent.putExtra(UpdaterUtils.KEY_MERGE_FAILURE, true);
                }
                intent.setClass(context, BaseActivity.class);
                intent.setFlags(805306368);
                if (valueOf.booleanValue()) {
                    intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.UPDATE_COMPLETE_FRAGMENT.toString());
                } else {
                    intent.putExtra(UpgradeUtilConstants.FRAGMENT_TYPE, UpgradeUtilConstants.FragmentTypeEnum.UPDATE_FAILED_FRAGMENT.toString());
                }
                context.startActivity(intent);
            }
        }
    };
    private View rootView;
    private BotaSettings settings;
    private TextView txtCheckUpdateBody;
    private TextView txtCheckUpdateTitle;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof Activity) {
            this.activity = (Activity) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must be an instance of Activity");
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        Window window = this.activity.getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.setStatusBarColor(0);
        window.setNavigationBarColor(0);
        Bundle arguments = getArguments();
        this.txtCheckUpdateTitle.setText(arguments.getString(UpdaterUtils.KEY_CHK_TITLE));
        this.txtCheckUpdateBody.setText(arguments.getString(UpdaterUtils.KEY_CHK_BODY));
        this.lottieAnimationView.setRepeatCount(-1);
        this.lottieAnimationView.setAnimation("loading_animation.json");
        this.lottieAnimationView.playAnimation();
        registerReceivers();
        if (this.settings.getBoolean(Configs.VAB_MERGE_PROCESS_RUNNING, false)) {
            this.btnDone.setVisibility(0);
        } else {
            this.btnDone.setVisibility(8);
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.rootView = layoutInflater.inflate(R.layout.check_update, viewGroup, false);
        this.settings = new BotaSettings();
        this.lottieAnimationView = (LottieAnimationView) this.rootView.findViewById(R.id.lottieAVCheckUpdate);
        this.txtCheckUpdateTitle = (TextView) this.rootView.findViewById(R.id.txtCheckUpdateTitle);
        this.txtCheckUpdateBody = (TextView) this.rootView.findViewById(R.id.txtCheckUpdateBody);
        Button button = (Button) this.rootView.findViewById(R.id.btnDone);
        this.btnDone = button;
        button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.CheckUpdateFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                CheckUpdateFragment.this.activity.finish();
            }
        });
        return this.rootView;
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE_RESPONSE);
        intentFilter.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_STATUS);
        if (this.settings.getBoolean(Configs.VAB_MERGE_PROCESS_RUNNING)) {
            intentFilter.addAction(UpgradeUtilConstants.START_MERGE_RESTART_ACTIVITY_INTENT);
        }
        this.context.registerReceiver(this.mUpdateReceiver, intentFilter, 2);
    }

    public void onDestroy() {
        super.onDestroy();
        BroadcastReceiver broadcastReceiver = this.mUpdateReceiver;
        if (broadcastReceiver != null) {
            this.context.unregisterReceiver(broadcastReceiver);
            this.mUpdateReceiver = null;
        }
    }
}
