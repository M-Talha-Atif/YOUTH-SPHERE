package com.event.youthSphere.bottomsheet.parent;


import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseEnrollmentHelper;
import com.event.youthSphere.firebase.FirebaseFeedbackHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.participant.EnrollmentModel;
import com.event.youthSphere.models.participant.FeedbackModel;
import com.event.youthSphere.models.users.ParticipantUser;
import com.event.youthSphere.models.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GiveFeedbackDialogFragment extends DialogFragment {

    private Spinner activitySpinner;
    private TextView instructorCommentView, instructorRatingView;
    private EditText parentCommentInput;
    private EditText parentRatingInput;

    private Button btnSubmit;

    private ParticipantUser child;
    private List<EnrollmentModel> enrollments = new ArrayList<>();

    public GiveFeedbackDialogFragment(ParticipantUser child) {
        this.child = child;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_give_feedback_parent, container, false);

        activitySpinner = view.findViewById(R.id.activitySpinner);
        instructorCommentView = view.findViewById(R.id.instructorCommentView);
        instructorRatingView = view.findViewById(R.id.instructorRatingView);
        parentCommentInput = view.findViewById(R.id.parentCommentInput);
        parentRatingInput = view.findViewById(R.id.parentRatingInput);

        btnSubmit = view.findViewById(R.id.btnSubmitFeedback);


        FirebaseEnrollmentHelper.getUserEnrollments( requireContext(), child.getUid(), task -> {
            enrollments = task.getResult();

            if (enrollments == null || enrollments.isEmpty()) {
                List<String> emptyList = new ArrayList<>();
                emptyList.add("No enrolled activities");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, emptyList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                activitySpinner.setAdapter(adapter);

                instructorCommentView.setText("N/A");
                instructorRatingView.setText("-");
                btnSubmit.setEnabled(false);
                return;
            }

            ArrayAdapter<EnrollmentModel> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, enrollments);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            activitySpinner.setAdapter(adapter);
        });


        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Object selectedItem = parent.getItemAtPosition(pos);

                // If it's not an EnrollmentModel (e.g. "No enrolled activities")
                if (!(selectedItem instanceof EnrollmentModel)) {
                    instructorCommentView.setText("N/A");
                    instructorRatingView.setText("-");
                    btnSubmit.setEnabled(false);
                    return;
                }

                EnrollmentModel selected = (EnrollmentModel) selectedItem;

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("activities").document(selected.getActivityId()).get()
                        .addOnSuccessListener(activityDoc -> {
                            ActivityModel activity = activityDoc.toObject(ActivityModel.class);
                            if (activity == null) {
                                btnSubmit.setEnabled(false);
                                return;
                            }

                            long now = System.currentTimeMillis();
                            long start = activity.getStartDate().getTime();
                            long end = activity.getEndDate().getTime();

                            // Allow only if current time is between start and end
                            boolean inFeedbackWindow = now >= start && now <= end;

                            db.collection("feedback")
                                    .whereEqualTo("participantId", child.getUid())
                                    .whereEqualTo("activityId", selected.getActivityId())
                                    .get()
                                    .addOnSuccessListener(snapshot -> {
                                        boolean hasInstructor = false;
                                        String instructorComment = "", instructorRating = "";

                                        for (DocumentSnapshot doc : snapshot) {
                                            FeedbackModel fb = doc.toObject(FeedbackModel.class);
                                            if (fb.getInstructorId() != null && fb.getParentId() == null) {
                                                hasInstructor = true;
                                                instructorComment = fb.getComment();
                                                instructorRating = String.valueOf(fb.getRating());
                                            }
                                        }

                                        instructorCommentView.setText(hasInstructor ? instructorComment : "No feedback from instructor yet.");
                                        instructorRatingView.setText(hasInstructor ? instructorRating : "-");
                                        btnSubmit.setEnabled(hasInstructor && inFeedbackWindow);
                                    });
                        });
            }


            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSubmit.setOnClickListener(v -> {



            Object selectedItem = activitySpinner.getSelectedItem();
            if (!(selectedItem instanceof EnrollmentModel)) {
                instructorCommentView.setText("N/A");
                instructorRatingView.setText("-");
                btnSubmit.setEnabled(false);
                return;
            }


            EnrollmentModel selected = (EnrollmentModel) selectedItem;


            String ratingStr = parentRatingInput.getText().toString().trim();
            if (ratingStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            int rating;
            try {
                rating = Integer.parseInt(ratingStr);
                if (rating < 1 || rating > 10) {
                    Toast.makeText(requireContext(), "Rating must be between 1 and 10", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Invalid rating value", Toast.LENGTH_SHORT).show();
                return;
            }

            FeedbackModel fb = new FeedbackModel(
                    selected.getActivityId(),
                    child.getUid(),
                    child.getFullName(),
                    null,
                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    rating,
                    parentCommentInput.getText().toString(),
                    new Date()
            );

            FirebaseFirestore.getInstance().collection("feedback")
                    .add(fb)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(requireContext(), "Feedback added", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
        });



        return view;
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

