package com.event.youthSphere.firebase;

import android.content.Context;
import android.util.Log;
import com.event.youthSphere.localdb.UserDBHelper;
import com.event.youthSphere.models.users.InstructorUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.*;

public class FirebaseCoordinateHelper {

    private static final CollectionReference userRef =
            FirebaseFirestore.getInstance().collection("users");

    /**
     * Listen to all instructors from Firebase and sync locally.
     */
    public static void getAllInstructors(Context context, EventListener<QuerySnapshot> listener) {
        userRef.whereEqualTo("role", "INSTRUCTOR")
                .addSnapshotListener((snapshots, error) -> {
                    listener.onEvent(snapshots, error);

                    // Sync with SQLite
                    if (snapshots != null && error == null) {
                        UserDBHelper userDB = new UserDBHelper(context);
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            InstructorUser instructor = doc.toObject(InstructorUser.class);
                            if (instructor != null && instructor.getUid() != null) {
                                userDB.updateInstructor(
                                        instructor.getUid(),
                                        instructor.getFullName(),
                                        instructor.getEmail(),
                                       null,
                                        instructor.getActivityIdsAsCommaString(),
                                        instructor.getFieldOfSpecialization(),
                                        instructor.getEmploymentStartDate()
                                );
                            }
                        }
                    }
                });
    }

    /**
     * Update an instructor in Firebase and local SQLite.
     */
    public static void updateInstructor(Context context, InstructorUser instructor, OnCompleteListener<Void> listener) {
        if (instructor.getUid() == null) return;

        userRef.document(instructor.getUid())
                .set(instructor.toMap(), SetOptions.merge())
                .addOnCompleteListener(task -> {
                    // ✅ Sync to SQLite
                    if (task.isSuccessful()) {
                        UserDBHelper userDB = new UserDBHelper(context);
                        userDB.updateInstructor(
                                instructor.getUid(),
                                instructor.getFullName(),
                                instructor.getEmail(),
                                null,
                                instructor.getActivityIdsAsCommaString(),
                                instructor.getFieldOfSpecialization(),
                                instructor.getEmploymentStartDate()
                        );
                        Log.d("FirebaseCoordinateHelper", "Instructor updated successfully.");
                    }

                    // Forward original task result
                    listener.onComplete(task);
                });
    }

    /**
     * Delete an instructor from Firebase and SQLite.
     */
    public static void deleteInstructor(Context context, String uid, OnCompleteListener<Void> listener) {
        userRef.document(uid).delete().addOnCompleteListener(task -> {
            // ✅ Sync delete locally
            if (task.isSuccessful()) {
                UserDBHelper userDB = new UserDBHelper(context);
                userDB.deleteInstructor(uid);
                Log.d("FirebaseCoordinateHelper", "Instructor deleted successfully.");
            }

            // Forward the original result
            listener.onComplete(task);
        });
    }
}
