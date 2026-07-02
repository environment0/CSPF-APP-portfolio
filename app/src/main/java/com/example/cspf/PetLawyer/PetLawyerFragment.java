package com.example.cspf.PetLawyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.ApiService;
import com.example.cspf.R;
import com.example.cspf.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PetLawyerFragment extends Fragment {

    private RecyclerView recyclerViewLawyers;
    private PetLawyerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pet_lawyer_info, container, false);

        recyclerViewLawyers = view.findViewById(R.id.recyclerViewLawyers);
        recyclerViewLawyers.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchLawyerProfiles();

        return view;
    }

    private void fetchLawyerProfiles() {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Lawyer>> call = apiService.getLawyers("Lawyer");

        call.enqueue(new Callback<List<Lawyer>>() {
            @Override
            public void onResponse(Call<List<Lawyer>> call, Response<List<Lawyer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Lawyer> lawyers = response.body();
                    adapter = new PetLawyerAdapter(lawyers);

                    adapter.setOnItemClickListener(lawyer -> {
                        // LawyerDetailActivity로 데이터 전달
                        Intent intent = new Intent(getContext(), LawyerDetailActivity.class);
                        intent.putExtra("name", lawyer.getName());
                        intent.putExtra("phone", lawyer.getPhone());
                        intent.putExtra("email", lawyer.getEmail());
                        intent.putExtra("company", lawyer.getCompany());
                        startActivity(intent);
                    });

                    recyclerViewLawyers.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Lawyer>> call, Throwable t) {
                // 에러 처리
            }
        });
    }
}
