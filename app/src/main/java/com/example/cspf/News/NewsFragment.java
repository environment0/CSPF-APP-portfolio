package com.example.cspf.News;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cspf.ApiService;
import com.example.cspf.R;
import com.example.cspf.RetrofitNews;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private CircularProgressIndicator progressBar;
    private EditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news, container, false);

        // 뷰 초기화
        recyclerView = rootView.findViewById(R.id.recycler_view);
        swipeRefresh = rootView.findViewById(R.id.swipe_refresh);
        progressBar = rootView.findViewById(R.id.progress_bar);
        searchEditText = rootView.findViewById(R.id.search_edit_text);

        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // SwipeRefreshLayout 설정
        swipeRefresh.setOnRefreshListener(this::fetchNews);

        // 검색 기능 구현
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    fetchNews(s.toString());
                } else {
                    fetchNews();
                }
            }
        });

        fetchNews();  // 초기 뉴스 리스트 불러오기
        return rootView;
    }

    private void fetchNews() {
        fetchNews("반려동물");  // 기본 검색어
    }

    private void fetchNews(String keyword) {
        showLoading(true);
        ApiService apiService = RetrofitNews.getApiService();
        Call<String> call = apiService.getNews(keyword);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body());
                        JSONArray items = jsonObject.getJSONArray("items");

                        List<News> newsList = parseNewsData(items);
                        newsAdapter = new NewsAdapter(newsList, requireContext(), NewsFragment.this::fetchArticleContent);
                        recyclerView.setAdapter(newsAdapter);
                    } catch (JSONException e) {
                        showErrorToast("JSON 파싱 오류: " + e.getMessage());
                    }
                } else {
                    showErrorToast("서버 응답 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showLoading(false);
                showErrorToast("네트워크 오류: " + t.getMessage());
            }
        });
    }

    private List<News> parseNewsData(JSONArray jsonArray) {
        List<News> newsList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject newsItem = jsonArray.getJSONObject(i);
                News news = new News();
                news.setTitle(removeHtmlTags(newsItem.optString("title", "제목 없음")));
                news.setContent(removeHtmlTags(newsItem.optString("description", "내용 없음")));
                news.setUrl(newsItem.optString("link"));
                newsList.add(news);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newsList;
    }

    private String removeHtmlTags(String input) {
        if (input != null) {
            return input
                    .replaceAll("<[^>]*>", "")
                    .replace("\n", "")
                    .replace("&amp;", "&")
                    .replace("&quot;", "\"")
                    .replace("&#39;", "'");
        }
        return input;
    }

    private void fetchArticleContent(String url) {
        showLoading(true);
        ApiService apiService = RetrofitNews.getApiService();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("url", url);

        apiService.getArticleContent(requestBody).enqueue(new Callback<List<NewsResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<NewsResponse>> call, @NonNull Response<List<NewsResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsResponse> articleContent = response.body();

                    if (!articleContent.isEmpty()) {
                        StringBuilder content = new StringBuilder();

                        // 첫 번째 기사의 기자 정보만 추가
                        NewsResponse firstItem = articleContent.get(0);
                        if (firstItem.getNewsAuthor() != null && !firstItem.getNewsAuthor().trim().isEmpty()) {
                            content.append("기자: ").append(firstItem.getNewsAuthor()).append("\n\n");
                        }

                        // 모든 사건 내용 추가 (빈 내용 필터링)
                        for (NewsResponse item : articleContent) {
                            if (item.getNewsContents() != null && !item.getNewsContents().trim().isEmpty()) {
                                content.append(item.getNewsContents()).append("\n\n");
                            }
                        }

                        showArticleContent(content.toString().trim());
                    } else {
                        showErrorToast("기사 본문이 없습니다.");
                    }
                } else {
                    showErrorToast("기사 본문을 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NewsResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                showErrorToast("네트워크 오류: " + t.getMessage());
            }
        });
    }

    private void showArticleContent(String content) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("기사 본문")
                .setMessage(content)
                .setPositiveButton("닫기", null)
                .show();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        }
    }

    private void showErrorToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}