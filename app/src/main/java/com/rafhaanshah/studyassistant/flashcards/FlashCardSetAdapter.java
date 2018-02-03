package com.rafhaanshah.studyassistant.flashcards;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;

public class FlashCardSetAdapter extends FragmentStatePagerAdapter {

    private ArrayList<FlashCardSetFragment> arr;
    private FlashCardSet item;
    private int lastChanged;

    FlashCardSetAdapter(FragmentManager fm, FlashCardSet set) {
        super(fm);
        item = set;
        arr = new ArrayList<>(item.getCards().size());
        arr.add(null);
        for (int i = 0; i < item.getCards().size(); i++) {
            arr.add(null);
        }
    }

    /*
    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FlashCardStackFragment frag = (FlashCardStackFragment) super.instantiateItem(container, position);
        arr.setValueAt(position, frag);
        return frag;
    }
    */

    // This is called to create new fragments
    @Override
    public Fragment getItem(int position) {
        Log.v("PAGE", "GET ITEM " + String.valueOf(position));
        FlashCardSetFragment frag = FlashCardSetFragment.newInstance(item.getCards().get(position), item.getAnswers().get(position), position);
        arr.set(position, frag);
        return frag;
    }

    // This updates fragments when they have changed
    @Override
    public int getItemPosition(@NonNull Object item) {
        int index = arr.indexOf(item);
        FlashCardSetFragment frag = (FlashCardSetFragment) item;
        Log.v("PAGE", "GET POSITION " + String.valueOf(index) + String.valueOf(frag.getPosition()));
        if (index != ((FlashCardSetFragment) item).getPosition()) {
            Log.v("PAGE", "RETURN NONE");
            return POSITION_NONE;
        } else {
            Log.v("PAGE", "RETURN " + String.valueOf(index));
            return index;
        }
    }

    // This fixes fragments being lost on rotation
    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    // Returns the fragment at a position
    FlashCardSetFragment getFragment(int position) {
        //Log.v("PAGE", "GET " + String.valueOf(position));
        return arr.get(position);
    }

    void removeFragment(int position) {
        Log.v("PAGE", "DELETE " + String.valueOf(position));
        arr.remove(position);
    }

    void addFragment(int position) {
        Log.v("PAGE", "ADD " + String.valueOf(position));
        arr.add(position, null);
    }

    @Override
    public int getCount() {
        if (item.isValid()) {
            return item.getAnswers().size();
        } else {
            return 0;
        }
    }

    void updateData() {
        Log.v("PAGE", "UPDATED " + String.valueOf(arr.size()) + String.valueOf(item.getCards().size()));
        notifyDataSetChanged();
    }
}
