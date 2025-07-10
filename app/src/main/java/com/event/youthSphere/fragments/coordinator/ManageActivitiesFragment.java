package com.event.youthSphere.fragments.coordinator;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.event.youthSphere.R;
import com.event.youthSphere.adapters.ActivityAdapter;
import com.event.youthSphere.bottomsheet.AddActivityBottomSheetDialog;
import com.event.youthSphere.bottomsheet.AssignActivityBottomSheetDialog;
import com.event.youthSphere.bottomsheet.EditActivityBottomSheetDialog;
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
    Spinner spinnerMonthFilter;

    private final Map<String, String> instructorNameToId = new HashMap<>();
    private final Handler searchHandler = new Handler();
    private final int DEBOUNCE_DELAY = 400;
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manage_activities, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerActivities);
        Button btnAddActivity = rootView.findViewById(R.id.btnAddActivity);
        Button btnAssignActivity = rootView.findViewById(R.id.btnAssignActivity);
        etSearch = rootView.findViewById(R.id.etSearch);
        spinnerInstructorFilter = rootView.findViewById(R.id.spinnerInstructorFilter);
        spinnerMonthFilter = rootView.findViewById(R.id.spinnerMonthFilter);

        List<String> instructorNames = new ArrayList<>();
        instructorNames.add("All Instructors");

        FirebaseActivityHelper.fetchAllInstructors((snapshots, error) -> {
            if (error != null || snapshots == null) return;

            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                String name = Objects.requireNonNull(doc.toObject(InstructorUser.class)).getFullName();
                String id = doc.getId();
                if (name != null) {
                    instructorNames.add(name);
                    instructorNameToId.put(name, id);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, instructorNames);
            spinnerInstructorFilter.setAdapter(adapter);
            spinnerInstructorFilter.setOnItemSelectedListener(new SimpleSpinnerListener(this::applyFilters));
        });

        List<String> months = Arrays.asList("All Months", "January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October", "November", "December");

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, months);
        spinnerMonthFilter.setAdapter(monthAdapter);
        spinnerMonthFilter.setOnItemSelectedListener(new SimpleSpinnerListener(this::applyFilters));

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
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Activity")
                    .setMessage("Are you sure you want to delete this activity?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        FirebaseActivityHelper.deleteActivityAndUpdateInstructor(
                                activity.getId(),
                                activity.getInstructorId(),
                                task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(requireContext(), "Activity deleted", Toast.LENGTH_SHORT).show();
                                        loadActivities();
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to delete activity", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        adapter.setOnEditClickListener(activity -> {
            EditActivityBottomSheetDialog dialog = new EditActivityBottomSheetDialog(activity, this::loadActivities);
            dialog.show(getChildFragmentManager(), "EditActivityDialog");
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        btnAddActivity.setOnClickListener(v -> showAddActivityBottomSheet());
        btnAssignActivity.setOnClickListener(v -> showAssignActivityBottomSheet());

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
        if (!isAdded() || rootView == null) return;

        FirebaseActivityHelper.getAllActivities((value, error) -> {
            if (!isAdded() || rootView == null || error != null || value == null) return;

            activityList.clear();
            for (DocumentChange dc : value.getDocumentChanges()) {
                ActivityModel activity = dc.getDocument().toObject(ActivityModel.class);
                activityList.add(activity);
            }

            if (!isAdded() || rootView == null) return;

            requireActivity().runOnUiThread(this::applyFilters);
        });
    }

    private void applyFilters() {
        if (spinnerInstructorFilter.getSelectedItem() == null || spinnerMonthFilter.getSelectedItem() == null) return;

        String searchText = etSearch.getText().toString().toLowerCase();
        String selectedInstructor = spinnerInstructorFilter.getSelectedItem().toString();
        String selectedMonth = spinnerMonthFilter.getSelectedItem().toString();

        String selectedInstructorId = instructorNameToId.get(selectedInstructor);

        List<ActivityModel> filtered = new ArrayList<>();
        for (ActivityModel activity : activityList) {
            boolean matchesSearch = activity.getName().toLowerCase().contains(searchText)
                    || activity.getDomain().toLowerCase().contains(searchText);

            boolean matchesInstructor = selectedInstructor.equals("All Instructors")
                    || (selectedInstructorId != null && selectedInstructorId.equals(activity.getInstructorId()));

            boolean matchesMonth = selectedMonth.equals("All Months")
                    || (activity.getMonth() != null && activity.getMonth().toLowerCase().startsWith(selectedMonth.toLowerCase()));

            if (matchesSearch && matchesInstructor && matchesMonth) {
                filtered.add(activity);
            }
        }

        adapter.submitList(filtered);
    }

}
