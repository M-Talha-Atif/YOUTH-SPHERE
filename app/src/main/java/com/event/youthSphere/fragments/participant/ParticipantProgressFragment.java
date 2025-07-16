package com.event.youthSphere.fragments.participant;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseParticipantHelper;
import com.event.youthSphere.models.users.ParticipantUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ParticipantProgressFragment extends Fragment {

    private TextView txtName, txtTotalActivities, txtAvgScore, txtDomainDistribution;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_participant_progress, container, false);

        txtName = view.findViewById(R.id.txtName);
        txtTotalActivities = view.findViewById(R.id.txtTotalActivities);
        txtAvgScore = view.findViewById(R.id.txtAvgScore);
        txtDomainDistribution = view.findViewById(R.id.txtDomainDistribution);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    ParticipantUser user = snapshot.toObject(ParticipantUser.class);
                    if (user != null) {
                        txtName.setText(user.getFullName());

                        FirebaseParticipantHelper.getProgressData(user.getUid(), (progress, error) -> {
                            if (progress == null) return;

                            txtTotalActivities.setText("Activities Attended: " + progress.getTotalActivities());
                            txtAvgScore.setText("Avg Feedback Score: " + String.format("%.2f", progress.getAverageScore()));

                            StringBuilder domainStats = new StringBuilder();
                            Map<String, Float> domainAverages = progress.getDomainWiseAverage();

                            for (String domain : progress.getDomainParticipation().keySet()) {
                                domainStats.append(domain)
                                        .append(": ")
                                        .append(progress.getDomainParticipation().get(domain))
                                        .append(" | Avg: ")
                                        .append(String.format("%.1f", domainAverages.getOrDefault(domain, 0f)))
                                        .append("\n");
                            }

                            txtDomainDistribution.setText(domainStats.toString().trim());
                        });
                    }
                });

        return view;
    }
}
