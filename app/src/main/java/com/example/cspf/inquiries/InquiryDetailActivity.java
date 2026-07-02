package com.example.cspf.inquiries;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.core.content.ContextCompat;

import com.example.cspf.ApiService;
import com.example.cspf.R;
import com.example.cspf.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InquiryDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView titleTextView;
    private TextView contentTextView;
    private TextView createdAtTextView;
    private TextView authorCodeTextView;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private EditText commentInput;
    private Button commentSubmitButton;
    private View commentProgress;
    private ProgressBar progressBar;
    private View contentLayout;

    private int boardId;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquiry_detail);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupCommentButton();
        loadInquiryData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        titleTextView = findViewById(R.id.detail_title);
        contentTextView = findViewById(R.id.detail_content);
        createdAtTextView = findViewById(R.id.detail_created_at);
        authorCodeTextView = findViewById(R.id.detail_author_code);
        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        commentInput = findViewById(R.id.comment_input);
        commentSubmitButton = findViewById(R.id.comment_submit_button);
        commentProgress = findViewById(R.id.comment_progress);
        progressBar = findViewById(R.id.progressBar);
        contentLayout = findViewById(R.id.content_layout);

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boardId = getIntent().getIntExtra("boardId", -1);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        commentsRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                commentsRecyclerView.getContext(), layoutManager.getOrientation());
        dividerItemDecoration.setDrawable(
                ContextCompat.getDrawable(this, R.drawable.custom_divider));
        commentsRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void setupCommentButton() {
        commentSubmitButton.setOnClickListener(v -> {
            String content = commentInput.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "댓글을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }
            postComment(content);
        });
    }

    private void loadInquiryData() {
        if (boardId == -1) {
            Toast.makeText(this, "잘못된 접근입니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading(true);
        loadInquiryDetail();
        loadComments();
    }

    private void loadInquiryDetail() {
        ApiService service = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<Inquiry> call = service.getInquiryDetail(boardId);

        call.enqueue(new Callback<Inquiry>() {
            @Override
            public void onResponse(Call<Inquiry> call, Response<Inquiry> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    handleError("상세 정보를 불러올 수 없습니다");
                }
            }

            @Override
            public void onFailure(Call<Inquiry> call, Throwable t) {
                showLoading(false);
                handleError("네트워크 오류가 발생했습니다");
            }
        });
    }

    private void loadComments() {
        ApiService service = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Comment>> call = service.getComments(boardId);

        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commentAdapter = new CommentAdapter(InquiryDetailActivity.this, response.body());
                    commentsRecyclerView.setAdapter(commentAdapter);
                } else {
                    handleError("댓글을 불러올 수 없습니다");
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                handleError("댓글 로딩 중 오류가 발생했습니다");
            }
        });
    }

    private void postComment(String content) {
        String token = sharedPreferences.getString("access_token", null);
        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        showCommentProgress(true);
        Comment comment = new Comment(content);

        ApiService service = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = service.createComment(boardId, token, comment);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showCommentProgress(false);
                if (response.isSuccessful()) {
                    commentInput.setText("");
                    Toast.makeText(InquiryDetailActivity.this,
                            "댓글이 등록되었습니다", Toast.LENGTH_SHORT).show();
                    loadComments();
                } else {
                    handleError("댓글 등록에 실패했습니다");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showCommentProgress(false);
                handleError("네트워크 오류가 발생했습니다");
            }
        });
    }

    private void updateUI(Inquiry inquiry) {
        titleTextView.setText(inquiry.getTitle());
        contentTextView.setText(inquiry.getContent());
        authorCodeTextView.setText(String.format("작성자: %s", inquiry.getAuthorCode()));
        createdAtTextView.setText(String.format("작성일: %s", formatDate(inquiry.getCreatedAt())));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        contentLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showCommentProgress(boolean show) {
        commentProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        commentSubmitButton.setEnabled(!show);
        commentSubmitButton.setText(show ? "" : "작성");
    }

    private void handleError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }
}