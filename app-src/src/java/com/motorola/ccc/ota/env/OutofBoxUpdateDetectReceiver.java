package com.motorola.ccc.ota.env;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.NotificationUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class OutofBoxUpdateDetectReceiver extends BroadcastReceiver {
    private final BotaSettings settings = new BotaSettings();

    @Override // android.content.BroadcastReceiver
    public void onReceive(final Context context, final Intent intent) {
        boolean z = context.getPackageManager().getComponentEnabledSetting(new ComponentName(context, OutofBoxUpdateDetectReceiver.class)) == 2;
        boolean z2 = this.settings.getBoolean(Configs.INITIAL_SETUP_TRIGGERED);
        if (z || z2) {
            Logger.info("OtaApp", "OutofBoxUpdateDetectReceiver was already disabled/triggered.No need to run again for intent : " + intent.getAction() + " isDisabled: " + z + " isSetupTriggered: " + z2);
        } else {
            OtaApplication.getExecutorService().submit(new Runnable() { // from class: com.motorola.ccc.ota.env.OutofBoxUpdateDetectReceiver.1
                @Override // java.lang.Runnable
                public void run() {
                    OutofBoxUpdateDetectReceiver outofBoxUpdateDetectReceiver = OutofBoxUpdateDetectReceiver.this;
                    outofBoxUpdateDetectReceiver.handleIntent(context, intent, outofBoxUpdateDetectReceiver.settings);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleIntent(Context context, Intent intent, BotaSettings botaSettings) {
        Logger.debug("OtaApp", "OutofBoxUpdateDetectReceiver.handleIntent: " + intent.getAction());
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (CusAndroidUtils.INTENT_ACTION_SETUP_COMPLETED.equals(intent.getAction())) {
            if (botaSettings.getBoolean(Configs.FLAG_IS_VITAL_UPDATE)) {
                UpgradeUtilMethods.cancelOta("Cancelling vital update after setup complete, user skipped wifi during initial setup ", ErrorCodeMapper.KEY_CANCEL_VU_POST_SETUP_COMPLETE);
            }
            botaSettings.removeConfig(Configs.FLAG_IS_VITAL_UPDATE);
            if (!botaSettings.getBoolean(Configs.INITIAL_SETUP_COMPLETED) && BuildPropReader.isSoftbank() && BuildPropReader.isSoftbankSIM()) {
                NotificationUtils.displaySmartUpdateNotification(context);
            }
            botaSettings.setBoolean(Configs.INITIAL_SETUP_COMPLETED, true);
            botaSettings.resetBackoffRetryValuesAfterSetupCompleted(botaSettings);
            AndroidFotaInterface.sendSetupIntentToFota(context);
            if (!BuildPropReader.isFotaATT()) {
                UpdaterUtils.scheduleWorkManager(context);
            }
            if (!BuildPropReader.isATT() && !TextUtils.isEmpty(BuildPropReader.getMCFGConfigVersion())) {
                botaSettings.setLong(Configs.POLL_MODEM_AFTER, 604800000L);
                botaSettings.setInt(Configs.MAX_MODEM_POLLING_COUNT, 3);
                UpdaterUtils.scheduleModemWorkManager(context);
            }
            if (botaSettings.getBoolean(Configs.INITIAL_SETUP_TRIGGERED)) {
                return;
            }
            if (!NetworkUtils.hasNetwork(connectivityManager) || BuildPropReader.isCtaVersion(botaSettings) || !botaSettings.getBoolean(Configs.CHANNEL_ID_UPDATED)) {
                Logger.info("OtaApp", "OutofBoxUpdateDetectReceiver.handleIntent: setup completed but no data connection, waiting for connectivity orTOS acceptance by user or Channel Id need to be updated");
                return;
            }
            Logger.debug("OtaApp", "OutofBoxUpdateDetectReceiver.handleIntent: setup completed and data connection avaialble , trigger setup initiated");
            disableSelf(context);
            broadcastCheckforUpdateIntent(context);
            botaSettings.setBoolean(Configs.INITIAL_SETUP_TRIGGERED, true);
        } else if (CusAndroidUtils.INTENT_ACTION_TOS_COMPLETED.equals(intent.getAction())) {
            if (botaSettings.getBoolean(Configs.INITIAL_SETUP_TRIGGERED)) {
                return;
            }
            if (!NetworkUtils.hasNetwork(connectivityManager) || !botaSettings.getBoolean(Configs.INITIAL_SETUP_COMPLETED) || !botaSettings.getBoolean(Configs.CHANNEL_ID_UPDATED)) {
                Logger.info("OtaApp", "OutofBoxUpdateDetectReceiver.handleIntent: setup completed but no data connection, waiting for connectivity");
                return;
            }
            Logger.debug("OtaApp", "OutofBoxUpdateDetectReceiver.handleIntent: setup completed and data connection avaialble , trigger setup initiated");
            disableSelf(context);
            broadcastCheckforUpdateIntent(context);
            botaSettings.setBoolean(Configs.INITIAL_SETUP_TRIGGERED, true);
        } else if (!CusAndroidUtils.INTENT_ACTION_CHANNEL_ID_UPDATED.equals(intent.getAction()) || botaSettings.getBoolean(Configs.INITIAL_SETUP_TRIGGERED)) {
        } else {
            if (!NetworkUtils.hasNetwork(connectivityManager) || BuildPropReader.isCtaVersion(botaSettings) || !botaSettings.getBoolean(Configs.INITIAL_SETUP_COMPLETED)) {
                Logger.info("OtaApp", "OutofBoxUpdateDetectReceiver.handleIntent: channel id updated but no data connection, waiting for connectivity orTOS acceptance by user or initial setup to be completed");
                return;
            }
            Logger.debug("OtaApp", "OutofBoxUpdateDetectReceiver.handleIntent: setup completed and data connection avaialble , trigger setup initiated");
            disableSelf(context);
            broadcastCheckforUpdateIntent(context);
            botaSettings.setBoolean(Configs.INITIAL_SETUP_TRIGGERED, true);
        }
    }

    public void broadcastCheckforUpdateIntent(Context context) {
        Logger.info("OtaApp", "OutofBoxUpdateDetectReceiver, firing an intent to start setup (out of box) initiated update");
        Intent intent = new Intent(UpgradeUtilConstants.UPGRADE_CHECK_FOR_UPDATE);
        intent.putExtra(UpgradeUtilConstants.KEY_BOOTSTRAP, false);
        intent.putExtra(UpgradeUtilConstants.KEY_REQUESTID, 1);
        context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
    }

    public void broadcastUpgradePollingIntent(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.ACTION_UPGRADE_POLL));
    }

    public void broadcastModemUpgradePollingIntent(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.ACTION_MODEM_UPGRADE_POLL));
    }

    public void disableSelf(Context context) {
        Logger.debug("OtaApp", "OutofBoxUpdateDetectReceiver, disabling this receiver");
        try {
            OtaApplication.getGlobalContext().getPackageManager().setComponentEnabledSetting(new ComponentName(OtaApplication.getGlobalContext(), OutofBoxUpdateDetectReceiver.class), 2, 1);
        } catch (Exception e) {
            Logger.debug("OtaApp", "OutofBoxUpdateDetectReceiver, exception in  disabling this receiver " + e);
        }
    }
}
