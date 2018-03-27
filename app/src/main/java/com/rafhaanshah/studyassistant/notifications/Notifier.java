package com.rafhaanshah.studyassistant.notifications;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;

import com.rafhaanshah.studyassistant.MainActivity;
import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.schedule.ScheduleEvent;
import com.rafhaanshah.studyassistant.schedule.ScheduleEventActivity;
import com.rafhaanshah.studyassistant.widgets.WidgetProvider;

import io.realm.Realm;
import io.realm.RealmResults;

// Examples used from https://developer.android.com/training/scheduling/alarms.html,
// https://developer.android.com/training/notify-user/build-notification.html
public class Notifier {

    public static final String ACTION_SNACKBAR_NOTIFICATION = "com.rafhaanshah.studyassistant.action.EVENT_SNACKBAR_NOTIFICATION";
    public static final String EXTRA_NOTIFICATION_TITLE = "EXTRA_NOTIFICATION_TITLE";
    public static final String EXTRA_NOTIFICATION_EVENT_TIME = "EXTRA_NOTIFICATION_EVENT_TIME";
    public static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";
    static final String ACTION_NOTIFICATION = "com.rafhaanshah.studyassistant.action.EVENT_NOTIFICATION";
    static final String ACTION_MARK_EVENT = "com.rafhaanshah.studyassistant.action.MARK_EVENT";
    private static final String NOTIFICATION_CHANNEL_EVENT = "com.rafhaanshah.studyassistant.notifications.CHANNEL_EVENT";
    private static final String NOTIFICATION_GROUP_EVENT = "com.rafhaanshah.studyassistant.notifications.GROUP_EVENT";

    private Notifier() {
    }

    public static void scheduleNotification(Context context, int eventID, String eventTitle, Long eventTime, long alarmTime) {
        // Schedule an Alarm to fire a Pending Intent at the given time
        // It will overwrite an existing Alarm with the same Pending Intent
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC, alarmTime, createAlarmIntent(context, eventID, eventTitle, eventTime));
            //alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 10000, createAlarmIntent(context, eventID, eventTitle, eventTime));
        }
    }

    public static void cancelScheduledNotification(Context context, int eventID) {
        // Get an existing Alarm Pending Intent and cancel it
        PendingIntent pendingIntent = getAlarmIntent(context, eventID);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static PendingIntent createAlarmIntent(Context context, int eventID, String eventTitle, Long eventTime) {
        // Create a Pending Intent for the Alarm, includes the ID, Notification Title and Text
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_NOTIFICATION);
        intent.putExtra(EXTRA_NOTIFICATION_ID, eventID);
        intent.putExtra(EXTRA_NOTIFICATION_TITLE, eventTitle);
        intent.putExtra(EXTRA_NOTIFICATION_EVENT_TIME, eventTime);
        return PendingIntent.getBroadcast(context, eventID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getAlarmIntent(Context context, int eventID) {
        // Get an existing Alarm Intent
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_NOTIFICATION);
        return PendingIntent.getBroadcast(context, eventID, intent, PendingIntent.FLAG_NO_CREATE);
    }

    static void showNotification(Context context, Intent intent) {
        final int ID = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        final String title = intent.getStringExtra(EXTRA_NOTIFICATION_TITLE);
        final Long eventTime = intent.getLongExtra(EXTRA_NOTIFICATION_EVENT_TIME, 0L);

        setEventReminderOff(ID);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // If the Main Activity is not active, show a notification
        if (notificationManager != null && !MainActivity.isActive()) {
            Notification notification = buildNotification(context,
                    createClickIntent(context, ID),
                    createButtonIntent(context, ID),
                    title, eventTime);
            notificationManager.notify(ID, notification);

            // Else show a snackbar
        } else {
            Intent snackBarIntent = new Intent(ACTION_SNACKBAR_NOTIFICATION);
            snackBarIntent.putExtra(EXTRA_NOTIFICATION_TITLE, title);
            snackBarIntent.putExtra(EXTRA_NOTIFICATION_EVENT_TIME, eventTime);
            snackBarIntent.putExtra(EXTRA_NOTIFICATION_ID, ID);
            LocalBroadcastManager.getInstance(context).sendBroadcast(snackBarIntent);
        }
    }

    private static Notification buildNotification(Context context, PendingIntent clickIntent, PendingIntent buttonIntent, String title, Long time) {
        // Build a notification with the given Title, Text and Intents
        final String timeString = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_EVENT)
                .setContentTitle(title)
                .setContentText(timeString)
                .setContentIntent(clickIntent)
                .setGroup(NOTIFICATION_GROUP_EVENT)
                .setShowWhen(true)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_notification_lamp)
                .setDefaults(Notification.DEFAULT_ALL)
                .addAction(R.drawable.ic_check_white_24dp, context.getString(R.string.mark_completed), buttonIntent);

        return builder.build();
    }

    private static PendingIntent createClickIntent(Context context, int eventID) {
        // Create a Pending Intent for clicking the notification
        Intent clickIntent = ScheduleEventActivity.getStartIntent(context, eventID);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(clickIntent);
        return stackBuilder.getPendingIntent(eventID, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent createButtonIntent(Context context, int eventID) {
        // Create a Pending Intent for clicking the Mark button on the notification
        Intent buttonIntent = new Intent(context, NotificationReceiver.class);
        buttonIntent.setAction(ACTION_MARK_EVENT);
        buttonIntent.putExtra(EXTRA_NOTIFICATION_ID, eventID);
        return PendingIntent.getBroadcast(context, eventID, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void setEventReminderOff(int eventID) {
        final Realm realm = Realm.getDefaultInstance();
        final ScheduleEvent scheduleEvent = realm.where(ScheduleEvent.class)
                .equalTo(ScheduleEvent.ScheduleEvent_ID, eventID).findFirst();
        if (scheduleEvent != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    scheduleEvent.setReminder(false);
                    scheduleEvent.setReminderTime(0L);
                }
            });
        }
        realm.close();
    }

    static void markEvent(Context context, Intent intent) {
        final int ID = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        final Realm realm = Realm.getDefaultInstance();
        final ScheduleEvent scheduleEvent = realm.where(ScheduleEvent.class)
                .equalTo(ScheduleEvent.ScheduleEvent_ID, ID).findFirst();
        if (scheduleEvent != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    scheduleEvent.setCompleted(true);
                }
            });
        }
        realm.close();
        WidgetProvider.updateWidgets(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(ID);
    }

    static void setReminders(Context context) {
        final Realm realm = Realm.getDefaultInstance();
        // Find all events that are
        RealmResults<ScheduleEvent> scheduleEvents = realm.where(ScheduleEvent.class)
                // not completed,
                .equalTo(ScheduleEvent.ScheduleEvent_COMPLETED, false)
                // have reminder enabled,
                .equalTo(ScheduleEvent.ScheduleEvent_REMINDER, true)
                .findAll();

        for (ScheduleEvent scheduleEvent : scheduleEvents) {
            // Schedule a notification for each event
            Notifier.scheduleNotification(context, scheduleEvent.getID(), scheduleEvent.getTitle(), scheduleEvent.getEventTime(), scheduleEvent.getReminderTime());
        }
        realm.close();
    }

    public static void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_EVENT, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(context.getString(R.string.channel_description));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.CYAN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{500, 500});
            notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build());

            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancelAll();
    }

    public static void enableBootReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, NotificationReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void disableBootReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, NotificationReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
