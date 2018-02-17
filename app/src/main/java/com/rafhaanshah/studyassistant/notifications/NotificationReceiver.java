package com.rafhaanshah.studyassistant.notifications;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.rafhaanshah.studyassistant.schedule.ScheduleItemActivity;

import static com.rafhaanshah.studyassistant.notifications.Notifier.ACTION_NOTIFICATION;
import static com.rafhaanshah.studyassistant.notifications.Notifier.EXTRA_NOTIFICATION_EVENT;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Notif", "Receive");
        Log.v("Notif", intent.getAction());
        if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
            // Boot completed, set notifications again

        } else if (intent.getAction().equals(ACTION_NOTIFICATION)) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = intent.getParcelableExtra(EXTRA_NOTIFICATION_EVENT);
            int id = intent.getIntExtra(ScheduleItemActivity.EXTRA_ITEM_ID, 0);
            notificationManager.notify(id, notification);

        }
    }
}
