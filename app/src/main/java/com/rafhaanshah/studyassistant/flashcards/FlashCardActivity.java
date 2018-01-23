package com.rafhaanshah.studyassistant.flashcards;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;
import io.realm.RealmQuery;

public class FlashCardActivity extends AppCompatActivity {

    FlashCardSet item;
    private ViewPager mPager;
    private FlashCardStackAdapter mAdapter;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_card);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getIntent().getStringExtra("item");

        realm = Realm.getDefaultInstance();
        RealmQuery query = realm.where(FlashCardSet.class).equalTo("title", title);
        item = (FlashCardSet) query.findFirst();

        mPager = findViewById(R.id.viewPager);
        mAdapter = new FlashCardStackAdapter(getSupportFragmentManager(), item);

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

    public void saveButton(View v) {
        final int pos = mPager.getCurrentItem();

        FlashCardStackFragment frag = (FlashCardStackFragment) mAdapter.getFragment(pos);
        Toast.makeText(getApplicationContext(), frag.getCardText(), Toast.LENGTH_SHORT).show();

        final String newCard = frag.getCardText();
        final String newAns = frag.getAnswerText();

        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                item.getCards().set(pos, newCard);
                item.getAnswers().set(pos, newAns);
            }
        });

        mAdapter.updateData(item.getCards(), item.getAnswers());
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
