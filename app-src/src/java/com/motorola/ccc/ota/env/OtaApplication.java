package com.motorola.ccc.ota.env;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.cdsservice.utils.CDSLogger;
import com.motorola.otalib.common.CommonLogger;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.downloadservice.utils.DownloadServiceLogger;
import java.lang.Thread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class OtaApplication extends Application {
    private static final int MAX_THREADS = 5;
    private static final int THREAD_POOL_THREAD_TIMEOUT = 60;
    private static final TimeUnit THREAD_POOL_TIMEOUT_TIMEUNIT = TimeUnit.SECONDS;
    private static ThreadPoolExecutor executor;
    private static Context global_context;
    private static ScheduledExecutorService scheduledExecutorService;

    public static ExecutorService getExecutorService() {
        if (executor == null) {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 60L, THREAD_POOL_TIMEOUT_TIMEUNIT, new LinkedBlockingQueue());
            executor = threadPoolExecutor;
            threadPoolExecutor.setThreadFactory(new ExceptionCatchingThreadFactory(executor.getThreadFactory()));
        }
        return executor;
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
        }
        return scheduledExecutorService;
    }

    public static Context getGlobalContext() {
        return global_context;
    }

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        global_context = getApplicationContext();
        saveDownloadLoggerContext(getApplicationContext());
        saveCDSLoggerContext(getApplicationContext());
        saveCommonLoggerContext(getApplicationContext());
    }

    @Override // android.app.Application
    public void onTerminate() {
        super.onTerminate();
        BroadcastUtils.sendLocalBroadcast(getApplicationContext(), new Intent(UpgradeUtilConstants.OTA_STOP_ACTION));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static class ExceptionCatchingThreadFactory implements ThreadFactory {
        private final ThreadFactory delegate;

        private ExceptionCatchingThreadFactory(ThreadFactory threadFactory) {
            this.delegate = threadFactory;
        }

        @Override // java.util.concurrent.ThreadFactory
        public Thread newThread(Runnable runnable) {
            Thread newThread = this.delegate.newThread(runnable);
            newThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() { // from class: com.motorola.ccc.ota.env.OtaApplication.ExceptionCatchingThreadFactory.1
                @Override // java.lang.Thread.UncaughtExceptionHandler
                public void uncaughtException(Thread thread, Throwable th) {
                    Logger.error("OtaApp", "uncaughtException in ExceptionCatchingThreadFactory: " + th);
                }
            });
            return newThread;
        }
    }

    private void saveDownloadLoggerContext(Context context) {
        DownloadServiceLogger.saveContext(context, "OtaApp");
    }

    private void saveCDSLoggerContext(Context context) {
        CDSLogger.saveContext(context, "OtaApp");
    }

    private void saveCommonLoggerContext(Context context) {
        CommonLogger.saveContext(context, "OtaApp");
    }
}
