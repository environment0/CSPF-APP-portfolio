package com.example.cspf.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cspf.MainActivity;
import com.example.cspf.R;
import com.example.cspf.UnsafeHttps;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;

public class GoogleSignInActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleSignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Google Sign-In 옵션 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_server_web_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 구글 로그인 버튼 클릭 리스너 설정
        ImageButton googleLoginButton = findViewById(R.id.buttonGoogleLogin);
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken(); // ID 토큰 가져오기
            sendTokenToServer(idToken);  // 서버로 ID 토큰 전송
        } catch (ApiException e) {
            Log.w("Google Sign-In", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "로그인 실패: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendTokenToServer(String idToken) {
        Log.d(TAG, "sendTokenToServer: "+idToken);
        String url = "https://hyproz.myds.me:3800/auth/google";  // 서버 주소
        RequestBody requestBody = new FormBody.Builder()
                .add("idToken", idToken)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        OkHttpClient client = UnsafeHttps.getUnsafeOkHttpClient().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(GoogleSignInActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("Google Sign-In", "Token successfully sent to server");
                    runOnUiThread(() -> {
                        Toast.makeText(GoogleSignInActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        // 성공 시 MainActivity로 이동
                        Intent intent = new Intent(GoogleSignInActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    Log.e("Google Sign-In", "Failed to send token to server: " + response.message());
                    runOnUiThread(() -> Toast.makeText(GoogleSignInActivity.this, "토큰 전송 실패", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
