package com.example.cspf.Hospital;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.cspf.ApiService;
import com.example.cspf.ExpertRetrofitClient;
import com.example.cspf.R;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PartnerHospitalsFragment extends Fragment {
    private RecyclerView recyclerView;
    private HospitalAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_partner_hospitals, container, false);

        recyclerView = view.findViewById(R.id.recycler_partner_hospitals);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::refreshHospitals);

        fetchHospitals("Veterinarian");

        return view;
    }

    private void refreshHospitals() {
        fetchHospitals("Veterinarian");
    }

    private void fetchHospitals(String type) {
        ApiService apiService = ExpertRetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getHospitalsByType(type).enqueue(new Callback<List<Hospital>>() {
            @Override
            public void onResponse(@NonNull Call<List<Hospital>> call,
                                   @NonNull Response<List<Hospital>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    adapter = new HospitalAdapter(response.body(), getContext());
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "데이터를 불러올 수 없습니다.",
                            Toast.LENGTH_SHORT).show();
                }
            }   @Override
            public void onFailure(@NonNull Call<List<Hospital>> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "서버 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}