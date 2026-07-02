package com.example.cspf;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFetcher {

    private static final String TAG = "ProfileFetcher";

    private static Retrofit getRetrofitWithToken(String accessToken) {
        OkHttpClient client = UnsafeHttps.getUnsafeOkHttpClient()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Cookie", "authorization=" + accessToken) // "Bearer"가 없는 순수 토큰만 추가
                            .build();
                    return chain.proceed(request);
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl("https://hyproz.myds.me:3800/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public static void fetchProfile(Context context, SharedPreferences sharedPreferences) {
        String accessToken = sharedPreferences.getString("access_token", null);
        Log.d(TAG, "Access Token: " + accessToken);

        if (accessToken == null) {
            Log.e(TAG, "토큰이 없습니다. 로그인 해주세요.");
            return;
        }

        ApiService apiService = getRetrofitWithToken(accessToken).create(ApiService.class);

        Call<ProfileResponse> call = apiService.userProfile("authorization=" + accessToken);

        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse res = response.body();
                    Log.d(TAG, "Profile Data: " + res.toString());
                    sessionProfile(res, sharedPreferences);
                } else {
                    Log.e(TAG, "Failed to fetch profile data: " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error Body: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.e(TAG, "Error fetching profile data", t);
            }
        });
    }

    private static void sessionProfile(ProfileResponse res, SharedPreferences sharedPreferences) {
        Log.d(TAG, "Saving profile data to SharedPreferences: " + res.getNickname());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", res.getName());
        editor.putString("email", res.getEmail());
        editor.putString("addr", res.getAddr());
        editor.putString("phone", res.getPhone());
        editor.putString("username", res.getUsername());
        editor.putString("gender", res.getGender());
        editor.putString("nickname", res.getNickname());
        editor.apply();
    }
}
