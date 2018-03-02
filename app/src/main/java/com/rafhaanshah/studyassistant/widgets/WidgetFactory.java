package com.rafhaanshah.studyassistant.widgets;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.schedule.ScheduleEvent;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final int itemCount = 25;
    private ArrayList<ScheduleEvent> scheduleEvents;
    private Context context;
    private int appWidgetId;
    //TODO: Update widget preview image

    WidgetFactory(Context getContext, Intent intent) {
        context = getContext;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
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
        ScheduleEvent event = scheduleEvents.get(position);

        final long currentTime = System.currentTimeMillis();
        final long eventTime = event.getEventTime();
        int colour = ContextCompat.getColor(context, R.color.materialGreen);
        final String showTime = DateUtils.getRelativeTimeSpanString(eventTime, currentTime, DateUtils.MINUTE_IN_MILLIS).toString();

        if (eventTime < currentTime) {
            colour = ContextCompat.getColor(context, R.color.materialRed);
        } else if (eventTime < (currentTime + (86400000 * 3))) {
            colour = ContextCompat.getColor(context, R.color.materialOrange);
        }

        setIcon(remoteView, event.getType());
        remoteView.setTextViewText(R.id.tv_widget_title, scheduleEvents.get(position).getTitle());
        remoteView.setTextViewText(R.id.tv_widget_date, showTime);
        remoteView.setInt(R.id.widget_rectangle, "setBackgroundColor", colour);
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

    @SuppressLint("ResourceType")
    private void setIcon(RemoteViews remoteView, ScheduleEvent.ScheduleEventType type) {
        final TypedArray icons = context.getResources().obtainTypedArray(R.array.event_type_icons);
        switch (type) {
            case HOMEWORK:
                remoteView.setImageViewResource(R.id.widget_image_type, icons.getResourceId(0, 0));
                break;
            case TEST:
                remoteView.setImageViewResource(R.id.widget_image_type, icons.getResourceId(1, 0));
                break;
            case COURSEWORK:
                remoteView.setImageViewResource(R.id.widget_image_type, icons.getResourceId(2, 0));
                break;
            case EXAM:
                remoteView.setImageViewResource(R.id.widget_image_type, icons.getResourceId(3, 0));
                break;
        }
        icons.recycle();
    }
}