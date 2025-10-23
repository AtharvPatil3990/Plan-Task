package com.android.plantask;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class AddTaskFragment extends Fragment {

    TextInputEditText etTitle, etDescription, etReminderDate, etReminderTime;
    TextView tvRGErrorMessage, tvReminderTimeErrorMessage;
    Button btnCancel, btnDone;
    RadioGroup rgDate;
    String selectedReminderChoice = "null";
    private boolean isReminderDateOptionSelected = false;
    private int reminderHour = 9, reminderMinute = 0, reminderDay = 0, reminderMonth = 0, reminderYear = 0;
    private String title, description;
    private long reminderTimeInMills;

    public AddTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_add_task, container, false);

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etReminderDate = view.findViewById(R.id.etReminderDate);
        etReminderTime = view.findViewById(R.id.etReminderTime);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnDone = view.findViewById(R.id.btnDone);
        rgDate = view.findViewById(R.id.rgSelectDate);
        tvRGErrorMessage = view.findViewById(R.id.tvDateErrorMessage);
        tvReminderTimeErrorMessage = view.findViewById(R.id.tvReminderTimeErrorMessage);

//        RadioButton check listener
        rgDate.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
                if(checkedId != -1){
                    RadioButton rbSelected = view.findViewById(checkedId);
                    String selectedChoiceText = rbSelected.getText().toString();

                    switch (selectedChoiceText) {
                        case "Today":
                            selectedReminderChoice = "today";
                            etReminderTime.setVisibility(View.VISIBLE);
                            etReminderDate.setVisibility(View.INVISIBLE);
                            break;
                        case "Select Custom Date":
                            etReminderDate.setVisibility(View.VISIBLE);
                            etReminderTime.setVisibility(View.VISIBLE);
                            selectedReminderChoice = "custom_date";
                            break;
                        case "Don't set a reminder":
                            selectedReminderChoice = "null";
                            etReminderTime.setVisibility(View.INVISIBLE);
                            etReminderDate.setVisibility(View.INVISIBLE);
                            break;
                    }
                    isReminderDateOptionSelected = true;
                }
            }
        });

//        Listener for date
        Calendar calendar = Calendar.getInstance();
        etReminderDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, month, dayOfMonth);
                                etReminderDate.setText(sdf.format(calendar.getTimeInMillis()));

                                reminderDay = dayOfMonth;
                                reminderMonth = month;
                                reminderYear = year;
                            }
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

//        Listener for reminder time
        etReminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTitleText("Select reminder Time")
                        .setTheme(MaterialTimePicker.INPUT_MODE_CLOCK)
                        .build();
                timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reminderHour = timePicker.getHour();
                        reminderMinute = timePicker.getMinute();
                        String amPm = (reminderHour<12) ? "am" : "pm";
                        reminderHour = reminderHour % 12;
                        reminderHour = (reminderHour == 0) ? 12 : reminderHour;
                        etReminderTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", reminderHour, reminderMinute, amPm));
                    }
                });
                timePicker.show(getParentFragmentManager(), "TIME_PICKER");
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle taskBundle = new Bundle();
                taskBundle.putBoolean("result", false);
                getParentFragmentManager().setFragmentResult("newTask", taskBundle);
                NavHostFragment.findNavController(AddTaskFragment.this).popBackStack();
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                title = etTitle.getText().toString();
                if (title.isEmpty()) {
                    etTitle.setError("Title cannot be empty");
                    etTitle.requestFocus();
                    return;
                }

                description = etDescription.getText().toString();
                if (description.isEmpty())
                    description = "null";

                if (isReminderDateOptionSelected) {
//                    Check the date selected in radio button
                    switch (selectedReminderChoice) {
                        case "today":
                            if(etReminderTime.getText().toString().isEmpty()){
                                tvReminderTimeErrorMessage.setVisibility(View.VISIBLE);
                                tvReminderTimeErrorMessage.requestFocus();
                                return;
                            }
                            calendar.set(Calendar.HOUR_OF_DAY, reminderHour);
                            calendar.set(Calendar.MINUTE, reminderMinute);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MILLISECOND, 0);
                            reminderTimeInMills = calendar.getTimeInMillis();
                            break;

                        case "custom_date":
                            if (reminderDay == 0 || reminderMonth == 0 || reminderYear == 0) {
                                etReminderDate.setError("Please select a date");
                                etReminderDate.requestFocus();
                                return;
                            }
                            calendar.set(reminderYear, reminderMonth, reminderDay, reminderHour, reminderMinute);
                            reminderTimeInMills = calendar.getTimeInMillis();
                            break;

                        case "null":
                            reminderTimeInMills = 0;
                            break;
                    }
                } else {
                    tvRGErrorMessage.setVisibility(View.VISIBLE);
                    tvRGErrorMessage.requestFocus();
                    return;
                }

                Bundle taskBundle = new Bundle();
                taskBundle.putBoolean("result", true);
                taskBundle.putString("title", title);
                taskBundle.putString("description", description);
                taskBundle.putLong("reminderTimeInMills", reminderTimeInMills);
                getParentFragmentManager().setFragmentResult("newTask", taskBundle);

                NavHostFragment.findNavController(AddTaskFragment.this).popBackStack();

            }
        });
        return view;
    }
}