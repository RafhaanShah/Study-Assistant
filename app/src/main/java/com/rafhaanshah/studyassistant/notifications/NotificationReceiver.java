package com.rafhaanshah.studyassistant.notifications;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.rafhaanshah.studyassistant.notifications.Notifier.ACTION_NOTIFICATION;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Notify", "Receive");
        if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
            //TODO: Boot completed, set notifications again

        } else if (intent.getAction().equals(ACTION_NOTIFICATION)) {
            // Show the notification for the event
            Notifier.showNotification(context, intent);
        }
    }
}
