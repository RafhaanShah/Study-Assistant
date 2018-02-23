package com.rafhaanshah.studyassistant;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.flashcards.FlashCardSetListFragment;
import com.rafhaanshah.studyassistant.lecture.LectureListFragment;
import com.rafhaanshah.studyassistant.notifications.Notifier;
import com.rafhaanshah.studyassistant.schedule.ScheduleEvent;
import com.rafhaanshah.studyassistant.schedule.ScheduleEventActivity;
import com.rafhaanshah.studyassistant.schedule.ScheduleEventListFragment;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    public static final String TYPE_APPLICATION_PDF = "application/pdf";
    public static final String PDF = ".pdf";
    public static final int SORT_TITLE = 0;
    public static final int SORT_DATE = 1;
    public static final int SORT_SIZE = 2;
    private static final int REQUEST_LECTURE = 100;
    private static final String PREF_SORTING = "PREF_SORTING";
    private static final String BUNDLE_SCHEDULE_HISTORY = "BUNDLE_SCHEDULE_HISTORY";
    private static boolean active = false;

    private boolean scheduleHistory;
    private int lectureSorting;
    private Menu menu;
    private Fragment selectedFragment;
    private Toolbar toolbar;
    private BottomNavigationView navigation;
    private File directory;
    private SharedPreferences preferences;
    private SearchView searchView;

    public static boolean isActive() {
        return active;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        lectureSorting = preferences.getInt(PREF_SORTING, 0);
        directory = HelperUtils.getLectureDirectory(MainActivity.this);

        navigation = findViewById(R.id.navigation);
        setNavigationListener(navigation);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            selectedFragment = ScheduleEventListFragment.newInstance(false);
            replaceFragment();
        } else {
            selectedFragment = getSupportFragmentManager().findFragmentById(R.id.content);
            scheduleHistory = savedInstanceState.getBoolean(BUNDLE_SCHEDULE_HISTORY, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (searchView != null && !searchView.isIconified()) {
            searchView.onActionViewCollapsed();
        }
        active = true;
        Notifier.clearAllNotifications(MainActivity.this);
    }

    @Override
    public void onPause() {
        super.onPause();
        active = false;
        if (lectureSorting != preferences.getInt(PREF_SORTING, 0)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREF_SORTING, lectureSorting);
            editor.apply();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_SCHEDULE_HISTORY, scheduleHistory);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu getMenu) {
        menu = getMenu;
        if (selectedFragment.getClass() == ScheduleEventListFragment.class) {
            if (scheduleHistory) {
                swapToolbarMenu(getString(R.string.menu_history), R.menu.fragment_schedule_list);
            } else {
                swapToolbarMenu(getString(R.string.menu_schedule), R.menu.fragment_schedule_list);
            }
        } else if (selectedFragment.getClass() == FlashCardSetListFragment.class) {
            swapToolbarMenu(getString(R.string.menu_flash_cards), R.menu.fragment_flash_card_list);
        } else if (selectedFragment.getClass() == LectureListFragment.class) {
            swapToolbarMenu(getString(R.string.menu_lectures), R.menu.fragment_lecture_list);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_btn_history:
                historyButtonPressed();
                return true;
            case R.id.menu_btn_lecture_sort:
                lectureSortButtonPressed();
                return true;
            case R.id.menu_btn_filter:
                filterButtonPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.onActionViewCollapsed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LECTURE && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData();
            String fileName = "temporary new file name" + PDF;

            try (Cursor cursor = getContentResolver().query(selectedFile, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }

            if (!fileName.toLowerCase().endsWith(PDF)) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_file), Toast.LENGTH_SHORT).show();
                return;
            }

            if (new File(directory + File.separator + fileName).exists()) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_file_added), Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                InputStream initialStream = getContentResolver().openInputStream(selectedFile);
                byte[] buffer = new byte[initialStream.available()];
                initialStream.read(buffer);
                File targetFile = new File(directory + File.separator + fileName);
                OutputStream outStream = new FileOutputStream(targetFile);
                outStream.write(buffer);
                initialStream.close();
                outStream.close();
            } catch (IOException | NullPointerException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_inaccessible_file), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                LectureListFragment frag = (LectureListFragment) selectedFragment;
                frag.updateData(lectureSorting, true);
            }
        }
    }

    private void setNavigationListener(BottomNavigationView navigation) {
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_schedule:
                        if (selectedFragment.getClass() == ScheduleEventListFragment.class) {
                            ScheduleEventListFragment frag = (ScheduleEventListFragment) selectedFragment;
                            frag.scrollToTop();
                            return true;
                        }
                        selectedFragment = ScheduleEventListFragment.newInstance(false);
                        scheduleHistory = false;
                        swapToolbarMenu(getString(R.string.menu_schedule), R.menu.fragment_schedule_list);
                        break;
                    case R.id.navigation_flash_cards:
                        if (selectedFragment.getClass() == FlashCardSetListFragment.class) {
                            FlashCardSetListFragment frag = (FlashCardSetListFragment) selectedFragment;
                            frag.scrollToTop();
                            return true;
                        }
                        selectedFragment = FlashCardSetListFragment.newInstance();
                        swapToolbarMenu(getString(R.string.menu_flash_cards), R.menu.fragment_flash_card_list);
                        break;
                    case R.id.navigation_lectures:
                        if (selectedFragment.getClass() == LectureListFragment.class) {
                            LectureListFragment frag = (LectureListFragment) selectedFragment;
                            frag.scrollToTop();
                            return true;
                        }
                        selectedFragment = LectureListFragment.newInstance(lectureSorting);
                        swapToolbarMenu(getString(R.string.menu_lectures), R.menu.fragment_lecture_list);
                        break;
                }
                replaceFragment();
                return true;
            }
        });
    }

    private void replaceFragment() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.content, selectedFragment)
                .commit();
    }

    private void setSearchView() {
        final MenuItem searchItem = menu.findItem(R.id.menu_btn_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        int options = searchView.getImeOptions();
        searchView.setImeOptions(options | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (getCurrentFocus() != null) {
                    HelperUtils.hideSoftKeyboard(MainActivity.this, getCurrentFocus());
                    getCurrentFocus().clearFocus();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (selectedFragment.getClass() == ScheduleEventListFragment.class) {
                    ScheduleEventListFragment frag = (ScheduleEventListFragment) selectedFragment;
                    frag.filter(query);
                } else if (selectedFragment.getClass() == FlashCardSetListFragment.class) {
                    FlashCardSetListFragment frag = (FlashCardSetListFragment) selectedFragment;
                    frag.filter(query);
                } else if (selectedFragment.getClass() == LectureListFragment.class) {
                    LectureListFragment frag = (LectureListFragment) selectedFragment;
                    frag.filter(query);
                }
                return false;
            }
        });
    }

    private void historyButtonPressed() {
        if (scheduleHistory) {
            scheduleHistory = false;
            HelperUtils.fadeTextChange(getString(R.string.menu_schedule), (TextView) toolbar.getChildAt(0), getResources().getInteger(R.integer.animation_fade_time));
        } else {
            scheduleHistory = true;
            HelperUtils.fadeTextChange(getString(R.string.menu_history), (TextView) toolbar.getChildAt(0), getResources().getInteger(R.integer.animation_fade_time));
        }
        selectedFragment = ScheduleEventListFragment.newInstance(scheduleHistory);
        replaceFragment();
    }

    private void lectureSortButtonPressed() {
        LectureListFragment lectureListFragment = (LectureListFragment) selectedFragment;
        switch (lectureSorting) {
            case SORT_TITLE:
                lectureListFragment.updateData(SORT_DATE, false);
                lectureSorting = SORT_DATE;
                Toast.makeText(getApplicationContext(), getString(R.string.sort_date), Toast.LENGTH_SHORT).show();
                break;
            case SORT_DATE:
                lectureListFragment.updateData(SORT_SIZE, false);
                lectureSorting = SORT_SIZE;
                Toast.makeText(getApplicationContext(), getString(R.string.sort_size), Toast.LENGTH_SHORT).show();
                break;
            case SORT_SIZE:
                lectureListFragment.updateData(SORT_TITLE, false);
                lectureSorting = SORT_TITLE;
                Toast.makeText(getApplicationContext(), getString(R.string.sort_title), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void filterButtonPressed() {
        final ScheduleEventListFragment frag = (ScheduleEventListFragment) selectedFragment;

        PopupMenu popup = new PopupMenu(MainActivity.this, findViewById(R.id.menu_btn_filter));
        popup.inflate(R.menu.fragment_schedule_list_filter);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.filter_homework:
                        frag.filterType(ScheduleEvent.ScheduleEventType.HOMEWORK);
                        return true;
                    case R.id.filter_coursework:
                        frag.filterType(ScheduleEvent.ScheduleEventType.COURSEWORK);
                        return true;
                    case R.id.filter_test:
                        frag.filterType(ScheduleEvent.ScheduleEventType.TEST);
                        return true;
                    case R.id.filter_exam:
                        frag.filterType(ScheduleEvent.ScheduleEventType.EXAM);
                        return true;
                    case R.id.filter_none:
                        frag.filterType(null);
                }
                return false;
            }
        });
        popup.show();
    }

    public void newScheduleItem(View view) {
        startActivity(ScheduleEventActivity.getStartIntent(MainActivity.this, -1));
        //overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top);
    }

    public void newFlashCardItem(View view) {
        FlashCardSetListFragment flashCardFragment = (FlashCardSetListFragment) selectedFragment;
        flashCardFragment.newFlashCardSet();
    }

    public void newLectureItem(View view) {
        Toast.makeText(getApplicationContext(), getString(R.string.pdf_select), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent()
                .setType(TYPE_APPLICATION_PDF)
                .setAction(Intent.ACTION_GET_CONTENT);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.pdf_select)), REQUEST_LECTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_file_picker), Toast.LENGTH_SHORT).show();
        }
    }

    private void swapToolbarMenu(final String title, final int menuLayout) {
        menu.clear();
        HelperUtils.fadeTextChange(title, (TextView) toolbar.getChildAt(0), getResources().getInteger(R.integer.animation_fade_time));
        getMenuInflater().inflate(menuLayout, menu);
        setSearchView();
    }
}
