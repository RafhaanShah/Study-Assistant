package com.rafhaanshah.studyassistant;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.ArrayList;

public class ScheduleFragment extends Fragment {
    public static ScheduleFragment newInstance() {
        ScheduleFragment fragment = new ScheduleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //create the ItemAdapter holding your Items
        ItemAdapter itemAdapter = new ItemAdapter();
        //create the managing FastAdapter, by passing in the itemAdapter
        FastAdapter fastAdapter = FastAdapter.with(itemAdapter);

        RecyclerView recyclerView = getView().findViewById(R.id.recyclerView);
        //set our adapters to the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(fastAdapter);

        //set the items to your ItemAdapter
        ArrayList<ScheduleItem> items = new ArrayList<>();
        items.add(new ScheduleItem(1, "A"));
        items.add(new ScheduleItem(2, "B"));
        itemAdapter.add(items);
    }

}