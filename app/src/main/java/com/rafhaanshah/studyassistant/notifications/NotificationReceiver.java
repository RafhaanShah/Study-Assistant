package com.rafhaanshah.studyassistant.notifications;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

import com.rafhaanshah.studyassistant.schedule.ScheduleEvent;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.rafhaanshah.studyassistant.notifications.Notifier.ACTION_MARK_EVENT;
import static com.rafhaanshah.studyassistant.notifications.Notifier.ACTION_NOTIFICATION;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            //Boot completed, set notifications again
            setReminders(context);
        } else if (intent.getAction().equals(ACTION_NOTIFICATION)) {
            // Show the notification for the event
            Notifier.showNotification(context, intent);
        } else if (intent.getAction().equals(ACTION_MARK_EVENT)) {
            // Mark the event as complete
            Notifier.markEvent(context, intent);
        }
    }

    private void setReminders(Context context) {
        final Realm realm = Realm.getDefaultInstance();
        // Find all events that are
        RealmResults<ScheduleEvent> scheduleEvents = realm.where(ScheduleEvent.class)
                // Not completed
                .equalTo(ScheduleEvent.ScheduleEvent_COMPLETED, false)
                // Reminder is enabled
                .equalTo(ScheduleEvent.ScheduleEvent_REMINDER, true)
                // Not in the past
                .greaterThan(ScheduleEvent.ScheduleEvent_TIME, System.currentTimeMillis())
                // Reminder time not in the past
                .greaterThan(ScheduleEvent.ScheduleEvent_REMINDER_TIME, System.currentTimeMillis())
                .findAll();

        for (ScheduleEvent scheduleEvent : scheduleEvents) {
            // Schedule a notification for each
            String timeString = DateUtils.getRelativeTimeSpanString(scheduleEvent.getEventTime(), scheduleEvent.getReminderTime(), DateUtils.MINUTE_IN_MILLIS).toString();
            Notifier.scheduleNotification(context, scheduleEvent.getID(), scheduleEvent.getTitle(), timeString, scheduleEvent.getReminderTime());
        }
        realm.close();
    }
}
