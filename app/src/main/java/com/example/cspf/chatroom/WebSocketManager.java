package com.example.cspf.chatroom;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.cspf.UnsafeHttps;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static final int RECONNECT_DELAY_MILLIS = 5000; // 5초
    private final Handler handler = new Handler(Looper.getMainLooper());

    private OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private WebSocketListener webSocketListener;
    private OnMessageReceivedListener messageListener;
    private String roomId = "2f7d753d-ef8a-4130-a4c3-c7387645fd1e";
    private String url = "https://hyproz.myds.me:3800/";
    private boolean reconnect = true; // 자동 재연결 플래그

    public WebSocketManager(OnMessageReceivedListener listener) {
        this.messageListener = listener;
        okHttpClient = UnsafeHttps.getUnsafeOkHttpClient()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        setupWebSocketListener();
    }

    private void setupWebSocketListener() {
        webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket connection opened");
                sendInitialMessage();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleMessageReceived(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.d(TAG, "Received WebSocket bytes: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket connection closing: " + code + " / " + reason);
                if (reconnect) {
                    scheduleReconnect();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket connection failed: " + t.getMessage());
                if (reconnect) {
                    scheduleReconnect();
                }
            }
        };
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("roomId", roomId)
                .build();

        webSocket = okHttpClient.newWebSocket(request, webSocketListener);
    }

    private void sendInitialMessage() {
        try {
            JSONObject message = new JSONObject();
            message.put("sender", "System");
            message.put("message", "Connected to chat room");
            webSocket.send(message.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to send initial message", e);
        }
    }

    private void handleMessageReceived(String messageText) {
        try {
            JSONObject jsonMessage = new JSONObject(messageText);
            String sender = jsonMessage.getString("sender");
            String messageContent = jsonMessage.getString("message");
            Message message = new Message(sender, messageContent, null);
            if (messageListener != null) {
                messageListener.onMessageReceived(message);
            }
            Log.d(TAG, "Received message: " + messageContent);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse received message", e);
        }
    }

    private void scheduleReconnect() {
        handler.postDelayed(() -> connect(), RECONNECT_DELAY_MILLIS);
    }

    public void sendMessage(String sender, String messageText) {
        try {
            JSONObject message = new JSONObject();
            message.put("sender", sender);
            message.put("message", messageText);
            webSocket.send(message.toString());
            Log.d(TAG, "Message sent: " + messageText);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to send message", e);
        }
    }

    public void disconnect() {
        reconnect = false; // 연결 해제 시 재연결 방지
        webSocket.close(1000, null);
        Log.d(TAG, "WebSocket disconnected");
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(Message message);
    }
}
