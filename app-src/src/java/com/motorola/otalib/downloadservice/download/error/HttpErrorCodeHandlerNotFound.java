package com.motorola.otalib.downloadservice.download.error;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class HttpErrorCodeHandlerNotFound extends Exception {
    private static final long serialVersionUID = 1;

    public HttpErrorCodeHandlerNotFound(int i) {
        super("Error Code :" + i + " does not have registered error handler");
    }
}
