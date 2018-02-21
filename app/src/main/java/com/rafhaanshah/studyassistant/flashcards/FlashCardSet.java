package com.rafhaanshah.studyassistant.flashcards;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class FlashCardSet extends RealmObject {

    public static final String FlashCardSet_TITLE = "TITLE";
    public static final String FlashCardSet_CARDS = "CARDS";
    public static final String FlashCardSet_ANSWERS = "ANSWERS";

    @Required
    private String TITLE;
    private RealmList<String> CARDS;
    private RealmList<String> ANSWERS;

    public String getTitle() {
        return TITLE;
    }

    public void setTitle(String title) {
        this.TITLE = title;
    }

    public RealmList<String> getCards() {
        return CARDS;
    }

    public void setCards(RealmList<String> cards) {
        this.CARDS = cards;
    }

    public RealmList<String> getAnswers() {
        return ANSWERS;
    }

    public void setAnswers(RealmList<String> answers) {
        this.ANSWERS = answers;
    }

}
