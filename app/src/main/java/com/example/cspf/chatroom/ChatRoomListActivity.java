package com.example.cspf.chatroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.ApiClient;
import com.example.cspf.ApiService;
import com.example.cspf.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatRoomListAdapter adapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_list);

        recyclerView = findViewById(R.id.recyclerViewChatRooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        loadChatRooms();
    }

    private void loadChatRooms() {
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        // SharedPreferences에서 토큰 가져오기
        String token = sharedPreferences.getString("access_token", "");
        System.out.print("chatRoom");
        Call<ReqChatRoom> call = apiService.getChatRooms(token);

        call.enqueue(new Callback<ReqChatRoom>() {
            @Override
            public void onResponse(Call<ReqChatRoom> call, Response<ReqChatRoom> response) {
                System.out.print("chatRoom"+response.body());
                if (response.isSuccessful() && response.body() != null) {
                    ReqChatRoom chatRoomList = response.body();

                    adapter = new ChatRoomListAdapter(chatRoomList.getContent(), chatRoom -> {
                        // 채팅방 클릭 시 ChatRoomActivity로 이동하도록 설정

                        Intent intent = new Intent(ChatRoomListActivity.this, ChatRoomActivity.class);
                        // 필요한 데이터를 전달
                        intent.putExtra("chatRoomID", chatRoom.getChatRoomID());
                        intent.putExtra("chatRoomName", chatRoom.getChatRoom());
                        intent.putExtra("accessToken", token);
                        startActivity(intent);

                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(ChatRoomListActivity.this, "채팅방 목록을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("ChatRoomListActivity", "Failed to load chat rooms");
                }
            }

            @Override
            public void onFailure(Call<ReqChatRoom> call, Throwable t) {
                Toast.makeText(ChatRoomListActivity.this, "오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ChatRoomListActivity", "Error: " + t.getMessage());
            }
        });
    }
}