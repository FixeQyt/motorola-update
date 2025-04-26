package com.motorola.ccc.ota.installer.updaterEngine.common;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import com.motorola.ccc.ota.Permissions;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.installer.InstallTypeResolver;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.json.JSONException;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class InstallerUtilMethods {
    private static final int FILE_DATA_DESCRIPTOR_LENGTH = 16;
    private static final int FILE_HEADER_LENGTH = 30;
    public static final String INTENT_MAX_ALARM_FOR_UE = "com.motorola.ccc.ota.INTENT_MAX_ALARM_FOR_UE";
    public static final String KEY_PERCENTAGE = "key_percentage";
    public static final String KEY_STATUS = "key_status";
    public static final long MAX_ALARM_TIME_FOR_DL_MODEM = 172800000;
    public static final long MAX_ALARM_TIME_FOR_UE = 900000;
    private static final String META_INF_COM_ANDROID_METADATA_FILE = "META-INF/com/android/metadata";
    private static final String PAYLOAD_BIN_FILE = "payload.bin";
    private static final String PAYLOAD_PROPERTIES_FILE = "payload_properties.txt";
    private static PowerManager.WakeLock wl;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public enum InstallerErrorStatus {
        STATUS_BUSY,
        STATUS_LOW_SPACE,
        STATUS_NO_FILE,
        STATUS_FOTA_LOW_MEMORY,
        STATUS_OK,
        STATUS_USER_CANCEL,
        STATUS_UE_NOT_RESPONDING,
        STATUS_SILENT_FAILURE,
        STATUS_SYSTEM_UPDATE_POLICY_SET,
        STATUS_SYSTEM_UPDATE_CANCEL_POLICY_SET
    }

    public static void sendUpgradeStatus(Context context, float f, int i, int i2, InstallTypeResolver.InstallerType installerType) {
        BotaSettings botaSettings = new BotaSettings();
        double floor = Math.floor(botaSettings.getFloat(Configs.STORED_AB_PROGRESS_PERCENT, 0.0f));
        double floor2 = Math.floor(f);
        int i3 = botaSettings.getInt(Configs.STORED_AB_STATUS, 0);
        if (i > i3 || ((i == i3 && floor2 > floor) || i2 != 0)) {
            Intent intent = new Intent(UpgradeUtilConstants.ACTION_AB_UPDATE_PROGRESS);
            intent.putExtra(UpgradeUtilConstants.KEY_PERCENTAGE, f);
            intent.putExtra(UpgradeUtilConstants.KEY_UPGRADE_STATUS, i);
            intent.putExtra(UpgradeUtilConstants.KEY_DOWNLOAD_DEFERRED, i2);
            intent.putExtra(UpgradeUtilConstants.KEY_INSTALLER, installerType.toString());
            context.sendBroadcast(intent, Permissions.INTERACT_OTA_SERVICE);
        }
    }

    public static void sendUpdaterEngineStatusCompleted(Context context, boolean z, String str, String str2) {
        Intent intent = new Intent(UpgradeUtilConstants.AB_UPGRADE_COMPLETED_INTENT);
        intent.putExtra(UpgradeUtilConstants.KEY_AB_UPGRADE_STATUS_SUCCESS, z);
        intent.putExtra(UpgradeUtilConstants.KEY_AB_UPGRADE_STATUS_REASON, str);
        intent.putExtra(UpgradeUtilConstants.UPGRADE_UPDATE_STATUS, str2);
        BroadcastUtils.sendLocalBroadcast(context, intent);
    }

    public static void sendCancelBackgroundInstallationResponse(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.UPGRADE_BACKGROUND_INSTALL_CANCEL_RESPONSE));
    }

    public static void sendActionABApplyPayloadStarted(Context context) {
        BroadcastUtils.sendLocalBroadcast(context, new Intent(UpgradeUtilConstants.ACTION_AB_APPLY_PAYLOAD_STARTED_INTENT));
    }

    public static void getPayloadMetaDataBin() throws IOException {
        int read;
        try {
            ZipFile zipFile = new ZipFile(FileUtils.getPackageFilePathForA2B());
            ZipEntry entry = zipFile.getEntry(PAYLOAD_BIN_FILE);
            if (entry == null) {
                throw new IOException("Package does not contain payload.bin file");
            }
            FileOutputStream fileOutputStream = new FileOutputStream(FileUtils.getPayloadMetaDataFileName(), false);
            byte[] bArr = new byte[1024];
            try {
                try {
                    InputStream inputStream = zipFile.getInputStream(entry);
                    long payloadMetadataSize = getPayloadMetadataSize();
                    int i = ((int) payloadMetadataSize) % 1024;
                    for (long j = payloadMetadataSize / 1024; j > 0 && (read = inputStream.read(bArr, 0, 1024)) >= 0; j--) {
                        fileOutputStream.write(bArr, 0, read);
                        Logger.debug("OtaApp", "len: " + read);
                    }
                    int read2 = inputStream.read(bArr, 0, i);
                    if (read2 >= 0) {
                        fileOutputStream.write(bArr, 0, read2);
                    }
                } catch (IOException e) {
                    Logger.error("OtaApp", "getPayloadMetaDataBin, Caught exception while extracting payload metadata file " + e);
                    fileOutputStream.close();
                    throw e;
                }
            } finally {
                zipFile.close();
                fileOutputStream.close();
            }
        } catch (IOException e2) {
            Logger.error("OtaApp", "getPayloadMetaDataBin, Caught exception during creating the zipFile " + e2);
            throw e2;
        }
    }

    public static long getPayloadMetadataSize() throws IOException {
        long j;
        boolean z;
        try {
            ZipFile zipFile = new ZipFile(FileUtils.getPackageFilePathForA2B());
            ZipEntry entry = zipFile.getEntry(META_INF_COM_ANDROID_METADATA_FILE);
            if (entry == null) {
                throw new IOException("Package does not contain META-INF/com/android/metadata file");
            }
            try {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
                    while (true) {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            j = 0;
                            z = false;
                            break;
                        } else if (!readLine.isEmpty() && readLine.contains(FileUtils.PAYLOAD_METADATA_BIN_FILE)) {
                            j = Long.parseLong(readLine.substring(readLine.indexOf("payload_metadata.bin:"), readLine.indexOf(",")).split(SmartUpdateUtils.MASK_SEPARATOR)[2]);
                            z = true;
                            break;
                        }
                    }
                    bufferedReader.close();
                    if (z) {
                        return j;
                    }
                    throw new IOException("metadata file does not contain payload_metadata.bin offset and size");
                } catch (IOException e) {
                    Logger.error("OtaApp", "getPayloadMetadataSize, Caught exception while reading metadata file " + e);
                    throw e;
                }
            } finally {
                zipFile.close();
            }
        } catch (IOException e2) {
            Logger.error("OtaApp", "getPayloadMetadataSize, Caught exception during creating the zipFile " + e2);
            throw e2;
        }
    }

    public static String[] getHeaderKeyValuePair() throws IOException {
        ArrayList arrayList = new ArrayList();
        try {
            ZipFile zipFile = new ZipFile(FileUtils.getPackageFilePathForA2B());
            ZipEntry entry = zipFile.getEntry(PAYLOAD_PROPERTIES_FILE);
            if (entry == null) {
                throw new IOException("Package does not contain payload_properties.txt file");
            }
            try {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
                    while (true) {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            break;
                        } else if (!readLine.isEmpty()) {
                            arrayList.add(readLine);
                        }
                    }
                    bufferedReader.close();
                    zipFile.close();
                    if (!arrayList.isEmpty()) {
                        Logger.debug("OtaApp", "Key value Pair from PAYLOAD_PROPERTIES_FILE," + arrayList.toString());
                        return (String[]) arrayList.toArray(new String[arrayList.size()]);
                    }
                    throw new IOException("payload_properties.txt file does not contain any entries");
                } catch (IOException e) {
                    Logger.error("OtaApp", "getHeaderKeyValuePair, Caught exception while reading property file " + e);
                    throw e;
                }
            } catch (Throwable th) {
                zipFile.close();
                throw th;
            }
        } catch (IOException e2) {
            Logger.error("OtaApp", "getHeaderKeyValuePair, Caught exception during creating the zipFile " + e2);
            throw e2;
        }
    }

    public static long getOffSetValue() throws IOException {
        boolean z;
        try {
            ZipFile zipFile = new ZipFile(FileUtils.getPackageFilePathForA2B());
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            RandomAccessFile randomAccessFile = new RandomAccessFile(FileUtils.getPackageFilePathForA2B(), "r");
            long j = 0;
            while (true) {
                if (!entries.hasMoreElements()) {
                    break;
                }
                ZipEntry nextElement = entries.nextElement();
                randomAccessFile.seek(6 + j);
                z = (randomAccessFile.read() & 8) != 0;
                j += nextElement.getName().length() + 30 + (nextElement.getExtra() == null ? 0L : nextElement.getExtra().length);
                if (nextElement.getName().equals(PAYLOAD_BIN_FILE)) {
                    z = true;
                    break;
                }
                j += !nextElement.isDirectory() ? nextElement.getCompressedSize() : 0L;
                if (z) {
                    j += 16;
                }
            }
            zipFile.close();
            if (z) {
                return j;
            }
            throw new IOException("Package does not contain payload.bin file");
        } catch (IOException e) {
            Logger.error("OtaApp", "getOffSetValue, Caught exception during creating the zipFile " + e);
            throw e;
        }
    }

    public static String getDownloadUrl() {
        return "file://" + FileUtils.getPackageFilePathForA2B();
    }

    public static synchronized void acquireWakeLock() {
        synchronized (InstallerUtilMethods.class) {
            PowerManager.WakeLock newWakeLock = ((PowerManager) OtaApplication.getGlobalContext().getSystemService("power")).newWakeLock(1, "Ota:Update_Engine");
            wl = newWakeLock;
            if (newWakeLock != null) {
                try {
                    if (!newWakeLock.isHeld()) {
                        wl.acquire();
                        Logger.debug("OtaApp", "UpdaterEngineInstaller:bindWithUpdaterEngine, acquiring wakelock");
                    }
                } catch (Exception e) {
                    Logger.error("OtaApp", "Exception in InstallerUtilMethods, acquireWakeLock: " + e);
                }
            }
        }
    }

    public static synchronized void releaseWakelock() {
        synchronized (InstallerUtilMethods.class) {
            try {
                PowerManager.WakeLock wakeLock = wl;
                if (wakeLock != null && wakeLock.isHeld()) {
                    wl.release();
                    Logger.debug("OtaApp", "UpdaterEngineInstaller:bindWithUpdaterEngine, releasing wakelock");
                    wl = null;
                }
            } catch (Exception e) {
                Logger.error("OtaApp", "Exception in InstallerUtilMethods, releaseWakelock: " + e);
            }
        }
    }

    public static boolean isMemoryLowForBackgroundInstallation(ApplicationEnv applicationEnv, BotaSettings botaSettings, ApplicationEnv.Database.Descriptor descriptor) {
        long availableDataPartitionSize = applicationEnv.getUtilities().getAvailableDataPartitionSize();
        long chunkSize = descriptor.getMeta().getChunkSize();
        if (chunkSize <= 0) {
            chunkSize = 0;
        }
        Logger.debug("OtaApp", "CusSM.checkSpaceForBackgroundInstallation, available space in data is " + (availableDataPartitionSize / 1048576) + " MB; extra space needed in data is " + (chunkSize / 1048576) + " MB");
        if (availableDataPartitionSize < chunkSize) {
            long j = (chunkSize - availableDataPartitionSize) / 1048576;
            if (FileUtils.getNumFilesInDir() <= 0 || !FileUtils.deleteReserveSpaceFiles(FileUtils.calculateNumFilesNeeded((int) j) + 1)) {
                return true;
            }
            Logger.debug("OtaApp", "CusSM.checkSpaceForBackgroundInstallation: Freed " + j + " MB");
            return false;
        }
        botaSettings.removeConfig(Configs.DATA_SPACE_RETRY_COUNT);
        return false;
    }

    public static String getAdminApnUrl(BotaSettings botaSettings) {
        String string = botaSettings.getString(Configs.DOWNLOAD_DESCRIPTOR);
        if (string != null) {
            try {
                return new JSONObject(string).optString("adminApnUrl");
            } catch (JSONException e) {
                Logger.debug("OtaApp", "DownloadService:getAdminApnUrl, Exception occured " + e);
                return null;
            }
        }
        return null;
    }

    public static boolean isDownloadAllowed(Context context, String str, ConnectivityManager connectivityManager, TelephonyManager telephonyManager) {
        if (str != null && str.length() != 0) {
            String wanTypeAsString = NetworkUtils.getWanTypeAsString(context, connectivityManager, telephonyManager);
            if (wanTypeAsString == null) {
                wanTypeAsString = "OTHER";
            }
            Logger.debug("OtaApp", "isDownloadAllowed: currently on: " + wanTypeAsString + "; disallowed nets are: " + str);
            StringTokenizer stringTokenizer = new StringTokenizer(str, ",");
            while (stringTokenizer.hasMoreTokens()) {
                String nextToken = stringTokenizer.nextToken();
                if (nextToken.compareToIgnoreCase(wanTypeAsString) == 0) {
                    Logger.debug("OtaApp", "isDownloadAllowed: network not allowed " + nextToken);
                    return false;
                }
            }
        }
        return true;
    }

    public static String[] getHeaderValues(JSONObject jSONObject) throws JSONException {
        JSONObject optJSONObject;
        try {
            ArrayList arrayList = new ArrayList();
            if (jSONObject != null && (optJSONObject = jSONObject.optJSONObject("header")) != null) {
                Iterator<String> keys = optJSONObject.keys();
                while (keys.hasNext()) {
                    String next = keys.next();
                    arrayList.add(next + "=" + optJSONObject.get(next));
                }
            }
            return (String[]) arrayList.toArray(new String[arrayList.size()]);
        } catch (NullPointerException e) {
            Logger.debug("OtaApp", "Exception in InstallerUtilMethods.getHeaderValues: " + e);
            return null;
        }
    }
}
