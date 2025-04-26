package com.motorola.ccc.ota.stats;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.nio.charset.Charset;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class ProcFileReader implements Closeable {
    private final byte[] mBuffer;
    private boolean mLineFinished;
    private final InputStream mStream;
    private int mTail;

    public ProcFileReader(InputStream inputStream) throws IOException {
        this(inputStream, 4096);
    }

    public ProcFileReader(InputStream inputStream, int i) throws IOException {
        this.mStream = inputStream;
        this.mBuffer = new byte[i];
        fillBuf();
    }

    private int fillBuf() throws IOException {
        byte[] bArr = this.mBuffer;
        int length = bArr.length;
        int i = this.mTail;
        int i2 = length - i;
        if (i2 == 0) {
            throw new IOException("attempting to fill already-full buffer");
        }
        int read = this.mStream.read(bArr, i, i2);
        if (read != -1) {
            this.mTail += read;
        }
        return read;
    }

    private void consumeBuf(int i) throws IOException {
        byte[] bArr = this.mBuffer;
        System.arraycopy(bArr, i, bArr, 0, this.mTail - i);
        int i2 = this.mTail - i;
        this.mTail = i2;
        if (i2 == 0) {
            fillBuf();
        }
    }

    private int nextTokenIndex() throws IOException {
        if (this.mLineFinished) {
            return -1;
        }
        int i = 0;
        while (true) {
            if (i < this.mTail) {
                byte b = this.mBuffer[i];
                if (b == 10) {
                    this.mLineFinished = true;
                    return i;
                } else if (b == 32) {
                    return i;
                } else {
                    i++;
                }
            } else if (fillBuf() <= 0) {
                throw new ProtocolException("End of stream while looking for token boundary");
            }
        }
    }

    public boolean hasMoreData() {
        return this.mTail > 0;
    }

    public void finishLine() throws IOException {
        int i = 0;
        if (this.mLineFinished) {
            this.mLineFinished = false;
            return;
        }
        while (true) {
            if (i < this.mTail) {
                if (this.mBuffer[i] == 10) {
                    consumeBuf(i + 1);
                    return;
                }
                i++;
            } else if (fillBuf() <= 0) {
                throw new ProtocolException("End of stream while looking for line boundary");
            }
        }
    }

    public String nextString() throws IOException {
        int nextTokenIndex = nextTokenIndex();
        if (nextTokenIndex == -1) {
            throw new ProtocolException("Missing required string");
        }
        return parseAndConsumeString(nextTokenIndex);
    }

    public long nextLong() throws IOException {
        int nextTokenIndex = nextTokenIndex();
        if (nextTokenIndex == -1) {
            throw new ProtocolException("Missing required long");
        }
        return parseAndConsumeLong(nextTokenIndex);
    }

    public long nextOptionalLong(long j) throws IOException {
        int nextTokenIndex = nextTokenIndex();
        return nextTokenIndex == -1 ? j : parseAndConsumeLong(nextTokenIndex);
    }

    private String parseAndConsumeString(int i) throws IOException {
        String str = new String(this.mBuffer, 0, i, Charset.forName("US_ASCII"));
        consumeBuf(i + 1);
        return str;
    }

    private long parseAndConsumeLong(int i) throws IOException {
        int i2 = this.mBuffer[0] == 45 ? 1 : 0;
        long j = 0;
        int i3 = i2;
        while (i3 < i) {
            int i4 = this.mBuffer[i3] - 48;
            if (i4 < 0 || i4 > 9) {
                throw invalidLong(i);
            }
            long j2 = (10 * j) - i4;
            if (j2 > j) {
                throw invalidLong(i);
            }
            i3++;
            j = j2;
        }
        consumeBuf(i + 1);
        return i2 != 0 ? j : -j;
    }

    private NumberFormatException invalidLong(int i) {
        return new NumberFormatException("invalid long: ".concat(new String(this.mBuffer, 0, i, Charset.forName("US_ASCII"))));
    }

    public int nextInt() throws IOException {
        long nextLong = nextLong();
        if (nextLong > 2147483647L || nextLong < -2147483648L) {
            throw new NumberFormatException("parsed value larger than integer");
        }
        return (int) nextLong;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.mStream.close();
    }
}
