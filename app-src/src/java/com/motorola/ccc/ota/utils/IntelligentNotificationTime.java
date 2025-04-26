package com.motorola.ccc.ota.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import com.motorola.ccc.ota.env.OtaApplication;
import com.motorola.ccc.ota.ui.UpdaterUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
public class IntelligentNotificationTime {
    private final long TIME_SEVEN_HOURS = 1262309400000L;
    private final long TIME_SEVENTEEN_HOURS = 1262345400000L;
    private Context context = OtaApplication.getGlobalContext();
    private Calendar cal = Calendar.getInstance();
    private Slot s1 = new Slot(3, 9, 0, 0);
    private Slot s2 = new Slot(9, 15, 0, 0);
    private Slot s3 = new Slot(15, 21, 0, 0);
    private Slot s4 = new Slot(21, 3, 0, 0);
    private List<Slot> slots = new ArrayList();

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: /storage/emulated/0/Documents/jadec/sources/com.motorola.ccc.ota/dex-files/1.dex */
    public class Slot {
        int count;
        long end;
        long last;
        long start;

        Slot(long j, long j2, int i, long j3) {
            this.start = j;
            this.end = j2;
            this.count = i;
            this.last = j3;
        }
    }

    public void compute() {
        for (String str : getOldInstallationTimes()) {
            long parseLong = Long.parseLong(str);
            long hour = getHour(parseLong);
            if (hour >= this.s1.start && hour < this.s1.end) {
                this.s1.count++;
                Slot slot = this.s1;
                slot.last = Math.max(slot.last, parseLong);
            } else if (hour >= this.s2.start && hour < this.s2.end) {
                this.s2.count++;
                Slot slot2 = this.s2;
                slot2.last = Math.max(slot2.last, parseLong);
            } else if (hour >= this.s3.start && hour < this.s3.end) {
                this.s3.count++;
                Slot slot3 = this.s3;
                slot3.last = Math.max(slot3.last, parseLong);
            } else {
                this.s4.count++;
                Slot slot4 = this.s4;
                slot4.last = Math.max(slot4.last, parseLong);
            }
        }
        this.slots.add(this.s1);
        this.slots.add(this.s2);
        this.slots.add(this.s3);
        this.slots.add(this.s4);
        Collections.sort(this.slots, new Comparator<Slot>() { // from class: com.motorola.ccc.ota.utils.IntelligentNotificationTime.1
            @Override // java.util.Comparator
            public int compare(Slot slot5, Slot slot6) {
                if (slot5.count < slot6.count) {
                    return 1;
                }
                return (slot5.count <= slot6.count && slot5.last < slot6.last) ? 1 : -1;
            }
        });
        printList();
        storeBestTimes();
    }

    private List<String> getOldInstallationTimes() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(UpdaterUtils.KEY_INTELLIGENT_NOTIFICATION, 0);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        String string = sharedPreferences.getString(UpdaterUtils.TIME_STRING, "");
        if (string.isEmpty()) {
            string = (string + "1262309400000;") + "1262345400000;";
            edit.putString(UpdaterUtils.TIME_STRING, string);
            edit.apply();
        }
        return Arrays.asList(string.split(";"));
    }

    private void storeBestTimes() {
        ArrayList arrayList = new ArrayList();
        Iterator<Slot> it = this.slots.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Slot next = it.next();
            if (arrayList.isEmpty()) {
                arrayList.add(Long.valueOf(next.last));
            } else {
                arrayList.add(Long.valueOf(next.last));
                break;
            }
        }
        if (arrayList.size() == 1) {
            long hour = getHour(((Long) arrayList.get(0)).longValue());
            if (hour >= this.s3.start && hour < this.s3.end) {
                arrayList.add(1262309400000L);
            } else {
                arrayList.add(1262345400000L);
            }
        }
        this.cal.setTimeInMillis(((Long) arrayList.get(0)).longValue());
        int i = (this.cal.get(11) * 60) + this.cal.get(12);
        this.cal.setTimeInMillis(((Long) arrayList.get(1)).longValue());
        int i2 = (this.cal.get(11) * 60) + this.cal.get(12);
        if (Math.abs(i - i2) <= 60) {
            if (i <= i2) {
                arrayList.set(0, Long.valueOf(((Long) arrayList.get(0)).longValue() - 1800000));
                arrayList.set(1, Long.valueOf(((Long) arrayList.get(1)).longValue() + 1800000));
            } else {
                arrayList.set(0, Long.valueOf(((Long) arrayList.get(0)).longValue() + 1800000));
                arrayList.set(1, Long.valueOf(((Long) arrayList.get(1)).longValue() - 1800000));
            }
        }
        SharedPreferences.Editor edit = this.context.getSharedPreferences(UpdaterUtils.KEY_INTELLIGENT_NOTIFICATION, 0).edit();
        this.cal.setTimeInMillis(((Long) arrayList.get(0)).longValue());
        edit.putInt(UpdaterUtils.BEST_TIME_1_HOUR, this.cal.get(11));
        edit.putInt(UpdaterUtils.BEST_TIME_1_MINUTE, this.cal.get(12));
        this.cal.setTimeInMillis(((Long) arrayList.get(1)).longValue());
        edit.putInt(UpdaterUtils.BEST_TIME_2_HOUR, this.cal.get(11));
        edit.putInt(UpdaterUtils.BEST_TIME_2_MINUTE, this.cal.get(12));
        edit.apply();
    }

    private int getHour(long j) {
        this.cal.setTimeInMillis(j);
        return this.cal.get(11);
    }

    private void printList() {
        for (Slot slot : this.slots) {
            Logger.debug("OtaApp", slot.start + SystemUpdateStatusUtils.SPACE + slot.end + SystemUpdateStatusUtils.SPACE + slot.count + "\t\t" + DateFormatUtils.getDateTime(slot.last));
        }
    }
}
