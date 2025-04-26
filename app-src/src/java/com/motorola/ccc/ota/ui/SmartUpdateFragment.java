package com.motorola.ccc.ota.ui;

import android.app.TimePickerDialog;
import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.SmartUpdateFragment;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class SmartUpdateFragment extends DialogFragment implements SmartUpdateUtils.SmartDialog {
    private Intent activityIntent;
    private Button advancedOptions;
    private TextInputLayout customEndTime;
    private EditText customEndTimeEd;
    private TextInputLayout customStartTime;
    private EditText customStartTimeEd;
    private Switch enableUpdateTypeSwitch;
    private ImageView imgBack;
    private LinearLayout lnrSmartUpdateSwitch;
    private FragmentActivity mActivity;
    private Context mContext;
    private String mLaunchMode;
    private View mRootView;
    private RadioGroup radioGroupTimeSlot;
    private RadioButton radioSlot1;
    private RadioButton radioSlot2;
    private RadioButton radioSlot3;
    private RadioButton radioSlotCustom;
    private int selectedHour;
    private int selectedMinute;
    private BotaSettings settings;
    private TextView smartUpdateNote;
    private TextView smartUpdateText;
    private Switch switchSmartUpdate;
    private TimePickerDialog timePickerDialog;
    private TextView timeSlotText;
    private AlertDialog turnOffSureSmartUpdateDialog;
    private TextView useSmartUpdates;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof FragmentActivity) {
            this.mActivity = (FragmentActivity) context;
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.settings = new BotaSettings();
        setStyle(0, R.style.CustomiseActivityTheme);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.smart_update, viewGroup, false);
        Intent intent = (Intent) getArguments().getParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT);
        this.activityIntent = intent;
        this.mLaunchMode = intent.getStringExtra(UpgradeUtilConstants.KEY_LAUNCH_MODE);
        UpdaterUtils.setNavBarColorFromDialog(getDialog());
        Window window = getDialog().getWindow();
        window.setStatusBarColor(0);
        if (!UpdaterUtils.isBatterySaverEnabled(this.mContext)) {
            WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
            if (((UiModeManager) this.mContext.getSystemService("uimode")).getNightMode() != 2) {
                windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
                windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
            }
        }
        findViewsById();
        handleButtons();
        initUI();
        return this.mRootView;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        if (getArguments() == null || this.activityIntent == null) {
            return;
        }
        this.settings.setString(Configs.STATS_SMART_UPDATE_LAUNCH_MODE, this.mLaunchMode);
    }

    private void findViewsById() {
        this.lnrSmartUpdateSwitch = (LinearLayout) this.mRootView.findViewById(R.id.lnrSmartUpdateSwitch);
        this.switchSmartUpdate = (Switch) this.mRootView.findViewById(R.id.switchSmartUpdate);
        this.radioGroupTimeSlot = (RadioGroup) this.mRootView.findViewById(R.id.radioGroupTimeSlot);
        this.radioSlot1 = (RadioButton) this.mRootView.findViewById(R.id.radioSlot1);
        this.radioSlot2 = (RadioButton) this.mRootView.findViewById(R.id.radioSlot2);
        this.radioSlot3 = (RadioButton) this.mRootView.findViewById(R.id.radioSlot3);
        this.radioSlotCustom = (RadioButton) this.mRootView.findViewById(R.id.radioSlotCustom);
        this.customStartTime = this.mRootView.findViewById(R.id.edtStartTimeLt);
        this.customStartTimeEd = (EditText) this.mRootView.findViewById(R.id.edtStartTimeEd);
        this.customEndTime = this.mRootView.findViewById(R.id.txtEndTimeLt);
        this.customEndTimeEd = (EditText) this.mRootView.findViewById(R.id.edtEndTimeEd);
        this.smartUpdateText = (TextView) this.mRootView.findViewById(R.id.smartUpdateText);
        this.imgBack = (ImageView) this.mRootView.findViewById(R.id.imgBack);
        this.advancedOptions = (Button) this.mRootView.findViewById(R.id.advanceOptions);
        this.timeSlotText = (TextView) this.mRootView.findViewById(R.id.timeSlotText);
        this.enableUpdateTypeSwitch = (Switch) this.mRootView.findViewById(R.id.enableUpdateTypeSwitch);
        this.smartUpdateNote = (TextView) this.mRootView.findViewById(R.id.smartUpdateNote);
        this.useSmartUpdates = (TextView) this.mRootView.findViewById(R.id.useSmartUpdates);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initUI() {
        this.customStartTime.getEditText().setInputType(0);
        this.customStartTime.setFocusable(false);
        this.radioSlot1.setText(SmartUpdateUtils.getDefaultTextForTimeSlot(getContext(), R.id.radioSlot1));
        this.radioSlot2.setText(SmartUpdateUtils.getDefaultTextForTimeSlot(getContext(), R.id.radioSlot2));
        this.radioSlot3.setText(SmartUpdateUtils.getDefaultTextForTimeSlot(getContext(), R.id.radioSlot3));
        checkSelectedTimeSlot();
        if (BuildPropReader.isUEUpdateEnabled()) {
            if (UpdaterUtils.isVerizon()) {
                this.smartUpdateText.setText(R.string.smart_update_text_AB_vzw);
            } else {
                this.smartUpdateText.setText(R.string.smart_update_text_AB);
            }
            this.timeSlotText.setText(getResources().getString(R.string.smart_update_time_slot_text_AB));
        } else {
            if (UpdaterUtils.isVerizon()) {
                this.smartUpdateText.setText(R.string.smart_update_text_classic_vzw);
            } else {
                this.smartUpdateText.setText(R.string.smart_update_text_classic);
            }
            this.timeSlotText.setText(getResources().getString(R.string.smart_update_time_slot_text_classic));
        }
        if (UpdaterUtils.isVerizon()) {
            this.smartUpdateNote.setText(R.string.smart_update_note_vzw);
        } else {
            this.smartUpdateNote.setText(R.string.smart_update_note);
        }
        this.switchSmartUpdate.setOnCheckedChangeListener(null);
        if (SmartUpdateUtils.isSmartUpdateEnabledByUser(this.settings)) {
            this.switchSmartUpdate.setChecked(true);
            this.useSmartUpdates.setTextColor(getContext().getColor(R.color.background_color));
            this.lnrSmartUpdateSwitch.setBackground(getContext().getDrawable(R.drawable.settingslib_switch_bar_bg_on));
        } else {
            String str = this.mLaunchMode;
            if (str != null && (str.equals(SmartUpdateUtils.SmartUpdateLaunchMode.TRY_SMART_UPDATES_POPUP_DOWNLOAD.name()) || this.mLaunchMode.equals(SmartUpdateUtils.SmartUpdateLaunchMode.TRY_SMART_UPDATES_POPUP_RESTART.name()) || this.mLaunchMode.equals(SmartUpdateUtils.SmartUpdateLaunchMode.TRY_SMART_UPDATES_POPUP_INSTALL.name()) || this.mLaunchMode.equals(SmartUpdateUtils.SmartUpdateLaunchMode.TRY_SMART_UPDATES_POPUP_UPDATE_COMPLETE.name()))) {
                this.mLaunchMode = null;
                this.switchSmartUpdate.setChecked(true);
                this.useSmartUpdates.setTextColor(getContext().getColor(R.color.background_color));
                this.lnrSmartUpdateSwitch.setBackground(getContext().getDrawable(R.drawable.settingslib_switch_bar_bg_on));
                smartUpdateTurnOn();
                initUI();
                return;
            }
            setViewsVisibility(false);
        }
        this.switchSmartUpdate.setOnCheckedChangeListener(new SwitchSmartUpdateListener());
        View findViewById = this.mRootView.findViewById(R.id.dash_line);
        if (SmartUpdateUtils.isForcedMRUpdateEnabledByServer()) {
            if (SmartUpdateUtils.isShowAdvancedSettingValueEnabledByServer()) {
                this.advancedOptions.setVisibility(0);
                this.enableUpdateTypeSwitch.setVisibility(0);
                findViewById.setVisibility(0);
                this.advancedOptions.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_drop_up_black_24dp, 0, 0, 0);
                this.enableUpdateTypeSwitch.setText(R.string.apply_os_update);
                this.enableUpdateTypeSwitch.setChecked(this.settings.getBoolean(Configs.SMART_UPDATE_OS_ENABLE_BY_USER));
                return;
            }
            this.advancedOptions.setVisibility(8);
            findViewById.setVisibility(8);
        } else if (SmartUpdateUtils.isShowAdvancedSettingValueEnabledByServer()) {
            this.advancedOptions.setVisibility(0);
            this.enableUpdateTypeSwitch.setVisibility(0);
            findViewById.setVisibility(0);
            this.advancedOptions.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_drop_up_black_24dp, 0, 0, 0);
            this.enableUpdateTypeSwitch.setText(R.string.apply_bug_fix);
            this.enableUpdateTypeSwitch.setChecked(this.settings.getBoolean(Configs.SMART_UPDATE_MR_ENABLE_BY_USER));
        } else {
            this.advancedOptions.setVisibility(8);
            findViewById.setVisibility(8);
        }
    }

    private void handleButtons() {
        this.advancedOptions.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.SmartUpdateFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (8 == SmartUpdateFragment.this.enableUpdateTypeSwitch.getVisibility()) {
                    SmartUpdateFragment.this.enableUpdateTypeSwitch.setVisibility(0);
                    SmartUpdateFragment.this.advancedOptions.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_drop_up_black_24dp, 0, 0, 0);
                    return;
                }
                SmartUpdateFragment.this.enableUpdateTypeSwitch.setVisibility(8);
                SmartUpdateFragment.this.advancedOptions.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_drop_down_black_24dp, 0, 0, 0);
            }
        });
        this.radioSlotCustom.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.SmartUpdateFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SmartUpdateFragment.this.showTimePickerDialog();
            }
        });
        this.customStartTime.getEditText().setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.SmartUpdateFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SmartUpdateFragment.this.showTimePickerDialog();
            }
        });
        this.imgBack.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.SmartUpdateFragment.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (SmartUpdateFragment.this.getFragmentManager().getFragments().toArray().length <= 1) {
                    SmartUpdateFragment.this.mActivity.finish();
                }
                SmartUpdateFragment.this.dismiss();
            }
        });
        this.enableUpdateTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.motorola.ccc.ota.ui.SmartUpdateFragment.5
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                if (z) {
                    if (SmartUpdateFragment.this.enableUpdateTypeSwitch.getText().equals(SmartUpdateFragment.this.getString(R.string.apply_os_update))) {
                        SmartUpdateFragment.this.settings.setBoolean(Configs.SMART_UPDATE_OS_ENABLE_BY_USER, true);
                    } else {
                        SmartUpdateFragment.this.settings.setBoolean(Configs.SMART_UPDATE_MR_ENABLE_BY_USER, true);
                    }
                } else if (SmartUpdateFragment.this.enableUpdateTypeSwitch.getText().equals(SmartUpdateFragment.this.getString(R.string.apply_os_update))) {
                    SmartUpdateFragment.this.settings.setBoolean(Configs.SMART_UPDATE_OS_ENABLE_BY_USER, false);
                } else {
                    SmartUpdateFragment.this.settings.setBoolean(Configs.SMART_UPDATE_MR_ENABLE_BY_USER, false);
                }
            }
        });
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() { // from class: com.motorola.ccc.ota.ui.SmartUpdateFragment.6
            @Override // android.content.DialogInterface.OnKeyListener
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == 4) {
                    if (SmartUpdateFragment.this.getFragmentManager().getFragments().toArray().length <= 1) {
                        SmartUpdateFragment.this.mActivity.finish();
                    }
                    SmartUpdateFragment.this.dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void smartUpdateTurnOff() {
        SmartUpdateUtils.sendSmartUpdateConfigChangedIntent(this.mContext, false);
        setViewsVisibility(false);
        SmartUpdateUtils.resetSmartUpdatePrefs(this.settings);
        this.settings.setString(Configs.STATS_SMART_UPDATE_DISABLED_VIA, "smart_frgment");
        this.customStartTimeEd.setText("");
        this.customEndTimeEd.setText("");
        if (this.settings.getBoolean(Configs.STATS_INSTALL_VIA_SMART_UPDATE)) {
            this.settings.setBoolean(Configs.STATS_DISABLED_SMART_UPDATE_AFTER_INSTALL, true);
        }
        initUI();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void smartUpdateTurnOn() {
        SmartUpdateUtils.sendSmartUpdateConfigChangedIntent(this.mContext, true);
        setViewsVisibility(true);
        SmartUpdateUtils.setSmartUpdateEnableByUser(this.settings, true);
        if (TextUtils.isEmpty(this.settings.getString(Configs.SMART_UPDATE_TIME_SLOT))) {
            saveTimeSlot(SmartUpdateUtils.getMappedTimeSlotForPrefs(R.id.radioSlot1));
        }
        SmartUpdateUtils.settingInstallTimeIntervalForSmartUpdate(this.settings);
        this.settings.removeConfig(Configs.STATS_DISABLED_SMART_UPDATE_AFTER_INSTALL);
        this.settings.setString(Configs.STATS_SMART_UPDATE_ENABLED_VIA, "smart_frgment");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this.mContext, R.style.CustomTimePickerDialog, new TimePickerDialog.OnTimeSetListener() { // from class: com.motorola.ccc.ota.ui.SmartUpdateFragment.7
            @Override // android.app.TimePickerDialog.OnTimeSetListener
            public void onTimeSet(TimePicker timePicker, int i, int i2) {
                SmartUpdateFragment.this.selectedHour = i;
                SmartUpdateFragment.this.selectedMinute = i2;
                SmartUpdateFragment.this.customStartTimeEd.setText(DateFormatUtils.formatTimeContent(SmartUpdateFragment.this.getContext(), i, i2));
                int i3 = i + 2;
                SmartUpdateFragment.this.customEndTimeEd.setText(DateFormatUtils.formatTimeContent(SmartUpdateFragment.this.getContext(), i3, i2));
                String mappedTimeSlotForPrefs = SmartUpdateUtils.getMappedTimeSlotForPrefs(i, i2, i3, i2);
                if (SmartUpdateUtils.getMappedTimeSlotForPrefs(R.id.radioSlot1).equals(mappedTimeSlotForPrefs)) {
                    SmartUpdateFragment.this.radioSlot1.setChecked(true);
                } else if (SmartUpdateUtils.getMappedTimeSlotForPrefs(R.id.radioSlot2).equals(mappedTimeSlotForPrefs)) {
                    SmartUpdateFragment.this.radioSlot2.setChecked(true);
                } else if (SmartUpdateUtils.getMappedTimeSlotForPrefs(R.id.radioSlot3).equals(mappedTimeSlotForPrefs)) {
                    SmartUpdateFragment.this.radioSlot3.setChecked(true);
                } else {
                    SmartUpdateFragment.this.radioSlotCustom.setChecked(true);
                    SmartUpdateFragment.this.saveTimeSlot(mappedTimeSlotForPrefs);
                    SmartUpdateUtils.settingInstallTimeIntervalForSmartUpdate(SmartUpdateFragment.this.settings);
                }
            }
        }, this.selectedHour, this.selectedMinute, DateFormat.is24HourFormat(getActivity()));
        this.timePickerDialog = timePickerDialog;
        timePickerDialog.show();
    }

    private void setViewsVisibility(boolean z) {
        for (int i = 0; i < this.radioGroupTimeSlot.getChildCount(); i++) {
            this.radioGroupTimeSlot.getChildAt(i).setEnabled(z);
        }
        this.customStartTime.setEnabled(z);
        this.customEndTime.setEnabled(z);
        this.timeSlotText.setEnabled(z);
        this.advancedOptions.setEnabled(z);
        this.enableUpdateTypeSwitch.setEnabled(z);
    }

    private void checkSelectedTimeSlot() {
        this.radioGroupTimeSlot.setOnCheckedChangeListener(null);
        String string = this.settings.getString(Configs.SMART_UPDATE_TIME_SLOT);
        if (TextUtils.isEmpty(string) || SmartUpdateUtils.getMappedTimeSlotForPrefs(R.id.radioSlot1).equals(string)) {
            this.radioGroupTimeSlot.check(R.id.radioSlot1);
        } else if (SmartUpdateUtils.getMappedTimeSlotForPrefs(R.id.radioSlot2).equals(string)) {
            this.radioGroupTimeSlot.check(R.id.radioSlot2);
        } else if (SmartUpdateUtils.getMappedTimeSlotForPrefs(R.id.radioSlot3).equals(string)) {
            this.radioGroupTimeSlot.check(R.id.radioSlot3);
        } else {
            this.radioGroupTimeSlot.check(R.id.radioSlotCustom);
            List<Calendar> parseTimeslot = SmartUpdateUtils.parseTimeslot(string);
            if (parseTimeslot != null && parseTimeslot.size() == 2) {
                Calendar calendar = parseTimeslot.get(0);
                this.customStartTime.getEditText().setText(DateFormatUtils.formatTimeContent(getContext(), calendar));
                this.customEndTime.getEditText().setText(DateFormatUtils.formatTimeContent(getContext(), parseTimeslot.get(1)));
                this.selectedHour = calendar.get(11);
                this.selectedMinute = calendar.get(12);
            }
        }
        this.radioGroupTimeSlot.setOnCheckedChangeListener(new RadioGroupTimeSlotListener());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void saveTimeSlot(String str) {
        this.settings.setString(Configs.SMART_UPDATE_TIME_SLOT, str);
    }

    public void onDestroyView() {
        if (!SmartUpdateUtils.isSmartUpdateEnabledByUser(this.settings)) {
            this.settings.incrementPrefs(Configs.STATS_SMART_UPDATE_VISITED_BUT_DISABLED);
        }
        SmartUpdateUtils.decideToShowSmartUpdateSuggestion(OtaApplication.getGlobalContext());
        for (SmartUpdateUtils.OnSmartUpdateConfigChangedLister onSmartUpdateConfigChangedLister : getFragmentManager().getFragments()) {
            if (!this.mActivity.isFinishing() && (onSmartUpdateConfigChangedLister instanceof SmartUpdateUtils.OnSmartUpdateConfigChangedLister)) {
                onSmartUpdateConfigChangedLister.onSmartUpdateConfigChanged();
            }
        }
        super.onDestroyView();
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.SmartDialog
    public void dismissSmartDialog() {
        AlertDialog alertDialog = this.turnOffSureSmartUpdateDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        dismiss();
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        TimePickerDialog timePickerDialog = this.timePickerDialog;
        if (timePickerDialog != null) {
            timePickerDialog.dismiss();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public class RadioGroupTimeSlotListener implements RadioGroup.OnCheckedChangeListener {
        private RadioGroupTimeSlotListener() {
        }

        @Override // android.widget.RadioGroup.OnCheckedChangeListener
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (i) {
                case R.id.radioSlot1 /* 2131296567 */:
                case R.id.radioSlot2 /* 2131296568 */:
                case R.id.radioSlot3 /* 2131296569 */:
                    SmartUpdateFragment.this.saveTimeSlot(SmartUpdateUtils.getMappedTimeSlotForPrefs(i));
                    SmartUpdateUtils.settingInstallTimeIntervalForSmartUpdate(SmartUpdateFragment.this.settings);
                    return;
                default:
                    return;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class SwitchSmartUpdateListener implements CompoundButton.OnCheckedChangeListener {
        private SwitchSmartUpdateListener() {
        }

        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            if (z) {
                SmartUpdateFragment.this.smartUpdateTurnOn();
                SmartUpdateFragment.this.useSmartUpdates.setTextColor(SmartUpdateFragment.this.getContext().getColor(R.color.background_color));
                SmartUpdateFragment.this.lnrSmartUpdateSwitch.setBackground(SmartUpdateFragment.this.getContext().getDrawable(R.drawable.settingslib_switch_bar_bg_on));
            } else if (SmartUpdateUtils.isSmartUpdateEnabledByUser(SmartUpdateFragment.this.settings)) {
                String string = SmartUpdateFragment.this.mContext.getResources().getString(R.string.are_you_sure);
                String string2 = SmartUpdateFragment.this.mContext.getResources().getString(R.string.are_you_sure_pop_up_text);
                SmartUpdateFragment smartUpdateFragment = SmartUpdateFragment.this;
                smartUpdateFragment.turnOffSureSmartUpdateDialog = SmartUpdateUtils.getAndShowAreYouSurePopUp(smartUpdateFragment.mContext, string, string2, SmartUpdateFragment.this.mActivity, new DialogInterface.OnClickListener() { // from class: com.motorola.ccc.ota.ui.SmartUpdateFragment$SwitchSmartUpdateListener$$ExternalSyntheticLambda0
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }, new DialogInterface.OnClickListener() { // from class: com.motorola.ccc.ota.ui.SmartUpdateFragment$SwitchSmartUpdateListener$$ExternalSyntheticLambda1
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        SmartUpdateFragment.SwitchSmartUpdateListener.this.m253lambda$onCheckedChanged$1$commotorolacccotauiSmartUpdateFragment$SwitchSmartUpdateListener(dialogInterface, i);
                    }
                });
                SmartUpdateFragment.this.initUI();
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* renamed from: lambda$onCheckedChanged$1$com-motorola-ccc-ota-ui-SmartUpdateFragment$SwitchSmartUpdateListener  reason: not valid java name */
        public /* synthetic */ void m253lambda$onCheckedChanged$1$commotorolacccotauiSmartUpdateFragment$SwitchSmartUpdateListener(DialogInterface dialogInterface, int i) {
            SmartUpdateFragment.this.smartUpdateTurnOff();
            SmartUpdateFragment.this.switchSmartUpdate.setChecked(false);
            SmartUpdateFragment.this.useSmartUpdates.setTextColor(SmartUpdateFragment.this.getContext().getColor(R.color.black));
            SmartUpdateFragment.this.lnrSmartUpdateSwitch.setBackground(SmartUpdateFragment.this.getContext().getDrawable(R.drawable.settingslib_switch_bar_bg_off));
            dialogInterface.cancel();
        }
    }
}
