package com.example.cspf.notices;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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

public class NotificationsFragment extends Fragment {

    private RecyclerView noticeRecyclerView;
    private NoticeAdapter noticeAdapter;
    private List<Notice> noticeList = new ArrayList<>();
    private List<Notice> filteredList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enable toolbar menu
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        noticeRecyclerView = view.findViewById(R.id.noticeRecyclerView);
        noticeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with empty list and set to RecyclerView
        noticeAdapter = new NoticeAdapter(getContext(), filteredList);
        noticeRecyclerView.setAdapter(noticeAdapter);

        // Load notices from server
        loadNotices();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNotices(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotices(newText);
                return true;
            }
        });
    }

    // Filter notices based on query text
    private void filterNotices(String query) {
        filteredList.clear();
        for (Notice notice : noticeList) {
            if (notice.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(notice);
            }
        }
        noticeAdapter.updateList(filteredList); // Update adapter with filtered list
    }

    // Load notices from the server
    private void loadNotices() {
        ApiService service = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        int page = 1;
        int limit = 10;

        service.getNotices(page, limit).enqueue(new Callback<NoticeResponse>() {
            @Override
            public void onResponse(Call<NoticeResponse> call, Response<NoticeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    noticeList.clear();
                    noticeList.addAll(response.body().getData());
                    filteredList.clear();
                    filteredList.addAll(noticeList);
                    noticeAdapter.updateList(filteredList); // Update adapter with loaded data
                } else {
                    showToast("Failed to retrieve notices");
                    Log.e("NotificationsFragment", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<NoticeResponse> call, Throwable t) {
                showToast("Error: " + t.getMessage());
                Log.e("NotificationsFragment", "Error: " + t.getMessage(), t);
            }
        });
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
