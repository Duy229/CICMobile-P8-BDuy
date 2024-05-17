package com.umut.soysal.util;

import static com.umut.soysal.util.LocalStorageUtil.writeClientId;
import static com.umut.soysal.util.LocalStorageUtil.writeClientName;
import static com.umut.soysal.util.LocalStorageUtil.writeToken;

import android.content.Context;

import androidx.annotation.NonNull;

import com.umut.soysal.api.ApiService;
import com.umut.soysal.modal.AuthClientModal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RefreshToken {

    private ApiService apiService;

    public static void refreshToken(Context context, ApiService apiService) {

        String account = LocalStorageUtil.readProfileUsername(context);
        String password = LocalStorageUtil.readTokenId(context);
        Boolean remember = true;

        AuthClientModal.RefreshTokenRequest request = new AuthClientModal.RefreshTokenRequest(account, password, remember);
        request.setAccount(account);
        request.setPassword(password);
        request.setRemember(true);

        Call<AuthClientModal.LoginResponse> refreshTokenCall = apiService.refreshToken(request);
        refreshTokenCall.enqueue(new Callback<AuthClientModal.LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthClientModal.LoginResponse> call, @NonNull Response<AuthClientModal.LoginResponse> response) {
                AuthClientModal.LoginResponse refreshTokenResponse = response.body();
                if (refreshTokenResponse != null && refreshTokenResponse.getStatus() == 200) {
                    writeToken(context, refreshTokenResponse.getResponse().getToken());
                    writeClientId(context, refreshTokenResponse.getResponse().getClientId());
                    writeClientName(context, refreshTokenResponse.getResponse().getClientName());
                } else {
                    PerformLogout performLogout = new PerformLogout(context, apiService);
                    performLogout.performLogout();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthClientModal.LoginResponse> call, @NonNull Throwable t) {
                PerformLogout performLogout = new PerformLogout(context, apiService);
                performLogout.performLogout();
            }
        });
    }
}

