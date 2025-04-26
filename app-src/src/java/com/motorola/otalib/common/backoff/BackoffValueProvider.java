package com.motorola.otalib.common.backoff;

import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public interface BackoffValueProvider {
    long getNextTimeoutValue();

    long getTimeOutValue(int i);

    TimeUnit getTimeUnit();

    void reset();
}
