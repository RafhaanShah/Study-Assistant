package com.rafhaanshah.studyassistant.schedule;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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

    private String title, type, dueDate, dueTime, notes;
    private long epochTime;
    private int day, month, year, hour, minute;
    private boolean newItem;
    private Realm realm;
    private ScheduleItem oldItem;
    private SimpleDateFormat timeFormat, dateFormat, dateTimeFormat;
    private TextView timeText, dateText;

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

        timeText = findViewById(R.id.timeText);
        dateText = findViewById(R.id.dateText);

        String item = getIntent().getStringExtra("item");
        if (item == null) {
            newItem = true;
            findViewById(R.id.finishButton).setVisibility(View.GONE);
            Button saveButton = findViewById(R.id.saveButton);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 2;
            saveButton.setLayoutParams(params);
            findViewById(R.id.titleText).requestFocus();
        } else {
            newItem = false;
            setFields(Integer.valueOf(item));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("time", timeText.getText().toString());
        out.putString("date", dateText.getText().toString());
        out.putString("dueTime", dueTime);
        out.putString("dueDate", dueDate);
    }

    @Override
    protected void onRestoreInstanceState(Bundle in) {
        super.onRestoreInstanceState(in);
        timeText.setText(in.getString("time"));
        dateText.setText(in.getString("date"));
        dueTime = in.getString("dueTime");
        dueDate = in.getString("dueDate");
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
            button.setText(getString(R.string.mark_incomplete));
        }

        Spinner spinner = findViewById(R.id.spinner);
        for (int i = 1; i < 4; i++) {
            if (spinner.getItemAtPosition(i).equals(oldItem.getType())) {
                spinner.setSelection(i);
                break;
            }
        }

        dueTime = timeFormat.format(new Date(oldItem.getTime()));
        dueDate = dateFormat.format(new Date(oldItem.getTime()));

        hour = Integer.parseInt(dueTime.substring(0, 2));
        minute = Integer.parseInt(dueTime.substring(3, 5));

        day = Integer.parseInt(dueDate.substring(0, 2));
        month = Integer.parseInt(dueDate.substring(3, 5)) - 1;
        year = Integer.parseInt(dueDate.substring(6, 10));

        timeText = findViewById(R.id.timeText);
        dateText = findViewById(R.id.dateText);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (newItem) {
            return false;
        } else {
            getMenuInflater().inflate(R.menu.menu_schedule_item_activity, menu);
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

    public void saveItem(View v) {
        title = ((EditText) findViewById(R.id.titleText)).getText().toString().trim();
        notes = ((EditText) findViewById(R.id.notesText)).getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(dueDate) || TextUtils.isEmpty(dueTime)) {
            Toast.makeText(getApplicationContext(), getString(R.string.fill_event), Toast.LENGTH_SHORT).show();
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
        HelperUtils.hideSoftKeyboard(ScheduleItemActivity.this, v);

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

    public void pickTime(View v) {
        HelperUtils.hideSoftKeyboard(ScheduleItemActivity.this, v);

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
