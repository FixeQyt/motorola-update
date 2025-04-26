package com.motorola.otalib.common.metaData;

import com.motorola.otalib.common.utils.UpgradeUtils;
import java.io.Serializable;
import java.net.URI;
import org.json.JSONObject;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class MetaData implements Serializable {
    private String abInstallType;
    private String annoy;
    private boolean continueOnServiceError;
    private String displayVersion;
    private String downloadOptionsNotes;
    private DownloadTimes downloadTimes;
    private URI downloadUrl;
    private long extraSpace;
    private String fingerprint;
    private boolean forced;
    private String installReminder;
    private int installTime;
    private String mActualTargetVersion;
    private int mBitmap;
    private boolean mBypassPreDownloadDialog;
    private long mChunkSize;
    private int mCriticalDeferCount;
    private int mCriticalUpdateExtraWaitCount;
    private long mCriticalUpdateExtraWaitPeriod;
    private int mCriticalUpdateReminder;
    private int mDownloadEndTime;
    private int mDownloadStartTime;
    private boolean mEnterpriseOta;
    private long mExtraCacheSpace;
    private double mForceDownloadTime;
    private double mForceInstallTime;
    private final boolean mForceOnCellular;
    private int mForceUpgradeTime;
    private int mIncrementalVersion;
    private int mMaxUpdateFailCount;
    private int mMinBatteryLevelRequiredForInstall;
    private boolean mOemConfigUpdate;
    private int mOptionalDeferCount;
    private int mOptionalUpdateCancelReminderDays;
    private boolean mPolicyBundle;
    private boolean mRebootRequired;
    private long mReserveSpaceInMb;
    private int mSeverity;
    private boolean mShowDownloadProgress;
    private boolean mShowPostInstallScreen;
    private boolean mShowPreInstallScreen;
    private JSONObject mUiWorkflowControl;
    private String mUpdateType;
    private String md5Checksum;
    private String metaVersion;
    private String minVersion;
    private OffPeakDownload offPeakDownload;
    private String osReleaseLink;
    private final String packageType;
    private String postInstallFailureMessage;
    private String postInstallNotes;
    private String preDownloadInstructions;
    private int preDownloadNotificationExpiryMins;
    private String preInstallInstructions;
    private String preInstallNotes;
    private int preInstallNotificationExpiryMins;
    private String releaseNotes;
    private String reportingTags;
    private boolean serviceControlEnabled;
    private int serviceTimeoutSeconds;
    private boolean showDownloadOptions;
    private boolean showPreDownloadDialog;
    private long size;
    private long sourceBuildTimestamp;
    private String sourceSha1;
    private JSONObject streamingData;
    private String targetOSVersion;
    private String targetSha1;
    private String trackingId;
    private String updateReqTriggeredBy;
    private String upgradeNotification;
    private long userDataRequiredForUpdate;
    private String version;
    private boolean wifiOnly;

    public MetaData(String str, String str2, String str3, boolean z, String str4, String str5, long j, String str6, long j2, String str7, String str8, boolean z2, String str9, int i, boolean z3, int i2, boolean z4, String str10, String str11, String str12, String str13, String str14, String str15, String str16, DownloadTimes downloadTimes, OffPeakDownload offPeakDownload, boolean z5, boolean z6, int i3, int i4, JSONObject jSONObject, int i5, int i6, int i7, int i8, boolean z7, boolean z8, boolean z9, boolean z10, boolean z11, int i9, double d, double d2, int i10, boolean z12, int i11, int i12, boolean z13, long j3, int i13, long j4, boolean z14, String str17, boolean z15, String str18, long j5, String str19, int i14, int i15, String str20, JSONObject jSONObject2, String str21, String str22, long j6, String str23, String str24, String str25, String str26, String str27, long j7, int i16, int i17, long j8) {
        this.metaVersion = str;
        this.version = str2;
        this.minVersion = str3;
        this.forced = z;
        this.releaseNotes = str5;
        this.downloadOptionsNotes = str4;
        this.size = j;
        this.md5Checksum = str6;
        this.extraSpace = j2;
        this.annoy = str7;
        this.installReminder = str8;
        this.wifiOnly = z2;
        this.fingerprint = str9;
        this.installTime = i;
        this.serviceControlEnabled = z3;
        this.serviceTimeoutSeconds = i2;
        this.continueOnServiceError = z4;
        this.upgradeNotification = str10;
        this.preInstallNotes = str11;
        this.postInstallNotes = str12;
        this.postInstallFailureMessage = str13;
        this.reportingTags = str14;
        this.trackingId = str15;
        this.updateReqTriggeredBy = str16;
        this.showPreDownloadDialog = z5;
        this.showDownloadOptions = z6;
        this.preDownloadNotificationExpiryMins = i3;
        this.preInstallNotificationExpiryMins = i4;
        this.mUiWorkflowControl = jSONObject;
        this.mOptionalDeferCount = i5;
        this.mCriticalDeferCount = i6;
        this.mMaxUpdateFailCount = i7;
        this.mMinBatteryLevelRequiredForInstall = i8;
        this.mBypassPreDownloadDialog = z7;
        this.mShowDownloadProgress = z8;
        this.mShowPreInstallScreen = z9;
        this.mShowPostInstallScreen = z10;
        this.mRebootRequired = z11;
        this.mIncrementalVersion = i9;
        this.mForceDownloadTime = d;
        this.mForceInstallTime = d2;
        this.mForceUpgradeTime = i10;
        this.mForceOnCellular = z12;
        this.mDownloadStartTime = i11;
        this.mDownloadEndTime = i12;
        this.mPolicyBundle = z13;
        this.mExtraCacheSpace = j3;
        this.mOptionalUpdateCancelReminderDays = i13;
        this.mReserveSpaceInMb = j4;
        this.mEnterpriseOta = z14;
        this.displayVersion = str17;
        this.mOemConfigUpdate = z15;
        this.mUpdateType = str18;
        this.mChunkSize = j5;
        this.mActualTargetVersion = str19;
        this.mSeverity = i14;
        this.mCriticalUpdateReminder = i15;
        this.abInstallType = str20;
        this.streamingData = jSONObject2;
        this.osReleaseLink = str21;
        this.targetOSVersion = str22;
        this.preDownloadInstructions = str23;
        this.preInstallInstructions = str24;
        this.sourceBuildTimestamp = j6;
        this.sourceSha1 = str25;
        this.targetSha1 = str26;
        this.packageType = str27;
        this.mCriticalUpdateExtraWaitCount = i16;
        this.mCriticalUpdateExtraWaitPeriod = j7;
        this.mBitmap = i17;
        this.userDataRequiredForUpdate = j8;
    }

    public String getMetaVersion() {
        return this.metaVersion;
    }

    public String getVersion() {
        return this.version;
    }

    public String getMinVersion() {
        return this.minVersion;
    }

    public boolean isForced() {
        return this.forced;
    }

    public String getReleaseNotes() {
        return this.releaseNotes;
    }

    public String getDownloadOptionsNotes() {
        return this.downloadOptionsNotes;
    }

    public long getSize() {
        return this.size;
    }

    public String getmd5CheckSum() {
        return this.md5Checksum;
    }

    public long getExtraSpace() {
        return this.extraSpace;
    }

    public String getAnnoy() {
        return this.annoy;
    }

    public String getInstallReminder() {
        return this.installReminder;
    }

    public boolean isWifiOnly() {
        return this.wifiOnly;
    }

    public String getFingerprint() {
        return this.fingerprint;
    }

    public URI getDownloadUrl() {
        return this.downloadUrl;
    }

    public int getInstallTime() {
        return this.installTime;
    }

    public boolean isServiceControlEnabled() {
        return this.serviceControlEnabled;
    }

    public int getServiceTimeoutSeconds() {
        return this.serviceTimeoutSeconds;
    }

    public boolean isContinueOnServiceError() {
        return this.continueOnServiceError;
    }

    public String getUpgradeNotification() {
        return this.upgradeNotification;
    }

    public String getPreInstallNotes() {
        return this.preInstallNotes;
    }

    public String getPostInstallNotes() {
        return this.postInstallNotes;
    }

    public String getPostInstallFailureMessage() {
        return this.postInstallFailureMessage;
    }

    public String getReportingTags() {
        return this.reportingTags;
    }

    public String getTrackingId() {
        return this.trackingId;
    }

    public String getUpdateReqTriggeredBy() {
        return this.updateReqTriggeredBy;
    }

    public DownloadTimes getDownloadTimes() {
        return this.downloadTimes;
    }

    public OffPeakDownload getOffPeakDownload() {
        return this.offPeakDownload;
    }

    public boolean showPreDownloadDialog() {
        return this.showPreDownloadDialog;
    }

    public boolean showDownloadOptions() {
        return this.showDownloadOptions;
    }

    public int getPreDownloadNotificationExpiryMins() {
        return this.preDownloadNotificationExpiryMins;
    }

    public int getPreInstallNotificationExpiryMins() {
        return this.preInstallNotificationExpiryMins;
    }

    public JSONObject getUiWorkflowControl() {
        return this.mUiWorkflowControl;
    }

    public int getOptionalDeferCount() {
        return this.mOptionalDeferCount;
    }

    public int getCriticalDeferCount() {
        int i = this.mCriticalDeferCount;
        if (i >= 0) {
            return i;
        }
        return 3;
    }

    public int getMaxUpdateFailCount() {
        return this.mMaxUpdateFailCount;
    }

    public int getminBatteryRequiredForInstall() {
        return this.mMinBatteryLevelRequiredForInstall;
    }

    public boolean getByPassPreDownloadDialog() {
        return this.mBypassPreDownloadDialog;
    }

    public boolean showDownloadProgress() {
        return this.mShowDownloadProgress;
    }

    public boolean showPreInstallScreen() {
        return this.mShowPreInstallScreen;
    }

    public boolean showPostInstallScreen() {
        return this.mShowPostInstallScreen;
    }

    public boolean getRebootRequired() {
        return this.mRebootRequired;
    }

    public int getIncrementalVersion() {
        return this.mIncrementalVersion;
    }

    public double getForceDownloadTime() {
        return this.mForceDownloadTime;
    }

    public double getForceInstallTime() {
        return this.mForceInstallTime;
    }

    public boolean isForceInstallTimeSet() {
        return Double.compare(getForceInstallTime(), -1.0d) > 0;
    }

    public int getForceUpgradeTime() {
        return this.mForceUpgradeTime;
    }

    public boolean isForceOnCellular() {
        return this.mForceOnCellular;
    }

    public int getDownloadStartTime() {
        return this.mDownloadStartTime;
    }

    public int getDownloadEndTime() {
        return this.mDownloadEndTime;
    }

    public boolean getPolicyBundle() {
        return this.mPolicyBundle;
    }

    public int getOptionalUpdateCancelReminderDays() {
        return this.mOptionalUpdateCancelReminderDays;
    }

    public long getReserveSpaceInMb() {
        return this.mReserveSpaceInMb;
    }

    public boolean getEnterpriseOta() {
        return this.mEnterpriseOta;
    }

    public String getDisplayVersion() {
        return this.displayVersion;
    }

    public boolean getOemConfigUpdateData() {
        return this.mOemConfigUpdate;
    }

    public String getUpdateTypeData() {
        return this.mUpdateType;
    }

    public long getChunkSize() {
        return this.mChunkSize;
    }

    public String getmActualTargetVersion() {
        return this.mActualTargetVersion;
    }

    public int getSeverity() {
        return this.mSeverity;
    }

    public int getCriticalUpdateReminder() {
        int i = this.mCriticalUpdateReminder;
        return i >= 0 ? i : UpgradeUtils.DEFAULT_CRITICAL_UPDATE_ANNOY_VALUE;
    }

    public String getAbInstallType() {
        return this.abInstallType;
    }

    public JSONObject getStreamingData() {
        return this.streamingData;
    }

    public String getOSreleaseLink() {
        return this.osReleaseLink;
    }

    public String getTargetOSVersion() {
        return this.targetOSVersion;
    }

    public String getPreDownloadInstructions() {
        return this.preDownloadInstructions;
    }

    public String getPreInstallInstructions() {
        return this.preInstallInstructions;
    }

    public long getSourceBuildTimeStamp() {
        return this.sourceBuildTimestamp;
    }

    public String getTargetSha1() {
        return this.targetSha1;
    }

    public String getSourceSha1() {
        return this.sourceSha1;
    }

    public String getPackageType() {
        return this.packageType;
    }

    public long getUserDataRequiredForUpdate() {
        return this.userDataRequiredForUpdate;
    }

    public long getCriticalUpdateExtraWaitPeriod() {
        long j = this.mCriticalUpdateExtraWaitPeriod;
        if (j >= 0) {
            return j;
        }
        return 10L;
    }

    public int getCriticalUpdateExtraWaitCount() {
        int i = this.mCriticalUpdateExtraWaitCount;
        if (i >= 0) {
            return i;
        }
        return 6;
    }

    public int getBitmap() {
        return this.mBitmap;
    }
}
