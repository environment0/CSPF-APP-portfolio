package com.example.cspf.insurance;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cspf.R;

public class InsuranceFragment extends Fragment {

    public InsuranceFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_insurance, container, false);

        TextView textView = view.findViewById(R.id.textViewInsurance);
        textView.setText("반려동물 보험  상품.");

        ImageView samsungImage = view.findViewById(R.id.samsungImage);
        ImageView hyundaiImage = view.findViewById(R.id.hyundaiImage);
        ImageView kbImage = view.findViewById(R.id.kbImage);
        ImageView dbImage = view.findViewById(R.id.dbImage);

        samsungImage.setOnClickListener(v -> openInsuranceInfoActivity("삼성화재"));
        hyundaiImage.setOnClickListener(v -> openInsuranceInfoActivity("현대해상"));
        kbImage.setOnClickListener(v -> openInsuranceInfoActivity("KB보험"));
        dbImage.setOnClickListener(v -> openInsuranceInfoActivity("DB손해보험"));

        return view;
    }

    private void openInsuranceInfoActivity(String insuranceName) {
        Intent intent = new Intent(getActivity(), InsuranceInfoActivity.class);
        intent.putExtra("INSURANCE_NAME", insuranceName);
        startActivity(intent);
    }
}
