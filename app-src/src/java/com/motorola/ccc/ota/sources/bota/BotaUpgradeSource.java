package com.motorola.ccc.ota.sources.bota;

import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.NewVersionHandler;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.env.SystemUpdaterPolicy;
import com.motorola.ccc.ota.sources.UpgradeSource;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.fota.FotaUpgradeSource;
import com.motorola.ccc.ota.stats.StatsHelper;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.otalib.cdsservice.ResponseHandler;
import com.motorola.otalib.cdsservice.UrlConstructor.CheckUrlConstructor;
import com.motorola.otalib.cdsservice.UrlConstructor.ResourcesUrlConstructor;
import com.motorola.otalib.cdsservice.WebService;
import com.motorola.otalib.cdsservice.WebServiceRetryHandler;
import com.motorola.otalib.cdsservice.requestdataobjects.CheckRequest;
import com.motorola.otalib.cdsservice.requestdataobjects.ResourcesRequest;
import com.motorola.otalib.cdsservice.requestdataobjects.UrlRequest;
import com.motorola.otalib.cdsservice.requestdataobjects.builders.CheckRequestBuilder;
import com.motorola.otalib.cdsservice.requestdataobjects.builders.ResourcesRequestBuilder;
import com.motorola.otalib.cdsservice.responsedataobjects.ContentResource;
import com.motorola.otalib.cdsservice.responsedataobjects.ContentResources;
import com.motorola.otalib.cdsservice.responsedataobjects.builders.ContentResourcesBuilder;
import com.motorola.otalib.cdsservice.utils.CDSUtils;
import com.motorola.otalib.cdsservice.utils.NetworkTags;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequest;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequestPayload;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequestPayloadType;
import com.motorola.otalib.cdsservice.webdataobjects.WebResponse;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class BotaUpgradeSource extends UpgradeSource {
    final CusSM _sm;
    private NewVersionHandler _versionHanlder;
    private final ConnectivityManager cm;
    private final ApplicationEnv env;
    private AtomicBoolean progress;
    private final BotaSettings settings;
    private final TelephonyManager tm;

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public boolean checkForChainUpgrade(UpgradeSourceType upgradeSourceType) {
        return true;
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void setMemoryLowInfo(ApplicationEnv.Database.Descriptor descriptor) {
    }

    public BotaUpgradeSource(CusSM cusSM, NewVersionHandler newVersionHandler, ApplicationEnv applicationEnv, BotaSettings botaSettings) {
        super(UpgradeSourceType.upgrade);
        this.cm = (ConnectivityManager) OtaApplication.getGlobalContext().getSystemService("connectivity");
        this.tm = (TelephonyManager) OtaApplication.getGlobalContext().getSystemService("phone");
        this._sm = cusSM;
        this._versionHanlder = newVersionHandler;
        this.env = applicationEnv;
        this.settings = botaSettings;
        this.progress = new AtomicBoolean(false);
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public DownloadHandler getDownloadHandler() {
        return new BotaDownloadHandler(OtaApplication.getGlobalContext(), this._sm, this.env, this.settings, false);
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void checkForUpdate(boolean z, final int i, final boolean z2) {
        Logger.debug("OtaApp", "OTA APK version - " + CusAndroidUtils.getApkVersion());
        if (NetworkUtils.isRoaming(this.cm) && !NetworkUtils.isWifi(this.cm)) {
            Logger.info("OtaApp", "BotaUpgradeSource.checkForUpdate, device is in roaming");
            this.env.getUtilities().sendActionUpdateResponse(UpgradeUtils.Error.ERR_ROAMING, i, z2, null);
            scheduleDefaultPolling(i);
            return;
        }
        String triggeredBy = getTriggeredBy(i);
        if (this.settings.getString(Configs.TRIGGERED_BY) == null) {
            this.settings.setString(Configs.TRIGGERED_BY, triggeredBy);
        }
        if (TextUtils.isEmpty(this.settings.getString(Configs.PROVISION_TIME))) {
            this.settings.setLong(Configs.PROVISION_TIME, System.currentTimeMillis());
        }
        UpdaterUtils.setSoftBankProxyData(OtaApplication.getGlobalContext());
        WebRequest webRequest = new WebRequest(CheckUrlConstructor.constructUrl(new UrlRequest(this.settings.getString(Configs.MASTER_CLOUD), this.settings.getString(Configs.CHECK_FOR_UPGRADE_URL), this.settings.getString(Configs.OTA_CONTEXT), BuildPropReader.getContextKey(UpgradeSourceType.upgrade.toString()), this.settings.getString(Configs.CHECK_FOR_UPGRADE_HTTP_SECURE), this.settings.getString(Configs.CHECK_FOR_UPGRADE_TEST_URL))), z2 ? 1 : this.settings.getInt(Configs.CHECK_FOR_UPGRADE_HTTP_RETRIES, 1), this.settings.getString(Configs.CHECK_FOR_UPGRADE_HTTP_METHOD), null, new WebRequestPayload(WebRequestPayloadType.string, CheckRequestBuilder.toJSONString(new CheckRequest(BuildPropertyUtils.getId(OtaApplication.getGlobalContext()), 0L, BuildPropReader.getDeviceInfoAsJsonObject(), buildExtraInfoForCheckRequest(i), BuildPropReader.getIdentityInfoAsJsonObject(OtaApplication.getGlobalContext()), triggeredBy, CDSUtils.IDTYPE))), this.settings.getString(Configs.CDS_HTTP_PROXY_HOST), this.settings.getInt(Configs.CDS_HTTP_PROXY_PORT, -1));
        ResponseHandler responseHandler = new ResponseHandler() { // from class: com.motorola.ccc.ota.sources.bota.BotaUpgradeSource.1
            @Override // com.motorola.otalib.cdsservice.ResponseHandler
            public void handleResponse(WebResponse webResponse) {
                BotaUpgradeSource.this.handleCheckWSResponse(webResponse, i, z2);
            }
        };
        if (i == 0) {
            WebService.call(OtaApplication.getGlobalContext(), webRequest, responseHandler, null);
        } else {
            WebService.call(OtaApplication.getGlobalContext(), webRequest, responseHandler, new WebServiceRetryHandler(), null);
        }
    }

    private JSONObject buildExtraInfoForCheckRequest(int i) {
        SystemUpdaterPolicy systemUpdaterPolicy = new SystemUpdaterPolicy();
        JSONObject extraInfoAsJsonObject = BuildPropReader.getExtraInfoAsJsonObject(OtaApplication.getGlobalContext(), this.env.getUtilities().getNetwork(), CusAndroidUtils.getApkVersion(), this.settings.getString(Configs.PROVISION_TIME), this.settings.getString(Configs.INCREMENTAL_VERSION), UpgradeSourceType.upgrade.toString(), this._sm.getDeviceAdditionalInfo(), this.settings);
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("autoDownloadPolicySet", Boolean.valueOf(systemUpdaterPolicy.isAutoDownloadOverAnyDataNetworkPolicySet()));
        linkedHashMap.put("disableOtaUpdatePolicySet", Boolean.valueOf(systemUpdaterPolicy.isOtaUpdateDisabledPolicySet()));
        linkedHashMap.put("isDeviceUnderFreezePeriod", Boolean.valueOf(systemUpdaterPolicy.isDeviceUnderFreezePeriod()));
        linkedHashMap.put("clientState", this._sm.getCurrentState().toString());
        linkedHashMap.put("pollingFeature", this.settings.getString(Configs.POLLING_FEATURE));
        linkedHashMap.put("pollingIntervalInMilliSeconds", String.valueOf(this.settings.getLong(Configs.NEXT_POLLING_VALUE, -1L)));
        linkedHashMap.put("prevSessionTrackingId", this.settings.getString(Configs.PREVIOUS_TRACKING_ID));
        if (i == 0) {
            linkedHashMap.put("userTriggerLaunchPoint", this.settings.getString(Configs.USER_TRIGGER_LAUNCH_POINT));
            this.settings.removeConfig(Configs.USER_TRIGGER_LAUNCH_POINT);
        }
        linkedHashMap.put("updatePreferenceVisitedCount", this.settings.getConfig(Configs.STATS_SMART_UPDATE_INSTALL_PREFS_SELECTED, "0"));
        linkedHashMap.put("updatePreferenceVisitedFromUpdateCompleteCount", this.settings.getConfig(Configs.STATS_UPDATE_PREFS_SELECTED_FROM_UPDATE_COMPLETE, "0"));
        linkedHashMap.put("smartUpdateStats", StatsHelper.getUpdatePreferenceStats(StatsHelper.getSmartUpdateJSON(), Configs.STATS_SMART_UPDATE_STORED));
        linkedHashMap.put("eolStats", StatsHelper.getUpdatePreferenceStats(StatsHelper.getEOLStatsJSON(), Configs.STATS_EOL_STORED));
        linkedHashMap.put("historyStats", StatsHelper.getUpdatePreferenceStats(StatsHelper.getHistoryStatsJSON(), Configs.STATS_HISTORY_STORED));
        linkedHashMap.put("whyUpdateMattersStats", StatsHelper.getUpdatePreferenceStats(StatsHelper.getWhyUpdateMattersStatsJSON(), Configs.STATS_WHY_UPDATE_MATTERS_STORED));
        linkedHashMap.put("vabMergeCheckUpdateAfterDeviceCorrupted", Long.valueOf(System.currentTimeMillis()));
        JSONObject jsonObjectFromMap = UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "buildExtraInfoForCheckRequest");
        Iterator<String> keys = jsonObjectFromMap.keys();
        while (keys.hasNext()) {
            String next = keys.next();
            try {
                extraInfoAsJsonObject.put(next, jsonObjectFromMap.get(next));
            } catch (JSONException e) {
                Logger.error("OtaApp", "buildExtraInfoForCheckRequest caught exception while getting key: " + next + " :" + e);
            }
        }
        return extraInfoAsJsonObject;
    }

    private void scheduleDefaultPolling(int i) {
        if (2 == i) {
            long j = this.settings.getLong(Configs.NO_POLLING_VALUE_FROM_SERVER, 86400L);
            Logger.info("OtaApp", "BotaUpgradeSource.scheduleDefaultPolling, Ota client configured next polling to (in secs) " + j);
            long j2 = j * 1000;
            this.settings.setLong(Configs.NEXT_POLLING_VALUE, System.currentTimeMillis() + j2);
            this.settings.setLong(Configs.POLL_AFTER, j2);
            this.env.getUtilities().sendPollIntent();
        }
    }

    private void saveSettings(JSONObject jSONObject) {
        if (jSONObject != null) {
            try {
                if (this.settings.getString(Configs.SETTINGS_VERSION).equals(jSONObject.getString("settingsVersion"))) {
                    return;
                }
                Logger.info("OtaApp", "Change in settingsVersion, update the new appSettings");
                JSONObject optJSONObject = jSONObject.optJSONObject("appSettings");
                if (optJSONObject != null) {
                    JSONArray names = optJSONObject.names();
                    int length = optJSONObject.length();
                    for (int i = 0; i < length; i++) {
                        String string = names.getString(i);
                        String string2 = optJSONObject.getString(string);
                        Logger.debug("OtaApp", "BotaUpgradeSource.saveSettings():saving :" + string + " value :" + string2);
                        this.settings.setConfig(string, string2);
                    }
                }
            } catch (JSONException e) {
                Logger.error("OtaApp", "Failed reading settings :" + e);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:116:0x0323  */
    /* JADX WARN: Removed duplicated region for block: B:119:0x032a  */
    /* JADX WARN: Removed duplicated region for block: B:141:0x03db  */
    /* JADX WARN: Removed duplicated region for block: B:147:0x03f3  */
    /* JADX WARN: Removed duplicated region for block: B:148:0x0404  */
    /* JADX WARN: Removed duplicated region for block: B:152:0x0424  */
    /* JADX WARN: Removed duplicated region for block: B:160:0x043a  */
    /* JADX WARN: Removed duplicated region for block: B:171:0x04a1  */
    /* JADX WARN: Removed duplicated region for block: B:178:0x0538  */
    /* JADX WARN: Removed duplicated region for block: B:187:0x056d  */
    /* JADX WARN: Removed duplicated region for block: B:196:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:43:0x0131  */
    /* JADX WARN: Removed duplicated region for block: B:54:0x0173  */
    /* JADX WARN: Removed duplicated region for block: B:55:0x0179  */
    /* JADX WARN: Removed duplicated region for block: B:58:0x0182  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x0225  */
    /* JADX WARN: Removed duplicated region for block: B:84:0x0254  */
    /* JADX WARN: Removed duplicated region for block: B:89:0x0275  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void handleCheckWSResponse(com.motorola.otalib.cdsservice.webdataobjects.WebResponse r18, int r19, boolean r20) {
        /*
            Method dump skipped, instructions count: 1393
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.ccc.ota.sources.bota.BotaUpgradeSource.handleCheckWSResponse(com.motorola.otalib.cdsservice.webdataobjects.WebResponse, int, boolean):void");
    }

    private void clearSomePrefs() {
        Logger.debug("OtaApp", "clearing triggered by setting: " + this.settings.getString(Configs.TRIGGERED_BY));
        this.settings.removeConfig(Configs.TRIGGERED_BY);
    }

    private String whoInitiated(int i) {
        if (i == 0) {
            return "user initiated upgrade";
        }
        if (1 == i) {
            return "client initiated upgrade";
        }
        if (2 == i) {
            return "polling initiated upgrade";
        }
        if (3 == i) {
            return "server initiated upgrade";
        }
        if (1138 != i) {
            return "An Upgrade";
        }
        return "chained upgrade";
    }

    private String getTriggeredBy(int i) {
        return (i < 0 || i >= 5) ? "other" : new String[]{"user", "setup", "polling", "notification", "other"}[i];
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void checkForDownloadDescriptor(String str) {
        if (MetaDataBuilder.from(this.settings.getString(Configs.METADATA)) == null) {
            Logger.error("OtaApp", "OTAUpgradeSource.checkForDownloadDescriptor: failed parsing metadata");
            this._sm.failDownload(str, UpgradeUtils.DownloadStatus.STATUS_FAIL, "BotaUpgradeSource.checkForDownloadDescriptor: error while parsing metadata", ErrorCodeMapper.KEY_PARSE_ERROR);
        } else if (this.progress.get()) {
            Logger.info("OtaApp", "OTAUpgradeSource.checkForDownloadDescriptor: already sent request and waiting for response .. so return from here");
        } else {
            UpdaterUtils.setSoftBankProxyData(OtaApplication.getGlobalContext());
            WebService.call(OtaApplication.getGlobalContext(), new WebRequest(ResourcesUrlConstructor.constructUrl(new UrlRequest(this.settings.getString(Configs.MASTER_CLOUD), this.settings.getString(Configs.DOWNLOAD_DESCRIPTOR_URL), this.settings.getString(Configs.OTA_CONTEXT), BuildPropReader.getContextKey(UpgradeSourceType.upgrade.toString()), this.settings.getString(Configs.TRACKINGID), this.settings.getString(Configs.DOWNLOAD_DESCRIPTOR_HTTP_SECURE), this.settings.getString(Configs.DOWNLOAD_DESCRIPTOR_TEST_URL))), this.settings.getInt(Configs.DOWNLOAD_DESCRIPTOR_HTTP_RETRIES, 9), this.settings.getString(Configs.DOWNLOAD_DESCRIPTOR_HTTP_METHOD), null, new WebRequestPayload(WebRequestPayloadType.string, ResourcesRequestBuilder.toJSONString(new ResourcesRequest(BuildPropertyUtils.getId(OtaApplication.getGlobalContext()), this.settings.getLong(Configs.CONTENT_TIMESTAMP, 0L), BuildPropReader.getDeviceInfoAsJsonObject(), BuildPropReader.getExtraInfoAsJsonObject(OtaApplication.getGlobalContext(), this.env.getUtilities().getNetwork(), CusAndroidUtils.getApkVersion(), this.settings.getString(Configs.PROVISION_TIME), this.settings.getString(Configs.INCREMENTAL_VERSION), UpgradeSourceType.upgrade.toString(), this._sm.getDeviceAdditionalInfo(), this.settings), BuildPropReader.getIdentityInfoAsJsonObject(OtaApplication.getGlobalContext()), CDSUtils.IDTYPE, this.settings.getString(Configs.REPORTINGTAGS), this.settings.getString(Configs.OTA_GET_DESCRIPTOR_REASON)))), this.settings.getString(Configs.CDS_HTTP_PROXY_HOST), this.settings.getInt(Configs.CDS_HTTP_PROXY_PORT, -1)), new ResponseHandler() { // from class: com.motorola.ccc.ota.sources.bota.BotaUpgradeSource.2
                @Override // com.motorola.otalib.cdsservice.ResponseHandler
                public void handleResponse(WebResponse webResponse) {
                    BotaUpgradeSource botaUpgradeSource = BotaUpgradeSource.this;
                    botaUpgradeSource.handleResouresWSResponse(webResponse, botaUpgradeSource.env.getServices().getDeviceSha1());
                }
            }, new WebServiceRetryHandler(), null);
            this.progress.set(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleResouresWSResponse(WebResponse webResponse, String str) {
        this.progress.set(false);
        if (webResponse == null) {
            Logger.error("OtaApp", "got null response for dd request from cds lib");
            this._sm.failDownload(str, UpgradeUtils.DownloadStatus.STATUS_FAIL, "DD response error : got null response for dd request", ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
        } else if (webResponse.getStatusCode() == 0) {
            Logger.error("OtaApp", "no connection response for dd request from cds lib");
        } else if (200 != webResponse.getStatusCode()) {
            Logger.error("OtaApp", "got error response for download descriptor request from cds lib");
            this._sm.failDownload(str, UpgradeUtils.DownloadStatus.STATUS_FAIL, "DD response error : giving the descriptor request after retry count " + this.settings.getInt(Configs.DOWNLOAD_DESCRIPTOR_HTTP_RETRIES, 9), ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
        } else {
            try {
                ContentResources from = ContentResourcesBuilder.from(convertJsonArraytoJsonObject(webResponse.getPayload()));
                String string = this.settings.getString(Configs.TRACKINGID);
                String trackingId = from.getTrackingId();
                if (string != null && !string.equals(trackingId)) {
                    Logger.error("OtaApp", "BotaUpgradeSource:resource request and response trackingId mismatch, return");
                } else if (from.getProceed()) {
                    this.settings.setString(Configs.DOWNLOAD_DESCRIPTOR, ContentResourcesBuilder.toJSONString(from));
                    this.settings.setLong(Configs.DOWNLOAD_DESCRIPTOR_TIME, System.currentTimeMillis());
                    this.env.getUtilities().sendStartDownloadNotification(str);
                } else {
                    this._sm.cancelOTA(new String[0]);
                }
            } catch (JSONException e) {
                Logger.error("OtaApp", "error parsing download descriptor response: " + e);
                this._sm.failDownload(str, UpgradeUtils.DownloadStatus.STATUS_FAIL, "error while parsing download descriptor response " + e, ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
            }
        }
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void sendChainUpgradeRequest(UpgradeSourceType upgradeSourceType) {
        this.env.getUtilities().sendCheckForUpdate(upgradeSourceType == UpgradeSourceType.bootstrap, FotaUpgradeSource.FOTA_REQUEST_ID);
    }

    public static JSONObject convertJsonArraytoJsonObject(JSONObject jSONObject) {
        String[] tags;
        if (jSONObject == null) {
            return null;
        }
        try {
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("proceed", jSONObject.getBoolean("proceed"));
            jSONObject2.putOpt("trackingId", jSONObject.getString("trackingId"));
            if (jSONObject.optJSONArray("contentResources") == null) {
                Logger.info("OtaApp", "convertJsonArraytoJsonObject, contentResources is null");
                return jSONObject2;
            }
            ContentResource[] contentResourceArr = (ContentResource[]) new Gson().fromJson(jSONObject.optJSONArray("contentResources").toString(), ContentResource[].class);
            if (contentResourceArr != null && contentResourceArr.length > 0) {
                for (ContentResource contentResource : contentResourceArr) {
                    for (String str : contentResource.getTags()) {
                        if (str != null && NetworkUtils.isNetworkTagValid(str)) {
                            int i = AnonymousClass3.$SwitchMap$com$motorola$otalib$cdsservice$utils$NetworkTags[NetworkTags.valueOf(str).ordinal()];
                            if (i == 1) {
                                jSONObject2.put("cellUrl", contentResource.getDownloadURL());
                                jSONObject2.put("cellHeaders", contentResource.getHeaders());
                            } else if (i == 2) {
                                jSONObject2.put("wifiUrl", contentResource.getDownloadURL());
                                jSONObject2.put("wifiHeaders", contentResource.getHeaders());
                            } else if (i == 3) {
                                jSONObject2.put("adminApnUrl", contentResource.getDownloadURL());
                                jSONObject2.put("adminApnHeaders", contentResource.getHeaders());
                            }
                        }
                    }
                }
                Logger.verbose("OtaApp", "downloadContent: " + jSONObject2.toString());
                return jSONObject2;
            }
            return null;
        } catch (Exception e) {
            Logger.error("OtaApp", "Error in parsing contentResources response " + e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.sources.bota.BotaUpgradeSource$3  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$cdsservice$utils$NetworkTags;

        static {
            int[] iArr = new int[NetworkTags.values().length];
            $SwitchMap$com$motorola$otalib$cdsservice$utils$NetworkTags = iArr;
            try {
                iArr[NetworkTags.CELL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$otalib$cdsservice$utils$NetworkTags[NetworkTags.WIFI.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$otalib$cdsservice$utils$NetworkTags[NetworkTags.USEADMINAPN.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }
}
