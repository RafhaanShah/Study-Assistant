package com.rafhaanshah.studyassistant.notifications;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.rafhaanshah.studyassistant.notifications.Notifier.ACTION_MARK_EVENT;
import static com.rafhaanshah.studyassistant.notifications.Notifier.ACTION_NOTIFICATION;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Checks the action of the intent then calls the corresponding method
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            //Boot completed, set notifications again
            Notifier.setReminders(context);
        } else if (intent.getAction().equals(ACTION_NOTIFICATION)) {
            // Show the notification for the event
            Notifier.showNotification(context, intent);
        } else if (intent.getAction().equals(ACTION_MARK_EVENT)) {
            // Mark the event as complete
            Notifier.markEvent(context, intent);
        }
    }
}
