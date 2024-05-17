package com.umut.soysal.api;

import com.umut.soysal.modal.AuthClientModal;
import com.umut.soysal.modal.InformationModal;
import com.umut.soysal.modal.ValidationModal;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/client")
    Call<AuthClientModal.LoginResponse> login(@Body AuthClientModal.LoginRequest request);

    @POST("/api/client/validation")
    Call<ValidationModal.ValidationResponse> validation(
            @Body ValidationModal.ValidationRequest request
    );

    @DELETE("/api/auth/logout")
    Call<AuthClientModal.LogoutResponse> logout();

    @GET("/api/client/information")
    Call<InformationModal> information();

    @GET("/api/public/server-information")
    Call<ResponseBody> getServerinformation();

    @POST("/api/auth/token")
    Call<AuthClientModal.LoginResponse> refreshToken(@Body AuthClientModal.RefreshTokenRequest refreshToken);

    @GET("/api/client/device-metadata")
    Call<ResponseBody> getDevices();

//    @GET("{url}/actuator/health")
//    Call<ResponseBody> getHealth(@Path("url") String url);

}
