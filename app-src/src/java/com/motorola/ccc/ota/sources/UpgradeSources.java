package com.motorola.ccc.ota.sources;

import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.NewVersionHandler;
import com.motorola.ccc.ota.sources.bota.BotaUpgradeSource;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.fota.FotaUpgradeSource;
import com.motorola.ccc.ota.sources.modem.ModemUpgradeSource;
import com.motorola.ccc.ota.sources.sdcard.SDCardUpgradeSource;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public final class UpgradeSources {
    private final Map<UpgradeSourceType, UpgradeSource> sources;

    public void releasePlugins() {
    }

    public UpgradeSources(CusSM cusSM, NewVersionHandler newVersionHandler, ApplicationEnv applicationEnv, BotaSettings botaSettings) {
        HashMap hashMap = new HashMap();
        this.sources = hashMap;
        hashMap.put(UpgradeSourceType.sdcard, new SDCardUpgradeSource(cusSM, newVersionHandler, applicationEnv, botaSettings));
        BotaUpgradeSource botaUpgradeSource = new BotaUpgradeSource(cusSM, newVersionHandler, applicationEnv, botaSettings);
        hashMap.put(UpgradeSourceType.upgrade, botaUpgradeSource);
        hashMap.put(UpgradeSourceType.bootstrap, botaUpgradeSource);
        hashMap.put(UpgradeSourceType.fota, new FotaUpgradeSource(cusSM, newVersionHandler, applicationEnv, botaSettings));
        hashMap.put(UpgradeSourceType.modem, new ModemUpgradeSource(cusSM, newVersionHandler, applicationEnv, botaSettings));
    }

    public UpgradeSource getUpgradeSource(UpgradeSourceType upgradeSourceType) {
        return this.sources.get(upgradeSourceType);
    }

    public void initializePlugins(List<ApplicationEnv.Database.Descriptor> list) {
        for (UpgradeSource upgradeSource : this.sources.values()) {
            upgradeSource.plugin_init(list);
        }
    }
}
