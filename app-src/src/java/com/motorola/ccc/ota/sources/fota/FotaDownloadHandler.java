package com.motorola.ccc.ota.sources.fota;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.DownloadHelper;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.MetadataOverrider;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.NetworkUtils;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class FotaDownloadHandler implements DownloadHandler {
    private static final String KEY_WIFI_PROMPT_COUNT = "com.motorola.ccc.ota.wifi_prompt_count";
    private static final int KEY_WIFI_PROMPT_DEFER_COUNT = 8;
    private Context c;
    private final ConnectivityManager cm;
    private final ApplicationEnv env;
    private SharedPreferences.Editor mEditor;
    private SharedPreferences pref;
    private AtomicBoolean progress;
    private final BotaSettings settings;
    private final CusSM sm;

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void radioGotDown() {
    }

    public FotaDownloadHandler(ApplicationEnv applicationEnv, BotaSettings botaSettings, CusSM cusSM) {
        this.env = applicationEnv;
        Context globalContext = OtaApplication.getGlobalContext();
        this.c = globalContext;
        this.cm = (ConnectivityManager) globalContext.getSystemService("connectivity");
        this.settings = botaSettings;
        this.sm = cusSM;
        this.progress = new AtomicBoolean(false);
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void transferUpgrade(ApplicationEnv.Database.Descriptor descriptor) {
        this.progress = new AtomicBoolean(new DownloadHelper(descriptor.getVersion(), FileUtils.getLocalPath(this.settings), descriptor.getMeta().getSize()).size() > 0);
        downloadDirectly(descriptor);
    }

    private void downloadDirectly(ApplicationEnv.Database.Descriptor descriptor) {
        if (descriptor.getRepository().equals(UpgradeSourceType.fota.toString())) {
            try {
                long parseLong = Long.parseLong(descriptor.getInfo());
                if (this.settings.getString(Configs.WIFI_DISCOVERY_EXPIRED_FOR_FOTA).equals("yes")) {
                    this.env.getUtilities().cancelDownloadNotification();
                    this.env.getFotaServices().sendWifiDiscoverTimerExpiry(parseLong, 1);
                    return;
                }
                MetaData meta = descriptor.getMeta();
                if (UpdaterUtils.isBatteryLowToStartDownload(this.c)) {
                    Logger.debug("OtaApp", "FotaDownloadHandler.sending suspended intent to UI due to low battery");
                    this.settings.setBoolean(Configs.BATTERY_LOW, true);
                    if (!meta.showDownloadProgress()) {
                        this.sm.overWriteMetaData(MetadataOverrider.returnOverWrittenMetaData(meta, "showDownloadProgress", true));
                    }
                    this.env.getUtilities().sendUpdateDownloadStatusSuspended(descriptor.getVersion(), new File(FileUtils.getLocalPath(this.settings)).length(), descriptor.getMeta().getSize(), descriptor.getRepository(), UpdaterUtils.isWifiOnly());
                } else if (this.progress.get()) {
                    sendDownloadStartIntent(parseLong);
                } else if (!meta.isWifiOnly()) {
                    sendDownloadStartIntent(parseLong);
                } else if (NetworkUtils.isWifi(this.cm)) {
                    sendDownloadStartIntent(parseLong);
                    this.settings.setInt(Configs.WIFI_PROMPT_COUNT_FOR_FOTA, 0);
                    this.env.getUtilities().cancelDownloadNotification();
                } else if (this.settings.getInt(Configs.WIFI_PROMPT_COUNT_FOR_FOTA, 0) < 8) {
                    if (!meta.showDownloadProgress()) {
                        this.sm.overWriteMetaData(MetadataOverrider.returnOverWrittenMetaData(meta, "showDownloadProgress", true));
                    }
                    Logger.debug("OtaApp", "In FotaDownloadHandler.transferupgrade,sending suspended intent to UI");
                    this.env.getUtilities().sendUpdateDownloadStatusSuspended(descriptor.getVersion(), new File(FileUtils.getLocalPath(this.settings)).length(), descriptor.getMeta().getSize(), descriptor.getRepository(), UpdaterUtils.isWifiOnly());
                } else {
                    this.env.getFotaServices().sendWifiDiscoverTimerExpiry(parseLong, 1);
                    this.settings.setInt(Configs.WIFI_PROMPT_COUNT_FOR_FOTA, 0);
                }
            } catch (Exception e) {
                Logger.error("OtaApp", "FotaDownlaodHandler.downlaodDirectly failed: could not parse fota request id" + e);
            }
        }
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public boolean isBusy() {
        return this.progress.get();
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void close() {
        this.progress.set(false);
    }

    private void sendDownloadStartIntent(long j) {
        this.env.getFotaServices().sendUpdateAvailableResponse(j, 0, MetaDataBuilder.from(this.settings.getString(Configs.METADATA)).showDownloadProgress());
        this.progress.set(true);
    }
}
