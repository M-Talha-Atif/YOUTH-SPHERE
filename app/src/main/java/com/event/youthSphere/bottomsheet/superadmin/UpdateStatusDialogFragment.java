package com.event.youthSphere.bottomsheet.superadmin;


import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.event.youthSphere.R;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.participant.FeedbackModel;
import com.event.youthSphere.firebase.FirebaseSuperAdminHelper;

import java.util.Arrays;
import java.util.List;

public class UpdateStatusDialogFragment extends DialogFragment {

    private ActivityModel activity;
    private TextView tvStats;
    private Spinner spinnerStatus;
    private Button btnSubmit;

    private LinearLayout feedbackContainer;


    public UpdateStatusDialogFragment(ActivityModel activity) {
        this.activity = activity;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b){
        View v = i.inflate(R.layout.dialog_update_status, c, false);
        tvStats = v.findViewById(R.id.tvStats);
        spinnerStatus = v.findViewById(R.id.spinnerStatus);
        btnSubmit = v.findViewById(R.id.btnSubmitStatus);
        feedbackContainer = v.findViewById(R.id.feedbackContainer);


        List<String> options = Arrays.asList("Poor", "Fair", "Excellent");
        ArrayAdapter<String> a = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(a);

        tvStats.setText("Loading feedback stats...");
        btnSubmit.setEnabled(false);

        FirebaseSuperAdminHelper.getActivityFeedback(activity.getId(), (fbList, err) -> {
            if (err != null) {
                tvStats.setText("Error loading feedback.");
                return;
            }

            if (fbList.isEmpty()) {
                tvStats.setText("No reviews yet.");
                return;
            }

            int sum = 0;
            feedbackContainer.removeAllViews();

            for (FeedbackModel f : fbList) {
                sum += f.getRating();

                String by;
                if (f.getInstructorId() != null) {
                    by = "Instructor ID: " + f.getInstructorId();
                } else if (f.getParentId() != null) {
                    by = "Parent ID: " + f.getParentId();
                } else {
                    by = "Unknown";
                }

                TextView tv = new TextView(getContext());
                tv.setText("Participant: " + f.getParticipantName() +
                        "\nRating: " + f.getRating() +
                        "\nComment: " + f.getComment() +
                        "\nGiven by: " + by + "\n");

                tv.setPadding(8, 8, 8, 16);
                feedbackContainer.addView(tv);
            }

            float avg = sum / (float) fbList.size();
            tvStats.setText(String.format("Total reviews: %d\nAvg rating: %.2f", fbList.size(), avg));
            btnSubmit.setEnabled(true);
        });


        btnSubmit.setOnClickListener(x -> {
            String selected = spinnerStatus.getSelectedItem().toString();
            FirebaseSuperAdminHelper.updateActivityStatus(requireContext(), activity.getId(), selected, (ok, e2) -> {
                if (ok) {
                    Toast.makeText(getContext(), "Status updated.", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return v;
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
