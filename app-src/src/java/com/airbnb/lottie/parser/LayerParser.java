package com.airbnb.lottie.parser;

import android.graphics.Color;
import android.graphics.Rect;
import android.util.JsonReader;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.R;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTextFrame;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.content.ContentModel;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.Keyframe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class LayerParser {
    private LayerParser() {
    }

    public static Layer parse(LottieComposition lottieComposition) {
        Rect bounds = lottieComposition.getBounds();
        return new Layer(Collections.emptyList(), lottieComposition, "__container", -1L, Layer.LayerType.PRE_COMP, -1L, null, Collections.emptyList(), new AnimatableTransform(), 0, 0, 0, 0.0f, 0.0f, bounds.width(), bounds.height(), null, null, Collections.emptyList(), Layer.MatteType.NONE, null, false);
    }

    public static Layer parse(JsonReader jsonReader, LottieComposition lottieComposition) throws IOException {
        ArrayList arrayList;
        ArrayList arrayList2;
        float f;
        Layer.MatteType matteType = Layer.MatteType.NONE;
        ArrayList arrayList3 = new ArrayList();
        ArrayList arrayList4 = new ArrayList();
        jsonReader.beginObject();
        Float valueOf = Float.valueOf(1.0f);
        Float valueOf2 = Float.valueOf(0.0f);
        Layer.MatteType matteType2 = matteType;
        Layer.LayerType layerType = null;
        String str = null;
        AnimatableTransform animatableTransform = null;
        AnimatableTextFrame animatableTextFrame = null;
        AnimatableTextProperties animatableTextProperties = null;
        AnimatableFloatValue animatableFloatValue = null;
        long j = 0;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        boolean z = false;
        float f2 = 1.0f;
        long j2 = -1;
        float f3 = 0.0f;
        float f4 = 0.0f;
        String str2 = "UNSET";
        String str3 = null;
        float f5 = 0.0f;
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            nextName.hashCode();
            char c = 65535;
            switch (nextName.hashCode()) {
                case -995424086:
                    if (nextName.equals("parent")) {
                        c = 0;
                        break;
                    }
                    break;
                case -903568142:
                    if (nextName.equals("shapes")) {
                        c = 1;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_textAppearancePopupMenuHeader /* 104 */:
                    if (nextName.equals("h")) {
                        c = 2;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_windowActionBarOverlay /* 116 */:
                    if (nextName.equals("t")) {
                        c = 3;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_windowFixedHeightMinor /* 119 */:
                    if (nextName.equals("w")) {
                        c = 4;
                        break;
                    }
                    break;
                case 3177:
                    if (nextName.equals("cl")) {
                        c = 5;
                        break;
                    }
                    break;
                case 3233:
                    if (nextName.equals("ef")) {
                        c = 6;
                        break;
                    }
                    break;
                case 3324:
                    if (nextName.equals("hd")) {
                        c = 7;
                        break;
                    }
                    break;
                case 3367:
                    if (nextName.equals("ip")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 3432:
                    if (nextName.equals("ks")) {
                        c = '\t';
                        break;
                    }
                    break;
                case 3519:
                    if (nextName.equals("nm")) {
                        c = '\n';
                        break;
                    }
                    break;
                case 3553:
                    if (nextName.equals("op")) {
                        c = 11;
                        break;
                    }
                    break;
                case 3664:
                    if (nextName.equals("sc")) {
                        c = '\f';
                        break;
                    }
                    break;
                case 3669:
                    if (nextName.equals("sh")) {
                        c = '\r';
                        break;
                    }
                    break;
                case 3679:
                    if (nextName.equals("sr")) {
                        c = 14;
                        break;
                    }
                    break;
                case 3681:
                    if (nextName.equals("st")) {
                        c = 15;
                        break;
                    }
                    break;
                case 3684:
                    if (nextName.equals("sw")) {
                        c = 16;
                        break;
                    }
                    break;
                case 3705:
                    if (nextName.equals("tm")) {
                        c = 17;
                        break;
                    }
                    break;
                case 3712:
                    if (nextName.equals("tt")) {
                        c = 18;
                        break;
                    }
                    break;
                case 3717:
                    if (nextName.equals("ty")) {
                        c = 19;
                        break;
                    }
                    break;
                case 104415:
                    if (nextName.equals("ind")) {
                        c = 20;
                        break;
                    }
                    break;
                case 108390670:
                    if (nextName.equals("refId")) {
                        c = 21;
                        break;
                    }
                    break;
                case 1441620890:
                    if (nextName.equals("masksProperties")) {
                        c = 22;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    j2 = jsonReader.nextInt();
                    continue;
                case 1:
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        ContentModel parse = ContentModelParser.parse(jsonReader, lottieComposition);
                        if (parse != null) {
                            arrayList4.add(parse);
                        }
                    }
                    jsonReader.endArray();
                    continue;
                case 2:
                    i5 = (int) (jsonReader.nextInt() * Utils.dpScale());
                    continue;
                case 3:
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String nextName2 = jsonReader.nextName();
                        nextName2.hashCode();
                        if (nextName2.equals("a")) {
                            jsonReader.beginArray();
                            if (jsonReader.hasNext()) {
                                animatableTextProperties = AnimatableTextPropertiesParser.parse(jsonReader, lottieComposition);
                            }
                            while (jsonReader.hasNext()) {
                                jsonReader.skipValue();
                            }
                            jsonReader.endArray();
                        } else if (nextName2.equals("d")) {
                            animatableTextFrame = AnimatableValueParser.parseDocumentData(jsonReader, lottieComposition);
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    jsonReader.endObject();
                    continue;
                case 4:
                    i4 = (int) (jsonReader.nextInt() * Utils.dpScale());
                    continue;
                case 5:
                    str3 = jsonReader.nextString();
                    continue;
                case 6:
                    jsonReader.beginArray();
                    ArrayList arrayList5 = new ArrayList();
                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            String nextName3 = jsonReader.nextName();
                            nextName3.hashCode();
                            if (nextName3.equals("nm")) {
                                arrayList5.add(jsonReader.nextString());
                            } else {
                                jsonReader.skipValue();
                            }
                        }
                        jsonReader.endObject();
                    }
                    jsonReader.endArray();
                    lottieComposition.addWarning("Lottie doesn't support layer effects. If you are using them for  fills, strokes, trim paths etc. then try adding them directly as contents  in your shape. Found: " + arrayList5);
                    continue;
                case 7:
                    z = jsonReader.nextBoolean();
                    continue;
                case '\b':
                    f3 = (float) jsonReader.nextDouble();
                    continue;
                case '\t':
                    animatableTransform = AnimatableTransformParser.parse(jsonReader, lottieComposition);
                    continue;
                case '\n':
                    str2 = jsonReader.nextString();
                    continue;
                case 11:
                    f5 = (float) jsonReader.nextDouble();
                    continue;
                case '\f':
                    i3 = Color.parseColor(jsonReader.nextString());
                    continue;
                case '\r':
                    i2 = (int) (jsonReader.nextInt() * Utils.dpScale());
                    continue;
                case 14:
                    f2 = (float) jsonReader.nextDouble();
                    continue;
                case 15:
                    f4 = (float) jsonReader.nextDouble();
                    continue;
                case 16:
                    i = (int) (jsonReader.nextInt() * Utils.dpScale());
                    continue;
                case 17:
                    animatableFloatValue = AnimatableValueParser.parseFloat(jsonReader, lottieComposition, false);
                    continue;
                case 18:
                    matteType2 = Layer.MatteType.values()[jsonReader.nextInt()];
                    lottieComposition.incrementMatteOrMaskCount(1);
                    break;
                case 19:
                    int nextInt = jsonReader.nextInt();
                    if (nextInt < Layer.LayerType.UNKNOWN.ordinal()) {
                        layerType = Layer.LayerType.values()[nextInt];
                        break;
                    } else {
                        layerType = Layer.LayerType.UNKNOWN;
                        break;
                    }
                case 20:
                    j = jsonReader.nextInt();
                    break;
                case 21:
                    str = jsonReader.nextString();
                    break;
                case 22:
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        arrayList3.add(MaskParser.parse(jsonReader, lottieComposition));
                    }
                    lottieComposition.incrementMatteOrMaskCount(arrayList3.size());
                    jsonReader.endArray();
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();
        float f6 = f3 / f2;
        float f7 = f5 / f2;
        ArrayList arrayList6 = new ArrayList();
        if (f6 > 0.0f) {
            arrayList = arrayList3;
            arrayList2 = arrayList6;
            arrayList2.add(new Keyframe(lottieComposition, valueOf2, valueOf2, null, 0.0f, Float.valueOf(f6)));
            f = 0.0f;
        } else {
            arrayList = arrayList3;
            arrayList2 = arrayList6;
            f = 0.0f;
        }
        if (f7 <= f) {
            f7 = lottieComposition.getEndFrame();
        }
        arrayList2.add(new Keyframe(lottieComposition, valueOf, valueOf, null, f6, Float.valueOf(f7)));
        arrayList2.add(new Keyframe(lottieComposition, valueOf2, valueOf2, null, f7, Float.valueOf(Float.MAX_VALUE)));
        if (str2.endsWith(".ai") || "ai".equals(str3)) {
            lottieComposition.addWarning("Convert your Illustrator layers to shape layers.");
        }
        return new Layer(arrayList4, lottieComposition, str2, j, layerType, j2, str, arrayList, animatableTransform, i, i2, i3, f2, f4, i4, i5, animatableTextFrame, animatableTextProperties, arrayList2, matteType2, animatableFloatValue, z);
    }
}
