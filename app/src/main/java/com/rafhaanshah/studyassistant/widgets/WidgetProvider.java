package com.rafhaanshah.studyassistant.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.rafhaanshah.studyassistant.R;

public class WidgetProvider extends AppWidgetProvider {

    public static void updateWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_view_widget);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v("Widget", "Provider onUpdate");
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = updateWidgetListView(context, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            //TODO: Test if widget is updated
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list_view_widget);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Widget", "Provider onReceive");
        super.onReceive(context, intent);
    }

    private RemoteViews updateWidgetListView(Context context, int appWidgetId) {
        //Which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        //RemoteViews Service needed to provide adapter for ListView
        Intent intent = new Intent(context, WidgetService.class);

        //Pass Widget ID to the RemoteViews Service
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        //Set a unique Uri to the intent
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        //Set an adapter to ListView of the widget
        remoteViews.setRemoteAdapter(R.id.list_view_widget, intent);

        //Set an empty view in case of no data objects
        remoteViews.setEmptyView(R.id.list_view_widget, R.id.tv_widget_empty);
        return remoteViews;
    }

}