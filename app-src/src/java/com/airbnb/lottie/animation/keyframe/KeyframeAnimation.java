package com.airbnb.lottie.animation.keyframe;

import com.airbnb.lottie.value.Keyframe;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
abstract class KeyframeAnimation<T> extends BaseKeyframeAnimation<T, T> {
    /* JADX INFO: Access modifiers changed from: package-private */
    public KeyframeAnimation(List<? extends Keyframe<T>> list) {
        super(list);
    }
}
