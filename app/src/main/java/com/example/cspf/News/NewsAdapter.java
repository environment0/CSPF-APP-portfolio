package com.example.cspf.News;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.R;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private final List<News> newsList;
    private final Context context;
    private final ArticleClickListener articleClickListener;

    public interface ArticleClickListener {
        void onArticleClick(String url);
    }

    public NewsAdapter(List<News> newsList, Context context, ArticleClickListener listener) {
        this.newsList = newsList;
        this.context = context;
        this.articleClickListener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.title.setText(news.getTitle());
        holder.content.setText(news.getContent());

        // 더보기 버튼 클릭 이벤트
        holder.readMore.setOnClickListener(v -> articleClickListener.onArticleClick(news.getUrl()));
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView title, content, timestamp, readMore;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            timestamp = itemView.findViewById(R.id.timestamp);
            readMore = itemView.findViewById(R.id.read_more);
        }
    }
}