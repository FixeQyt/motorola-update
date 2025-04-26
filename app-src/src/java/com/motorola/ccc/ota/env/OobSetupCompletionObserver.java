package com.motorola.ccc.ota.env;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import com.motorola.ccc.ota.utils.Logger;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class OobSetupCompletionObserver {
    private static final int CHECK_SETUP_STATE = 1;
    static final String HANDLER_TAG = "OobSetupCompletionObserver";
    private static final int POST_SETUP_COMPLETED = 0;
    private static Context mCtx;
    private static OobSetupCompletionObserver mMe;
    private ContentObserver mContentObserver = null;
    private HandlerThread mHandlerThread = null;
    private MessageHandler mMessageHandler = null;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    private class MessageHandler extends Handler {
        public MessageHandler() {
        }

        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            super.handleMessage(message);
            int i = message.what;
            if (i == 0) {
                CusAndroidUtils.postSetupCompleted(OobSetupCompletionObserver.mCtx);
            } else if (i == 1 && OobSetupCompletionObserver.this.getSetupCompletedStatus() == 1) {
                try {
                    OobSetupCompletionObserver.mCtx.getContentResolver().unregisterContentObserver(OobSetupCompletionObserver.this.mContentObserver);
                } catch (NullPointerException e) {
                    Logger.error("OtaApp", "OobSetupCompletionObserver: unregister failed." + e);
                }
                OobSetupCompletionObserver.this.mMessageHandler.sendEmptyMessage(0);
            }
        }
    }

    public void init(Context context) {
        mCtx = context;
        HandlerThread handlerThread = new HandlerThread(HANDLER_TAG);
        this.mHandlerThread = handlerThread;
        handlerThread.start();
        this.mMessageHandler = new MessageHandler(this.mHandlerThread.getLooper());
    }

    private OobSetupCompletionObserver() {
    }

    public static synchronized OobSetupCompletionObserver getInstance() {
        OobSetupCompletionObserver oobSetupCompletionObserver;
        synchronized (OobSetupCompletionObserver.class) {
            if (mMe == null) {
                mMe = new OobSetupCompletionObserver();
            }
            oobSetupCompletionObserver = mMe;
        }
        return oobSetupCompletionObserver;
    }

    public void shutdown() {
        try {
            mCtx.getContentResolver().unregisterContentObserver(this.mContentObserver);
        } catch (NullPointerException e) {
            Logger.error("OtaApp", "OobSetupCompletionObserver: unregister failed." + e);
        }
        this.mHandlerThread.quitSafely();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getSetupCompletedStatus() {
        int i = Settings.Secure.getInt(mCtx.getContentResolver(), CusFrameworkDeps.getUserSetupCompleteAsString(), 0);
        Logger.debug("OtaApp", "OobSetupCompletionObserver: Setup completion status: " + i);
        return i;
    }

    public void checkSetupCompleted() {
        if (getSetupCompletedStatus() == 1) {
            this.mMessageHandler.sendEmptyMessage(0);
            return;
        }
        this.mContentObserver = new SetupContentObserver(this.mMessageHandler);
        try {
            mCtx.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(CusFrameworkDeps.getUserSetupCompleteAsString()), true, this.mContentObserver);
        } catch (IllegalArgumentException e) {
            Logger.error("OtaApp", "OobSetupCompletionObserver: register failed due to invalid Uri: " + e);
        } catch (NullPointerException e2) {
            Logger.error("OtaApp", "OobSetupCompletionObserver: register failed due to Null Uri: " + e2);
        }
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    private final class SetupContentObserver extends ContentObserver {
        Handler mHandler;

        public SetupContentObserver(Handler handler) {
            super(handler);
            this.mHandler = handler;
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            this.mHandler.sendEmptyMessage(1);
        }
    }
}
