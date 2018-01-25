package com.rafhaanshah.studyassistant.flashcards;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import io.realm.RealmList;

public class FlashCardStackAdapter extends FragmentStatePagerAdapter {

    private RealmList<String> cardTexts;
    private RealmList<String> answerTexts;
    private SparseArray<FlashCardStackFragment> arr;

    public FlashCardStackAdapter(FragmentManager fm, FlashCardSet set) {
        super(fm);
        cardTexts = set.getCards();
        answerTexts = set.getAnswers();
        Log.v("PAGER", "ADAPTER " + String.valueOf(cardTexts.size()));
        arr = new SparseArray<>(cardTexts.size());
        for (int i = 0; i < cardTexts.size(); i++) {
            arr.put(i, null);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FlashCardStackFragment frag = (FlashCardStackFragment) super.instantiateItem(container, position);
        Log.v("PAGER NEW", String.valueOf(position));
        arr.setValueAt(position, frag);
        return frag;
    }

    @Override
    public Fragment getItem(int position) {
        FlashCardStackFragment frag = FlashCardStackFragment.newInstance(cardTexts.get(position), answerTexts.get(position));
        Log.v("PAGER OLD", String.valueOf(position));
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
        Log.v("PAGER", "ADAPTER GET" + String.valueOf(position));
        Log.v("PAGER", "ADAPTER SIZE" + String.valueOf(arr.size()));
        if (arr.get(position) == null)
            Log.v("PAGER", "ADAPTER FRAG NULL");
        Log.v("PAGER", "ADAPTER FRAG" + String.valueOf(position));
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
