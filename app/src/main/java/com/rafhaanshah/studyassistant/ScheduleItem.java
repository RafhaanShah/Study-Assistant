package com.rafhaanshah.studyassistant;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

/**
 * Created by Raf on 05/11/2017.
 */

public class ScheduleItem extends AbstractItem<ScheduleItem, ScheduleItem.ViewHolder> {
    public int ID;
    public String title;

    ScheduleItem(int num, String text) {
        ID = num;
        title = text;
    }

    public String getTitle() {
        return title;
    }

    //The unique ID for this type of item
    @Override
    public int getType() {
        return ID;
    }

    //The layout to be used for this type of item
    @Override
    public int getLayoutRes() {
        return R.layout.schedule_item;
    }

    @Override
    public ViewHolder getViewHolder(@NonNull View v) {
        return new ViewHolder(v);
    }

    protected static class ViewHolder extends FastAdapter.ViewHolder<ScheduleItem> {
        TextView title;
        TextView date;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.itemTitle);
            date = view.findViewById(R.id.itemDate);
        }

        @Override
        public void bindView(ScheduleItem item, List<Object> payloads) {
            Log.v("ITEM", payloads.toString());
            Log.v("ITEM", String.valueOf(payloads.size()));
            //title.setText(payloads.get(0).toString());
            //date.setText(payloads.get(1).toString());
            title.setText(item.getTitle());
            date.setText(String.valueOf(item.getType()));
        }

        @Override
        public void unbindView(ScheduleItem item) {
            title.setText(null);
            date.setText(null);
        }
    }
}


