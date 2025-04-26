package com.motorola.ccc.ota.env;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.CusUtilMethods;
import com.motorola.ccc.ota.utils.DmSendAlertService;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.utils.BroadcastUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class SystemUpdateQSTile extends TileService {
    private static final String ACTION_SYSTEM_UPDATE_SETTINGS = "motorola.settings.SYSTEM_UPDATE_SETTINGS";
    private Context context;
    private BroadcastReceiver mUIRefreshSimChangeReceiver = new BroadcastReceiver() { // from class: com.motorola.ccc.ota.env.SystemUpdateQSTile.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (UpgradeUtilConstants.REFRESH_CHKUPDATE_UI_ON_SIMCHANGE.equals(intent.getAction())) {
                Logger.debug("OtaApp", "SystemUpdateQSTile:mUIRefreshSimChangeReceiver:update system tile ui on liberty sim change");
                SystemUpdateQSTile.this.updateTileUi();
            }
        }
    };

    @Override // android.service.quicksettings.TileService, android.app.Service
    public void onDestroy() {
        super.onDestroy();
    }

    @Override // android.service.quicksettings.TileService
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override // android.service.quicksettings.TileService
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override // android.service.quicksettings.TileService
    public void onStartListening() {
        super.onStartListening();
        this.context = getApplicationContext();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpgradeUtilConstants.REFRESH_CHKUPDATE_UI_ON_SIMCHANGE);
        BroadcastUtils.registerLocalReceiver(this.context, this.mUIRefreshSimChangeReceiver, intentFilter);
        updateTileUi();
    }

    @Override // android.service.quicksettings.TileService
    public void onStopListening() {
        super.onStopListening();
        BroadcastUtils.unregisterLocalReceiver(this.context, this.mUIRefreshSimChangeReceiver);
    }

    @Override // android.service.quicksettings.TileService
    public void onClick() {
        super.onClick();
        launchOTA();
    }

    public void launchOTA() {
        try {
            Logger.debug("OtaApp", "Launching check update from Quick Settings");
            DmSendAlertService.startManualDMSync(this.context);
            Intent intent = new Intent(ACTION_SYSTEM_UPDATE_SETTINGS);
            intent.setFlags(268435456);
            new BotaSettings().setString(Configs.USER_TRIGGER_LAUNCH_POINT, "quickTileSettings");
            startActivityAndCollapse(PendingIntent.getActivity(this, 0, intent, 67108864));
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception in SystemUpdateQSTile: launchOTA: " + e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateTileUi() {
        if (!UpgradeUtilMethods.isSystemUser(this) || CusUtilMethods.isCheckUpdateDisabled(this)) {
            Tile qsTile = getQsTile();
            if (qsTile == null) {
                return;
            }
            qsTile.setState(0);
            qsTile.updateTile();
            return;
        }
        Tile qsTile2 = getQsTile();
        if (qsTile2 == null) {
            return;
        }
        qsTile2.setIcon(Icon.createWithResource(this, (int) R.drawable.ic_ota_title_icon_32dp));
        qsTile2.setLabel(getApplicationContext().getString(R.string.system_update));
        qsTile2.setState(2);
        qsTile2.updateTile();
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
    }
}
