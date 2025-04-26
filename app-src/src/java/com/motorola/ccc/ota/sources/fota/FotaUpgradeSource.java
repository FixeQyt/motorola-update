package com.motorola.ccc.ota.sources.fota;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.NewVersionHandler;
import com.motorola.ccc.ota.env.AndroidFotaInterface;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.UpgradeSource;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.UpgradeStatusConstents;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.fota.FotaUpdateTypes;
import com.motorola.ccc.ota.stats.StatsHelper;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.DmSendAlertService;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.MetadataOverrider;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class FotaUpgradeSource extends UpgradeSource {
    public static final int FOTA_REQUEST_ID = 1138;
    public static final long REBOOT_NOTIFICATION_ANNOY_VALUE_MINS = 240;
    private final ApplicationEnv env;
    private final BotaSettings settings;
    private final CusSM sm;
    private final FotaUpdateTypes updateTypes;
    private final NewVersionHandler versionHanlder;

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public boolean doYouDownloadDirectly(ApplicationEnv.Database.Descriptor descriptor) {
        return false;
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void plugin_exit() {
    }

    public FotaUpgradeSource(CusSM cusSM, NewVersionHandler newVersionHandler, ApplicationEnv applicationEnv, BotaSettings botaSettings) {
        super(UpgradeSourceType.fota);
        this.sm = cusSM;
        this.env = applicationEnv;
        this.versionHanlder = newVersionHandler;
        this.settings = botaSettings;
        this.updateTypes = FotaUpdateTypes.getInstance();
        Logger.debug("OtaApp", "FotaUpgradeSource.FotaUpgradeSource object is constructed");
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void plugin_init(List<ApplicationEnv.Database.Descriptor> list) {
        Logger.debug("OtaApp", "FotaUpgradeSource.plugin_init: sending Fota Initialization intent");
        this.env.getFotaServices().sendFotaInitializationIntent();
    }

    public static UpgradeUtils.Error fotaRequestUpdateResponse(long j, int i) {
        Logger.info("OtaApp", "FotaUpgradeSource.fotaRequestUpdateResponse (" + j + "," + i + ")");
        UpgradeUtils.Error error = UpgradeUtils.Error.ERR_OK;
        switch (i) {
            case 0:
                return UpgradeUtils.Error.ERR_OK;
            case 1:
                return UpgradeUtils.Error.ERR_FAIL;
            case 2:
                return UpgradeUtils.Error.ERR_NET;
            case 3:
                return UpgradeUtils.Error.ERR_ALREADY;
            case 4:
                return UpgradeUtils.Error.ERR_ALREADY;
            case 5:
                return UpgradeUtils.Error.ERR_NOTFOUND;
            case 6:
                return UpgradeUtils.Error.ERR_ROAMING;
            case 7:
                return UpgradeUtils.Error.ERR_INTERNAL;
            case 8:
                return UpgradeUtils.Error.ERR_STORAGE_LOW;
            default:
                return UpgradeUtils.Error.ERR_FAIL;
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:34:0x01c0  */
    /* JADX WARN: Removed duplicated region for block: B:54:0x023b  */
    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void updateAvailable(long r26, long r28, java.lang.String r30, boolean r31, java.lang.String r32, boolean r33) {
        /*
            Method dump skipped, instructions count: 585
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.ccc.ota.sources.fota.FotaUpgradeSource.updateAvailable(long, long, java.lang.String, boolean, java.lang.String, boolean):void");
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void downloadModeChanged(long j, boolean z) {
        this.sm.overWriteMetaData(MetadataOverrider.returnOverWrittenMetaData(MetaDataBuilder.from(this.settings.getString(Configs.METADATA)), "wifionly", z));
        if (z) {
            if (this.settings.getString(Configs.WIFI_DISCOVERY_EXPIRED_FOR_FOTA).equals("none")) {
                this.env.getUtilities().startFotaWifiDiscoveryTimer();
                this.settings.setString(Configs.WIFI_DISCOVERY_EXPIRED_FOR_FOTA, "no");
                return;
            }
            return;
        }
        this.settings.setString(Configs.WIFI_DISCOVERY_EXPIRED_FOR_FOTA, "none");
        this.env.getUtilities().cleanFotaWifiDiscoveryTimer();
    }

    public static void sendFotaDownloadModeChanged(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(AndroidFotaInterface.ACTION_FOTA_DOWNLOAD_MODE_CHANGED));
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void downloadCompleted(long j, int i, String str) {
        MetaData metaData;
        int i2;
        String str2;
        StatsHelper.setDownloadEndTime(this.settings);
        String deviceSha1 = this.env.getServices().getDeviceSha1();
        UpgradeUtils.DownloadStatus fotaDownloadCompleted = this.env.getFotaServices().getFotaDownloadCompleted(j, i);
        boolean z = fotaDownloadCompleted != UpgradeUtils.DownloadStatus.STATUS_VERIFY;
        int i3 = z ? 2 : 0;
        MetaData from = MetaDataBuilder.from(this.settings.getString(Configs.METADATA));
        this.env.getUtilities().cleanFotaWifiDiscoveryTimer();
        String str3 = "OtaApp";
        if (!z) {
            Logger.info("OtaApp", "FotaUpgradeSource.downloadCompleted " + j + " verifying " + str);
            this.settings.setString(Configs.PACKAGE_DOWNLOAD_PATH, FileUtils.DATA);
            if (!verifyFile(this.env, str, this.settings)) {
                Logger.error("OtaApp", "FotaUpgradeSource.downloadCompleted " + j + " failed verifying " + str + " Failure Reason : " + this.settings.getString(Configs.UPGRADE_STATUS_VERIFY) + ".");
                i3 = 4;
                z = true;
            }
        }
        if (z) {
            metaData = null;
        } else {
            Logger.debug("OtaApp", "FotaUpgradeSource.downloadCompleted " + j + " checking metadata " + str);
            metaData = MetaDataBuilder.readMetaDataFromFile(str, this.settings, Configs.METADATA_FILE);
            if (metaData == null) {
                Logger.error("OtaApp", "FotaUpgradeSource.downloadCompleted " + j + " could not read metadata " + str);
                z = true;
                i3 = 3;
            }
        }
        if (z) {
            i2 = i3;
            str2 = null;
        } else {
            str2 = super.isUpgradeAcceptable(BuildPropReader.getCurrentUTC(), metaData.getSourceBuildTimeStamp());
            if (str2 != null) {
                str3 = "OtaApp";
                Logger.error(str3, "FotaUpgradeSource.downloadCompleted failed: " + str2);
                z = true;
                i2 = 3;
            } else {
                str3 = "OtaApp";
                i2 = i3;
            }
        }
        if (z) {
            Logger.info(str3, "FotaUpgradeSource.downloadCompleted failed: " + deviceSha1 + SystemUpdateStatusUtils.SPACE + j + SystemUpdateStatusUtils.SPACE + i2 + SystemUpdateStatusUtils.SPACE + fotaDownloadCompleted);
            this.env.getFotaServices().sendUpgradeResult(j, i2);
            Integer valueOf = Integer.valueOf(i);
            Integer valueOf2 = Integer.valueOf(i2);
            if (str2 == null) {
                str2 = "N/A";
            }
            String format = String.format("fota download failed: (%s) %s %s", valueOf, valueOf2, str2);
            if (from.showDownloadProgress()) {
                this.sm.failDownload(deviceSha1, fotaDownloadCompleted, format, ErrorCodeMapper.KEY_FAILED_FOTA);
                return;
            } else {
                this.sm.failDownloadInternalSilent(deviceSha1, format, ErrorCodeMapper.KEY_FAILED_FOTA);
                return;
            }
        }
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("version", metaData.getVersion()).put("otaTargetSha1", metaData.getTargetSha1()).put("extraSpace", metaData.getExtraSpace()).put("displayVersion", metaData.getDisplayVersion()).put("abMaxChunkSize", metaData.getChunkSize()).put("actualTargetBlurVersion", metaData.getmActualTargetVersion()).put("abInstallType", metaData.getAbInstallType()).put("USERDATA_REQUIRED_FOR_UPDATE", metaData.getUserDataRequiredForUpdate()).put("sourceBuildTimestamp", metaData.getSourceBuildTimeStamp());
            this.sm.overWriteMetaData(MetadataOverrider.returnOverWrittenMetaData(from, jSONObject));
        } catch (JSONException unused) {
            Logger.error(str3, "FotaUpgradeSource.downloadCompleted: override metadata failed");
        }
        this.env.getUtilities().sendInternalNotification(deviceSha1, UpgradeSourceType.fota.toString(), null);
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void handleUpdateStatus(ApplicationEnv.Database.Descriptor descriptor, UpgradeStatusConstents upgradeStatusConstents) {
        if (BuildPropReader.isFotaATT()) {
            try {
                long parseLong = Long.parseLong(descriptor.getInfo());
                switch (AnonymousClass1.$SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[upgradeStatusConstents.ordinal()]) {
                    case 1:
                        this.env.getFotaServices().sendUpdateAvailableResponse(parseLong, 2);
                        return;
                    case 2:
                        this.env.getFotaServices().sendUpgradeResult(parseLong, 2);
                        return;
                    case 3:
                        this.env.getFotaServices().sendUpgradeResult(parseLong, 0);
                        return;
                    case 4:
                        this.env.getFotaServices().sendUpgradeResult(parseLong, 6);
                        return;
                    case 5:
                        this.env.getFotaServices().sendUpgradeResult(parseLong, 1);
                        return;
                    case 6:
                        this.env.getFotaServices().sendUpgradeResult(parseLong, 1);
                        return;
                    case 7:
                        this.env.getFotaServices().sendUpdateAvailableResponse(parseLong, 3);
                        return;
                    case 8:
                        this.env.getFotaServices().sendUpgradeResult(parseLong, 5);
                        return;
                    default:
                        Logger.error("OtaApp", "FotaUpgradeSource.handleUpdateStatus: unknown status " + upgradeStatusConstents);
                        return;
                }
            } catch (Exception e) {
                Logger.error("OtaApp", "FotaUpgradeSource.handleUpdateStatus failed parsing fota request id " + descriptor.getInfo() + e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.sources.fota.FotaUpgradeSource$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$ccc$ota$NewVersionHandler$ReturnCode;
        static final /* synthetic */ int[] $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents;
        static final /* synthetic */ int[] $SwitchMap$com$motorola$ccc$ota$sources$fota$FotaUpdateTypes$Type;

        static {
            int[] iArr = new int[UpgradeStatusConstents.values().length];
            $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents = iArr;
            try {
                iArr[UpgradeStatusConstents.User_Declined_The_Request_Notification.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.User_Declined_Launching_The_Upgrade.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.Successfully_Launched_The_Upgrade.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.Unsuccessfully_Launched_The_Upgrade.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.Internal_Error_Aborting_The_Query.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.Internal_Error_Aborting_The_Upgrade.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.Resources_Error_Aborting_The_Query.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.Resources_Error_Aborting_The_Installation.ordinal()] = 8;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.User_Canceled_The_Update.ordinal()] = 9;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.Package_Verification_failed.ordinal()] = 10;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.System_Update_Policy_Enabled.ordinal()] = 11;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[UpgradeStatusConstents.Download_Failed_Due_To_WiFi_Timeout.ordinal()] = 12;
            } catch (NoSuchFieldError unused12) {
            }
            int[] iArr2 = new int[NewVersionHandler.ReturnCode.values().length];
            $SwitchMap$com$motorola$ccc$ota$NewVersionHandler$ReturnCode = iArr2;
            try {
                iArr2[NewVersionHandler.ReturnCode.NEW_VERSION_ALREADY.ordinal()] = 1;
            } catch (NoSuchFieldError unused13) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$NewVersionHandler$ReturnCode[NewVersionHandler.ReturnCode.NEW_VERSION_INVALID.ordinal()] = 2;
            } catch (NoSuchFieldError unused14) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$NewVersionHandler$ReturnCode[NewVersionHandler.ReturnCode.NEW_VERSION_OK.ordinal()] = 3;
            } catch (NoSuchFieldError unused15) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$NewVersionHandler$ReturnCode[NewVersionHandler.ReturnCode.NEW_VERSION_FAIL.ordinal()] = 4;
            } catch (NoSuchFieldError unused16) {
            }
            int[] iArr3 = new int[FotaUpdateTypes.Type.values().length];
            $SwitchMap$com$motorola$ccc$ota$sources$fota$FotaUpdateTypes$Type = iArr3;
            try {
                iArr3[FotaUpdateTypes.Type.USER_INITIATED.ordinal()] = 1;
            } catch (NoSuchFieldError unused17) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$fota$FotaUpdateTypes$Type[FotaUpdateTypes.Type.DEVICE_INITIATED.ordinal()] = 2;
            } catch (NoSuchFieldError unused18) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$sources$fota$FotaUpdateTypes$Type[FotaUpdateTypes.Type.NETWORK_INITIATED.ordinal()] = 3;
            } catch (NoSuchFieldError unused19) {
            }
        }
    }

    public static void handleUpdateStatus(UpgradeStatusConstents upgradeStatusConstents) {
        if (BuildPropReader.isBotaATT()) {
            switch (AnonymousClass1.$SwitchMap$com$motorola$ccc$ota$sources$UpgradeStatusConstents[upgradeStatusConstents.ordinal()]) {
                case 1:
                case 2:
                case 9:
                    DmSendAlertService.sendFotaDownloadAndUpdateAlert(OtaApplication.getGlobalContext(), 401);
                    return;
                case 3:
                    DmSendAlertService.sendFotaDownloadAndUpdateAlert(OtaApplication.getGlobalContext(), 200);
                    return;
                case 4:
                    DmSendAlertService.sendFotaDownloadAndUpdateAlert(OtaApplication.getGlobalContext(), SystemUpdateStatusUtils.ALERT_CORRUPTED_FIRMWARE_UPDATE_PACKAGE);
                    return;
                case 5:
                case 6:
                    DmSendAlertService.sendFotaDownloadAndUpdateAlert(OtaApplication.getGlobalContext(), SystemUpdateStatusUtils.ALERT_UNDEFINED_ERROR_FIRMWARE_UPDATE_PACKAGE);
                    return;
                case 7:
                    DmSendAlertService.sendFotaDownloadAndUpdateAlert(OtaApplication.getGlobalContext(), SystemUpdateStatusUtils.ALERT_DOWNLOAD_FAILED_DUE_TO_MEMORY_FULL);
                    return;
                case 8:
                    DmSendAlertService.sendFotaDownloadAndUpdateAlert(OtaApplication.getGlobalContext(), SystemUpdateStatusUtils.ALERT_INSTALL_FAILED_DUE_TO_MEMORY_FULL);
                    return;
                case 10:
                    DmSendAlertService.sendFotaDownloadAndUpdateAlert(OtaApplication.getGlobalContext(), SystemUpdateStatusUtils.ALERT_FAILED_FIRMWARE_UPDATE_PACKAGE_CERT_VALIDATE);
                    return;
                case 11:
                    DmSendAlertService.sendFotaDownloadAndUpdateAlert(OtaApplication.getGlobalContext(), SystemUpdateStatusUtils.ALERT_FIRMWARE_UPDATE_FAILED_DUE_TO_SYSTEM_UPDATE_POLICY_ENABLED);
                    return;
                case 12:
                    DmSendAlertService.sendFotaDownloadAndUpdateAlert(OtaApplication.getGlobalContext(), SystemUpdateStatusUtils.ALERT_DOWNLOAD_FAILED_DUE_TO_TIMEOUT);
                    return;
                default:
                    Logger.error("OtaApp", "FotaUpgradeSource.handleUpdateStatus: unknown status " + upgradeStatusConstents);
                    DmSendAlertService.sendFotaDownloadAndUpdateAlert(OtaApplication.getGlobalContext(), SystemUpdateStatusUtils.ALERT_UNDEFINED_ERROR_FIRMWARE_UPDATE_PACKAGE);
                    return;
            }
        }
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void checkForUpdate(boolean z, int i, boolean z2) {
        Logger.debug("OtaApp", "sending check for update call against FOTA repository");
        ConnectivityManager connectivityManager = (ConnectivityManager) OtaApplication.getGlobalContext().getSystemService("connectivity");
        if (NetworkUtils.isRoaming(connectivityManager) && !NetworkUtils.isWifi(connectivityManager)) {
            Logger.info("OtaApp", "FotaUpgradeSource.checkForUpdate, device is in roaming");
            this.env.getUtilities().sendActionUpdateResponse(UpgradeUtils.Error.ERR_ROAMING, i, z2, null);
        } else if (z2) {
            this.env.getFotaServices().sendRequestUpdate(i);
        } else {
            shutDownBotaPolling();
        }
    }

    private void shutDownBotaPolling() {
        this.settings.setLong(Configs.NEXT_POLLING_VALUE, 0L);
        this.settings.setLong(Configs.POLL_AFTER, 0L);
        this.settings.removeConfig(Configs.POLLING_FEATURE);
        this.env.getUtilities().sendPollIntent();
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void checkForDownloadDescriptor(String str) {
        MetaData from = MetaDataBuilder.from(this.settings.getString(Configs.METADATA));
        if (this.settings.getInt(Configs.DATA_SPACE_RETRY_COUNT, 0) > 0) {
            this.sm.overWriteMetaData(MetadataOverrider.returnOverWrittenMetaData(from, "showDownloadProgress", true));
        }
        if (from.isWifiOnly()) {
            if (this.settings.getString(Configs.WIFI_DISCOVERY_EXPIRED_FOR_FOTA).equals("none")) {
                this.env.getUtilities().startFotaWifiDiscoveryTimer();
                this.settings.setString(Configs.WIFI_DISCOVERY_EXPIRED_FOR_FOTA, "no");
            } else if (this.settings.getString(Configs.WIFI_DISCOVERY_EXPIRED_FOR_FOTA).equals("no")) {
                this.settings.setString(Configs.WIFI_DISCOVERY_EXPIRED_FOR_FOTA, "yes");
            }
        }
        this.env.getUtilities().sendStartDownloadNotification(str);
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public DownloadHandler getDownloadHandler() {
        return new FotaDownloadHandler(this.env, this.settings, this.sm);
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void setMemoryLowInfo(ApplicationEnv.Database.Descriptor descriptor) {
        MetaData meta = descriptor.getMeta();
        if (meta.showDownloadProgress()) {
            return;
        }
        this.sm.overWriteMetaData(MetadataOverrider.returnOverWrittenMetaData(meta, "showDownloadProgress", true));
    }
}
