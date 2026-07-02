package com.example.cspf.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cspf.ApiService;
import com.example.cspf.ExpertRetrofitClient;
import com.example.cspf.R;
import com.example.cspf.RetrofitClient;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private static final int REQUEST_GALLERY_IMAGE = 1;
    private static final int REQUEST_PROFILE_IMAGE = 2;
    private static final String TAG = "SignupActivity";

    // Image related fields
    private Uri certImageUri;
    private Uri normalProfileImageUri;
    private String uploadedCertImagePath;
    private String uploadedNormalProfilePath;

    // Normal user fields
    private EditText normalUsername, normalPassword, normalName, normalNickname, normalEmail, normalAddr, normalPhone;
    private EditText editTextNormalCode;
    private RadioGroup radioGroupNormalGender;
    private Button buttonUploadNormalProfile;
    private ImageView imageNormalProfile, imageNormalProfilePlaceholder;
    private TextView labelNormalCode;

    // Expert user fields
    private EditText expertUsername, expertPassword, expertName, expertCompany, expertEmail, expertPhone;
    private EditText editTextExpertCode;
    private RadioGroup radioGroupExpertType;
    private Button buttonUploadImage, buttonSendExpertCode, buttonVerifyExpertCode;
    private ImageView imageCert;
    private TextView labelExpertCode;

    // Common fields
    private RadioGroup radioGroupUserType;
    private Button signupButton, buttonSendNormalCode, buttonVerifyNormalCode;
    private View normalUserFields, expertFields;

    private boolean isNormalCodeVerified = false;
    private boolean isExpertCodeVerified = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeFields();
        setupListeners();
    }

    private void initializeFields() {
        // Normal user fields
        normalUsername = findViewById(R.id.editTextNormalUsername);
        normalPassword = findViewById(R.id.editTextNormalPassword);
        normalName = findViewById(R.id.editTextNormalName);
        normalNickname = findViewById(R.id.editTextNormalNickname);
        normalEmail = findViewById(R.id.editTextNormalEmail);
        editTextNormalCode = findViewById(R.id.editTextNormalCode);
        normalAddr = findViewById(R.id.editTextNormalAddr);
        normalPhone = findViewById(R.id.editTextNormalPhone);
        radioGroupNormalGender = findViewById(R.id.radioGroupNormalGender);
        buttonUploadNormalProfile = findViewById(R.id.buttonUploadNormalProfile);
        imageNormalProfile = findViewById(R.id.imageNormalProfile);
        imageNormalProfilePlaceholder = findViewById(R.id.imageNormalProfilePlaceholder);
        labelNormalCode = findViewById(R.id.labelNormalCode);
        buttonSendNormalCode = findViewById(R.id.buttonSendNormalCode);
        buttonVerifyNormalCode = findViewById(R.id.buttonVerifyNormalCode);

        // Expert user fields
        expertUsername = findViewById(R.id.editTextExpertUsername);
        expertPassword = findViewById(R.id.editTextExpertPassword);
        expertName = findViewById(R.id.editTextExpertName);
        expertCompany = findViewById(R.id.editTextExpertCompany);
        expertEmail = findViewById(R.id.editTextExpertEmail);
        editTextExpertCode = findViewById(R.id.editTextExpertCode);
        expertPhone = findViewById(R.id.editTextExpertPhone);
        radioGroupExpertType = findViewById(R.id.radioGroupExpertType);
        buttonUploadImage = findViewById(R.id.buttonUploadImage);
        imageCert = findViewById(R.id.imageCert);
        labelExpertCode = findViewById(R.id.labelExpertCode);
        buttonSendExpertCode = findViewById(R.id.buttonSendExpertCode);
        buttonVerifyExpertCode = findViewById(R.id.buttonVerifyExpertCode);

        // Common fields
        radioGroupUserType = findViewById(R.id.radioGroupUserType);
        signupButton = findViewById(R.id.buttonSignup);
        normalUserFields = findViewById(R.id.normalUserFields);
        expertFields = findViewById(R.id.expertFields);
    }

    private void setupListeners() {
        radioGroupUserType.setOnCheckedChangeListener((group, checkedId) -> {
            normalUserFields.setVisibility(checkedId == R.id.radioButtonNormalUser ? View.VISIBLE : View.GONE);
            expertFields.setVisibility(checkedId == R.id.radioButtonExpertUser ? View.VISIBLE : View.GONE);
        });

        buttonUploadImage.setOnClickListener(v -> openGallery(REQUEST_GALLERY_IMAGE));
        buttonUploadNormalProfile.setOnClickListener(v -> openGallery(REQUEST_PROFILE_IMAGE));
        signupButton.setOnClickListener(v -> handleSignupAction());

        // 일반 사용자 이메일 인증
        buttonSendNormalCode.setOnClickListener(v -> {
            String email = normalEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                sendNormalVerificationCode(email);
            } else {
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonVerifyNormalCode.setOnClickListener(v -> {
            String code = editTextNormalCode.getText().toString().trim();
            if (!code.isEmpty()) {
                verifyNormalCode(code);
            } else {
                Toast.makeText(this, "인증코드를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 전문가 이메일 인증
        buttonSendExpertCode.setOnClickListener(v -> {
            String email = expertEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                sendExpertVerificationCode(email);
            } else {
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonVerifyExpertCode.setOnClickListener(v -> {
            String code = editTextExpertCode.getText().toString().trim();
            if (!code.isEmpty()) {
                verifyExpertCode(code);
            } else {
                Toast.makeText(this, "인증코드를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_GALLERY_IMAGE) {
                certImageUri = data.getData();
                imageCert.setImageURI(certImageUri);
                uploadCertificationImage(certImageUri);
            } else if (requestCode == REQUEST_PROFILE_IMAGE) {
                normalProfileImageUri = data.getData();
                imageNormalProfile.setImageURI(normalProfileImageUri);
                imageNormalProfilePlaceholder.setVisibility(View.GONE);
                uploadNormalProfileImage(normalProfileImageUri);
            }
        }
    }

    private void uploadNormalProfileImage(Uri imageUri) {
        String imagePath = getRealPathFromURI(imageUri);
        if (imagePath == null) {
            Toast.makeText(this, "이미지 경로를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(imagePath);
        Log.d(TAG, "파일 경로: " + imagePath);
        Log.d(TAG, "파일 존재 여부: " + file.exists());
        Log.d(TAG, "파일 크기: " + file.length());

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("img", file.getName(), requestFile);

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        String cookie = getCookie("access_token");

        apiService.uploadProfileImage(body, cookie).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        uploadedNormalProfilePath = response.body().string();
                        runOnUiThread(() -> {
                            Toast.makeText(SignupActivity.this, "프로필 이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                            imageNormalProfile.setImageURI(imageUri);
                            imageNormalProfilePlaceholder.setVisibility(View.GONE);
                        });
                    } catch (Exception e) {
                        handleUploadError(e);
                    }
                } else {
                    handleUploadFailure(response);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleApiError(t);
            }
        });
    }

    private void uploadCertificationImage(Uri imageUri) {
        String imagePath = getRealPathFromURI(imageUri);
        if (imagePath == null) {
            Toast.makeText(this, "이미지 경로를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(imagePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        ApiService apiService = ExpertRetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.uploadCertificationImage(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        uploadedCertImagePath = response.body().string();
                        Toast.makeText(SignupActivity.this, "자격증 이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        handleUploadError(e);
                    }
                } else {
                    handleUploadFailure(response);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                handleApiError(t);
            }
        });
    }

    private void handleSignupAction() {
        if (radioGroupUserType.getCheckedRadioButtonId() == R.id.radioButtonNormalUser) {
            if (!isNormalCodeVerified) {
                Toast.makeText(this, "일반 회원 이메일 인증을 먼저 완료하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            registerNormalUser();
        } else if (radioGroupUserType.getCheckedRadioButtonId() == R.id.radioButtonExpertUser) {
            if (!isExpertCodeVerified) {
                Toast.makeText(this, "전문가 이메일 인증을 먼저 완료하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            registerExpertUser();
        } else {
            Toast.makeText(this, "회원 유형을 선택하세요", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerNormalUser() {
        String username = normalUsername.getText().toString().trim();
        String password = normalPassword.getText().toString().trim();
        String name = normalName.getText().toString().trim();
        String nickname = normalNickname.getText().toString().trim();
        String email = normalEmail.getText().toString().trim();
        String addr = normalAddr.getText().toString().trim();
        String phone = normalPhone.getText().toString().trim();
        String gender = ((RadioButton) findViewById(radioGroupNormalGender.getCheckedRadioButtonId())).getText().toString();

        if (!validateFields(username, password, name, nickname, email, addr, phone, gender)) {
            return;
        }

        SignupRequest request = new SignupRequest(username, password, name, nickname, email, addr, phone, gender, uploadedNormalProfilePath);
        sendSignupRequest(request);
    }

    private void registerExpertUser() {
        String username = expertUsername.getText().toString().trim();
        String password = expertPassword.getText().toString().trim();
        String name = expertName.getText().toString().trim();
        String company = expertCompany.getText().toString().trim();
        String email = expertEmail.getText().toString().trim();
        String phone = expertPhone.getText().toString().trim();
        String expertType = getSelectedExpertType();

        if (expertType == null || !validateFields(username, password, name, company, email, phone, expertType)) {
            return;
        }

        if (uploadedCertImagePath != null) {
            ExpertRequest expertRequest = new ExpertRequest(username, password, name, company, email, phone, expertType, uploadedCertImagePath);
            sendExpertSignupRequest(expertRequest);
        } else {
            Toast.makeText(this, "자격 증명 이미지를 업로드하세요.", Toast.LENGTH_SHORT).show();
        }
    }
    private void sendSignupRequest(SignupRequest request) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.signup(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "회원가입 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendExpertSignupRequest(ExpertRequest expertRequest) {
        ApiService apiService = ExpertRetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.signupExpert(expertRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, "전문가 회원가입 성공", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "전문가 회원가입 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNormalVerificationCode(String email) {
        ApiService apiService = ExpertRetrofitClient.getRetrofitInstance().create(ApiService.class);
        EmailRequest request = new EmailRequest(email);

        apiService.sendRegisterAuthCode(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    saveCookie("normalEmailToken", response.headers().get("Set-Cookie"));
                    Toast.makeText(SignupActivity.this, "인증코드 발송 성공", Toast.LENGTH_SHORT).show();

                    labelNormalCode.setVisibility(View.VISIBLE);
                    editTextNormalCode.setVisibility(View.VISIBLE);
                    buttonVerifyNormalCode.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(SignupActivity.this, "인증코드 발송 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "서버 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendExpertVerificationCode(String email) {
        ApiService apiService = ExpertRetrofitClient.getRetrofitInstance().create(ApiService.class);
        EmailRequest request = new EmailRequest(email);

        apiService.sendRegisterAuthCode(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    saveCookie("expertEmailToken", response.headers().get("Set-Cookie"));
                    Toast.makeText(SignupActivity.this, "인증코드 발송 성공", Toast.LENGTH_SHORT).show();

                    labelExpertCode.setVisibility(View.VISIBLE);
                    editTextExpertCode.setVisibility(View.VISIBLE);
                    buttonVerifyExpertCode.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(SignupActivity.this, "인증코드 발송 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "서버 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyNormalCode(String code) {
        String emailToken = getCookie("normalEmailToken");
        ApiService apiService = ExpertRetrofitClient.getRetrofitInstance().create(ApiService.class);
        VerificationRequest request = new VerificationRequest(code);

        apiService.verifyMailCode(emailToken, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isNormalCodeVerified = true;
                    Toast.makeText(SignupActivity.this, "인증 성공", Toast.LENGTH_SHORT).show();

                    editTextNormalCode.setEnabled(false);
                    buttonVerifyNormalCode.setEnabled(false);
                    buttonSendNormalCode.setEnabled(false);
                } else {
                    Toast.makeText(SignupActivity.this, "인증 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyExpertCode(String code) {
        String emailToken = getCookie("expertEmailToken");
        ApiService apiService = ExpertRetrofitClient.getRetrofitInstance().create(ApiService.class);
        VerificationRequest request = new VerificationRequest(code);

        apiService.verifyMailCode(emailToken, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isExpertCodeVerified = true;
                    Toast.makeText(SignupActivity.this, "인증 성공", Toast.LENGTH_SHORT).show();

                    editTextExpertCode.setEnabled(false);
                    buttonVerifyExpertCode.setEnabled(false);
                    buttonSendExpertCode.setEnabled(false);
                } else {
                    Toast.makeText(SignupActivity.this, "인증 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SignupActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedExpertType() {
        int selectedExpertTypeId = radioGroupExpertType.getCheckedRadioButtonId();
        if (selectedExpertTypeId == R.id.radioButtonLawyer) {
            return "Attorney";
        } else if (selectedExpertTypeId == R.id.radioButtonVeterinarian) {
            return "Veterinarian";
        } else if (selectedExpertTypeId == R.id.radioButtonInsurance) {
            return "Insurance";
        } else {
            return null;
        }
    }

    private boolean validateFields(String... fields) {
        for (String field : fields) {
            if (field.isEmpty()) {
                Toast.makeText(this, "모든 필드를 입력하세요", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void handleUploadError(Exception e) {
        e.printStackTrace();
        Log.e(TAG, "응답 처리 실패", e);
        runOnUiThread(() -> {
            Toast.makeText(SignupActivity.this, "응답 처리 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void handleUploadFailure(Response<ResponseBody> response) {
        try {
            String errorBody = response.errorBody().string();
            Log.e(TAG, "업로드 실패. HTTP 코드: " + response.code());
            Log.e(TAG, "에러 응답: " + errorBody);
            runOnUiThread(() -> {
                Toast.makeText(SignupActivity.this, "업로드 실패: " + errorBody, Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "에러 응답 처리 실패", e);
        }
    }

    private void handleApiError(Throwable t) {
        Log.e(TAG, "API 호출 실패", t);
        runOnUiThread(() -> {
            Toast.makeText(SignupActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    private void saveCookie(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppCookies", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String getCookie(String cookieName) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppCookies", MODE_PRIVATE);
        return sharedPreferences.getString(cookieName, "");
    }
}