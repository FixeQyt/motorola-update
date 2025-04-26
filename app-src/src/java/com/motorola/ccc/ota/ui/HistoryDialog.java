package com.motorola.ccc.ota.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class HistoryDialog extends DialogFragment implements SmartUpdateUtils.SmartDialog {
    private Intent activityIntent;
    private ExpandableListView expandableListView;
    private TextView mExpandableHeading;
    private TextView mHeading;
    private Button mOkayButton;
    private String mReleaseNotes;
    private View mRootView;
    private String mSourceVersion;
    private String mTargetVersion;
    private String mUpdateNotes;
    private String mUpdateType;
    private TextView mUpgradeNotesTextView;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle arguments = getArguments();
        BotaSettings botaSettings = new BotaSettings();
        if (arguments != null) {
            Intent intent = (Intent) arguments.getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
            this.activityIntent = intent;
            this.mSourceVersion = intent.getStringExtra(UpgradeUtilConstants.KEY_HISTORY_SOURCE_VERSION);
            this.mTargetVersion = this.activityIntent.getStringExtra(UpgradeUtilConstants.KEY_HISTORY_TARGET_VERSION);
            this.mUpdateType = this.activityIntent.getStringExtra(UpgradeUtilConstants.KEY_UPDATE_TYPE);
            this.mReleaseNotes = this.activityIntent.getStringExtra(UpgradeUtilConstants.KEY_HISTORY_RELEASE_NOTES);
            this.mUpdateNotes = this.activityIntent.getStringExtra(UpgradeUtilConstants.KEY_HISTORY_UPGRADE_NOTES);
        }
        botaSettings.incrementPrefs(Configs.HISTORY_TAB_CLICK_COUNT);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.history_dialog, viewGroup, false);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.corner_rounded);
        UpdaterUtils.setNavBarColorFromDialog(getDialog());
        findViewsById();
        handleButtons();
        return this.mRootView;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        if (TextUtils.isEmpty(this.mSourceVersion)) {
            this.mHeading.setText(getContext().getResources().getString(R.string.update_history_version, this.mTargetVersion));
        } else {
            this.mHeading.setText(getContext().getResources().getString(R.string.update_history_src_target, this.mSourceVersion, this.mTargetVersion));
        }
        if (!TextUtils.isEmpty(this.mReleaseNotes)) {
            ExpandableListView expandableList = UpdaterUtils.setExpandableList(this.expandableListView, true, this.mReleaseNotes, this.mExpandableHeading, getActivity(), UpdateType.getUpdateType(this.mUpdateType), this.mRootView.findViewById(R.id.dash_line));
            this.expandableListView = expandableList;
            this.expandableListView = UpdaterUtils.handleExpList(expandableList, "history");
        } else if (TextUtils.isEmpty(this.mUpdateNotes)) {
        } else {
            this.mUpgradeNotesTextView.setText(Html.fromHtml(this.mUpdateNotes, 0, null, new HtmlUtils(this.mUpdateNotes)));
            this.mUpgradeNotesTextView.setMovementMethod(LinkMovementMethod.getInstance());
            this.mUpgradeNotesTextView.setVisibility(0);
        }
    }

    private void findViewsById() {
        this.mHeading = (TextView) this.mRootView.findViewById(R.id.history_dialog_title);
        this.mExpandableHeading = (TextView) this.mRootView.findViewById(R.id.expandable_list_title);
        this.mUpgradeNotesTextView = (TextView) this.mRootView.findViewById(R.id.updateNotes);
        this.expandableListView = (ExpandableListView) this.mRootView.findViewById(R.id.history_dialog_notes);
        this.mOkayButton = (Button) this.mRootView.findViewById(R.id.history_okay);
    }

    private void handleButtons() {
        this.mOkayButton.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.HistoryDialog.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                HistoryDialog.this.dismiss();
            }
        });
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.SmartDialog
    public void dismissSmartDialog() {
        dismiss();
    }
}
