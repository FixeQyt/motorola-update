package com.airbnb.lottie.parser;

import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.R;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePathValue;
import com.airbnb.lottie.model.animatable.AnimatableScaleValue;
import com.airbnb.lottie.model.animatable.AnimatableSplitDimensionPathValue;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.value.Keyframe;
import com.airbnb.lottie.value.ScaleXY;
import java.io.IOException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class AnimatableTransformParser {
    private AnimatableTransformParser() {
    }

    public static AnimatableTransform parse(JsonReader jsonReader, LottieComposition lottieComposition) throws IOException {
        boolean z = jsonReader.peek() == JsonToken.BEGIN_OBJECT;
        if (z) {
            jsonReader.beginObject();
        }
        AnimatableFloatValue animatableFloatValue = null;
        AnimatablePathValue animatablePathValue = null;
        AnimatableValue<PointF, PointF> animatableValue = null;
        AnimatableScaleValue animatableScaleValue = null;
        AnimatableFloatValue animatableFloatValue2 = null;
        AnimatableFloatValue animatableFloatValue3 = null;
        AnimatableIntegerValue animatableIntegerValue = null;
        AnimatableFloatValue animatableFloatValue4 = null;
        AnimatableFloatValue animatableFloatValue5 = null;
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            nextName.hashCode();
            char c = 65535;
            switch (nextName.hashCode()) {
                case R.styleable.AppCompatTheme_spinnerDropDownItemStyle /* 97 */:
                    if (nextName.equals("a")) {
                        c = 0;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_toolbarStyle /* 111 */:
                    if (nextName.equals("o")) {
                        c = 1;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_tooltipForegroundColor /* 112 */:
                    if (nextName.equals("p")) {
                        c = 2;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_viewInflaterClass /* 114 */:
                    if (nextName.equals("r")) {
                        c = 3;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_windowActionBar /* 115 */:
                    if (nextName.equals("s")) {
                        c = 4;
                        break;
                    }
                    break;
                case 3242:
                    if (nextName.equals("eo")) {
                        c = 5;
                        break;
                    }
                    break;
                case 3656:
                    if (nextName.equals("rz")) {
                        c = 6;
                        break;
                    }
                    break;
                case 3662:
                    if (nextName.equals("sa")) {
                        c = 7;
                        break;
                    }
                    break;
                case 3672:
                    if (nextName.equals("sk")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 3676:
                    if (nextName.equals("so")) {
                        c = '\t';
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        if (jsonReader.nextName().equals("k")) {
                            animatablePathValue = AnimatablePathValueParser.parse(jsonReader, lottieComposition);
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    jsonReader.endObject();
                    continue;
                case 1:
                    animatableIntegerValue = AnimatableValueParser.parseInteger(jsonReader, lottieComposition);
                    continue;
                case 2:
                    animatableValue = AnimatablePathValueParser.parseSplitPath(jsonReader, lottieComposition);
                    continue;
                case 3:
                    break;
                case 4:
                    animatableScaleValue = AnimatableValueParser.parseScale(jsonReader, lottieComposition);
                    continue;
                case 5:
                    animatableFloatValue5 = AnimatableValueParser.parseFloat(jsonReader, lottieComposition, false);
                    continue;
                case 6:
                    lottieComposition.addWarning("Lottie doesn't support 3D layers.");
                    break;
                case 7:
                    animatableFloatValue3 = AnimatableValueParser.parseFloat(jsonReader, lottieComposition, false);
                    continue;
                case '\b':
                    animatableFloatValue2 = AnimatableValueParser.parseFloat(jsonReader, lottieComposition, false);
                    continue;
                case '\t':
                    animatableFloatValue4 = AnimatableValueParser.parseFloat(jsonReader, lottieComposition, false);
                    continue;
                default:
                    jsonReader.skipValue();
                    continue;
            }
            AnimatableFloatValue parseFloat = AnimatableValueParser.parseFloat(jsonReader, lottieComposition, false);
            if (parseFloat.getKeyframes().isEmpty()) {
                parseFloat.getKeyframes().add(new Keyframe(lottieComposition, Float.valueOf(0.0f), Float.valueOf(0.0f), null, 0.0f, Float.valueOf(lottieComposition.getEndFrame())));
            } else if (((Keyframe) parseFloat.getKeyframes().get(0)).startValue == 0) {
                parseFloat.getKeyframes().set(0, new Keyframe(lottieComposition, Float.valueOf(0.0f), Float.valueOf(0.0f), null, 0.0f, Float.valueOf(lottieComposition.getEndFrame())));
            }
            animatableFloatValue = parseFloat;
        }
        if (z) {
            jsonReader.endObject();
        }
        AnimatablePathValue animatablePathValue2 = isAnchorPointIdentity(animatablePathValue) ? null : animatablePathValue;
        if (isPositionIdentity(animatableValue)) {
            animatableValue = null;
        }
        return new AnimatableTransform(animatablePathValue2, animatableValue, isScaleIdentity(animatableScaleValue) ? null : animatableScaleValue, isRotationIdentity(animatableFloatValue) ? null : animatableFloatValue, animatableIntegerValue, animatableFloatValue4, animatableFloatValue5, isSkewIdentity(animatableFloatValue2) ? null : animatableFloatValue2, isSkewAngleIdentity(animatableFloatValue3) ? null : animatableFloatValue3);
    }

    private static boolean isAnchorPointIdentity(AnimatablePathValue animatablePathValue) {
        return animatablePathValue == null || (animatablePathValue.isStatic() && animatablePathValue.getKeyframes().get(0).startValue.equals(0.0f, 0.0f));
    }

    private static boolean isPositionIdentity(AnimatableValue<PointF, PointF> animatableValue) {
        return animatableValue == null || (!(animatableValue instanceof AnimatableSplitDimensionPathValue) && animatableValue.isStatic() && animatableValue.getKeyframes().get(0).startValue.equals(0.0f, 0.0f));
    }

    private static boolean isRotationIdentity(AnimatableFloatValue animatableFloatValue) {
        return animatableFloatValue == null || (animatableFloatValue.isStatic() && ((Float) ((Keyframe) animatableFloatValue.getKeyframes().get(0)).startValue).floatValue() == 0.0f);
    }

    private static boolean isScaleIdentity(AnimatableScaleValue animatableScaleValue) {
        return animatableScaleValue == null || (animatableScaleValue.isStatic() && ((ScaleXY) ((Keyframe) animatableScaleValue.getKeyframes().get(0)).startValue).equals(1.0f, 1.0f));
    }

    private static boolean isSkewIdentity(AnimatableFloatValue animatableFloatValue) {
        return animatableFloatValue == null || (animatableFloatValue.isStatic() && ((Float) ((Keyframe) animatableFloatValue.getKeyframes().get(0)).startValue).floatValue() == 0.0f);
    }

    private static boolean isSkewAngleIdentity(AnimatableFloatValue animatableFloatValue) {
        return animatableFloatValue == null || (animatableFloatValue.isStatic() && ((Float) ((Keyframe) animatableFloatValue.getKeyframes().get(0)).startValue).floatValue() == 0.0f);
    }
}
