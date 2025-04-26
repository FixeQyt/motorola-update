package com.motorola.ccc.ota.ui.updateType;

import android.content.Context;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.ui.updateType.UpdateType;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class SMR implements UpdateType.UpdateTypeInterface {
    private Context mContext = OtaApplication.getGlobalContext();

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getCriticalInstallMessagePopup() {
        return R.string.critical_install_message_popup_smr;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getDefaultInstructionImage() {
        return R.drawable.ic_caution_purple;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getDialogTheme() {
        return R.style.DialogThemeSMR;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getDownloadInstructionImage() {
        return R.drawable.ic_download_purple;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getDownloadNotificationImage() {
        return R.drawable.ic_img_notification_see_new_purple;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getInstallNotificationImage() {
        return R.drawable.ic_img_notification_install_purple;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getInstallToastMessage() {
        return R.string.selected_later_time_smr;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getLowStorageTitle() {
        return R.string.security_update_low_storage_title;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getPersonalInfoInstructionImage() {
        return R.drawable.ic_personal_info_purple;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getRestartInstructionImage() {
        return R.drawable.ic_restart_purple;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getRestartNotificationImage() {
        return R.drawable.ic_img_notification_restart_purple;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getSecureInstructionImage() {
        return R.drawable.ic_secure_purple;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getStatusBarColor() {
        return R.color.smr_status_bar;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getSystemUpdateAvailablePendingNotificationText() {
        return R.string.security_update_pending_notification_text;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getToolbarColor() {
        return R.color.smr_toolbar_bar;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getUpdateCompleteNotificationImage() {
        return R.drawable.ic_img_notification_complete_purple;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getUpdateFailedImage() {
        return R.drawable.ic_img_update_failed_purple;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public int getUpdateSpecificColor() {
        return R.color.smr_color;
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getToolbarTitle() {
        return this.mContext.getResources().getString(R.string.security_update_title);
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getSystemUpdateAvailableNotificationText() {
        return this.mContext.getResources().getString(R.string.security_update_notification_text);
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getDownloadNotificationTitle() {
        return this.mContext.getResources().getString(R.string.security_download_notification_title);
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getPDLNotificationTitle() {
        return this.mContext.getResources().getString(R.string.security_download_notification_title);
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getSystemUpdatePausedNotificationTitle() {
        return this.mContext.getResources().getString(R.string.security_update_paused);
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getDownloadProgressText() {
        return this.mContext.getResources().getString(R.string.progress_downloading_security);
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getInstallationTitle() {
        return this.mContext.getResources().getString(R.string.security_install_title);
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getInstallUpdateNotificationText() {
        return this.mContext.getResources().getString(R.string.security_install_notification_text);
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getABRestartWarning() {
        return this.mContext.getResources().getString(R.string.ab_restart_warning_smr);
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getPdlAnimation() {
        return "pdlAnimation_purple.json";
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getRestartAnimation() {
        return "restart_purple.json";
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getInstallAnimation() {
        return "just_one_more_step_purple.json";
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getUpdateStatusAnimation() {
        return "purple_complete.json";
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getDownloadProgressAnimation() {
        return "downloading_purple.json";
    }

    @Override // com.motorola.ccc.ota.ui.updateType.UpdateType.UpdateTypeInterface
    public String getDownloadPauseAnimation() {
        return "download_stop_purple.json";
    }
}
