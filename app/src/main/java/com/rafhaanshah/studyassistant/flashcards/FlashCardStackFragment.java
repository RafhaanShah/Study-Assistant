package com.rafhaanshah.studyassistant.flashcards;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;

public class FlashCardStackFragment extends Fragment {


    private String card, answer;
    private int position, colour;
    private boolean cardFlipped;
    private Fragment currentFragment;

    public static FlashCardStackFragment newInstance(String cardText, String answerText, int pos) {
        Log.v("CARDS", "frag instance");
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
        Log.v("CARDS", "frag oncreate");
        card = getArguments().getString("cardText");
        answer = getArguments().getString("answerText");
        position = getArguments().getInt("position");
        getColour();
        if (savedInstanceState == null) {
            currentFragment = CardFrontFragment.newInstance(card, colour);
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, currentFragment)
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v("CARDS", "frag create view");
        View inflatedView = inflater.inflate(R.layout.fragment_flash_card_stack, container, false);

        //TODO: fix horizontal layout
        /*
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


        inflatedView.findViewById(R.id.relativeLayout).setBackgroundColor(color);
        if (card.equals("")) {
            editCard();
        }
        */

        return inflatedView;
    }

    public void getColour() {
        switch (position % 5) {
            case 0:
                colour = ContextCompat.getColor(getContext(), R.color.scheduleRed);
                break;
            case 1:
                colour = ContextCompat.getColor(getContext(), R.color.scheduleBlue);
                break;
            case 2:
                colour = ContextCompat.getColor(getContext(), R.color.scheduleOrange);
                break;
            case 3:
                colour = ContextCompat.getColor(getContext(), R.color.materialPurple);
                break;
            case 4:
                colour = ContextCompat.getColor(getContext(), R.color.scheduleGreen);
                break;
        }
    }

    public boolean isCardFlipped() {
        return cardFlipped;
    }

    public String getText() {
        if (!cardFlipped) {
            CardFrontFragment frag = (CardFrontFragment) currentFragment;
            return frag.getText();
        } else {
            CardBackFragment frag = (CardBackFragment) currentFragment;
            return frag.getText();
        }
    }

    public void editCard() {
        if (!cardFlipped) {
            CardFrontFragment frag = (CardFrontFragment) currentFragment;
            frag.editCard();
        } else {
            CardBackFragment frag = (CardBackFragment) currentFragment;
            frag.editCard();
        }
    }

    public boolean isEditing() {
        if (!cardFlipped) {
            CardFrontFragment frag = (CardFrontFragment) currentFragment;
            return frag.isEditing();
        } else {
            CardBackFragment frag = (CardBackFragment) currentFragment;
            return frag.isEditing();
        }
    }

    public void flipCard() {
        Fragment newFragment;
        if (cardFlipped) {
            newFragment = CardFrontFragment.newInstance(card, colour);
        } else {
            newFragment = CardBackFragment.newInstance(answer, colour);
        }

        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                .replace(R.id.container, newFragment)
                .commit();

        cardFlipped = !cardFlipped;
        currentFragment = newFragment;
    }

    public static class CardFrontFragment extends Fragment {

        private String text;
        private TextView textView, editText;
        private Button button;
        private boolean editing;
        private int colour;
        private CardView card;

        public static CardFrontFragment newInstance(String str, int col) {
            Log.v("CARDS", "front instance");
            CardFrontFragment frag = new CardFrontFragment();
            Bundle bundle = new Bundle(2);
            bundle.putString("text", str);
            bundle.putInt("col", col);
            frag.setArguments(bundle);
            return frag;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.v("CARDS", "front oncreate");
            super.onCreate(savedInstanceState);
            text = getArguments().getString("text");
            colour = getArguments().getInt("col");
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.v("CARDS", "front create view");
            View inflatedView = inflater.inflate(R.layout.fragment_card_front, container, false);
            inflatedView.setCameraDistance(getResources().getDisplayMetrics().density * 10000);

            return inflatedView;
        }

        @Override
        public void onViewCreated(View inflatedView, Bundle savedInstanceState) {
            super.onViewCreated(inflatedView, savedInstanceState);
            Log.v("CARDS", "front view created");

            textView = inflatedView.findViewById(R.id.text);
            editText = inflatedView.findViewById(R.id.edit);
            button = inflatedView.findViewById(R.id.cardButton);
            card = inflatedView.findViewById(R.id.cardView);

            textView.setText(text);
            editText.setText(text);
            card.setCardBackgroundColor(colour);
        }

        public void editCard() {
            editing = true;
            card.setClickable(false);
            button.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.VISIBLE);
            editText.requestFocus();
        }

        public boolean isEditing() {
            return editing;
        }

        public String getText() {
            return editText.getText().toString().trim();
        }
    }

    public static class CardBackFragment extends Fragment {

        private String text;
        private TextView textView, editText;
        private Button button;
        private boolean editing;
        private int colour;
        private CardView card;

        public static CardBackFragment newInstance(String str, int col) {
            Log.v("CARDS", "back instance");
            CardBackFragment frag = new CardBackFragment();
            Bundle bundle = new Bundle(2);
            bundle.putString("text", str);
            bundle.putInt("col", col);
            frag.setArguments(bundle);
            return frag;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            Log.v("CARDS", "back oncreate");
            super.onCreate(savedInstanceState);
            text = getArguments().getString("text");
            colour = getArguments().getInt("col");
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.v("CARDS", "back create view");
            View inflatedView = inflater.inflate(R.layout.fragment_card_back, container, false);
            inflatedView.setCameraDistance(getResources().getDisplayMetrics().density * 10000);

            return inflatedView;
        }

        @Override
        public void onViewCreated(View inflatedView, Bundle savedInstanceState) {
            super.onViewCreated(inflatedView, savedInstanceState);
            Log.v("CARDS", "back view created");


            textView = inflatedView.findViewById(R.id.text);
            editText = inflatedView.findViewById(R.id.edit);
            button = inflatedView.findViewById(R.id.cardButton);
            card = inflatedView.findViewById(R.id.cardView);

            textView.setText(text);
            editText.setText(text);
            card.setCardBackgroundColor(Color.YELLOW);
        }

        public void editCard() {
            editing = true;
            card.setClickable(false);
            button.setVisibility(View.VISIBLE);
            textView.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.VISIBLE);
            editText.requestFocus();
        }

        public String getText() {
            return editText.getText().toString().trim();
        }

        public boolean isEditing() {
            return editing;
        }
    }
}
