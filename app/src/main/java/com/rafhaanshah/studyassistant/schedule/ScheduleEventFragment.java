package com.rafhaanshah.studyassistant.schedule;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.MainApplication;
import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import java.text.DateFormat;

import io.realm.Realm;

public class ScheduleEventFragment extends DialogFragment {

    public static final String TAG_EVENT_DIALOG_FRAGMENT = "TAG_EVENT_DIALOG_FRAGMENT";
    private static final String BUNDLE_ID = "BUNDLE_ID";
    private ScheduleEvent event;
    private Realm realm;
    private String typeString;

    public static ScheduleEventFragment newInstance(int ID) {
        ScheduleEventFragment frag = new ScheduleEventFragment();
        Bundle args = new Bundle();
        args.putInt(BUNDLE_ID, ID);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
        int eventID = getArguments().getInt(BUNDLE_ID, -1);
        realm = Realm.getDefaultInstance();
        event = realm.where(ScheduleEvent.class).equalTo(ScheduleEvent.ScheduleEvent_ID, eventID).findFirst();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_event, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (event != null) {
            setHeaderColour(view);
            setIconAndType(getContext(), (TextView) view.findViewById(R.id.tv_event_type), event.getType());
            setTextViews(view);
            setButtonActions((ImageButton) view.findViewById(R.id.btn_close), (ImageButton) view.findViewById(R.id.btn_calendar), (ImageButton) view.findViewById(R.id.btn_edit));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        super.onDestroy();
        MainApplication.getRefWatcher(getActivity()).watch(this);
    }

    private void setHeaderColour(@NonNull final View view) {
        final long currentTime = System.currentTimeMillis();
        int colour = ContextCompat.getColor(getContext(), R.color.materialGreen);
        if (event.isCompleted()) {
            colour = ContextCompat.getColor(getContext(), R.color.materialBlue);
        } else if (event.getEventTime() < currentTime) {
            colour = ContextCompat.getColor(getContext(), R.color.materialRed);
        } else if (event.getEventTime() < (currentTime + (86400000 * 3))) {
            colour = ContextCompat.getColor(getContext(), R.color.materialOrange);
        }
        view.findViewById(R.id.title_layout).setBackgroundColor(colour);
    }

    private void setTextViews(@NonNull final View view) {
        TextView statusText = view.findViewById(R.id.tv_event_status);
        if (event.isCompleted()) {
            statusText.setText(getString(R.string.completed));
            statusText.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check_white_24dp, 0, 0, 0);
        }
        HelperUtils.setDrawableColour(statusText.getCompoundDrawablesRelative()[0], ContextCompat.getColor(getContext(), R.color.textGrey));

        TextView titleText = view.findViewById(R.id.tv_event_title);
        titleText.setText(event.getTitle());
        HelperUtils.setDrawableColour(titleText.getCompoundDrawablesRelative()[0], ContextCompat.getColor(getContext(), R.color.textGrey));

        TextView dateText = view.findViewById(R.id.tv_event_date);
        dateText.setText(getString(R.string.date_and_time,
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(event.getEventTime()),
                DateFormat.getTimeInstance(DateFormat.SHORT).format(event.getEventTime())));
        HelperUtils.setDrawableColour(dateText.getCompoundDrawablesRelative()[0], ContextCompat.getColor(getContext(), R.color.textGrey));

        TextView reminderText = view.findViewById(R.id.tv_event_reminder_date);
        if (event.isReminderSet() && event.getReminderTime() > System.currentTimeMillis()) {
            reminderText.setText(getString(R.string.date_and_time,
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(event.getReminderTime()),
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(event.getReminderTime())));
        } else {
            reminderText.setText(getString(R.string.no_reminder));
        }
        HelperUtils.setDrawableColour(reminderText.getCompoundDrawablesRelative()[0], ContextCompat.getColor(getContext(), R.color.textGrey));

        TextView notesText = view.findViewById(R.id.tv_event_notes);
        if (TextUtils.isEmpty(event.getNotes())) {
            notesText.setVisibility(View.GONE);
        } else {
            notesText.setText(event.getNotes());
            notesText.setMovementMethod(new ScrollingMovementMethod());
            HelperUtils.setDrawableColour(notesText.getCompoundDrawablesRelative()[0], ContextCompat.getColor(getContext(), R.color.textGrey));
        }
    }

    @SuppressLint("ResourceType")
    private void setIconAndType(Context context, TextView typeText, ScheduleEvent.ScheduleEventType type) {
        final TypedArray icons = getResources().obtainTypedArray(R.array.event_type_icons);
        final String[] eventTypes = getResources().getStringArray(R.array.event_types);
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
        typeString = typeText.getText().toString();
        icons.recycle();
        HelperUtils.setDrawableColour(typeText.getCompoundDrawablesRelative()[0], ContextCompat.getColor(context, R.color.textGrey));
    }

    private void setButtonActions(ImageButton closeButton, ImageButton calendarButton, ImageButton editButton) {
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getEventTime())
                        .putExtra(CalendarContract.Events.TITLE, event.getTitle())
                        .putExtra(CalendarContract.Events.DESCRIPTION, typeString + System.lineSeparator() + event.getNotes())
                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    dismiss();
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_calendar_app), Toast.LENGTH_LONG).show();
                }
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                getContext().startActivity(ScheduleEventActivity.getStartIntent(getContext(), event.getID()));
            }
        });
    }
}