package com.example.cspf.notices;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.cspf.R;

public class NoticeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_detail);

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 뒤로가기 버튼 활성화
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        // Intent로부터 데이터 가져오기
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String date = intent.getStringExtra("date");

        // 레이아웃에 데이터 설정
        TextView titleTextView = findViewById(R.id.detailTitleTextView);
        TextView contentTextView = findViewById(R.id.detailContentTextView);
        TextView dateTextView = findViewById(R.id.detailDateTextView);

        titleTextView.setText(title);
        contentTextView.setText(content);
        dateTextView.setText(date);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();  // 뒤로 가기 버튼 클릭 시 현재 Activity 종료
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
