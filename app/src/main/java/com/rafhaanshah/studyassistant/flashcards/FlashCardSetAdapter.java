package com.rafhaanshah.studyassistant.flashcards;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class FlashCardSetAdapter extends FragmentStatePagerAdapter {

    private ArrayList<FlashCardSetFragment> arr;
    private FlashCardSet item;

    FlashCardSetAdapter(FragmentManager fm, FlashCardSet set) {
        super(fm);
        item = set;
        arr = new ArrayList<>(item.getCards().size());
        arr.add(null);
        for (int i = 0; i < item.getCards().size(); i++) {
            arr.add(null);
        }
    }

    // This is called to create new fragments
    @Override
    public Fragment getItem(int position) {
        FlashCardSetFragment frag = FlashCardSetFragment.newInstance(item.getCards().get(position), item.getAnswers().get(position), position);
        arr.set(position, frag);
        return frag;
    }

    // This updates fragments when they have changed
    @Override
    public int getItemPosition(@NonNull Object item) {
        int index = arr.indexOf(item);
        FlashCardSetFragment frag = (FlashCardSetFragment) item;
        if (index != ((FlashCardSetFragment) item).getPosition()) {
            return POSITION_NONE;
        } else {
            return index;
        }
    }

    // This fixes fragments being lost on rotation
    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    // Returns the fragment at a position
    FlashCardSetFragment getFragment(int position) {
        return arr.get(position);
    }

    void removeFragment(int position) {
        arr.remove(position);
    }

    void addFragment(int position) {
        arr.add(position, null);
    }

    @Override
    public int getCount() {
        if (item.isValid()) {
            return item.getCards().size();
        } else {
            return 0;
        }
    }

    void updateData() {
        notifyDataSetChanged();
    }
}
