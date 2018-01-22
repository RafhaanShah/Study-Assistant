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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.flashcards.FlashCardFragment;
import com.rafhaanshah.studyassistant.lecture.LectureFragment;
import com.rafhaanshah.studyassistant.schedule.ScheduleFragment;
import com.rafhaanshah.studyassistant.schedule.ScheduleItemActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private Menu menu;
    private Fragment selectedFragment;
    private boolean scheduleHistory;
    private ActionBar actionBar;
    private File directory;
    private int lectureSorting;
    private SharedPreferences preferences;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_schedule:
                    selectedFragment = ScheduleFragment.newInstance();
                    scheduleSelected();
                    break;
                case R.id.navigation_flash_cards:
                    selectedFragment = FlashCardFragment.newInstance();
                    flashCardSelected();
                    break;
                case R.id.navigation_lectures:
                    selectedFragment = LectureFragment.newInstance(lectureSorting);
                    lectureSelected();
                    break;
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, selectedFragment);
            transaction.commit();
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        lectureSorting = preferences.getInt("sorting", 0);
        directory = new File(getFilesDir().getAbsolutePath() + File.separator + "lectures");

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        actionBar = getSupportActionBar();

        if (savedInstanceState == null) {
            selectedFragment = ScheduleFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, selectedFragment);
            transaction.commit();
        } else {
            selectedFragment = getSupportFragmentManager().findFragmentById(R.id.content);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        if (selectedFragment.getClass() == ScheduleFragment.class) {
            scheduleSelected();
        } else if (selectedFragment.getClass() == FlashCardFragment.class) {
            flashCardSelected();
        } else if (selectedFragment.getClass() == LectureFragment.class) {
            lectureSelected();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("sorting", lectureSorting);
        editor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.historyButton:
                historyButtonPressed();
                return true;
            case R.id.lectureSortButton:
                lectureSortButtonPressed();
                return true;
        }
        return false;
    }

    private void historyButtonPressed() {
        if (scheduleHistory) {
            scheduleHistory = false;
            actionBar.setTitle(getString(R.string.menu_schedule));
        } else {
            scheduleHistory = true;
            actionBar.setTitle(getString(R.string.menu_history));
        }
        ScheduleFragment scheduleFragment = (ScheduleFragment) selectedFragment;
        scheduleFragment.showData(scheduleHistory);
    }

    private void lectureSortButtonPressed() {
        LectureFragment lectureFragment = (LectureFragment) selectedFragment;
        switch (lectureSorting) {
            case 0:
                lectureFragment.updateData(1, false);
                lectureSorting = 1;
                Toast.makeText(getApplicationContext(), getString(R.string.sort_date), Toast.LENGTH_SHORT).show();
                break;
            case 1:
                lectureFragment.updateData(2, false);
                lectureSorting = 2;
                Toast.makeText(getApplicationContext(), getString(R.string.sort_size), Toast.LENGTH_SHORT).show();
                break;
            case 2:
                lectureFragment.updateData(0, false);
                lectureSorting = 0;
                Toast.makeText(getApplicationContext(), getString(R.string.sort_title), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void newScheduleItem(View v) {
        Intent nextScreen = new Intent(getApplicationContext(), ScheduleItemActivity.class);
        startActivity(nextScreen);
    }

    public void newLectureItem(View v) {
        Toast.makeText(getApplicationContext(), getString(R.string.pdf_select), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent()
                .setType("application/pdf")
                .setAction(Intent.ACTION_GET_CONTENT);

        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.pdf_select)), 100);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_file_picker), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void newFlashCardItem(View v) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData();
            String fileName = "New File.pdf";

            try (Cursor cursor = getContentResolver().query(selectedFile, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }

            if (!fileName.toLowerCase().endsWith(".pdf")) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_file_picker), Toast.LENGTH_SHORT).show();
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
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_inaccessible_file), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                LectureFragment frag = (LectureFragment) selectedFragment;
                frag.updateData(lectureSorting, true);
            }
        }
    }

    private void scheduleSelected() {
        menu.clear();
        scheduleHistory = false;
        actionBar.setTitle(getString(R.string.menu_schedule));
        getMenuInflater().inflate(R.menu.schedule_menu, menu);
    }

    private void flashCardSelected() {
        menu.clear();
        actionBar.setTitle(getString(R.string.menu_flash_cards));
        //getMenuInflater().inflate(R.menu.flash_card_menu, menu);
    }

    private void lectureSelected() {
        menu.clear();
        actionBar.setTitle(getString(R.string.menu_lectures));
        getMenuInflater().inflate(R.menu.lecture_menu, menu);
    }
}
