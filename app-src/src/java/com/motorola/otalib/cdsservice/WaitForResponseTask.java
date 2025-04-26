package com.motorola.otalib.cdsservice;

import android.content.Context;
import android.net.ConnectivityManager;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.motorola.otalib.cdsservice.responsedataobjects.builders.HashMapBuilder;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.common.utils.NetworkUtils;
import java.util.Map;
import java.util.concurrent.Callable;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WaitForResponseTask implements Callable<JSONObject> {
    private final Context mContext;
    private final Map<String, String> mHeaders;
    private final String mHttpMethod;
    private final Map<String, String> mQueryParams;
    private final JSONObject mRequestBody;
    private final RequestQueue mRequestQueue;
    private final String mUrl;

    public WaitForResponseTask(Context context, String str, JSONObject jSONObject, String str2, Map<String, String> map, Map<String, String> map2, RequestQueue requestQueue) {
        this.mContext = context;
        this.mUrl = str;
        this.mRequestBody = jSONObject;
        this.mHttpMethod = str2;
        this.mQueryParams = map;
        this.mHeaders = map2;
        this.mRequestQueue = requestQueue;
    }

    @Override // java.util.concurrent.Callable
    public JSONObject call() throws Exception {
        Object obj = new Object();
        InternalResponseReceiver internalResponseReceiver = new InternalResponseReceiver(obj);
        synchronized (obj) {
            try {
                internalResponseReceiver.executeRequestUsingVolley(this.mContext, this.mUrl, this.mRequestBody, this.mHttpMethod, this.mQueryParams, this.mHeaders);
                obj.wait();
            } catch (InterruptedException e) {
                throw new Exception(e.getMessage());
            }
        }
        return internalResponseReceiver.getResponse();
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class InternalResponseReceiver {
        private JSONObject volleyResponse = null;
        Object waitObject;

        InternalResponseReceiver(Object obj) {
            this.waitObject = obj;
        }

        public void executeRequestUsingVolley(final Context context, String str, JSONObject jSONObject, String str2, final Map<String, String> map, final Map<String, String> map2) {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(1, str, jSONObject, new Response.Listener<JSONObject>() { // from class: com.motorola.otalib.cdsservice.WaitForResponseTask.InternalResponseReceiver.1
                public void onResponse(JSONObject jSONObject2) {
                    CDSLogger.d(CDSLogger.TAG, "success response : " + jSONObject2.toString());
                    try {
                        InternalResponseReceiver.this.volleyResponse = new JSONObject().put("statusCode", 200).put("payload", jSONObject2).put("headers", (Object) null);
                    } catch (Exception e) {
                        InternalResponseReceiver.this.volleyResponse = null;
                        CDSLogger.e(CDSLogger.TAG, "error while constructing success response " + e);
                    }
                    synchronized (InternalResponseReceiver.this.waitObject) {
                        InternalResponseReceiver.this.waitObject.notify();
                    }
                }
            }, new Response.ErrorListener() { // from class: com.motorola.otalib.cdsservice.WaitForResponseTask.InternalResponseReceiver.2
                public void onErrorResponse(VolleyError volleyError) {
                    CDSLogger.i(CDSLogger.TAG, "failure response with status code :" + volleyError.toString());
                    try {
                        InternalResponseReceiver.this.volleyResponse = new JSONObject();
                        if (volleyError.networkResponse != null) {
                            InternalResponseReceiver.this.volleyResponse.put("statusCode", volleyError.networkResponse.statusCode);
                            InternalResponseReceiver.this.volleyResponse.put("headers", HashMapBuilder.toJSONObject(volleyError.networkResponse.headers));
                        } else {
                            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                            if (volleyError == null || !volleyError.toString().contains("NoConnectionError") || NetworkUtils.hasNetwork(connectivityManager)) {
                                InternalResponseReceiver.this.volleyResponse.put("statusCode", 1000);
                            } else {
                                InternalResponseReceiver.this.volleyResponse.put("statusCode", 0);
                            }
                            InternalResponseReceiver.this.volleyResponse.put("headers", (Object) null);
                        }
                        InternalResponseReceiver.this.volleyResponse.put("payload", (Object) null);
                    } catch (Exception e) {
                        CDSLogger.e(CDSLogger.TAG, "error while constructing failure response " + e);
                    }
                    synchronized (InternalResponseReceiver.this.waitObject) {
                        InternalResponseReceiver.this.waitObject.notify();
                    }
                }
            }) { // from class: com.motorola.otalib.cdsservice.WaitForResponseTask.InternalResponseReceiver.3
                public Map<String, String> getHeaders() {
                    return map2;
                }

                public Map<String, String> getParams() {
                    return map;
                }
            };
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(60000, 0, 1.0f));
            jsonObjectRequest.setShouldCache(false);
            jsonObjectRequest.setTag("OTA");
            WaitForResponseTask.this.mRequestQueue.add(jsonObjectRequest);
        }

        public JSONObject getResponse() {
            return this.volleyResponse;
        }
    }
}
