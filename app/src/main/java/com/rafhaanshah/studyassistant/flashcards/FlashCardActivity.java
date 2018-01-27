package com.rafhaanshah.studyassistant.flashcards;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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
                editFlashCard();
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
        total += 1;
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                item.getCards().add(current + 1, "");
                item.getAnswers().add(current + 1, "");
            }
        });
        mAdapter = new FlashCardStackAdapter(getSupportFragmentManager(), item);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(current + 1, true);
        editFlashCard();
    }

    private void deleteFlashCard() {
        total -= 1;
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                if (total == 0) {
                    item.deleteFromRealm();
                    mPager.setAdapter(null);
                } else {
                    item.getCards().remove(current);
                    item.getAnswers().remove(current);
                }
            }
        });
        if (mPager.getAdapter() == null) {
            finish();
        } else {
            if (!(current == 0)) {
                current -= 1;
            }
            updateTitle();
            mAdapter = new FlashCardStackAdapter(getSupportFragmentManager(), item);
            mPager.setAdapter(mAdapter);
            mPager.setCurrentItem(current, true);
        }
    }

    private void editFlashCard() {
        FlashCardStackFragment frag = (FlashCardStackFragment) mAdapter.getFragment(mPager.getCurrentItem());
        frag.editCard();
    }

    public void buttonPressed(View v) {
        final int pos = mPager.getCurrentItem();
        FlashCardStackFragment frag = (FlashCardStackFragment) mAdapter.getFragment(pos);

        if (frag.isEditing()) {
            final String newCard = frag.getCardText();
            final String newAns = frag.getAnswerText();

            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    item.getCards().set(pos, newCard);
                    item.getAnswers().set(pos, newAns);
                }
            });

            mAdapter.updateData();
        } else {
            frag.revealAnswer();
        }
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
