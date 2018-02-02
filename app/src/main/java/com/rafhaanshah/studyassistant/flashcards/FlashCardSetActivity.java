package com.rafhaanshah.studyassistant.flashcards;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;
import io.realm.RealmQuery;

public class FlashCardSetActivity extends AppCompatActivity {

    FlashCardSet item;
    private ViewPager mPager;
    private FlashCardSetAdapter mAdapter;
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
        mAdapter = new FlashCardSetAdapter(getSupportFragmentManager(), item);

        //mPager.setPageTransformer(true, new FlashCardStackTransformer());
        mPager.setOffscreenPageLimit(5);
        mPager.setAdapter(mAdapter);

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                /* TODO: Fix this
                FlashCardSetFragment frag = mAdapter.getFragment(current);
                if (frag.isEditing()) {
                    save(frag, current);
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(mPager.getWindowToken(), 0);
                */
                current = position;
                updateTitle();
            }
        });
        updateTitle();
    }

    private void updateTitle() {
        //setTitle(title + " - " + String.valueOf(current + 1) + "/" + String.valueOf(total));
        setTitle(getString(R.string.card) + " " + String.valueOf(current + 1) + "/" + String.valueOf(total));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.flash_card_set_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onDestroy() {
        FlashCardSetFragment frag = getFrag();
        if (frag.isEditing()) {
            save(frag, mPager.getCurrentItem());
        }
        super.onDestroy();
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
            case R.id.searchFlashCardButton:
                jumpToFlashCard();
                return true;
        }
        return false;
    }

    private void jumpToFlashCard() {
        final InputMethodManager imm = (InputMethodManager) FlashCardSetActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

        final EditText input = new EditText(FlashCardSetActivity.this);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder builder = new AlertDialog.Builder(FlashCardSetActivity.this);
        builder.setTitle(FlashCardSetActivity.this.getString(R.string.go_to_card));
        builder.setPositiveButton(FlashCardSetActivity.this.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton(FlashCardSetActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.ic_search_black_24dp);
        builder.setView(input);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = input.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(FlashCardSetActivity.this, FlashCardSetActivity.this.getString(R.string.error_blank), Toast.LENGTH_LONG).show();
                    input.setText("");
                } else {
                    if (Integer.valueOf(text) <= total && Integer.valueOf(text) > 0) {
                        mPager.setCurrentItem(Integer.valueOf(text) - 1, true);
                        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(FlashCardSetActivity.this, FlashCardSetActivity.this.getString(R.string.error_out_of_bounds), Toast.LENGTH_LONG).show();
                        input.setText("");
                    }
                }
            }
        });
    }

    private void addFlashCard() {
        if (total > 99) {
            Toast.makeText(FlashCardSetActivity.this, FlashCardSetActivity.this.getString(R.string.max_cards), Toast.LENGTH_LONG).show();
            return;
        }
        final int pos = mPager.getCurrentItem();
        FlashCardSetFragment frag = getFrag();
        if (frag.isEditing()) {
            save(frag, pos);
        }
        total += 1;
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                item.getCards().add(current + 1, "");
                item.getAnswers().add(current + 1, "");
            }
        });
        //mAdapter = new FlashCardStackAdapter(getSupportFragmentManager(), item);
        //mPager.setAdapter(mAdapter);
        mAdapter.updateData();
        mPager.setCurrentItem(current + 1, true);

        //final InputMethodManager imm = (InputMethodManager) FlashCardSetActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        //if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
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
            //mAdapter = new FlashCardStackAdapter(getSupportFragmentManager(), item);
            //mPager.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mPager.setCurrentItem(current, true);
        }
    }

    private void editFlashCard() {
        FlashCardSetFragment frag = getFrag();
        if (frag.isEditing()) {
            save(frag, mPager.getCurrentItem());
        } else {
            frag.editCard();
            //final InputMethodManager imm = (InputMethodManager) FlashCardSetActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            //if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    private void save(FlashCardSetFragment frag, int pos) {
        // TODO: Clean this up
        String text = frag.getText();
        boolean flipped = frag.isCardFlipped();
        if (!TextUtils.isEmpty(text) && !item.getCards().get(pos).equals(text) && !item.getAnswers().get(pos).equals(text)) {
            if (!flipped) {
                saveCard(pos, text);
            } else {
                saveAnswer(pos, text);
            }
            mAdapter.updateData();
            if (flipped) {
                getFrag().flipCard();
            }
        } else {
            frag.editCard();
        }
    }

    private void saveCard(final int pos, final String text) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                item.getCards().set(pos, text);
            }
        });
    }

    private void saveAnswer(final int pos, final String text) {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                item.getAnswers().set(pos, text);
            }
        });
    }

    public void cardPressed(View v) {
        FlashCardSetFragment frag = getFrag();
        if (frag.isEditing()) {
            save(frag, mPager.getCurrentItem());
        }
        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        getFrag().flipCard();
    }

    private FlashCardSetFragment getFrag() {
        final int pos = mPager.getCurrentItem();
        return mAdapter.getFragment(pos);
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
