package com.motorola.ccc.ota.ui;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.FileUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.ZipUtils;
import com.motorola.otalib.downloadservice.utils.DownloadServiceSettings;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class FileUploadService {
    /* renamed from: -$$Nest$smuploadUELogFileToServer  reason: not valid java name */
    static /* bridge */ /* synthetic */ boolean m190$$Nest$smuploadUELogFileToServer() {
        return uploadUELogFileToServer();
    }

    public void uploadUEFailureFiles() {
        HandlerThread handlerThread = new HandlerThread("uploadUEFailureFiles");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper()) { // from class: com.motorola.ccc.ota.ui.FileUploadService.1
            @Override // android.os.Handler
            public void handleMessage(Message message) {
                super.handleMessage(message);
                FileUploadService.this.deleteZipFilePostUpload(((Boolean) message.obj).booleanValue());
            }
        };
        handler.post(new Runnable() { // from class: com.motorola.ccc.ota.ui.FileUploadService.2
            @Override // java.lang.Runnable
            public void run() {
                BotaSettings botaSettings = new BotaSettings();
                Logger.debug("OtaApp", "FileUploadService, uploadUEFailureFiles");
                Logger.debug("OtaApp", "FileUploadService: before upload, LOG_FILE_UPLOAD_COUNT_TODAY:" + botaSettings.getConfig(Configs.LOG_FILE_UPLOAD_COUNT_TODAY, "0") + " & LOG_FILE_UPLOAD_TIME:" + botaSettings.getConfig(Configs.PREV_LOG_FILE_UPLOAD_TIME, ""));
                Message message = new Message();
                message.obj = Boolean.valueOf(FileUploadService.m190$$Nest$smuploadUELogFileToServer());
                handler.sendMessage(message);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void deleteZipFilePostUpload(final boolean z) {
        new Handler(Looper.myLooper()).post(new Runnable() { // from class: com.motorola.ccc.ota.ui.FileUploadService.3
            @Override // java.lang.Runnable
            public void run() {
                BotaSettings botaSettings = new BotaSettings();
                FileUtils.deleteFile(FileUtils.getUEZipFilePath());
                Logger.debug("OtaApp", "FileUploadService, deleteZipFilePostUpload");
                if (z) {
                    Logger.debug("OtaApp", "FileUploadService, Successfully uploaded update engine log file to server");
                    Logger.debug("OtaApp", "FileUploadService: post upload,  LOG_FILE_UPLOAD_COUNT_TODAY:" + botaSettings.getConfig(Configs.LOG_FILE_UPLOAD_COUNT_TODAY, "0") + " & LOG_FILE_UPLOAD_TIME:" + botaSettings.getConfig(Configs.PREV_LOG_FILE_UPLOAD_TIME, ""));
                }
            }
        });
    }

    private static boolean uploadFileToServer(String str, String str2) {
        FileInputStream fileInputStream;
        HttpsURLConnection httpsURLConnection;
        DataOutputStream dataOutputStream;
        DataOutputStream dataOutputStream2 = null;
        try {
            fileInputStream = new FileInputStream(new File(str));
            httpsURLConnection = (HttpsURLConnection) new URL(str2).openConnection();
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setUseCaches(false);
            httpsURLConnection.setChunkedStreamingMode(1024);
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setConnectTimeout(DownloadServiceSettings.DOWNLOAD_SOCKET_TIMEOUT);
            httpsURLConnection.setReadTimeout(DownloadServiceSettings.DOWNLOAD_SOCKET_TIMEOUT);
            httpsURLConnection.setRequestProperty("Content-Type", "application/octet-stream");
            httpsURLConnection.setRequestProperty("X-Moto-Auth-Sign", "d928bee85b45cffe7b0f21084dd3d20e");
            httpsURLConnection.setRequestProperty("Secretkey", "SecretMOTOKey321");
            dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
        } catch (Exception e) {
            e = e;
        }
        try {
            int min = Math.min(fileInputStream.available(), 1024);
            byte[] bArr = new byte[min];
            int read = fileInputStream.read(bArr, 0, min);
            while (read > 0) {
                try {
                    try {
                        dataOutputStream.write(bArr, 0, min);
                        min = Math.min(fileInputStream.available(), 1024);
                        read = fileInputStream.read(bArr, 0, min);
                    } catch (OutOfMemoryError e2) {
                        Logger.debug("OtaApp", "FileUploadService, Exception in uploadFileToServer" + e2);
                        fileInputStream.close();
                        dataOutputStream.flush();
                        dataOutputStream.close();
                        return false;
                    }
                } catch (Exception e3) {
                    Logger.debug("OtaApp", "FileUploadService, Exception in uploadFileToServer" + e3);
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();
                    return false;
                }
            }
            int responseCode = httpsURLConnection.getResponseCode();
            String responseMessage = httpsURLConnection.getResponseMessage();
            Logger.debug("OtaApp", "FileUploadService, Server Response Code  " + responseCode);
            Logger.debug("OtaApp", "FileUploadService, Server Response Message " + responseMessage);
            fileInputStream.close();
            dataOutputStream.flush();
            dataOutputStream.close();
            if (responseCode != 200) {
                return false;
            }
            httpsURLConnection.getInputStream();
            InputStream inputStream = httpsURLConnection.getInputStream();
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                int read2 = inputStream.read();
                if (read2 != -1) {
                    stringBuffer.append((char) read2);
                } else {
                    String stringBuffer2 = stringBuffer.toString();
                    Logger.debug("OtaApp", "FileUploadService, response string is: " + stringBuffer2);
                    return "File Received".equalsIgnoreCase(stringBuffer2);
                }
            }
        } catch (Exception e4) {
            e = e4;
            dataOutputStream2 = dataOutputStream;
            Logger.debug("OtaApp", "FileUploadService, Send file Exception" + e.getMessage() + "");
            try {
                dataOutputStream2.flush();
                dataOutputStream2.close();
                return false;
            } catch (Exception unused) {
                return false;
            }
        }
    }

    private static boolean uploadUELogFileToServer() {
        String uEZipFilePath = FileUtils.getUEZipFilePath();
        if (ZipUtils.zipDirectory(new File(FileUtils.UPDATER_ENGINE_FOLDER), uEZipFilePath)) {
            Logger.debug("OtaApp", "FileUploadService, Uploading UE log file to server");
            String uELogFileStorageURL = FileUtils.getUELogFileStorageURL();
            if (!uploadFileToServer(uEZipFilePath, uELogFileStorageURL)) {
                Logger.verbose("OtaApp", "FileUploadService, Failed to upload UE log file to server URI: " + uELogFileStorageURL);
                return false;
            }
            BotaSettings botaSettings = new BotaSettings();
            botaSettings.incrementPrefs(Configs.LOG_FILE_UPLOAD_COUNT);
            botaSettings.incrementPrefs(Configs.LOG_FILE_UPLOAD_COUNT_TODAY);
            Logger.verbose("OtaApp", "FileUploadService, UE log file uploaded to server URI: " + uELogFileStorageURL);
            botaSettings.setString(Configs.PREV_LOG_FILE_UPLOAD_LINK, uELogFileStorageURL);
            botaSettings.setLong(Configs.PREV_LOG_FILE_UPLOAD_TIME, System.currentTimeMillis());
            return true;
        }
        Logger.debug("OtaApp", "FileUploadService, Failed to zip UE log file");
        return false;
    }

    public static boolean canUploadLogFile(BotaSettings botaSettings) {
        int intValue = Integer.valueOf(botaSettings.getConfig(Configs.LOG_FILE_UPLOAD_COUNT_TODAY, "0")).intValue();
        if (intValue == 0) {
            return true;
        }
        if (checkSameDay(new Date(System.currentTimeMillis()), new Date(Long.valueOf(botaSettings.getConfig(Configs.PREV_LOG_FILE_UPLOAD_TIME, "")).longValue()))) {
            return intValue < 2;
        }
        botaSettings.removeConfig(Configs.LOG_FILE_UPLOAD_COUNT_TODAY);
        botaSettings.removeConfig(Configs.PREV_LOG_FILE_UPLOAD_TIME);
        return true;
    }

    private static boolean checkSameDay(Date date, Date date2) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.setTime(date);
        calendar2.setTime(date2);
        return calendar.get(6) == calendar2.get(6) && calendar.get(1) == calendar2.get(1);
    }
}
