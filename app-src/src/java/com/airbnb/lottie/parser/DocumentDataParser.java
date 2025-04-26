package com.airbnb.lottie.parser;

import android.util.JsonReader;
import com.airbnb.lottie.R;
import com.airbnb.lottie.model.DocumentData;
import java.io.IOException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class DocumentDataParser implements ValueParser<DocumentData> {
    public static final DocumentDataParser INSTANCE = new DocumentDataParser();

    private DocumentDataParser() {
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.airbnb.lottie.parser.ValueParser
    public DocumentData parse(JsonReader jsonReader, float f) throws IOException {
        DocumentData.Justification justification;
        DocumentData.Justification justification2 = DocumentData.Justification.CENTER;
        jsonReader.beginObject();
        DocumentData.Justification justification3 = justification2;
        String str = null;
        String str2 = null;
        double d = 0.0d;
        double d2 = 0.0d;
        double d3 = 0.0d;
        double d4 = 0.0d;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        boolean z = true;
        while (jsonReader.hasNext()) {
            String nextName = jsonReader.nextName();
            nextName.hashCode();
            char c = 65535;
            switch (nextName.hashCode()) {
                case R.styleable.AppCompatTheme_textAppearanceListItemSecondary /* 102 */:
                    if (nextName.equals("f")) {
                        c = 0;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_textAppearanceSearchResultTitle /* 106 */:
                    if (nextName.equals("j")) {
                        c = 1;
                        break;
                    }
                    break;
                case R.styleable.AppCompatTheme_windowActionBar /* 115 */:
                    if (nextName.equals("s")) {
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
                case 3261:
                    if (nextName.equals("fc")) {
                        c = 4;
                        break;
                    }
                    break;
                case 3452:
                    if (nextName.equals("lh")) {
                        c = 5;
                        break;
                    }
                    break;
                case 3463:
                    if (nextName.equals("ls")) {
                        c = 6;
                        break;
                    }
                    break;
                case 3543:
                    if (nextName.equals("of")) {
                        c = 7;
                        break;
                    }
                    break;
                case 3664:
                    if (nextName.equals("sc")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 3684:
                    if (nextName.equals("sw")) {
                        c = '\t';
                        break;
                    }
                    break;
                case 3710:
                    if (nextName.equals("tr")) {
                        c = '\n';
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    str2 = jsonReader.nextString();
                    break;
                case 1:
                    int nextInt = jsonReader.nextInt();
                    if (nextInt > DocumentData.Justification.CENTER.ordinal() || nextInt < 0) {
                        justification = DocumentData.Justification.CENTER;
                    } else {
                        justification = DocumentData.Justification.values()[nextInt];
                    }
                    justification3 = justification;
                    break;
                case 2:
                    d = jsonReader.nextDouble();
                    break;
                case 3:
                    str = jsonReader.nextString();
                    break;
                case 4:
                    i2 = JsonUtils.jsonToColor(jsonReader);
                    break;
                case 5:
                    d2 = jsonReader.nextDouble();
                    break;
                case 6:
                    d3 = jsonReader.nextDouble();
                    break;
                case 7:
                    z = jsonReader.nextBoolean();
                    break;
                case '\b':
                    i3 = JsonUtils.jsonToColor(jsonReader);
                    break;
                case '\t':
                    d4 = jsonReader.nextDouble();
                    break;
                case '\n':
                    i = jsonReader.nextInt();
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();
        return new DocumentData(str, str2, d, justification3, i, d2, d3, i2, i3, d4, z);
    }
}
