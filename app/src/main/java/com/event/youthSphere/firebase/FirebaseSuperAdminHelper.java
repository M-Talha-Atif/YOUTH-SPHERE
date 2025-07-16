package com.event.youthSphere.firebase;

import com.event.youthSphere.models.participant.FeedbackModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class FirebaseSuperAdminHelper {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /** Get all feedback for an activity */
    public static void getActivityFeedback(String activityId, BiConsumer<List<FeedbackModel>, Exception> cb) {
        db.collection("feedback")
                .whereEqualTo("activityId", activityId)
                .get()
                .addOnSuccessListener(qs -> {
                    List<FeedbackModel> allFb = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        FeedbackModel f = doc.toObject(FeedbackModel.class);
                        if (f != null)
                            allFb.add(f);
                    }
                    cb.accept(allFb, null);
                })
                .addOnFailureListener(e -> cb.accept(null, e));
    }

    /** Update the activity’s status */
    public static void updateActivityStatus(String activityId, String status,
                                            BiConsumer<Boolean, Exception> cb) {
        db.collection("activities").document(activityId)
                .update("status", status)
                .addOnSuccessListener(u -> cb.accept(true, null))
                .addOnFailureListener(e -> cb.accept(false, e));
    }
}

