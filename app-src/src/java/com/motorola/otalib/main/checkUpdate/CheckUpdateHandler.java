package com.motorola.otalib.main.checkUpdate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.motorola.otalib.BuildConfig;
import com.motorola.otalib.aidl.IOtaLibServiceCallBack;
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
import com.motorola.otalib.cdsservice.responsedataobjects.CheckResponse;
import com.motorola.otalib.cdsservice.responsedataobjects.ContentResources;
import com.motorola.otalib.cdsservice.responsedataobjects.builders.CheckResponseBuilder;
import com.motorola.otalib.cdsservice.responsedataobjects.builders.ContentResourcesBuilder;
import com.motorola.otalib.cdsservice.utils.CDSUtils;
import com.motorola.otalib.cdsservice.utils.NetworkTags;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequest;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequestPayload;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequestPayloadType;
import com.motorola.otalib.cdsservice.webdataobjects.WebResponse;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.metaData.CheckForUpgradeTriggeredBy;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import com.motorola.otalib.main.InstallStatusInfo;
import com.motorola.otalib.main.LibCussm;
import com.motorola.otalib.main.Logger;
import com.motorola.otalib.main.PublicUtilityMethods;
import com.motorola.otalib.main.Settings.LibConfigs;
import com.motorola.otalib.main.Settings.LibSettings;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CheckUpdateHandler {
    private static AtomicBoolean resourceProgress = new AtomicBoolean(false);

    public synchronized void checkForUpdate(final CheckRequestObj checkRequestObj, final ApplicationEnv.Database database, final LibCussm libCussm, final IOtaLibServiceCallBack iOtaLibServiceCallBack, final Context context, boolean z, final LibSettings libSettings) throws RemoteException {
        Log.d(Logger.OTALib_TAG, "Check for update");
        if (libCussm.isBusy(checkRequestObj.getPrimaryKey())) {
            libCussm.pleaseRunStateMachine(context);
            return;
        }
        final InstallStatusInfo installStatusInfo = new InstallStatusInfo(context, checkRequestObj.getPrimaryKey(), libSettings, null, PublicUtilityMethods.ERROR_INVALID_RESPONSE);
        if (TextUtils.isEmpty(libSettings.getString(LibConfigs.PROVISION_TIME))) {
            libSettings.setLong(LibConfigs.PROVISION_TIME, System.currentTimeMillis());
        }
        if (!NetworkUtils.hasNetwork((ConnectivityManager) context.getSystemService("connectivity"))) {
            installStatusInfo.setStatusCode(PublicUtilityMethods.NO_NETWORK);
            installStatusInfo.setStatusMessage("No network");
            UpdateSessionInfo.deleteSessionDetails(libSettings, checkRequestObj.getPrimaryKey());
            iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
            return;
        }
        UrlRequest urlRequest = new UrlRequest(getMasterCloud(checkRequestObj), libSettings.getString(LibConfigs.CHECK_FOR_UPGRADE_URL), libSettings.getString(LibConfigs.OTA_CONTEXT), checkRequestObj.getContextKey(), libSettings.getString(LibConfigs.CHECK_FOR_UPGRADE_HTTP_SECURE), libSettings.getString(LibConfigs.CHECK_FOR_UPGRADE_TEST_URL));
        Log.d(Logger.OTALib_TAG, "Check for update urlRequest" + urlRequest.toString());
        JSONObject buildExtraInfoForCheckRequest = buildExtraInfoForCheckRequest(context, checkRequestObj, libCussm, libSettings);
        Log.d(Logger.OTALib_TAG, "Check for update extraInfoObject" + buildExtraInfoForCheckRequest.toString());
        CheckRequest checkRequest = new CheckRequest(checkRequestObj.getAccsSerialNumber(), 0L, getDeviceInfoAsJsonObject(checkRequestObj), buildExtraInfoForCheckRequest, getIdentityInfoAsJsonObject(checkRequestObj.getAccsSerialNumber()), checkRequestObj.getTriggeredBy().toString(), CDSUtils.IDTYPE);
        Log.d(Logger.OTALib_TAG, "Check for update checkRequest" + checkRequest.toString());
        WebRequest webRequest = new WebRequest(CheckUrlConstructor.constructUrl(urlRequest), 0, libSettings.getString(LibConfigs.CHECK_FOR_UPGRADE_HTTP_METHOD), null, new WebRequestPayload(WebRequestPayloadType.string, CheckRequestBuilder.toJSONString(checkRequest)), libSettings.getString(LibConfigs.CDS_HTTP_PROXY_HOST), libSettings.getInt(LibConfigs.CDS_HTTP_PROXY_PORT, -1));
        Log.d(Logger.OTALib_TAG, "Check for update webRequest" + webRequest.toString());
        ResponseHandler responseHandler = new ResponseHandler() { // from class: com.motorola.otalib.main.checkUpdate.CheckUpdateHandler.1
            @Override // com.motorola.otalib.cdsservice.ResponseHandler
            public void handleResponse(WebResponse webResponse) {
                try {
                    if (CheckUpdateHandler.this.handleCheckWSResponse(context, webResponse, iOtaLibServiceCallBack, checkRequestObj, database, libCussm, libSettings, installStatusInfo)) {
                        return;
                    }
                    UpdateSessionInfo.deleteSessionDetails(libSettings, checkRequestObj.getPrimaryKey());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        if (CheckForUpgradeTriggeredBy.user.name().equalsIgnoreCase(checkRequestObj.getTriggeredBy().toString())) {
            WebService.call(context, webRequest, responseHandler, null);
        } else {
            WebService.call(context, webRequest, responseHandler, new WebServiceRetryHandler(), null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized boolean handleCheckWSResponse(Context context, WebResponse webResponse, IOtaLibServiceCallBack iOtaLibServiceCallBack, CheckRequestObj checkRequestObj, ApplicationEnv.Database database, LibCussm libCussm, LibSettings libSettings, InstallStatusInfo installStatusInfo) throws RemoteException {
        CheckResponse from;
        if (libCussm.isBusy(checkRequestObj.getPrimaryKey())) {
            libCussm.pleaseRunStateMachine(context);
            return true;
        } else if (webResponse == null) {
            Logger.error(Logger.OTALib_TAG, "handleCheckWSResponse: received null response, exit from here");
            installStatusInfo.setStatusMessage("Received null response, exit from here");
            iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
            return false;
        } else {
            Logger.debug(Logger.OTALib_TAG, "OTAlibUpgradeSource.handleCheckWSResponse:" + webResponse.getStatusCode());
            if (200 != webResponse.getStatusCode()) {
                Logger.error(Logger.OTALib_TAG, "handleCheckWSResponse: received response with error code");
                installStatusInfo.setStatusMessage("Received response with error code");
                iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                return false;
            }
            Logger.debug(Logger.OTALib_TAG, "OTAlibUpgradeSource.handleCheckWSResponse: Payload :" + webResponse.getPayload());
            if (webResponse.getPayload() != null && webResponse.getPayload().toString().contains("contentResponse")) {
                Logger.debug(Logger.OTALib_TAG, "success response from previous auth, parse it" + webResponse.getPayload().toString());
                try {
                    from = CheckResponseBuilder.from(new JSONObject(webResponse.getPayload().toString()).get("contentResponse").toString());
                    if (from != null && !from.proceed()) {
                        installStatusInfo.setStatusMessage("Server sent proceed false");
                        iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                        return false;
                    }
                } catch (JSONException e) {
                    Logger.error(Logger.OTALib_TAG, "handleCheckWSResponse. error parsing contentResponse payload:" + e);
                    installStatusInfo.setStatusMessage("Error parsing contentResponse payload");
                    iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                    return false;
                }
            } else {
                from = CheckResponseBuilder.from(webResponse.getPayload());
            }
            CheckResponse checkResponse = from;
            if (checkResponse == null) {
                Logger.error(Logger.OTALib_TAG, "handleCheckWSResponse. error parsing payload");
                installStatusInfo.setStatusMessage("Error parsing payload");
                iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                return false;
            } else if (!checkResponse.proceed()) {
                Logger.debug(Logger.OTALib_TAG, "OTAUpgradeSource.handleCheckWSResponse: no upgrades found for this device at this time");
                installStatusInfo.setStatusMessage("No upgrades found for this device at this time");
                iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                return false;
            } else if (checkResponse.proceed() && checkResponse.getContent() == null) {
                Logger.info(Logger.OTALib_TAG, "OTAUpgradeSource.handleCheckWSResponse: something wrong at server side, proceed = true and no content");
                installStatusInfo.setStatusMessage("something wrong at server side, proceed = true and no content");
                iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                return false;
            } else {
                MetaData from2 = MetaDataBuilder.from(checkResponse.getContent());
                if (from2 == null) {
                    Logger.error(Logger.OTALib_TAG, "OTAUpgradeSource.handleCheckWSResponse failed: check_for_update metadata parse exception");
                    installStatusInfo.setStatusMessage("check_for_update metadata parse exception");
                    iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                    return false;
                }
                boolean handleNewVersion = handleNewVersion(from2, checkRequestObj, database, whoInitiated(checkRequestObj.getTriggeredBy()), from2, libSettings, installStatusInfo, iOtaLibServiceCallBack, libCussm);
                if (handleNewVersion) {
                    UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(libSettings, checkRequestObj.getPrimaryKey());
                    sessionDetails.setContentTimeSTamp(checkResponse.getContextTimeStamp());
                    sessionDetails.setTrackingId(checkResponse.getTrackingId());
                    sessionDetails.setReportingTag(checkResponse.getReportingTags());
                    sessionDetails.setStatusCode(PublicUtilityMethods.SUCCESS);
                    UpdateSessionInfo.setSessionDetails(libSettings, checkRequestObj.getPrimaryKey(), sessionDetails);
                }
                libCussm.pleaseRunStateMachine(context);
                return handleNewVersion;
            }
        }
    }

    public synchronized boolean handleNewVersion(MetaData metaData, CheckRequestObj checkRequestObj, ApplicationEnv.Database database, String str, MetaData metaData2, LibSettings libSettings, InstallStatusInfo installStatusInfo, IOtaLibServiceCallBack iOtaLibServiceCallBack, LibCussm libCussm) throws RemoteException {
        String str2 = str;
        synchronized (this) {
            Logger.debug(Logger.OTALib_TAG, "CusSM.handleNewVersion: was notified of a new version :" + metaData.getDisplayVersion() + " source Version " + checkRequestObj.getSourceVersion() + " source Sha1 " + metaData.getSourceSha1());
            if (metaData.getSize() <= 0) {
                Logger.debug(Logger.OTALib_TAG, "CusSM.handleNewVersion failed: size of package not proper");
                String str3 = str2 + " : size of package not proper";
                installStatusInfo.setStatusMessage(str3);
                iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                database.setStatus(checkRequestObj.getPrimaryKey(), metaData, "OtaLib", null, "size of package not proper", str3);
                return false;
            }
            try {
                if (Long.parseLong(metaData.getDisplayVersion()) <= checkRequestObj.getSourceVersion()) {
                    Logger.debug(Logger.OTALib_TAG, "CusSM.handleNewVersion failed: target version is less than source");
                    String str4 = str2 + " : target version is less than source";
                    try {
                        installStatusInfo.setStatusMessage(str4);
                        iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                        return false;
                    } catch (NumberFormatException e) {
                        e = e;
                        str2 = str4;
                        Logger.debug(Logger.OTALib_TAG, "CusSM.handleNewVersion failed: target version exception " + e.getMessage());
                        String str5 = str2 + " : target version exception " + e.getMessage();
                        installStatusInfo.setStatusMessage(str5);
                        iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                        database.setStatus(checkRequestObj.getPrimaryKey(), metaData, "OtaLib", null, "target version exception", str5);
                        return false;
                    }
                }
                if (!database.insert(checkRequestObj.getPrimaryKey(), "OtaLib", metaData, null, str)) {
                    Logger.debug(Logger.OTALib_TAG, "CusSM.handleNewVersion failed: could not store data for " + checkRequestObj.getPrimaryKey() + "in db");
                    if (!libCussm.isBusy(checkRequestObj.getPrimaryKey())) {
                        String str6 = str2 + " : could not store data in db for " + checkRequestObj.getPrimaryKey();
                        installStatusInfo.setStatusMessage(str6);
                        iOtaLibServiceCallBack.onStatusUpdate(checkRequestObj.getContextKey(), false, installStatusInfo.toString());
                        database.setStatus(checkRequestObj.getPrimaryKey(), metaData, "OtaLib", null, "could not store data in db for " + checkRequestObj.getPrimaryKey(), str6);
                        return false;
                    }
                } else {
                    Logger.debug(Logger.OTALib_TAG, "CusSM.handleNewVersion new check request " + checkRequestObj.getPrimaryKey());
                }
                return true;
            } catch (NumberFormatException e2) {
                e = e2;
            }
        }
    }

    private JSONObject buildExtraInfoForCheckRequest(Context context, CheckRequestObj checkRequestObj, LibCussm libCussm, LibSettings libSettings) {
        JSONObject extraInfoAsJsonObject = getExtraInfoAsJsonObject(context, "", BuildPropertyUtils.getApkVersion(context), libSettings.getString(LibConfigs.PROVISION_TIME), context.getPackageName(), checkRequestObj);
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("clientState", libCussm.getCurrentState(checkRequestObj.getPrimaryKey()));
        JSONObject jsonObjectFromMap = UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "buildExtraInfoForCheckRequest");
        Iterator<String> keys = jsonObjectFromMap.keys();
        while (keys.hasNext()) {
            String next = keys.next();
            try {
                extraInfoAsJsonObject.put(next, jsonObjectFromMap.get(next));
            } catch (JSONException e) {
                Logger.error(Logger.OTALib_TAG, "buildExtraInfoForCheckRequest caught exception while getting key: " + next + " :" + e);
            }
        }
        return extraInfoAsJsonObject;
    }

    public static JSONObject getDeviceInfoAsJsonObject(CheckRequestObj checkRequestObj) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("manufacturer", checkRequestObj.getManufacturer());
        linkedHashMap.put("model", checkRequestObj.getModelId());
        linkedHashMap.put("product", checkRequestObj.getProduct());
        linkedHashMap.put("isPRC", Boolean.valueOf(checkRequestObj.getIsPRCDevice()));
        linkedHashMap.put("carrier", checkRequestObj.getCarrier());
        linkedHashMap.put("userLanguage", Locale.getDefault().toString());
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "getDeviceInfoAsJsonObject");
    }

    public static JSONObject getIdentityInfoAsJsonObject(String str) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(CDSUtils.IDTYPE, str);
            return jSONObject;
        } catch (JSONException e) {
            Logger.error(Logger.OTALib_TAG, "Failed to create JSON object for identityInfo" + e);
            return null;
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v3, types: [java.lang.Integer] */
    public static JSONObject getExtraInfoAsJsonObject(Context context, String str, int i, String str2, String str3, CheckRequestObj checkRequestObj) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("clientIdentity", "motorola-ota-client-app");
        linkedHashMap.put("brand", checkRequestObj.getColorId());
        linkedHashMap.put("buildDevice", checkRequestObj.getInternalName());
        linkedHashMap.put("otaSourceSha1", checkRequestObj.getContextKey());
        linkedHashMap.put("buildId", Long.valueOf(checkRequestObj.getSourceVersion()));
        linkedHashMap.put("buildDisplayId", checkRequestObj.getSourceVersion() + "_" + checkRequestObj.getProduct());
        linkedHashMap.put("network", str);
        String str4 = str2;
        if (str2 == null) {
            str4 = 0;
        }
        linkedHashMap.put("provisionedTime", str4);
        linkedHashMap.put("colorId", checkRequestObj.getColorId());
        linkedHashMap.put("apkPackageName", str3);
        linkedHashMap.put("apkVersion", Integer.valueOf(i));
        linkedHashMap.put("OtaLibVersion", Integer.valueOf((int) BuildConfig.VERSION_CODE));
        linkedHashMap.put("mobileModel", Build.MODEL);
        return UpgradeUtils.getJsonObjectFromMap(linkedHashMap, "BuildPropReader:ExtraInfo ");
    }

    public static String whoInitiated(PublicUtilityMethods.TRIGGER_BY trigger_by) {
        if (PublicUtilityMethods.TRIGGER_BY.user == trigger_by) {
            return "user initiated upgrade";
        }
        if (PublicUtilityMethods.TRIGGER_BY.setup == trigger_by) {
            return "setup initiated upgrade";
        }
        if (PublicUtilityMethods.TRIGGER_BY.pairing != trigger_by) {
            return "An Upgrade";
        }
        return "pairing initiated upgrade";
    }

    public void checkForDownloadDescriptor(final LibCussm libCussm, final ApplicationEnv.Database database, final Context context, final IOtaLibServiceCallBack iOtaLibServiceCallBack, final LibSettings libSettings, final CheckRequestObj checkRequestObj, String str) throws RemoteException {
        final UpdateSessionInfo sessionDetails = UpdateSessionInfo.getSessionDetails(libSettings, checkRequestObj.getPrimaryKey());
        if (resourceProgress.get()) {
            Logger.info(Logger.OTALib_TAG, "OTAUpgradeSource.checkForDownloadDescriptor: already sent request and waiting for response .. so return from here");
            return;
        }
        WebRequest webRequest = new WebRequest(ResourcesUrlConstructor.constructUrl(new UrlRequest(getMasterCloud(checkRequestObj), libSettings.getString(LibConfigs.DOWNLOAD_DESCRIPTOR_URL), libSettings.getString(LibConfigs.OTA_CONTEXT), checkRequestObj.getContextKey(), sessionDetails.getTrackingId(), libSettings.getString(LibConfigs.DOWNLOAD_DESCRIPTOR_HTTP_SECURE), libSettings.getString(LibConfigs.DOWNLOAD_DESCRIPTOR_TEST_URL))), libSettings.getInt(LibConfigs.DOWNLOAD_DESCRIPTOR_HTTP_RETRIES, 3), libSettings.getString(LibConfigs.DOWNLOAD_DESCRIPTOR_HTTP_METHOD), null, new WebRequestPayload(WebRequestPayloadType.string, ResourcesRequestBuilder.toJSONString(new ResourcesRequest(checkRequestObj.getAccsSerialNumber(), sessionDetails.getContentTimeSTamp(), getDeviceInfoAsJsonObject(checkRequestObj), getExtraInfoAsJsonObject(context, "", BuildPropertyUtils.getApkVersion(context), libSettings.getString(LibConfigs.PROVISION_TIME), context.getPackageName(), checkRequestObj), getIdentityInfoAsJsonObject(checkRequestObj.getAccsSerialNumber()), CDSUtils.IDTYPE, sessionDetails.getReportingTag(), str))), libSettings.getString(LibConfigs.CDS_HTTP_PROXY_HOST), libSettings.getInt(LibConfigs.CDS_HTTP_PROXY_PORT, -1));
        Logger.debug(Logger.OTALib_TAG, "Resource request " + webRequest.toString());
        WebService.call(context, webRequest, new ResponseHandler() { // from class: com.motorola.otalib.main.checkUpdate.CheckUpdateHandler.2
            @Override // com.motorola.otalib.cdsservice.ResponseHandler
            public void handleResponse(WebResponse webResponse) {
                try {
                    CheckUpdateHandler.this.handleResouresWSResponse(context, webResponse, sessionDetails.getTrackingId(), checkRequestObj.getPrimaryKey(), libCussm, database, iOtaLibServiceCallBack, libSettings);
                } catch (RemoteException unused) {
                }
            }
        }, new WebServiceRetryHandler(), null);
        resourceProgress.set(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleResouresWSResponse(Context context, WebResponse webResponse, String str, String str2, LibCussm libCussm, ApplicationEnv.Database database, IOtaLibServiceCallBack iOtaLibServiceCallBack, LibSettings libSettings) throws RemoteException {
        if (webResponse == null) {
            Logger.error(Logger.OTALib_TAG, "got null response for dd request from cds lib");
            libCussm.failProgress(context, str2, UpgradeUtils.DownloadStatus.STATUS_FAIL, "DD response error : got null response for dd request", ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
        } else if (webResponse.getStatusCode() == 0) {
            libCussm.failProgress(context, str2, UpgradeUtils.DownloadStatus.STATUS_FAIL, "no connection response for dd request from cds lib", ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
        } else if (200 != webResponse.getStatusCode()) {
            Logger.error(Logger.OTALib_TAG, "got error response for download descriptor request from cds lib");
            libCussm.failProgress(context, str2, UpgradeUtils.DownloadStatus.STATUS_FAIL, "DD response error : giving the descriptor request after retry count " + libSettings.getInt(LibConfigs.DOWNLOAD_DESCRIPTOR_HTTP_RETRIES, 3), ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
        } else {
            JSONObject convertJsonArraytoJsonObject = convertJsonArraytoJsonObject(webResponse.getPayload());
            if (convertJsonArraytoJsonObject == null) {
                libCussm.failProgress(context, str2, UpgradeUtils.DownloadStatus.STATUS_FAIL, "Error while parsing contentResources", ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
                return;
            }
            try {
                ContentResources from = ContentResourcesBuilder.from(convertJsonArraytoJsonObject);
                String trackingId = from.getTrackingId();
                if (str != null && !str.equals(trackingId)) {
                    Logger.error(Logger.OTALib_TAG, "BotaUpgradeSource:resource request and response trackingId mismatch, return");
                    return;
                }
                if (from.getProceed()) {
                    libSettings.setString(LibConfigs.DOWNLOAD_DESCRIPTOR, ContentResourcesBuilder.toJSONString(from));
                    libSettings.setLong(LibConfigs.DOWNLOAD_DESCRIPTOR_TIME, System.currentTimeMillis());
                    database.setState(str2, ApplicationEnv.PackageState.GettingPackage, null);
                } else {
                    libCussm.failProgress(context, str2, UpgradeUtils.DownloadStatus.STATUS_FAIL, "server cancelled the update", ErrorCodeMapper.KEY_OTA_CANCELED_BY_SERVER);
                }
                resourceProgress.set(false);
                libCussm.pleaseRunStateMachine(context);
            } catch (JSONException e) {
                Logger.error(Logger.OTALib_TAG, "error parsing download descriptor response: " + e);
                libCussm.failProgress(context, str2, UpgradeUtils.DownloadStatus.STATUS_FAIL, "error while parsing download descriptor response " + e, ErrorCodeMapper.KEY_ERROR_DD_RESPONSE);
            }
        }
    }

    public static JSONObject convertJsonArraytoJsonObject(JSONObject jSONObject) {
        if (jSONObject == null) {
            return null;
        }
        try {
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("proceed", jSONObject.getBoolean("proceed"));
            jSONObject2.putOpt("trackingId", jSONObject.getString("trackingId"));
            if (jSONObject.optJSONArray("contentResources") == null) {
                Logger.info(Logger.OTALib_TAG, "convertJsonArraytoJsonObject, contentResources is null");
                return jSONObject2;
            }
            JSONArray optJSONArray = jSONObject.optJSONArray("contentResources");
            for (int i = 0; i < optJSONArray.length(); i++) {
                JSONArray jSONArray = optJSONArray.getJSONObject(i).getJSONArray("tags");
                for (int i2 = 0; i2 < jSONArray.length(); i2++) {
                    String string = jSONArray.getString(i2);
                    if (string != null && NetworkUtils.isNetworkTagValid(string)) {
                        String string2 = optJSONArray.getJSONObject(i).getString("url");
                        String string3 = optJSONArray.getJSONObject(i).getString("headers");
                        int i3 = AnonymousClass3.$SwitchMap$com$motorola$otalib$cdsservice$utils$NetworkTags[NetworkTags.valueOf(string).ordinal()];
                        if (i3 == 1) {
                            jSONObject2.put("cellUrl", string2);
                            jSONObject2.put("cellHeaders", string3);
                        } else if (i3 == 2) {
                            jSONObject2.put("wifiUrl", string2);
                            jSONObject2.put("wifiHeaders", string3);
                        } else if (i3 == 3) {
                            jSONObject2.put("adminApnUrl", string2);
                            jSONObject2.put("adminApnHeaders", string3);
                        }
                    }
                }
            }
            Logger.verbose(Logger.OTALib_TAG, "downloadContent: " + jSONObject2.toString());
            return jSONObject2;
        } catch (Exception e) {
            Logger.error(Logger.OTALib_TAG, "Error in parsing contentResources response " + e);
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.otalib.main.checkUpdate.CheckUpdateHandler$3  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
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

    public static String getMasterCloud(CheckRequestObj checkRequestObj) {
        if (checkRequestObj.getIsPRCDevice()) {
            if (!checkRequestObj.getIsProductionDevice()) {
                return UpgradeUtils.CHINA_STAGING_SERVER;
            }
            return UpgradeUtils.CHINA_PRODUCTION_SERVER;
        } else if (!checkRequestObj.getIsProductionDevice()) {
            return UpgradeUtils.STAGING_SERVER;
        } else {
            return "moto-cds.appspot.com";
        }
    }

    public static String getUpgradeSource(PublicUtilityMethods.TRIGGER_BY trigger_by) {
        if (PublicUtilityMethods.TRIGGER_BY.user.name().equals(trigger_by.name())) {
            return "UPGRADED_VIA_PULL";
        }
        if (PublicUtilityMethods.TRIGGER_BY.pairing.name().equals(trigger_by.name())) {
            return "UPGRADED_VIA_PAIR";
        }
        if (PublicUtilityMethods.TRIGGER_BY.setup.name().equals(trigger_by.name())) {
            return "UPGRADED_VIA_INTIAL_SETUP";
        }
        return "UPGRADED_VIA_UNKNOWN_METHOD";
    }
}
