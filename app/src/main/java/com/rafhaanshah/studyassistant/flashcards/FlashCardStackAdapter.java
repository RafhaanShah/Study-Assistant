package com.rafhaanshah.studyassistant.flashcards;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import io.realm.RealmList;

public class FlashCardStackAdapter extends FragmentStatePagerAdapter {

    private RealmList<String> cardTexts;
    private RealmList<String> answerTexts;
    private SparseArray<FlashCardStackFragment> arr;
    private FlashCardSet item;
    private FragmentManager manager;

    public FlashCardStackAdapter(FragmentManager fm, FlashCardSet set) {
        super(fm);
        manager = fm;
        item = set;
        cardTexts = set.getCards();
        answerTexts = set.getAnswers();
        arr = new SparseArray<>(cardTexts.size());
        for (int i = 0; i < cardTexts.size() + 1; i++) {
            arr.put(i, null);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        FlashCardStackFragment frag = (FlashCardStackFragment) super.instantiateItem(container, position);
        arr.setValueAt(position, frag);
        return frag;
    }

    @Override
    public Fragment getItem(int position) {
        FlashCardStackFragment frag = FlashCardStackFragment.newInstance(cardTexts.get(position), answerTexts.get(position), position);
        //arr.setValueAt(position, frag);
        return frag;
    }

    @Override
    public int getItemPosition(Object item) {
        return POSITION_NONE;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }


    public Fragment getFragment(int position) {
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

    public void updateData() {
        cardTexts = item.getCards();
        answerTexts = item.getAnswers();
        notifyDataSetChanged();
    }
}
