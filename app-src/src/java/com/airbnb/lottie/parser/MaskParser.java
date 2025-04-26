package com.airbnb.lottie.parser;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class MaskParser {
    private MaskParser() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:30:0x0069, code lost:
        if (r0.equals("s") == false) goto L11;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static com.airbnb.lottie.model.content.Mask parse(android.util.JsonReader r10, com.airbnb.lottie.LottieComposition r11) throws java.io.IOException {
        /*
            r10.beginObject()
            r0 = 0
            r1 = 0
            r2 = r0
            r3 = r2
            r4 = r1
        L8:
            boolean r5 = r10.hasNext()
            if (r5 == 0) goto Lc5
            java.lang.String r5 = r10.nextName()
            r5.hashCode()
            int r6 = r5.hashCode()
            r7 = 2
            r8 = 1
            r9 = -1
            switch(r6) {
                case 111: goto L42;
                case 3588: goto L37;
                case 104433: goto L2c;
                case 3357091: goto L21;
                default: goto L1f;
            }
        L1f:
            r6 = r9
            goto L4c
        L21:
            java.lang.String r6 = "mode"
            boolean r6 = r5.equals(r6)
            if (r6 != 0) goto L2a
            goto L1f
        L2a:
            r6 = 3
            goto L4c
        L2c:
            java.lang.String r6 = "inv"
            boolean r6 = r5.equals(r6)
            if (r6 != 0) goto L35
            goto L1f
        L35:
            r6 = r7
            goto L4c
        L37:
            java.lang.String r6 = "pt"
            boolean r6 = r5.equals(r6)
            if (r6 != 0) goto L40
            goto L1f
        L40:
            r6 = r8
            goto L4c
        L42:
            java.lang.String r6 = "o"
            boolean r6 = r5.equals(r6)
            if (r6 != 0) goto L4b
            goto L1f
        L4b:
            r6 = r1
        L4c:
            switch(r6) {
                case 0: goto Lbf;
                case 1: goto Lb9;
                case 2: goto Lb3;
                case 3: goto L53;
                default: goto L4f;
            }
        L4f:
            r10.skipValue()
            goto L8
        L53:
            java.lang.String r0 = r10.nextString()
            r0.hashCode()
            int r6 = r0.hashCode()
            switch(r6) {
                case 97: goto L77;
                case 105: goto L6c;
                case 115: goto L63;
                default: goto L61;
            }
        L61:
            r7 = r9
            goto L81
        L63:
            java.lang.String r6 = "s"
            boolean r0 = r0.equals(r6)
            if (r0 != 0) goto L81
            goto L61
        L6c:
            java.lang.String r6 = "i"
            boolean r0 = r0.equals(r6)
            if (r0 != 0) goto L75
            goto L61
        L75:
            r7 = r8
            goto L81
        L77:
            java.lang.String r6 = "a"
            boolean r0 = r0.equals(r6)
            if (r0 != 0) goto L80
            goto L61
        L80:
            r7 = r1
        L81:
            switch(r7) {
                case 0: goto Laf;
                case 1: goto La6;
                case 2: goto La2;
                default: goto L84;
            }
        L84:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r6 = "Unknown mask mode "
            r0.<init>(r6)
            java.lang.StringBuilder r0 = r0.append(r5)
            java.lang.String r5 = ". Defaulting to Add."
            java.lang.StringBuilder r0 = r0.append(r5)
            java.lang.String r0 = r0.toString()
            java.lang.String r5 = "LOTTIE"
            android.util.Log.w(r5, r0)
            com.airbnb.lottie.model.content.Mask$MaskMode r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_ADD
            goto L8
        La2:
            com.airbnb.lottie.model.content.Mask$MaskMode r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_SUBTRACT
            goto L8
        La6:
            java.lang.String r0 = "Animation contains intersect masks. They are not supported but will be treated like add masks."
            r11.addWarning(r0)
            com.airbnb.lottie.model.content.Mask$MaskMode r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_INTERSECT
            goto L8
        Laf:
            com.airbnb.lottie.model.content.Mask$MaskMode r0 = com.airbnb.lottie.model.content.Mask.MaskMode.MASK_MODE_ADD
            goto L8
        Lb3:
            boolean r4 = r10.nextBoolean()
            goto L8
        Lb9:
            com.airbnb.lottie.model.animatable.AnimatableShapeValue r2 = com.airbnb.lottie.parser.AnimatableValueParser.parseShapeData(r10, r11)
            goto L8
        Lbf:
            com.airbnb.lottie.model.animatable.AnimatableIntegerValue r3 = com.airbnb.lottie.parser.AnimatableValueParser.parseInteger(r10, r11)
            goto L8
        Lc5:
            r10.endObject()
            com.airbnb.lottie.model.content.Mask r10 = new com.airbnb.lottie.model.content.Mask
            r10.<init>(r0, r2, r3, r4)
            return r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.airbnb.lottie.parser.MaskParser.parse(android.util.JsonReader, com.airbnb.lottie.LottieComposition):com.airbnb.lottie.model.content.Mask");
    }
}
