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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rafhaanshah.studyassistant.HelperUtils;
import com.rafhaanshah.studyassistant.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmQuery;

public class ScheduleItemActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String BUNDLE_TIME = "BUNDLE_TIME";
    public static final String BUNDLE_DATE = "BUNDLE_DATE";
    public static final String BUNDLE_DUE_TIME = "BUNDLE_DUE_TIME";
    public static final String BUNDLE_DUE_DATE = "BUNDLE_DUE_DATE";
    private static final String EXTRA_ITEM_ID = "EXTRA_ITEM_ID";

    private String title, dueDate, dueTime, notes;
    private ScheduleItem.ScheduleItemType type;
    private long epochTime;
    private int day, month, year, hour, minute;
    private boolean newItem;
    private Realm realm;
    private ScheduleItem oldItem;
    private SimpleDateFormat timeFormat, dateFormat, dateTimeFormat;
    private TextView timeText, dateText;

    public static Intent getStartIntent(Context context, int ID) {
        Intent intent = new Intent(context, ScheduleItemActivity.class);
        intent.putExtra(EXTRA_ITEM_ID, ID);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_item);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.event));
        }

        realm = Realm.getDefaultInstance();
        setSpinner();

        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        timeText = findViewById(R.id.tv_time);
        dateText = findViewById(R.id.tv_date);

        int itemID = getIntent().getIntExtra(EXTRA_ITEM_ID, 0);
        if (itemID == 0) {
            newItem = true;
            findViewById(R.id.btn_finish).setVisibility(View.GONE);
            Button saveButton = findViewById(R.id.btn_save);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 2;
            saveButton.setLayoutParams(params);
            findViewById(R.id.et_title).requestFocus();
        } else {
            newItem = false;
            setFields(itemID);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (newItem) {
            return false;
        } else {
            getMenuInflater().inflate(R.menu.activity_schedule_item, menu);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_btn_delete_event:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.confirm_delete))
                        .setMessage(getString(R.string.delete_event))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(@NonNull Realm realm) {
                                        oldItem.deleteFromRealm();
                                    }
                                });
                                finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(R.drawable.ic_delete_black_24dp)
                        .show();
                return true;
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString(BUNDLE_TIME, timeText.getText().toString());
        out.putString(BUNDLE_DATE, dateText.getText().toString());
        out.putString(BUNDLE_DUE_TIME, dueTime);
        out.putString(BUNDLE_DUE_DATE, dueDate);
    }

    @Override
    protected void onRestoreInstanceState(Bundle in) {
        super.onRestoreInstanceState(in);
        timeText.setText(in.getString(BUNDLE_TIME));
        dateText.setText(in.getString(BUNDLE_DATE));
        dueTime = in.getString(BUNDLE_DUE_TIME);
        dueDate = in.getString(BUNDLE_DUE_DATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    private void setFields(int ID) {
        RealmQuery query = realm.where(ScheduleItem.class).equalTo(ScheduleItem.ScheduleItem_ID, ID);
        oldItem = (ScheduleItem) query.findFirst();

        EditText editTitle = findViewById(R.id.et_title);
        editTitle.setText(oldItem.getTitle());
        EditText editNote = findViewById(R.id.et_notes);
        editNote.setText(oldItem.getNotes());

        if (oldItem.isCompleted()) {
            Button button = findViewById(R.id.btn_finish);
            button.setText(getString(R.string.mark_incomplete));
        }

        Spinner spinner = findViewById(R.id.spinner);
        switch (oldItem.getType()) {
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
            default:
                spinner.setSelection(0);
        }

        dueTime = timeFormat.format(new Date(oldItem.getTime()));
        dueDate = dateFormat.format(new Date(oldItem.getTime()));

        hour = Integer.parseInt(dueTime.substring(0, 2));
        minute = Integer.parseInt(dueTime.substring(3, 5));

        day = Integer.parseInt(dueDate.substring(0, 2));
        month = Integer.parseInt(dueDate.substring(3, 5)) - 1;
        year = Integer.parseInt(dueDate.substring(6, 10));

        timeText = findViewById(R.id.tv_time);
        dateText = findViewById(R.id.tv_date);
        try {
            timeText.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(timeFormat.parse(dueTime)));
            dateText.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(dateFormat.parse(dueDate)));
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            timeText.setText(dueTime);
            dateText.setText(dueDate);
        }
    }

    public void setSpinner() {
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<>();
        categories.add(getString(R.string.homework));
        categories.add(getString(R.string.coursework));
        categories.add(getString(R.string.class_test));
        categories.add(getString(R.string.exam));

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void saveItem(View view) {
        title = ((EditText) findViewById(R.id.et_title)).getText().toString().trim();
        notes = ((EditText) findViewById(R.id.et_notes)).getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(dueDate) || TextUtils.isEmpty(dueTime)) {
            Toast.makeText(getApplicationContext(), getString(R.string.fill_event), Toast.LENGTH_SHORT).show();
            return;
        }
        epochTime = parseDateTime(dueDate, dueTime);

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
                ScheduleItem item = oldItem;

                if (newItem) {
                    item = realm.createObject(ScheduleItem.class);
                    item.setID(maxID + 1);
                    item.setCompleted(false);
                }
                item.setTitle(title);
                item.setNotes(notes);
                item.setTime(epochTime);
                item.setType(type);
            }
        });
        finish();
    }

    public void finishItem(View view) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                if (oldItem.isCompleted()) {
                    oldItem.setCompleted(false);
                } else {
                    oldItem.setCompleted(true);
                }
            }
        });
        saveItem(view);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                type = ScheduleItem.ScheduleItemType.HOMEWORK;
                break;
            case 1:
                type = ScheduleItem.ScheduleItemType.COURSEWORK;
                break;
            case 2:
                type = ScheduleItem.ScheduleItemType.TEST;
                break;
            case 3:
                type = ScheduleItem.ScheduleItemType.EXAM;
                break;
            default:
                type = ScheduleItem.ScheduleItemType.HOMEWORK;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void pickDate(View view) {
        HelperUtils.hideSoftKeyboard(ScheduleItemActivity.this, view);

        if (newItem) {
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int pickedYear, int monthOfYear, int dayOfMonth) {
                        dueDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + pickedYear;
                        year = pickedYear;
                        month = monthOfYear;
                        day = dayOfMonth;
                        try {
                            dateText.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(dateFormat.parse(dueDate)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                            dateText.setText(dueDate);
                        }
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    public void pickTime(View view) {
        HelperUtils.hideSoftKeyboard(ScheduleItemActivity.this, view);

        if (newItem) {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                        dueTime = (String.valueOf(hourOfDay) + ":" + String.valueOf(minuteOfHour));
                        hour = hourOfDay;
                        minute = minuteOfHour;
                        try {
                            timeText.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(timeFormat.parse(dueTime)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            timeText.setText(dueTime);
                        }
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private long parseDateTime(String date, String time) {
        Date epochDate = null;
        try {
            epochDate = dateTimeFormat.parse(date + " " + time);
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        if (epochDate != null) return epochDate.getTime();
        else return Calendar.getInstance().getTimeInMillis();
    }
}
