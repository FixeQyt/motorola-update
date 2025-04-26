package com.airbnb.lottie.parser;

import android.graphics.Path;
import android.util.JsonReader;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.R;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.content.ShapeFill;
import java.io.IOException;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class ShapeFillParser {
    private ShapeFillParser() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ShapeFill parse(JsonReader jsonReader, LottieComposition lottieComposition) throws IOException {
        String str = null;
        AnimatableColorValue animatableColorValue = null;
        AnimatableIntegerValue animatableIntegerValue = null;
        boolean z = false;
        boolean z2 = false;
        int i = 1;
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            nextName.hashCode();
            char c = 65535;
            switch (nextName.hashCode()) {
                case -396065730:
                    if (nextName.equals("fillEnabled")) {
                        c = 0;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_switchStyle /* 99 */:
                    if (nextName.equals("c")) {
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
                case R.styleable.AppCompatTheme_viewInflaterClass /* 114 */:
                    if (nextName.equals("r")) {
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
                    z = jsonReader.nextBoolean();
                    break;
                case 1:
                    animatableColorValue = AnimatableValueParser.parseColor(jsonReader, lottieComposition);
                    break;
                case 2:
                    animatableIntegerValue = AnimatableValueParser.parseInteger(jsonReader, lottieComposition);
                    break;
                case 3:
                    i = jsonReader.nextInt();
                    break;
                case 4:
                    z2 = jsonReader.nextBoolean();
                    break;
                case 5:
                    str = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        return new ShapeFill(str, z, i == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD, animatableColorValue, animatableIntegerValue, z2);
    }
}
