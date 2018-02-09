package com.rafhaanshah.studyassistant.schedule;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;


public class ScheduleRecyclerAdapter extends RecyclerView.Adapter<ScheduleRecyclerAdapter.ViewHolder> {

    private static final int ONE_DAY_MS = 86400000;

    private RealmResults<ScheduleItem> scheduleItems;
    private RealmResults<ScheduleItem> currentItems;
    private boolean history;
    private Context context;
    private Realm realm;

    ScheduleRecyclerAdapter(Realm getRealm) {
        realm = getRealm;
        scheduleItems = realm.where(ScheduleItem.class).findAll();
        currentItems = scheduleItems.where().equalTo(ScheduleItem.ScheduleItem_COMPLETED, false).findAllSorted(ScheduleItem.ScheduleItem_TIME, Sort.ASCENDING);
        scheduleItems.addChangeListener(new RealmChangeListener<RealmResults<ScheduleItem>>() {
            @Override
            public void onChange(@NonNull RealmResults<ScheduleItem> scheduleItems) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public ScheduleRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_schedule, parent, false);
        context = view.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ScheduleItem item = currentItems.get(position);
        final long currentTime = System.currentTimeMillis();
        final long eventTime = item.getTime();
        String showTime = (String) DateUtils.getRelativeTimeSpanString(eventTime, currentTime, DateUtils.MINUTE_IN_MILLIS);
        int colour = ContextCompat.getColor(context, R.color.materialGreen);

        if (item.isCompleted()) {
            colour = ContextCompat.getColor(context, R.color.materialBlue);
        } else if (eventTime < currentTime) {
            colour = ContextCompat.getColor(context, R.color.materialRed);
        } else if (eventTime < (currentTime + (ONE_DAY_MS * 3))) {
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
            public void onClick(View view) {
                context.startActivity(ScheduleItemActivity.getStartIntent(context, item.getID()));
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                showPopupMenu(holder, item, holder.getAdapterPosition());
                return true;
            }
        });
    }

    private void showPopupMenu(ViewHolder holder, final ScheduleItem item, final int position) {
        PopupMenu popup = new PopupMenu(context, holder.itemView, Gravity.END);
        popup.inflate(R.menu.activity_main_popup);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.popup_edit:
                        return true;
                    case R.id.popup_delete:
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return currentItems.size();
    }

    void updateData(boolean hist) {
        history = hist;
        if (history) {
            currentItems = scheduleItems.where().equalTo(ScheduleItem.ScheduleItem_COMPLETED, true).findAllSorted(ScheduleItem.ScheduleItem_TIME, Sort.DESCENDING);
        } else {
            currentItems = scheduleItems.where().equalTo(ScheduleItem.ScheduleItem_COMPLETED, false).findAllSorted(ScheduleItem.ScheduleItem_TIME, Sort.ASCENDING);
        }
        notifyDataSetChanged();
    }

    void filter(String query) {
        if (!TextUtils.isEmpty(query)) {
            if (history) {
                currentItems = scheduleItems.where().equalTo(ScheduleItem.ScheduleItem_COMPLETED, true).contains(ScheduleItem.ScheduleItem_TITLE, query.toLowerCase(), Case.INSENSITIVE).findAllSorted(ScheduleItem.ScheduleItem_TIME, Sort.DESCENDING);
            } else {
                currentItems = scheduleItems.where().equalTo(ScheduleItem.ScheduleItem_COMPLETED, false).contains(ScheduleItem.ScheduleItem_TITLE, query.toLowerCase(), Case.INSENSITIVE).findAllSorted(ScheduleItem.ScheduleItem_TIME, Sort.ASCENDING);
            }
            notifyDataSetChanged();
        } else {
            updateData(history);
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
        updateData(history);
    }

    void removeListener() {
        scheduleItems.removeAllChangeListeners();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView timeText;
        private TextView typeText;
        private View rectangle;
        private CardView cardView;

        ViewHolder(View view) {
            super(view);
            titleText = view.findViewById(R.id.tv_event_title);
            timeText = view.findViewById(R.id.tv_event_date);
            typeText = view.findViewById(R.id.tv_event_type);
            rectangle = view.findViewById(R.id.rectangle);
            cardView = view.findViewById(R.id.card_view);
        }
    }
}