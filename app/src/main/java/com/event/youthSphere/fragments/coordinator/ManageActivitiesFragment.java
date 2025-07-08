package com.event.youthSphere.fragments.coordinator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.event.youthSphere.R;
import com.event.youthSphere.adapters.ActivityAdapter;
import com.event.youthSphere.bottomsheet.AddActivityBottomSheetDialog;
import com.event.youthSphere.bottomsheet.AssignActivityBottomSheetDialog;
import com.event.youthSphere.firebase.FirebaseActivityHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import java.util.ArrayList;

public class ManageActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private ArrayList<ActivityModel> activityList;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manage_activities, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerActivities);
        Button btnAddActivity = rootView.findViewById(R.id.btnAddActivity);
        Button btnAssignActivity = rootView.findViewById(R.id.btnAssignActivity);

        activityList = new ArrayList<>();
        adapter = new ActivityAdapter();
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

            requireActivity().runOnUiThread(() -> {
                adapter.submitList(new ArrayList<>(activityList));
            });
        });
    }
}
