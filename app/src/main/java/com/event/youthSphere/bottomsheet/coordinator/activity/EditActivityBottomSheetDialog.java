package com.event.youthSphere.bottomsheet.coordinator.activity;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseActivityHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.*;
public class EditActivityBottomSheetDialog extends DialogFragment {

    private final ActivityModel activity;
    private final Runnable onActivityUpdated;

    /**
     * Constructor*/
    public EditActivityBottomSheetDialog(ActivityModel activity, Runnable onActivityUpdated) {
        this.activity = activity;
        this.onActivityUpdated = onActivityUpdated;
    }

    /**
     * A bottom sheet dialog for assigning an existing activity to an instructor.
     * Sets up the UI components and handles the assignment logic when the user interacts with the dialog.
     *
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * @return Return a new Dialog instance to be displayed
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_update_activity, null);
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(view);

        EditText etStartDate = view.findViewById(R.id.etStartDate);
        EditText etEndDate = view.findViewById(R.id.etEndDate);

        final Calendar startCalendar = Calendar.getInstance();
        final Calendar endCalendar = Calendar.getInstance();

        EditText etName = view.findViewById(R.id.etName);
        Spinner spinnerDomain = view.findViewById(R.id.spinnerDomain);
        EditText etMinAge = view.findViewById(R.id.etMinAge);
        EditText etMaxAge = view.findViewById(R.id.etMaxAge);
        EditText etDescription = view.findViewById(R.id.etDescription);
        EditText etMaxParticipants = view.findViewById(R.id.etMaxParticipants);
        Button btnUpdate = view.findViewById(R.id.btnUpdateActivity); // use same ID, just change text to Update

        // Checkboxes
        CheckBox cbMon = view.findViewById(R.id.cbMonday);
        CheckBox cbTue = view.findViewById(R.id.cbTuesday);
        CheckBox cbWed = view.findViewById(R.id.cbWednesday);
        CheckBox cbThu = view.findViewById(R.id.cbThursday);
        CheckBox cbFri = view.findViewById(R.id.cbFriday);
        CheckBox cbSat = view.findViewById(R.id.cbSaturday);
        CheckBox cbSun = view.findViewById(R.id.cbSunday);

        DatePickerDialog.OnDateSetListener startDateListener = (view1, year, month, dayOfMonth) -> {
            startCalendar.set(year, month, dayOfMonth);
            etStartDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(startCalendar.getTime()));
        };

        DatePickerDialog.OnDateSetListener endDateListener = (view12, year, month, dayOfMonth) -> {
            endCalendar.set(year, month, dayOfMonth);
            etEndDate.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(endCalendar.getTime()));
        };

        etStartDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), startDateListener,
                    startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        etEndDate.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), endDateListener,
                    endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        // Set existing data
        etName.setText(activity.getName());
        etMinAge.setText(String.valueOf(activity.getMinAge()));
        etMaxAge.setText(String.valueOf(activity.getMaxAge()));
        etDescription.setText(activity.getDescription());
        etMaxParticipants.setText(String.valueOf(activity.getMaxParticipants()));
        btnUpdate.setText("Update Activity");

        ArrayAdapter<String> domainAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Science", "Society", "Creativity"));
        spinnerDomain.setAdapter(domainAdapter);

        int domainIndex = domainAdapter.getPosition(activity.getDomain());
        spinnerDomain.setSelection(domainIndex);

        for (String day : activity.getDays()) {
            switch (day) {
                case "MONDAY": cbMon.setChecked(true); break;
                case "TUESDAY": cbTue.setChecked(true); break;
                case "WEDNESDAY": cbWed.setChecked(true); break;
                case "THURSDAY": cbThu.setChecked(true); break;
                case "FRIDAY": cbFri.setChecked(true); break;
                case "SATURDAY": cbSat.setChecked(true); break;
                case "SUNDAY": cbSun.setChecked(true); break;
            }
        }

        btnUpdate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String domain = spinnerDomain.getSelectedItem().toString();
            String minAgeStr = etMinAge.getText().toString().trim();
            String maxAgeStr = etMaxAge.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String maxPartStr = etMaxParticipants.getText().toString().trim();

            List<String> selectedDays = new ArrayList<>();
            if (cbMon.isChecked()) selectedDays.add("MONDAY");
            if (cbTue.isChecked()) selectedDays.add("TUESDAY");
            if (cbWed.isChecked()) selectedDays.add("WEDNESDAY");
            if (cbThu.isChecked()) selectedDays.add("THURSDAY");
            if (cbFri.isChecked()) selectedDays.add("FRIDAY");
            if (cbSat.isChecked()) selectedDays.add("SATURDAY");
            if (cbSun.isChecked()) selectedDays.add("SUNDAY");

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(minAgeStr) ||
                    TextUtils.isEmpty(maxAgeStr) || TextUtils.isEmpty(description) ||
                    TextUtils.isEmpty(maxPartStr) || selectedDays.isEmpty()) {
                Snackbar.make(view, "All fields required", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(etStartDate.getText()) || TextUtils.isEmpty(etEndDate.getText())) {
                Snackbar.make(view, "Please select both start and end dates", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (startCalendar.getTime().after(endCalendar.getTime())) {
                Snackbar.make(view, "Start date cannot be after end date", Snackbar.LENGTH_SHORT).show();
                return;
            }


            int minAge = Integer.parseInt(minAgeStr);
            int maxAge = Integer.parseInt(maxAgeStr);
            int maxParticipants = Integer.parseInt(maxPartStr);

            if (minAge > maxAge || maxParticipants <= 0) {
                Snackbar.make(view, "Invalid data", Snackbar.LENGTH_SHORT).show();
                return;
            }

            activity.setName(name);
            activity.setDomain(domain);
            activity.setMinAge(minAge);
            activity.setStartDate(startCalendar.getTime());
            activity.setEndDate(endCalendar.getTime());
            activity.setMaxAge(maxAge);
            activity.setDescription(description);
            activity.setDays(selectedDays);
            activity.setMaxParticipants(maxParticipants);


            FirebaseActivityHelper.updateActivity(requireContext(), activity, task -> {
                if (task.isSuccessful()) {
                    if(getContext() != null && isAdded() ) {
                        Toast.makeText(getContext(), "Activity updated", Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                    if (onActivityUpdated != null) onActivityUpdated.run();
                }
                else {
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(), "Failed to update activity", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resize bottom sheet dialog so it doesn't span the entire width past a particular measurement
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels < 1280 ? metrics.widthPixels : 1280;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(width, height);
    }
}
