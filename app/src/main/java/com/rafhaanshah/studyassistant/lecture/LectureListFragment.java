package com.rafhaanshah.studyassistant.lecture;

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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.HelperUtils;
import com.rafhaanshah.studyassistant.R;

public class LectureListFragment extends Fragment {

    private static int sorting;
    private LectureRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private TextView emptyText;

    public static LectureListFragment newInstance(int i) {
        LectureListFragment lectureListFragment = new LectureListFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt("sorting", i);
        lectureListFragment.setArguments(bundle);
        return lectureListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sorting = getArguments().getInt("sorting");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lecture_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final FloatingActionButton fab = view.findViewById(R.id.fab);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        emptyText = view.findViewById(R.id.tv_empty);

        recyclerAdapter = new LectureRecyclerAdapter(sorting, HelperUtils.getLectureFiles(getContext()));
        recyclerView = view.findViewById(R.id.fragment_recycler_view);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());

        recyclerView.addItemDecoration(dividerItemDecoration);
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
        setOnTouchHelper();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        updateView();
        super.onResume();
    }

    private void setOnTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                recyclerAdapter.deleteLecture(viewHolder.getAdapterPosition());
            }

            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;

                    Paint p = new Paint();
                    Bitmap icon;

                    if (dX > 0) {
                        icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_delete_white_24dp);
                        p.setColor(ContextCompat.getColor(getContext(), R.color.materialRed));
                        c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom(), p);
                        c.drawBitmap(icon,
                                (float) itemView.getLeft() + Math.round(16 * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT)),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2, p);
                    } else {
                        icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_delete_white_24dp);
                        p.setColor(ContextCompat.getColor(getContext(), R.color.materialRed));
                        c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), p);
                        c.drawBitmap(icon,
                                (float) itemView.getRight() - Math.round(16 * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT)) - icon.getWidth(),
                                (float) itemView.getTop() + ((float) itemView.getBottom() - (float) itemView.getTop() - icon.getHeight()) / 2, p);
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void filter(String query) {
        recyclerAdapter.filter(query);
    }

    public void updateData(int sort, boolean update) {
        sorting = sort;
        if (update) {
            recyclerAdapter.updateData(sort, HelperUtils.getLectureFiles(getContext()));
        } else {
            recyclerAdapter.updateData(sort, null);
        }
        updateView();
    }

    private void updateView() {
        if (recyclerAdapter.getItemCount() == 0) {
            emptyText.setVisibility(View.VISIBLE);
        } else if (emptyText.getVisibility() == View.VISIBLE) {
            emptyText.setVisibility(View.GONE);
        }
    }
}