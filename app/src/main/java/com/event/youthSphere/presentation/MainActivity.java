package com.event.youthSphere.presentation;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.event.youthSphere.R;
import com.event.youthSphere.databinding.ActivityMainBinding;
import com.event.youthSphere.presentation.login.LoginFragment;
import com.event.youthSphere.presentation.signup.SignupFragment;
import com.event.youthSphere.utils.SessionManager;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final String[] tabTitles = new String[]{"Login", "Signup"};


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        SessionManager sessionManager = new SessionManager(this);

        if (currentUser != null && sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish(); // prevent back to login/signup
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tabLayout), (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(0, topInset, 0, 0);
            return insets;
        });


        setupViewPager();
    }

    private void setupViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new LoginFragment());
        fragments.add(new SignupFragment());

        binding.viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        });

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }
}
