package com.example.cspf.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cspf.ApiService;
import com.example.cspf.ExpertApiService;
import com.example.cspf.ExpertRetrofitClient;
import com.example.cspf.MainActivity;
import com.example.cspf.R;
import com.example.cspf.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonExpertLogin, buttonGuest;
    private TextView buttonSignup;
    private SharedPreferences loginPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonExpertLogin = findViewById(R.id.buttonExpertLogin);
        buttonSignup = findViewById(R.id.buttonSignup);
        buttonGuest = findViewById(R.id.buttonGuest);

        loginPrefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        buttonLogin.setOnClickListener(view -> login(false));
        buttonExpertLogin.setOnClickListener(view -> login(true));
        buttonSignup.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        buttonGuest.setOnClickListener(view -> {
            Toast.makeText(this, "비회원으로 시작", Toast.LENGTH_SHORT).show();
            navigateToMainActivity(true);
        });
    }

    private void login(boolean isExpert) {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "아이디와 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(username, password);

        if (isExpert) {
            ExpertApiService expertService = ExpertRetrofitClient.getRetrofitInstance().create(ExpertApiService.class);
            Call<LoginResponse> call = expertService.loginExpert(loginRequest);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    handleLoginResponse(response, true);
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    showErrorToast("전문가 로그인 오류: " + t.getMessage());
                    Log.e(TAG, "Expert Login Error", t);
                }
            });
        } else {
            ApiService userService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
            Call<LoginResponse> call = userService.login(loginRequest);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    handleLoginResponse(response, false);
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    showErrorToast("일반 로그인 오류: " + t.getMessage());
                    Log.e(TAG, "User Login Error", t);
                }
            });
        }
    }

    private void handleLoginResponse(Response<LoginResponse> response, boolean isExpert) {
        if (response.isSuccessful() && response.body() != null) {
            LoginResponse loginResponse = response.body();

            List<String> cookies = response.headers().values("Set-Cookie");
            String accessToken = null;
            String refreshToken = null;
            String username = editTextUsername.getText().toString().trim();

            for (String cookie : cookies) {
                if (cookie.startsWith("authorization")) {
                    accessToken = cookie.split(";")[0];
                } else if (cookie.startsWith("refreshToken")) {
                    refreshToken = cookie.split(";")[0];
                }
            }

            if (accessToken != null && refreshToken != null) {
                SharedPreferences.Editor editor = loginPrefs.edit();


                String nickname;
                if (loginResponse != null && loginResponse.getNickname() != null) {
                    nickname = loginResponse.getNickname();
                } else {
                    // 서버에서 닉네임이 안왔을 때는 유저네임을 보여줌
                    nickname = editTextUsername.getText().toString().trim();
                }

                editor.putString("access_token", accessToken);
                editor.putString("refresh_token", refreshToken);
                editor.putString("nickname", nickname);
                editor.putBoolean("isLoggedIn", true);
                editor.commit();

                // 저장된 값 확인용 로그
                Log.d(TAG, "Saved nickname: " + loginPrefs.getString("nickname", "Guest"));

                showSuccessToast((isExpert ? "전문가" : "일반") + " 로그인 성공!");
                navigateToMainActivity(false);
            } else {
                showErrorToast("토큰을 가져올 수 없습니다.");
            }
        }
    }
    private void navigateToMainActivity(boolean isGuest) {
        SharedPreferences.Editor editor = loginPrefs.edit();

        if (isGuest) {
            // 게스트 모드일 경우 기존 로그인 데이터 초기화
            editor.clear();
            editor.putBoolean("isLoggedIn", false);
        } else {
            editor.putBoolean("isLoggedIn", true);
        }
        editor.apply();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("isGuest", isGuest);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    private void showSuccessToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}