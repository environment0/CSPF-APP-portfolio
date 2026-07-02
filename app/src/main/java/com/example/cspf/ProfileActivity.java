package com.example.cspf;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private SharedPreferences sharedPreferences;

    private ProgressBar progressBar;
    private TextView textViewError;
    private TextView textViewUsername, textViewName, textViewNickname, textViewEmail, textViewAddr, textViewPhone, textViewGender;
    private Button buttonRegisterPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        initializeViews();
        setupPetRegistration();
        fetchProfile();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        textViewError = findViewById(R.id.textViewError);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewName = findViewById(R.id.textViewName);
        textViewNickname = findViewById(R.id.textViewNickname);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewAddr = findViewById(R.id.textViewAddr);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewGender = findViewById(R.id.textViewGender);
        buttonRegisterPet = findViewById(R.id.buttonRegisterPet);
    }

    private void setupPetRegistration() {
        buttonRegisterPet.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, CameraView.class);
            startActivity(intent);
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        textViewError.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        textViewError.setText(message);
        textViewError.setVisibility(View.VISIBLE);
    }

    private void fetchProfile() {
        String accessToken = sharedPreferences.getString("access_token", null);
        Log.d(TAG, "Access Token for Profile: " + accessToken);

        if (accessToken == null) {
            showError("로그인이 필요합니다.");
            return;
        }

        showLoading();
        ApiService apiService = ExpertRetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<ProfileResponse> call = apiService.userProfile(accessToken);

        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    updateProfileUI(response.body());
                } else {
                    showError("프로필 정보를 가져올 수 없습니다.");
                    Log.e(TAG, "Failed to fetch profile data: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                hideLoading();
                showError("서버 연결 실패");
                Log.e(TAG, "Error fetching profile data", t);
            }
        });
    }

    private void updateProfileUI(ProfileResponse profile) {
        Log.d(TAG, "Profile Data: " + profile.toString());
        textViewUsername.setText(profile.getUsername());
        textViewName.setText(profile.getName());
        textViewNickname.setText(profile.getNickname());
        textViewEmail.setText(profile.getEmail());
        textViewAddr.setText(profile.getAddr());
        textViewPhone.setText(profile.getPhone());
        textViewGender.setText(profile.getGender());
    }
}