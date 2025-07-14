package com.event.youthSphere.bottomsheet.instructor;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.*;
import com.event.youthSphere.R;
import com.event.youthSphere.adapters.ImageGalleryAdapter;
import com.event.youthSphere.firebase.FirebaseInstructorHelper;
import com.event.youthSphere.models.activity.ActivityImageModel;

import java.util.*;

public class ViewPhotosDialogFragment extends DialogFragment {

    private final String activityId;
    private RecyclerView recyclerView;
    private ImageGalleryAdapter adapter;
    private List<ActivityImageModel> images = new ArrayList<>();

    public ViewPhotosDialogFragment(String activityId) {
        this.activityId = activityId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_view_photos, container, false);
        recyclerView = view.findViewById(R.id.recyclerImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ImageGalleryAdapter(images);
        recyclerView.setAdapter(adapter);

        loadImages();
        return view;
    }

    private void loadImages() {
        FirebaseInstructorHelper.getActivityImages(activityId, (result, error) -> {
            if (error != null || result == null) {
                Toast.makeText(getContext(), "Failed to load images", Toast.LENGTH_SHORT).show();
                return;
            }

            images.clear();
            images.addAll(result);
            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Called when the fragment is visible to the user and actively running
     * Adjusts the dialog window size
     */
    @Override
    public void onResume() {
        super.onResume();

        // Resize bottom sheet dialog so it doesn't span the entire width past a particular measurement
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels < 1280 ? metrics.widthPixels : 1280;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(width, height);
    }
}
