package com.motorola.ccc.ota.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.utils.BroadcastUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WarningAlertDialog extends DialogFragment {
    private static final String ARG_DISMISS_ACTIVITY = "arg_dismiss_activity";
    private static final String ARG_ID = "arg_id";
    private static final String ARG_MESSAGE = "arg_message";
    private static final String ARG_NEGATIVE = "arg_negative";
    private static final String ARG_POSITIVE = "arg_positve";
    private static final String ARG_TITLE = "arg_title";
    private boolean dismissActivity;
    private int id;
    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.WarningAlertDialog.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Logger.debug("OtaApp", "Finished warning alert dialog");
            if (UpgradeUtilConstants.FINISH_WARNING_ALERT_DIALOG.equals(intent.getAction())) {
                WarningAlertDialog.this.dismissAllowingStateLoss();
            }
        }
    };
    private UpdaterUtils.OnDialogInteractionListener mListener;
    private String message;
    private String negativeText;
    private String positiveText;
    private String title;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static class WarningAlertDialogBuilder {
        int mButtonColor;
        boolean mDismissActivity = true;
        int mId;
        String mMessage;
        String mNegativeMessage;
        String mPositiveMessage;
        String mTitle;

        public WarningAlertDialogBuilder(int i) {
            this.mId = i;
        }

        public WarningAlertDialogBuilder setTitle(String str) {
            this.mTitle = str;
            return this;
        }

        public WarningAlertDialogBuilder setMessage(String str) {
            this.mMessage = str;
            return this;
        }

        public WarningAlertDialogBuilder setPositiveText(String str) {
            this.mPositiveMessage = str;
            return this;
        }

        public WarningAlertDialogBuilder setNegativeText(String str) {
            this.mNegativeMessage = str;
            return this;
        }

        public WarningAlertDialogBuilder setDismissActivity(boolean z) {
            this.mDismissActivity = z;
            return this;
        }

        public WarningAlertDialogBuilder setButtonColor(int i) {
            this.mButtonColor = i;
            return this;
        }

        public WarningAlertDialog buildDialog() {
            return WarningAlertDialog.newInstance(this.mMessage, this.mTitle, this.mPositiveMessage, this.mNegativeMessage, this.mDismissActivity, this.mId, this.mButtonColor);
        }

        public boolean isEmpty() {
            return TextUtils.isEmpty(this.mMessage) && TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mPositiveMessage) && TextUtils.isEmpty(this.mNegativeMessage);
        }
    }

    public static WarningAlertDialog newInstance(String str, String str2, String str3, String str4, boolean z, int i, int i2) {
        WarningAlertDialog warningAlertDialog = new WarningAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_MESSAGE, str);
        bundle.putString(ARG_TITLE, str2);
        bundle.putString(ARG_POSITIVE, str3);
        bundle.putString(ARG_NEGATIVE, str4);
        bundle.putBoolean(ARG_DISMISS_ACTIVITY, z);
        bundle.putInt(ARG_ID, i);
        warningAlertDialog.setArguments(bundle);
        return warningAlertDialog;
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }

    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View inflate = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog, (ViewGroup) null);
        if (!TextUtils.isEmpty(this.title)) {
            TextView textView = (TextView) inflate.findViewById(R.id.txtDialogTitle);
            textView.setVisibility(0);
            textView.setText(this.title);
        }
        if (!TextUtils.isEmpty(this.message)) {
            TextView textView2 = (TextView) inflate.findViewById(R.id.txtMessage);
            textView2.setText(this.message);
            UpdaterUtils.makeTextViewLinkify(textView2, this.message);
        }
        if (!TextUtils.isEmpty(this.positiveText)) {
            Button button = (Button) inflate.findViewById(R.id.btnPositive);
            button.setVisibility(0);
            button.setText(this.positiveText);
            button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.WarningAlertDialog$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    WarningAlertDialog.this.m296lambda$onCreateDialog$0$commotorolacccotauiWarningAlertDialog(view);
                }
            });
        }
        if (!TextUtils.isEmpty(this.negativeText)) {
            Button button2 = (Button) inflate.findViewById(R.id.btnNegative);
            button2.setVisibility(0);
            button2.setText(this.negativeText);
            button2.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.WarningAlertDialog$$ExternalSyntheticLambda1
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    WarningAlertDialog.this.m297lambda$onCreateDialog$1$commotorolacccotauiWarningAlertDialog(view);
                }
            });
        }
        builder.setView(inflate);
        AlertDialog create = builder.create();
        UpdaterUtils.setCornersRounded(getContext(), create);
        return create;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$onCreateDialog$0$com-motorola-ccc-ota-ui-WarningAlertDialog  reason: not valid java name */
    public /* synthetic */ void m296lambda$onCreateDialog$0$commotorolacccotauiWarningAlertDialog(View view) {
        dismiss();
        Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            targetFragment.onActivityResult(getTargetRequestCode(), 0, (Intent) null);
        } else {
            this.mListener.onPositiveClick(this.id, null);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$onCreateDialog$1$com-motorola-ccc-ota-ui-WarningAlertDialog  reason: not valid java name */
    public /* synthetic */ void m297lambda$onCreateDialog$1$commotorolacccotauiWarningAlertDialog(View view) {
        this.mListener.onNegativeClick(this.id);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.message = getArguments().getString(ARG_MESSAGE);
            this.title = getArguments().getString(ARG_TITLE);
            this.positiveText = getArguments().getString(ARG_POSITIVE);
            this.negativeText = getArguments().getString(ARG_NEGATIVE);
            this.dismissActivity = getArguments().getBoolean(ARG_DISMISS_ACTIVITY);
            this.id = getArguments().getInt(ARG_ID);
        }
        setCancelable(false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.FINISH_WARNING_ALERT_DIALOG);
        BroadcastUtils.registerLocalReceiver(getContext(), this.mFinishReceiver, intentFilter);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof UpdaterUtils.OnDialogInteractionListener) {
            this.mListener = (UpdaterUtils.OnDialogInteractionListener) context;
            return;
        }
        throw new RuntimeException(context.toString() + " must implement OnDialogInteractionListener");
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        UpdaterUtils.OnDialogInteractionListener onDialogInteractionListener = this.mListener;
        if (onDialogInteractionListener != null) {
            onDialogInteractionListener.onDismiss(this.id, this.dismissActivity);
        }
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        BroadcastUtils.unregisterLocalReceiver(getContext(), this.mFinishReceiver);
    }
}
