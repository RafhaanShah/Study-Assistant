package com.rafhaanshah.studyassistant.schedule;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;

import io.realm.RealmResults;

public class ScheduleRecyclerAdapter extends RecyclerView.Adapter<ScheduleRecyclerAdapter.ViewHolder> {
    private RealmResults<ScheduleItem> values;
    private Context context;

    ScheduleRecyclerAdapter(RealmResults<ScheduleItem> dataset) {
        values = dataset;
    }

    @Override
    public ScheduleRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.schedule_item, parent, false);
        context = v.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final ScheduleItem item = values.get(position);
        final long currentTime = System.currentTimeMillis();
        final long eventTime = item.getTime();
        String showTime = (String) DateUtils.getRelativeTimeSpanString(eventTime, currentTime, DateUtils.HOUR_IN_MILLIS);
        int colour = ContextCompat.getColor(context, R.color.scheduleGreen);

        if (item.isCompleted()) {
            colour = ContextCompat.getColor(context, R.color.scheduleBlue);
        } else if (eventTime < currentTime) {
            colour = ContextCompat.getColor(context, R.color.scheduleRed);
        } else if (eventTime < (currentTime + (86400000 * 3))) {
            colour = ContextCompat.getColor(context, R.color.scheduleOrange);
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
        return values.size();
    }

    void updateData(RealmResults<ScheduleItem> items) {
        values = items;
        notifyDataSetChanged();
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