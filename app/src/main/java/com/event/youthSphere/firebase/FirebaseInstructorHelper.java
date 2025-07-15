package com.event.youthSphere.firebase;

import com.event.youthSphere.models.users.ParticipantUser;
import com.event.youthSphere.models.users.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;
import java.util.function.BiConsumer;
import com.event.youthSphere.models.activity.ActivityImageModel;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FirebaseInstructorHelper {

    public static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnParticipantsLoaded {
        void onLoaded(List<User> participants, Exception error);
    }

    public static void getInstructorActivities(String instructorId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("activities")
                .whereEqualTo("instructorId", instructorId)
                .get()
                .addOnCompleteListener(listener);
    }

    public  static String getCurrentUserID(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();

        }
        return  null;
    }

    public static void uploadActivityImage(String activityId, String base64Image, String domain, String name, OnCompleteListener<DocumentReference> listener) {
        String uploadDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("imageUrl", base64Image);
        imageData.put("uploadDate", uploadDate);
        imageData.put("domain", domain);
        imageData.put("name", name);

        FirebaseFirestore.getInstance()
                .collection("activities")
                .document(activityId)
                .collection("ActivityImages")
                .add(imageData)
                .addOnCompleteListener(listener);
    }

    public static void getActivityImages(String activityId, BiConsumer<List<ActivityImageModel>, Exception> callback) {
        db.collection("activities")
                .document(activityId)
                .collection("ActivityImages")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<ActivityImageModel> images = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        ActivityImageModel image = doc.toObject(ActivityImageModel.class);
                        images.add(image);
                    }
                    callback.accept(images, null);
                })
                .addOnFailureListener(e -> callback.accept(null, e));
    }

    public static void getParticipantsForActivity(String activityId, OnParticipantsLoaded callback) {
        db.collection("enrollments")
                .whereEqualTo("activityId", activityId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> participants = new ArrayList<>();
                    if (querySnapshot.isEmpty()) {
                        callback.onLoaded(participants, null);
                        return;
                    }

                    final int total = querySnapshot.size();
                    final int[] loaded = {0};

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String participantId = doc.getString("participantId");
                        if (participantId == null) {
                            loaded[0]++;
                            if (loaded[0] == total) callback.onLoaded(participants, null);
                            continue;
                        }

                        db.collection("users")
                                .document(participantId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        ParticipantUser p = userDoc.toObject(ParticipantUser.class);
                                        if (p != null) {
                                            participants.add(p);
                                        }
                                    }
                                    loaded[0]++;
                                    if (loaded[0] == total) callback.onLoaded(participants, null);
                                })
                                .addOnFailureListener(e -> {
                                    loaded[0]++;
                                    if (loaded[0] == total) callback.onLoaded(participants, null);
                                });
                    }
                })
                .addOnFailureListener(e -> callback.onLoaded(null, e));
    }

}
