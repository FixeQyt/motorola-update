package com.motorola.ccc.ota.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.utils.BroadcastUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DownloadOptionsFragment extends DialogFragment {
    public static final String ARG_PARAM_ID = "param_id";
    public static final String ARG_PARAM_MODE = "param_mode";
    private static final String ARG_PARAM_SIZE = "param_size";
    private static final String ARG_PARAM_UPDATE_TYPE = "param_update_type";
    private TextView mDlOptionNotes;
    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.DownloadOptionsFragment.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.debug("OtaApp", "Finished download options dialogFragment");
            if (UpgradeUtilConstants.FINISH_DOWNLOAD_OPTIONS_FRAGMENT.equals(intent.getAction())) {
                DownloadOptionsFragment.this.dismiss();
            }
        }
    };
    private String mParamDownloadMode;
    private int mParamId;
    private long mParamSize;
    private String mParamUpdateType;
    private UpdateType.UpdateTypeInterface mUpdateType;
    private RadioButton mWifiAndMobile;
    private RadioButton mWifiOnly;

    public static DownloadOptionsFragment newInstance(String str, long j, String str2, int i) {
        DownloadOptionsFragment downloadOptionsFragment = new DownloadOptionsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PARAM_UPDATE_TYPE, str);
        bundle.putLong(ARG_PARAM_SIZE, j);
        bundle.putInt(ARG_PARAM_ID, i);
        bundle.putString(ARG_PARAM_MODE, str2);
        downloadOptionsFragment.setArguments(bundle);
        return downloadOptionsFragment;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.mParamUpdateType = getArguments().getString(ARG_PARAM_UPDATE_TYPE);
            this.mParamSize = getArguments().getLong(ARG_PARAM_SIZE);
            this.mParamId = getArguments().getInt(ARG_PARAM_ID);
            this.mParamDownloadMode = getArguments().getString(ARG_PARAM_MODE);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.FINISH_DOWNLOAD_OPTIONS_FRAGMENT);
        BroadcastUtils.registerLocalReceiver(getContext(), this.mFinishReceiver, intentFilter);
    }

    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() { // from class: com.motorola.ccc.ota.ui.DownloadOptionsFragment$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnKeyListener
            public final boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                return DownloadOptionsFragment.this.m178lambda$onResume$0$commotorolacccotauiDownloadOptionsFragment(dialogInterface, i, keyEvent);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$onResume$0$com-motorola-ccc-ota-ui-DownloadOptionsFragment  reason: not valid java name */
    public /* synthetic */ boolean m178lambda$onResume$0$commotorolacccotauiDownloadOptionsFragment(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
        if (i == 4) {
            dismiss();
            return true;
        }
        return false;
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.download_wifi_option, viewGroup, false);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.corner_rounded);
        this.mUpdateType = UpdateType.getUpdateType(this.mParamUpdateType);
        setDisplaySize(this.mParamSize, (TextView) inflate.findViewById(R.id.download_size));
        this.mDlOptionNotes = (TextView) inflate.findViewById(R.id.dloptionsnotes);
        handleRadioGroup(inflate);
        ((Button) inflate.findViewById(R.id.done)).setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.DownloadOptionsFragment$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                DownloadOptionsFragment.this.m177lambda$onCreateView$1$commotorolacccotauiDownloadOptionsFragment(view);
            }
        });
        this.mWifiAndMobile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.motorola.ccc.ota.ui.DownloadOptionsFragment.1
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (z) {
                    DownloadOptionsFragment.this.mDlOptionNotes.setVisibility(0);
                } else {
                    DownloadOptionsFragment.this.mDlOptionNotes.setVisibility(8);
                }
            }
        });
        return inflate;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$onCreateView$1$com-motorola-ccc-ota-ui-DownloadOptionsFragment  reason: not valid java name */
    public /* synthetic */ void m177lambda$onCreateView$1$commotorolacccotauiDownloadOptionsFragment(View view) {
        int ordinal;
        Intent intent = new Intent();
        intent.putExtra(ARG_PARAM_MODE, this.mParamDownloadMode);
        if (this.mWifiAndMobile.isChecked()) {
            ordinal = UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI_AND_MOBILE.ordinal();
        } else {
            ordinal = UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI.ordinal();
        }
        intent.putExtra(ARG_PARAM_ID, ordinal);
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            targetFragment.onActivityResult(getTargetRequestCode(), 2, intent);
        }
        dismiss();
    }

    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
    }

    public void onDestroy() {
        super.onDestroy();
        BroadcastUtils.unregisterLocalReceiver(getContext(), this.mFinishReceiver);
    }

    private void handleRadioGroup(View view) {
        this.mWifiOnly = (RadioButton) view.findViewById(R.id.picker_wifi_only_radio);
        RadioButton radioButton = (RadioButton) view.findViewById(R.id.picker_cell_radio);
        this.mWifiAndMobile = radioButton;
        radioButton.setChecked(true);
        this.mDlOptionNotes.setVisibility(0);
    }

    private void setDisplaySize(long j, TextView textView) {
        textView.setText(getResources().getString(R.string.package_size, Formatter.formatFileSize(getContext(), j)));
    }
}
