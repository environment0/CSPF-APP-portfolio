package com.example.cspf.inquiries;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class InquiriesFragment extends Fragment {

    private InquiryAdapter inquiryAdapter;
    private List<Inquiry> inquiryList = new ArrayList<>();
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inquiries, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 어댑터 초기화 및 설정
        inquiryAdapter = new InquiryAdapter(getContext(), inquiryList);
        recyclerView.setAdapter(inquiryAdapter);

        // 서버에서 문의사항 목록을 로드
        loadInquiries();
    }

    // 서버에서 문의사항 목록 데이터를 로드하는 메서드
    private void loadInquiries() {
        ApiService service = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<InquiryResponse> call = service.getInquiries();

        call.enqueue(new Callback<InquiryResponse>() {
            @Override
            public void onResponse(Call<InquiryResponse> call, Response<InquiryResponse> response) {
                Log.d("InquiriesFragments", "ResponseBody Result, "+response.body());
                if (response.isSuccessful() && response.body() != null) {
//                    inquiryList = response.body();
//                    inquiryAdapter.setInquiries(inquiryList);
                } else {
                    Toast.makeText(getContext(), "데이터를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InquiryResponse> call, Throwable t) {
                Toast.makeText(getContext(), "데이터 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
