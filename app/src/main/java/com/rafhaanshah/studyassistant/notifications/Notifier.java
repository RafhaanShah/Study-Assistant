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
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.schedule.ScheduleItemActivity;

public class Notifier {

    static final String ACTION_NOTIFICATION = "com.rafhaanshah.studyassistant.action.EVENT_NOTIFICATION";
    private static final String EXTRA_NOTIFICATION_EVENT = "EXTRA_NOTIFICATION_EVENT";
    private static final String NOTIFICATION_CHANNEL_EVENT = "NOTIFICATION_CHANNEL_EVENT";

    private Notifier() {
    }

    public static void scheduleNotification(Context context, int itemID, String itemTitle, String timeString, long notificationTime) {
        Log.v("Notify", "Set " + String.valueOf(itemID));
        setAlarm(context, getAlarmIntent(context, itemID, itemTitle, timeString), notificationTime);
    }

    public static void cancelScheduledNotification(Context context, int itemID, String itemTitle, String timeString) {
        Log.v("Notify", "Cancel " + String.valueOf(itemID));
        cancelAlarm(context, getAlarmIntent(context, itemID, itemTitle, timeString));
    }

    private static void setAlarm(Context context, PendingIntent pendingIntent, long alarmTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            // TODO: Change to actual notification time
            //alarmManager.set(AlarmManager.RTC, alarmTime, pendingIntent);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 30000, pendingIntent);
    }

    private static void cancelAlarm(Context context, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            alarmManager.cancel(pendingIntent);
    }

    private static PendingIntent getAlarmIntent(Context context, int itemID, String itemTitle, String timeString) {
        // Get intent for the timed alarm
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_NOTIFICATION);
        intent.putExtra(ScheduleItemActivity.EXTRA_ITEM_ID, itemID);

        // Get intent for Schedule Item Activity
        Intent resultIntent = ScheduleItemActivity.getStartIntent(context, itemID);

        // Add correct activity stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ScheduleItemActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        // Create Pending Intent for clicking the notification
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(itemID, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent resultPendingIntent = PendingIntent.getActivity(context, REQUEST_EVENT_RESULT, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Add the notification to the intent for the timed alarm
        intent.putExtra(EXTRA_NOTIFICATION_EVENT, buildNotification(context, resultPendingIntent, itemTitle, timeString));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, itemID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Log.v("Notify", timeString);
        Log.v("Notify Intent", pendingIntent.toString());

        return pendingIntent;
    }

    private static Notification buildNotification(Context context, PendingIntent intent, String title, String text) {
        //TODO: Add button to notification
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(intent)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_check_white_24dp);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_CHANNEL_EVENT);
        } else {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            builder.setVibrate(new long[]{100, 200, 100});
        }

        return builder.build();
    }

    static void showNotification(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(EXTRA_NOTIFICATION_EVENT);
        int id = intent.getIntExtra(ScheduleItemActivity.EXTRA_ITEM_ID, 0);
        Log.v("Notify", "Show " + String.valueOf(id));
        if (notificationManager != null)
            notificationManager.notify(id, notification);
    }

    public static void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_EVENT, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(context.getString(R.string.channel_description));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.YELLOW);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100});
            notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build());

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancelAll();
    }

    //TODO: Disable enabled in Manifest and use this
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
