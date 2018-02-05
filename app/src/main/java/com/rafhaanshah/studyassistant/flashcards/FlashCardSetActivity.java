package com.rafhaanshah.studyassistant.flashcards;

import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.HelperUtils;
import com.rafhaanshah.studyassistant.R;

import io.realm.Realm;

public class FlashCardSetActivity extends AppCompatActivity {

    FlashCardSet item;
    private ViewPager mPager;
    private FlashCardSetAdapter mAdapter;
    private Realm realm;
    private int lastPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_card);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getIntent().getStringExtra("item");

        realm = Realm.getDefaultInstance();
        item = realm.where(FlashCardSet.class).equalTo("title", title).findFirst();

        mPager = findViewById(R.id.viewPager);
        mAdapter = new FlashCardSetAdapter(getSupportFragmentManager(), item);

        //mPager.setPageTransformer(true, new FlashCardStackTransformer());
        mPager.setOffscreenPageLimit(2);
        mPager.setAdapter(mAdapter);

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (mAdapter.getFragment(lastPage) != null) {
                    saveFlashCard(mAdapter.getFragment(lastPage), lastPage);
                }
                if (getFragment() != null && TextUtils.isEmpty(getFragment().getText()) && getFragment().isEditing()) {
                    HelperUtils.showSoftKeyboard(FlashCardSetActivity.this);
                }
                lastPage = position;
                updateTitle();
            }
        });
        updateTitle();
    }

    private void updateTitle() {
        setTitle(getString(R.string.card) + " " + String.valueOf(mPager.getCurrentItem() + 1) + "/" + String.valueOf(item.getCards().size()));
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
        if (mPager.getAdapter() != null) {
            saveFlashCard(getFragment(), mPager.getCurrentItem());
        }
        super.onDestroy();
        realm.close();
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
        HelperUtils.showSoftKeyboard(FlashCardSetActivity.this);

        final EditText input = new EditText(FlashCardSetActivity.this);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setSelectAllOnFocus(true);

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
                    if (Integer.valueOf(text) <= item.getCards().size() && Integer.valueOf(text) > 0) {
                        mPager.setCurrentItem(Integer.valueOf(text) - 1, true);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(FlashCardSetActivity.this, FlashCardSetActivity.this.getString(R.string.error_out_of_bounds), Toast.LENGTH_LONG).show();
                        input.requestFocus();
                        input.selectAll();
                    }
                }
            }
        });
    }

    private void addFlashCard() {
        if (item.getCards().size() > 99) {
            Toast.makeText(getApplicationContext(), getString(R.string.max_cards), Toast.LENGTH_SHORT).show();
            return;
        }
        saveFlashCard(getFragment(), mPager.getCurrentItem());
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                item.getCards().add(mPager.getCurrentItem() + 1, "");
                item.getAnswers().add(mPager.getCurrentItem() + 1, "");
            }
        });
        mAdapter.addFragment(mPager.getCurrentItem() + 1);
        mAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
        HelperUtils.showSoftKeyboard(FlashCardSetActivity.this);
    }

    private void deleteFlashCard() {
        final int current = mPager.getCurrentItem();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                if (item.getCards().size() == 1) {
                    mPager.setAdapter(null);
                    item.deleteFromRealm();
                } else {
                    item.getCards().remove(current);
                    item.getAnswers().remove(current);
                }
            }
        });
        if (mPager.getAdapter() == null) {
            finish();
        } else {
            mAdapter.removeFragment(current);
            mAdapter.notifyDataSetChanged();
            updateTitle();
        }
    }

    private void editFlashCard() {
        getFragment().editCard();
        if (getFragment().isEditing()) {
            HelperUtils.showSoftKeyboard(FlashCardSetActivity.this);
        }
    }

    private void saveFlashCard(FlashCardSetFragment frag, int pos) {
        if (frag.isCardFlipped()) {
            saveAnswerText(pos, frag.getText());
        } else {
            saveCardText(pos, frag.getText());
        }
        if (frag.isEditing()) {
            frag.editCard();
        }
    }

    private void saveCardText(final int pos, final String text) {
        if (pos < item.getCards().size() && !text.equals(item.getCards().get(pos))) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    item.getCards().set(pos, text);
                }
            });
        }
    }

    private void saveAnswerText(final int pos, final String text) {
        if (pos < item.getAnswers().size() && !text.equals(item.getAnswers().get(pos))) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    item.getAnswers().set(pos, text);
                }
            });
        }
    }

    public void cardPressed(View v) {
        saveFlashCard(getFragment(), mPager.getCurrentItem());
        getFragment().setText();
        getFragment().flipCard();
        if (getFragment().isEditing()) {
            HelperUtils.showSoftKeyboard(FlashCardSetActivity.this);
        }
    }

    private FlashCardSetFragment getFragment() {
        return mAdapter.getFragment(mPager.getCurrentItem());
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
