package com.rafhaanshah.studyassistant.flashcards;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rafhaanshah.studyassistant.R;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

public class FlashCardFragment extends Fragment {

    private Realm realm;
    private RealmResults<FlashCardSet> items;
    private RealmChangeListener realmListener;
    private FlashCardRecycleAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private boolean dataChanged;

    public static FlashCardFragment newInstance() {
        FlashCardFragment fragment = new FlashCardFragment();
        return fragment;
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
        items = realm.where(FlashCardSet.class).findAllSorted("title", Sort.ASCENDING);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flash_card, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerView);
        final FloatingActionButton fab = view.findViewById(R.id.fab);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        recyclerAdapter = new FlashCardRecycleAdapter(items);
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
    }

    public void newSet() {

        final char[] ALPHA_NUMERIC_STRING = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',};
        final Random random = new Random();


        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                RealmList<String> list = new RealmList<String>();
                RealmList<String> list2 = new RealmList<String>();
                list.add("Q ONE");
                list.add("Q TWO");
                list.add("Q THREE");
                list.add("Q FOUR");
                list.add("Q FIVE");

                list2.add("A ONE");
                list2.add("A TWO");
                list2.add("A THREE");
                list2.add("A FOUR");
                list2.add("A FIVE");

                FlashCardSet item = realm.createObject(FlashCardSet.class);
                item.setTitle("Set " + String.valueOf(ALPHA_NUMERIC_STRING[random.nextInt(10)]));
                item.setCards(list);
                item.setAnswers(list2);
            }

        });

        updateData();
    }

    private void updateData() {
        if (dataChanged) {
            dataChanged = false;

        }
        recyclerAdapter.updateData(realm.where(FlashCardSet.class).findAllSorted("title", Sort.ASCENDING));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.removeChangeListener(realmListener);
        realm.close();
    }
}