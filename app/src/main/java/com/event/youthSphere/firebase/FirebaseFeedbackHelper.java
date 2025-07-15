package com.event.youthSphere.firebase;


import com.event.youthSphere.models.participant.FeedbackModel;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public interface OnFeedbackAddedListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}

