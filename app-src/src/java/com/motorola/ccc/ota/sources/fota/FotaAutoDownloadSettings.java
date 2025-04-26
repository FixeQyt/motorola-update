package com.motorola.ccc.ota.sources.fota;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.env.AndroidFotaInterface;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.sources.fota.FotaConstants;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import com.motorola.ccc.ota.utils.BuildPropReader;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.PMUtils;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;
import com.motorola.otalib.common.metaData.MetaData;
import com.motorola.otalib.common.metaData.builder.MetaDataBuilder;
import com.motorola.otalib.common.utils.BroadcastUtils;
import com.motorola.otalib.common.utils.NetworkUtils;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class FotaAutoDownloadSettings extends DialogFragment implements SmartUpdateUtils.SmartDialog {
    ConnectivityManager cm;
    private ImageView imgBack;
    private FragmentActivity mActivity;
    private Context mContext;
    private View mRootView;
    MetaData metaData;
    private RadioGroup radioGroupWifiCellular;
    private RadioButton radioWifi;
    private RadioButton radioWifiCellular;
    private BotaSettings settings;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.settings = new BotaSettings();
        this.cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        setStyle(0, R.style.CustomiseActivityTheme);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof FragmentActivity) {
            this.mActivity = (FragmentActivity) context;
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.auto_download_settings, viewGroup, false);
        this.metaData = MetaDataBuilder.from(this.settings.getString(Configs.METADATA));
        UpdaterUtils.setNavBarColorFromDialog(getDialog());
        Window window = getDialog().getWindow();
        window.setStatusBarColor(0);
        if (!UpdaterUtils.isBatterySaverEnabled(this.mContext)) {
            WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
            if (((UiModeManager) this.mContext.getSystemService("uimode")).getNightMode() != 2) {
                windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
                windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
            }
        }
        findViewsById();
        handleListeners();
        return this.mRootView;
    }

    private void findViewsById() {
        this.imgBack = (ImageView) this.mRootView.findViewById(R.id.imgBack);
        this.radioGroupWifiCellular = (RadioGroup) this.mRootView.findViewById(R.id.radioGroupWifiCellular);
        this.radioWifi = (RadioButton) this.mRootView.findViewById(R.id.radioWifi);
        this.radioWifiCellular = (RadioButton) this.mRootView.findViewById(R.id.radioWifiCellular);
    }

    private void handleListeners() {
        this.radioGroupWifiCellular.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { // from class: com.motorola.ccc.ota.sources.fota.FotaAutoDownloadSettings.1
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radioWifi /* 2131296571 */:
                        FotaAutoDownloadSettings.this.settings.setString(Configs.USER_AUTO_DOWNLOAD_OPTION, FotaConstants.AutoDownloadOption.WiFi.toString());
                        if (BuildPropReader.isFotaATT()) {
                            AndroidFotaInterface.sendAutoDownloadSettingsToFota(FotaAutoDownloadSettings.this.mContext, FotaConstants.AutoDownloadOption.WiFi);
                            return;
                        }
                        FotaUpgradeSource.sendFotaDownloadModeChanged(FotaAutoDownloadSettings.this.mContext);
                        if (FotaAutoDownloadSettings.this.metaData != null) {
                            FotaAutoDownloadSettings.this.settings.setString(Configs.FLAVOUR, UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI.name());
                            if (FotaAutoDownloadSettings.this.metaData.isWifiOnly()) {
                                return;
                            }
                            FotaAutoDownloadSettings.this.broadcastPollingIntent();
                            return;
                        }
                        return;
                    case R.id.radioWifiCellular /* 2131296572 */:
                        FotaAutoDownloadSettings.this.settings.setString(Configs.USER_AUTO_DOWNLOAD_OPTION, FotaConstants.AutoDownloadOption.OTAorWiFi.toString());
                        if (BuildPropReader.isFotaATT()) {
                            AndroidFotaInterface.sendAutoDownloadSettingsToFota(FotaAutoDownloadSettings.this.mContext, FotaConstants.AutoDownloadOption.OTAorWiFi);
                            return;
                        }
                        FotaUpgradeSource.sendFotaDownloadModeChanged(FotaAutoDownloadSettings.this.mContext);
                        if (FotaAutoDownloadSettings.this.metaData != null) {
                            FotaAutoDownloadSettings.this.settings.setString(Configs.FLAVOUR, UpgradeUtilConstants.ResponseFlavour.RESPONSE_FLAVOUR_WIFI_AND_MOBILE.name());
                            FotaAutoDownloadSettings.this.broadcastPollingIntent();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        });
        this.imgBack.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.sources.fota.FotaAutoDownloadSettings$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                FotaAutoDownloadSettings.this.m141lambda$handleListeners$0$commotorolacccotasourcesfotaFotaAutoDownloadSettings(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$handleListeners$0$com-motorola-ccc-ota-sources-fota-FotaAutoDownloadSettings  reason: not valid java name */
    public /* synthetic */ void m141lambda$handleListeners$0$commotorolacccotasourcesfotaFotaAutoDownloadSettings(View view) {
        if (getFragmentManager().getFragments().toArray().length <= 1) {
            this.mActivity.finish();
        }
        dismiss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void broadcastPollingIntent() {
        Intent intent = new Intent(PMUtils.POLLINGMGR_CONNECTIVITY);
        intent.putExtra(PMUtils.KEY_CONNECTIVITY_EXTRA, NetworkUtils.hasNetwork(this.cm));
        intent.putExtra(PMUtils.KEY_CONNECTIVITY_TYPE_EXTRA, NetworkUtils.getNetworkCapabilityType(this.cm));
        BroadcastUtils.sendLocalBroadcast(OtaApplication.getGlobalContext(), intent);
    }

    private void initUI() {
        boolean isForceOnCellular;
        if (FotaConstants.AutoDownloadOption.OTAorWiFi.toString().equals(this.settings.getString(Configs.USER_AUTO_DOWNLOAD_OPTION))) {
            this.radioWifiCellular.setChecked(true);
        } else {
            this.radioWifi.setChecked(true);
        }
        MetaData metaData = this.metaData;
        boolean isWifiOnly = metaData != null ? metaData.isWifiOnly() : false;
        String string = this.settings.getString(Configs.SERVER_FOTA_TRANSPORTMEDIA_VALUE);
        if (BuildPropReader.isFotaATT()) {
            isWifiOnly = FotaConstants.AutoDownloadOption.WiFi.toString().equalsIgnoreCase(string);
        }
        if (BuildPropReader.isFotaATT()) {
            isForceOnCellular = FotaConstants.AutoDownloadOption.RAN.toString().equalsIgnoreCase(string);
        } else {
            isForceOnCellular = UpdaterUtils.isForceOnCellular();
        }
        Logger.debug("OtaApp", "FotaAutoDownloadSettings:pkg wifiOnly=" + isWifiOnly + " : isForceOnCellular=" + isForceOnCellular);
        if (isForceOnCellular || UpdaterUtils.getAutomaticDownloadForCellular()) {
            this.radioWifiCellular.setEnabled(false);
            this.radioWifiCellular.setAlpha(0.5f);
            this.radioWifi.setEnabled(false);
            this.radioWifi.setAlpha(0.5f);
        } else if (isWifiOnly) {
            this.radioWifiCellular.setEnabled(false);
            this.radioWifiCellular.setAlpha(0.5f);
            this.radioWifi.setEnabled(true);
            this.radioWifi.setAlpha(1.0f);
        } else {
            this.radioWifiCellular.setEnabled(true);
            this.radioWifiCellular.setAlpha(1.0f);
            this.radioWifi.setEnabled(true);
            this.radioWifi.setAlpha(1.0f);
        }
    }

    public void onResume() {
        super.onResume();
        initUI();
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.SmartDialog
    public void dismissSmartDialog() {
        dismiss();
    }
}
