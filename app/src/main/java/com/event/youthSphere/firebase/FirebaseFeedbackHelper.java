package com.event.youthSphere.firebase;


import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.participant.FeedbackModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.BiConsumer;

public class FirebaseFeedbackHelper {

    private static final String COLLECTION_NAME = "feedback";

    public static void addFeedback(FeedbackModel feedback, OnFeedbackAddedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("activities").document(feedback.getActivityId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    ActivityModel activity = snapshot.toObject(ActivityModel.class);
                    if (activity == null || activity.getStartDate() == null || activity.getEndDate() == null) {
                        listener.onFailure(new Exception("Activity time not found"));
                        return;
                    }

                    long now = System.currentTimeMillis();
                    long startMillis = activity.getStartDate().getTime();
                    long endMillis = activity.getEndDate().getTime();

                    if (now < startMillis || now > endMillis) {
                        listener.onFailure(new Exception("Feedback allowed only during activity duration"));
                        return;
                    }

                    // Check if feedback already exists
                    db.collection(COLLECTION_NAME)
                            .whereEqualTo("activityId", feedback.getActivityId())
                            .whereEqualTo("participantId", feedback.getParticipantId())
                            .whereEqualTo("instructorId", feedback.getInstructorId())
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    listener.onFailure(new Exception("Feedback already submitted by this instructor."));
                                    return;
                                }

                                db.collection(COLLECTION_NAME)
                                        .add(feedback)
                                        .addOnSuccessListener(docRef -> listener.onSuccess())
                                        .addOnFailureListener(listener::onFailure);
                            })
                            .addOnFailureListener(listener::onFailure);
                })
                .addOnFailureListener(e -> listener.onFailure(new Exception("Activity not found")));
    }




    public interface OnFeedbackAddedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}

