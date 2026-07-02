package com.example.cspf;

import android.content.Context;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://hyproz.myds.me:3800/"; // 실제 URL로 변경
    private static Retrofit retrofit = null;
    private static OkHttpClient client;
    private static ClearableCookieJar cookieJar;
    // 기본 Retrofit 인스턴스 반환

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

            client = UnsafeHttps.getUnsafeOkHttpClient().build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // OkHttpClient를 매개변수로 받아 커스텀 Retrofit 인스턴스 반환
    public static Retrofit getClient(Context context, OkHttpClient customClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(customClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // 기본 OkHttpClient 인스턴스 반환
    public static OkHttpClient getOkHttpClient(Context context) {
        if (client == null) {
            cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
            client = UnsafeHttps.getUnsafeOkHttpClient()
                    .cookieJar(cookieJar)
                    .build();
        }
        return client;
    }

    // 쿠키 삭제 메서드
    public static void clearCookies() {
        if (cookieJar != null) {
            cookieJar.clear();
        }
    }
}
