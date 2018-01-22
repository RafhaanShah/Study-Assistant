package com.rafhaanshah.studyassistant.flashcards;

import io.realm.RealmList;
import io.realm.RealmObject;

public class FlashCardSet extends RealmObject {

    private String title;
    private RealmList<String> cards;
    private RealmList<String> answers;

    public RealmList<String> getAnswers() {
        return answers;
    }

    public void setAnswers(RealmList<String> answers) {
        this.answers = answers;
    }

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
}
