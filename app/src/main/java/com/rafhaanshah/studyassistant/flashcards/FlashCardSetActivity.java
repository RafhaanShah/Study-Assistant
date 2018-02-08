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

    FlashCardSet flashCardSet;
    private ViewPager viewPager;
    private FlashCardSetAdapter flashCardSetAdapter;
    private Realm realm;
    private int lastPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_card);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getIntent().getStringExtra("flashCardSet");

        realm = Realm.getDefaultInstance();
        flashCardSet = realm.where(FlashCardSet.class).equalTo("title", title).findFirst();

        viewPager = findViewById(R.id.view_pager);
        flashCardSetAdapter = new FlashCardSetAdapter(getSupportFragmentManager(), flashCardSet);

        //viewPager.setPageTransformer(true, new FlashCardStackTransformer());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(flashCardSetAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (flashCardSetAdapter.getFragment(lastPage) != null) {
                    saveFlashCard(flashCardSetAdapter.getFragment(lastPage), lastPage);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_flash_card_set, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_btn_edit_flash_card:
                editFlashCard();
                return true;
            case R.id.menu_btn_delete_flash_card:
                deleteFlashCard();
                return true;
            case R.id.menu_btn_add_flash_card:
                addFlashCard();
                return true;
            case R.id.menu_btn_search_flash_card:
                jumpToFlashCard();
                return true;
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onDestroy() {
        if (viewPager.getAdapter() != null) {
            saveFlashCard(getFragment(), viewPager.getCurrentItem());
        }
        super.onDestroy();
        realm.close();
    }

    private void updateTitle() {
        setTitle(getString(R.string.card) + " " + String.valueOf(viewPager.getCurrentItem() + 1) + "/" + String.valueOf(flashCardSet.getCards().size()));
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
            public void onClick(View view) {
                String text = input.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(FlashCardSetActivity.this, FlashCardSetActivity.this.getString(R.string.error_blank), Toast.LENGTH_LONG).show();
                    input.setText("");
                } else {
                    if (Integer.valueOf(text) <= flashCardSet.getCards().size() && Integer.valueOf(text) > 0) {
                        viewPager.setCurrentItem(Integer.valueOf(text) - 1, true);
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
        if (flashCardSet.getCards().size() > 99) {
            Toast.makeText(getApplicationContext(), getString(R.string.max_cards), Toast.LENGTH_SHORT).show();
            return;
        }
        saveFlashCard(getFragment(), viewPager.getCurrentItem());
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                flashCardSet.getCards().add(viewPager.getCurrentItem() + 1, "");
                flashCardSet.getAnswers().add(viewPager.getCurrentItem() + 1, "");
            }
        });
        flashCardSetAdapter.addFragment(viewPager.getCurrentItem() + 1);
        flashCardSetAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        HelperUtils.showSoftKeyboard(FlashCardSetActivity.this);
    }

    private void deleteFlashCard() {
        final int current = viewPager.getCurrentItem();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                if (flashCardSet.getCards().size() == 1) {
                    viewPager.setAdapter(null);
                    flashCardSet.deleteFromRealm();
                } else {
                    flashCardSet.getCards().remove(current);
                    flashCardSet.getAnswers().remove(current);
                }
            }
        });
        if (viewPager.getAdapter() == null) {
            finish();
        } else {
            flashCardSetAdapter.removeFragment(current);
            flashCardSetAdapter.notifyDataSetChanged();
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
        if (pos < flashCardSet.getCards().size() && !text.equals(flashCardSet.getCards().get(pos))) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    flashCardSet.getCards().set(pos, text);
                }
            });
        }
    }

    private void saveAnswerText(final int pos, final String text) {
        if (pos < flashCardSet.getAnswers().size() && !text.equals(flashCardSet.getAnswers().get(pos))) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    flashCardSet.getAnswers().set(pos, text);
                }
            });
        }
    }

    public void cardPressed(View view) {
        saveFlashCard(getFragment(), viewPager.getCurrentItem());
        getFragment().setText();
        getFragment().flipCard();
        if (getFragment().isEditing()) {
            HelperUtils.showSoftKeyboard(FlashCardSetActivity.this);
        }
    }

    private FlashCardSetFragment getFragment() {
        return flashCardSetAdapter.getFragment(viewPager.getCurrentItem());
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
