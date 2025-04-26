package com.motorola.otalib.downloadservice.download;

import android.text.TextUtils;
import com.motorola.otalib.downloadservice.utils.DownloadServiceLogger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class HttpUrlBuilder {
    private HttpURLConnection createConnection(String str, int i, URL url) throws IOException {
        Proxy constructProxy = constructProxy(str, i);
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "HttpUrlBuilder:createConnection:proxy=" + constructProxy);
        if (constructProxy != null) {
            return (HttpURLConnection) url.openConnection(constructProxy);
        }
        return (HttpURLConnection) url.openConnection();
    }

    public HttpURLConnection openConnection(String str, int i, URL url, int i2, int i3, SSLSocketFactory sSLSocketFactory) throws IOException {
        HttpURLConnection createConnection = createConnection(str, i, url);
        createConnection.setConnectTimeout(i2);
        createConnection.setReadTimeout(i3);
        createConnection.setUseCaches(false);
        createConnection.setInstanceFollowRedirects(false);
        if ("https".equals(url.getProtocol()) && sSLSocketFactory != null) {
            ((HttpsURLConnection) createConnection).setSSLSocketFactory(sSLSocketFactory);
        }
        return createConnection;
    }

    public HttpURLConnection reopenConnection(String str, int i, URL url, HttpURLConnection httpURLConnection) throws IOException {
        return openConnection(str, i, url, httpURLConnection.getConnectTimeout(), httpURLConnection.getReadTimeout(), ((HttpsURLConnection) httpURLConnection).getSSLSocketFactory());
    }

    private Proxy constructProxy(String str, int i) {
        if (!TextUtils.isEmpty(str) && i > 0) {
            try {
                return new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(str, i));
            } catch (Exception e) {
                DownloadServiceLogger.e(DownloadServiceLogger.TAG, " Exception while constructing the proxy from webrequest: " + e);
            }
        }
        return null;
    }
}
