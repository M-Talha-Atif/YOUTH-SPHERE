package com.event.youthSphere.firebase;


import com.event.youthSphere.models.participant.FeedbackModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.function.BiConsumer;

public class FirebaseFeedbackHelper {

    private static final String COLLECTION_NAME = "feedback";

    public static void addFeedback(FeedbackModel feedback, OnFeedbackAddedListener listener) {
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_NAME)
                .whereEqualTo("activityId", feedback.getActivityId())
                .whereEqualTo("participantId", feedback.getParticipantId())
                .whereEqualTo("instructorId", feedback.getInstructorId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Feedback already exists
                        listener.onFailure(new Exception("Feedback already submitted by this instructor."));
                        return;
                    }

                    // Safe to add new feedback
                    FirebaseFirestore.getInstance()
                            .collection(COLLECTION_NAME)
                            .add(feedback)
                            .addOnSuccessListener(docRef -> listener.onSuccess())
                            .addOnFailureListener(listener::onFailure);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public static void checkInstructorFeedbackExists(String activityId, String participantId, BiConsumer<Boolean, Exception> callback) {
        FirebaseFirestore.getInstance()
                .collection("feedback")
                .whereEqualTo("activityId", activityId)
                .whereEqualTo("participantId", participantId)
                .whereNotEqualTo("instructorId", null)
                .get()
                .addOnSuccessListener(snapshot -> callback.accept(!snapshot.isEmpty(), null))
                .addOnFailureListener(e -> callback.accept(false, e));
    }


    public interface OnFeedbackAddedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}

