package com.example.cspf.insurance;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;

import com.example.cspf.ApiService;
import com.example.cspf.R;
import com.example.cspf.RetrofitClient;
import com.example.cspf.UnsafeHttps;
import com.example.cspf.utilss.NotificationChannelManager;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private static final Map<String, InsuranceProduct> PRODUCTS = new HashMap<>();
    private static final int NOTIFICATION_ID = 1001;

    private EventSource eventSource;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextView consultationStatusView;
    private ProgressBar statusProgressBar;
    private CardView consultationStatusCard;
    private Button buttonSubscribe;

    static {
        // 삼성화재 상품
        PRODUCTS.put("애니펫 라이프", new InsuranceProduct(
                "I00000001",
                "애니펫 라이프",
                "반려동물 평생 보장, 노령견도 가입 가능",
                "월 39,800원부터",
                "수술비, 입원비, 슬관절 수술비 보장",
                R.drawable.samsung_hwa_jae
        ));
        PRODUCTS.put("애니펫 실버", new InsuranceProduct(
                "I00000001",
                "애니펫 실버",
                "8세 이상 노령견을 위한 맞춤형 상품",
                "월 29,800원부터",
                "노령견 다빈도 질병 보장 강화",
                R.drawable.samsung_hwa_jae
        ));
        PRODUCTS.put("애니펫 건강검진", new InsuranceProduct(
                "I00000001",
                "애니펫 건강검진",
                "반려동물 종합 건강검진 무료 제공",
                "월 24,800원부터",
                "매년 건강검진과 함께 기본 보장 제공",
                R.drawable.samsung_hwa_jae
        ));

        // 현대해상 상품
        PRODUCTS.put("하이펫 100세", new InsuranceProduct(
                "I00000002",
                "하이펫 100세",
                "반려동물 생애 전체를 보장하는 평생보험",
                "월 45,000원부터",
                "슬관절 수술, 중성화 수술비 추가 보장",
                R.drawable.hyundai_haesang
        ));
        PRODUCTS.put("하이펫 실버케어", new InsuranceProduct(
                "I00000002",
                "하이펫 실버케어",
                "7세 이상 노령견을 위한 전용 상품",
                "월 35,000원부터",
                "노령성 질환 보장 강화 및 건강검진 할인",
                R.drawable.hyundai_haesang
        ));
        PRODUCTS.put("하이펫 수술비", new InsuranceProduct(
                "I00000002",
                "하이펫 수술비",
                "수술비 걱정 없는 든든한 수술비 보험",
                "월 25,000원부터",
                "일반 수술, 중성화 수술, 슬관절 수술 등 보장",
                R.drawable.hyundai_haesang
        ));

        // KB손해보험 상품
        PRODUCTS.put("KB 골든라이프 펫보험", new InsuranceProduct(
                "I00000003",
                "KB 골든라이프 펫보험",
                "노령견도 가입 가능한 실버케어 플랜 제공",
                "월 33,000원부터",
                "반려동물 생애주기별 맞춤 보장",
                R.drawable.kb
        ));
        PRODUCTS.put("KB 펫코노미", new InsuranceProduct(
                "I00000003",
                "KB 펫코노미",
                "합리적인 보험료로 실속있게 보장",
                "월 27,000원부터",
                "기본 보장에 치과치료 보장 추가 가능",
                R.drawable.kb
        ));
        PRODUCTS.put("KB 펫드림", new InsuranceProduct(
                "I00000003",
                "KB 펫드림",
                "꿈꾸던 펫보험, 원하는 보장 맞춤 설계",
                "월 37,000원부터",
                "기본형, 실속형, 고급형 중 선택 가능",
                R.drawable.kb
        ));

        // DB손해보험 상품
        PRODUCTS.put("프로미 펫사랑 평생보험", new InsuranceProduct(
                "I00000004",
                "프로미 펫사랑 평생보험",
                "반려동물 생애 전체를 보장하는 평생보험",
                "월 41,000원부터",
                "각종 수술비, 입원비, 치료비 보장",
                R.drawable.db_sonhae_bohum
        ));
        PRODUCTS.put("프로미 펫사랑 골드플랜", new InsuranceProduct(
                "I00000004",
                "프로미 펫사랑 골드플랜",
                "필요한 보장을 한 곳에, 골드플랜",
                "월 31,000원부터",
                "다빈도 질병 집중 보장 및 건강검진 제공",
                R.drawable.db_sonhae_bohum
        ));
        PRODUCTS.put("프로미 펫사랑 실속플랜", new InsuranceProduct(
                "I00000004",
                "프로미 펫사랑 실속플랜",
                "부담 없는 보험료로 반려동물 보장",
                "월 26,000원부터",
                "기본 질병, 상해 및 배상책임 보장 제공",
                R.drawable.db_sonhae_bohum
        ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initializeViews();

        String productName = getIntent().getStringExtra("PRODUCT_NAME");
        InsuranceProduct product = getProductDetails(productName);

        if (product != null) {
            setupProductDetails(product);
            buttonSubscribe.setOnClickListener(v -> startSubscription(product));
        } else {
            Toast.makeText(this, "상품 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish());

        consultationStatusView = findViewById(R.id.consultationStatus);
        statusProgressBar = findViewById(R.id.statusProgressBar);
        consultationStatusCard = findViewById(R.id.consultationStatusCard);
        buttonSubscribe = findViewById(R.id.buttonSubscribe);

        consultationStatusCard.setVisibility(View.GONE);
        statusProgressBar.setVisibility(View.GONE);
    }

    private void setupProductDetails(InsuranceProduct product) {
        TextView titleText = findViewById(R.id.titleText);
        TextView productNameView = findViewById(R.id.productName);
        TextView productDescriptionView = findViewById(R.id.productDescription);
        TextView productPriceView = findViewById(R.id.productPrice);
        TextView productCoverageView = findViewById(R.id.productCoverage);
        ImageView productImageView = findViewById(R.id.productImage);

        titleText.setText(product.getName());
        productNameView.setText(product.getName());
        productDescriptionView.setText(product.getDescription());
        productPriceView.setText(product.getPrice());
        productCoverageView.setText(product.getCoverage());
        productImageView.setImageResource(product.getImageResId());
    }

    private void startSubscription(InsuranceProduct product) {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("access_token", null);

        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        statusProgressBar.setVisibility(View.VISIBLE);
        buttonSubscribe.setEnabled(false);

        InsuranceConsultationRequest request = new InsuranceConsultationRequest(
                product.getInsurerId(),
                "410160010630474"  // 예시 반려동물 등록번호
        );

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.requestConsultation(token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, "상담 신청이 완료되었습니다", Toast.LENGTH_SHORT).show();
                    startConsultationStatusMonitoring(token, product.getInsurerId());
                } else {
                    Toast.makeText(ProductDetailActivity.this, "상담 신청 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "상담 신청 실패: " + response.code());
                    buttonSubscribe.setEnabled(true);
                }
                statusProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "서버 오류 발생", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "상담 신청 실패", t);
                statusProgressBar.setVisibility(View.GONE);
                buttonSubscribe.setEnabled(true);
            }
        });
    }

    private void startConsultationStatusMonitoring(String token, String insurerId) {
        if (eventSource != null) {
            eventSource.cancel();
        }

        String sseUrl = "https://hyproz.myds.me:3800/sse/"+insurerId;

        OkHttpClient client = UnsafeHttps.getUnsafeOkHttpClient()
                .connectTimeout(0, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder()
                .url(sseUrl)
                .addHeader("Accept", "text/event-stream")
                .header("Cookie", token)
                .build();

        EventSourceListener eventSourceListener = new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                Log.d(TAG, "SSE Connection opened");
                mainHandler.post(() -> {
                    consultationStatusCard.setVisibility(View.VISIBLE);
                    consultationStatusView.setText("상담 진행 상태 모니터링 시작");
                });
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                Log.d(TAG, "Received SSE event - Type: " + type + ", Data: " + data);
                mainHandler.post(() -> handleSseEvent(type, data));
            }

            @Override
            public void onClosed(EventSource eventSource) {
                Log.d(TAG, "SSE Connection closed");
                mainHandler.post(() -> {
                    consultationStatusCard.setVisibility(View.GONE);
                    buttonSubscribe.setEnabled(true);
                });
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                Log.e(TAG, "SSE Connection failed", t);
                mainHandler.post(() -> {
//                    Toast.makeText(ProductDetailActivity.this,
//                            "상태 모니터링 연결 실패", Toast.LENGTH_SHORT).show();
                    consultationStatusCard.setVisibility(View.GONE);
                    buttonSubscribe.setEnabled(true);
                });
            }
        };

        eventSource = EventSources.createFactory(client)
                .newEventSource(request, eventSourceListener);
    }

    private void handleSseEvent(String type, String data) {
        try {
            switch (type) {
                case "chat_created":
                    consultationStatusView.setText("채팅방이 생성되었습니다");
                    showNotification("채팅방이 생성되었습니다", "상담사와 채팅이 시작되었습니다");
                    break;
                case "vetResv_success":
                    consultationStatusView.setText("예약이 완료되었습니다");
                    showNotification("예약이 완료되었습니다", "동물병원 예약이 확정되었습니다");
                    break;
                default:
                    consultationStatusView.setText(data);
                    break;
            }

            if (data.equals("상담이 완료되었습니다")) {
                if (eventSource != null) {
                    eventSource.cancel();
                }
                buttonSubscribe.setEnabled(true);
                showNotification("상담 완료", "보험 상담이 완료되었습니다");
            }
        } catch (Exception e) {
            Log.e(TAG, "상태 업데이트 실패", e);
            Toast.makeText(this, "상태 업데이트 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotification(String title, String message) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannelManager.CONSULTATION_PROGRESS_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventSource != null) {
            eventSource.cancel();
        }
    }

    private InsuranceProduct getProductDetails(String productName) {
        return PRODUCTS.get(productName);
    }
}