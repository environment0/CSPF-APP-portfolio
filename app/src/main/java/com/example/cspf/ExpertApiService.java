package com.example.cspf;

import com.example.cspf.auth.LoginRequest;
import com.example.cspf.auth.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ExpertApiService {

    @POST("/auth/login/expert")
    Call<LoginResponse> loginExpert(@Body LoginRequest loginRequest);  // 전문가 전용 로그인
}