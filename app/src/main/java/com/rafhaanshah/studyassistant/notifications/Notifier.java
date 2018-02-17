package com.rafhaanshah.studyassistant.notifications;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.schedule.ScheduleItem;
import com.rafhaanshah.studyassistant.schedule.ScheduleItemActivity;

public class Notifier {

    static final String ACTION_NOTIFICATION = "com.rafhaanshah.studyassistant.action.EVENT_NOTIFICATION";
    static final String EXTRA_NOTIFICATION_EVENT = "NOTIFICATION_EVENT";
    private static final int REQUEST_EVENT_NOTIFICATION = 101;
    private static final int REQUEST_EVENT_RESULT = 102;
    private static final String NOTIFICATION_CHANNEL_EVENT = "NOTIFICATION_CHANNEL_EVENT";

    private Notifier() {
    }

    public static void setNotification(Context context, int itemID, String itemTitle, String itemTime, long notificationTime) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(ACTION_NOTIFICATION);
        intent.putExtra(ScheduleItem.ScheduleItem_ID, itemID);

        Intent resultIntent = ScheduleItemActivity.getStartIntent(context, itemID);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, REQUEST_EVENT_RESULT, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        intent.putExtra(EXTRA_NOTIFICATION_EVENT, buildNotification(context, resultPendingIntent, itemTitle, itemTime));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_EVENT_NOTIFICATION, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);

    }

    private static Notification buildNotification(Context context, PendingIntent intent, String title, String text) {
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(intent)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_check_white_24dp);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_CHANNEL_EVENT);
        }

        return builder.build();
    }

    public static void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_EVENT, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(context.getString(R.string.channel_description));
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.YELLOW);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100});

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
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
