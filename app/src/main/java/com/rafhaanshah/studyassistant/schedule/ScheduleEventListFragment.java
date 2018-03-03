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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.MainApplication;
import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import io.realm.Realm;

public class ScheduleEventListFragment extends Fragment {

    private static final String BUNDLE_HISTORY = "BUNDLE_HISTORY";

    private boolean history;
    private Realm realm;
    private ScheduleEventRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private TextView emptyText;

    public static ScheduleEventListFragment newInstance(boolean getHistory) {
        ScheduleEventListFragment scheduleEventListFragment = new ScheduleEventListFragment();
        Bundle bundle = new Bundle(1);
        bundle.putBoolean(BUNDLE_HISTORY, getHistory);
        scheduleEventListFragment.setArguments(bundle);
        return scheduleEventListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        history = getArguments().getBoolean(BUNDLE_HISTORY);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final FloatingActionButton fab = view.findViewById(R.id.fab);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        emptyText = view.findViewById(R.id.tv_empty);

        recyclerView = view.findViewById(R.id.fragment_recycler_view);
        recyclerAdapter = new ScheduleEventRecyclerAdapter(getContext(), getChildFragmentManager(), realm, recyclerView, history);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);
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

        setItemTouchHelper();
    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerAdapter.notifyDataSetChanged();
        recyclerAdapter.addListener();
        updateView();
    }

    @Override
    public void onPause() {
        super.onPause();
        recyclerAdapter.removeListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        super.onDestroy();
        MainApplication.getRefWatcher(getActivity()).watch(this);
    }

    private void setItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                recyclerAdapter.markEvent(viewHolder.getAdapterPosition());
            }

            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    int col;
                    Bitmap icon;

                    if (history) {
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
    }

    private void updateView() {
        if (recyclerAdapter.getItemCount() == 0) {
            emptyText.setVisibility(View.VISIBLE);
        } else if (emptyText.getVisibility() == View.VISIBLE) {
            emptyText.setVisibility(View.GONE);
        }
    }

    public void filter(String query) {
        recyclerAdapter.filter(query);
        if (TextUtils.isEmpty(query)) {
            updateView();
        }
    }

    public void filterType(ScheduleEvent.ScheduleEventType type) {
        recyclerAdapter.filterType(type);
    }

    public void scrollToTop() {
        HelperUtils.scrollToTop(getContext(), recyclerView);
    }
}