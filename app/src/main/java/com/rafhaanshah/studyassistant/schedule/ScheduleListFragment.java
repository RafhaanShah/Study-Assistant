package com.rafhaanshah.studyassistant.schedule;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class ScheduleListFragment extends Fragment {

    private Realm realm;
    private RealmResults<ScheduleItem> items, oldItems;
    private RealmChangeListener realmListener;
    private ScheduleRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private boolean history, dataChanged;
    private TextView emptyText;

    public static ScheduleListFragment newInstance() {
        return new ScheduleListFragment();
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
        return inflater.inflate(R.layout.fragment_schedule_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerView);
        final FloatingActionButton fab = view.findViewById(R.id.fab);
        emptyText = view.findViewById(R.id.emptyText);
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
                        } else {
                            item.setCompleted(true);
                        }
                    }
                });
                updateData();
            }

            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    final int position = viewHolder.getAdapterPosition();
                    final ScheduleItem item;
                    int col;
                    Bitmap icon;

                    if (!history) {
                        item = items.get(position);
                    } else {
                        item = oldItems.get(position);
                    }

                    if (item.isCompleted()) {
                        col = (ContextCompat.getColor(getContext(), R.color.materialRed));
                        icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_undo_white_24dp);
                    } else {
                        col = (ContextCompat.getColor(getContext(), R.color.materialGreen));
                        icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_check_white_24dp);
                    }

                    Paint p = new Paint();

                    if (dX > 0) {
                        p.setColor(col);
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), p);
                        c.drawBitmap(icon,
                                (float) itemView.getLeft() + Math.round(16 * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT)),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2, p);
                    } else {
                        p.setColor(col);
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), p);
                        c.drawBitmap(icon,
                                (float) itemView.getRight() - Math.round(16 * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT)) - icon.getWidth(),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2,
                                p);
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
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

    public void showData(boolean showHistory) {
        history = showHistory;
        if (history) {
            recyclerAdapter.updateData(oldItems);
            if (oldItems.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
            } else if (emptyText.getVisibility() == View.VISIBLE) {
                emptyText.setVisibility(View.GONE);
            }
        } else {
            recyclerAdapter.updateData(items);
            if (items.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
            } else if (emptyText.getVisibility() == View.VISIBLE) {
                emptyText.setVisibility(View.GONE);
            }
        }
    }
}