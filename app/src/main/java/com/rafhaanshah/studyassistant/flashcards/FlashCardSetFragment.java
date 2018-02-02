package com.rafhaanshah.studyassistant.flashcards;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.HelperUtils;
import com.rafhaanshah.studyassistant.R;

public class FlashCardSetFragment extends Fragment {


    private String card, answer;
    private int position, colour;
    private boolean cardFlipped;
    private CardFragment currentFragment;

    public static FlashCardSetFragment newInstance(String cardText, String answerText, int pos) {
        FlashCardSetFragment fcs = new FlashCardSetFragment();
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
        getColour();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_flash_card_stack, container, false);

        currentFragment = CardFragment.newInstance(card, colour);
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                .add(R.id.container, currentFragment)
                .commit();

        return inflatedView;
    }

    private void getColour() {
        switch (position % 5) {
            case 0:
                colour = ContextCompat.getColor(getContext(), R.color.materialRed);
                break;
            case 1:
                colour = ContextCompat.getColor(getContext(), R.color.materialBlue);
                break;
            case 2:
                colour = ContextCompat.getColor(getContext(), R.color.materialOrange);
                break;
            case 3:
                colour = ContextCompat.getColor(getContext(), R.color.materialPurple);
                break;
            case 4:
                colour = ContextCompat.getColor(getContext(), R.color.materialGreen);
                break;
        }
    }

    public boolean isCardFlipped() {
        return cardFlipped;
    }

    public boolean isEditing() {
        return currentFragment.isEditing();
    }

    public String getText() {
        return currentFragment.getText();
    }

    public void setText(String text) {
        if (!cardFlipped) {
            answer = text;
        } else {
            card = text;
        }
    }

    public void editCard() {
        currentFragment.editCard();
    }

    public void flipCard() {
        Fragment newFragment;
        if (cardFlipped) {
            newFragment = CardFragment.newInstance(card, colour);
        } else {
            newFragment = CardFragment.newInstance(answer, HelperUtils.darkenColor(colour, 0.2));
        }

        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                .replace(R.id.container, newFragment)
                .commit();

        cardFlipped = !cardFlipped;
        currentFragment = (CardFragment) newFragment;
    }

    public static class CardFragment extends Fragment {

        private String text;
        private TextView textView, editText;
        private boolean editing;
        private int colour;
        private CardView card;

        public static CardFragment newInstance(String str, int col) {
            CardFragment frag = new CardFragment();
            Bundle bundle = new Bundle(2);
            bundle.putString("text", str);
            bundle.putInt("col", col);
            frag.setArguments(bundle);
            return frag;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            text = getArguments().getString("text");
            colour = getArguments().getInt("col");
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View inflatedView = inflater.inflate(R.layout.fragment_flash_card, container, false);
            inflatedView.setCameraDistance(getResources().getDisplayMetrics().density * 10000);

            return inflatedView;
        }

        @Override
        public void onDestroy() {
            FlashCardSetFragment frag = (FlashCardSetFragment) getParentFragment();
            frag.setText(editText.getText().toString().trim());
            super.onDestroy();
        }

        @Override
        public void onViewCreated(@NonNull View inflatedView, Bundle savedInstanceState) {
            super.onViewCreated(inflatedView, savedInstanceState);

            textView = inflatedView.findViewById(R.id.text);
            editText = inflatedView.findViewById(R.id.edit);
            card = inflatedView.findViewById(R.id.cardView);

            textView.setText(text);
            editText.setText(text);
            editText.setSelectAllOnFocus(true);
            card.setCardBackgroundColor(colour);

            if (TextUtils.isEmpty(text)) {
                editCard();
            }
        }

        private void editCard() {
            if (!editing) {
                editing = true;
                textView.setVisibility(View.INVISIBLE);
                editText.setVisibility(View.VISIBLE);
                editText.requestFocus();
            } else {
                editing = false;
                textView.setVisibility(View.VISIBLE);
                editText.setVisibility(View.INVISIBLE);

            }
        }

        private boolean isEditing() {
            return editing;
        }

        private String getText() {
            return editText.getText().toString().trim();
        }
    }
}
