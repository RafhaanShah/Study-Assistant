package com.rafhaanshah.studyassistant.flashcards;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class FlashCardStackAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> items;

    public FlashCardStackAdapter(FragmentManager fm) {
        super(fm);

        items = new ArrayList<>();
        items.add("Item 1");
        items.add("Item 2");
        items.add("Item 3");
        items.add("Item 4");
        items.add("Item 5");
        items.add("Item 6");
        items.add("Item 7");
        items.add("Item 8");
        items.add("Item 9");
        items.add("Item 10");
        items.add("Item 11");

    }

    @Override
    public Fragment getItem(int position) {
        return FlashCardStackFragment.newInstance(items.get(position));
    }

    @Override
    public int getCount() {
        // Total number of cards
        return 10;
    }
}
