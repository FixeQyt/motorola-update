package com.motorola.ccc.ota.ui;

import android.app.UiModeManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.HistoryDbHandler;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpdateHistoryFragment extends DialogFragment implements SmartUpdateUtils.SmartDialog {
    private LinearLayout emptyHistory;
    private ScrollView historyScroll;
    private ImageView imgBack;
    private boolean isTransactionSafe;
    private LinearLayout mHistoryToolBar;
    private View mRootView;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.update_history, viewGroup, false);
        Window window = getDialog().getWindow();
        window.addFlags(Integer.MIN_VALUE);
        window.setStatusBarColor(0);
        if (!UpdaterUtils.isBatterySaverEnabled(getContext())) {
            WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
            if (((UiModeManager) getContext().getSystemService("uimode")).getNightMode() != 2) {
                windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
                windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
            }
        }
        UpdaterUtils.setNavBarColorFromDialog(getDialog());
        findViewsById();
        initUI();
        return this.mRootView;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setStyle(0, R.style.CustomiseActivityTheme);
        new BotaSettings().incrementPrefs(Configs.HISTORY_VISIT_COUNT);
    }

    public void onResume() {
        super.onResume();
        this.isTransactionSafe = true;
    }

    public void onPause() {
        super.onPause();
        this.isTransactionSafe = false;
    }

    private void findViewsById() {
        this.imgBack = (ImageView) this.mRootView.findViewById(R.id.imgBack);
        this.historyScroll = (ScrollView) this.mRootView.findViewById(R.id.history_scroll);
        this.mHistoryToolBar = (LinearLayout) this.mRootView.findViewById(R.id.update_history_toolbar);
        this.emptyHistory = (LinearLayout) this.mRootView.findViewById(R.id.no_history);
    }

    private void initUI() {
        handleButtons();
        LinearLayout linearLayout = (LinearLayout) this.mRootView.findViewById(R.id.history_parent_layout);
        ArrayList<HistoryDbHandler.History> history = new HistoryDbHandler(getContext()).getHistory();
        this.emptyHistory.setVisibility(8);
        this.historyScroll.setVisibility(0);
        if (history != null && !history.isEmpty()) {
            this.emptyHistory.setVisibility(8);
            this.historyScroll.setVisibility(0);
            Collections.reverse(history);
            Iterator<HistoryDbHandler.History> it = history.iterator();
            while (it.hasNext()) {
                HistoryDbHandler.History next = it.next();
                addHistoryLayouts(linearLayout, next.getSourceVersion(), next.getTargetVersion(), next.getUpdateType(), next.getUpdateTime(), next.getReleaseNotes(), next.getUpgradeNotes());
            }
            return;
        }
        this.emptyHistory.setVisibility(0);
        this.historyScroll.setVisibility(8);
    }

    private void addHistoryLayouts(LinearLayout linearLayout, final String str, final String str2, final String str3, long j, final String str4, final String str5) {
        if (j == 0) {
            Logger.debug("OtaApp", "Entry in the database is wrong let's ignore this row");
            return;
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(0, 0, 0, 0);
        LinearLayout linearLayout2 = (LinearLayout) ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.history_list, (ViewGroup) null);
        ((TextView) linearLayout2.findViewById(R.id.historyTitle)).setText(getContext().getResources().getString(R.string.update_history_version, str2));
        ((TextView) linearLayout2.findViewById(R.id.historyBody)).setText(getContext().getResources().getString(R.string.update_history_time, DateFormatUtils.getHistoryDate(j)));
        linearLayout2.setLayoutParams(layoutParams);
        linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdateHistoryFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Intent intent = UpdateHistoryFragment.this.getActivity().getIntent();
                intent.putExtra(UpgradeUtilConstants.KEY_HISTORY_SOURCE_VERSION, str);
                intent.putExtra(UpgradeUtilConstants.KEY_HISTORY_TARGET_VERSION, str2);
                intent.putExtra(UpgradeUtilConstants.KEY_HISTORY_UPDATE_TYPE, str3);
                intent.putExtra(UpgradeUtilConstants.KEY_HISTORY_RELEASE_NOTES, str4);
                intent.putExtra(UpgradeUtilConstants.KEY_HISTORY_UPGRADE_NOTES, str5);
                Bundle bundle = new Bundle();
                bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
                HistoryDialog historyDialog = new HistoryDialog();
                historyDialog.setArguments(bundle);
                if (UpdateHistoryFragment.this.isTransactionSafe) {
                    historyDialog.show(UpdateHistoryFragment.this.getActivity().getSupportFragmentManager(), "HistoryDescriptionDialog");
                }
            }
        });
        linearLayout.addView(linearLayout2);
    }

    private void handleButtons() {
        this.imgBack.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.UpdateHistoryFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                UpdateHistoryFragment.this.dismiss();
            }
        });
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.SmartDialog
    public void dismissSmartDialog() {
        dismiss();
    }
}
