package com.rafhaanshah.studyassistant.flashcards;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import io.realm.RealmList;

public class FlashCardStackAdapter extends FragmentStatePagerAdapter {

    private RealmList<String> cardTexts;
    private RealmList<String> answerTexts;

    public FlashCardStackAdapter(FragmentManager fm, FlashCardSet set) {
        super(fm);

        cardTexts = set.getCards();
        answerTexts = set.getAnswers();

    }

    @Override
    public Fragment getItem(int position) {
        return FlashCardStackFragment.newInstance(cardTexts.get(position), answerTexts.get(position));
    }

    @Override
    public int getCount() {
        return cardTexts.size();
    }
}
