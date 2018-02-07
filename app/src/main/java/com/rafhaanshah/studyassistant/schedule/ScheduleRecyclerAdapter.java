package com.rafhaanshah.studyassistant.schedule;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ScheduleRecyclerAdapter extends RecyclerView.Adapter<ScheduleRecyclerAdapter.ViewHolder> {
    private RealmResults<ScheduleItem> scheduleItems;
    private RealmResults<ScheduleItem> completeItems;
    private RealmResults<ScheduleItem> currentItems;
    private boolean history;
    private Context context;
    private Realm realm;

    ScheduleRecyclerAdapter(Realm getRealm) {
        realm = getRealm;
        scheduleItems = realm.where(ScheduleItem.class).equalTo("completed", false).findAllSorted("time", Sort.ASCENDING);
        completeItems = realm.where(ScheduleItem.class).equalTo("completed", true).findAllSorted("time", Sort.DESCENDING);
        currentItems = scheduleItems;
    }

    @Override
    public ScheduleRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_schedule, parent, false);
        context = v.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final ScheduleItem item = currentItems.get(position);
        final long currentTime = System.currentTimeMillis();
        final long eventTime = item.getTime();
        String showTime = (String) DateUtils.getRelativeTimeSpanString(eventTime, currentTime, DateUtils.MINUTE_IN_MILLIS);
        int colour = ContextCompat.getColor(context, R.color.materialGreen);

        if (item.isCompleted()) {
            colour = ContextCompat.getColor(context, R.color.materialBlue);
        } else if (eventTime < currentTime) {
            colour = ContextCompat.getColor(context, R.color.materialRed);
        } else if (eventTime < (currentTime + (86400000 * 3))) {
            colour = ContextCompat.getColor(context, R.color.materialOrange);
        }

        GradientDrawable shape = new GradientDrawable();
        shape.setColor(colour);
        shape.setShape(GradientDrawable.RECTANGLE);
        holder.rectangle.setBackground(shape);

        holder.titleText.setText(item.getTitle());
        holder.timeText.setText(showTime);
        holder.typeText.setText(item.getType());
        holder.cardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(v.getContext(), ScheduleItemActivity.class);
                nextScreen.putExtra("item", String.valueOf(item.getID()));
                v.getContext().startActivity(nextScreen);
            }
        });
    }

    @Override
    public int getItemCount() {
        return currentItems.size();
    }

    void updateData(boolean hist, boolean upd) {
        if (upd) {
            scheduleItems = realm.where(ScheduleItem.class).equalTo("completed", false).findAllSorted("time", Sort.ASCENDING);
            completeItems = realm.where(ScheduleItem.class).equalTo("completed", true).findAllSorted("time", Sort.DESCENDING);
        }
        history = hist;
        if (history) {
            currentItems = completeItems;
        } else {
            currentItems = scheduleItems;
        }
        notifyDataSetChanged();
    }

    void filter(String query) {
        if (!TextUtils.isEmpty(query)) {
            if (history) {
                currentItems = completeItems.where().contains("title", query, Case.INSENSITIVE).findAllSorted("time", Sort.DESCENDING);
            } else {
                currentItems = scheduleItems.where().contains("title", query, Case.INSENSITIVE).findAllSorted("time", Sort.ASCENDING);
            }
            Log.v("Filter", String.valueOf(currentItems.size()));
            notifyDataSetChanged();
        } else {
            updateData(history, true);
        }
    }

    ScheduleItem getItem(int position) {
        return currentItems.get(position);
    }

    void completeItem(int position) {
        final ScheduleItem item = currentItems.get(position);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                if (item.isCompleted()) {
                    item.setCompleted(false);
                } else {
                    item.setCompleted(true);
                }
            }
        });
        updateData(history, true);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView timeText;
        private TextView typeText;
        private View rectangle;
        private CardView cardView;

        ViewHolder(View v) {
            super(v);
            titleText = v.findViewById(R.id.itemTitle);
            timeText = v.findViewById(R.id.itemDate);
            typeText = v.findViewById(R.id.itemType);
            rectangle = v.findViewById(R.id.rectangle);
            cardView = v.findViewById(R.id.cardView);
        }
    }
}