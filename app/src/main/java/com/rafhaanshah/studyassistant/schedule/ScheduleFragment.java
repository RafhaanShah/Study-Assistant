package com.rafhaanshah.studyassistant.schedule;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ScheduleFragment extends Fragment {

    private Realm realm;
    private RealmResults<ScheduleItem> items;
    private ScheduleRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private boolean history;

    public static ScheduleFragment newInstance() {
        ScheduleFragment fragment = new ScheduleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerView);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerAdapter = new ScheduleRecyclerAdapter(items);
        recyclerView.setAdapter(recyclerAdapter);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        items = realm.where(ScheduleItem.class).equalTo("completed", false).findAllSorted("time", Sort.ASCENDING);
        recyclerAdapter.updateData(items);
        recyclerView.invalidate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.
    }

    public void showCompleted() {
        if (!history) {
            history = true;
            Toast.makeText(getContext(), "Completed events", Toast.LENGTH_SHORT).show();
            items = realm.where(ScheduleItem.class).equalTo("completed", true).findAllSorted("time", Sort.DESCENDING);
            recyclerAdapter.updateData(items);
            recyclerView.invalidate();
        } else {
            history = false;
            Toast.makeText(getContext(), "Incomplete events", Toast.LENGTH_SHORT).show();
            onResume();
        }
    }
}