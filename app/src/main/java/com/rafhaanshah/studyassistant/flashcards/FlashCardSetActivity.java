package com.rafhaanshah.studyassistant.flashcards;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.github.omadahealth.lollipin.lib.PinCompatActivity;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.rafhaanshah.studyassistant.LockScreenActivity;
import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import io.realm.Realm;

public class FlashCardSetActivity extends PinCompatActivity {

    private static final String EXTRA_SET_TITLE = "EXTRA_SET_TITLE";
    private static final String EXTRA_SET_OFFSET = "EXTRA_SET_OFFSET";

    private FlashCardSet flashCardSet;
    private ViewPager viewPager;
    private FlashCardSetAdapter flashCardSetAdapter;
    private Realm realm;
    private int lastPage;
    private AlertDialog dialog;
    private Toolbar toolbar;

    public static Intent getStartIntent(Context context, String title, int offset) {
        Intent intent = new Intent(context, FlashCardSetActivity.class);
        intent.putExtra(EXTRA_SET_TITLE, title);
        intent.putExtra(EXTRA_SET_OFFSET, offset);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_card);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getIntent().getStringExtra(EXTRA_SET_TITLE);
        int offset = getIntent().getIntExtra(EXTRA_SET_OFFSET, 0);

        realm = Realm.getDefaultInstance();
        flashCardSet = realm.where(FlashCardSet.class).equalTo(FlashCardSet.FlashCardSet_TITLE, title).findFirst();

        viewPager = findViewById(R.id.view_pager);
        flashCardSetAdapter = new FlashCardSetAdapter(getSupportFragmentManager(), flashCardSet, offset);

        //viewPager.setPageTransformer(true, new FlashCardStackTransformer());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(flashCardSetAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (flashCardSetAdapter.getFragment(lastPage) != null) {
                    saveFlashCard(flashCardSetAdapter.getFragment(lastPage), lastPage);
                }
                if (getFragment() != null && TextUtils.isEmpty(getFragment().getText()) && !getFragment().isEditing()) {
                    getFragment().editCard();
                }
                lastPage = position;
                updateTitle();
            }
        });
        setTitle(title);
        updateTitle();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getFragment() != null && TextUtils.isEmpty(getFragment().getText())) {
                    getFragment().editCard();
                }
            }
        }, 100);
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
                getFragment().editCard();
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
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        LockManager<LockScreenActivity> lockManager = LockManager.getInstance();
        if (lockManager != null)
            lockManager.getAppLock().setLastActiveMillis();
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (viewPager.getAdapter() != null)
            saveFlashCard(getFragment(), viewPager.getCurrentItem());
        super.onDestroy();
        realm.close();
    }

    private void updateTitle() {
        toolbar.setSubtitle(getString(R.string.card, viewPager.getCurrentItem() + 1, flashCardSet.getCards().size()));
    }

    private void jumpToFlashCard() {
        final EditText input = new EditText(FlashCardSetActivity.this);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
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
        dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = input.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    input.setError(getString(R.string.already_exists));
                } else {
                    if (Integer.valueOf(text) <= flashCardSet.getCards().size() && Integer.valueOf(text) > 0) {
                        viewPager.setCurrentItem(Integer.valueOf(text) - 1, true);
                        dialog.dismiss();
                    } else {
                        input.setError(getString(R.string.error_out_of_bounds));
                        input.requestFocus();
                        input.selectAll();
                    }
                }
            }
        });
        HelperUtils.showSoftKeyboard(FlashCardSetActivity.this, input);
    }

    private void addFlashCard() {
        if (flashCardSet.getCards().size() > 99) {
            Toast.makeText(getApplicationContext(), getString(R.string.max_cards), Toast.LENGTH_LONG).show();
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

    private void saveFlashCard(FlashCardSetFragment frag, int pos) {
        if (getFragment() != null && !TextUtils.isEmpty(getFragment().getText())) {
            if (frag.isCardFlipped()) {
                saveAnswerText(pos, frag.getText());
            } else {
                saveCardText(pos, frag.getText());
            }
            if (frag.isEditing()) {
                frag.editCard();
            }
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getFragment() != null && TextUtils.isEmpty(getFragment().getText())) {
                    getFragment().editCard();
                }
            }
        }, 100);
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
