package com.rafhaanshah.studyassistant.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.schedule.ScheduleEvent;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<ScheduleEvent> scheduleEvents;
    private Context context;
    private int appWidgetId, itemCount;
    //TODO: Update widget preview image

    WidgetFactory(Context getContext, Intent intent) {
        Log.v("Widget", "Factory Create");
        context = getContext;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        itemCount = 5;
    }

    @Override
    public void onCreate() {
        Log.v("Widget", "Factory onCreate");

    }

    @Override
    public void onDataSetChanged() {
        Log.v("Widget", "Factory Data Changed");
        scheduleEvents = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ScheduleEvent> results = realm.where(ScheduleEvent.class)
                .equalTo(ScheduleEvent.ScheduleEvent_COMPLETED, false)
                .findAllSorted(ScheduleEvent.ScheduleEvent_TIME, Sort.ASCENDING);
        for (int i = 0; i < itemCount; i++) {
            if (i < results.size()) {
                ScheduleEvent scheduleEvent = results.get(i);
                if (scheduleEvent != null) {
                    scheduleEvents.add(realm.copyFromRealm(scheduleEvent));
                }
            }
        }
        realm.close();
    }


    @Override
    public void onDestroy() {
        Log.v("Widget", "Factory On Destroy");
    }

    @Override
    public int getCount() {
        return scheduleEvents.size();
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
        remoteView.setTextViewText(R.id.tv_widget, scheduleEvents.get(position).getTitle());
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