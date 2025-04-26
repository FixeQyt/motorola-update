package com.motorola.ccc.ota.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class CriticalUpdate {
    private long mCriticalAnnoyValue;
    private String mLocationType;
    private int mSeverity;
    private UpdaterUtils.UpgradeInfo mUpgradeInfo;
    private BotaSettings settings = new BotaSettings();

    public CriticalUpdate(UpdaterUtils.UpgradeInfo upgradeInfo) {
        this.mUpgradeInfo = upgradeInfo;
        this.mCriticalAnnoyValue = upgradeInfo.getCriticalUpdateReminder() * 60 * 1000;
        this.mSeverity = upgradeInfo.getSeverity();
        this.mLocationType = upgradeInfo.getLocationType();
    }

    public boolean isCriticalUpdate() {
        if (this.mUpgradeInfo.isCriticalUpdate()) {
            return true;
        }
        return UpdaterUtils.isWaitForDozeModeOver();
    }

    public boolean isCriticalUpdateTimerExpired() {
        if (UpdaterUtils.isCriticalUpdateTimerExpired(this.mUpgradeInfo)) {
            UpdaterUtils.sendInstallModeStats("criticalUpdateAutoInstall");
            return true;
        } else if (UpdaterUtils.isWaitForDozeModeOver()) {
            UpdaterUtils.sendInstallModeStats("forceInstallAfterDozeWait");
            return true;
        } else {
            return false;
        }
    }

    public void setInstallStats() {
        if (UpdaterUtils.isCriticalUpdateTimerExpired(this.mUpgradeInfo)) {
            if (!isOutsideCriticalUpdateExtendedTime()) {
                UpdaterUtils.sendInstallModeStats("userInitiatedDuringCriticalUpdateExtendedPeriod");
            } else {
                UpdaterUtils.sendInstallModeStats("criticalUpdateAutoInstall");
            }
        } else if (isInCriticalUpdateExtraPopUpTime()) {
            UpdaterUtils.sendInstallModeStats("userInitiatedAfterCriticalAnnoy");
        } else if (UpdaterUtils.isWaitForDozeModeOver()) {
            UpdaterUtils.sendInstallModeStats("forceInstallAfterDozeWait");
        } else if (SmartUpdateUtils.isSmartUpdateTimerExpired(this.settings, this.mUpgradeInfo)) {
            UpdaterUtils.sendInstallModeStats("installedViaSmartUpdate");
        } else {
            UpdaterUtils.sendInstallModeStats("userInitiated");
        }
    }

    public long getAndSetNextPromptValue(long j) {
        long j2 = this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L) - j;
        long j3 = this.settings.getLong(Configs.NEXT_CRITICAL_UPDATE_PROMPT_TIME, -1L);
        if (BuildPropReader.isFotaATT()) {
            this.mCriticalAnnoyValue = 14400000L;
            if (j3 <= j) {
                if (j2 >= 14400000) {
                    j3 = j + 14400000;
                }
                j3 = j + j2;
            }
        } else if (j3 <= j) {
            if (j2 > this.mUpgradeInfo.getCriticalUpdateExtraWaitPeriodInMillis()) {
                j2 = j2 <= totalExtraPopUpTime() ? this.mUpgradeInfo.getCriticalUpdateExtraWaitPeriodInMillis() : this.mCriticalAnnoyValue;
            }
            j3 = j + j2;
        }
        this.settings.setLong(Configs.NEXT_CRITICAL_UPDATE_PROMPT_TIME, j3);
        Logger.debug("OtaApp", "next prompt is : " + j3);
        return j3;
    }

    public String getMaxUpdateCalendarString(Context context) {
        return DateFormatUtils.getCalendarString(context, this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L));
    }

    public AlertDialog buildPopUp(Context context, String str, String str2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View inflate = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.custom_dialog, (ViewGroup) null);
        builder.setView(inflate);
        final AlertDialog create = builder.create();
        TextView textView = (TextView) inflate.findViewById(R.id.custom_alert_title);
        textView.setVisibility(0);
        textView.setText(str);
        TextView textView2 = (TextView) inflate.findViewById(R.id.custom_alert_text);
        textView2.setVisibility(0);
        textView2.setText(str2);
        create.setCancelable(true);
        Button button = (Button) inflate.findViewById(R.id.custom_btn_allow);
        button.setVisibility(0);
        button.setText(context.getResources().getString(R.string.got_it));
        button.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.CriticalUpdate$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                create.cancel();
            }
        });
        UpdaterUtils.setCornersRounded(context, create);
        return create;
    }

    public boolean shouldAllowExtendedCriticalUpdate() {
        return isCriticalUpdate() && !BuildPropReader.isATT() && this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_EXTENDED_TIME, -1L) < 0 && UpdaterUtils.isCriticalUpdateTimerExpired(this.mUpgradeInfo);
    }

    public void setExtendRestartTime(long j) {
        this.settings.setLong(Configs.MAX_CRITICAL_UPDATE_EXTENDED_TIME, System.currentTimeMillis() + j);
    }

    public long getExtendRestartTime() {
        return this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_EXTENDED_TIME, -1L);
    }

    public boolean isOutsideCriticalUpdateExtendedTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long j = this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_EXTENDED_TIME, -1L);
        long j2 = j - currentTimeMillis;
        if (j > 0 && j2 <= 0) {
            UpdaterUtils.sendInstallModeStats("criticalUpdateExtendedAutoInstall");
        }
        return j2 <= 0 || j == -1;
    }

    public boolean shouldDisplayPopUp(Context context) {
        boolean z = context.getSharedPreferences(NotificationUtils.KEY_INSTALL, 0).getBoolean(UpdaterUtils.Checkbox_selected, false);
        long j = this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L) - System.currentTimeMillis();
        return j <= totalExtraPopUpTime() - this.mUpgradeInfo.getCriticalUpdateExtraWaitPeriodInMillis() && j > 0 && !z;
    }

    private boolean isInCriticalUpdateExtraPopUpTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long j = this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L);
        return j >= 0 && j - currentTimeMillis <= totalExtraPopUpTime();
    }

    private long totalExtraPopUpTime() {
        return this.mUpgradeInfo.getCriticalUpdateExtraWaitPeriodInMillis() * this.mUpgradeInfo.getCriticalUpdateExtraWaitCount();
    }

    public boolean isUpdateBotaCritical() {
        return UpdaterUtils.UPGRADE.contentEquals(this.mLocationType) && this.mSeverity == UpgradeUtils.SeverityType.CRITICAL.ordinal();
    }

    public boolean isUpdateFotaCritical() {
        return BuildPropReader.isATT() && UpdaterUtils.isCriticalUpdateTimerExpired(this.mUpgradeInfo);
    }

    public void updatePrompts(Context context) {
        long j = this.settings.getLong(Configs.NEXT_CRITICAL_UPDATE_PROMPT_TIME, -1L);
        long currentTimeMillis = System.currentTimeMillis();
        long j2 = this.settings.getLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, -1L);
        long j3 = this.mCriticalAnnoyValue + currentTimeMillis;
        this.settings.setLong(Configs.NEXT_CRITICAL_UPDATE_PROMPT_TIME, j3);
        this.settings.setLong(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME, System.currentTimeMillis() + ((j2 + (j3 - j)) - currentTimeMillis));
        Logger.debug("OtaApp", "new maxupdate prompt : " + getMaxUpdateCalendarString(context));
    }
}
