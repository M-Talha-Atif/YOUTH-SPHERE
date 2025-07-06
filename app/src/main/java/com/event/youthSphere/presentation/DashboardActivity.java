package com.event.youthSphere.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.event.youthSphere.R;
import com.event.youthSphere.databinding.ActivityDashboardBinding;
import com.event.youthSphere.domain.model.UserRole;
import com.event.youthSphere.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        sessionManager = new SessionManager(this);

        // Set up toolbar
        setSupportActionBar(binding.topAppBar);

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply padding to toolbar and bottom nav
            ViewCompat.setPaddingRelative(
                    binding.topAppBar,
                    ViewCompat.getPaddingStart(binding.topAppBar),
                    systemBars.top,
                    ViewCompat.getPaddingEnd(binding.topAppBar),
                    binding.topAppBar.getPaddingBottom()
            );

            ViewCompat.setPaddingRelative(
                    binding.bottomNavigationView,
                    ViewCompat.getPaddingStart(binding.bottomNavigationView),
                    binding.bottomNavigationView.getPaddingTop(),
                    ViewCompat.getPaddingEnd(binding.bottomNavigationView),
                    systemBars.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });

        // Handle user role
        handleUserRole();
    }

    private void handleUserRole() {
        String roleString = getIntent().getStringExtra("user_role");

        if (roleString != null && !roleString.isEmpty()) {
            try {
                UserRole userRole = UserRole.valueOf(roleString);
                setupMenuForRole(userRole);
                sessionManager.setUserRole(roleString);
                setupNavigationForRole(userRole);
            } catch (IllegalArgumentException e) {
                showToast("Invalid role received");
            }
        } else {
            roleString = sessionManager.getUserRole();
            if (roleString != null) {
                try {
                    UserRole userRole = UserRole.valueOf(roleString);
                    setupMenuForRole(userRole);
                    setupNavigationForRole(userRole);
                } catch (IllegalArgumentException e) {
                    showToast("Invalid role in session");
                }
            } else {
                showToast("No user role provided");
            }
        }
    }

    private void setupMenuForRole(UserRole userRole) {
        int menuRes = getMenuResourceForRole(userRole);
        binding.bottomNavigationView.getMenu().clear();
        binding.bottomNavigationView.inflateMenu(menuRes);
    }

    private int getMenuResourceForRole(UserRole role) {
        switch (role) {
            case PARTICIPANT: return R.menu.menu_participant_nav;
            case PARENT: return R.menu.menu_parent_nav;
            case COORDINATOR: return R.menu.menu_coordinator_nav;
            case INSTRUCTOR: return R.menu.menu_instructor_nav;
            case SUPER_ADMIN: return R.menu.menu_super_admin_nav;
            default: return R.menu.menu_default_nav;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard_overflow, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performLogout() {
        sessionManager.clearSession();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void setupNavigationForRole(UserRole role) {
        showToast("Logged in as: " + role.name());
        // TODO: Load fragments and handle bottom nav interactions
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Clean up binding reference
    }
}