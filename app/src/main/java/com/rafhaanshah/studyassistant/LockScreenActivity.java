package com.rafhaanshah.studyassistant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.omadahealth.lollipin.lib.managers.AppLockActivity;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LockScreenActivity extends AppLockActivity {

    static final String PREF_PASSCODE = "PREF_PASSCODE";
    static final String PREF_QUESTION = "PREF_QUESTION";
    static final String PREF_ANSWER = "PREF_ANSWER";
    static final int REQUEST_CODE_ENABLE = 999;

    public static void setSecurityQuestion(final Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        final EditText input = new EditText(context);
        input.setHint("Security Question Answer");
        final EditText inputQuestion = new EditText(context);
        inputQuestion.setHint("Security Question");
        inputQuestion.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        inputQuestion.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
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
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String answer = input.getText().toString();
                final String question = inputQuestion.getText().toString();
                if (TextUtils.isEmpty(answer) || TextUtils.isEmpty(question)) {
                    Toast.makeText(context, R.string.error_blank, Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                        messageDigest.update(answer.getBytes());
                        String encryptedString = new String(messageDigest.digest());
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        editor.putString(PREF_QUESTION, question);
                        editor.putString(PREF_ANSWER, encryptedString);
                        editor.putBoolean(LockScreenActivity.PREF_PASSCODE, true);
                        editor.apply();
                        dialog.dismiss();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        HelperUtils.showSoftKeyboard(context, input);
    }

    @Override
    public void showForgotDialog() {
        LinearLayout layout = new LinearLayout(LockScreenActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        final EditText input = new EditText(LockScreenActivity.this);
        final TextView question = new TextView(LockScreenActivity.this);
        question.setPadding(20, 10, 20, 10);

        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});

        final String encryptedAnswer = PreferenceManager.getDefaultSharedPreferences(LockScreenActivity.this).getString(PREF_ANSWER, "");
        String questionString = PreferenceManager.getDefaultSharedPreferences(LockScreenActivity.this).getString(PREF_QUESTION, "");

        question.setText(questionString);

        layout.addView(question);
        layout.addView(input);

        AlertDialog.Builder builder = new AlertDialog.Builder(LockScreenActivity.this);
        builder.setTitle("Answer Your Security Question");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setIcon(R.drawable.ic_lock_black_24dp);
        builder.setView(layout);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String title = input.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(LockScreenActivity.this, R.string.error_blank, Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                        messageDigest.update(input.getText().toString().getBytes());
                        String encryptedString = new String(messageDigest.digest());
                        if (encryptedString.equals(encryptedAnswer)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LockScreenActivity.this).edit();
                            editor.putBoolean(PREF_PASSCODE, false);
                            editor.apply();
                            Toast.makeText(LockScreenActivity.this, "Restart App To Set New Passcode", Toast.LENGTH_SHORT).show();
                            LockManager<LockScreenActivity> lockManager = LockManager.getInstance();
                            if (lockManager != null)
                                lockManager.getAppLock().disableAndRemoveConfiguration();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(LockScreenActivity.this, "Incorrect Answer", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        HelperUtils.showSoftKeyboard(LockScreenActivity.this, input);
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
