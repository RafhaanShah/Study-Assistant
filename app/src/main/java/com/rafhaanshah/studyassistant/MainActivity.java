package com.rafhaanshah.studyassistant;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rafhaanshah.studyassistant.schedule.ScheduleFragment;
import com.rafhaanshah.studyassistant.schedule.ScheduleItemActivity;

public class MainActivity extends AppCompatActivity {

    private Menu menu;
    private Fragment selectedFragment;
    private boolean scheduleHistory;
    private ActionBar actionBar;

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
