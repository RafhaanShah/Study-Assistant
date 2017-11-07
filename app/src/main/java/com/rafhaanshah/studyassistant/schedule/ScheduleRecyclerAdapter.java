package com.rafhaanshah.studyassistant.schedule;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmResults;

public class ScheduleRecyclerAdapter extends RecyclerView.Adapter<ScheduleRecyclerAdapter.ViewHolder> {
    private final long daySeconds = 86400;
    private RealmResults<ScheduleItem> values;

    public ScheduleRecyclerAdapter(RealmResults<ScheduleItem> dataset) {
        values = dataset;
    }

    @Override
    public ScheduleRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.schedule_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final ScheduleItem item = values.get(position);
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        final long currentTime = System.currentTimeMillis();
        final long eventTime = item.getTime();
        int colour = R.color.scheduleGreen;

        if (eventTime < currentTime) {
            colour = R.color.scheduleRed;
        } else if (eventTime < (currentTime + (daySeconds * 3))) {
            colour = R.color.scheduleOrange;
        }

        //TODO: Set colour
        //holder.rectangle.getBackground().setColorFilter(new PorterDuffColorFilter(colour, PorterDuff.Mode.DARKEN));
        holder.titleText.setText(item.getTitle());
        holder.timeText.setText(dateTimeFormat.format(new Date(eventTime)));
        holder.cardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), String.valueOf(eventTime) + "-" + String.valueOf(currentTime), Toast.LENGTH_LONG).show();
                Intent nextScreen = new Intent(v.getContext(), ScheduleItemActivity.class);
                nextScreen.putExtra("item", String.valueOf(item.getID()));
                v.getContext().startActivity(nextScreen);
            }
        });
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

    public void updateData(RealmResults<ScheduleItem> items) {
        values = items;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView timeText;
        private View rectangle;
        private CardView cardView;

        ViewHolder(View v) {
            super(v);
            titleText = v.findViewById(R.id.itemTitle);
            timeText = v.findViewById(R.id.itemDate);
            rectangle = v.findViewById(R.id.rectangle);
            cardView = v.findViewById(R.id.cardView);
        }
    }
}