package com.motorola.ccc.ota.sources.sdcard;

import android.os.PowerManager;
import com.motorola.ccc.ota.sources.UpgradeSourceType;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.otalib.common.Environment.ApplicationEnv;
import com.motorola.otalib.common.Environment.DownloadHandler;
import java.io.File;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class SDCardDownloadHandler implements DownloadHandler {
    private ApplicationEnv _env;
    private volatile boolean _isCopying;
    private PowerManager manager;
    private BotaSettings settings;

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void radioGotDown() {
    }

    public SDCardDownloadHandler(PowerManager powerManager, ApplicationEnv applicationEnv, BotaSettings botaSettings) {
        this.manager = powerManager;
        this._env = applicationEnv;
        this.settings = botaSettings;
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void transferUpgrade(ApplicationEnv.Database.Descriptor descriptor) {
        if (this._isCopying) {
            Logger.error("OtaApp", "SDCardUpgradeSource.transferUpgrade: already one copy is in progress; cannot start a new one");
            return;
        }
        this._isCopying = true;
        copyFromSdCard(descriptor);
    }

    private void copyFromSdCard(ApplicationEnv.Database.Descriptor descriptor) {
        final String info = descriptor.getInfo();
        final String version = descriptor.getVersion();
        final String localPath = FileUtils.getLocalPath(this.settings);
        final String repository = descriptor.getRepository();
        final long fileSize = FileUtils.getFileSize(info);
        Logger.debug("OtaApp", "location: " + info + " version: " + version + " dest: " + localPath + " repo: " + repository + " size: " + fileSize);
        if (!repository.equals(UpgradeSourceType.sdcard.toString())) {
            this._isCopying = false;
            Logger.error("OtaApp", "SDCardUpgradeSource.copyFromSdCard.Runnable.run: internal programming defect");
            this._env.getUtilities().sendInternalNotification(version, UpgradeSourceType.sdcard.toString(), "SDCardUpgradeSource.copyFromSdCard.Runnable.run: internal programming defect");
        } else if (fileSize <= 0) {
            this._isCopying = false;
            this._env.getUtilities().sendInternalNotification(version, UpgradeSourceType.sdcard.toString(), info + " is empty or could not be opened");
        } else {
            new Thread(new Runnable() { // from class: com.motorola.ccc.ota.sources.sdcard.SDCardDownloadHandler.1
                @Override // java.lang.Runnable
                public void run() {
                    PowerManager.WakeLock newWakeLock;
                    File file;
                    PowerManager.WakeLock wakeLock = null;
                    String str = null;
                    wakeLock = null;
                    try {
                        try {
                            newWakeLock = SDCardDownloadHandler.this.manager.newWakeLock(1, "OtaApp");
                        } catch (Throwable th) {
                            th = th;
                        }
                    } catch (Exception e) {
                        e = e;
                    }
                    try {
                        newWakeLock.acquire();
                        Logger.info("OtaApp", "SDCardUpgradeSource.copyFromSdCard.Runnable.run: copying for Repo: " + repository + " from " + info + " to " + localPath + " of size " + fileSize);
                        long fileSize2 = FileUtils.getFileSize(localPath);
                        SDCardDownloadHandler.this._env.getUtilities().sendUpdateDownloadStatusProgress(version, fileSize2, fileSize, repository);
                        while (true) {
                            long j = fileSize;
                            if (fileSize2 >= j) {
                                break;
                            }
                            long j2 = j - fileSize2;
                            long j3 = j2 > 1048576 ? 1048576L : j2;
                            str = FileUtils.copy(info, localPath, fileSize2, j3);
                            if (str != null) {
                                break;
                            }
                            fileSize2 += j3;
                            SDCardDownloadHandler.this._env.getUtilities().sendUpdateDownloadStatusProgress(version, fileSize2, fileSize, repository);
                        }
                        if (str == null) {
                            if (new File(localPath).length() != fileSize) {
                                str = "File size missmatch [source " + info + " size " + fileSize + " ] [ destination " + localPath + " size " + file.length() + " ]";
                            }
                        }
                        if (str != null) {
                            Logger.error("OtaApp", "SDCardUpgradeSource.copyFromSdCard.Runnable.run: " + str);
                        } else {
                            Logger.info("OtaApp", "SDCardUpgradeSource.copyFromSdCard.Runnable.run: copy from " + info + " to " + localPath + " is successful");
                        }
                        SDCardDownloadHandler.this._env.getUtilities().sendInternalNotification(version, UpgradeSourceType.sdcard.toString(), str);
                        SDCardDownloadHandler.this._isCopying = false;
                        if (newWakeLock != null) {
                            newWakeLock.release();
                        }
                    } catch (Exception e2) {
                        e = e2;
                        wakeLock = newWakeLock;
                        SDCardDownloadHandler.this._env.getUtilities().sendInternalNotification(version, UpgradeSourceType.sdcard.toString(), "SDCardUpgradeSource.copyFromSdCard.Runnable.run: got exception: " + e.toString());
                        SDCardDownloadHandler.this._isCopying = false;
                        if (wakeLock != null) {
                            wakeLock.release();
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        wakeLock = newWakeLock;
                        SDCardDownloadHandler.this._isCopying = false;
                        if (wakeLock != null) {
                            wakeLock.release();
                        }
                        throw th;
                    }
                }
            }).start();
        }
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public boolean isBusy() {
        if (this._isCopying) {
            Logger.debug("OtaApp", "SDCardUpgradeSource.isBusy is busy");
        }
        return this._isCopying;
    }

    @Override // com.motorola.otalib.common.Environment.DownloadHandler
    public void close() {
        this._isCopying = false;
    }
}
