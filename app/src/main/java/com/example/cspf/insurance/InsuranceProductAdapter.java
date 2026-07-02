package com.example.cspf.insurance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.R;

import java.util.List;

import lombok.NonNull;

public class InsuranceProductAdapter extends RecyclerView.Adapter<InsuranceProductAdapter.ProductViewHolder> {

    private List<InsuranceProduct> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(InsuranceProduct product);
    }

    public InsuranceProductAdapter(List<InsuranceProduct> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_insurance_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView productImage;
        private TextView productName;
        private TextView productDescription;
        private TextView productPrice;
        private TextView productCoverage;
        private Button buttonDetail;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productDescription = itemView.findViewById(R.id.productDescription);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCoverage = itemView.findViewById(R.id.productCoverage);
            buttonDetail = itemView.findViewById(R.id.buttonDetail);
        }

        void bind(InsuranceProduct product) {
            productImage.setImageResource(product.getImageResId());
            productName.setText(product.getName());
            productDescription.setText(product.getDescription());
            productPrice.setText(product.getPrice());
            productCoverage.setText(product.getCoverage());

            buttonDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}