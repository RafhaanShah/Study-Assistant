package com.rafhaanshah.studyassistant.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.rafhaanshah.studyassistant.R;

import java.util.ArrayList;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<String> listItemList = new ArrayList<>();
    private Context context = null;
    private int appWidgetId;

    WidgetFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        getData();
    }

    private void getData() {
        for (int i = 0; i < 50; i++) {
            listItemList.add("This is item " + i);
        }
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.item_widget);
        remoteView.setTextViewText(R.id.tv_widget, listItemList.get(position));
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}