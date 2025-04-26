package com.airbnb.lottie.parser;

import android.util.JsonReader;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.R;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.content.ShapeTrimPath;
import java.io.IOException;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class ShapeTrimPathParser {
    private ShapeTrimPathParser() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ShapeTrimPath parse(JsonReader jsonReader, LottieComposition lottieComposition) throws IOException {
        String str = null;
        ShapeTrimPath.Type type = null;
        AnimatableFloatValue animatableFloatValue = null;
        AnimatableFloatValue animatableFloatValue2 = null;
        AnimatableFloatValue animatableFloatValue3 = null;
        boolean z = false;
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            nextName.hashCode();
            char c = 65535;
            switch (nextName.hashCode()) {
                case R.styleable.AppCompatTheme_textAppearanceListItem /* 101 */:
                    if (nextName.equals("e")) {
                        c = 0;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_textColorSearchUrl /* 109 */:
                    if (nextName.equals("m")) {
                        c = 1;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_toolbarStyle /* 111 */:
                    if (nextName.equals("o")) {
                        c = 2;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_windowActionBar /* 115 */:
                    if (nextName.equals("s")) {
                        c = 3;
                        break;
                    }
                    break;
                case 3324:
                    if (nextName.equals("hd")) {
                        c = 4;
                        break;
                    }
                    break;
                case 3519:
                    if (nextName.equals("nm")) {
                        c = 5;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    animatableFloatValue2 = AnimatableValueParser.parseFloat(jsonReader, lottieComposition, false);
                    break;
                case 1:
                    type = ShapeTrimPath.Type.forId(jsonReader.nextInt());
                    break;
                case 2:
                    animatableFloatValue3 = AnimatableValueParser.parseFloat(jsonReader, lottieComposition, false);
                    break;
                case 3:
                    animatableFloatValue = AnimatableValueParser.parseFloat(jsonReader, lottieComposition, false);
                    break;
                case 4:
                    z = jsonReader.nextBoolean();
                    break;
                case 5:
                    str = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        return new ShapeTrimPath(str, type, animatableFloatValue, animatableFloatValue2, animatableFloatValue3, z);
    }
}
