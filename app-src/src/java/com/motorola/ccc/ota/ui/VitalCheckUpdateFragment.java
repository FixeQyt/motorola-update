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
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class VitalCheckUpdateFragment extends Fragment {
    private Activity activity;
    private Context context;
    private LottieAnimationView lottieAnimationView;
    private View lottieView;
    private BroadcastReceiver mUpdateReceiver;
    private View rootView;

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
        registerCheckUpdateReceiver();
        this.lottieAnimationView.setRepeatCount(-1);
        this.lottieAnimationView.setAnimation("vitalCheckUpdate.json");
        this.lottieAnimationView.playAnimation();
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.vital_check_update, viewGroup, false);
        this.rootView = inflate;
        LottieAnimationView lottieAnimationView = (LottieAnimationView) inflate.findViewById(R.id.lottieAVCheckUpdate);
        this.lottieAnimationView = lottieAnimationView;
        lottieAnimationView.setVisibility(0);
        return this.rootView;
    }

    private void registerCheckUpdateReceiver() {
        if (this.mUpdateReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE_RESPONSE);
            intentFilter.addAction(UpgradeUtilConstants.UPGRADE_UPDATE_STATUS);
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.VitalCheckUpdateFragment.1
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE_RESPONSE.equals(intent.getAction())) {
                        int intExtra = intent.getIntExtra(UpgradeUtilConstants.KEY_REQUESTID, -1);
                        if (intExtra != 0) {
                            Logger.debug("OtaApp", "Vital Update, Ignore check update response, requestId: " + intExtra);
                            return;
                        }
                        String stringExtra = intent.getStringExtra(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE);
                        Logger.debug("OtaApp", "Vital Update, Received check update response, code: " + stringExtra);
                        try {
                            Bundle bundle = new Bundle();
                            bundle.putString(UpgradeUtilConstants.KEY_UPDATE_ACTION_RESPONSE, stringExtra);
                            ((FragmentActionListener) VitalCheckUpdateFragment.this.activity).onUpdateActionResponse(bundle);
                        } catch (ClassCastException e) {
                            Logger.error("OtaApp", "Exception in VitalCheckUpdateFragment, registerCheckUpdateReceiver: " + e);
                        }
                    } else if (UpgradeUtilConstants.UPGRADE_UPDATE_STATUS.equals(intent.getAction())) {
                        Boolean valueOf = Boolean.valueOf(intent.getBooleanExtra(UpgradeUtilConstants.KEY_UPDATE_STATUS, false));
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
            this.mUpdateReceiver = broadcastReceiver;
            this.context.registerReceiver(broadcastReceiver, intentFilter, 2);
        }
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
