package com.rafhaanshah.studyassistant.notifications;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;

import com.rafhaanshah.studyassistant.schedule.ScheduleEvent;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.rafhaanshah.studyassistant.notifications.Notifier.ACTION_NOTIFICATION;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Notify", "Receive");
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.v("Notify", "Boot Complete");
            //Boot completed, set notifications again
            final Realm realm = Realm.getDefaultInstance();
            // Find all events where
            RealmResults<ScheduleEvent> scheduleEvents = realm.where(ScheduleEvent.class)
                    // Not completed
                    .equalTo(ScheduleEvent.ScheduleEvent_COMPLETED, false)
                    // Reminder is enabled
                    .equalTo(ScheduleEvent.ScheduleEvent_REMINDER, true)
                    // Not in the past
                    .greaterThan(ScheduleEvent.ScheduleEvent_TIME, System.currentTimeMillis())
                    // Reminder time is not in the past
                    .greaterThan(ScheduleEvent.ScheduleEvent_REMINDER_TIME, System.currentTimeMillis())
                    .findAll();

            for (ScheduleEvent scheduleEvent : scheduleEvents) {
                String timeString = DateUtils.getRelativeTimeSpanString(scheduleEvent.getTime(), scheduleEvent.getReminderTime(), DateUtils.MINUTE_IN_MILLIS).toString();
                Notifier.scheduleNotification(context, scheduleEvent.getID(), scheduleEvent.getTitle(), timeString, scheduleEvent.getReminderTime());
            }

            realm.close();

        } else if (intent.getAction().equals(ACTION_NOTIFICATION)) {
            // Show the notification for the event
            Notifier.showNotification(context, intent);
        }
    }
}
