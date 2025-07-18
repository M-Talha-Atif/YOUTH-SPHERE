// EditInstructorBottomSheetDialog.java
package com.event.youthSphere.bottomsheet.coordinator.instructor;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseCoordinateHelper;
import com.event.youthSphere.models.users.InstructorUser;

import android.app.DatePickerDialog;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;


public class EditInstructorBottomSheetDialog extends DialogFragment {

    private final InstructorUser instructor;
    private final Runnable onUpdated;

    public EditInstructorBottomSheetDialog(InstructorUser instructor, Runnable onUpdated) {
        this.instructor = instructor;
        this.onUpdated = onUpdated;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.bottomsheet_edit_instructor, container, false);

        EditText etFullName = view.findViewById(R.id.etInstructorName);
        Spinner spinnerField = view.findViewById(R.id.spinnerFieldOfSpecialization);


        EditText etEmployment = view.findViewById(R.id.etEmploymentStartDate);
        Button btnSave = view.findViewById(R.id.btnSave);

        etFullName.setText(instructor.getFullName());
        etEmployment.setText(instructor.getEmploymentStartDate());

        etEmployment.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                    (view1, year, month, day) -> {
                        String selectedDate = String.format("%02d/%02d/%04d", day, month + 1, year);
                        etEmployment.setText(selectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Disallow past dates
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePicker.show();
        });


        // Setup spinner
        List<String> fields = Arrays.asList("Science", "Society", "Creativity");
        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, fields);
        spinnerField.setAdapter(fieldAdapter);

        // Select current specialization
        int currentIndex = fields.indexOf(instructor.getFieldOfSpecialization());
        if (currentIndex != -1) spinnerField.setSelection(currentIndex);


        btnSave.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String field = spinnerField.getSelectedItem().toString();

            String date = etEmployment.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(field) || TextUtils.isEmpty(date)) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            instructor.setFullName(name);
            instructor.setActivityIds(instructor.getActivityIds());
            instructor.setFieldOfSpecialization(field);
            instructor.setEmploymentStartDate(date);

            FirebaseCoordinateHelper.updateInstructor(requireContext(), instructor, task -> {
                if (task.isSuccessful()) {
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(), "Instructor updated", Toast.LENGTH_SHORT).show();
                    }
                    onUpdated.run();
                    dismiss();
                } else {
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        return view;
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
