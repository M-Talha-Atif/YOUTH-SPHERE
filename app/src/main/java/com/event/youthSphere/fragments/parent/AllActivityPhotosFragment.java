package com.event.youthSphere.fragments.parent;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.event.youthSphere.R;
import com.event.youthSphere.adapters.ImageGalleryAdapter;
import com.event.youthSphere.firebase.FirebaseInstructorHelper;
import com.event.youthSphere.models.activity.ActivityImageModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AllActivityPhotosFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageGalleryAdapter adapter;
    private List<ActivityImageModel> fullList = new ArrayList<>();
    private List<ActivityImageModel> filteredList = new ArrayList<>();

    private EditText etSearchName, etStartDate, etEndDate;
    private Spinner spinnerDomain;
    private Handler handler = new Handler();
    private Runnable searchRunnable;

    private final int DEBOUNCE_DELAY = 400;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_all_activity_photos, container, false);

        recyclerView = root.findViewById(R.id.recyclerPhotos);
        etSearchName = root.findViewById(R.id.etSearchActivityName);
        etStartDate = root.findViewById(R.id.etStartDateFilter);
        etEndDate = root.findViewById(R.id.etEndDateFilter);
        spinnerDomain = root.findViewById(R.id.spinnerDomain);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ImageGalleryAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        List<String> domains = Arrays.asList("All Domains", "SOCIETY", "CREATIVITY", "SCIENCE"); // Add actual domains
        spinnerDomain.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, domains));

        spinnerDomain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etSearchName.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = AllActivityPhotosFragment.this::applyFilters;
                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY);
            }
        });

        etStartDate.setFocusable(false);
        etEndDate.setFocusable(false);

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        loadAllImages();

        return root;
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
            target.setText(dateStr);
            applyFilters();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadAllImages() {
        // You may need to modify this to load from all activities
        FirebaseInstructorHelper.db.collectionGroup("ActivityImages").get()
                .addOnSuccessListener(snapshots -> {
                    fullList.clear();
                    for (var doc : snapshots) {
                        ActivityImageModel model = doc.toObject(ActivityImageModel.class);
                        fullList.add(model);
                    }
                    applyFilters();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load images", Toast.LENGTH_SHORT).show());
    }

    private void applyFilters() {
        String nameSearch = etSearchName.getText().toString().toLowerCase().trim();
        String selectedDomain = spinnerDomain.getSelectedItem().toString();

        Date start = parseDateSafe(etStartDate.getText().toString());
        Date end = parseDateSafe(etEndDate.getText().toString());

        filteredList.clear();
        for (ActivityImageModel image : fullList) {
            boolean matchesName = image.getName() != null && image.getName().toLowerCase().contains(nameSearch);
            boolean matchesDomain = selectedDomain.equals("All Domains") || selectedDomain.equalsIgnoreCase(image.getDomain());

            boolean matchesDate = true;
            if (start != null || end != null) {
                Date uploadDate = parseUploadDate(image.getUploadDate());
                if (uploadDate != null) {
                    if (start != null && uploadDate.before(start)) matchesDate = false;
                    if (end != null && uploadDate.after(end)) matchesDate = false;
                }
            }

            if (matchesName && matchesDomain && matchesDate) {
                filteredList.add(image);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private Date parseUploadDate(String str) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    private Date parseDateSafe(String str) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(str);
        } catch (Exception e) {
            return null;
        }
    }
}
