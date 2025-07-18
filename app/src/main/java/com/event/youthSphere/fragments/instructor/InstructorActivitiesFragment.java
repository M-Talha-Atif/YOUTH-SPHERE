package com.event.youthSphere.fragments.instructor;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.event.youthSphere.R;
import com.event.youthSphere.adapters.InstructorActivityAdapter;
import com.event.youthSphere.bottomsheet.instructor.GiveFeedbackDialogFragment;
import com.event.youthSphere.bottomsheet.instructor.UploadPhotoDialogFragment;
import com.event.youthSphere.bottomsheet.instructor.ViewPhotosDialogFragment;
import com.event.youthSphere.firebase.FirebaseInstructorHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import java.util.*;

public class InstructorActivitiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private InstructorActivityAdapter adapter;
    private List<ActivityModel> instructorActivities = new ArrayList<>();
    private String currentInstructorId;

    public InstructorActivitiesFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_instructor_activities, container, false);

        recyclerView = root.findViewById(R.id.recyclerInstructorActivities);
        if (isSafe()) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        currentInstructorId = FirebaseInstructorHelper.getCurrentUserID();

        adapter = new InstructorActivityAdapter(instructorActivities, new InstructorActivityAdapter.OnItemClickListener() {
            @Override
            public void onUploadPhoto(ActivityModel activity) {
                Toast.makeText(getContext(), "Upload photo for: " + activity.getName(), Toast.LENGTH_SHORT).show();
                UploadPhotoDialogFragment dialog = new UploadPhotoDialogFragment(activity);
                dialog.show(getParentFragmentManager(), "UploadPhotoDialog");

            }

            @Override
            public void onGiveFeedback(ActivityModel activity) {
                FirebaseInstructorHelper.getParticipantsForActivity(activity.getId(), (participantsList, error) -> {
                    if (error != null || participantsList == null || participantsList.isEmpty()) {
                        Toast.makeText(getContext(), "No participants found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    GiveFeedbackDialogFragment dialog = new GiveFeedbackDialogFragment(
                            activity,
                            participantsList,
                            currentInstructorId,
                            true // instructor
                    );
                    dialog.show(getParentFragmentManager(), "GiveFeedbackDialog");
                });
                Toast.makeText(getContext(), "Give feedback for: " + activity.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onViewPhotos(ActivityModel activity) {
                Toast.makeText(getContext(), "Viewing photos for: " + activity.getName(), Toast.LENGTH_SHORT).show();
                ViewPhotosDialogFragment dialog = new ViewPhotosDialogFragment(activity.getId());
                dialog.show(getParentFragmentManager(), "ViewPhotosDialog");

            }
        });

        recyclerView.setAdapter(adapter);
        loadInstructorActivities();
        return root;
    }

    private boolean isSafe() {
        return isAdded() && getContext() != null;
    }

    private void loadInstructorActivities() {
        if (currentInstructorId == null) return;

        FirebaseInstructorHelper.getInstructorActivities(requireContext(), currentInstructorId, (activities, error) -> {
            instructorActivities.clear();

            if (activities != null && !activities.isEmpty()) {
                instructorActivities.addAll(activities);
            }

            // Optionally log or show offline warning
            if (error != null) {
                Log.w("InstructorActivities", "Using fallback due to error: " + error.getMessage());
                Toast.makeText(requireContext(), "Offline data loaded", Toast.LENGTH_SHORT).show();
            }

            adapter.notifyDataSetChanged();
        });

    }
}
