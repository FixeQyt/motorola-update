package com.motorola.otalib.cdsservice.UrlConstructor;

import android.net.Uri;
import android.text.TextUtils;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.otalib.cdsservice.requestdataobjects.UrlRequest;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.common.utils.BuildPropertyUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CheckUrlConstructor {
    public static String constructUrl(UrlRequest urlRequest) {
        if (!TextUtils.isEmpty(urlRequest.getTestUrl()) && BuildPropertyUtils.isDogfoodDevice()) {
            CDSLogger.v(CDSLogger.TAG, "Check Request test url is " + urlRequest.getTestUrl());
            return urlRequest.getTestUrl();
        }
        StringBuilder sb = new StringBuilder();
        if ("true".equalsIgnoreCase(urlRequest.getIsSecure())) {
            sb.append("https://");
        } else {
            sb.append("http://");
        }
        sb.append(urlRequest.getServerUrl());
        sb.append(FileUtils.SD_CARD_DIR);
        sb.append(urlRequest.getBaseUrl());
        sb.append("/ctx/");
        sb.append(urlRequest.getContext());
        sb.append("/key/");
        sb.append(Uri.encode(urlRequest.getContextKey()));
        CDSLogger.v(CDSLogger.TAG, "Check Request getUrl(): url is " + sb.toString());
        return sb.toString();
    }
}
