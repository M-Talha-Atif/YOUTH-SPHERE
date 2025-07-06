package com.event.youthSphere.presentation.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.event.youthSphere.data.firebase.repository.FirebaseUserRepository;
import com.event.youthSphere.databinding.FragmentLoginBinding;
import com.event.youthSphere.domain.model.UserRole;
import com.event.youthSphere.domain.usecase.auth.GetCurrentUserRoleUseCase;
import com.event.youthSphere.domain.usecase.auth.LoginUserUseCase;
import com.event.youthSphere.presentation.DashboardActivity;
import com.event.youthSphere.utils.LottieHelper;
import com.event.youthSphere.utils.SessionManager;

/**
 * Fragment handling user authentication and login functionality.
 * Manages UI interactions and observes ViewModel state changes.
 */
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);

        // Initialize Lottie animation
        LottieHelper.setupLoadingAnimation(binding.loadingAnimation);

        setupLoginFunctionality();
        return binding.getRoot();
    }

    /**
     * Initializes all login-related functionality including:
     * - ViewModel setup
     * - Button click listener
     * - LiveData observers
     */
    private void setupLoginFunctionality() {
        // Initialize ViewModel with required dependencies
        viewModel = initializeViewModel();

        // Set up login button click handler
        setupLoginButton();

        // Observe login success/failure states
        observeLoginResults();
    }

    /**
     * Creates and returns LoginViewModel instance with required dependencies.
     * @return Configured LoginViewModel instance
     */
    private LoginViewModel initializeViewModel() {
        FirebaseUserRepository userRepository = new FirebaseUserRepository();
        LoginUserUseCase loginUserUseCase = new LoginUserUseCase(userRepository);
        GetCurrentUserRoleUseCase roleUseCase = new GetCurrentUserRoleUseCase(userRepository);

        LoginViewModelFactory factory = new LoginViewModelFactory(loginUserUseCase, roleUseCase);
        return new ViewModelProvider(this, factory).get(LoginViewModel.class);
    }

    /**
     * Sets up login button click listener to trigger authentication.
     */
    private void setupLoginButton() {
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString();
            String password = binding.passwordEditText.getText().toString();

            if (validateInput(email, password)) {
                LottieHelper.showLoading(binding.loadingAnimation, binding.loadingOverlay);
                viewModel.login(email, password);
            }
        });
    }

    /**
     * Validates user input before login attempt.
     */
    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Observes ViewModel LiveData for login state changes.
     */
    private void observeLoginResults() {
        observeLoginSuccess();
        observeLoginErrors();
    }

    /**
     * Handles successful login by saving session and navigating to Dashboard.
     */
    private void observeLoginSuccess() {
        viewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                viewModel.getUserRole().observe(getViewLifecycleOwner(), role -> {
                    LottieHelper.hideLoading(binding.loadingAnimation, binding.loadingOverlay);
                    if (role != null) {
                        SessionManager sessionManager = new SessionManager(requireContext());
                        sessionManager.setLogin(true);
                        navigateToDashboard(role);
                    }
                });
            }
        });
    }

    /**
     * Shows error messages when login fails.
     */
    private void observeLoginErrors() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            LottieHelper.hideLoading(binding.loadingAnimation, binding.loadingOverlay);
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Starts DashboardActivity and finishes current activity.
     * @param role UserRole to pass to dashboard
     */
    private void navigateToDashboard(UserRole role) {
        Intent intent = new Intent(requireContext(), DashboardActivity.class);
        intent.putExtra("user_role", role.name());
        startActivity(intent);
        requireActivity().finish();
    }

    /** Clear the animations and binding references*/

    @Override
    public void onDestroyView() {
        // Clean up animation and binding references
        if (binding != null) {
            binding.loadingAnimation.pauseAnimation();
            binding = null;
        }
        super.onDestroyView();
    }
}