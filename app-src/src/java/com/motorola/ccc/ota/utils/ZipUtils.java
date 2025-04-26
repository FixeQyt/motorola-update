package com.motorola.ccc.ota.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public final class ZipUtils {
    private ZipUtils() {
    }

    public static void extract(File file, File file2) throws IOException {
        ZipFile zipFile = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry nextElement = entries.nextElement();
            File checkDirectories = checkDirectories(nextElement, file2);
            if (checkDirectories != null) {
                InputStream inputStream = zipFile.getInputStream(nextElement);
                try {
                    writeFile(inputStream, checkDirectories);
                } finally {
                    inputStream.close();
                }
            }
        }
        zipFile.close();
    }

    public static void extract(InputStream inputStream, File file) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        while (true) {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            if (nextEntry == null) {
                return;
            }
            File checkDirectories = checkDirectories(nextEntry, file);
            if (checkDirectories != null) {
                writeFile(zipInputStream, checkDirectories);
            }
        }
    }

    protected static File checkDirectories(ZipEntry zipEntry, File file) throws IOException {
        String name = zipEntry.getName();
        if (zipEntry.isDirectory()) {
            createDir(new File(file, name));
            return null;
        }
        File file2 = new File(file, name);
        if (!file2.getCanonicalPath().startsWith(file.getPath())) {
            throw new SecurityException("Security violation: Resolved path jumped beyond configured root");
        }
        if (!file2.getParentFile().exists()) {
            createDir(file2.getParentFile());
        }
        Logger.debug("OtaApp", "Extracting: " + name);
        return file2;
    }

    protected static void writeFile(InputStream inputStream, File file) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        try {
            IOUtils.copy(bufferedInputStream, bufferedOutputStream);
        } finally {
            bufferedOutputStream.close();
        }
    }

    private static void createDir(File file) {
        Logger.info("OtaApp", "Creating dir: " + file.getName());
        if (file.mkdirs()) {
            return;
        }
        Logger.info("OtaApp", "Can not create dir: " + file);
    }

    public static boolean zipDirectory(File file, String str) {
        try {
            ArrayList<String> arrayList = new ArrayList();
            File[] listFiles = file.listFiles();
            if (listFiles == null) {
                Logger.debug("OtaApp", "ZipUtils, No files found in folder " + str + " for zipping");
                return false;
            }
            for (File file2 : listFiles) {
                if (file2.isFile()) {
                    arrayList.add(file2.getAbsolutePath());
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream(str);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            for (String str2 : arrayList) {
                Logger.debug("OtaApp", "ZipUtils, zipping " + str2);
                zipOutputStream.putNextEntry(new ZipEntry(str2.substring(file.getAbsolutePath().length() + 1, str2.length())));
                FileInputStream fileInputStream = new FileInputStream(str2);
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read > 0) {
                        zipOutputStream.write(bArr, 0, read);
                    }
                }
                zipOutputStream.closeEntry();
                fileInputStream.close();
            }
            zipOutputStream.close();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            Logger.debug("OtaApp", "ZipUtils, Exception in zipDirectory" + e);
            return false;
        }
    }
}
