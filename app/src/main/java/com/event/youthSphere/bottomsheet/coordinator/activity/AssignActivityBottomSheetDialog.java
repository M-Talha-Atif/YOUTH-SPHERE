package com.event.youthSphere.bottomsheet.coordinator.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseActivityHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.users.InstructorUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class AssignActivityBottomSheetDialog extends DialogFragment {

    private Spinner spinnerActivities, spinnerInstructors;
    private Button btnAssign, btnClose;
    private List<ActivityModel> activities = new ArrayList<>();
    private List<InstructorUser> instructors = new ArrayList<>();

    private Spinner spinnerDomain;
    private TextView tvEmploymentStartDate;

   
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

    /**
     * Initializes the UI components of the dialog.
     * Sets up spinners for activity selection and domain, a text view for employment start date,
     * and buttons for assignment and closing. Also configures the domain spinner with predefined
     * activity domains.
     *
     * @param view The root view of the dialog's layout from which components are initialized
     */
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

    /**
     * Loads activities from Firebase and populates the activities spinner.
     * <p>
     * This method retrieves all existing activities via FirebaseActivityHelper and handles the results
     * through a callback. It clears the current activities list, extracts activity names from the results,
     * and creates an ArrayAdapter to populate the spinnerActivities dropdown.
     * </p>
     * <p>
     * The callback parameters include:
     * <ul>
     *   <li>{@code snapshots}: Query results containing activity documents</li>
     *   <li>{@code error}: Optional error parameter from the Firebase operation</li>
     * </ul>
     * Error or null snapshots handling ensures UI doesn't update with invalid data.
     * </p>
     */
    private void loadActivities() {
        FirebaseActivityHelper.getAllActivities(requireContext(), activityList -> {
            if (!isAdded() || getContext() == null) return;

            activities.clear();
            List<String> names = new ArrayList<>();

            for (ActivityModel model : activityList) {
                if (model.getInstructorId() == null || model.getInstructorId().isEmpty()) {
                    activities.add(model);
                    names.add(model.getName());
                }
            }

            if (activities.isEmpty()) {
                spinnerActivities.setVisibility(View.GONE);
                spinnerInstructors.setVisibility(View.GONE);
                btnAssign.setEnabled(false);
                Toast.makeText(getContext(), "No unassigned activities available.", Toast.LENGTH_SHORT).show();
            } else {
                spinnerActivities.setVisibility(View.VISIBLE);
                spinnerInstructors.setVisibility(View.VISIBLE);
                btnAssign.setEnabled(true);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerActivities.setAdapter(adapter);
            }
        });
    }


    /**
     * Loads instructors from Firebase and populates the instructor spinner.
     * <p>
     * This method queries the Firestore "users" collection for documents with the "INSTRUCTOR" role.
     * When instructors are found, the current instructor list is cleared, full names are extracted from results,
     * and a new ArrayAdapter is created to populate the spinnerInstructors dropdown.
     * If no instructors exist in the database, a toast message is shown and the dialog is dismissed.
     * </p>
     * <p>
     * The success callback handles:
     * <ul>
     *   <li>Empty query results check via {@code query.isEmpty()}</li>
     *   <li>UI Toast display when no instructors are available</li>
     *   <li>Spinner adapter population with fetched instructor data</li>
     * </ul>
     * </p>
     */
    private void loadInstructors() {
        if (!isAdded() || getContext() == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("role", "INSTRUCTOR")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        if (getContext() != null && isAdded()) {
                            Toast.makeText(getContext(), R.string.no_instructors_yet, Toast.LENGTH_SHORT).show();
                        }
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

                    if (isSafe())
                    {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerInstructors.setAdapter(adapter);

                    }


                });
    }

    /**
     * Opens a DatePickerDialog to select and update the employment start date.
     * <p>
     * This method initializes a DatePickerDialog with the current system date.
     * Upon user selection of a date, the {@link #tvEmploymentStartDate} is updated
     * to display the selected date in the format "dd/MM/yyyy".
     * </p>
     * <p>
     * The date picker is displayed with the system's current date set as the initial
     * value for convenience.
     * </p>
     */
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (isSafe()) {
            DatePickerDialog datePicker = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                tvEmploymentStartDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        }

    }

    /**
     * Handles the assignment of an activity to an instructor.
     * <p>
     * This method retrieves selected activity, instructor, domain, and employment start date from
     * UI components. It performs validation checks to ensure all fields are properly selected/entered
     * before initiating the Firebase assignment operation.
     * </p>
     * <p>
     * The validation checks include:
     * <ul>
     *   <li>Activity selection validation via {@code getSelectedItemPosition() >= 0}</li>
     *   <li>Instructor selection validation via {@code getSelectedItemPosition() >= 0}</li>
     *   <li>Employment start date validation using {@code isEmpty() check}</li>
     * </ul>
     * </p>
     * <p>
     * The assignment operation uses FirebaseActivityHelper to persist the association between:
     * <ul>
     *   <li>The selected ActivityModel's ID</li>
     *   <li>The selected InstructorUser's UID</li>
     *   <li>Activity domain (converted to uppercase)</li>
     *   <li>Employment start date (format: dd/MM/yyyy)</li>
     * </ul>
     * The success callback displays a Toast message using both getContext() and requireContext()
     * to ensure visibility regardless of fragment state changes. The dialog is only dismissed on
     * successful assignment to allow for potential retry actions with error states.
     * </p>
     * <p>
     * Error handling:
     * <ul>
     *   <li>Shows Toast via getContext() for safe fragment lifecycle handling</li>
     *   <li>Provides specific failure message from Firebase callback when assignment fails</li>
     * </ul>
     * </p>
     */
    private void assignActivity() {
        int activityIndex = spinnerActivities.getSelectedItemPosition();
        int instructorIndex = spinnerInstructors.getSelectedItemPosition();
        String startDate = tvEmploymentStartDate.getText().toString().trim();
        String domain = spinnerDomain.getSelectedItem().toString();

        if (activityIndex < 0 || instructorIndex < 0 || startDate.isEmpty()) {
            if (getContext() != null && isAdded()) {
                Toast.makeText(getContext(), R.string.snackbar_fill_all_fields, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        ActivityModel selectedActivity = activities.get(activityIndex);
        InstructorUser selectedInstructor = instructors.get(instructorIndex);

        FirebaseActivityHelper.assignActivityToInstructor(
                selectedActivity.getId(),
                selectedInstructor.getUid(),
                domain,
                startDate,
                requireContext(),
                task -> {
                    if (task.isSuccessful()) {
                        if (getContext() != null && isAdded()) {
                            Toast.makeText(getContext(), R.string.snackbar_activity_assigned, Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(requireContext(), R.string.snackbar_activity_assigned, Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        if (getContext() != null && isAdded()) {
                            Toast.makeText(getContext(), R.string.snackbar_activity_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private boolean isSafe() {
        return isAdded() && getContext() != null;
    }

}
