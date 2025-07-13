package com.event.youthSphere.bottomsheet.coordinator.activity;


import android.annotation.SuppressLint;
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



/**
 * A bottom sheet dialog for adding new activities to the system.
 * Handles activity creation with validation and posts data to Firebase.
 */
public class AddActivityBottomSheetDialog extends DialogFragment {

    private final Runnable onActivityAdded;


    /**
     * Constructs a new AddActivityBottomSheetDialog
     * @param onActivityAdded Callback to execute after successful activity addition
     */
    public AddActivityBottomSheetDialog(Runnable onActivityAdded) {
        this.onActivityAdded = onActivityAdded;
    }

    /**
     * Creates the dialog and sets up its content view
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * @return Return a new Dialog instance to be displayed
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_activity, null);
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
        Button btnAdd = view.findViewById(R.id.btnAddActivity);

        // Find all checkboxes
        CheckBox cbMonday = view.findViewById(R.id.cbMonday);
        CheckBox cbTuesday = view.findViewById(R.id.cbTuesday);
        CheckBox cbWednesday = view.findViewById(R.id.cbWednesday);
        CheckBox cbThursday = view.findViewById(R.id.cbThursday);
        CheckBox cbFriday = view.findViewById(R.id.cbFriday);
        CheckBox cbSaturday = view.findViewById(R.id.cbSaturday);
        CheckBox cbSunday = view.findViewById(R.id.cbSunday);

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


        ArrayAdapter<String> domainAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Science", "Society", "Creativity"));
        spinnerDomain.setAdapter(domainAdapter);

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String domain = spinnerDomain.getSelectedItem().toString().toUpperCase();
            String minAgeStr = etMinAge.getText().toString().trim();
            String maxAgeStr = etMaxAge.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String maxPartStr = etMaxParticipants.getText().toString().trim();

            // Collect selected days from checkboxes
            List<String> selectedDays = new ArrayList<>();
            if (cbMonday.isChecked()) selectedDays.add("MONDAY");
            if (cbTuesday.isChecked()) selectedDays.add("TUESDAY");
            if (cbWednesday.isChecked()) selectedDays.add("WEDNESDAY");
            if (cbThursday.isChecked()) selectedDays.add("THURSDAY");
            if (cbFriday.isChecked()) selectedDays.add("FRIDAY");
            if (cbSaturday.isChecked()) selectedDays.add("SATURDAY");
            if (cbSunday.isChecked()) selectedDays.add("SUNDAY");

            // Validation
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(minAgeStr) || TextUtils.isEmpty(maxAgeStr)
                    || TextUtils.isEmpty(description) || TextUtils.isEmpty(maxPartStr)) {
                Snackbar.make(view, R.string.snackbar_activity_failed, Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (selectedDays.isEmpty()) {
                Snackbar.make(view, "Please select at least one day", Snackbar.LENGTH_SHORT).show();
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


            try {
                int minAge = Integer.parseInt(minAgeStr);
                int maxAge = Integer.parseInt(maxAgeStr);
                int maxParticipants = Integer.parseInt(maxPartStr);

                // Validate age range
                if (minAge > maxAge) {
                    Snackbar.make(view, "Minimum age cannot be greater than maximum age", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (minAge < 0 || maxAge < 0) {
                    Snackbar.make(view, "Age cannot be negative", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (maxParticipants <= 0) {
                    Snackbar.make(view, "Maximum participants must be greater than 0", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // Set month to NEXT MONTH
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, 1);
                String nextMonth = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.getTime());

                ActivityModel model = new ActivityModel(
                        null,
                        name,
                        domain,
                        minAge,
                        maxAge,
                        description,
                        selectedDays,
                        maxParticipants,
                        nextMonth,
                        null,
                        0,
                        startCalendar.getTime(),
                        endCalendar.getTime()
                );


                FirebaseActivityHelper.createActivity(model, task -> {
                    if (task.isSuccessful()) {
                        dismiss();
                        if (onActivityAdded != null) onActivityAdded.run();
                    } else {
                        Snackbar.make(view, R.string.snackbar_activity_failed, Snackbar.LENGTH_SHORT).show();
                    }
                });

            } catch (NumberFormatException e) {
                Snackbar.make(view, "Please enter valid numbers for age and participants", Snackbar.LENGTH_SHORT).show();
            }
        });

        return dialog;
    }



    /**
     * Called when the fragment is visible to the user and actively running
     * Adjusts the dialog window size
     */
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