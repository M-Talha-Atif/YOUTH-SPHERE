package com.event.youthSphere.domain.usecase.auth;


import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.event.youthSphere.domain.repository.UserRepository;

public class LoginUserUseCase {

    private final UserRepository repository;

    public LoginUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Task<AuthResult> execute(String email, String password) {
        return repository.loginUserWithEmail(email, password);
    }

    public UserRepository getUserRepository() {
        return repository;
    }
}

