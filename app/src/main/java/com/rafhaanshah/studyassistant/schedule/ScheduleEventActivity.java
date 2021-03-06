package com.rafhaanshah.studyassistant.schedule;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.omadahealth.lollipin.lib.PinCompatActivity;
import com.github.omadahealth.lollipin.lib.managers.LockManager;
import com.rafhaanshah.studyassistant.LockScreenActivity;
import com.rafhaanshah.studyassistant.R;
import com.rafhaanshah.studyassistant.notifications.Notifier;
import com.rafhaanshah.studyassistant.utils.HelperUtils;

import java.text.DateFormat;
import java.util.Calendar;

import io.realm.Realm;

public class ScheduleEventActivity extends PinCompatActivity {

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    private static final String BUNDLE_EVENT_TIME_MS = "BUNDLE_EVENT_TIME_MS";
    private static final String BUNDLE_NOTIFICATION_TIME_MS = "BUNDLE_NOTIFICATION_TIME_MS";
    private int eventID;
    private boolean newEvent, reminderSetting;
    private Calendar eventCal, notificationCal;
    private Realm realm;
    private ScheduleEvent scheduleEvent;
    private ScheduleEvent.ScheduleEventType type;
    private TextView titleText, notesText, dateText, timeText, notificationDateText, notificationTimeText;
    private Spinner spinner;
    private CheckBox checkBox;
    private Switch notificationSwitch;
    private ImageView imageView;

    public static Intent getStartIntent(Context context, int ID) {
        Intent intent = new Intent(context, ScheduleEventActivity.class);
        intent.putExtra(EXTRA_EVENT_ID, ID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getViews();

        eventCal = Calendar.getInstance();
        eventCal.set(Calendar.SECOND, 0);
        eventCal.set(Calendar.MILLISECOND, 0);
        notificationCal = Calendar.getInstance();
        notificationCal.set(Calendar.SECOND, 0);
        notificationCal.set(Calendar.MILLISECOND, 0);

        realm = Realm.getDefaultInstance();
        eventID = getIntent().getIntExtra(EXTRA_EVENT_ID, -1);

        if (eventID < 0) {
            newEvent = true;
            titleText.requestFocus();
            toolbar.setTitle(getString(R.string.new_event));
            findViewById(R.id.btn_delete_event).setVisibility(View.GONE);
        } else {
            newEvent = false;
            scheduleEvent = realm.where(ScheduleEvent.class).equalTo(ScheduleEvent.ScheduleEvent_ID, eventID).findFirst();
            setViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_schedule_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_btn_save_event:
                saveEvent();
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
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putLong(BUNDLE_EVENT_TIME_MS, eventCal.getTimeInMillis());
        out.putLong(BUNDLE_NOTIFICATION_TIME_MS, notificationCal.getTimeInMillis());
    }

    @Override
    protected void onRestoreInstanceState(Bundle in) {
        super.onRestoreInstanceState(in);
        eventCal.setTimeInMillis(in.getLong(BUNDLE_EVENT_TIME_MS));
        notificationCal.setTimeInMillis(in.getLong(BUNDLE_NOTIFICATION_TIME_MS));
    }

    @Override
    public void onPause() {
        LockManager<LockScreenActivity> lockManager = LockManager.getInstance();
        if (lockManager != null && lockManager.isAppLockEnabled())
            lockManager.getAppLock().setLastActiveMillis();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void getViews() {
        titleText = findViewById(R.id.et_title);
        notesText = findViewById(R.id.et_notes);
        notesText.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.et_notes) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
        dateText = findViewById(R.id.et_date);
        timeText = findViewById(R.id.et_time);
        notificationDateText = findViewById(R.id.et_notification_date);
        notificationTimeText = findViewById(R.id.et_notification_time);
        notificationSwitch = findViewById(R.id.switch_notification);

        final RelativeLayout notificationLayout = findViewById(R.id.reminder_layout);
        imageView = findViewById(R.id.image_view_type);

        checkBox = findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            notificationLayout.setVisibility(View.GONE);
                        } else {
                            notificationLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                final TypedArray icons = getResources().obtainTypedArray(R.array.event_type_icons);
                switch (i) {
                    case 0:
                        type = ScheduleEvent.ScheduleEventType.HOMEWORK;
                        imageView.setImageDrawable(icons.getDrawable(0));
                        break;
                    case 1:
                        type = ScheduleEvent.ScheduleEventType.TEST;
                        imageView.setImageDrawable(icons.getDrawable(1));
                        break;
                    case 2:
                        type = ScheduleEvent.ScheduleEventType.COURSEWORK;
                        imageView.setImageDrawable(icons.getDrawable(2));
                        break;
                    case 3:
                        type = ScheduleEvent.ScheduleEventType.EXAM;
                        imageView.setImageDrawable(icons.getDrawable(3));
                        break;
                }
                icons.recycle();
                HelperUtils.setDrawableColour(imageView.getDrawable(), ContextCompat.getColor(ScheduleEventActivity.this, android.R.color.black));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setViews() {
        if (scheduleEvent != null) {
            titleText.setText(scheduleEvent.getTitle());
            notesText.setText(scheduleEvent.getNotes());

            spinner = findViewById(R.id.spinner);
            switch (scheduleEvent.getType()) {
                case HOMEWORK:
                    spinner.setSelection(0);
                    break;
                case TEST:
                    spinner.setSelection(1);
                    break;
                case COURSEWORK:
                    spinner.setSelection(2);
                    break;
                case EXAM:
                    spinner.setSelection(3);
                    break;
            }

            checkBox.setChecked(scheduleEvent.isCompleted());
            notificationSwitch.setChecked(scheduleEvent.isReminderSet());
            reminderSetting = scheduleEvent.isReminderSet();

            eventCal.setTimeInMillis(scheduleEvent.getEventTime());
            dateText.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(eventCal.getTimeInMillis()));
            timeText.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(eventCal.getTimeInMillis()));

            if (scheduleEvent.isReminderSet() && scheduleEvent.getReminderTime() > System.currentTimeMillis()) {
                notificationCal.setTimeInMillis(scheduleEvent.getReminderTime());
                notificationDateText.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(notificationCal.getTimeInMillis()));
                notificationTimeText.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(notificationCal.getTimeInMillis()));
            }
        }
    }

    private void saveEvent() {
        final String title = titleText.getText().toString().trim();
        final String notes = notesText.getText().toString().trim();
        final boolean completed = checkBox.isChecked();
        final boolean reminder;
        final long reminderTime;
        final long eventTime = eventCal.getTimeInMillis();

        if (TextUtils.isEmpty(title)) {
            titleText.setError(getString(R.string.blank_input));
            return;
        }
        if (TextUtils.isEmpty(dateText.getText().toString())) {
            timeText.setError(getString(R.string.blank_input));
            return;
        }
        if (TextUtils.isEmpty(timeText.getText().toString())) {
            dateText.setError(getString(R.string.blank_input));
            return;
        }

        final int newID = getNextID();
        if (newEvent)
            eventID = newID;

        // If event is not complete and notification is on:
        if (!completed && notificationSwitch.isChecked()) {
            if (TextUtils.isEmpty(notificationTimeText.getText().toString()) || TextUtils.isEmpty(notificationDateText.getText().toString())) {
                notificationDateText.setError(getString(R.string.blank_input));
                notificationTimeText.setError(getString(R.string.blank_input));
                Toast.makeText(getApplicationContext(), getString(R.string.fill_notification), Toast.LENGTH_LONG).show();
                return;
            }
            if (eventCal.getTimeInMillis() < notificationCal.getTimeInMillis()) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_reminder_time), Toast.LENGTH_LONG).show();
                notificationDateText.setError(getString(R.string.blank_input));
                notificationTimeText.setError(getString(R.string.blank_input));
                return;
            }
            if (System.currentTimeMillis() > notificationCal.getTimeInMillis()) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_reminder_time_past), Toast.LENGTH_LONG).show();
                notificationDateText.setError(getString(R.string.blank_input));
                notificationTimeText.setError(getString(R.string.blank_input));
                return;
            }
            reminder = true;
            reminderTime = notificationCal.getTimeInMillis();
            Notifier.scheduleNotification(ScheduleEventActivity.this, eventID, title, eventTime, reminderTime);
        } else {
            // No reminder
            reminder = false;
            reminderTime = 0L;

            // Cancel notification only if it had been set previously
            if (reminderSetting)
                Notifier.cancelScheduledNotification(ScheduleEventActivity.this, eventID);
        }

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                ScheduleEvent scheduleEvent = ScheduleEventActivity.this.scheduleEvent;
                if (newEvent) {
                    scheduleEvent = realm.createObject(ScheduleEvent.class, newID);
                }
                scheduleEvent.setCompleted(completed);
                scheduleEvent.setTitle(title);
                scheduleEvent.setNotes(notes);
                scheduleEvent.setEventTime(eventTime);
                scheduleEvent.setType(type);
                scheduleEvent.setReminder(reminder);
                scheduleEvent.setReminderTime(reminderTime);
            }
        });
        finish();
    }

    private int getNextID() {
        try {
            Number number = realm.where(ScheduleEvent.class).max(ScheduleEvent.ScheduleEvent_ID);
            if (number != null) {
                return number.intValue() + 1;
            } else {
                return 0;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public void deleteEvent(View view) {
        new AlertDialog.Builder(ScheduleEventActivity.this)
                .setTitle(getString(R.string.confirm_delete))
                .setMessage(getString(R.string.delete_event))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(@NonNull Realm realm) {
                                scheduleEvent.deleteFromRealm();
                            }
                        });
                        if (reminderSetting)
                            Notifier.cancelScheduledNotification(ScheduleEventActivity.this, eventID);
                        finish();
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
        titleText.clearFocus();
        HelperUtils.hideSoftKeyboard(ScheduleEventActivity.this, view);
        DatePickerDialog datePickerDialog = new DatePickerDialog(ScheduleEventActivity.this,
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
        titleText.clearFocus();
        HelperUtils.hideSoftKeyboard(ScheduleEventActivity.this, view);
        TimePickerDialog timePickerDialog = new TimePickerDialog(ScheduleEventActivity.this,
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
