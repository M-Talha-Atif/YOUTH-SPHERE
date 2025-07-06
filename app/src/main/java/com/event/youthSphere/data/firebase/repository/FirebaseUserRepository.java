package com.event.youthSphere.data.firebase.repository;

import com.event.youthSphere.domain.model.users.RoleUser;
import com.event.youthSphere.domain.model.UserRole;
import com.event.youthSphere.domain.repository.UserRepository;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Firebase implementation of the UserRepository interface.
 * Handles all user-related operations including registration, authentication,
 * role management, and persistence using Firebase Authentication and Firestore.
 */
public class FirebaseUserRepository implements UserRepository {

    // Firebase Authentication instance
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    // Firestore database instance
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    /**
     * Registers a new user with email and password.
     *
     * @param email The user's email address
     * @param password The user's password
     * @param role The user's assigned role (not currently used in Firebase Auth)
     * @return Task<AuthResult> containing authentication result
     */
    @Override
    public Task<AuthResult> registerUserWithEmail(String email, String password, UserRole role) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param email The user's email address
     * @param password The user's password
     * @return Task<AuthResult> containing authentication result
     */
    @Override
    public Task<AuthResult> loginUserWithEmail(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Saves user data to Firestore database.
     * Stores the complete user profile including role information.
     *
     * @param user The RoleUser object containing user details
     * @return Task<Void> indicating completion status
     */
    @Override
    public Task<Void> saveUser(RoleUser user) {
        return firestore.collection("users")
                .document(user.getUid())
                .set(user.toMap());
    }

    /**
     * Retrieves the current authenticated user's role from Firestore.
     *
     * @return Task<UserRole> containing the user's role
     * @throws Exception if user is not logged in or role data is invalid/missing
     */
    @Override
    public Task<UserRole> getCurrentUserRole() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Return failed task if no user is logged in
        if (currentUser == null) {
            return Tasks.forException(new Exception("User not logged in"));
        }

        TaskCompletionSource<UserRole> taskSource = new TaskCompletionSource<>();

        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists() && snapshot.contains("role")) {
                        try {
                            String role = snapshot.getString("role");
                            UserRole userRole = UserRole.valueOf(role.toUpperCase());
                            taskSource.setResult(userRole);
                        } catch (IllegalArgumentException e) {
                            taskSource.setException(new Exception("Invalid role format"));
                        }
                    } else {
                        taskSource.setException(new Exception("Role not found in user document"));
                    }
                })
                .addOnFailureListener(taskSource::setException);

        return taskSource.getTask();
    }

    /**
     * Signs out the currently authenticated user.
     * Clears the Firebase Auth session but does not affect Firestore data.
     */
    @Override
    public void signOut() {
        firebaseAuth.signOut();
    }
}