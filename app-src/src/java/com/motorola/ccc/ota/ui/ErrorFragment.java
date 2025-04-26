package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.airbnb.lottie.LottieAnimationView;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.utils.BuildPropReader;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ErrorFragment extends Fragment {
    private Activity activity;
    private Button btnRetry;
    private LottieAnimationView lottieAnimationView;
    private String mErrorMsg = "";
    private String mErrorTitle = "";
    private Intent mIntent;
    private View rootView;
    private TextView txtBadConnBody;
    private TextView txtBadConnHeader;

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.activity = (Activity) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must be an instance of Activity");
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.mIntent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
            this.mErrorMsg = arguments.getString(UpdaterUtils.KEY_ERR_MSG);
            this.mErrorTitle = arguments.getString(UpdaterUtils.KEY_ERR_TITLE);
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.error, viewGroup, false);
        this.rootView = inflate;
        TextView textView = (TextView) inflate.findViewById(R.id.txtBadConnBody);
        this.txtBadConnBody = textView;
        textView.setMovementMethod(new ScrollingMovementMethod());
        this.txtBadConnHeader = (TextView) this.rootView.findViewById(R.id.txtBadConnHeader);
        this.btnRetry = (Button) this.rootView.findViewById(R.id.btnRetry);
        LottieAnimationView lottieAnimationView = (LottieAnimationView) this.rootView.findViewById(R.id.lottieAlertMark);
        this.lottieAnimationView = lottieAnimationView;
        lottieAnimationView.setRepeatCount(-1);
        this.lottieAnimationView.setAnimation("alert_state.json");
        this.txtBadConnBody.setText(this.mErrorMsg);
        this.txtBadConnHeader.setText(this.mErrorTitle);
        if (getString(R.string.bootloader_unlock_title).equals(this.mErrorTitle)) {
            this.btnRetry.setText(getString(R.string.done));
        } else if (getString(R.string.vab_validation_update_success_title).equals(this.mErrorTitle)) {
            this.btnRetry.setText(getString(R.string.done));
            this.lottieAnimationView.setAnimation("check_mark.json");
        } else if (getString(R.string.vab_validation_update_failure_title).equals(this.mErrorTitle)) {
            this.btnRetry.setText(getString(R.string.done));
        }
        this.lottieAnimationView.playAnimation();
        this.btnRetry.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.ErrorFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (((Button) view).getText().equals(ErrorFragment.this.getString(R.string.try_again))) {
                    ErrorFragment.this.activity.sendBroadcast(new Intent(UpdaterUtils.ACTION_MANUAL_CHECK_UPDATE), Permissions.INTERACT_OTA_SERVICE);
                    UpdaterUtils.sendCheckUpdateIntent(ErrorFragment.this.activity);
                } else if (ErrorFragment.this.activity != null) {
                    ErrorFragment.this.activity.finish();
                }
            }
        });
        if (BuildPropReader.isATT()) {
            this.btnRetry.setText(getString(R.string.done));
        }
        return this.rootView;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
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
}
