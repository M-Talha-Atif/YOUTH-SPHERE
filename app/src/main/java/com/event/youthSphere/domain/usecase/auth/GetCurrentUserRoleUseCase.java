package com.event.youthSphere.domain.usecase.auth;



import com.event.youthSphere.domain.model.UserRole;
import com.event.youthSphere.domain.repository.UserRepository;
import com.google.android.gms.tasks.Task;

public class GetCurrentUserRoleUseCase {

    private final UserRepository userRepository;

    public GetCurrentUserRoleUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Task<UserRole> execute() {
        return userRepository.getCurrentUserRole();
    }
}

