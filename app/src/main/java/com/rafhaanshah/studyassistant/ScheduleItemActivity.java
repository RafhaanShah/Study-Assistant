package com.rafhaanshah.studyassistant;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ScheduleItemActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String title, type, dueDate, dueTime, notes;
    private long epochTime;
    private int day, month, year, hour, minute;
    private boolean newItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_item);

        String item = getIntent().getStringExtra("item");
        if (item == null) {
            newItem = true;
            findViewById(R.id.finishButton).setVisibility(View.INVISIBLE);
        } else {

        }
        setSpinner();
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
        // Spinner element
        Spinner spinner = findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<String>();
        categories.add("Homework");
        categories.add("Coursework Assignment");
        categories.add("Class Test");
        categories.add("Exam");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

    }

    public void saveItem(View v) {
        title = ((EditText) findViewById(R.id.titleText)).getText().toString();
        notes = ((EditText) findViewById(R.id.notesText)).getText().toString();
        epochTime = parseDateTime(dueDate, dueTime);


        //TODO: save item
        finish();
    }

    public void finishItem(View v) {
        //TODO: mark as completed. Hide button if new item, and delete button
        return;

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // On selecting a spinner item
        String item = adapterView.getItemAtPosition(i).toString();
        type = item;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void pickDate(View v) {
        final TextView dateText = findViewById(R.id.dateText);

        if (day == 0) {
            final Calendar c = Calendar.getInstance();
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        //Launch date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int pickedYear,
                                          int monthOfYear, int dayOfMonth) {

                        dateText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + pickedYear);
                        dueDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + pickedYear;
                        year = pickedYear;
                        month = monthOfYear;
                        day = dayOfMonth;
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    public void pickTime(View v) {
        final TextView timeText = findViewById(R.id.timeText);

        if (day == 0) {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // Launch time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minuteOfHour) {

                        timeText.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minuteOfHour));
                        dueTime = hourOfDay + ":" + minute;
                        hour = hourOfDay;
                        minute = minuteOfHour;
                    }
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private long parseDateTime(String date, String time) {
        SimpleDateFormat df = new SimpleDateFormat("dd/mm/yyyy HH:mm");
        Date epochDate = null;
        try {
            epochDate = df.parse(date + " " + time);
        } catch (ParseException e) {
            epochTime = Calendar.getInstance().getTimeInMillis();
        }
        return epochDate.getTime();
    }
}
