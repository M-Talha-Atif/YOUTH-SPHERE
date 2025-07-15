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

    public static void addChildToParent(String childId, BiConsumer<Boolean, Exception> callback) {
        String parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        isChildAlreadyAdded(childId, (alreadyAdded, error) -> {
            if (error != null || alreadyAdded) {
                callback.accept(false, error);
                return;
            }

            db.collection("users").document(parentId).get()
                    .addOnSuccessListener(doc -> {
                        ParentUser parent = doc.toObject(ParentUser.class);
                        if (parent == null) {
                            callback.accept(false, null);
                            return;
                        }

                        List<String> childIds = parent.getChildIds() != null ? parent.getChildIds() : new ArrayList<>();
                        if (!childIds.contains(childId)) {
                            childIds.add(childId);
                            db.collection("users").document(parentId)
                                    .update("childIds", childIds)
                                    .addOnSuccessListener(unused -> callback.accept(true, null))
                                    .addOnFailureListener(e -> callback.accept(false, e));
                        } else {
                            callback.accept(true, null); // Already added to this parent
                        }
                    })
                    .addOnFailureListener(e -> callback.accept(false, e));
        });
    }

    public static void getMyChildren(BiConsumer<List<ParticipantUser>, Exception> callback) {
        String parentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(parentId).get()
                .addOnSuccessListener(parentDoc -> {
                    ParentUser parent = parentDoc.toObject(ParentUser.class);
                    if (parent == null || parent.getChildIds() == null || parent.getChildIds().isEmpty()) {
                        callback.accept(new ArrayList<>(), null);
                        return;
                    }

                    db.collection("users")
                            .whereIn("uid", parent.getChildIds())
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                List<ParticipantUser> children = new ArrayList<>();
                                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                    ParticipantUser user = doc.toObject(ParticipantUser.class);
                                    if (user != null) children.add(user);
                                }
                                callback.accept(children, null);
                            })
                            .addOnFailureListener(e -> callback.accept(null, e));
                })
                .addOnFailureListener(e -> callback.accept(null, e));
    }

    public static void getAllAvailableParticipants(BiConsumer<List<ParticipantUser>, Exception> callback) {
        getAllParticipants((allParticipants, error1) -> {
            if (error1 != null) {
                callback.accept(null, error1);
                return;
            }

            db.collection("users")
                    .whereEqualTo("role", "PARENT")
                    .get()
                    .addOnSuccessListener(parentsSnapshot -> {
                        Set<String> usedChildIds = new HashSet<>();
                        for (DocumentSnapshot doc : parentsSnapshot) {
                            ParentUser parent = doc.toObject(ParentUser.class);
                            if (parent != null && parent.getChildIds() != null) {
                                usedChildIds.addAll(parent.getChildIds());
                            }
                        }

                        List<ParticipantUser> available = new ArrayList<>();
                        for (ParticipantUser p : allParticipants) {
                            if (!usedChildIds.contains(p.getUid())) {
                                available.add(p);
                            }
                        }

                        callback.accept(available, null);
                    })
                    .addOnFailureListener(e -> callback.accept(null, e));
        });
    }



}

