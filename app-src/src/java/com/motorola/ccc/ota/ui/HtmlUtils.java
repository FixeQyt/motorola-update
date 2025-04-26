package com.motorola.ccc.ota.ui;

import android.text.Editable;
import android.text.Html;
import com.motorola.ccc.ota.utils.SystemUpdateStatusUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.XMLReader;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/0.dex */
public class HtmlUtils implements Html.TagHandler {
    private static final int END_TAG_LENGTH = 5;
    private static final String END_TAG_OL = "</ol>";
    private static final String END_TAG_UL = "</ul>";
    private static final String HTML_BULLET = "•  ";
    private static final String LI = "li";
    private static final String OL = "ol";
    private static final String START_TAG_OL = "<ol>";
    private static final String START_TAG_UL = "<ul>";
    private static final String UL = "ul";
    private String mUpgrageNotes;
    private String mParent = null;
    private int mIndex = 0;
    private int mOlcount = 0;
    private int mUlcount = 0;
    private int mOloccurence = getOccurence(END_TAG_OL);
    private int mUloccurence = getOccurence(END_TAG_UL);

    /* JADX INFO: Access modifiers changed from: package-private */
    public HtmlUtils(String str) {
        this.mUpgrageNotes = str.trim();
    }

    @Override // android.text.Html.TagHandler
    public void handleTag(boolean z, String str, Editable editable, XMLReader xMLReader) {
        if (str.equals(UL)) {
            handleUnOrderedList(z, editable);
        } else if (str.equals(OL)) {
            handleOrederdList(z, editable);
        } else if (str.equals(LI)) {
            handleList(z, editable);
        }
    }

    private void handleOrederdList(boolean z, Editable editable) {
        if (z) {
            this.mParent = OL;
            this.mIndex = 1;
            return;
        }
        this.mIndex = 0;
        int i = this.mOlcount + 1;
        this.mOlcount = i;
        mangaeSpace(END_TAG_OL, i, this.mOloccurence, editable);
    }

    private void handleUnOrderedList(boolean z, Editable editable) {
        if (z) {
            this.mParent = UL;
            return;
        }
        int i = this.mUlcount + 1;
        this.mUlcount = i;
        mangaeSpace(END_TAG_UL, i, this.mUloccurence, editable);
    }

    private void mangaeSpace(String str, int i, int i2, Editable editable) {
        if (!this.mUpgrageNotes.endsWith(str)) {
            if (nextTag(str, i)) {
                editable.append(SystemUpdateStatusUtils.NEWLINE_SEPERATOR);
            } else {
                editable.append("\n\n");
            }
        } else if (i < i2) {
            if (nextTag(str, i)) {
                editable.append(SystemUpdateStatusUtils.NEWLINE_SEPERATOR);
            } else {
                editable.append("\n\n");
            }
        }
    }

    private void handleList(boolean z, Editable editable) {
        if (z && this.mParent.equals(UL)) {
            editable.append("\n\t•  ");
        }
        if (z && this.mParent.equals(OL) && this.mIndex > 0) {
            editable.append((CharSequence) ("\n\t" + this.mIndex + ". "));
            this.mIndex++;
        }
    }

    private int getOccurence(String str) {
        Matcher matcher = Pattern.compile(str).matcher(this.mUpgrageNotes);
        int i = 0;
        while (matcher.find()) {
            i++;
        }
        return i;
    }

    private boolean nextTag(String str, int i) {
        int i2 = 0;
        int i3 = 0;
        while (i2 != i) {
            i2++;
            i3 = this.mUpgrageNotes.indexOf(str, i3) + 5;
        }
        return this.mUpgrageNotes.substring(i3).trim().startsWith(START_TAG_UL) || this.mUpgrageNotes.substring(i3).trim().startsWith(START_TAG_OL);
    }
}
