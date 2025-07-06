package com.event.youthSphere.presentation.signup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.event.youthSphere.domain.usecase.auth.RegisterUserUseCase;
import com.event.youthSphere.domain.usecase.auth.SaveUserUseCase;

/**
 * Factory class for creating {@link SignupViewModel} instances with dependencies.
 * Implements {@link ViewModelProvider.Factory} to enable proper dependency injection
 * while maintaining ViewModel lifecycle awareness.
 */
public class SignupViewModelFactory implements ViewModelProvider.Factory {

    private final RegisterUserUseCase registerUserUseCase;
    private final SaveUserUseCase saveUserUseCase;

    /**
     * Constructs a SignupViewModelFactory with required dependencies.
     *
     * @param registerUserUseCase The use case for user registration operations
     * @param saveUserUseCase The use case for persisting user data
     */
    public SignupViewModelFactory(RegisterUserUseCase registerUserUseCase,
                                  SaveUserUseCase saveUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.saveUserUseCase = saveUserUseCase;
    }

    /**
     * Creates a new instance of the specified ViewModel class.
     *
     * @param modelClass The class of ViewModel to create
     * @param <T> The type parameter extending ViewModel
     * @return A newly created SignupViewModel instance
     * @throws IllegalArgumentException if the ViewModel class is not SignupViewModel
     */
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SignupViewModel.class)) {
            return (T) new SignupViewModel(registerUserUseCase, saveUserUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}