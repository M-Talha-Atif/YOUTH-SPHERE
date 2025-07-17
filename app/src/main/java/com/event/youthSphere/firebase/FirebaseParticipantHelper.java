package com.event.youthSphere.firebase;

import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.participant.EnrollmentModel;
import com.event.youthSphere.models.participant.ParticipantProgressData;
import com.event.youthSphere.models.participant.FeedbackModel;
import com.event.youthSphere.utils.TaskUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class FirebaseParticipantHelper {

    public static void getProgressData(String participantId, BiConsumer<ParticipantProgressData, Exception> callback) {
        FirebaseEnrollmentHelper.getUserEnrollments(participantId, enrollmentTask -> {
            List<EnrollmentModel> enrollments = enrollmentTask.getResult();

            if (enrollments == null || enrollments.isEmpty()) {
                callback.accept(new ParticipantProgressData(), null); // empty data
                return;
            }

            FirebaseFirestore.getInstance()
                    .collection("feedback")
                    .whereEqualTo("participantId", participantId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<FeedbackModel> feedbackList = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            FeedbackModel model = doc.toObject(FeedbackModel.class);
                            if (model != null && model.getRating() > 0)
                                feedbackList.add(model);
                        }

                        Map<String, Integer> domainCount = new HashMap<>();
                        Map<String, List<Integer>> domainRatings = new HashMap<>();

                        AtomicInteger totalScore = new AtomicInteger(0);
                        AtomicInteger scoreCount = new AtomicInteger(0);
                        AtomicInteger loaded = new AtomicInteger(0);
                        AtomicInteger validActivities = new AtomicInteger(0);

                        int total = enrollments.size();

                        for (EnrollmentModel enrollment : enrollments) {
                            FirebaseFirestore.getInstance().collection("activities")
                                    .document(enrollment.getActivityId())
                                    .get()
                                    .addOnSuccessListener(activityDoc -> {
                                        String domain = activityDoc.getString("domain");
                                        if (domain == null) domain = "Unknown";

                                        Date startDate = activityDoc.getDate("startDate");
                                        if (startDate != null && startDate.after(new Date())) {
                                            // Future activity — skip
                                            if (loaded.incrementAndGet() == total) {
                                                finalizeProgressData(callback, domainCount, domainRatings, totalScore, scoreCount, validActivities.get());
                                            }
                                            return;
                                        }

                                        // Only include activity if it’s ongoing or past
                                        validActivities.incrementAndGet();
                                        domainCount.put(domain, domainCount.getOrDefault(domain, 0) + 1);

                                        for (FeedbackModel f : feedbackList) {
                                            if (f.getActivityId().equals(enrollment.getActivityId())) {
                                                totalScore.addAndGet(f.getRating());
                                                scoreCount.incrementAndGet();

                                                domainRatings.putIfAbsent(domain, new ArrayList<>());
                                                domainRatings.get(domain).add(f.getRating());
                                            }
                                        }

                                        if (loaded.incrementAndGet() == total) {
                                            finalizeProgressData(callback, domainCount, domainRatings, totalScore, scoreCount, validActivities.get());
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (loaded.incrementAndGet() == total) {
                                            finalizeProgressData(callback, domainCount, domainRatings, totalScore, scoreCount, validActivities.get());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> callback.accept(null, e));
        });
    }

    private static void finalizeProgressData(
            BiConsumer<ParticipantProgressData, Exception> callback,
            Map<String, Integer> domainCount,
            Map<String, List<Integer>> domainRatings,
            AtomicInteger totalScore,
            AtomicInteger scoreCount,
            int validTotal
    ) {
        ParticipantProgressData data = new ParticipantProgressData();
        data.setTotalActivities(validTotal);
        data.setAverageScore(scoreCount.get() == 0 ? 0 : totalScore.get() / (float) scoreCount.get());
        data.setDomainParticipation(domainCount);
        data.setDomainAvgScores(domainRatings);
        callback.accept(data, null);
    }



    public static void getRegisteredActivities(String participantId, BiConsumer<List<ActivityModel>, Exception> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("enrollments")
                .whereEqualTo("participantId", participantId)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        callback.accept(new ArrayList<>(), null);
                        return;
                    }

                    List<ActivityModel> activities = new ArrayList<>();
                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                    for (DocumentSnapshot snapshot : query.getDocuments()) {
                        String activityId = snapshot.getString("activityId");
                        if (activityId != null) {
                            Task<DocumentSnapshot> task = db.collection("activities").document(activityId).get();
                            tasks.add(task);
                        }
                    }

                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                        for (Object obj : results) {
                            if (obj instanceof DocumentSnapshot) {
                                DocumentSnapshot doc = (DocumentSnapshot) obj;
                                ActivityModel model = doc.toObject(ActivityModel.class);
                                if (model != null) activities.add(model);
                            }
                        }
                        callback.accept(activities, null);
                    }).addOnFailureListener(e -> callback.accept(null, e));
                })
                .addOnFailureListener(e -> callback.accept(null, e));
    }


}
