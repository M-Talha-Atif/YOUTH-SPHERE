package com.event.youthSphere.presentation.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.event.youthSphere.domain.model.UserRole;
import com.event.youthSphere.domain.usecase.auth.GetCurrentUserRoleUseCase;
import com.event.youthSphere.domain.usecase.auth.LoginUserUseCase;

/**
 * ViewModel for handling login screen business logic.
 * Manages user authentication and role retrieval, exposing observable state
 * for the login UI components.
 */
public class LoginViewModel extends ViewModel {

    // Use cases for login operations
    private final LoginUserUseCase loginUserUseCase;
    private final GetCurrentUserRoleUseCase getUserRoleUseCase;

    // LiveData for observing login state
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<UserRole> userRoleLiveData = new MutableLiveData<>();

    /**
     * Constructs a LoginViewModel with required use cases.
     *
     * @param loginUserUseCase Use case for user authentication
     * @param getUserRoleUseCase Use case for retrieving user role
     */
    public LoginViewModel(LoginUserUseCase loginUserUseCase,
                          GetCurrentUserRoleUseCase getUserRoleUseCase) {
        this.loginUserUseCase = loginUserUseCase;
        this.getUserRoleUseCase = getUserRoleUseCase;
    }

    /**
     * Provides observable login success state.
     * @return LiveData<Boolean> that emits true on successful login
     */
    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    /**
     * Provides observable error messages.
     * @return LiveData<String> that emits error messages when they occur
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Provides observable user role information.
     * @return LiveData<UserRole> that emits the authenticated user's role
     */
    public LiveData<UserRole> getUserRole() {
        return userRoleLiveData;
    }

    /**
     * Performs login operation with provided credentials.
     * Handles the authentication flow and subsequent role retrieval.
     * Updates the appropriate LiveData objects based on operation outcomes.
     *
     * @param email User's email address
     * @param password User's password
     */
    public void login(String email, String password) {
        loginUserUseCase.execute(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // On successful authentication, fetch user role
                        getUserRoleUseCase.execute()
                                .addOnSuccessListener(role -> {
                                    userRoleLiveData.setValue(role);
                                    loginSuccess.setValue(true);
                                })
                                .addOnFailureListener(e ->
                                        errorMessage.setValue(
                                                "Login succeeded but role fetch failed: " +
                                                        e.getMessage()));
                    } else {
                        // Handle authentication failure
                        errorMessage.setValue(
                                "Login failed: " +
                                        (task.getException() != null ?
                                                task.getException().getMessage() : "Unknown error"));
                    }
                });
    }
}