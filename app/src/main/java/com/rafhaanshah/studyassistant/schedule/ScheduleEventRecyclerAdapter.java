package com.rafhaanshah.studyassistant.schedule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
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
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.notifications.Notifier;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;


public class ScheduleEventRecyclerAdapter extends RecyclerView.Adapter<ScheduleEventRecyclerAdapter.ViewHolder> {

    private static final int ONE_DAY_MS = 86400000;

    private RealmResults<ScheduleEvent> scheduleEvents;
    private RealmResults<ScheduleEvent> filteredEvents;
    private FragmentManager fragmentManager;
    private RecyclerView recyclerView;
    private Context context;
    private Realm realm;
    private Sort sort;

    ScheduleEventRecyclerAdapter(Context getContext, FragmentManager fm, Realm getRealm, RecyclerView getRecyclerView, boolean history) {
        realm = getRealm;
        context = getContext;
        fragmentManager = fm;
        if (history) {
            sort = Sort.DESCENDING;
        } else {
            sort = Sort.ASCENDING;
        }
        recyclerView = getRecyclerView;
        scheduleEvents = realm.where(ScheduleEvent.class).equalTo(ScheduleEvent.ScheduleEvent_COMPLETED, history).findAllSorted(ScheduleEvent.ScheduleEvent_TIME, sort);
        filteredEvents = scheduleEvents;
    }

    @Override
    public ScheduleEventRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_schedule_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ScheduleEvent scheduleEvent = filteredEvents.get(position);
        final long currentTime = System.currentTimeMillis();
        final long eventTime = scheduleEvent.getTime();
        String showTime = DateUtils.getRelativeTimeSpanString(eventTime, currentTime, DateUtils.MINUTE_IN_MILLIS).toString();
        int colour = ContextCompat.getColor(context, R.color.materialGreen);

        if (scheduleEvent.isCompleted()) {
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
        holder.titleText.setText(scheduleEvent.getTitle());
        holder.timeText.setText(showTime);

        switch (scheduleEvent.getType()) {
            case HOMEWORK:
                holder.typeText.setText(context.getString(R.string.homework));
                holder.typeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_black_24dp, 0, 0, 0);
                break;
            case TEST:
                holder.typeText.setText(context.getString(R.string.class_test));
                holder.typeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_chrome_reader_mode_black_24dp, 0, 0, 0);
                break;
            case COURSEWORK:
                holder.typeText.setText(context.getString(R.string.coursework));
                holder.typeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_computer_black_24dp, 0, 0, 0);
                break;
            case EXAM:
                holder.typeText.setText(context.getString(R.string.exam));
                holder.typeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_black_24dp, 0, 0, 0);
                break;
        }

        HelperUtils.setDrawableColour(holder.typeText.getCompoundDrawables()[0], ContextCompat.getColor(context, R.color.textGrey));

        holder.cardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //showDialogFragment();
                context.startActivity(ScheduleEventActivity.getStartIntent(context, scheduleEvent.getID()));
                //((Activity) context).overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                showPopupMenu(holder, scheduleEvent, holder.getAdapterPosition());
                return true;
            }
        });
    }

    private void showPopupMenu(ViewHolder holder, final ScheduleEvent scheduleEvent, final int position) {
        PopupMenu popup = new PopupMenu(context, holder.itemView, Gravity.END);
        popup.inflate(R.menu.activity_main_popup);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.popup_edit:
                        editEvent(scheduleEvent);
                        return true;
                    case R.id.popup_delete:
                        deleteEvent(scheduleEvent, position);
                        return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void editEvent(ScheduleEvent scheduleEvent) {
        context.startActivity(ScheduleEventActivity.getStartIntent(context, scheduleEvent.getID()));
        //((Activity) context).overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
    }

    private void deleteEvent(final ScheduleEvent scheduleEvent, final int position) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm_delete))
                .setMessage(context.getString(R.string.delete_event))
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (scheduleEvent.isReminder() && scheduleEvent.getReminderTime() != 0L && scheduleEvent.getReminderTime() > System.currentTimeMillis()) {
                            Notifier.cancelScheduledNotification(context, scheduleEvent.getID());
                        }
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                scheduleEvent.deleteFromRealm();
                            }
                        });

                        notifyItemRemoved(position);
                    }
                })
                .setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.ic_delete_black_24dp)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return filteredEvents.size();
    }

    private void resetList() {
        filteredEvents = scheduleEvents;
    }

    void animateList() {
        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(controller);
        notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    void filterType(ScheduleEvent.ScheduleItemType type) {
        if (type != null) {
            filteredEvents = scheduleEvents.where().equalTo(ScheduleEvent.ScheduleEvent_TYPE, type.name()).findAllSorted(ScheduleEvent.ScheduleEvent_TIME, sort);
        } else {
            resetList();
        }
        animateList();
    }

    void filter(String query) {
        if (!TextUtils.isEmpty(query)) {
            filteredEvents = scheduleEvents.where().contains(ScheduleEvent.ScheduleEvent_TITLE, query.toLowerCase(), Case.INSENSITIVE).findAllSorted(ScheduleEvent.ScheduleEvent_TIME, sort);
        } else {
            resetList();
        }
        animateList();
    }

    void completeEvent(int position) {
        final ScheduleEvent scheduleEvent = filteredEvents.get(position);
        final long notificationTime = scheduleEvent.getReminderTime();
        final boolean notification = scheduleEvent.isReminder();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                if (scheduleEvent.isCompleted()) {
                    scheduleEvent.setCompleted(false);
                } else {
                    scheduleEvent.setCompleted(true);
                    scheduleEvent.setReminder(false);
                    scheduleEvent.setReminderTime(0L);
                }
            }
        });
        notifyItemRemoved(position);
        if (notification && notificationTime != 0L && notificationTime > System.currentTimeMillis()) {
            Notifier.cancelScheduledNotification(context, scheduleEvent.getID());
        }
    }

    void addListener() {
        scheduleEvents.addChangeListener(new RealmChangeListener<RealmResults<ScheduleEvent>>() {
            @Override
            public void onChange(@NonNull RealmResults<ScheduleEvent> items) {
                notifyItemRangeChanged(0, items.size());
            }
        });
    }

    void removeListener() {
        scheduleEvents.removeAllChangeListeners();
    }

    private void showDialogFragment() {
        ScheduleEventFragment scheduleEventFragment = ScheduleEventFragment.newInstance();
        scheduleEventFragment.show(fragmentManager, "event_fragment");
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