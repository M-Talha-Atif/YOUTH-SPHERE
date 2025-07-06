package com.event.youthSphere.domain.repository;

import com.event.youthSphere.domain.model.users.RoleUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.event.youthSphere.domain.model.UserRole;

/**
 * Defines the contract for user-related operations in the application.
 * This interface abstracts user authentication, registration, and role management operations,
 * allowing for different implementations (e.g., Firebase, Mock, etc.).
 */
public interface UserRepository {

    /**
     * Registers a new user with the provided credentials and role.
     *
     * @param email The email address to register with
     * @param password The password for the new account
     * @param role The user's assigned role (PARTICIPANT, INSTRUCTOR, etc.)
     * @return Task<AuthResult> containing the authentication result
     */
    Task<AuthResult> registerUserWithEmail(String email, String password, UserRole role);

    /**
     * Authenticates an existing user with email and password credentials.
     *
     * @param email The user's registered email address
     * @param password The user's password
     * @return Task<AuthResult> containing the authentication result
     */
    Task<AuthResult> loginUserWithEmail(String email, String password);

    /**
     * Persists user data to the repository.
     *
     * @param user The complete user object including metadata and role
     * @return Task<Void> indicating completion status
     */
    Task<Void> saveUser(RoleUser user);

    /**
     * Retrieves the current authenticated user's role.
     *
     * @return Task<UserRole> containing the user's role if available
     * @throws Exception if no user is authenticated or role data is missing
     */
    Task<UserRole> getCurrentUserRole();

    /**
     * Terminates the current user session.
     * Clears authentication state but preserves user data in the repository.
     */
    void signOut();
}