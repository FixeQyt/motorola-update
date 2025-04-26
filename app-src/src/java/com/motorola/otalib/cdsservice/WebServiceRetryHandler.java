package com.motorola.otalib.cdsservice;

import com.motorola.otalib.cdsservice.webdataobjects.WebResponse;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WebServiceRetryHandler implements RetryHandler {
    @Override // com.motorola.otalib.cdsservice.RetryHandler
    public boolean retryRequest(WebResponse webResponse) {
        return webResponse != null && webResponse.getStatusCode() == 401;
    }
}
