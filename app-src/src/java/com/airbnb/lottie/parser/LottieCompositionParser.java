package com.airbnb.lottie.parser;

import android.graphics.Rect;
import android.util.JsonReader;
import androidx.collection.LongSparseArray;
import androidx.collection.SparseArrayCompat;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.R;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.Marker;
import com.airbnb.lottie.model.layer.Layer;
import com.airbnb.lottie.utils.Utils;
import com.motorola.otalib.downloadservice.utils.DownloadServiceSettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class LottieCompositionParser {
    private LottieCompositionParser() {
    }

    public static LottieComposition parse(JsonReader jsonReader) throws IOException {
        float f;
        char c;
        HashMap hashMap;
        ArrayList arrayList;
        float dpScale = Utils.dpScale();
        LongSparseArray<Layer> longSparseArray = new LongSparseArray<>();
        ArrayList arrayList2 = new ArrayList();
        HashMap hashMap2 = new HashMap();
        HashMap hashMap3 = new HashMap();
        HashMap hashMap4 = new HashMap();
        ArrayList arrayList3 = new ArrayList();
        SparseArrayCompat<FontCharacter> sparseArrayCompat = new SparseArrayCompat<>();
        LottieComposition lottieComposition = new LottieComposition();
        jsonReader.beginObject();
        float f2 = 0.0f;
        float f3 = 0.0f;
        float f4 = 0.0f;
        int i = 0;
        int i2 = 0;
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            nextName.hashCode();
            char c2 = 65535;
            switch (nextName.hashCode()) {
                case -1408207997:
                    f = f4;
                    if (nextName.equals("assets")) {
                        c2 = 0;
                        break;
                    }
                    break;
                case -1109732030:
                    f = f4;
                    if (nextName.equals("layers")) {
                        c2 = 1;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_textAppearancePopupMenuHeader /* 104 */:
                    f = f4;
                    if (nextName.equals("h")) {
                        c2 = 2;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_windowFixedHeightMajor /* 118 */:
                    f = f4;
                    if (nextName.equals("v")) {
                        c = 3;
                        c2 = c;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_windowFixedHeightMinor /* 119 */:
                    f = f4;
                    if (nextName.equals("w")) {
                        c = 4;
                        c2 = c;
                        break;
                    }
                    break;
                case 3276:
                    f = f4;
                    if (nextName.equals("fr")) {
                        c = 5;
                        c2 = c;
                        break;
                    }
                    break;
                case 3367:
                    f = f4;
                    if (nextName.equals("ip")) {
                        c = 6;
                        c2 = c;
                        break;
                    }
                    break;
                case 3553:
                    f = f4;
                    if (nextName.equals("op")) {
                        c = 7;
                        c2 = c;
                        break;
                    }
                    break;
                case 94623709:
                    f = f4;
                    if (nextName.equals("chars")) {
                        c = '\b';
                        c2 = c;
                        break;
                    }
                    break;
                case 97615364:
                    f = f4;
                    if (nextName.equals("fonts")) {
                        c = '\t';
                        c2 = c;
                        break;
                    }
                    break;
                case 839250809:
                    f = f4;
                    if (nextName.equals("markers")) {
                        c = '\n';
                        c2 = c;
                        break;
                    }
                    break;
                default:
                    f = f4;
                    break;
            }
            switch (c2) {
                case 0:
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    parseAssets(jsonReader, lottieComposition, hashMap2, hashMap3);
                    break;
                case 1:
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    parseLayers(jsonReader, lottieComposition, arrayList2, longSparseArray);
                    break;
                case 2:
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    i2 = jsonReader.nextInt();
                    break;
                case 3:
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    String[] split = jsonReader.nextString().split("\\.");
                    if (!Utils.isAtLeastVersion(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), 4, 4, 0)) {
                        lottieComposition.addWarning("Lottie only supports bodymovin >= 4.4.0");
                        break;
                    }
                    break;
                case 4:
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    i = jsonReader.nextInt();
                    break;
                case 5:
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    f4 = (float) jsonReader.nextDouble();
                    continue;
                    hashMap4 = hashMap;
                    arrayList3 = arrayList;
                case 6:
                    f2 = (float) jsonReader.nextDouble();
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    break;
                case 7:
                    f3 = ((float) jsonReader.nextDouble()) - 0.01f;
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    break;
                case '\b':
                    parseChars(jsonReader, lottieComposition, sparseArrayCompat);
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    break;
                case '\t':
                    parseFonts(jsonReader, hashMap4);
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    break;
                case '\n':
                    parseMarkers(jsonReader, lottieComposition, arrayList3);
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    break;
                default:
                    jsonReader.skipValue();
                    hashMap = hashMap4;
                    arrayList = arrayList3;
                    break;
            }
            f4 = f;
            hashMap4 = hashMap;
            arrayList3 = arrayList;
        }
        jsonReader.endObject();
        lottieComposition.init(new Rect(0, 0, (int) (i * dpScale), (int) (i2 * dpScale)), f2, f3, f4, arrayList2, longSparseArray, hashMap2, hashMap3, sparseArrayCompat, hashMap4, arrayList3);
        return lottieComposition;
    }

    private static void parseLayers(JsonReader jsonReader, LottieComposition lottieComposition, List<Layer> list, LongSparseArray<Layer> longSparseArray) throws IOException {
        jsonReader.beginArray();
        int i = 0;
        while (jsonReader.hasNext()) {
            Layer parse = LayerParser.parse(jsonReader, lottieComposition);
            if (parse.getLayerType() == Layer.LayerType.IMAGE) {
                i++;
            }
            list.add(parse);
            longSparseArray.put(parse.getId(), parse);
            if (i > 4) {
                L.warn("You have " + i + " images. Lottie should primarily be used with shapes. If you are using Adobe Illustrator, convert the Illustrator layers to shape layers.");
            }
        }
        jsonReader.endArray();
    }

    private static void parseAssets(JsonReader jsonReader, LottieComposition lottieComposition, Map<String, List<Layer>> map, Map<String, LottieImageAsset> map2) throws IOException {
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            ArrayList arrayList = new ArrayList();
            LongSparseArray longSparseArray = new LongSparseArray();
            jsonReader.beginObject();
            int i = 0;
            int i2 = 0;
            String str = null;
            String str2 = null;
            String str3 = null;
            while (jsonReader.hasNext()) {
                String nextName = jsonReader.nextName();
                nextName.hashCode();
                char c = 65535;
                switch (nextName.hashCode()) {
                    case -1109732030:
                        if (nextName.equals("layers")) {
                            c = 0;
                            break;
                        }
                        break;
                    case R.styleable.AppCompatTheme_textAppearancePopupMenuHeader /* 104 */:
                        if (nextName.equals("h")) {
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
                    case R.styleable.AppCompatTheme_windowActionModeOverlay /* 117 */:
                        if (nextName.equals("u")) {
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
                    case 3355:
                        if (nextName.equals(DownloadServiceSettings.KEY_ID)) {
                            c = 5;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        jsonReader.beginArray();
                        while (jsonReader.hasNext()) {
                            Layer parse = LayerParser.parse(jsonReader, lottieComposition);
                            longSparseArray.put(parse.getId(), parse);
                            arrayList.add(parse);
                        }
                        jsonReader.endArray();
                        break;
                    case 1:
                        i2 = jsonReader.nextInt();
                        break;
                    case 2:
                        str2 = jsonReader.nextString();
                        break;
                    case 3:
                        str3 = jsonReader.nextString();
                        break;
                    case 4:
                        i = jsonReader.nextInt();
                        break;
                    case 5:
                        str = jsonReader.nextString();
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
            }
            jsonReader.endObject();
            if (str2 != null) {
                LottieImageAsset lottieImageAsset = new LottieImageAsset(i, i2, str, str2, str3);
                map2.put(lottieImageAsset.getId(), lottieImageAsset);
            } else {
                map.put(str, arrayList);
            }
        }
        jsonReader.endArray();
    }

    private static void parseFonts(JsonReader jsonReader, Map<String, Font> map) throws IOException {
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            nextName.hashCode();
            if (nextName.equals("list")) {
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    Font parse = FontParser.parse(jsonReader);
                    map.put(parse.getName(), parse);
                }
                jsonReader.endArray();
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
    }

    private static void parseChars(JsonReader jsonReader, LottieComposition lottieComposition, SparseArrayCompat<FontCharacter> sparseArrayCompat) throws IOException {
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            FontCharacter parse = FontCharacterParser.parse(jsonReader, lottieComposition);
            sparseArrayCompat.put(parse.hashCode(), parse);
        }
        jsonReader.endArray();
    }

    private static void parseMarkers(JsonReader jsonReader, LottieComposition lottieComposition, List<Marker> list) throws IOException {
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            jsonReader.beginObject();
            float f = 0.0f;
            String str = null;
            float f2 = 0.0f;
            while (jsonReader.hasNext()) {
                String nextName = jsonReader.nextName();
                nextName.hashCode();
                char c = 65535;
                switch (nextName.hashCode()) {
                    case 3178:
                        if (nextName.equals("cm")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 3214:
                        if (nextName.equals("dr")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 3705:
                        if (nextName.equals("tm")) {
                            c = 2;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        str = jsonReader.nextString();
                        break;
                    case 1:
                        f2 = (float) jsonReader.nextDouble();
                        break;
                    case 2:
                        f = (float) jsonReader.nextDouble();
                        break;
                    default:
                        jsonReader.skipValue();
                        break;
                }
            }
            jsonReader.endObject();
            list.add(new Marker(str, f, f2));
        }
        jsonReader.endArray();
    }
}
