package com.event.youthSphere.firebase;


import android.content.Context;
import android.util.Log;

import com.event.youthSphere.local.ActivityDBHelper;
import com.event.youthSphere.local.InstructorDBHelper;
import com.event.youthSphere.models.activity.ActivityModel;
import com.event.youthSphere.models.users.InstructorUser;
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

    // Update activity
    public static void updateActivity(ActivityModel activity, OnCompleteListener<Void> listener) {
        if (activity.getId() == null) return;
        activityRef.document(activity.getId()).set(activity).addOnCompleteListener(listener);
    }

    public static void deleteActivity(String id, OnCompleteListener<Void> listener) {
        activityRef.document(id).delete().addOnCompleteListener(listener);
    }

    // Get all activities
    public static void getAllActivities(Context context, OnActivitiesResult callback) {
        FirebaseFirestore.getInstance().collection("activities")
                .orderBy("registeredCount", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<ActivityModel> activityList = new ArrayList<>();
                    ActivityDBHelper dbHelper = new ActivityDBHelper(context);

                    for (QueryDocumentSnapshot doc : snapshot) {
                        ActivityModel model = doc.toObject(ActivityModel.class);
                        dbHelper.insertOrUpdate(model); // Sync to SQLite
                        activityList.add(model);
                    }

                    callback.onSuccess(activityList);
                })
                .addOnFailureListener(e -> {
                    // 🔌 Offline fallback
                    Log.d("ActivityHelper", "Offline mode");
                    ActivityDBHelper dbHelper = new ActivityDBHelper(context);
                    List<ActivityModel> offlineList = dbHelper.getAllActivities();
                    callback.onSuccess(offlineList);
                });
    }

    // Callback interface
    public interface OnActivitiesResult {
        void onSuccess(List<ActivityModel> activities);
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

        // Update the activity with instructorId
        activityRef.document(activityId).update("instructorId", instructorId)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        listener.onComplete(task);
                        return;
                    }

                    // Prepare instructor updates
                    Map<String, Object> instructorUpdates = new HashMap<>();
                    instructorUpdates.put("activityIds", FieldValue.arrayUnion(activityId));
                    instructorUpdates.put("fieldOfSpecialization", specialization);
                    instructorUpdates.put("employmentStartDate", employmentDate);

                    //  Apply updates to instructor
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(instructorId)
                            .update(instructorUpdates)
                            .addOnCompleteListener(listener);
                });
    }

    // Fetch users with role INSTRUCTOR
    public static void fetchAllInstructors(Context context, OnFetchInstructor callback) {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("role", "INSTRUCTOR")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<InstructorUser> instructors = new ArrayList<>();
                    InstructorDBHelper dbHelper = new InstructorDBHelper(context);

                    for (QueryDocumentSnapshot doc : snapshot) {
                        InstructorUser model = doc.toObject(InstructorUser.class);
                        dbHelper.insertOrUpdate(model); // sync to SQLite
                        instructors.add(model);
                    }

                    callback.onSuccess(instructors);
                })
                .addOnFailureListener(e -> {
                    InstructorDBHelper dbHelper = new InstructorDBHelper(context);
                    List<InstructorUser> offlineInstructors = dbHelper.getAllInstructors();
                    callback.onSuccess(offlineInstructors);
                });
    }

    public interface OnFetchInstructor {
        void onSuccess(List<InstructorUser> instructors);
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

    public interface TopActivitiesCallback {
        void onResult(List<ActivityModel> topActivities);
    }

    public static void getTop10Activities(TopActivitiesCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("activities").get().addOnSuccessListener(activitySnapshot -> {
            if (activitySnapshot.isEmpty()) {
                callback.onResult(Collections.emptyList());
                return;
            }

            Map<String, ActivityModel> validActivityMap = new HashMap<>();
            Date now = new Date();

            for (QueryDocumentSnapshot doc : activitySnapshot) {
                ActivityModel model = doc.toObject(ActivityModel.class);
                if (model.getStartDate() != null && model.getStartDate().before(now)) {
                    validActivityMap.put(model.getId(), model);
                }
            }

            db.collection("feedback").get().addOnSuccessListener(feedbackSnapshot -> {
                Map<String, List<Integer>> feedbackScores = new HashMap<>();

                for (QueryDocumentSnapshot doc : feedbackSnapshot) {
                    String activityId = doc.getString("activityId");
                    Long rating = doc.getLong("rating");
                    if (activityId != null && rating != null && validActivityMap.containsKey(activityId)) {
                        feedbackScores.putIfAbsent(activityId, new ArrayList<>());
                        feedbackScores.get(activityId).add(rating.intValue());
                    }
                }

                List<ActivityModel> filteredList = new ArrayList<>();
                for (Map.Entry<String, ActivityModel> entry : validActivityMap.entrySet()) {
                    double avg = computeScore(entry.getValue(), feedbackScores);
                    Log.d("ActivityScore", "Activity: " + entry.getValue().getName() + " Score: " + avg);
                    if (avg > 7) {
                        filteredList.add(entry.getValue());
                    }
                }

                filteredList.sort((a1, a2) -> {
                    double score1 = computeScore(a1, feedbackScores);
                    double score2 = computeScore(a2, feedbackScores);
                    return Double.compare(score2, score1); // descending
                });

                callback.onResult(filteredList.subList(0, Math.min(10, filteredList.size())));
            });
        });
    }

    private static double computeScore(ActivityModel activity, Map<String, List<Integer>> feedbackMap) {
        List<Integer> ratings = feedbackMap.get(activity.getId());
        if (ratings == null || ratings.isEmpty()) return 0;
        return ratings.stream().mapToInt(i -> i).average().orElse(0);
    }



}

