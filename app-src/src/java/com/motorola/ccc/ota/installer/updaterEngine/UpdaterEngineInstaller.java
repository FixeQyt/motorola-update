package com.motorola.ccc.ota.installer.updaterEngine;

import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.installer.InstallTypeResolver;
import com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineStateHandler;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.metaData.CheckForUpgradeTriggeredBy;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public abstract class UpdaterEngineInstaller implements InstallTypeResolver.Installer {
    protected static UpdaterEngineStateHandler updaterEngineStateHandler;
    protected ApplicationEnv.Database _db;
    protected ApplicationEnv _env;
    protected BotaSettings _settings;
    protected String mVersion;
    private static AtomicBoolean APPLY_PAYLOAD_STARTED = new AtomicBoolean();
    private static AtomicBoolean ALLOCATE_SPACE_STARTED = new AtomicBoolean();

    public void clearUEInstallerBeforeExit() {
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public boolean doesDownloadedFileClearedFromDisk() {
        return false;
    }

    public static UpdaterEngineInstaller builder(String str, ApplicationEnv applicationEnv, BotaSettings botaSettings, ApplicationEnv.Database database, ApplicationEnv.Database.Descriptor descriptor, CusSM cusSM) {
        if (UpgradeUtils.AB_INSTALL_TYPE.defaultAb.name().contentEquals(str)) {
            return new ABUpdate(applicationEnv, botaSettings, database, descriptor);
        }
        return new StreamingUpdate(applicationEnv, botaSettings, database);
    }

    public static boolean getApplyPayloadStarted() {
        return APPLY_PAYLOAD_STARTED.get();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static void setApplyPayloadStarted(boolean z) {
        APPLY_PAYLOAD_STARTED.set(z);
    }

    public static boolean getAllocateSpaceStarted() {
        return ALLOCATE_SPACE_STARTED.get();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public static void setAllocateSpaceStarted(boolean z) {
        ALLOCATE_SPACE_STARTED.set(z);
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public boolean shouldPromptUpgradeNotification(ApplicationEnv.Database.Descriptor descriptor) {
        if (CheckForUpgradeTriggeredBy.user.name().equals(this._settings.getString(Configs.TRIGGERED_BY)) || CheckForUpgradeTriggeredBy.user.name().equalsIgnoreCase(descriptor.getMeta().getUpdateReqTriggeredBy())) {
            UpdaterUtils.setFullScreenStartPoint(Configs.STATS_RESTART_START_POINT, "userDidCheckUpdate");
            return false;
        } else if (SmartUpdateUtils.isSmartUpdateNearestToInstall(this._settings, descriptor.getMeta().getUpdateTypeData())) {
            this._settings.setLong(Configs.ACTIVITY_NEXT_PROMPT, this._settings.getLong(Configs.SMART_UPDATE_MIN_INSTALL_TIME, -1L));
            return false;
        } else if (UpdaterUtils.isVerizon()) {
            UpdaterUtils.setFullScreenStartPoint(Configs.STATS_RESTART_START_POINT, "fullScreenForVZW");
            return false;
        } else if (descriptor.getMeta().getSeverity() == UpgradeUtils.SeverityType.CRITICAL.ordinal()) {
            UpdaterUtils.setFullScreenStartPoint(Configs.STATS_RESTART_START_POINT, "fullScreenForCriticalUpdate");
            return false;
        } else {
            return true;
        }
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void promptUpgradeNotification(ApplicationEnv.Database.Descriptor descriptor) throws JSONException {
        this._env.getUtilities().sendSystemRestartNotificationForABUpdate(MetaDataBuilder.toJSONString(descriptor.getMeta()), this.mVersion, descriptor.getRepository());
    }

    @Override // com.motorola.ccc.ota.installer.InstallTypeResolver.Installer
    public void promptUpgradeActivity(ApplicationEnv.Database.Descriptor descriptor) throws JSONException {
        this._env.getUtilities().sendStartRestartActivity(MetaDataBuilder.toJSONString(descriptor.getMeta()), this.mVersion, descriptor.getRepository());
    }
}
