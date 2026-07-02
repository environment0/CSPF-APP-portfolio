package com.example.cspf.notices;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cspf.R; // R 파일의 위치에 따라 패키지명을 조정하세요
import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {

    private List<Notice> noticeList;
    private Context context;

    public NoticeAdapter(Context context, List<Notice> noticeList) {
        this.context = context;
        this.noticeList = noticeList;
    }

    @NonNull
    @Override
    public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticeViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        holder.titleTextView.setText(notice.getTitle());
        holder.contentTextView.setText(notice.getContent());
        holder.dateTextView.setText(notice.getCreatedAt());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NoticeDetailActivity.class);
            intent.putExtra("title", notice.getTitle());
            intent.putExtra("content", notice.getContent());
            intent.putExtra("date", notice.getCreatedAt());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    // 리스트 업데이트 메서드 추가
    public void updateList(List<Notice> newList) {
        noticeList = newList;
        notifyDataSetChanged();
    }

    public static class NoticeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, dateTextView;

        public NoticeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
