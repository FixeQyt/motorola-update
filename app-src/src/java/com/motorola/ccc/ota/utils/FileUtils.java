package com.motorola.ccc.ota.utils;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import com.motorola.ccc.ota.env.CusFrameworkDeps;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.ui.updateType.UpdateType;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.utils.BuildPropertyUtils;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class FileUtils {
    private static final String AB_PACKAGE_DOWNLOAD_PATH = "ota_package";
    private static final String ACTION_UPDATE_SEPOLICY = "com.motorola.intent.action.UPDATE_SEPOLICY";
    public static final String DATA = "data";
    public static final int DEFAULT_DATA_SPACE_REQUIRED = 52428800;
    public static final int DEFAULT_STATUS_SPACE_COMPATIBILITY_CHECK = 1048576;
    private static final String DIR_APK_PATH = "apk_path";
    private static final String DIR_MODEM_FILE_PATH = "modem";
    private static final String DIR_PERMISSION = "755";
    private static final String DIR_POLICY_BUNDLE_PATH = "policyBundle";
    public static final String EXT = ".zip";
    private static final String EXTRA_CONTENT_PATH = "CONTENT_PATH";
    private static final String EXTRA_REQUIRED_HASH = "REQUIRED_HASH";
    private static final String EXTRA_SIGNATURE = "SIGNATURE";
    private static final String EXTRA_VERSION_NUMBER = "VERSION";
    public static final String EXT_ALT_FAIL = ".failure";
    public static final String EXT_ALT_OK = ".success";
    public static final String EXT_FAIL = ".zip.failure";
    public static final String EXT_OK = ".zip.success";
    public static final String FILE_NAME_PREFIX = "OTA_Package_";
    public static final String FILE_PERMISSION = "644";
    private static final int MAX_READ_SIZE = 12288;
    public static final String MODEM_FILE_NAME_PREFIX = "Modem_Package_";
    private static final String NOT_AVAILABLE = "not_available";
    public static final String OTADIR = "otadir";
    private static final String OTA_RESERVED_SPACE_CONFIG_PATH = "/etc/motorola/com.motorola.ccc.ota/reserved_space.txt";
    private static final String OTA_RESERVED_SPACE_DIR = "reservedSpace";
    private static final String OTA_RESERVED_SPACE_FILENAME = "dummy";
    private static final String OTA_RESERVED_SPACE_FILE_EXTN = "reserved";
    public static final String OTA_VERSION = "Ota_Version";
    private static final String PACKAGE_DOWNLOAD_PATH = "misc_ne";
    public static final String PAYLOAD_METADATA_BIN_FILE = "payload_metadata.bin";
    private static final String PREVIOUS_DL_PATH = "download";
    private static final int RESERVE_FILES_CREATED = 1;
    private static final int RESERVE_FILES_DELETED = 2;
    private static final int RESERVE_FILES_NO_CHANGE = 0;
    public static final String SDCARD = "sdcard";
    public static final String SD_CARD_DIR = "/";
    private static final String SE_BUNDLE_METADATA_FILE = "update_bundle_metadata";
    private static final int SIZE_OF_EACH_FILE_MB = 50;
    public static final String UE_LOG_STORAGE_URL = "https://store-ota.svcmot.com/";
    public static final String UPDATER_ENGINE_FOLDER = "/data/misc/update_engine_log";
    public static final String UPDATER_ENGINE_LOG_FILE = "/data/misc/update_engine_log/update_engine.log";
    public static final String VALIDATION = "validation";

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum ReturnCode {
        OK,
        PACKAGE_PATH_FAIL_CACHE,
        PACKAGE_PATH_FAIL_DATA,
        PACKAGE_PATH_FAIL_SDCARD
    }

    public static String returnOtaPackageName() {
        if (UpdateType.DIFFUpdateType.SMR.toString().equalsIgnoreCase(UpdaterUtils.getUpdateType(new BotaSettings().getString(Configs.METADATA)))) {
            return FILE_NAME_PREFIX + BuildPropReader.getBuildId() + "_s";
        }
        return FILE_NAME_PREFIX + BuildPropReader.getBuildId();
    }

    public static final String getDataDirectory() {
        return "/data/";
    }

    public static final String getDownloadDataDirectory() {
        return getDataDirectory() + PACKAGE_DOWNLOAD_PATH;
    }

    public static final String getPrevDownloadDirectory() {
        return BuildPropReader.isUEUpdateEnabled() ? getDataDirectory() + AB_PACKAGE_DOWNLOAD_PATH : OtaApplication.getGlobalContext().getDir(PREVIOUS_DL_PATH, 0).toString();
    }

    public static final File getExternalScopedStorage() {
        return OtaApplication.getGlobalContext().getExternalFilesDir(null);
    }

    public static final boolean isExternalScopedStorageMounted() {
        return "mounted".equals(Environment.getExternalStorageState(getExternalScopedStorage()));
    }

    public static final String getExternalScopedStorageOtaDir() {
        return getExternalScopedStorage().getAbsolutePath() + "/otadir/";
    }

    public static final String getLocalPath(BotaSettings botaSettings) {
        if (DATA.equals(botaSettings.getPackageDownloadLocation())) {
            return getDownloadDataDirectory() + SD_CARD_DIR + returnOtaPackageName() + EXT;
        }
        if ("sdcard".equals(botaSettings.getPackageDownloadLocation()) && isExternalScopedStorageMounted()) {
            return getExternalScopedStorageOtaDir() + returnOtaPackageName() + EXT;
        }
        return getDownloadDataDirectory() + SD_CARD_DIR + returnOtaPackageName() + EXT;
    }

    public static String getModemDownloadFilePath() {
        String modemDownloadDirectoryPath = getModemDownloadDirectoryPath();
        File file = new File(modemDownloadDirectoryPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return modemDownloadDirectoryPath + MODEM_FILE_NAME_PREFIX + BuildPropReader.getBuildId() + "_" + BuildPropReader.getDeviceModemConfigVersion() + "_ModemConfig.zip";
    }

    private static String getModemDownloadDirectoryPath() {
        return OtaApplication.getGlobalContext().getFilesDir().toString() + "/modem/";
    }

    public static final void checkAndCreateReserveSpace(long j) {
        if (isNoOfFilesProper(j)) {
            Logger.debug("OtaApp", "checkAndCreateReserveSpace: already allocated required reserve space");
            return;
        }
        Logger.debug("OtaApp", "checkAndCreateReserveSpace: Attempting to adjust space with " + j + " MB");
        if (reserveSpaceDirExists()) {
            adjustReserveSpaceFiles(j);
        } else {
            createReserveSpace(1L, getNumReserveFilesToCreate(j));
        }
    }

    public static String getPayloadMetaDataFileName() {
        return getDownloadDataDirectory() + "/payload_metadata.bin";
    }

    public static final boolean isNoOfFilesProper(long j) {
        return j > 0 && reserveSpaceDirExists() && getNumReserveFilesToCreate(j) == getNumFilesInDir();
    }

    public static final boolean reserveSpaceDirExists() {
        return new File("/data/data/" + OtaApplication.getGlobalContext().getPackageName() + "/app_reservedSpace").exists();
    }

    public static final String getPackageFilePathForA2B() {
        return getDownloadDataDirectory() + SD_CARD_DIR + returnOtaPackageName() + EXT;
    }

    public static final boolean deleteReserveSpaceFiles(long j) {
        if (getNumFilesInDir() == 0) {
            Logger.debug("OtaApp", "Nothing to delete");
            return false;
        }
        String str = null;
        try {
            long numFilesInDir = getNumFilesInDir();
            for (long j2 = numFilesInDir; j2 > numFilesInDir - j; j2--) {
                str = OtaApplication.getGlobalContext().getDir(OTA_RESERVED_SPACE_DIR, 0).toString() + SD_CARD_DIR + OTA_RESERVED_SPACE_FILENAME + "-" + j2 + "." + OTA_RESERVED_SPACE_FILE_EXTN;
                org.apache.commons.io.FileUtils.forceDelete(new File(str));
            }
            return true;
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception " + e + ", deleting file: " + str);
            return false;
        }
    }

    public static final int adjustReserveSpaceFiles(long j) {
        long calculateNumFilesNeeded = calculateNumFilesNeeded(j);
        long numFilesInDir = getNumFilesInDir();
        int i = (calculateNumFilesNeeded > numFilesInDir ? 1 : (calculateNumFilesNeeded == numFilesInDir ? 0 : -1));
        if (i > 0) {
            createReserveSpace(numFilesInDir + 1, calculateNumFilesNeeded);
            return 1;
        } else if (i < 0) {
            deleteReserveSpaceFiles(numFilesInDir - calculateNumFilesNeeded);
            return 2;
        } else {
            return 0;
        }
    }

    public static final long getNumReserveFilesToCreate(long j) {
        if (j > 0) {
            return calculateNumFilesNeeded(j);
        }
        return 0L;
    }

    public static final long getReservedSpaceValue() {
        File file = new File(Environment.getRootDirectory(), OTA_RESERVED_SPACE_CONFIG_PATH);
        long otaReservedSpaceValue = BuildPropReader.getOtaReservedSpaceValue();
        if (otaReservedSpaceValue <= 0) {
            if (file.exists()) {
                try {
                    otaReservedSpaceValue = Long.parseLong(org.apache.commons.io.FileUtils.readFileToString(file).replaceAll("[^0-9]", ""));
                } catch (Exception e) {
                    Logger.error("OtaApp", "Reading config file, exception " + e);
                    return -1L;
                }
            } else if (BuildPropReader.doesDeviceSupportVABUpdate()) {
                Logger.debug("OtaApp", "ReserveSpace data doesn't exist. Setting default values for vab");
                otaReservedSpaceValue = BuildPropReader.isFotaATT() ? 3072L : 1024L;
            } else {
                otaReservedSpaceValue = -1;
            }
        }
        if (BuildPropReader.doesDeviceSupportVABc()) {
            return 0L;
        }
        return otaReservedSpaceValue;
    }

    public static final long calculateNumFilesNeeded(long j) {
        long j2 = j / 50;
        return j % 50 > 0 ? j2 + 1 : j2;
    }

    public static final long getNumFilesInDir() {
        try {
            return org.apache.commons.io.FileUtils.listFiles(new File(OtaApplication.getGlobalContext().getDir(OTA_RESERVED_SPACE_DIR, 0).toString()), (String[]) null, false).size();
        } catch (Exception e) {
            Logger.error("OtaApp", "Exception counting ReserveSpace files" + e);
            return 0L;
        }
    }

    public static final long getAvailableReserveSpace() {
        return getNumFilesInDir() * 52428800;
    }

    public static final boolean createReserveSpace(long j, long j2) {
        byte[] bArr = new byte[DEFAULT_DATA_SPACE_REQUIRED];
        while (j <= j2) {
            String str = OtaApplication.getGlobalContext().getDir(OTA_RESERVED_SPACE_DIR, 0).toString() + "/dummy-" + j + ".reserved";
            File file = new File(str);
            try {
                if (file.exists()) {
                    Logger.debug("OtaApp", str + " already exists. Skipping");
                } else {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(bArr);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                j++;
            } catch (Exception e) {
                Logger.error("OtaApp", "Exception while creating ReserveSpace files" + e + ".\nRequired space of " + (((j2 - j) + 1) * 50) + " MB, Not found. \n Deleting the partial file: " + str);
                try {
                    org.apache.commons.io.FileUtils.forceDelete(file);
                } catch (Exception unused) {
                }
                return false;
            }
        }
        return true;
    }

    public static final boolean isSuccessFile(String str) {
        return str.endsWith(EXT_OK) || str.endsWith(EXT_ALT_OK);
    }

    public static final long getFileSize(String str) {
        return new File(str).length();
    }

    public static void cleanupFiles() {
        cleanupDataFiles();
    }

    public static void cleanupModemFiles() {
        File[] listFiles;
        File file = new File(getModemDownloadDirectoryPath());
        if (file.exists() && file.isDirectory() && (listFiles = file.listFiles()) != null) {
            for (File file2 : listFiles) {
                try {
                    org.apache.commons.io.FileUtils.forceDelete(file2);
                } catch (Exception e) {
                    Logger.error("OtaApp", "FileUtils.cleanupModemFiles, exception : " + e);
                }
            }
        }
    }

    private static void cleanupDataFiles() {
        try {
            for (File file : org.apache.commons.io.FileUtils.listFiles(new File(getDownloadDataDirectory()), new String[]{"zip", "crc", "bin"}, false)) {
                Logger.debug("OtaApp", "FileUtils.cleanupDataFiles, filename " + file.getName());
                org.apache.commons.io.FileUtils.forceDelete(file);
            }
        } catch (Exception e) {
            Logger.error("OtaApp", "FileUtils.cleanupDataFiles, exception : " + e);
        }
        File file2 = new File(getPrevDownloadDirectory());
        if (file2.exists()) {
            try {
                for (File file3 : org.apache.commons.io.FileUtils.listFiles(file2, new String[]{"zip", "crc"}, false)) {
                    Logger.debug("OtaApp", "FileUtils.cleanupDataFiles, filename " + file3.getName());
                    org.apache.commons.io.FileUtils.forceDelete(file3);
                }
            } catch (Exception e2) {
                Logger.error("OtaApp", "FileUtils.cleanupDataFiles, exception : " + e2);
            }
        }
    }

    public static boolean isSpaceAvailableData(ApplicationEnv applicationEnv, long j) {
        if (j <= 0.0d) {
            Logger.debug("OtaApp", "Extra Space value defaulted to : 52428800");
            j = 52428800;
        } else {
            Logger.debug("OtaApp", "Extra Space required for update is : " + j);
        }
        boolean isSpaceAvailable = applicationEnv.getUtilities().isSpaceAvailable(getDataDirectory(), j);
        Logger.debug("OtaApp", "isSpaceAvailableData returning: " + isSpaceAvailable);
        return isSpaceAvailable;
    }

    public static boolean isSpaceAvailableScopedStorage(ApplicationEnv applicationEnv, long j) {
        if (isExternalScopedStorageMounted()) {
            return applicationEnv.getUtilities().isSpaceAvailable(getExternalScopedStorage().getAbsolutePath(), j);
        }
        return false;
    }

    public static final List<String> getUpgradeFileList(String str) {
        String[] list = new File(str).list(new FilenameFilter() { // from class: com.motorola.ccc.ota.utils.FileUtils.1
            @Override // java.io.FilenameFilter
            public boolean accept(File file, String str2) {
                return str2.endsWith(FileUtils.EXT) && str2.contains(FileUtils.OTA_VERSION);
            }
        });
        if (list == null) {
            Logger.warn("OtaApp", "CusFileUtils.getUpgradeFileList failed: list of files is null sdcard not mounted or io error occured");
            return null;
        } else if (list.length == 0) {
            return null;
        } else {
            LinkedList linkedList = new LinkedList();
            for (int i = 0; i < list.length; i++) {
                if (list[i].contains(VALIDATION)) {
                    linkedList.add(0, list[i]);
                } else {
                    linkedList.add(list[i]);
                }
            }
            return linkedList;
        }
    }

    private static final String getFailureDetailsFromFile(String str) {
        return CusFrameworkDeps.readTextFile(new File(str), -12288, "\n...\n");
    }

    public static final String getFailureDetailsFromFileToUpload(String str, String str2) {
        String failureDetailsFromFile = getFailureDetailsFromFile(str);
        if (str2 != null) {
            failureDetailsFromFile = getFailureDetailsFromFile(str) + str2;
        }
        if (failureDetailsFromFile != null) {
            try {
                return failureDetailsFromFile.length() > 6000 ? failureDetailsFromFile.substring(failureDetailsFromFile.length() - 6000) : failureDetailsFromFile;
            } catch (IndexOutOfBoundsException e) {
                Logger.error("OtaApp", "FileUtils:getFailureDetailsFromFileToUpload(): caught exception " + e);
                return failureDetailsFromFile;
            }
        }
        return failureDetailsFromFile;
    }

    public static final String copy(String str, String str2) {
        try {
            File file = new File(str);
            String copy = copy(str, str2, 0L, file.length());
            if (copy == null) {
                File file2 = new File(str2);
                if (file.length() == file2.length()) {
                    return null;
                }
                throw new Exception(String.format("copy: size does not match: %s %s %s/%s", str, str2, Long.valueOf(file.length()), Long.valueOf(file2.length())));
            }
            return copy;
        } catch (Exception e) {
            return "src:" + str + " dest:" + str2 + "error:" + e.toString();
        }
    }

    public static final String copy(String str, String str2, long j, long j2) {
        try {
            FileInputStream fileInputStream = new FileInputStream(str);
            FileChannel channel = fileInputStream.getChannel();
            FileOutputStream fileOutputStream = new FileOutputStream(str2, true);
            long transferTo = channel.transferTo(j, j2, fileOutputStream.getChannel());
            fileInputStream.close();
            fileOutputStream.close();
            if (transferTo != j2) {
                return String.format("src: %s dest: %s offset: %s size: %s actual transfered: %s", str, str2, Long.valueOf(j), Long.valueOf(j2), Long.valueOf(transferTo));
            }
            return null;
        } catch (Exception e) {
            return String.format("src: %s dest: %s offset: %s size: %s Exception: %s", str, str2, Long.valueOf(j), Long.valueOf(j2), e.toString());
        }
    }

    public static final void cleanScopedStorageFiles() {
        if (isExternalScopedStorageMounted()) {
            File file = new File(getExternalScopedStorageOtaDir());
            if (!file.exists()) {
                Logger.debug("OtaApp", "FileUtils.cleanSDCardFiles,otaDir path doesn't exists " + file);
                return;
            }
            try {
                Logger.info("OtaApp", "FileUtils.cleanSDCardFiles, cleanup dir : " + file);
                org.apache.commons.io.FileUtils.deleteDirectory(file);
            } catch (Exception e) {
                Logger.error("OtaApp", "FileUtils.cleanSDCardFiles, exception : " + e);
            }
        }
    }

    public static final void cleanupPrefs(ApplicationEnv applicationEnv, BotaSettings botaSettings) {
        botaSettings.removeConfig(Configs.SDCARD_LOCATION);
        botaSettings.removeConfig(Configs.SERVICE_CONTROL_RESPONSE);
        botaSettings.removeConfig(Configs.METADATA);
        botaSettings.removeConfig(Configs.METADATA_ORIGINAL);
        botaSettings.removeConfig(Configs.METADATA_VERSION);
        botaSettings.removeConfig(Configs.METADATA_FILE);
        botaSettings.removeConfig(Configs.DOWNLOAD_DESCRIPTOR);
        botaSettings.removeConfig(Configs.DOWNLOAD_DESCRIPTOR_TIME);
        botaSettings.removeConfig(Configs.FLAVOUR);
        botaSettings.removeConfig(Configs.UPGRADE_STATUS_VERIFY);
        botaSettings.removeConfig(Configs.TRIGGERED_BY);
        botaSettings.removeConfig(Configs.UPGRADE_ATTEMPT_COUNT);
        botaSettings.removeConfig(Configs.OTA_DOWNLOAD_RETRY_ATTEMPTS);
        botaSettings.removeConfig(Configs.OTA_DOWNLOAD_EXCEPTION_RETRY_ATTEMPTS);
        botaSettings.removeConfig(Configs.OTA_GET_DESCRIPTOR_REASON);
        botaSettings.removeConfig(Configs.TRACKINGID);
        botaSettings.removeConfig(Configs.REPORTINGTAGS);
        botaSettings.removeConfig(Configs.CONTENT_TIMESTAMP);
        botaSettings.removeConfig(Configs.PACKAGE_DOWNLOAD_PATH);
        botaSettings.removeConfig(Configs.DOWNLOAD_COMPLETED);
        botaSettings.removeConfig(Configs.FORCE_UPGRADE_TIME_COMPLETED);
        botaSettings.removeConfig(Configs.FOTA_ORIGINAL_FORCED);
        botaSettings.removeConfig(Configs.MAX_FORCE_INSTALL_DEFER_TIME.key());
        botaSettings.removeConfig(Configs.MANDATORY_INSTALL_TIME.key());
        botaSettings.removeConfig(Configs.MAX_CRITICAL_UPDATE_DEFER_TIME.key());
        botaSettings.removeConfig(Configs.MAX_CRITICAL_UPDATE_EXTENDED_TIME);
        botaSettings.removeConfig(Configs.SMART_UPDATE_MIN_INSTALL_TIME.key());
        botaSettings.removeConfig(Configs.SMART_UPDATE_MAX_INSTALL_TIME.key());
        botaSettings.removeConfig(Configs.SCREEN_ANIMATION_VIEW);
        botaSettings.setBoolean(Configs.IS_UPDATE_PENDING_ON_REBOOT, false);
        cleanWifiDiscoveryTimer(applicationEnv, botaSettings);
        cleanForceUpgradeTimer(applicationEnv, botaSettings);
        cleanForceDownloadTimer(applicationEnv, botaSettings);
        botaSettings.clearBestTimesForIntelligentNotification();
        botaSettings.removeConfig(Configs.ADVANCE_NOTICE_URL.key());
        botaSettings.removeEndOfLifeInfo();
        botaSettings.removeConfig(Configs.NEXT_CRITICAL_UPDATE_PROMPT_TIME.key());
        botaSettings.removeConfig(Configs.STORED_AB_STATUS.key());
        botaSettings.removeConfig(Configs.STORED_AB_PROGRESS_PERCENT.key());
        botaSettings.removeConfig(Configs.AUTOMATIC_DOWNLOAD_FOR_CELLULAR);
        botaSettings.removeConfig(Configs.VERIFY_PAYLOAD_STATUS_CHECK.key());
        botaSettings.setString(Configs.WIFI_DISCOVERY_EXPIRED_FOR_FOTA, "none");
        botaSettings.setInt(Configs.WIFI_PROMPT_COUNT_FOR_FOTA, 0);
        botaSettings.setBoolean(Configs.ALLOW_ON_ROAMING, false);
        botaSettings.setBoolean(Configs.BATTERY_LOW, false);
        botaSettings.removeConfig(Configs.HEALTH_CHECK_TIME);
        botaSettings.removeConfig(Configs.SHOULD_BLOCK_FULL_SCREEN_DISPLAY);
        botaSettings.removeConfig(Configs.UPDATING_VALIDATION_FILE);
        botaSettings.removeConfig(Configs.RESERVE_SPACE_CLEAR_VAB);
        botaSettings.removeConfig(Configs.VITAL_UPDATE_CANCEL_REASON);
        botaSettings.removeConfig(Configs.WINDOW_POLICY_START_TIMESTAMP);
        botaSettings.removeConfig(Configs.WINDOW_POLICY_END_TIMESTAMP);
        botaSettings.removeConfig(Configs.SERVER_FOTA_TRANSPORTMEDIA_VALUE);
        botaSettings.removeConfig(Configs.RESTART_EXPIRY_TIMER);
        botaSettings.removeConfig(Configs.SMART_UPDATE_ENABLE_BY_SERVER);
        botaSettings.removeConfig(Configs.STATS_VAB_MERGE_REBOOT_FAILURE_COUNT);
        botaSettings.removeConfig(Configs.VAB_MERGE_PROCESS_RUNNING);
    }

    public static final void cleanupModemPrefs(BotaSettings botaSettings) {
        botaSettings.removeConfig(Configs.MODEM_METADATA_FILE);
        botaSettings.removeConfig(Configs.MODEM_CONTENT_TIMESTAMP);
        botaSettings.removeConfig(Configs.MODEM_TRACKINGID);
        botaSettings.removeConfig(Configs.MODEM_REPORTINGTAGS);
        botaSettings.removeConfig(Configs.MODEM_DOWNLOAD_DESCRIPTOR);
        botaSettings.removeConfig(Configs.MODEM_DOWNLOAD_DESCRIPTOR_TIME);
        botaSettings.removeConfig(Configs.MODEM_METADATA_FILE);
        botaSettings.removeConfig(Configs.MODEM_METADATA_ORIGINAL);
        botaSettings.removeConfig(Configs.MODEM_METADATA);
        botaSettings.removeConfig(Configs.MODEM_FILE_DL_NOTIFIED_TIMESTAMP);
        botaSettings.removeConfig(Configs.MODEM_FILE_DL_EXPIRED_TIMESTAMP);
        botaSettings.removeConfig(Configs.MODEM_SERVICE_CONTROL_RESPONSE);
        botaSettings.removeConfig(Configs.MODEM_DOWNLOAD_COMPLETED);
        botaSettings.removeConfig(Configs.MODEM_GET_DESCRIPTOR_REASON);
    }

    public static final void createScopedStorageOtaDir() {
        if (isExternalScopedStorageMounted()) {
            File file = new File(getExternalScopedStorageOtaDir());
            if (file.exists()) {
                return;
            }
            file.mkdirs();
        }
    }

    private static void cleanWifiDiscoveryTimer(ApplicationEnv applicationEnv, BotaSettings botaSettings) {
        if (botaSettings.getLong(Configs.WIFI_DISCOVER_TIME, -1L) > 0) {
            botaSettings.removeConfig(Configs.WIFI_DISCOVER_TIME);
            applicationEnv.getUtilities().unRegisterWithWiFiDiscoveryManager();
        }
    }

    private static void cleanForceUpgradeTimer(ApplicationEnv applicationEnv, BotaSettings botaSettings) {
        long j = botaSettings.getLong(Configs.FORCE_UPGRADE_TIME, -1L);
        Logger.debug("OtaApp", "FileUtils.cleanForceUpgradeTimer,  forceUpgradeTime = " + j);
        if (j > 0) {
            botaSettings.removeConfig(Configs.FORCE_UPGRADE_TIME);
            applicationEnv.getUtilities().unRegisterWithForceUpgradeManager();
        }
    }

    private static void cleanForceDownloadTimer(ApplicationEnv applicationEnv, BotaSettings botaSettings) {
        Logger.debug("OtaApp", "FileUtils.cleanForceDownloadTimer,  maxForceDownloadDeferTime = " + botaSettings.getLong(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME, -1L));
        botaSettings.removeConfig(Configs.MAX_FORCE_DOWNLOAD_DEFER_TIME);
        applicationEnv.getUtilities().unRegisterWithForceUpgradeManager();
    }

    @Deprecated
    public static void silentInstall(String str) {
        if (!new File(str).exists()) {
            Logger.info("OtaApp", "FileUtils.silentInstall, zip file path : " + str + " doesn't exists");
            cleanupFiles();
            return;
        }
        Logger.info("OtaApp", "FileUtils.silentInstall, zip filePath: " + str);
        File apkDirPath = getApkDirPath();
        try {
            ZipUtils.extract(new File(str), apkDirPath);
            cleanupFiles();
            setPermission(apkDirPath.toString(), DIR_PERMISSION);
            if (!apkDirPath.exists()) {
                Logger.info("OtaApp", "FileUtils.silentInstall, no apk dir: " + apkDirPath);
                return;
            }
            Iterator iterateFiles = org.apache.commons.io.FileUtils.iterateFiles(apkDirPath, new String[]{"apk"}, true);
            while (iterateFiles.hasNext()) {
                File file = (File) iterateFiles.next();
                Logger.info("OtaApp", "FileUtils.silentInstall, silent install for " + file.toString() + " initiated");
                setPermission(file.toString(), FILE_PERMISSION);
                install(file.toString());
            }
            cleanupApkDir(apkDirPath);
        } catch (Exception e) {
            Logger.error("OtaApp", "FileUtils.silentInstall, extract gave exception " + e);
            cleanupFiles();
        }
    }

    public static File getApkDirPath() {
        return OtaApplication.getGlobalContext().getDir(DIR_APK_PATH, 0);
    }

    public static synchronized void setPermission(String str, String str2) {
        synchronized (FileUtils.class) {
            try {
                Process exec = Runtime.getRuntime().exec("chmod " + str2 + SystemUpdateStatusUtils.SPACE + str);
                exec.waitFor();
                if (exec.exitValue() == 0) {
                    Logger.info("OtaApp", "permission set successfully for : " + str);
                }
            } catch (Exception e) {
                Logger.error("OtaApp", "FileUtils.setPermission, exception " + e);
            }
        }
    }

    private static synchronized void install(String str) {
        synchronized (FileUtils.class) {
            try {
                Process exec = Runtime.getRuntime().exec("/system/bin/pm install -r " + str + SystemUpdateStatusUtils.NEWLINE_SEPERATOR);
                exec.waitFor();
                if (exec.exitValue() == 0) {
                    Logger.info("OtaApp", "apk install success for : " + str);
                }
            } catch (Exception e) {
                Logger.error("OtaApp", "FileUtils.install, exception " + e);
            }
        }
    }

    private static final void cleanupApkDir(File file) {
        if (!file.exists()) {
            Logger.debug("OtaApp", "FileUtils.cleanupApkDir, nothing left to clean");
            return;
        }
        try {
            Logger.info("OtaApp", "FileUtils.cleanupApkDir, cleanup dir : " + file);
            org.apache.commons.io.FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            Logger.error("OtaApp", "FileUtils.cleanupApkDir, exception : " + e);
        }
    }

    public static ReturnCode setPackageDownloadLocation(ApplicationEnv applicationEnv, long j, long j2, BotaSettings botaSettings, String str) {
        if (isSpaceAvailableData(applicationEnv, j2 + j)) {
            Logger.info("OtaApp", "packgeDownloadLocation set to data");
            botaSettings.setString(Configs.PACKAGE_DOWNLOAD_PATH, DATA);
            return ReturnCode.OK;
        } else if (UpdaterUtils.isFeatureOn(botaSettings.getString(Configs.DOWNLOAD_SDCARD_FEATURE))) {
            if (isSpaceAvailableScopedStorage(applicationEnv, j)) {
                Logger.info("OtaApp", "packgeDownloadLocation set to sdcard");
                botaSettings.setString(Configs.PACKAGE_DOWNLOAD_PATH, "sdcard");
                if (!"sdcard".equalsIgnoreCase(botaSettings.getString(Configs.SDCARD_LOCATION))) {
                    applicationEnv.getUtilities().sendUpdateDownloadStatusError(str, UpgradeUtils.DownloadStatus.STATUS_SDCARD_RESOURCES_WARNING);
                }
                botaSettings.setString(Configs.SDCARD_LOCATION, "sdcard");
                createScopedStorageOtaDir();
                return ReturnCode.OK;
            }
            return ReturnCode.PACKAGE_PATH_FAIL_SDCARD;
        } else {
            return ReturnCode.PACKAGE_PATH_FAIL_DATA;
        }
    }

    public static void doPolicyBundleUpdate(String str) {
        if (!new File(str).exists()) {
            Logger.info("OtaApp", "FileUtils.doPolicyBundleUpdate, zip file path : " + str + " doesn't exists");
            cleanupFiles();
            return;
        }
        Logger.info("OtaApp", "FileUtils.doPolicyBundleUpdate, zip filePath: " + str);
        File policyBundleDirPath = getPolicyBundleDirPath();
        try {
            ZipUtils.extract(new File(str), policyBundleDirPath);
            Logger.info("OtaApp", "FileUtils.doPolicyBundleUpdate, policyBundleDir : " + policyBundleDirPath);
            cleanupFiles();
            if (!policyBundleDirPath.exists()) {
                Logger.info("OtaApp", "FileUtils.doPolicyBundleUpdate, no policyBundle dir: " + policyBundleDirPath);
                return;
            }
            Intent intent = new Intent();
            Uri uriForFile = FileProvider.getUriForFile(OtaApplication.getGlobalContext(), "com.motorola.ccc.ota.fileprovider", new File(new File(OtaApplication.getGlobalContext().getCacheDir(), DIR_POLICY_BUNDLE_PATH), "update_bundle"));
            OtaApplication.getGlobalContext().grantUriPermission("com.motorola.android.sepolicyupdate", uriForFile, 3);
            intent.setDataAndType(uriForFile, "*/*");
            intent.setAction(ACTION_UPDATE_SEPOLICY);
            intent.putExtra(EXTRA_CONTENT_PATH, policyBundleDirPath.toString() + "/update_bundle");
            String readFromMetadata = readFromMetadata(policyBundleDirPath);
            String value = getValue(readFromMetadata, EXTRA_REQUIRED_HASH);
            String value2 = getValue(readFromMetadata, EXTRA_SIGNATURE);
            String value3 = getValue(readFromMetadata, EXTRA_VERSION_NUMBER);
            intent.putExtra(EXTRA_REQUIRED_HASH, value);
            intent.putExtra(EXTRA_SIGNATURE, value2);
            intent.putExtra(EXTRA_VERSION_NUMBER, value3);
            OtaApplication.getGlobalContext().sendBroadcast(intent.setFlags(3));
            StringBuilder sb = new StringBuilder("FileUtils.doPolicyBundleUpdate:");
            sb.append(policyBundleDirPath.toString() + "/update_bundle");
            sb.append(SmartUpdateUtils.MASK_SEPARATOR);
            sb.append(value);
            sb.append(SmartUpdateUtils.MASK_SEPARATOR);
            sb.append(value2);
            sb.append(SmartUpdateUtils.MASK_SEPARATOR);
            sb.append(value3);
            Logger.debug("OtaApp", sb.toString());
        } catch (Exception e) {
            Logger.error("OtaApp", "FileUtils.doPolicyBundleUpdate, extract gave exception " + e);
            cleanupFiles();
            cleanupPolicyBundleDir(policyBundleDirPath);
        }
    }

    public static File getPolicyBundleDirPath() {
        return new File(OtaApplication.getGlobalContext().getCacheDir().toString() + "/policyBundle");
    }

    public static final void cleanupPolicyBundleDir(File file) {
        if (!file.exists()) {
            Logger.debug("OtaApp", "FileUtils.cleanupPolicyBundleDir, nothing left to clean");
            return;
        }
        try {
            Logger.info("OtaApp", "FileUtils.cleanupPolicyBundleDir, cleanup dir : " + file);
            org.apache.commons.io.FileUtils.deleteDirectory(file);
        } catch (Exception e) {
            Logger.error("OtaApp", "FileUtils.cleanupPolicyBundleDir, exception : " + e);
        }
    }

    private static String readFromMetadata(File file) {
        try {
            Iterator it = org.apache.commons.io.FileUtils.listFiles(file, FileFilterUtils.nameFileFilter(SE_BUNDLE_METADATA_FILE), (IOFileFilter) null).iterator();
            if (it.hasNext()) {
                File file2 = (File) it.next();
                Logger.debug("OtaApp", "FileUtils.readFromMetadta, filename " + file2.getName());
                return org.apache.commons.io.FileUtils.readFileToString(file2);
            }
            return NOT_AVAILABLE;
        } catch (Exception e) {
            Logger.error("OtaApp", "FileUtils.readFromMetadta, exception : " + e);
            return NOT_AVAILABLE;
        }
    }

    private static String getValue(String str, String str2) {
        try {
        } catch (Exception e) {
            Logger.error("OtaApp", "FileUtils.getValue, exception : " + e);
        }
        if (NOT_AVAILABLE.equals(str)) {
            return NOT_AVAILABLE;
        }
        String[] split = str.split(SmartUpdateUtils.MASK_SEPARATOR);
        if (EXTRA_REQUIRED_HASH.equals(str2)) {
            return split[0];
        }
        if (EXTRA_SIGNATURE.equals(str2)) {
            return split[1];
        }
        if (EXTRA_VERSION_NUMBER.equals(str2)) {
            return split[2];
        }
        return NOT_AVAILABLE;
    }

    public static boolean isDataMemoryLow(ApplicationEnv applicationEnv, long j, long j2) {
        if (j2 <= 0.0d) {
            j2 = 52428800;
        }
        long spaceAvailable = applicationEnv.getUtilities().getSpaceAvailable(getDataDirectory());
        Logger.debug("OtaApp", "FileUtils.isDataMemoryLow, availableDataSpace: " + spaceAvailable + "\nReserveSpace: " + getAvailableReserveSpace() + "\nRequired extraDataSpace: " + j2);
        return spaceAvailable + getAvailableReserveSpace() < j + j2;
    }

    public static String getUELogFileName() {
        String string = new BotaSettings().getString(Configs.LOG_FILE_UPLOAD_COUNT);
        return BuildPropertyUtils.getId(OtaApplication.getGlobalContext()) + "-" + string + "-" + new Timestamp(System.currentTimeMillis()).toString().replaceAll("\\s", "-") + "-updateEngineLog.zip";
    }

    public static String getUELogFileStorageURL() {
        return UE_LOG_STORAGE_URL + getUELogFileName();
    }

    public static void deleteFile(String str) {
        try {
            File file = new File(str);
            if (file.exists()) {
                Logger.debug("OtaApp", "FileUtils.deleteFile, filename " + file.getName());
                org.apache.commons.io.FileUtils.forceDelete(file);
            }
        } catch (Exception unused) {
            Logger.debug("OtaApp", "FileUtils, failed to delete file: " + str);
        }
    }

    public static String getUEZipFilePath() {
        return getDownloadDataDirectory() + "/update_engine.zip";
    }
}
