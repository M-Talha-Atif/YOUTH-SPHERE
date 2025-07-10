package com.event.youthSphere.firebase;


import com.event.youthSphere.models.activity.ActivityModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.EventListener;

import java.util.*;

public class FirebaseActivityHelper {
    private static final CollectionReference activityRef =
            FirebaseFirestore.getInstance().collection("activities");
    private static final CollectionReference userRef =
            FirebaseFirestore.getInstance().collection("users");


    public static void createActivity(ActivityModel activity, OnCompleteListener<Void> listener) {
        String id = activityRef.document().getId();
        activity.setId(id);
        activityRef.document(id).set(activity).addOnCompleteListener(listener);
    }

    public static void updateActivity(ActivityModel activity, OnCompleteListener<Void> listener) {
        if (activity.getId() == null) return;
        activityRef.document(activity.getId()).set(activity).addOnCompleteListener(listener);
    }

    public static void deleteActivity(String id, OnCompleteListener<Void> listener) {
        activityRef.document(id).delete().addOnCompleteListener(listener);
    }

    public static void getAllActivities(EventListener<QuerySnapshot> listener) {
        activityRef.orderBy("registeredCount", Query.Direction.DESCENDING).addSnapshotListener(listener);
    }

    public static void searchByDomain(String domain, EventListener<QuerySnapshot> listener) {
        activityRef.whereEqualTo("domain", domain).addSnapshotListener(listener);
    }

    public static void searchByMonth(String month, EventListener<QuerySnapshot> listener) {
        activityRef.whereEqualTo("month", month).addSnapshotListener(listener);
    }

    // Assign instructor to activity
    public static void assignActivityToInstructor(String activityId, String instructorId,
                                                  String specialization, String employmentDate,
                                                  OnCompleteListener<Void> listener) {

        // Step 1: Update the activity with instructorId
        activityRef.document(activityId).update("instructorId", instructorId)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        listener.onComplete(task);
                        return;
                    }

                    // Step 2: Prepare instructor updates
                    Map<String, Object> instructorUpdates = new HashMap<>();
                    instructorUpdates.put("activityIds", FieldValue.arrayUnion(activityId));
                    instructorUpdates.put("fieldOfSpecialization", specialization);
                    instructorUpdates.put("employmentStartDate", employmentDate);

                    // Step 3: Apply updates to instructor
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(instructorId)
                            .update(instructorUpdates)
                            .addOnCompleteListener(listener);
                });
    }

    // Fetch users with role INSTRUCTOR
    public static void fetchAllInstructors(EventListener<QuerySnapshot> listener) {
        userRef.whereEqualTo("role", "INSTRUCTOR").addSnapshotListener(listener);
    }

    public static void deleteActivityAndUpdateInstructor(String activityId, String instructorId, OnCompleteListener<Void> listener) {
        WriteBatch batch = FirebaseFirestore.getInstance().batch();
        batch.delete(activityRef.document(activityId));

        if (instructorId != null) {
            DocumentReference instructorRef = userRef.document(instructorId);
            batch.update(instructorRef, "activityIds", FieldValue.arrayRemove(activityId));
        }

        batch.commit().addOnCompleteListener(listener);
    }

}

