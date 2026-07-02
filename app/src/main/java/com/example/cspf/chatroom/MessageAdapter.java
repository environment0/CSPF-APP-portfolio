package com.example.cspf.chatroom;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cspf.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final String TAG = "MessageAdapter";
    private final List<Message> messages;
    private final Context context;
    private final String currentUserId;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
        Log.d(TAG, "MessageAdapter initialized with currentUserId: " + currentUserId);
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        resetLayouts(holder);

        boolean isSentByMe = message.getSender().equals(currentUserId);

        if (message.isImageMessage()) {
            displayImageMessage(holder, message, isSentByMe);
        } else if (message.isTextMessage()) {
            displayTextMessage(holder, message, isSentByMe);
        }
    }

    private void displayTextMessage(MessageViewHolder holder, Message message, boolean isSentByMe) {
        if (isSentByMe) {
            holder.sentMessageLayout.setVisibility(View.VISIBLE);
            holder.textViewSentMessage.setText(message.getMessage());
        } else {
            holder.receivedMessageLayout.setVisibility(View.VISIBLE);
            holder.textViewReceivedMessage.setText(message.getMessage());
            holder.textViewReceivedSender.setText(message.getSender());
        }
    }
    private void displayImageMessage(MessageViewHolder holder, Message message, boolean isSentByMe) {
        String messageSender = message.getSender();
        String imageUrl = message.getImageUrl();

        Log.d(TAG, "Processing image message:");
        Log.d(TAG, "  Sender ID: " + messageSender);
        Log.d(TAG, "  Current User ID: " + currentUserId);
        Log.d(TAG, "  Image URL: " + imageUrl);

        if (isSentByMe) {
            holder.sentImageLayout.setVisibility(View.VISIBLE);
            holder.receivedImageLayout.setVisibility(View.GONE);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(holder.sentImageView);
            Log.d(TAG, "  Displaying as sent image (right side)");
        } else {
            holder.sentImageLayout.setVisibility(View.GONE);
            holder.receivedImageLayout.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(holder.receivedImageView);
            holder.receivedImageSender.setText(messageSender != null ? messageSender : "Unknown");
            Log.d(TAG, "  Displaying as received image (left side)");
        }
    }
    private void resetLayouts(MessageViewHolder holder) {
        holder.sentMessageLayout.setVisibility(View.GONE);
        holder.receivedMessageLayout.setVisibility(View.GONE);
        holder.sentImageLayout.setVisibility(View.GONE);
        holder.receivedImageLayout.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        // 텍스트 메시지 레이아웃
        final LinearLayout sentMessageLayout;
        final LinearLayout receivedMessageLayout;

        // 이미지 메시지 레이아웃
        final LinearLayout sentImageLayout;
        final LinearLayout receivedImageLayout;

        // 텍스트 메시지 뷰
        final TextView textViewSentMessage;
        final TextView textViewReceivedMessage;
        final TextView textViewReceivedSender;

        // 이미지 메시지 뷰
        final ImageView sentImageView;
        final ImageView receivedImageView;
        final TextView receivedImageSender;

        MessageViewHolder(View itemView) {
            super(itemView);

            // 텍스트 메시지 레이아웃 초기화
            sentMessageLayout = itemView.findViewById(R.id.sentMessageLayout);
            receivedMessageLayout = itemView.findViewById(R.id.receivedMessageLayout);

            // 이미지 메시지 레이아웃 초기화
            sentImageLayout = itemView.findViewById(R.id.sentImageLayout);
            receivedImageLayout = itemView.findViewById(R.id.receivedImageLayout);

            // 텍스트 메시지 뷰 초기화
            textViewSentMessage = itemView.findViewById(R.id.textViewSentMessage);
            textViewReceivedMessage = itemView.findViewById(R.id.textViewReceivedMessage);
            textViewReceivedSender = itemView.findViewById(R.id.receivedSender);

            // 이미지 메시지 뷰 초기화
            sentImageView = itemView.findViewById(R.id.sentImageView);
            receivedImageView = itemView.findViewById(R.id.receivedImageView);
            receivedImageSender = itemView.findViewById(R.id.receivedImageSender);
        }
    }
}