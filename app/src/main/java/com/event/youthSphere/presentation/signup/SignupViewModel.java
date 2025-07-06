package com.event.youthSphere.presentation.signup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.event.youthSphere.domain.model.users.CoordinatorUser;
import com.event.youthSphere.domain.model.users.GenericUser;
import com.event.youthSphere.domain.model.users.InstructorUser;
import com.event.youthSphere.domain.model.users.ParentUser;
import com.event.youthSphere.domain.model.users.ParticipantUser;
import com.event.youthSphere.domain.model.users.RoleUser;
import com.event.youthSphere.domain.model.UserRole;
import com.event.youthSphere.domain.model.users.SuperAdminUser;
import com.event.youthSphere.domain.usecase.auth.RegisterUserUseCase;
import com.event.youthSphere.domain.usecase.auth.SaveUserUseCase;

import java.util.ArrayList;
import java.util.Objects;

/**
 * ViewModel for handling user registration business logic.
 * Manages the signup process including authentication and user data persistence,
 * while exposing observable state for the UI.
 */
public class SignupViewModel extends ViewModel {

    private final RegisterUserUseCase registerUserUseCase;
    private final SaveUserUseCase saveUserUseCase;

    private final MutableLiveData<Boolean> signupSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Constructs a SignupViewModel with required use cases.
     *
     * @param registerUserUseCase Use case for user authentication registration
     * @param saveUserUseCase Use case for persisting user data
     */
    public SignupViewModel(RegisterUserUseCase registerUserUseCase, SaveUserUseCase saveUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.saveUserUseCase = saveUserUseCase;
    }

    /**
     * Provides observable signup success state.
     * @return LiveData<Boolean> that emits true when signup completes successfully
     */
    public LiveData<Boolean> getSignupSuccess() {
        return signupSuccess;
    }

    /**
     * Provides observable error messages during signup process.
     * @return LiveData<String> that emits error messages when they occur
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Handles the complete user registration flow including:
     * 1. Authentication registration
     * 2. Role-specific user object creation
     * 3. User data persistence
     *
     * @param email User's email address
     * @param password User's password
     * @param fullName User's full name
     * @param role User's assigned role
     */
    public void signup(String email, String password, String fullName, UserRole role) {
        registerUserUseCase.execute(email, password, role)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = Objects.requireNonNull(task.getResult().getUser()).getUid();

                        RoleUser user;

                        switch (role) {
                            case PARTICIPANT:
                                user = new ParticipantUser(uid, email, fullName, new ArrayList<>());
                                break;
                            case INSTRUCTOR:
                                user = new InstructorUser(uid, email, fullName, new ArrayList<>(), "", "");
                                break;
                            case PARENT:
                                user = new ParentUser(uid, email, fullName, new ArrayList<>());
                                break;
                            case COORDINATOR:
                                user = new CoordinatorUser(uid, email, fullName);
                                break;
                            case SUPER_ADMIN:
                                user = new SuperAdminUser(uid, email, fullName);
                                break;
                            default:
                                user = new GenericUser(uid, email, fullName, role.name());
                                break;
                        }

                        saveUserUseCase.execute(user)
                                .addOnCompleteListener(saveTask -> {
                                    if (saveTask.isSuccessful()) {
                                        signupSuccess.setValue(true);
                                    } else {
                                        errorMessage.setValue("Failed to save user data: " + Objects.requireNonNull(saveTask.getException()).getMessage());
                                    }
                                });
                    } else {
                        errorMessage.setValue("Signup failed: " + Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }
}