package com.motorola.ccc.ota.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import com.motorola.ccc.ota.R;
import com.motorola.ccc.ota.sources.bota.settings.BotaSettings;
import com.motorola.ccc.ota.sources.bota.settings.Configs;
import com.motorola.ccc.ota.utils.Logger;
import com.motorola.otalib.common.utils.UpgradeUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class CloudPickerActivity extends Activity {
    public static final String TAG = "TAG";
    private static final String[] cloud_groups = {"QA", "Production", "Staging", "Dev", "China", "ChinaStaging"};
    private static final String[][] cloud_names = {new String[]{UpgradeUtils.QA_SERVER}, new String[]{"moto-cds.appspot.com"}, new String[]{UpgradeUtils.STAGING_SERVER}, new String[]{UpgradeUtils.DEVELOPMENT_SERVER}, new String[]{UpgradeUtils.CHINA_PRODUCTION_SERVER}, new String[]{UpgradeUtils.CHINA_STAGING_SERVER}};
    private static String[] groups;
    private static String[][] masters;
    private ExpandableListAdapter mAdapter;
    private List<Map<String, String>> topLevelData = new ArrayList();
    private List<List<Map<String, String>>> childData = new ArrayList();

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        Logger.debug("OtaApp", "CloudPickerActivity.onCreate()");
        super.onCreate(bundle);
        if (Boolean.valueOf(getResources().getString(R.string.is_cloudpicker_for_small_screen)).booleanValue()) {
            requestWindowFeature(1);
            setContentView(R.layout.cloudpicker);
            findViewById(R.id.CurrentCloud).setPadding(0, 0, 0, 0);
        } else {
            setContentView(R.layout.cloudpicker);
        }
        groups = cloud_groups;
        masters = cloud_names;
        for (int i = 0; i < groups.length; i++) {
            HashMap hashMap = new HashMap();
            this.topLevelData.add(hashMap);
            hashMap.put(TAG, groups[i]);
            ArrayList arrayList = new ArrayList();
            for (int i2 = 0; i2 < masters[i].length; i2++) {
                HashMap hashMap2 = new HashMap();
                arrayList.add(hashMap2);
                hashMap2.put(TAG, masters[i][i2]);
            }
            this.childData.add(arrayList);
        }
        this.mAdapter = new SimpleExpandableListAdapter(this, this.topLevelData, 17367046, new String[]{TAG}, new int[]{16908308}, this.childData, 17367047, new String[]{TAG}, new int[]{16908308});
        ExpandableListView expandableListView = (ExpandableListView) findViewById(16908298);
        expandableListView.setAdapter(this.mAdapter);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() { // from class: com.motorola.ccc.ota.ui.CloudPickerActivity.1
            @Override // android.widget.ExpandableListView.OnChildClickListener
            public boolean onChildClick(ExpandableListView expandableListView2, View view, int i3, int i4, long j) {
                Logger.debug("OtaApp", "changing master cloud to: " + CloudPickerActivity.masters[i3][i4]);
                new BotaSettings().setString(Configs.MASTER_CLOUD, CloudPickerActivity.masters[i3][i4]);
                CloudPickerActivity.this.finish();
                return true;
            }
        });
    }

    @Override // android.app.Activity
    public void onStart() {
        super.onStart();
        String string = new BotaSettings().getString(Configs.MASTER_CLOUD);
        ((TextView) findViewById(R.id.CurrentCloud)).setText("Current master cloud: " + string);
        ExpandableListView expandableListView = (ExpandableListView) findViewById(16908298);
        for (int i = 0; i < masters.length; i++) {
            int i2 = 0;
            while (true) {
                String[] strArr = masters[i];
                if (i2 >= strArr.length) {
                    break;
                } else if (string.equals(strArr[i2])) {
                    expandableListView.setSelectedChild(i, i2, true);
                    break;
                } else {
                    i2++;
                }
            }
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }
}
