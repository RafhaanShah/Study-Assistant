package com.rafhaanshah.studyassistant;

import android.content.Intent;

import com.github.omadahealth.lollipin.lib.managers.AppLockActivity;

public class LockScreenActivity extends AppLockActivity {

    @Override
    public void showForgotDialog() {
        //TODO: Show dialog to enter secret question answer
    }

    @Override
    public void onPinFailure(int attempts) {
    }

    @Override
    public void onPinSuccess(int attempts) {
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}
