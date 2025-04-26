package com.motorola.ccc.ota.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.SystemClock;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import java.io.File;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpgraderReceiver extends BroadcastReceiver {
    private static String FILE_LOCATION = "com.motorola.blur.service.blur.upgrade.file_location";
    private static String FLAVOUR = "com.motorola.blur.service.blur.upgrade.result_flavour";
    private static String RESULT_INTENT = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATE_RESULT";
    private static String VERSION = "com.motorola.blur.service.blur.upgrade.version";
    private static boolean inProgress;
    BotaSettings botaSettings;

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        UpdaterUtils.UpgradeInfo upgradeInfoDuringOTAUpdate;
        String str;
        this.botaSettings = new BotaSettings();
        while (inProgress) {
            SystemClock.sleep(1000L);
        }
        inProgress = true;
        if (UpgradeUtilConstants.MERGE_RESTART_UPGRADE.equals(intent.getAction())) {
            Logger.info("OtaApp", "Restarting device for merge process");
            String string = this.botaSettings.getString(Configs.STATS_VAB_MERGE_RESTARTED_BY);
            this.botaSettings.setString(Configs.STATS_VAB_MERGE_RESTARTED_BY, ((string == null || string.equalsIgnoreCase("null")) ? "" : "") + ",User");
            rebootDevice(context);
            return;
        }
        String stringExtra = intent.getStringExtra(FILE_LOCATION);
        String stringExtra2 = intent.getStringExtra(VERSION);
        Logger.info("OtaApp", "!!! INSTALL UPDATE !!!: for version : " + stringExtra2);
        this.botaSettings.setString(Configs.HISTORY_SOURCE_VERSION, BuildPropReader.getBuildDescription());
        UpdaterUtils.updateMotorolaSettingsProvider(context, UpdaterUtils.OEM_CONFIG_UPDATE_FLAG, String.valueOf(UpdaterUtils.getUpgradeInfoDuringOTAUpdate(intent).getOemConfigUpdateData()));
        Logger.debug("OtaApp", "OEMConfigUpdate Flag = " + upgradeInfoDuringOTAUpdate.getOemConfigUpdateData());
        String installModeStats = UpdaterUtils.getInstallModeStats();
        if ("userInitiated".equals(installModeStats) || "userSelectedAutoInstall".equals(installModeStats)) {
            UpdaterUtils.addNewInstallationTime(System.currentTimeMillis());
        }
        this.botaSettings.setLong(Configs.BOOT_START_TIMESTAMP, System.currentTimeMillis());
        if (BuildPropReader.isUEUpdateEnabled()) {
            this.botaSettings.incrementPrefs(Configs.UPGRADE_ATTEMPT_COUNT);
            Logger.info("OtaApp", "Restarting the device");
            rebootDevice(context);
            return;
        }
        this.botaSettings.setLong(Configs.STATS_INSTALL_STARTED, System.currentTimeMillis());
        if (this.botaSettings.getBoolean(Configs.IS_UPDATE_PENDING_ON_REBOOT)) {
            UpdaterUtils.notifyRecoveryAboutPendingUpdate(false);
        }
        if (stringExtra != null && stringExtra2 != null) {
            File file = new File(stringExtra);
            if (file.exists()) {
                try {
                    this.botaSettings.incrementPrefs(Configs.UPGRADE_ATTEMPT_COUNT);
                    RecoverySystem.installPackage(context, file);
                } catch (Exception e) {
                    str = e.toString();
                    Logger.error("OtaApp", "!!Ooops...Unable to invoke RecoverySystem.installPackage!!" + e);
                }
            }
        }
        str = "Unknown reason";
        inProgress = false;
        Intent intent2 = new Intent(RESULT_INTENT);
        if (stringExtra2 == null) {
            str = "Version is not supplied! " + stringExtra2;
        } else {
            intent2.putExtra(VERSION, stringExtra2);
        }
        if (stringExtra == null) {
            str = "Update filename is not supplied! " + stringExtra;
        }
        Logger.info("InstallUpdate", str);
        intent2.putExtra(FLAVOUR, str);
        BroadcastUtils.sendLocalBroadcast(context, intent2);
    }

    private void rebootDevice(final Context context) {
        new Thread(new Runnable() { // from class: com.motorola.ccc.ota.ui.UpgraderReceiver.1
            @Override // java.lang.Runnable
            public void run() {
                boolean isNetWorkConnected = NetworkUtils.isNetWorkConnected((ConnectivityManager) context.getSystemService("connectivity"));
                boolean isPreparedForUnattendedUpdate = UpdaterUtils.isPreparedForUnattendedUpdate();
                if (!isPreparedForUnattendedUpdate || !isNetWorkConnected) {
                    Logger.info("OtaApp", "Device not prepared for ROR, proceed with normal reboot");
                    if (!isPreparedForUnattendedUpdate) {
                        UpgraderReceiver.this.botaSettings.setString(Configs.STATS_REBOOT_MODE, "notPreparedForROR");
                    } else {
                        UpgraderReceiver.this.botaSettings.setString(Configs.STATS_REBOOT_MODE, "preparedForRORNoNetwork");
                    }
                    ((PowerManager) OtaApplication.getGlobalContext().getSystemService("power")).reboot(null);
                    return;
                }
                Logger.debug("OtaApp", "Resume on reboot");
                UpgraderReceiver.this.botaSettings.setString(Configs.STATS_REBOOT_MODE, "resumeOnReboot");
                UpdaterUtils.rebootAndApply();
            }
        }).start();
    }
}
