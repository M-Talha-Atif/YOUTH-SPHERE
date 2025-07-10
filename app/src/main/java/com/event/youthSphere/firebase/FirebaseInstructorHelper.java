package com.event.youthSphere.firebase;

import com.event.youthSphere.models.users.InstructorUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.*;


public class FirebaseInstructorHelper {

    private static final CollectionReference userRef =
            FirebaseFirestore.getInstance().collection("users");

    public static void getAllInstructors(EventListener<QuerySnapshot> listener) {
        userRef.whereEqualTo("role", "INSTRUCTOR").addSnapshotListener(listener);
    }

    public static void updateInstructor(InstructorUser instructor, OnCompleteListener<Void> listener) {
        if (instructor.getUid() == null) return;
        userRef.document(instructor.getUid())
                .set(instructor.toMap(), SetOptions.merge())
                .addOnCompleteListener(listener);
    }

    public static void deleteInstructor(String uid, OnCompleteListener<Void> listener) {
        userRef.document(uid).delete().addOnCompleteListener(listener);
    }
}
