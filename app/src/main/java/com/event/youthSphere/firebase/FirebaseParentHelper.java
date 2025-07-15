package com.event.youthSphere.firebase;


import com.event.youthSphere.models.users.ParentUser;
import com.event.youthSphere.models.users.ParticipantUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;
import java.util.function.BiConsumer;

public class FirebaseParentHelper {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void getAllParticipants(BiConsumer<List<ParticipantUser>, Exception> callback) {
        db.collection("users")
                .whereEqualTo("role", "PARTICIPANT")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<ParticipantUser> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ParticipantUser user = doc.toObject(ParticipantUser.class);
                        if (user != null) list.add(user);
                    }
                    callback.accept(list, null);
                })
                .addOnFailureListener(e -> callback.accept(null, e));
    }

    public static void getCurrentParent(BiConsumer<ParentUser, Exception> callback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    ParentUser parent = snapshot.toObject(ParentUser.class);
                    callback.accept(parent, null);
                })
                .addOnFailureListener(e -> callback.accept(null, e));
    }

    public static void updateChildIds(String parentId, List<String> updatedChildIds, BiConsumer<Boolean, Exception> callback) {
        db.collection("users").document(parentId)
                .update("childIds", updatedChildIds)
                .addOnSuccessListener(unused -> callback.accept(true, null))
                .addOnFailureListener(e -> callback.accept(false, e));
    }

    public static void isChildAlreadyAdded(String childId, BiConsumer<Boolean, Exception> callback) {
        db.collection("users")
                .whereEqualTo("role", "PARENT")
                .whereArrayContains("childIds", childId)
                .get()
                .addOnSuccessListener(snapshot -> callback.accept(!snapshot.isEmpty(), null))
                .addOnFailureListener(e -> callback.accept(false, e));
    }
}

