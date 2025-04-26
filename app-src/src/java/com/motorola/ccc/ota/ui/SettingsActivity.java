package com.motorola.ccc.ota.ui;

import android.app.UiModeManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.utils.Logger;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class SettingsActivity extends AppCompatActivity {
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    private FrameLayout frameLayout;
    private FrameLayout frameLayoutMenu;

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Logger.debug("OtaApp", "SettingsActivity, onCreate");
        setContentView(R.layout.ota_activity_main);
        this.frameLayout = (FrameLayout) findViewById(R.id.fragment_id);
        this.frameLayoutMenu = (FrameLayout) findViewById(R.id.fragment_id_menu);
        this.frameLayout.setVisibility(8);
        this.frameLayoutMenu.setVisibility(0);
        this.fragmentManager = getSupportFragmentManager();
        addMenuFragment(getIntent());
        if (UpdaterUtils.isBatterySaverEnabled(this)) {
            return;
        }
        Window window = getWindow();
        WindowInsetsControllerCompat windowInsetsControllerCompat = new WindowInsetsControllerCompat(window, window.getDecorView());
        if (((UiModeManager) getSystemService("uimode")).getNightMode() != 2) {
            windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
            windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
        }
    }

    protected void onResume() {
        super.onResume();
        Logger.debug("OtaApp", "SettingsActivity, onResume");
        addMenuFragment(getIntent());
    }

    private void addMenuFragment(Intent intent) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(UpdaterUtils.KEY_ACTIVITY_INTENT, intent);
        this.fragmentTransaction = this.fragmentManager.beginTransaction();
        MenuFragment menuFragment = new MenuFragment();
        menuFragment.setArguments(bundle);
        this.fragmentTransaction.replace((int) R.id.fragment_id_menu, menuFragment);
        this.fragmentTransaction.commit();
    }
}
