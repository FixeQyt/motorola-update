package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.utils.Logger;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class VitalUpdateFailedFragment extends Fragment {
    private Activity mActivity;
    private Button mDone;
    private Intent mIntent;
    private View mRootView;
    private TextView mUpdateFailedDesc;
    private TextView mUpdateFailedTitle;

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) { // from class: com.motorola.ccc.ota.ui.VitalUpdateFailedFragment.1
            public void handleOnBackPressed() {
                Logger.info("OtaApp", "VitalUpdateFailedFragment, handleOnBackPressed");
                UpdaterUtils.launchNextSetupActivityVitalUpdate(VitalUpdateFailedFragment.this.mActivity);
                VitalUpdateFailedFragment.this.handleOnCancelUpdateFragment();
            }
        });
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.vital_check_update, viewGroup, false);
        initUI();
        return this.mRootView;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getResources();
    }

    public void onStart() {
        super.onStart();
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
    }

    void initUI() {
        Logger.debug("OtaApp", "Vital Update Fail Status displayed to user");
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.mIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        }
        this.mUpdateFailedDesc = (TextView) this.mRootView.findViewById(R.id.vitalUpdateBody);
        this.mUpdateFailedTitle = (TextView) this.mRootView.findViewById(R.id.vitalUpdateTitle);
        Button button = (Button) this.mRootView.findViewById(R.id.button_with_bg);
        this.mDone = button;
        button.setVisibility(0);
        this.mUpdateFailedDesc.setText(getResources().getString(R.string.vital_error_fail));
        this.mUpdateFailedTitle.setText(getResources().getString(R.string.update_fail_status));
        this.mDone.setText(getResources().getString(R.string.alert_dialog_ok));
        this.mDone.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.VitalUpdateFailedFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Logger.info("OtaApp", "OTA Update Fail Status accepted by user!");
                UpdaterUtils.launchNextSetupActivityVitalUpdate(VitalUpdateFailedFragment.this.mActivity);
                VitalUpdateFailedFragment.this.handleOnCancelUpdateFragment();
            }
        });
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
