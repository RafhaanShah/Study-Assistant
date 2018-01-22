package com.rafhaanshah.studyassistant.flashcards;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.rafhaanshah.studyassistant.R;

public class FlashCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_card);

        ViewPager mPager = findViewById(R.id.viewPager);

        FlashCardStackAdapter mAdapter = new FlashCardStackAdapter(getSupportFragmentManager());

        mPager.setPageTransformer(true, new FlashCardStackTransformer());

        mPager.setOffscreenPageLimit(10);

        mPager.setAdapter(mAdapter);
    }

    private class FlashCardStackTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View page, float position) {
            if (position >= 0) {
                page.setScaleX(0.8f - 0.02f * position);

                page.setScaleY(0.8f);

                page.setTranslationX(-page.getWidth() * position);

                page.setTranslationY(30 * position);
            }

        }
    }
}
