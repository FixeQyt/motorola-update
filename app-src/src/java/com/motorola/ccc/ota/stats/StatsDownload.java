package com.motorola.ccc.ota.stats;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.os.Process;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.stats.StatsDownloadIface;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilMethods;
import com.motorola.otalib.common.utils.NetworkUtils;
import com.motorola.otalib.downloadservice.download.policy.ZeroRatedManager;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.io.IOUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class StatsDownload {
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    public static final String OTAAPP_TAG = "OtaApp";
    private static final String PROC_STAT_FILE = "/proc/net/xt_qtaguid/stats";

    public static final ArrayList<StatsDownloadIface> getIfaceUsageStats(Context context, ConnectivityManager connectivityManager, BotaSettings botaSettings, StatsDownloadIface.IfaceName ifaceName) {
        String string;
        String str;
        String str2;
        ProcFileReader procFileReader;
        StatsDownloadIface statsDownloadIface;
        Closeable closeable = null;
        if (!new File(PROC_STAT_FILE).exists()) {
            Logger.debug("OtaApp", "getIfaceUsageStats(), no kernel support");
            return null;
        }
        int i = AnonymousClass1.$SwitchMap$com$motorola$ccc$ota$stats$StatsDownloadIface$IfaceName[ifaceName.ordinal()];
        int i2 = 1;
        if (i == 1) {
            string = botaSettings.getString(Configs.STATS_DL_ADMIN_IFACE);
            str = null;
            str2 = null;
        } else if (i != 2) {
            if (i == 3) {
                str = botaSettings.getString(Configs.STATS_DL_WIFI_IFACE);
                string = null;
            } else if (i != 4) {
                str = null;
                string = null;
            } else {
                string = botaSettings.getString(Configs.STATS_DL_ADMIN_IFACE);
                str2 = botaSettings.getString(Configs.STATS_DL_INTERNET_IFACE);
                str = botaSettings.getString(Configs.STATS_DL_WIFI_IFACE);
            }
            str2 = string;
        } else {
            str2 = botaSettings.getString(Configs.STATS_DL_INTERNET_IFACE);
            str = null;
            string = null;
        }
        Logger.debug("OtaApp", "getIfaceUsageStats() Interface names: " + string + SystemUpdateStatusUtils.SPACE + str2 + SystemUpdateStatusUtils.SPACE + str);
        ArrayList<StatsDownloadIface> arrayList = new ArrayList<>();
        try {
            try {
                procFileReader = new ProcFileReader(new FileInputStream(PROC_STAT_FILE));
                try {
                    procFileReader.finishLine();
                    while (procFileReader.hasMoreData()) {
                        int nextInt = procFileReader.nextInt();
                        if (nextInt != i2 + 1) {
                            Logger.info("OtaApp", "getIfaceUsageStats() consistent idx=" + nextInt + " after lastIdx=" + i2);
                            IOUtils.closeQuietly(procFileReader);
                            IOUtils.closeQuietly(procFileReader);
                            return null;
                        }
                        String nextString = procFileReader.nextString();
                        kernelToTag(procFileReader.nextString());
                        int nextInt2 = procFileReader.nextInt();
                        int nextInt3 = procFileReader.nextInt();
                        long nextLong = procFileReader.nextLong();
                        procFileReader.nextLong();
                        long nextLong2 = procFileReader.nextLong();
                        procFileReader.nextLong();
                        if (nextInt2 == Process.myUid()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("iface=").append(nextString);
                            sb.append(" countSet=").append(nextInt3);
                            sb.append(" rxBytes=").append(nextLong);
                            sb.append(" txBytes=").append(nextLong2);
                            Logger.debug("OtaApp", "getIfaceUsageStats() for uid: " + sb.toString());
                            if (nextString.equalsIgnoreCase(string)) {
                                statsDownloadIface = new StatsDownloadIface(StatsDownloadIface.IfaceName.admin, nextLong, nextLong2);
                            } else if (nextString.equalsIgnoreCase(str2)) {
                                statsDownloadIface = new StatsDownloadIface(StatsDownloadIface.IfaceName.internet, nextLong, nextLong2);
                            } else {
                                statsDownloadIface = nextString.equalsIgnoreCase(str) ? new StatsDownloadIface(StatsDownloadIface.IfaceName.wifi, nextLong, nextLong2) : null;
                            }
                            if (statsDownloadIface != null) {
                                arrayList.add(statsDownloadIface);
                            }
                        }
                        procFileReader.finishLine();
                        i2 = nextInt;
                    }
                    IOUtils.closeQuietly(procFileReader);
                    return arrayList;
                } catch (Exception e) {
                    e = e;
                    Logger.debug("OtaApp", "getIfaceUsageStats() caught exception " + e.toString());
                    IOUtils.closeQuietly(procFileReader);
                    return null;
                }
            } catch (Throwable th) {
                th = th;
                closeable = SystemUpdateStatusUtils.SPACE;
                IOUtils.closeQuietly(closeable);
                throw th;
            }
        } catch (Exception e2) {
            e = e2;
            procFileReader = null;
        } catch (Throwable th2) {
            th = th2;
            IOUtils.closeQuietly(closeable);
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: com.motorola.ccc.ota.stats.StatsDownload$1  reason: invalid class name */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$motorola$ccc$ota$stats$StatsDownloadIface$IfaceName;

        static {
            int[] iArr = new int[StatsDownloadIface.IfaceName.values().length];
            $SwitchMap$com$motorola$ccc$ota$stats$StatsDownloadIface$IfaceName = iArr;
            try {
                iArr[StatsDownloadIface.IfaceName.admin.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$stats$StatsDownloadIface$IfaceName[StatsDownloadIface.IfaceName.internet.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$stats$StatsDownloadIface$IfaceName[StatsDownloadIface.IfaceName.wifi.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$motorola$ccc$ota$stats$StatsDownloadIface$IfaceName[StatsDownloadIface.IfaceName.all.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public static void storeIfaceUsageAtDLStart(BotaSettings botaSettings, ArrayList<StatsDownloadIface> arrayList, StatsDownloadIface.IfaceName ifaceName) {
        if (arrayList == null) {
            return;
        }
        Iterator<StatsDownloadIface> it = arrayList.iterator();
        while (it.hasNext()) {
            StatsDownloadIface next = it.next();
            if (next == null) {
                Logger.debug("OtaApp", "entry is null , skip it");
            } else {
                Logger.debug("OtaApp", "Storing Iface At DL Start, " + next.iface + " stats: " + next.txBytes + SmartUpdateUtils.MASK_SEPARATOR + next.rxBytes + " ifaceName interested in is " + ifaceName);
                if (ifaceName != null && !next.iface.equals(ifaceName)) {
                    Logger.debug("OtaApp", "storeIfaceUsageAtDLStart, not interested in ifaceName: " + next.iface);
                } else {
                    int i = AnonymousClass1.$SwitchMap$com$motorola$ccc$ota$stats$StatsDownloadIface$IfaceName[next.iface.ordinal()];
                    if (i == 1) {
                        botaSettings.setLong(Configs.STATS_DL_START_ADMINAPN_TX_BYTES, botaSettings.getLong(Configs.STATS_DL_START_ADMINAPN_TX_BYTES, 0L) + next.txBytes);
                        botaSettings.setLong(Configs.STATS_DL_START_ADMINAPN_RX_BYTES, botaSettings.getLong(Configs.STATS_DL_START_ADMINAPN_RX_BYTES, 0L) + next.rxBytes);
                    } else if (i == 2) {
                        botaSettings.getLong(Configs.STATS_DL_START_CELLULAR_TX_BYTES, botaSettings.getLong(Configs.STATS_DL_START_CELLULAR_TX_BYTES, 0L) + next.txBytes);
                        botaSettings.setLong(Configs.STATS_DL_START_CELLULAR_RX_BYTES, botaSettings.getLong(Configs.STATS_DL_START_CELLULAR_RX_BYTES, 0L) + next.rxBytes);
                    } else if (i != 3) {
                        Logger.debug("OtaApp", "storeIfaceUsageAtDLStart, default case " + next.iface);
                    } else {
                        botaSettings.setLong(Configs.STATS_DL_START_WIFI_TX_BYTES, botaSettings.getLong(Configs.STATS_DL_START_WIFI_TX_BYTES, 0L) + next.txBytes);
                        botaSettings.setLong(Configs.STATS_DL_START_WIFI_RX_BYTES, botaSettings.getLong(Configs.STATS_DL_START_WIFI_RX_BYTES, 0L) + next.rxBytes);
                    }
                }
            }
        }
    }

    public static final void downloadingOrStopped(Intent intent, BotaSettings botaSettings, ConnectivityManager connectivityManager, Context context) {
        long receivedBytesFromIntent = UpgradeUtilMethods.receivedBytesFromIntent(intent);
        long j = UpgradeUtilMethods.totalBytesFromIntent(intent);
        int deferredFromIntent = UpgradeUtilMethods.deferredFromIntent(intent);
        if (deferredFromIntent == 0) {
            if (BuildPropReader.isVerizon() && !NetworkUtils.isWifi(connectivityManager) && botaSettings.getString(Configs.STATS_DL_ADMIN_IFACE) == null) {
                botaSettings.setString(Configs.STATS_DL_ADMIN_IFACE, getIfaceNameFromNetworkType(connectivityManager, ZeroRatedManager.returnActiveAdminApnNetwork()));
                storeIfaceUsageAtDLStart(botaSettings, getIfaceUsageStats(context, connectivityManager, botaSettings, StatsDownloadIface.IfaceName.admin), StatsDownloadIface.IfaceName.admin);
            }
            if (didDownloadRestarted(botaSettings, receivedBytesFromIntent)) {
                buildDownloadRestartStats(botaSettings, context, connectivityManager, j);
                botaSettings.clearStatsForDownloading();
                storeIfaceUsageAtDLStart(botaSettings, getIfaceUsageStats(context, connectivityManager, botaSettings, StatsDownloadIface.IfaceName.all), null);
            }
            StatsHelper.setDownloadTotalSize(botaSettings, j);
            if (NetworkUtils.isWifi(connectivityManager)) {
                StatsHelper.setDownloadedSizeViaWifi(botaSettings, receivedBytesFromIntent);
                if (botaSettings.getString(Configs.STATS_DL_WIFI_IFACE) == null) {
                    botaSettings.setString(Configs.STATS_DL_WIFI_IFACE, getIfaceNameFromNetworkType(connectivityManager, NetworkUtils.returnActiveNetwork(connectivityManager)));
                    storeIfaceUsageAtDLStart(botaSettings, getIfaceUsageStats(context, connectivityManager, botaSettings, StatsDownloadIface.IfaceName.wifi), StatsDownloadIface.IfaceName.wifi);
                }
            } else if (botaSettings.getString(Configs.STATS_DL_ADMIN_IFACE) != null) {
                StatsHelper.setDownloadedSizeViaAdminApn(botaSettings, receivedBytesFromIntent);
            } else {
                StatsHelper.setDownloadedSizeViaCellular(botaSettings, receivedBytesFromIntent);
                if (botaSettings.getString(Configs.STATS_DL_INTERNET_IFACE) == null) {
                    botaSettings.setString(Configs.STATS_DL_INTERNET_IFACE, getIfaceNameFromNetworkType(connectivityManager, NetworkUtils.returnActiveNetwork(connectivityManager)));
                    storeIfaceUsageAtDLStart(botaSettings, getIfaceUsageStats(context, connectivityManager, botaSettings, StatsDownloadIface.IfaceName.internet), StatsDownloadIface.IfaceName.internet);
                }
            }
            StatsHelper.setReceivedSize(botaSettings, receivedBytesFromIntent);
        }
        if (deferredFromIntent < 0) {
            StatsHelper.setDownloadSuspendedCount(botaSettings);
        }
    }

    public static final void downloadErrorcode(BotaSettings botaSettings, int i) {
        if (i == 403) {
            botaSettings.incrementPrefs(Configs.STATS_DL_FORBIDDEN_ERROR);
        } else if (i == 404) {
            botaSettings.incrementPrefs(Configs.STATS_DL_NOTFOUND_ERROR);
        } else if (i == 410) {
            botaSettings.incrementPrefs(Configs.STATS_DL_GONE_ERROR);
        } else if (i == 412) {
            botaSettings.incrementPrefs(Configs.STATS_DL_PRECONDITION_ERROR);
        } else if (i == 416) {
            botaSettings.incrementPrefs(Configs.STATS_DL_RANGE_ERROR);
        } else if (i == HTTP_TOO_MANY_REQUESTS) {
            botaSettings.incrementPrefs(Configs.STATS_DL_TOOMANYREQUEST_ERROR);
        } else if (i != 503) {
        } else {
            botaSettings.incrementPrefs(Configs.STATS_DL_SU_ERROR);
        }
    }

    public static final void downloadException(BotaSettings botaSettings, String str) {
        if ("java.io.IOException".contains(str)) {
            botaSettings.incrementPrefs(Configs.STATS_DL_IO_ERROR);
        }
    }

    public static void storeIfaceUsageAtDLEnd(BotaSettings botaSettings, ArrayList<StatsDownloadIface> arrayList) {
        if (arrayList == null) {
            return;
        }
        Iterator<StatsDownloadIface> it = arrayList.iterator();
        while (it.hasNext()) {
            StatsDownloadIface next = it.next();
            if (next == null) {
                Logger.debug("OtaApp", "entry is null , skip it");
            } else {
                Logger.debug("OtaApp", "Storing Iface At DL END, " + next.iface + " stats: " + next.txBytes + SmartUpdateUtils.MASK_SEPARATOR + next.rxBytes);
                int i = AnonymousClass1.$SwitchMap$com$motorola$ccc$ota$stats$StatsDownloadIface$IfaceName[next.iface.ordinal()];
                if (i == 1) {
                    botaSettings.setLong(Configs.STATS_DL_END_ADMINAPN_TX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_ADMINAPN_TX_BYTES, 0L) + next.txBytes);
                    botaSettings.setLong(Configs.STATS_DL_END_ADMINAPN_RX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_ADMINAPN_RX_BYTES, 0L) + next.rxBytes);
                } else if (i == 2) {
                    botaSettings.setLong(Configs.STATS_DL_END_CELLULAR_TX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_CELLULAR_TX_BYTES, 0L) + next.txBytes);
                    botaSettings.setLong(Configs.STATS_DL_END_CELLULAR_RX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_CELLULAR_RX_BYTES, 0L) + next.rxBytes);
                } else if (i == 3) {
                    botaSettings.setLong(Configs.STATS_DL_END_WIFI_TX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_WIFI_TX_BYTES, 0L) + next.txBytes);
                    botaSettings.setLong(Configs.STATS_DL_END_WIFI_RX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_WIFI_RX_BYTES, 0L) + next.rxBytes);
                }
            }
        }
    }

    public static final void buildDownloadCompletedOrFailedStats(BotaSettings botaSettings, Context context, ConnectivityManager connectivityManager, long j) {
        storeIfaceUsageAtDLEnd(botaSettings, getIfaceUsageStats(context, connectivityManager, botaSettings, StatsDownloadIface.IfaceName.all));
        storeIfaceDifferences(botaSettings);
        StatsHelper.setDownloadPercentageViaWifi(botaSettings, j);
        StatsHelper.setDownloadPercentageViaCellular(botaSettings, j);
        StatsHelper.setDownloadPercentageViaAdminApn(botaSettings, j);
        StatsHelper.setNetworkUsedByDownload(botaSettings);
        StatsHelper.setDownloadErrorCodes(botaSettings);
        detectRevenueLeak(botaSettings, j);
    }

    private static String getIfaceNameFromNetworkType(ConnectivityManager connectivityManager, Network network) {
        if (network == null) {
            Logger.debug("OtaApp", "getIfaceNameFromNetworkType: network is null");
            return null;
        }
        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);
        if (linkProperties == null) {
            Logger.debug("OtaApp", "getIfaceNameFromNetworkType: link properties is null");
            return null;
        }
        String interfaceName = linkProperties.getInterfaceName();
        Logger.debug("OtaApp", "getIfaceNameFromNetworkType: iface is : " + interfaceName);
        return interfaceName;
    }

    private static void buildDownloadRestartStats(BotaSettings botaSettings, Context context, ConnectivityManager connectivityManager, long j) {
        storeIfaceUsageAtDLEnd(botaSettings, getIfaceUsageStats(context, connectivityManager, botaSettings, StatsDownloadIface.IfaceName.all));
        StatsHelper.setDownloadPercentageViaWifiBeforeRestart(botaSettings, j);
        StatsHelper.setDownloadPercentageViaCellularBeforeRestart(botaSettings, j);
        StatsHelper.setDownloadPercentageViaAdminApnBeforeRestart(botaSettings, j);
    }

    private static void storeIfaceDifferences(BotaSettings botaSettings) {
        botaSettings.setLong(Configs.STATS_DL_ADMINAPN_TX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_ADMINAPN_TX_BYTES, 0L) - botaSettings.getLong(Configs.STATS_DL_START_ADMINAPN_TX_BYTES, 0L));
        botaSettings.setLong(Configs.STATS_DL_ADMINAPN_RX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_ADMINAPN_RX_BYTES, 0L) - botaSettings.getLong(Configs.STATS_DL_START_ADMINAPN_RX_BYTES, 0L));
        botaSettings.setLong(Configs.STATS_DL_CELLULAR_TX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_CELLULAR_TX_BYTES, 0L) - botaSettings.getLong(Configs.STATS_DL_START_CELLULAR_TX_BYTES, 0L));
        botaSettings.setLong(Configs.STATS_DL_CELLULAR_RX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_CELLULAR_RX_BYTES, 0L) - botaSettings.getLong(Configs.STATS_DL_START_CELLULAR_RX_BYTES, 0L));
        botaSettings.setLong(Configs.STATS_DL_WIFI_TX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_WIFI_TX_BYTES, 0L) - botaSettings.getLong(Configs.STATS_DL_START_WIFI_TX_BYTES, 0L));
        botaSettings.setLong(Configs.STATS_DL_WIFI_RX_BYTES, botaSettings.getLong(Configs.STATS_DL_END_WIFI_RX_BYTES, 0L) - botaSettings.getLong(Configs.STATS_DL_START_WIFI_RX_BYTES, 0L));
    }

    private static void detectRevenueLeak(BotaSettings botaSettings, long j) {
        if (!BuildPropReader.isVerizon()) {
            botaSettings.setString(Configs.STATS_DL_REVENUE_LEAK_DETECTED, "NOT_APPLICABLE");
            return;
        }
        try {
            if (botaSettings.getInt(Configs.STATS_REBOOT_DURING_DL, 0) <= 0 && !BuildPropReader.isStreamingUpdate()) {
                Long valueOf = Long.valueOf(botaSettings.getLong(Configs.STATS_DL_ADMINAPN_RX_BYTES, 0L));
                if (Math.abs((valueOf.longValue() > 0 ? Math.min((valueOf.longValue() * 100) / j, 100.0d) : 0.0d) - Double.parseDouble(botaSettings.getString(Configs.STATS_DL_VIA_ADMINAPN))) > botaSettings.getDouble(Configs.DOWNLOAD_REVENUE_LEAK_TOLERANCE, Double.valueOf(5.0d))) {
                    botaSettings.setString(Configs.STATS_DL_REVENUE_LEAK_DETECTED, "true");
                    return;
                } else {
                    botaSettings.setString(Configs.STATS_DL_REVENUE_LEAK_DETECTED, "false");
                    return;
                }
            }
            if ((botaSettings.getString(Configs.STATS_DL_VIA_CELLULAR) != null ? Double.parseDouble(botaSettings.getString(Configs.STATS_DL_VIA_CELLULAR)) : 0.0d) > botaSettings.getDouble(Configs.DOWNLOAD_REVENUE_LEAK_TOLERANCE, Double.valueOf(5.0d))) {
                botaSettings.setString(Configs.STATS_DL_REVENUE_LEAK_DETECTED, "true");
            } else {
                botaSettings.setString(Configs.STATS_DL_REVENUE_LEAK_DETECTED, "false");
            }
        } catch (Exception e) {
            Logger.debug("OtaApp", "detectRevenueLeak() exception while reading settings" + e.toString());
            botaSettings.setString(Configs.STATS_DL_REVENUE_LEAK_DETECTED, "NOT_APPLICABLE");
        }
    }

    private static int kernelToTag(String str) {
        try {
            int length = str.length();
            if (length > 10) {
                return Long.decode(str.substring(0, length - 8)).intValue();
            }
        } catch (Exception unused) {
        }
        return 0;
    }

    private static boolean didDownloadRestarted(BotaSettings botaSettings, long j) {
        long j2 = botaSettings.getLong(Configs.STATS_DL_RECEIVED_SIZE, 0L);
        if (j2 <= j || j2 <= downloadFileSize()) {
            return false;
        }
        Logger.debug("OtaApp", "didDownloadRestarted(), size mismatch. Received: " + j + ": StoredReceivedSize: " + j2 + ": DownloadFileSize: " + downloadFileSize());
        return true;
    }

    private static long downloadFileSize() {
        return UpdaterUtils.downloadFileSize();
    }
}
