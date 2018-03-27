package com.rafhaanshah.studyassistant.widgets;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.schedule.ScheduleEventActivity;

public class WidgetProvider extends AppWidgetProvider {

    public static void updateWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_view_widget);
    }

    private static PendingIntent createClickIntent(Context context, int eventID) {
        Intent clickIntent = new Intent(context, ScheduleEventActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(clickIntent);
        return stackBuilder.getPendingIntent(eventID, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = updateWidgetListView(context, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view_widget);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    // Examples used from https://laaptu.wordpress.com/2013/07/19/android-app-widget-with-listview/
    private RemoteViews updateWidgetListView(Context context, int appWidgetId) {
        // Which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        // RemoteViews Service needed to provide adapter for ListView
        Intent intent = new Intent(context, WidgetService.class);

        // Pass Widget ID to the RemoteViews Service
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        // Set a unique Uri to the intent
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        // Set an adapter to ListView of the widget
        remoteViews.setRemoteAdapter(R.id.list_view_widget, intent);

        // Set an empty view in case of no data objects
        remoteViews.setEmptyView(R.id.list_view_widget, R.id.tv_widget_empty);

        // Set intent template for list items
        remoteViews.setPendingIntentTemplate(R.id.list_view_widget, createClickIntent(context, -2));

        // Set intent for the new button
        remoteViews.setOnClickPendingIntent(R.id.btn_widget_new, createClickIntent(context, -1));

        return remoteViews;
    }
}