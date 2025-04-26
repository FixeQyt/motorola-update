package com.motorola.ccc.ota;

import com.motorola.ccc.ota.utils.Logger;
import java.io.File;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public final class DownloadHelper {
    private final long _expected;
    private final File _file;
    private final String _fileName;
    private int _percentDownloaded;
    private final String _version;

    public DownloadHelper(String str, String str2, long j) {
        this._version = str;
        this._fileName = str2;
        this._expected = j;
        File file = new File(str2);
        this._file = file;
        this._percentDownloaded = (int) ((size() * 100) / j);
        Logger.debug("OtaApp", "DownloadHelper(): file size: " + file.length() + " expected size: " + j + " percentage: " + this._percentDownloaded);
    }

    public String version() {
        return this._version;
    }

    public String fileName() {
        return this._fileName;
    }

    public long size() {
        return this._file.length();
    }

    public long expected() {
        return this._expected;
    }

    public long left() {
        if (this._expected > size()) {
            return this._expected - size();
        }
        return 0L;
    }

    public boolean isDone() {
        return this._expected <= size();
    }

    public boolean notifyProgress() {
        long size = size();
        int i = this._percentDownloaded;
        if (i == 0) {
            return true;
        }
        long j = this._expected;
        if (size < ((i - 1) * j) / 100) {
            this._percentDownloaded = (int) ((size * 100) / j);
        }
        return size >= (j * ((long) this._percentDownloaded)) / 100;
    }

    public void incrementPercentDownloaded() {
        int i = this._percentDownloaded;
        if (i < 100) {
            this._percentDownloaded = i + 1;
        }
    }

    public int get_percentDownloaded() {
        return this._percentDownloaded;
    }
}
