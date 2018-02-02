package com.rafhaanshah.studyassistant.flashcards;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;

import io.realm.RealmList;

public class FlashCardSetAdapter extends FragmentStatePagerAdapter {

    private RealmList<String> cardTexts;
    private RealmList<String> answerTexts;
    private SparseArray<FlashCardSetFragment> arr;
    private FlashCardSet item;

    FlashCardSetAdapter(FragmentManager fm, FlashCardSet set) {
        super(fm);
        item = set;
        cardTexts = set.getCards();
        answerTexts = set.getAnswers();
        arr = new SparseArray<>(cardTexts.size());
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
        FlashCardSetFragment frag = FlashCardSetFragment.newInstance(cardTexts.get(position), answerTexts.get(position), position);
        arr.append(position, frag);
        return frag;
    }

    // This updates fragments when they have changed
    @Override
    public int getItemPosition(@NonNull Object item) {
        int i = arr.indexOfValue((FlashCardSetFragment) item);
        Log.v("GET", String.valueOf(i));
        if (((FlashCardSetFragment) item).getText().equals(cardTexts.get(i))) {
            Log.v("GET", "UNCHANGED");
            return POSITION_UNCHANGED;
        }
        Log.v("GET", "NONE");
        return POSITION_NONE;
    }


    // This fixes fragments being lost on rotation
    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    // Returns the fragment at a position
    FlashCardSetFragment getFragment(int position) {
        if (arr.get(position) == null) {
            getItem(position);
        }
        return arr.get(position);
    }

    @Override
    public int getCount() {
        if (item.isValid()) {
            return cardTexts.size();
        } else {
            return 0;
        }
    }

    void updateData() {
        cardTexts = item.getCards();
        answerTexts = item.getAnswers();
        notifyDataSetChanged();
    }
}
