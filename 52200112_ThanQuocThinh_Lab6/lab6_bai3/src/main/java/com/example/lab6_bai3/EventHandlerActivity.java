package com.example.lab6_bai3;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Objects;
import java.util.stream.IntStream;

public class EventHandlerActivity extends AppCompatActivity  {

    private final Calendar rightNow = Calendar.getInstance();
    private static final int OPTION_NAME = 0, OPTION_PLACE = 1, OPTION_DATE = 2, OPTION_TIME = 3;

    private TextInputEditText txtEventName,txtEventPlace, txtEventDate, txtEventTime;
    private TextInputLayout txtLayoutName, txtLayoutPlace, txtLayoutDate, txtLayoutTime;

    private int accessMode = -1, editId = -1;
    private boolean booleanId = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        setUpVariables();
        setUpEventsListener();
        setUpDataIfAvailable();
    }

    private void setUpVariables(){
        txtEventName = findViewById(R.id.txtEventName);
        txtEventPlace = findViewById(R.id.txtEventPlace);
        txtEventDate = findViewById(R.id.txtEventDate);
        txtEventTime = findViewById(R.id.txtEventTime);
        txtLayoutName = findViewById(R.id.txtLayoutName);
        txtLayoutPlace = findViewById(R.id.txtLayoutPlace);
        txtLayoutDate = findViewById(R.id.txtLayoutDate);
        txtLayoutTime = findViewById(R.id.txtLayoutTime);
        txtEventName.requestFocus();
    }

    private void setUpEventsListener(){
        txtEventPlace.setOnFocusChangeListener(((view, focus) -> {
            if (focus){ showDialogChoosePlace(); }
        }));
        txtEventDate.setOnFocusChangeListener(((view, focus) -> {
            if (focus) { showDialogChooseDate(); }
        }));
        txtEventTime.setOnFocusChangeListener(((view, focus) -> {
            if (focus) { showDialogChooseTime(); }
        }));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpDataIfAvailable(){
        Intent intent = getIntent();
        accessMode = intent.getIntExtra("requestId", MainActivity.RESULT_CODE_CREATE_EVENT);
        if (accessMode == MainActivity.RESULT_CODE_EDIT_EVENT ){
            setTitle("Edit event");
            Event event = (Event) intent.getSerializableExtra("event");
            assert event != null;
            editId = event.getId();
            booleanId = event.isStatus();
            txtEventName.setText(event.getName());
            txtEventPlace.setText(event.getPlace());
            txtEventDate.setText(event.getDateStringFormat());
            txtEventTime.setText(event.getTimeStringFormat());
        }
        else{
            setTitle("Create a new event");
        }
    }

    private void showDialogChoosePlace(){
        txtEventPlace.clearFocus();
        // List to store options
        String[] listOption = new String[] { "C201", "C202", "C203", "C204" };
        // Default index and selected index
        int defaultIndex =
            IntStream.range(0, listOption.length)
                .filter(i -> listOption[i].equals(Objects.requireNonNull(txtEventPlace.getText()).toString()))
                .findFirst().orElse(0);
        final int[] selectedIndex = {defaultIndex};
        // Set up alert dialog builder
        new AlertDialog.Builder(this)
            .setTitle("Select place")
            .setSingleChoiceItems(listOption, defaultIndex, ((dialogInterface, i) -> {
                selectedIndex[0] = i;
            }))
            .setPositiveButton("OK", ((dialogInterface, i) -> {
                txtEventPlace.setText(listOption[selectedIndex[0]]);
            }))
            .setOnCancelListener((dialog -> {
                txtEventPlace.setText(listOption[selectedIndex[0]]);
            }))
            .show();
    }

    private void showDialogChooseDate(){
        txtEventDate.clearFocus();
        String dateStr = Objects.requireNonNull(txtEventDate.getText()).toString();
        // Set up default  value
        int defaultYear = rightNow.get(Calendar.YEAR);
        int defaultMonth = rightNow.get(Calendar.MONTH);
        int defaultDayOfMonth = rightNow.get(Calendar.DAY_OF_MONTH);
        if (!dateStr.isEmpty()){
            String[] s = dateStr.split("/");
            defaultYear = Integer.parseInt(s[2]);
            defaultMonth = Integer.parseInt(s[1]) - 1;
            defaultDayOfMonth = Integer.parseInt(s[0]);
        }
        // Set date picker dialog
        @SuppressLint("DefaultLocale")
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (datePicker, year, month, dayOfMonth) -> {
                txtEventDate.setText( String.format("%d/%d/%d", dayOfMonth, month+1, year) );
            },
            defaultYear,
            defaultMonth,
            defaultDayOfMonth
        );
        datePickerDialog.show();
    }

    private void showDialogChooseTime(){
        txtEventTime.clearFocus();
        // Set up default variable
        String timeStr = Objects.requireNonNull(txtEventTime.getText()).toString();
        int hour = rightNow.get(Calendar.HOUR_OF_DAY);
        int minute = rightNow.get(Calendar.MINUTE);
        if (!timeStr.isEmpty()){
            String[] s = timeStr.split(":");
            hour = Integer.parseInt(s[0]);
            minute = Integer.parseInt(s[1]);
        }
        // Set up time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this, 0, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                txtEventTime.setText( String.format("%02d:%02d", hour, minute) );
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveInformation(){
        if (checkCondition()){
            Intent intent = new Intent();
            intent.putExtra("event", processEventResult());
            setResult(accessMode, intent);
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Event processEventResult(){
        String[] date = Objects.requireNonNull(txtEventDate.getText()).toString().split("/");
        String[] time = Objects.requireNonNull(txtEventTime.getText()).toString().split(":");
        String name = Objects.requireNonNull(txtEventName.getText()).toString();
        String place = Objects.requireNonNull(txtEventPlace.getText()).toString();
        LocalDateTime dateTime = LocalDateTime.of(
            Integer.parseInt(date[2]),
            Integer.parseInt(date[1]),
            Integer.parseInt(date[0]),
            Integer.parseInt(time[0]),
            Integer.parseInt(time[1]),
            0
        );
        return accessMode == MainActivity.RESULT_CODE_CREATE_EVENT ?
            new Event(name, place, dateTime, false ) :
            new Event(editId, name, place, dateTime, booleanId);
    }

    private boolean checkCondition(){
        // Check condition of name option
        if (Objects.requireNonNull(txtEventName.getText()).toString().isEmpty()){
            setErrorOption(OPTION_NAME, true);
            return false;
        } else setErrorOption(OPTION_NAME, false);

        // Check condition of place option
        if (Objects.requireNonNull(txtEventPlace.getText()).toString().isEmpty()){
            setErrorOption(OPTION_PLACE, true);
            return false;
        } else setErrorOption(OPTION_PLACE, false);

        // Check condition of date option
        if (Objects.requireNonNull(txtEventDate.getText()).toString().isEmpty()){
            setErrorOption(OPTION_DATE, true);
            return false;
        } else setErrorOption(OPTION_DATE, false);

        // Check condition of time option
        if (Objects.requireNonNull(txtEventTime.getText()).toString().isEmpty()){
            setErrorOption(OPTION_TIME, true);
            return false;
        } else setErrorOption(OPTION_TIME, false);

        return true;
    }

    private void setErrorOption(int optionId, boolean isError){
        switch (optionId){
            case OPTION_NAME: {
                txtEventName.setBackgroundTintList(
                    isError ? ContextCompat.getColorStateList(this, R.color.red) : null
                );
                txtEventName.setTextColor(
                    isError ? ContextCompat.getColorStateList(this, R.color.red)
                            : ContextCompat.getColorStateList(this, R.color.black)
                );
                txtEventName.setHintTextColor(
                        isError ? ContextCompat.getColorStateList(this, R.color.red)
                                : ContextCompat.getColorStateList(this, R.color.black)
                );
                txtLayoutName.setHelperText(
                    isError ? ContextCompat.getString(this, R.string.helper_event_name) : ""
                );
                break;
            }
            case OPTION_PLACE: {
                txtEventPlace.setBackgroundTintList(
                    isError ? ContextCompat.getColorStateList(this, R.color.red) : null
                );
                txtEventPlace.setTextColor(
                        isError ? ContextCompat.getColorStateList(this, R.color.red)
                                : ContextCompat.getColorStateList(this, R.color.black)
                );
                txtEventPlace.setHintTextColor(
                        isError ? ContextCompat.getColorStateList(this, R.color.red)
                                : ContextCompat.getColorStateList(this, R.color.black)
                );
                txtLayoutPlace.setHelperText(
                    isError ? ContextCompat.getString(this, R.string.helper_event_place) : ""
                );
                break;
            }
            case OPTION_DATE: {
                txtEventDate.setBackgroundTintList(
                    isError ? ContextCompat.getColorStateList(this, R.color.red) : null
                );
                txtEventDate.setTextColor(
                        isError ? ContextCompat.getColorStateList(this, R.color.red)
                                : ContextCompat.getColorStateList(this, R.color.black)
                );
                txtEventDate.setHintTextColor(
                        isError ? ContextCompat.getColorStateList(this, R.color.red)
                                : ContextCompat.getColorStateList(this, R.color.black)
                );
                txtLayoutDate.setHelperText(
                    isError ? ContextCompat.getString(this, R.string.helper_event_date) : ""
                );
                break;
            }
            default: {
                txtEventTime.setBackgroundTintList(
                    isError ? ContextCompat.getColorStateList(this, R.color.red) : null
                );
                txtEventTime.setTextColor(
                        isError ? ContextCompat.getColorStateList(this, R.color.red)
                                : ContextCompat.getColorStateList(this, R.color.black)
                );
                txtEventTime.setHintTextColor(
                        isError ? ContextCompat.getColorStateList(this, R.color.red)
                                : ContextCompat.getColorStateList(this, R.color.black)
                );
                txtLayoutTime.setHelperText(
                    isError ? ContextCompat.getString(this, R.string.helper_event_time) : ""
                );
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_create_event, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menuSave){
            saveInformation();
        }
        return true;
    }
}