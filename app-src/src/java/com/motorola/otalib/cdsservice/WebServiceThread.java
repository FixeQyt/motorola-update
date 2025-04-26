package com.motorola.otalib.cdsservice;

import com.android.volley.RequestQueue;
import com.motorola.otalib.cdsservice.WebService;
import com.motorola.otalib.cdsservice.requestdataobjects.CheckRequest;
import com.motorola.otalib.cdsservice.requestdataobjects.builders.CheckRequestBuilder;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.cdsservice.utils.CDSUtils;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequest;
import com.motorola.otalib.cdsservice.webdataobjects.WebResponse;
import com.motorola.otalib.cdsservice.webdataobjects.builders.WebResponseBuilder;
import com.motorola.otalib.common.backoff.BackoffValueProvider;
import com.motorola.otalib.common.backoff.IncrementalBackoffValueProvider;
import com.motorola.otalib.common.metaData.CheckForUpgradeTriggeredBy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
class WebServiceThread extends Thread {
    private static final String backOffValues = "2000,5000,15000,30000,60000,300000,600000,600000,600000";
    private final RequestQueue mRequestQueue;
    private int mRetryCount;
    private String mUrl;
    private String mVerificationType;
    private final WebRequest request;
    private final InternalResponseHandler responseHandler;
    private final InternalRetryHandler retryHandler;
    private final WebService service;
    private final Integer threadToken;
    private Map<String, String> headers = new HashMap();
    private String mRedirectUrl = null;
    private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() { // from class: com.motorola.otalib.cdsservice.WebServiceThread.1
        @Override // java.util.concurrent.ThreadFactory
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "CDSWebServiceThread.RequestExecutor");
        }
    });
    private final BackoffValueProvider backOffProvider = new IncrementalBackoffValueProvider(backOffValues);

    /* JADX INFO: Access modifiers changed from: package-private */
    public WebServiceThread(WebRequest webRequest, InternalResponseHandler internalResponseHandler, InternalRetryHandler internalRetryHandler, WebService webService, Integer num, WebService.ServiceHandler serviceHandler, RequestQueue requestQueue) {
        this.request = webRequest;
        this.responseHandler = internalResponseHandler;
        this.retryHandler = internalRetryHandler;
        this.service = webService;
        this.threadToken = num;
        this.mRequestQueue = requestQueue;
    }

    @Override // java.lang.Thread, java.lang.Runnable
    public void run() {
        WebResponse waitForVolleyResponse;
        boolean shouldRetry;
        long nextTimeoutValue;
        do {
            try {
                String str = this.mRedirectUrl;
                if (str == null) {
                    str = this.request.getUrl();
                }
                this.mUrl = str;
                waitForVolleyResponse = waitForVolleyResponse(makeWebServiceCallWithVolley(this.request));
                shouldRetry = shouldRetry(waitForVolleyResponse, this.retryHandler);
                if (shouldRetry) {
                    int i = this.mRetryCount + 1;
                    this.mRetryCount = i;
                    if (i > this.request.getRetries()) {
                        CDSLogger.i(CDSLogger.TAG, "Retry count for this request url " + this.mUrl + " has been set to " + this.request.getRetries() + " so giving up after " + this.mRetryCount + " attempts (initial request inclusive)");
                        shouldRetry = false;
                    }
                }
                if (shouldRetry) {
                    if (waitForVolleyResponse != null && waitForVolleyResponse.getHeaders() != null) {
                        try {
                            nextTimeoutValue = Long.parseLong(waitForVolleyResponse.getHeaders().get("x-moto-backoff"));
                            if (nextTimeoutValue <= 0) {
                                nextTimeoutValue = this.backOffProvider.getNextTimeoutValue();
                            }
                        } catch (NumberFormatException unused) {
                            nextTimeoutValue = this.backOffProvider.getNextTimeoutValue();
                        }
                    } else {
                        nextTimeoutValue = this.backOffProvider.getNextTimeoutValue();
                    }
                    CDSLogger.i(CDSLogger.TAG, "Retry handler returned true; Retry web request after backoff time: " + nextTimeoutValue);
                    try {
                        this.backOffProvider.getTimeUnit().sleep(nextTimeoutValue);
                        continue;
                    } catch (InterruptedException unused2) {
                        continue;
                    }
                }
            } finally {
                this.service.appendWebResponse(this.threadToken);
                this.requestExecutor.shutdown();
            }
        } while (shouldRetry);
        checkAndInvokeResponseHandler(waitForVolleyResponse, this.responseHandler);
    }

    private boolean shouldRetry(WebResponse webResponse, InternalRetryHandler internalRetryHandler) {
        String str;
        if (webResponse == null) {
            CDSLogger.i(CDSLogger.TAG, "strange, response received as null will be retried based on retry count set for the request");
            return true;
        } else if (webResponse.getStatusCode() == 200) {
            return false;
        } else {
            if (webResponse.getStatusCode() == 1000 || webResponse.getStatusCode() == 0) {
                if (this.mUrl.contains(CDSUtils.CHECK_BASE_URL)) {
                    CheckRequest from = CheckRequestBuilder.from(this.request.getPayload().getData());
                    if (from == null) {
                        return false;
                    }
                    String triggerdBy = from.getTriggerdBy();
                    if (CheckForUpgradeTriggeredBy.user.name().equals(triggerdBy)) {
                        return false;
                    }
                    if (webResponse.getStatusCode() == 0 && CheckForUpgradeTriggeredBy.polling.name().equals(triggerdBy)) {
                        CDSLogger.d(CDSLogger.TAG, "No internet connection, pending polling is set");
                        return false;
                    }
                } else if ((this.mUrl.contains(CDSUtils.STATE_BASE_URL) || this.mUrl.contains(CDSUtils.RESOURCES_BASE_URL)) && webResponse.getStatusCode() == 0) {
                    CDSLogger.d(CDSLogger.TAG, "No internet connection, retry for state and resource request is not required");
                    return false;
                }
                return true;
            }
            int statusCode = webResponse.getStatusCode();
            if (statusCode >= 500 && statusCode <= 599) {
                CDSLogger.i(CDSLogger.TAG, "5XX series error , request will be backed off and will be retried");
                return true;
            } else if (statusCode == 401) {
                if ((internalRetryHandler == null || checkAndInvokeRetryHandler(webResponse, internalRetryHandler)) && webResponse.getHeaders() != null) {
                    String str2 = webResponse.getHeaders().get("x-moto-accept-verification-methods");
                    this.mVerificationType = str2;
                    if (str2 == null) {
                        CDSLogger.i(CDSLogger.TAG, "Unauthorized response with no verification-methods, can't proceed further");
                        return false;
                    }
                    CDSLogger.i(CDSLogger.TAG, "server sent verificationMethod as " + this.mVerificationType);
                }
                return false;
            } else if (statusCode == 403 || statusCode == 404) {
                CDSLogger.i(CDSLogger.TAG, statusCode + " error , request will be backed off and will be retried");
                return true;
            } else if ((statusCode == 302 || statusCode == 307) && webResponse.getHeaders() != null && (str = webResponse.getHeaders().get("Location")) != null) {
                CDSLogger.i(CDSLogger.TAG, "redirect error (" + statusCode + ")request will be backed off and will be retried with redirected url " + str);
                this.mRedirectUrl = str;
                return true;
            } else if (checkAndInvokeRetryHandler(webResponse, internalRetryHandler)) {
                CDSLogger.i(CDSLogger.TAG, "caller wants this request " + this.mUrl + " to be retried");
                return true;
            } else {
                return false;
            }
        }
    }

    private Future<JSONObject> makeWebServiceCallWithVolley(WebRequest webRequest) {
        try {
            return this.requestExecutor.submit(new WaitForResponseTask(this.service, this.mUrl, new JSONObject(webRequest.getPayload().getData()), webRequest.getHttpMethod(), webRequest.getQueryParams(), this.headers, this.mRequestQueue));
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "WebService.call() Request:" + this.mUrl + " caught json exception" + e);
            return null;
        }
    }

    private WebResponse waitForVolleyResponse(Future<JSONObject> future) {
        try {
            return WebResponseBuilder.from(future.get());
        } catch (InterruptedException e) {
            CDSLogger.e(CDSLogger.TAG, "WebService.call() Request:" + this.mUrl + " caught interrupted exception " + e);
            return null;
        } catch (ExecutionException e2) {
            CDSLogger.e(CDSLogger.TAG, "WebService.call() Request:" + this.mUrl + " caught execution exception " + e2);
            return null;
        }
    }

    private boolean checkAndInvokeRetryHandler(WebResponse webResponse, InternalRetryHandler internalRetryHandler) {
        return (webResponse == null || internalRetryHandler == null || !internalRetryHandler.invokeRetryHandler(webResponse)) ? false : true;
    }

    private void checkAndInvokeResponseHandler(WebResponse webResponse, InternalResponseHandler internalResponseHandler) {
        if (webResponse == null || internalResponseHandler == null) {
            return;
        }
        internalResponseHandler.invokeHandleResponse(webResponse);
    }
}
