package com.airbnb.lottie.network;

import com.airbnb.lottie.L;
import com.motorola.ccc.ota.utils.FileUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public enum FileExtension {
    JSON(".json"),
    ZIP(FileUtils.EXT);
    
    public final String extension;

    FileExtension(String str) {
        this.extension = str;
    }

    public String tempExtension() {
        return ".temp" + this.extension;
    }

    @Override // java.lang.Enum
    public String toString() {
        return this.extension;
    }

    public static FileExtension forFile(String str) {
        FileExtension[] values;
        for (FileExtension fileExtension : values()) {
            if (str.endsWith(fileExtension.extension)) {
                return fileExtension;
            }
        }
        L.warn("Unable to find correct extension for " + str);
        return JSON;
    }
}
