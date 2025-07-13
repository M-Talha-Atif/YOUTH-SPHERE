package com.event.youthSphere.firebase;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Locale;
import java.util.function.BiConsumer;
import com.event.youthSphere.models.activity.ActivityImageModel;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FirebaseInstructorHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

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

}
