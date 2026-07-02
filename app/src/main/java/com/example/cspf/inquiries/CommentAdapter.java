package com.example.cspf.inquiries;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.ApiService;
import com.example.cspf.R;
import com.example.cspf.RetrofitClient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private final Context context;
    private final List<Comment> comments;
    private final SimpleDateFormat inputFormat;
    private final SimpleDateFormat outputFormat;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        this.outputFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.authorTextView.setText(comment.getAuthorCode());
        holder.contentTextView.setText(comment.getContent());
        holder.dateTextView.setText(formatDate(comment.getCreatedAt()));

        boolean isAuthor = checkIsAuthor(comment.getAuthorCode());
        holder.editTextView.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
        holder.deleteTextView.setVisibility(isAuthor ? View.VISIBLE : View.GONE);

        holder.editTextView.setOnClickListener(v -> showEditDialog(comment, position));
        holder.deleteTextView.setOnClickListener(v -> showDeleteDialog(comment, position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    private String formatDate(String dateStr) {
        try {
            Date date = inputFormat.parse(dateStr);
            return date != null ? outputFormat.format(date) : dateStr;
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private boolean checkIsAuthor(String authorCode) {
        SharedPreferences prefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String currentUserCode = prefs.getString("user_code", "");
        return currentUserCode.equals(authorCode);
    }

    private void showEditDialog(Comment comment, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_comment, null);
        EditText editText = dialogView.findViewById(R.id.edit_comment);
        editText.setText(comment.getContent());

        new MaterialAlertDialogBuilder(context)
                .setTitle("댓글 수정")
                .setView(dialogView)
                .setPositiveButton("수정", (dialog, which) -> {
                    String newContent = editText.getText().toString().trim();
                    if (!newContent.isEmpty()) {
                        updateComment(comment.getId(), newContent, position);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showDeleteDialog(Comment comment, int position) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("댓글 삭제")
                .setMessage("이 댓글을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteComment(comment.getId(), position))
                .setNegativeButton("취소", null)
                .show();
    }

    private void updateComment(int commentId, String content, int position) {
        SharedPreferences prefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        if (token == null) {
            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService service = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Comment updatedComment = new Comment(content);
        Call<Void> call = service.updateComment(commentId, token, updatedComment);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    comments.get(position).setContent(content);
                    notifyItemChanged(position);
                    Toast.makeText(context, "댓글이 수정되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "댓글 수정에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "네트워크 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteComment(int commentId, int position) {
        SharedPreferences prefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        if (token == null) {
            Toast.makeText(context, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService service = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<Void> call = service.deleteComment(commentId, token);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    comments.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "댓글이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "댓글 삭제에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "네트워크 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView authorTextView;
        TextView contentTextView;
        TextView dateTextView;
        TextView editTextView;
        TextView deleteTextView;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.comment_author);
            contentTextView = itemView.findViewById(R.id.comment_content);
            dateTextView = itemView.findViewById(R.id.comment_date);
            editTextView = itemView.findViewById(R.id.comment_edit);
            deleteTextView = itemView.findViewById(R.id.comment_delete);
        }
    }
}