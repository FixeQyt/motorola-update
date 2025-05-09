package com.airbnb.lottie.model;

import androidx.core.util.Pair;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class MutablePair<T> {
    T first;
    T second;

    public void set(T t, T t2) {
        this.first = t;
        this.second = t2;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair pair = (Pair) obj;
            return objectsEqual(pair.first, this.first) && objectsEqual(pair.second, this.second);
        }
        return false;
    }

    private static boolean objectsEqual(Object obj, Object obj2) {
        return obj == obj2 || (obj != null && obj.equals(obj2));
    }

    public int hashCode() {
        T t = this.first;
        int hashCode = t == null ? 0 : t.hashCode();
        T t2 = this.second;
        return hashCode ^ (t2 != null ? t2.hashCode() : 0);
    }

    public String toString() {
        return "Pair{" + String.valueOf(this.first) + SystemUpdateStatusUtils.SPACE + String.valueOf(this.second) + "}";
    }
}
