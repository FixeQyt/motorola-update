package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import androidx.collection.LongSparseArray;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.value.LottieValueCallback;
import java.util.ArrayList;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class CompositionLayer extends BaseLayer {
    private Boolean hasMasks;
    private Boolean hasMatte;
    private final List<BaseLayer> layers;
    private final RectF newClipRect;
    private final RectF rect;
    private BaseKeyframeAnimation<Float, Float> timeRemapping;

    public CompositionLayer(LottieDrawable lottieDrawable, Layer layer, List<Layer> list, LottieComposition lottieComposition) {
        super(lottieDrawable, layer);
        int i;
        BaseLayer baseLayer;
        this.layers = new ArrayList();
        this.rect = new RectF();
        this.newClipRect = new RectF();
        AnimatableFloatValue timeRemapping = layer.getTimeRemapping();
        if (timeRemapping != null) {
            BaseKeyframeAnimation<Float, Float> createAnimation = timeRemapping.createAnimation();
            this.timeRemapping = createAnimation;
            addAnimation(createAnimation);
            this.timeRemapping.addUpdateListener(this);
        } else {
            this.timeRemapping = null;
        }
        LongSparseArray longSparseArray = new LongSparseArray(lottieComposition.getLayers().size());
        int size = list.size() - 1;
        BaseLayer baseLayer2 = null;
        while (true) {
            if (size < 0) {
                break;
            }
            Layer layer2 = list.get(size);
            BaseLayer forModel = BaseLayer.forModel(layer2, lottieDrawable, lottieComposition);
            if (forModel != null) {
                longSparseArray.put(forModel.getLayerModel().getId(), forModel);
                if (baseLayer2 != null) {
                    baseLayer2.setMatteLayer(forModel);
                    baseLayer2 = null;
                } else {
                    this.layers.add(0, forModel);
                    int i2 = AnonymousClass1.$SwitchMap$com$airbnb$lottie$model$layer$Layer$MatteType[layer2.getMatteType().ordinal()];
                    if (i2 == 1 || i2 == 2) {
                        baseLayer2 = forModel;
                    }
                }
            }
            size--;
        }
        for (i = 0; i < longSparseArray.size(); i++) {
            BaseLayer baseLayer3 = (BaseLayer) longSparseArray.get(longSparseArray.keyAt(i));
            if (baseLayer3 != null && (baseLayer = (BaseLayer) longSparseArray.get(baseLayer3.getLayerModel().getParentId())) != null) {
                baseLayer3.setParentLayer(baseLayer);
            }
        }
    }

    /* renamed from: com.airbnb.lottie.model.layer.CompositionLayer$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$airbnb$lottie$model$layer$Layer$MatteType;

        static {
            int[] iArr = new int[Layer.MatteType.values().length];
            $SwitchMap$com$airbnb$lottie$model$layer$Layer$MatteType = iArr;
            try {
                iArr[Layer.MatteType.ADD.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$airbnb$lottie$model$layer$Layer$MatteType[Layer.MatteType.INVERT.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer
    void drawLayer(Canvas canvas, Matrix matrix, int i) {
        L.beginSection("CompositionLayer#draw");
        canvas.save();
        this.newClipRect.set(0.0f, 0.0f, this.layerModel.getPreCompWidth(), this.layerModel.getPreCompHeight());
        matrix.mapRect(this.newClipRect);
        for (int size = this.layers.size() - 1; size >= 0; size--) {
            if (!this.newClipRect.isEmpty() ? canvas.clipRect(this.newClipRect) : true) {
                this.layers.get(size).draw(canvas, matrix, i);
            }
        }
        canvas.restore();
        L.endSection("CompositionLayer#draw");
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer, com.airbnb.lottie.animation.content.DrawingContent
    public void getBounds(RectF rectF, Matrix matrix, boolean z) {
        super.getBounds(rectF, matrix, z);
        for (int size = this.layers.size() - 1; size >= 0; size--) {
            this.rect.set(0.0f, 0.0f, 0.0f, 0.0f);
            this.layers.get(size).getBounds(this.rect, this.boundsMatrix, true);
            rectF.union(this.rect);
        }
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer
    public void setProgress(float f) {
        super.setProgress(f);
        if (this.timeRemapping != null) {
            f = (this.timeRemapping.getValue().floatValue() * 1000.0f) / this.lottieDrawable.getComposition().getDuration();
        }
        if (this.layerModel.getTimeStretch() != 0.0f) {
            f /= this.layerModel.getTimeStretch();
        }
        float startProgress = f - this.layerModel.getStartProgress();
        for (int size = this.layers.size() - 1; size >= 0; size--) {
            this.layers.get(size).setProgress(startProgress);
        }
    }

    public boolean hasMasks() {
        if (this.hasMasks == null) {
            for (int size = this.layers.size() - 1; size >= 0; size--) {
                BaseLayer baseLayer = this.layers.get(size);
                if (baseLayer instanceof ShapeLayer) {
                    if (baseLayer.hasMasksOnThisLayer()) {
                        this.hasMasks = true;
                        return true;
                    }
                } else if ((baseLayer instanceof CompositionLayer) && ((CompositionLayer) baseLayer).hasMasks()) {
                    this.hasMasks = true;
                    return true;
                }
            }
            this.hasMasks = false;
        }
        return this.hasMasks.booleanValue();
    }

    public boolean hasMatte() {
        if (this.hasMatte == null) {
            if (hasMatteOnThisLayer()) {
                this.hasMatte = true;
                return true;
            }
            for (int size = this.layers.size() - 1; size >= 0; size--) {
                if (this.layers.get(size).hasMatteOnThisLayer()) {
                    this.hasMatte = true;
                    return true;
                }
            }
            this.hasMatte = false;
        }
        return this.hasMatte.booleanValue();
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer
    protected void resolveChildKeyPath(KeyPath keyPath, int i, List<KeyPath> list, KeyPath keyPath2) {
        for (int i2 = 0; i2 < this.layers.size(); i2++) {
            this.layers.get(i2).resolveKeyPath(keyPath, i, list, keyPath2);
        }
    }

    @Override // com.airbnb.lottie.model.layer.BaseLayer, com.airbnb.lottie.model.KeyPathElement
    public <T> void addValueCallback(T t, LottieValueCallback<T> lottieValueCallback) {
        super.addValueCallback(t, lottieValueCallback);
        if (t == LottieProperty.TIME_REMAP) {
            if (lottieValueCallback == null) {
                this.timeRemapping = null;
                return;
            }
            ValueCallbackKeyframeAnimation valueCallbackKeyframeAnimation = new ValueCallbackKeyframeAnimation(lottieValueCallback);
            this.timeRemapping = valueCallbackKeyframeAnimation;
            addAnimation(valueCallbackKeyframeAnimation);
        }
    }
}
