package com.example.cspf.cases;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cspf.ApiService;
import com.example.cspf.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CasesFragment extends Fragment {
    private TextInputEditText searchEditText;
    private CaseLawAdapter adapter;
    private final List<CaseLawResponse> caseLawList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private CircularProgressIndicator progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cases, container, false);

        searchEditText = view.findViewById(R.id.searchEditText);
        TextView searchButton = view.findViewById(R.id.searchButton);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CaseLawAdapter(caseLawList, new OnUrlClickListener() {
            @Override
            public void onUrlClick(String url) {
                if (getContext() != null && url != null && !url.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> searchCases());
        swipeRefresh.setOnRefreshListener(this::searchCases);

        return view;
    }

    private void searchCases() {
        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
            return;
        }

        showLoading(true);
        ApiService apiService = CaseRetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.getCaseLaw(query).enqueue(new Callback<String>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseString = response.body();
                        Log.d("CasesFragment", "Response: " + responseString);
                        parseResponse(responseString);
                    } catch (Exception e) {
                        Log.e("CasesFragment", "Exception during response handling: ", e);
                        Toast.makeText(getContext(), "데이터 처리 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("CasesFragment", "API call failed: ", t);
                Toast.makeText(getContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void parseResponse(String responseString) {
        try {
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray precArray = jsonObject.optJSONArray("prec");

            if (precArray == null || precArray.length() == 0) {
                Log.e("CasesFragment", "precArray is null or empty. Check JSON structure.");
                Toast.makeText(getContext(), "판례 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            caseLawList.clear();
            for (int i = 0; i < precArray.length(); i++) {
                JSONObject caseObject = precArray.getJSONObject(i);

                String title = caseObject.optString("사건명", "제목 없음");
                String link = "https://www.law.go.kr" + caseObject.optString("판례상세링크", "");
                String description = caseObject.optString("사건번호", "") + " " + caseObject.optString("법원명", "");
                String pubDate = caseObject.optString("선고일자", "");
                String types = caseObject.optString("판결유형", "");

                caseLawList.add(new CaseLawResponse(title, link, description, pubDate, types));
            }

            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e("CasesFragment", "JSONException during JSON parsing: ", e);
            Toast.makeText(getContext(), "JSON 형식 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("CasesFragment", "Unexpected exception during JSON parsing: ", e);
            Toast.makeText(getContext(), "데이터 처리 중 알 수 없는 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
        }
    }

    public interface OnUrlClickListener {
        void onUrlClick(String url);
    }

    private static class CaseLawAdapter extends RecyclerView.Adapter<CaseLawAdapter.ViewHolder> {
        private final List<CaseLawResponse> caseLawList;
        private final OnUrlClickListener urlClickListener;

        public CaseLawAdapter(List<CaseLawResponse> caseLawList, OnUrlClickListener listener) {
            this.caseLawList = caseLawList;
            this.urlClickListener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_case, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CaseLawResponse caseLaw = caseLawList.get(position);
            holder.titleTextView.setText(caseLaw.getTitle());
            holder.descriptionTextView.setText(caseLaw.getDescription());
            holder.pubDateTextView.setText(caseLaw.getPubDate());
            holder.typesTextView.setText(caseLaw.getTypes());

            holder.itemView.setOnClickListener(v ->
                    urlClickListener.onUrlClick(caseLaw.getLink())
            );
        }

        @Override
        public int getItemCount() {
            return caseLawList.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleTextView, descriptionTextView, pubDateTextView, typesTextView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
                pubDateTextView = itemView.findViewById(R.id.pubDateTextView);
                typesTextView = itemView.findViewById(R.id.typesTextView);
            }
        }
    }
}