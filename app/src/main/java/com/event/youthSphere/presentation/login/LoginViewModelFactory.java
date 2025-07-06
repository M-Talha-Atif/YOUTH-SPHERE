package com.event.youthSphere.presentation.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.event.youthSphere.domain.usecase.auth.GetCurrentUserRoleUseCase;
import com.event.youthSphere.domain.usecase.auth.LoginUserUseCase;

/**
 * Factory class for creating {@link LoginViewModel} instances.
 * Implements the {@link ViewModelProvider.Factory} interface to provide
 * dependency injection capabilities for the LoginViewModel.
 *
 * <p>This factory enables proper ViewModel creation with required use cases
 * while maintaining lifecycle awareness.</p>
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    private final LoginUserUseCase loginUserUseCase;
    private final GetCurrentUserRoleUseCase getUserRoleUseCase;

    /**
     * Constructs a LoginViewModelFactory with required dependencies.
     *
     * @param loginUserUseCase The use case for user authentication operations
     * @param getUserRoleUseCase The use case for retrieving user role information
     */
    public LoginViewModelFactory(LoginUserUseCase loginUserUseCase,
                                 GetCurrentUserRoleUseCase getUserRoleUseCase) {
        this.loginUserUseCase = loginUserUseCase;
        this.getUserRoleUseCase = getUserRoleUseCase;
    }

    /**
     * Creates a new instance of the specified ViewModel class.
     *
     * @param modelClass The class of ViewModel to create
     * @param <T> The type parameter extending ViewModel
     * @return A newly created ViewModel instance
     * @throws IllegalArgumentException if the ViewModel class is unknown
     * @throws ClassCastException if the created ViewModel cannot be cast to the requested type
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(loginUserUseCase, getUserRoleUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}