package com.motorola.otalib.cdsservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.motorola.otalib.cdsservice.utils.CDSHurlStack;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.cdsservice.webdataobjects.WebRequest;
import com.motorola.otalib.cdsservice.webdataobjects.builders.WebRequestBuilder;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Random;
import org.json.JSONException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WebService extends Service {
    private static final String REQ_FIELDNAME = "REQ";
    private static final String RESPONSE_HANDLER_FIELDNAME = "RESP_HANDLER";
    private static final String RETRY_HANDLER_FIELDNAME = "RETRY_HANDLER";
    private Random randomNumber;
    private SparseArray<InternalRequest> requestResponseMapping;
    private ServiceHandler serviceHandler;
    private Looper serviceLooper;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum WHAT {
        CDS_WEB_SERVICE_REQUEST,
        CDS_WEB_SERVICE_RESPONSE_RECEIVED
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class InternalRequest {
        public WebRequest request;
        public RequestQueue requestQueue;
        public InternalResponseHandler responeHandler;
        public InternalRetryHandler retryHandler;
        public int startId;

        public InternalRequest(WebRequest webRequest, InternalResponseHandler internalResponseHandler, int i) {
            this.request = webRequest;
            this.responeHandler = internalResponseHandler;
            this.startId = i;
            this.requestQueue = null;
        }

        public InternalRequest(WebService webService, WebRequest webRequest, InternalResponseHandler internalResponseHandler, InternalRetryHandler internalRetryHandler, int i) {
            this(webRequest, internalResponseHandler, i);
            this.retryHandler = internalRetryHandler;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = AnonymousClass1.$SwitchMap$com$motorola$otalib$cdsservice$WebService$WHAT[WHAT.values()[message.what].ordinal()];
            if (i == 1) {
                InternalRequest internalRequest = (InternalRequest) message.obj;
                Integer valueOf = Integer.valueOf(WebService.this.randomNumber.nextInt());
                WebService webService = WebService.this;
                internalRequest.requestQueue = webService.createRequestQueue(internalRequest, webService);
                WebServiceThread webServiceThread = new WebServiceThread(internalRequest.request, internalRequest.responeHandler, internalRequest.retryHandler, WebService.this, valueOf, this, internalRequest.requestQueue);
                WebService.this.requestResponseMapping.put(valueOf.intValue(), internalRequest);
                webServiceThread.start();
            } else if (i != 2) {
            } else {
                Integer num = (Integer) message.obj;
                InternalRequest internalRequest2 = (InternalRequest) WebService.this.requestResponseMapping.get(num.intValue());
                if (internalRequest2 != null) {
                    CDSLogger.v(CDSLogger.TAG, "Removing request :" + internalRequest2.request.getUrl() + " from queue ");
                    WebService.this.requestResponseMapping.remove(num.intValue());
                    RequestQueue requestQueue = internalRequest2.requestQueue;
                    if (requestQueue != null) {
                        requestQueue.cancelAll("OTA");
                        CDSLogger.d(CDSLogger.TAG, "Canceling all requests from request queue");
                        requestQueue.stop();
                    }
                }
                if (WebService.this.requestResponseMapping.size() == 0) {
                    WebService.this.stopSelf(internalRequest2.startId);
                }
            }
        }
    }

    /* renamed from: com.motorola.otalib.cdsservice.WebService$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$otalib$cdsservice$WebService$WHAT;

        static {
            int[] iArr = new int[WHAT.values().length];
            $SwitchMap$com$motorola$otalib$cdsservice$WebService$WHAT = iArr;
            try {
                iArr[WHAT.CDS_WEB_SERVICE_REQUEST.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$otalib$cdsservice$WebService$WHAT[WHAT.CDS_WEB_SERVICE_RESPONSE_RECEIVED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public static void call(Context context, WebRequest webRequest, ResponseHandler responseHandler, Class<?> cls) {
        if (cls == null) {
            cls = WebService.class;
        }
        call(context, webRequest, responseHandler, null, cls);
    }

    public static void call(Context context, WebRequest webRequest, ResponseHandler responseHandler, RetryHandler retryHandler, Class<?> cls) {
        try {
            Intent intent = new Intent(context, WebService.class);
            intent.putExtra(REQ_FIELDNAME, WebRequestBuilder.toJSONString(webRequest));
            intent.putExtra(RESPONSE_HANDLER_FIELDNAME, new InternalResponseHandler(responseHandler));
            intent.putExtra(RETRY_HANDLER_FIELDNAME, retryHandler != null ? new InternalRetryHandler(retryHandler) : null);
            context.startService(intent);
        } catch (JSONException e) {
            CDSLogger.e(CDSLogger.TAG, "Caught json exception while sending request to WebService" + e);
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        CDSLogger.d(CDSLogger.TAG, "Starting webservice android service");
        this.randomNumber = new Random();
        this.requestResponseMapping = new SparseArray<>();
        HandlerThread handlerThread = new HandlerThread("WebService.ServiceHandlerThread");
        handlerThread.start();
        this.serviceLooper = handlerThread.getLooper();
        this.serviceHandler = new ServiceHandler(this.serviceLooper);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        WebRequest from = WebRequestBuilder.from(intent.getStringExtra(REQ_FIELDNAME));
        InternalResponseHandler internalResponseHandler = (InternalResponseHandler) intent.getParcelableExtra(RESPONSE_HANDLER_FIELDNAME);
        InternalRetryHandler internalRetryHandler = (InternalRetryHandler) intent.getParcelableExtra(RETRY_HANDLER_FIELDNAME);
        CDSLogger.v(CDSLogger.TAG, "Received web service call for request :" + from.getUrl());
        appendWebRequest(new InternalRequest(this, from, internalResponseHandler, internalRetryHandler, i2));
        return 2;
    }

    @Override // android.app.Service
    public void onDestroy() {
        CDSLogger.d(CDSLogger.TAG, "Stopping webservice android service");
        this.serviceLooper.quit();
    }

    synchronized void appendWebRequest(InternalRequest internalRequest) {
        CDSLogger.d(CDSLogger.TAG, "appending web service request to serviceHandler");
        Message obtainMessage = this.serviceHandler.obtainMessage();
        obtainMessage.what = WHAT.CDS_WEB_SERVICE_REQUEST.ordinal();
        obtainMessage.obj = internalRequest;
        this.serviceHandler.sendMessage(obtainMessage);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public synchronized void appendWebResponse(Integer num) {
        CDSLogger.d(CDSLogger.TAG, "appending web service response to serviceHandler");
        Message obtainMessage = this.serviceHandler.obtainMessage();
        obtainMessage.what = WHAT.CDS_WEB_SERVICE_RESPONSE_RECEIVED.ordinal();
        obtainMessage.obj = num;
        this.serviceHandler.sendMessage(obtainMessage);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public RequestQueue createRequestQueue(InternalRequest internalRequest, Context context) {
        Proxy constructProxy = constructProxy(internalRequest.request.getProxyHost(), internalRequest.request.getProxyPort());
        if (constructProxy != null) {
            return Volley.newRequestQueue(context, new CDSHurlStack(constructProxy));
        }
        return Volley.newRequestQueue(context);
    }

    private Proxy constructProxy(String str, int i) {
        if (!TextUtils.isEmpty(str) && i > 0) {
            try {
                return new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(str, i));
            } catch (Exception e) {
                CDSLogger.e(CDSLogger.TAG, " Exception while constructing the proxy from webrequest: " + e);
            }
        }
        return null;
    }
}
