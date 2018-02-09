package com.rafhaanshah.studyassistant.flashcards;

import io.realm.RealmList;
import io.realm.RealmObject;

public class FlashCardSet extends RealmObject {

    static final String FlashCardSet_TITLE = "title";
    static final String FlashCardSet_CARDS = "cards";
    static final String FlashCardSet_ANSWERS = "answers";

    private String title;
    private RealmList<String> cards;
    private RealmList<String> answers;

    RealmList<String> getAnswers() {
        return answers;
    }

    void setAnswers(RealmList<String> answers) {
        this.answers = answers;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    RealmList<String> getCards() {
        return cards;
    }

    void setCards(RealmList<String> cards) {
        this.cards = cards;
    }
}
