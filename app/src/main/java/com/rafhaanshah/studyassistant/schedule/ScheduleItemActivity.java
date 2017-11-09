package com.rafhaanshah.studyassistant.schedule;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

import com.rafhaanshah.studyassistant.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;

public class ScheduleItemActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String title, type, dueDate, dueTime, notes;
    private long epochTime;
    private int day, month, year, hour, minute;
    private boolean newItem;
    private Realm realm;
    private ScheduleItem oldItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_item);
        getSupportActionBar().setTitle("Event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();
        setSpinner();

        String item = getIntent().getStringExtra("item");
        if (item == null) {
            newItem = true;
            findViewById(R.id.finishButton).setVisibility(View.GONE);
            Button saveButton = findViewById(R.id.saveButton);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 2;
            saveButton.setLayoutParams(params);
        } else {
            newItem = false;
            setFields(Integer.valueOf(item));
        }
    }

    private void setFields(int ID) {
        RealmQuery query = realm.where(ScheduleItem.class).equalTo("ID", ID);
        oldItem = (ScheduleItem) query.findFirst();

        EditText editTitle = findViewById(R.id.titleText);
        editTitle.setText(oldItem.getTitle());
        EditText editNote = findViewById(R.id.notesText);
        editNote.setText(oldItem.getNotes());

        if (oldItem.isCompleted()) {
            Button button = findViewById(R.id.finishButton);
            button.setText("Mark Incomplete");
        }

        Spinner spinner = findViewById(R.id.spinner);
        for (int i = 1; i < 4; i++) {
            if (spinner.getItemAtPosition(i).equals(oldItem.getType())) {
                spinner.setSelection(i);
                break;
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        dueDate = dateFormat.format(new Date(oldItem.getTime()));
        dueTime = timeFormat.format(new Date(oldItem.getTime()));

        hour = Integer.parseInt(dueTime.substring(0, 2));
        minute = Integer.parseInt(dueTime.substring(3, 5));

        day = Integer.parseInt(dueDate.substring(0, 2));
        month = Integer.parseInt(dueDate.substring(3, 5)) - 1;
        year = Integer.parseInt(dueDate.substring(6, 10));

        TextView timeText = findViewById(R.id.timeText);
        timeText.setText(dueTime);

        TextView dateText = findViewById(R.id.dateText);
        dateText.setText(dueDate);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (newItem) {
            return false;
        } else {
            getMenuInflater().inflate(R.menu.schedule_item_menu, menu);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteButton:
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO: delete item, check if exists first
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(@NonNull Realm realm) {
                                        oldItem.deleteFromRealm();
                                    }
                                });
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return true;
        }
        return false;
    }

    public void setSpinner() {
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<>();
        categories.add("Homework");
        categories.add("Coursework Assignment");
        categories.add("Class Test");
        categories.add("Exam");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }

    public void saveItem(View v) {
        title = ((EditText) findViewById(R.id.titleText)).getText().toString();
        notes = ((EditText) findViewById(R.id.notesText)).getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(dueDate) || TextUtils.isEmpty(dueTime)) {
            Toast.makeText(getApplicationContext(), "Please fill in the title and select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        epochTime = parseDateTime(dueDate, dueTime);

        Number num = realm.where(ScheduleItem.class).max("ID");
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

    public void finishItem(View v) {
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
        saveItem(v);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        type = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void pickDate(View v) {
        final TextView dateText = findViewById(R.id.dateText);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        if (day == 0) {
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
                        dateText.setText(dueDate);
                        year = pickedYear;
                        month = monthOfYear;
                        day = dayOfMonth;
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    public void pickTime(View v) {
        final TextView timeText = findViewById(R.id.timeText);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        if (hour == 0 && minute == 0) {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                        dueTime = (String.format("%02d", hourOfDay) + ":" + String.format("%02d", minuteOfHour));
                        timeText.setText(dueTime);
                        hour = hourOfDay;
                        minute = minuteOfHour;
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private long parseDateTime(String date, String time) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date epochDate = null;
        try {
            epochDate = df.parse(date + " " + time);
        } catch (ParseException e) {
            epochTime = Calendar.getInstance().getTimeInMillis();
        }
        return epochDate.getTime();
    }
}
