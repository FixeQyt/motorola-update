package com.motorola.ccc.ota.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.Formatter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.AndroidFotaInterface;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.fota.FotaConstants;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.WarningAlertDialog;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.text.NumberFormat;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class MessageActivity extends AppCompatActivity implements UpdaterUtils.OnDialogInteractionListener {
    private static final String BOTA_FAILURE_DIALOG_CLEARED_ACTION = "com.motorola.blur.updater.FAILURE_DIALOG_CLEARED";
    private static final int DEFAULT_SPACE_REQUIRED = 50;
    private static final int DIALOG_ID = 1;
    private static final int MEGABYTE = 1048576;
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    private String error_message;
    private String error_title;
    private Intent intent;
    private boolean isTransactionSafe;
    private boolean mMemoryLow;
    private UpgradeUtils.DownloadStatus status;
    private BroadcastReceiver mCloseSystemDialogsIntentReceiver = null;
    private BotaSettings settings = new BotaSettings();
    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.MessageActivity.2
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (UpgradeUtilConstants.FINISH_MESSAGE_ACTIVITY.equals(intent.getAction())) {
                MessageActivity.this.finish();
            }
        }
    };

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        this.intent = intent;
        this.isTransactionSafe = true;
        this.status = UpgradeUtilMethods.downloadStatusFromIntent(intent);
        Logger.debug("OtaApp", "MessageActivity:onCreate:status:" + this.status);
        this.error_title = getResources().getString(R.string.error_title);
        switch (AnonymousClass3.$SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[this.status.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
                this.error_title = getResources().getString(R.string.storage_low);
                this.mMemoryLow = true;
                UpdaterUtils.UpgradeInfo upgradeInfoAfterOTAUpdate = UpdaterUtils.getUpgradeInfoAfterOTAUpdate(this.intent);
                handleLowMemory();
                this.error_message = getLowSpaceMessage(upgradeInfoAfterOTAUpdate, this.status);
                break;
            case 5:
                handleLowMemory();
                this.error_message = getLowSpaceMessage(this.status);
                break;
            case 6:
                this.error_title = getString(R.string.dialog_alert_title);
                this.error_message = getString(R.string.low_battery_install, new Object[]{NumberFormat.getPercentInstance().format(UpdaterUtils.getminBatteryRequiredForInstall(UpdaterUtils.getUpgradeInfoDuringOTAUpdate(getIntent()).getminBatteryRequiredForInstall()) / 100.0f)});
                UpdaterUtils.enableReceiversForBatteryLow();
                break;
            case 7:
            case 8:
            case 9:
                this.error_message = getString(R.string.error_resources);
                break;
            case 10:
                this.error_message = getString(R.string.error_reboot);
                break;
            case 11:
                this.error_message = getString(R.string.error_disabled);
                break;
            case 12:
                this.error_message = getString(R.string.error_network);
                break;
            case 13:
                this.error_message = getString(R.string.error_verify);
                break;
            case 14:
                this.error_message = String.format(getResources().getString(R.string.error_fail, BuildPropReader.getModel()), new Object[0]);
                break;
            case 15:
                this.error_title = getResources().getString(R.string.error_install_title);
                this.error_message = String.format(getResources().getString(R.string.error_fail, BuildPropReader.getModel()), new Object[0]);
                break;
            case 16:
                if ("sdcard".equals(UpgradeUtilMethods.locationTypeFromIntent(this.intent))) {
                    this.error_message = getString(R.string.error_mismatch_sd);
                    break;
                } else {
                    this.error_message = getString(R.string.error_mismatch);
                    break;
                }
            case 17:
                this.error_message = getString(R.string.error_copyfail);
                break;
            case 18:
                this.error_message = getString(R.string.error_server);
                break;
            case 19:
                this.error_title = getResources().getString(R.string.error_sdcard_resources_title);
                this.error_message = getString(R.string.error_sdcard_resources_nosdcard);
                break;
            case 20:
                this.error_title = getResources().getString(R.string.error_sdcard_resources_title);
                this.error_message = getString(R.string.error_sdcard_resources_notmounted);
                break;
            case 21:
                this.error_message = getString(R.string.error_sdcard_resources_warning);
                break;
            case 22:
                this.error_title = getResources().getString(R.string.error_sdcard_resources_title);
                this.error_message = getLowSpaceMessage(UpdaterUtils.getUpgradeInfoAfterOTAUpdate(this.intent), this.status);
                break;
            case 23:
                this.error_title = getResources().getString(R.string.error_sdcard_resources_title);
                this.error_message = getString(R.string.error_sdcard_resources_fail_removal);
                break;
            case 24:
                this.error_title = getResources().getString(R.string.error_sdcard_resources_title);
                this.error_message = getString(R.string.error_sdcard_resources_fail_space);
                break;
            case 25:
                this.error_title = getResources().getString(R.string.install_cancelled_title);
                this.error_message = getString(R.string.install_progress_cancel_message);
                break;
            case 26:
                this.error_title = getResources().getString(R.string.download_cancelled_title);
                this.error_message = getString(R.string.download_progress_cancel_message);
                break;
            case 27:
                this.error_title = getResources().getString(R.string.compatibility_failed_title);
                this.error_message = String.format(getResources().getString(R.string.compatibility_verification_failure), new Object[0]);
                if (BuildPropertyUtils.isDogfoodDevice()) {
                    this.error_message += getResources().getString(R.string.compatibility_verification_failure_df);
                    break;
                }
                break;
            case 28:
            case 29:
                this.error_title = "";
                this.error_message = getResources().getString(R.string.bg_install_cancel_confirm);
                break;
            case 30:
                this.error_title = "";
                this.error_message = getResources().getString(R.string.auto_download_enable_cellular_future_updates);
                break;
            default:
                throw new IllegalStateException();
        }
        showErrorMessage();
    }

    /* renamed from: com.motorola.ccc.ota.ui.MessageActivity$3  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus;

        static {
            int[] iArr = new int[UpgradeUtils.DownloadStatus.values().length];
            $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus = iArr;
            try {
                iArr[UpgradeUtils.DownloadStatus.STATUS_SPACE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SPACE_INSTALL.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SPACE_BACKGROUND_INSTALL.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SPACE_PAYLOAD_METADATA_CHECK.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_VAB_MAKE_SPACE_REQUEST_USER.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.LOW_BATTERY_INSTALL.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SPACE_INSTALL_CACHE.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_RESOURCES.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_RESOURCES_WIFI.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_RESOURCES_REBOOT.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_DISABLED.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_NETWORK.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_VERIFY.ordinal()] = 13;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_FAIL.ordinal()] = 14;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_INSTALL_FAIL.ordinal()] = 15;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_MISMATCH.ordinal()] = 16;
            } catch (NoSuchFieldError unused16) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_COPYFAIL.ordinal()] = 17;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SERVER.ordinal()] = 18;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_NOSDCARD.ordinal()] = 19;
            } catch (NoSuchFieldError unused19) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_NOTMOUNTED.ordinal()] = 20;
            } catch (NoSuchFieldError unused20) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_WARNING.ordinal()] = 21;
            } catch (NoSuchFieldError unused21) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_SPACE.ordinal()] = 22;
            } catch (NoSuchFieldError unused22) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_FAIL_REMOVAL.ordinal()] = 23;
            } catch (NoSuchFieldError unused23) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_FAIL_SPACE.ordinal()] = 24;
            } catch (NoSuchFieldError unused24) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL.ordinal()] = 25;
            } catch (NoSuchFieldError unused25) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_CANCEL.ordinal()] = 26;
            } catch (NoSuchFieldError unused26) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_FAIL_PAYLOAD_METADATA_VERIFY.ordinal()] = 27;
            } catch (NoSuchFieldError unused27) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL_NOTIFICATION.ordinal()] = 28;
            } catch (NoSuchFieldError unused28) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.STATUS_DOWNLOAD_CANCEL_NOTIFICATION.ordinal()] = 29;
            } catch (NoSuchFieldError unused29) {
            }
            try {
                $SwitchMap$com$motorola$otalib$common$utils$UpgradeUtils$DownloadStatus[UpgradeUtils.DownloadStatus.FOTA_SHOW_ALERT_CELLULAR_POPUP.ordinal()] = 30;
            } catch (NoSuchFieldError unused30) {
            }
        }
    }

    protected void onResume() {
        super.onResume();
        this.isTransactionSafe = true;
    }

    protected void onPause() {
        super.onPause();
        this.isTransactionSafe = false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private String getLowSpaceMessage(UpdaterUtils.UpgradeInfo upgradeInfo, UpgradeUtils.DownloadStatus downloadStatus) {
        Resources resources = getResources();
        long extraSize = upgradeInfo.getExtraSize();
        long size = upgradeInfo.getSize();
        long j = 2097152;
        if (downloadStatus == UpgradeUtils.DownloadStatus.STATUS_SPACE) {
            long availableDataPartitionSize = getAvailableDataPartitionSize();
            long size2 = upgradeInfo.getSize();
            long availableReserveSpace = FileUtils.getAvailableReserveSpace();
            long j2 = extraSize <= 0 ? size2 + 52428800 : extraSize + size2;
            long abs = Math.abs(j2 - (availableDataPartitionSize + availableReserveSpace));
            if (abs == 0) {
                Logger.info("OtaApp", "MessageActivity.getLowSpaceMessage, requiredsize matches with availableSize, show it as 2");
            } else {
                j = abs;
            }
            if (isOtaSessionDone()) {
                return resources.getString(R.string.error_space, Formatter.formatFileSize(this, j2), Formatter.formatFileSize(this, j));
            }
            return resources.getString(R.string.error_space_retry, Formatter.formatFileSize(this, j2), Formatter.formatFileSize(this, j));
        } else if (downloadStatus == UpgradeUtils.DownloadStatus.STATUS_SPACE_BACKGROUND_INSTALL) {
            long availableDataPartitionSize2 = getAvailableDataPartitionSize();
            long chunkSize = upgradeInfo.getChunkSize() > 0 ? upgradeInfo.getChunkSize() : 0L;
            return resources.getString(R.string.error_space_retry, Formatter.formatFileSize(this, chunkSize), Formatter.formatFileSize(this, Math.abs(chunkSize - availableDataPartitionSize2) + 1));
        } else if (downloadStatus == UpgradeUtils.DownloadStatus.STATUS_SPACE_PAYLOAD_METADATA_CHECK) {
            long availableDataPartitionSize3 = getAvailableDataPartitionSize();
            long compatibilityFileSize = upgradeInfo.getCompatibilityFileSize() <= 0 ? 1048576L : upgradeInfo.getCompatibilityFileSize();
            return getResources().getString(R.string.error_space_retry, Formatter.formatFileSize(this, compatibilityFileSize >= 1048576 ? compatibilityFileSize : 1048576L), Formatter.formatFileSize(this, Math.abs(compatibilityFileSize - availableDataPartitionSize3) + 1));
        } else if (downloadStatus == UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_SPACE) {
            return resources.getString(R.string.error_sdcard_resources_space, Formatter.formatFileSize(this, size));
        } else {
            if (downloadStatus == UpgradeUtils.DownloadStatus.STATUS_SPACE_INSTALL) {
                long availableDataPartitionSize4 = getAvailableDataPartitionSize();
                long availableReserveSpace2 = FileUtils.getAvailableReserveSpace();
                if (extraSize <= 0) {
                    extraSize = 52428800;
                }
                long abs2 = Math.abs(extraSize - (availableDataPartitionSize4 + availableReserveSpace2));
                if (abs2 == 0) {
                    Logger.info("OtaApp", "MessageActivity.getLowSpaceMessage, requiredsize matches with availableSize, show it as 2");
                } else {
                    j = abs2;
                }
                if (isOtaSessionDone()) {
                    resources.getString(R.string.error_space, Formatter.formatFileSize(this, extraSize), Formatter.formatFileSize(this, j));
                } else {
                    return resources.getString(R.string.error_space_retry, Formatter.formatFileSize(this, extraSize), Formatter.formatFileSize(this, j));
                }
            }
            return "";
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private String getLowSpaceMessage(UpgradeUtils.DownloadStatus downloadStatus) {
        long availableDataPartitionSize = getAvailableDataPartitionSize();
        Resources resources = getResources();
        if (downloadStatus != UpgradeUtils.DownloadStatus.STATUS_VAB_MAKE_SPACE_REQUEST_USER) {
            return "";
        }
        if (BuildPropReader.isATT()) {
            MetaData from = MetaDataBuilder.from(this.intent.getStringExtra(UpgradeUtilConstants.KEY_METADATA));
            long userdataSpaceRequired = UpdaterUtils.getUserdataSpaceRequired(from);
            if (!BuildPropReader.isStreamingUpdate()) {
                availableDataPartitionSize -= from.getSize();
            }
            return resources.getString(R.string.error_space, Formatter.formatFileSize(this, userdataSpaceRequired), Formatter.formatFileSize(this, userdataSpaceRequired - availableDataPartitionSize));
        }
        return resources.getString(R.string.error_space_vab_make_room_popup, Formatter.formatFileSize(this, UpgradeUtilMethods.getFreeSpaceFromIntent(this.intent) - availableDataPartitionSize));
    }

    private boolean isOtaSessionDone() {
        String string = this.settings.getString(Configs.TRACKINGID);
        Logger.debug("OtaApp", "isOtaSessionDone, trackingId: " + string);
        if (TextUtils.isEmpty(string)) {
            return true;
        }
        if (UpdaterUtils.isDeviceAtInstallPhase(getIntent())) {
            if (BuildPropReader.isATT()) {
                this.settings.setBoolean(Configs.CHECKBOX_SELECTED, true);
            } else {
                this.settings.setBoolean(Configs.CHECKBOX_SELECTED, false);
            }
        }
        return false;
    }

    protected void onStop() {
        sendBroadcast(new Intent(BOTA_FAILURE_DIALOG_CLEARED_ACTION), Permissions.INTERACT_OTA_SERVICE);
        super.onStop();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void onDestroy() {
        super.onDestroy();
        if (this.mMemoryLow) {
            if (BuildPropReader.isATT()) {
                NotificationUtils.showLowMemoryNotification(this.error_message);
            }
            if (isOtaSessionDone()) {
                this.settings.removeConfig(Configs.DATA_SPACE_RETRY_COUNT);
                NotificationUtils.stopNotificationService(this);
            } else {
                this.settings.incrementPrefs(Configs.DATA_SPACE_RETRY_COUNT);
                NotificationUtils.showOtaLowMemoryNotification(this, getIntent());
            }
        }
        this.mMemoryLow = false;
        BroadcastReceiver broadcastReceiver = this.mCloseSystemDialogsIntentReceiver;
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        BroadcastUtils.unregisterLocalReceiver(this, this.mFinishReceiver);
    }

    private void showErrorMessage() {
        WarningAlertDialog.WarningAlertDialogBuilder warningAlertDialogBuilder = new WarningAlertDialog.WarningAlertDialogBuilder(1);
        warningAlertDialogBuilder.setMessage(this.error_message);
        warningAlertDialogBuilder.setTitle(this.error_title);
        getUpdateTypeInterfaceAfterOTAUpdate();
        if (this.status == UpgradeUtils.DownloadStatus.STATUS_DOWNLOAD_CANCEL_NOTIFICATION || this.status == UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL_NOTIFICATION) {
            getUpdateTypeInterfaceDuringOTAUpdate();
            warningAlertDialogBuilder.setPositiveText(getResources().getString(R.string.alert_dialog_continue));
            warningAlertDialogBuilder.setNegativeText(getResources().getString(R.string.alert_dialog_cancel));
        } else if (this.status == UpgradeUtils.DownloadStatus.FOTA_SHOW_ALERT_CELLULAR_POPUP) {
            warningAlertDialogBuilder.setPositiveText(getResources().getString(R.string.alert_dialog_yes));
            warningAlertDialogBuilder.setNegativeText(getResources().getString(R.string.alert_dialog_no));
        } else if (this.mMemoryLow) {
            if (this.status == UpgradeUtils.DownloadStatus.STATUS_VAB_MAKE_SPACE_REQUEST_USER) {
                warningAlertDialogBuilder.setPositiveText(getString(R.string.clear_space));
                getUpdateTypeInterfaceDuringOTAUpdate();
            } else {
                warningAlertDialogBuilder.setPositiveText(getString(R.string.make_space));
            }
            warningAlertDialogBuilder.setNegativeText(getString(isOtaSessionDone() ? R.string.download_no_button : R.string.not_now));
        } else {
            warningAlertDialogBuilder.setPositiveText(getString(R.string.alert_dialog_ok));
        }
        warningAlertDialogBuilder.setButtonColor(R.color.black);
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        WarningAlertDialog buildDialog = warningAlertDialogBuilder.buildDialog();
        if (this.isTransactionSafe) {
            buildDialog.show(supportFragmentManager, "alert");
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void handleLowMemory() {
        this.error_title = getResources().getString(R.string.storage_low);
        this.mMemoryLow = true;
        this.mCloseSystemDialogsIntentReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.ui.MessageActivity.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String stringExtra = intent.getStringExtra(MessageActivity.SYSTEM_DIALOG_REASON_KEY);
                if (stringExtra != null) {
                    if (MessageActivity.SYSTEM_DIALOG_REASON_HOME_KEY.equals(stringExtra) || MessageActivity.SYSTEM_DIALOG_REASON_RECENT_APPS.equals(stringExtra)) {
                        Logger.debug("OtaApp", "MessageActivity.onCreate, home/recent app key pressed.");
                        MessageActivity.this.finish();
                    }
                }
            }
        };
        registerReceiver(this.mCloseSystemDialogsIntentReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"), 2);
        BroadcastUtils.registerLocalReceiver(this, this.mFinishReceiver, new IntentFilter(UpgradeUtilConstants.FINISH_MESSAGE_ACTIVITY));
    }

    private UpdateType.UpdateTypeInterface getUpdateTypeInterfaceAfterOTAUpdate() {
        UpdaterUtils.UpgradeInfo upgradeInfoAfterOTAUpdate = UpdaterUtils.getUpgradeInfoAfterOTAUpdate(getIntent());
        if (upgradeInfoAfterOTAUpdate != null) {
            return UpdateType.getUpdateType(upgradeInfoAfterOTAUpdate.getUpdateTypeData());
        }
        return UpdateType.getUpdateType(UpdateType.DIFFUpdateType.DEFAULT.toString());
    }

    private UpdateType.UpdateTypeInterface getUpdateTypeInterfaceDuringOTAUpdate() {
        UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate = UpdaterUtils.getUpgradeInfoDuringOTAUpdate(getIntent());
        if (upgradeInfoDuringOTAUpdate != null) {
            return UpdateType.getUpdateType(upgradeInfoDuringOTAUpdate.getUpdateTypeData());
        }
        return UpdateType.getUpdateType(UpdateType.DIFFUpdateType.DEFAULT.toString());
    }

    private boolean isSpaceAvailable(String str, long j) {
        StatFs statFs = new StatFs(str);
        return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong() >= j;
    }

    public static long getAvailableDataPartitionSize() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().toString());
        return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.motorola.ccc.ota.ui.UpdaterUtils.OnDialogInteractionListener
    public void onPositiveClick(int i, JSONObject jSONObject) {
        if (this.mMemoryLow) {
            startActivity(new Intent("android.settings.INTERNAL_STORAGE_SETTINGS"));
            finish();
        } else {
            if (this.status == UpgradeUtils.DownloadStatus.STATUS_MISMATCH) {
                UpgradeUtilMethods.sendCheckForUpdate(this, false, 10);
            } else if (this.status == UpgradeUtils.DownloadStatus.LOW_BATTERY_INSTALL) {
                this.settings.setBoolean(Configs.BATTERY_LOW, true);
                UpdaterUtils.checkAndEnableBatteryStatusReceiver();
                UpdaterUtils.checkAndEnablePowerDownReceiver();
            } else if (this.status == UpgradeUtils.DownloadStatus.FOTA_SHOW_ALERT_CELLULAR_POPUP) {
                this.settings.setString(Configs.USER_AUTO_DOWNLOAD_OPTION, FotaConstants.AutoDownloadOption.OTAorWiFi.toString());
                if (BuildPropReader.isFotaATT()) {
                    AndroidFotaInterface.sendAutoDownloadSettingsToFota(this, FotaConstants.AutoDownloadOption.OTAorWiFi);
                }
            }
            finish();
        }
        UpdaterUtils.stopDownloadProgressActivity(this);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.motorola.ccc.ota.ui.UpdaterUtils.OnDialogInteractionListener
    public void onNegativeClick(int i) {
        if (UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL_NOTIFICATION.equals(this.status)) {
            UpgradeUtilMethods.sendUserResponseDuringBackgroundInstallation(this, UpgradeUtils.DownloadStatus.STATUS_INSTALL_CANCEL);
        } else if (UpgradeUtils.DownloadStatus.STATUS_DOWNLOAD_CANCEL_NOTIFICATION.equals(this.status)) {
            UpgradeUtilMethods.sendDownloadNotificationResponse(this, UpgradeUtils.DownloadStatus.STATUS_CANCEL);
        }
        finish();
        UpdaterUtils.stopDownloadProgressActivity(this);
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.motorola.ccc.ota.ui.UpdaterUtils.OnDialogInteractionListener
    public void onDismiss(int i, boolean z) {
        if (z) {
            finish();
        }
        UpdaterUtils.stopDownloadProgressActivity(this);
    }
}
