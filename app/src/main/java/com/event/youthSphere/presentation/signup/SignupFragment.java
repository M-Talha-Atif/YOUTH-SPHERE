package com.event.youthSphere.presentation.signup;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.event.youthSphere.R;
import com.event.youthSphere.databinding.FragmentSignupBinding;

public class SignupFragment extends Fragment {

    private FragmentSignupBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        binding.signupButton.setOnClickListener(v -> {
            String email = binding.signupEmailEditText.getText().toString();
            String pass = binding.signupPasswordEditText.getText().toString();
            String confirm = binding.confirmPasswordEditText.getText().toString();
            // TODO: Validate & register user
        });
        return binding.getRoot();
    }
}
