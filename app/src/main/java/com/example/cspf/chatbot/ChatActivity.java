package com.example.cspf.chatbot;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cspf.ApiService;
import com.example.cspf.PetLawyer.Lawyer;
import com.example.cspf.R;
import com.example.cspf.UnsafeHttps;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {
    private LinearLayout chatContainer;
    private ScrollView chatScrollView;
    private EditText messageInput;
    private Button sendButton;
    private Button lawyerBookingButton;
    private Button veterinarianBookingButton;

    private String selectedDate = "";
    private String selectedTime = "";
    private String description = "";
    private String expertType = "";

    private ApiService apiService;
    private ChatGPTService chatGPTService;
    private List<Lawyer> experts = new ArrayList<>();
    private Lawyer selectedExpert;
    private SharedPreferences sharedPreferences;

    private enum BookingState {
        NONE,
        SELECTING_EXPERT,
        SELECTING_DATE,
        SELECTING_TIME,
        ENTERING_DESCRIPTION,
        CONFIRMING
    }

    private BookingState currentBookingState = BookingState.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initializeViews();
        initializeServices();
        setupListeners();
    }

    private void initializeViews() {
        chatContainer = findViewById(R.id.chatContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        lawyerBookingButton = findViewById(R.id.lawyerBookingButton);
        veterinarianBookingButton = findViewById(R.id.btnInsurance);
    }

    private void initializeServices() {
        OkHttpClient client = UnsafeHttps.getUnsafeOkHttpClient()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Cookie", getJwtToken())
                            .header("Content-Type", "application/json");
                    return chain.proceed(requestBuilder.build());
                })
                .build();

        apiService = new Retrofit.Builder()
                .baseUrl("https://hyproz.myds.me:3800/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService.class);

        chatGPTService = new Retrofit.Builder()
                .baseUrl("http://api.openai.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ChatGPTService.class);
    }

    private void setupListeners() {
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {
                handleMessageSend();
                return true;
            }
            return false;
        });

        sendButton.setOnClickListener(v -> handleMessageSend());
        lawyerBookingButton.setOnClickListener(v -> startBookingProcess("Lawyer"));
        veterinarianBookingButton.setOnClickListener(v -> startBookingProcess("Veterinarian"));
    }

    private void handleMessageSend() {
        String userInput = messageInput.getText().toString().trim();
        if (!userInput.isEmpty()) {
            if (currentBookingState != BookingState.NONE) {
                processBookingInput(userInput);
            } else {
                addMessageToChat("user", userInput);
                callChatGPTAPI(userInput);
            }
            messageInput.setText("");
        }
    }

    private void startBookingProcess(String type) {
        if (currentBookingState != BookingState.NONE) {
            Toast.makeText(this, "이미 예약이 진행 중입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        expertType = type;
        String expertTitle = type.equals("Lawyer") ? "법률 상담" : "수의사 상담";
        addMessageToChat("bot", expertTitle + " 예약을 시작합니다.");
        currentBookingState = BookingState.SELECTING_EXPERT;
        fetchExperts(type);
    }

    private void fetchExperts(String type) {
        apiService.getLawyers(type).enqueue(new Callback<List<Lawyer>>() {
            @Override
            public void onResponse(Call<List<Lawyer>> call, Response<List<Lawyer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    experts.clear();
                    experts.addAll(response.body());
                    showExpertList();
                } else {
                    String expertTitle = type.equals("Lawyer") ? "변호사" : "수의사";
                    addMessageToChat("bot", expertTitle + " 목록을 불러올 수 없습니다. 다시 시도해주세요.");
                    currentBookingState = BookingState.NONE;
                }
            }

            @Override
            public void onFailure(Call<List<Lawyer>> call, Throwable t) {
                addMessageToChat("bot", "네트워크 오류가 발생했습니다. 다시 시도해주세요.");
                currentBookingState = BookingState.NONE;
            }
        });
    }

    private void showExpertList() {
        String expertTitle = expertType.equals("Lawyer") ? "변호사" : "수의사";
        StringBuilder message = new StringBuilder("상담 가능한 " + expertTitle + " 목록입니다. 번호를 선택해주세요:\n\n");
        for (int i = 0; i < experts.size(); i++) {
            message.append(i + 1).append(". ")
                    .append(experts.get(i).getName())
                    .append("\n");
        }
        addMessageToChat("bot", message.toString());
    }

    private void processBookingInput(String userInput) {
        addMessageToChat("user", userInput);

        if (userInput.equals("취소")) {
            cancelBooking();
            return;
        }

        switch (currentBookingState) {
            case SELECTING_EXPERT:
                processExpertSelection(userInput);
                break;
            case SELECTING_DATE:
                showDatePicker();
                break;
            case SELECTING_TIME:
                processTimeSelection(userInput);
                break;
            case ENTERING_DESCRIPTION:
                processDescription(userInput);
                break;
            case CONFIRMING:
                finalizeBooking(userInput);
                break;
        }
    }

    private void processExpertSelection(String userInput) {
        try {
            int selection = Integer.parseInt(userInput) - 1;
            if (selection >= 0 && selection < experts.size()) {
                selectedExpert = experts.get(selection);
                String expertTitle = expertType.equals("Lawyer") ? "변호사" : "수의사";
                addMessageToChat("bot", selectedExpert.getName() + " " + expertTitle + "를 선택하셨습니다.");
                currentBookingState = BookingState.SELECTING_DATE;
                showDatePicker();
            } else {
                addMessageToChat("bot", "올바른 번호를 선택해주세요.");
            }
        } catch (NumberFormatException e) {
            addMessageToChat("bot", "올바른 번호를 입력해주세요.");
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                    addMessageToChat("bot", "선택하신 날짜는 " + selectedDate + " 입니다.");
                    currentBookingState = BookingState.SELECTING_TIME;
                    showTimeOptions();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimeOptions() {
        String[] timeSlots = {"09:00", "10:00", "11:00", "14:00", "15:00","16:00","17:00"};
        StringBuilder message = new StringBuilder("상담 가능한 시간입니다. 번호를 선택해주세요:\n\n");
        for (int i = 0; i < timeSlots.length; i++) {
            message.append(i + 1).append(". ").append(timeSlots[i]).append("\n");
        }
        addMessageToChat("bot", message.toString());
    }

    private void processTimeSelection(String userInput) {
        String[] timeSlots = {"09:00", "10:00", "11:00", "14:00", "15:00","16:00","17:00"};
        try {
            int selection = Integer.parseInt(userInput) - 1;
            if (selection >= 0 && selection < timeSlots.length) {
                selectedTime = timeSlots[selection];
                addMessageToChat("bot", "선택하신 시간은 " + selectedTime + " 입니다.\n\n상담 사유를 입력해주세요.");
                currentBookingState = BookingState.ENTERING_DESCRIPTION;
            } else {
                addMessageToChat("bot", "올바른 번호를 선택해주세요.");
            }
        } catch (NumberFormatException e) {
            addMessageToChat("bot", "올바른 번호를 입력해주세요.");
        }
    }

    private void processDescription(String userInput) {
        description = userInput;
        confirmBooking();
    }

    private void confirmBooking() {
        addMessageToChat("bot", "다음 예약 정보를 확인해주세요:");
        String summary = String.format("상담사: %s\n날짜: %s\n시간: %s\n사유: %s",
                selectedExpert.getName(), selectedDate, selectedTime, description);
        addMessageToChat("bot", summary);
        addMessageToChat("bot", "예약을 진행하려면 '확인', 취소하려면 '취소'를 입력해주세요.");
        currentBookingState = BookingState.CONFIRMING;
    }

    private void finalizeBooking(String userInput) {
        if ("확인".equalsIgnoreCase(userInput)) {
            submitReservation();
        } else if ("취소".equalsIgnoreCase(userInput)) {
            cancelBooking();
        } else {
            addMessageToChat("bot", "예약 진행을 위해 '확인' 또는 '취소'를 입력해주세요.");
        }
    }

    private void submitReservation() {
        if (expertType.equals("Lawyer")) {
            LawyerChatRequest request = new LawyerChatRequest(
                    selectedExpert.getExpertCode(),
                    selectedDate,
                    selectedTime,
                    description
            );

            apiService.submitLawyerChatReservation(getJwtToken(), request).enqueue(new Callback<ReservationResponse>() {
                @Override
                public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                    handleReservationResponse(response);
                }

                @Override
                public void onFailure(Call<ReservationResponse> call, Throwable t) {
                    handleReservationFailure(t);
                }
            });
        } else {
            VetReservationRequest request = new VetReservationRequest(
                    selectedDate,
                    selectedTime,
                    description,
                    "410160010630474",    // pet 파라미터 추가
                    selectedExpert.getExpertCode()
            );

            apiService.submitVetReservation(getJwtToken(), request).enqueue(new Callback<ReservationResponse>() {
                @Override
                public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                    handleReservationResponse(response);
                }

                @Override
                public void onFailure(Call<ReservationResponse> call, Throwable t) {
                    handleReservationFailure(t);
                }
            });
        }
    }

    private void handleReservationResponse(Response<ReservationResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            addMessageToChat("bot", "예약이 성공적으로 완료되었습니다.");
        } else {
            addMessageToChat("bot", "예약 처리 중 문제가 발생했습니다.");
        }
        clearBookingData();
    }

    private void handleReservationFailure(Throwable t) {
        addMessageToChat("bot", "예약 요청 실패: " + t.getMessage());
        clearBookingData();
    }

    private void cancelBooking() {
        addMessageToChat("bot", "예약이 취소되었습니다.");
        clearBookingData();
    }

    private void clearBookingData() {
        currentBookingState = BookingState.NONE;
        selectedExpert = null;
        selectedDate = "";
        selectedTime = "";
        description = "";
        expertType = "";
    }

    private void callChatGPTAPI(String message) {
        List<ChatGPTRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatGPTRequest.Message("user", message));
        ChatGPTRequest request = new ChatGPTRequest("gpt-3.5-turbo", messages, 1024);

        chatGPTService.sendMessage(request).enqueue(new Callback<ChatGPTResponse>() {
            @Override
            public void onResponse(Call<ChatGPTResponse> call, Response<ChatGPTResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String botMessage = response.body().getChoices().get(0).getMessage().getContent();
                    addMessageToChat("bot", botMessage);
                } else {
                    addMessageToChat("bot", "응답 실패: " + response.code() + " " + response.message());
                }
            }@Override
            public void onFailure(Call<ChatGPTResponse> call, Throwable t) {
                addMessageToChat("bot", "오류가 발생했습니다: " + t.getMessage());
            }
        });
    }

    private void addMessageToChat(String sender, String message) {
        View messageView = getLayoutInflater().inflate(R.layout.message_item, chatContainer, false);
        TextView botMessage = messageView.findViewById(R.id.botMessage);
        TextView userMessage = messageView.findViewById(R.id.userMessage);

        if ("bot".equals(sender)) {
            botMessage.setVisibility(View.VISIBLE);
            botMessage.setText(message);
            userMessage.setVisibility(View.GONE);
        } else {
            userMessage.setVisibility(View.VISIBLE);
            userMessage.setText(message);
            botMessage.setVisibility(View.GONE);
        }

        chatContainer.addView(messageView);
        chatScrollView.post(() -> chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private String getJwtToken() {
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }
}