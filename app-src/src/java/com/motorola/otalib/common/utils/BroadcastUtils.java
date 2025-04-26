package com.motorola.otalib.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.motorola.otalib.common.CommonLogger;
import java.util.Iterator;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class BroadcastUtils {
    public static final boolean DBG = false;

    public static void registerLocalReceiver(Context context, BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
    }

    public static void unregisterLocalReceiver(Context context, BroadcastReceiver broadcastReceiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
    }

    public static void sendLocalBroadcast(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void log() {
        CommonLogger.i(CommonLogger.TAG, "A broadcast receiver is unregistered using LocalBroadcastManager.");
    }

    private static void log(Intent intent) {
        CommonLogger.i(CommonLogger.TAG, "Broadcast is sent using LocalBroadcastManager with: " + intent.getAction());
    }

    private static void log(IntentFilter intentFilter) {
        StringBuilder sb = new StringBuilder("A broadcast receiver is registered using LocalBroadcastManager with: ");
        Iterator<String> actionsIterator = intentFilter.actionsIterator();
        while (actionsIterator.hasNext()) {
            sb.append(actionsIterator.next());
            sb.append(", ");
        }
        CommonLogger.i(CommonLogger.TAG, sb.toString());
    }
}
