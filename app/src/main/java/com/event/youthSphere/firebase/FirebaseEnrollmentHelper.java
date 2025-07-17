package com.event.youthSphere.firebase;

import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.participant.EnrollmentModel;
import com.event.youthSphere.models.users.ParticipantUser;
import com.event.youthSphere.utils.TaskUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public class FirebaseEnrollmentHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void enrollUser(String activityId, ParticipantUser user, BiConsumer<Boolean, Exception> callback) {
        DocumentReference activityRef = db.collection("activities").document(activityId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    DocumentSnapshot activitySnapshot = transaction.get(activityRef);
                    ActivityModel activity = activitySnapshot.toObject(ActivityModel.class);

                    if (activity == null)
                        throw new FirebaseFirestoreException("Activity not found", FirebaseFirestoreException.Code.NOT_FOUND);

                    Timestamp now = Timestamp.now();

                    if (activity.getStartDate() != null && now.toDate().after(activity.getStartDate()))
                        throw new FirebaseFirestoreException("Cannot enroll after start date", FirebaseFirestoreException.Code.ABORTED);

                    List<String> enrolledUserIds = activitySnapshot.contains("enrolledUserIds")
                            ? (List<String>) activitySnapshot.get("enrolledUserIds") : new ArrayList<>();

                    if (enrolledUserIds.contains(user.getUid()))
                        throw new FirebaseFirestoreException("User already enrolled", FirebaseFirestoreException.Code.ALREADY_EXISTS);

                    int currentCount = activity.getRegisteredCount();
                    int maxAllowed = activity.getMaxParticipants();

                    if (currentCount >= maxAllowed)
                        throw new FirebaseFirestoreException("Activity is full", FirebaseFirestoreException.Code.ABORTED);

                    enrolledUserIds.add(user.getUid());
                    transaction.update(activityRef, "registeredCount", currentCount + 1);

                    // Save in top-level enrollments collection
                    DocumentReference enrollmentRef = db.collection("enrollments").document();
                    EnrollmentModel model = new EnrollmentModel(
                            enrollmentRef.getId(),
                            user.getUid(),
                            activityId,
                            now.toDate().getTime(),
                            activity.getName(),
                            activity.getDomain(),
                            activity.getDescription()
                    );
                    transaction.set(enrollmentRef, model);

                    return null;
                }).addOnSuccessListener(unused -> callback.accept(true, null))
                .addOnFailureListener(e -> callback.accept(false, e));
    }



    public static void unenrollUser(String participantId, String activityId, OnCompleteListener<Void> listener) {
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
                    Long enrolledAtMillis = doc.getLong("enrolledAt");

                    if (enrolledAtMillis == null) {
                        listener.onComplete(TaskUtils.failure("Enrollment time missing"));
                        return;
                    }

                    long now = System.currentTimeMillis();
                    long sevenDaysMillis = 7L * 24 * 60 * 60 * 1000;

                    if (now - enrolledAtMillis > sevenDaysMillis) {
                        listener.onComplete(TaskUtils.failure("You can only unenroll within 7 days of enrollment"));
                        return;
                    }

                    // proceed with deletion
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

