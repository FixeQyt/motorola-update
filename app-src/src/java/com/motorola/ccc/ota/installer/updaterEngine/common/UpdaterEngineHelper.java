package com.motorola.ccc.ota.installer.updaterEngine.common;

import android.content.Context;
import android.os.UpdateEngineCallback;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.errorCodes.ErrorCodeMapper;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class UpdaterEngineHelper {
    private static Object mUpdateEngine = null;
    public static String mergeStatsFilePath = "/data/misc/update_engine_log/merge_stats.json";
    public static String metricsFilePath = "/data/misc/update_engine_log/metrics.json";
    private static Class updateEngine;

    /* JADX INFO: Access modifiers changed from: private */
    public static void setUpdateEngineObject() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> cls = Class.forName("android.os.UpdateEngine");
        updateEngine = cls;
        if (mUpdateEngine == null) {
            mUpdateEngine = cls.newInstance();
        }
    }

    public static void bindWithUpdaterEngine(UpdateEngineCallbacker updateEngineCallbacker) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        try {
            setUpdateEngineObject();
            Logger.debug("OtaApp", "UpdaterEngineHelper:bindWithUpdaterEngine, binding with updater engine");
            updateEngine.getDeclaredMethod("bind", UpdateEngineCallback.class).invoke(mUpdateEngine, updateEngineCallbacker);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Logger.debug("OtaApp", "UpdaterEngineHelper:bindWithUpdaterEngine, failed to bind with updater engine" + e);
            UEBinder.setBinded(false);
            throw e;
        }
    }

    public static void applyPayload(String str, long j, long j2, String[] strArr) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?>[] clsArr = {String.class, Long.TYPE, Long.TYPE, String[].class};
        Logger.debug("OtaApp", "UpdaterEngineHelper:applyPayload, applying payload with downloadUrl = " + str + " offsetvalue = " + j + " fileSize = " + j2);
        try {
            setUpdateEngineObject();
            updateEngine.getDeclaredMethod("applyPayload", clsArr).invoke(mUpdateEngine, str, Long.valueOf(j), Long.valueOf(j2), strArr);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Logger.debug("OtaApp", "UpdaterEngineHelper:applyPayload, failed to apply payload" + e);
            throw e;
        }
    }

    public static void resetUpdateEngine() {
        Logger.debug("OtaApp", "UpdaterEngineHelper:resetUpdateEngine");
        new Thread(new Runnable() { // from class: com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineHelper.1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    UpdaterEngineHelper.setUpdateEngineObject();
                    UpdaterEngineHelper.updateEngine.getDeclaredMethod("resetStatus", new Class[0]).invoke(UpdaterEngineHelper.mUpdateEngine, new Object[0]);
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    Logger.error("OtaApp", "exception in UpdaterEngineHelper:resetUpdateEngine " + e);
                }
            }
        }).start();
    }

    public static boolean cancelUpdateEngine() {
        Logger.debug("OtaApp", "UpdaterEngineHelper:cancelUpdateEngine");
        try {
            setUpdateEngineObject();
            updateEngine.getDeclaredMethod("cancel", new Class[0]).invoke(mUpdateEngine, new Object[0]);
            return true;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Logger.error("OtaApp", "exception in UpdaterEngineHelper:cancelUpdateEngine " + e);
            return false;
        }
    }

    public static void verifyPayloadMetadata(final String str, final boolean z) {
        new Thread(new Runnable() { // from class: com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineHelper.2
            @Override // java.lang.Runnable
            public void run() {
                Logger.debug("OtaApp", "UpdaterEngineHelper:verifyPayloadMetadata");
                Context globalContext = OtaApplication.getGlobalContext();
                boolean z2 = false;
                try {
                    UpdaterEngineHelper.setUpdateEngineObject();
                    z2 = ((Boolean) UpdaterEngineHelper.updateEngine.getDeclaredMethod("verifyPayloadMetadata", String.class).invoke(UpdaterEngineHelper.mUpdateEngine, str)).booleanValue();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    Logger.error("OtaApp", "exception in UpdaterEngineHelper:verifyPayloadMetadata " + e);
                }
                Logger.debug("OtaApp", "UpdaterEngHelper:verifyPayloadMetadata:isVerifySuccessful=" + z2);
                if (z) {
                    UpgradeUtilMethods.sendActionVerifyPayloadStatus(globalContext, z2);
                } else if (z2) {
                    UpgradeUtilMethods.sendActionVerifyPayloadStatus(globalContext, "success", UpgradeUtils.DownloadStatus.STATUS_OK, null);
                } else {
                    UpgradeUtilMethods.sendActionVerifyPayloadStatus(globalContext, "Payload metadata verification failed ", UpgradeUtils.DownloadStatus.STATUS_FAIL_PAYLOAD_METADATA_VERIFY, ErrorCodeMapper.KEY_PAYLOAD_METADATA_VERIFICATION_FAILED);
                }
            }
        }).start();
    }

    public static void unbindUpdateEngine() {
        Logger.debug("OtaApp", "UpdaterEngineHelper:unbindUpdateEngine");
        new Thread(new Runnable() { // from class: com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineHelper.3
            @Override // java.lang.Runnable
            public void run() {
                try {
                    UpdaterEngineHelper.setUpdateEngineObject();
                    Logger.debug("OtaApp", "UpdaterEngineHelper:unbindRunnable completed " + ((Boolean) UpdaterEngineHelper.updateEngine.getDeclaredMethod("unbind", new Class[0]).invoke(UpdaterEngineHelper.mUpdateEngine, new Object[0])).booleanValue());
                    UpdaterEngineHelper.mUpdateEngine = null;
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    Logger.error("OtaApp", "exception in UpdaterEngineHelper:unbindRunnable " + e);
                    UpdaterEngineHelper.mUpdateEngine = null;
                }
            }
        }).start();
    }

    public static void allocateSpace(final Context context, final String str, final String[] strArr) {
        Logger.debug("OtaApp", "UpdaterEngineHelper:allocateSpace");
        new Thread(new Runnable() { // from class: com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineHelper.4
            @Override // java.lang.Runnable
            public void run() {
                Class<?>[] clsArr = {String.class, String[].class};
                Logger.debug("OtaApp", "UpdaterEngineHelper:allocateSpace, allocateSpace with payloadMetaDataPath" + str);
                try {
                    UpdaterEngineHelper.setUpdateEngineObject();
                    Object invoke = UpdaterEngineHelper.updateEngine.getDeclaredMethod("allocateSpace", clsArr).invoke(UpdaterEngineHelper.mUpdateEngine, str, strArr);
                    long longValue = ((Long) invoke.getClass().getMethod("getFreeSpaceRequired", new Class[0]).invoke(invoke, new Object[0])).longValue();
                    if (longValue < 0) {
                        UpgradeUtilMethods.sendActionAllocateSpaceResult(context, 0L);
                    } else {
                        UpgradeUtilMethods.sendActionAllocateSpaceResult(context, longValue);
                    }
                } catch (Exception e) {
                    Logger.error("OtaApp", "exception in UpdaterEngineHelper:allocateSpace " + e.getCause());
                    e.printStackTrace();
                    UpgradeUtilMethods.sendActionAllocateSpaceResult(context, 0L);
                }
            }
        }).start();
    }

    public static String getJsonDataFromFile(String str) {
        try {
            File file = new File(str);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            for (String readLine = bufferedReader.readLine(); readLine != null; readLine = bufferedReader.readLine()) {
                sb.append(readLine).append(SystemUpdateStatusUtils.NEWLINE_SEPERATOR);
            }
            bufferedReader.close();
            fileReader.close();
            Logger.debug("OtaApp", "json string = " + sb.toString());
            file.delete();
            return sb.toString();
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception while reading data from " + str + " exception=" + e);
            return null;
        }
    }

    public static void cleanupAppliedPayload() {
        new Thread(new Runnable() { // from class: com.motorola.ccc.ota.installer.updaterEngine.common.UpdaterEngineHelper.5
            @Override // java.lang.Runnable
            public void run() {
                int i;
                Logger.debug("OtaApp", "UpdaterEngineHelper:cleanupAppliedPayload");
                try {
                    UpdaterEngineHelper.setUpdateEngineObject();
                    i = ((Integer) UpdaterEngineHelper.updateEngine.getDeclaredMethod("cleanupAppliedPayload", new Class[0]).invoke(UpdaterEngineHelper.mUpdateEngine, new Object[0])).intValue();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    Logger.error("OtaApp", "exceptionUpdaterEngineHelper:cleanupAppliedPayload " + e);
                    i = UpgradeUtils.MergeStatus.APPLY_PAYLOAD_FAILURE;
                }
                UpgradeUtilMethods.sendCleanupAppliedPayloadResult(OtaApplication.getGlobalContext(), i);
            }
        }).start();
    }
}
