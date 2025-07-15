package com.event.youthSphere.fragments.coordinator;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.event.youthSphere.R;
import com.event.youthSphere.adapters.ActivityAdapter;
import com.event.youthSphere.bottomsheet.coordinator.activity.AddActivityBottomSheetDialog;
import com.event.youthSphere.bottomsheet.coordinator.activity.AssignActivityBottomSheetDialog;
import com.event.youthSphere.bottomsheet.coordinator.activity.EditActivityBottomSheetDialog;
import com.event.youthSphere.firebase.FirebaseActivityHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.users.InstructorUser;
import com.event.youthSphere.utils.SimpleSpinnerListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;



import java.util.*;

public class ManageActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private ArrayList<ActivityModel> activityList;
    private View rootView;

    EditText etSearch;
    Spinner spinnerInstructorFilter;

    private final Map<String, String> instructorNameToId = new HashMap<>();
    private final Handler searchHandler = new Handler();
    private final int DEBOUNCE_DELAY = 400;
    private Runnable searchRunnable;

    EditText etMinAge, etMaxAge, etMaxParticipants;

    EditText etStartDate, etEndDate;

    Spinner spinnerDayFilter;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manage_activities, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerActivities);
        Button btnAddActivity = rootView.findViewById(R.id.btnAddActivity);
        Button btnAssignActivity = rootView.findViewById(R.id.btnAssignActivity);
        etSearch = rootView.findViewById(R.id.etSearch);
        spinnerInstructorFilter = rootView.findViewById(R.id.spinnerInstructorFilter);
        etMinAge = rootView.findViewById(R.id.etMinAge);
        etMaxAge = rootView.findViewById(R.id.etMaxAge);
        etMaxParticipants = rootView.findViewById(R.id.etMaxParticipants);
        etStartDate = rootView.findViewById(R.id.etStartDate);
        etEndDate = rootView.findViewById(R.id.etEndDate);


        InputFilter[] positiveFilter = new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    try {
                        String input = dest.toString() + source.toString();
                        if (input.isEmpty()) return null;
                        int value = Integer.parseInt(input);
                        return value >= 0 ? null : "";
                    } catch (NumberFormatException e) {
                        return "";
                    }
                }
        };

        etMinAge.setFilters(positiveFilter);
        etMaxAge.setFilters(positiveFilter);
        etMaxParticipants.setFilters(positiveFilter);



        spinnerDayFilter = rootView.findViewById(R.id.spinnerDayFilter);

        List<String> dayOptions = Arrays.asList("All Days", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, dayOptions);
        spinnerDayFilter.setAdapter(dayAdapter);
        spinnerDayFilter.setOnItemSelectedListener(new SimpleSpinnerListener(this::applyFilters));





        Set<String> uniqueInstructorNamesSet = new HashSet<>();
        List<String> instructorNames = new ArrayList<>();
        instructorNames.add("All Instructors");
        uniqueInstructorNamesSet.add("All Instructors");

        FirebaseActivityHelper.fetchAllInstructors((snapshots, error) -> {
            if (error != null || snapshots == null) return;

            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                InstructorUser instructor = doc.toObject(InstructorUser.class);
                if (instructor != null) {
                    String name = instructor.getFullName();
                    String id = doc.getId();
                    if (name != null && uniqueInstructorNamesSet.add(name)) {
                        instructorNames.add(name);
                        instructorNameToId.put(name, id);
                    }
                }
            }

            if (isSafe()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_dropdown_item, instructorNames);
                spinnerInstructorFilter.setAdapter(adapter);
                spinnerInstructorFilter.setOnItemSelectedListener(new SimpleSpinnerListener(this::applyFilters));
            }
        });



        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = ManageActivitiesFragment.this::applyFilters;
                searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        });

        activityList = new ArrayList<>();
        adapter = new ActivityAdapter();

        adapter.setOnDeleteClickListener(activity -> {
            if (isSafe()) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Activity")
                        .setMessage("Are you sure you want to delete this activity?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseActivityHelper.deleteActivityAndUpdateInstructor(
                                    activity.getId(),
                                    activity.getInstructorId(),
                                    task -> {
                                        if (task.isSuccessful()) {
                                            if (!isAdded() || rootView == null) return;

                                            if (isSafe()) {
                                                Snackbar.make(rootView, "Activity deleted", Snackbar.LENGTH_SHORT).show();
                                            }
                                            loadActivities();
                                        } else {
                                            if (!isAdded() || rootView == null) return;

                                            if (isSafe()) {
                                                Snackbar.make(rootView, "Failed to delete activity", Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                            );
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });


        adapter.setOnEditClickListener(activity -> {
            EditActivityBottomSheetDialog dialog = new EditActivityBottomSheetDialog(activity, this::loadActivities);
            dialog.show(getChildFragmentManager(), "EditActivityDialog");
        });


        if (isSafe()) {

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);

        }

        btnAddActivity.setOnClickListener(v -> showAddActivityBottomSheet());
        btnAssignActivity.setOnClickListener(v -> showAssignActivityBottomSheet());

        TextWatcher autoFilterWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
        };

        // date filter

        etStartDate.setFocusable(false);
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));

        etEndDate.setFocusable(false);
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));


        etMinAge.addTextChangedListener(autoFilterWatcher);
        etMaxAge.addTextChangedListener(autoFilterWatcher);
        etMaxParticipants.addTextChangedListener(autoFilterWatcher);

        etStartDate.setOnClickListener(v -> {
            showDatePicker(etStartDate);
            applyFilters();
        });
        etEndDate.setOnClickListener(v -> {
            showDatePicker(etEndDate);
            applyFilters();
        });



        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivities();
    }

    private void showAddActivityBottomSheet() {
        AddActivityBottomSheetDialog bottomSheet = new AddActivityBottomSheetDialog(() -> {
            if (isAdded() && rootView != null) {
                loadActivities();
                Snackbar.make(rootView, R.string.snackbar_activity_added, Snackbar.LENGTH_SHORT).show();
            }
        });
        bottomSheet.show(getParentFragmentManager(), "AddActivitySheet");
    }

    private void showAssignActivityBottomSheet() {
        AssignActivityBottomSheetDialog dialog = new AssignActivityBottomSheetDialog();
        dialog.show(getParentFragmentManager(), "AssignActivityBottomSheet");
    }

    private void loadActivities() {
        if (!isSafe()) return;

        FirebaseActivityHelper.getAllActivities((value, error) -> {
            if (!isAdded() || rootView == null || error != null || value == null) return;

            activityList.clear();
            for (DocumentChange dc : value.getDocumentChanges()) {
                ActivityModel activity = dc.getDocument().toObject(ActivityModel.class);
                activityList.add(activity);
            }

            if (!isSafe()) return;

            if ( getActivity()!=null && getContext()!=null ){
                requireActivity().runOnUiThread(this::applyFilters);
            }


        });
    }

    private boolean isSafe() {
        return isAdded() && getContext() != null && rootView != null;
    }

    /**
     * Safely parses a string into an integer with a default fallback value
     * @param input The string to parse
     * @param defaultValue The value to return if parsing fails
     * @return Parsed integer or defaultValue if parsing fails
     */
    public static int parseIntSafe(String input, int defaultValue) {
        if (input == null || input.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Date parseDateSafe(String str) {
        try {
            return new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(str.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    // Format selected date
                    String dateString = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    targetEditText.setText(dateString);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }




    private void applyFilters() {
        if (spinnerInstructorFilter.getSelectedItem() == null) return;


        Date start = parseDateSafe(etStartDate.getText().toString());
        Date end = parseDateSafe(etEndDate.getText().toString());



        String searchText = etSearch.getText().toString().toLowerCase();
        String selectedInstructor = spinnerInstructorFilter.getSelectedItem().toString();

        String selectedInstructorId = instructorNameToId.get(selectedInstructor);



        String selectedDay = spinnerDayFilter.getSelectedItem().toString();
        boolean filterByDay = !selectedDay.equals("All Days");


        int minAge = parseIntSafe(etMinAge.getText().toString(), -1);
        int maxAge = parseIntSafe(etMaxAge.getText().toString(), -1);
        int maxParticipantsFilter = parseIntSafe(etMaxParticipants.getText().toString(), -1);

        List<ActivityModel> filtered = new ArrayList<>();
        for (ActivityModel activity : activityList) {
            boolean matchesSearch = activity.getName().toLowerCase().contains(searchText)
                    || activity.getDomain().toLowerCase().contains(searchText)
                    || (activity.getDescription() != null && activity.getDescription().toLowerCase().contains(searchText));

            boolean matchesInstructor = selectedInstructor.equals("All Instructors")
                    || (selectedInstructorId != null && selectedInstructorId.equals(activity.getInstructorId()));


            boolean matchesDate = true;
            if (start != null && activity.getStartDate() != null) {
                matchesDate = !activity.getStartDate().before(start);
            }
            if (matchesDate && end != null && activity.getEndDate() != null) {
                matchesDate = !activity.getEndDate().after(end);
            }

            boolean matchesAgeRange = (minAge == -1 || activity.getMinAge() >= minAge)
                    && (maxAge == -1 || activity.getMaxAge() <= maxAge);

            boolean matchesParticipants = maxParticipantsFilter == -1
                    || activity.getMaxParticipants() <= maxParticipantsFilter;


            boolean matchesDay = !filterByDay || (activity.getDays() != null && activity.getDays().contains(selectedDay));



            if (matchesSearch && matchesInstructor && matchesDate && matchesAgeRange && matchesParticipants && matchesDay) {
                filtered.add(activity);
            }


        }

        adapter.submitList(filtered);
    }

}
