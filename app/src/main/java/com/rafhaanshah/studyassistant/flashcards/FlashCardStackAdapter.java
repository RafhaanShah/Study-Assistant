package com.rafhaanshah.studyassistant.flashcards;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import io.realm.RealmList;

public class FlashCardStackAdapter extends FragmentStatePagerAdapter {

    private RealmList<String> cardTexts;
    private RealmList<String> answerTexts;
    private FlashCardStackFragment[] arr;

    public FlashCardStackAdapter(FragmentManager fm, FlashCardSet set) {
        super(fm);

        cardTexts = set.getCards();
        answerTexts = set.getAnswers();
        arr = new FlashCardStackFragment[cardTexts.size()];
    }

    @Override
    public Fragment getItem(int position) {
        FlashCardStackFragment frag = FlashCardStackFragment.newInstance(cardTexts.get(position), answerTexts.get(position));
        arr[position] = frag;
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
        return arr[position];
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
