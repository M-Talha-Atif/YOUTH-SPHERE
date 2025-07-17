package com.event.youthSphere.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.event.youthSphere.activities.DashboardActivity;
import com.event.youthSphere.R;
import com.event.youthSphere.firebase.FirebaseAuthHelper;
import com.event.youthSphere.utils.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Fragment for handling user login functionality.
 * Manages login form validation, authentication execution, and
 * post-authentication navigation to dashboard based on user role.
 */
public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    private final FirebaseAuthHelper firebaseAuthHelper = new FirebaseAuthHelper();


    /**
     * Called to have the fragment instantiate its user interface view. This is optional,
     * and non-graphical fragments can return null. This will be called between
     * {@link #onCreate(Bundle)} and {@link #onViewCreated(View, Bundle)}.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *                  The fragment should not add the view itself, but this can be used to generate
     *                  the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     *                           saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     *
     * @implNote This implementation:
     * 1. Inflates the layout defined in R.layout.fragment_login
     * 2. Initializes view references for:
     *    - Email input field (emailEditText)
     *    - Password input field (passwordEditText)
     *    - Login button (loginButton)
     *    - Loading overlay (loadingOverlay)
     *    - Loading progress bar (loadingProgressBar)
     * 3. Sets a click listener on the login button that triggers the attemptLogin() method
     */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = rootView.findViewById(R.id.emailEditText);
        passwordEditText = rootView.findViewById(R.id.passwordEditText);
        loginButton = rootView.findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> attemptLogin());

        return rootView;
    }


    /**
     * Attempts user login with email and password.
     * Validates input, shows loading state, and authenticates via Firebase.
     * On success, fetches user role; on failure, shows error message.
     */
    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuthHelper.login(email, password)
                .addOnSuccessListener(authResult -> {
                    firebaseAuthHelper.getUserRole()
                            .addOnSuccessListener(this::handleRoleSuccess)
                            .addOnFailureListener(e -> showError("Role fetch failed: " + e.getMessage()));
                })
                .addOnFailureListener(e -> showError("Login failed: " + e.getMessage()));
    }

    private void handleRoleSuccess(DocumentSnapshot document) {

        if (document.exists() && document.contains("role")) {
            String role = document.getString("role");
            SessionManager sessionManager = new SessionManager(requireContext());
            sessionManager.setLogin(true);
            navigateToDashboard(role);
        } else {
            showError("Role not found in user document");
        }
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToDashboard(String role) {
        Intent intent = new Intent(getContext(), DashboardActivity.class);
        intent.putExtra("user_role", role);
        startActivity(intent);
        requireActivity().finish();
    }
}
