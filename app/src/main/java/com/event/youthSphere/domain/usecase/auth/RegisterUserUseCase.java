package com.event.youthSphere.domain.usecase.auth;


import com.event.youthSphere.domain.model.UserRole;
import com.event.youthSphere.domain.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class RegisterUserUseCase {

    private final UserRepository repository;

    public RegisterUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Task<AuthResult> execute(String email, String password, UserRole role) {
        return repository.registerUserWithEmail(email, password, role);
    }
}
