package com.example.cspf.inquiries;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.R;

import java.util.List;

public class InquiryAdapter extends RecyclerView.Adapter<InquiryAdapter.InquiryViewHolder> {
    private List<Inquiry> inquiryList;
    private Context context;

    public InquiryAdapter(Context context, List<Inquiry> inquiryList) {
        this.context = context;
        this.inquiryList = inquiryList;
    }

    @NonNull
    @Override
    public InquiryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inquiry, parent, false);
        return new InquiryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InquiryViewHolder holder, int position) {
        Inquiry inquiry = inquiryList.get(position);

        // 제목 설정
        holder.titleView.setText(inquiry.getTitle());

        // 내용 설정
        if (inquiry.getContent() != null && !inquiry.getContent().isEmpty()) {
            holder.contentView.setText(inquiry.getContent());
            holder.contentView.setVisibility(View.VISIBLE);
        } else {
            holder.contentView.setVisibility(View.GONE);
        }

        // 작성자 설정
        if (inquiry.getAuthorCode() != null) {
            holder.authorView.setText(inquiry.getAuthorCode());
        }

        // 날짜 설정
        if (inquiry.getCreatedAt() != null) {
            holder.dateView.setText(inquiry.getCreatedAt());
        }

        // 클릭 효과 및 이벤트 처리
        holder.itemView.setBackgroundResource(R.drawable.custom_ripple_background);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, InquiryDetailActivity.class);
            intent.putExtra("boardId", inquiry.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return inquiryList.size();
    }

    public void setInquiries(List<Inquiry> inquiries) {
        this.inquiryList = inquiries;
        notifyDataSetChanged();
    }

    static class InquiryViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView contentView;
        TextView authorView;
        TextView dateView;

        InquiryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.inquiry_title);
            contentView = itemView.findViewById(R.id.inquiry_content);
            authorView = itemView.findViewById(R.id.inquiry_author);
            dateView = itemView.findViewById(R.id.inquiry_date);
        }
    }
}