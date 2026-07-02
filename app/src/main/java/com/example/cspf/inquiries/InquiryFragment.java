package com.example.cspf.inquiries;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.facebook.shimmer.ShimmerFrameLayout;

import com.example.cspf.ApiService;
import com.example.cspf.R;
import com.example.cspf.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InquiryFragment extends Fragment {

    private RecyclerView recyclerView;
    private InquiryAdapter inquiryAdapter;
    private List<Inquiry> inquiryList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ShimmerFrameLayout shimmerFrameLayout;
    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inquiry, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadInquiries();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        shimmerFrameLayout = view.findViewById(R.id.shimmerLayout);
        emptyView = view.findViewById(R.id.emptyView);
        inquiryList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        inquiryAdapter = new InquiryAdapter(getActivity(), inquiryList);
        recyclerView.setAdapter(inquiryAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.black));
        swipeRefreshLayout.setOnRefreshListener(this::loadInquiries);
    }

    private void loadInquiries() {
        showLoading(true);

        ApiService service = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<InquiryResponse> call = service.getInquiries();

        call.enqueue(new Callback<InquiryResponse>() {
            @Override
            public void onResponse(Call<InquiryResponse> call, Response<InquiryResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    inquiryList.clear();
                    inquiryList.addAll(response.body().getData());
                    inquiryAdapter.notifyDataSetChanged();

                    updateEmptyView();
                } else {
                    showError("데이터를 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(Call<InquiryResponse> call, Throwable t) {
                showLoading(false);
                showError("네트워크 오류가 발생했습니다.");
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            shimmerFrameLayout.startShimmer();
        } else {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void updateEmptyView() {
        emptyView.setVisibility(inquiryList.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(inquiryList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.startShimmer();
        }
    }

    @Override
    public void onPause() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
        }
        super.onPause();
    }
}