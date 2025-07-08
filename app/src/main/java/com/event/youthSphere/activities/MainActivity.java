package com.event.youthSphere.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.event.youthSphere.R;
import com.event.youthSphere.fragments.auth.LoginFragment;
import com.event.youthSphere.fragments.auth.SignupFragment;
import com.event.youthSphere.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity hosts the login and signup screens using ViewPager2 and TabLayout.
 * If the user is already logged in, it redirects to DashboardActivity.
 */
public class MainActivity extends AppCompatActivity {

    private final String[] tabTitles = new String[]{"Login", "Signup"};
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    /**
     * Redirects authenticated and logged-in users directly to the dashboard.
     */
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SessionManager sessionManager = new SessionManager(this);

        if (currentUser != null && sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish(); // Prevent returning to login/signup
        }
    }

    /**
     * Initializes the activity, sets up tab layout and view pager for login/signup.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        applyWindowInsetsToTabLayout();
        setupViewPager();
    }

    /**
     * Applies top padding to the tab layout to handle system insets (status bar).
     */
    private void applyWindowInsetsToTabLayout() {
        ViewCompat.setOnApplyWindowInsetsListener(tabLayout, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(0, topInset, 0, 0);
            return insets;
        });
    }

    /**
     * Sets up the ViewPager2 and attaches it to the TabLayout using a FragmentStateAdapter.
     */
    private void setupViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new LoginFragment());
        fragments.add(new SignupFragment());

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        });

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
}
