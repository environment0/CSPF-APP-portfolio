package com.example.cspf.PetLawyer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.R;

import java.util.List;

public class PetLawyerAdapter extends RecyclerView.Adapter<PetLawyerAdapter.ViewHolder> {

    private final List<Lawyer> lawyers;
    private OnItemClickListener onItemClickListener;

    public PetLawyerAdapter(List<Lawyer> lawyers) {
        this.lawyers = lawyers;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lawyer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lawyer lawyer = lawyers.get(position);

        holder.textViewName.setText(lawyer.getName());
        holder.textViewCompany.setText(lawyer.getCompany());
        holder.textViewEmail.setText(lawyer.getEmail());
        holder.textViewPhone.setText(lawyer.getPhone());

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(lawyer);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lawyers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewCompany, textViewEmail, textViewPhone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewCompany = itemView.findViewById(R.id.textViewCompany);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewPhone = itemView.findViewById(R.id.textViewPhone);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Lawyer lawyer);
    }
}
