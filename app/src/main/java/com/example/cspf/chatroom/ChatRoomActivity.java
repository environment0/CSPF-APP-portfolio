package com.example.cspf.chatroom;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.ApiClient;
import com.example.cspf.ApiService;
import com.example.cspf.R;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity {

    private static final String TAG = "ChatRoomActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_REQUEST = 100;

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private EditText messageInput;
    private ImageButton sendButton, attachButton, backButton;

    private List<Message> messageList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private String roomId;
    private String accessToken;
    private String currentUserId;

    private ChatRoomSocket chatRoomSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        initializeData();
        initializeUI();
        initializeChatSocket();
        setupButtons();
    }

    private void initializeData() {
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        roomId = getIntent().getStringExtra("chatRoomID");
        accessToken = getIntent().getStringExtra("accessToken");

        if (roomId == null || accessToken == null) {
            Toast.makeText(this, "유효하지 않은 채팅방 정보입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            String token = accessToken;
            if (token.startsWith("authorization=")) {
                token = token.substring("authorization=".length());
            }

            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT));
                JSONObject jsonPayload = new JSONObject(payload);
                if (jsonPayload.has("sub")) {
                    currentUserId = jsonPayload.getString("sub");
                    Log.d(TAG, "User ID extracted from token: " + currentUserId);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userCode", currentUserId);
                    editor.apply();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting user ID from token", e);
        }

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "Failed to extract user ID from token");
            Toast.makeText(this, "사용자 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!accessToken.startsWith("authorization=")) {
            accessToken = "authorization=" + accessToken;
        }

        Log.d(TAG, "Initialized with roomId: " + roomId);
        Log.d(TAG, "Current User ID: " + currentUserId);
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerViewMessages);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        attachButton = findViewById(R.id.attachButton);
        backButton = findViewById(R.id.backButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        recyclerView.setAdapter(messageAdapter);

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void initializeChatSocket() {
        OkHttpClient okHttpClient = ApiClient.getOkHttpClient(this);
        chatRoomSocket = new ChatRoomSocket(message -> runOnUiThread(() -> {
            messageAdapter.addMessage(message);
            recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
        }), okHttpClient, sharedPreferences);

        chatRoomSocket.connect(roomId, accessToken);
    }

    private void setupButtons() {
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendTextMessage(messageText);
                messageInput.setText("");
            }
        });

        attachButton.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_MEDIA_IMAGES)) {
                    new AlertDialog.Builder(this)
                            .setTitle("권한 필요")
                            .setMessage("이미지 업로드를 위해 저장소 접근 권한이 필요합니다.")
                            .setPositiveButton("설정", (dialog, which) -> {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                        STORAGE_PERMISSION_REQUEST);
                            })
                            .setNegativeButton("취소", null)
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                            STORAGE_PERMISSION_REQUEST);
                }
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setTitle("권한 필요")
                            .setMessage("이미지 업로드를 위해 저장소 접근 권한이 필요합니다.")
                            .setPositiveButton("설정", (dialog, which) -> {
                                ActivityCompat.requestPermissions(this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        STORAGE_PERMISSION_REQUEST);
                            })
                            .setNegativeButton("취소", null)
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_REQUEST);
                }
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    new AlertDialog.Builder(this)
                            .setTitle("권한 필요")
                            .setMessage("앱 설정에서 저장소 접근 권한을 허용해주세요")
                            .setPositiveButton("설정으로 이동", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .setNegativeButton("취소", null)
                            .show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImageToServer(imageUri);
        }
    }

    private File getFileFromUri(Uri uri) {
        String realPath = getRealPathFromURI(uri);
        if (realPath != null) {
            File file = new File(realPath);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {"_data"};
        String filePath = null;
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow("_data");
                filePath = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting real path: " + e.getMessage());
        }
        return filePath;
    }

    private void uploadImageToServer(Uri imageUri) {
        File file = getFileFromUri(imageUri);
        if (file == null || !file.exists()) {
            Toast.makeText(this, "이미지를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        runOnUiThread(() -> Toast.makeText(this, "이미지 업로드 중...", Toast.LENGTH_SHORT).show());

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("files", file.getName(), requestFile);

        apiService.uploadImage(roomId, body, accessToken).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(ChatRoomActivity.this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show());
                    // 소켓을 통해 이미지 메시지가 자동으로 전달되므로 추가 처리 불필요
                } else {
                    runOnUiThread(() -> Toast.makeText(ChatRoomActivity.this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                runOnUiThread(() -> Toast.makeText(ChatRoomActivity.this, "네트워크 오류: 이미지 업로드 실패", Toast.LENGTH_SHORT).show());
                Log.e(TAG, "Image upload network error: " + t.getMessage());
            }
        });
    }

    private void sendTextMessage(String messageText) {
        Message textMessage = new Message(currentUserId, messageText, null);
        chatRoomSocket.sendMessage(currentUserId, messageText);
        messageAdapter.addMessage(textMessage);
        recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRoomSocket != null) {
            chatRoomSocket.disconnect();
        }
    }
}