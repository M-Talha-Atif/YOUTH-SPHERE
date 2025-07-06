package com.event.youthSphere.presentation.signup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.event.youthSphere.data.firebase.repository.FirebaseUserRepository;
import com.event.youthSphere.databinding.FragmentSignupBinding;
import com.event.youthSphere.domain.model.UserRole;
import com.event.youthSphere.domain.usecase.auth.RegisterUserUseCase;
import com.event.youthSphere.domain.usecase.auth.SaveUserUseCase;
import com.event.youthSphere.utils.LottieHelper;

import java.util.Objects;

/**
 * Fragment handling user registration functionality.
 * Manages the signup UI flow including form validation, role selection,
 * and communication with the SignupViewModel.
 */
public class SignupFragment extends Fragment {

    private FragmentSignupBinding binding;
    private SignupViewModel signupViewModel;
    private static final String[] ROLES = {"Participant", "Parent", "Instructor", "Coordinator", "Super Admin"};

    /**
     * Inflates the fragment view using data binding.
     * @param inflater The LayoutInflater object
     * @param container The parent view group
     * @param savedInstanceState Saved instance state
     * @return The inflated view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called immediately after onCreateView().
     * Initializes ViewModel and sets up UI components.
     * @param view The created view
     * @param savedInstanceState Saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel first to start any async operations early
        initViewModel();

        // Setup UI components
        setupUI();
    }

    /**
     * Initializes the SignupViewModel with required use cases.
     */
    private void initViewModel() {
        signupViewModel = new ViewModelProvider(
                this,
                new SignupViewModelFactory(
                        new RegisterUserUseCase(new FirebaseUserRepository()),
                        new SaveUserUseCase(new FirebaseUserRepository())
                )
        ).get(SignupViewModel.class);
    }

    /**
     * Sets up all UI components including:
     * - Loading animation
     * - Role spinner
     * - Signup button
     * - LiveData observers
     */
    private void setupUI() {
        // Initialize Lottie animation
        LottieHelper.setupLoadingAnimation(binding.loadingAnimation);

        // Setup role spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                ROLES
        );
        binding.roleSpinner.setAdapter(adapter);

        // Setup signup button
        binding.signupButton.setOnClickListener(v -> validateAndSignup());

        // Setup observers
        setupObservers();
    }

    /**
     * Validates user input and initiates signup process.
     * Shows loading animation during registration.
     */
    private void validateAndSignup() {
        String email = Objects.requireNonNull(binding.signupEmailEditText.getText()).toString().trim();
        String fullName = Objects.requireNonNull(binding.signupFullNameEditText.getText()).toString().trim();
        String pass = Objects.requireNonNull(binding.signupPasswordEditText.getText()).toString().trim();
        String confirm = Objects.requireNonNull(binding.confirmPasswordEditText.getText()).toString().trim();
        String selectedRole = binding.roleSpinner.getSelectedItem().toString();

        if (email.isEmpty() || fullName.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        LottieHelper.showLoading(binding.loadingAnimation, binding.loadingOverlay);
        UserRole role = UserRole.valueOf(selectedRole.toUpperCase().replace(" ", "_"));
        signupViewModel.signup(email, pass, fullName, role);
    }

    /**
     * Sets up observers for ViewModel LiveData:
     * - Signup success state
     * - Error messages
     */
    private void setupObservers() {
        signupViewModel.getSignupSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                LottieHelper.hideLoading(binding.loadingAnimation, binding.loadingOverlay);
                Toast.makeText(requireContext(), "Signup successful", Toast.LENGTH_SHORT).show();
            }
        });

        signupViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            LottieHelper.hideLoading(binding.loadingAnimation, binding.loadingOverlay);
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Cleans up resources when view is destroyed.
     * Pauses animation and clears binding reference.
     */
    @Override
    public void onDestroyView() {
        // Clean up references to prevent memory leaks
        if (binding != null) {
            binding.loadingAnimation.pauseAnimation();
            binding = null;
        }
        super.onDestroyView();
    }
}