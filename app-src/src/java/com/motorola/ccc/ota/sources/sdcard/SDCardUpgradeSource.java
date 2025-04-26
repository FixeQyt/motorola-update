package com.motorola.ccc.ota.sources.sdcard;

import android.os.Build;
import android.os.PowerManager;
import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.NewVersionHandler;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineHelper;
import com.motorola.ccc.ota.sources.UpgradeSource;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.ZipUtils;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.common.metaData.CheckForUpgradeTriggeredBy;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class SDCardUpgradeSource extends UpgradeSource {
    static final int CP_NOTIFY_CHUNK = 1048576;
    private final ApplicationEnv env;
    private final PowerManager pmanager;
    private final BotaSettings settings;
    private final CusSM sm;
    private final NewVersionHandler versionHandler;

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void setMemoryLowInfo(ApplicationEnv.Database.Descriptor descriptor) {
    }

    public SDCardUpgradeSource(CusSM cusSM, NewVersionHandler newVersionHandler, ApplicationEnv applicationEnv, BotaSettings botaSettings) {
        super(UpgradeSourceType.sdcard);
        this.sm = cusSM;
        this.env = applicationEnv;
        this.pmanager = (PowerManager) OtaApplication.getGlobalContext().getSystemService("power");
        this.settings = botaSettings;
        this.versionHandler = newVersionHandler;
        Logger.debug("OtaApp", "SDCardUpgradeSource.SDCardUpgradeSource object is constructed");
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public DownloadHandler getDownloadHandler() {
        return new SDCardDownloadHandler(this.pmanager, this.env, this.settings);
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void checkForUpdate(String str, String str2) {
        Logger.debug("OtaApp", "SDCardUpgradeSource.checkForUpdate: path " + str + " [AddOnInfo: " + str2 + "]");
        String str3 = str + FileUtils.SD_CARD_DIR;
        Logger.debug("OtaApp", "SDCardUpgradeSource.checkForUpdate: looking for upgrade files in " + str3);
        List<String> upgradeFileList = FileUtils.getUpgradeFileList(str3);
        if (upgradeFileList == null || upgradeFileList.isEmpty()) {
            Logger.debug("OtaApp", "SDCardUpgradeSource.checkForUpdate: no upgrade files found on sd card");
            return;
        }
        Logger.debug("OtaApp", "SDCardUpgradeSource.checkForUpdate: found the following upgrades on sdcard");
        for (int i = 0; i < upgradeFileList.size(); i++) {
            Logger.debug("OtaApp", "SDCardUpgradeSource.checkForUpdate: " + upgradeFileList.get(i));
        }
        Iterator<String> it = upgradeFileList.iterator();
        while (it.hasNext()) {
            String str4 = str3 + it.next();
            try {
                Logger.debug("OtaApp", "SDCardUpgradeSource._onSDCardinserted: verifying file " + str4);
                if (!verifyFile(this.env, str4, this.settings)) {
                    Logger.error("OtaApp", "SDCardUpgradeSource.checkForUpdate: failed to verify file " + str4 + " Failure  Reason : " + this.settings.getString(Configs.UPGRADE_STATUS_VERIFY) + ".");
                } else {
                    Logger.debug("OtaApp", "SDCardUpgradeSource.checkForUpdate: file verified " + str4);
                    MetaData readMetaDataFromFile = MetaDataBuilder.readMetaDataFromFile(str4, this.settings, Configs.METADATA_FILE);
                    if (readMetaDataFromFile == null) {
                        Logger.error("OtaApp", "SDCardUpgradeSource.checkForUpdate: could not parse metadata from " + str4);
                    } else {
                        JSONObject jSONObject = MetaDataBuilder.toJSONObject(readMetaDataFromFile);
                        long length = new File(str4).length();
                        if (length <= 0) {
                            Logger.error("OtaApp", "SDCardUpgradeSource.checkForUpdate: file size zero for " + str4);
                        } else {
                            jSONObject.put("size", length);
                            Logger.debug("OtaApp", "SDCardUpgradeSource.checkForUpdate: setting size to " + length);
                            jSONObject.put("fingerprint", Build.FINGERPRINT);
                            Logger.debug("OtaApp", "metaDataBuiled (sdcard) : " + jSONObject.toString());
                            MetaData from = MetaDataBuilder.from(jSONObject);
                            if (this.settings.getString(Configs.TRIGGERED_BY) == null) {
                                this.settings.setString(Configs.TRIGGERED_BY, CheckForUpgradeTriggeredBy.user.name());
                            }
                            Logger.debug("OtaApp", "SDCardUpgradeSource.checkForUpdate: Found an upgrade file on sd card " + str4);
                            NewVersionHandler.ReturnCode handleNewVersion = this.versionHandler.handleNewVersion(from, str4, UpgradeSourceType.sdcard, str2, 0L, "unknown", "unknown");
                            if (handleNewVersion == NewVersionHandler.ReturnCode.VAB_VALIDATION_PKG_FOUND) {
                                Logger.info("OtaApp", "SDCardUpgradeSource.checkForUpdate: VAB Validation package found " + str4 + " worthy");
                                this.settings.setString(Configs.UPDATING_VALIDATION_FILE, str4);
                                validateVABUpdate(str4);
                                return;
                            } else if (handleNewVersion == NewVersionHandler.ReturnCode.NEW_VERSION_OK) {
                                Logger.info("OtaApp", "SDCardUpgradeSource.checkForUpdate: handleNewVersion found " + str4 + " worthy");
                                return;
                            } else {
                                Logger.debug("OtaApp", "SDCardUpgradeSource.checkForUpdate: handleNewVersion did not find " + str4 + " worthy");
                                this.settings.removeConfig(Configs.TRIGGERED_BY);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.error("OtaApp", "SDCardUpgradeSource.checkForUpdate: failed to read jar file " + str4 + e);
                this.settings.removeConfig(Configs.TRIGGERED_BY);
            }
        }
    }

    private void validateVABUpdate(String str) {
        try {
            String substring = str.substring(0, str.lastIndexOf(FileUtils.EXT));
            File file = new File(substring);
            ZipUtils.extract(new File(str), file);
            String str2 = FileUtils.getDownloadDataDirectory() + "/payload_metadata.bin";
            FileUtils.copy(substring + "/payload_metadata.bin", str2);
            File file2 = new File(str2);
            file2.setReadable(true, false);
            file2.setWritable(true);
            UpdaterEngineHelper.verifyPayloadMetadata(str2, true);
        } catch (IOException | NullPointerException e) {
            Logger.error("OtaApp", "Exception occurred while verifying payload metadata:" + e);
            this.sm.onIntentVABVerifyPayloadStatus(false);
        }
    }
}
