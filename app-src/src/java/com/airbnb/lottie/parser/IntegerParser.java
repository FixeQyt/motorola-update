package com.airbnb.lottie.parser;

import android.util.JsonReader;
import java.io.IOException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class IntegerParser implements ValueParser<Integer> {
    public static final IntegerParser INSTANCE = new IntegerParser();

    private IntegerParser() {
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.airbnb.lottie.parser.ValueParser
    public Integer parse(JsonReader jsonReader, float f) throws IOException {
        return Integer.valueOf(Math.round(JsonUtils.valueFromObject(jsonReader) * f));
    }
}
