package com.rafhaanshah.studyassistant.lecture;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rafhaanshah.studyassistant.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class LectureFragment extends Fragment {

    private static int sorting;
    private LectureRecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private ArrayList<File> items;
    private File directory;

    public static LectureFragment newInstance(int i) {
        sorting = i;
        return new LectureFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lecture, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        directory = new File(getContext().getFilesDir().getAbsolutePath() + File.separator + "lectures");
        getData();

        final FloatingActionButton fab = view.findViewById(R.id.fab);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recyclerAdapter = new LectureRecyclerAdapter(items);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);

        updateData(sorting, false);

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
        super.onViewCreated(view, savedInstanceState);
    }

    private void getData() {
        items = new ArrayList<>(Arrays.asList(directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".pdf");
            }
        })));
    }

    public void updateData(int i, boolean update) {
        if (update) {
            getData();
        }
        sorting = i;
        switch (i) {
            case 0:
                Collections.sort(items, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        return a.getName().compareTo(b.getName());
                    }
                });
                break;
            case 1:
                Collections.sort(items, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        Long lng = (b.lastModified() - a.lastModified());
                        return lng.intValue();
                    }
                });
                break;
            case 2:
                Collections.sort(items, new Comparator<File>() {
                    @Override
                    public int compare(File a, File b) {
                        Long lng = (b.length() - a.length());
                        return lng.intValue();
                    }
                });
                break;
        }
        recyclerAdapter.updateData(items);
    }
}