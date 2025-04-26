package com.motorola.ccc.ota.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.ccc.ota.utils.SmartUpdateUtils;
import com.motorola.ccc.ota.utils.UpgradeUtilConstants;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class WebViewFragment extends DialogFragment implements SmartUpdateUtils.SmartDialog {
    private Handler handler = new Handler() { // from class: com.motorola.ccc.ota.ui.WebViewFragment.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what != 1) {
                return;
            }
            WebViewFragment.this.webViewGoBack();
        }
    };
    private WebView mWebview;
    private View rootView;
    private String webViewBaseFragment;
    private String webViewURL;

    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public static class ProgressDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle bundle) {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle(R.string.dialog_progress);
            return progressDialog;
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setStyle(0, R.style.CustomiseActivityTheme);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.webViewURL = arguments.getString(UpgradeUtilConstants.KEY_WEBVIEW_URL);
            this.webViewBaseFragment = arguments.getString(UpgradeUtilConstants.KEY_WEBVIEW_BASE_FRAGMENT_STATS);
        }
        collectStats();
    }

    private void collectStats() {
        BotaSettings botaSettings = new BotaSettings();
        String str = this.webViewBaseFragment;
        str.hashCode();
        if (str.equals("whyUpdateMatters")) {
            botaSettings.incrementPrefs(Configs.WHY_UPDATE_MATTERS_OS_LINK_COUNT);
        } else if (TextUtils.isEmpty(botaSettings.getString(Configs.STATS_OS_LINK_LAUNCH_SCREEN))) {
            botaSettings.setString(Configs.STATS_OS_LINK_LAUNCH_SCREEN, this.webViewBaseFragment);
        }
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.rootView = layoutInflater.inflate(R.layout.web_view_fragment, viewGroup, false);
        findViewsById();
        if (bundle != null) {
            this.mWebview.restoreState(bundle);
        } else {
            showProgressBar();
            this.mWebview.loadUrl(this.webViewURL);
        }
        this.mWebview.setOnKeyListener(new View.OnKeyListener() { // from class: com.motorola.ccc.ota.ui.WebViewFragment.2
            @Override // android.view.View.OnKeyListener
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == 4 && keyEvent.getAction() == 1 && WebViewFragment.this.mWebview.canGoBack()) {
                    WebViewFragment.this.handler.sendEmptyMessage(1);
                    return true;
                }
                return false;
            }
        });
        return this.rootView;
    }

    private void findViewsById() {
        this.mWebview = (WebView) this.rootView.findViewById(R.id.html_view);
        fillWebViewParameters();
    }

    private void fillWebViewParameters() {
        this.mWebview.getSettings().setJavaScriptEnabled(true);
        this.mWebview.getSettings().setBuiltInZoomControls(true);
        this.mWebview.getSettings().supportZoom();
        this.mWebview.getSettings().setLoadWithOverviewMode(true);
        this.mWebview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        this.mWebview.setWebViewClient(new WebViewClient() { // from class: com.motorola.ccc.ota.ui.WebViewFragment.3
            @Override // android.webkit.WebViewClient
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
                return super.shouldInterceptRequest(webView, webResourceRequest);
            }

            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                WebViewFragment.this.showProgressBar();
                return false;
            }

            @Override // android.webkit.WebViewClient
            public void onPageCommitVisible(WebView webView, String str) {
                WebViewFragment.this.dismissProgressBar();
                super.onPageCommitVisible(webView, str);
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView, String str) {
                super.onPageFinished(webView, str);
            }

            @Override // android.webkit.WebViewClient
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                WebViewFragment.this.dismissProgressBar();
                Logger.debug("OtaApp", "URL recieved error");
                super.onReceivedError(webView, webResourceRequest, webResourceError);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showProgressBar() {
        FragmentActivity activity = getActivity();
        if (isRemoving() || isDetached() || activity == null || activity.getSupportFragmentManager().findFragmentByTag("progressdialog") != null) {
            return;
        }
        ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
        progressDialogFragment.setCancelable(true);
        FragmentTransaction beginTransaction = activity.getSupportFragmentManager().beginTransaction();
        beginTransaction.add(progressDialogFragment, "progressdialog");
        beginTransaction.commitAllowingStateLoss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissProgressBar() {
        ProgressDialogFragment findFragmentByTag;
        FragmentActivity activity = getActivity();
        if (activity == null || (findFragmentByTag = activity.getSupportFragmentManager().findFragmentByTag("progressdialog")) == null) {
            return;
        }
        findFragmentByTag.dismissAllowingStateLoss();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void webViewGoBack() {
        this.mWebview.goBack();
    }

    @Override // com.motorola.ccc.ota.utils.SmartUpdateUtils.SmartDialog
    public void dismissSmartDialog() {
        dismiss();
    }
}
