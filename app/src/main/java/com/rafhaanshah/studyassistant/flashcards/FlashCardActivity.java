package com.rafhaanshah.studyassistant.flashcards;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;
import io.realm.RealmQuery;

public class FlashCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_card);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager mPager = findViewById(R.id.viewPager);

        String title = getIntent().getStringExtra("item");

        RealmQuery query = Realm.getDefaultInstance().where(FlashCardSet.class).equalTo("title", title);
        FlashCardSet item = (FlashCardSet) query.findFirst();

        FlashCardStackAdapter mAdapter = new FlashCardStackAdapter(getSupportFragmentManager(), item);

        mPager.setPageTransformer(true, new FlashCardStackTransformer());

        mPager.setOffscreenPageLimit(10);

        mPager.setAdapter(mAdapter);

        setTitle(title);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
