package com.rafhaanshah.studyassistant.flashcards;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;

import io.realm.RealmList;

public class FlashCardStackAdapter extends FragmentStatePagerAdapter {

    private RealmList<String> cardTexts;
    private RealmList<String> answerTexts;
    private SparseArray<FlashCardStackFragment> arr;

    public FlashCardStackAdapter(FragmentManager fm, FlashCardSet set) {
        super(fm);
        cardTexts = set.getCards();
        answerTexts = set.getAnswers();
        Log.v("CREATED", "ADAPTER" + String.valueOf(cardTexts.size()));
        arr = new SparseArray<>(cardTexts.size());
        for (int i = 0; i < cardTexts.size(); i++) {
            arr.put(0, null);
        }
    }

    @Override
    public Fragment getItem(int position) {
        FlashCardStackFragment frag = FlashCardStackFragment.newInstance(cardTexts.get(position), answerTexts.get(position));
        Log.v("CREATED", String.valueOf(position));
        arr.setValueAt(position, frag);
        return frag;
    }

    public int getItemPosition(Object item) {
//        FlashCardStackFragment fragment = (FlashCardStackFragment) item;
//        int position = Arrays.asList(arr).indexOf(fragment);
//        if (position >= 0) {
//            return position;
//        } else {
//            return POSITION_NONE;
//        }
        return POSITION_NONE;
    }

    public Fragment getFragment(int position) {
        return arr.get(position);
    }

    @Override
    public int getCount() {
        return cardTexts.size();
    }

    public void updateData(RealmList<String> cards, RealmList<String> answers) {
        cardTexts = cards;
        answerTexts = answers;
        notifyDataSetChanged();
    }

}
