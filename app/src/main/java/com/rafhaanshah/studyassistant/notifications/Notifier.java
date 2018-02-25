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

import com.rafhaanshah.studyassistant.MainActivity;
import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.schedule.ScheduleEvent;
import com.rafhaanshah.studyassistant.schedule.ScheduleEventActivity;

import io.realm.Realm;

public class Notifier {

    public static final String ACTION_ACTIVE_NOTIFICATION = "com.rafhaanshah.studyassistant.action.EVENT_ACTIVE_NOTIFICATION";
    public static final String EXTRA_NOTIFICATION_TITLE = "EXTRA_NOTIFICATION_TITLE";
    public static final String EXTRA_NOTIFICATION_TEXT = "EXTRA_NOTIFICATION_TEXT";
    public static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";
    static final String ACTION_NOTIFICATION = "com.rafhaanshah.studyassistant.action.EVENT_NOTIFICATION";
    static final String ACTION_MARK_EVENT = "com.rafhaanshah.studyassistant.action.MARK_EVENT";
    private static final String EXTRA_NOTIFICATION_EVENT = "EXTRA_NOTIFICATION_EVENT";
    private static final String NOTIFICATION_CHANNEL_EVENT = "com.rafhaanshah.studyassistant.notifications.CHANNEL_EVENT";
    private static final String NOTIFICATION_GROUP_EVENT = "com.rafhaanshah.studyassistant.notifications.GROUP_EVENT";

    private Notifier() {
    }

    public static void scheduleNotification(Context context, int eventID, String eventTitle, String timeString, long notificationTime) {
        setAlarm(context, getAlarmIntent(context, eventID, eventTitle, timeString), notificationTime);
    }

    public static void cancelScheduledNotification(Context context, int eventID) {
        cancelAlarm(context, eventID);
    }

    private static void setAlarm(Context context, PendingIntent pendingIntent, long alarmTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            alarmManager.set(AlarmManager.RTC, alarmTime, pendingIntent);
        //alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 10000, pendingIntent);
    }

    private static void cancelAlarm(Context context, int eventID) {
        // Get intent for the timed alarm
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_NOTIFICATION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, eventID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            alarmManager.cancel(pendingIntent);
    }

    private static PendingIntent getAlarmIntent(Context context, int eventID, String eventTitle, String timeString) {
        // Get intent for the timed alarm
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_NOTIFICATION);
        intent.putExtra(ScheduleEventActivity.EXTRA_ITEM_ID, eventID);

        // Get intent for Schedule Event Activity
        Intent clickIntent = ScheduleEventActivity.getStartIntent(context, eventID);

        // Add correct activity stack and create pending intent for clicking the notification
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ScheduleEventActivity.class);
        stackBuilder.addNextIntent(clickIntent);
        PendingIntent clickPendingIntent = stackBuilder.getPendingIntent(eventID, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create intent for the notification button and pending intent
        Intent buttonIntent = new Intent(context, NotificationReceiver.class);
        buttonIntent.setAction(ACTION_MARK_EVENT);
        buttonIntent.putExtra(ScheduleEventActivity.EXTRA_ITEM_ID, eventID);
        PendingIntent buttonPendingIntent = PendingIntent.getBroadcast(context, eventID, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Add the notification to the intent for the timed alarm
        intent.putExtra(EXTRA_NOTIFICATION_EVENT, buildNotification(context, clickPendingIntent, buttonPendingIntent, eventTitle, timeString));
        return PendingIntent.getBroadcast(context, eventID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static Notification buildNotification(Context context, PendingIntent clickIntent, PendingIntent buttonIntent, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_EVENT)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(clickIntent)
                .setGroup(NOTIFICATION_GROUP_EVENT)
                .setShowWhen(true)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_notification_lamp)
                .addAction(R.drawable.ic_check_white_24dp, context.getString(R.string.mark_completed), buttonIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{100, 200, 100});

        return builder.build();
    }

    static void showNotification(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(EXTRA_NOTIFICATION_EVENT);
        final int ID = intent.getIntExtra(ScheduleEventActivity.EXTRA_ITEM_ID, -1);
        setEventReminderOff(ID);

        if (notificationManager != null && !MainActivity.isActive()) {
            notificationManager.notify(ID, notification);
        } else {
            Intent activeIntent = new Intent(ACTION_ACTIVE_NOTIFICATION);
            activeIntent.putExtra(EXTRA_NOTIFICATION_TITLE, notification.extras.getString(Notification.EXTRA_TITLE));
            activeIntent.putExtra(EXTRA_NOTIFICATION_TEXT, notification.extras.getString(Notification.EXTRA_TEXT));
            activeIntent.putExtra(EXTRA_NOTIFICATION_ID, ID);
            context.sendBroadcast(activeIntent);
        }
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
        final int ID = intent.getIntExtra(ScheduleEventActivity.EXTRA_ITEM_ID, -1);
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
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(ID);

    }

    public static void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_EVENT, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(context.getString(R.string.channel_description));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.YELLOW);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100});
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
