package com.rafhaanshah.studyassistant.flashcards;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.ArrayList;

import io.realm.RealmList;

public class FlashCardSetAdapter extends FragmentStatePagerAdapter {

    private RealmList<String> cardTexts;
    private RealmList<String> answerTexts;
    private ArrayList<FlashCardSetFragment> arr;
    private FlashCardSet item;

    FlashCardSetAdapter(FragmentManager fm, FlashCardSet set) {
        super(fm);
        item = set;
        cardTexts = set.getCards();
        answerTexts = set.getAnswers();
        arr = new ArrayList<>(cardTexts.size());
        for (int i = 0; i < cardTexts.size(); i++) {
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

    @Override
    public Fragment getItem(int position) {
        Log.v("UPDATE2", String.valueOf(position) + " " + String.valueOf(arr.size()));
        FlashCardSetFragment frag = FlashCardSetFragment.newInstance(cardTexts.get(position), answerTexts.get(position), position);
        while (position > arr.size() - 1) {
            arr.add(null);
            Log.v("UPDATE999", "WHILE");
        }
        Log.v("UPDATE3", String.valueOf(position) + " " + String.valueOf(arr.size()));
        arr.set(position, frag);
        return frag;
    }

    @Override
    public int getItemPosition(@NonNull Object item) {
        return POSITION_NONE;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }


    FlashCardSetFragment getFragment(int position) {
        Log.v("UPDATE4", String.valueOf(position) + " " + String.valueOf(arr.size()));
        if (arr.get(position) == null) {
            Log.v("UPDATE5", "NULL");
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
        Log.v("UPDATE1", String.valueOf(cardTexts.size()) + " " + String.valueOf(arr.size()));
        notifyDataSetChanged();
    }
}
