package com.example.cspf.inquiries;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.ApiService;
import com.example.cspf.R;
import com.example.cspf.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InquiriesActivity extends AppCompatActivity {

    private InquiryAdapter inquiryAdapter;
    private List<Inquiry> inquiryList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquiries);

        // RecyclerView와 SearchView 초기화
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 어댑터 초기화 및 설정
        inquiryAdapter = new InquiryAdapter(this, inquiryList);
        recyclerView.setAdapter(inquiryAdapter);

        // 서버에서 문의사항 목록을 로드
        loadInquiries();

        // SearchView의 쿼리 리스너 설정
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    // 검색어에 따라 목록을 필터링하는 메서드
    private void filterList(String query) {
        List<Inquiry> filteredList = new ArrayList<>();
        for (Inquiry inquiry : inquiryList) {
            if (inquiry.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(inquiry);
            }
        }
        inquiryAdapter.setInquiries(filteredList);
        inquiryAdapter.notifyDataSetChanged(); // RecyclerView에 새 데이터를 반영
    }

    // 서버에서 문의사항 목록 데이터를 로드하는 메서드
    private void loadInquiries() {
        ApiService service = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<InquiryResponse> call = service.getInquiries();

        call.enqueue(new Callback<InquiryResponse>() {


            @Override
            public void onResponse(Call<InquiryResponse> call, Response<InquiryResponse> response) {
                Log.d("InquiresActivity", "ResponseBody Result, "+response.body());
                if (response.isSuccessful() && response.body() != null) {

//                    inquiryList = response.body();
                    Log.d("InquiryResult", "InquiryList Result, "+inquiryList);
//                    inquiryAdapter.setInquiries(inquiryList);
//                    inquiryAdapter.notifyDataSetChanged(); // RecyclerView에 새 데이터를 반영
                } else {
                    Toast.makeText(InquiriesActivity.this, "데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<InquiryResponse> call, Throwable t) {
                Toast.makeText(InquiriesActivity.this, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
