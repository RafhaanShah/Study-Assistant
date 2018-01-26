package com.rafhaanshah.studyassistant.flashcards;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;
import io.realm.RealmQuery;

public class FlashCardActivity extends AppCompatActivity {

    FlashCardSet item;
    private ViewPager mPager;
    private FlashCardStackAdapter mAdapter;
    private Realm realm;
    private int total, current;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_card);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = getIntent().getStringExtra("item");

        realm = Realm.getDefaultInstance();
        RealmQuery query = realm.where(FlashCardSet.class).equalTo("title", title);
        item = (FlashCardSet) query.findFirst();
        total = item.getCards().size();

        mPager = findViewById(R.id.viewPager);
        mAdapter = new FlashCardStackAdapter(getSupportFragmentManager(), item);

        //mPager.setPageTransformer(true, new FlashCardStackTransformer());
        mPager.setOffscreenPageLimit(10);
        mPager.setAdapter(mAdapter);

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                current = position;
                updateTitle();
            }
        });
        updateTitle();
    }

    private void updateTitle() {
        setTitle(title + " - " + String.valueOf(current + 1) + "/" + String.valueOf(total));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.flash_card_stack_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editFlashCardButton:
                return true;
            case R.id.deleteFlashCardButton:
                deleteFlashCard();
                return true;
            case R.id.addFlashCardButton:
                addFlashCard();
                return true;
        }
        return false;
    }

    private void addFlashCard() {
        Log.v("PAGER", "ADD");
        total += 1;
        current += 1;
        updateTitle();
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                if (current + 1 == total) {
                    item.getCards().add("BLANK");
                    item.getAnswers().add("BLANK");
                } else {
                    item.getCards().add(current + 1, "BLANK");
                    item.getAnswers().add(current + 1, "BLANK");
                }
            }
        });
        mAdapter = new FlashCardStackAdapter(getSupportFragmentManager(), item);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(current, true);
    }

    private void deleteFlashCard() {
        total -= 1;
        updateTitle();
        final boolean[] finish = new boolean[1];
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                if (total == 0) {
                    item.deleteFromRealm();
                    finish[0] = true;
                } else {
                    item.getCards().remove(current);
                    item.getAnswers().remove(current);
                }
            }
        });
        if (finish[0]) {
            finish();
        } else {
            mAdapter = new FlashCardStackAdapter(getSupportFragmentManager(), item);
            mPager.setAdapter(mAdapter);
            mPager.setCurrentItem(current, true);
        }
    }


    public void buttonPressed(View v) {

        final int pos = mPager.getCurrentItem();

        Log.v("PAGER", "ACTIVITY BUTTON " + String.valueOf(pos));

        FlashCardStackFragment frag = (FlashCardStackFragment) mAdapter.getFragment(pos);

        frag.revealAnswer();

        /*
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
        */
    }

    /*private class FlashCardStackTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View page, float position) {
            if (position >= 0) {
                page.setScaleX(0.8f - 0.02f * position);
                page.setScaleY(0.8f);
                page.setTranslationX(-page.getWidth() * position);
                page.setTranslationY(30 * position);
            }

        }
    }*/
}
