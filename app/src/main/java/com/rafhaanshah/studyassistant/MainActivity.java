package com.rafhaanshah.studyassistant;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedFragment = ScheduleFragment.newInstance();
                    scheduleSelected();
                    break;
                case R.id.navigation_dashboard:
                    selectedFragment = FlashCardFragment.newInstance();
                    flashCardSelected();
                    break;
                case R.id.navigation_notifications:
                    selectedFragment = LectureFragment.newInstance();
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

        directory = new File(getFilesDir().getAbsolutePath() + File.separator + "lectures");

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        selectedFragment = ScheduleFragment.newInstance();
        actionBar = getSupportActionBar();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, selectedFragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.schedule_menu, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        for (File f : directory.listFiles()) {
            Log.v("FILES", f.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.historyButton:
                if (scheduleHistory) {
                    scheduleHistory = false;
                    actionBar.setTitle("Schedule");
                } else {
                    scheduleHistory = true;
                    actionBar.setTitle("History");
                }
                ScheduleFragment frag = (ScheduleFragment) selectedFragment;
                frag.showData(scheduleHistory);
        }
        return false;
    }

    public void newScheduleItem(View v) {
        Intent nextScreen = new Intent(getApplicationContext(), ScheduleItemActivity.class);
        startActivity(nextScreen);
    }

    public void newLectureItem(View v) {
        Intent intent = new Intent()
                .setType("application/pdf")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData();
            String fileName = "tempFile.pdf";
            try (Cursor cursor = getContentResolver().query(selectedFile, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }

            Log.v("FILES", selectedFile.toString());
            Log.v("FILES", selectedFile.getPath());

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
                e.printStackTrace();
            }

            LectureFragment frag = (LectureFragment) selectedFragment;
            frag.updateData();

        } else {
            Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleSelected() {
        menu.clear();
        scheduleHistory = false;
        actionBar.setTitle("Schedule");
        getMenuInflater().inflate(R.menu.schedule_menu, menu);
    }

    private void flashCardSelected() {
        menu.clear();
        actionBar.setTitle("Flash Cards");
        //getMenuInflater().inflate(R.menu.schedule_menu, menu);
    }

    private void lectureSelected() {
        menu.clear();
        actionBar.setTitle("Lectures");
        //getMenuInflater().inflate(R.menu.schedule_menu, menu);
    }

}
