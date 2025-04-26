package com.motorola.otalib.downloadservice.download;

import android.os.PowerManager;
import android.text.TextUtils;
import com.motorola.otalib.common.backoff.BackoffValueProvider;
import com.motorola.otalib.downloadservice.download.PackageDownloader;
import com.motorola.otalib.downloadservice.download.error.ExceptionHandler;
import com.motorola.otalib.downloadservice.download.error.HttpErrorCodeHandlerNotFound;
import com.motorola.otalib.downloadservice.utils.DownloadServiceLogger;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class AdvancedFileDownloader implements PackageDownloader.FileDownloader {
    private static final String BotaUpgrade = "upgrade";
    public static final int DOWNLOAD_BUFFER_SIZE = 16384;
    private static final int HTTP_TEMP_REDIRECT = 307;
    public static final int READ_BUFFER_SIZE = 8192;
    private static String mETag = null;
    private static boolean proceedWithSaveCrc = true;
    private BackoffValueProvider backoffValues;
    private Map<Class<? extends Exception>, ExceptionHandler> exceptionHandlers;
    private HttpURLConnection urlBuilder;
    private List<HttpHeader> headers = new ArrayList();
    private AtomicBoolean shouldShutdown = new AtomicBoolean(false);
    private List<DownloadProcessor> processors = new ArrayList();

    private boolean validateLength(long j, long j2) {
        return j == j2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class HttpHeader {
        private final String key;
        private final String value;

        public HttpHeader(String str, String str2) {
            this.key = str;
            this.value = str2;
        }
    }

    public AdvancedFileDownloader(HttpURLConnection httpURLConnection, BackoffValueProvider backoffValueProvider) {
        HashMap hashMap = new HashMap();
        this.exceptionHandlers = hashMap;
        hashMap.put(IOException.class, new ExceptionHandler() { // from class: com.motorola.otalib.downloadservice.download.AdvancedFileDownloader.1
            @Override // com.motorola.otalib.downloadservice.download.error.ExceptionHandler
            public boolean handleException(Exception exc) {
                if (exc instanceof IOException) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "handleException false " + exc);
                    return false;
                }
                return true;
            }
        });
        this.urlBuilder = httpURLConnection;
        this.backoffValues = backoffValueProvider;
    }

    @Override // com.motorola.otalib.downloadservice.download.PackageDownloader.FileDownloader
    public void addHeader(String str, String str2) {
        this.headers.add(new HttpHeader(str, str2));
    }

    @Override // com.motorola.otalib.downloadservice.download.PackageDownloader.FileDownloader
    public void addProcessor(DownloadProcessor downloadProcessor) {
        this.processors.add(downloadProcessor);
    }

    @Override // com.motorola.otalib.downloadservice.download.PackageDownloader.FileDownloader
    public void downloadFile(String str, int i, String str2, File file, long j, PowerManager.WakeLock wakeLock, String str3, long j2) throws HttpFileDownloadException {
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Creating synchrozised call");
        advancedDownloadFile(str, i, str2, file, j, wakeLock, str3, j2);
        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "Ending synchronized call");
    }

    public void advancedDownloadFile(String str, int i, String str2, File file, long j, PowerManager.WakeLock wakeLock, String str3, long j2) throws HttpFileDownloadException {
        boolean z = false;
        while (true) {
            try {
                try {
                    try {
                        try {
                            try {
                                wakeLock.acquire();
                                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, acquiring wakelock");
                                if (file.exists()) {
                                    innerResumeDownload(str, i, str2, file, j, str3, j2);
                                } else {
                                    innerDownloadFile(str, i, str2, file, j2, j, new CRC32(), str3);
                                }
                                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, Exiting from download loop");
                                try {
                                    if (wakeLock.isHeld()) {
                                        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, Releasing wakelock");
                                        wakeLock.release();
                                    }
                                } catch (RuntimeException e) {
                                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, wakelock RuntimeException " + e);
                                    if (!e.getMessage().startsWith("WakeLock under-locked ")) {
                                        throw e;
                                    }
                                }
                                shutdownClient();
                                z = false;
                            } catch (RetryDownloadException e2) {
                                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, Caught RetryException " + e2.getMessage());
                                try {
                                    if (wakeLock.isHeld()) {
                                        DownloadServiceLogger.d(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, Releasing wakelock");
                                        wakeLock.release();
                                    }
                                } catch (RuntimeException e3) {
                                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, wakelock RuntimeException " + e3);
                                    if (!e3.getMessage().startsWith("WakeLock under-locked ")) {
                                        throw e3;
                                    }
                                }
                                shutdownClient();
                                z = true;
                            }
                        } catch (RuntimeException e4) {
                            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, RuntimeException: " + e4);
                            throw e4;
                        }
                    } catch (Throwable th) {
                        try {
                            if (wakeLock.isHeld()) {
                                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, Releasing wakelock");
                                wakeLock.release();
                            }
                        } catch (RuntimeException e5) {
                            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, wakelock RuntimeException " + e5);
                            if (!e5.getMessage().startsWith("WakeLock under-locked ")) {
                                throw e5;
                            }
                        }
                        shutdownClient();
                        throw th;
                    }
                } catch (Exception e6) {
                    DownloadServiceLogger.e(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, Caught Exception " + e6);
                    for (DownloadProcessor downloadProcessor : this.processors) {
                        downloadProcessor.exception(e6);
                    }
                    try {
                        if (wakeLock.isHeld()) {
                            DownloadServiceLogger.d(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, Releasing wakelock");
                            wakeLock.release();
                        }
                    } catch (RuntimeException e7) {
                        DownloadServiceLogger.e(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, wakelock RuntimeException " + e7);
                        if (!e7.getMessage().startsWith("WakeLock under-locked ")) {
                            throw e7;
                        }
                    }
                    shutdownClient();
                }
                if (!z) {
                    return;
                }
                AtomicBoolean atomicBoolean = this.shouldShutdown;
                if (atomicBoolean != null && atomicBoolean.get()) {
                    return;
                }
            } catch (HttpFileDownloadException e8) {
                DownloadServiceLogger.e(DownloadServiceLogger.TAG, "AdvancedFileDownloader.newDownloadFile, HttpFileDownloadException: " + e8);
                throw e8;
            }
        }
    }

    @Override // com.motorola.otalib.downloadservice.download.PackageDownloader.FileDownloader
    public void shutdown() {
        this.shouldShutdown.set(true);
        shutdownClient();
    }

    private synchronized void shutdownClient() {
        try {
            if (this.urlBuilder != null) {
                DownloadServiceLogger.d(DownloadServiceLogger.TAG, "AdvancedFileDownloader.shutDownClient, disconnecting HttpUrlConnection..");
                this.urlBuilder.disconnect();
                this.urlBuilder = null;
                System.clearProperty("java.net.preferIPv4Stack");
                System.clearProperty("java.net.preferIPv6Addresses");
            }
        } catch (Exception e) {
            DownloadServiceLogger.e(DownloadServiceLogger.TAG, "AdvancedFileDownloader.shutDownClient, url disconnect exception " + e);
        }
    }

    /* JADX WARN: Can't wrap try/catch for region: R(6:61|(1:62)|(3:(3:259|260|(5:262|65|66|68|(9:78|79|81|82|(2:235|236)|84|(1:86)(2:232|233)|87|(3:88|89|(4:91|(4:94|95|(1:97)(1:98)|92)|213|(2:204|205)(2:100|(6:102|103|(5:182|183|184|185|186)(1:105)|106|107|(4:109|110|111|(5:113|(2:116|114)|117|118|(2:121|122)(1:120))(3:148|149|150))(3:172|173|174))(3:201|202|203)))(2:214|215)))(5:70|(3:74|72|71)|75|76|77)))|68|(0)(0))|64|65|66) */
    /* JADX WARN: Can't wrap try/catch for region: R(6:61|62|(3:(3:259|260|(5:262|65|66|68|(9:78|79|81|82|(2:235|236)|84|(1:86)(2:232|233)|87|(3:88|89|(4:91|(4:94|95|(1:97)(1:98)|92)|213|(2:204|205)(2:100|(6:102|103|(5:182|183|184|185|186)(1:105)|106|107|(4:109|110|111|(5:113|(2:116|114)|117|118|(2:121|122)(1:120))(3:148|149|150))(3:172|173|174))(3:201|202|203)))(2:214|215)))(5:70|(3:74|72|71)|75|76|77)))|68|(0)(0))|64|65|66) */
    /* JADX WARN: Code restructure failed: missing block: B:157:0x039d, code lost:
        if (r17.shouldShutdown.get() == false) goto L125;
     */
    /* JADX WARN: Code restructure failed: missing block: B:158:0x039f, code lost:
        r8.close();
        r5.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:159:0x03a5, code lost:
        return;
     */
    /* JADX WARN: Code restructure failed: missing block: B:160:0x03a6, code lost:
        if (r12 > 0) goto L129;
     */
    /* JADX WARN: Code restructure failed: missing block: B:161:0x03a8, code lost:
        if (r0 == false) goto L127;
     */
    /* JADX WARN: Code restructure failed: missing block: B:164:0x03cd, code lost:
        throw new java.io.IOException("AdvancedFileDownloader.innerDownloadFile,Got -1 for read length and but did not read expectedSize:" + r6 + com.motorola.ccc.ota.utils.FileUtils.SD_CARD_DIR + r3);
     */
    /* JADX WARN: Code restructure failed: missing block: B:165:0x03ce, code lost:
        if (r0 == false) goto L136;
     */
    /* JADX WARN: Code restructure failed: missing block: B:166:0x03d0, code lost:
        r0 = r17.processors.iterator();
     */
    /* JADX WARN: Code restructure failed: missing block: B:168:0x03da, code lost:
        if (r0.hasNext() == false) goto L134;
     */
    /* JADX WARN: Code restructure failed: missing block: B:169:0x03dc, code lost:
        r0.next().finished();
     */
    /* JADX WARN: Code restructure failed: missing block: B:170:0x03e6, code lost:
        com.motorola.otalib.downloadservice.download.AdvancedFileDownloader.mETag = null;
        deleteCRC(r11);
        com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.i(com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.TAG, "AdvancedFileDownloader.innerDownloadFile, download file completed");
     */
    /* JADX WARN: Code restructure failed: missing block: B:171:0x03f3, code lost:
        r8.close();
        r5.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:172:0x03f9, code lost:
        return;
     */
    /* JADX WARN: Code restructure failed: missing block: B:210:0x0462, code lost:
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:211:0x0463, code lost:
        r2 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:213:0x0467, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:214:0x0468, code lost:
        r2 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:221:0x048b, code lost:
        r0 = e;
     */
    /* JADX WARN: Code restructure failed: missing block: B:222:0x048c, code lost:
        r2 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:286:?, code lost:
        return;
     */
    /* JADX WARN: Code restructure failed: missing block: B:287:?, code lost:
        return;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:197:0x0436 A[Catch: all -> 0x045c, RuntimeException -> 0x045e, IOException -> 0x0460, TryCatch #20 {IOException -> 0x0460, RuntimeException -> 0x045e, all -> 0x045c, blocks: (B:197:0x0436, B:198:0x043d, B:201:0x0445, B:202:0x0456, B:203:0x045b, B:195:0x041d, B:196:0x0435, B:72:0x0248), top: B:266:0x0246, inners: #37 }] */
    /* JADX WARN: Removed duplicated region for block: B:219:0x0477 A[Catch: all -> 0x0490, LOOP:8: B:217:0x0471->B:219:0x0477, LOOP_END, TryCatch #28 {all -> 0x0490, blocks: (B:216:0x046b, B:217:0x0471, B:219:0x0477, B:220:0x048a, B:224:0x048f), top: B:246:0x01f5 }] */
    /* JADX WARN: Removed duplicated region for block: B:231:0x049a A[Catch: Exception -> 0x049d, TRY_LEAVE, TryCatch #11 {Exception -> 0x049d, blocks: (B:229:0x0495, B:231:0x049a), top: B:238:0x0495 }] */
    /* JADX WARN: Removed duplicated region for block: B:238:0x0495 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:249:0x0248 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Type inference failed for: r4v32 */
    /* JADX WARN: Type inference failed for: r5v1 */
    /* JADX WARN: Type inference failed for: r5v10, types: [java.io.BufferedInputStream, java.io.InputStream] */
    /* JADX WARN: Type inference failed for: r5v2, types: [java.io.InputStream] */
    /* JADX WARN: Type inference failed for: r5v3 */
    /* JADX WARN: Type inference failed for: r5v4 */
    /* JADX WARN: Type inference failed for: r5v7 */
    /* JADX WARN: Type inference failed for: r8v17 */
    /* JADX WARN: Type inference failed for: r8v31 */
    /* JADX WARN: Type inference failed for: r8v36, types: [java.util.zip.CheckedOutputStream] */
    /* JADX WARN: Type inference failed for: r8v8 */
    /* JADX WARN: Type inference failed for: r8v9 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void innerDownloadFile(java.lang.String r18, int r19, java.lang.String r20, java.io.File r21, long r22, long r24, java.util.zip.CRC32 r26, java.lang.String r27) throws com.motorola.otalib.downloadservice.download.HttpFileDownloadException, java.io.IOException {
        /*
            Method dump skipped, instructions count: 1194
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.otalib.downloadservice.download.AdvancedFileDownloader.innerDownloadFile(java.lang.String, int, java.lang.String, java.io.File, long, long, java.util.zip.CRC32, java.lang.String):void");
    }

    /* JADX WARN: Removed duplicated region for block: B:14:0x0075  */
    /* JADX WARN: Removed duplicated region for block: B:15:0x0082  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    protected void innerResumeDownload(java.lang.String r14, int r15, java.lang.String r16, java.io.File r17, long r18, java.lang.String r20, long r21) throws java.lang.Exception {
        /*
            r13 = this;
            r1 = r13
            r5 = r17
            java.lang.String r0 = "AdvancedFileDownloader.innerResumeDownload,CRC Validation failed. Resetting offset to 0 ComputeCRC value :"
            boolean r2 = r17.exists()
            r3 = 0
            r6 = 0
            if (r2 == 0) goto L70
            long r7 = r17.length()
            java.lang.String r2 = com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.TAG
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            java.lang.String r10 = "AdvancedFileDownloader.innerResumeDownload, Offset value : "
            r9.<init>(r10)
            java.lang.StringBuilder r9 = r9.append(r7)
            java.lang.String r9 = r9.toString()
            com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.i(r2, r9)
            java.util.zip.CRC32 r6 = r13.computeCRCFromDownloadFile(r5)     // Catch: java.lang.Exception -> L5b
            long r9 = r6.getValue()     // Catch: java.lang.Exception -> L5b
            long r11 = r13.readCRC(r5)     // Catch: java.lang.Exception -> L5b
            int r2 = (r9 > r11 ? 1 : (r9 == r11 ? 0 : -1))
            if (r2 == 0) goto L71
            java.lang.String r2 = com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.TAG     // Catch: java.lang.Exception -> L5b
            java.lang.StringBuilder r7 = new java.lang.StringBuilder     // Catch: java.lang.Exception -> L5b
            r7.<init>(r0)     // Catch: java.lang.Exception -> L5b
            long r8 = r6.getValue()     // Catch: java.lang.Exception -> L5b
            java.lang.StringBuilder r0 = r7.append(r8)     // Catch: java.lang.Exception -> L5b
            java.lang.String r7 = "ReadCRC value "
            java.lang.StringBuilder r0 = r0.append(r7)     // Catch: java.lang.Exception -> L5b
            long r7 = r13.readCRC(r5)     // Catch: java.lang.Exception -> L5b
            java.lang.StringBuilder r0 = r0.append(r7)     // Catch: java.lang.Exception -> L5b
            java.lang.String r0 = r0.toString()     // Catch: java.lang.Exception -> L5b
            com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.e(r2, r0)     // Catch: java.lang.Exception -> L5b
            goto L70
        L5b:
            r0 = move-exception
            java.lang.String r2 = com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            java.lang.String r8 = "AdvancedFileDownloader.innerResumeDownload,Exception while computing or reading crc "
            r7.<init>(r8)
            java.lang.StringBuilder r0 = r7.append(r0)
            java.lang.String r0 = r0.toString()
            com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.d(r2, r0)
        L70:
            r7 = r3
        L71:
            int r0 = (r7 > r3 ? 1 : (r7 == r3 ? 0 : -1))
            if (r0 != 0) goto L82
            r17.delete()
            r13.deleteCRC(r5)
            java.util.zip.CRC32 r0 = new java.util.zip.CRC32
            r0.<init>()
            r10 = r0
            goto L83
        L82:
            r10 = r6
        L83:
            java.lang.String r0 = com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r3 = "AdvancedFileDownloader.innerResumeDownload,Restarting from offset:"
            r2.<init>(r3)
            java.lang.StringBuilder r2 = r2.append(r7)
            java.lang.String r2 = r2.toString()
            com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.d(r0, r2)
            java.lang.String r0 = com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            java.lang.String r3 = "AdvancedFileDownloader.innerResumeDownload,Resuming from CRC:"
            r2.<init>(r3)
            long r3 = r10.getValue()
            java.lang.StringBuilder r2 = r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.d(r0, r2)
            r1 = r13
            r2 = r14
            r3 = r15
            r4 = r16
            r5 = r17
            r6 = r21
            r8 = r18
            r11 = r20
            r1.innerDownloadFile(r2, r3, r4, r5, r6, r8, r10, r11)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.otalib.downloadservice.download.AdvancedFileDownloader.innerResumeDownload(java.lang.String, int, java.lang.String, java.io.File, long, java.lang.String, long):void");
    }

    protected boolean executeErrorHandler(int i) throws HttpErrorCodeHandlerNotFound {
        if (i < 500 || i > 507) {
            throw new HttpErrorCodeHandlerNotFound(i);
        }
        return true;
    }

    protected void saveCRC(File file, long j) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(new File(file.getParent(), file.getName() + ".crc")));
            dataOutputStream.writeLong(j);
            dataOutputStream.close();
        } catch (Exception unused) {
            proceedWithSaveCrc = false;
        }
    }

    protected long readCRC(File file) {
        try {
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(new File(file.getParent(), file.getName() + ".crc")));
            long readLong = dataInputStream.readLong();
            dataInputStream.close();
            return readLong;
        } catch (Exception unused) {
            return -1L;
        }
    }

    protected void deleteCRC(File file) {
        new File(file.getParent(), file.getName() + ".crc").delete();
    }

    protected CRC32 computeCRCFromDownloadFile(File file) throws IOException {
        CheckedInputStream checkedInputStream = new CheckedInputStream(new FileInputStream(file), new CRC32());
        do {
        } while (checkedInputStream.read(new byte[1024]) > 0);
        checkedInputStream.close();
        return (CRC32) checkedInputStream.getChecksum();
    }

    /* JADX WARN: Removed duplicated region for block: B:12:0x0021  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean readResponseHeaders(java.net.HttpURLConnection r10, long r11, int r13) {
        /*
            r9 = this;
            r0 = 206(0xce, float:2.89E-43)
            r1 = 1
            if (r13 != r0) goto L6
            return r1
        L6:
            java.lang.String r13 = "Transfer-Encoding"
            java.lang.String r13 = r10.getHeaderField(r13)
            r2 = -1
            if (r13 != 0) goto L1b
            java.lang.String r0 = "Content-Length"
            java.lang.String r0 = r10.getHeaderField(r0)     // Catch: java.lang.NumberFormatException -> L1b
            long r4 = java.lang.Long.parseLong(r0)     // Catch: java.lang.NumberFormatException -> L1b
            goto L1c
        L1b:
            r4 = r2
        L1c:
            int r0 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1))
            r6 = 0
            if (r0 != 0) goto L40
            java.lang.String r0 = com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            java.lang.String r8 = "AdvancedFileDownloader.readResponseHeaders, mContentLength = "
            r7.<init>(r8)
            java.lang.StringBuilder r4 = r7.append(r4)
            java.lang.String r4 = r4.toString()
            com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.d(r0, r4)
            if (r13 == 0) goto L3f
            java.lang.String r0 = "chunked"
            boolean r13 = r13.equalsIgnoreCase(r0)
            if (r13 != 0) goto L40
        L3f:
            return r6
        L40:
            java.lang.String r13 = "Content-Range"
            java.lang.String r10 = r10.getHeaderField(r13)
            long r4 = r9.getLengthFromContentRange(r10)
            int r10 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1))
            if (r10 == 0) goto L73
            boolean r10 = r9.validateLength(r11, r4)
            if (r10 != 0) goto L73
            java.lang.String r9 = com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.TAG
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            java.lang.String r13 = "AdvancedFileDownloader.readResponseHeaders:Total file size in Content-Range:"
            r10.<init>(r13)
            java.lang.StringBuilder r10 = r10.append(r4)
            java.lang.String r13 = ", does not match expectedSize:"
            java.lang.StringBuilder r10 = r10.append(r13)
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.String r10 = r10.toString()
            com.motorola.otalib.downloadservice.utils.DownloadServiceLogger.e(r9, r10)
            return r6
        L73:
            java.net.HttpURLConnection r9 = r9.urlBuilder
            java.lang.String r10 = "ETag"
            java.lang.String r9 = r9.getHeaderField(r10)
            com.motorola.otalib.downloadservice.download.AdvancedFileDownloader.mETag = r9
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.motorola.otalib.downloadservice.download.AdvancedFileDownloader.readResponseHeaders(java.net.HttpURLConnection, long, int):boolean");
    }

    private long getLengthFromContentRange(String str) {
        if (TextUtils.isEmpty(str)) {
            return -1L;
        }
        try {
            int lastIndexOf = str.lastIndexOf(47);
            if (lastIndexOf >= 0) {
                return Long.parseLong(str.substring(lastIndexOf + 1));
            }
            return -1L;
        } catch (Exception unused) {
            return -1L;
        }
    }
}
