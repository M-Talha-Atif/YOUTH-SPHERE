package com.event.youthSphere.fragments.participant;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.event.youthSphere.R;
import com.event.youthSphere.adapters.ParticipantActivityAdapter;
import com.event.youthSphere.firebase.FirebaseActivityHelper;
import com.event.youthSphere.firebase.FirebaseEnrollmentHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.participant.EnrollmentModel;
import com.event.youthSphere.models.users.InstructorUser;
import com.event.youthSphere.utils.SimpleSpinnerListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.*;

public class ParticipantActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ParticipantActivityAdapter adapter;
    private ArrayList<ActivityModel> activityList = new ArrayList<>();
    private View rootView;

    private EditText etSearch, etMinAge, etMaxAge, etMaxParticipants;
    private Spinner spinnerInstructorFilter;

    private final Map<String, String> instructorNameToId = new HashMap<>();
    private final Handler searchHandler = new Handler();
    private final int DEBOUNCE_DELAY = 400;
    private Runnable searchRunnable;

    private String currentUserId;

    EditText etStartDate, etEndDate;

    Spinner spinnerDayFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_participant_activities, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            loadUserEnrollments();
        }

        initViews();
        setupFilters();
        loadActivities();
        return rootView;
    }

    private void initViews() {
        recyclerView = rootView.findViewById(R.id.recyclerActivities);
        etSearch = rootView.findViewById(R.id.etSearch);
        spinnerInstructorFilter = rootView.findViewById(R.id.spinnerInstructorFilter);
        etMinAge = rootView.findViewById(R.id.etMinAge);
        etMaxAge = rootView.findViewById(R.id.etMaxAge);
        etMaxParticipants = rootView.findViewById(R.id.etMaxParticipants);
        etStartDate = rootView.findViewById(R.id.etStartDate);
        etEndDate = rootView.findViewById(R.id.etEndDate);

        adapter = new ParticipantActivityAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

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
        // date filter
        etStartDate.setFocusable(false);
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));

        // days filter

        spinnerDayFilter = rootView.findViewById(R.id.spinnerDayFilter);

        List<String> dayOptions = Arrays.asList("All Days", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, dayOptions);
        spinnerDayFilter.setAdapter(dayAdapter);
        spinnerDayFilter.setOnItemSelectedListener(new SimpleSpinnerListener(this::applyFilters));

        etEndDate.setFocusable(false);
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));
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

    private Date parseDateSafe(String str) {
        try {
            return new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(str.trim());
        } catch (Exception e) {
            return null;
        }
    }


    private void setupFilters() {

        Set<String> uniqueInstructorNamesSet = new HashSet<>();
        List<String> instructorNames = new ArrayList<>(List.of("All Instructors"));
        uniqueInstructorNamesSet.add("All Instructors");

        FirebaseActivityHelper.fetchAllInstructors((snapshots, error) -> {
            if (error != null || snapshots == null) return;
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                InstructorUser instructor = doc.toObject(InstructorUser.class);
                if (instructor != null && uniqueInstructorNamesSet.add(instructor.getFullName())) {
                    instructorNames.add(instructor.getFullName());
                    instructorNameToId.put(instructor.getFullName(), doc.getId());
                }
            }

            ArrayAdapter<String> instructorAdapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, instructorNames);
            spinnerInstructorFilter.setAdapter(instructorAdapter);
            spinnerInstructorFilter.setOnItemSelectedListener(new SimpleSpinnerListener(this::applyFilters));
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = ParticipantActivitiesFragment.this::applyFilters;
                searchHandler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        });

        TextWatcher filterWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
        };

        etMinAge.addTextChangedListener(filterWatcher);
        etMaxAge.addTextChangedListener(filterWatcher);
        etMaxParticipants.addTextChangedListener(filterWatcher);
    }

    private void loadActivities() {
        FirebaseActivityHelper.getAllActivities((value, error) -> {
            if (value != null && isSafe()) {
                activityList.clear();
                for (DocumentChange dc : value.getDocumentChanges()) {
                    activityList.add(dc.getDocument().toObject(ActivityModel.class));
                }
                requireActivity().runOnUiThread(this::applyFilters);
            }
        });
    }

    private void loadUserEnrollments() {
        FirebaseEnrollmentHelper.getUserEnrollments(currentUserId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Set<String> enrolledIds = new HashSet<>();
                for (EnrollmentModel model : task.getResult()) {
                    enrolledIds.add(model.getActivityId());
                }

                adapter.setEnrolledActivityIds(enrolledIds);
                adapter.setOnEnrollClickListener((activityModel, isEnrolled) -> {
                    if (isEnrolled) {
                        FirebaseEnrollmentHelper.unenrollUser(currentUserId, activityModel.getId(), unenrollTask -> {
                            if (unenrollTask.isSuccessful()) {
                                enrolledIds.remove(activityModel.getId());
                                adapter.setEnrolledActivityIds(new HashSet<>(enrolledIds));
                            }

                            else {
                                Exception e = unenrollTask.getException();
                                String msg = e != null ? e.getMessage() : "Unenrollment failed";
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            }


                        });
                    } else {
                        FirebaseEnrollmentHelper.enrollUser(currentUserId, activityModel.getId(), enrollTask -> {
                            if (enrollTask.isSuccessful()) {

                                enrolledIds.add(activityModel.getId());
                                adapter.setEnrolledActivityIds(new HashSet<>(enrolledIds));
                            }

                            else {
                                Exception e = enrollTask.getException();
                                String msg = e != null ? e.getMessage() : "Enrollment failed";
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private void applyFilters() {
        if (!isSafe()) return;

        String searchText = etSearch.getText().toString().toLowerCase();
        String selectedInstructor = Objects.toString(spinnerInstructorFilter.getSelectedItem(), "All Instructors");
        String selectedInstructorId = instructorNameToId.get(selectedInstructor);
        int minAge = parseIntSafe(etMinAge.getText().toString(), -1);
        int maxAge = parseIntSafe(etMaxAge.getText().toString(), -1);
        int maxParticipants = parseIntSafe(etMaxParticipants.getText().toString(), -1);


        Date start = parseDateSafe(etStartDate.getText().toString());
        Date end = parseDateSafe(etEndDate.getText().toString());


        String selectedDay = spinnerDayFilter.getSelectedItem().toString();
        boolean filterByDay = !selectedDay.equals("All Days");

        List<ActivityModel> filtered = new ArrayList<>();
        for (ActivityModel a : activityList) {
            boolean matchesSearch = a.getName().toLowerCase().contains(searchText)
                    || a.getDomain().toLowerCase().contains(searchText)
                    || (a.getDescription() != null && a.getDescription().toLowerCase().contains(searchText));

            boolean matchesInstructor = selectedInstructor.equals("All Instructors")
                    || (selectedInstructorId != null && selectedInstructorId.equals(a.getInstructorId()));


            boolean matchesAgeRange = (minAge == -1 || a.getMinAge() >= minAge) && (maxAge == -1 || a.getMaxAge() <= maxAge);
            boolean matchesParticipants = maxParticipants == -1 || a.getMaxParticipants() <= maxParticipants;

            boolean matchesDate = true;
            if (start != null && a.getStartDate() != null) {
                matchesDate = !a.getStartDate().before(start);
            }
            if (matchesDate && end != null && a.getEndDate() != null) {
                matchesDate = !a.getEndDate().after(end);
            }


            boolean matchesDay = !filterByDay || (a.getDays() != null && a.getDays().contains(selectedDay));



            if (matchesSearch && matchesInstructor && matchesDate && matchesAgeRange && matchesParticipants && matchesDay) {
                filtered.add(a);
            }
        }

        adapter.submitList(filtered);
    }

    public static int parseIntSafe(String input, int defaultValue) {
        if (input == null || input.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean isSafe() {
        return isAdded() && getContext() != null && rootView != null;
    }
}
