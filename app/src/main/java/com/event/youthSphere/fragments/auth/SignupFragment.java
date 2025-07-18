package com.event.youthSphere.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.event.youthSphere.R;

import java.util.Objects;

import com.event.youthSphere.models.UserRole;
import com.event.youthSphere.firebase.FirebaseAuthHelper;

/**
 * Fragment handling user registration functionality.
 * Manages the signup UI flow including form validation, role selection,
 * and communication with the SignupViewModel.
 */
public class SignupFragment extends Fragment {



    // UI components
    private EditText emailEditText;
    private EditText fullNameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Spinner roleSpinner;
    private Button signupButton;

    private FirebaseAuthHelper firebaseAuthHelper;


    private static final String[] ROLES = {
            "Participant", "Parent", "Instructor", "Coordinator", "Super Admin"
    };

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * @param inflater The LayoutInflater object that can be used to inflate views
     * @param container The parent view that the fragment's UI should be attached to
     * @param savedInstanceState Bundle containing previous state, if any
     * @return The View for the fragment's UI
     */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }



    /**
     * Called immediately after onCreateView() has returned, but before any saved state has been restored.
     * @param view The View returned by onCreateView()
     * @param savedInstanceState Bundle containing previous state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuthHelper = new FirebaseAuthHelper(requireContext());
        initViews(view);
        setupUI();
    }


    /**
     * Initializes all the UI components by finding their views.
     * @param view The parent view containing all the UI elements
     */

    private void initViews(View view) {
        emailEditText = view.findViewById(R.id.signupEmailEditText);
        fullNameEditText = view.findViewById(R.id.signupFullNameEditText);
        passwordEditText = view.findViewById(R.id.signupPasswordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        roleSpinner = view.findViewById(R.id.roleSpinner);
        signupButton = view.findViewById(R.id.signupButton);
    }


    /**
     * Sets up the UI components including spinner adapter and click listeners.
     */
    private void setupUI() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                ROLES
        );
        roleSpinner.setAdapter(adapter);

        signupButton.setOnClickListener(v -> validateAndSignup());

    }


    /**
     * Validates the input fields and initiates the signup process if validation passes.
     */
    private void validateAndSignup() {
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String fullName = Objects.requireNonNull(fullNameEditText.getText()).toString().trim();
        String pass = Objects.requireNonNull(passwordEditText.getText()).toString().trim();
        String confirm = Objects.requireNonNull(confirmPasswordEditText.getText()).toString().trim();
        String selectedRole = roleSpinner.getSelectedItem().toString();

        if (email.isEmpty() || fullName.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }


        UserRole role = UserRole.valueOf(selectedRole.toUpperCase().replace(" ", "_"));

        firebaseAuthHelper.signup(email, pass, fullName, role)
                .addOnSuccessListener(task -> {
                    // After successful signup in Firebase, store in SQLite as backup
                    String uid = firebaseAuthHelper.getCurrentFirebaseUid();
                    firebaseAuthHelper.localSignup(
                            uid,
                            email,
                            fullName,
                            pass,
                            role,
                            "", // childIds
                            "", // activityIds
                            role == UserRole.INSTRUCTOR ? "" : null, // fieldOfSpecialization
                            role == UserRole.INSTRUCTOR ? "" : null  // employmentStartDate
                    );
                    Toast.makeText(requireContext(), "Signup successful", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {


                    Toast.makeText(requireContext(), "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }








    /**
     * Called when the view hierarchy associated with the fragment is being removed.
     */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
