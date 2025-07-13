package com.event.youthSphere.firebase;

import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.participant.EnrollmentModel;
import com.event.youthSphere.utils.TaskUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FirebaseEnrollmentHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final long startTimeMillis = System.currentTimeMillis();
    private static final long endTimeMillis = startTimeMillis +   10 * 1000;

    public static void enrollUser(String participantId, String activityId, OnCompleteListener<Void> listener) {
        long now = System.currentTimeMillis();

        if (now < startTimeMillis || now > endTimeMillis) {
            listener.onComplete(TaskUtils.failure("Enrollment period is closed"));
            return;
        }

        db.collection("enrollments")
                .whereEqualTo("participantId", participantId)
                .whereEqualTo("activityId", activityId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        listener.onComplete(TaskUtils.failure("You are already enrolled in this activity"));
                        return;
                    }

                    db.collection("activities").document(activityId).get()
                            .addOnSuccessListener(snapshot -> {
                                if (!snapshot.exists()) {
                                    listener.onComplete(TaskUtils.failure("Activity not found"));
                                    return;
                                }

                                ActivityModel activity = snapshot.toObject(ActivityModel.class);
                                if (activity == null || activity.getRegisteredCount() >= activity.getMaxParticipants()) {
                                    listener.onComplete(TaskUtils.failure("Max participants limit reached"));
                                    return;
                                }

                                String enrollmentId = UUID.randomUUID().toString();
                                Map<String, Object> data = new HashMap<>();
                                data.put("enrollmentId", enrollmentId);
                                data.put("participantId", participantId);
                                data.put("activityId", activityId);
                                data.put("name", activity.getName());
                                data.put("description", activity.getDescription());
                                data.put("domain", activity.getDomain());
                                data.put("enrolledAt", now);

                                db.collection("enrollments").document(enrollmentId).set(data)
                                        .addOnSuccessListener(unused -> {
                                            db.collection("activities").document(activityId)
                                                    .set(Map.of("registeredCount", activity.getRegisteredCount() + 1), SetOptions.merge())
                                                    .addOnCompleteListener(listener);
                                        })
                                        .addOnFailureListener(e -> listener.onComplete(TaskUtils.failure("Failed to enroll")));
                            })
                            .addOnFailureListener(e -> listener.onComplete(TaskUtils.failure("Failed to fetch activity")));
                })
                .addOnFailureListener(e -> listener.onComplete(TaskUtils.failure("Error checking previous enrollment")));
    }

    public static void unenrollUser(String participantId, String activityId, OnCompleteListener<Void> listener) {
        long now = System.currentTimeMillis();
        if (now < startTimeMillis || now > endTimeMillis) {
            listener.onComplete(TaskUtils.failure("Unenrollment period is closed"));
            return;
        }

        db.collection("enrollments")
                .whereEqualTo("participantId", participantId)
                .whereEqualTo("activityId", activityId)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        listener.onComplete(TaskUtils.failure("Not enrolled in this activity"));
                        return;
                    }

                    DocumentSnapshot doc = query.getDocuments().get(0);
                    db.collection("enrollments").document(doc.getId()).delete()
                            .addOnSuccessListener(unused -> {
                                db.collection("activities").document(activityId).get()
                                        .addOnSuccessListener(snapshot -> {
                                            ActivityModel activity = snapshot.toObject(ActivityModel.class);
                                            int currentCount = activity != null ? activity.getRegisteredCount() : 1;
                                            db.collection("activities").document(activityId)
                                                    .set(Map.of("registeredCount", Math.max(0, currentCount - 1)), SetOptions.merge())
                                                    .addOnCompleteListener(listener);
                                        });
                            })
                            .addOnFailureListener(e -> listener.onComplete(TaskUtils.failure("Failed to unenroll")));
                })
                .addOnFailureListener(e -> listener.onComplete(TaskUtils.failure("Unenroll failed")));
    }


    public static void getUserEnrollments(String participantId, OnCompleteListener<java.util.List<EnrollmentModel>> listener) {
        db.collection("enrollments")
                .whereEqualTo("participantId", participantId)
                .get()
                .addOnSuccessListener(query -> {
                    java.util.List<EnrollmentModel> enrollments = new java.util.ArrayList<>();

                    if (query.isEmpty()) {
                        listener.onComplete(TaskUtils.success(enrollments));
                        return;
                    }

                    final int total = query.size();
                    final int[] loaded = {0};

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        EnrollmentModel model = doc.toObject(EnrollmentModel.class);
                        if (model == null) {
                            loaded[0]++;
                            if (loaded[0] == total) listener.onComplete(TaskUtils.success(enrollments));
                            continue;
                        }

                        db.collection("activities")
                                .document(model.getActivityId())
                                .get()
                                .addOnSuccessListener(activityDoc -> {
                                    if (activityDoc.exists()) {
                                        String name = activityDoc.getString("name");
                                        String domain = activityDoc.getString("domain");
                                        String desc = activityDoc.getString("description");

                                        model.setName(name);
                                        model.setDomain(domain);
                                        model.setDescription(desc);
                                    }
                                    enrollments.add(model);
                                    loaded[0]++;
                                    if (loaded[0] == total) listener.onComplete(TaskUtils.success(enrollments));
                                })
                                .addOnFailureListener(e -> {
                                    loaded[0]++;
                                    if (loaded[0] == total) listener.onComplete(TaskUtils.success(enrollments));
                                });
                    }
                })
                .addOnFailureListener(e -> listener.onComplete(TaskUtils.failure("Failed to load enrollments")));
    }

}

