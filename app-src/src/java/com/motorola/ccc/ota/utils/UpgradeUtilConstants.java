package com.motorola.ccc.ota.utils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class UpgradeUtilConstants {
    public static final String AB_UPGRADE_COMPLETED_INTENT = "com.motorola.ccc.ota.AB_UPGRADE_COMPLETED";
    public static final String AB_UPGRADE_RESTART_PENDING = "com.motorola.blur.service.blur.upgrade.AB_UPGRADE_RESTART_PENDING";
    public static final String ACTION_AB_APPLY_PAYLOAD_STARTED_INTENT = "com.motorola.ccc.ota.AB_APPLY_PAYLOAD_STARTED";
    public static final String ACTION_AB_APPLY_PAYLOAD_SUSPEND = "com.motorola.ccc.ota.ACTION_AB_APPLY_PAYLOAD_SUSPEND";
    public static final String ACTION_AB_UPDATE_PROGRESS = "com.motorola.ccc.ota.ACTION_AB_UPDATE_PROGRESS";
    public static final String ACTION_BATTERY_CHANGED = "com.motorola.ccc.ota.Actions.BATTERY_CHANGED";
    public static final String ACTION_BATTERY_LOW = "com.motorola.ccc.ota.ACTION_BATTERY_LOW";
    public static final String ACTION_DATA_SAVER_DURING_AB_STREAMING = "com.motorola.ccc.ota.ACTION_DATA_SAVER_DURING_AB_STREAMING";
    public static final String ACTION_DM_CANCEL_ONGOING_UPGRADE = "com.motorola.ccc.ota.Actions.ACTION_DM_CANCEL_ONGOING_UPGRADE";
    public static final String ACTION_MODEM_FSG_POLL = "com.motorola.modemservice.START_FSG_POLLING";
    public static final String ACTION_MODEM_UPDATE = "com.motorola.ccc.ota.MODEM_UPDATE";
    public static final String ACTION_MODEM_UPDATE_STATUS = "com.motorola.modemservice.MODEM_UPDATE_STATUS";
    public static final String ACTION_MODEM_UPGRADE_POLL = "com.motorola.ccc.ota.modem_upgrade_poll";
    public static final String ACTION_NOTIFICATION_SWIPED = "com.motorola.ccc.ota.ACTION_NOTIFICATION_SWIPED";
    public static final String ACTION_OTA_DEFERRED = "com.motorola.ccc.ota.ACTION_OTA_DEFERRED";
    public static final String ACTION_OTA_RESERVE_SPACE_RESPONSE = "com.motorola.app.Actions.OTA_RESERVED_SPACE_RESPONSE";
    public static final String ACTION_OVERRIDE_METADATA = "com.motorola.ccc.ota.Actions.OVERRIDE_METADATA";
    public static final String ACTION_PAUSE_DOWNLOAD_FOR_CELLULAR = "com.motorola.ccc.ota.ACTION_PAUSE_DOWNLOAD_FOR_CELLULAR";
    public static final String ACTION_SIM_STATE_CHANGE = "android.intent.action.SIM_STATE_CHANGED";
    public static final String ACTION_SMART_UPDATE_CONFIG_CHANGED = "com.motorola.ccc.ota.ACTION_SMART_UPDATE_CONFIG_CHANGED";
    public static final String ACTION_STOP_OTA_SERVICE = "com.motorola.ccc.ota.STOP_OTA_SERVICE";
    public static final String ACTION_UPGRADE_POLL = "com.motorola.blur.service.blur.upgrade_poll";
    public static final String ACTION_UPGRADE_UPDATE_STATUS = "com.motorola.app.action.UPGRADE_UPDATE_STATUS";
    public static final String ACTION_VAB_ALLOCATE_SPACE_RESULT = "com.motorola.ccc.ota.ACTION_VAB_ALLOCATE_SPACE_RESULT";
    public static final String ACTION_VAB_CLEANUP_APLLIED_PAYLOAD = "com.motorola.ccc.ota.ACTION_CLEANUP_APLLIED_PAYLOAD";
    public static final String ACTION_VAB_VERIFY_STATUS = "com.motorola.ccc.ota.ACTION_VAB_VERIFY_STATUS";
    public static final String ACTION_VERIFY_PAYLOAD_STATUS = "com.motorola.ccc.ota.ACTION_VERIFY_PAYLOAD_STATUS";
    public static final String ACTIVITY_ANNOY_VALUE_EXPIRY = "com.motorola.ccc.ota.Actions.ACTIVITY_ANNOY_VALUE_EXPIRY";
    public static final String ALREADY_UP_TO_DATE = "com.motorola.ccc.ota.ALREADY_UP_TO_DATE";
    public static final String CANCEL_UPDATE = "com.motorola.ccc.ota.Actions.CANCEL_UPDATE";
    public static final int CHAINED_UPDATE_DELAY_TIME = 900;
    public static final String CREATE_RESERVE_SPACE_POST_FIFTEEN_MINUTES = "com.motorola.app.CREATE_RESERVE_SPACE_POST_FIFTEEN_MINUTES";
    public static final long DEFAULT_MAX_INSTALL_DEFER_TIME = 86400000;
    public static final long DEFAULT_MAX_REBOOT_DEFER_TIME = 43200000;
    public static final int DEFAULT_MAX_RETRY_COUNT_DOWNLOAD_PACKAGE = 9;
    public static final String DOWNLOAD = "DOWNLOAD_DEFERRED";
    public static final String DOWNLOAD_COMPLETED_TO_SETTINGS = "com.motorola.ccc.ota.CusAndroidUtils.DOWNLOAD_COMPLETED";
    public static final String DOWNLOAD_NOTIFIED_TO_SETTINGS = "com.motorola.ccc.ota.CusAndroidUtils.DOWNLOAD_NOTIFIED";
    public static final int DOWNLOAD_OPTIONS = 2;
    public static final long DOWNLOAD_PENDING_NOTIFICATION_EXPIRY_DAYS = 15;
    public static final String FINISH_BG_INSTALL_ACTIVITY = "com.motorola.ccc.ota.Actions.FINISH_BG_INSTALL_ACTIVITY";
    public static final String FINISH_DOWNLOAD_ACTIVITY = "com.motorola.ccc.ota.Actions.FINISH_DOWNLOAD_ACTIVITY";
    public static final String FINISH_DOWNLOAD_OPTIONS_FRAGMENT = "com.motorola.ccc.ota.ui.finish_download_options_fragment";
    public static final String FINISH_DOWNLOAD_PROGRESS_ACTIVITY = "com.motorola.ccc.ota.Actions.FINISH_DOWNLOAD_PROGRESS_ACTIVITY";
    public static final String FINISH_INSTALL_ACTIVITY = "com.motorola.ccc.ota.Actions.FINISH_INSTALL_ACTIVITY";
    public static final String FINISH_MESSAGE_ACTIVITY = "com.motorola.ccc.ota.ui.finish_message_activity";
    public static final String FINISH_RESTART_ACTIVITY = "com.motorola.ccc.ota.Actions.FINISH_RESTART_ACTIVITY";
    public static final String FINISH_WARNING_ALERT_DIALOG = "com.motorola.ccc.ota.ui.finish_wad";
    public static final String FORCE_UPGRADE_TIMER_EXPIRY = "com.motorola.ccc.ota.Actions.FORCE_UPGRADE_TIMER_EXPIRY";
    public static final String FRAGMENT_TYPE = "fragment_type";
    public static final String INSTALL = "INSTALL_DEFERRED";
    public static final String INTENT_HEALTH_CHECK = "com.motorola.com.ccc.ota.healthCheckIntent";
    public static final String KEY_AB_UPGRADE_STATUS_REASON = "ab_upgrade_status_reason";
    public static final String KEY_AB_UPGRADE_STATUS_SUCCESS = "ab_upgrade_status_success";
    public static final String KEY_ALLOCATE_SPACE_RESULT = "com.motorola.ccc.ota.upgrade.allocate_space_result";
    public static final String KEY_ALLOW_ON_ROAMING = "KEY_ALLOW_ON_ROAMING";
    public static final String KEY_ANNOY_EXPIRY_TARGET_INTENT = "ANNOY_EXPIRY_INTENT";
    public static final String KEY_BOOTSTRAP = "com.motorola.blur.service.blur.upgrade.bootstrap";
    public static final String KEY_BYTES_RECEIVED = "com.motorola.blur.service.blur.upgrade.bytes_received";
    public static final String KEY_BYTES_TOTAL = "com.motorola.blur.service.blur.upgrade.bytes_total";
    public static final String KEY_CHECK_FOR_LOW_BATTERY = "com.motorola.ccc.ota.KEY_CHECK_FOR_LOW_BATTERY";
    public static final String KEY_CHECK_RESPONSE_INTENT = "com.motorola.blur.service.blur.check_response_intent";
    public static final String KEY_COMPATIBILITY_STATUS = "streaming_compatibility_status";
    public static final String KEY_CURRENT_STATE = "com.motorola.app.cus.state";
    public static final String KEY_DESTINAION_SHA1 = "com.motorola.app.destination.sha1";
    public static final String KEY_DESTINAION_VERSION = "com.motorola.app.destination.version";
    public static final String KEY_DISCOVERY_TIME = "com.motorola.ccc.ota.upgrade.discover_time";
    public static final String KEY_DISPLAY_VERSION = "com.motorola.ota.service.upgrade.displayVersion";
    public static final String KEY_DL_PERCENTAGE = "com.motorola.ccc.ota.download_percentage";
    public static final String KEY_DOWNLOAD_COMPLETED = "sys_update_downloaded_timestamp";
    public static final String KEY_DOWNLOAD_DEFERRED = "com.motorola.blur.service.blur.upgrade.download.deferred";
    public static final String KEY_DOWNLOAD_MODE = "com.motorola.ccc.ota.KEY_DOWNLOAD_MODE";
    public static final String KEY_DOWNLOAD_NOTIFIED = "sys_update_available_timestamp";
    public static final String KEY_DOWNLOAD_ON_WIFI = "com.motorola.blur.service.blur.upgrade.download.on_wifi";
    public static final String KEY_DOWNLOAD_OPT_CHECK = "com.motorola.blur.service.blur.upgrade.download_opt_check";
    public static final String KEY_DOWNLOAD_REQ_FROM_NOTIFY = "com.motorola.blur.service.blur.upgrade.download_from_notify";
    public static final String KEY_DOWNLOAD_SOURCE = "com.motorola.app.download.source";
    public static final String KEY_DOWNLOAD_STATUS = "com.motorola.blur.service.blur.upgrade.download_status";
    public static final String KEY_DOWNLOAD_WIFIONLY = "com.motorola.app.download.wifionly";
    public static final String KEY_EXTRA_BOOTLOADER_UNLOCK = "com.motorola.ccc.ota.bootloader_unlock";
    public static final String KEY_EXTRA_DEVICE_ROOTED = "com.motorola.ccc.ota.device_rooted";
    public static final String KEY_EXTRA_MODEM_UPDATE_STATUS_CODE = "errorCode";
    public static final String KEY_EXTRA_MODEM_UPDATE_STATUS_MSG = "statusWord";
    public static final String KEY_FILE_LOCATION = "com.motorola.blur.service.blur.upgrade.file_location";
    public static final String KEY_FORCE_INSTALL_TIME = "key_force_install_time";
    public static final String KEY_FORCE_UPGRADE_TIME = "com.motorola.ccc.ota.force_upgrade_time";
    public static final String KEY_FREE_SPACE_REQUIRED = "com.motorola.blur.service.blur.upgrade.free_space_req";
    public static final String KEY_FULL_SCREEN_REMINDER = "com.motorola.blur.service.blur.upgrade.KEY_FULL_SCREEN_REMINDER";
    public static final String KEY_HISTORY_RELEASE_NOTES = "releaseNotes";
    public static final String KEY_HISTORY_SOURCE_VERSION = "sourceVersion";
    public static final String KEY_HISTORY_TARGET_VERSION = "targetVersion";
    public static final String KEY_HISTORY_UPDATE_TIME = "updateTime";
    public static final String KEY_HISTORY_UPDATE_TYPE = "updateType";
    public static final String KEY_HISTORY_UPGRADE_NOTES = "upgradeNotes";
    public static final String KEY_INSTALLER = "com.motorola.app.installer";
    public static final String KEY_INSTALL_MODE = "com.motorola.ccc.ota.KEY_INSTALL_MODE";
    public static final String KEY_INTERACTIVE = "com.motorola.blur.service.blur.upgrade.interactive";
    public static final String KEY_INVALID_VALUE = "invalid";
    public static final String KEY_LAUNCH_MODE = "com.motorola.blur.service.blur.update.launch_mode";
    public static final String KEY_LOCATION_TYPE = "com.motorola.blur.service.blur.upgrade.location_type";
    public static final String KEY_METADATA = "com.motorola.blur.service.blur.upgrade.metadata";
    public static final String KEY_NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    public static final String KEY_ONLY_ON_NETWORK = "KEY_ONLY_ON_NETWORK";
    public static final String KEY_OTA_UPDATE_PLANNED = "com.motorola.blur.service.blur.upgrade.ota_update_planned";
    public static final String KEY_PACKAGE_SIZE = "com.motorola.ccc.ota.KEY_PACKAGE_SIZE";
    public static final String KEY_PERCENTAGE = "com.motorola.ccc.ota.upgrade_percentage";
    public static final String KEY_REASON = "com.motorola.ccc.ota.UPDATE_FAILURE_REASON";
    public static final String KEY_RELEASE_NOTES = "com.motorola.app.release.notes";
    public static final String KEY_REQUESTID = "com.motorola.blur.service.blur.upgrade.requestid";
    public static final String KEY_RESERVE_SPACE_IN_MB = "com.motorola.app.KEY_RESERVE_SPACE_IN_MB";
    public static final String KEY_RESPONSE_ACTION = "com.motorola.blur.service.blur.upgrade.response_action";
    public static final String KEY_RESPONSE_FLAVOUR = "com.motorola.blur.service.blur.upgrade.response_flavour";
    public static final String KEY_SDCARD_DOWNLOAD_LOCATION = "sdcard";
    public static final String KEY_SERVICE_STARTED_ON_CHK_UPDATE = "com.motorola.ccc.ota.SERVICE_STARTED_ON_CHK_UPDATE";
    public static final String KEY_SILENT_OTA = "com.motorola.blur.service.upgrade.silentOta";
    public static final String KEY_SMART_UPDATE_ENABLED = "com.motorola.ccc.ota.SMART_UPDATE_ENABLED";
    public static final String KEY_SOURCE_SHA1 = "com.motorola.app.source.sha1";
    public static final String KEY_SU_CANCEL_BY_DM = " cancelled by DM";
    public static final String KEY_TIMESTAMP = "com.motorola.app.time.stamp";
    public static final String KEY_UPDATE_ACTION_RESPONSE = "com.motorola.blur.service.blur.upgrade.check_error";
    public static final String KEY_UPDATE_FAILURE_COUNT = "com.motorola.ccc.ota.upgrade.failure_count";
    public static final String KEY_UPDATE_INFO = "com.motorola.app.cus.info";
    public static final String KEY_UPDATE_STATUS = "com.motorola.blur.service.blur.upgrade.update_status";
    public static final String KEY_UPDATE_TYPE = "com.motorola.blur.service.upgrade.updateType";
    public static final String KEY_UPGRADE_LAUNCH_PROCEED = "com.motorola.blur.service.blur.upgrade.upgrade_launch_proceed";
    public static final String KEY_UPGRADE_STATUS = "com.motorola.ccc.ota.upgrade_status";
    public static final String KEY_VAB_CLEANUP_APPLIED_PAYLOAD = "com.motorola.ccc.ota.KEY_VAB_CLEANUP_APPLIED_PAYLOAD";
    public static final String KEY_VAB_VALIDATION_STATUS = "com.motorola.ccc.ota.VAB_VALIDATION_STATUS";
    public static final String KEY_VERSION = "com.motorola.blur.service.blur.upgrade.version";
    public static final String KEY_WEBVIEW_BASE_FRAGMENT_STATS = "webViewBaseFragmentStats";
    public static final String KEY_WEBVIEW_URL = "webViewURL";
    public static final String MERGE_RESTART_UPGRADE = "com.motorola.blur.service.blur.Actions.MERGE_RESTART_UPGRADE";
    public static final String MOTOROLA_WEBSITE = "https://www.motorola.com";
    public static final String MOVE_FOTA_TO_GETTING_DESCRIPTOR = "com.motorola.ccc.ota.MOVE_FOTA_TO_GETTING_DESCRIPTOR";
    public static final String NOTIFICATION_ID = "notification_id";
    public static final long ONE_DAY_IN_MILLISECONDS = 86400000;
    public static final long ONE_HOUR = 3600000;
    public static final String OTA_SERVICE_RESTART_ACTION = "com.motorola.ccc.ota.Action.OTA_SERVICE_RESTART";
    public static final String OTA_START_ACTION = "com.motorola.ccc.ota.START_ACTION";
    public static final String OTA_STOP_ACTION = "com.motorola.ccc.ota.STOP_ACTION";
    public static final int OTA_WAITING_FOR_MODEM_UPDATE_STATUS_EXP_DAYS = 2;
    public static final String PROVISION_DEVICE_RESPONSE_INTENT = "com.motorola.blur.service.blur.Actions.CCE_PROVISION_DEVICE_RESPONSE";
    public static final String REFRESH_CHKUPDATE_UI_ON_SIMCHANGE = "com.motorola.ccc.ota.Actions.REFRESH_CHKUPDATE_UI_ON_SIMCHANGE";
    public static final String REGISTER_FORCE_UPGRADE_MANAGER = "com.motorola.ccc.ota.Actions.REGISTER_FORCE_UPGRADE_MANAGER";
    public static final String REGISTER_WIFI_DISCOVER_MANAGER = "com.motorola.ccc.ota.Actions.REGISTER_WIFI_DISCOVER_MANAGER";
    public static final String RESTART = "RESTART_DEFERRED";
    public static final long RESTART_PENDING_NOTIFICATION_EXPIRY_DAYS = 15;
    public static final String RUN_STATE_MACHINE = "com.motorola.ccc.ota.RUN_STATE_MACHINE";
    public static final String SETUP_TOS_ACCEPTED = "com.motorola.ccc.cce.SETUP_TOS_ACCEPTED";
    public static final int SIXTY_MINUTES = 60;
    public static final String SMART_UPDATE_OPTIN = "smartupdateOptin";
    public static final int SMART_UPDATE_RANDOM_NUMBER = 6000;
    public static final String SMART_UPDATE_USER_OPTIN = "com.motorola.ccc.ota.smart.update.USER_OPTIN";
    public static final String SOFTBANK_APN_NAME = "plus.acs.jp.v6";
    public static final String SOFTBANK_PROXY_HOST = "dmint.softbank.ne.jp";
    public static final int SOFTBANK_PROXY_PORT = 8080;
    public static final String START_BACKGROUND_INSTALLATION_FRAGMENT = "com.motorola.blur.service.blur.Actions.START_BACKGROUND_INSTALLATION_FRAGMENT";
    public static final String START_DOWNLOAD_PROGRESS_FRAGMENT = "com.motorola.blur.service.blur.Actions.START_DOWNLOAD_PROGRESS_FRAGMENT";
    public static final String START_MERGE_RESTART_ACTIVITY_INTENT = "com.motorola.ccc.ota.START_MERGE_RESTART_ACTIVITY_INTENT";
    public static final String START_RESTART_ACTIVITY_INTENT = "com.motorola.ccc.ota.START_RESTART_ACTIVITY_INTENT";
    public static final String STATS_TYPE = "ota_deferred";
    public static final long SYSTEM_UPDATE_POLICY_POSTPONE_INTERVAL = 2592000000L;
    public static final String UNREGISTER_FORCE_UPGRADE_MANAGER = "com.motorola.ccc.ota.Actions.UNREGISTER_FORCE_UPGRADE_MANAGER";
    public static final String UNREGISTER_WIFI_DISCOVER_MANAGER = "com.motorola.ccc.ota.Actions.UNREGISTER_WIFI_DISCOVER_MANAGER";
    public static final int UPDATE_ERROR_REASON_MAX_LENGTH = 6000;
    public static final String UPGRADE_ACTION_UPDATE_RESPONSE = "com.motorola.blur.service.blur.Actions.UPGRADE_ACTION_UPDATE_RESPONSE";
    public static final String UPGRADE_BACKGROUND_INSTALL_CANCEL_RESPONSE = "com.motorola.ccc.ota.UPGRADE_BACKGROUND_INSTALL_CANCEL_RESPONSE";
    public static final String UPGRADE_CHECK_FOR_UPDATE = "com.motorola.blur.service.blur.Actions.UPGRADE_CHECK_FOR_UPDATE";
    public static final String UPGRADE_CHECK_FOR_UPDATE_RESPONSE = "com.motorola.blur.service.blur.Actions.UPGRADE_CHECK_FOR_UPDATE_RESPONSE";
    public static final String UPGRADE_DOWNLOAD_NOTIFICATION_RESPONSE = "com.motorola.ccc.ota.UPGRADE_DOWNLOAD_NOTIFICATION_RESPONSE";
    public static final String UPGRADE_EXECUTE_UPGRADE = "com.motorola.blur.service.blur.Actions.UPGRADE_EXECUTE_UPGRADE";
    public static final String UPGRADE_INSTALL_NOTIFICATION_AVAILABLE = "com.motorola.ccc.ota.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE";
    public static final String UPGRADE_INSTALL_NOTIFICATION_AVAILABLE_RESPONSE = "com.motorola.ccc.ota.UPGRADE_INSTALL_NOTIFICATION_AVAILABLE_RESPONSE";
    public static final String UPGRADE_LAUNCH_UPGRADE = "com.motorola.blur.service.blur.Actions.UPGRADE_LAUNCH_UPGRADE";
    public static final String UPGRADE_RESTART_NOTIFICATION = "com.motorola.ccc.ota.UPGRADE_RESTART_NOTIFICATION";
    public static final String UPGRADE_UPDATER_BG_INSTALL_NOTIFICATION_CLEAR = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATER_BG_INSTALL_NOTIFICATION_CLEAR";
    public static final String UPGRADE_UPDATER_DOWNLOAD_NOTIFICATION_CLEAR = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATER_DOWNLOAD_NOTIFICATION_CLEAR";
    public static final String UPGRADE_UPDATER_INSTALL_NOTIFICATION_CLEAR = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATER_INSTALL_NOTIFICATION_CLEAR";
    public static final String UPGRADE_UPDATER_RESTART_NOTIFICATION_CLEAR = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATER_RESTART_NOTIFICATION_CLEAR";
    public static final String UPGRADE_UPDATER_STATE_CLEAR = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATER_STATE_CLEAR";
    public static final String UPGRADE_UPDATER_UPDATE_NOTIFICATION_CLEAR = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATER_UPDATE_NOTIFICATION_CLEAR";
    public static final String UPGRADE_UPDATE_DOWNLOAD_STATUS = "com.motorola.blur.service.blur.Actions.UPGRADE_DOWNLOAD_STATUS";
    public static final String UPGRADE_UPDATE_NOTIFICATION = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATE_NOTIFICATION";
    public static final String UPGRADE_UPDATE_NOTIFICATION_AVAILABLE = "com.motorola.ccc.ota.UPGRADE_UPDATE_NOTIFICATION_AVAILABLE";
    public static final String UPGRADE_UPDATE_NOTIFICATION_AVAILABLE_RESPONSE = "com.motorola.ccc.ota.UPGRADE_UPDATE_NOTIFICATION_AVAILABLE_RESPONSE";
    public static final String UPGRADE_UPDATE_NOTIFICATION_RESPONSE = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATE_NOTIFICATION_RESPONSE";
    public static final String UPGRADE_UPDATE_STATUS = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATE_STATUS";
    public static final String UPGRADE_UPDATE_VERIFY_PAYLOAD_METADATA_DOWNLOAD_STATUS = "com.motorola.blur.service.blur.Actions.UPGRADE_UPDATE_VERIFY_PAYLOAD_METADATA_DOWNLOAD_STATUS";
    public static final String USER_BACKGROUND_INSTALL_RESPONSE = "com.motorola.ccc.ota.USER_BACKGROUND_INSTALL_RESPONSE";
    public static final int VAB_MERGE_REBOOT_FAILURE_MAX_COUNT = 1;
    public static final long WAITING_FOR_NETWORK = -1729;
    public static final int WARNING_ALERT_DIALOG = 0;
    public static final String WIFI_DISCOVER_TIMER_EXPIRY = "com.motorola.ccc.ota.Actions.WIFI_DISCOVER_TIMER_EXPIRY";

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum CheckSpaceEnum {
        SPACE_AVAILABLE,
        SPACE_NOT_AVAILABLE,
        SPACE_AVAILABLE_AFTER_LOW_STORAGE
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum FragmentTypeEnum {
        CHECK_UPDATE_FRAGMENT,
        DOWNLOAD_FRAGMENT,
        DOWNLOAD_PROGRESS_FRAGMENT,
        BACKGROUND_INSTALLATION_FRAGMENT,
        INSTALL_FRAGMENT,
        RESTART_FRAGMENT,
        MERGE_RESTART_FRAGMENT,
        UPDATE_COMPLETE_FRAGMENT,
        SMART_UPDATE_FRAGMENT,
        UPDATE_PREF_FRAGMENT,
        UPDATE_FAILED_FRAGMENT
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum ResponseFlavour {
        RESPONSE_FLAVOUR_WIFI,
        RESPONSE_FLAVOUR_WIFI_AND_MOBILE
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static final class UpdateStatus {
        public static final int ATTEMPTING_ROLLBACK = 8;
        public static final int CHECKING_FOR_UPDATE = 1;
        public static final int CLEANUP_PREVIOUS_UPDATE = 11;
        public static final int DISABLED = 9;
        public static final int DOWNLOADING = 3;
        public static final int FINALIZING = 5;
        public static final int IDLE = 0;
        public static final int REPORTING_ERROR_EVENT = 7;
        public static final int UPDATED_NEED_REBOOT = 6;
        public static final int UPDATE_AVAILABLE = 2;
        public static final int VERIFYING = 4;
    }

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public enum networkType {
        WIFI,
        CELLULAR,
        CELL3G,
        CELL4G,
        ROAMING,
        UNKNOWN
    }
}
