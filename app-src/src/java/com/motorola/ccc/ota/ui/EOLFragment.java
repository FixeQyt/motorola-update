package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.DateFormatUtils;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class EOLFragment extends Fragment {
    private Button btnCheckNow;
    private ImageView imgEOL;
    private long launchTimeInMillis;
    private Activity mActivity;
    private Context mContext;
    private boolean mCouponShown;
    private TextView mEOLAdditionalInfo;
    private ProgressBar mProgressBar;
    private View mRootView;
    private TextView mSecurityPatch;
    private BotaSettings settings;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.settings = new BotaSettings();
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.end_of_life, viewGroup, false);
        findViewsById();
        this.settings.incrementPrefs(Configs.EOL_VISIT_COUNT);
        this.launchTimeInMillis = System.currentTimeMillis();
        return this.mRootView;
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        checkAndDisplayText(this.mSecurityPatch, getContext().getString(R.string.eol_last_update_text), DateFormatUtils.getSecurityPatch());
        if (!TextUtils.isEmpty(this.settings.getString(Configs.END_OF_LIFE_ADDITIONAL_INFO))) {
            this.mEOLAdditionalInfo.setText(Html.fromHtml(this.settings.getString(Configs.END_OF_LIFE_ADDITIONAL_INFO), 0, null, new HtmlUtils(this.settings.getString(Configs.END_OF_LIFE_ADDITIONAL_INFO))));
        } else {
            this.mEOLAdditionalInfo.setVisibility(8);
        }
        if (URLUtil.isHttpsUrl(this.settings.getString(Configs.END_OF_LIFE_PROMO_IMAGE_URL))) {
            this.mCouponShown = true;
            this.btnCheckNow.setText(getContext().getString(R.string.eol_promote));
            downloadImage(this.settings.getString(Configs.END_OF_LIFE_PROMO_IMAGE_URL));
        } else {
            this.btnCheckNow.setText(getContext().getString(R.string.eol_explore));
            downloadImage(this.settings.getString(Configs.END_OF_LIFE_MAIN_IMAGE_URL));
        }
        this.imgEOL.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.EOLFragment.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                EOLFragment.this.sendIntentToWebsite();
            }
        });
        this.btnCheckNow.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.EOLFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                EOLFragment.this.sendIntentToWebsite();
            }
        });
    }

    private void findViewsById() {
        this.mProgressBar = (ProgressBar) this.mRootView.findViewById(R.id.progress);
        this.btnCheckNow = (Button) this.mRootView.findViewById(R.id.check_now);
        this.imgEOL = (ImageView) this.mRootView.findViewById(R.id.eol_image);
        this.mSecurityPatch = (TextView) this.mRootView.findViewById(R.id.security_patch);
        this.mEOLAdditionalInfo = (TextView) this.mRootView.findViewById(R.id.eol_additional_info);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendIntentToWebsite() {
        String string = this.settings.getString(Configs.END_OF_LIFE_PROMO_LINK_URL);
        Intent intent = new Intent("android.intent.action.VIEW");
        if (this.mCouponShown && URLUtil.isHttpsUrl(string)) {
            this.settings.incrementPrefs(Configs.PROMOTIONAL_LINK_CLICK_COUNT);
            intent.setData(Uri.parse(string));
        } else {
            this.settings.incrementPrefs(Configs.NON_PROMOTIONAL_LINK_CLICK_COUNT);
            intent.setData(Uri.parse(UpgradeUtilConstants.MOTOROLA_WEBSITE));
        }
        startActivity(intent);
    }

    private void downloadImage(String str) {
        try {
            Glide.with(this).load(str).listener(new RequestListener<String, GlideDrawable>() { // from class: com.motorola.ccc.ota.ui.EOLFragment.3
                public /* bridge */ /* synthetic */ boolean onException(Exception exc, Object obj, Target target, boolean z) {
                    return onException(exc, (String) obj, (Target<GlideDrawable>) target, z);
                }

                public /* bridge */ /* synthetic */ boolean onResourceReady(Object obj, Object obj2, Target target, boolean z, boolean z2) {
                    return onResourceReady((GlideDrawable) obj, (String) obj2, (Target<GlideDrawable>) target, z, z2);
                }

                public boolean onException(Exception exc, String str2, Target<GlideDrawable> target, boolean z) {
                    Logger.debug("OtaApp", "downloadImage.onException");
                    EOLFragment.this.mProgressBar.setVisibility(8);
                    EOLFragment.this.showUptoDateScreen();
                    return false;
                }

                public boolean onResourceReady(GlideDrawable glideDrawable, String str2, Target<GlideDrawable> target, boolean z, boolean z2) {
                    EOLFragment.this.mProgressBar.setVisibility(8);
                    return false;
                }
            }).into(this.imgEOL);
        } catch (Exception e) {
            this.mProgressBar.setVisibility(8);
            Logger.error("OtaApp", "Glide library exception : " + e);
            showUptoDateScreen();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showUptoDateScreen() {
        FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
        beginTransaction.replace((int) R.id.fragment_id, new UptoDateFragment());
        beginTransaction.commit();
    }

    private void checkAndDisplayText(TextView textView, String str, String str2) {
        if (str2 != null) {
            textView.setText(str + SystemUpdateStatusUtils.SPACE + str2);
        } else {
            textView.setVisibility(8);
        }
    }

    public void onStop() {
        super.onStop();
        this.settings.setLong(Configs.TOTAL_TIME_SPEND_ON_EOL_SCREEN, this.settings.getLong(Configs.TOTAL_TIME_SPEND_ON_EOL_SCREEN, 0L) + (System.currentTimeMillis() - this.launchTimeInMillis));
    }
}
