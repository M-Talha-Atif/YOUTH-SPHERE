package com.event.youthSphere.domain.usecase.auth;

import com.event.youthSphere.domain.model.users.RoleUser;
import com.event.youthSphere.domain.repository.UserRepository;
import com.google.android.gms.tasks.Task;

public class SaveUserUseCase {
    private final UserRepository repository;

    public SaveUserUseCase(UserRepository repository) {
        this.repository = repository;
    }

    public Task<Void> execute(RoleUser user) {
        return repository.saveUser(user);
    }
}
