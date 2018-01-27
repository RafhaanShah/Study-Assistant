package com.rafhaanshah.studyassistant.flashcards;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;


public class FlashCardStackFragment extends Fragment {


    private String card, answer;
    private TextView cardTextView, answerTextView, cardEditText, answerEditText;
    private Button button;
    private int position;
    private boolean editing;

    public static FlashCardStackFragment newInstance(String cardText, String answerText, int pos) {
        FlashCardStackFragment fcs = new FlashCardStackFragment();
        Bundle bundle = new Bundle(2);
        bundle.putString("cardText", cardText);
        bundle.putString("answerText", answerText);
        bundle.putInt("position", pos);
        fcs.setArguments(bundle);
        return fcs;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        card = getArguments().getString("cardText");
        answer = getArguments().getString("answerText");
        position = getArguments().getInt("position");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_flash_card_stack, container, false);

        cardTextView = inflatedView.findViewById(R.id.cardText);
        answerTextView = inflatedView.findViewById(R.id.answerText);
        cardEditText = inflatedView.findViewById(R.id.cardEdit);
        answerEditText = inflatedView.findViewById(R.id.answerEdit);
        button = inflatedView.findViewById(R.id.cardButton);

        cardTextView.setText(card);
        answerTextView.setText(answer);
        cardEditText.setText(card);
        answerEditText.setText(answer);

        if (answer.equals("")) {
            button.setVisibility(View.INVISIBLE);
        }

        int color = 0;
        switch (position % 5) {
            case 0:
                color = ContextCompat.getColor(getContext(), R.color.scheduleRed);
                break;
            case 1:
                color = ContextCompat.getColor(getContext(), R.color.scheduleBlue);
                break;
            case 2:
                color = ContextCompat.getColor(getContext(), R.color.scheduleOrange);
                break;
            case 3:
                color = ContextCompat.getColor(getContext(), R.color.materialPurple);
                break;
            case 4:
                color = ContextCompat.getColor(getContext(), R.color.scheduleGreen);
                break;
        }

        inflatedView.findViewById(R.id.relativeLayout).setBackgroundColor(color);
        if (card.equals("")) {
            editCard();
        }
        return inflatedView;
    }

    public String getCardText() {
        return cardEditText.getText().toString();
    }

    public String getAnswerText() {
        return answerEditText.getText().toString();
    }

    public void revealAnswer() {
        answerTextView.setVisibility(View.VISIBLE);
        button.setVisibility(View.INVISIBLE);
    }

    public void editCard() {
        editing = true;
        button.setText(R.string.save);
        button.setVisibility(View.VISIBLE);
        cardTextView.setVisibility(View.INVISIBLE);
        answerTextView.setVisibility(View.INVISIBLE);
        cardEditText.setVisibility(View.VISIBLE);
        answerEditText.setVisibility(View.VISIBLE);
        cardEditText.requestFocus();
    }

    public boolean isEditing() {
        return editing;
    }
}
