package com.event.youthSphere.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.event.youthSphere.R;
import com.event.youthSphere.fragments.coordinator.ManageActivitiesFragment;
import com.event.youthSphere.models.UserRole;
import com.event.youthSphere.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * DashboardActivity is the landing screen after a successful login.
 * It displays different bottom navigation menus and behaviors based on user role.
 */
public class DashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private Toolbar topAppBar;
    private LinearLayout bottomNavigationView;
    private Button navBtn1, navBtn2, navBtn3;
    private UserRole currentRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge UI
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Initialize views using findViewById
        topAppBar = findViewById(R.id.topAppBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navBtn1 = findViewById(R.id.nav_btn_1);
        navBtn2 = findViewById(R.id.nav_btn_2);
        navBtn3 = findViewById(R.id.nav_btn_3);


        // Set up toolbar as the action bar
        setSupportActionBar(topAppBar);

        // Handle insets (status bar, navigation bar)
        applyEdgeToEdgeInsets();

        // Handle user role setup
        handleUserRole();
    }

    /**
     * Applies edge-to-edge padding for status bar and bottom navigation.
     */
    private void applyEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply top padding to app bar
            ViewCompat.setPaddingRelative(
                    topAppBar,
                    ViewCompat.getPaddingStart(topAppBar),
                    systemBars.top,
                    ViewCompat.getPaddingEnd(topAppBar),
                    topAppBar.getPaddingBottom()
            );

            // Apply bottom padding to bottom nav
            ViewCompat.setPaddingRelative(
                    bottomNavigationView,
                    ViewCompat.getPaddingStart(bottomNavigationView),
                    bottomNavigationView.getPaddingTop(),
                    ViewCompat.getPaddingEnd(bottomNavigationView),
                    systemBars.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Handles user role determination from intent or session, and sets up appropriate UI.
     */
    private void handleUserRole() {
        String roleString = getIntent().getStringExtra("user_role");

        if (roleString != null && !roleString.isEmpty()) {
            try {
                currentRole = UserRole.valueOf(roleString);
                sessionManager.setUserRole(roleString);
            } catch (IllegalArgumentException e) {
                showToast("Invalid role received");
                return;
            }
        } else {
            roleString = sessionManager.getUserRole();
            if (roleString != null) {
                try {
                    currentRole = UserRole.valueOf(roleString);
                } catch (IllegalArgumentException e) {
                    showToast("Invalid role in session");
                    return;
                }
            } else {
                showToast("No user role provided");
                return;
            }
        }

        setupNativeBottomNav(currentRole);
        setupNavigationForRole(currentRole);
    }

    /**
     * Configures the bottom navigation menu based on user role.
     * @param role The authenticated user's role
     */
    private void setupNativeBottomNav(UserRole role) {
        switch (role) {
            case PARTICIPANT:
                navBtn1.setText("Activities");
                navBtn2.setText("My Calendar");
                navBtn3.setText("Progress");
                break;
            case PARENT:
                navBtn1.setText("Child Progress");
                navBtn2.setText("Feedback");
                navBtn3.setText("Photos");
                break;
            case COORDINATOR:
                navBtn1.setText("Manage Activities");
                navBtn2.setText("Instructors");
                navBtn3.setText("Top Activities");
                break;
            case INSTRUCTOR:
                navBtn1.setText("My Sessions");
                navBtn2.setText("Feedback");
                navBtn3.setText("Upload Photos");
                break;
            case SUPER_ADMIN:
                navBtn1.setText("Overview");
                navBtn2.setText("Users");
                navBtn3.setText("Reviews");
                break;
            default:
                navBtn1.setText("Nav 1");
                navBtn2.setText("Nav 2");
                navBtn3.setText("Nav 3");
        }

        navBtn1.setOnClickListener(v -> showToast(navBtn1.getText().toString()));
        navBtn2.setOnClickListener(v -> showToast(navBtn2.getText().toString()));
        navBtn3.setOnClickListener(v -> showToast(navBtn3.getText().toString()));
    }


    /**
     * Inflates the top-right overflow menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard_overflow, menu);
        return true;
    }

    /**
     * Handles click events on the overflow menu items.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Logs out the user, clears session, and redirects to login screen.
     */
    private void performLogout() {
        sessionManager.clearSession();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Placeholder method to set up navigation based on user role.
     * @param role The authenticated user's role
     */
    private void setupNavigationForRole(UserRole role) {
        showToast("Logged in as: " + role.name());
        if (role == UserRole.COORDINATOR) {
            loadFragment(new ManageActivitiesFragment());
            navBtn1.setOnClickListener(v -> loadFragment(new ManageActivitiesFragment()));
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboardFragmentContainer, fragment)
                .commit();
    }


    /**
     * Utility to show a Toast message.
     * @param message Message to display
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
