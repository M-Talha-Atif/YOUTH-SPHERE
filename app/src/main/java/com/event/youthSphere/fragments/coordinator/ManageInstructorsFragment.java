package com.event.youthSphere.fragments.coordinator;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.event.youthSphere.adapters.InstructorAdapter;
import com.event.youthSphere.bottomsheet.coordinator.instructor.EditInstructorBottomSheetDialog;
import com.event.youthSphere.firebase.FirebaseCoordinateHelper;
import com.event.youthSphere.models.users.InstructorUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class ManageInstructorsFragment extends Fragment {

    private View rootView;
    private RecyclerView recyclerView;
    private InstructorAdapter adapter;
    private final List<InstructorUser> instructors = new ArrayList<>();
    private EditText etSearch;
    private Spinner spinnerDomainFilter;
    private final Handler handler = new Handler();
    private Runnable searchRunnable;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manage_instructors, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerInstructors);

        adapter = new InstructorAdapter(instructors);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);


        etSearch = rootView.findViewById(R.id.etSearchInstructor);
        spinnerDomainFilter = rootView.findViewById(R.id.spinnerDomainFilter);

// Set domain filter
        List<String> domains = Arrays.asList("All Domains", "Science", "Society", "Creativity");
        ArrayAdapter<String> domainAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, domains);
        spinnerDomainFilter.setAdapter(domainAdapter);

// Add listeners
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = ManageInstructorsFragment.this::applyFilters;
                handler.postDelayed(searchRunnable, 300);
            }
        });

        spinnerDomainFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });




        adapter.setOnEditClickListener(instructor -> {
            EditInstructorBottomSheetDialog dialog = new EditInstructorBottomSheetDialog(instructor, this::loadInstructors);
            dialog.show(getChildFragmentManager(), "EditInstructor");
        });

        adapter.setOnDeleteClickListener(instructor -> {
            if(getContext() != null) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Instructor")
                        .setMessage("Are you sure you want to delete this instructor?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseCoordinateHelper.deleteInstructor( requireContext(), instructor.getUid(), task -> {
                                if (task.isSuccessful()) {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "Instructor deleted", Toast.LENGTH_SHORT).show();
                                    }
                                    loadInstructors();
                                } else {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "Failed to delete instructor", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadInstructors();
    }

    private void loadInstructors() {
        FirebaseCoordinateHelper.getAllInstructors( requireContext(), (snapshots, error) -> {
            if (error != null || snapshots == null) return;

            instructors.clear();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                InstructorUser user = doc.toObject(InstructorUser.class);
                if (user != null) instructors.add(user);
            }
            if (!isAdded()) return;
            adapter.updateList(new ArrayList<>(instructors));
        });
    }

    private void applyFilters() {
        String search = etSearch.getText().toString().toLowerCase().trim();
        String selectedDomain = spinnerDomainFilter.getSelectedItem().toString();

        List<InstructorUser> filtered = new ArrayList<>();
        for (InstructorUser i : instructors) {
            boolean matchesName = i.getFullName() != null && i.getFullName().toLowerCase().contains(search);
            boolean matchesDomain = selectedDomain.equals("All Domains") ||
                    (i.getFieldOfSpecialization() != null && i.getFieldOfSpecialization().equals(selectedDomain));

            if (matchesName && matchesDomain) {
                filtered.add(i);
            }
        }

        adapter.updateList(filtered);
    }

}
