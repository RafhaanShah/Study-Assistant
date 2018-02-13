package com.rafhaanshah.studyassistant.schedule;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;

public class ScheduleItemFragment extends DialogFragment {

    private Realm realm;
    private int ID;

    public static ScheduleItemFragment newInstance() {

        //TODO: what's the difference between member variables and bundles???
        // Bundles are the correct way, and for Realm just open and close a new instance

        ScheduleItemFragment frag = new ScheduleItemFragment();
        Bundle args = new Bundle();
        //args.putInt(ScheduleItem.ScheduleItem_ID, getID);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_item_view, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}