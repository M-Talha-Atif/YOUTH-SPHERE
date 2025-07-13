package com.event.youthSphere.fragments.parent;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.event.youthSphere.adapters.ParentActivityAdapter;
import com.event.youthSphere.bottomsheet.coordinator.activity.AddActivityBottomSheetDialog;
import com.event.youthSphere.bottomsheet.coordinator.activity.AssignActivityBottomSheetDialog;
import com.event.youthSphere.bottomsheet.coordinator.activity.EditActivityBottomSheetDialog;
import com.event.youthSphere.bottomsheet.instructor.ViewPhotosDialogFragment;
import com.event.youthSphere.firebase.FirebaseActivityHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.users.InstructorUser;
import com.event.youthSphere.utils.SimpleSpinnerListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.*;

public class ViewActivitiesWithPhotosFragment extends Fragment {

    private RecyclerView recyclerView;
    private ParentActivityAdapter adapter;
    private ArrayList<ActivityModel> activityList;
    private View rootView;

    EditText etSearch;
    Spinner spinnerInstructorFilter;
    Spinner spinnerMonthFilter;

    private final Map<String, String> instructorNameToId = new HashMap<>();
    private final Handler searchHandler = new Handler();
    private final int DEBOUNCE_DELAY = 400;
    private Runnable searchRunnable;

    EditText etMinAge, etMaxAge, etMaxParticipants;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_view_activities_with_photos, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerActivities);


        etSearch = rootView.findViewById(R.id.etSearch);
        spinnerInstructorFilter = rootView.findViewById(R.id.spinnerInstructorFilter);
        spinnerMonthFilter = rootView.findViewById(R.id.spinnerMonthFilter);
        etMinAge = rootView.findViewById(R.id.etMinAge);
        etMaxAge = rootView.findViewById(R.id.etMaxAge);
        etMaxParticipants = rootView.findViewById(R.id.etMaxParticipants);

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


        List<String> months = Arrays.asList("All Months", "January", "February", "March", "April", "May",
                "June", "July", "August", "September", "October", "November", "December");


        if (isSafe()) {

            ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item, months);
            spinnerMonthFilter.setAdapter(monthAdapter);
            spinnerMonthFilter.setOnItemSelectedListener(new SimpleSpinnerListener(this::applyFilters));

        }

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = ViewActivitiesWithPhotosFragment.this::applyFilters;
                searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        });

        activityList = new ArrayList<>();
        adapter = new ParentActivityAdapter(activity -> {
            ViewPhotosDialogFragment dialog = new ViewPhotosDialogFragment(activity.getId());
            dialog.show(getParentFragmentManager(), "ViewPhotosDialog");
        });



        if (isSafe()) {

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);

        }



        TextWatcher autoFilterWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
        };

        etMinAge.addTextChangedListener(autoFilterWatcher);
        etMaxAge.addTextChangedListener(autoFilterWatcher);
        etMaxParticipants.addTextChangedListener(autoFilterWatcher);


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivities();
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


    private void applyFilters() {
        if (spinnerInstructorFilter.getSelectedItem() == null || spinnerMonthFilter.getSelectedItem() == null) return;

        String searchText = etSearch.getText().toString().toLowerCase();
        String selectedInstructor = spinnerInstructorFilter.getSelectedItem().toString();
        String selectedMonth = spinnerMonthFilter.getSelectedItem().toString();

        String selectedInstructorId = instructorNameToId.get(selectedInstructor);

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

            boolean matchesMonth = selectedMonth.equals("All Months")
                    || (activity.getMonth() != null && activity.getMonth().toLowerCase().startsWith(selectedMonth.toLowerCase()));

            boolean matchesAgeRange = (minAge == -1 || activity.getMinAge() >= minAge)
                    && (maxAge == -1 || activity.getMaxAge() <= maxAge);

            boolean matchesParticipants = maxParticipantsFilter == -1
                    || activity.getMaxParticipants() <= maxParticipantsFilter;


            if (matchesSearch && matchesInstructor && matchesMonth && matchesAgeRange && matchesParticipants) {
                filtered.add(activity);
            }

        }

        adapter.submitList(filtered);
    }

}
