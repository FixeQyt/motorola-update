package com.motorola.ccc.ota.ui;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.motorola.ccc.ota.R;
import java.util.HashMap;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class ReleaseNotesAdapter extends BaseExpandableListAdapter {
    private Context context;
    private HashMap<String, List<String>> expandableListDetail;
    private List<String> expandableListTitle;

    @Override // android.widget.ExpandableListAdapter
    public long getChildId(int i, int i2) {
        return i2;
    }

    @Override // android.widget.ExpandableListAdapter
    public long getGroupId(int i) {
        return i;
    }

    @Override // android.widget.ExpandableListAdapter
    public boolean hasStableIds() {
        return false;
    }

    @Override // android.widget.ExpandableListAdapter
    public boolean isChildSelectable(int i, int i2) {
        return true;
    }

    public ReleaseNotesAdapter(Context context, List<String> list, HashMap<String, List<String>> hashMap) {
        this.context = context;
        this.expandableListTitle = list;
        this.expandableListDetail = hashMap;
    }

    @Override // android.widget.ExpandableListAdapter
    public Object getChild(int i, int i2) {
        return this.expandableListDetail.get(this.expandableListTitle.get(i)).get(i2);
    }

    @Override // android.widget.ExpandableListAdapter
    public View getChildView(int i, int i2, boolean z, View view, ViewGroup viewGroup) {
        String str = (String) getChild(i, i2);
        if (view == null) {
            view = ((LayoutInflater) this.context.getSystemService("layout_inflater")).inflate(R.layout.release_notes_item, (ViewGroup) null);
        }
        TextView textView = (TextView) view.findViewById(R.id.expandedListItem);
        if (str.indexOf("a href=") != -1) {
            textView.setText(Html.fromHtml(str, 0, null, new HtmlUtils(str)));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            textView.setText(Html.fromHtml(str, 0, null, new HtmlUtils(str)));
        }
        return view;
    }

    @Override // android.widget.ExpandableListAdapter
    public int getChildrenCount(int i) {
        return this.expandableListDetail.get(this.expandableListTitle.get(i)).size();
    }

    @Override // android.widget.ExpandableListAdapter
    public Object getGroup(int i) {
        return this.expandableListTitle.get(i);
    }

    @Override // android.widget.ExpandableListAdapter
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override // android.widget.ExpandableListAdapter
    public View getGroupView(int i, boolean z, View view, ViewGroup viewGroup) {
        String str = (String) getGroup(i);
        if (view == null) {
            view = ((LayoutInflater) this.context.getSystemService("layout_inflater")).inflate(R.layout.release_notes_group, (ViewGroup) null);
        }
        TextView textView = (TextView) view.findViewById(R.id.listTitle);
        textView.setTypeface(null, 1);
        textView.setText(str);
        return view;
    }
}
