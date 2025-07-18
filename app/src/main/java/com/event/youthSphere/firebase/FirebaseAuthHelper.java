package com.event.youthSphere.firebase;

import android.content.Context;
import com.event.youthSphere.localdb.UserDBHelper;
import com.event.youthSphere.models.UserRole;
import com.event.youthSphere.models.users.ParticipantUser;
import com.event.youthSphere.models.users.RoleUser;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.event.youthSphere.models.users.*;
import java.util.List;
import java.util.Objects;

/**
 * Auth helper for Firebase and local SQLite fallback.
 */
public class FirebaseAuthHelper {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Context context;

    /**
     * Pass context for SQLite fallback.
     */
    public FirebaseAuthHelper(Context context) {
        this.context = context;
    }

    /**
     * Firebase login. On failure, try local DB login.
     */
    public Task<AuthResult> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
        // For fallback, handle exception in UI and call localLogin (see below)
    }

    /**
     * Local SQLite login fallback for offline usage.
     * Returns role string (or null if not found).
     */
    public String localLogin(String email, String password) {
        UserDBHelper dbHelper = new UserDBHelper(context);
        return dbHelper.login(email, password);
    }

    /**
     * Firebase signup.
     */
    public Task<Void> signup(String email, String password, String fullName, UserRole role) {
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser == null) {
                        throw new Exception("User registration failed");
                    }

                    String uid = firebaseUser.getUid();
                    RoleUser userModel;

                    switch (role) {
                        case PARTICIPANT:
                            userModel = new ParticipantUser(uid, email, fullName, List.of());
                            break;
                        case PARENT:
                            userModel = new ParentUser(uid, email, fullName, List.of());
                            break;
                        case INSTRUCTOR:
                            userModel = new InstructorUser(uid, email, fullName, List.of(), "", "");
                            break;
                        case COORDINATOR:
                            userModel = new CoordinatorUser(uid, email, fullName);
                            break;
                        case SUPER_ADMIN:
                            userModel = new SuperAdminUser(uid, email, fullName);
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported user role");
                    }

                    return db.collection("users").document(uid).set(userModel.toMap());
                });
    }

    /**
     * Local SQLite signup fallback for offline usage.
     * Returns row id (or -1 if failed).
     */
    public long localSignup(String uid, String email, String fullName, String password, UserRole role,
                            String childIds, String activityIds, String fieldOfSpecialization, String employmentStartDate) {
        UserDBHelper dbHelper = new UserDBHelper(context);
        return dbHelper.addUser(uid, email, fullName, password, role.name(),
                childIds, activityIds, fieldOfSpecialization, employmentStartDate);
    }

    /**
     * Helper to get the current Firebase UID after signup.
     */
    public String getCurrentFirebaseUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : "";
    }


    /**
     * Firebase get user role by current user. On failure, use local DB (see below).
     */
    public Task<com.google.firebase.firestore.DocumentSnapshot> getUserRole() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("No user logged in"));
        }
        return db.collection("users").document(currentUser.getUid()).get();
    }



}