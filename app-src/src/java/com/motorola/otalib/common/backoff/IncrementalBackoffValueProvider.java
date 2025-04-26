package com.motorola.otalib.common.backoff;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class IncrementalBackoffValueProvider implements BackoffValueProvider {
    private static final TimeUnit backOffTimeUnit = TimeUnit.MILLISECONDS;
    private Iterator<Long> iterator;
    private final List<Long> timeoutValues;

    public IncrementalBackoffValueProvider(String str) {
        List<Long> parseTimeoutValues = parseTimeoutValues(str);
        this.timeoutValues = parseTimeoutValues;
        this.iterator = parseTimeoutValues.iterator();
    }

    private List<Long> parseTimeoutValues(String str) {
        LinkedList linkedList = new LinkedList();
        for (String str2 : str.split(",")) {
            try {
                linkedList.add(Long.valueOf(str2));
            } catch (NumberFormatException unused) {
            }
        }
        return linkedList;
    }

    @Override // com.motorola.otalib.common.backoff.BackoffValueProvider
    public long getNextTimeoutValue() {
        if (!this.iterator.hasNext()) {
            this.iterator = this.timeoutValues.iterator();
        }
        return this.iterator.next().longValue();
    }

    @Override // com.motorola.otalib.common.backoff.BackoffValueProvider
    public long getTimeOutValue(int i) {
        if (i < 0 || i >= this.timeoutValues.size()) {
            return -1L;
        }
        return this.timeoutValues.get(i).longValue();
    }

    @Override // com.motorola.otalib.common.backoff.BackoffValueProvider
    public TimeUnit getTimeUnit() {
        return backOffTimeUnit;
    }

    @Override // com.motorola.otalib.common.backoff.BackoffValueProvider
    public void reset() {
        this.iterator = this.timeoutValues.iterator();
    }
}
