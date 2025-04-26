package com.motorola.otalib.cdsservice.utils;

import com.android.volley.toolbox.HurlStack;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class CDSHurlStack extends HurlStack {
    private final Proxy proxy;

    public CDSHurlStack(Proxy proxy) {
        this.proxy = proxy;
    }

    protected HttpURLConnection createConnection(URL url) throws IOException {
        if (this.proxy != null) {
            CDSLogger.d(CDSLogger.TAG, "CDSHurlStack.createConnection, proxy " + this.proxy.address());
            return (HttpURLConnection) url.openConnection(this.proxy);
        }
        return (HttpURLConnection) url.openConnection();
    }
}
