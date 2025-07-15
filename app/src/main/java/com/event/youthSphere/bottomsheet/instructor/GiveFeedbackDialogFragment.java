package com.event.youthSphere.bottomsheet.instructor;


import android.os.Bundle;
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
import androidx.fragment.app.DialogFragment;
import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseFeedbackHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.participant.FeedbackModel;
import com.event.youthSphere.models.users.User;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GiveFeedbackDialogFragment extends DialogFragment {

    private ActivityModel activity;
    private List<User> participants;
    private String currentUserId; // parentId or instructorId
    private boolean isInstructor;

    public GiveFeedbackDialogFragment(ActivityModel activity, List<User> participants, String userId, boolean isInstructor) {
        this.activity = activity;
        this.participants = participants;
        this.currentUserId = userId;
        this.isInstructor = isInstructor;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feedback_dialog, container, false);

        Spinner spinner = view.findViewById(R.id.spinnerParticipants);
        EditText etComment = view.findViewById(R.id.etFeedbackComment);
        EditText etRating = view.findViewById(R.id.etFeedbackRating);
        Button btnSubmit = view.findViewById(R.id.btnSubmitFeedback);

        List<String> names = new ArrayList<>();
        for (User p : participants) {
            names.add(p.getFullName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names);
        spinner.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> {
            int pos = spinner.getSelectedItemPosition();
            User selected = participants.get(pos);
            int rating = Integer.parseInt(etRating.getText().toString());
            String comment = etComment.getText().toString();

            FeedbackModel feedback = new FeedbackModel(
                    activity.getId(),
                    selected.getUid(),
                    selected.getFullName(),
                    isInstructor ? currentUserId : null,
                    isInstructor ? null : currentUserId,
                    rating,
                    comment,
                    new Date()
            );

            FirebaseFeedbackHelper.addFeedback(feedback, new FirebaseFeedbackHelper.OnFeedbackAddedListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Feedback submitted!", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }
}
