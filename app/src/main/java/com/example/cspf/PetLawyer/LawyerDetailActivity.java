package com.example.cspf.PetLawyer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cspf.R;
import com.example.cspf.chatbot.ChatActivity;

public class LawyerDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lawyer_detail);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String phone = intent.getStringExtra("phone");
        String email = intent.getStringExtra("email");
        String company = intent.getStringExtra("company");
        String workexperience = intent.getStringExtra("workexperience");
        String companyaddr = intent.getStringExtra("companyaddr");
        String product = intent.getStringExtra("product");

        // 기존 TextView
        TextView nameTextView = findViewById(R.id.lawyerNameTextView);
        TextView phoneTextView = findViewById(R.id.lawyerPhoneTextView);
        TextView emailTextView = findViewById(R.id.lawyerEmailTextView);
        TextView companyTextView = findViewById(R.id.lawyerCompanyTextView);
        TextView experienceTextView = findViewById(R.id.lawyerExperienceTextView);
        TextView addressTextView = findViewById(R.id.lawyerAddressTextView);
        TextView productTextView = findViewById(R.id.lawyerProductTextView);

        // 값 설정
        nameTextView.setText(name);
        phoneTextView.setText(phone);
        emailTextView.setText(email);
        companyTextView.setText(company);
        experienceTextView.setText(workexperience);
        addressTextView.setText(companyaddr);
        productTextView.setText(product);

        // 챗봇으로 이동 버튼 설정
        Button btnChatbot = findViewById(R.id.btn_chatbot);
        btnChatbot.setOnClickListener(view -> {
            Intent chatbotIntent = new Intent(LawyerDetailActivity.this, ChatActivity.class);
            chatbotIntent.putExtra("name", name);
            chatbotIntent.putExtra("phone", phone);
            chatbotIntent.putExtra("email", email);
            chatbotIntent.putExtra("company", company);
            chatbotIntent.putExtra("workexperience", workexperience);
            chatbotIntent.putExtra("companyaddr", companyaddr);
            chatbotIntent.putExtra("product", product);
            startActivity(chatbotIntent);
        });
    }
}
