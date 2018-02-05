package com.rafhaanshah.studyassistant.flashcards;

import io.realm.RealmList;
import io.realm.RealmObject;

public class FlashCardSet extends RealmObject {

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
