package com.rafhaanshah.studyassistant.flashcards;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;


public class FlashCardStackFragment extends Fragment {


    private String card, answer;
    private TextView cardTextView, answerTextView;

    public static FlashCardStackFragment newInstance(String cardText, String answerText) {
        FlashCardStackFragment fcs = new FlashCardStackFragment();
        Bundle bundle = new Bundle(2);
        bundle.putString("cardText", cardText);
        bundle.putString("answerText", answerText);
        fcs.setArguments(bundle);
        return fcs;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        card = getArguments().getString("cardText");
        answer = getArguments().getString("answerText");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_flash_card_stack, container, false);

        cardTextView = inflatedView.findViewById(R.id.cardText);
        answerTextView = inflatedView.findViewById(R.id.answerText);
        cardTextView.setText(card);
        answerTextView.setText(answer);

        return inflatedView;
    }

    public String getCardText() {
        return cardTextView.getText().toString();
    }

    public String getAnswerText() {
        return answerTextView.getText().toString();
    }
}
