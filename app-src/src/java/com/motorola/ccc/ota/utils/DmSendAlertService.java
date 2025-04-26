package com.motorola.ccc.ota.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import java.util.Iterator;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class DmSendAlertService {
    private static final String ACTION_START_CI_SESSION = "com.motorola.omadm.service.START_CI_SESSION";
    private static final String ALERT_TYPE_DEVICEREQUEST = "org.openmobilealliance.dm.firmwareupdate.devicerequest";
    private static final String ALERT_TYPE_DOWNLOADANDUPDATE = "org.openmobilealliance.dm.firmwareupdate.downloadandupdate";
    private static final String EXTRA_ALERT_STRING = "AlertStr";
    private static final String EXTRA_SERVER_ID = "ServerID";
    private static final String INTENT_RECEIVER_CLASS_NAME = "com.motorola.omadm.service.DMIntentReceiver";
    public static final long REQUEST_ID = 65793;
    private static final String SERVICE_PACKAGE_NAME = "com.motorola.omadm.service";
    private static final String TAG = "DmSendAlertService";

    public static void sendDmAlertNotification(Context context, String str) {
        if (BuildPropReader.isVerizon()) {
            Intent dmAlertNotificationIntent = getDmAlertNotificationIntent(context, str);
            dmAlertNotificationIntent.putExtra(EXTRA_ALERT_STRING, ALERT_TYPE_DOWNLOADANDUPDATE);
            context.sendBroadcast(dmAlertNotificationIntent);
        }
    }

    public static void sendDmAlertDeviceReqNotification(Context context, String str) {
        if (BuildPropReader.isVerizon()) {
            Intent dmAlertNotificationIntent = getDmAlertNotificationIntent(context, str);
            dmAlertNotificationIntent.putExtra(EXTRA_ALERT_STRING, ALERT_TYPE_DEVICEREQUEST);
            context.sendBroadcast(dmAlertNotificationIntent);
        }
    }

    private static Intent getDmAlertNotificationIntent(Context context, String str) {
        int subId = getSubId(context);
        Logger.debug("OtaApp", "DmSendAlertService ,Launching DM Client Service Result Code: " + str + " subID: " + subId);
        Intent intent = new Intent(ACTION_START_CI_SESSION);
        intent.setClassName(SERVICE_PACKAGE_NAME, INTENT_RECEIVER_CLASS_NAME);
        intent.putExtra("AlertData", str);
        intent.putExtra("AlertURI", "./ManagedObjects/FUMO");
        intent.putExtra(EXTRA_SERVER_ID, "com.vzwdmserver");
        intent.putExtra("RequestID", REQUEST_ID);
        intent.putExtra("Correlator", "");
        intent.putExtra("subscription", subId);
        return intent;
    }

    public static final void sendFotaDownloadAndUpdateAlert(Context context, int i) {
        if (BuildPropReader.isBotaATT()) {
            int subId = getSubId(context);
            Logger.debug("OtaApp", "DmSendAlertService ,Launching Fota DM Client Service Result Code: " + i + " subID: " + subId);
            Intent intent = new Intent(ACTION_START_CI_SESSION);
            intent.setClassName(SERVICE_PACKAGE_NAME, INTENT_RECEIVER_CLASS_NAME);
            intent.putExtra("AlertData", "" + i);
            intent.putExtra("AlertURI", "./ManagedObjects/FUMO");
            intent.putExtra(EXTRA_SERVER_ID, "Cingular");
            intent.putExtra("RequestID", System.currentTimeMillis());
            intent.putExtra("Correlator", "");
            intent.putExtra("subscription", subId);
            intent.putExtra(EXTRA_ALERT_STRING, ALERT_TYPE_DOWNLOADANDUPDATE);
            context.sendBroadcast(intent);
        }
    }

    private static int getSubId(Context context) {
        List<SubscriptionInfo> activeSubscriptionInfoList = ((SubscriptionManager) context.getSystemService("telephony_subscription_service")).getActiveSubscriptionInfoList();
        if (activeSubscriptionInfoList == null) {
            Logger.debug("OtaApp", "DmSendAlertService ,getSubId:subscriptionInfoList is null:return INVALID_SUBSCRIPTION_ID");
            return -1;
        }
        Iterator<SubscriptionInfo> it = activeSubscriptionInfoList.iterator();
        if (it.hasNext()) {
            return it.next().getSubscriptionId();
        }
        return -1;
    }

    public static void startManualDMSync(Context context) {
        if (BuildPropReader.isVerizon()) {
            int subId = getSubId(context);
            Intent intent = new Intent(ACTION_START_CI_SESSION);
            intent.setComponent(new ComponentName(SERVICE_PACKAGE_NAME, INTENT_RECEIVER_CLASS_NAME));
            intent.putExtra(EXTRA_SERVER_ID, "com.vzwdmserver");
            intent.putExtra(EXTRA_ALERT_STRING, "org.openmobilealliance.dm.firmwareupdate.userrequest");
            intent.putExtra("subscription", subId);
            Logger.debug("OtaApp", "DmSendAlertService ,Send manual DM Sync Intent on System update check");
            context.sendBroadcast(intent);
        }
    }
}
