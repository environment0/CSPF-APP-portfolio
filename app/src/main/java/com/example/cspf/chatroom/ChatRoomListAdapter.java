package com.example.cspf.chatroom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.R;

import java.util.List;

public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ChatRoomViewHolder> {
    private final List<ChatRoom> chatRoomList;
    private final OnChatRoomClickListener clickListener;

    public ChatRoomListAdapter(List<ChatRoom> chatRoomList, OnChatRoomClickListener clickListener) {
        this.chatRoomList = chatRoomList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ChatRoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomList.get(position);
        holder.roomName.setText(chatRoom.getChatRoom());
        holder.itemView.setOnClickListener(v -> clickListener.onChatRoomClick(chatRoom));
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder {
        TextView roomName;

        public ChatRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.roomName);
        }
    }
}
