package com.rafhaanshah.studyassistant.schedule;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import java.text.DateFormat;
import java.util.Calendar;

import io.realm.Realm;

public class ScheduleItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "EXTRA_ITEM_ID";
    private static final String BUNDLE_TIME_MS = "BUNDLE_TIME_MS";
    private static final String BUNDLE_NOTIFICATION_TIME_MS = "BUNDLE_NOTIFICATION_TIME_MS";
    private int itemID;
    private boolean newItem;
    private Calendar eventCal, notificationCal;
    private Realm realm;
    private ScheduleItem item;
    private ScheduleItem.ScheduleItemType type;
    private TextView titleText, notesText, dateText, timeText, notificationDateText, notificationTimeText;
    private Spinner spinner;
    private CheckBox checkBox;
    private Switch notificationSwitch;

    public static Intent getStartIntent(Context context, int ID) {
        Intent intent = new Intent(context, ScheduleItemActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, ID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_item);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getViews();

        eventCal = Calendar.getInstance();
        notificationCal = Calendar.getInstance();

        realm = Realm.getDefaultInstance();
        itemID = getIntent().getIntExtra(EXTRA_ITEM_ID, 0);

        if (itemID == 0) {
            newItem = true;
            toolbar.setTitle(getString(R.string.new_event));
            titleText.requestFocus();
            findViewById(R.id.btn_delete_event).setVisibility(View.GONE);
        } else {
            newItem = false;
            setFields();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_schedule_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_btn_save_event:
                saveItem();
                return true;
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        super.onSupportNavigateUp();
        overridePendingTransition(R.anim.slide_to_bottom, R.anim.slide_from_top);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_to_bottom, R.anim.slide_from_top);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putLong(BUNDLE_TIME_MS, eventCal.getTimeInMillis());
        out.putLong(BUNDLE_NOTIFICATION_TIME_MS, notificationCal.getTimeInMillis());
    }

    @Override
    protected void onRestoreInstanceState(Bundle in) {
        super.onRestoreInstanceState(in);
        eventCal.setTimeInMillis(in.getLong(BUNDLE_TIME_MS));
        notificationCal.setTimeInMillis(in.getLong(BUNDLE_NOTIFICATION_TIME_MS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void getViews() {

        titleText = findViewById(R.id.et_title);
        notesText = findViewById(R.id.et_notes);
        dateText = findViewById(R.id.et_date);
        timeText = findViewById(R.id.et_time);
        checkBox = findViewById(R.id.checkBox);
        notificationDateText = findViewById(R.id.et_notification_date);
        notificationTimeText = findViewById(R.id.et_notification_time);
        notificationSwitch = findViewById(R.id.switch_notification);

        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        type = ScheduleItem.ScheduleItemType.HOMEWORK;
                        //((TextView) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_black_24dp, 0, 0, 0);
                        break;
                    case 1:
                        type = ScheduleItem.ScheduleItemType.COURSEWORK;
                        //((TextView) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_computer_black_24dp, 0, 0, 0);
                        break;
                    case 2:
                        type = ScheduleItem.ScheduleItemType.TEST;
                        //((TextView) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_chrome_reader_mode_black_24dp, 0, 0, 0);
                        break;
                    case 3:
                        type = ScheduleItem.ScheduleItemType.EXAM;
                        //((TextView) view).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_event_note_black_24dp, 0, 0, 0);
                        break;
                }
                //((TextView) view).setCompoundDrawablePadding(50);
                //HelperUtils.setDrawableColour(((TextView) view).getCompoundDrawables()[0], ContextCompat.getColor(ScheduleItemActivity.this, R.color.textGrey));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setFields() {
        item = realm.where(ScheduleItem.class).equalTo(ScheduleItem.ScheduleItem_ID, itemID).findFirst();

        if (item != null) {
            titleText.setText(item.getTitle());
            notesText.setText(item.getNotes());

            spinner = findViewById(R.id.spinner);
            switch (item.getType()) {
                case HOMEWORK:
                    spinner.setSelection(0);
                    break;
                case COURSEWORK:
                    spinner.setSelection(1);
                    break;
                case TEST:
                    spinner.setSelection(2);
                    break;
                case EXAM:
                    spinner.setSelection(3);
                    break;
            }

            checkBox.setChecked(item.isCompleted());
            notificationSwitch.setChecked(item.isReminder());

            eventCal.setTimeInMillis(item.getTime());
            dateText.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(eventCal.getTimeInMillis()));
            timeText.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(eventCal.getTimeInMillis()));

            if (item.getReminderTime() != 0L) {
                notificationCal.setTimeInMillis(item.getReminderTime());
                notificationDateText.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(notificationCal.getTimeInMillis()));
                notificationTimeText.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(notificationCal.getTimeInMillis()));
            }
        }
    }

    private void saveItem() {
        final String title = titleText.getText().toString().trim();
        final String notes = notesText.getText().toString().trim();
        final boolean completed = checkBox.isChecked();
        final boolean reminder;
        final long reminderTime;

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(timeText.getText().toString()) || TextUtils.isEmpty(dateText.getText().toString())) {
            Toast.makeText(getApplicationContext(), getString(R.string.fill_event), Toast.LENGTH_SHORT).show();
            return;
        }

        // If item is not complete and notification is on:
        if (!completed && notificationSwitch.isChecked()) {
            if (TextUtils.isEmpty(notificationTimeText.getText().toString()) || TextUtils.isEmpty(notificationDateText.getText().toString())) {
                Toast.makeText(getApplicationContext(), getString(R.string.fill_notification), Toast.LENGTH_SHORT).show();
                return;
            }
            if (eventCal.getTimeInMillis() < notificationCal.getTimeInMillis()) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_reminder_time), Toast.LENGTH_SHORT).show();
                return;
            }
            if (System.currentTimeMillis() < notificationCal.getTimeInMillis()) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_reminder_time_past), Toast.LENGTH_SHORT).show();
                return;
            }
            reminder = true;
            reminderTime = notificationCal.getTimeInMillis();
            // Set alarm on
        } else {
            reminder = false;
            reminderTime = 0L;
            // Set alarm off
        }

        Number num = realm.where(ScheduleItem.class).max(ScheduleItem.ScheduleItem_ID);
        final int maxID;
        if (num != null) {
            maxID = num.intValue();
        } else {
            maxID = 0;
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                ScheduleItem scheduleItem = item;
                if (newItem) {
                    scheduleItem = realm.createObject(ScheduleItem.class);
                    scheduleItem.setID(maxID + 1);
                }
                scheduleItem.setCompleted(completed);
                scheduleItem.setTitle(title);
                scheduleItem.setNotes(notes);
                scheduleItem.setTime(eventCal.getTimeInMillis());
                scheduleItem.setType(type);
                scheduleItem.setReminder(reminder);
                scheduleItem.setReminderTime(reminderTime);
            }
        });
        finish();
        overridePendingTransition(R.anim.slide_to_bottom, R.anim.slide_from_top);
    }

    public void deleteEvent(View view) {
        new AlertDialog.Builder(ScheduleItemActivity.this)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.delete_event))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                item.deleteFromRealm();
                            }
                        });
                        finish();
                        overridePendingTransition(R.anim.slide_to_bottom, R.anim.slide_from_top);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(R.drawable.ic_delete_black_24dp)
                .show();
    }

    public void pickDate(final View view) {
        HelperUtils.hideSoftKeyboard(ScheduleItemActivity.this, view);
        DatePickerDialog datePickerDialog = new DatePickerDialog(ScheduleItemActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePickerView, int year, int month, int day) {
                        switch (view.getId()) {
                            case R.id.et_date:
                                eventCal.set(Calendar.YEAR, year);
                                eventCal.set(Calendar.MONTH, month);
                                eventCal.set(Calendar.DAY_OF_MONTH, day);
                                dateText.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(eventCal.getTimeInMillis()));
                                break;
                            case R.id.et_notification_date:
                                notificationCal.set(Calendar.YEAR, year);
                                notificationCal.set(Calendar.MONTH, month);
                                notificationCal.set(Calendar.DAY_OF_MONTH, day);
                                notificationDateText.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(notificationCal.getTimeInMillis()));
                                break;
                        }
                    }
                }, eventCal.get(Calendar.YEAR), eventCal.get(Calendar.MONTH), eventCal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    public void pickTime(final View view) {
        HelperUtils.hideSoftKeyboard(ScheduleItemActivity.this, view);
        TimePickerDialog timePickerDialog = new TimePickerDialog(ScheduleItemActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePickerView, int hour, int minute) {
                        switch (view.getId()) {
                            case R.id.et_time:
                                eventCal.set(Calendar.HOUR_OF_DAY, hour);
                                eventCal.set(Calendar.MINUTE, minute);
                                timeText.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(eventCal.getTimeInMillis()));
                                break;
                            case R.id.et_notification_time:
                                notificationCal.set(Calendar.HOUR_OF_DAY, hour);
                                notificationCal.set(Calendar.MINUTE, minute);
                                notificationTimeText.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(notificationCal.getTimeInMillis()));
                                break;
                        }
                    }
                }, eventCal.get(Calendar.HOUR_OF_DAY), eventCal.get(Calendar.MINUTE), false);
        timePickerDialog.show();
    }
}
