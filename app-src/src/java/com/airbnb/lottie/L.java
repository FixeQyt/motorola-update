package com.airbnb.lottie;

import android.util.Log;
import androidx.core.os.TraceCompat;
import java.util.HashSet;
import java.util.Set;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class L {
    public static boolean DBG = false;
    private static final int MAX_DEPTH = 20;
    public static final String TAG = "LOTTIE";
    private static String[] sections;
    private static long[] startTimeNs;
    private static final Set<String> loggedMessages = new HashSet();
    private static boolean traceEnabled = false;
    private static int traceDepth = 0;
    private static int depthPastMaxDepth = 0;

    public static void debug(String str) {
        if (DBG) {
            Log.d(TAG, str);
        }
    }

    public static void warn(String str) {
        Set<String> set = loggedMessages;
        if (set.contains(str)) {
            return;
        }
        Log.w(TAG, str);
        set.add(str);
    }

    public static void setTraceEnabled(boolean z) {
        if (traceEnabled == z) {
            return;
        }
        traceEnabled = z;
        if (z) {
            sections = new String[20];
            startTimeNs = new long[20];
        }
    }

    public static void beginSection(String str) {
        if (traceEnabled) {
            int i = traceDepth;
            if (i == 20) {
                depthPastMaxDepth++;
                return;
            }
            sections[i] = str;
            startTimeNs[i] = System.nanoTime();
            TraceCompat.beginSection(str);
            traceDepth++;
        }
    }

    public static float endSection(String str) {
        int i = depthPastMaxDepth;
        if (i > 0) {
            depthPastMaxDepth = i - 1;
            return 0.0f;
        } else if (traceEnabled) {
            int i2 = traceDepth - 1;
            traceDepth = i2;
            if (i2 == -1) {
                throw new IllegalStateException("Can't end trace section. There are none.");
            }
            if (!str.equals(sections[i2])) {
                throw new IllegalStateException("Unbalanced trace call " + str + ". Expected " + sections[traceDepth] + ".");
            }
            TraceCompat.endSection();
            return ((float) (System.nanoTime() - startTimeNs[traceDepth])) / 1000000.0f;
        } else {
            return 0.0f;
        }
    }
}
