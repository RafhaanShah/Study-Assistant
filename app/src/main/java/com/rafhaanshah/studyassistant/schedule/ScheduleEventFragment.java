package com.rafhaanshah.studyassistant.schedule;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import java.text.DateFormat;

import io.realm.Realm;

public class ScheduleEventFragment extends DialogFragment {

    private static final String BUNDLE_ID = "BUNDLE_ID";
    private ScheduleEvent event;

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
        int eventID = getArguments().getInt(BUNDLE_ID, -1);
        Realm realm = Realm.getDefaultInstance();
        event = realm.where(ScheduleEvent.class).equalTo(ScheduleEvent.ScheduleEvent_ID, eventID).findFirst();
        realm.close();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_event, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        ImageView closeButton = view.findViewById(R.id.btn_close);
        ImageView editButton = view.findViewById(R.id.btn_edit);
        setButtonActions(closeButton, editButton);

        TextView statusText = view.findViewById(R.id.tv_event_status);
        if (event.isCompleted()) {
            statusText.setText(getString(R.string.completed));
            statusText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_white_24dp, 0, 0, 0);
        }

        TextView titleText = view.findViewById(R.id.tv_event_title);
        titleText.setText(event.getTitle());

        TextView typeText = view.findViewById(R.id.tv_event_type);
        setIconAndType(getContext(), typeText, event.getType());

        TextView dateText = view.findViewById(R.id.tv_event_date);
        dateText.setText(getString(R.string.date_and_time,
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(event.getEventTime()),
                DateFormat.getTimeInstance(DateFormat.SHORT).format(event.getEventTime())));

        TextView reminderText = view.findViewById(R.id.tv_event_reminder_date);
        if (event.isReminderSet() && event.getReminderTime() < System.currentTimeMillis()) {
            reminderText.setText(getString(R.string.date_and_time,
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(event.getEventTime()),
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(event.getEventTime())));
        } else {
            reminderText.setText(getString(R.string.no_reminder));
        }

        TextView notesText = view.findViewById(R.id.tv_event_notes);
        if (TextUtils.isEmpty(event.getNotes())) {
            notesText.setVisibility(View.GONE);
        } else {
            notesText.setText(event.getNotes());
            notesText.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    private void setIconAndType(Context context, TextView typeText, ScheduleEvent.ScheduleEventType type) {
        switch (type) {
            case HOMEWORK:
                typeText.setText(context.getString(R.string.homework));
                typeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_black_24dp, 0, 0, 0);
                break;
            case TEST:
                typeText.setText(context.getString(R.string.class_test));
                typeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_chrome_reader_mode_black_24dp, 0, 0, 0);
                break;
            case COURSEWORK:
                typeText.setText(context.getString(R.string.coursework));
                typeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_computer_black_24dp, 0, 0, 0);
                break;
            case EXAM:
                typeText.setText(context.getString(R.string.exam));
                typeText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_black_24dp, 0, 0, 0);
                break;
        }
        HelperUtils.setDrawableColour(typeText.getCompoundDrawables()[0], ContextCompat.getColor(context, R.color.textGrey));
    }

    private void setButtonActions(ImageView closeButton, ImageView editButton) {
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dismiss();
                getContext().startActivity(ScheduleEventActivity.getStartIntent(getContext(), event.getID()));
            }
        });
    }
}