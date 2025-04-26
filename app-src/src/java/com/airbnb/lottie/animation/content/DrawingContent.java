package com.airbnb.lottie.animation.content;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public interface DrawingContent extends Content {
    void draw(Canvas canvas, Matrix matrix, int i);

    void getBounds(RectF rectF, Matrix matrix, boolean z);
}
