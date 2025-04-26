package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class AdvNoticeFragment extends Fragment {
    private Button btnDone;
    private ImageView imgAdvance;
    private Activity mActivity;
    private Context mContext;
    private ProgressBar mProgressBar;
    private View mRootView;
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
        this.mRootView = layoutInflater.inflate(R.layout.advance_notice, viewGroup, false);
        findViewsById();
        return this.mRootView;
    }

    private void findViewsById() {
        this.mProgressBar = (ProgressBar) this.mRootView.findViewById(R.id.progress);
        this.btnDone = (Button) this.mRootView.findViewById(R.id.btnDone);
        this.imgAdvance = (ImageView) this.mRootView.findViewById(R.id.imgAdvance);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        downloadImage();
        this.btnDone.setOnClickListener(new View.OnClickListener() { // from class: com.motorola.ccc.ota.ui.AdvNoticeFragment$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AdvNoticeFragment.this.m155lambda$onActivityCreated$0$commotorolacccotauiAdvNoticeFragment(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: lambda$onActivityCreated$0$com-motorola-ccc-ota-ui-AdvNoticeFragment  reason: not valid java name */
    public /* synthetic */ void m155lambda$onActivityCreated$0$commotorolacccotauiAdvNoticeFragment(View view) {
        this.mActivity.finish();
    }

    private void downloadImage() {
        try {
            Glide.with(this).load(this.settings.getString(Configs.ADVANCE_NOTICE_URL).trim()).listener(new RequestListener<String, GlideDrawable>() { // from class: com.motorola.ccc.ota.ui.AdvNoticeFragment.1
                public /* bridge */ /* synthetic */ boolean onException(Exception exc, Object obj, Target target, boolean z) {
                    return onException(exc, (String) obj, (Target<GlideDrawable>) target, z);
                }

                public /* bridge */ /* synthetic */ boolean onResourceReady(Object obj, Object obj2, Target target, boolean z, boolean z2) {
                    return onResourceReady((GlideDrawable) obj, (String) obj2, (Target<GlideDrawable>) target, z, z2);
                }

                public boolean onException(Exception exc, String str, Target<GlideDrawable> target, boolean z) {
                    Logger.debug("OtaApp", "downloadImage.onException");
                    AdvNoticeFragment.this.btnDone.setVisibility(8);
                    AdvNoticeFragment.this.mProgressBar.setVisibility(8);
                    AdvNoticeFragment.this.showUptoDateScreen();
                    return false;
                }

                public boolean onResourceReady(GlideDrawable glideDrawable, String str, Target<GlideDrawable> target, boolean z, boolean z2) {
                    AdvNoticeFragment.this.mProgressBar.setVisibility(8);
                    AdvNoticeFragment.this.btnDone.setVisibility(0);
                    return false;
                }
            }).into(this.imgAdvance);
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
}
