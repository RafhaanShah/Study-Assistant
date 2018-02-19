package com.rafhaanshah.studyassistant.flashcards;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class FlashCardSet extends RealmObject {

    public static final String FlashCardSet_TITLE = "title";
    public static final String FlashCardSet_CARDS = "cards";
    public static final String FlashCardSet_ANSWERS = "answers";

    @Required
    private String title;
    private RealmList<String> cards;
    private RealmList<String> answers;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RealmList<String> getCards() {
        return cards;
    }

    public void setCards(RealmList<String> cards) {
        this.cards = cards;
    }

    public RealmList<String> getAnswers() {
        return answers;
    }

    public void setAnswers(RealmList<String> answers) {
        this.answers = answers;
    }

}
