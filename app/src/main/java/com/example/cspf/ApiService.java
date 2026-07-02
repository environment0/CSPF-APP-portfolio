package com.example.cspf;

import com.example.cspf.Hospital.Hospital;
import com.example.cspf.News.NewsResponse;
import com.example.cspf.PetLawyer.Lawyer;
import com.example.cspf.auth.EmailRequest;
import com.example.cspf.auth.ExpertRequest;
import com.example.cspf.auth.LoginRequest;
import com.example.cspf.auth.LoginResponse;
import com.example.cspf.auth.SignupRequest;
import com.example.cspf.auth.VerificationRequest;
import com.example.cspf.chatbot.LawyerChatRequest;
import com.example.cspf.chatbot.ReservationResponse;
import com.example.cspf.chatbot.VetReservationRequest;
import com.example.cspf.chatroom.ReqChatRoom;
import com.example.cspf.inquiries.Comment;
import com.example.cspf.inquiries.Inquiry;
import com.example.cspf.inquiries.InquiryResponse;
import com.example.cspf.insurance.InsuranceConsultationRequest;
import com.example.cspf.notices.NoticeResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {



    @GET("/searchNews")
    Call<String> getNews(@Query("query") String query);  // 전체 응답을 문자열로 받음

    @POST("/scrip/news")
    Call<List<NewsResponse>> getArticleContent(@Body Map<String, String> body);

    @GET("/caseLaw")
    Call<String> getCaseLaw(@Query("query") String query);



    @POST("/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);


    @POST("/user")
    Call<Void> signup(@Body SignupRequest signupRequest);

    @Multipart
    @POST("/user/createImage")
    Call<ResponseBody> uploadProfileImage(
            @Part MultipartBody.Part img,
            @Header("Cookie") String cookie
    );

    @POST("/expert")
    Call<Void> signupExpert(@Body ExpertRequest expertRequest);

    @Multipart
    @POST("/images/cert")
    Call<ResponseBody> uploadCertificationImage(
            @Part MultipartBody.Part file
    );

    @POST("/auth/google")
    Call<LoginResponse> sendGoogleToken(@Body String googleTokenRequest);

    @GET("/user/profile")
    Call<ProfileResponse> userProfile(@Header("Cookie")String token);

    @Multipart
    @POST("/pet")  // 엔드포인트 확인 필요
    Call<Void> uploadPetImage(
            @Part MultipartBody.Part file,
            @Header("Cookie") String cookie  // Cookie 헤더 전달
    );

    @GET("/expert/type/{type}")
    Call<List<Hospital>> getHospitalsByType(@Path("type") String type);


    @GET("/mail/createSuccess")
    Call<Void> sendSuccessMail(@Query("userDto") String userDto);

    @POST("/mailauth/registerAuth")
    Call<Void> sendRegisterAuthCode(@Body EmailRequest emailRequest);

    @POST("/mailauth/mailVerify")
    Call<Void> verifyMailCode(
            @Header("Cookie") String emailToken,
            @Body VerificationRequest verificationRequest
    );


    // Inquiry APIs
    @GET("/questions")
//    Call<List<Inquiry>> getInquiries();
    Call<InquiryResponse> getInquiries();

    @GET("/questions/{boardId}")
    Call<Inquiry> getInquiryDetail(@Path("boardId") int boardId);

    // Notice APIs
    @GET("announcement/pages")
    Call<NoticeResponse> getNotices(
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Comment APIs
    @GET("/comment/{boardId}/comments")
    Call<List<Comment>> getComments(@Path("boardId") int boardId);

    @POST("/comment/{boardId}")
    Call<Void> createComment(@Path("boardId") int boardId,
                             @Header("Cookie") String token,
                             @Body Comment content);

    @PATCH("/comment/{commentId}")
    Call<Void> updateComment(
            @Path("commentId") int commentId,
            @Header("Cookie") String authorization,   // "Authorization" 헤더로 설정
            @Body Comment comment
    );

    // 댓글 삭제 메서드
    @DELETE("/comment/{commentId}")
    Call<Void> deleteComment(
            @Path("commentId") int commentId,
            @Header("Cookie") String authorization    // "Authorization" 헤더로 설정
    );

    @POST("/insurerchat")
    Call<Void> requestConsultation(
            @Header("Cookie") String token,
            @Body InsuranceConsultationRequest consultationRequest
    );



    @GET("expert/type/{type}")
    Call<List<Lawyer>> getLawyers(@Path("type") String type);

    @POST("/lawyerchat")
    Call<ReservationResponse> submitLawyerChatReservation(
            @Header("Cookie") String token,  // JWT 토큰을 Cookie 헤더로 전달
            @Body LawyerChatRequest lawyerChatRequest
    );

    @POST("/vetReservation")
    Call<ReservationResponse> submitVetReservation(
            @Header("Cookie") String token,
            @Body VetReservationRequest vetReservationRequest
    );

    @GET("/chatRoom/user/access")
//    Call<List<ChatRoom>> getChatRooms(@Header("Cookie") String token);
    Call<ReqChatRoom> getChatRooms(@Header("Cookie") String token);


    @Multipart
    @POST("/images/chat/")
    Call<ResponseBody> uploadImage(
            @Query("roomId") String roomId,
            @Part MultipartBody.Part files,
            @Header("Cookie") String accessToken
    );

}
