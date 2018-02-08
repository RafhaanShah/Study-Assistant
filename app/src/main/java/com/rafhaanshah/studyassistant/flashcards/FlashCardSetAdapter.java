package com.rafhaanshah.studyassistant.flashcards;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class FlashCardSetAdapter extends FragmentStatePagerAdapter {

    private ArrayList<FlashCardSetFragment> flashCardSetFragments;
    private FlashCardSet flashCardSet;

    FlashCardSetAdapter(FragmentManager fm, FlashCardSet set) {
        super(fm);
        flashCardSet = set;
        flashCardSetFragments = new ArrayList<>(flashCardSet.getCards().size());
        flashCardSetFragments.add(null);
        for (int i = 0; i < flashCardSet.getCards().size(); i++) {
            flashCardSetFragments.add(null);
        }
    }

    // This is called to create new fragments
    @Override
    public Fragment getItem(int position) {
        FlashCardSetFragment frag = FlashCardSetFragment.newInstance(flashCardSet.getCards().get(position), flashCardSet.getAnswers().get(position), position);
        flashCardSetFragments.set(position, frag);
        return frag;
    }

    // This updates fragments when they have changed
    @Override
    public int getItemPosition(@NonNull Object item) {
        FlashCardSetFragment frag = (FlashCardSetFragment) item;
        int index = flashCardSetFragments.indexOf(frag);
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
        return flashCardSetFragments.get(position);
    }

    void removeFragment(int position) {
        flashCardSetFragments.remove(position);
    }

    void addFragment(int position) {
        flashCardSetFragments.add(position, null);
    }

    @Override
    public int getCount() {
        if (flashCardSet.isValid()) {
            return flashCardSet.getCards().size();
        } else {
            return 0;
        }
    }
}
