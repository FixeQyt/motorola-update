package com.airbnb.lottie.parser;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
class JsonUtils {
    private JsonUtils() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int jsonToColor(JsonReader jsonReader) throws IOException {
        jsonReader.beginArray();
        int nextDouble = (int) (jsonReader.nextDouble() * 255.0d);
        int nextDouble2 = (int) (jsonReader.nextDouble() * 255.0d);
        int nextDouble3 = (int) (jsonReader.nextDouble() * 255.0d);
        while (jsonReader.hasNext()) {
            jsonReader.skipValue();
        }
        jsonReader.endArray();
        return Color.argb(255, nextDouble, nextDouble2, nextDouble3);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static List<PointF> jsonToPoints(JsonReader jsonReader, float f) throws IOException {
        ArrayList arrayList = new ArrayList();
        jsonReader.beginArray();
        while (jsonReader.peek() == JsonToken.BEGIN_ARRAY) {
            jsonReader.beginArray();
            arrayList.add(jsonToPoint(jsonReader, f));
            jsonReader.endArray();
        }
        jsonReader.endArray();
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.airbnb.lottie.parser.JsonUtils$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$util$JsonToken;

        static {
            int[] iArr = new int[JsonToken.values().length];
            $SwitchMap$android$util$JsonToken = iArr;
            try {
                iArr[JsonToken.NUMBER.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$android$util$JsonToken[JsonToken.BEGIN_ARRAY.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$android$util$JsonToken[JsonToken.BEGIN_OBJECT.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static PointF jsonToPoint(JsonReader jsonReader, float f) throws IOException {
        int i = AnonymousClass1.$SwitchMap$android$util$JsonToken[jsonReader.peek().ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    return jsonObjectToPoint(jsonReader, f);
                }
                throw new IllegalArgumentException("Unknown point starts with " + jsonReader.peek());
            }
            return jsonArrayToPoint(jsonReader, f);
        }
        return jsonNumbersToPoint(jsonReader, f);
    }

    private static PointF jsonNumbersToPoint(JsonReader jsonReader, float f) throws IOException {
        float nextDouble = (float) jsonReader.nextDouble();
        float nextDouble2 = (float) jsonReader.nextDouble();
        while (jsonReader.hasNext()) {
            jsonReader.skipValue();
        }
        return new PointF(nextDouble * f, nextDouble2 * f);
    }

    private static PointF jsonArrayToPoint(JsonReader jsonReader, float f) throws IOException {
        jsonReader.beginArray();
        float nextDouble = (float) jsonReader.nextDouble();
        float nextDouble2 = (float) jsonReader.nextDouble();
        while (jsonReader.peek() != JsonToken.END_ARRAY) {
            jsonReader.skipValue();
        }
        jsonReader.endArray();
        return new PointF(nextDouble * f, nextDouble2 * f);
    }

    private static PointF jsonObjectToPoint(JsonReader jsonReader, float f) throws IOException {
        jsonReader.beginObject();
        float f2 = 0.0f;
        float f3 = 0.0f;
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            nextName.hashCode();
            if (nextName.equals("x")) {
                f2 = valueFromObject(jsonReader);
            } else if (nextName.equals("y")) {
                f3 = valueFromObject(jsonReader);
            } else {
                jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return new PointF(f2 * f, f3 * f);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static float valueFromObject(JsonReader jsonReader) throws IOException {
        JsonToken peek = jsonReader.peek();
        int i = AnonymousClass1.$SwitchMap$android$util$JsonToken[peek.ordinal()];
        if (i != 1) {
            if (i == 2) {
                jsonReader.beginArray();
                float nextDouble = (float) jsonReader.nextDouble();
                while (jsonReader.hasNext()) {
                    jsonReader.skipValue();
                }
                jsonReader.endArray();
                return nextDouble;
            }
            throw new IllegalArgumentException("Unknown value for token of type " + peek);
        }
        return (float) jsonReader.nextDouble();
    }
}
