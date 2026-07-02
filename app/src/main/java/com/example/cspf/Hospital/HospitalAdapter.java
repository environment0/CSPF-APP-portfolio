package com.example.cspf.Hospital;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.example.cspf.R;
import com.example.cspf.chatbot.ChatActivity;

import com.bumptech.glide.Glide;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {
    private final List<Hospital> hospitalList;
    private final Context context;

    public HospitalAdapter(List<Hospital> hospitalList, Context context) {
        this.hospitalList = hospitalList;
        this.context = context;
    }

    @NonNull
    @Override
    public HospitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hospital_item, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HospitalViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);
        holder.name.setText(hospital.getName());
        holder.company.setText(hospital.getCompany());
        holder.email.setText(hospital.getEmail());
        holder.phone.setText(hospital.getPhone());

        holder.chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("hospitalName", hospital.getName());
            intent.putExtra("hospitalId", hospital.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

    static class HospitalViewHolder extends RecyclerView.ViewHolder {
        TextView name, company, email, phone;
        MaterialButton chatButton;

        public HospitalViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_hospital_name);
            company = itemView.findViewById(R.id.text_hospital_company);
            email = itemView.findViewById(R.id.text_hospital_email);
            phone = itemView.findViewById(R.id.text_hospital_phone);
            chatButton = itemView.findViewById(R.id.btn_chat);
        }
    }
}
