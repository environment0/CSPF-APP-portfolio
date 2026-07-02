package com.example.cspf;

import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

public class UnsafeHttps {
    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // 신뢰하지 않은 모든 인증서를 수락하는 TrustManager
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // SSLContext를 "신뢰하지 않는" TrustManager로 초기화
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // SSL 소켓 팩토리 생성
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // OkHttpClient 빌더 생성
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);

            builder.protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS) // SSE 무제한
                    .writeTimeout(30, TimeUnit.SECONDS);

            // 모든 호스트 이름을 신뢰하도록 설정
            builder.hostnameVerifier((hostname, session) -> true);

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging); // 로그 인터셉터 추가

            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

