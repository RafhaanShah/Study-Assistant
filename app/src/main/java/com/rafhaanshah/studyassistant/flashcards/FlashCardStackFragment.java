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


    private String title;

    public static FlashCardStackFragment newInstance(String title) {
        FlashCardStackFragment fcs = new FlashCardStackFragment();
        Bundle bndl = new Bundle(1);
        bndl.putString("title", title);
        fcs.setArguments(bndl);
        return fcs;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getArguments().getString("title");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_flash_card_stack, container, false);
        TextView tv = inflatedView.findViewById(R.id.textView);
        tv.setText(title);

        return inflatedView;
    }
}
