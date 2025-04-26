package com.airbnb.lottie.parser;

import android.util.JsonReader;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.R;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.content.GradientStroke;
import com.airbnb.lottie.model.content.GradientType;
import com.airbnb.lottie.model.content.ShapeStroke;
import java.io.IOException;
import java.util.ArrayList;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class GradientStrokeParser {
    private GradientStrokeParser() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public static GradientStroke parse(JsonReader jsonReader, LottieComposition lottieComposition) throws IOException {
        char c;
        ArrayList arrayList = new ArrayList();
        float f = 0.0f;
        String str = null;
        GradientType gradientType = null;
        AnimatableGradientColorValue animatableGradientColorValue = null;
        AnimatableIntegerValue animatableIntegerValue = null;
        AnimatablePointValue animatablePointValue = null;
        AnimatablePointValue animatablePointValue2 = null;
        AnimatableFloatValue animatableFloatValue = null;
        ShapeStroke.LineCapType lineCapType = null;
        ShapeStroke.LineJoinType lineJoinType = null;
        AnimatableFloatValue animatableFloatValue2 = null;
        boolean z = false;
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            nextName.hashCode();
            boolean z2 = z;
            AnimatableFloatValue animatableFloatValue3 = animatableFloatValue2;
            float f2 = f;
            switch (nextName.hashCode()) {
                case R.styleable.AppCompatTheme_textAppearanceLargePopupMenu /* 100 */:
                    if (nextName.equals("d")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case R.styleable.AppCompatTheme_textAppearanceListItem /* 101 */:
                    if (nextName.equals("e")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case R.styleable.AppCompatTheme_textAppearanceListItemSmall /* 103 */:
                    if (nextName.equals("g")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case R.styleable.AppCompatTheme_toolbarStyle /* 111 */:
                    if (nextName.equals("o")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case R.styleable.AppCompatTheme_windowActionBar /* 115 */:
                    if (nextName.equals("s")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case R.styleable.AppCompatTheme_windowActionBarOverlay /* 116 */:
                    if (nextName.equals("t")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case R.styleable.AppCompatTheme_windowFixedHeightMinor /* 119 */:
                    if (nextName.equals("w")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 3324:
                    if (nextName.equals("hd")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 3447:
                    if (nextName.equals("lc")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 3454:
                    if (nextName.equals("lj")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 3487:
                    if (nextName.equals("ml")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case 3519:
                    if (nextName.equals("nm")) {
                        c = 11;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject();
                        String str2 = null;
                        AnimatableFloatValue animatableFloatValue4 = null;
                        while (jsonReader.hasNext()) {
                            ShapeStroke.LineJoinType lineJoinType2 = lineJoinType;
                            String nextName2 = jsonReader.nextName();
                            nextName2.hashCode();
                            ShapeStroke.LineCapType lineCapType2 = lineCapType;
                            if (nextName2.equals("n")) {
                                str2 = jsonReader.nextString();
                            } else if (nextName2.equals("v")) {
                                animatableFloatValue4 = AnimatableValueParser.parseFloat(jsonReader, lottieComposition);
                            } else {
                                jsonReader.skipValue();
                            }
                            lineJoinType = lineJoinType2;
                            lineCapType = lineCapType2;
                        }
                        ShapeStroke.LineCapType lineCapType3 = lineCapType;
                        ShapeStroke.LineJoinType lineJoinType3 = lineJoinType;
                        jsonReader.endObject();
                        if (str2.equals("o")) {
                            animatableFloatValue3 = animatableFloatValue4;
                        } else if (str2.equals("d") || str2.equals("g")) {
                            lottieComposition.setHasDashPattern(true);
                            arrayList.add(animatableFloatValue4);
                            lineJoinType = lineJoinType3;
                            lineCapType = lineCapType3;
                        }
                        lineJoinType = lineJoinType3;
                        lineCapType = lineCapType3;
                    }
                    ShapeStroke.LineCapType lineCapType4 = lineCapType;
                    ShapeStroke.LineJoinType lineJoinType4 = lineJoinType;
                    jsonReader.endArray();
                    if (arrayList.size() == 1) {
                        arrayList.add(arrayList.get(0));
                    }
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    lineJoinType = lineJoinType4;
                    f = f2;
                    lineCapType = lineCapType4;
                    continue;
                    break;
                case 1:
                    animatablePointValue2 = AnimatableValueParser.parsePoint(jsonReader, lottieComposition);
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
                case 2:
                    jsonReader.beginObject();
                    int i = -1;
                    while (jsonReader.hasNext()) {
                        String nextName3 = jsonReader.nextName();
                        nextName3.hashCode();
                        if (nextName3.equals("k")) {
                            animatableGradientColorValue = AnimatableValueParser.parseGradientColor(jsonReader, lottieComposition, i);
                        } else if (nextName3.equals("p")) {
                            i = jsonReader.nextInt();
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    jsonReader.endObject();
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
                case 3:
                    animatableIntegerValue = AnimatableValueParser.parseInteger(jsonReader, lottieComposition);
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
                case 4:
                    animatablePointValue = AnimatableValueParser.parsePoint(jsonReader, lottieComposition);
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
                case 5:
                    gradientType = jsonReader.nextInt() == 1 ? GradientType.LINEAR : GradientType.RADIAL;
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
                case 6:
                    animatableFloatValue = AnimatableValueParser.parseFloat(jsonReader, lottieComposition);
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
                case 7:
                    z = jsonReader.nextBoolean();
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
                case '\b':
                    lineCapType = ShapeStroke.LineCapType.values()[jsonReader.nextInt() - 1];
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
                case '\t':
                    lineJoinType = ShapeStroke.LineJoinType.values()[jsonReader.nextInt() - 1];
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
                case '\n':
                    f = (float) jsonReader.nextDouble();
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    break;
                case 11:
                    str = jsonReader.nextString();
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
                default:
                    jsonReader.skipValue();
                    z = z2;
                    animatableFloatValue2 = animatableFloatValue3;
                    f = f2;
                    break;
            }
        }
        return new GradientStroke(str, gradientType, animatableGradientColorValue, animatableIntegerValue, animatablePointValue, animatablePointValue2, animatableFloatValue, lineCapType, lineJoinType, f, arrayList, animatableFloatValue2, z);
    }
}
