package com.rafhaanshah.studyassistant.flashcards;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

public class FlashCardSetFragment extends Fragment {

    private static final String BUNDLE_CARD_TEXT = "BUNDLE_CARD_TEXT";
    private static final String BUNDLE_ANSWER_TEXT = "BUNDLE_ANSWER_TEXT";
    private static final String BUNDLE_POSITION = "BUNDLE_POSITION";
    private static final String BUNDLE_OFFSET = "BUNDLE_OFFSET";

    private String cardText, answerText;
    private int colour, position, offset;
    private boolean cardFlipped;
    private CardFragment currentFragment;

    static FlashCardSetFragment newInstance(String card, String answer, int pos, int offset) {
        FlashCardSetFragment flashCardSetFragment = new FlashCardSetFragment();
        Bundle bundle = new Bundle(2);
        bundle.putString(BUNDLE_CARD_TEXT, card);
        bundle.putString(BUNDLE_ANSWER_TEXT, answer);
        bundle.putInt(BUNDLE_POSITION, pos);
        bundle.putInt(BUNDLE_OFFSET, offset);
        flashCardSetFragment.setArguments(bundle);
        return flashCardSetFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cardText = getArguments().getString(BUNDLE_CARD_TEXT);
        answerText = getArguments().getString(BUNDLE_ANSWER_TEXT);
        position = getArguments().getInt(BUNDLE_POSITION);
        offset = getArguments().getInt(BUNDLE_OFFSET);
        colour = HelperUtils.getColour(getContext(), position + offset);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_flash_card_set, container, false);

        currentFragment = CardFragment.newInstance(cardText, colour);
        getChildFragmentManager()
                .beginTransaction()
                .add(R.id.container, currentFragment)
                .commit();

        return inflatedView;
    }

    int getPosition() {
        return position;
    }

    boolean isCardFlipped() {
        return cardFlipped;
    }

    boolean isEditing() {
        return currentFragment.isEditing();
    }

    String getText() {
        return currentFragment.getText();
    }

    void setText() {
        if (cardFlipped) {
            answerText = getText();
        } else {
            cardText = getText();
        }
    }

    void editCard() {
        currentFragment.editCard();
    }

    void flipCard() {
        setText();
        Fragment newFragment;
        if (cardFlipped) {
            newFragment = CardFragment.newInstance(cardText, colour);
        } else {
            newFragment = CardFragment.newInstance(answerText, HelperUtils.darkenColor(colour, 0.25));
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

        private static final String BUNDLE_TEXT = "BUNDLE_TEXT";
        private static final String BUNDLE_COLOUR = "BUNDLE_COLOUR";

        private String text;
        private TextView textView, editText;
        private boolean editing;
        private int colour;
        private CardView card;

        private static CardFragment newInstance(String str, int col) {
            CardFragment frag = new CardFragment();
            Bundle bundle = new Bundle(2);
            bundle.putString(BUNDLE_TEXT, str);
            bundle.putInt(BUNDLE_COLOUR, col);
            frag.setArguments(bundle);
            return frag;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            text = getArguments().getString(BUNDLE_TEXT);
            colour = getArguments().getInt(BUNDLE_COLOUR);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View inflatedView = inflater.inflate(R.layout.fragment_flash_card, container, false);
            inflatedView.setCameraDistance(getResources().getDisplayMetrics().density * 10000);
            return inflatedView;
        }

        @Override
        public void onViewCreated(@NonNull View inflatedView, Bundle savedInstanceState) {
            super.onViewCreated(inflatedView, savedInstanceState);

            textView = inflatedView.findViewById(R.id.tv_flash_card);
            editText = inflatedView.findViewById(R.id.et_flash_card);
            card = inflatedView.findViewById(R.id.card_view);

            textView.setText(text);
            editText.setText(text);

            editText.setSelectAllOnFocus(true);
            card.setCardBackgroundColor(colour);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    text = charSequence.toString().trim();
                    textView.setText(text);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean focused) {
                    if (!focused) {
                        HelperUtils.hideSoftKeyboard(getContext(), view);
                    } else {
                        HelperUtils.showSoftKeyboard(getContext(), view);
                    }
                }
            });

        }

        private void editCard() {
            if (textView != null && editText != null) {
                if (!editing) {
                    editing = true;
                    textView.setVisibility(View.GONE);
                    editText.setVisibility(View.VISIBLE);
                    editText.requestFocus();
                } else {
                    editing = false;
                    editText.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);
                }
            }
        }

        private boolean isEditing() {
            return editing;
        }

        private String getText() {
            return text;
        }

    }
}
