package com.motorola.ccc.ota.sources;

import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.Logger;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public final class BotaFotaResolver {
    private boolean _isBotaFirst = true;
    private boolean _isFotaEnabled = false;

    public BotaFotaResolver(String str) {
        parseCheckOrder(str);
    }

    public boolean isBotaFirst() {
        return this._isBotaFirst;
    }

    public boolean isFotaFirst() {
        return !this._isBotaFirst;
    }

    public boolean isFotaEnabled() {
        return this._isFotaEnabled;
    }

    private void parseCheckOrder(String str) {
        if (str == null || str.length() == 0) {
            return;
        }
        try {
            String[] split = str.split(",");
            if (split.length != 2) {
                return;
            }
            if (split[0].equalsIgnoreCase(UpdaterUtils.FOTA) || split[1].equalsIgnoreCase(UpdaterUtils.FOTA)) {
                this._isFotaEnabled = true;
            }
            if (split[0].equalsIgnoreCase(UpdaterUtils.FOTA)) {
                this._isBotaFirst = false;
            }
        } catch (Exception e) {
            Logger.error("OtaApp", "BotaFotaResolver.parse failed : for following check order (" + str + "); assuming only bota is enabled" + e);
        }
    }

    public UpgradeSourceType resolveCheckForUpdateRepository() {
        if (isBotaFirst()) {
            return UpgradeSourceType.upgrade;
        }
        if (isFotaEnabled()) {
            return UpgradeSourceType.fota;
        }
        return UpgradeSourceType.upgrade;
    }
}
