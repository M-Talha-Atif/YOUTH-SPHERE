package com.event.youthSphere.firebase;

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

public class FirebaseAuthHelper {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<AuthResult> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

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

    public Task<com.google.firebase.firestore.DocumentSnapshot> getUserRole() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("No user logged in"));
        }
        return db.collection("users").document(currentUser.getUid()).get();
    }
}
