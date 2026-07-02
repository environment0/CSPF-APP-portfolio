package com.example.cspf.chatroom;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.WebSocket;

public class ChatRoomSocket {

    private static final String TAG = "ChatRoomSocket";
    private Socket socket;
    private OnMessageReceivedListener messageListener;
    private final String serverUrl = "https://hyproz.myds.me:3800";
    private IO.Options options;
    private final OkHttpClient client;
    private final SharedPreferences sharedPreferences;

    public ChatRoomSocket(OnMessageReceivedListener listener, OkHttpClient client, SharedPreferences sharedPreferences) {
        this.messageListener = listener;
        this.client = client;
        this.sharedPreferences = sharedPreferences;
    }

    public void connect(String roomId, String accessToken) {
        try {
            options = new IO.Options();
            options.forceNew = true;
            options.reconnection = true;
            options.reconnectionAttempts = Integer.MAX_VALUE;
            options.reconnectionDelay = 1000;
            options.timeout = 60000;
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Cookie", Arrays.asList(accessToken));
            options.extraHeaders = headers;

            options.query = "roomId=" + roomId;
            options.callFactory = (Call.Factory) client;
            options.webSocketFactory = (WebSocket.Factory) client;

            socket = IO.socket(serverUrl, options);
            socket.connect();

            socket.on(Socket.EVENT_CONNECT, args -> Log.d(TAG, "Connected to server"));
            socket.on(Socket.EVENT_DISCONNECT, args -> Log.d(TAG, "Disconnected from server"));
            socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                if (args.length > 0) {
                    Log.e(TAG, "Connection error: " + args[0]);
                }
            });

            socket.on("message", onMessageReceived);


        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket connection error", e);
        }
    }
    public void sendMessage(String sender, String messageText) {
        try {
            String userNickname = sharedPreferences.getString("nickname", sender);

            // 프로필 정보
            JSONObject profile = new JSONObject()
                    .put("userCode", sender)
                    .put("nickname", userNickname)
                    .put("profileImg", null);

            // 메시지 구성
            JSONObject message = new JSONObject()
                    .put("userType", "user")
                    .put("msgType", "text")
                    .put("profile", profile)
                    .put("msg", messageText)
                    .put("imgUrl", null)  // 텍스트 메시지는 imgUrl null
                    .put("petMsg", null)
                    .put("date", new Date().toGMTString());

            socket.emit("message", message);  // toString() 제거
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
        }
    }

    private final Emitter.Listener onMessageReceived = args -> {
        if (args.length > 0) {
            try {
                Log.d(TAG, "Raw message received: " + args[0].toString());
                JSONObject data = new JSONObject(args[0].toString());

                JSONObject profile = data.optJSONObject("profile");
                String sender = profile != null ?
                        profile.optString("userCode", "unknown") :
                        data.optString("userCode", "unknown");

                String msgType = data.optString("msgType", "text");
                String messageText = null;
                String imgUrl = null;

                if ("text".equals(msgType)) {
                    messageText = data.optString("msg", "");
                } else if ("img".equals(msgType)) {
                    imgUrl = data.optString("imgUrl", null);
                }

                Message message = new Message(sender, messageText, imgUrl);
                if (messageListener != null) {
                    messageListener.onMessageReceived(message);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing message: " + e.getMessage());
            }
        }
    };

    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.off("message", onMessageReceived);

            Log.d(TAG, "Socket disconnected");
        }
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(Message message);
    }
}