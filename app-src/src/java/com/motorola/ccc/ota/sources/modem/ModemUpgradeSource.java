package com.motorola.ccc.ota.sources.modem;

import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.motorola.ccc.ota.CusSM;
import com.motorola.ccc.ota.NewVersionHandler;
import com.motorola.ccc.ota.env.CusAndroidUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.UpgradeSource;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.BotaDownloadHandler;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
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
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class ModemUpgradeSource extends UpgradeSource {
    final CusSM _sm;
    private NewVersionHandler _versionHanlder;
    private final ConnectivityManager cm;
    private final ApplicationEnv env;
    private AtomicBoolean progress;
    private final BotaSettings settings;
    private final TelephonyManager tm;

    public ModemUpgradeSource(CusSM cusSM, NewVersionHandler newVersionHandler, ApplicationEnv applicationEnv, BotaSettings botaSettings) {
        super(UpgradeSourceType.modem);
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
        return new BotaDownloadHandler(OtaApplication.getGlobalContext(), this._sm, this.env, this.settings, true);
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void checkForUpdate(boolean z) {
        if (TextUtils.isEmpty(this.settings.getString(Configs.PROVISION_TIME))) {
            this.settings.setLong(Configs.PROVISION_TIME, System.currentTimeMillis());
        }
        WebService.call(OtaApplication.getGlobalContext(), new WebRequest(CheckUrlConstructor.constructUrl(new UrlRequest(this.settings.getString(Configs.MASTER_CLOUD), this.settings.getString(Configs.CHECK_FOR_UPGRADE_URL), this.settings.getString(Configs.OTA_CONTEXT), BuildPropReader.getDeviceModemConfigVersionSha1(), this.settings.getString(Configs.MODEM_TRACKINGID), this.settings.getString(Configs.CHECK_FOR_UPGRADE_HTTP_SECURE), this.settings.getString(Configs.CHECK_FOR_UPGRADE_TEST_URL))), z ? 1 : this.settings.getInt(Configs.CHECK_FOR_UPGRADE_HTTP_RETRIES, 1), this.settings.getString(Configs.CHECK_FOR_UPGRADE_HTTP_METHOD), null, new WebRequestPayload(WebRequestPayloadType.string, CheckRequestBuilder.toJSONString(new CheckRequest(BuildPropertyUtils.getId(OtaApplication.getGlobalContext()), 0L, BuildPropReader.getDeviceInfoAsJsonObject(), buildExtraInfoForCheckRequest(2), BuildPropReader.getIdentityInfoAsJsonObject(OtaApplication.getGlobalContext()), "polling", CDSUtils.IDTYPE))), this.settings.getString(Configs.CDS_HTTP_PROXY_HOST), this.settings.getInt(Configs.CDS_HTTP_PROXY_PORT, -1)), new ResponseHandler() { // from class: com.motorola.ccc.ota.sources.modem.ModemUpgradeSource.1
            @Override // com.motorola.otalib.cdsservice.ResponseHandler
            public void handleResponse(WebResponse webResponse) {
                ModemUpgradeSource.this.handleCheckWSResponse(webResponse);
            }
        }, new WebServiceRetryHandler(), null);
    }

    private JSONObject buildExtraInfoForCheckRequest(int i) {
        JSONObject extraInfoAsJsonObject = BuildPropReader.getExtraInfoAsJsonObject(OtaApplication.getGlobalContext(), this.env.getUtilities().getNetwork(), CusAndroidUtils.getApkVersion(), this.settings.getString(Configs.PROVISION_TIME), this.settings.getString(Configs.INCREMENTAL_VERSION), UpgradeSourceType.modem.toString(), this._sm.getDeviceAdditionalInfo(), this.settings);
        try {
            extraInfoAsJsonObject.put("buildId", extraInfoAsJsonObject.optString("buildId") + "--ModemConfig");
            extraInfoAsJsonObject.put("otaSourceSha1", BuildPropReader.getDeviceModemConfigVersionSha1());
        } catch (JSONException e) {
            Logger.error("OtaApp", "ModemUpgradeSource:buildExtraInfoForCheckRequest caught exception while overriding buildId for modem:exeMsg=" + e);
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("clientState", this._sm.getCurrentState(BuildPropReader.getDeviceModemConfigVersionSha1()).toString());
        linkedHashMap.put("pollingFeature", this.settings.getString(Configs.POLLING_FEATURE));
        linkedHashMap.put("modemPollingIntervalInMilliSeconds", String.valueOf(this.settings.getLong(Configs.NEXT_MODEM_POLLING_VALUE, -1L)));
        linkedHashMap.put("prevSessionTrackingId", this.settings.getString(Configs.MODEM_PREVIOUS_TRACKING_ID));
        JSONObject jsonObjectFromMap = UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "buildExtraInfoForCheckRequest");
        Iterator<String> keys = jsonObjectFromMap.keys();
        while (keys.hasNext()) {
            String next = keys.next();
            try {
                extraInfoAsJsonObject.put(next, jsonObjectFromMap.get(next));
            } catch (JSONException e2) {
                Logger.error("OtaApp", "ModemUpgradeSource:buildExtraInfoForCheckRequest caught exception while getting key: " + next + " :" + e2);
            }
        }
        return extraInfoAsJsonObject;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:53:0x0121  */
    /* JADX WARN: Removed duplicated region for block: B:59:0x013a  */
    /* JADX WARN: Removed duplicated region for block: B:81:0x01e6  */
    /* JADX WARN: Removed duplicated region for block: B:84:0x0194 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void handleCheckWSResponse(com.motorola.otalib.cdsservice.webdataobjects.WebResponse r15) {
        /*
            Method dump skipped, instructions count: 495
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.ccc.ota.sources.modem.ModemUpgradeSource.handleCheckWSResponse(com.motorola.otalib.cdsservice.webdataobjects.WebResponse):void");
    }

    @Override // com.motorola.ccc.ota.sources.UpgradeSource
    public void checkForDownloadDescriptor(String str) {
        Logger.debug("OtaApp", "ModemUpgradeSource:checkForDownloadDescriptor:version=" + str);
        Logger.debug("OtaApp", "MODEM_METADATA=" + this.settings.getString(Configs.MODEM_METADATA));
        if (MetaDataBuilder.from(this.settings.getString(Configs.MODEM_METADATA)) == null) {
            Logger.error("OtaApp", "ModemUpgradeSource.checkForDownloadDescriptor: failed parsing metadata");
            this._sm.failDownload(str, UpgradeUtils.DownloadStatus.STATUS_FAIL, "ModemUpgradeSource.checkForDownloadDescriptor: error while parsing metadata", ErrorCodeMapper.KEY_PARSE_ERROR);
        } else if (this.progress.get()) {
            Logger.info("OtaApp", "ModemUpgradeSource.checkForDownloadDescriptor: already sent request and waiting for response .. so return from here");
        } else {
            WebService.call(OtaApplication.getGlobalContext(), new WebRequest(ResourcesUrlConstructor.constructUrl(new UrlRequest(this.settings.getString(Configs.MASTER_CLOUD), this.settings.getString(Configs.DOWNLOAD_DESCRIPTOR_URL), this.settings.getString(Configs.OTA_CONTEXT), BuildPropReader.getDeviceModemConfigVersionSha1(), this.settings.getString(Configs.MODEM_TRACKINGID), this.settings.getString(Configs.DOWNLOAD_DESCRIPTOR_HTTP_SECURE), this.settings.getString(Configs.DOWNLOAD_DESCRIPTOR_TEST_URL))), this.settings.getInt(Configs.DOWNLOAD_DESCRIPTOR_HTTP_RETRIES, 9), this.settings.getString(Configs.DOWNLOAD_DESCRIPTOR_HTTP_METHOD), null, new WebRequestPayload(WebRequestPayloadType.string, ResourcesRequestBuilder.toJSONString(new ResourcesRequest(BuildPropertyUtils.getId(OtaApplication.getGlobalContext()), this.settings.getLong(Configs.MODEM_CONTENT_TIMESTAMP, 0L), BuildPropReader.getDeviceInfoAsJsonObject(), BuildPropReader.getExtraInfoAsJsonObject(OtaApplication.getGlobalContext(), this.env.getUtilities().getNetwork(), CusAndroidUtils.getApkVersion(), this.settings.getString(Configs.PROVISION_TIME), this.settings.getString(Configs.INCREMENTAL_VERSION), UpgradeSourceType.modem.toString(), this._sm.getDeviceAdditionalInfo(), this.settings), BuildPropReader.getIdentityInfoAsJsonObject(OtaApplication.getGlobalContext()), CDSUtils.IDTYPE, this.settings.getString(Configs.MODEM_REPORTINGTAGS), this.settings.getString(Configs.MODEM_GET_DESCRIPTOR_REASON)))), this.settings.getString(Configs.CDS_HTTP_PROXY_HOST), this.settings.getInt(Configs.CDS_HTTP_PROXY_PORT, -1)), new ResponseHandler() { // from class: com.motorola.ccc.ota.sources.modem.ModemUpgradeSource.2
                @Override // com.motorola.otalib.cdsservice.ResponseHandler
                public void handleResponse(WebResponse webResponse) {
                    ModemUpgradeSource.this.handleResourceWSResponse(webResponse, BuildPropReader.getDeviceModemConfigVersionSha1());
                }
            }, new WebServiceRetryHandler(), null);
            this.progress.set(true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleResourceWSResponse(WebResponse webResponse, String str) {
        this.progress.set(false);
        if (webResponse == null) {
            Logger.error("OtaApp", "ModemUpgradeSource:got null response for dd request from cds lib");
            this._sm.failDownload(str, UpgradeUtils.DownloadStatus.STATUS_FAIL, "DD response error : got null response for dd request", ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
        } else if (webResponse.getStatusCode() == 0) {
            Logger.error("OtaApp", "ModemUpgradeSource:no connection response for dd request from cds lib");
        } else if (200 != webResponse.getStatusCode()) {
            Logger.error("OtaApp", "ModemUpgradeSource:got error response for download descriptor request from cds lib");
            this._sm.failDownload(str, UpgradeUtils.DownloadStatus.STATUS_FAIL, "DD response error : giving the modem descriptor request after retry count " + this.settings.getInt(Configs.DOWNLOAD_DESCRIPTOR_HTTP_RETRIES, 9), ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
        } else {
            try {
                ContentResources from = ContentResourcesBuilder.from(convertJsonArraytoJsonObject(webResponse.getPayload()));
                String string = this.settings.getString(Configs.MODEM_TRACKINGID);
                String trackingId = from.getTrackingId();
                if (string != null && !string.equals(trackingId)) {
                    Logger.error("OtaApp", "ModemUpgradeSource:resource request and response trackingId mismatch, return");
                } else if (from.getProceed()) {
                    this.settings.setString(Configs.MODEM_DOWNLOAD_DESCRIPTOR, ContentResourcesBuilder.toJSONString(from));
                    this.settings.setLong(Configs.MODEM_DOWNLOAD_DESCRIPTOR_TIME, System.currentTimeMillis());
                    this.env.getUtilities().sendStartDownloadNotification(str);
                } else {
                    this._sm.cancelModemUpdate(new String[0]);
                }
            } catch (JSONException e) {
                Logger.error("OtaApp", "ModemUpgradeSource:error parsing download descriptor response: " + e);
                this._sm.failDownload(str, UpgradeUtils.DownloadStatus.STATUS_FAIL, "error while parsing modem download descriptor response " + e, ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
            }
        }
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
    /* renamed from: com.motorola.ccc.ota.sources.modem.ModemUpgradeSource$3  reason: invalid class name */
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
