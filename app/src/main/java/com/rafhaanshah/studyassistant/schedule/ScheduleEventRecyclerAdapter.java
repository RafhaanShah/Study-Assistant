package com.rafhaanshah.studyassistant.schedule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.ContextMenu;
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
        final long eventTime = scheduleEvent.getEventTime();
        String showTime = DateUtils.getRelativeTimeSpanString(eventTime, currentTime, DateUtils.MINUTE_IN_MILLIS).toString();
        int colour = ContextCompat.getColor(context, R.color.materialGreen);

        if (scheduleEvent.isCompleted()) {
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
        holder.titleText.setText(scheduleEvent.getTitle());
        holder.timeText.setText(showTime);
        setIconAndType(holder.typeText, scheduleEvent.getType());

        holder.cardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogFragment(scheduleEvent.getID());
                //context.startActivity(ScheduleEventActivity.getStartIntent(context, scheduleEvent.getID()));
                //((Activity) context).overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
            }
        });
        setContextMenu(holder, scheduleEvent);
    }

    @Override
    public int getItemCount() {
        return filteredEvents.size();
    }

    private void setContextMenu(final ScheduleEventRecyclerAdapter.ViewHolder holder, final ScheduleEvent event) {
        holder.cardView.setOnCreateContextMenuListener(
                new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        final String markEvent;
                        if (event.isCompleted()) {
                            markEvent = context.getString(R.string.mark_incomplete);
                        } else {
                            markEvent = context.getString(R.string.mark_completed);
                        }
                        menu.add(markEvent).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                markEvent(holder.getAdapterPosition());
                                return true;
                            }
                        });
                        menu.add(context.getString(R.string.edit_event)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                editEvent(event);
                                return true;
                            }
                        });
                        menu.add(context.getString(R.string.delete)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                deleteEvent(event, holder.getAdapterPosition());
                                return true;
                            }
                        });
                    }
                }
        );
    }

    @SuppressLint("ResourceType")
    private void setIconAndType(TextView typeText, ScheduleEvent.ScheduleEventType type) {
        final TypedArray icons = context.getResources().obtainTypedArray(R.array.event_type_icons);
        final String[] eventTypes = context.getResources().getStringArray(R.array.event_types);
        switch (type) {
            case HOMEWORK:
                typeText.setText(eventTypes[0]);
                typeText.setCompoundDrawablesRelativeWithIntrinsicBounds(icons.getDrawable(0), null, null, null);
                break;
            case TEST:
                typeText.setText(eventTypes[1]);
                typeText.setCompoundDrawablesRelativeWithIntrinsicBounds(icons.getDrawable(1), null, null, null);
                break;
            case COURSEWORK:
                typeText.setText(eventTypes[2]);
                typeText.setCompoundDrawablesRelativeWithIntrinsicBounds(icons.getDrawable(2), null, null, null);
                break;
            case EXAM:
                typeText.setText(eventTypes[3]);
                typeText.setCompoundDrawablesRelativeWithIntrinsicBounds(icons.getDrawable(3), null, null, null);
                break;
        }
        HelperUtils.setDrawableColour(typeText.getCompoundDrawablesRelative()[0], ContextCompat.getColor(context, R.color.textGrey));
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
                        if (scheduleEvent.isReminderSet() && scheduleEvent.getReminderTime() > System.currentTimeMillis()) {
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

    void markEvent(final int position) {
        final ScheduleEvent scheduleEvent = filteredEvents.get(position);
        final long notificationTime = scheduleEvent.getReminderTime();
        final boolean notification = scheduleEvent.isReminderSet();
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
        ((Activity) context).closeContextMenu();
        notifyItemRemoved(position);
        if (notification && notificationTime > System.currentTimeMillis()) {
            Notifier.cancelScheduledNotification(context, scheduleEvent.getID());
        }
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

    void filterType(ScheduleEvent.ScheduleEventType type) {
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

    void addListener() {
        scheduleEvents.addChangeListener(new RealmChangeListener<RealmResults<ScheduleEvent>>() {
            @Override
            public void onChange(@NonNull RealmResults<ScheduleEvent> events) {
                notifyItemRangeChanged(0, events.size());
            }
        });
    }

    void removeListener() {
        scheduleEvents.removeAllChangeListeners();
    }

    private void showDialogFragment(int ID) {
        ScheduleEventFragment scheduleEventFragment = ScheduleEventFragment.newInstance(ID);
        scheduleEventFragment.show(fragmentManager, ScheduleEventFragment.TAG_EVENT_DIALOG_FRAGMENT);
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