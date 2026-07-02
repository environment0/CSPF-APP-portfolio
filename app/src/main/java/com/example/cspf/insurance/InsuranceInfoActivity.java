package com.example.cspf.insurance;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cspf.R;

import java.util.ArrayList;
import java.util.List;


public class InsuranceInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insurance_info);

        // 선택된 보험사 이름 가져오기
        String insuranceName = getIntent().getStringExtra("INSURANCE_NAME");

        // 타이틀 설정
        TextView titleText = findViewById(R.id.titleText);
        titleText.setText(insuranceName + " 반려동물보험");

        // 뒤로가기 버튼 설정
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        // RecyclerView 설정
        RecyclerView recyclerView = findViewById(R.id.recyclerProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 보험사별 상품 데이터 로드 및 어댑터 설정
        List<InsuranceProduct> products = getProductsForInsurance(insuranceName);
        InsuranceProductAdapter adapter = new InsuranceProductAdapter(products,
                product -> showProductDetail(product));
        recyclerView.setAdapter(adapter);
    }

    private List<InsuranceProduct> getProductsForInsurance(String insuranceName) {
        List<InsuranceProduct> products = new ArrayList<>();

        switch (insuranceName) {
            case "삼성화재":
                products.add(new InsuranceProduct(
                        "I00000001",
                        "애니펫 라이프",
                        "반려동물 평생 보장, 노령견도 가입 가능",
                        "월 39,800원부터",
                        "수술비, 입원비, 슬관절 수술비 보장",
                        R.drawable.samsung_hwa_jae
                ));
                products.add(new InsuranceProduct(
                        "I00000001",
                        "애니펫 실버",
                        "8세 이상 노령견을 위한 맞춤형 상품",
                        "월 29,800원부터",
                        "노령견 다빈도 질병 보장 강화",
                        R.drawable.samsung_hwa_jae
                ));
                products.add(new InsuranceProduct(
                        "I00000001",
                        "애니펫 건강검진",
                        "반려동물 종합 건강검진 무료 제공",
                        "월 24,800원부터",
                        "매년 건강검진과 함께 기본 보장 제공",
                        R.drawable.samsung_hwa_jae
                ));
                break;

            case "현대해상":
                products.add(new InsuranceProduct(
                        "I00000002",
                        "하이펫 100세",
                        "반려동물 생애 전체를 보장하는 평생보험",
                        "월 45,000원부터",
                        "슬관절 수술, 중성화 수술비 추가 보장",
                        R.drawable.hyundai_haesang
                ));
                products.add(new InsuranceProduct(
                        "I00000002",
                        "하이펫 실버케어",
                        "7세 이상 노령견을 위한 전용 상품",
                        "월 35,000원부터",
                        "노령성 질환 보장 강화 및 건강검진 할인",
                        R.drawable.hyundai_haesang
                ));
                products.add(new InsuranceProduct(
                        "I00000002",
                        "하이펫 수술비",
                        "수술비 걱정 없는 든든한 수술비 보험",
                        "월 25,000원부터",
                        "일반 수술, 중성화 수술, 슬관절 수술 등 보장",
                        R.drawable.hyundai_haesang
                ));
                break;

            case "KB보험":
                products.add(new InsuranceProduct(
                        "I00000003",
                        "KB 골든라이프 펫보험",
                        "노령견도 가입 가능한 실버케어 플랜 제공",
                        "월 33,000원부터",
                        "반려동물 생애주기별 맞춤 보장",
                        R.drawable.kb
                ));
                products.add(new InsuranceProduct(
                        "I00000003",
                        "KB 펫코노미",
                        "합리적인 보험료로 실속있게 보장",
                        "월 27,000원부터",
                        "기본 보장에 치과치료 보장 추가 가능",
                        R.drawable.kb
                ));
                products.add(new InsuranceProduct(
                        "I00000003",
                        "KB 펫드림",
                        "꿈꾸던 펫보험, 원하는 보장 맞춤 설계",
                        "월 37,000원부터",
                        "기본형, 실속형, 고급형 중 선택 가능",
                        R.drawable.kb
                ));
                break;

            case "DB손해보험":
                products.add(new InsuranceProduct(
                        "I00000004",
                        "프로미 펫사랑 평생보험",
                        "반려동물 생애 전체를 보장하는 평생보험",
                        "월 41,000원부터",
                        "각종 수술비, 입원비, 치료비 보장",
                        R.drawable.db_sonhae_bohum
                ));
                products.add(new InsuranceProduct(
                        "I00000004",
                        "프로미 펫사랑 골드플랜",
                        "필요한 보장을 한 곳에, 골드플랜",
                        "월 31,000원부터",
                        "다빈도 질병 집중 보장 및 건강검진 제공",
                        R.drawable.db_sonhae_bohum
                ));
                products.add(new InsuranceProduct(
                        "I00000004",
                        "프로미 펫사랑 실속플랜",
                        "부담 없는 보험료로 반려동물 보장",
                        "월 26,000원부터",
                        "기본 질병, 상해 및 배상책임 보장 제공",
                        R.drawable.db_sonhae_bohum
                ));
                break;
        }

        return products;
    }

    private void showProductDetail(InsuranceProduct product) {
        // 상품 상세 페이지로 이동
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_NAME", product.getName());
        // 필요한 다른 데이터도 전달
        startActivity(intent);
    }
}