package com.rafhaanshah.studyassistant.schedule;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class ScheduleFragment extends Fragment {

    private Realm realm;
    private RealmResults<ScheduleItem> items, oldItems;
    private RealmChangeListener realmListener;
    private ScheduleRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private boolean history, dataChanged;

    public static ScheduleFragment newInstance() {
        return new ScheduleFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataChanged = true;
        realm = Realm.getDefaultInstance();
        realmListener = new RealmChangeListener() {
            @Override
            public void onChange(Object o) {
                dataChanged = true;
            }
        };
        realm.addChangeListener(realmListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerView);
        final FloatingActionButton fab = view.findViewById(R.id.fab);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerAdapter = new ScheduleRecyclerAdapter(items);
        recyclerView.setAdapter(recyclerAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < 0 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int position = viewHolder.getAdapterPosition();
                final ScheduleItem item;
                if (!history) {
                    item = items.get(position);
                } else {
                    item = oldItems.get(position);
                }
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(@NonNull Realm realm) {
                        if (item.isCompleted()) {
                            item.setCompleted(false);
                            Toast.makeText(getContext(), item.getTitle() + " Incomplete", Toast.LENGTH_SHORT).show();
                        } else {
                            item.setCompleted(true);
                            Toast.makeText(getContext(), item.getTitle() + " Completed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                updateData();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    private void updateData() {
        if (dataChanged) {
            dataChanged = false;
            items = realm.where(ScheduleItem.class).equalTo("completed", false).findAllSorted("time", Sort.ASCENDING);
            oldItems = realm.where(ScheduleItem.class).equalTo("completed", true).findAllSorted("time", Sort.DESCENDING);
        }
        showData(history);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.removeChangeListener(realmListener);
        realm.close();
    }

    public void showData(boolean val) {
        history = val;
        if (history) {
            recyclerAdapter.updateData(oldItems);
        } else {
            recyclerAdapter.updateData(items);
        }
    }
}