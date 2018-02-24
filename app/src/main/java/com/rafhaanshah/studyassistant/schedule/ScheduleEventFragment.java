package com.rafhaanshah.studyassistant.schedule;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;

public class ScheduleEventFragment extends DialogFragment {

    private static final String BUNDLE_ID = "BUNDLE_ID";
    private Realm realm;
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
        realm = Realm.getDefaultInstance();
        event = realm.where(ScheduleEvent.class).equalTo(ScheduleEvent.ScheduleEvent_ID, eventID).findFirst();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_event, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (event != null) {
            TextView titleText = view.findViewById(R.id.tv_title);
            titleText.setText(event.getTitle());
        }
    }
}