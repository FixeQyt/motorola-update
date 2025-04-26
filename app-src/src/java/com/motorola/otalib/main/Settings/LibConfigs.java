package com.motorola.otalib.main.Settings;

import com.motorola.otalib.cdsservice.utils.CDSUtils;
import com.motorola.otalib.common.settings.ISetting;
import java.util.ArrayList;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public enum LibConfigs implements ISetting {
    PROVISION_TIME("otalib.service.update.provisionTime", ""),
    CHECK_FOR_UPGRADE_URL("ota.service.update.check.url", CDSUtils.CHECK_BASE_URL),
    OTA_CONTEXT("ota.service.update.context", "ota"),
    CHECK_FOR_UPGRADE_HTTP_SECURE("ota.service.update.check.issecure", "true"),
    CDS_HTTP_PROXY_HOST("ota.service.update.cds.httpProxyHost", ""),
    CDS_HTTP_PROXY_PORT("ota.service.update.cds.httpProxyPort", "-1"),
    BACKOFF_VALUES("ota.service.update.backOffValues", "5000,15000,30000"),
    CHECK_FOR_UPGRADE_HTTP_METHOD("ota.service.update.check.httpmethod", "post"),
    DOWNLOAD_DESCRIPTOR_URL("ota.service.update.dd.url", CDSUtils.RESOURCES_BASE_URL),
    DOWNLOAD_DESCRIPTOR_HTTP_METHOD("ota.service.update.dd.httpmethod", "post"),
    DOWNLOAD_DESCRIPTOR_HTTP_SECURE("ota.service.update.dd.issecure", "true"),
    DOWNLOAD_DESCRIPTOR_HTTP_RETRIES("ota.service.update.dd.retries", "3"),
    UPGRADE_STATE_URL("ota.service.update.state.url", CDSUtils.STATE_BASE_URL),
    UPGRADE_STATE_HTTP_METHOD("ota.service.update.state.httpmethod", "post"),
    UPGRADE_STATE_HTTP_SECURE("ota.service.update.state.issecure", "true"),
    UPGRADE_STATE_HTTP_RETRIES("ota.service.update.state.retries", "3"),
    DOWNLOAD_HTTP_PROXY_HOST("ota.service.update.download.httpProxyHost", ""),
    DOWNLOAD_HTTP_PROXY_PORT("ota.service.update.download.httpProxyPort", "-1"),
    DISALLOWED_NETS("ota.service.update.disallowed_networks", ""),
    APPIID("com.motorola.ccc.ota.botaplugin.appid", "MGVKHZWFLNFPYQYLCTOVJLD5LURFMPKZ"),
    APPSECERET("com.motorola.ccc.ota.botaplugin.appsecret", "zdG4h4k2NOm6MSh"),
    CHECK_FOR_UPGRADE_TEST_URL("ota.service.update.checkrequest.test.url", ""),
    UPGRADE_STATE_TEST_URL("ota.service.update.staterequest.test.url", ""),
    DOWNLOAD_DESCRIPTOR_TEST_URL("ota.service.update.resourcerequest.test.url", ""),
    MAX_RETRY_COUNT_DL("ota.service.update.maxRetryCountDownload", "3"),
    OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS("com.motorola.otalib.OTA_LIB_DOWNLOAD_RETRY_ATTEMPTS", ""),
    OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS("com.motorola.otalib.OTA_LIB_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS", ""),
    DOWNLOAD_DESCRIPTOR("com.motorola.ccc.ota.botaplugin.download_descriptor", ""),
    DOWNLOAD_DESCRIPTOR_TIME("com.motorola.ccc.ota.botaplugin.download_descriptor_time", ""),
    UPDATE_SESSION_MAPPER("com.motorola.ccc.ota.UPDATE_SESSION_MAPPER", "");
    
    private String key;
    private String value;

    LibConfigs(String str, String str2) {
        this.key = str;
        this.value = str2;
    }

    public static List<String> returnAll() {
        LibConfigs[] values = values();
        ArrayList arrayList = new ArrayList();
        for (LibConfigs libConfigs : values) {
            arrayList.add(libConfigs.key());
        }
        return arrayList;
    }

    @Override // com.motorola.otalib.common.settings.ISetting
    public String key() {
        return this.key;
    }

    @Override // com.motorola.otalib.common.settings.ISetting
    public String value() {
        return this.value;
    }
}
