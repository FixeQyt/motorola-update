package com.motorola.ccc.ota.installer.updaterEngine.download;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.installer.updaterEngine.common.InstallerUtilMethods;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.downloadservice.download.policy.ZeroRatedManager;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class UEDownloadRequestBuilder {
    private static final String NETWORK_ID_KEY = "NETWORK_ID";
    private static final String NETWORK_TYPE_KEY = "NETWORK_TYPE";
    private static final String USER_AGENT_KEY = "USER_AGENT";
    private static BotaSettings settings;
    private ConnectivityManager mCm;
    private TelephonyManager mTm;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public enum DownloadingChoices {
        WIFI_OK,
        WAN_OK,
        WIFI_ONLY,
        ROAMING,
        NO_WAN,
        WAN_DISALLOWED,
        ADMIN_OK
    }

    public UEDownloadRequestBuilder(BotaSettings botaSettings, ConnectivityManager connectivityManager, TelephonyManager telephonyManager) {
        this.mCm = connectivityManager;
        this.mTm = telephonyManager;
        settings = botaSettings;
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static class NetworkDetails {
        private String downloadUrl;
        private DownloadingChoices downloadingChoices;
        private String netHandler;
        private String networkType;

        NetworkDetails(DownloadingChoices downloadingChoices) {
            this.downloadingChoices = downloadingChoices;
        }

        NetworkDetails(String str, String str2, DownloadingChoices downloadingChoices, String str3) {
            this.downloadingChoices = downloadingChoices;
            this.downloadUrl = str;
            this.netHandler = str3;
            this.networkType = str2;
        }

        public String getDownloadUrl() {
            return this.downloadUrl;
        }

        public String getNetworkType() {
            return this.networkType;
        }

        public String getNetHandler() {
            return this.netHandler;
        }

        public DownloadingChoices getDownloadingChoices() {
            return this.downloadingChoices;
        }

        public static NetworkDetails buildAndReturnNetworkDetails(DownloadingChoices downloadingChoices, Network network) throws JSONException {
            Logger.debug("OtaApp", "buildAndReturnNetworkDetails: downloadingChoice " + downloadingChoices);
            JSONObject jSONObject = new JSONObject(UEDownloadRequestBuilder.settings.getString(Configs.DOWNLOAD_DESCRIPTOR));
            int i = AnonymousClass1.$SwitchMap$com$motorola$ccc$ota$installer$updaterEngine$download$UEDownloadRequestBuilder$DownloadingChoices[downloadingChoices.ordinal()];
            if (i != 1) {
                if (i != 2) {
                    if (i == 3) {
                        return new NetworkDetails(jSONObject.optString("adminApnUrl"), "ADMINAPN", downloadingChoices, String.valueOf(network.getNetworkHandle()));
                    }
                    return new NetworkDetails(downloadingChoices);
                }
                return new NetworkDetails(jSONObject.optString("cellUrl"), "CELLULAR", downloadingChoices, String.valueOf(network.getNetworkHandle()));
            }
            return new NetworkDetails(jSONObject.optString("wifiUrl"), "WIFI", downloadingChoices, String.valueOf(network.getNetworkHandle()));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.installer.updaterEngine.download.UEDownloadRequestBuilder$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$ccc$ota$installer$updaterEngine$download$UEDownloadRequestBuilder$DownloadingChoices;

        static {
            int[] iArr = new int[DownloadingChoices.values().length];
            $SwitchMap$com$motorola$ccc$ota$installer$updaterEngine$download$UEDownloadRequestBuilder$DownloadingChoices = iArr;
            try {
                iArr[DownloadingChoices.WIFI_OK.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$installer$updaterEngine$download$UEDownloadRequestBuilder$DownloadingChoices[DownloadingChoices.WAN_OK.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$installer$updaterEngine$download$UEDownloadRequestBuilder$DownloadingChoices[DownloadingChoices.ADMIN_OK.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public NetworkDetails fetchNonAdminapnNetworkDetails(Context context, String str, String str2, boolean z) throws JSONException {
        String wanTypeAsString = NetworkUtils.getWanTypeAsString(context, this.mCm, this.mTm);
        Logger.debug("OtaApp", "UEDownloadRequestBuilder.canIDownload: current wantype " + wanTypeAsString);
        Network activeNetwork = this.mCm.getActiveNetwork();
        if (activeNetwork == null) {
            return NetworkDetails.buildAndReturnNetworkDetails(DownloadingChoices.NO_WAN, activeNetwork);
        }
        if (NetworkUtils.hasNetwork(this.mCm) && NetworkUtils.isWifi(this.mCm)) {
            Logger.debug("OtaApp", "UEDownloadRequestBuilder.canIDownload:: on WiFi; we can continue the on-going download");
            return NetworkDetails.buildAndReturnNetworkDetails(DownloadingChoices.WIFI_OK, activeNetwork);
        } else if (UpdaterUtils.isWifiOnly()) {
            Logger.debug("OtaApp", "UEDownloadRequestBuilder.canIDownload:: WiFi-only package but not on WiFi; we cannot start a new download");
            return NetworkDetails.buildAndReturnNetworkDetails(DownloadingChoices.WIFI_ONLY, activeNetwork);
        } else if (!z && NetworkUtils.isRoaming(this.mCm)) {
            Logger.debug("OtaApp", "UEDownloadRequestBuilder.canIDownload:: downloading is discontinued due to roaming");
            return NetworkDetails.buildAndReturnNetworkDetails(DownloadingChoices.ROAMING, activeNetwork);
        } else if (NetworkUtils.hasNetwork(this.mCm) && !NetworkUtils.isWan(this.mCm)) {
            Logger.debug("OtaApp", "UEDownloadRequestBuilder.canIDownload: cannot continue download network seems to be down");
            return NetworkDetails.buildAndReturnNetworkDetails(DownloadingChoices.NO_WAN, activeNetwork);
        } else if (!InstallerUtilMethods.isDownloadAllowed(context, str2, this.mCm, this.mTm)) {
            Logger.debug("OtaApp", "UEDownloadRequestBuilder.canIDownload: cannot downloaded over a disallowed network " + wanTypeAsString);
            return NetworkDetails.buildAndReturnNetworkDetails(DownloadingChoices.WAN_DISALLOWED, activeNetwork);
        } else if (!TextUtils.isEmpty(str)) {
            Logger.debug("OtaApp", "UEDownloadRequestBuilder.canIDownload: cannot downloaded over non-AdminAPN N/W " + wanTypeAsString);
            return NetworkDetails.buildAndReturnNetworkDetails(DownloadingChoices.WAN_DISALLOWED, activeNetwork);
        } else {
            Logger.debug("OtaApp", "UEDownloadRequestBuilder.canIDownload: no restriction in continuing the on-going download");
            return NetworkDetails.buildAndReturnNetworkDetails(DownloadingChoices.WAN_OK, activeNetwork);
        }
    }

    public NetworkDetails fetchAdminapnNetworkDetails() throws JSONException {
        return NetworkDetails.buildAndReturnNetworkDetails(DownloadingChoices.ADMIN_OK, ZeroRatedManager.returnActiveAdminApnNetwork());
    }

    public DownloadRequestToUE build(NetworkDetails networkDetails, JSONObject jSONObject) throws DownloadBuilderException, JSONException {
        Logger.debug("OtaApp", "building UE download request");
        if (jSONObject == null) {
            throw new DownloadBuilderException("streamingData is null in metadata");
        }
        String downloadUrl = networkDetails.getDownloadUrl();
        long[] payloadOffsetAndSize = getPayloadOffsetAndSize(jSONObject);
        String[] headerKeyValuePair = getHeaderKeyValuePair(jSONObject, networkDetails);
        Logger.verbose("OtaApp", "Download url " + downloadUrl + " Offset value " + payloadOffsetAndSize[0] + " File size " + payloadOffsetAndSize[1]);
        return new DownloadRequestToUE(downloadUrl, payloadOffsetAndSize[0], payloadOffsetAndSize[1], headerKeyValuePair);
    }

    public long[] getPayloadOffsetAndSize(JSONObject jSONObject) throws DownloadBuilderException, JSONException {
        JSONObject optJSONObject;
        JSONObject optJSONObject2 = jSONObject.optJSONObject("additionalInfo");
        if (optJSONObject2 != null && (optJSONObject = optJSONObject2.optJSONObject("payload")) != null) {
            return new long[]{optJSONObject.getLong("offset"), optJSONObject.getLong("size")};
        }
        throw new DownloadBuilderException("StreamingUpdate.getOffsetValue: server did not sendpayload properties");
    }

    public String[] getHeaderKeyValuePair(JSONObject jSONObject, NetworkDetails networkDetails) throws DownloadBuilderException, JSONException {
        ArrayList arrayList = new ArrayList();
        JSONObject optJSONObject = jSONObject.optJSONObject("header");
        if (optJSONObject != null) {
            Iterator<String> keys = optJSONObject.keys();
            while (keys.hasNext()) {
                String next = keys.next();
                arrayList.add(next + "=" + optJSONObject.get(next));
            }
            arrayList.add("NETWORK_ID=" + networkDetails.getNetHandler());
            arrayList.add("NETWORK_TYPE=" + networkDetails.getNetworkType());
            arrayList.add("USER_AGENT=" + System.getProperty("http.agent"));
            if (UpdaterUtils.isSoftBankApn(OtaApplication.getGlobalContext())) {
                arrayList.add("PROXY_HOST=dmint.softbank.ne.jp");
                arrayList.add("PROXY_PORT=8080");
            }
            Logger.verbose("OtaApp", "Header key value pair" + arrayList.toString());
            return (String[]) arrayList.toArray(new String[arrayList.size()]);
        }
        throw new DownloadBuilderException("StreamingUpdate.getHeaderKeyValuePair:: null value came from server");
    }
}
