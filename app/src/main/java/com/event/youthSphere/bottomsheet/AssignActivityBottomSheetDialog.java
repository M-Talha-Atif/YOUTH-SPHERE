package com.event.youthSphere.bottomsheet;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseActivityHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.users.InstructorUser;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class AssignActivityBottomSheetDialog extends BottomSheetDialogFragment {

    private Spinner spinnerActivities, spinnerInstructors;
    private Button btnAssign, btnClose;
    private List<ActivityModel> activities = new ArrayList<>();
    private List<InstructorUser> instructors = new ArrayList<>();

    private Spinner spinnerDomain;
    private TextView tvEmploymentStartDate;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_assign_activity, null);
        dialog.setContentView(view);

        initViews(view);
        loadActivities();
        loadInstructors();

        tvEmploymentStartDate.setOnClickListener(v -> openDatePicker());

        btnAssign.setOnClickListener(v -> assignActivity());
        btnClose.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private void initViews(View view) {
        spinnerActivities = view.findViewById(R.id.spinnerActivities);
        spinnerInstructors = view.findViewById(R.id.spinnerInstructors);
        spinnerDomain = view.findViewById(R.id.spinnerDomain);
        tvEmploymentStartDate = view.findViewById(R.id.tvEmploymentStartDate);
        btnAssign = view.findViewById(R.id.btnAssign);
        btnClose = view.findViewById(R.id.btnClose);

        ArrayAdapter<String> domainAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                Arrays.asList("Science", "Society", "Creativity")
        );
        spinnerDomain.setAdapter(domainAdapter);
    }


    private void loadActivities() {
        FirebaseActivityHelper.getAllActivities((snapshots, error) -> {
            if (error != null || snapshots == null || !isAdded() || getContext() == null) return;

            activities.clear();
            List<String> names = new ArrayList<>();

            for (QueryDocumentSnapshot doc : snapshots) {
                ActivityModel model = doc.toObject(ActivityModel.class);
                activities.add(model);
                names.add(model.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerActivities.setAdapter(adapter);
        });
    }

    private void loadInstructors() {
        if (!isAdded() || getContext() == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("role", "INSTRUCTOR")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.no_instructors_yet, Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }

                    instructors.clear();
                    List<String> names = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : query) {
                        InstructorUser instructor = doc.toObject(InstructorUser.class);
                        instructors.add(instructor);
                        names.add(instructor.getFullName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerInstructors.setAdapter(adapter);
                });
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            tvEmploymentStartDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void assignActivity() {
        int activityIndex = spinnerActivities.getSelectedItemPosition();
        int instructorIndex = spinnerInstructors.getSelectedItemPosition();
        String startDate = tvEmploymentStartDate.getText().toString().trim();
        String domain = spinnerDomain.getSelectedItem().toString();

        if (activityIndex < 0 || instructorIndex < 0 || startDate.isEmpty()) {
            Toast.makeText(requireContext(), R.string.snackbar_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        ActivityModel selectedActivity = activities.get(activityIndex);
        InstructorUser selectedInstructor = instructors.get(instructorIndex);

        FirebaseActivityHelper.assignActivityToInstructor(
                selectedActivity.getId(),
                selectedInstructor.getUid(),
                domain,
                startDate,
                task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), R.string.snackbar_activity_assigned, Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(requireContext(), R.string.snackbar_activity_failed, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

}
