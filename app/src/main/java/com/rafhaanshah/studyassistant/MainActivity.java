package com.rafhaanshah.studyassistant;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.omadahealth.lollipin.lib.PinCompatActivity;
import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.rafhaanshah.studyassistant.flashcards.FlashCardSetListFragment;
import com.rafhaanshah.studyassistant.lecture.LectureListFragment;
import com.rafhaanshah.studyassistant.notifications.Notifier;
import com.rafhaanshah.studyassistant.schedule.ScheduleEvent;
import com.rafhaanshah.studyassistant.schedule.ScheduleEventActivity;
import com.rafhaanshah.studyassistant.schedule.ScheduleEventFragment;
import com.rafhaanshah.studyassistant.schedule.ScheduleEventListFragment;
import com.rafhaanshah.studyassistant.utils.HelperUtils;
import com.rafhaanshah.studyassistant.widgets.WidgetProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.rafhaanshah.studyassistant.LockScreenActivity.PREF_ANSWER;
import static com.rafhaanshah.studyassistant.LockScreenActivity.PREF_QUESTION;

public class MainActivity extends PinCompatActivity {

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
    private BroadcastReceiver broadcastReceiver;
    private AlertDialog dialog;

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
        setBroadcastReceiver();

        if (savedInstanceState == null) {
            selectedFragment = ScheduleEventListFragment.newInstance(false);
            replaceFragment();
        } else {
            selectedFragment = getSupportFragmentManager().findFragmentById(R.id.content);
            scheduleHistory = savedInstanceState.getBoolean(BUNDLE_SCHEDULE_HISTORY, false);
        }

        setPassCode();
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
        WidgetProvider.updateWidgets(MainActivity.this);
        active = false;
        if (lectureSorting != preferences.getInt(PREF_SORTING, 0)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREF_SORTING, lectureSorting);
            editor.apply();
        }
        LockManager<LockScreenActivity> lockManager = LockManager.getInstance();
        if (lockManager != null && lockManager.isAppLockEnabled())
            lockManager.getAppLock().setLastActiveMillis();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        LockScreenActivity.closeDialog();
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = null;
        super.onDestroy();
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
                swapToolbarMenu(getString(R.string.menu_history), R.menu.fragment_schedule_list, ContextCompat.getColor(MainActivity.this, R.color.colorPrimary),
                        ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
            } else {
                swapToolbarMenu(getString(R.string.menu_schedule), R.menu.fragment_schedule_list, ContextCompat.getColor(MainActivity.this, R.color.colorPrimary),
                        ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
            }
        } else if (selectedFragment.getClass() == FlashCardSetListFragment.class) {
            swapToolbarMenu(getString(R.string.menu_flash_cards), R.menu.fragment_flash_card_list, ContextCompat.getColor(MainActivity.this, R.color.materialRed),
                    ContextCompat.getColor(MainActivity.this, R.color.materialRedDark));
        } else if (selectedFragment.getClass() == LectureListFragment.class) {
            swapToolbarMenu(getString(R.string.menu_lectures), R.menu.fragment_lecture_list, ContextCompat.getColor(MainActivity.this, R.color.materialGreen),
                    ContextCompat.getColor(MainActivity.this, R.color.materialGreenDark));
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
        // If the pin code has been set, prompt for the security question
        if (requestCode == LockScreenActivity.REQUEST_CODE_ENABLE) {
            setSecurityQuestion(MainActivity.this);
        } else if (requestCode == REQUEST_LECTURE && resultCode == RESULT_OK) {
            // If a PDF file has been selected, copy it to the internal storage
            Uri selectedFile = data.getData();
            String fileName = "temp" + PDF;

            try (Cursor cursor = getContentResolver().query(selectedFile, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }

            if (!fileName.toLowerCase().endsWith(PDF)) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_file), Toast.LENGTH_LONG).show();
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
                        swapToolbarMenu(getString(R.string.menu_schedule), R.menu.fragment_schedule_list, ContextCompat.getColor(MainActivity.this, R.color.colorPrimary),
                                ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryDark));
                        break;
                    case R.id.navigation_flash_cards:
                        if (selectedFragment.getClass() == FlashCardSetListFragment.class) {
                            FlashCardSetListFragment frag = (FlashCardSetListFragment) selectedFragment;
                            frag.scrollToTop();
                            return true;
                        }
                        selectedFragment = FlashCardSetListFragment.newInstance();
                        swapToolbarMenu(getString(R.string.menu_flash_cards), R.menu.fragment_flash_card_list, ContextCompat.getColor(MainActivity.this, R.color.materialRed),
                                ContextCompat.getColor(MainActivity.this, R.color.materialRedDark));
                        break;
                    case R.id.navigation_lectures:
                        if (selectedFragment.getClass() == LectureListFragment.class) {
                            LectureListFragment frag = (LectureListFragment) selectedFragment;
                            frag.scrollToTop();
                            return true;
                        }
                        selectedFragment = LectureListFragment.newInstance(lectureSorting);
                        swapToolbarMenu(getString(R.string.menu_lectures), R.menu.fragment_lecture_list, ContextCompat.getColor(MainActivity.this, R.color.materialGreen),
                                ContextCompat.getColor(MainActivity.this, R.color.materialGreenDark));
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

    private void setBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String title = intent.getStringExtra(Notifier.EXTRA_NOTIFICATION_TITLE);
                final Long time = intent.getLongExtra(Notifier.EXTRA_NOTIFICATION_EVENT_TIME, 0L);
                final int ID = intent.getIntExtra(Notifier.EXTRA_NOTIFICATION_ID, -1);
                final String timeString = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();

                Snackbar snackbar = Snackbar.make(findViewById(R.id.content), title + " " + timeString, Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.view), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ScheduleEventFragment scheduleEventFragment = ScheduleEventFragment.newInstance(ID);
                                scheduleEventFragment.show(getSupportFragmentManager(), ScheduleEventFragment.TAG_EVENT_DIALOG_FRAGMENT);
                            }
                        });
                snackbar.show();
            }
        };
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastReceiver, new IntentFilter(Notifier.ACTION_SNACKBAR_NOTIFICATION));
    }

    private void setPassCode() {
        if (!preferences.getBoolean(LockScreenActivity.PREF_PASSCODE_SET, false)) {
            Intent intent = new Intent(MainActivity.this, LockScreenActivity.class);
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
            startActivityForResult(intent, LockScreenActivity.REQUEST_CODE_ENABLE);
        }
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
    }

    public void newFlashCardItem(View view) {
        FlashCardSetListFragment flashCardFragment = (FlashCardSetListFragment) selectedFragment;
        flashCardFragment.newFlashCardSet();
    }

    public void newLectureItem(View view) {
        Toast.makeText(getApplicationContext(), getString(R.string.pdf_select), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                .setType(TYPE_APPLICATION_PDF);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_LECTURE);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.error_file_app), Toast.LENGTH_LONG).show();
        }
    }

    private void swapToolbarMenu(final String title, final int menuLayout, final int colour, final int darkColour) {
        final int duration = getResources().getInteger(R.integer.animation_fade_time);

        HelperUtils.fadeTextChange(title, (TextView) toolbar.getChildAt(0), duration);
        HelperUtils.fadeColourChange(MainActivity.this, toolbar, colour, duration);
        HelperUtils.fadeColourChange(MainActivity.this, navigation, colour, duration);
        HelperUtils.fadeStatusBarColourChange(getWindow(), darkColour, duration);

        menu.clear();
        getMenuInflater().inflate(menuLayout, menu);
        setSearchView();

        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name),
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_lamp), darkColour));
    }

    public void setSecurityQuestion(final Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        final EditText inputQuestion = new EditText(context);
        inputQuestion.setHint("Security Question");
        inputQuestion.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        inputQuestion.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inputQuestion.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});

        final EditText input = new EditText(context);
        input.setHint("Security Question Answer");
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});

        layout.addView(inputQuestion);
        layout.addView(input);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set Security Question and Answer");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.ic_lock_black_24dp);
        builder.setView(layout);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String answer = input.getText().toString();
                final String question = inputQuestion.getText().toString();
                if (TextUtils.isEmpty(answer) || TextUtils.isEmpty(question)) {
                    input.setError(context.getString(R.string.blank_input));
                    inputQuestion.setError(context.getString(R.string.blank_input));
                } else {
                    String encryptedString = HelperUtils.hashString(answer);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putString(PREF_QUESTION, question);
                    editor.putString(PREF_ANSWER, encryptedString);
                    editor.putBoolean(LockScreenActivity.PREF_PASSCODE_SET, true);
                    editor.apply();
                    dialog.dismiss();
                }
            }
        });
        HelperUtils.showSoftKeyboard(context);
    }
}
